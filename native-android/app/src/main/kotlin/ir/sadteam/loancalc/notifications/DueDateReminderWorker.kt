package ir.sadteam.loancalc.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ir.sadteam.loancalc.MainActivity
import ir.sadteam.loancalc.R
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.parseReminderOffsets
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.ChequeRepository
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.db.ChequeEntity
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.data.db.RecurringPaymentEntity
import ir.sadteam.loancalc.data.prefs.UiPrefs
import kotlinx.coroutines.flow.first

/**
 * یه‌بار در روز همه‌ی وام‌ها/چک‌ها رو چک می‌کنه؛ برای هر قسط/چکِ پرداخت‌نشده‌ای که فاصله‌ش تا سررسید
 * (طبق dueDate واقعیِ محاسبه‌شده تو LoanRepository.getRows) با یکی از زمان‌بندی‌های یادآوریِ اون مورد
 * (اختصاصیِ خودش، یا اگه نداشت پیش‌فرضِ سراسری تو UiPrefs) یکی باشه، یه نوتیف جدا می‌ده. شناسه‌ی
 * نوتیف از رو (loanId, m) ساخته می‌شه تا اجراهای بعدی worker به‌جای تکرار، همون نوتیف رو جایگزین کنن.
 */
@HiltWorker
class DueDateReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val loanRepository: LoanRepository,
    private val chequeRepository: ChequeRepository,
    private val accountRepository: AccountRepository,
    private val uiPrefs: UiPrefs,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val defaultOffsets = parseReminderOffsets(uiPrefs.reminderDayOffsets.first())
        val soundUri = uiPrefs.reminderSoundUri.first()
        val vibrate = uiPrefs.reminderVibrate.first()
        val channelId = ReminderChannels.ensure(applicationContext, soundUri, vibrate)

        val today = JalaliCalendar.today()
        loanRepository.getLoans().forEach { loan ->
            val offsets = loan.reminderDayOffsets?.let { parseReminderOffsets(it) } ?: defaultOffsets
            if (offsets.isEmpty()) return@forEach
            loanRepository.getRows(loan).forEach { row ->
                if (row["paid"] == true) return@forEach
                val due = row["dueDate"] as? Map<*, *> ?: return@forEach
                val y = (due["y"] as? Number)?.toInt() ?: return@forEach
                val mo = (due["m"] as? Number)?.toInt() ?: return@forEach
                val d = (due["d"] as? Number)?.toInt() ?: return@forEach
                val daysLeft = JalaliCalendar.daysBetween(today, PersianDate(y, mo, d))
                if (daysLeft !in offsets) return@forEach
                val m = (row["m"] as? Number)?.toInt() ?: return@forEach
                notifyLoan(loan, m, daysLeft, channelId)
            }
        }

        // پورت «یادآوری هوشمند سررسید چک» رقیب - همون منطق، فقط رو چک‌های وضع‌نشده (بایگانی‌نشده)
        // به‌جای قسط وام.
        chequeRepository.getAllCheques()
            .filter { it.status == "PENDING" && !it.archived }
            .forEach { cheque ->
                val offsets = cheque.reminderDayOffsets?.let { parseReminderOffsets(it) } ?: defaultOffsets
                if (offsets.isEmpty()) return@forEach
                val daysLeft = JalaliCalendar.daysBetween(today, PersianDate(cheque.dueYear, cheque.dueMonth, cheque.dueDay))
                if (daysLeft in offsets) notifyCheque(cheque, daysLeft, channelId)
            }

        // یادآوریِ پرداخت‌های تکراریِ ماژولِ حسابداری (مثلِ اجاره) - رجوع کن به CLAUDE.md، بخشِ
        // «تغییرِ نامِ اپ + افزودنِ ماژولِ حسابداریِ شخصی». برخلافِ وام/چک که یه dueDateِ ثابت دارن،
        // این‌ها هر ماه تکرار می‌شن - نزدیک‌ترین وقوعِ بعدی (امروز یا آینده) حساب می‌شه.
        accountRepository.getRecurringPayments().forEach { payment ->
            val offsets = payment.reminderDayOffsets?.let { parseReminderOffsets(it) } ?: defaultOffsets
            if (offsets.isEmpty()) return@forEach
            val due = nextOccurrence(today, payment.dayOfMonth)
            val daysLeft = JalaliCalendar.daysBetween(today, due)
            if (daysLeft in offsets) notifyRecurringPayment(payment, daysLeft, channelId)
        }

        // یادآوریِ روزانه‌ی «دخل‌وخرج امروز یادت نره» (رجوع کن به CLAUDE.md، الهام از اپِ رفرنسِ
        // Poolaki) - این workerِ هر۲۴ساعته بدونِ زمانِ ثابتِ روزانه اجرا می‌شه (رجوع کن به
        // ReminderScheduler)، پس این یادآوری هم best-effort یه‌بار در روزه، نه دقیقاً عصر/شب. اگه
        // امروز هیچ تراکنشی (چه دخل چه خرج) تو هیچ حسابی ثبت نشده باشه، یه نوتیفِ ساده یادآوری می‌ده.
        if (uiPrefs.dailyExpenseReminderEnabled.first()) {
            val todayHasTransaction = accountRepository.observeTransactions().first()
                .any { it.year == today.y && it.month == today.m && it.day == today.d }
            if (!todayHasTransaction) {
                notifyDailyExpenseReminder(channelId)
            }
        }
        return Result.success()
    }

    /** نزدیک‌ترین تاریخی که [dayOfMonth] رخ می‌ده (امروز یا بعدش) - اگه امسال/همین‌ماه گذشته باشه
     * می‌ره ماهِ بعد. روزِ بزرگ‌تر از تعدادِ روزهای واقعیِ ماه (مثلاً ۳۱ تو ماهی که فقط ۳۰ روزه) به
     * آخرِ همون ماه clamp می‌شه - هم‌الگو با PersianCalendar.addMonths. */
    private fun nextOccurrence(today: PersianDate, dayOfMonth: Int): PersianDate {
        val clampedThisMonth = dayOfMonth.coerceAtMost(JalaliCalendar.daysInMonth(today.y, today.m))
        if (clampedThisMonth >= today.d) return PersianDate(today.y, today.m, clampedThisMonth)
        val nextM = if (today.m == 12) 1 else today.m + 1
        val nextY = if (today.m == 12) today.y + 1 else today.y
        val clampedNextMonth = dayOfMonth.coerceAtMost(JalaliCalendar.daysInMonth(nextY, nextM))
        return PersianDate(nextY, nextM, clampedNextMonth)
    }

    private fun dayLabel(daysLeft: Int): String = when (daysLeft) {
        0 -> "امروز"
        1 -> "فردا"
        else -> "${toFa(daysLeft.toString())} روز دیگه"
    }

    private fun notifyLoan(loan: LoanEntity, m: Int, daysLeft: Int, channelId: String) {
        val whenLabel = dayLabel(daysLeft)
        val notificationId = "${loan.id}_$m".hashCode()
        // زدنِ نوتیفیکیشن باید مستقیم همون وام رو باز کنه (مورد ۵ تو CLAUDE.md) - رجوع کن به
        // DeepLinkTarget/MainActivity.handleDeepLinkIntent. requestCode باید یکتا باشه وگرنه
        // extras یه PendingIntentِ قدیمی‌تر رو بازنویسی نمی‌کنن.
        val contentIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_LOAN_ID, loan.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(ReminderChannels.largeIcon(applicationContext))
            .setContentTitle("یادآوری قسط ${loan.name}")
            .setContentText("قسط شماره $m وام «${loan.name}» $whenLabel سررسید می‌شه")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
    }

    private fun notifyCheque(cheque: ChequeEntity, daysLeft: Int, channelId: String) {
        val whenLabel = dayLabel(daysLeft)
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(ReminderChannels.largeIcon(applicationContext))
            .setContentTitle("یادآوری سررسید چک")
            .setContentText("چک شماره ${cheque.chequeNumber} (${cheque.bankName}) $whenLabel سررسید می‌شه")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        val notificationId = "cheque_${cheque.id}".hashCode()
        NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
    }

    private fun notifyRecurringPayment(payment: RecurringPaymentEntity, daysLeft: Int, channelId: String) {
        val whenLabel = dayLabel(daysLeft)
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(ReminderChannels.largeIcon(applicationContext))
            .setContentTitle("یادآوریِ پرداختِ تکراری")
            .setContentText("«${payment.name}» (${fmt(payment.amount)} ریال) $whenLabel سررسید می‌شه")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        val notificationId = "recurring_${payment.id}".hashCode()
        NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
    }

    private fun notifyDailyExpenseReminder(channelId: String) {
        val contentIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            DAILY_EXPENSE_REMINDER_REQUEST_CODE,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(ReminderChannels.largeIcon(applicationContext))
            .setContentTitle("دخل‌وخرج امروز یادت نره")
            .setContentText("امروز هنوز هیچ تراکنشی ثبت نکردی - یه سر بزن به «حسابدار من»")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(DAILY_EXPENSE_REMINDER_NOTIFICATION_ID, notification)
    }

    private companion object {
        const val DAILY_EXPENSE_REMINDER_REQUEST_CODE = 990011
        const val DAILY_EXPENSE_REMINDER_NOTIFICATION_ID = 990011
    }
}
