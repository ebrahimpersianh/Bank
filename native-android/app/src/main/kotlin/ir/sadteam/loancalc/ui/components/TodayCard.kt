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
import androidx.compose.material.icons.filled.CalendarMonth
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
import ir.sadteam.loancalc.ui.theme.AppMuted
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

    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // چیدمانِ سرِ کارت دقیقاً هم‌شکلِ رفرنس: آیکونِ تقویم + «امروز» تو خطِ اول، خودِ تاریخ
            // (کم‌رنگ‌تر/کوچیک‌تر) خطِ دوم، و هر دو فلش کنارِ هم سمتِ دیگه‌ی ردیف. تپ رو همین بلوک،
            // صفحه‌ی تقویمِ کامل رو باز می‌کنه (خواسته‌ی قبلیِ کاربر).
            Column(modifier = Modifier.weight(1f).pressScaleClickable(onClick = onDateClick)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = AppText,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        "امروز",
                        color = AppText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
                Text(
                    dateText,
                    color = AppMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
            // ترتیبِ فلش‌ها هم‌شکلِ رفرنس - تو RTL آیتمِ اولِ Row سمتِ راست می‌شینه، پس «بعدی» (>)
            // راست و «قبلی» (<) چپ دیده می‌شه.
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNextDay) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "روزِ بعد")
                }
                IconButton(onClick = onPrevDay) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "روزِ قبل")
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            TodayQuickAction("یادداشت", Icons.Filled.EditNote, onAddNote)
            TodayQuickAction("چک", Icons.Filled.ReceiptLong, onAddCheque)
            TodayQuickAction("قسط", Icons.Filled.Payments, onAddInstallment)
        }
    }
}

/** دکمه‌ی کپسولیِ میان‌بر - هم‌شکلِ رفرنس: آیکون و نوشته کنارِ هم (نه زیرِ هم)، عرض به‌اندازه‌ی
 * محتوا (نه تقسیمِ مساویِ عرضِ کارت). */
@Composable
private fun TodayQuickAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .background(AppPrimary.copy(alpha = 0.12f), RoundedCornerShape(50))
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = label, tint = AppPrimary, modifier = Modifier.size(17.dp))
        Text(
            label,
            color = AppPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}
