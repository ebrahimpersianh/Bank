package ir.sadteam.loancalc.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
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

    // نامِ اختیاریِ کاربر - رجوع کن به AuthRepository.updateName. رشته‌ی خالی/null یعنی پاک‌کردن.
    @PUT("api/auth/name")
    suspend fun setName(
        @Header("Authorization") authHeader: String,
        @Body body: SetNameRequest,
    ): Response<Unit>

    // حذف کامل حساب (شماره + وام‌ها + پشتیبان‌های ابری چک/حساب سمت سرور) - الزامِ استانداردِ
    // فروشگاه‌های اپ برای هر اپی که ورود با شماره‌موبایل داره.
    @DELETE("api/auth/account")
    suspend fun deleteAccount(@Header("Authorization") authHeader: String): Response<Unit>

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

    // تاریخچه‌ی خریدهای اشتراک - سرور از نسخه‌ی مرداد ۱۴۰۵ هر خریدِ تاییدشده رو تو
    // subscription_purchases ثبت می‌کنه؛ خریدهای قبل از اون تو تاریخچه نیستن.
    @GET("api/subscription/history")
    suspend fun subscriptionHistory(
        @Header("Authorization") authHeader: String,
    ): SubscriptionHistoryResponse

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

    // آپدیتِ خودکار - عمومی، بدون نیاز به ورود؛ رجوع کن به server/routes/AppVersionRoutes.kt.
    @GET("api/app-version")
    suspend fun getAppVersion(): AppVersionResponse
}

data class RequestOtpRequest(val phone: String)

data class VerifyOtpRequest(val phone: String, val code: String)

data class VerifyOtpResponse(
    val token: String,
    val phone: String,
    val subscribed: Boolean,
    val subscribedUntil: String? = null,
    val subscriptionTier: String? = null,
    val trialDaysLeft: Int? = null,
)

/** [trialDaysLeft] چند روز از دوره‌ی آزمایشیِ ۷روزه‌ی رایگان مونده - عددِ نهایی، از قبل سمت سرور
 * (SubscriptionStatus.trialDaysLeft، کلید‌خورده به created_at شماره‌موبایل) با ساعتِ خودِ سرور
 * حساب شده، نه یه timestampِ خام که کلاینت بخواد با ساعتِ گوشی حسابش کنه. [subscribed] از قبل
 * ترکیبِ اشتراکِ واقعی + دوره‌ی آزمایشیِ فعاله - برای گیت «۱ وام رایگان» فقط همون کافیه. */
data class MeResponse(
    val phone: String,
    val subscribed: Boolean,
    val subscribedUntil: String?,
    val subscriptionTier: String? = null,
    val trialDaysLeft: Int? = null,
    /** true یعنی این شماره از قبل تو سرور بوده و ۱۵ روزِ هدیه‌ی اضافه گرفته - اپ جمله‌ی
     * «چون از قبل وارد برنامه شده بودی...» رو نشون می‌ده. */
    val legacyGift: Boolean = false,
    /** نامِ اختیاریِ کاربر؛ null کاملاً عادیه (هیچ‌وقت اجباری نیست). */
    val name: String? = null,
)

data class LoansResponse(val loans: List<Map<String, Any?>>, val updatedAt: String?)

data class PutLoansRequest(val loans: List<Map<String, Any?>>)

/** productId یکی از unlimited_loans_1m/3m/6m/1y — باید دقیقاً با پنل کافه‌بازار/مایکت و
 * TIER_DURATION_DAYS تو server/routes/SubscriptionRoutes.kt یکی باشه. store مشخص می‌کنه سرور
 * خرید رو با کدوم API (کافه‌بازار یا مایکت) تایید کنه؛ BuildConfig.FLAVOR از سمتِ app پاس داده
 * می‌شه (رجوع کن به AuthViewModel.verifySubscriptionPurchase). */
data class VerifySubscriptionRequest(val productId: String, val purchaseToken: String, val store: String)

data class VerifySubscriptionResponse(val ok: Boolean, val subscribed: Boolean, val subscribedUntil: String)

/** یه ردیفِ تاریخچه‌ی خرید. `createdAt` فرمتِ `yyyy-MM-dd HH:mm:ss` (خروجیِ datetime('now')ِ
 * SQLite) و `subscribedUntil` ISO-8601ه - رجوع کن به SubscriptionRoutes.kt سمتِ سرور. */
data class SubscriptionPurchaseDto(
    val productId: String,
    val tier: String?,
    val store: String,
    val durationDays: Int,
    val subscribedUntil: String,
    val createdAt: String,
)

data class SubscriptionHistoryResponse(val ok: Boolean, val items: List<SubscriptionPurchaseDto>)

data class SetNameRequest(val name: String?)

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

/** [latestVersionCode] با BuildConfig.VERSION_CODE مقایسه می‌شه - سرور بزرگ‌تر یعنی نسخه‌ی جدید
 * موجوده. [cafebazaarUrl]/[myketUrl] هرکدوم null باشن یعنی هنوز دستی رو سرور ست نشدن (رجوع کن به
 * کامنتِ AppVersionRoutes.kt برای دستورِ SQLِ آپدیت بعدِ هر انتشار). */
data class AppVersionResponse(
    val latestVersionCode: Int,
    val cafebazaarUrl: String?,
    val myketUrl: String?,
)
