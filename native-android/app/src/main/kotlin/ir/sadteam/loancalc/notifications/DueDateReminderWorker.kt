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
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.rialToToman
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

        // اگه امروز اعلانِ «برگشت» زده شده، یادآورِ عادی ساکت می‌مونه - وگرنه کاربرِ غایب یه روز
        // دو تا اعلان می‌گرفت که آزاردهنده‌ست (رجوع کن به ComeBackWorker).
        val today0 = JalaliCalendar.today()
        if (uiPrefs.lastComeBackNotifiedAt.first() == "${today0.y}-${today0.m}-${today0.d}") {
            return Result.success()
        }

        // 🚨 **اجرای دیررسیده ساکت می‌ماند** - گزارشِ واقعیِ کاربر: «به‌محضِ باز کردنِ برنامه
        // همه با هم می‌آیند». روی گوشی‌هایی که پس‌زمینه را می‌کشند، اجرای موعدرسیده تا لحظه‌ی
        // بالا آمدنِ پروسه عقب می‌افتد - یعنی وسطِ کار کردنِ کاربر، نه سرِ ساعتِ یادآور. اگر
        // بیش از [STALE_RUN_HOURS] از ساعتِ مقرر گذشته باشد چیزی فرستاده نمی‌شود و کار به
        // اجرای فردا موکول می‌شود؛ سررسیدها روزهای بعد هم هنوز سررسیدند، پس چیزی گم نمی‌شود.
        if (isStaleCatchUpRun(uiPrefs.reminderHour.first())) return Result.success()

        val defaultOffsets = parseReminderOffsets(uiPrefs.reminderDayOffsets.first())
        // موردهایی که کاربر دیروز «فردا یادم بیاور» زده بود دوباره می‌آیند؛ آن‌هایی که
        // **امروز** تعویق خورده‌اند رد می‌شوند.
        val todayKey = "${today0.y}-${today0.m}-${today0.d}"
        val snoozedToday = uiPrefs.snoozedReminders.first()
            .filter { it.substringAfter('@', "") == todayKey }
            .map { it.substringBefore('@') }
            .toSet()
        // سه کانالِ ثابت جای کانالِ پویا - رجوع کن به کامنتِ ReminderChannels. صدا و ویبره
        // دیگر از این‌جا نمی‌آیند؛ کانال خودش داردشان.
        ReminderChannels.ensureAll(applicationContext)

        // حالتِ خصوصی **یک‌بار** خوانده می‌شود و به همه‌ی اعلان‌ها می‌رود. قبلاً فقط
        // notifyRecurringPayment می‌گرفتش، پس شماره‌ی چک و نامِ وام روی صفحه‌ی قفل می‌آمدند -
        // دقیقاً همان چیزی که این کلید جلوش را می‌گیرد.
        val privacyMode = uiPrefs.privacyModeEnabled.first()

        // برای گروه‌بندی: هر اعلان جدا می‌ماند ولی زیرِ یک سرِ مشترک جمع می‌شود، پس باید
        // بدانیم چند تا شد.
        var dueCount = 0

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
                // کلید دقیقاً همانی است که دکمه‌ی «فردا یادم بیاور» می‌فرستد.
                if ("loan_${loan.id}_$m" in snoozedToday) return@forEach
                notifyLoan(loan, m, daysLeft, privacyMode)
                dueCount++
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
                if (daysLeft in offsets && "cheque_${cheque.id}" !in snoozedToday) {
                    notifyCheque(cheque, daysLeft, privacyMode)
                    dueCount++
                }
            }

        // یادآوریِ پرداخت‌های تکراریِ ماژولِ حسابداری (مثلِ اجاره) - رجوع کن به CLAUDE.md، بخشِ
        // «تغییرِ نامِ اپ + افزودنِ ماژولِ حسابداریِ شخصی». برخلافِ وام/چک که یه dueDateِ ثابت دارن،
        // این‌ها هر ماه تکرار می‌شن - نزدیک‌ترین وقوعِ بعدی (امروز یا آینده) حساب می‌شه.
        accountRepository.getRecurringPayments().forEach { payment ->
            val offsets = payment.reminderDayOffsets?.let { parseReminderOffsets(it) } ?: defaultOffsets
            if (offsets.isEmpty()) return@forEach
            val due = nextOccurrence(today, payment.dayOfMonth)
            val daysLeft = JalaliCalendar.daysBetween(today, due)
            if (daysLeft in offsets && "recurring_${payment.id}" !in snoozedToday) {
                notifyRecurringPayment(payment, daysLeft, privacyMode)
                dueCount++
            }
        }

        // یادآوریِ روزانه‌ی «دخل‌وخرج امروز یادت نره» (رجوع کن به CLAUDE.md، الهام از اپِ رفرنسِ
        // Poolaki) - این workerِ هر۲۴ساعته بدونِ زمانِ ثابتِ روزانه اجرا می‌شه (رجوع کن به
        // ReminderScheduler)، پس این یادآوری هم best-effort یه‌بار در روزه، نه دقیقاً عصر/شب. اگه
        // امروز هیچ تراکنشی (چه دخل چه خرج) تو هیچ حسابی ثبت نشده باشه، یه نوتیفِ ساده یادآوری می‌ده.
        if (uiPrefs.dailyExpenseReminderEnabled.first()) {
            val todayHasTransaction = accountRepository.observeTransactions().first()
                .any { it.year == today.y && it.month == today.m && it.day == today.d }
            if (!todayHasTransaction) {
                notifyDailyExpenseReminder()
            }
        }

        // سرِ گروه فقط از دو مورد به بالا. با یک اعلان، سرِ گروه روی اندروید ۷ یک ردیفِ
        // تکراریِ اضافه می‌سازد و چیزی هم جمع نمی‌کند.
        if (dueCount >= 2) notifyGroupSummary(dueCount)
        return Result.success()
    }

    /**
     * سرِ مشترکِ گروه. سه قسطِ یک روز حالا زیرِ یک ردیفِ جمع‌شونده می‌نشینند، ولی هر کدام
     * اعلانِ خودش را دارد - پس کنشِ «پرداخت شد» برای هر مورد جدا می‌ماند.
     *
     * `setGroupSummary(true)` و `setGroup`ِ یکسان با بچه‌ها اجباری‌اند؛ بی سرِ گروه،
     * اندروید ۷+ خودش بعدِ چهار اعلان یک سرِ بی‌متن می‌سازد.
     */
    private fun notifyGroupSummary(count: Int) {
        val notification = NotificationCompat.Builder(applicationContext, ReminderChannels.CHANNEL_DUE_DATES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("${toFa(count.toString())} سررسیدِ نزدیک")
            .setContentText("برای دیدنِ همه باز کن")
            .setGroup(GROUP_DUE_DATES)
            .setGroupSummary(true)
            .setContentIntent(openAppIntent(GROUP_SUMMARY_NOTIFICATION_ID))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(GROUP_SUMMARY_NOTIFICATION_ID, notification)
    }

    /** بازکردنِ خودِ برنامه، بی مقصدِ خاص. */
    private fun openAppIntent(requestCode: Int): PendingIntent {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            applicationContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /** دکمه‌ی «فردا یادم بیاور» - روی هر یادآورِ سررسید. */
    private fun snoozeAction(notificationId: Int, snoozeKey: String): NotificationCompat.Action {
        val intent = Intent(applicationContext, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_SNOOZE_KEY, snoozeKey)
        }
        // requestCode یکتا اجباریه: با کدِ تکراری، extras یه PendingIntentِ قدیمی‌تر رو
        // بازنویسی نمی‌کنن و دکمه روی موردِ اشتباه عمل می‌کنه.
        val pending = PendingIntent.getBroadcast(
            applicationContext,
            "snooze_$snoozeKey".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Action.Builder(0, "فردا یادم بیاور", pending).build()
    }

    /** دکمه‌ی «پرداخت شد» - قسطِ وام یا چک. */
    private fun markPaidAction(
        notificationId: Int,
        loanId: Long? = null,
        installmentNumber: Int? = null,
        chequeId: Long? = null,
    ): NotificationCompat.Action {
        val intent = Intent(applicationContext, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_MARK_PAID
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            loanId?.let { putExtra(NotificationActionReceiver.EXTRA_LOAN_ID, it) }
            installmentNumber?.let { putExtra(NotificationActionReceiver.EXTRA_INSTALLMENT, it) }
            chequeId?.let { putExtra(NotificationActionReceiver.EXTRA_CHEQUE_ID, it) }
        }
        val pending = PendingIntent.getBroadcast(
            applicationContext,
            "paid_$notificationId".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Action.Builder(0, "پرداخت شد", pending).build()
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

    private fun notifyLoan(loan: LoanEntity, m: Int, daysLeft: Int, privacyMode: Boolean) {
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
        val notification = NotificationCompat.Builder(applicationContext, ReminderChannels.CHANNEL_DUE_DATES)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(ReminderChannels.largeIcon(applicationContext))
            // در حالتِ خصوصی نامِ وام هم نمی‌آید؛ «وامِ مسکنِ ۱۲ میلیونی» روی صفحه‌ی قفل
            // همان‌قدر افشاست که مبلغ.
            .setContentTitle(if (privacyMode) "یادآوریِ قسط" else "یادآوری قسط ${loan.name}")
            .setContentText(
                if (privacyMode) {
                    "قسط شماره ${toFa(m)} $whenLabel سررسید می‌شه"
                } else {
                    "قسط شماره ${toFa(m)} وام «${loan.name}» $whenLabel سررسید می‌شه"
                },
            )
            .setContentIntent(pendingIntent)
            .addAction(markPaidAction(notificationId, loanId = loan.id, installmentNumber = m))
            .addAction(snoozeAction(notificationId, "loan_${loan.id}_$m"))
            .setGroup(GROUP_DUE_DATES)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
    }

    private fun notifyCheque(cheque: ChequeEntity, daysLeft: Int, privacyMode: Boolean) {
        val whenLabel = dayLabel(daysLeft)
        val notificationId = "cheque_${cheque.id}".hashCode()
        // 🚨 این اعلان `setContentIntent` **نداشت**، پس تپ روش هیچ کاری نمی‌کرد - نه برنامه
        // را باز می‌کرد و نه بسته می‌شد (`setAutoCancel` بی contentIntent بی‌اثر است).
        // دیپ‌لینک فقط برای وام نوشته شده بود.
        val contentIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_CHEQUE_ID, cheque.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(applicationContext, ReminderChannels.CHANNEL_DUE_DATES)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(ReminderChannels.largeIcon(applicationContext))
            .setContentTitle("یادآوری سررسید چک")
            // شماره‌ی چک و نامِ بانک در حالتِ خصوصی نمی‌آیند. شماره‌ی چک استثنای ارقام است
            // (لاتین می‌ماند) ولی این‌جا اصلاً نشان داده نمی‌شود.
            .setContentText(
                if (privacyMode) {
                    "یک چک $whenLabel سررسید می‌شه"
                } else {
                    "چک شماره ${cheque.chequeNumber} (${cheque.bankName}) $whenLabel سررسید می‌شه"
                },
            )
            .setContentIntent(pendingIntent)
            .addAction(markPaidAction(notificationId, chequeId = cheque.id))
            .addAction(snoozeAction(notificationId, "cheque_${cheque.id}"))
            .setGroup(GROUP_DUE_DATES)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
    }

    /**
     * ⚠️ **حالتِ خصوصی رو اعلان‌ها هم اثر می‌ذاره** (تصمیمِ تاییدشده‌ی طراح): اعلانِ
     * «۳۲۰٬۰۰۰ ریال» رو صفحه‌ی قفلِ گوشی دقیقاً همون چیزیه که این کلید می‌خواد جلوشو بگیره.
     * پس وقتی روشنه مبلغ از متنِ اعلان حذف می‌شه - نه یه کلیدِ دومِ جدا.
     */
    private fun notifyRecurringPayment(
        payment: RecurringPaymentEntity,
        daysLeft: Int,
        privacyMode: Boolean,
    ) {
        val whenLabel = dayLabel(daysLeft)
        val notificationId = "recurring_${payment.id}".hashCode()
        // این هم `setContentIntent` نداشت - همان باگِ اعلانِ چک.
        val notification = NotificationCompat.Builder(applicationContext, ReminderChannels.CHANNEL_DUE_DATES)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(ReminderChannels.largeIcon(applicationContext))
            .setContentTitle("یادآوریِ پرداختِ تکراری")
            .setContentText(
                if (privacyMode) {
                    "«${payment.name}» $whenLabel سررسید می‌شه"
                } else {
                    // واحد **تومان** و رقمِ فارسی (بندِ ۲ی README + لایه‌ی ارقام). ستون ریال
                    // است پس تبدیل همین لبه.
                    "«${payment.name}» (${fmt(rialToToman(payment.amount.toLong()).toDouble()).faDigits()} تومان) " +
                        "$whenLabel سررسید می‌شه"
                },
            )
            .setContentIntent(openAppIntent(notificationId))
            .addAction(snoozeAction(notificationId, "recurring_${payment.id}"))
            .setGroup(GROUP_DUE_DATES)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
    }

    private fun notifyDailyExpenseReminder() {
        val pendingIntent = openAppIntent(DAILY_EXPENSE_REMINDER_REQUEST_CODE)
        // کانالِ انگیزشیِ کم‌اهمیت، نه کانالِ سررسید - این یکی نباید صدا کند و کاربر باید
        // بتواند جدا خاموشش کند بی این‌که یادآورِ قسط را از دست بدهد.
        val notification = NotificationCompat.Builder(applicationContext, ReminderChannels.CHANNEL_NUDGES)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(ReminderChannels.largeIcon(applicationContext))
            .setContentTitle("دخل‌وخرج امروز یادت نره")
            .setContentText("امروز هنوز هیچ تراکنشی ثبت نکردی - یه سر بزن به «جیبک»")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(DAILY_EXPENSE_REMINDER_NOTIFICATION_ID, notification)
    }

    private companion object {
        const val DAILY_EXPENSE_REMINDER_REQUEST_CODE = 990011
        const val DAILY_EXPENSE_REMINDER_NOTIFICATION_ID = 990011

        /** کلیدِ گروهِ سررسیدها. همه‌ی بچه‌ها و سرِ گروه باید همین را داشته باشند. */
        const val GROUP_DUE_DATES = "ir.sadteam.loancalc.group.DUE_DATES"
        const val GROUP_SUMMARY_NOTIFICATION_ID = 990012

        /** پنجره‌ی تحملِ دیرکردِ اجرا - رجوع کن به isStaleCatchUpRun. */
        const val STALE_RUN_HOURS = 6
    }

    /**
     * آیا این اجرا خیلی دیرتر از ساعتِ مقررِ یادآور است؟
     *
     * پنجره‌ی [STALE_RUN_HOURS]ساعته عمدی است: اجرایی که چند دقیقه/یکی‌دو ساعت دیر شده هنوز
     * «صبح» است و باید بفرستد؛ اجرایی که شش ساعت دیر شده یعنی گوشی جلویش را گرفته بود و
     * حالا دارد سرِ فرصت خالی می‌شود.
     */
    private fun isStaleCatchUpRun(reminderHour: Int): Boolean {
        val now = java.util.Calendar.getInstance()
        val hoursSinceTarget = now.get(java.util.Calendar.HOUR_OF_DAY) - reminderHour
        return hoursSinceTarget > STALE_RUN_HOURS
    }
}
