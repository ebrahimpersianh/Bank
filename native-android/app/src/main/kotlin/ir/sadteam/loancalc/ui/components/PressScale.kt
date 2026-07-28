package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.ui.haptics.rememberBuzz
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.Motion

/**
 * پورت افکت لمسی وب (`.chip`, `.preset-card`, `.bank-item`, `.loan-card`, `.cta` همه یه
 * transform:scale خفیف رو `:active` دارن - رجوع کن به CLAUDE.md) رو المان‌های قابل‌تپِ native.
 * برخلاف یه `Modifier` ساده‌ی جدا از کلیک، این یکی خودش هم `clickable` رو انجام می‌ده (با یه
 * `interactionSource` مشترک) چون press-state باید دقیقاً همون تعامل کلیک رو ببینه.
 *
 * علاوه بر press-scale، حالا دو تا چیز جدید هم اضافه شده (به‌درخواست کاربر «برنامه خیلی خشکه»):
 *  - **هپتیک**: موقع فشردن یه tick ظریف حس می‌شه.
 *  - **قاب طلایی روی تپ**: اگه [goldBorderShape] داده بشه، تا وقتی انگشت روی المانه یه حاشیه‌ی
 *    طلایی (AppAccent) دورش می‌افته و با رها کردن محو می‌شه.
 */
@Composable
fun Modifier.pressScaleClickable(
    scale: Float = 0.96f,
    goldBorderShape: Shape? = null,
    onClick: () -> Unit,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val buzz = rememberBuzz()
    LaunchedEffect(pressed) {
        if (pressed) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            buzz()
        }
    }
    // فنری به‌جای خطی: موقعِ رها کردن یه برگشتِ خیلی ریزِ کِش‌مانند داره، دقیقاً همون حسی که
    // دکمه‌های iOS می‌دن - رجوع کن به Motion.kt برای دلیلِ کاملِ فنر در برابر tween.
    val animatedScale by animateFloatAsState(
        targetValue = if (pressed) scale else 1f,
        animationSpec = Motion.snappy(),
        label = "pressScale",
    )
    val gold = AppAccent
    val borderColor by animateColorAsState(
        targetValue = if (pressed && goldBorderShape != null) gold else Color.Transparent,
        animationSpec = tween(120),
        label = "pressGoldBorder",
    )
    return this
        .graphicsLayer { scaleX = animatedScale; scaleY = animatedScale }
        .then(if (goldBorderShape != null) Modifier.border(2.dp, borderColor, goldBorderShape) else Modifier)
        .clickable(interactionSource = interactionSource, indication = LocalIndication.current, onClick = onClick)
}
