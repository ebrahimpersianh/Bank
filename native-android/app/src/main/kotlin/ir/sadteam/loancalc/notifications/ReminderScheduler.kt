package ir.sadteam.loancalc.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.sadteam.loancalc.data.prefs.UiPrefs
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/**
 * زمان‌بندِ یادآورِ سررسید - یک `PeriodicWorkRequest`ِ ۲۴ساعته که [DueDateReminderWorker] را
 * اجرا می‌کند. WorkManager خودش این زمان‌بندی را روی ری‌استارتِ گوشی نگه می‌دارد، برای همین
 * `AlarmManager`ِ خام استفاده نشده.
 *
 * 🚨 **چرا تاخیرِ اولیه لازم شد (گزارشِ واقعیِ کاربر):** «به‌محضِ اینکه برنامه را باز می‌کنم
 * همه با هم می‌آیند؛ می‌خواهم قبلش بیایند.»
 *
 * نسخه‌ی قبلی هیچ لنگری به ساعتِ روز نداشت: کار هر ۲۴ ساعت **از لحظه‌ی زمان‌بندی** اجرا
 * می‌شد. روی گوشی‌هایی که پس‌زمینه را می‌کشند (شیائومی و…) اجرای موعدرسیده عقب می‌افتاد تا
 * لحظه‌ای که پروسه‌ی اپ بالا بیاید - یعنی دقیقاً لحظه‌ی باز کردنِ برنامه، آن هم همه‌ی
 * سررسیدها یک‌جا. حالا اجرا به ساعتِ مشخصی از روز لنگر می‌خورد
 * ([UiPrefs.reminderHour]، پیش‌فرض ۹ صبح) و خودِ worker هم اجرای خیلی‌دیررسیده را
 * تشخیص می‌دهد و ساکت می‌ماند (رجوع کن به `isStaleCatchUpRun` آن‌جا).
 *
 * ⚠️ این تضمینِ کامل نیست و نمی‌تواند باشد: اگر «اجرای خودکار»ِ سازنده خاموش باشد، هیچ
 * زمان‌بندی‌ای اجرا نمی‌شود. مسیرش در صفحه‌ی «اجرا در پس‌زمینه»ی تنظیمات است.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val uiPrefs: UiPrefs,
) {
    /** نسخه‌ی suspend - ساعتِ دلخواهِ کاربر را می‌خواند. */
    suspend fun schedule() = scheduleAt(uiPrefs.reminderHour.first())

    /** وقتی خواندنِ تنظیمات ممکن/لازم نیست (مثلِ گیرنده‌ی بوت) با ساعتِ پیش‌فرض. */
    fun scheduleWithDefaultHour() = scheduleAt(UiPrefs.DEFAULT_REMINDER_HOUR)

    private fun scheduleAt(hour: Int) {
        val request = PeriodicWorkRequestBuilder<DueDateReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(millisUntilNext(hour), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context)
            // ⚠️ UPDATE نه KEEP: با KEEP زمان‌بندیِ بی‌لنگرِ نسخه‌ی قبلی روی گوشیِ کاربرانِ
            // فعلی برای همیشه می‌ماند و همین باگ هیچ‌وقت برایشان رفع نمی‌شد.
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private fun millisUntilNext(hour: Int): Long {
        val now = Calendar.getInstance()
        val target = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // اگر ساعتِ امروزش گذشته، فردا. بدونِ این، تاخیرِ منفی یعنی اجرای فوری - همان
            // رفتارِ باگ‌دار.
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }

    companion object {
        private const val WORK_NAME = "due_date_reminder_work"
    }
}
