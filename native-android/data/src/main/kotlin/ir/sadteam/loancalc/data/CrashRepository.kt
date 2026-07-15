package ir.sadteam.loancalc.data

import ir.sadteam.loancalc.data.network.ApiService
import ir.sadteam.loancalc.data.network.CrashReportRequest

/**
 * فقط برای این وجود داره که Retrofit (implementation-scoped تو :data) از classpath کامپایل
 * :app بیرون بمونه - crash/CrashReporter.kt تو :app مستقیم به ApiService.reportCrash دسترسی نداره
 * چون نوع برگشتیش (retrofit2.Response) از :app دیده نمی‌شه (همون باگی که LoanRepository/
 * AuthRepository هم به همین دلیل ساخته شدن). خطاهای شبکه عمداً قورت داده می‌شن - گزارش کرش نباید
 * خودش باعث یه کرش/تاخیر دیگه بشه.
 */
class CrashRepository(private val apiService: ApiService) {
    suspend fun reportCrash(message: String, stack: String?, context: String?, appVersion: String?) {
        try {
            apiService.reportCrash(CrashReportRequest(message, stack, context, appVersion))
        } catch (e: Exception) {
            // عمداً نادیده گرفته می‌شه
        }
    }
}
