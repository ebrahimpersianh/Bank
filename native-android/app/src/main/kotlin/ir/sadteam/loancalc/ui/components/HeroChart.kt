package ir.sadteam.loancalc.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * شکلِ نمودارِ کارت‌های قهرمان. قرار است از **فروشگاه** خریده شود و روی **همه‌ی** کارت‌ها
 * یک‌جا بنشیند (خواسته‌ی کاربر، ۲ مهر).
 */
enum class HeroChartStyle(val itemId: String?) {
    // رایگان - شکلِ پیش‌فرضِ هر صفحه.
    BARS(null),
    LINE(null),

    // خریدنی از فروشگاه (بسته‌ی ChatGPT) - [StyledHeroChart].
    SOFT_WAVE("chart:soft_wave"),
    STEPPED("chart:stepped"),
    DOTS("chart:dots"),
    GRADIENT_COLUMNS("chart:gradient_columns"),
    AREA_WAVE("chart:area_wave"),
    BUBBLES("chart:bubbles"),
    ;

    companion object {
        /** شناسه‌ی ذخیره‌شده ← سبک؛ ناشناخته یا `null` یعنی «پیش‌فرضِ هر صفحه». */
        fun fromItemId(id: String?): HeroChartStyle? = entries.firstOrNull { it.itemId != null && it.itemId == id }
    }
}

/**
 * شکلِ انتخابیِ کاربر. `null` یعنی «پیش‌فرضِ خودِ هر صفحه» ([HeroChart] پارامترِ `natural` را
 * می‌گیرد). ریشه‌ی اپ وقتی فروشگاه وصل شد این را از تنظیمات فراهم می‌کند.
 */
val LocalHeroChartStyle = compositionLocalOf<HeroChartStyle?> { null }

/**
 * **تنها نمودارِ کارت‌های قهرمان.** هر صفحه فقط داده می‌دهد (مقدارها، برچسب‌ها، «حالا»)؛
 * شکل از [LocalHeroChartStyle] می‌آید تا یک نمودارِ خریده‌شده روی خانه، گزارش، بودجه،
 * دارایی و وام یک‌جور بنشیند. رنگ‌ها ثابتِ سفید روی زمینه‌ی قهرمان‌اند.
 *
 * ⚠️ داده همیشه **زمان‌محور، قدیمی چپ ← جدید راست** است؛ هر دو شکل همین را می‌فهمند.
 */
@Composable
fun HeroChart(
    values: List<Double>,
    labels: List<String>,
    valueLabel: (Double) -> String,
    currentIndex: Int,
    natural: HeroChartStyle,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    /** فقط برای شکلِ خطی: طولِ کاملِ محور (مثلاً ۳۰ روز) وقتی داده کمتر است. */
    slots: Int? = null,
    tooltipBackground: Color = Color.Black.copy(alpha = 0.40f),
) {
    val style = LocalHeroChartStyle.current ?: natural
    when (style) {
        HeroChartStyle.BARS -> InteractiveBars(
            values = values,
            labels = labels,
            valueLabel = valueLabel,
            currentIndex = currentIndex,
            barColor = Color.White.copy(alpha = 0.30f),
            currentBarColor = Color.White,
            tooltipBackground = tooltipBackground,
            tooltipTitleColor = Color.White.copy(alpha = 0.75f),
            tooltipValueColor = Color.White,
            modifier = modifier,
            height = height,
            // میله‌های زیاد در عرضِ کارت با فاصله‌ی ۳ جا نمی‌شوند.
            spacing = if (values.size > 12) 1.5.dp else 3.dp,
        )
        HeroChartStyle.LINE -> TrendLineChart(
            values = values,
            lineColor = Color.White,
            fillTop = Color.White.copy(alpha = 0.32f),
            dotColor = Color.White,
            modifier = modifier,
            height = height,
            labels = labels,
            valueLabel = valueLabel,
            tooltipBackground = tooltipBackground,
            slots = slots,
            restIndex = currentIndex,
        )
        else -> StyledHeroChart(
            style = style,
            values = values,
            labels = labels,
            valueLabel = valueLabel,
            currentIndex = currentIndex,
            modifier = modifier,
            height = height,
            slots = slots,
            tooltipBackground = tooltipBackground,
        )
    }
}
