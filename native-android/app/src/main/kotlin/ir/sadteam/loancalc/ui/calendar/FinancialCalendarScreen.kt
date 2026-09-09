package ir.sadteam.loancalc.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.faMonthName
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

// ⚠️ فهرستِ محلیِ نامِ ماه حذف شد - پنجمین کپی در برنامه بود. `faMonthName(m)`ِ
// `ui.jibak` همان است و `CalendarPickerScreen` از قبل از همان می‌خورد.
private val faWeekDayShort = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

/** ذخیره و محاسبه ریال، نمایش تومان - قاعده‌ی ۵ی README. */
private fun amountToman(rial: Double): String = fmt(rialToToman(rial.toLong()).toDouble()).faDigits()

/**
 * پورت مفهومی «تقویم مالی» اپ رقیب (VAMMAN) - گرید تقویم شمسی که روزهای دارای سررسید قسط رو با یه
 * نقطه‌ی رنگی مشخص می‌کنه (قرمز = حداقل یه قسط پرداخت‌نشده اون روز، سبز = همه‌ی اقساط اون روز
 * پرداخت‌شده)؛ تپ رو یه روز، لیست اقساط همون روز رو پایین گرید نشون می‌ده. سررسیدها از
 * [FinancialCalendarViewModel.dueItemsByDate] میان که خودش از منطق موجود getRows (LoanRepository)
 * استفاده می‌کنه - محاسبه‌ی سررسید تکرار/تغییر داده نشده.
 */
@Composable
fun FinancialCalendarScreen(onBack: () -> Unit, viewModel: FinancialCalendarViewModel = hiltViewModel()) {
    val loans by viewModel.loans.collectAsState()
    // dueItemsByDate دیگه نمی‌تونه محاسبه‌ی همزمان (remember{}) باشه چون از رو رَدیف‌های واقعیِ Room
    // (loan_rows) می‌خونه، نه دیگه از رو JSONِ درون‌حافظه‌ای - رجوع کن به CLAUDE.md.
    var dueMap by remember { mutableStateOf<Map<PersianDate, List<DueItem>>>(emptyMap()) }
    LaunchedEffect(loans) {
        dueMap = viewModel.dueItemsByDate(loans)
    }
    val today = remember { JalaliCalendar.today() }

    var viewYear by remember { mutableIntStateOf(today.y) }
    var viewMonth by remember { mutableIntStateOf(today.m) }
    var selectedDate by remember { mutableStateOf<PersianDate?>(today) }
    val privacyMode = LocalPrivacyMode.current

    fun stepMonth(delta: Int) {
        var m = viewMonth + delta
        var y = viewYear
        while (m > 12) { m -= 12; y++ }
        while (m < 1) { m += 12; y-- }
        viewYear = y
        viewMonth = m
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
            }
            Text(
                "تقویم مالی",
                color = AppText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp),
            )
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
                "${faMonthName(viewMonth)} ${toFa(viewYear)}",
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
        val rowCount = (firstDow + daysInMonth + 6) / 7

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
                                // ⚠️ `.padding()` **بیرونِ** `pressScaleClickable`ِ داخلی
                                // می‌مانَد (قاعده‌ی ترتیبِ مودیفایرِ README)، پس هدفِ لمسی به
                                // اندازه‌ی همین پدینگ کوچک می‌شود. در گریدِ ۷ستونه‌ی عرضِ ۳۶۰
                                // هر خانه ~۴۷dp است؛ ۲dp از هر طرف آن را به ~۴۳ می‌رساند،
                                // یعنی دقیقاً زیرِ حداقلِ ۴۴. به ۱ کم شد.
                                .padding(1.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (day in 1..daysInMonth) {
                                val thisDate = PersianDate(viewYear, viewMonth, day)
                                val items = dueMap[thisDate]
                                val isSelected = selectedDate == thisDate
                                val isToday = today == thisDate
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .background(
                                            if (isSelected) AppPrimary.copy(alpha = 0.18f) else AppBg,
                                            CircleShape,
                                        )
                                        .pressScaleClickable(scale = 0.9f) { selectedDate = thisDate },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            toFa(day),
                                            color = if (isToday) AppPrimary else AppText,
                                            fontSize = 13.sp,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                        )
                                        // نقطه‌ی وضعیت. رنگ از **پرداخت‌شدن** می‌آید نه از نوعِ
                                        // تعهد - همان قاعده‌ی فریمِ `51a` (تمایزِ نوع از آیکون
                                        // می‌آید، نه رنگ). راهنمای زیرِ گرید می‌گوید کدام کدام است.
                                        if (items != null) {
                                            val allPaid = items.all { it.paid }
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 2.dp)
                                                    .size(5.dp)
                                                    .background(
                                                        if (allPaid) AppPrimary else AppDanger,
                                                        CircleShape,
                                                    ),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // راهنمای رنگِ نقطه‌ها. بی این، دو نقطه‌ی قرمز و سبز بی‌معنا بودند - کاربر باید
        // روی روز بزند تا بفهمد نقطه چه می‌گفت.
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DotLegend(color = AppDanger, label = "پرداخت‌نشده")
            Spacer(Modifier.size(14.dp))
            DotLegend(color = AppPrimary, label = "پرداخت‌شده")
        }

        val selectedItems = selectedDate?.let { dueMap[it] } ?: emptyList()
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            AppCard(
                label = selectedDate?.let { "سررسیدهای ${toFa(it.d)} ${faMonthName(it.m)} ${toFa(it.y)}" }
                    ?: "یه روز رو از تقویم انتخاب کن",
            ) {
                if (selectedItems.isEmpty()) {
                    Text("این روز سررسیدی ندارد", color = AppMuted, fontSize = 12.sp)
                } else {
                    Column {
                        selectedItems.forEachIndexed { index, item ->
                            if (index > 0) {
                                Spacer(Modifier.height(10.dp))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.loanName, color = AppText, fontSize = 13.sp)
                                    // نامِ بانک می‌تواند خالی باشد (حسابِ نقدی) - همان موردی
                                    // که در تبِ دارایی بجِ خالی می‌ساخت.
                                    if (item.bank.isNotBlank()) {
                                        Text(item.bank, color = AppMuted, fontSize = 11.sp)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    PrivacyCrossfade(privacyMode) { masked ->
                                        Text(
                                            "${maskIfPrivate(masked, amountToman(item.installment))} تومان",
                                            color = AppText,
                                            fontSize = 13.sp,
                                        )
                                    }
                                    Text(
                                        if (item.paid) "پرداخت‌شده" else "پرداخت‌نشده",
                                        color = if (item.paid) AppPrimary else AppDanger,
                                        fontSize = 11.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DotLegend(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
        Text(
            label,
            color = AppLabel,
            fontSize = 10.5.sp,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}
