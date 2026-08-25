package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill

private val inlineMonthNames = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

private val InlineWheelItemHeight = 34.dp

/**
 * انتخاب تاریخ شمسی به‌صورت اینلاین، چرخونه‌ای (spinner) - قبلاً سه دراپ‌داون بود؛ کاربر چندبار
 * توضیح داد منظورش این بود که همینِ حالتِ چرخونه‌ی تمام‌صفحه‌ی [WheelDatePickerScreen] رو، بدون
 * رفتن به صفحه‌ی جدا، مستقیم همینجا تو کارتِ «تاریخ دریافت وام» داشته باشیم - فقط با اسکرول‌کردنِ
 * روی هرکدوم از سه ستون تنظیم می‌شه (همون [WheelColumn] با ارتفاع/تعداد ردیفِ کوچیک‌تر که تو یه
 * کارتِ کوچیک جا بشه)، بدون هیچ ناوبری‌ای.
 */
@Composable
fun InlineJalaliDateRow(
    year: Int,
    month: Int,
    day: Int,
    onDateChange: (year: Int, month: Int, day: Int) -> Unit,
    modifier: Modifier = Modifier,
    yearRange: IntRange = 1350..1410,
) {
    val years = remember(yearRange) { yearRange.toList() }
    val maxDay = JalaliCalendar.daysInMonth(year, month)
    val days = remember(maxDay) { (1..maxDay).toList() }
    val safeDay = day.coerceAtMost(maxDay)

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        // نوار هایلایتِ ردیفِ وسط - عینِ چرخونه‌ی تمام‌صفحه، فقط جمع‌وجورتر.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(InlineWheelItemHeight)
                .background(AppPrimaryPill, RoundedCornerShape(10.dp))
                .border(1.dp, AppPrimary.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            WheelColumn(
                items = days.map { toFa(it) },
                selectedIndex = days.indexOf(safeDay).coerceAtLeast(0),
                onCentered = { onDateChange(year, month, days[it]) },
                modifier = Modifier.weight(1f),
                itemHeight = InlineWheelItemHeight,
                visibleRows = 3,
            )
            WheelColumn(
                items = inlineMonthNames,
                selectedIndex = month - 1,
                onCentered = { idx ->
                    val newMax = JalaliCalendar.daysInMonth(year, idx + 1)
                    onDateChange(year, idx + 1, safeDay.coerceAtMost(newMax))
                },
                modifier = Modifier.weight(1.3f),
                itemHeight = InlineWheelItemHeight,
                visibleRows = 3,
            )
            WheelColumn(
                items = years.map { toFa(it) },
                selectedIndex = years.indexOf(year).coerceAtLeast(0),
                onCentered = { idx ->
                    val y = years[idx]
                    val newMax = JalaliCalendar.daysInMonth(y, month)
                    onDateChange(y, month, safeDay.coerceAtMost(newMax))
                },
                modifier = Modifier.weight(1f),
                itemHeight = InlineWheelItemHeight,
                visibleRows = 3,
            )
        }
    }
}
