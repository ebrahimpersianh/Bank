package ir.sadteam.loancalc.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.formatReminderOffsets
import ir.sadteam.loancalc.core.parseReminderOffsets
import ir.sadteam.loancalc.data.prefs.UiPrefs
import ir.sadteam.loancalc.notifications.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** پیش‌فرضِ سراسریِ زمان‌بندی/صدا/ویبره‌ی یادآوری - هر وام/چکی که تنظیمِ اختصاصی نداره همینو
 * استفاده می‌کنه (رجوع کن به [ir.sadteam.loancalc.notifications.DueDateReminderWorker]). */
@HiltViewModel
class ReminderSettingsViewModel @Inject constructor(
    private val uiPrefs: UiPrefs,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {
    /** ساعتِ یادآوری - طبقِ فریمِ `50a` **یکی** برای هر سه کانال است، نه سه‌تا. */
    val reminderHour: StateFlow<Int> = uiPrefs.reminderHour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiPrefs.DEFAULT_REMINDER_HOUR)

    val autoTxEnabled: StateFlow<Boolean> = uiPrefs.autoTxNotifyEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val comeBackEnabled: StateFlow<Boolean> = uiPrefs.comeBackReminderEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    /** ⚠️ بعدِ عوض‌کردنِ ساعت باید دوباره زمان‌بندی شود، وگرنه تا اجرای بعدی ساعتِ قدیمی می‌ماند. */
    fun setReminderHour(hour: Int) {
        viewModelScope.launch {
            uiPrefs.setReminderHour(hour)
            reminderScheduler.schedule()
        }
    }

    fun setAutoTxEnabled(value: Boolean) {
        viewModelScope.launch { uiPrefs.setAutoTxNotifyEnabled(value) }
    }

    /**
     * کلیدِ «یادآورِ روزانه» **هر دو** یادآورِ روزانه را با هم می‌برد: پیامِ بازگشتِ کاربرِ غایب
     * ([UiPrefs.comeBackReminderEnabled]) و «امروز چیزی ثبت نکردی»
     * ([UiPrefs.dailyExpenseReminderEnabled]).
     *
     * ⚠️ قبلاً فقط اولی را ست می‌کرد، پس کاربری که این کلید را خاموش می‌کرد همچنان یادآورِ
     * روزانه می‌گرفت - یعنی کلید کاری را که متنش وعده می‌داد انجام نمی‌داد. دومی هیچ کنترلِ
     * دیگری در برنامه ندارد، پس همین‌جا با هم می‌روند.
     */
    fun setComeBackEnabled(value: Boolean) {
        viewModelScope.launch {
            uiPrefs.setComeBackReminderEnabled(value)
            uiPrefs.setDailyExpenseReminderEnabled(value)
        }
    }

    val dayOffsets: StateFlow<Set<Int>> = uiPrefs.reminderDayOffsets
        .map { parseReminderOffsets(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), setOf(1))

    fun toggleOffset(offset: Int) {
        viewModelScope.launch {
            val current = parseReminderOffsets(uiPrefs.reminderDayOffsets.first())
            val updated = if (offset in current) current - offset else current + offset
            uiPrefs.setReminderDayOffsets(formatReminderOffsets(updated))
        }
    }


}
