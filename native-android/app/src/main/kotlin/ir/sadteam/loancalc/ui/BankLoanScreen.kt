package ir.sadteam.loancalc.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.LoanCalculator
import ir.sadteam.loancalc.core.LoanMethod
import ir.sadteam.loancalc.core.LoanResult
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.cleanNumDecimal
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.BankEntry
import ir.sadteam.loancalc.data.banks
import ir.sadteam.loancalc.data.creditServices
import ir.sadteam.loancalc.data.loanPresets
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.BankTile
import ir.sadteam.loancalc.ui.components.CalendarPickerScreen
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InlineJalaliDateRow
import ir.sadteam.loancalc.ui.components.PresetCard
import ir.sadteam.loancalc.ui.components.SlimSlider
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.components.horizontalScrollbar
import ir.sadteam.loancalc.ui.components.lazyColumnScrollbar
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppText

private val monthChipValues = listOf(12, 18, 24, 36, 60, 84, 120, 180, 240)
private val intervalChipOptions = listOf(7 to "هفتگی", 14 to "دوهفته‌ای", 30 to "ماهانه", 60 to "دوماهه", 90 to "سه‌ماهه")

data class BankLoanOutcome(
    val result: LoanResult,
    val startDate: PersianDate,
    val ratePct: Double,
    val n: Int,
    val method: LoanMethod,
    val borrower: String,
    val bankName: String,
)

@Composable
fun BankLoanScreen(onCalculated: (BankLoanOutcome) -> Unit) {
    var borrowerName by remember { mutableStateOf("") }
    var selectedBank by remember { mutableStateOf<BankEntry?>(null) }
    var selectedPresetKey by remember { mutableStateOf<String?>(null) }

    var startYear by remember { mutableStateOf(1404) }
    var startMonth by remember { mutableStateOf(1) }
    var startDay by remember { mutableStateOf(1) }

    var amountText by remember { mutableStateOf("2,500,000,000") }
    var amountSliderRange by remember { mutableStateOf(100_000_000f..10_000_000_000f) }
    var amountSlider by remember { mutableStateOf(2_500_000_000f) }

    var rateText by remember { mutableStateOf("23") }
    var rateSlider by remember { mutableStateOf(23f) }

    var selectedMonths by remember { mutableStateOf(36) }
    var customMonthsText by remember { mutableStateOf("") }

    var intervalDays by remember { mutableStateOf(30) }

    var graceOn by remember { mutableStateOf(false) }
    var graceMonths by remember { mutableStateOf(6f) }

    var presetNote by remember { mutableStateOf<String?>(null) }
    var showCalendarPicker by remember { mutableStateOf(false) }

    fun applyAmount(rial: Long) {
        amountText = fmtGroupedEn(rial)
        if (rial <= amountSliderRange.endInclusive.toLong()) amountSlider = rial.toFloat()
    }

    if (showCalendarPicker) {
        CalendarPickerScreen(
            initialDate = PersianDate(startYear, startMonth, startDay),
            onDateSelected = { date ->
                startYear = date.y
                startMonth = date.m
                startDay = date.d
                showCalendarPicker = false
            },
            onBack = { showCalendarPicker = false },
        )
        return
    }

    val listState = rememberLazyListState()
    val scrollbarColor = AppPrimary
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .lazyColumnScrollbar(listState, scrollbarColor),
        contentPadding = PaddingValues(14.dp, 14.dp, 14.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item {
            // کارت وام‌های پرتکرار خط مشکی می‌گیره (نه سبز) - چون خودِ کارت‌های داخلش خط مشکی دارن
            // و کاربر خواست حاشیه‌ی سبز مخصوص بقیه‌ی باکس‌ها باشه، نه این بخشِ اول.
            AppCard(label = "وام‌های پرتکرار", borderColor = AppText.copy(alpha = 0.5f)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    loanPresets.forEach { p ->
                        PresetCard(
                            icon = p.icon,
                            title = p.title,
                            sub = p.sub,
                            selected = selectedPresetKey == p.key,
                            onClick = {
                                selectedPresetKey = p.key
                                applyAmount(p.amount)
                                amountSliderRange = 100_000_000f..10_000_000_000f
                                rateText = trimRate(p.ratePct)
                                rateSlider = p.ratePct.toFloat()
                                selectedMonths = p.months
                                customMonthsText = ""
                                graceOn = p.graceMonths > 0
                                if (p.graceMonths > 0) graceMonths = p.graceMonths.toFloat()
                                presetNote = p.note
                            },
                        )
                    }
                }
                if (presetNote != null) {
                    Text(
                        text = "📋 طبق قانون بانک مرکزی\n$presetNote",
                        fontSize = 13.sp,
                        color = AppText,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }

        item {
            AppCard(label = "نام وام‌گیرنده") {
                OutlinedTextField(
                    value = borrowerName,
                    onValueChange = { borrowerName = it },
                    placeholder = { Text("نام وام‌گیرنده") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = appFieldColors(),
                )
            }
        }

        item {
            AppCard(label = "بانک یا سرویس اعتباری") {
                val banksScroll = rememberScrollState()
                val creditScroll = rememberScrollState()
                Text("بانک‌ها", fontSize = 13.sp, color = AppMuted, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .horizontalScroll(banksScroll)
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    banks.forEach { b ->
                        BankTile(
                            bank = b,
                            selected = selectedBank?.name == b.name,
                            onClick = { selectedBank = b },
                        )
                    }
                }
                // نشانگر اسکرول افقی زیر ردیفِ بانک‌ها (تو همون فاصله‌ی ظریفِ زیرِ لیبل).
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 8.dp)
                        .height(4.dp)
                        .horizontalScrollbar(banksScroll, AppPrimary),
                )
                Text("خدمات اعتباری", fontSize = 13.sp, color = AppMuted, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .horizontalScroll(creditScroll)
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    creditServices.forEach { b ->
                        BankTile(
                            bank = b,
                            selected = selectedBank?.name == b.name,
                            onClick = {
                                selectedBank = b
                                rateText = trimRate(b.ratePct)
                                rateSlider = b.ratePct.toFloat()
                                selectedMonths = b.months
                                customMonthsText = ""
                                val mid = (b.minAmount + b.maxAmount) / 2
                                applyAmount(mid)
                                amountSliderRange = b.minAmount.toFloat()..b.maxAmount.toFloat()
                                selectedPresetKey = null
                                presetNote = null
                            },
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .height(4.dp)
                        .horizontalScrollbar(creditScroll, AppPrimary),
                )
            }
        }

        item {
            AppCard(label = "تاریخ دریافت وام") {
                // تاریخ اینلاین با سه دراپ‌داونِ روز/ماه/سال - همینجا عوض می‌شه بدون رفتن به یه صفحه‌ی
                // جدا (خواسته‌ی کاربر). آیکون تقویم کنارش، برای کسی که تقویم گریدیِ کامل رو بخواد.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InlineJalaliDateRow(
                        year = startYear,
                        month = startMonth,
                        day = startDay,
                        onDateChange = { y, m, d -> startYear = y; startMonth = m; startDay = d },
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { showCalendarPicker = true }) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = "انتخاب از تقویم")
                    }
                }
            }
        }

        item {
            AppCard(label = "مبلغ وام") {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { raw ->
                        val digits = cleanNum(raw)
                        val n = digits.toLongOrNull() ?: 0L
                        amountText = if (digits.isEmpty()) "" else fmtGroupedEn(n)
                        if (n in amountSliderRange.start.toLong()..amountSliderRange.endInclusive.toLong()) {
                            amountSlider = n.toFloat()
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = appFieldColors(),
                    suffix = { Text("ریال", color = AppMuted, fontSize = 13.sp) },
                )
                val rialVal = cleanNum(amountText).toLongOrNull() ?: 0L
                if (rialVal > 0) {
                    Text(
                        text = "${numberToWordsFa((rialVal / 10).toDouble())} تومان",
                        color = AppAccent,
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                SlimSlider(
                    value = amountSlider,
                    onValueChange = { v ->
                        amountSlider = v
                        amountText = fmtGroupedEn(v.toLong())
                    },
                    valueRange = amountSliderRange,
                )
            }
        }

        item {
            AppCard(label = "نرخ سود سالانه") {
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { raw ->
                        val filtered = cleanNumDecimal(raw)
                        rateText = filtered
                        val num = filtered.toDoubleOrNull()
                        if (num != null && num in 0.0..35.0) rateSlider = num.toFloat()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = appFieldColors(),
                    suffix = { Text("درصد", color = AppMuted, fontSize = 13.sp) },
                )
                SlimSlider(
                    value = rateSlider,
                    onValueChange = { v -> rateSlider = v; rateText = trimRate(v.toDouble()) },
                    valueRange = 0f..35f,
                )
            }
        }

        item {
            AppCard(label = "تعداد اقساط (ماه)") {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    monthChipValues.forEach { v ->
                        AppChip(
                            label = toFa(v),
                            selected = customMonthsText.isEmpty() && selectedMonths == v,
                            onClick = { selectedMonths = v; customMonthsText = "" },
                        )
                    }
                }
                OutlinedTextField(
                    value = customMonthsText,
                    onValueChange = { customMonthsText = cleanNum(it) },
                    placeholder = { Text("تعداد دلخواه") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    singleLine = true,
                    colors = appFieldColors(),
                    suffix = { Text("ماه", color = AppMuted, fontSize = 13.sp) },
                )
            }
        }

        item {
            AppCard(label = "فاصله زمانی هر قسط") {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    intervalChipOptions.forEach { (v, label) ->
                        AppChip(label = label, selected = intervalDays == v, onClick = { intervalDays = v })
                    }
                }
            }
        }

        item {
            AppCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text("دوره تنفس", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("بدون پرداخت اقساط در ماه‌های اول", fontSize = 13.sp, color = AppMuted)
                    }
                    Switch(
                        checked = graceOn,
                        onCheckedChange = { graceOn = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = AppPrimaryDim, checkedThumbColor = AppPrimary),
                    )
                }
                if (graceOn) {
                    Text("مدت تنفس (ماه)", fontSize = 13.5.sp, color = AppMuted, modifier = Modifier.padding(top = 12.dp))
                    SlimSlider(
                        value = graceMonths,
                        onValueChange = { graceMonths = it },
                        valueRange = 1f..24f,
                        steps = 22,
                    )
                    Text(
                        text = "${toFa(graceMonths.toInt())} ماه",
                        fontSize = 12.5.sp,
                        color = AppMuted,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        item {
            AppCard {
                Text(
                    "کارمزد بانک به‌صورت خودکار طبق قانون بانک مرکزی و ضوابط هر بانک محاسبه می‌شود.",
                    fontSize = 13.5.sp,
                    color = AppMuted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }

        item {
            val rialAmount = cleanNum(amountText).toLongOrNull() ?: 0L
            val n = customMonthsText.toIntOrNull() ?: selectedMonths
            val rate = rateText.toDoubleOrNull() ?: 0.0
            GradientButton(
                onClick = {
                    if (rialAmount <= 0 || n <= 0) return@GradientButton
                    val method = if (rate <= 4.0) LoanMethod.QARZ else LoanMethod.STANDARD
                    val grace = if (graceOn) graceMonths.toInt() else 0
                    val result = LoanCalculator.compute(rialAmount.toDouble(), rate, n, method, grace, intervalDays)
                    onCalculated(
                        BankLoanOutcome(
                            result = result,
                            startDate = PersianDate(startYear, startMonth, startDay),
                            ratePct = rate,
                            n = n,
                            method = method,
                            borrower = borrowerName.ifBlank { "—" },
                            bankName = selectedBank?.name ?: "مشخص‌نشده",
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("محاسبه کن", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun fmtGroupedEn(n: Long): String {
    return "%,d".format(n)
}

private fun trimRate(v: Double): String {
    return if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()
}
