package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
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
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

private val faMonthNamesCalendar = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)
private val faWeekDayShort = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

/**
 * صفحه‌ی گرید تقویم شمسی برای انتخاب دقیق تاریخ - پورت مفهومی از یه ابزار تاریخ‌انتخاب فارسی که
 * کاربر به‌عنوان مرجع فرستاد (نمونه‌ی «رادوو»)، نه کپی کد؛ فقط الگوی گرید+ناوبری ماه/سال+دکمه‌ی
 * «امروز». طول واقعی ماه (با کبیسه‌ی واقعی اسفند) و روز هفته از [JalaliCalendar] گرفته می‌شه.
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
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
            }
            Text("انتخاب تاریخ", color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { stepMonth(-1) }) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "ماه قبل")
            }
            Text(
                "${faMonthNamesCalendar[viewMonth - 1]} ${toFa(viewYear)}",
                color = AppText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(onClick = { stepMonth(1) }) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "ماه بعد")
            }
        }

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
                                        .clickable { selected = thisDate },
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
