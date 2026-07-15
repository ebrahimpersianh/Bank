package ir.sadteam.loancalc.ui.security

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.sha256Hex
import ir.sadteam.loancalc.data.prefs.SecurityPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** نتیجه‌ی هر تلاش ورود با PIN - پورت مفهومی «قفل موقت بعد از چند تلاش ناموفق» اپ رقیب (VAMMAN)،
 * که قبلاً این پروژه نداشت (فقط verifyPin ساده، بدون شمارش تلاش‌ها). */
sealed class PinAttemptResult {
    data object Success : PinAttemptResult()
    data class WrongPin(val attemptsLeft: Int) : PinAttemptResult()
    data class LockedOut(val secondsLeft: Long) : PinAttemptResult()
}

/**
 * قفل امنیتی PIN+اثر انگشت (برگردوندن تصمیم قبلی حذف بایومتریک، با درخواست صریح جدید کاربر - رجوع
 * کن به CLAUDE.md). [unlocked] با هر شروع تازه‌ی پروسه از false شروع می‌شه (پیش‌فرض ViewModel).
 *
 * قبلاً با هر [ProcessLifecycleOwner] ON_STOP (کل اپ رفت پس‌زمینه) بی‌قید‌وشرط دوباره قفل می‌شد. حالا
 * یه «قفل خودکار با تاخیر» داره: موقع ON_STOP فقط زمان رفتن به پس‌زمینه ثبت می‌شه؛ موقع ON_START
 * (برگشت به اپ) اگه فاصله‌ی زمانی از [autoLockTimeoutMinutes] بیشتر بود، تازه قفل می‌شه - وگرنه
 * (مثلاً فقط چند ثانیه سوییچ به اپ دیگه) بدون قفل ادامه پیدا می‌کنه. پیش‌فرض ۰ دقیقه (بی‌درنگ) رفتار
 * قبلی رو کاملاً حفظ می‌کنه، مگر کاربر از تنظیمات عوضش کنه.
 *
 * علاوه‌براین، [attemptPin] بعد از ۵ تلاش ناموفق پشت‌سرهم یه قفل موقت ۳۰ ثانیه‌ای (ضدـبروت‌فورس) رو
 * فعال می‌کنه - پورت مفهومی «قفل موقت» اپ رقیب.
 */
@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val securityPrefs: SecurityPrefs,
) : ViewModel(), DefaultLifecycleObserver {
    val pinHash: StateFlow<String?> = securityPrefs.pinHash
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val biometricEnabled: StateFlow<Boolean> = securityPrefs.biometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val autoLockTimeoutMinutes: StateFlow<Int> = securityPrefs.autoLockTimeoutMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _unlocked = MutableStateFlow(false)
    val unlocked: StateFlow<Boolean> = _unlocked.asStateFlow()

    private var backgroundedAtMillis: Long? = null

    private val _failedAttempts = MutableStateFlow(0)
    private val _lockedOutUntilMillis = MutableStateFlow<Long?>(null)

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        backgroundedAtMillis = System.currentTimeMillis()
    }

    override fun onStart(owner: LifecycleOwner) {
        val backgroundedAt = backgroundedAtMillis ?: return
        backgroundedAtMillis = null
        val elapsedMinutes = (System.currentTimeMillis() - backgroundedAt) / 60_000
        if (elapsedMinutes >= autoLockTimeoutMinutes.value) {
            _unlocked.value = false
        }
    }

    override fun onCleared() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }

    fun unlock() {
        _unlocked.value = true
    }

    fun verifyPin(pin: String): Boolean = pinHash.value != null && pinHash.value == sha256Hex(pin)

    /** پورت مفهومی قفل موقت بعد از تلاش‌های ناموفق - [MAX_ATTEMPTS] تلاش، بعدش [LOCKOUT_DURATION_MILLIS]
     * قفل موقت. تلاش موفق یا سپری‌شدن مدت قفل، شمارنده رو صفر می‌کنه. */
    fun attemptPin(pin: String): PinAttemptResult {
        val lockedUntil = _lockedOutUntilMillis.value
        if (lockedUntil != null) {
            val remainingMillis = lockedUntil - System.currentTimeMillis()
            if (remainingMillis > 0) {
                return PinAttemptResult.LockedOut(remainingMillis / 1000 + 1)
            }
            _lockedOutUntilMillis.value = null
            _failedAttempts.value = 0
        }

        if (verifyPin(pin)) {
            _failedAttempts.value = 0
            unlock()
            return PinAttemptResult.Success
        }

        val attempts = _failedAttempts.value + 1
        _failedAttempts.value = attempts
        return if (attempts >= MAX_ATTEMPTS) {
            _lockedOutUntilMillis.value = System.currentTimeMillis() + LOCKOUT_DURATION_MILLIS
            PinAttemptResult.LockedOut(LOCKOUT_DURATION_MILLIS / 1000)
        } else {
            PinAttemptResult.WrongPin(MAX_ATTEMPTS - attempts)
        }
    }

    fun setPin(pin: String) {
        viewModelScope.launch { securityPrefs.setPinHash(sha256Hex(pin)) }
    }

    fun clearPin() {
        viewModelScope.launch { securityPrefs.setPinHash(null) }
    }

    fun setBiometricEnabled(value: Boolean) {
        viewModelScope.launch { securityPrefs.setBiometricEnabled(value) }
    }

    fun setAutoLockTimeoutMinutes(value: Int) {
        viewModelScope.launch { securityPrefs.setAutoLockTimeoutMinutes(value) }
    }

    private companion object {
        const val MAX_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MILLIS = 30_000L
    }
}
