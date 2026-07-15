package ir.sadteam.loancalc.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.DepositCalculator
import ir.sadteam.loancalc.core.DepositResult
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.cleanNumDecimal
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.SlimSlider
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary

private val depositMonthOptions = listOf(1 to "۱ ماهه", 3 to "۳ ماهه", 6 to "۶ ماهه", 12 to "۱ ساله", 24 to "۲ ساله")

/** پورت مو‌به‌موی تب «سود سپرده» (view-deposit تو www/index.html، calculateDeposit). */
@Composable
fun DepositScreen() {
    var amountText by remember { mutableStateOf("2,500,000,000") }
    var amountSlider by remember { mutableStateOf(2_500_000_000f) }

    var rateText by remember { mutableStateOf("18") }
    var rateSlider by remember { mutableStateOf(18f) }

    var selectedMonths by remember { mutableStateOf(12) }

    var result by remember { mutableStateOf<DepositResult?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp, 14.dp, 14.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item {
            AppCard(label = "مبلغ سپرده") {
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
                    suffix = { Text("ریال", color = AppMuted, fontSize = 13.sp) },
                )
                val rialVal = cleanNum(amountText).toLongOrNull() ?: 0L
                if (rialVal > 0) {
                    Text(
                        text = "${numberToWordsFa((rialVal / 10).toDouble())} تومان",
                        color = AppAccent,
                        fontSize = 13.5.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                SlimSlider(
                    value = amountSlider,
                    onValueChange = { v ->
                        amountSlider = v
                        amountText = "%,d".format(v.toLong())
                    },
                    valueRange = 100_000_000f..10_000_000_000f,
                )
                Text(
                    text = "بازه اسلایدر: ۱۰۰ میلیون تا ۱۰ میلیارد ریال — برای اعداد خارج از بازه، مستقیم تایپ کن",
                    fontSize = 12.sp,
                    color = AppMuted,
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
            AppCard(label = "مدت سپرده") {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    depositMonthOptions.forEach { (v, label) ->
                        AppChip(label = label, selected = selectedMonths == v, onClick = { selectedMonths = v })
                    }
                }
            }
        }

        item {
            GradientButton(
                onClick = {
                    val principal = cleanNum(amountText).toLongOrNull() ?: 0L
                    if (principal > 0) {
                        result = DepositCalculator.compute(principal.toDouble(), rateText.toDoubleOrNull() ?: 0.0, selectedMonths)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("محاسبه سود سپرده")
            }
        }

        result?.let { r ->
            item {
                AppCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        DepositStat(label = "سود روزانه (ریال)", value = fmt(r.dailyInterest), modifier = Modifier.weight(1f))
                        DepositStat(label = "سود ماهانه (ریال)", value = fmt(r.monthlyInterest), modifier = Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        DepositStat(label = "کل سود دوره (ریال)", value = fmt(r.totalInterest), modifier = Modifier.weight(1f))
                        DepositStat(label = "مبلغ نهایی (ریال)", value = fmt(r.finalAmount), modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DepositStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = value, fontSize = 12.sp, color = AppPrimary)
        Text(text = label, fontSize = 12.sp, color = AppMuted, modifier = Modifier.padding(top = 2.dp))
    }
}
