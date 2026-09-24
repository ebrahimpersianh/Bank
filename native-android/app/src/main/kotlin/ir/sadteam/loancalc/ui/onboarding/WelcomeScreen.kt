package ir.sadteam.loancalc.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * ورودِ صفحه‌ی اصلی، بلافاصله بعدِ محوشدنِ اسپلش.
 *
 * قبلاً یه «box-swoosh» بود (از بالا-چپ سُر می‌خورد تو). به‌خواستِ کاربر («بعد از اسپلش یه انیمیشن،
 * ولی خب سریع وارد برنامه بشیم») عوض شد به یه بزرگ‌شدنِ خیلی کوتاهِ محو (۰.۹۶ → ۱) که دقیقاً
 * ادامه‌ی همون بزرگ‌شدن/محوشدنِ خروجِ [SplashIntroScreen]ه - پس گذار یکپارچه دیده می‌شه، نه دو تا
 * انیمیشنِ بی‌ربط. مدت عمداً کوتاهه (۳۰۰ میلی‌ثانیه) تا حسِ معطلی نده.
 *
 * چون [MutableTransitionState] از false شروع و بلافاصله targetState=true می‌شه، این انیمیشن خودکار
 * رو اولین composition اجرا می‌شه.
 */
@Composable
fun AnimatedAppEntrance(content: @Composable () -> Unit) {
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
    val spec = tween<Float>(300, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f))
    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(spec) + scaleIn(spec, initialScale = 0.96f),
    ) {
        content()
    }
}
