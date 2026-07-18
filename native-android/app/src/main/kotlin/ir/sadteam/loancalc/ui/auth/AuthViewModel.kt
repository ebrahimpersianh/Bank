package ir.sadteam.loancalc.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.AuthRepository
import ir.sadteam.loancalc.data.AuthResult
import ir.sadteam.loancalc.data.ChequeRepository
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
    private val chequeRepository: ChequeRepository,
    private val accountRepository: AccountRepository,
) : ViewModel() {
    val gateState: StateFlow<GateState?> = combine(authPrefs.authToken, authPrefs.guestMode) { token, guest ->
        val state: GateState? = when {
            !token.isNullOrEmpty() -> GateState.LOGGED_IN
            guest -> GateState.GUEST
            else -> GateState.NEEDS_LOGIN
        }
        state
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** پورت setSubscribed/refreshSubscriptionStatus تو www/index.html - از DataStore محلی خونده
     * می‌شه (بعد از verify-otp یا [verifySubscriptionPurchase] نوشته شده)، ولی حالا [refreshStatus]
     * هم از AppRoot هر بار اپ باز می‌شه صدا زده می‌شه تا وضعیت واقعاً زنده از سرور تازه بشه (مثلاً
     * دقیقاً روزی که دوره‌ی آزمایشیِ ۷روزه تموم می‌شه، بدون نیاز به خروج/ورود دوباره). */
    val subscribed: StateFlow<Boolean> = authPrefs.subscribed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val trialDaysLeft: StateFlow<Int?> = authPrefs.trialDaysLeft
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val phone: StateFlow<String?> = authPrefs.phone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun refreshStatus() {
        viewModelScope.launch { authRepository.refreshSubscriptionStatus() }
    }

    /** پورت گیت مجوز → [BenefitsScreen] تو AppRoot: تا اولین مقدار واقعی از DataStore نیومده null
     * می‌مونه (همون الگوی [gateState]) که یه فلش اشتباهی صفحه‌ی امکانات دیده نشه. */
    val benefitsSeen: StateFlow<Boolean?> = authPrefs.benefitsSeen
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun markBenefitsSeen() {
        viewModelScope.launch { authPrefs.setBenefitsSeen(true) }
    }

    /** پورت syncAfterLogin - وقتی هم گوشی هم سرور داده‌ی متفاوت دارن، غیر-null می‌شه و منتظر
     * تصمیم کاربر (resolveSyncConflict) می‌مونه؛ UI (LoginScreen) اینو observe می‌کنه. */
    private val _syncConflict = MutableStateFlow<List<Map<String, Any?>>?>(null)
    val syncConflict: StateFlow<List<Map<String, Any?>>?> = _syncConflict.asStateFlow()

    fun continueAsGuest() {
        viewModelScope.launch { authPrefs.setGuestMode(true) }
    }

    /** پورت handleLogout: بعد از خروج، guest_mode رو ست می‌کنه (نه اینکه گیت اجباری رو دوباره باز
     * کنه) تا کاربر بعد از خروج آزادانه به‌عنوان مهمان ادامه بده - رجوع کن به CLAUDE.md.
     *
     * وام‌ها/چک‌ها/حساب‌های محلی هم همین‌جا پاک می‌شن - وگرنه اگه کاربر بعداً با یه شماره‌ی *دیگه*
     * رو همین گوشی وارد بشه، [LoanRepository.syncAfterLogin] چون سرورِ حسابِ جدید هنوز خالیه ولی
     * محلی داده داره، اون رو «پوش به سرور» تفسیر می‌کنه و اشتباهی وام‌های حسابِ قبلی رو زیرِ حسابِ
     * جدید آپلود می‌کنه (باگی که کاربر موقع تست با دو شماره‌ی مختلف رو یه گوشی گزارش داد). داده‌ی
     * خودِ حسابِ قبلی جایی از دست نمی‌ره چون از قبل رو سرور پشتیبان گرفته شده - دفعه‌ی بعد که با
     * همون شماره وارد بشه، [LoanRepository.syncAfterLogin] از سرور برش می‌گردونه. */
    fun logout() {
        viewModelScope.launch {
            authPrefs.clearSession()
            authPrefs.setGuestMode(true)
            loanRepository.clearLocal()
            chequeRepository.clearLocal()
            accountRepository.clearLocal()
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
     *
     * علاوه بر سینک وام، اگه کاربر مشترک باشه، چک‌ها و حساب‌ها هم بلافاصله (نه فقط با
     * AutoBackupWorkerِ دوره‌ای) به سرور پوش می‌شن - خواسته‌ی کاربر: «هر کاربری ورود کرد خودکار
     * پشتیبان بگیره». چون چک/حساب برخلاف وام تعارض‌سنجی ندارن (همون pushToServerِ یک‌طرفه‌ی
     * AutoBackupWorker)، این‌جا هم بی‌قید صدا زده می‌شن؛ خودشون fire-and-forget-ن (خطا رو قورت
     * می‌دن)، پس منتظرشون نمی‌مونیم و onSuccess بلافاصله بعد از تصمیم سینکِ وام صدا زده می‌شه.
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
                        if (authPrefs.subscribed.first()) {
                            launch { chequeRepository.pushToServer(token) }
                            launch { accountRepository.pushToServer(token) }
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

    /** الزامِ استانداردِ فروشگاه‌های اپ: حذفِ کاملِ حساب (نه فقط خروج) - سرور شماره/وام‌ها/پشتیبان‌های
     * ابریِ چک و حساب رو پاک می‌کنه (رجوع کن به AuthRoutes.kt سمت سرور). موفق که شد، لوکال هم دقیقاً
     * مثل [logout] پاک می‌شه، چون دیگه توکنی نیست که باهاش کار کنه. */
    fun deleteAccount(onSuccess: () -> Unit, onError: (String?) -> Unit) {
        viewModelScope.launch {
            when (val result = authRepository.deleteAccount()) {
                is AuthResult.Success -> {
                    authPrefs.clearSession()
                    authPrefs.setGuestMode(true)
                    loanRepository.clearLocal()
                    chequeRepository.clearLocal()
                    accountRepository.clearLocal()
                    onSuccess()
                }
                is AuthResult.Error -> onError(result.code)
            }
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
