package ir.sadteam.loancalc.ui.privacy

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.prefs.UiPrefs
import ir.sadteam.loancalc.ui.theme.Motion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
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

    /**
     * 🚨 **باگِ رفع‌شده (گزارشِ کاربر: «یه‌بار روشن می‌کنی، دیگه خاموش نمی‌شه»)**
     *
     * قبلاً این تابع از `enabled.value` می‌خوند. ولی `enabled` یه `stateIn(WhileSubscribed)`ه و
     * هر تب **نمونه‌ی ViewModelِ خودش** رو می‌گیره، در حالی که خودِ صفحه مقدار رو از
     * [LocalPrivacyMode] (نمونه‌ی `MainActivity`) می‌خونه - یعنی رو نمونه‌ی تب هیچ‌کس
     * `enabled` رو collect نمی‌کنه، پس `.value` همیشه رو مقدارِ اولیه‌ی `false` می‌مونه و
     * `!false` هر بار «روشن کن» می‌شد. حالا مقدارِ واقعی مستقیم از DataStore خونده می‌شه.
     */
    fun toggle() {
        viewModelScope.launch {
            uiPrefs.setPrivacyModeEnabled(!uiPrefs.privacyModeEnabled.first())
        }
    }
}

val LocalPrivacyMode = staticCompositionLocalOf { false }

/** رشته‌ی مبلغ رو، اگه حالت خصوصی روشن باشه، با «•••» جایگزین می‌کنه؛ وگرنه دست‌نخورده برمی‌گردونه. */
fun maskIfPrivate(privacyMode: Boolean, formatted: String): String =
    if (privacyMode) "•••••" else formatted

/**
 * قبلاً فعال/غیرفعال‌کردنِ حالتِ خصوصی باعثِ عوض‌شدنِ یهوییِ مبلغ↔«•••» می‌شد. این کامپوننت هر
 * جایی که یه مبلغِ ماسک‌پذیر رندر می‌شه رو می‌پیچه و بینِ دو حالت یه محوشدنِ نرم اجرا می‌کنه -
 * بدونِ نیاز به تغییرِ کامپوننت‌های پایین‌دست (StatBox، ProgressRing و...): محتوای واقعی
 * (`content`) دقیقاً همون چیزیه که قبلاً بود، فقط به‌جای خواندنِ مستقیمِ `privacyMode` از
 * پارامترِ `masked` که این کامپوننت پاس می‌ده استفاده کن (تا هر دو فریمِ درحالِ‌محوشدن مقدارِ
 * درستِ خودشون رو نشون بدن).
 */
@Composable
fun PrivacyCrossfade(
    privacyMode: Boolean,
    // اگه این ردیف قراره تویِ یه Row/Column با `weight(...)` جا بگیره، اون weight باید رو *این*
    // modifier اعمال بشه، نه رو کامپوننتِ داخلیِ محتوا - چون `AnimatedContent` (نه محتوای توش)
    // فرزندِ واقعیِ Row/Columnه.
    modifier: Modifier = Modifier,
    content: @Composable (masked: Boolean) -> Unit,
) {
    AnimatedContent(
        targetState = privacyMode,
        transitionSpec = { fadeIn(tween(Motion.FADE_IN_MS)) togetherWith fadeOut(tween(Motion.FADE_OUT_MS)) },
        modifier = modifier,
        label = "privacyCrossfade",
    ) { masked -> content(masked) }
}
