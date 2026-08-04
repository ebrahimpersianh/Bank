package ir.sadteam.loancalc.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * پورت افکت «box-swoosh» رقیب: وقتی محتوا (صفحه‌ی اصلی) اولین‌بار ظاهر می‌شه، سریع (۳۵۰ میلی‌ثانیه)
 * از بالا-چپ (offset منفی هم رو X هم رو Y) + fade میاد تو، نه این‌که یهو ظاهر بشه. چون
 * [MutableTransitionState] از false شروع و بلافاصله targetState=true می‌شه، این انیمیشن خودکار رو
 * اولین composition اجرا می‌شه.
 */
@Composable
fun AnimatedAppEntrance(content: @Composable () -> Unit) {
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(tween(350)) +
            slideInHorizontally(animationSpec = tween(350)) { -it / 3 } +
            slideInVertically(animationSpec = tween(350)) { -it / 3 },
    ) {
        content()
    }
}
