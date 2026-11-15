package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import ir.sadteam.loancalc.ui.text.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppSurface2
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.jibak.faMonthName
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill

// ⚠️ سومین کپیِ نامِ ماه حذف شد - `faMonthName`. (بندِ «یک زبانِ بصری»ِ README.)
private val InlineWheelItemHeight = 34.dp

/** بعد از این‌قدر بی‌کاری، چرخونه‌ی اینلاین دوباره قفل می‌شود تا اسکرولِ صفحه را ندزدد. */
private const val InlineWheelIdleLockMillis = 4_000L

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
    yearRange: IntRange = JalaliYearRange,
) {
    val years = remember(yearRange) { yearRange.toList() }
    val maxDay = JalaliCalendar.daysInMonth(year, month)
    val days = remember(maxDay) { (1..maxDay).toList() }
    val safeDay = day.coerceAtMost(maxDay)

    // یافته‌ی ۴۱: قفل تا لمس (جزئیات در [WheelColumn]). قابِ سبز = باز.
    var active by remember { mutableStateOf(false) }
    var poke by remember { mutableIntStateOf(0) }
    LaunchedEffect(active, poke) {
        if (active) { kotlinx.coroutines.delay(InlineWheelIdleLockMillis); active = false }
    }
    val unlock: () -> Unit = { active = true; poke++ }
    val changed: (Int, Int, Int) -> Unit = { y, m, d -> poke++; onDateChange(y, m, d) }

    // قابِ سطحِ دوم + برچسبِ ستون‌ها (بازطراحیِ ۸ مهر): چرخونه‌ی بی‌قاب وسطِ فرم گم می‌شد.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppSurface2)
            .border(if (active) 1.5.dp else 1.dp, if (active) AppPrimary else AppLine, RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
    Row(Modifier.fillMaxWidth().padding(bottom = 2.dp)) {
        listOf("روز" to 1f, "ماه" to 1.3f, "سال" to 1f).forEach { (label, w) ->
            Text(label, color = AppMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(w))
        }
    }
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            WheelColumn(
                items = days.map { toFa(it) },
                selectedIndex = days.indexOf(safeDay).coerceAtLeast(0),
                onCentered = { changed(year, month, days[it]) },
                modifier = Modifier.weight(1f),
                itemHeight = InlineWheelItemHeight,
                visibleRows = 3,
                scrollEnabled = active,
                onUnlock = unlock,
            )
            WheelColumn(
                items = remember { (1..12).map { faMonthName(it) } },
                selectedIndex = month - 1,
                onCentered = { idx ->
                    val newMax = JalaliCalendar.daysInMonth(year, idx + 1)
                    changed(year, idx + 1, safeDay.coerceAtMost(newMax))
                },
                modifier = Modifier.weight(1.3f),
                itemHeight = InlineWheelItemHeight,
                visibleRows = 3,
                scrollEnabled = active,
                onUnlock = unlock,
            )
            WheelColumn(
                items = years.map { toFa(it) },
                selectedIndex = years.indexOf(year).coerceAtLeast(0),
                onCentered = { idx ->
                    val y = years[idx]
                    val newMax = JalaliCalendar.daysInMonth(y, month)
                    changed(y, month, safeDay.coerceAtMost(newMax))
                },
                modifier = Modifier.weight(1f),
                itemHeight = InlineWheelItemHeight,
                visibleRows = 3,
                scrollEnabled = active,
                onUnlock = unlock,
            )
        }
    }
}
}
