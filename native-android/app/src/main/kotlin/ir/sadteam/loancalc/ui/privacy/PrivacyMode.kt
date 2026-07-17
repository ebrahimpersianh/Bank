package ir.sadteam.loancalc.ui.privacy

import androidx.compose.runtime.staticCompositionLocalOf
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
 * حالت خصوصی (Privacy Mode): با زدن آیکون چشم تو نوار بالا، همه‌ی مبلغ‌های صفحه پشت «•••» مخفی
 * می‌شن (برای وقتی گوشیتو دستِ کسی می‌دی). مثل ThemeViewModel، یه‌بار تو LoanCalcApp جمع‌آوری و از
 * طریق [LocalPrivacyMode] به همه‌ی صفحات پایین‌دست می‌رسه.
 */
@HiltViewModel
class PrivacyModeViewModel @Inject constructor(
    private val uiPrefs: UiPrefs,
) : ViewModel() {
    val enabled: StateFlow<Boolean> = uiPrefs.privacyModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggle() {
        viewModelScope.launch {
            uiPrefs.setPrivacyModeEnabled(!enabled.value)
        }
    }
}

val LocalPrivacyMode = staticCompositionLocalOf { false }

/** رشته‌ی مبلغ رو، اگه حالت خصوصی روشن باشه، با «•••» جایگزین می‌کنه؛ وگرنه دست‌نخورده برمی‌گردونه. */
fun maskIfPrivate(privacyMode: Boolean, formatted: String): String =
    if (privacyMode) "•••••" else formatted
