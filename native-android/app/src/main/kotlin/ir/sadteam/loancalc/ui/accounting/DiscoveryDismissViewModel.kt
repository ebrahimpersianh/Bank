package ir.sadteam.loancalc.ui.accounting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.prefs.UiPrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ماندگاریِ ضربدرِ کارت‌های کشفِ تبِ گزارش (فریمِ `52a`).
 *
 * کلیدِ هر مورد `«نوع@سالِ-ماه»` است، پس نادیده‌گرفتن **ماهانه** است نه دائمی - ماهِ بعد
 * دوباره می‌آید. خاموشیِ دائمی یعنی خبری که هیچ‌وقت به کاربر نمی‌رسد.
 */
@HiltViewModel
class DiscoveryDismissViewModel @Inject constructor(
    private val uiPrefs: UiPrefs,
) : ViewModel() {
    val dismissed: StateFlow<Set<String>> = uiPrefs.dismissedDiscoveries
        .map { raw -> raw?.split(',')?.filter { it.isNotBlank() }?.toSet() ?: emptySet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    /** کلیدهای ماه‌های گذشته هرس می‌شوند تا این رشته بی‌انتها رشد نکند. */
    fun dismiss(key: String, monthKey: String) {
        viewModelScope.launch {
            val kept = (dismissed.value + key).filter { it.endsWith("@$monthKey") }
            uiPrefs.setDismissedDiscoveries(kept)
        }
    }
}
