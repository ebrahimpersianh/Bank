package ir.sadteam.loancalc.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.prefs.UiPrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * سوییچِ «خوندنِ خودکارِ پیامکِ بانکی» تو تنظیمات - مجوزِ RECEIVE_SMS خودِ UI (کامپوزبلِ
 * SettingsScreen، چون به Activity نیاز داره) درخواست می‌کنه؛ اینجا فقط بعدِ گرفتنِ مجوز صدا زده
 * می‌شه تا تنظیم تو DataStore ذخیره بشه - رجوع کن به BankSmsReceiver که واقعاً این پرچم رو چک
 * می‌کنه.
 */
@HiltViewModel
class SmsAutoImportViewModel @Inject constructor(
    private val uiPrefs: UiPrefs,
) : ViewModel() {
    val enabled: StateFlow<Boolean> = uiPrefs.smsAutoImportEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val lastImportAt: StateFlow<String?> = uiPrefs.lastSmsImportAt
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun enable() {
        viewModelScope.launch { uiPrefs.setSmsAutoImportEnabled(true) }
    }

    fun disable() {
        viewModelScope.launch { uiPrefs.setSmsAutoImportEnabled(false) }
    }
}
