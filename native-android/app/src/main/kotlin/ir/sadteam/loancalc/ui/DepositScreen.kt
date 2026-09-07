package ir.sadteam.loancalc.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.DepositCalculator
import ir.sadteam.loancalc.core.DepositResult
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.cleanNumDecimal
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.StaggerIn
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.SlimSlider
import ir.sadteam.loancalc.ui.components.amountSliderSteps
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.components.countUpDouble
import ir.sadteam.loancalc.ui.components.lazyColumnScrollbar
import ir.sadteam.loancalc.ui.history.CalculationHistoryViewModel
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import java.util.Locale

private val depositMonthOptions = listOf(1 to "۱ ماهه", 3 to "۳ ماهه", 6 to "۶ ماهه", 12 to "۱ ساله", 24 to "۲ ساله")

/** پورت مو‌به‌موی تب «سود سپرده» (view-deposit تو www/index.html، calculateDeposit). */
@Composable
fun DepositScreen(historyViewModel: CalculationHistoryViewModel = hiltViewModel()) {
    var amountText by remember { mutableStateOf("2500000000") }
    var amountSlider by remember { mutableStateOf(2_500_000_000f) }

    var rateText by remember { mutableStateOf("18") }
    var rateSlider by remember { mutableStateOf(18f) }

    var selectedMonths by remember { mutableStateOf(12) }

    var result by remember { mutableStateOf<DepositResult?>(null) }

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
            StaggerIn(0) {
                AppCard(label = "مبلغ سپرده") {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { raw ->
                            // فقط رقم تو state؛ کاما نمایشیه (ThousandsSeparatorTransformation) - فرمت تو
                            // onValueChange مکان‌نما رو می‌پروند و رقم وسطِ عدد درج می‌شد (باگ گزارش‌شده).
                            val digits = cleanNum(raw)
                            val n = digits.toLongOrNull() ?: 0L
                            amountText = digits
                            if (n in 100_000_000L..10_000_000_000L) amountSlider = n.toFloat()
                        },
                        visualTransformation = ThousandsSeparatorTransformation(),
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
                            color = AppMuted,
                            fontSize = 11.5.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    SlimSlider(
                        value = amountSlider,
                        onValueChange = { v ->
                            amountSlider = v
                            amountText = v.toLong().toString()
                        },
                        valueRange = 100_000_000f..10_000_000_000f,
                        // پله‌بندی به گامِ ۱۰میلیون‌تومانی - رجوع کن به amountSliderSteps.
                        steps = amountSliderSteps(100_000_000f..10_000_000_000f),
                    )
                }
            }
        }

        item {
            StaggerIn(1) {
                AppCard(label = "نرخ سود سالانه") {
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { raw ->
                            val filtered = cleanNumDecimal(raw)
                            rateText = filtered
                            // اسلایدر فقط تا ۵۰ می‌ره، ولی خودِ فیلد بالاتر از ۵۰ رو هم دستی قبول
                            // می‌کنه (خواسته‌ی صریحِ کاربر) - رجوع کن به مورد ۱ تو CLAUDE.md.
                            val num = filtered.toDoubleOrNull()
                            if (num != null && num in 0.0..50.0) rateSlider = num.toFloat()
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = appFieldColors(),
                        suffix = { Text("درصد", color = AppMuted, fontSize = 13.sp) },
                    )
                    SlimSlider(
                        value = rateSlider,
                        onValueChange = { v ->
                            rateSlider = v
                            // نمایشِ حداکثر دو رقمِ اعشار - رجوع کن به BankLoanScreen.trimRate.
                            rateText = if (v == v.toLong().toFloat()) v.toLong().toString() else String.format(Locale.US, "%.2f", v)
                        },
                        valueRange = 0f..50f,
                        steps = 99,
                    )
                }
            }
        }

        item {
            StaggerIn(2) {
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
        }

        item {
            StaggerIn(3) {
                GradientButton(
                    onClick = {
                        val principal = cleanNum(amountText).toLongOrNull() ?: 0L
                        if (principal > 0) {
                            val computed = DepositCalculator.compute(principal.toDouble(), rateText.toDoubleOrNull() ?: 0.0, selectedMonths)
                            result = computed
                            historyViewModel.log(
                                kind = "DEPOSIT",
                                title = "سود سپرده",
                                summary = "مبلغ ${fmt(principal.toDouble())} ریال × ${toFa(selectedMonths)} ماه",
                                amount = computed.finalAmount,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("محاسبه سود سپرده")
                }
            }
        }

        result?.let { r ->
            item {
                // شمارشِ صعودیِ نرمِ هر ۴ عدد (همون الگوی countUpDouble داشبوردِ وام‌های من) -
                // به‌جای پرشِ یهوییِ نتیجه، اعداد «جون می‌گیرن».
                val animatedDaily = countUpDouble(r.dailyInterest)
                val animatedMonthly = countUpDouble(r.monthlyInterest)
                val animatedTotal = countUpDouble(r.totalInterest)
                val animatedFinal = countUpDouble(r.finalAmount)
                AppCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        DepositStat(label = "سود روزانه (ریال)", value = fmt(animatedDaily), modifier = Modifier.weight(1f))
                        DepositStat(label = "سود ماهانه (ریال)", value = fmt(animatedMonthly), modifier = Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        DepositStat(label = "کل سود دوره (ریال)", value = fmt(animatedTotal), modifier = Modifier.weight(1f))
                        DepositStat(label = "مبلغ نهایی (ریال)", value = fmt(animatedFinal), modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DepositStat(label: String, value: String, modifier: Modifier = Modifier) {
    // به‌درخواست کاربر اول عنوان (به حروف) بالا، بعد عددش پایین.
    Column(modifier = modifier) {
        Text(text = label, fontSize = 12.sp, color = AppMuted)
        Text(text = value, fontSize = 14.sp, color = AppPrimary, modifier = Modifier.padding(top = 2.dp))
    }
}
