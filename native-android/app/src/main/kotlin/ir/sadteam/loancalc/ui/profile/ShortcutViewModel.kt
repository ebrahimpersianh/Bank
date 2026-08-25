package ir.sadteam.loancalc.ui.profile

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
 * ترتیبِ میان‌برهای **کشوی میان‌بُر** (بخشِ ۳۱ فایلِ طراحی).
 *
 * فقط شناسه‌ها نگه داشته می‌شن، نه خودِ آیکون/برچسب - تا اضافه/کم‌شدنِ یه میان‌بر تو نسخه‌های
 * بعدی ترتیبِ ذخیره‌شده رو خراب نکنه. شناسه‌ی ناشناخته بی‌صدا نادیده گرفته و شناسه‌ی جدید ته
 * لیست اضافه می‌شه.
 */
@HiltViewModel
class ShortcutViewModel @Inject constructor(private val uiPrefs: UiPrefs) : ViewModel() {
    val order: StateFlow<List<String>> = uiPrefs.shortcutOrder
        .map { raw -> raw?.split(',')?.filter { it.isNotBlank() } ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(ids: List<String>) {
        viewModelScope.launch { uiPrefs.setShortcutOrder(ids) }
    }
}
