package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.toFa

private val inlineMonthNames = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

/**
 * انتخاب تاریخ شمسی به‌صورت اینلاین (سه دراپ‌داونِ روز/ماه/سال) - تغییر تاریخ بدون رفتن به یه صفحه‌ی
 * جدا (خواسته‌ی کاربر). طول واقعی ماه (کبیسه‌ی اسفند) از [JalaliCalendar] گرفته می‌شه و اگه با عوض
 * شدن ماه/سال روزِ فعلی از تعداد روزهای ماه بیشتر بشه، به آخرین روز کلمپ می‌شه.
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
    val maxDay = JalaliCalendar.daysInMonth(year, month)
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DateDropdown(
            options = (1..maxDay).map { it to toFa(it) },
            selected = day.coerceAtMost(maxDay),
            onSelect = { onDateChange(year, month, it) },
            modifier = Modifier.weight(1f),
        )
        DateDropdown(
            options = inlineMonthNames.mapIndexed { idx, name -> (idx + 1) to name },
            selected = month,
            onSelect = { m ->
                val newMax = JalaliCalendar.daysInMonth(year, m)
                onDateChange(year, m, day.coerceAtMost(newMax))
            },
            modifier = Modifier.weight(1.3f),
        )
        DateDropdown(
            options = yearRange.map { it to toFa(it) },
            selected = year,
            onSelect = { y ->
                val newMax = JalaliCalendar.daysInMonth(y, month)
                onDateChange(y, month, day.coerceAtMost(newMax))
            },
            modifier = Modifier.weight(1f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateDropdown(
    options: List<Pair<Int, String>>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selected }?.second ?: ""
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            modifier = Modifier.menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, label) ->
                DropdownMenuItem(text = { Text(label) }, onClick = { onSelect(value); expanded = false })
            }
        }
    }
}
