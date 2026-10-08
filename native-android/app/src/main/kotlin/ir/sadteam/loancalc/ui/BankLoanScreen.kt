package ir.sadteam.loancalc.ui

import ir.sadteam.loancalc.ui.components.HeroPillBg
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.LaunchedEffect
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
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.HeroMuted
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
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppSegmentPill
import ir.sadteam.loancalc.ui.theme.AppSegmentRail
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppText
import java.util.Locale
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Color
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.ui.jibak.faDigits

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
internal val intervalChipOptions = listOf(7 to "هفتگی", 14 to "دوهفته‌ای", 30 to "ماهانه", 60 to "دوماهه", 90 to "سه‌ماهه")

data class BankLoanOutcome(
    val result: LoanResult,
    val startDate: PersianDate,
    val ratePct: Double,
    val n: Int,
    val method: LoanMethod,
    val borrower: String,
    val bankName: String,
    /** `69b`: «سرویسِ اعتباری» یا «وامِ بانکی» - یعنی نرخ از سرور آمده یا کاربر خودش زده.
     * روی قرصِ منبعِ `ResultScreen` (`68`) دیده می‌شود، وگرنه ماه‌ها بعد معلوم نیست. */
    val rateSourceLabel: String = RateSource.BANK_LOAN.label,
)

@Composable
fun BankLoanScreen(
    onCalculated: (BankLoanOutcome) -> Unit,
    /**
     * قسطِ **زنده**ی هیرو - فریمِ `70a`. `null` یعنی ورودی ناقص است یا ترکیب نامعتبر
     * (قرض‌الحسنه با یک قسط) و هیرو ساخته نشده.
     *
     * جای `onInputChanged` آمد: آن کال‌بک وظیفه‌اش باطل‌کردنِ نتیجه‌ی کهنه بود، و کارتِ پل
     * عددش را از `onCalculated` می‌گرفت - یعنی **فقط با تپِ دکمه**. با هیروِ زنده و دکمه‌ای
     * که حالا فقط به جدول می‌رود، کاربری که فقط قسط را می‌خواست دکمه را نمی‌زند و کارتِ پل
     * هیچ‌وقت ظاهر نمی‌شد. حالا مقدار خودش با ورودی عوض می‌شود، پس **یک** منبعِ حقیقت است
     * نه دو تا.
     */
    onLiveInstallment: (Double?) -> Unit = {},
    /**
     * «افزودنِ وامِ دستی» - خواسته‌ی کاربر (۲۶ شهریور): دکمه‌ی «+»ِ فهرستِ وام‌ها حالا همین
     * صفحه را باز می‌کند، پس ثبتِ دستی باید از این‌جا هم در دسترس باشد. `{}` یعنی این صفحه
     * از جایی باز شده که مقصدی برایش ندارد (آن‌وقت ردیفش اصلاً ساخته نمی‌شود).
     */
    onAddManualLoan: () -> Unit = {},
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
    val borrowerNameState = rememberSaveable { mutableStateOf("") }
    var borrowerName by borrowerNameState
    // فقط اسمِ بانک (String، قابلِ‌ذخیره) نگه داشته می‌شه، نه خودِ BankEntry (که Color داره و Saverِ
    // ساده نداره) - خودِ BankEntry هر بار از رو همین اسم از لیستِ بانک‌ها/خدماتِ اعتباری پیدا می‌شه.
    // این یعنی انتخابِ بانک هم مثلِ بقیه‌ی فیلدها، موقعِ برگشتن از «نتیجه‌ی محاسبه» (رجوع کن به
    // BankLoanTab تو MainActivity.kt) از دست نمی‌ره.
    val selectedBankNameState = rememberSaveable { mutableStateOf<String?>(null) }
    var selectedBankName by selectedBankNameState
    val selectedBank = remember(selectedBankName, creditServices) {
        selectedBankName?.let { n -> banks.firstOrNull { it.name == n } ?: creditServices.firstOrNull { it.name == n } }
    }
    val selectedPresetKeyState = rememberSaveable { mutableStateOf<String?>(null) }
    var selectedPresetKey by selectedPresetKeyState
    // «تصمیمِ ۱٫۵»: منبعِ نرخ. پیش‌فرض سرویسِ اعتباریه چون تنها حالتیه که نرخ واقعاً خودکار میاد.
    val rateSourceState = rememberSaveable { mutableStateOf(RateSource.CREDIT_SERVICE) }
    var rateSource by rateSourceState
    val selectedLoanTypeState = rememberSaveable { mutableStateOf<String?>(null) }
    var selectedLoanType by selectedLoanTypeState

    // پیش‌فرض **امروز**ه. عددِ ثابتِ ۱۴۰۴/۱/۱ با گذشتِ سال کهنه می‌شد و کاربر تاریخی رو
    // می‌دید که هیچ ربطی به حالا نداشت.
    val today = remember { JalaliCalendar.today() }
    val startYearState = rememberSaveable { mutableIntStateOf(today.y) }
    var startYear by startYearState
    val startMonthState = rememberSaveable { mutableIntStateOf(today.m) }
    var startMonth by startMonthState
    val startDayState = rememberSaveable { mutableIntStateOf(today.d) }
    var startDay by startDayState

    // هر جا مبلغ نمایش داده می‌شود باید از حالتِ خصوصی عبور کند (بندِ ۳ی README) - هیروِ
    // زنده سه مبلغ نشان می‌دهد و تا امروز این صفحه هیچ مبلغِ **محاسبه‌شده**ای نداشت.
    val privacyMode = LocalPrivacyMode.current

    // state فقط رقم نگه می‌داره؛ کاما نمایشیه (ThousandsSeparatorTransformation) - رجوع کن به کامنتِ فیلد.
    val amountTextState = rememberSaveable { mutableStateOf("250000000") }
    var amountText by amountTextState
    val amountSliderRangeState = remember { mutableStateOf(defaultAmountRangeToman) }
    var amountSliderRange by amountSliderRangeState
    var amountSlider by rememberSaveable { mutableFloatStateOf(250_000_000f) }

    val rateTextState = rememberSaveable { mutableStateOf("23") }
    var rateText by rateTextState
    val rateSliderState = rememberSaveable { mutableFloatStateOf(23f) }
    var rateSlider by rateSliderState

    val selectedMonthsState = rememberSaveable { mutableIntStateOf(36) }
    var selectedMonths by selectedMonthsState
    val customMonthsTextState = rememberSaveable { mutableStateOf("") }
    var customMonthsText by customMonthsTextState

    val intervalDaysState = rememberSaveable { mutableIntStateOf(30) }
    var intervalDays by intervalDaysState

    val graceOnState = rememberSaveable { mutableStateOf(false) }
    var graceOn by graceOnState
    val graceMonthsState = rememberSaveable { mutableFloatStateOf(6f) }
    var graceMonths by graceMonthsState
    // تپِ «محاسبه کن» با فیلدِ خالی قبلاً بی‌صدا هیچ‌کاری نمی‌کرد.
    val formErrorState = remember { mutableStateOf<String?>(null) }
    var formError by formErrorState

    var showCalendarPicker by rememberSaveable { mutableStateOf(false) }
    var showCheque by rememberSaveable { mutableStateOf(false) }

    // پریست‌ها و سرویس‌های اعتباری ریال می‌دن؛ فیلد تومانه.
    fun applyAmount(rial: Long) {
        val toman = rialToToman(rial)
        amountText = toman.toString()
        if (toman <= amountSliderRange.endInclusive.toLong()) amountSlider = toman.toFloat()
    }

    val listState = rememberLazyListState()
    val scrollbarColor = AppPrimary

    // 🚨 پنجمین و ششمین موردِ باگِ `return`: تقویم و «امور چک» هر دو با `return` کلِ
    // `LazyColumn` را از composition بیرون می‌کردند، پس موقعِ بازگشت اسکرول **صفر** می‌شد.
    // فیلدها `rememberSaveable`ند و داده نمی‌رفت، ولی کاربرِ وسطِ یک فرمِ یازده‌فیلدی
    // می‌پرید سرِ بالای فرم و فکر می‌کرد پاک شده.
    //
    // حالا هر دو **پوششِ تمام‌صفحه** روی همان `LazyColumn`ند: فهرست composed می‌ماند و
    // `listState` سرِ جایش.
    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .lazyColumnScrollbar(listState, scrollbarColor),
        contentPadding = PaddingValues(14.dp, 14.dp, 14.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // 🚨 **بالای صفحه، نه تهِ آن** (جوابِ طراح، دورِ ۸).
        //
        // «+»ِ فهرستِ وام‌ها این صفحه را باز می‌کند، پس کسی که «+» زده ممکن است **فرم**
        // می‌خواسته نه ماشین‌حساب. اولین چیزی که می‌بیند باید راهِ رسیدن به فرم باشد، نه
        // آخرین - نمادِ FAB به‌تنهایی این شکاف را پر نمی‌کند.
        item {
            AppCard(modifier = Modifier.padding(bottom = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().pressScaleClickable { onAddManualLoan() },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("افزودنِ وامِ دستی", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "وامی که از قبل گرفته‌ای رو ثبت کن تا قسط‌هاش پیگیری بشن",
                            color = AppMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = AppMuted,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }

        // فریمِ `69a`: هیروِ زنده - همان الگوی `67a`.
        //
        // کاربر یازده فیلد را پر می‌کرد و برای دیدنِ قسط باید به صفحه‌ی دیگری می‌رفت؛ اگر
        // عدد غلط بود برمی‌گشت و دوباره. محاسبه از قبل بی‌هزینه در دست است، فقط دیده
        // نمی‌شد.
        item {
            BankLoanHeroSection(
                onLiveInstallment = onLiveInstallment,
                privacyMode = privacyMode,
                amountTextState = amountTextState,
                rateTextState = rateTextState,
                selectedMonthsState = selectedMonthsState,
                customMonthsTextState = customMonthsTextState,
                intervalDaysState = intervalDaysState,
                graceOnState = graceOnState,
                graceMonthsState = graceMonthsState,
            )
        }

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
                        colors = appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                }
            }
        }

        item {
            BankLoanInputsSection(
                creditServices = creditServices,
                creditRatesLoading = creditRatesLoading,
                applyAmount = ::applyAmount,
                selectedBankNameState = selectedBankNameState,
                selectedPresetKeyState = selectedPresetKeyState,
                rateSourceState = rateSourceState,
                selectedLoanTypeState = selectedLoanTypeState,
                amountSliderRangeState = amountSliderRangeState,
                rateTextState = rateTextState,
                rateSliderState = rateSliderState,
                selectedMonthsState = selectedMonthsState,
                customMonthsTextState = customMonthsTextState,
            )
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
                        suffix = { Text("تومان", color = AppMuted, fontSize = 13.sp) }, shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
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
                        suffix = { Text("درصد", color = AppMuted, fontSize = 13.sp) }, shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                    // فریمِ `69b` بندِ ۱: اسلایدر در حالتِ خواندنی **نمی‌آید**، نه این‌که
                    // بی‌اثر باشد. قبلاً رندر می‌شد و `onValueChange` با `if (!rateReadOnly)`
                    // بی‌صدا دورش می‌انداخت - کاربر دستگیره را می‌کشید و هیچ اتفاقی نمی‌افتاد.
                    // کنترلِ بی‌اثر از نبودنِ کنترل بدتر است.
                    if (!rateReadOnly) {
                        SlimSlider(
                            value = rateSlider,
                            onValueChange = { v ->
                                rateSlider = v
                                rateText = trimRate(v.toDouble())
                            },
                            valueRange = 0f..50f,
                            // پله‌ی ۰.۵ درصدی - رجوع کن به کامنتِ SlimSlider برای فرمولِ steps.
                            steps = 99,
                        )
                    }
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
                            onClick = { selectedMonths = v; customMonthsText = "" },
                        )
                    }
                }
                OutlinedTextField(
                    value = customMonthsText,
                    onValueChange = { formError = null; customMonthsText = cleanNum(it).take(3) },
                    placeholder = { Text("تعداد دلخواه") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    singleLine = true,
                    colors = appFieldColors(),
                    suffix = { Text("ماه", color = AppMuted, fontSize = 13.sp) }, shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
            }
        }

        // فریمِ `69b` بندِ ۴: «فاصله‌ی هر قسط» و «دوره‌ی تنفس» یک کارت شدند.
        //
        // هر دو پیش‌فرضِ درستی دارند (ماهانه، خاموش) و اکثرِ کاربران دستشان نمی‌زنند، پس
        // دو کارتِ هم‌وزنِ فیلدهای اصلی گرفتن جایی که تصمیمِ واقعی نیست.
        item {
            BankLoanAdvancedSection(
                intervalDaysState = intervalDaysState,
                graceOnState = graceOnState,
                graceMonthsState = graceMonthsState,
            )
        }

        item {
            BankLoanActionsSection(
                onCalculated = onCalculated,
                footer = footer,
                selectedBank = selectedBank,
                borrowerNameState = borrowerNameState,
                rateSourceState = rateSourceState,
                startYearState = startYearState,
                startMonthState = startMonthState,
                startDayState = startDayState,
                amountTextState = amountTextState,
                rateTextState = rateTextState,
                selectedMonthsState = selectedMonthsState,
                customMonthsTextState = customMonthsTextState,
                intervalDaysState = intervalDaysState,
                graceOnState = graceOnState,
                graceMonthsState = graceMonthsState,
                formErrorState = formErrorState,
            )
        }
    }

        if (showCalendarPicker) {
            Box(modifier = Modifier.fillMaxSize().background(AppBg)) {
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
            }
        }

        // «امور چک» رو کاربر می‌خواست تو همین تب هم در دسترس باشه، نه فقط از تنظیمات -
        // رجوع کن به کارتِ «امور چک» تهِ همین LazyColumn.
        if (showCheque) {
            Box(modifier = Modifier.fillMaxSize().background(AppBg)) {
                ChequeScreen(onBack = { showCheque = false })
            }
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
enum class RateSource(val label: String) {
    CREDIT_SERVICE("سرویسِ اعتباری"),
    BANK_LOAN("وامِ بانکی"),
}

/**
 * قرص‌های نوعِ وام تو حالتِ «وامِ بانکی» - **فقط فیلدِ نرخ رو پر می‌کنن** و نرخ قابلِ ویرایش می‌مونه.
 *
 * عمداً **محلیه نه سرور** (تاکیدِ صریحِ هندآف): سالی یه‌بار عوض می‌شن و آفلاین هم باید کار کنه.
 * `null` یعنی «سایر» - نرخ رو دست نمی‌زنه و به خودِ کاربر واگذار می‌کنه.
 */
internal val loanTypePresets: List<Pair<String, Double?>> = listOf(
    "ازدواج" to 4.0,
    "مسکن" to 18.0,
    "قرض‌الحسنه" to 0.0,
    "سایر" to null,
)

/** ذخیره/محاسبه ریال است و نمایش تومان (بندِ ۲ی README) - تنها نقطه‌ی تبدیلِ نمایشِ این فایل. */
@Composable
internal fun HeroResultCell(label: String, rial: Double, privacyMode: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(HeroPillBg)
            .padding(horizontal = 11.dp, vertical = 8.dp),
    ) {
        Text(label, color = HeroMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
        PrivacyCrossfade(privacyMode) { masked ->
            AutoShrinkText(
                text = "${maskIfPrivate(masked, amountToman(rial))} تومان",
                color = Color.White,
                maxFontSize = 13.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}


internal fun trimRate(v: Double): String {
    // نمایشِ حداکثر دو رقمِ اعشار (خواسته‌ی صریحِ کاربر، مورد ۱) - وگرنه v.toString() خامِ فلوتینگ-
    // پوینت می‌تونست چیزی مثلِ «23.500000001» نشون بده.
    return if (v == v.toLong().toDouble()) v.toLong().toString() else String.format(Locale.US, "%.2f", v)
}
