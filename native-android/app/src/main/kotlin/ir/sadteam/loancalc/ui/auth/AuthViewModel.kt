package ir.sadteam.loancalc.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.network.ApiService
import ir.sadteam.loancalc.data.network.RequestOtpRequest
import ir.sadteam.loancalc.data.network.VerifyOtpRequest
import ir.sadteam.loancalc.data.prefs.AuthPrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

/** پورت checkLoginGateOnStart/isGuestMode تو www/index.html: تا اولین مقدار واقعی از DataStore
 * نیومده null می‌مونه (که UI اون رو به‌جای فلش اشتباهی صفحه‌ی ورود، به‌عنوان لودینگ نشون بده). */
enum class GateState { NEEDS_LOGIN, GUEST, LOGGED_IN }

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authPrefs: AuthPrefs,
    private val apiService: ApiService,
) : ViewModel() {
    private val gson = Gson()

    val gateState: StateFlow<GateState?> = combine(authPrefs.authToken, authPrefs.guestMode) { token, guest ->
        val state: GateState? = when {
            !token.isNullOrEmpty() -> GateState.LOGGED_IN
            guest -> GateState.GUEST
            else -> GateState.NEEDS_LOGIN
        }
        state
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun continueAsGuest() {
        viewModelScope.launch { authPrefs.setGuestMode(true) }
    }

    /** پورت sendPhoneOtp: موفق بشه onSuccess صدا زده می‌شه، وگرنه onError با کد خطای سرور
     * (مثلاً "too_soon") تا خودِ UI پیام فارسی متناظرش رو نشون بده - دقیقاً مثل msgs تو وب. */
    fun requestOtp(phone: String, onSuccess: () -> Unit, onError: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.requestOtp(RequestOtpRequest(phone))
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError(errorCodeFrom(response.errorBody()?.string()))
                }
            } catch (e: Exception) {
                onError(null)
            }
        }
    }

    /** پورت confirmPhoneOtp: onSuccess موفقیت رو گزارش می‌کنه (session از قبلش تو AuthPrefs ذخیره
     * شده)، onError کد خطای سرور رو می‌ده (wrong_code/code_expired/too_many_attempts). */
    fun verifyOtp(phone: String, code: String, onSuccess: () -> Unit, onError: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = apiService.verifyOtp(VerifyOtpRequest(phone, code))
                authPrefs.saveSession(result.token, result.phone, result.subscribed)
                onSuccess()
            } catch (e: HttpException) {
                onError(errorCodeFrom(e.response()?.errorBody()?.string()))
            } catch (e: Exception) {
                onError(null)
            }
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
