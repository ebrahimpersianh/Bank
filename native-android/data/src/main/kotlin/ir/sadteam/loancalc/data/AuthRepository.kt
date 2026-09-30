package ir.sadteam.loancalc.data

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ir.sadteam.loancalc.data.network.ApiService
import ir.sadteam.loancalc.data.network.RequestOtpRequest
import ir.sadteam.loancalc.data.network.SetNameRequest
import ir.sadteam.loancalc.data.network.SubscriptionPurchaseDto
import ir.sadteam.loancalc.data.network.BugReportRequest
import ir.sadteam.loancalc.data.network.RedeemGiftRequest
import ir.sadteam.loancalc.data.network.VerifySubscriptionRequest
import ir.sadteam.loancalc.data.network.VerifyOtpRequest
import ir.sadteam.loancalc.data.prefs.AuthPrefs
import kotlinx.coroutines.flow.first
import retrofit2.HttpException

/** نتیجه‌ی درخواست‌های OTP - [Error.code] دقیقاً همون رشته‌ی error سرور (مثلاً "wrong_code") رو
 * برمی‌گردونه تا لایه‌ی UI (نه اینجا) پیام فارسی متناظرش رو نشون بده، مثل msgs تو www/index.html. */
sealed class AuthResult {
    data object Success : AuthResult()
    data class Error(val code: String?) : AuthResult()
}

/**
 * پورت sendPhoneOtp/confirmPhoneOtp تو www/index.html. عمداً تو :data زندگی می‌کنه (نه :app) تا
 * Retrofit/Gson فقط همین‌جا لازم باشن - دقیقاً مثل LoanRepository برای Room.
 */
class AuthRepository(
    private val apiService: ApiService,
    private val authPrefs: AuthPrefs,
    /** کدِ یک‌طرفه‌ی شناسه‌ی گوشی - «ماهِ مجانی یک بار برای هر گوشی» (سرور). */
    private val deviceHash: () -> String? = { null },
) {
    private val gson = Gson()

    suspend fun requestOtp(phone: String): AuthResult {
        return try {
            val response = apiService.requestOtp(RequestOtpRequest(phone))
            if (response.isSuccessful) {
                AuthResult.Success
            } else {
                AuthResult.Error(errorCodeFrom(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            AuthResult.Error(null)
        }
    }

    suspend fun verifyOtp(phone: String, code: String): AuthResult {
        return try {
            val result = apiService.verifyOtp(VerifyOtpRequest(phone, code, runCatching { deviceHash() }.getOrNull()))
            authPrefs.saveSession(
                result.token,
                result.phone,
                result.subscribed,
                result.trialDaysLeft,
                result.subscribedUntil,
                result.subscriptionTier,
            )
            AuthResult.Success
        } catch (e: HttpException) {
            AuthResult.Error(errorCodeFrom(e.response()?.errorBody()?.string()))
        } catch (e: Exception) {
            AuthResult.Error(null)
        }
    }

    /** آیا حسابِ واردشده صفحه‌ی «آمارِ جیبک» را می‌بیند - بی‌اینترنت/واردنشده = نه. */
    suspend fun isAdmin(): Boolean {
        val token = authPrefs.authToken.first() ?: return false
        return runCatching { apiService.adminCheck("Bearer $token").admin }.getOrDefault(false)
    }

    /** گزارشِ کاملِ آمار (فقط ادمین)؛ `null` یعنی نشد (شبکه یا دسترسی). */
    suspend fun adminDigest(period: String, store: String? = null): ir.sadteam.loancalc.data.network.AdminDigestResponse? {
        val token = authPrefs.authToken.first() ?: return null
        return runCatching { apiService.adminDigest("Bearer $token", period, store) }.getOrNull()
    }

    suspend fun adminMoney(): ir.sadteam.loancalc.data.network.AdminMoneyResponse? {
        val token = authPrefs.authToken.first() ?: return null
        return runCatching { apiService.adminMoney("Bearer $token") }.getOrNull()
    }

    suspend fun adminUser(code: String): ir.sadteam.loancalc.data.network.AdminUserTimeline? {
        val token = authPrefs.authToken.first() ?: return null
        return runCatching { apiService.adminUser("Bearer $token", code) }.getOrNull()
    }

    suspend fun adminBroadcast(segment: String, title: String, body: String, dryRun: Boolean): ir.sadteam.loancalc.data.network.AdminBroadcastResult? {
        val token = authPrefs.authToken.first() ?: return null
        return runCatching {
            apiService.adminBroadcast("Bearer $token", ir.sadteam.loancalc.data.network.AdminBroadcastRequest(segment, title, body, dryRun))
        }.getOrNull()
    }

    /** JSONِ خامِ یک کلیدِ تنظیمِ از-راه-دور؛ `null` = نرسید (صدازننده نسخه‌ی ذخیره‌شده را نگه دارد). */
    suspend fun remoteConfig(key: String): String? =
        runCatching { apiService.getRemoteConfig(key).string() }.getOrNull()

    suspend fun sendSurvey(id: String, answer: String): Boolean = runCatching {
        apiService.postSurvey(ir.sadteam.loancalc.data.network.SurveyAnswerRequest(id, answer, UsageStats.publicInstallId())); true
    }.getOrDefault(false)

    suspend fun adminSurvey(id: String): List<ir.sadteam.loancalc.data.network.AdminNamedCount>? {
        val token = authPrefs.authToken.first() ?: return null
        return runCatching { apiService.adminSurvey("Bearer $token", id) }.getOrNull()
    }

    suspend fun adminAppVersion(): ir.sadteam.loancalc.data.network.AdminAppVersion? {
        val token = authPrefs.authToken.first() ?: return null
        return runCatching { apiService.adminAppVersion("Bearer $token") }.getOrNull()
    }

    suspend fun adminSetAppVersion(code: Int, changelog: String): Boolean {
        val token = authPrefs.authToken.first() ?: return false
        return runCatching { apiService.adminSetAppVersion("Bearer $token", ir.sadteam.loancalc.data.network.AdminAppVersion(code, changelog)); true }.getOrDefault(false)
    }

    suspend fun adminSetRemoteConfig(key: String, json: String): Boolean {
        val token = authPrefs.authToken.first() ?: return false
        return runCatching {
            apiService.adminSetRemoteConfig(
                "Bearer $token", key,
                json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()),
            )
            true
        }.getOrDefault(false)
    }

    suspend fun adminStats(): ir.sadteam.loancalc.data.network.AdminStatsResponse? {
        val token = authPrefs.authToken.first() ?: return null
        return runCatching { apiService.adminStats("Bearer $token") }.getOrNull()
    }

    /** پورت «refreshSubscriptionStatus» که تو کامنتِ قبلیِ AuthViewModel «فاز بعد» علامت خورده بود -
     * چون قبلاً هیچ‌جا GET /api/auth/me واقعاً صدا زده نمی‌شد، وضعیتِ اشتراک/دوره‌ی آزمایشیِ محلی
     * می‌تونست کهنه بمونه (مثلاً دقیقاً روز هشتم که آزمایشی تموم می‌شه، بدون خروج/ورودِ دوباره تا
     * مدت‌ها اپ فکر می‌کرد هنوز مشترکه). حالا از AppRoot هر بار اپ باز می‌شه صدا زده می‌شه. */
    suspend fun refreshSubscriptionStatus() {
        val token = authPrefs.authToken.first() ?: return
        try {
            val result = apiService.me("Bearer $token")
            authPrefs.setSubscribed(result.subscribed)
            authPrefs.setTrialDaysLeft(result.trialDaysLeft)
            authPrefs.setSubscribedUntil(result.subscribedUntil)
            authPrefs.setSubscriptionTier(result.subscriptionTier)
            // 🚨 سرور تا امروز `name` را در `/me` نمی‌فرستاد و این خط اسمِ ذخیره‌شده را هر بار
            // پاک می‌کرد (گزارشِ کاربر: «چند بار اسمم رو ذخیره کردم ولی سیو نشد»). اگر سرور
            // خالی داد ولی گوشی اسم دارد، اسمِ گوشی را دوباره به سرور می‌فرستیم.
            val localName = authPrefs.userName.first()
            if (result.name.isNullOrBlank() && !localName.isNullOrBlank()) {
                runCatching { apiService.setName("Bearer $token", SetNameRequest(localName)) }
            } else {
                authPrefs.setUserName(result.name)
            }
            authPrefs.setUserId(result.userId)
            authPrefs.setUserCode(result.userCode)
            authPrefs.setLegacyGift(result.legacyGift)
        } catch (e: Exception) {
            // بی‌صدا نادیده گرفته می‌شه - این فقط یه تازه‌سازیِ پس‌زمینه‌ست؛ اگه شکست بخوره (مثلاً
            // بی‌اینترنتی)، مقدارِ محلیِ قبلی همچنان معتبر می‌مونه تا دفعه‌ی بعد.
        }
    }

    /** پورت verifySubscriptionPurchase تو www/index.html: به سرور می‌گه یه خریدِ Poolakey/IabHelper
     * واقعیه (کلاینت خودش قابل‌اعتماد نیست)، و اگه تایید شد subscribed رو تو AuthPrefs به‌روز
     * می‌کنه. store مشخص می‌کنه سرور خرید رو با API کدوم استور تایید کنه. */
    suspend fun verifySubscription(productId: String, purchaseToken: String, store: String): AuthResult {
        val token = authPrefs.authToken.first()
        if (token.isNullOrEmpty()) return AuthResult.Error(null)
        return try {
            val result = apiService.verifySubscription(
                "Bearer $token",
                VerifySubscriptionRequest(productId, purchaseToken, store),
            )
            authPrefs.setSubscribed(result.subscribed)
            AuthResult.Success
        } catch (e: HttpException) {
            AuthResult.Error(errorCodeFrom(e.response()?.errorBody()?.string()))
        } catch (e: Exception) {
            AuthResult.Error(null)
        }
    }

    /**
     * 🐞 ثبتِ گزارشِ مشکل روی سرور. خروجی **کدِ پیگیری** است، یا `null` اگر نشد.
     *
     * ⚠️ فقط برای کاربرِ واردشده: بی `user_id` گزارش به هیچ حسابی بسته نمی‌شود و
     * دادنِ هدیه ممکن نیست - همان دلیلی که این کار را سمتِ سرور می‌بَرَد.
     */
    /**
     * [onProgress] از ۰ تا ۱ - سهمِ بایت‌های فرستاده‌شده‌ی همه‌ی پیوست‌ها (برای نوارِ آپلود).
     * [onAttachmentFailed] تعدادِ پیوست‌هایی که بالا نرفتند - قبلاً بی‌صدا جا می‌ماندند.
     */
    suspend fun reportBug(
        message: String,
        appVersion: String?,
        device: String?,
        attachments: List<Pair<ByteArray, String>> = emptyList(),
        category: String = "bug",
        onProgress: (Float) -> Unit = {},
        onAttachmentFailed: (Int) -> Unit = {},
    ): String? {
        val token = authPrefs.authToken.first()
        if (token.isNullOrEmpty()) return null
        return try {
            val total = attachments.sumOf { it.first.size.toLong() }.coerceAtLeast(1L)
            var done = 0L
            var failed = 0
            // پیوستِ ردشده (نوعِ نامجاز/بزرگ) کلِ پیام را نمی‌شکند؛ فقط همان جا می‌ماند.
            val ids = attachments.mapNotNull { (bytes, mime) ->
                val base = done
                val id = runCatching {
                    val body = ProgressRequestBody(bytes, mime.toMediaType()) { written ->
                        onProgress(((base + written).toFloat() / total).coerceIn(0f, 1f))
                    }
                    apiService.uploadSupportFile("Bearer $token", body).id
                }.getOrNull()
                done += bytes.size
                onProgress((done.toFloat() / total).coerceIn(0f, 1f))
                if (id == null) failed++
                id
            }
            if (failed > 0) onAttachmentFailed(failed)
            apiService.reportBug("Bearer $token", BugReportRequest(message, appVersion, device, ids, category)).ticket
        } catch (e: Exception) {
            null
        }
    }

    suspend fun adminSupport(): ir.sadteam.loancalc.data.network.AdminSupportResponse? {
        val token = authPrefs.authToken.first() ?: return null
        return runCatching { apiService.adminSupport("Bearer $token") }.getOrNull()
    }

    suspend fun adminSupportFile(id: String): ByteArray? {
        val token = authPrefs.authToken.first() ?: return null
        return runCatching { apiService.adminSupportFile("Bearer $token", id).bytes() }.getOrNull()
    }

    suspend fun adminSupportReply(id: Long, text: String): Boolean {
        val token = authPrefs.authToken.first() ?: return false
        return runCatching {
            apiService.adminSupportReply("Bearer $token", ir.sadteam.loancalc.data.network.AdminSupportReplyRequest(id, text)).isSuccessful
        }.getOrDefault(false)
    }

    /** هدیه‌ی مستقیم. نتیجه: `null` = موفق، `user_not_found`، یا `failed`. */
    suspend fun adminGift(user: String, days: Int, coins: Int, text: String): String? {
        val token = authPrefs.authToken.first() ?: return "no_auth"
        return runCatching {
            val r = apiService.adminGift("Bearer $token", ir.sadteam.loancalc.data.network.AdminGiftRequest(user, days, coins, text))
            if (r.isSuccessful) null else if (r.code() == 404) "user_not_found" else "failed"
        }.getOrDefault("failed")
    }

    /** نتیجه: `null` = موفق، وگرنه کدِ خطا (`already_rewarded` و…). */
    suspend fun adminSupportGift(id: Long, days: Int, text: String): String? {
        val token = authPrefs.authToken.first() ?: return "no_auth"
        return runCatching {
            val r = apiService.adminSupportGift("Bearer $token", ir.sadteam.loancalc.data.network.AdminSupportGiftRequest(id, days, text))
            if (r.isSuccessful) null else if (r.code() == 409) "already_rewarded" else "failed"
        }.getOrDefault("network")
    }

    suspend fun adminSupportStatus(id: Long, status: String): Boolean {
        val token = authPrefs.authToken.first() ?: return false
        return runCatching {
            apiService.adminSupportStatus("Bearer $token", ir.sadteam.loancalc.data.network.AdminSupportStatusRequest(id, status)).isSuccessful
        }.getOrDefault(false)
    }

    /**
     * 🎁 خرج‌کردنِ کدِ هدیه. موفق که شد، اشتراکِ محلی هم فوراً روشن می‌شود تا کاربر
     * برای دیدنِ نتیجه مجبور به ورود/خروج نباشد.
     *
     * خطاها همان کدهای سرورند: `not_found` / `already_used` / `expired`.
     */
    suspend fun redeemGiftCode(code: String): AuthResult {
        val token = authPrefs.authToken.first()
        if (token.isNullOrEmpty()) return AuthResult.Error(null)
        return try {
            apiService.redeemGiftCode("Bearer $token", RedeemGiftRequest(code.trim().uppercase()))
            authPrefs.setSubscribed(true)
            AuthResult.Success
        } catch (e: HttpException) {
            AuthResult.Error(errorCodeFrom(e.response()?.errorBody()?.string()))
        } catch (e: Exception) {
            AuthResult.Error(null)
        }
    }

    /** ذخیره‌ی نامِ اختیاریِ کاربر رو سرور (تا با عوض‌کردنِ گوشی هم بمونه) + محلی.
     * رشته‌ی خالی یعنی پاک‌کردنِ اسم. اگه سرور در دسترس نباشه، حداقل محلی ذخیره می‌شه. */
    suspend fun updateName(name: String?): Boolean {
        val clean = name?.trim()?.ifBlank { null }
        authPrefs.setUserName(clean)
        val token = authPrefs.authToken.first() ?: return false
        return try {
            apiService.setName("Bearer $token", SetNameRequest(clean))
            true
        } catch (e: Exception) {
            false
        }
    }

    /** تاریخچه‌ی خریدهای اشتراک. اگه کاربر لاگین نباشه یا سرور در دسترس نباشه لیستِ خالی برمی‌گرده
     * (صفحه‌ی اشتراک اون‌وقت فقط پیامِ «تاریخچه‌ای نیست» نشون می‌ده، نه خطا). */
    suspend fun subscriptionHistory(): List<SubscriptionPurchaseDto> {
        val token = authPrefs.authToken.first()
        if (token.isNullOrEmpty()) return emptyList()
        return try {
            apiService.subscriptionHistory("Bearer $token").items
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** پورت الزامِ استانداردِ فروشگاه‌های اپ: حذفِ کاملِ حساب (شماره + وام‌ها + پشتیبان‌های ابری) از
     * سرور - نه فقط یه خروجِ محلی مثل [AuthPrefs.clearSession]. پاک‌کردنِ لوکالِ سشن بعد از موفقیت
     * وظیفه‌ی خودِ فراخوان (AuthViewModel) ـه، دقیقاً مثل الگوی logout. */
    suspend fun deleteAccount(): AuthResult {
        val token = authPrefs.authToken.first()
        if (token.isNullOrEmpty()) return AuthResult.Error(null)
        return try {
            apiService.deleteAccount("Bearer $token")
            AuthResult.Success
        } catch (e: HttpException) {
            AuthResult.Error(errorCodeFrom(e.response()?.errorBody()?.string()))
        } catch (e: Exception) {
            AuthResult.Error(null)
        }
    }

    private fun errorCodeFrom(body: String?): String? {
        if (body.isNullOrEmpty()) return null
        return try {
            val type = object : TypeToken<Map<String, Any?>>() {}.type
            val data: Map<String, Any?>? = gson.fromJson(body, type)
            data?.get("error") as? String
        } catch (e: Exception) {
            null
        }
    }
}

/** بدنه‌ی آپلود که بایت‌های نوشته‌شده را گزارش می‌دهد (نوارِ پیشرفتِ پیوستِ پشتیبانی). */
private class ProgressRequestBody(
    private val bytes: ByteArray,
    private val type: okhttp3.MediaType,
    private val onWritten: (Long) -> Unit,
) : okhttp3.RequestBody() {
    override fun contentType() = type
    override fun contentLength() = bytes.size.toLong()
    override fun writeTo(sink: okio.BufferedSink) {
        var off = 0
        while (off < bytes.size) {
            val n = minOf(16 * 1024, bytes.size - off)
            sink.write(bytes, off, n)
            off += n
            onWritten(off.toLong())
        }
    }
}
