package ir.sadteam.loancalc.ui.accounting

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.minimumInteractiveComponentSize
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.components.AppButtonVariant
import ir.sadteam.loancalc.ui.components.GradientButton
import androidx.compose.animation.AnimatedVisibility
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.theme.AppDueNextBorder
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import ir.sadteam.loancalc.ui.components.HeroChart
import ir.sadteam.loancalc.ui.components.HeroChartStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import androidx.compose.foundation.layout.defaultMinSize
import ir.sadteam.loancalc.core.ChequeStatus
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.InteractiveBars
import ir.sadteam.loancalc.ui.components.drawHeroLeaves
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.jibak.toFaMoney
import ir.sadteam.loancalc.ui.theme.AppPurpleInk
import ir.sadteam.loancalc.ui.theme.AppSpacing
import ir.sadteam.loancalc.ui.theme.AppPurplePill
import ir.sadteam.loancalc.ui.cheque.ChequeViewModel
import ir.sadteam.loancalc.ui.myloans.MyLoansViewModel
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.jibak.rialToFaCompactParts
import ir.sadteam.loancalc.ui.jibak.toFa
import ir.sadteam.loancalc.ui.components.CategoryDonut
import ir.sadteam.loancalc.ui.components.DonutSlice
import ir.sadteam.loancalc.ui.components.dashedBorder
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.PrivacyModeViewModel
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppAssetBorder
import ir.sadteam.loancalc.ui.theme.AppAssetInk
import ir.sadteam.loancalc.ui.theme.AppChartGrid
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppGoldInkSoft
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMarkOff
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppPurple
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppUrgentBorder
import ir.sadteam.loancalc.ui.theme.AppUrgentShadow
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import ir.sadteam.loancalc.ui.theme.AppWarningPill
import ir.sadteam.loancalc.ui.theme.hardShadow
import ir.sadteam.loancalc.ui.components.AppHeroCard

/**
 * تبِ **گزارش** - بازسازیِ کاملِ فریمِ `26a`.
 *
 * ```
 * ۱ عنوان + تاگلِ ماه/فصل/سال + کلیدِ خصوصی
 * ۲ هیرویِ بنفش: خرجِ دوره + نمودارِ ۷ ماهه
 * ۳ ثابت و متغیر - نوارِ راه‌راهِ قرمز/سبز
 * ۴ دوناتِ دسته‌ها
 * ۵ کارتِ کشف: اشتراک‌های تکراری
 * ۶ کارتِ کشف: دسته‌ی پرخرج نسبت به میانگینِ سه ماه
 * ۷ خروجیِ اکسل و PDF
 * ```
 *
 * ⚠️ **کارتِ کشفِ دوم** طبقِ `design/ANSWERS-section-37.md` بندِ ۵ بازنویسی شد. متنِ اولیه
 * («نان ۲۲٪ گران‌تر شد») با داده‌ی ما درنمی‌اومد چون اپ اسمِ قلم/فروشنده رو ذخیره نمی‌کنه.
 * قاعده‌ی جایگزینِ خودِ طراح: **دسته‌ای که جمعِ ماهِ جاری‌اش دستِ‌کم ۱۵٪ از میانگینِ سه ماهِ
 * کاملِ گذشته بیشتر باشه، با شرطِ حداقل پنج تراکنش در هر پنجره. بزرگ‌ترین انحراف، یکی در ماه.**
 */
@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
fun ReportTabScreen(
    onOpenExport: () -> Unit = {},
    onOpenLoanStats: () -> Unit = {},
    onOpenChequeReport: () -> Unit = {},
    accountViewModel: AccountViewModel = hiltViewModel(),
    privacyViewModel: PrivacyModeViewModel = hiltViewModel(),
    discoveryDismissViewModel: DiscoveryDismissViewModel = hiltViewModel(),
    loansViewModel: MyLoansViewModel = hiltViewModel(),
    chequeViewModel: ChequeViewModel = hiltViewModel(),
) {
    val isPremium = ir.sadteam.loancalc.ui.subscription.LocalIsPremium.current
    androidx.compose.runtime.LaunchedEffect(Unit) {
        ir.sadteam.loancalc.data.UsageStats.track(ir.sadteam.loancalc.data.UsageStats.REPORT_VIEWED)
    }
    val transactions by accountViewModel.transactions.collectAsState()
    val recurring by accountViewModel.recurringPayments.collectAsState()
    val privacyMode = LocalPrivacyMode.current
    val today = remember { JalaliCalendar.today() }

    // دو عددِ ردیف‌های تعهد (بخشِ ۸۱). هیچ‌کدام تازه نیستند: مبلغ همان عددِ کارتِ طلاییِ تبِ
    // وام است و شمارش همان چکِ در انتظارِ تبِ چک.
    // ⚠️ `totalCurrentInstallment` **suspend** است، پس از `LaunchedEffect` صدا زده می‌شود نه
    // از `remember{}` - قاعده‌ی ماندگارِ پروژه.
    val loans by loansViewModel.loans.collectAsState()
    val cheques by chequeViewModel.cheques.collectAsState()
    var monthlyInstallmentRial by remember { mutableStateOf(0.0) }
    LaunchedEffect(loans) { monthlyInstallmentRial = loansViewModel.totalCurrentInstallment(loans) }
    val chequesThisMonth = remember(cheques, today) {
        cheques.count {
            !it.archived &&
                it.status == ChequeStatus.PENDING.name &&
                it.dueYear == today.y &&
                it.dueMonth == today.m
        }
    }
    var period by remember { mutableStateOf(ReportPeriod.MONTH) }

    val stats = remember(transactions, recurring, period) {
        buildReportStats(transactions, recurring, today, period)
    }
    // دو عددِ حالتِ «هیچ کشفی نیست». همان فیلترِ `buildReportStats`: انتقالِ بینِ حساب‌ها
    // خرج نیست.
    val realExpenses = remember(transactions) {
        transactions.filter { it.sourceType !in ir.sadteam.loancalc.data.NON_SPENDING_SOURCES && it.type == "WITHDRAWAL" }
    }
    val checkedTxCount = remember(realExpenses, today) {
        realExpenses.count { it.year == today.y && it.month == today.m }
    }
    val monthsOfHistory = remember(realExpenses) {
        realExpenses.map { it.year to it.month }.distinct().size
    }
    var showNewTransaction by remember { mutableStateOf(false) }
    var showSubscriptionFinder by remember { mutableStateOf(false) }

    // خروجیِ همین ماه (بسته‌ی ChatGPT، ۷ مهر): دو دکمه‌ی جدا به‌جای ردیفی که به جایی وصل نبود.
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val banner = rememberInAppBanner()
    val monthTx = remember(transactions, today) {
        transactions.filter { it.year == today.y && it.month == today.m }.sortedWith(compareBy({ it.day }, { it.id }))
    }
    // فهرستِ خروجی همه‌ی تراکنش‌هاست؛ جمعِ درآمد/خرج بی جابه‌جایی و خرید/فروشِ دارایی.
    val monthReal = monthTx.filter { it.sourceType !in ir.sadteam.loancalc.data.NON_SPENDING_SOURCES }
    val monthIncome = monthReal.filter { it.type == "DEPOSIT" }.sumOf { it.amount }
    val monthExpense = monthReal.filter { it.type == "WITHDRAWAL" }.sumOf { it.amount }
    val monthBreakdown = monthReal.groupBy { it.category ?: "بدونِ دسته" }
        .map { (name, txs) -> name to txs.sumOf { it.amount } }
        .sortedByDescending { it.second }
    val monthRangeLabel = "${persianMonthName(today.m)} ${today.y.toFa()}"
    fun runExport(uri: android.net.Uri?, pdf: Boolean) {
        if (uri == null) return
        scope.launch(Dispatchers.IO) {
            val ok = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    if (pdf) AccountingPdfExporter.export(monthRangeLabel, "همه‌ی حساب‌ها", monthIncome, monthExpense, monthBreakdown, monthTx, out)
                    else AccountingXlsxExporter.export(monthIncome, monthExpense, monthBreakdown, monthTx, out)
                }
            }.isSuccess
            withContext(Dispatchers.Main) {
                banner.show(
                    if (ok) (if (pdf) "PDF ذخیره شد" else "اکسل ذخیره شد") else "ذخیره‌ی فایل ناموفق بود",
                    isSuccess = ok,
                )
            }
        }
    }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { runExport(it, true) }
    val xlsxLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    ) { runExport(it, false) }

    // کلیدِ نادیده‌گرفتن = «نوع + ماهِ شمسی». **خاموشیِ دائمی نه**: کشفی که برای همیشه
    // خاموش می‌شود یعنی باگی که هیچ‌وقت گزارش نمی‌شود. ماهِ بعد دوباره می‌آید.
    // ماندگاری از `UiPrefs.dismissedDiscoveries` می‌آید - رجوع کن به [DiscoveryDismissViewModel].
    val dismissed by discoveryDismissViewModel.dismissed.collectAsState()
    val simpleReport = ir.sadteam.loancalc.ui.privacy.LocalSimpleMode.current
    val ignoredSubs by hiltViewModel<ir.sadteam.loancalc.ui.settings.SmsAutoImportViewModel>().ignoredSubscriptions.collectAsState()
    val monthKey = "${today.y}-${today.m}"

    // ⚠️ زیرصفحه‌ها **روی** تب می‌نشینند، نه به‌جایش. قبلاً با `return` صدا زده می‌شدند و
    // کلِ LazyColumn از کامپوزیشن بیرون می‌رفت: پشتِ شیت سفیدِ خالی بود و اسکرولِ گزارش با
    // هر بستنِ اشتراک‌یاب صفر می‌شد. همان باگی که در خانه و تبِ دارایی رفع شد.
    BackHandler(enabled = showNewTransaction || showSubscriptionFinder) {
        if (showNewTransaction) showNewTransaction = false else showSubscriptionFinder = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
    // کشیدن به پایین = تازه‌سازی (خواسته‌ی کاربر ۱۳ مهر؛ داده‌ها خودشان زنده‌اند، این اشتراک/سرور را هم می‌گیرد).
    val reportAuthVm: ir.sadteam.loancalc.ui.auth.AuthViewModel = hiltViewModel()
    var reportRefreshing by remember { mutableStateOf(false) }
    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = reportRefreshing,
        onRefresh = { reportRefreshing = true; reportAuthVm.refreshStatus { reportRefreshing = false } },
        modifier = Modifier.fillMaxSize(),
    ) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 14.dp, 16.dp, 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ReportHeader(
                period = period,
                onPeriod = {
                    if (isPremium || it == ReportPeriod.MONTH) period = it
                    else ir.sadteam.loancalc.ui.subscription.PremiumPaywall.ask("report_period", "گزارشِ فصل و سال")
                },
                privacyMode = privacyMode,
                onTogglePrivacy = { privacyViewModel.toggle() },
                showControls = transactions.isNotEmpty(),
                // خروجی از کارتِ بزرگِ تهِ صفحه به آیکونِ کنارِ چشم آمد (۱۴ مهر). فقط برای مشترک، مثلِ قبل.
                onExcel = if (isPremium) ({ xlsxLauncher.launch("jibak-${today.y}-${today.m}.xlsx") }) else null,
                onPdf = { pdfLauncher.launch("jibak-${today.y}-${today.m}.pdf") },
            )
        }
        // هیچ تراکنشی ثبت نشده → **فریمِ `21c`**: کارتِ خط‌چینِ نمودارِ خالی، یادآوریِ پیامکِ
        // بانکی، و لیستِ «وقتی داده داشته باشی اینها را می‌بینی». هیرویِ بنفشِ صفر نشون داده نمی‌شه.
        if (transactions.isEmpty()) {
            item { NoChartCard(onAddTransaction = { showNewTransaction = true }) }
            item { BankSmsHintCard() }
            item { ComingSoonCard() }
            return@LazyColumn
        }
        item {
            PeriodSpendHero(
                label = stats.periodLabel,
                spend = stats.periodSpend,
                deltaPercent = stats.deltaPercent,
                bars = stats.monthlyBars,
                firstLabel = stats.firstBarLabel,
                lastLabel = stats.lastBarLabel,
                privacyMode = privacyMode,
                // در نمای ماه، میله‌ی سفید **امروز** است نه آخرین روزِ ماه.
                currentBarIndex = if (period == ReportPeriod.MONTH) today.d - 1 else stats.monthlyBars.lastIndex,
                barLabels = stats.barLabels,
            )
        }
        // بی اشتراک: فقط خلاصه (خرج/درآمد/تعداد)؛ بقیه‌ی گزارش پشتِ یک کارتِ اشتراک.
        if (!isPremium) {
            item {
                PeriodStatRow(
                    income = stats.periodIncome,
                    incomeChangePercent = stats.incomeDeltaPercent,
                    spend = stats.periodSpend,
                    spendChangePercent = stats.deltaPercent,
                    count = stats.transactionCount,
                    countDelta = stats.transactionCountDelta,
                    privacyMode = privacyMode,
                )
            }
            item {
                ir.sadteam.loancalc.ui.components.AppCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("گزارشِ کامل با اشتراک", fontWeight = FontWeight.Black, fontSize = 15.sp)
                        Text(
                            "سهمِ هر دسته، خرجِ ثابت و متغیر، مقایسه‌ی ماه‌ها، ماه‌ها و سال‌های قبل و خروجیِ PDF و اکسل.",
                            color = AppMuted, fontSize = 12.5.sp, lineHeight = 22.sp,
                        )
                        ir.sadteam.loancalc.ui.components.GradientButton(
                            onClick = { ir.sadteam.loancalc.ui.subscription.PremiumPaywall.showPlans = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("دیدنِ اشتراک‌ها") }
                    }
                }
            }
            return@LazyColumn
        }
        if (stats.fixedShare != null) {
            // ردیفِ سه‌تاییِ آمارِ دوره (طرحِ مرجعِ کاربر). زیرِ هیرو می‌نشیند چون هیرو
            // فقط **خرج** را می‌گوید و این سه، همان یک عدد را در جا می‌گذارد: چقدر آمد،
            // چقدر رفت، در چند تراکنش.
            item {
                PeriodStatRow(
                    income = stats.periodIncome,
                    incomeChangePercent = stats.incomeDeltaPercent,
                    spend = stats.periodSpend,
                    spendChangePercent = stats.deltaPercent,
                    count = stats.transactionCount,
                    countDelta = stats.transactionCountDelta,
                    privacyMode = privacyMode,
                )
            }
            item {
                FixedVsFreeCard(
                    fixedShare = stats.fixedShare,
                    fixedAmount = stats.fixedAmount,
                    freeAmount = stats.freeAmount,
                    privacyMode = privacyMode,
                )
            }
        }
        if (stats.byCategory.isNotEmpty()) {
            item {
                CategoryDonutCard(
                    byCategory = stats.byCategory,
                    total = stats.periodSpend,
                    periodLabel = stats.periodLabel,
                    privacyMode = privacyMode,
                    monthlyInstallmentRial = monthlyInstallmentRial,
                    chequesThisMonth = chequesThisMonth,
                    onOpenLoanStats = onOpenLoanStats,
                    onOpenChequeReport = onOpenChequeReport,
                )
            }
            item {
                CommitmentRows(
                    monthlyInstallmentRial = monthlyInstallmentRial,
                    chequesThisMonth = chequesThisMonth,
                    privacyMode = privacyMode,
                    onOpenLoanStats = onOpenLoanStats,
                    onOpenChequeReport = onOpenChequeReport,
                )
            }
        }
        // حالتِ ساده: فقط خلاصه + نمودار + دسته‌ها؛ کشف/مقایسه/برچسب/خروجی نه.
        if (simpleReport) return@LazyColumn
        // ── کارت‌های کشف ────────────────────────────────────────────────────
        // ترتیب **بر پایه‌ی فوریت**، نه ترتیبِ نوشته‌شدن در فایل: کسری اول، پرداختِ دوباره
        // دوم، اطلاعاتی سوم. قبلاً اشتراک‌یاب همیشه بالای «۳۰٪ بیشتر از معمول» می‌نشست.
        val discoveries = buildList {
            stats.overspentCategory?.let { over ->
                fun catSpend(y: Int, m: Int) = realExpenses.filter { it.year == y && it.month == m && (it.category ?: "") == over.name }.sumOf { it.amount }
                var py = today.y
                var pm = today.m
                val prevThree = (1..3).map { pm -= 1; if (pm == 0) { pm = 12; py -= 1 }; catSpend(py, pm) }
                val nowSpend = catSpend(today.y, today.m)
                val avgSpend = prevThree.average()
                add(
                    Discovery("overspent", 1) {
                        // «ببین چرا»: همین ماه در برابرِ میانگینِ سه ماهِ قبلِ همان دسته، درجا.
                        DiscoveryCard(
                            icon = Icons.Filled.BarChart,
                            title = "${over.name} ${(over.percent).toFa()}٪ بیشتر از معمول",
                            subtitle = "نسبت به میانگینِ سه ماه",
                            bg = DiscoverDangerBg,
                            border = DiscoverDangerBorder,
                            pill = DiscoverDangerPill,
                            ink = AppText,
                            subInk = AppMuted,
                            iconInk = AppDanger,
                            bigValue = "${(over.percent).toFa()}٪ بیشتر",
                            bigInk = AppDangerInk,
                            whyText = "این ماه ${maskIfPrivate(privacyMode, nowSpend.rialToFaCompact())} تومان · " +
                                "میانگینِ سه ماهِ قبل ${maskIfPrivate(privacyMode, avgSpend.rialToFaCompact())} تومان",
                            onDismiss = { discoveryDismissViewModel.dismiss("overspent@$monthKey", monthKey) },
                        )
                    },
                )
            }
            // ⚠️ **اشتراک‌یاب** با کارتِ بعدی فرق دارد: آن پرداخت‌های تکراریِ **اعلام‌شده‌ی
            // خودِ کاربر** است، این چیزی است که اپ از روی تاریخچه **کشف** کرده و کاربر خبر
            // نداشته. تنها کارتِ کشفی که با تپ صفحه باز می‌کند.
            val liveSubs = stats.detectedSubscriptions.filter { it.label !in ignoredSubs }
            if (liveSubs.isNotEmpty()) {
                add(
                    Discovery("subscriptions", 2) {
                        DiscoveryCard(
                            icon = Icons.Filled.Autorenew,
                            title = "${(liveSubs.size).toFa()} خرجِ تکرارشونده پیدا شد",
                            subtitle = "ماهی ${maskIfPrivate(privacyMode, (liveSubs.sumOf { it.typicalAmountRial }).rialToFaCompact())} تومان — لمس کن ببین چی‌ان",
                            bg = DiscoverWarnBg,
                            border = DiscoverWarnBorder,
                            pill = DiscoverWarnPill,
                            ink = DiscoverWarnInk,
                            subInk = DiscoverWarnSubInk,
                            iconInk = DiscoverWarnIconInk,
                            // دیده‌شده = بسته؛ تا ماهِ بعد برنگردد (گزارشِ کاربر ۱۳ مهر).
                            onClick = { showSubscriptionFinder = true; discoveryDismissViewModel.dismiss("subscriptions@$monthKey", monthKey) },
                            onDismiss = { discoveryDismissViewModel.dismiss("subscriptions@$monthKey", monthKey) },
                        )
                    },
                )
            }
            if (stats.recurringCount > 0) {
                add(
                    // این کارت و اشتراک‌یاب قبلاً هم‌رنگ، هم‌آیکون و هم‌جمله بودند و
                    // پشتِ‌هم می‌نشستند. این یکی اعلامِ خودِ کاربر است نه کشفِ برنامه، پس
                    // سطحِ خنثی می‌گیرد و آخرین اولویت را دارد - خبر نیست، یادآوری است.
                    Discovery("recurring", 3) {
                        DiscoveryCard(
                            icon = Icons.Filled.EventRepeat,
                            title = "${(stats.recurringCount).toFa()} پرداختِ تکراریِ ثبت‌شده",
                            subtitle = "ماهی ${maskIfPrivate(privacyMode, (stats.recurringMonthly).rialToFaCompact())} تومان",
                            bg = AppSurface,
                            border = AppLineRow,
                            pill = AppIconFrame,
                            ink = AppText,
                            subInk = AppMuted,
                            iconInk = AppMuted,
                            onDismiss = { discoveryDismissViewModel.dismiss("recurring@$monthKey", monthKey) },
                        )
                    },
                )
            }
        }
        val visibleDiscoveries = discoveries
            .filterNot { "${it.kind}@$monthKey" in dismissed }
            .sortedBy { it.priority }
            .take(DISCOVERY_LIMIT)

        if (visibleDiscoveries.isEmpty()) {
            // «هیچ کشفی نیست» با **دو عددِ واقعی**، نه جمله‌ی تشویقی: بی عدد، کاربر فکر
            // می‌کند بخشِ کشف خراب است. عددها می‌گویند چه چیزی بررسی شد و چه چیزی کم است.
            item { NoDiscoveryCard(checkedCount = checkedTxCount, monthsOfHistory = monthsOfHistory) }
        } else {
            visibleDiscoveries.forEach { d -> item(key = d.kind) { d.render() } }
        }
        // ── مقایسه‌ی ماه‌به‌ماه، برچسب‌ها، بازپرداختی‌ها (۶ مهر، از مقایسه با پارمیس/پولکس) ──
        item { MonthCompareCard(transactions, privacyMode) }
        item { TagsAndReimbursableCard(transactions, privacyMode) }
    }
    }

        if (showSubscriptionFinder) {
            Box(modifier = Modifier.fillMaxSize().background(AppSurface)) {
                SubscriptionFinderScreen(
                    subscriptions = stats.detectedSubscriptions,
                    onBack = { showSubscriptionFinder = false },
                )
            }
        }
        if (showNewTransaction) {
            NewTransactionSheet(onDismiss = { showNewTransaction = false })
        }
        InAppBannerHost(banner)
    }
}

/**
 * یک کارتِ کشف، پیش از تصمیمِ نمایش.
 *
 * فریمِ `52a`: کارت‌های کشف **جمع می‌شوند** - سه شرطِ مستقل بودند و هر سه می‌توانستند
 * هم‌زمان درست باشند، پس ماهِ شلوغ سه کارتِ پشتِ‌هم می‌داد و بخشِ کشف به دیوارِ هشدار
 * تبدیل می‌شد. حالا همه ساخته می‌شوند، مرتب می‌شوند، و **دوتای اول** نشان داده می‌شوند.
 */
private data class Discovery(
    /** کلیدِ پایدارِ نوع - نیمه‌ی اولِ کلیدِ نادیده‌گرفتن. */
    val kind: String,
    /** کوچک‌تر = فوری‌تر. */
    val priority: Int,
    val render: @Composable () -> Unit,
)

/** سقفِ کارتِ کشف در یک صفحه (فریمِ `52a`). */
private const val DISCOVERY_LIMIT = 2

/**
 * بازه‌ی گزارش. [months] فقط برای بازه‌های ماهانه معنی دارد؛ «هفته» با [days] کار می‌کند
 * (خواسته‌ی کاربر، ۳۱ شهریور: «بین هفته و ماه و فصل و سال قابلِ تنظیم باشد»).
 *
 * ⚠️ هفته عمداً «۷ روزِ گذشته» است نه «از شنبه»: بقیه‌ی برنامه (نمودارِ خانه، مرورِ
 * هفتگی) هم همین تعریف را دارد و دو تعریف از «هفته» یعنی دو عددِ متفاوت برای یک چیز.
 */
enum class ReportPeriod(val label: String, val months: Int, val days: Int = 0) {
    WEEK("هفته", 0, days = 7),
    MONTH("ماه", 1),
    SEASON("فصل", 3),
    YEAR("سال", 12),
}

// ═══ ۱ · هدر ═══════════════════════════════════════════════════════════════════
@Composable
private fun ReportHeader(
    period: ReportPeriod,
    onPeriod: (ReportPeriod) -> Unit,
    privacyMode: Boolean,
    onTogglePrivacy: () -> Unit,
    showControls: Boolean = true,
    onExcel: (() -> Unit)? = null,
    onPdf: () -> Unit = {},
) {
    var exportMenu by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("گزارش", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Black)
        // تو حالتِ خالی نه بازه‌ای برای انتخاب هست نه مبلغی برای پنهان‌کردن (فریمِ `21c` هدرِ لخت).
        if (!showControls) return@Row
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            // تاگلِ ماه/فصل/سال - انتخاب‌شده قرصِ سبزِ پرشده، بقیه فقط متن.
            ReportPeriod.entries.forEach { p ->
                val selected = p == period
                Text(
                    p.label,
                    color = if (selected) Color.White else AppMuted,
                    fontSize = 10.sp,
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (selected) AppPrimary else Color.Transparent)
                        .pressScaleClickable { onPeriod(p) }
                        .padding(horizontal = if (selected) 11.dp else 9.dp, vertical = 6.dp),
                )
            }
            if (onExcel != null) Box {
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(32.dp)
                        .clip(RoundedCornerShape(AppRadius.icon))
                        .background(PrivacyOffBg)
                        .border(1.5.dp, AppLine, RoundedCornerShape(10.dp))
                        .pressScaleClickable { exportMenu = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Download, contentDescription = "خروجیِ اکسل و PDF", tint = AppMuted, modifier = Modifier.size(16.dp))
                }
                androidx.compose.material3.DropdownMenu(expanded = exportMenu, onDismissRequest = { exportMenu = false }) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("خروجیِ اکسل") },
                        onClick = { exportMenu = false; onExcel() },
                    )
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("خروجیِ PDF") },
                        onClick = { exportMenu = false; onPdf() },
                    )
                }
            }
            Box(
                modifier = Modifier
                    // نزدیک‌تر به دکمه‌ی دانلود (مثلِ فاصله‌ی دکمه‌های سربرگِ خانه).
                    .offset(x = (-9).dp)
                    .minimumInteractiveComponentSize()
                    .size(32.dp)
                    .clip(RoundedCornerShape(AppRadius.icon))
                    .background(if (privacyMode) PrivacyOnBg else PrivacyOffBg)
                    .border(
                        1.5.dp,
                        if (privacyMode) PrivacyOnBorder else AppLine,
                        RoundedCornerShape(10.dp),
                    )
                    .pressScaleClickable(onClick = onTogglePrivacy),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (privacyMode) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = "پنهان‌کردنِ مبلغ‌ها",
                    tint = if (privacyMode) PrivacyOnInk else AppMuted,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

private val PrivacyOffBg: Color
    @Composable get() = AppIconFrame
private val PrivacyOnBg: Color
    @Composable get() = AppWarningPill
private val PrivacyOnBorder: Color
    @Composable get() = AppAssetBorder
private val PrivacyOnInk: Color
    @Composable get() = AppWarningInk
// ═══ ۱ب · کارتِ خط‌چینِ «نموداری برای کشیدن نیست» (فریمِ `21c`) ═════════════════════
@Composable
private fun NoChartCard(onAddTransaction: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppSurface)
            .dashedBorder(20.dp)
            .padding(horizontal = 16.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // تصویرِ سه‌بعدیِ ChatGPT (۷ مهر) به‌جای میله‌های خط‌چین.
        androidx.compose.foundation.Image(
            androidx.compose.ui.res.painterResource(ir.sadteam.loancalc.R.drawable.empty_illu_report),
            contentDescription = null,
            modifier = Modifier.size(112.dp),
        )
        // ⚠️ عنوان و توضیح **یه بلوکِ واحد**ن با فاصله‌ی ۶ (مثلِ `margin-top`ی فریم)، نه دو
        // آیتمِ جدا با فاصله‌ی منفی - `Modifier.padding` عددِ منفی رو قبول نمی‌کنه و همون
        // لحظه‌ی رسم کرش می‌ده (کرشِ نسخه‌ی ۱.۰.۴۷۷: «Padding must be non-negative»).
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "نموداری برای کشیدن نیست",
                color = AppText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Text(
                "با سه تراکنش، اولین نمودارت شکل می‌گیرد. با یک ماه، مقایسه‌ی ماه‌به‌ماه هم اضافه می‌شود.",
                color = AppMuted,
                fontSize = 12.5.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            "ثبتِ اولین خرج",
            color = Color.White,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .hardShadow(PrimaryShadow, 4.dp, 999.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(AppPrimary)
                .pressScaleClickable(onClick = onAddTransaction)
                .padding(vertical = 15.dp),
        )
    }
}

/** نوارِ طلاییِ «اگر پیامکِ بانکی را وصل کنی، گزارش خودش پر می‌شود». */
@Composable
private fun BankSmsHintCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.card))
            .background(GoldHintBg)
            .border(1.5.dp, GoldHintBorder, RoundedCornerShape(AppRadius.card))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = GoldHintIcon,
            modifier = Modifier.size(17.dp),
        )
        Text(
            "اگر پیامکِ بانکی را وصل کنی، گزارش خودش پر می‌شود",
            color = GoldHintInk,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 21.sp,
        )
    }
}

/** «وقتی داده داشته باشی اینها را می‌بینی» - سه نقطه‌ی رنگیِ فریم. */
@Composable
private fun ComingSoonCard() {
    // بازطراحیِ ChatGPT (۷ مهر، دورِ دوم): آیکون‌های رنگیِ تخت به‌جای نقطه.
    ir.sadteam.loancalc.ui.components.EmptyFeatureList(
        "وقتی داده داشته باشی این‌ها رو می‌بینی",
        listOf(
            ir.sadteam.loancalc.R.drawable.empty_icon_report_breakdown to "سهمِ هر دسته از خرجِ ماه",
            ir.sadteam.loancalc.R.drawable.empty_icon_report_compare to "مقایسه‌ی این ماه با ماهِ قبل و پارسال",
            ir.sadteam.loancalc.R.drawable.empty_icon_report_trend to "تفکیکِ خرجِ ثابت از متغیر",
        ),
    )
}

private val SkeletonBarBg: Color
    @Composable get() = AppLineRow
private val SkeletonBarFill: Color
    @Composable get() = AppPrimaryPill
private val SkeletonBaseline: Color
    @Composable get() = AppChartGrid
private val PrimaryShadow: Color
    @Composable get() = AppPrimaryDim
private val GoldHintBg: Color
    @Composable get() = AppWarningPill
private val GoldHintBorder: Color
    @Composable get() = AppDueNextBorder
private val GoldHintIcon: Color
    @Composable get() = AppWarningInk
private val GoldHintInk: Color
    @Composable get() = AppGoldInkSoft
// ═══ ۲ · هیرویِ بنفش ════════════════════════════════════════════════════════════
@Composable
private fun PeriodSpendHero(
    label: String,
    spend: Double,
    deltaPercent: Int?,
    bars: List<Double>,
    firstLabel: String,
    lastLabel: String,
    privacyMode: Boolean,
    /** کدام میله «حالا»ست. در نمای ماه روزِ جاری، در فصل/سال آخرین ماه. */
    currentBarIndex: Int = bars.lastIndex,
    /** برچسبِ هر میله برای حبابِ لمس. خالی یعنی نمودار لمس‌پذیر نیست. */
    barLabels: List<String> = emptyList(),
) {
    // خواسته‌ی کاربر (۳ مهر): مثلِ بقیه‌ی صفحه‌ها با تم عوض شود و همان موجِ نرم را داشته
    // باشد - پس همان `AppHeroCard` است، نه گرادیانِ بنفشِ ثابتِ قبلی.
    AppHeroCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text(
                    "خرجِ $label",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        // fmt() جداکننده‌ی لاتین می‌داد و عدد ریال بود: خرجِ ۱۰۲ میلیون
                        // تومان «1,026,600,000» چاپ می‌شد.
                        maskIfPrivate(masked, spend.rialToFaCompact()),
                        color = Color.White,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
                // واحد یک‌بار زیرِ عدد می‌آید، طبقِ قاعده‌ی عددِ سیستمِ طراحی.
                Text(
                    "تومان",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            if (deltaPercent != null) {
                Text(
                    // ⚠️ برچسبِ قبلی «قدرتِ خرید» بود و غلط: این عدد نسبتِ خرجِ این دوره به
                    // دوره‌ی قبل است، نه تورم و نه قدرتِ خرید. کاربری که خرجش ۲۰٪ بیشتر
                    // شده «۲۰٪ قدرتِ خرید» می‌دید و معنایش را برعکس می‌فهمید.
                    (if (deltaPercent > 0) "▲ " else "▼ ") +
                        "${(kotlin.math.abs(deltaPercent)).toFa()}٪ از دوره‌ی قبل",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        // خرجِ بیشتر پُررنگ‌تر. قرمز روی هیرویِ بنفش نمی‌نشیند، پس شدتِ
                        // همان سفید جهت را می‌رساند - کنارِ مثلثِ ▲/▼ که مستقل از رنگ است.
                        .background(Color.White.copy(alpha = if (deltaPercent > 0) 0.28f else 0.16f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }
        if (bars.any { it > 0.0 }) {
            HeroChart(
                values = bars,
                labels = barLabels,
                valueLabel = { value -> if (privacyMode) "•••" else "${value.rialToFaCompact()} تومان" },
                currentIndex = currentBarIndex,
                natural = HeroChartStyle.BARS,
                modifier = Modifier.padding(top = 12.dp),
            )
            // هم‌جهتِ میله‌ها (فیزیکی چپ‌به‌راست): اولِ دوره چپ، امروز راست.
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(firstLabel, color = Color.White.copy(alpha = 0.85f), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    Text(lastLabel, color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        } else {
            Text(
                "هنوز خرجی ثبت نکردی - با اولین تراکنش، روندِ ماه‌ها همین‌جا ساخته می‌شه",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

private val PurpleDeep = Color(0xFF7440C9)
private val PurpleShadow = Color(0xFF5C2FA8)

// ═══ ۳ · ثابت و متغیر ══════════════════════════════════════════════════════════
/** نوارِ ۲۴ پیکسلی با گوشه‌ی ۹: بخشِ «ثابت» راه‌راهِ قرمز، بخشِ «آزاد» سبزِ کم‌رنگ. */
/**
 * سه کارتِ کوچکِ «درآمد / هزینه / تعدادِ تراکنش»، هرکدام با تغییرِ نسبت به دوره‌ی قبل.
 *
 * ⚠️ رنگِ فلش **معناییِ پول** است نه «خوب/بد»: بالا رفتنِ درآمد سبز و بالا رفتنِ هزینه
 * قرمز است. همان دو توکنِ سراسری، پس با تمِ خریدنی نمی‌چرخند.
 */
@Composable
private fun PeriodStatRow(
    income: Double,
    incomeChangePercent: Int?,
    spend: Double,
    spendChangePercent: Int?,
    count: Int,
    countDelta: Int,
    privacyMode: Boolean,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PeriodStatCard(
            icon = Icons.Filled.ArrowDownward,
            iconTint = AppPrimaryInk,
            iconBg = AppPrimaryPill,
            title = "درآمد",
            value = maskIfPrivate(privacyMode, income.rialToFaCompact()),
            unit = "تومان",
            footer = incomeChangePercent?.let { "${kotlin.math.abs(it).toFa()}٪ از دوره‌ی قبل" } ?: "دوره‌ی قبل خالی",
            footerTint = if ((incomeChangePercent ?: 0) >= 0) AppPrimaryInk else AppDangerInk,
            modifier = Modifier.weight(1f),
        )
        PeriodStatCard(
            icon = Icons.Filled.ArrowUpward,
            iconTint = AppDangerInk,
            iconBg = AppDangerPill,
            title = "هزینه",
            value = maskIfPrivate(privacyMode, spend.rialToFaCompact()),
            unit = "تومان",
            footer = spendChangePercent?.let { "${kotlin.math.abs(it).toFa()}٪ از دوره‌ی قبل" } ?: "دوره‌ی قبل خالی",
            footerTint = if ((spendChangePercent ?: 0) > 0) AppDangerInk else AppPrimaryInk,
            modifier = Modifier.weight(1f),
        )
        PeriodStatCard(
            icon = Icons.Filled.SwapHoriz,
            iconTint = AppPurple,
            iconBg = AppIconFrame,
            title = "تعدادِ تراکنش",
            value = count.toFa(),
            unit = "عدد",
            footer = when {
                countDelta > 0 -> "+${countDelta.toFa()} از دوره‌ی قبل"
                countDelta < 0 -> "−${(-countDelta).toFa()} از دوره‌ی قبل"
                else -> "بی‌تغییر"
            },
            footerTint = AppMuted,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PeriodStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    value: String,
    unit: String,
    footer: String,
    footerTint: Color,
    modifier: Modifier = Modifier,
) {
    AppCard(contentPadding = 10.dp, modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = AppText, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier.size(22.dp).clip(RoundedCornerShape(999.dp)).background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(12.dp))
            }
        }
        Text(
            value,
            color = AppText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(unit, color = AppLabel, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
        Text(
            footer,
            color = footerTint,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(top = 5.dp),
        )
    }
}

@Composable
private fun FixedVsFreeCard(
    fixedShare: Int,
    fixedAmount: Double,
    freeAmount: Double,
    privacyMode: Boolean,
) {
    val shape = RoundedCornerShape(AppRadius.card)
    Column(
        verticalArrangement = Arrangement.spacedBy(11.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppSurface)
            .border(2.dp, AppLine, shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text("ثابت و متغیر", color = AppText, fontSize = 12.sp, fontWeight = FontWeight.Black)
            Text("${(fixedShare).toFa()}٪", color = AppDangerInk, fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
        Row(
            modifier = Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(9.dp)),
        ) {
            Box(
                modifier = Modifier
                    .weight(fixedShare.coerceIn(1, 99).toFloat())
                    .fillMaxHeight()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(AppDanger, StripeDark, AppDanger),
                            start = Offset(0f, 0f),
                            end = Offset(24f, 24f),
                            tileMode = androidx.compose.ui.graphics.TileMode.Repeated,
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("ثابت", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
            Box(
                modifier = Modifier
                    .weight((100 - fixedShare).coerceIn(1, 99).toFloat())
                    .fillMaxHeight()
                    .background(AppPrimaryPill),
                contentAlignment = Alignment.Center,
            ) {
                Text("آزاد", color = AppPrimaryDim, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
        }
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                "اجاره، اقساط و قبض ${maskIfPrivate(masked, (fixedAmount).rialToFaCompact())} تومان از " +
                    "درآمدت را برده — ${maskIfPrivate(masked, (freeAmount).rialToFaCompact())} تومان " +
                    "برای خرجِ آزاد مانده.",
                color = AppMuted,
                fontSize = 10.sp,
                lineHeight = 18.sp,
            )
        }
    }
}

private val StripeDark = Color(0xFFE23F3F)

// ═══ ۴ · دونات ════════════════════════════════════════════════════════════════
@Composable
private fun CategoryDonutCard(
    byCategory: Map<String, Double>,
    total: Double,
    periodLabel: String,
    privacyMode: Boolean,
    monthlyInstallmentRial: Double,
    chequesThisMonth: Int,
    onOpenLoanStats: () -> Unit,
    onOpenChequeReport: () -> Unit,
) {
    // زیرِ ۱٪ پنهان (۱۴ مهر، مثلِ خانه) - «قبض ۰٪» فقط شلوغی بود.
    val top = remember(byCategory, total) {
        byCategory.entries.sortedByDescending { it.value }
            .filter { total <= 0.0 || it.value / total >= 0.01 }.take(3)
    }
    val colors = listOf(AppDanger, AppPurple, AppInfo)
    // مبلغِ کل همین بالا در کارتِ سبز هست؛ وسطِ دایره تعدادِ دسته‌ها می‌آید.
    val centerNumber = top.size.toFa()
    val centerUnit = "دسته"
    val shape = RoundedCornerShape(AppRadius.card)
    Column(
        verticalArrangement = Arrangement.spacedBy(11.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppSurface)
            .border(2.dp, AppLine, shape)
            .padding(14.dp),
    ) {
    // برچسبِ دوره از داخلِ دایره بیرون آمد. «این ماه»ِ ثابت هم غلط بود: با تاگلِ فصل/سال
    // عوض نمی‌شد، پس روی دوره‌ی سالانه هم «این ماه» می‌نوشت.
    Text("سهمِ دسته‌ها از خرجِ $periodLabel", color = AppLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    // ۱۶ مهر (طرحِ مرتب‌تر، مثلِ خانه): حلقه + فهرستِ هم‌ترازِ دسته‌ها با مبلغ و درصد.
    // جمعِ کل همین بالا در کارتِ سبز هست، پس این‌جا تکرارش نمی‌کنیم.
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        CategoryDonut(
            slices = top.mapIndexed { i, e -> DonutSlice(e.value, colors[i % colors.size]) },
            size = 80.dp,
            strokeWidth = 9.dp,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    centerNumber,
                    color = AppText,
                    fontSize = 17.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                )
                Text(
                    centerUnit,
                    color = AppLabel,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f).padding(start = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            top.forEachIndexed { i, entry ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(colors[i % colors.size]),
                    )
                    Text(
                        entry.key,
                        color = AppText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                    )
                    PrivacyCrossfade(privacyMode) { masked ->
                        Text(
                            maskIfPrivate(masked, entry.value.rialToFaCompact()),
                            color = AppText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                        )
                    }
                    Text(
                        // total صفر → NaN٪. کارت با جمعِ صفر نمی‌آید، ولی نگهبانش یک خط است.
                        if (total <= 0.0) "—" else "${((entry.value / total * 100).toInt()).toFa()}٪",
                        color = AppMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(40.dp),
                    )
                }
            }
        }
    }
    }
}

// ═══ ۵ و ۶ · کارت‌های کشف ═══════════════════════════════════════════════════════

/**
 * دو ردیفِ ورودی به گزارشِ تعهدی — بخشِ ۸۱، فریمِ `81a`.
 *
 * 🚨 **چرا ردیف و نه کارت** (تشخیصِ مرکزیِ طراح): کاری که این تکه می‌کند **دو تپ** است، و
 * هر چیزی که فقط در را باز می‌کند در این برنامه ردیف است. نسخه‌ی قبلی یک کارتِ مادر با
 * سرصفحه داشت و دو کارتِ فرزند داخلش — سه لایه قاب برای دو تپ، و «کارتِ داخلِ کارت» با
 * تبِ تختِ گزارش هم‌خانواده نمی‌شد. **دو کارتِ هم‌عرض هم امتحان و رد شد**: عرضِ ~۱۵۰dp جای
 * `۴۶٬۸۳۸٬۶۳۶` را ندارد و عدد خلاصه می‌شد.
 *
 * 🚨 **سرصفحه حذف شد و عددِ سرصفحه هم ساخته نشد**: جمعِ قسط و چک همان ۵۷٪ِ دوناتِ بالای
 * همین صفحه است، و یک عدد دو بار در یک صفحه کاربر را دنبالِ تفاوتشان می‌فرستد. جایش خطِ
 * گروه با نقطه‌ی هم‌رنگِ تکه‌ی دونات است — کارِ سرصفحه‌ی ۳۴پیکسلی را ۹ پیکسل انجام می‌دهد.
 *
 * ⚠️ **هیچ عددِ تازه‌ای ساخته نشد**: مبلغ همان صورتِ کسرِ قرصِ «۱۰۹٪ از درآمدت» در تبِ وام
 * است، و شمارش همان چکِ در انتظارِ تبِ چک. دو ردیف دو **جنسِ** عدد دارند (مبلغ و شمارش) و
 * همین تفاوت دو مقصد را بی یک کلمه توضیح از هم جدا می‌کند.
 */
@Composable
private fun CommitmentRows(
    monthlyInstallmentRial: Double,
    chequesThisMonth: Int,
    privacyMode: Boolean,
    onOpenLoanStats: () -> Unit,
    onOpenChequeReport: () -> Unit,
) {
    // ۱۴ مهر: از داخلِ کارتِ دونات بیرون آمد - دیگر کارت توی کارت نیست.
    run {
        AppCard(label = "تعهدهای این ماه") {
            CommitmentRow(
                icon = Icons.Filled.EventRepeat,
                title = "اقساط وام",
                value = maskIfPrivate(privacyMode, rialToToman(monthlyInstallmentRial.toLong()).toFaMoney()) + " تومان",
                caption = "این ماه",
                onClick = onOpenLoanStats,
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AppLineRow))
            CommitmentRow(
                icon = Icons.Filled.CheckCircle,
                title = "چک‌ها",
                // شمارش است نه مبلغ، پس حالتِ خصوصی پوشانده‌اش نمی‌کند.
                value = "${chequesThisMonth.toFa()} چک",
                caption = "تا آخرِ ماه",
                onClick = onOpenChequeReport,
            )
        }
    }
}

/** یک ردیفِ تعهد. بنفش فقط سه نقطه می‌آید: زمینه‌ی آیکون، خودِ عدد، و نقطه‌ی خطِ گروه. */
@Composable
private fun CommitmentRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    caption: String,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .pressScaleClickable(onClick = onClick)
            .defaultMinSize(minHeight = AppSpacing.minTouchTarget)
            .padding(vertical = 9.dp),
    ) {
        Box(
            modifier = Modifier.size(30.dp).clip(RoundedCornerShape(AppRadius.icon)).background(AppPurplePill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = AppPurple, modifier = Modifier.size(15.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 9.dp)) {
            Text(title, color = AppText, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
            Text(caption, color = AppMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 1.dp))
        }
        Text(value, color = AppPurpleInk, fontSize = 12.5.sp, fontWeight = FontWeight.Black)
        Icon(
            Icons.Filled.ChevronLeft,
            contentDescription = null,
            tint = AppMuted,
            modifier = Modifier.padding(start = 4.dp).size(15.dp),
        )
    }
}

/** کارتِ کشف - گوشه ۲۰ · پدینگ ۱۴×۱۶ · حاشیه ۲ · قابِ آیکونِ ۳۴ با گوشه‌ی ۱۱ · فاصله ۱۱. */
@Composable
private fun DiscoveryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    bg: Color,
    border: Color,
    pill: Color,
    ink: Color,
    subInk: Color,
    iconInk: Color,
    // فلشِ کارت از اول تو فریم بود ولی هیچ‌کاری نمی‌کرد؛ کارتِ اشتراک‌یاب اولین کارتیه که
    // واقعاً یه صفحه باز می‌کنه، پس onClick اختیاری اضافه شد نه اجباری.
    onClick: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    /** بسته‌ی ChatGPT (۷ مهر): عددِ درشت + «ببین چرا» که توضیح را درجا باز می‌کند. */
    bigValue: String? = null,
    bigInk: Color = ink,
    whyText: String? = null,
) {
    val shape = RoundedCornerShape(AppRadius.card)
    var whyOpen by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg)
            .border(2.dp, border, shape),
    ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.pressScaleClickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Box(
            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(AppRadius.icon)).background(pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconInk, modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = ink, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = subInk, fontSize = 9.5.sp, modifier = Modifier.padding(top = 2.dp))
        }
        // فلش فقط وقتی می‌آید که جایی برود. سه کارت فلش داشتند و دو تایشان با تپ کاری
        // نمی‌کردند.
        if (onClick != null) {
            Icon(
                Icons.Filled.ChevronLeft,
                contentDescription = null,
                tint = ChevronInk,
                modifier = Modifier.size(13.dp),
            )
        }
        if (onDismiss != null) {
            // هدفِ لمسیِ ۴۴ با `.size()` **قبل از** `pressScaleClickable` ساخته می‌شود و
            // آیکون داخلش ۱۳ می‌مانَد - پدینگ بعدِ کلیک‌پذیری هدف را کوچک می‌کرد (قاعده‌ی ۵).
            Box(
                modifier = Modifier.size(44.dp).pressScaleClickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "نادیده بگیر",
                    tint = ChevronInk,
                    modifier = Modifier.size(13.dp),
                )
            }
        }
    }
    if (bigValue != null) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
        ) {
            Text(bigValue, color = bigInk, fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            if (whyText != null) {
                Box(
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(AppSurface)
                        .border(1.dp, border, RoundedCornerShape(13.dp))
                        .pressScaleClickable { whyOpen = !whyOpen }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (whyOpen) "بستن" else "ببین چرا", color = bigInk, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        AnimatedVisibility(visible = whyOpen && whyText != null) {
            Text(
                whyText.orEmpty(),
                color = subInk,
                fontSize = 11.sp,
                lineHeight = 19.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppSurface)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }
    }
    }
}

/**
 * حالتِ «این ماه چیزی پیدا نشد» - فریمِ `52a`.
 *
 * بی این کارت، ماهی که هیچ شرطی برقرار نبود بخشِ کشف را **کاملاً غیب** می‌کرد و کاربر
 * فرق «بررسی شد، چیزی نبود» با «کار نمی‌کند» را نمی‌فهمید.
 */
@Composable
private fun NoDiscoveryCard(checkedCount: Int, monthsOfHistory: Int) {
    // ۱۴ مهر: وقتی چیزی پیدا نشده یک خطِ کوچک کافی است، نه یک کارتِ کامل.
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
    ) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(16.dp))
        Text(
            if (monthsOfHistory < 4) {
                "خرجِ غیرعادی نداشتی · ${(checkedCount).toFa()} تراکنش بررسی شد"
            } else {
                "خرجِ غیرعادی نداشتی · ${(checkedCount).toFa()} تراکنش با سه ماهِ قبل سنجیده شد"
            },
            color = AppMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

private val DiscoverWarnBg: Color
    @Composable get() = AppWarningPill
private val DiscoverWarnBorder: Color
    @Composable get() = AppDueNextBorder
private val DiscoverWarnPill: Color
    @Composable get() = AppDueNextBorder
private val DiscoverWarnInk: Color
    @Composable get() = AppWarningInk
private val DiscoverWarnSubInk: Color
    @Composable get() = AppAssetInk
private val DiscoverWarnIconInk: Color
    @Composable get() = AppWarningInk
private val DiscoverDangerBg: Color
    @Composable get() = AppDangerPill
private val DiscoverDangerBorder: Color
    @Composable get() = AppUrgentBorder
private val DiscoverDangerPill: Color
    @Composable get() = AppUrgentShadow
private val ChevronInk: Color
    @Composable get() = AppMarkOff
// ═══ ۷ · خروجی (بسته‌ی ChatGPT، ۷ مهر: دو دکمه‌ی جدا) ═══════════════════════════════════
@Composable
private fun ExportCard(onExcel: () -> Unit, onPdf: () -> Unit) {
    AppCard(contentPadding = 14.dp, horizontalPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(AppRadius.icon)).background(AppPrimaryPill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Download, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(21.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("خروجیِ اکسل و PDF", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black)
                Text(
                    "گزارشِ این ماه را برای ذخیره یا اشتراک آماده کن",
                    color = AppMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            GradientButton(onClick = onExcel, variant = AppButtonVariant.SECONDARY, modifier = Modifier.weight(1f)) {
                Text("اکسل", fontWeight = FontWeight.Black)
            }
            GradientButton(onClick = onPdf, variant = AppButtonVariant.SECONDARY, modifier = Modifier.weight(1f)) {
                Text("PDF", fontWeight = FontWeight.Black)
            }
        }
    }
}

/** خرجِ هر دسته در ماهِ جاری در برابرِ ماهِ قبل - پنج دسته با بیشترین تغییر. */
@Composable
private fun MonthCompareCard(all: List<ir.sadteam.loancalc.data.db.AccountTransactionEntity>, privacyMode: Boolean) {
    val today = remember { ir.sadteam.loancalc.core.JalaliCalendar.today() }
    val (py, pm) = if (today.m == 1) (today.y - 1) to 12 else today.y to (today.m - 1)
    fun spend(y: Int, m: Int) = all.filter {
        it.confirmed && it.year == y && it.month == m && it.type == "WITHDRAWAL" &&
            it.sourceType !in ir.sadteam.loancalc.data.NON_SPENDING_SOURCES
    }.groupBy { it.category ?: "بی‌دسته" }.mapValues { e -> e.value.sumOf { it.amount } }
    val now = spend(today.y, today.m)
    val prev = spend(py, pm)
    if (now.isEmpty() && prev.isEmpty()) return
    val rows = (now.keys + prev.keys).map { k -> Triple(k, now[k] ?: 0.0, prev[k] ?: 0.0) }
        .sortedByDescending { kotlin.math.abs(it.second - it.third) }.take(5)
    val totalNow = now.values.sum()
    val totalPrev = prev.values.sum()
    AppCard(contentPadding = 14.dp) {
        Text("مقایسه با ماهِ قبل", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black)
        PrivacyCrossfade(privacyMode) { masked ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppBg)
                    .border(1.dp, AppLine, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("این ماه", color = AppMuted, fontSize = 10.sp)
                    Text("${maskIfPrivate(masked, totalNow.rialToFaCompact())} تومان", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("ماهِ قبل", color = AppMuted, fontSize = 10.sp)
                    Text("${maskIfPrivate(masked, totalPrev.rialToFaCompact())} تومان", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        rows.forEach { (cat, a, b) ->
            val diff = a - b
            val up = diff > 0
            val ink = if (diff == 0.0) AppMuted else if (up) AppDangerInk else AppPrimaryInk
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(cat, color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    PrivacyCrossfade(privacyMode) { masked ->
                        Text(
                            maskIfPrivate(masked, kotlin.math.abs(diff).rialToFaCompact()) +
                                (if (up) "  ↑ بیشتر" else if (diff < 0) "  ↓ کمتر" else ""),
                            color = ink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
                // طولِ نوار = این ماه نسبت به بیشترینِ دو ماه؛ خطِ نازکِ کم‌رنگ = ماهِ قبل.
                val maxV = maxOf(a, b).coerceAtLeast(1.0)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .height(7.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(AppLine),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth((b / maxV).toFloat().coerceIn(0f, 1f))
                            .height(7.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(AppMuted.copy(alpha = 0.35f)),
                    )
                    Box(
                        Modifier
                            .fillMaxWidth((a / maxV).toFloat().coerceIn(0f, 1f))
                            .height(7.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(ink.copy(alpha = 0.85f)),
                    )
                }
            }
        }
        Text(
            "نوارِ رنگی این ماه · نوارِ کم‌رنگ ماهِ قبل",
            color = AppLabel,
            fontSize = 9.5.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/** جمعِ خرجِ هر برچسب (کلِ زمان) + خرج‌های بازپرداختی. */
@Composable
private fun TagsAndReimbursableCard(all: List<ir.sadteam.loancalc.data.db.AccountTransactionEntity>, privacyMode: Boolean) {
    val spends = all.filter { it.confirmed && it.type == "WITHDRAWAL" }
    val byTag = spends.flatMap { tx -> tx.tags?.split(',')?.filter { it.isNotBlank() }?.map { it to tx.amount } ?: emptyList() }
        .groupBy({ it.first }, { it.second }).mapValues { it.value.sum() }
        .entries.sortedByDescending { it.value }.take(6)
    val reimb = spends.filter { it.reimbursable }.sumOf { it.amount }
    if (byTag.isEmpty() && reimb == 0.0) return
    AppCard(contentPadding = 14.dp) {
        if (byTag.isNotEmpty()) {
            Text("برچسب‌ها", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black)
            byTag.forEach { (tag, sum) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("#$tag", color = AppText, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    PrivacyCrossfade(privacyMode) { masked ->
                        Text(maskIfPrivate(masked, sum.rialToFaCompact()) + " تومان", color = AppMuted, fontSize = 12.sp)
                    }
                }
            }
        }
        if (reimb > 0) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("↩ خرج‌های بازپرداختی (قرار است پس بگیری)", color = AppText, fontSize = 12.sp, modifier = Modifier.weight(1f))
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(maskIfPrivate(masked, reimb.rialToFaCompact()) + " تومان", color = AppPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
