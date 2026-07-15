package ir.sadteam.loancalc.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * زیرسیستم یادآوری سررسید (وب هنوز نداره - این کاملاً native-only هست): یه WorkManager
 * PeriodicWorkRequest هر ۲۴ ساعت [DueDateReminderWorker] رو اجرا می‌کنه. WorkManager خودش این
 * زمان‌بندی رو حتی بعد از ری‌استارت گوشی حفظ می‌کنه (بدون نیاز به BroadcastReceiver دستی برای
 * BOOT_COMPLETED)، برای همین AlarmManager خام استفاده نشده.
 */
@Singleton
class ReminderScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "یادآوری سررسید اقساط",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "یادآوری برای اقساطی که سررسیدشون امروز یا فرداست"
        }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    fun schedule() {
        ensureChannel()
        val request = PeriodicWorkRequestBuilder<DueDateReminderWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    companion object {
        const val CHANNEL_ID = "due_date_reminders"
        private const val WORK_NAME = "due_date_reminder_work"
    }
}
