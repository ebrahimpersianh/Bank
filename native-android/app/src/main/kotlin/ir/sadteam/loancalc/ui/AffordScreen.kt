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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import java.util.Locale

private val affordMonthChipValues = listOf(12, 24, 36, 60, 120)

// همه‌ی فیلدهای این صفحه **تومان**ن (بندِ ۲ی README)؛ موتورهای محاسبه ریال می‌گیرن، پس تبدیل
// فقط تو لبه‌ی هر `onClick` می‌شینه. بازه‌های اسلایدر هم ÷۱۰ شدن.
private const val PAY_MIN_TOMAN = 1_000_000f
private const val PAY_MAX_TOMAN = 50_000_000f
private const val LOAN_MIN_TOMAN = 10_000_000f
private const val LOAN_MAX_TOMAN = 1_000_000_000f
private const val INST_MIN_TOMAN = 100_000f
private const val INST_MAX_TOMAN = 20_000_000f

private fun amountToman(rial: Double): String = fmt(rialToToman(rial.toLong()).toDouble()).faDigits()

/** نمایشِ حداکثر دو رقمِ اعشار. `Locale.US` اجباریه - خروجی تو فیلدِ نرخ می‌شینه که بعد
 * `toDoubleOrNull()` می‌شه؛ رقمِ فارسی یعنی نرخِ بی‌صدا صفر. */
private fun trimRateAfford(v: Float): String =
    if (v == v.toLong().toFloat()) v.toLong().toString() else String.format(Locale.US, "%.2f", v)

/** پورت مو‌به‌موی تب «چقدر وام می‌تونم بگیرم؟» (view-afford تو www/index.html). */
@Composable
fun AffordScreen(
    historyViewModel: CalculationHistoryViewModel = hiltViewModel(),
    /** قسطی که از حالتِ «قسط از نرخِ بانک» اومده - فرم با همون از پیش پر می‌شه تا کاربر لازم
     * نباشه عددی که همین الان محاسبه شد رو دستی دوباره بزنه (بندِ صریحِ فریمِ `27f`). */
    initialInstallment: Double? = null,
) {
    // هر جا مبلغ نمایش داده می‌شود باید از حالتِ خصوصی عبور کند (بندِ ۳ی README).
    // ⚠️ این صفحه تا امروز **هیچ** ماسکی نداشت و سه مبلغ نشان می‌دهد.
    val privacyMode = LocalPrivacyMode.current

    // `initialInstallment` از موتور میاد پس ریاله؛ فیلد تومانه.
    //
    // ⚠️ `remember(initialInstallment)` نه `remember`ِ خالی: قسطِ زنده‌ی کارتِ پل با هر
    // دستکاریِ فرمِ قبلی عوض می‌شود، و بی این کلید صفحه عددِ **اولِ** ورود را نگه می‌داشت.
    val seed = initialInstallment?.takeIf { it > 0 }?.let { rialToToman(it.toLong()) }
    var payText by rememberSaveable(initialInstallment) { mutableStateOf(seed?.toString() ?: "10000000") }
    var paySlider by rememberSaveable(initialInstallment) {
        mutableFloatStateOf((seed ?: 10_000_000L).toFloat().coerceIn(PAY_MIN_TOMAN, PAY_MAX_TOMAN))
    }

    // `rememberSaveable` جای `remember`: چرخشِ گوشی کلِ فرم را پاک می‌کرد.
    var rateText by rememberSaveable { mutableStateOf("23") }
    var rateSlider by rememberSaveable { mutableFloatStateOf(23f) }

    var monthsText by rememberSaveable { mutableStateOf("36") }

    // فریمِ `70b`: نتیجه **زنده** است، نه گره‌خورده به دکمه. `computeMaxPrincipal` یک
    // فرمولِ بسته است و هر recomposition ارزان اجرا می‌شود.
    val payToman = cleanNum(payText).toLongOrNull() ?: 0L
    val rate = rateText.toDoubleOrNull() ?: 0.0
    val months = monthsText.toIntOrNull() ?: 0
    val liveMax = remember(payToman, rate, months) {
        if (payToman <= 0 || months <= 0) {
            null
        } else {
            runCatching {
                AffordabilityCalculator.computeMaxPrincipal(tomanToRial(payToman).toDouble(), rate, months)
            }.getOrNull()?.takeIf { it > 0 }
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
        // فریمِ `70b`: هیروِ زنده - همان الگوی `69a`.
        if (liveMax != null) {
            item {
                StaggerIn(0) {
                    AppHeroCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "حداکثر وامی که می‌توانی بگیری",
                                color = HeroMuted,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                            )
                            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 3.dp)) {
                                PrivacyCrossfade(privacyMode) { masked ->
                                    Text(
                                        maskIfPrivate(masked, amountToman(countUpDouble(liveMax))),
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
                                    "با قسطِ ${maskIfPrivate(masked, fmt(payToman.toDouble()).faDigits())} · ${toFa(months)} ماه · ${toFa(trimRateAfford(rate.toFloat()))}٪",
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
                // معادلِ حروفی **زیرِ** هیرو، نه داخلش: در هیرو با عددِ درشت رقابت می‌کند.
                AppCard {
                    PrivacyCrossfade(privacyMode) { masked ->
                        Text(
                            maskIfPrivate(
                                masked,
                                "${numberToWordsFa(rialToToman(liveMax.toLong()).toDouble())} تومان",
                            ),
                            color = AppMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 19.sp,
                        )
                    }
                }
            }
        }

        item {
            StaggerIn(0) {
                AppCard(label = "مبلغی که می‌تونم ماهانه قسط بدم") {
                    OutlinedTextField(
                        value = payText,
                        onValueChange = { raw ->
                            val digits = cleanNum(raw)
                            val n = digits.toLongOrNull() ?: 0L
                            payText = digits
                            if (n in PAY_MIN_TOMAN.toLong()..PAY_MAX_TOMAN.toLong()) paySlider = n.toFloat()
                        },
                        visualTransformation = ThousandsSeparatorTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = appFieldColors(),
                        suffix = { Text("تومان", color = AppMuted, fontSize = 13.sp) },
                    )
                    // ورودی از اول تومانه، پس معادلِ حروفی مستقیم از همین عدد میاد.
                    val tomanVal = cleanNum(payText).toLongOrNull() ?: 0L
                    if (tomanVal > 0) {
                        Text(
                            text = "${numberToWordsFa(tomanVal.toDouble())} تومان",
                            color = AppMuted,
                            fontSize = 11.5.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    SlimSlider(
                        value = paySlider,
                        onValueChange = { v ->
                            paySlider = v
                            payText = v.toLong().toString()
                        },
                        valueRange = PAY_MIN_TOMAN..PAY_MAX_TOMAN,
                        // پله‌بندی به گامِ نیم‌میلیون‌تومانی (رنجِ این فیلد کوچیک‌تر از مبلغِ وامه،
                        // برای همین گامِ ریزتر - رجوع کن به amountSliderSteps).
                        steps = amountSliderSteps(PAY_MIN_TOMAN..PAY_MAX_TOMAN, chunk = 500_000f),
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
                            rateText = trimRateAfford(v)
                        },
                        valueRange = 0f..50f,
                        steps = 99,
                    )
                }
            }
        }

        item {
            StaggerIn(2) {
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
        }

        item {
            StaggerIn(3) {
                // دکمه **می‌ماند** ولی کارش عوض شد: نتیجه از قبل در هیرو دیده می‌شود، پس
                // تنها چیزی که این تپ اضافه می‌کند **ثبت در تاریخچه** است - و آن یک کنشِ
                // عمدی است، نه محاسبه. متنِ «محاسبه حداکثر وام» دیگر کارِ واقعی‌اش را
                // نمی‌گفت.
                GradientButton(
                    onClick = {
                        val max = liveMax ?: return@GradientButton
                        historyViewModel.log(
                            kind = "AFFORD",
                            title = "محاسبه‌گر سقف وام",
                            summary = "قسط ${fmt(payToman.toDouble()).faDigits()} تومان × ${toFa(months)} ماه، نرخ ${toFa(rate)}٪",
                            amount = max,
                        )
                    },
                    enabled = liveMax != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("ثبت در تاریخچه")
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
    var amountSlider by remember { mutableFloatStateOf(LOAN_MIN_TOMAN) }
    var installmentText by remember { mutableStateOf("") }
    var installmentSlider by remember { mutableFloatStateOf(INST_MIN_TOMAN) }
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
                    amountText = digits
                    if (n in LOAN_MIN_TOMAN.toLong()..LOAN_MAX_TOMAN.toLong()) amountSlider = n.toFloat()
                },
                visualTransformation = ThousandsSeparatorTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = appFieldColors(),
                suffix = { Text("مبلغ وام (تومان)", color = AppMuted, fontSize = 13.sp) },
            )
            (cleanNum(amountText).toLongOrNull() ?: 0L).takeIf { it > 0 }?.let { t ->
                Text(
                    "${numberToWordsFa(t.toDouble())} تومان",
                    color = AppMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            SlimSlider(
                value = amountSlider,
                onValueChange = { v -> amountSlider = v; amountText = v.toLong().toString() },
                valueRange = LOAN_MIN_TOMAN..LOAN_MAX_TOMAN,
                modifier = Modifier.padding(top = 6.dp),
                steps = amountSliderSteps(LOAN_MIN_TOMAN..LOAN_MAX_TOMAN),
            )
            OutlinedTextField(
                value = installmentText,
                onValueChange = { raw ->
                    val digits = cleanNum(raw)
                    val n = digits.toLongOrNull() ?: 0L
                    installmentText = digits
                    if (n in INST_MIN_TOMAN.toLong()..INST_MAX_TOMAN.toLong()) installmentSlider = n.toFloat()
                },
                visualTransformation = ThousandsSeparatorTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                singleLine = true,
                colors = appFieldColors(),
                suffix = { Text("مبلغ هر قسط (تومان)", color = AppMuted, fontSize = 13.sp) },
            )
            (cleanNum(installmentText).toLongOrNull() ?: 0L).takeIf { it > 0 }?.let { t ->
                Text(
                    "${numberToWordsFa(t.toDouble())} تومان",
                    color = AppMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            SlimSlider(
                value = installmentSlider,
                onValueChange = { v -> installmentSlider = v; installmentText = v.toLong().toString() },
                valueRange = INST_MIN_TOMAN..INST_MAX_TOMAN,
                modifier = Modifier.padding(top = 6.dp),
                steps = amountSliderSteps(INST_MIN_TOMAN..INST_MAX_TOMAN, chunk = 100_000f),
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
                    // هر دو فیلد تومانن؛ نسبتشون تو نرخ اثر نداره ولی موتور ریال می‌گیره.
                    val principal = tomanToRial(cleanNum(amountText).toLongOrNull() ?: 0L)
                    val installment = tomanToRial(cleanNum(installmentText).toLongOrNull() ?: 0L)
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
                    text = "نرخ سود سالانه تقریبی: ${toFa(String.format(Locale.US, "%.1f", r))}٪",
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
