package ir.sadteam.loancalc.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * قرارداد دقیق API طبق server/src/routes/{auth,loans,subscription,crash}.js — بدون هیچ حدسی،
 * مستقیم از رو کد فعلی بک‌اند استخراج شده. توضیح کامل هر مسیر (ارورها، وضعیت auth و ...) تو
 * server/README.md و کامنت‌های خودِ فایل‌های پوشه‌ی routes هست.
 *
 * جدول وام‌ها (loans) سمت سرور schema نداره (JSON مات)، برای همین اینجا هم فعلاً به‌صورت
 * List<Map<String, Any?>> نگه داشته می‌شه؛ مدل تایپ‌شده‌ی کامل (با rows/method/تاریخ‌ها) تو فاز ۱
 * جایگزین می‌شه.
 */
interface ApiService {
    @POST("api/auth/request-otp")
    suspend fun requestOtp(@Body body: RequestOtpRequest): Response<Unit>

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpRequest): VerifyOtpResponse

    @GET("api/auth/me")
    suspend fun me(@Header("Authorization") authHeader: String): MeResponse

    @GET("api/loans")
    suspend fun getLoans(@Header("Authorization") authHeader: String): LoansResponse

    @PUT("api/loans")
    suspend fun putLoans(
        @Header("Authorization") authHeader: String,
        @Body body: PutLoansRequest,
    ): Response<Unit>

    @POST("api/subscription/verify")
    suspend fun verifySubscription(
        @Header("Authorization") authHeader: String,
        @Body body: VerifySubscriptionRequest,
    ): VerifySubscriptionResponse

    @POST("api/crash")
    suspend fun reportCrash(@Body body: CrashReportRequest): Response<Unit>

    // پشتیبان‌گیری ابری چک‌ها/حساب‌ها (پورت مفهومی «پشتیبان‌گیری ابری از تمامی وام‌ها» تبلیغ‌شده تو
    // BenefitsScreen): برخلاف loans که سرور شکلش رو می‌دونه، این دوتا فقط یه blob مات از همون JSON
    // ای هستن که ChequeRepository/AccountRepository.exportBackupJson تولید می‌کنه.
    @GET("api/cheques")
    suspend fun getChequesBackup(@Header("Authorization") authHeader: String): BackupBlobResponse

    @PUT("api/cheques")
    suspend fun putChequesBackup(
        @Header("Authorization") authHeader: String,
        @Body body: BackupBlobRequest,
    ): Response<Unit>

    @GET("api/accounts")
    suspend fun getAccountsBackup(@Header("Authorization") authHeader: String): BackupBlobResponse

    @PUT("api/accounts")
    suspend fun putAccountsBackup(
        @Header("Authorization") authHeader: String,
        @Body body: BackupBlobRequest,
    ): Response<Unit>

    // نرخِ خدمات اعتباری (دیجی‌پی، اسنپ‌پی و ...) - عمومی، بدون نیاز به ورود؛ منبع حقیقتش سرور شد
    // بجای هاردکد تو خودِ اپ (رجوع کن به data/Banks.kt برای fallback آفلاین).
    @GET("api/credit-rates")
    suspend fun getCreditRates(): CreditRatesResponse
}

data class RequestOtpRequest(val phone: String)

data class VerifyOtpRequest(val phone: String, val code: String)

data class VerifyOtpResponse(val token: String, val phone: String, val subscribed: Boolean, val trialEndsAt: Long? = null)

/** [trialEndsAt] پایانِ دوره‌ی آزمایشیِ ۷روزه‌ی رایگان (میلی‌ثانیه‌ی epoch، سمت سرور کلید‌خورده به
 * created_at شماره‌موبایل - رجوع کن به server/src/subscriptionStatus.js). [subscribed] از قبل
 * ترکیبِ اشتراکِ واقعی + دوره‌ی آزمایشیِ فعاله - برای گیت «۱ وام رایگان» فقط همون کافیه. */
data class MeResponse(val phone: String, val subscribed: Boolean, val subscribedUntil: String?, val trialEndsAt: Long? = null)

data class LoansResponse(val loans: List<Map<String, Any?>>, val updatedAt: String?)

data class PutLoansRequest(val loans: List<Map<String, Any?>>)

/** productId یکی از unlimited_loans_1m/3m/6m/1y — باید دقیقاً با پنل کافه‌بازار و
 * TIER_DURATION_DAYS تو server/src/routes/subscription.js یکی باشه. */
data class VerifySubscriptionRequest(val productId: String, val purchaseToken: String)

data class VerifySubscriptionResponse(val ok: Boolean, val subscribed: Boolean, val subscribedUntil: String)

data class BackupBlobResponse(val data: String, val updatedAt: String?)

data class BackupBlobRequest(val data: String)

data class CreditRateDto(
    val key: String,
    val name: String,
    val colorHex: String,
    val logoAsset: String,
    val ratePct: Double,
    val months: Int,
    val minAmount: Long,
    val maxAmount: Long,
)

data class CreditRatesResponse(val rates: List<CreditRateDto>)

data class CrashReportRequest(
    val message: String,
    val stack: String? = null,
    val context: String? = null,
    val appVersion: String? = null,
)
