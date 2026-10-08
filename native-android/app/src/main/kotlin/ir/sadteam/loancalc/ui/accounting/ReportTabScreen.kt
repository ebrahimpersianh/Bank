package ir.sadteam.loancalc.ui.accounting

import ir.sadteam.loancalc.data.netDangShares
import androidx.compose.foundation.layout.heightIn
import ir.sadteam.loancalc.ui.components.AppButtonVariant
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Download
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.ChequeStatus
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.cheque.ChequeViewModel
import ir.sadteam.loancalc.ui.myloans.MyLoansViewModel
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.jibak.toFa
import ir.sadteam.loancalc.ui.components.dashedBorder
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyModeViewModel
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppAssetBorder
import ir.sadteam.loancalc.ui.theme.AppChartGrid
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import ir.sadteam.loancalc.ui.theme.AppWarningPill
import ir.sadteam.loancalc.ui.theme.hardShadow

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
        transactions.netDangShares().filter { ir.sadteam.loancalc.data.countsInReports(it) && it.type == "WITHDRAWAL" }
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
    val monthReal = monthTx.netDangShares().filter { ir.sadteam.loancalc.data.countsInReports(it) }
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
                            whyText = "این ماه ${maskIfPrivate(privacyMode, nowSpend.rialToFaCompact())} ${ir.sadteam.loancalc.ui.jibak.unitFa()} · " +
                                "میانگینِ سه ماهِ قبل ${maskIfPrivate(privacyMode, avgSpend.rialToFaCompact())} ${ir.sadteam.loancalc.ui.jibak.unitFa()}",
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
                            subtitle = "ماهی ${maskIfPrivate(privacyMode, (liveSubs.sumOf { it.typicalAmountRial }).rialToFaCompact())} ${ir.sadteam.loancalc.ui.jibak.unitFa()} — لمس کن ببین چی‌ان",
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
                            subtitle = "ماهی ${maskIfPrivate(privacyMode, (stats.recurringMonthly).rialToFaCompact())} ${ir.sadteam.loancalc.ui.jibak.unitFa()}",
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
    // سربرگِ یکدست (۱۵ مهر): ردیفِ اولِ عنوان + دکمه‌ها (چشم چپ‌ترین)، و بازه‌ی ماه/فصل/سال در
    // ردیفِ دوم - با دکمه‌های هم‌اندازه‌ی ۴۴ دیگر در یک ردیف جا نمی‌شدند.
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = ir.sadteam.loancalc.ui.components.PageHeaderHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("گزارش", color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            // تو حالتِ خالی نه بازه‌ای برای انتخاب هست نه مبلغی برای پنهان‌کردن (فریمِ `21c` هدرِ لخت).
            if (showControls) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (onExcel != null) Box {
                    ir.sadteam.loancalc.ui.components.HeaderIconButton(
                        icon = Icons.Filled.Download,
                        description = "خروجیِ اکسل و PDF",
                        onClick = { exportMenu = true },
                    )
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
                ir.sadteam.loancalc.ui.components.PrivacyEyeHeaderButton(privacyMode = privacyMode, onToggle = onTogglePrivacy)
            }
        }
        if (showControls) Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(bottom = 4.dp),
        ) {
            // تاگلِ ماه/فصل/سال - انتخاب‌شده قرصِ سبزِ پرشده، بقیه فقط متن.
            ReportPeriod.entries.forEach { p ->
                val selected = p == period
                Text(
                    p.label,
                    color = if (selected) Color.White else AppMuted,
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (selected) AppPrimary else Color.Transparent)
                        .pressScaleClickable { onPeriod(p) }
                        .padding(horizontal = if (selected) 14.dp else 12.dp, vertical = 7.dp),
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
