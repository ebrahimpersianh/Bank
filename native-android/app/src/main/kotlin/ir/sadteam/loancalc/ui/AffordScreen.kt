package ir.sadteam.loancalc.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.AffordabilityCalculator
import ir.sadteam.loancalc.core.RateFinderCalculator
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.cleanNumDecimal
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.SlimSlider
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.components.lazyColumnScrollbar
import ir.sadteam.loancalc.ui.history.CalculationHistoryViewModel
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

private val affordMonthChipValues = listOf(12, 24, 36, 60, 120)

/** پورت مو‌به‌موی تب «چقدر وام می‌تونم بگیرم؟» (view-afford تو www/index.html). */
@Composable
fun AffordScreen(historyViewModel: CalculationHistoryViewModel = hiltViewModel()) {
    var payText by remember { mutableStateOf("100,000,000") }
    var paySlider by remember { mutableStateOf(100_000_000f) }

    var rateText by remember { mutableStateOf("23") }
    var rateSlider by remember { mutableStateOf(23f) }

    var monthsText by remember { mutableStateOf("36") }

    var result by remember { mutableStateOf<Double?>(null) }

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
            AppCard(label = "مبلغی که می‌تونم ماهانه قسط بدم") {
                OutlinedTextField(
                    value = payText,
                    onValueChange = { raw ->
                        val digits = cleanNum(raw)
                        val n = digits.toLongOrNull() ?: 0L
                        payText = if (digits.isEmpty()) "" else "%,d".format(n)
                        if (n in 10_000_000L..500_000_000L) paySlider = n.toFloat()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = appFieldColors(),
                    suffix = { Text("ریال", color = AppMuted, fontSize = 13.sp) },
                )
                val rialVal = cleanNum(payText).toLongOrNull() ?: 0L
                if (rialVal > 0) {
                    Text(
                        text = "${numberToWordsFa((rialVal / 10).toDouble())} تومان",
                        color = AppMuted,
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                SlimSlider(
                    value = paySlider,
                    onValueChange = { v ->
                        paySlider = v
                        payText = "%,d".format(v.toLong())
                    },
                    valueRange = 10_000_000f..500_000_000f,
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
                    onValueChange = { v -> rateSlider = v; rateText = if (v == v.toLong().toFloat()) v.toLong().toString() else v.toString() },
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
                    affordMonthChipValues.forEach { v ->
                        AppChip(
                            label = toFa(v),
                            selected = monthsText == v.toString(),
                            onClick = { monthsText = v.toString() },
                        )
                    }
                }
                OutlinedTextField(
                    value = monthsText,
                    onValueChange = { monthsText = cleanNum(it) },
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
            val pay = cleanNum(payText).toLongOrNull() ?: 0L
            val rate = rateText.toDoubleOrNull() ?: 0.0
            val n = monthsText.toIntOrNull() ?: 36
            GradientButton(
                onClick = {
                    if (pay > 0) {
                        val maxPrincipal = AffordabilityCalculator.computeMaxPrincipal(pay.toDouble(), rate, n)
                        result = maxPrincipal
                        historyViewModel.log(
                            kind = "AFFORD",
                            title = "محاسبه‌گر سقف وام",
                            summary = "قسط ${fmt(pay.toDouble())} ریال × ${toFa(n)} ماه، نرخ ${toFa(rate)}٪",
                            amount = maxPrincipal,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("محاسبه حداکثر وام")
            }
        }

        result?.let { p ->
            item {
                AppCard(label = "حداکثر مبلغ وامی که می‌تونی بگیری") {
                    Text(text = "${fmt(p)} ریال", fontSize = 20.sp, color = AppPrimary)
                    Text(
                        text = "${numberToWordsFa(p / 10)} تومان",
                        color = AppMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        item { RateFinderCard() }
    }
}

/** پورت مو‌به‌موی «یا برعکس: نرخ سود رو پیدا کن» (calculateRateFinder تو www/index.html) - از رو
 * مبلغ وام، قسط ماهانه و تعداد ماه، نرخ سود سالانه‌ی تقریبی رو حساب می‌کنه. */
@Composable
private fun RateFinderCard() {
    var amountText by remember { mutableStateOf("") }
    var amountSlider by remember { mutableStateOf(100_000_000f) }
    var installmentText by remember { mutableStateOf("") }
    var installmentSlider by remember { mutableStateOf(1_000_000f) }
    var monthsText by remember { mutableStateOf("36") }
    var result by remember { mutableStateOf<Double?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppSurface, RoundedCornerShape(18.dp))
            .border(1.dp, AppPrimary.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .padding(14.dp),
    ) {
        Column {
            Text("یا برعکس: نرخ سود رو پیدا کن", color = AppPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(
                "مبلغ وام و قسطی که می‌دی رو بگو، نرخ سودش رو حساب می‌کنم",
                color = AppMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )
            OutlinedTextField(
                value = amountText,
                onValueChange = { raw ->
                    val digits = cleanNum(raw)
                    val n = digits.toLongOrNull() ?: 0L
                    amountText = if (digits.isEmpty()) "" else "%,d".format(n)
                    if (n in 100_000_000L..10_000_000_000L) amountSlider = n.toFloat()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = appFieldColors(),
                suffix = { Text("مبلغ وام (ریال)", color = AppMuted, fontSize = 13.sp) },
            )
            (cleanNum(amountText).toLongOrNull() ?: 0L).takeIf { it > 0 }?.let { r ->
                Text("${numberToWordsFa((r / 10).toDouble())} تومان", color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
            }
            SlimSlider(
                value = amountSlider,
                onValueChange = { v -> amountSlider = v; amountText = "%,d".format(v.toLong()) },
                valueRange = 100_000_000f..10_000_000_000f,
                modifier = Modifier.padding(top = 6.dp),
            )
            OutlinedTextField(
                value = installmentText,
                onValueChange = { raw ->
                    val digits = cleanNum(raw)
                    val n = digits.toLongOrNull() ?: 0L
                    installmentText = if (digits.isEmpty()) "" else "%,d".format(n)
                    if (n in 1_000_000L..200_000_000L) installmentSlider = n.toFloat()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                singleLine = true,
                colors = appFieldColors(),
                suffix = { Text("مبلغ هر قسط (ریال)", color = AppMuted, fontSize = 13.sp) },
            )
            (cleanNum(installmentText).toLongOrNull() ?: 0L).takeIf { it > 0 }?.let { r ->
                Text("${numberToWordsFa((r / 10).toDouble())} تومان", color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
            }
            SlimSlider(
                value = installmentSlider,
                onValueChange = { v -> installmentSlider = v; installmentText = "%,d".format(v.toLong()) },
                valueRange = 1_000_000f..200_000_000f,
                modifier = Modifier.padding(top = 6.dp),
            )
            OutlinedTextField(
                value = monthsText,
                onValueChange = { monthsText = cleanNum(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                singleLine = true,
                colors = appFieldColors(),
                suffix = { Text("تعداد اقساط (ماه)", color = AppMuted, fontSize = 13.sp) },
            )
            if (error != null) {
                Text(error ?: "", color = AppDanger, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
            }
            OutlinedButton(
                onClick = {
                    val principal = cleanNum(amountText).toLongOrNull() ?: 0L
                    val installment = cleanNum(installmentText).toLongOrNull() ?: 0L
                    val months = monthsText.toIntOrNull() ?: 0
                    val found = RateFinderCalculator.findRate(principal.toDouble(), installment.toDouble(), months)
                    if (found == null) {
                        error = if (installment * months < principal) {
                            "مبلغ کل قسط‌ها از مبلغ وام کمتره؛ عددها رو چک کن"
                        } else {
                            "هر سه مقدار رو وارد کن"
                        }
                        result = null
                    } else {
                        error = null
                        result = found
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppText),
                border = BorderStroke(1.5.dp, AppAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
            ) {
                Text("محاسبه نرخ سود")
            }
            result?.let { r ->
                Text(
                    text = "نرخ سود سالانه تقریبی: ${toFa(String.format("%.1f", r))}٪",
                    color = AppPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                )
            }
        }
    }
}
