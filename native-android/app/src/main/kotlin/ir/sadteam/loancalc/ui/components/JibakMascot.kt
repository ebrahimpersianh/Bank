package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.rotateRad
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius

/**
 * **جیبک** - نمادِ برند: کیفِ پولِ سبز با اسکناس و سکه.
 *
 * بندِ ۱۰ سیستمِ طراحی: «کیفِ سبز با اسکناس و سکه. ۱۱۰ در اسپلش، ۹۶ در آنبوردینگ، ۶۴ در
 * حالتِ خالی، ۳۸ در حبابِ راهنما.»
 *
 * ⚠️ این **شخصیتِ کارتونی/آدمک نیست** - تصمیمِ صریحِ قبلیِ کاربر («بدونِ آدمک/مسکات») سرِ جاشه؛
 * این فقط یه شیِ ساده‌ی برندیه.
 *
 * همه‌ی نسبت‌ها از فریمِ `15b` برداشته شدن (جعبه‌ی ۳۸×۲۹ داخلِ قابِ ۶۴):
 * بدنه ۳۸×۲۲ گوشه‌ی `5 5 10 10` · درزِ خط‌چین ۱٫۲ · درِ کیف ۲۷×۱۴ با چرخشِ ۴− درجه ·
 * سکه‌ی ۱۶ رو گوشه‌ی بالا.
 */
@Composable
fun JibakMascot(size: Dp, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            // نسبت‌ها بر پایه‌ی عرضِ ۳۸ واحدِ طرح.
            fun u(v: Float) = w * v / 38f
            val bodyTop = h - u(22f)

            // بدنه‌ی کیف
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF3DDC96), Color(0xFF0EA968), Color(0xFF07724A)),
                    start = Offset(0f, bodyTop),
                    end = Offset(w, h),
                ),
                topLeft = Offset(0f, bodyTop),
                size = Size(w, u(22f)),
                cornerRadius = CornerRadius(u(8f), u(8f)),
            )
            // درزِ خط‌چینِ روی بدنه
            drawLine(
                color = Color.White.copy(alpha = 0.4f),
                start = Offset(u(4f), h - u(4f)),
                end = Offset(w - u(4f), h - u(4f)),
                strokeWidth = u(1.2f),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(u(3f), u(3f)), 0f),
            )
            // درِ کیف - کمی کج، دقیقاً مثلِ طرح
            rotateRad(radians = -0.07f, pivot = Offset(u(2f), bodyTop + u(14f))) {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF2FBF83), Color(0xFF07724A)),
                        start = Offset(u(2f), bodyTop),
                        end = Offset(u(29f), bodyTop + u(14f)),
                    ),
                    topLeft = Offset(u(2f), bodyTop - u(4f)),
                    size = Size(u(27f), u(14f)),
                    cornerRadius = CornerRadius(u(6f), u(6f)),
                )
            }
        }
        // سکه‌ی گوشه - همون آیکونِ سکه‌ی برند، نه یه دایره‌ی جدا.
        CoinIcon(
            size = size * (16f / 64f),
            modifier = Modifier.align(Alignment.TopStart),
        )
    }
}

/**
 * قابِ استانداردِ مسکات تو حالتِ خالی - مربعِ ۶۴ با گوشه‌ی ۲۰ و ته‌رنگِ سبزِ کم‌رنگ
 * (`rgba(61,220,150,.14)` تو تیره، `#E9F7EF` تو روشن - هر دو همون `AppPrimaryPill`).
 */
@Composable
fun JibakMascotFrame(modifier: Modifier = Modifier, frameSize: Dp = 64.dp) {
    Box(
        modifier = modifier
            .size(frameSize)
            .clip(RoundedCornerShape(AppRadius.card))
            .background(AppPrimaryPill),
        contentAlignment = Alignment.Center,
    ) {
        JibakMascot(size = frameSize * (38f / 64f))
    }
}
