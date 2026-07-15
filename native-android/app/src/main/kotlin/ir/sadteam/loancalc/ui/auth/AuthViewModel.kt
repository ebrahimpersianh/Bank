package ir.sadteam.loancalc.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.AuthRepository
import ir.sadteam.loancalc.data.AuthResult
import ir.sadteam.loancalc.data.prefs.AuthPrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** پورت checkLoginGateOnStart/isGuestMode تو www/index.html: تا اولین مقدار واقعی از DataStore
 * نیومده null می‌مونه (که UI اون رو به‌جای فلش اشتباهی صفحه‌ی ورود، به‌عنوان لودینگ نشون بده). */
enum class GateState { NEEDS_LOGIN, GUEST, LOGGED_IN }

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authPrefs: AuthPrefs,
    private val authRepository: AuthRepository,
) : ViewModel() {
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
            when (val result = authRepository.requestOtp(phone)) {
                is AuthResult.Success -> onSuccess()
                is AuthResult.Error -> onError(result.code)
            }
        }
    }

    /** پورت confirmPhoneOtp: onSuccess موفقیت رو گزارش می‌کنه (session از قبلش تو AuthPrefs ذخیره
     * شده)، onError کد خطای سرور رو می‌ده (wrong_code/code_expired/too_many_attempts). */
    fun verifyOtp(phone: String, code: String, onSuccess: () -> Unit, onError: (String?) -> Unit) {
        viewModelScope.launch {
            when (val result = authRepository.verifyOtp(phone, code)) {
                is AuthResult.Success -> onSuccess()
                is AuthResult.Error -> onError(result.code)
            }
        }
    }
}
