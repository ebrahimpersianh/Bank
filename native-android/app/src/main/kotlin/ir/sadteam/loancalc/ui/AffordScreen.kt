package ir.sadteam.loancalc.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import ir.sadteam.loancalc.core.AffordabilityCalculator
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.cleanNumDecimal
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary

private val affordMonthChipValues = listOf(12, 24, 36, 60, 120)

/** پورت مو‌به‌موی تب «چقدر وام می‌تونم بگیرم؟» (view-afford تو www/index.html). */
@Composable
fun AffordScreen() {
    var payText by remember { mutableStateOf("100,000,000") }
    var paySlider by remember { mutableStateOf(100_000_000f) }

    var rateText by remember { mutableStateOf("23") }
    var rateSlider by remember { mutableStateOf(23f) }

    var monthsText by remember { mutableStateOf("36") }

    var result by remember { mutableStateOf<Double?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(6.dp, 14.dp, 6.dp, 100.dp),
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
                )
                val rialVal = cleanNum(payText).toLongOrNull() ?: 0L
                if (rialVal > 0) {
                    Text(
                        text = "${numberToWordsFa((rialVal / 10).toDouble())} تومان",
                        color = AppAccent,
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Slider(
                    value = paySlider,
                    onValueChange = { v ->
                        paySlider = v
                        payText = "%,d".format(v.toLong())
                    },
                    valueRange = 10_000_000f..500_000_000f,
                    colors = SliderDefaults.colors(thumbColor = AppPrimary, activeTrackColor = AppPrimary),
                )
                Text(
                    text = "بازه اسلایدر: ۱۰ تا ۵۰۰ میلیون ریال — برای اعداد خارج از بازه، مستقیم تایپ کن",
                    fontSize = 10.sp,
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
                )
                Slider(
                    value = rateSlider,
                    onValueChange = { v -> rateSlider = v; rateText = if (v == v.toLong().toFloat()) v.toLong().toString() else v.toString() },
                    valueRange = 0f..35f,
                    colors = SliderDefaults.colors(thumbColor = AppPrimary, activeTrackColor = AppPrimary),
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
                )
            }
        }

        item {
            val pay = cleanNum(payText).toLongOrNull() ?: 0L
            val rate = rateText.toDoubleOrNull() ?: 0.0
            val n = monthsText.toIntOrNull() ?: 36
            Button(
                onClick = {
                    if (pay > 0) {
                        result = AffordabilityCalculator.computeMaxPrincipal(pay.toDouble(), rate, n)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
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
    }
}
