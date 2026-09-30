package ir.sadteam.loancalc.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.PUT
import retrofit2.http.Path

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

    // 🎁 کدِ هدیه‌ی اشتراک (جایزه، یا هدیه‌ی گزارشِ باگ به پشتیبانی). سرور کد را
    // می‌سازد و یک‌بارمصرف نگه می‌دارد - رجوع کن به `server/routes/GiftCodeRoutes.kt`.
    // 🐞 گزارشِ مشکل. پاسخ یک **کدِ پیگیری** می‌دهد که هم به کاربر نشان داده می‌شود و
    // هم در ایمیل می‌رود؛ روی سرور به `user_id` بسته است تا بشود به همان حساب هدیه داد.
    @POST("api/support/report")
    suspend fun reportBug(
        @Header("Authorization") authHeader: String,
        @Body body: BugReportRequest,
    ): BugReportResponse

    // 🔒 پیوستِ پیامِ پشتیبانی: فقط عکس/فیلم؛ سرور نوع را از بایت‌ها می‌سنجد و عکس را بازسازی می‌کند.
    @POST("api/support/upload")
    suspend fun uploadSupportFile(
        @Header("Authorization") authHeader: String,
        @Body body: okhttp3.RequestBody,
    ): SupportUploadResponse

    @GET("api/admin/support")
    suspend fun adminSupport(@Header("Authorization") authHeader: String): AdminSupportResponse

    @GET("api/admin/support/file/{id}")
    suspend fun adminSupportFile(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): okhttp3.ResponseBody

    @POST("api/admin/support/reply")
    suspend fun adminSupportReply(
        @Header("Authorization") authHeader: String,
        @Body body: AdminSupportReplyRequest,
    ): Response<Unit>

    /** هدیه‌ی مستقیمِ ادمین به یک شماره‌ی کاربری: روزِ اشتراک و/یا سکه (۸ مهر). */
    @POST("api/admin/gift")
    suspend fun adminGift(
        @Header("Authorization") authHeader: String,
        @Body body: AdminGiftRequest,
    ): Response<Unit>

    @POST("api/admin/support/gift")
    suspend fun adminSupportGift(
        @Header("Authorization") authHeader: String,
        @Body body: AdminSupportGiftRequest,
    ): Response<Unit>

    @POST("api/admin/support/status")
    suspend fun adminSupportStatus(
        @Header("Authorization") authHeader: String,
        @Body body: AdminSupportStatusRequest,
    ): Response<Unit>

    @POST("api/gift/redeem")
    suspend fun redeemGiftCode(
        @Header("Authorization") authHeader: String,
        @Body body: RedeemGiftRequest,
    ): RedeemGiftResponse

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

    /** آمارِ بی‌نام - رجوع کن به [ir.sadteam.loancalc.data.UsageStats]. عمداً بدونِ توکن. */
    @POST("api/events")
    suspend fun trackEvent(@Body body: UsageEventRequest): Response<Unit>

    /** عکس‌های کاربر روی سرور (۷ مهر) - رجوع کن به `server/.../FileRoutes.kt` و [ir.sadteam.loancalc.data.PhotoSync]. */
    @GET("api/files")
    suspend fun listFiles(@Header("Authorization") authHeader: String): FileListResponse

    @PUT("api/files/{dir}/{name}")
    suspend fun putFile(
        @Header("Authorization") authHeader: String,
        @Path("dir") dir: String,
        @Path("name") name: String,
        @Body body: okhttp3.RequestBody,
    ): Response<Unit>

    @GET("api/files/{dir}/{name}")
    suspend fun getFile(
        @Header("Authorization") authHeader: String,
        @Path("dir") dir: String,
        @Path("name") name: String,
    ): okhttp3.ResponseBody

    @POST("api/events/batch")
    suspend fun trackBatch(@retrofit2.http.Header("Authorization") auth: String?, @Body body: UsageBatchRequest): Response<Unit>

    /** آیا این حساب صفحه‌ی «آمارِ جیبک» را می‌بیند (ورک‌فلوی make-admin). */
    @GET("api/admin/check")
    suspend fun adminCheck(@Header("Authorization") authHeader: String): AdminCheckResponse

    @GET("api/admin/stats")
    suspend fun adminStats(@Header("Authorization") authHeader: String): AdminStatsResponse

    @POST("api/crash")
    suspend fun reportCrash(
        @Body body: CrashReportRequest,
        // اختیاری: اگر کاربر وارد است، کرش به شماره‌ی کاربری‌اش وصل می‌شود (نه موبایل).
        @Header("Authorization") authHeader: String? = null,
    ): Response<Unit>

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

    /** «پیام‌های جیبک» - عمومی و بی‌ورود. رجوع کن به server/routes/AnnouncementRoutes.kt. */
    @GET("api/announcements")
    suspend fun getAnnouncements(
        @Query("since") since: Long = 0,
        // اختیاری: با توکن، پیام‌های اختصاصیِ همین کاربر هم می‌آیند.
        @Header("Authorization") authHeader: String? = null,
    ): AnnouncementsResponse

    // قیمتِ روزِ طلا/ارز/رمزارز - عمومی. کلیدِ APIِ سرویسِ بیرونی فقط رو سرورِ خودمونه و اپ
    // هیچ‌وقت مستقیم به اون سرویس وصل نمی‌شه. نگاشتِ نمادها هم سمتِ سروره، پس کلیدهای این
    // نگاشت دقیقاً همون symbolِ کاتالوگِ اپ‌ان (BTC/GOLD_18/...) و مقدارها **ریال**ن.
    @GET("api/prices")
    suspend fun getPrices(): PricesResponse

    // تاریخچه‌ی روزانه‌ی یه نماد - مبنای «نسبت به ماهِ قبل» تو تبِ دارایی.
    @GET("api/prices/history")
    suspend fun getPriceHistory(
        @Query("symbol") symbol: String,
        @Query("days") days: Int = 30,
    ): PriceHistoryResponse
}

data class RequestOtpRequest(val phone: String)

data class VerifyOtpRequest(val phone: String, val code: String, val deviceHash: String? = null)

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
    /** شماره‌ی کاربریِ یکتا (۳ مهر)؛ سرورِ قدیمی نمی‌فرستد → ۰. */
    val userId: Long = 0,
    val userCode: String? = null,
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

data class LoansResponse(val loans: List<Map<String, Any?>>, val updatedAt: String?, val revision: Long = 0)

data class PutLoansRequest(val loans: List<Map<String, Any?>>, val expectedRevision: Long? = null)

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

data class BugReportRequest(
    val message: String,
    val appVersion: String?,
    val device: String?,
    val attachments: List<String> = emptyList(),
    val category: String = "bug",
)

data class SupportUploadResponse(val ok: Boolean, val id: String, val kind: String)
data class SupportAttachment(val id: String, val kind: String)
data class SupportMessage(
    val id: Long,
    val ticket: String,
    val userId: Long?,
    val message: String,
    val appVersion: String?,
    val device: String?,
    val status: String,
    val createdAt: String,
    val attachments: List<SupportAttachment> = emptyList(),
    val category: String = "bug",
    val rewardedDays: Int = 0,
)
data class AdminSupportGiftRequest(val id: Long, val days: Int, val text: String)
data class AdminSupportResponse(val items: List<SupportMessage> = emptyList(), val openCount: Int = 0)
data class AdminSupportReplyRequest(val id: Long, val text: String, val close: Boolean = true)
data class AdminSupportStatusRequest(val id: Long, val status: String)

data class BugReportResponse(val ok: Boolean, val ticket: String)

data class RedeemGiftRequest(val code: String)

data class RedeemGiftResponse(val ok: Boolean, val days: Int, val subscribedUntil: String)

data class BackupBlobResponse(val data: String, val updatedAt: String?, val revision: Long = 0)

/** [expectedRevision] = نسخه‌ای که این گوشی آخرین بار از سرور دید. اگر سرور جلوتر باشد
 * (گوشیِ دیگری نوشته)، پاسخ ۴۰۹ است و نوشتن انجام نمی‌شود - رجوع کن به `server/BackupRoutes.kt`. */
data class BackupBlobRequest(val data: String, val expectedRevision: Long? = null)

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

data class PricesResponse(
    val updatedAt: String? = null,
    val prices: Map<String, Double> = emptyMap(),
    /** قیمتِ دلاریِ رمزارزها (کلید نمادِ کاتالوگ، مقدار دلار). سرورِ قدیمی خالی می‌دهد. */
    val pricesUsd: Map<String, Double> = emptyMap(),
)

data class PricePointDto(val date: String, val price: Double)

data class PriceHistoryResponse(
    val symbol: String,
    val points: List<PricePointDto> = emptyList(),
)

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
    /** «تغییراتِ این نسخه» - هر خط یک بند؛ null/خالی یعنی متنِ پیش‌فرض. */
    val changelog: String? = null,
)

/** یک اطلاعیه‌ی عمومی؛ [kind] یکی از update/outage/feature/info. [createdAt] زمانِ UTCِ سرور. */
data class AnnouncementDto(
    val id: Long,
    val title: String,
    val body: String,
    val kind: String,
    val createdAt: String,
    /** سکه‌ی هدیه‌ی ادمین (۰ = پیامِ معمولی). */
    val coins: Int = 0,
)

data class AdminGiftRequest(val user: String, val days: Int = 0, val coins: Int = 0, val text: String = "")

data class AnnouncementsResponse(val items: List<AnnouncementDto>)

data class UsageEventRequest(val name: String)

data class FileListResponse(val files: List<String>? = null)

data class UsageBatchEvent(val name: String, val count: Int, val day: String)

data class UsageBatchRequest(
    val installId: String,
    val appVersion: Int?,
    val store: String?,
    val sdk: Int,
    val loggedIn: Boolean,
    val events: List<UsageBatchEvent>,
    /** روزی یک بار: مشخصاتِ بی‌نامِ گوشی و برنامه (مدل، تنظیمات، تعدادِ موارد) - بدونِ هیچ مبلغ/متن/شماره. */
    val profile: Map<String, String>? = null,
)

data class AdminCheckResponse(val admin: Boolean = false)

data class AdminDayPoint(val day: String = "", val active: Int = 0, val new: Int = 0)

data class AdminNamedCount(val name: String = "", val count: Int = 0)

data class AdminFeatureUsage(val name: String = "", val users: Int = 0, val total: Int = 0)

data class AdminRetentionPoint(val afterDays: Int = 0, val base: Int = 0, val returned: Int = 0)

/** گزارشِ «آمارِ جیبک» - رجوع کن به `server/.../AdminRoutes.kt`. فهرست‌ها با Gson ممکن است null بیایند. */
data class AdminStatsResponse(
    val today: String = "",
    val totalInstalls: Int = 0,
    val activeToday: Int = 0,
    val active7: Int = 0,
    val active30: Int = 0,
    val new7: Int = 0,
    val new30: Int = 0,
    val loggedInActive30: Int = 0,
    val avgActiveDays30: Double = 0.0,
    val sessions7: Int = 0,
    val daily: List<AdminDayPoint>? = null,
    val retention: List<AdminRetentionPoint>? = null,
    val funnel: List<AdminNamedCount>? = null,
    val screens: List<AdminFeatureUsage>? = null,
    val actions: List<AdminFeatureUsage>? = null,
    val versions: List<AdminNamedCount>? = null,
    val stores: List<AdminNamedCount>? = null,
    val sdks: List<AdminNamedCount>? = null,
    val hours: List<AdminNamedCount>? = null,
    val weekdays: List<AdminNamedCount>? = null,
    val avgSessionMinutes: Double = 0.0,
    val totalMinutes30: Int = 0,
    val avgScreensPerSession: Double = 0.0,
    val screenTime: List<AdminFeatureUsage>? = null,
    val profiledInstalls: Int = 0,
    val profileSplits: List<AdminProfileSplit>? = null,
    val adoption: List<AdminAdoption>? = null,
    val installsList: List<AdminInstallRow>? = null,
    val sales: List<AdminSaleRow>? = null,
    val salesByStore: List<AdminNamedCount>? = null,
    val salesDaily: List<AdminNamedCount>? = null,
    val activeSubscribers: Int = 0,
    val activeByTier: List<AdminNamedCount>? = null,
    val giftsUsed30: Int = 0,
    val activeNow: Int = 0,
    val activeLastHour: Int = 0,
    val avgDau30: Double = 0.0,
    val cohorts: List<AdminCohortRow>? = null,
    val churned: Int = 0,
    val flows: List<AdminFeatureUsage>? = null,
    val errors: List<AdminFeatureUsage>? = null,
    val perf: List<AdminFeatureUsage>? = null,
    val crashes30: Int = 0,
    val nonFatal30: Int = 0,
    val multiAccountDevices: Int = 0,
    val trialBlockedUsers: Int = 0,
    val crashesByVersion: List<AdminNamedCount>? = null,
    val topCrashes: List<AdminNamedCount>? = null,
    val activeByVersion: List<AdminNamedCount>? = null,
)

data class AdminCohortRow(val weekStart: String = "", val size: Int = 0, val weeks: List<Int>? = null)

data class AdminSaleRow(
    val product: String = "",
    val count30: Int = 0,
    val countAll: Int = 0,
    val tomans30: Long = 0,
    val tomansAll: Long = 0,
)

data class AdminProfileSplit(val key: String = "", val values: List<AdminNamedCount>? = null)

data class AdminAdoption(val key: String = "", val installs: Int = 0, val avg: Double = 0.0)

data class AdminInstallRow(
    val id: String = "",
    val firstDay: String = "",
    val lastDay: String = "",
    val activeDays: Int = 0,
    val version: Int? = null,
    val store: String? = null,
    val device: String? = null,
    val android: String? = null,
    val sessions30: Int = 0,
    val minutes30: Int = 0,
    val topScreen: String? = null,
    val loggedIn: Boolean = false,
    /** شماره‌ی کاربریِ نمایشی (`Uid:…`) اگر این نصب با حساب وارد شده باشد. */
    val userCode: String? = null,
)
