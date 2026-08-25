package ir.sadteam.loancalc.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * نمودارِ میله‌ایِ ۷ روزِ گذشته - داخلِ کارتِ قهرمانِ سبزِ تبِ خانه (کارتِ `15a`ی فایلِ طراحی).
 *
 * مقادیرِ دقیقِ طرح: ارتفاعِ کلِ ۳۴ · فاصله‌ی میله‌ها ۳ · گوشه‌ی بالای میله ۳ · میله‌های گذشته
 * `rgba(255,255,255,.32)` و **میله‌ی امروز سفیدِ توپر** · برچسب‌های زیرش ۸٫۵ (چپ `.6`، راست `.85`).
 *
 * @param values خرجِ هر روز، از قدیمی‌ترین تا امروز. طولش باید ۷ باشه ولی کمتر/بیشتر هم می‌پذیره.
 */
@Composable
fun HomeSevenDayChart(
    values: List<Double>,
    modifier: Modifier = Modifier,
) {
    val max = values.maxOrNull()?.takeIf { it > 0.0 } ?: 1.0
    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            values.forEachIndexed { index, value ->
                val isToday = index == values.lastIndex
                // کسرِ ارتفاع با انیمیشن بالا میاد - همون حسِ نموداری که بقیه‌ی نمودارهای اپ دارن.
                val target = (value / max).toFloat().coerceIn(0.06f, 1f)
                val fraction by animateFloatAsState(target, tween(520), label = "bar$index")
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(fraction)
                        .background(
                            color = if (isToday) Color.White else Color.White.copy(alpha = 0.32f),
                            shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp),
                        ),
                )
            }
        }
    }
}

/** ردیفِ برچسبِ زیرِ نمودار - «۷ روزِ گذشته» چپ‌رنگ‌تر، «امروز» پررنگ‌تر. طبقِ طرح. */
@Composable
fun HomeSevenDayChartLabels(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            "۷ روزِ گذشته",
            color = Color.White.copy(alpha = 0.60f),
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "امروز",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 8.5.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}
