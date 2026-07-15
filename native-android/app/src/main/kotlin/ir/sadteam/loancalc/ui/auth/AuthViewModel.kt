package ir.sadteam.loancalc.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.AuthRepository
import ir.sadteam.loancalc.data.AuthResult
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.SyncOutcome
import ir.sadteam.loancalc.data.prefs.AuthPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    private val loanRepository: LoanRepository,
) : ViewModel() {
    val gateState: StateFlow<GateState?> = combine(authPrefs.authToken, authPrefs.guestMode) { token, guest ->
        val state: GateState? = when {
            !token.isNullOrEmpty() -> GateState.LOGGED_IN
            guest -> GateState.GUEST
            else -> GateState.NEEDS_LOGIN
        }
        state
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** پورت setSubscribed/refreshSubscriptionStatus تو www/index.html - فعلاً فقط از DataStore
     * محلی خونده می‌شه (بعد از verify-otp یا [verifySubscriptionPurchase] نوشته شده)؛ تازه‌سازی
     * زنده از GET /api/auth/me فاز بعده. */
    val subscribed: StateFlow<Boolean> = authPrefs.subscribed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val phone: StateFlow<String?> = authPrefs.phone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** پورت syncAfterLogin - وقتی هم گوشی هم سرور داده‌ی متفاوت دارن، غیر-null می‌شه و منتظر
     * تصمیم کاربر (resolveSyncConflict) می‌مونه؛ UI (LoginScreen) اینو observe می‌کنه. */
    private val _syncConflict = MutableStateFlow<List<Map<String, Any?>>?>(null)
    val syncConflict: StateFlow<List<Map<String, Any?>>?> = _syncConflict.asStateFlow()

    fun continueAsGuest() {
        viewModelScope.launch { authPrefs.setGuestMode(true) }
    }

    /** پورت handleLogout: بعد از خروج، guest_mode رو ست می‌کنه (نه اینکه گیت اجباری رو دوباره باز
     * کنه) تا کاربر بعد از خروج آزادانه به‌عنوان مهمان ادامه بده - رجوع کن به CLAUDE.md. */
    fun logout() {
        viewModelScope.launch {
            authPrefs.clearSession()
            authPrefs.setGuestMode(true)
        }
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

    /**
     * پورت confirmPhoneOtp + syncAfterLogin: بعد از ورود موفق، قبل از صدا زدن onSuccess، یه‌بار
     * وضعیت سینک رو چک می‌کنه - اگه تعارض داشت [syncConflict] پر می‌شه و onSuccess طبق قرارداد
     * همچنان صدا زده می‌شه (UI باید اول [syncConflict] رو چک کنه، نه این‌که کورکورانه ناوبری کنه).
     */
    fun verifyOtp(phone: String, code: String, onSuccess: () -> Unit, onError: (String?) -> Unit) {
        viewModelScope.launch {
            when (val result = authRepository.verifyOtp(phone, code)) {
                is AuthResult.Success -> {
                    val token = authPrefs.authToken.first()
                    if (!token.isNullOrEmpty()) {
                        when (val outcome = loanRepository.syncAfterLogin(token)) {
                            is SyncOutcome.ConflictNeedsChoice -> _syncConflict.value = outcome.serverLoans
                            else -> Unit
                        }
                    }
                    onSuccess()
                }
                is AuthResult.Error -> onError(result.code)
            }
        }
    }

    /** پورت تصمیم کاربر تو openConfirmModal (syncAfterLogin): [useServer]=true یعنی «بله، نسخه‌ی
     * ابری رو بیار» (جایگزینی محلی)، false یعنی نسخه‌ی همین گوشی بمونه و همون به سرور پوش بشه. */
    fun resolveSyncConflict(useServer: Boolean, onDone: () -> Unit) {
        viewModelScope.launch {
            val conflict = _syncConflict.value
            if (conflict != null) {
                if (useServer) {
                    loanRepository.replaceAllWithServerData(conflict)
                } else {
                    authPrefs.authToken.first()?.let { loanRepository.pushToServer(it) }
                }
            }
            _syncConflict.value = null
            onDone()
        }
    }

    /** پورت verifySubscriptionPurchase تو www/index.html: بعد از یه خرید موفق Poolakey صدا زده
     * می‌شه، سرور خودش مستقل از کافه‌بازار تایید می‌کنه (به کلاینت اعتماد نمی‌شه). */
    fun verifySubscriptionPurchase(
        productId: String,
        purchaseToken: String,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit,
    ) {
        viewModelScope.launch {
            when (val result = authRepository.verifySubscription(productId, purchaseToken)) {
                is AuthResult.Success -> onSuccess()
                is AuthResult.Error -> onError(result.code)
            }
        }
    }
}
