package ir.sadteam.loancalc.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.LoanCalculator
import ir.sadteam.loancalc.core.LoanMethod
import ir.sadteam.loancalc.core.PersianCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.cleanNumDecimal
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.ordinalFa
import ir.sadteam.loancalc.core.toFa
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.data.banks
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.auth.GateState
import ir.sadteam.loancalc.ui.components.StaggerIn
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.BankTile
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InlineJalaliDateRow
import ir.sadteam.loancalc.ui.components.LottieSpinner
import ir.sadteam.loancalc.ui.components.SlimSlider
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.amountSliderSteps
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.components.lazyColumnScrollbar
import ir.sadteam.loancalc.ui.history.CalculationHistoryViewModel
import ir.sadteam.loancalc.ui.myloans.MyLoansViewModel
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.Motion
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlinx.coroutines.delay
import java.util.Locale

private val faMonthNamesResult = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

@Composable
fun ResultScreen(
    outcome: BankLoanOutcome,
    // بعدِ ذخیره‌ی موفقِ وام، «ویرایش» دیگه معنی نداره (وام از قبل با همین اطلاعات ذخیره شده) -
    // این callback به‌جاش صدا زده می‌شه و باید فرم رو کاملاً خالی/ریست کنه (نه فقط نگه‌داشتنِ
    // مقادیرِ قبلی) تا کاربر بتونه بدونِ باقی‌موندنِ اعداد/بانکِ وامِ قبلی، وامِ بعدی رو وارد کنه -
    // رجوع کن به BankLoanTab تو MainActivity.kt (formStateHolder.removeState).
    onNewCalculation: () -> Unit = {},
    historyViewModel: CalculationHistoryViewModel = hiltViewModel(),
) {
    val privacyMode = LocalPrivacyMode.current

    // هر محاسبه‌ی وامی که تا نتیجه می‌رسه (نه فقط اونایی که کاربر صریحاً «ذخیره» می‌زنه) تو تاریخچه‌ی
    // محاسبات هم ثبت می‌شه - رجوع کن به ui/history/. فقط یه‌بار به‌ازای هر outcome *اصلی* (خودِ محاسبه‌ی
    // ورودی از فرم، نه هر تغییرِ زنده‌ی ویرایشِ درجا پایین‌تر) ثبت می‌شه.
    LaunchedEffect(outcome) {
        historyViewModel.log(
            kind = "LOAN",
            title = if (outcome.borrower != "—") "وام ${outcome.borrower} (${outcome.bankName})" else outcome.bankName,
            summary = "قسط ${fmt(outcome.result.installment)} ریال × ${toFa(outcome.n)} ماه، نرخ ${toFa(outcome.ratePct)}٪",
            amount = outcome.result.principal,
        )
    }

    // ویرایشِ زنده/درجا (مورد ۴): قبلاً «ویرایش» به فرمِ محاسبه‌گر برمی‌گشت (onEdit، حذف شد) - الان
    // خودِ همین صفحه با فیلدهای اینلاین ویرایش می‌شه و نتیجه با هر تغییر بلافاصله دوباره محاسبه/
    // انیمیت می‌شه (رجوع کن به liveOutcome پایین‌تر). فیلدها با remember(outcome) از رو
    // outcome*ِ ورودی* دوباره مقداردهی می‌شن - یعنی هر بار یه محاسبه‌ی *جدید* از فرم میاد (outcome
    // عوض می‌شه)، ویرایشِ قبلی خودکار پاک/بازنشانی می‌شه.
    var editMode by remember { mutableStateOf(false) }
    var editAmountText by remember(outcome) { mutableStateOf(outcome.result.originalPrincipal.toLong().toString()) }
    var editAmountSlider by remember(outcome) {
        mutableStateOf(outcome.result.originalPrincipal.toFloat().coerceIn(100_000_000f, 10_000_000_000f))
    }
    var editRateText by remember(outcome) { mutableStateOf(trimRateResult(outcome.ratePct)) }
    var editRateSlider by remember(outcome) { mutableStateOf(outcome.ratePct.toFloat().coerceIn(0f, 50f)) }
    var editNText by remember(outcome) { mutableStateOf(outcome.n.toString()) }
    var editStartYear by remember(outcome) { mutableStateOf(outcome.startDate.y) }
    var editStartMonth by remember(outcome) { mutableStateOf(outcome.startDate.m) }
    var editStartDay by remember(outcome) { mutableStateOf(outcome.startDate.d) }
    var editGraceOn by remember(outcome) { mutableStateOf(outcome.result.graceMonths > 0) }
    var editGraceMonths by remember(outcome) {
        mutableStateOf(if (outcome.result.graceMonths > 0) outcome.result.graceMonths.toFloat() else 6f)
    }
    var editBankName by remember(outcome) { mutableStateOf(outcome.bankName) }
    var editBorrower by remember(outcome) { mutableStateOf(outcome.borrower) }

    // نتیجه‌ی زنده - همیشه از رو فیلدهای بالا دوباره محاسبه می‌شه (چه editMode روشن باشه چه نه؛
    // وقتی خاموشه فیلدها همون مقادیرِ outcome*ِ اصلی*ان، پس نتیجه هم دقیقاً همونه). این یعنی هیچ‌جای
    // دیگه‌ی این کامپوزیبل دیگه مستقیم از رو `outcome` نمی‌خونه، همه‌جا از رو همین liveOutcome.
    val liveOutcome = remember(
        editAmountText, editRateText, editNText,
        editStartYear, editStartMonth, editStartDay,
        editGraceOn, editGraceMonths, editBankName, editBorrower,
    ) {
        val amount = cleanNum(editAmountText).toDoubleOrNull()?.takeIf { it > 0 } ?: outcome.result.originalPrincipal
        val rate = editRateText.toDoubleOrNull() ?: outcome.ratePct
        val n = editNText.toIntOrNull()?.coerceAtLeast(1) ?: outcome.n
        val method = if (rate <= 4.0) LoanMethod.QARZ else LoanMethod.STANDARD
        val grace = if (editGraceOn) editGraceMonths.toInt() else 0
        val startDate = PersianDate(editStartYear, editStartMonth, editStartDay)
        val result = LoanCalculator.compute(amount, rate, n, method, grace, outcome.result.intervalDays)
        outcome.copy(
            result = result,
            startDate = startDate,
            ratePct = rate,
            n = n,
            method = method,
            bankName = editBankName.ifBlank { outcome.bankName },
            borrower = editBorrower.ifBlank { "—" },
        )
    }
    val outcome = liveOutcome
    val result = outcome.result

    // اول گریس‌پیریود بعد فاصله‌ی هر قسط اضافه می‌شه. هم‌راستا با LoanRepository.getRows (که منبعِ
    // حقیقتِ سررسیدِ وام‌های ذخیره‌شده‌ست): فاصله‌های مضربِ ۳۰ ماهِ تقویمیِ واقعی جلو می‌رن (روزِ
    // ماه ثابت، نه ۳۰+روزِ ثابت که هر ماه یه روز عقب می‌رفت - باگِ گزارش‌شده‌ی کاربر)؛ فقط
    // هفتگی/دوهفته‌ای روزشمار می‌مونن. فرمولِ مالی (interval تو LoanCalculator) دست نخورده.
    val interval = result.intervalDays
    val dueDates = remember(result, outcome.startDate) {
        val base = if (result.graceMonths > 0) {
            PersianCalendar.addMonths(outcome.startDate, result.graceMonths)
        } else {
            outcome.startDate
        }
        result.rows.map { row ->
            // (row.month - 1): خواسته‌ی صریحِ کاربر - تاریخی که تو «تاریخ دریافت وام» می‌زنه خودش
            // مستقیم سررسیدِ قسطِ اول باشه (نه یه دوره جلوتر، که رفتارِ قبلی/بانکیِ استاندارد بود).
            // فرمولِ مالی (LoanCalculator) به تاریخ کاری نداره، این تغییر فقط رو نمایشِ تاریخ اثر داره.
            if (interval % 30 == 0) {
                PersianCalendar.addMonths(base, (row.month - 1) * (interval / 30))
            } else {
                PersianCalendar.addDays(base, (row.month - 1) * interval)
            }
        }
    }
    val endDate = dueDates.lastOrNull() ?: outcome.startDate

    val interestPct = if (result.principal > 0) result.totalInterest / result.principal * 100 else 0.0
    val feeAmount = if (outcome.method == LoanMethod.QARZ) result.totalInterest else 0.0

    // شمارش صعودی اعداد (پورت animateNumber وب) - رو Double تا برای مبالغ میلیاردی خطای گردکردن
    // Float (که تا چند صد ریال می‌رسید) پیش نیاد.
    var animatedInstallment by remember { mutableStateOf(0.0) }
    LaunchedEffect(result.installment) {
        animateValue(0.0, result.installment) { animatedInstallment = it }
    }
    var animatedTotal by remember { mutableStateOf(0.0) }
    LaunchedEffect(result.totalPaid) {
        animateValue(0.0, result.totalPaid) { animatedTotal = it }
    }
    // حلقه‌ی دونات با یه sweep از صفر «کشیده» می‌شه (حس پریمیوم‌تر از ظاهر شدن یهویی).
    val ringProgress = remember { Animatable(0f) }
    LaunchedEffect(result) {
        ringProgress.snapTo(0f)
        ringProgress.animateTo(1f, animationSpec = tween(900, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)))
    }

    // دکمه‌ی «ذخیره وام»: همون محدودیتِ «۱ وام رایگان» تب «وام‌های من» رو رعایت می‌کنه
    // (canSaveAnotherLoan = هیچ وامی ذخیره نشده، یا واردشده‌ی مشترک).
    val myLoansViewModel: MyLoansViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    val savedLoans by myLoansViewModel.loans.collectAsState()
    val gateState by authViewModel.gateState.collectAsState()
    val subscribed by authViewModel.subscribed.collectAsState()
    val canSaveAnotherLoan = savedLoans.isEmpty() || (gateState == GateState.LOGGED_IN && subscribed)
    var saved by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    // همون گاردِ AddManualLoanScreen: قبل از این‌که `saved=true` بشه (که پوشِ شبکه‌ای بعدِ ذخیره هم
    // توش هست)، دکمه هنوز دیده می‌شد و قابلِ تپِ دوباره بود - با تاخیرِ شبکه، چندبار زدن می‌تونست
    // چندتا وامِ تکراری بسازه.
    var saving by remember { mutableStateOf(false) }
    // برای وامی که تازه واقعاً وجود داره و کاربر داره از رو محاسبه‌گر واردش می‌کنه (نه یه وامِ
    // کاملاً جدید) - هم‌الگو با فیلدِ «تعداد اقساط پرداخت‌شده» تو فرمِ افزودنِ وامِ دستی.
    var paidCountText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp, 14.dp, 14.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (outcome.borrower != "—") "وام ${outcome.borrower}" else "نتیجه محاسبه",
                    color = AppText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
                if (saved) {
                    // بعدِ ذخیره‌ی موفق، به‌جای «ویرایش» (که مقادیرِ قبلی رو نگه می‌داشت) یه دکمه‌ی
                    // «محاسبه‌ی جدید» میاد که فرم رو کاملاً خالی می‌کنه.
                    TextButton(onClick = onNewCalculation) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = AppPrimary, modifier = Modifier.padding(end = 4.dp))
                        Text("محاسبه‌ی جدید", color = AppPrimary, fontSize = 13.sp)
                    }
                } else if (!editMode) {
                    // مورد ۴: قبلاً این دکمه برمی‌گشت به فرمِ محاسبه‌گر (صفحه‌ی قبل)؛ الان همینجا
                    // یه پنلِ ویرایشِ اینلاین باز می‌کنه - رجوع کن به liveOutcome بالاتر.
                    TextButton(onClick = { editMode = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = null, tint = AppPrimary, modifier = Modifier.padding(end = 4.dp))
                        Text("ویرایش", color = AppPrimary, fontSize = 13.sp)
                    }
                } else {
                    TextButton(onClick = { editMode = false }) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = AppPrimary, modifier = Modifier.padding(end = 4.dp))
                        Text("پایانِ ویرایش", color = AppPrimary, fontSize = 13.sp)
                    }
                }
            }
        }

        item {
            // پنلِ ویرایشِ اینلاین (مورد ۴) - با محو+گسترشِ نرم باز/بسته می‌شه؛ هر تغییرِ فیلد بلافاصله
            // liveOutcome رو دوباره حساب می‌کنه و کلِ نتیجه (دایره/اعداد/جدول) با همون انیمیشنِ
            // موجودشون (countUp، sweep حلقه) خودش رو به‌روز می‌کنه - نیازی به انیمیشنِ اضافه نیست.
            AnimatedVisibility(
                visible = editMode,
                enter = fadeIn(tween(Motion.FADE_IN_MS)) + expandVertically(Motion.standard()),
                exit = fadeOut(tween(Motion.FADE_OUT_MS)) + shrinkVertically(tween(200)),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                ) {
                    AppCard(label = "مبلغ وام") {
                        OutlinedTextField(
                            value = editAmountText,
                            onValueChange = { raw ->
                                val digits = cleanNum(raw)
                                editAmountText = digits
                                val n = digits.toLongOrNull() ?: 0L
                                if (n in 100_000_000L..10_000_000_000L) editAmountSlider = n.toFloat()
                            },
                            visualTransformation = ThousandsSeparatorTransformation(),
                            suffix = { Text("ریال", color = AppMuted, fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = appFieldColors(),
                        )
                        (editAmountText.toLongOrNull() ?: 0L).takeIf { it > 0 }?.let { r ->
                            Text(
                                "${numberToWordsFa((r / 10).toDouble())} تومان",
                                color = AppMuted,
                                fontSize = 11.5.sp,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        SlimSlider(
                            value = editAmountSlider,
                            onValueChange = { v ->
                                editAmountSlider = v
                                editAmountText = v.toLong().toString()
                            },
                            valueRange = 100_000_000f..10_000_000_000f,
                            steps = amountSliderSteps(100_000_000f..10_000_000_000f),
                        )
                    }
                    AppCard(label = "نرخ سود سالانه") {
                        OutlinedTextField(
                            value = editRateText,
                            onValueChange = { raw ->
                                val filtered = cleanNumDecimal(raw)
                                editRateText = filtered
                                val num = filtered.toDoubleOrNull()
                                if (num != null && num in 0.0..50.0) editRateSlider = num.toFloat()
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = appFieldColors(),
                            suffix = { Text("درصد", color = AppMuted, fontSize = 13.sp) },
                        )
                        SlimSlider(
                            value = editRateSlider,
                            onValueChange = { v ->
                                editRateSlider = v
                                editRateText = trimRateResult(v.toDouble())
                            },
                            valueRange = 0f..50f,
                            steps = 99,
                        )
                    }
                    AppCard(label = "تعداد اقساط (ماه)") {
                        OutlinedTextField(
                            value = editNText,
                            onValueChange = { editNText = cleanNum(it) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = appFieldColors(),
                        )
                    }
                    AppCard(
                        label = if (editGraceOn && editGraceMonths.toInt() > 0) {
                            "تاریخ دریافت وام (قسطِ اول ${toFa(editGraceMonths.toInt())} ماه بعد، به‌خاطرِ دوره‌ی تنفس)"
                        } else {
                            "تاریخ دریافت وام (سررسیدِ قسطِ اول)"
                        },
                    ) {
                        InlineJalaliDateRow(
                            year = editStartYear,
                            month = editStartMonth,
                            day = editStartDay,
                            onDateChange = { y, m, d -> editStartYear = y; editStartMonth = m; editStartDay = d },
                        )
                    }
                    AppCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("دوره تنفس", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Switch(
                                checked = editGraceOn,
                                onCheckedChange = { editGraceOn = it },
                                colors = SwitchDefaults.colors(checkedTrackColor = AppPrimaryDim, checkedThumbColor = AppPrimary),
                            )
                        }
                        if (editGraceOn) {
                            SlimSlider(
                                value = editGraceMonths,
                                onValueChange = { editGraceMonths = it },
                                valueRange = 1f..24f,
                                steps = 22,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                            Text(
                                "${toFa(editGraceMonths.toInt())} ماه",
                                fontSize = 12.5.sp,
                                color = AppMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    AppCard(label = "اسم بانک یا فروشنده") {
                        OutlinedTextField(
                            value = editBankName,
                            onValueChange = { editBankName = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = appFieldColors(),
                        )
                        // انتخابِ لوگو - همون الگویی که رو دیالوگِ ویرایشِ مشخصاتِ LoanDetailScreen
                        // پیاده شده؛ لمسِ یه لوگو اسمِ دقیقش رو تو فیلدِ بالا می‌ذاره، فیلد همچنان
                        // برای بانک/فروشنده‌ی خارج از لیست دستی باز می‌مونه.
                        LazyRow(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            items(banks, key = { it.name }) { b ->
                                BankTile(
                                    bank = b,
                                    selected = editBankName == b.name,
                                    onClick = { editBankName = b.name },
                                )
                            }
                        }
                    }
                    AppCard(label = "وام‌گیرنده (اختیاری)") {
                        OutlinedTextField(
                            value = editBorrower.let { if (it == "—") "" else it },
                            onValueChange = { editBorrower = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = appFieldColors(),
                        )
                    }
                }
            }
        }

        item {
            StaggerIn(0) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    LoanRing(
                        principal = result.principal,
                        interest = result.totalInterest,
                        progress = ringProgress.value,
                        modifier = Modifier.size(180.dp),
                    )
                    MoneyParticleBurst(trigger = result, modifier = Modifier.size(220.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PrivacyCrossfade(privacyMode) { masked ->
                            Text(maskIfPrivate(masked, fmt(animatedInstallment)), color = AppText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Text("قسط ماهانه (ریال)", fontSize = 12.5.sp, color = AppMuted)
                    }
                }
            }
        }

        item {
            StaggerIn(1) {
                Text(
                    text = "${numberToWordsFa(result.installment / 10)} تومان",
                    color = AppMuted,
                    fontSize = 13.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                )
            }
        }

        item {
            // این صفحه بعدِ ذخیره جایی نمی‌ره (برخلافِ فرمِ افزودنِ دستی/چک که می‌بندن) - همینجا
            // دکمه با این متنِ تاییدی عوض می‌شه. قبلاً این تعویض یهویی بود؛ الان با AnimatedVisibility
            // یه ورودِ فنریِ کوچیک (بزرگ‌شدن از ۰.۸ + محو) داره.
            AnimatedVisibility(
                visible = saved,
                enter = fadeIn(tween(Motion.FADE_IN_MS)) + scaleIn(animationSpec = Motion.snappy(), initialScale = 0.8f),
            ) {
                Text(
                    "✓ وام تو «وام‌های من» ذخیره شد",
                    color = AppPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                )
            }
            if (!saved) {
                AppCard(label = "تعداد اقساط پرداخت‌شده (اختیاری)", modifier = Modifier.padding(bottom = 10.dp)) {
                    Text(
                        "اگه این وام از قبل هست و چندتا قسطش رو پرداخت کردی، اینجا بنویس",
                        color = AppMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    OutlinedTextField(
                        value = paidCountText,
                        onValueChange = { paidCountText = cleanNum(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = appFieldColors(),
                    )
                }
                GradientButton(
                    enabled = !saving,
                    onClick = {
                        if (saving) return@GradientButton
                        val paidCount = (paidCountText.toIntOrNull() ?: 0).coerceIn(0, outcome.n)
                        when {
                            canSaveAnotherLoan -> {
                                saving = true
                                myLoansViewModel.saveComputedLoan(outcome, paidCount) {
                                    saving = false
                                    saved = true
                                    saveMessage = null
                                }
                            }
                            gateState == null -> Unit
                            gateState != GateState.LOGGED_IN ->
                                saveMessage = "برای ذخیره‌ی وام دوم اول باید وارد بشی — از تب «وام‌های من» وارد شو"
                            else ->
                                saveMessage = "برای ذخیره‌ی بیش از یک وام باید اشتراک بگیری — از تب «وام‌های من»"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                ) {
                    if (saving) {
                        LottieSpinner(modifier = Modifier.size(18.dp))
                    } else {
                        Text("ذخیره وام", fontWeight = FontWeight.Bold)
                    }
                }
                if (saveMessage != null) {
                    Text(
                        saveMessage!!,
                        color = AppDanger,
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    )
                }
            }
        }

        item {
            StaggerIn(2) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PrivacyCrossfade(privacyMode, modifier = Modifier.weight(1.3f)) { masked ->
                        StatBox("کل بازپرداخت (ریال)", maskIfPrivate(masked, fmt(animatedTotal)))
                    }
                    StatBox("سررسید هر ماه", ordinalFa(outcome.startDate.d), Modifier.weight(1f))
                    StatBox("مدت وام", "${toFa(outcome.n)} ماه", Modifier.weight(1f))
                }
            }
        }

        item {
            StaggerIn(3) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val pctText = if (interestPct < 10) {
                        toFa(String.format("%.1f", interestPct))
                    } else {
                        toFa(interestPct.roundToLong().toString())
                    }
                    StatBox("سود نسبت به اصل وام", "$pctText٪", Modifier.weight(1f))
                    StatBox(
                        "تاریخ پایان وام",
                        "${toFa(endDate.d)} ${faMonthNamesResult[endDate.m - 1]} ${toFa(endDate.y)}",
                        Modifier.weight(1f),
                    )
                }
            }
        }

        if (feeAmount > 0) {
            item {
                val rateLabel = if (outcome.ratePct == outcome.ratePct.toLong().toDouble()) {
                    outcome.ratePct.toLong().toString()
                } else {
                    outcome.ratePct.toString()
                }
                PrivacyCrossfade(privacyMode, modifier = Modifier.fillMaxWidth()) { masked ->
                    StatBox(
                        "کارمزد سالانه (قرض‌الحسنه)",
                        "${maskIfPrivate(masked, fmt(feeAmount))} ریال (${toFa(rateLabel)}٪ سالانه)",
                    )
                }
            }
        }

        item {
            StaggerIn(4) {
            AppCard(label = "جدول کامل اقساط") {
                // حداکثر ۵ قسط تو صفحه جا می‌شه، بقیه با اسکرول - کنارش یه اسکرول‌بار سبز نشون می‌ده
                // چقدر پایین رفتیم (خواسته‌ی کاربر).
                val tableState = rememberLazyListState()
                val rowH = 48.dp
                val visibleRows = minOf(result.rows.size, 5)
                LazyColumn(
                    state = tableState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowH * visibleRows)
                        .lazyColumnScrollbar(tableState, AppPrimary),
                    // start=10.dp (نه end) چون RTLه - رجوع کن به همین رفع رو LoanDetailScreen: start
                    // تو RTL یعنی سمتِ راست، دقیقاً همونجایی که اسکرول‌بار (رسمِ raw canvas، مستقل از
                    // جهت) کشیده می‌شه؛ بدونش متنِ «قسط N» زیرِ اسکرول‌بار می‌رفت.
                    contentPadding = PaddingValues(start = 10.dp),
                ) {
                    itemsIndexed(result.rows, key = { _, row -> row.month }) { idx, row ->
                        val due = dueDates[idx]
                        val dateLabel = if (interval >= 28) {
                            "${faMonthNamesResult[due.m - 1]} ${toFa(due.y)}"
                        } else {
                            "${toFa(due.d)} ${faMonthNamesResult[due.m - 1]}"
                        }
                        Column(Modifier.fillMaxWidth().height(rowH)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("قسط ${toFa(row.month)}", fontSize = 12.sp)
                                Text(dateLabel, fontSize = 12.sp)
                                PrivacyCrossfade(privacyMode) { masked ->
                                    Text("${maskIfPrivate(masked, fmt(row.installment))} ریال", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (idx != result.rows.lastIndex) HorizontalDivider(color = AppLine)
                        }
                    }
                }
            }
            }
        }
    }
}

/** یه پاشیدنِ کوتاهِ «سکه‌های طلایی» از مرکزِ حلقه‌ی نتیجه به بیرون - خواسته‌ی «انیمیشن‌های سفارشی»،
 * حسِ جشن‌گرفتنِ لحظه‌ای که نتیجه‌ی محاسبه آماده می‌شه. [trigger] هر بار عوض بشه (یعنی محاسبه‌ی
 * جدید) یه دور کامل پخش می‌شه؛ صرفاً تزئینیه و روی هیچ داده‌ای اثر نمی‌ذاره. */
@Composable
private fun MoneyParticleBurst(trigger: Any, modifier: Modifier = Modifier) {
    val progress = remember(trigger) { Animatable(0f) }
    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(900, easing = LinearOutSlowInEasing))
    }
    val particleCount = 10
    Canvas(modifier = modifier) {
        val p = progress.value
        if (p <= 0f) return@Canvas
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.minDimension / 2f
        val alpha = (1f - p).coerceIn(0f, 1f)
        if (alpha <= 0f) return@Canvas
        repeat(particleCount) { i ->
            val angle = 2 * PI * i / particleCount + PI / particleCount
            val dist = maxRadius * p
            val x = center.x + (dist * cos(angle)).toFloat()
            val y = center.y + (dist * sin(angle)).toFloat()
            val radius = 3.5.dp.toPx() * (1f - p * 0.35f)
            drawCircle(
                color = androidx.compose.ui.graphics.Color(0xFFD4AF37).copy(alpha = alpha),
                radius = radius,
                center = Offset(x, y),
            )
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
            Text(label, fontSize = 9.sp, color = AppMuted, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 3.dp))
        }
    }
}

@Composable
private fun LoanRing(principal: Double, interest: Double, progress: Float, modifier: Modifier = Modifier) {
    val total = principal + interest
    val principalFrac = if (total > 0) (principal / total).toFloat() else 0f
    // drawArc اجرا می‌شه تو DrawScope که @Composable نیست - رنگ‌های تم (که حالا @Composable
    // get() هستن، برای پشتیبانی از تم روشن) باید همینجا تو بدنه‌ی @Composable گرفته بشن، نه
    // مستقیم تو بلوک Canvas.
    val trackColor = AppSurface2
    val primaryColor = AppPrimary
    val accentColor = AppAccent
    Canvas(modifier = modifier.aspectRatio(1f)) {
        val strokeWidth = size.minDimension * 0.1f
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
        // هر دو کمان با [progress] از صفر «کشیده» می‌شن (انیمیشن sweep ورود به صفحه).
        drawArc(
            color = primaryColor,
            startAngle = -90f,
            sweepAngle = 360f * principalFrac * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
        drawArc(
            color = accentColor,
            startAngle = -90f + 360f * principalFrac * progress,
            sweepAngle = 360f * (1f - principalFrac) * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }
}

/** نمایشِ حداکثر دو رقمِ اعشار - هم‌الگو با trimRate تو BankLoanScreen.kt (خصوصیِ همون فایله، برای
 * همین نسخه‌ی جداگانه‌ی خودِ این فایل). */
private fun trimRateResult(v: Double): String {
    return if (v == v.toLong().toDouble()) v.toLong().toString() else String.format(Locale.US, "%.2f", v)
}

private suspend fun animateValue(from: Double, to: Double, durationMs: Long = 500, onUpdate: (Double) -> Unit) {
    val start = System.currentTimeMillis()
    while (true) {
        val elapsed = System.currentTimeMillis() - start
        val p = (elapsed.toDouble() / durationMs).coerceIn(0.0, 1.0)
        val eased = 1.0 - (1.0 - p) * (1.0 - p) * (1.0 - p)
        onUpdate(from + (to - from) * eased)
        if (p >= 1.0) break
        delay(16)
    }
}
