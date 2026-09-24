package ir.sadteam.loancalc.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.ReceiptLong
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.PersianCalendar
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.faMonthName
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppDangerBorder
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppPrimaryPillBorder
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import ir.sadteam.loancalc.ui.theme.AppWarningPill
import ir.sadteam.loancalc.ui.theme.AppDueNextBorder
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.hardShadow
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
fun FinancialCalendarScreen(
    onBack: () -> Unit,
    /** تپ روی ردیفِ قسط → صفحه‌ی همان وام (دورِ ۹). `{}` یعنی این صفحه جایی باز شده که مقصدی ندارد. */
    onOpenLoan: (Long) -> Unit = {},
    onOpenCheque: (Long) -> Unit = {},
    viewModel: FinancialCalendarViewModel = hiltViewModel(),
) {
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
        selectedDate = PersianDate(y, m, minOf(selectedDate?.d ?: 1, JalaliCalendar.daysInMonth(y, m)))
    }

    val monthInstallments = dueMap.filterKeys { it.y == viewYear && it.m == viewMonth }
        .values.flatten().filter { it.kind == DueKind.INSTALLMENT }
    // «نزدیک» یعنی امروز تا شش روز بعد، فقط در ماهِ نمایش‌داده‌شده؛ تکراری‌ها وضعیت ندارند.
    val nearDates = remember(today) { (0..6).map { PersianCalendar.addDays(today, it) }.toSet() }
    val nearCount = dueMap.filterKeys { it.y == viewYear && it.m == viewMonth && it in nearDates }
        .values.flatten().count { it.kind == DueKind.INSTALLMENT && it.paid == false }

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
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 4.dp).weight(1f),
            )
            Box(
                Modifier.padding(end = 6.dp).size(40.dp).background(AppPrimaryPill, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = AppPrimaryInk, modifier = Modifier.size(22.dp))
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CalendarSummary(nearCount, "نزدیک · ۷ روز", Icons.Filled.Schedule, AppDangerInk, AppDangerPill, AppDangerBorder, Modifier.weight(1f))
                CalendarSummary(monthInstallments.count { it.paid == true }, "پرداخت‌شده", Icons.Filled.CheckCircle, AppPrimaryInk, AppPrimaryPill, AppPrimaryPillBorder, Modifier.weight(1f))
                CalendarSummary(monthInstallments.size, "اقساطِ این‌ماه", Icons.Filled.CalendarMonth, AppWarningInk, AppWarningPill, AppDueNextBorder, Modifier.weight(1f))
            }
            AppCard(contentPadding = 12.dp, horizontalPadding = 12.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MonthArrow(Icons.Filled.ChevronRight, "ماه قبل") { stepMonth(-1) }
                    Text(
                        "${faMonthName(viewMonth)} ${toFa(viewYear)}",
                        color = AppText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    MonthArrow(Icons.Filled.ChevronLeft, "ماه بعد") { stepMonth(1) }
                }

                // روی نمایشگرِ باریک، به‌جای کوچک‌کردنِ هدفِ لمس، گرید کمی افقی پیمایش می‌شود.
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val gridWidth = maxWidth.coerceAtLeast(308.dp)
                    Column(Modifier.horizontalScroll(rememberScrollState()).width(gridWidth)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            faWeekDayShort.forEachIndexed { index, label ->
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    Text(label, color = if (index == 6) AppWarningInk else AppMuted, fontSize = 9.5.sp)
                                }
                            }
                        }

                        val daysInMonth = JalaliCalendar.daysInMonth(viewYear, viewMonth)
                        val firstDow = JalaliCalendar.dayOfWeekSaturdayFirst(PersianDate(viewYear, viewMonth, 1))
                        val rowCount = (firstDow + daysInMonth + 6) / 7

                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            for (row in 0 until rowCount) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    for (col in 0 until 7) {
                                        val cellIndex = row * 7 + col
                                        val day = cellIndex - firstDow + 1
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 44.dp),
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
                                                        .heightIn(min = 44.dp)
                                                        .semantics {
                                                            selected = isSelected
                                                            contentDescription = "${toFa(day)} ${faMonthName(viewMonth)} ${toFa(viewYear)}" +
                                                                if (isToday) "، امروز" else ""
                                                        }
                                                        .pressScaleClickable(scale = 0.9f) { selectedDate = thisDate }
                                                        .padding(3.dp),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Column(
                                                        modifier = Modifier.size(38.dp)
                                                            .then(if (isSelected) Modifier.shadow(10.dp, CircleShape, ambientColor = AppPrimary, spotColor = AppPrimary) else Modifier)
                                                            .background(
                                                                when { isSelected -> AppPrimary; col == 6 -> AppWarningPill; else -> AppLineRow.copy(alpha = 0.45f) },
                                                                if (isSelected) CircleShape else RoundedCornerShape(12.dp),
                                                            ),
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center,
                                                    ) {
                                                        Text(
                                                            toFa(day),
                                                            color = when { isSelected -> Color.White; isToday -> AppPrimaryInk; col == 6 -> AppWarningInk; else -> AppText },
                                                            fontSize = 11.sp,
                                                            fontWeight = if (isSelected || isToday) FontWeight.Black else FontWeight.Normal,
                                                        )
                                                        // نقطه‌ی وضعیت. رنگ از **پرداخت‌شدن** می‌آید نه از نوعِ
                                                        // تعهد - همان قاعده‌ی فریمِ `51a` (تمایزِ نوع از آیکون
                                                        // می‌آید، نه رنگ). راهنمای زیرِ گرید می‌گوید کدام کدام است.
                                                        if (items != null) {
                                                            // سه حالت، چون `paid` سه‌حالتی است: قرمز اگر
                                                            // چیزی **واقعاً** پرداخت‌نشده باشد · سبز اگر همه
                                                            // پرداخت شده‌اند · خاکستری اگر فقط پرداختِ تکراری
                                                            // است (وضعیتی ثبت نشده).
                                                            val known = items.mapNotNull { it.paid }
                                                            Box(
                                                                modifier = Modifier
                                                                    .padding(top = 2.dp)
                                                                    .size(5.dp)
                                                                    .background(
                                                                        when {
                                                                            isSelected -> Color.White
                                                                            known.isEmpty() -> AppLabel
                                                                            known.all { it } -> AppPrimary
                                                                            else -> AppDanger
                                                                        },
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
                    }
                }

                // راهنمای رنگِ نقطه‌ها. بی این، دو نقطه‌ی قرمز و سبز بی‌معنا بودند - کاربر باید
                // روی روز بزند تا بفهمد نقطه چه می‌گفت.
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                        .background(AppPrimaryPill.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DotLegend(color = AppDanger, label = "پرداخت‌نشده")
                    Spacer(Modifier.size(12.dp))
                    DotLegend(color = AppPrimary, label = "پرداخت‌شده")
                    Spacer(Modifier.size(12.dp))
                    DotLegend(color = AppLabel, label = "تکراری")
                }
            }

            val selectedItems = selectedDate?.let { dueMap[it] } ?: emptyList()
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                AppCard(
                    contentPadding = 12.dp,
                    horizontalPadding = 12.dp,
                    borderColor = AppLineRow,
                    shadow = false,
                ) {
                    Text(
                        selectedDate?.let { "سررسیدهای ${toFa(it.d)} ${faMonthName(it.m)} ${toFa(it.y)}" }
                            ?: "یه روز رو از تقویم انتخاب کن",
                        color = AppText, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    if (selectedItems.isEmpty()) {
                        Text("این روز سررسیدی ندارد", color = AppMuted, fontSize = 12.sp)
                    } else {
                        Column {
                            selectedItems.forEachIndexed { index, item ->
                                if (index > 0) {
                                    Spacer(Modifier.height(10.dp))
                                }
                                // ردیفی که مقصد دارد کلیک‌پذیر می‌شود؛ پرداختِ تکراری الگوست و
                                // ردیفِ مستقلی ندارد، پس عمداً بی‌کنش می‌مانَد.
                                val target: (() -> Unit)? = item.loanId?.let { id -> { onOpenLoan(id) } }
                                    ?: item.chequeId?.let { id -> { onOpenCheque(id) } }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 44.dp)
                                        .then(
                                            if (target != null) {
                                                Modifier.pressScaleClickable(scale = 0.99f, onClick = target)
                                            } else {
                                                Modifier
                                            },
                                        ),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    // تفکیکِ نوع با **آیکون** می‌آید نه با رنگ - قاعده‌ی فریمِ `51a`.
                                    Box(
                                        Modifier.size(30.dp).background(AppPrimaryPill, RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            when (item.kind) {
                                                DueKind.INSTALLMENT -> Icons.Filled.AccountBalance
                                                DueKind.CHEQUE -> Icons.Filled.ReceiptLong
                                                DueKind.RECURRING -> Icons.Filled.Repeat
                                            },
                                            contentDescription = when (item.kind) {
                                                DueKind.INSTALLMENT -> "قسط"
                                                DueKind.CHEQUE -> "چک"
                                                DueKind.RECURRING -> "پرداخت تکراری"
                                            },
                                            tint = AppPrimaryInk,
                                            modifier = Modifier.size(14.dp),
                                        )
                                    }
                                    Spacer(Modifier.size(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, color = AppText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        // زیرعنوان می‌تواند خالی باشد (وامِ بی‌بانک، پرداختِ تکراریِ
                                        // بی‌دسته) - همان موردی که در تبِ دارایی بجِ خالی می‌ساخت.
                                        if (item.subtitle.isNotBlank()) {
                                            Text(item.subtitle, color = AppMuted, fontSize = 10.sp)
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        PrivacyCrossfade(privacyMode) { masked ->
                                            Text(
                                                "${maskIfPrivate(masked, amountToman(item.amount))} تومان",
                                                color = AppText,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                            )
                                        }
                                        // `null` یعنی این جدول وضعیتی ندارد - «پرداخت‌نشده»
                                        // نوشتن سرِ اجاره‌ای که سالِ پیش داده شده، غلط است.
                                        Text(
                                            when (item.paid) {
                                                true -> "پرداخت‌شده"
                                                false -> "پرداخت‌نشده"
                                                null -> "پرداختِ تکراری"
                                            },
                                            color = when (item.paid) {
                                                true -> AppPrimaryInk
                                                false -> AppDangerInk
                                                null -> AppLabel
                                            },
                                            fontSize = 9.5.sp,
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
}

@Composable
private fun MonthArrow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).padding(4.dp).background(AppPrimaryPill, CircleShape)
            .pressScaleClickable(scale = 0.9f, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = AppPrimaryInk, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun CalendarSummary(count: Int, label: String, icon: ImageVector, ink: Color, fill: Color, border: Color, modifier: Modifier) {
    AppCard(modifier = modifier, backgroundColor = fill, borderColor = border,
        contentPadding = 9.dp, horizontalPadding = 4.dp, shadow = false) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(Modifier.size(30.dp).background(ink.copy(alpha = 0.14f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = ink, modifier = Modifier.size(16.dp))
            }
        }
        Text(toFa(count), color = ink, fontSize = 15.sp, fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(label, color = ink, fontSize = 8.5.sp, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 2.dp))
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
