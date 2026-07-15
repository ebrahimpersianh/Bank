package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * پورت افکت لمسی وب (`.chip`, `.preset-card`, `.bank-item`, `.loan-card`, `.cta` همه یه
 * transform:scale خفیف رو `:active` دارن - رجوع کن به CLAUDE.md) رو المان‌های قابل‌تپِ native.
 * برخلاف یه `Modifier` ساده‌ی جدا از کلیک، این یکی خودش هم `clickable` رو انجام می‌ده (با یه
 * `interactionSource` مشترک) چون press-state باید دقیقاً همون تعامل کلیک رو ببینه، نه یه
 * `interactionSource` جدای بی‌ربط.
 */
@Composable
fun Modifier.pressScaleClickable(scale: Float = 0.96f, onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (pressed) scale else 1f,
        animationSpec = tween(100),
        label = "pressScale",
    )
    return this
        .graphicsLayer { scaleX = animatedScale; scaleY = animatedScale }
        .clickable(interactionSource = interactionSource, indication = LocalIndication.current, onClick = onClick)
}
