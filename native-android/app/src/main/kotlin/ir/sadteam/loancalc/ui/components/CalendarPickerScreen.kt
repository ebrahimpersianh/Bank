package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText

private val faMonthNamesCalendar = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)
private val faWeekDayShort = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

/** بازه‌ی سال‌های قابل‌انتخاب تو گرید سال (نزولی نشون داده می‌شه: ۱۴۱۰ بالا، ۱۳۵۰ پایین). */
private val calendarYearRange = 1350..1410

private enum class CalendarMode { DAYS, MONTHS, YEARS }

/**
 * صفحه‌ی گرید تقویم شمسی برای انتخاب دقیق تاریخ - پورت مفهومی از یه ابزار تاریخ‌انتخاب فارسی که
 * کاربر به‌عنوان مرجع فرستاد (نمونه‌ی «رادوو»)، نه کپی کد؛ فقط الگوی گرید+ناوبری ماه/سال+دکمه‌ی
 * «امروز». طول واقعی ماه (با کبیسه‌ی واقعی اسفند) و روز هفته از [JalaliCalendar] گرفته می‌شه.
 *
 * تپ روی خودِ اسم ماه تو هدر → گرید ۱۲ ماه؛ تپ روی سال → گرید سال‌های [calendarYearRange] (اسکرول
 * می‌شه، ۱۴۱۰ تا ۱۳۵۰). بعد از انتخاب ماه/سال به گرید روزها برمی‌گرده. این جایگزین «فقط با فلش
 * ماه‌به‌ماه جلو/عقب رفتن» قبلیه که برای رسیدن به یه سال دور خیلی کند بود.
 */
@Composable
fun CalendarPickerScreen(
    initialDate: PersianDate,
    onDateSelected: (PersianDate) -> Unit,
    onBack: () -> Unit,
) {
    var viewYear by remember { mutableStateOf(initialDate.y) }
    var viewMonth by remember { mutableStateOf(initialDate.m) }
    var selected by remember { mutableStateOf<PersianDate?>(initialDate) }
    var mode by remember { mutableStateOf(CalendarMode.DAYS) }
    val today = remember { JalaliCalendar.today() }

    fun stepMonth(delta: Int) {
        var m = viewMonth + delta
        var y = viewYear
        while (m > 12) { m -= 12; y++ }
        while (m < 1) { m += 12; y-- }
        viewYear = y
        viewMonth = m
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { if (mode == CalendarMode.DAYS) onBack() else mode = CalendarMode.DAYS }) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
            }
            Text("انتخاب تاریخ", color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
        }

        // هدر ماه/سال - هر کدوم جدا قابل‌تپه: اسم ماه → گرید ماه، سال → گرید سال.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { stepMonth(-1) },
                enabled = mode == CalendarMode.DAYS,
            ) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "ماه قبل")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                HeaderChip(
                    label = faMonthNamesCalendar[viewMonth - 1],
                    active = mode == CalendarMode.MONTHS,
                    onClick = { mode = if (mode == CalendarMode.MONTHS) CalendarMode.DAYS else CalendarMode.MONTHS },
                )
                HeaderChip(
                    label = toFa(viewYear),
                    active = mode == CalendarMode.YEARS,
                    onClick = { mode = if (mode == CalendarMode.YEARS) CalendarMode.DAYS else CalendarMode.YEARS },
                )
            }
            IconButton(
                onClick = { stepMonth(1) },
                enabled = mode == CalendarMode.DAYS,
            ) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "ماه بعد")
            }
        }

        when (mode) {
            CalendarMode.YEARS -> YearGrid(
                current = viewYear,
                onSelect = { y -> viewYear = y; mode = CalendarMode.DAYS },
            )
            CalendarMode.MONTHS -> MonthGrid(
                current = viewMonth,
                onSelect = { m -> viewMonth = m; mode = CalendarMode.DAYS },
            )
            CalendarMode.DAYS -> DayGrid(
                viewYear = viewYear,
                viewMonth = viewMonth,
                selected = selected,
                today = today,
                onSelect = { selected = it },
            )
        }

        if (mode == CalendarMode.DAYS) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = {
                    viewYear = today.y
                    viewMonth = today.m
                    selected = today
                }) { Text("امروز") }

                OutlinedButton(onClick = { selected?.let(onDateSelected) }) {
                    Text("تایید")
                }
            }
        }
    }
}

@Composable
private fun HeaderChip(label: String, active: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (active) AppPrimary else AppText,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(
                if (active) AppPrimary.copy(alpha = 0.12f) else AppSurface2,
                RoundedCornerShape(8.dp),
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun DayGrid(
    viewYear: Int,
    viewMonth: Int,
    selected: PersianDate?,
    today: PersianDate,
    onSelect: (PersianDate) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
        faWeekDayShort.forEach { label ->
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(label, color = AppMuted, fontSize = 12.sp)
            }
        }
    }

    val daysInMonth = JalaliCalendar.daysInMonth(viewYear, viewMonth)
    val firstDow = JalaliCalendar.dayOfWeekSaturdayFirst(PersianDate(viewYear, viewMonth, 1))
    val totalCells = firstDow + daysInMonth
    val rowCount = (totalCells + 6) / 7

    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
        for (row in 0 until rowCount) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val day = cellIndex - firstDow + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (day in 1..daysInMonth) {
                            val thisDate = PersianDate(viewYear, viewMonth, day)
                            val isSelected = selected == thisDate
                            val isToday = today == thisDate
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .background(
                                        if (isSelected) AppPrimary else AppBg,
                                        CircleShape,
                                    )
                                    .pressScaleClickable(scale = 0.9f) { onSelect(thisDate) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    toFa(day),
                                    color = if (isSelected) AppBg else if (isToday) AppPrimary else AppText,
                                    fontSize = 13.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YearGrid(current: Int, onSelect: (Int) -> Unit) {
    // نزولی: جدیدترین سال بالا (۱۴۱۰)، قدیمی‌ترین پایین (۱۳۵۰) - مطابق خواسته‌ی کاربر.
    val years = remember { calendarYearRange.reversed().toList() }
    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = years.indexOf(current).coerceAtLeast(0),
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        state = gridState,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(years, key = { it }) { year ->
            GridCell(label = toFa(year), selected = year == current, onClick = { onSelect(year) })
        }
    }
}

@Composable
private fun MonthGrid(current: Int, onSelect: (Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (row in 0 until 4) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (col in 0 until 3) {
                    val monthIndex = row * 3 + col
                    val monthNumber = monthIndex + 1
                    Box(modifier = Modifier.weight(1f)) {
                        GridCell(
                            label = faMonthNamesCalendar[monthIndex],
                            selected = monthNumber == current,
                            onClick = { onSelect(monthNumber) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GridCell(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) AppPrimary else AppSurface2,
                RoundedCornerShape(10.dp),
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (selected) AppBg else AppText,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
