package ir.sadteam.loancalc.crash

import android.util.Log
import ir.sadteam.loancalc.BuildConfig
import ir.sadteam.loancalc.data.network.ApiService
import ir.sadteam.loancalc.data.network.CrashReportRequest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * پورت reportCrash تو www/index.html (window.onerror/unhandledrejection → POST /api/crash، بدون
 * سرویس ثالث مثل Sentry/Crashlytics، حداکثر ۵ گزارش در هر بار باز شدن اپ). ApiService.reportCrash
 * از قبل تعریف شده بود اما هیچ‌جا صدا زده نمی‌شد - این کلاس واقعاً وصلش می‌کنه.
 *
 * معادل native دو قلاب جدای وب (onerror/unhandledrejection) یه Thread.UncaughtExceptionHandler
 * سراسریه: چون کوروتین‌های ساختاریافته (viewModelScope و ...) هم exception بدون catch رو نهایتاً
 * به همین handler می‌رسونن، یه قلاب برای هر دو حالت کافیه. گزارش با runBlocking (تایم‌اوت کوتاه)
 * قبل از این‌که handler قبلی (سیستم) پردازش رو واقعاً بکشه ارسال می‌شه، نه fire-and-forget که ممکنه
 * هیچ‌وقت واقعاً فرستاده نشه.
 */
@Singleton
class CrashReporter @Inject constructor(private val apiService: ApiService) {
    private var reportsSent = 0

    fun install() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            report(throwable, thread.name)
            previous?.uncaughtException(thread, throwable)
        }
    }

    private fun report(throwable: Throwable, threadName: String) {
        if (reportsSent >= 5) return
        reportsSent++
        try {
            runBlocking {
                withTimeoutOrNull(3000) {
                    apiService.reportCrash(
                        CrashReportRequest(
                            message = throwable.message ?: throwable.toString(),
                            stack = Log.getStackTraceString(throwable).take(8000),
                            context = threadName,
                            appVersion = BuildConfig.VERSION_NAME,
                        ),
                    )
                }
            }
        } catch (e: Exception) {
            // عمداً نادیده گرفته می‌شه - گزارش کرش نباید خودش باعث یه کرش/تاخیر دیگه بشه
        }
    }
}
