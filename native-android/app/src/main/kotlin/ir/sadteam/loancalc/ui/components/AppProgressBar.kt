package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.layout

/**
 * نوارِ پیشرفتِ خودیِ اپ - جایگزینِ `LinearProgressIndicator`ِ پیش‌فرضِ متریال (که تخت‌رنگ بود و
 * با زبانِ گرادیانی/گردِ بقیه‌ی اپ جور نبود). خواسته‌ی صریحِ کاربر (بستهٔ ارتقاهای گرافیکی).
 *
 * تفاوت‌ها با نسخه‌ی متریال:
 * - پرشدن **گرادیانیه** (تیره→روشنِ همون رنگ) نه تخت‌رنگ.
 * - گوشه‌های هم ریل هم خودِ نوار کاملاً گِردن.
 * - تغییرِ مقدار **انیمیشن داره** (نه پرشِ ناگهانی).
 *
 * @param fraction بینِ ۰ تا ۱ (خودش clamp می‌شه).
 * @param color رنگِ پایه؛ گرادیان از نسخه‌ی کم‌رنگ‌ترش به خودش ساخته می‌شه.
 */
@Composable
fun AppProgressBar(
    fraction: Float,
    color: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
) {
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "appProgressBar",
    )
    val shape = RoundedCornerShape(percent = 50)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(trackColor),
    ) {
        Box(
            modifier = Modifier
                // به‌جای fillMaxWidth(animated) - که برای کسرِ صفر یه گرهِ با عرضِ صفر می‌سازه و
                // گوشه‌ی گِرد رو بد نشون می‌ده - عرض مستقیم از عرضِ والد حساب می‌شه.
                .layout { measurable, constraints ->
                    val w = (constraints.maxWidth * animated).toInt().coerceAtLeast(0)
                    val placeable = measurable.measure(constraints.copy(minWidth = w, maxWidth = w))
                    layout(constraints.maxWidth, placeable.height) { placeable.place(0, 0) }
                }
                .fillMaxHeight()
                .clip(shape)
                .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.55f), color))),
        )
    }
}
