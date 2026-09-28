package ir.sadteam.loancalc.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.ui.theme.AppDashedBorder

/**
 * حاشیه‌ی **خط‌چینِ** کارت‌های حالتِ خالی - `border:2px dashed #C9D6CF`ی فایلِ طراحی.
 *
 * Compose خودش `border` خط‌چین نداره، پس با `drawBehind` کشیده می‌شه. رنگ عمداً پارامتره تا
 * کارت‌هایی که تو فریم رنگِ دیگه‌ای دارن هم بتونن از همین استفاده کنن.
 */
@Composable
fun Modifier.dashedBorder(radius: Dp, color: Color = AppDashedBorder, width: Dp = 2.dp): Modifier {
    val ink = color
    return this.drawBehind {
        val stroke = width.toPx()
        val r = radius.toPx()
        drawRoundRect(
            color = ink,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(size.width - stroke, size.height - stroke),
            cornerRadius = CornerRadius(r, r),
            style = Stroke(
                width = stroke,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f, 7f), 0f),
            ),
        )
    }
}
