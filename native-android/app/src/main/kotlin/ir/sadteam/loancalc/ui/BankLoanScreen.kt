package ir.sadteam.loancalc.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.LoanCalculator
import ir.sadteam.loancalc.core.LoanMethod
import ir.sadteam.loancalc.core.LoanResult
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.cleanNumDecimal
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.BankEntry
import ir.sadteam.loancalc.data.CreditRatesViewModel
import ir.sadteam.loancalc.data.banks
import ir.sadteam.loancalc.data.loanPresets
import ir.sadteam.loancalc.ui.cheque.ChequeScreen
import ir.sadteam.loancalc.ui.components.StaggerIn
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.AutoShrinkText
import ir.sadteam.loancalc.ui.components.BankTile
import ir.sadteam.loancalc.ui.components.BankTileShimmer
import ir.sadteam.loancalc.ui.components.CalendarPickerScreen
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InlineJalaliDateRow
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.PresetCard
import ir.sadteam.loancalc.ui.components.SlimSlider
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.components.lazyRowScrollbar
import ir.sadteam.loancalc.ui.components.lazyColumnScrollbar
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.jibak.tomanToRial
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppSegmentPill
import ir.sadteam.loancalc.ui.theme.AppSegmentRail
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppText

private val monthChipValues = listOf(12, 18, 24, 36, 60, 84, 120, 180, 240)

/**
 * فیلدِ مبلغ و اسلایدرش **تومان**ند (بندِ ۲ی README: ذخیره ریال، نمایش تومان). هر عددی که
 * از پریست/سرویسِ اعتباری میاد ریاله، پس در `applyAmount` به تومان برمی‌گرده و سرِ
 * `محاسبه کن` با `tomanToRial` به ریال.
 */
private const val AMOUNT_STEP_TOMAN = 10_000_000f
private val defaultAmountRangeToman = 10_000_000f..1_000_000_000f

/**
 * گامِ گردِ ۱۰میلیون‌تومانی. `amountSliderSteps` عمداً صدا زده نمی‌شه چون گامش رو **ریالی**
 * حساب می‌کرد؛ روی بازه‌ی تومانی گامِ ۱۰برابر درشت می‌داد.
 */
private fun tomanSliderSteps(range: ClosedFloatingPointRange<Float>): Int =
    ((range.endInclusive - range.start) / AMOUNT_STEP_TOMAN).toInt().minus(1).coerceAtLeast(0)
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
fun BankLoanScreen(
    onCalculated: (BankLoanOutcome) -> Unit,
    /** هر دستکاریِ ورودی، نتیجه‌ی قبلی رو باطل می‌کنه - کارتِ پلِ سبزِ `27f` وگرنه عددِ
     * کهنه رو با خودش به حالتِ توانِ بازپرداخت می‌بره. */
    onInputChanged: () -> Unit = {},
    creditRatesViewModel: CreditRatesViewModel = hiltViewModel(),
    /** خانه‌ی خالیِ **زیرِ** دکمه‌ی محاسبه - میزبانِ `27f` کارتِ «از عهده‌اش برمی‌آیم؟» رو
     * اینجا می‌ذاره. پیش‌فرض خالیه، پس هر جای دیگه‌ای که این صفحه صدا زده بشه فرقی نمی‌کنه. */
    footer: @Composable () -> Unit = {},
) {
    val creditServices by creditRatesViewModel.rates.collectAsState()
    val creditRatesLoading by creditRatesViewModel.isLoading.collectAsState()
    // فیلدهای ورودیِ ساده (String/Int/Float/Boolean) با rememberSaveable - چرخشِ صفحه یا اومدنِ اپ به
    // پس‌زمینه (که Compose گاهی state رو از دست می‌ده) دیگه فرمِ نیمه‌پرشده رو پاک نمی‌کنه. انتخابِ
    // بانک (BankEntry، شامل Color) عمداً هنوز remember ساده‌ست چون Saver سفارشی می‌خواد.
    var borrowerName by rememberSaveable { mutableStateOf("") }
    // فقط اسمِ بانک (String، قابلِ‌ذخیره) نگه داشته می‌شه، نه خودِ BankEntry (که Color داره و Saverِ
    // ساده نداره) - خودِ BankEntry هر بار از رو همین اسم از لیستِ بانک‌ها/خدماتِ اعتباری پیدا می‌شه.
    // این یعنی انتخابِ بانک هم مثلِ بقیه‌ی فیلدها، موقعِ برگشتن از «نتیجه‌ی محاسبه» (رجوع کن به
    // BankLoanTab تو MainActivity.kt) از دست نمی‌ره.
    var selectedBankName by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedBank = remember(selectedBankName, creditServices) {
        selectedBankName?.let { n -> banks.firstOrNull { it.name == n } ?: creditServices.firstOrNull { it.name == n } }
    }
    var selectedPresetKey by rememberSaveable { mutableStateOf<String?>(null) }
    // «تصمیمِ ۱٫۵»: منبعِ نرخ. پیش‌فرض سرویسِ اعتباریه چون تنها حالتیه که نرخ واقعاً خودکار میاد.
    var rateSource by rememberSaveable { mutableStateOf(RateSource.CREDIT_SERVICE) }
    var selectedLoanType by rememberSaveable { mutableStateOf<String?>(null) }

    // پیش‌فرض **امروز**ه. عددِ ثابتِ ۱۴۰۴/۱/۱ با گذشتِ سال کهنه می‌شد و کاربر تاریخی رو
    // می‌دید که هیچ ربطی به حالا نداشت.
    val today = remember { JalaliCalendar.today() }
    var startYear by rememberSaveable { mutableIntStateOf(today.y) }
    var startMonth by rememberSaveable { mutableIntStateOf(today.m) }
    var startDay by rememberSaveable { mutableIntStateOf(today.d) }

    // state فقط رقم نگه می‌داره؛ کاما نمایشیه (ThousandsSeparatorTransformation) - رجوع کن به کامنتِ فیلد.
    var amountText by rememberSaveable { mutableStateOf("250000000") }
    var amountSliderRange by remember { mutableStateOf(defaultAmountRangeToman) }
    var amountSlider by rememberSaveable { mutableFloatStateOf(250_000_000f) }

    var rateText by rememberSaveable { mutableStateOf("23") }
    var rateSlider by rememberSaveable { mutableFloatStateOf(23f) }

    var selectedMonths by rememberSaveable { mutableIntStateOf(36) }
    var customMonthsText by rememberSaveable { mutableStateOf("") }

    var intervalDays by rememberSaveable { mutableIntStateOf(30) }

    var graceOn by rememberSaveable { mutableStateOf(false) }
    var graceMonths by rememberSaveable { mutableFloatStateOf(6f) }
    // تپِ «محاسبه کن» با فیلدِ خالی قبلاً بی‌صدا هیچ‌کاری نمی‌کرد.
    var formError by remember { mutableStateOf<String?>(null) }

    var showCalendarPicker by rememberSaveable { mutableStateOf(false) }
    var showCheque by rememberSaveable { mutableStateOf(false) }

    // پریست‌ها و سرویس‌های اعتباری ریال می‌دن؛ فیلد تومانه.
    fun applyAmount(rial: Long) {
        val toman = rialToToman(rial)
        amountText = toman.toString()
        if (toman <= amountSliderRange.endInclusive.toLong()) amountSlider = toman.toFloat()
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

    // «امور چک» رو کاربر می‌خواست مستقیم زیرِ بانک‌ها/خدمات اعتباری تو همین تبِ «وام بانکی» هم در
    // دسترس باشه، نه فقط از تنظیمات - رجوع کن به کارتِ «امور چک» تو ادامه‌ی همین LazyColumn.
    if (showCheque) {
        ChequeScreen(onBack = { showCheque = false })
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
            StaggerIn(0) {
                // این کارت قبلاً یه حاشیه‌ی مشکی مخصوص خودش داشت؛ کاربر بعداً همون تصمیمِ «بدون خط دور»ی
                // که رو بقیه‌ی اپ اعمال شد رو اینجا هم خواست، پس override حذف شد - حالا مثل همه‌ی
                // AppCardهای دیگه از پیش‌فرضِ بدون‌حاشیه استفاده می‌کنه.
                AppCard(label = "وام‌های پرتکرار") {
                    // LazyRow به‌جای Row+horizontalScroll: فقط کارت‌های قابل‌دیدن compose می‌شن (پرفورمنس).
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(loanPresets, key = { it.key }) { p ->
                            PresetCard(
                                icon = p.icon,
                                title = p.title,
                                sub = p.sub,
                                selected = selectedPresetKey == p.key,
                                onClick = {
                                    onInputChanged()
                                    selectedPresetKey = p.key
                                    applyAmount(p.amount)
                                    amountSliderRange = defaultAmountRangeToman
                                    rateText = trimRate(p.ratePct)
                                    rateSlider = p.ratePct.toFloat()
                                    selectedMonths = p.months
                                    customMonthsText = ""
                                    graceOn = p.graceMonths > 0
                                    if (p.graceMonths > 0) graceMonths = p.graceMonths.toFloat()
                                },
                            )
                        }
                    }
                }
            }
        }

        item {
            StaggerIn(1) {
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
        }

        item {
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
                                        onInputChanged()
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
                                onClick = { onInputChanged(); selectedBankName = b.name },
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
                                        onInputChanged()
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

        item {
            StaggerIn(3) {
                // «امور چک» مستقیم زیرِ بانک‌ها/خدمات اعتباری (خواسته‌ی کاربر) - قبلاً فقط از تنظیمات
                // در دسترس بود.
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().pressScaleClickable { showCheque = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("امور چک", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "چک‌های دریافتی/پرداختی و دسته‌چک‌هات رو مدیریت کن",
                                color = AppMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        // `ArrowForwardIos`ِ خودچرخان: تو RTL چپ رو نشون می‌ده، یعنی «برو».
                        // `Icons.Filled.ArrowBack`ِ قبلی نه خودچرخان بود و نه معنیِ درستی داشت.
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = AppMuted,
                        )
                    }
                }
            }
        }

        item {
            StaggerIn(4) {
                // برچسب + توضیحِ دینامیک قبلاً دو تیکه‌ی جدا بودن (لیبلِ کارت + یه خطِ راهنمای زیرش) -
                // خواسته‌ی صریحِ کاربر (مورد ۳) یکی‌شدنشون تو یه جمله‌ی تمیزه، پس الان خودِ لیبلِ
                // کارت این توضیح رو داره، بدونِ نیازِ خط/متنِ جدا زیرِ تاریخ.
                val dateLabel = if (graceOn && graceMonths.toInt() > 0) {
                    "تاریخ دریافت وام (قسطِ اول ${toFa(graceMonths.toInt())} ماه بعد، به‌خاطرِ دوره‌ی تنفس)"
                } else {
                    "تاریخ دریافت وام (سررسیدِ قسطِ اول)"
                }
                AppCard(label = dateLabel) {
                    // تاریخ اینلاینِ چرخونه‌ای (روی روز/ماه/سال اسکرول می‌کنی) - همینجا عوض می‌شه بدون
                    // رفتن به یه صفحه‌ی جدا (خواسته‌ی کاربر، چندبار تکرار شد). آیکون تقویم کنارش، برای
                    // کسی که تقویم گریدیِ کامل رو بخواد.
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
        }

        item {
            StaggerIn(5) {
                AppCard(label = "مبلغ وام") {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { raw ->
                            // فقط رقم تو state می‌مونه؛ فرمتِ هزارگان نمایشیه (ThousandsSeparatorTransformation) -
                            // فرمت‌کردن تو onValueChange مکان‌نما رو می‌پروند و رقم وسطِ عدد درج می‌شد.
                            onInputChanged()
                            formError = null
                            val digits = cleanNum(raw)
                            val n = digits.toLongOrNull() ?: 0L
                            amountText = digits
                            if (n in amountSliderRange.start.toLong()..amountSliderRange.endInclusive.toLong()) {
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
                    // ورودی از اول تومانه، پس معادلِ حروفی مستقیم از همین عدد میاد - تقسیمِ
                    // دستیِ «/ ۱۰» رفت؛ تنها مرجعِ تبدیل tomanToRial/rialToToman ئه.
                    val tomanVal = cleanNum(amountText).toLongOrNull() ?: 0L
                    if (tomanVal > 0) {
                        // همیشه تک‌خطی - اگه جا نشه فونت کوچیک می‌شه، نه این‌که به خط دوم بشکنه.
                        AutoShrinkText(
                            text = "${numberToWordsFa(tomanVal.toDouble())} تومان",
                            color = AppMuted,
                            maxFontSize = 11.5.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    SlimSlider(
                        value = amountSlider,
                        onValueChange = { v ->
                            onInputChanged()
                            formError = null
                            amountSlider = v
                            amountText = v.toLong().toString()
                        },
                        valueRange = amountSliderRange,
                        // پله‌بندی به گام‌های ۱۰میلیون‌تومانی - خواسته‌ی صریحِ کاربر: کشیدنِ
                        // اسلایدر باید عددِ گرد بده (۲۰۰ بعد ۲۱۰ میلیون تومان...)، نه مقادیرِ
                        // پیوسته/نامرتب. رجوع کن به tomanSliderSteps بالای فایل.
                        steps = tomanSliderSteps(amountSliderRange),
                    )
                }
            }
        }

        item {
            StaggerIn(6) {
                // تو حالتِ سرویسِ اعتباری نرخ **خواندنی**ه (از سرور میاد)؛ تو وامِ بانکی دستیه و
                // قرص‌های نوعِ وام فقط پرش می‌کنن - «تصمیمِ ۱٫۵».
                val rateReadOnly = rateSource == RateSource.CREDIT_SERVICE
                AppCard(
                    label = if (rateReadOnly) "نرخ سود سالانه (از سرور)" else "نرخ سود سالانه (قابلِ تغییر)",
                ) {
                    OutlinedTextField(
                        value = rateText,
                        readOnly = rateReadOnly,
                        onValueChange = { raw ->
                            onInputChanged()
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
                            if (!rateReadOnly) {
                                onInputChanged()
                                rateSlider = v
                                rateText = trimRate(v.toDouble())
                            }
                        },
                        valueRange = 0f..50f,
                        // پله‌ی ۰.۵ درصدی - رجوع کن به کامنتِ SlimSlider برای فرمولِ steps.
                        steps = 99,
                    )
                }
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
                            onClick = { onInputChanged(); selectedMonths = v; customMonthsText = "" },
                        )
                    }
                }
                OutlinedTextField(
                    value = customMonthsText,
                    onValueChange = { onInputChanged(); formError = null; customMonthsText = cleanNum(it) },
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
                        AppChip(
                            label = label,
                            selected = intervalDays == v,
                            onClick = { onInputChanged(); intervalDays = v },
                        )
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
                        onCheckedChange = { onInputChanged(); graceOn = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = AppPrimaryDim, checkedThumbColor = AppPrimary),
                    )
                }
                if (graceOn) {
                    Text("مدت تنفس (ماه)", fontSize = 13.5.sp, color = AppMuted, modifier = Modifier.padding(top = 12.dp))
                    SlimSlider(
                        value = graceMonths,
                        onValueChange = { onInputChanged(); graceMonths = it },
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
                    textAlign = TextAlign.Center,
                )
            }
        }

        item {
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
                    formError = null
                    val method = if (rate <= 4.0) LoanMethod.QARZ else LoanMethod.STANDARD
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
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("محاسبه کن", fontWeight = FontWeight.Bold)
            }

            footer()
        }
    }
}

/**
 * منبعِ نرخ - «تصمیمِ ۱٫۵»ِ هندآفِ `27a`/`27f`.
 *
 * نرخِ خودکار **فقط** برای شش سرویسِ اعتباری معنی داره (نرخِ واحد دارن و از سرورِ خودمون میاد).
 * نرخِ وامِ بانک‌های دولتی به **نوعِ وام** بستگی داره نه به بانک (ازدواج ۴٪، مسکن ۱۸٪،
 * قرض‌الحسنه ۰٪)، پس «بانک ملی» یه نرخِ واحد نداره که خودکار پر بشه.
 */
private enum class RateSource(val label: String) {
    CREDIT_SERVICE("سرویسِ اعتباری"),
    BANK_LOAN("وامِ بانکی"),
}

/**
 * قرص‌های نوعِ وام تو حالتِ «وامِ بانکی» - **فقط فیلدِ نرخ رو پر می‌کنن** و نرخ قابلِ ویرایش می‌مونه.
 *
 * عمداً **محلیه نه سرور** (تاکیدِ صریحِ هندآف): سالی یه‌بار عوض می‌شن و آفلاین هم باید کار کنه.
 * `null` یعنی «سایر» - نرخ رو دست نمی‌زنه و به خودِ کاربر واگذار می‌کنه.
 */
private val loanTypePresets: List<Pair<String, Double?>> = listOf(
    "ازدواج" to 4.0,
    "مسکن" to 18.0,
    "قرض‌الحسنه" to 0.0,
    "سایر" to null,
)

private fun trimRate(v: Double): String {
    // نمایشِ حداکثر دو رقمِ اعشار (خواسته‌ی صریحِ کاربر، مورد ۱) - وگرنه v.toString() خامِ فلوتینگ-
    // پوینت می‌تونست چیزی مثلِ «23.500000001» نشون بده.
    return if (v == v.toLong().toDouble()) v.toLong().toString() else "%.2f".format(v)
}
