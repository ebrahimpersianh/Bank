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

/**
 * قفل امنیتی PIN+اثر انگشت (برگردوندن تصمیم قبلی حذف بایومتریک، با درخواست صریح جدید کاربر - رجوع
 * کن به CLAUDE.md). [unlocked] با هر شروع تازه‌ی پروسه از false شروع می‌شه (پیش‌فرض ViewModel) و با
 * [ProcessLifecycleOwner]'s ON_STOP (یعنی کل اپ رفت پس‌زمینه، نه فقط چرخش صفحه) دوباره false
 * می‌شه - تا برگشتن به اپ همیشه دوباره قفل بخواد، نه فقط قفل اولیه‌ی موقع باز شدن.
 */
@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val securityPrefs: SecurityPrefs,
) : ViewModel(), DefaultLifecycleObserver {
    val pinHash: StateFlow<String?> = securityPrefs.pinHash
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val biometricEnabled: StateFlow<Boolean> = securityPrefs.biometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _unlocked = MutableStateFlow(false)
    val unlocked: StateFlow<Boolean> = _unlocked.asStateFlow()

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        _unlocked.value = false
    }

    override fun onCleared() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }

    fun unlock() {
        _unlocked.value = true
    }

    fun verifyPin(pin: String): Boolean = pinHash.value != null && pinHash.value == sha256Hex(pin)

    fun setPin(pin: String) {
        viewModelScope.launch { securityPrefs.setPinHash(sha256Hex(pin)) }
    }

    fun clearPin() {
        viewModelScope.launch { securityPrefs.setPinHash(null) }
    }

    fun setBiometricEnabled(value: Boolean) {
        viewModelScope.launch { securityPrefs.setBiometricEnabled(value) }
    }
}
