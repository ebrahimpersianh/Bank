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
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.db.LoanEntity

/**
 * یه‌بار در روز (پورت مفهومیِ «یادآوری سررسید» که تو www/index.html هنوز پیاده نشده - کاملاً
 * native-only) همه‌ی وام‌ها رو چک می‌کنه؛ برای هر قسط پرداخت‌نشده‌ای که سررسیدش (طبق dueDate واقعی
 * محاسبه‌شده تو LoanRepository.getRows) امروز یا فرداست، یه نوتیف جدا می‌ده. شناسه‌ی نوتیف از رو
 * (loanId, m) ساخته می‌شه تا اجراهای بعدی worker به‌جای تکرار، همون نوتیف رو جایگزین کنن.
 */
@HiltWorker
class DueDateReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val loanRepository: LoanRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val today = JalaliCalendar.today()
        loanRepository.getLoans().forEach { loan ->
            loanRepository.getRows(loan).forEach { row ->
                if (row["paid"] == true) return@forEach
                val due = row["dueDate"] as? Map<*, *> ?: return@forEach
                val y = (due["y"] as? Number)?.toInt() ?: return@forEach
                val mo = (due["m"] as? Number)?.toInt() ?: return@forEach
                val d = (due["d"] as? Number)?.toInt() ?: return@forEach
                val daysLeft = JalaliCalendar.daysBetween(today, PersianDate(y, mo, d))
                if (daysLeft !in 0..1) return@forEach
                val m = (row["m"] as? Number)?.toInt() ?: return@forEach
                notify(loan, m, daysLeft == 0)
            }
        }
        return Result.success()
    }

    private fun notify(loan: LoanEntity, m: Int, isToday: Boolean) {
        val whenLabel = if (isToday) "امروز" else "فردا"
        val notification = NotificationCompat.Builder(applicationContext, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("یادآوری قسط ${loan.name}")
            .setContentText("قسط شماره $m وام «${loan.name}» $whenLabel سررسید می‌شه")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        val notificationId = "${loan.id}_$m".hashCode()
        NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
    }
}
