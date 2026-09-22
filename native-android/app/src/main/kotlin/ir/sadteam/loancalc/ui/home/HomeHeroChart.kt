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
import ir.sadteam.loancalc.ui.components.InteractiveBars
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
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
    /** برچسبِ هر میله برای حبابِ لمس - خالی یعنی نمودار لمس‌پذیر نیست. */
    labels: List<String> = emptyList(),
) {
    // ⚠️ **حالتِ خالی**: اگه کلِ هفته خرجی نبوده، هر هفت میله ارتفاعِ صفر می‌گیرن و یه نوارِ
    // ۳۴ پیکسلیِ **کاملاً خالی** وسطِ کارتِ قهرمان جا می‌مونه - رو گوشیِ واقعی مثلِ یه سوراخ
    // دیده می‌شه (گزارشِ کاربر رو بیلدِ ۴۶۸). قاعده‌ی بخشِ ۳۳ طرح: هیچ‌جا «داده‌ای موجود نیست»
    // نوشته نمی‌شه، ولی حالتِ خالی هم حق نداره شبیهِ خرابی باشه.
    if (values.none { it > 0.0 }) {
        Text(
            "این هفته هنوز خرجی ثبت نکردی",
            color = Color.White.copy(alpha = 0.72f),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = modifier.fillMaxWidth().padding(top = 2.dp),
        )
        return
    }
    InteractiveBars(
        values = values,
        labels = labels,
        valueLabel = { value -> "${value.rialToFaCompact()} تومان" },
        currentIndex = values.lastIndex,
        barColor = Color.White.copy(alpha = 0.32f),
        currentBarColor = Color.White,
        tooltipBackground = Color.Black.copy(alpha = 0.45f),
        tooltipTitleColor = Color.White.copy(alpha = 0.75f),
        tooltipValueColor = Color.White,
        modifier = modifier,
        // 🚨 **۲۶ نه ۳۴** (بازخوردِ ۳۱ شهریور با طرحِ مرجع): نمودار حالا **کنارِ**
        // جفتِ درآمد/خرج می‌نشیند نه زیرِ کلِ کارت، پس عرضش کمتر است و میله‌ی
        // بلند در عرضِ کم، کارت را دراز می‌کرد.
        height = 26.dp,
    )
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
