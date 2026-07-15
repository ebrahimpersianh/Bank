package ir.sadteam.loancalc.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ir.sadteam.loancalc.data.network.ApiService
import ir.sadteam.loancalc.data.network.RequestOtpRequest
import ir.sadteam.loancalc.data.network.VerifyOtpRequest
import ir.sadteam.loancalc.data.prefs.AuthPrefs
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
            authPrefs.saveSession(result.token, result.phone, result.subscribed)
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
