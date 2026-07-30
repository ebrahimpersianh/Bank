package ir.sadteam.loancalc.notifications

import android.Manifest
import android.content.Context
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
import ir.sadteam.loancalc.R
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.parseReminderOffsets
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.ChequeRepository
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.db.ChequeEntity
import ir.sadteam.loancalc.data.db.LoanEntity
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
        return Result.success()
    }

    private fun dayLabel(daysLeft: Int): String = when (daysLeft) {
        0 -> "امروز"
        1 -> "فردا"
        else -> "${toFa(daysLeft.toString())} روز دیگه"
    }

    private fun notifyLoan(loan: LoanEntity, m: Int, daysLeft: Int, channelId: String) {
        val whenLabel = dayLabel(daysLeft)
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(ReminderChannels.largeIcon(applicationContext))
            .setContentTitle("یادآوری قسط ${loan.name}")
            .setContentText("قسط شماره $m وام «${loan.name}» $whenLabel سررسید می‌شه")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        val notificationId = "${loan.id}_$m".hashCode()
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
}
