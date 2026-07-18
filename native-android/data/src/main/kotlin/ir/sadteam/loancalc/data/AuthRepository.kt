package ir.sadteam.loancalc.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ir.sadteam.loancalc.data.network.ApiService
import ir.sadteam.loancalc.data.network.RequestOtpRequest
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
            val result = apiService.verifyOtp(VerifyOtpRequest(phone, code))
            authPrefs.saveSession(result.token, result.phone, result.subscribed, result.trialDaysLeft)
            AuthResult.Success
        } catch (e: HttpException) {
            AuthResult.Error(errorCodeFrom(e.response()?.errorBody()?.string()))
        } catch (e: Exception) {
            AuthResult.Error(null)
        }
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
