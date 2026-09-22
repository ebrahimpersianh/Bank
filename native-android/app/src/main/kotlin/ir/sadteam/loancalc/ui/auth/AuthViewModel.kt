package ir.sadteam.loancalc.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.BuildConfig
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.AuthRepository
import ir.sadteam.loancalc.data.AuthResult
import ir.sadteam.loancalc.data.ChequeRepository
import ir.sadteam.loancalc.data.DangRepository
import ir.sadteam.loancalc.data.DebtRepository
import ir.sadteam.loancalc.data.IncomeRepository
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.GamificationRepository
import ir.sadteam.loancalc.data.NoteRepository
import ir.sadteam.loancalc.data.WealthSnapshotRepository
import ir.sadteam.loancalc.data.SyncOutcome
import ir.sadteam.loancalc.data.network.SubscriptionPurchaseDto
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
    private val incomeRepository: IncomeRepository,
    private val debtRepository: DebtRepository,
    private val dangRepository: DangRepository,
    private val noteRepository: NoteRepository,
    private val wealthSnapshotRepository: WealthSnapshotRepository,
    private val gamification: GamificationRepository,
) : ViewModel() {
    init {
        // ⚠️ **هدیه‌ی ۵۰ سکه فقط موقعِ `verifyOtp` داده می‌شد**، یعنی کاربری که از قبل وارد
        // شده بود هیچ‌وقت نمی‌گرفتش (گزارشِ کاربر رو بیلدِ ۴۷۱: شمارنده ۱۰ بود نه ۶۰).
        // اینجا هر بار که اپ با توکنِ معتبر بالا میاد یه‌بار تلاش می‌شه؛ تکرارش بی‌اثره
        // چون دفترِ سکه رو نوعِ رویداد ایندکسِ یکتا داره.
        viewModelScope.launch {
            if (!authPrefs.authToken.first().isNullOrEmpty()) {
                gamification.awardOnce(
                    GamificationRepository.Type.NEW_PHONE_GIFT,
                    GamificationRepository.Reward.NEW_PHONE_GIFT,
                )
            }
        }
    }

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

    /** null یعنی یا مشترک نیست یا اشتراکش دستی/دائمیه (نه یه خریدِ زمان‌دار) - رجوع کن به
     * کامنتِ AuthPrefs.subscribedUntil. */
    val subscribedUntil: StateFlow<String?> = authPrefs.subscribedUntil
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** پلنِ خریداری‌شده ("1m"/"3m"/"6m"/"1y") - فقط برای خریدهای واقعیِ زمان‌دار پر می‌شه، رجوع کن
     * به کامنتِ AuthPrefs.subscriptionTier. */
    val subscriptionTier: StateFlow<String?> = authPrefs.subscriptionTier
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val phone: StateFlow<String?> = authPrefs.phone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** نامِ اختیاریِ کاربر - null یعنی وارد نکرده (کاملاً عادی). */
    val userName: StateFlow<String?> = authPrefs.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateName(name: String?) {
        viewModelScope.launch {
            authRepository.updateName(name)
            // «تکمیلِ پروفایل ۵۰ سکه» (جدولِ `20e`). فقط وقتی اسمِ واقعی ثبت شده، نه وقتی
            // کاربر اسمش رو پاک کرده.
            if (!name.isNullOrBlank()) {
                gamification.awardOnce(
                    GamificationRepository.Type.COMPLETE_PROFILE,
                    GamificationRepository.Reward.COMPLETE_PROFILE,
                )
            }
        }
    }

    fun refreshStatus() {
        viewModelScope.launch { authRepository.refreshSubscriptionStatus() }
    }

    /** گیتِ مسیرِ اولین ورود ([ir.sadteam.loancalc.ui.onboarding.OnboardingFlow]) تو AppRoot: تا
     * اولین مقدار واقعی از DataStore نیومده null می‌مونه (همون الگوی [gateState]) که یه فلشِ
     * اشتباهیِ صفحه‌ی آنبوردینگ دیده نشه.
     *
     * کلیدِ DataStore عمداً همون `benefits_seen`ِ قدیمی مونده تا کسی که قبلاً صفحه‌ی امکاناتِ
     * حذف‌شده رو دیده، حالا مسیرِ آنبوردینگ رو دوباره نبینه. */
    val onboardingDone: StateFlow<Boolean?> = authPrefs.benefitsSeen
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun markOnboardingDone() {
        viewModelScope.launch { authPrefs.setBenefitsSeen(true) }
    }

    /** صفحه‌ی مجوزها رد شده - هر دو مجوز اختیاری‌اند و بی این، گیت بن‌بست بود. */
    val permissionGateSkipped: StateFlow<Boolean> = authPrefs.permissionGateSkipped
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun skipPermissionGate() {
        viewModelScope.launch { authPrefs.setPermissionGateSkipped(true) }
    }

    /** true یعنی این شماره از قبل تو سرور بوده و ۱۵ روزِ هدیه‌ی اضافه گرفته - متنِ شیتِ هدیه
     * ([ir.sadteam.loancalc.ui.onboarding.PostLoginSheets]) بر اساسِ همین عوض می‌شه. */
    val legacyGift: StateFlow<Boolean> = authPrefs.legacyGift
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** گیتِ دو شیتِ بعد از ورود - همون الگوی null-تا-لود-شدنِ [onboardingDone]. */
    val postLoginSheetsSeen: StateFlow<Boolean?> = authPrefs.postLoginSheetsSeen
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun markPostLoginSheetsSeen() {
        viewModelScope.launch { authPrefs.setPostLoginSheetsSeen(true) }
    }

    /** پورت گیتِ تورِ راهنمای اولین ورود (TabTourOverlay تو LoanCalcApp، نه یه صفحه‌ی جدا) - همون
     * الگوی null-تا-لود-شدنِ [onboardingDone]. */
    val tourSeen: StateFlow<Boolean?> = authPrefs.tourSeen
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun markTourSeen() {
        viewModelScope.launch { authPrefs.setTourSeen(true) }
    }

    /** پورت syncAfterLogin - وقتی هم گوشی هم سرور داده‌ی متفاوت دارن، غیر-null می‌شه و منتظر
     * تصمیم کاربر (resolveSyncConflict) می‌مونه؛ UI (LoginScreen) اینو observe می‌کنه. */
    private val _syncConflict = MutableStateFlow<List<Map<String, Any?>>?>(null)
    val syncConflict: StateFlow<List<Map<String, Any?>>?> = _syncConflict.asStateFlow()

    private val _purchaseHistory = MutableStateFlow<List<SubscriptionPurchaseDto>?>(null)

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
            incomeRepository.clearLocal()
            debtRepository.clearLocal()
            dangRepository.clearLocal()
            noteRepository.clearLocal()
            // عکس‌های روزانه‌ی دارایی هم **کاربرمحور**ند: مانده‌ی حسابِ کاربرِ قبلی
            // نباید در نمودارِ کاربرِ بعدی دیده شود (قاعده‌ی داده‌ی کاربرمحور در خروج).
            wealthSnapshotRepository.clearLocal()
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
                    // «هدیه‌ی شماره‌ی تازه ۵۰ سکه» (جدولِ `20e`). یک‌باره‌ست، پس ورودهای بعدی
                    // دوباره سکه نمی‌دن (یگانگی رو خودِ نوعِ رویداد تو دفترِ سکه).
                    gamification.awardOnce(
                        GamificationRepository.Type.NEW_PHONE_GIFT,
                        GamificationRepository.Reward.NEW_PHONE_GIFT,
                    )
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
                    incomeRepository.clearLocal()
                    debtRepository.clearLocal()
                    noteRepository.clearLocal()
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
            when (val result = authRepository.verifySubscription(productId, purchaseToken, BuildConfig.FLAVOR)) {
                is AuthResult.Success -> onSuccess()
                is AuthResult.Error -> onError(result.code)
            }
        }
    }

    /** 🐞 ثبتِ گزارشِ مشکل - `onResult` کدِ پیگیری می‌گیرد، یا `null` اگر نشد. */
    fun reportBug(message: String, appVersion: String?, device: String?, onResult: (String?) -> Unit) {
        viewModelScope.launch { onResult(authRepository.reportBug(message, appVersion, device)) }
    }

    /** 🎁 خرج‌کردنِ کدِ هدیه - رجوع کن به [AuthRepository.redeemGiftCode]. */
    fun redeemGiftCode(code: String, onSuccess: () -> Unit, onError: (String?) -> Unit) {
        viewModelScope.launch {
            when (val result = authRepository.redeemGiftCode(code)) {
                is AuthResult.Success -> {
                    refreshStatus()
                    onSuccess()
                }
                is AuthResult.Error -> onError(result.code)
            }
        }
    }

    /** تاریخچه‌ی خریدهای اشتراک - صفحه‌ی اشتراک هر بار که باز می‌شه یه‌بار می‌خونتش. */
    val purchaseHistory: StateFlow<List<SubscriptionPurchaseDto>?> = _purchaseHistory

    fun loadPurchaseHistory() {
        viewModelScope.launch { _purchaseHistory.value = authRepository.subscriptionHistory() }
    }
}
