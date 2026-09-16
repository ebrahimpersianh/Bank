package ir.sadteam.loancalc.ui.privacy

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
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

/**
 * رشته‌ی مبلغ رو، اگه حالت خصوصی روشن باشه، با ماسک جایگزین می‌کنه.
 *
 * 🚨 **هفت نقطه، همیشه - مستقل از خودِ عدد** (تصمیمِ دورِ ۱۱). طولِ ثابت **تنها چیزی است
 * که تعدادِ رقم را پنهان می‌کند**: ماسکی که هم‌طولِ عدد باشد، تفاوتِ میلیون و میلیارد را
 * با همان دقتی لو می‌دهد که خودِ عدد می‌گفت. هفت، چون بیشترِ مبالغِ این برنامه شش تا
 * نُه رقم‌اند و هفت نقطه در هر سه حالت عرضی می‌گیرد که چیدمان را نمی‌شکند.
 *
 * ⚠️ پسوندِ «تومان» در خودِ جای مصرف می‌مانَد و اینجا حذف نمی‌شود - بی آن کاربر نمی‌داند
 * چه چیزی پنهان شده.
 */
fun maskIfPrivate(privacyMode: Boolean, formatted: String): String =
    if (privacyMode) MASK_DOTS else formatted

private const val MASK_DOTS = "•••••••"

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

/**
 * **تپ برای نمایشِ کوتاه** (فریمِ دورِ ۱۱، بندِ ۲).
 *
 * تا امروز تنها راهِ دیدنِ یک مبلغِ پنهان، خاموش‌کردنِ **کلِ** حالتِ خصوصی بود - یعنی
 * برای دیدنِ یک عدد، همه‌ی عددهای صفحه با هم لو می‌رفتند.
 *
 * سه تصمیمِ طراح که عمداً رعایت شده‌اند:
 * - **۴ ثانیه.** کمتر برای خواندنِ یک عددِ نُه‌رقمی کم است، بیشتر یعنی عملاً خاموش.
 * - **هیچ نشانه‌ی شمارشِ معکوسی نیست** - نه حلقه، نه عدد. شمارنده‌ی کنارِ مبلغ کاربر را
 *   به عجله وادار می‌کند و چشمش را از خودِ عدد می‌برد. برگشتِ ماسک خودش «وقتش تمام شد»
 *   را می‌گوید.
 * - **دامنه فقط همین یک عدد است** - بقیه‌ی صفحه پنهان می‌مانَد، وگرنه این همان
 *   خاموش‌کردنِ کلِ حالتِ خصوصی است با یک تپِ راحت‌تر.
 *
 * ⚠️ فقط روی چند عددِ **خلاصه** بگذارش (هیروِ کل، وسطِ دونات، خلاصه‌ی گزارش). روی
 * ردیف‌های فهرست نه: تپشان از قبل به جزئیات می‌رود و آن‌جا عدد پنهان نیست.
 */
@Composable
fun RevealOnTap(
    privacyMode: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (masked: Boolean) -> Unit,
) {
    var revealed by remember { mutableStateOf(false) }
    // کلیدِ privacyMode: اگر کاربر خودش حالت را عوض کرد، نمایشِ موقت باید تمام شود.
    LaunchedEffect(revealed, privacyMode) {
        if (revealed) {
            delay(REVEAL_MS)
            revealed = false
        }
    }
    val masked = privacyMode && !revealed
    PrivacyCrossfade(
        privacyMode = masked,
        modifier = if (privacyMode) {
            modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { revealed = true }
        } else {
            modifier
        },
        content = content,
    )
}

private const val REVEAL_MS = 4_000L
