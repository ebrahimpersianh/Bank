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

    /** یادآوریِ روزانه‌ی «دخل‌وخرج امروز یادت نره» (رجوع کن به DueDateReminderWorker) - سوییچِ
     * جداگانه‌ای از یادآوریِ سررسید، ولی از همون Workerِ زمان‌بندی‌شده استفاده می‌کنه؛ برای همین باید
     * حواسمون باشه اگه یکی از این دو خاموش شد ولی اون‌یکی هنوز روشنه، زمان‌بندی لغو نشه. */
    val dailyExpenseReminderEnabled: StateFlow<Boolean> = uiPrefs.dailyExpenseReminderEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** یادآورِ دنگ (سهمِ دوست‌ها هنوز نیامده) - پیش‌فرض روشن. */
    val dangReminderEnabled: StateFlow<Boolean> = uiPrefs.dangReminderEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    /** یادآورِ دنگ مستقل است: با روشن‌بودنش زمان‌بندی زنده می‌ماند، حتی اگر دو یادآورِ دیگر خاموش باشند. */
    fun setDangReminderEnabled(value: Boolean) {
        viewModelScope.launch {
            uiPrefs.setDangReminderEnabled(value)
            if (value) reminderScheduler.schedule() else if (!anyReminderOn()) reminderScheduler.cancel()
        }
    }

    private suspend fun anyReminderOn(): Boolean =
        uiPrefs.notificationsEnabled.first() || uiPrefs.dailyExpenseReminderEnabled.first() || uiPrefs.dangReminderEnabled.first()

    init {
        viewModelScope.launch {
            if (anyReminderOn()) {
                reminderScheduler.schedule()
            }
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
            if (!anyReminderOn()) reminderScheduler.cancel()
        }
    }

    fun enableDailyExpenseReminder() {
        viewModelScope.launch {
            uiPrefs.setDailyExpenseReminderEnabled(true)
            reminderScheduler.schedule()
        }
    }

    fun disableDailyExpenseReminder() {
        viewModelScope.launch {
            uiPrefs.setDailyExpenseReminderEnabled(false)
            if (!anyReminderOn()) reminderScheduler.cancel()
        }
    }
}
