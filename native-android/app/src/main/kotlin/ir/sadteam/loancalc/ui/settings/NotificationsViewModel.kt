package ir.sadteam.loancalc.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.prefs.UiPrefs
import ir.sadteam.loancalc.notifications.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * سوییچ «یادآوری سررسید» تو تنظیمات - مجوز POST_NOTIFICATIONS (اندروید ۱۳+) خودِ UI (کامپوزبل
 * تو SettingsScreen، چون به Activity نیاز داره) درخواست می‌کنه؛ اینجا فقط بعد از گرفتن مجوز
 * صدا زده می‌شه تا هم تنظیم تو DataStore ذخیره بشه هم WorkManager زمان‌بندی/لغو بشه.
 */
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val uiPrefs: UiPrefs,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {
    val enabled: StateFlow<Boolean> = uiPrefs.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        viewModelScope.launch {
            if (uiPrefs.notificationsEnabled.first()) reminderScheduler.schedule()
        }
    }

    /** بعد از این‌که کاربر مجوز رو داد (یا اصلاً روی API < 33 نیازی نبود) صدا زده می‌شه. */
    fun enable() {
        viewModelScope.launch {
            uiPrefs.setNotificationsEnabled(true)
            reminderScheduler.schedule()
        }
    }

    fun disable() {
        viewModelScope.launch {
            uiPrefs.setNotificationsEnabled(false)
            reminderScheduler.cancel()
        }
    }
}
