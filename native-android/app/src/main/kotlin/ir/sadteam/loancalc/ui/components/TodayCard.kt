package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

val todayCardWeekDayNames = listOf("شنبه", "یک‌شنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")

private val todayCardMonthNames = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

fun persianMonthName(month: Int): String = todayCardMonthNames[(month - 1).coerceIn(0, 11)]

/**
 * کارتِ «امروز» (ناوبرِ روز + میان‌برِ سریعِ قسط/چک/یادداشت) - اولش فقط تویِ HomeScreen بود، حالا
 * چون کاربر خواستِ تبِ «سررسید» هم دقیقاً همینو داشته باشه (رجوع کن به CLAUDE.md)، به یه کامپوننتِ
 * مشترک منتقل شد تا کدش تکرار نشه.
 */
@Composable
fun TodayCard(
    date: PersianDate,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onDateClick: () -> Unit,
    onAddInstallment: () -> Unit,
    onAddCheque: () -> Unit,
    onAddNote: () -> Unit,
) {
    val weekDay = todayCardWeekDayNames[JalaliCalendar.dayOfWeekSaturdayFirst(date)]
    val dateText = "$weekDay، ${toFa(date.d)} ${persianMonthName(date.m)}"

    AppCard(label = "امروز") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // خواسته‌ی صریحِ کاربر: تپ رو تاریخ صفحه‌ی تقویمِ کامل باز کنه، و فلش‌های قبلی/بعدی کنارِ
            // هم باشن (نه دو سرِ ردیف) - برای همین تاریخ با weight فضای باقی‌مونده رو می‌گیره و هر دو
            // فلش تویِ یه Rowِ مجزا کنارِ هم می‌شینن.
            Text(
                dateText,
                color = AppText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).pressScaleClickable(onClick = onDateClick),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrevDay) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "روزِ قبل")
                }
                IconButton(onClick = onNextDay) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "روزِ بعد")
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TodayQuickAction("قسط", Icons.Filled.Payments, Modifier.weight(1f), onAddInstallment)
            TodayQuickAction("چک", Icons.Filled.ReceiptLong, Modifier.weight(1f), onAddCheque)
            TodayQuickAction("یادداشت", Icons.Filled.EditNote, Modifier.weight(1f), onAddNote)
        }
    }
}

@Composable
private fun TodayQuickAction(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .background(AppPrimary.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .pressScaleClickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = AppPrimary, modifier = Modifier.size(20.dp))
        Text(label, color = AppPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
    }
}
