package ir.sadteam.loancalc.notifications

import android.content.Context
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
 *
 * ساختِ کانالِ نوتیف دیگه اینجا نیست - چون صدا/ویبره حالا قابل‌شخصی‌سازیه، کانال به‌ازای هر ترکیبِ
 * تنظیمات پویا محاسبه/ساخته می‌شه (رجوع کن به [ReminderChannels])، نه یه کانالِ ثابتِ از پیش‌ساخته.
 */
@Singleton
class ReminderScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    fun schedule() {
        val request = PeriodicWorkRequestBuilder<DueDateReminderWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    companion object {
        private const val WORK_NAME = "due_date_reminder_work"
    }
}
