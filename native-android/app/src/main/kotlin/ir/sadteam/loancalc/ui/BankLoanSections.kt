package ir.sadteam.loancalc.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.LoanCalculator
import ir.sadteam.loancalc.core.LoanMethod
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.BankEntry
import ir.sadteam.loancalc.data.banks
import ir.sadteam.loancalc.ui.components.StaggerIn
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.AutoShrinkText
import ir.sadteam.loancalc.ui.components.BankTile
import ir.sadteam.loancalc.ui.components.BankTileShimmer
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.SlimSlider
import ir.sadteam.loancalc.ui.components.lazyRowScrollbar
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.jibak.tomanToRial
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppSegmentPill
import ir.sadteam.loancalc.ui.theme.AppSegmentRail
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppText
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Color
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import androidx.compose.runtime.MutableState
import androidx.compose.foundation.lazy.LazyItemScope

@Composable
internal fun BankLoanHeroSection(
    onLiveInstallment: (Double?) -> Unit,
    privacyMode: Boolean,
    amountTextState: MutableState<String>,
    rateTextState: MutableState<String>,
    selectedMonthsState: MutableState<Int>,
    customMonthsTextState: MutableState<String>,
    intervalDaysState: MutableState<Int>,
    graceOnState: MutableState<Boolean>,
    graceMonthsState: MutableState<Float>,
) {
    var amountText by amountTextState
    var rateText by rateTextState
    var selectedMonths by selectedMonthsState
    var customMonthsText by customMonthsTextState
    var intervalDays by intervalDaysState
    var graceOn by graceOnState
    var graceMonths by graceMonthsState
            val heroToman = cleanNum(amountText).toLongOrNull() ?: 0L
            val heroN = customMonthsText.toIntOrNull() ?: selectedMonths
            val heroRate = rateText.toDoubleOrNull() ?: 0.0
            // `compute` روی ترکیبِ نامعتبر (قرض‌الحسنه با یک قسط) تقسیم بر صفر می‌کرد، و
            // این‌جا برخلافِ دکمه راهی برای نشان‌دادنِ خطا نیست - پس هیرو در آن حالت
            // فقط ساخته نمی‌شود.
            val heroResult = remember(heroToman, heroN, heroRate, graceOn, graceMonths, intervalDays) {
                if (heroToman <= 0 || heroN <= 0) {
                    null
                } else {
                    runCatching {
                        LoanCalculator.compute(
                            tomanToRial(heroToman).toDouble(),
                            heroRate,
                            heroN,
                            if (heroRate > 0.0 && heroRate <= 4.0) LoanMethod.QARZ else LoanMethod.STANDARD,
                            if (graceOn) graceMonths.toInt() else 0,
                            intervalDays,
                        )
                    }.getOrNull()
                }
            }
            // فریمِ `70a`: میزبان قسطِ زنده را از همین‌جا می‌گیرد، نه از تپِ دکمه.
            // `LaunchedEffect` روی خودِ مقدار: فقط وقتی عوض شد خبر می‌رود، نه هر recomposition.
            LaunchedEffect(heroResult?.installment) { onLiveInstallment(heroResult?.installment) }
            if (heroResult != null) {
                StaggerIn(0) {
                    AppHeroCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "قسطِ ماهانه",
                                color = HeroMuted,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                            )
                            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 3.dp)) {
                                PrivacyCrossfade(privacyMode) { masked ->
                                    AutoShrinkText(
                                        text = maskIfPrivate(masked, amountToman(heroResult.installment)),
                                        color = Color.White,
                                        maxFontSize = 25.sp,
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
                            Text(
                                "${toFa(heroN)} قسط",
                                color = HeroMuted,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                            // کارتِ نتیجه‌ی برجسته (فریمِ `35`): کلِ بازپرداخت و سود هر کدام خانه‌ی خودشان را
                            // دارند، نه یک خطِ ریزِ زیرِ قسط.
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            ) {
                                HeroResultCell("کلِ بازپرداخت", heroResult.totalPaid, privacyMode, Modifier.weight(1f))
                                HeroResultCell("سودِ کل", heroResult.totalInterest, privacyMode, Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
}

@Composable
internal fun LazyItemScope.BankLoanInputsSection(
    creditServices: List<BankEntry>,
    creditRatesLoading: Boolean,
    applyAmount: (Long) -> Unit,
    selectedBankNameState: MutableState<String?>,
    selectedPresetKeyState: MutableState<String?>,
    rateSourceState: MutableState<RateSource>,
    selectedLoanTypeState: MutableState<String?>,
    amountSliderRangeState: MutableState<ClosedFloatingPointRange<Float>>,
    rateTextState: MutableState<String>,
    rateSliderState: MutableState<Float>,
    selectedMonthsState: MutableState<Int>,
    customMonthsTextState: MutableState<String>,
) {
    var selectedBankName by selectedBankNameState
    var selectedPresetKey by selectedPresetKeyState
    var rateSource by rateSourceState
    var selectedLoanType by selectedLoanTypeState
    var amountSliderRange by amountSliderRangeState
    var rateText by rateTextState
    var rateSlider by rateSliderState
    var selectedMonths by selectedMonthsState
    var customMonthsText by customMonthsTextState
            StaggerIn(2) {
                AppCard(label = "بانک یا سرویس اعتباری") {
                    // LazyRow به‌جای Row+horizontalScroll: قبلاً هر ۳۴ لوگوی بانک + ۶ سرویس همیشه یک‌جا
                    // compose می‌شدن (یکی از منابع اصلی لگ تعویض تب)؛ حالا فقط ~۵ تای قابل‌دیدن.
                    val banksScroll = rememberLazyListState()
                    val creditScroll = rememberLazyListState()
                    // هایلایتِ نوریِ مدام رو نشانگرهای اسکرولِ زیرِ بانک‌ها/خدمات (خواسته‌ی کاربر «اسکرول
                    // زیر بانک‌ها رو یکم شیک‌تر بکن») - مستقل از خودِ اسکرول، همیشه در حال حرکته.
                    val shimmerTransition = rememberInfiniteTransition(label = "bankScrollShimmer")
                    val shimmerPhase by shimmerTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart),
                        label = "shimmerPhase",
                    )
                    // سگمنتِ منبعِ نرخ (`27f`) - ریلِ چیپ با قرصِ فعالِ سطحِ سفید.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(AppSegmentRail)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        RateSource.entries.forEach { src ->
                            val selected = src == rateSource
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(999.dp))
                                    .then(if (selected) Modifier.background(AppSegmentPill) else Modifier)
                                    .pressScaleClickable { rateSource = src }
                                    .heightIn(min = 44.dp)
                                    .padding(vertical = 7.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    src.label,
                                    color = if (selected) AppText else AppMuted,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Black else FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }

                    if (rateSource == RateSource.BANK_LOAN) {
                        // قرص‌های نوعِ وام - فقط فیلدِ نرخ رو پر می‌کنن، خودِ نرخ قابلِ ویرایش می‌مونه.
                        Text(
                            "نوعِ وام نرخ را می‌دهد",
                            fontSize = 10.sp,
                            color = AppMuted,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            loanTypePresets.forEach { (name, pct) ->
                                AppChip(
                                    label = if (pct != null) "$name · ${toFa(trimRate(pct))}٪" else name,
                                    selected = selectedLoanType == name,
                                    onClick = {
                                        selectedLoanType = name
                                        if (pct != null) {
                                            rateText = trimRate(pct)
                                            rateSlider = pct.toFloat()
                                        }
                                    },
                                )
                            }
                        }
                        // تو این حالت انتخابِ بانک **اختیاری**ه و فقط اسم/لوگو/رنگ می‌ده، هیچ نرخی نه.
                        Text(
                            "بانک (اختیاری — فقط برای اسم)",
                            fontSize = 13.sp,
                            color = AppMuted,
                            fontWeight = FontWeight.Bold,
                        )
                    } else {
                        Text("خدمات اعتباری", fontSize = 13.sp, color = AppMuted, fontWeight = FontWeight.Bold)
                    }
                    if (rateSource == RateSource.BANK_LOAN) {
                    LazyRow(
                        state = banksScroll,
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(banks, key = { it.name }) { b ->
                            BankTile(
                                bank = b,
                                selected = selectedBankName == b.name,
                                onClick = { selectedBankName = b.name },
                            )
                        }
                    }
                    // نشانگر اسکرول افقی زیر ردیفِ بانک‌ها (تو همون فاصله‌ی ظریفِ زیرِ لیبل).
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 8.dp)
                            .height(4.dp)
                            .lazyRowScrollbar(banksScroll, AppPrimary, shimmerPhase = shimmerPhase),
                    )
                    } else if (creditRatesLoading) {
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            repeat(5) { BankTileShimmer() }
                        }
                    } else {
                        LazyRow(
                            state = creditScroll,
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            // این لیست برخلافِ بانک‌ها ثابت نیست: نرخ‌ها از سرور می‌رسن و لیست
                            // جایگزین می‌شه - بدونِ animateItem اون لحظه یه پرشِ ناگهانیه.
                            items(creditServices, key = { it.name }) { b ->
                                BankTile(
                                    modifier = Modifier.animateItem(),
                                    bank = b,
                                    selected = selectedBankName == b.name,
                                    onClick = {
                                        selectedBankName = b.name
                                        rateText = trimRate(b.ratePct)
                                        rateSlider = b.ratePct.toFloat()
                                        selectedMonths = b.months
                                        customMonthsText = ""
                                        val mid = (b.minAmount + b.maxAmount) / 2
                                        applyAmount(mid)
                                        amountSliderRange = rialToToman(b.minAmount).toFloat()..
                                            rialToToman(b.maxAmount).toFloat()
                                        selectedPresetKey = null
                                    },
                                )
                            }
                        }
                    }
                    if (rateSource == RateSource.CREDIT_SERVICE) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                                .height(4.dp)
                                .lazyRowScrollbar(creditScroll, AppPrimary, shimmerPhase = shimmerPhase),
                        )
                        Text(
                            "شش سرویسِ اعتباری نرخِ واحد دارند، پس خودکار می‌آید. برای وامِ بانکی " +
                                "نرخ به نوعِ وام بستگی دارد نه به بانک.",
                            fontSize = 9.5.sp,
                            color = AppMuted,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
}

@Composable
internal fun BankLoanAdvancedSection(
    intervalDaysState: MutableState<Int>,
    graceOnState: MutableState<Boolean>,
    graceMonthsState: MutableState<Float>,
) {
    var intervalDays by intervalDaysState
    var graceOn by graceOnState
    var graceMonths by graceMonthsState
            AppCard(label = "تنظیماتِ پیشرفته") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "فاصله‌ی هر قسط",
                        color = AppText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()).padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    intervalChipOptions.forEach { (v, label) ->
                        AppChip(
                            label = label,
                            selected = intervalDays == v,
                            onClick = { intervalDays = v },
                        )
                    }
                }
                HorizontalDivider(color = AppLine, modifier = Modifier.padding(vertical = 10.dp))
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

@Composable
internal fun BankLoanActionsSection(
    onCalculated: (BankLoanOutcome) -> Unit,
    footer: @Composable () -> Unit,
    selectedBank: BankEntry?,
    borrowerNameState: MutableState<String>,
    rateSourceState: MutableState<RateSource>,
    startYearState: MutableState<Int>,
    startMonthState: MutableState<Int>,
    startDayState: MutableState<Int>,
    amountTextState: MutableState<String>,
    rateTextState: MutableState<String>,
    selectedMonthsState: MutableState<Int>,
    customMonthsTextState: MutableState<String>,
    intervalDaysState: MutableState<Int>,
    graceOnState: MutableState<Boolean>,
    graceMonthsState: MutableState<Float>,
    formErrorState: MutableState<String?>,
) {
    var borrowerName by borrowerNameState
    var rateSource by rateSourceState
    var startYear by startYearState
    var startMonth by startMonthState
    var startDay by startDayState
    var amountText by amountTextState
    var rateText by rateTextState
    var selectedMonths by selectedMonthsState
    var customMonthsText by customMonthsTextState
    var intervalDays by intervalDaysState
    var graceOn by graceOnState
    var graceMonths by graceMonthsState
    var formError by formErrorState
            val tomanAmount = cleanNum(amountText).toLongOrNull() ?: 0L
            val n = customMonthsText.toIntOrNull() ?: selectedMonths
            val rate = rateText.toDoubleOrNull() ?: 0.0
            formError?.let {
                Text(
                    it,
                    color = AppDanger,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    textAlign = TextAlign.Center,
                )
            }
            GradientButton(
                onClick = {
                    // قبلاً `return@GradientButton`ِ خالی بود: تپ روی فیلدِ خالی بی هیچ نشونه‌ای
                    // هیچ‌کاری نمی‌کرد و کاربر فکر می‌کرد دکمه خرابه.
                    if (tomanAmount <= 0) {
                        formError = "مبلغِ وام رو وارد کن"
                        return@GradientButton
                    }
                    if (n <= 0) {
                        formError = "تعدادِ اقساط باید بیشتر از صفر باشه"
                        return@GradientButton
                    }
                    // 🚨 روشِ قرض‌الحسنه قسطِ اولِ هر سال را کاملاً کارمزدی می‌گیرد، پس با یک قسط
                    // هیچ قسطی برای اصلِ وام نمی‌مانَد و محاسبه تقسیم بر صفر می‌شد (کرش).
                    // ⚠️ نرخِ **صفر** هم عمداً از این مسیر بیرون رفت: خریدِ اقساطیِ بدونِ سود
                    // (مثلِ پیش‌تنظیمِ چهارقسطی) باید اصل را بینِ همه‌ی اقساط مساوی تقسیم کند،
                    // نه اینکه قسطِ اولش صفر شود - و مسیرِ STANDARD در نرخِ صفر دقیقاً همین است.
                    if (rate in 0.0..4.0 && rate > 0.0 && n == 1) {
                        formError = "وامِ قرض‌الحسنه با یک قسط قابلِ محاسبه نیست"
                        return@GradientButton
                    }
                    formError = null
                    val method = if (rate > 0.0 && rate <= 4.0) LoanMethod.QARZ else LoanMethod.STANDARD
                    val grace = if (graceOn) graceMonths.toInt() else 0
                    // ورودی تومانه و موتورِ محاسبه ریال - تبدیل فقط همین یک نقطه.
                    val rialAmount = tomanToRial(tomanAmount)
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
                            rateSourceLabel = rateSource.label,
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                // فریمِ `69b` بندِ ۶: «محاسبه کن» وقتی درست بود که نتیجه فقط آن‌جا دیده
                // می‌شد. با هیروِ زنده، محاسبه از قبل جلوی چشم است و دکمه کارِ واقعی‌اش را
                // می‌گوید: رفتن به جدول.
                Text("جدولِ اقساط را ببین", fontWeight = FontWeight.Bold)
            }

            // فریمِ `69b` بندِ ۵: پانویس `AppCard` نمی‌گیرد - یک کارت که فقط متنِ
            // خاکستریِ وسط‌چین دارد، وزنِ یک فیلد می‌گیرد برای چیزی که پانویس است.
            Text(
                "کارمزد بانک به‌صورت خودکار طبق قانون بانک مرکزی و ضوابط هر بانک محاسبه می‌شود.",
                fontSize = 10.sp,
                color = AppMuted,
                fontWeight = FontWeight.Bold,
                lineHeight = 17.sp,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, start = 4.dp, end = 4.dp),
                textAlign = TextAlign.Center,
            )

            // «امور چک» از تهِ محاسبه‌گر برداشته شد (۱۰ مهر، خواسته‌ی کاربر) - از خانه/سررسید در دسترس است.
            footer()
}
