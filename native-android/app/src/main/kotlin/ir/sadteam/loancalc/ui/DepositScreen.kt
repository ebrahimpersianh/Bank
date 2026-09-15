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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.SlimSlider
import ir.sadteam.loancalc.ui.components.amountSliderSteps
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.components.countUpDouble
import ir.sadteam.loancalc.ui.components.lazyColumnScrollbar
import ir.sadteam.loancalc.ui.history.CalculationHistoryViewModel
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.jibak.tomanToRial
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText
import java.util.Locale

private val depositMonthOptions = listOf(1 to "۱ ماهه", 3 to "۳ ماهه", 6 to "۶ ماهه", 12 to "۱ ساله", 24 to "۲ ساله")

// فیلدِ مبلغ **تومان**ه (بندِ ۲ی README)؛ `DepositCalculator` ریال می‌گیره، پس تبدیل فقط تو
// لبه‌ی دکمه‌ی محاسبه. بازه‌ی اسلایدر هم ÷۱۰ شد.
private const val DEPOSIT_MIN_TOMAN = 10_000_000f
private const val DEPOSIT_MAX_TOMAN = 1_000_000_000f

private fun amountToman(rial: Double): String = fmt(rialToToman(rial.toLong()).toDouble()).faDigits()

/** `Locale.US` اجباریه - خروجی تو فیلدِ نرخ می‌شینه که بعد `toDoubleOrNull()` می‌شه. */
private fun trimRateDeposit(v: Float): String =
    if (v == v.toLong().toFloat()) v.toLong().toString() else String.format(Locale.US, "%.2f", v)

/** پورت مو‌به‌موی تب «سود سپرده» (view-deposit تو www/index.html، calculateDeposit). */
@Composable
fun DepositScreen(historyViewModel: CalculationHistoryViewModel = hiltViewModel()) {
    // هر جا مبلغ نمایش داده می‌شود باید از حالتِ خصوصی عبور کند (بندِ ۳ی README).
    // ⚠️ این صفحه تا امروز **هیچ** ماسکی نداشت و چهار مبلغ نشان می‌دهد.
    val privacyMode = LocalPrivacyMode.current

    // `rememberSaveable` جای `remember`: چرخشِ گوشی کلِ فرم را پاک می‌کرد.
    var amountText by rememberSaveable { mutableStateOf("250000000") }
    var amountSlider by rememberSaveable { mutableFloatStateOf(250_000_000f) }

    var rateText by rememberSaveable { mutableStateOf("18") }
    var rateSlider by rememberSaveable { mutableFloatStateOf(18f) }

    var selectedMonths by rememberSaveable { mutableIntStateOf(12) }

    // فریمِ `70c`: نتیجه **زنده** است، نه گره‌خورده به دکمه. `DepositCalculator.compute`
    // یک فرمولِ بسته است و هر recomposition ارزان اجرا می‌شود.
    val principalToman = cleanNum(amountText).toLongOrNull() ?: 0L
    val rate = rateText.toDoubleOrNull() ?: 0.0
    val live: DepositResult? = remember(principalToman, rate, selectedMonths) {
        if (principalToman <= 0) {
            null
        } else {
            runCatching {
                DepositCalculator.compute(tomanToRial(principalToman).toDouble(), rate, selectedMonths)
            }.getOrNull()
        }
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
        // فریمِ `70c`: **سودِ ماهانه** هیرو شد، نه «مبلغِ نهایی».
        //
        // کسی که سپرده حساب می‌کند می‌خواهد بداند ماهی چقدر می‌گیرد؛ مبلغِ نهایی همان اصل
        // به‌علاوه‌ی سود است و خبرِ تازه‌ای ندارد.
        if (live != null) {
            item {
                StaggerIn(0) {
                    AppHeroCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "سودِ ماهانه",
                                color = HeroMuted,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                            )
                            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 3.dp)) {
                                PrivacyCrossfade(privacyMode) { masked ->
                                    Text(
                                        maskIfPrivate(masked, amountToman(countUpDouble(live.monthlyInterest))),
                                        color = Color.White,
                                        fontSize = 25.sp,
                                        fontWeight = FontWeight.Black,
                                    )
                                }
                                Text(
                                    "تومان",
                                    color = HeroMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 5.dp, bottom = 2.dp),
                                )
                            }
                            PrivacyCrossfade(privacyMode) { masked ->
                                Text(
                                    "${maskIfPrivate(masked, fmt(principalToman.toDouble()).faDigits())} تومان · ${depositMonthLabel(selectedMonths)} · ${toFa(trimRateDeposit(rate.toFloat()))}٪",
                                    color = HeroMuted,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
            item {
                // سه عددِ دیگر **ردیفِ برچسب/مقدار** شدند نه جعبه - همان تصمیمِ `68a`:
                // چهار جعبه‌ی هم‌اندازه یعنی چهار عددِ هم‌اهمیت، در حالی که سودِ ماهانه
                // جوابِ اصلی است و بقیه پشتوانه‌اش.
                AppCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        DepositRow("سودِ روزانه", live.dailyInterest, privacyMode)
                        HorizontalDivider(color = AppLine)
                        DepositRow("کلِ سودِ دوره", live.totalInterest, privacyMode)
                        HorizontalDivider(color = AppLine)
                        DepositRow("مبلغِ نهایی", live.finalAmount, privacyMode)
                    }
                }
            }
        }

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
                            if (n in DEPOSIT_MIN_TOMAN.toLong()..DEPOSIT_MAX_TOMAN.toLong()) {
                                amountSlider = n.toFloat()
                            }
                        },
                        visualTransformation = ThousandsSeparatorTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = appFieldColors(),
                        suffix = { Text("تومان", color = AppMuted, fontSize = 13.sp) },
                    )
                    // ورودی از اول تومانه، پس معادلِ حروفی مستقیم از همین عدد میاد.
                    val tomanVal = cleanNum(amountText).toLongOrNull() ?: 0L
                    if (tomanVal > 0) {
                        Text(
                            text = "${numberToWordsFa(tomanVal.toDouble())} تومان",
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
                        valueRange = DEPOSIT_MIN_TOMAN..DEPOSIT_MAX_TOMAN,
                        // پله‌بندی به گامِ ۱۰میلیون‌تومانی - رجوع کن به amountSliderSteps.
                        steps = amountSliderSteps(DEPOSIT_MIN_TOMAN..DEPOSIT_MAX_TOMAN),
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
                            rateText = trimRateDeposit(v)
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
                // دکمه **می‌ماند** ولی کارش عوض شد: نتیجه از قبل در هیرو دیده می‌شود، پس
                // تنها چیزی که این تپ اضافه می‌کند **ثبت در تاریخچه** است.
                GradientButton(
                    onClick = {
                        val computed = live ?: return@GradientButton
                        historyViewModel.log(
                            kind = "DEPOSIT",
                            title = "سود سپرده",
                            summary = "مبلغ ${fmt(principalToman.toDouble()).faDigits()} تومان × ${toFa(selectedMonths)} ماه",
                            amount = computed.finalAmount,
                        )
                    },
                    enabled = live != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("ثبت در تاریخچه")
                }
            }
        }
    }
}

/** ردیفِ برچسب/مقدارِ کارتِ پشتوانه - جای `DepositStat`ِ جعبه‌ای. */
@Composable
private fun DepositRow(label: String, rial: Double, privacyMode: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            color = AppMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                maskIfPrivate(masked, amountToman(countUpDouble(rial))),
                color = AppText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

/** برچسبِ مدت - همان متنِ قرص‌ها، تا هیرو و انتخابگر یک زبان داشته باشند. */
private fun depositMonthLabel(months: Int): String =
    depositMonthOptions.firstOrNull { it.first == months }?.second ?: "${toFa(months)} ماه"
