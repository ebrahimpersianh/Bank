package ir.sadteam.loancalc.ui.accounting

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
import ir.sadteam.loancalc.ui.theme.AppGoldPillSoft
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
        transactions.filter { it.sourceType != "transfer" && it.type == "WITHDRAWAL" }
    }
    val checkedTxCount = remember(realExpenses, today) {
        realExpenses.count { it.year == today.y && it.month == today.m }
    }
    val monthsOfHistory = remember(realExpenses) {
        realExpenses.map { it.year to it.month }.distinct().size
    }
    var showNewTransaction by remember { mutableStateOf(false) }
    var showSubscriptionFinder by remember { mutableStateOf(false) }

    // کلیدِ نادیده‌گرفتن = «نوع + ماهِ شمسی». **خاموشیِ دائمی نه**: کشفی که برای همیشه
    // خاموش می‌شود یعنی باگی که هیچ‌وقت گزارش نمی‌شود. ماهِ بعد دوباره می‌آید.
    // ماندگاری از `UiPrefs.dismissedDiscoveries` می‌آید - رجوع کن به [DiscoveryDismissViewModel].
    val dismissed by discoveryDismissViewModel.dismissed.collectAsState()
    val monthKey = "${today.y}-${today.m}"

    // ⚠️ زیرصفحه‌ها **روی** تب می‌نشینند، نه به‌جایش. قبلاً با `return` صدا زده می‌شدند و
    // کلِ LazyColumn از کامپوزیشن بیرون می‌رفت: پشتِ شیت سفیدِ خالی بود و اسکرولِ گزارش با
    // هر بستنِ اشتراک‌یاب صفر می‌شد. همان باگی که در خانه و تبِ دارایی رفع شد.
    BackHandler(enabled = showNewTransaction || showSubscriptionFinder) {
        if (showNewTransaction) showNewTransaction = false else showSubscriptionFinder = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 14.dp, 16.dp, 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ReportHeader(
                period = period,
                onPeriod = { period = it },
                privacyMode = privacyMode,
                onTogglePrivacy = { privacyViewModel.toggle() },
                showControls = transactions.isNotEmpty(),
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
        }
        // ── کارت‌های کشف ────────────────────────────────────────────────────
        // ترتیب **بر پایه‌ی فوریت**، نه ترتیبِ نوشته‌شدن در فایل: کسری اول، پرداختِ دوباره
        // دوم، اطلاعاتی سوم. قبلاً اشتراک‌یاب همیشه بالای «۳۰٪ بیشتر از معمول» می‌نشست.
        val discoveries = buildList {
            stats.overspentCategory?.let { over ->
                add(
                    Discovery("overspent", 1) {
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
                            onDismiss = { discoveryDismissViewModel.dismiss("overspent@$monthKey", monthKey) },
                        )
                    },
                )
            }
            // ⚠️ **اشتراک‌یاب** با کارتِ بعدی فرق دارد: آن پرداخت‌های تکراریِ **اعلام‌شده‌ی
            // خودِ کاربر** است، این چیزی است که اپ از روی تاریخچه **کشف** کرده و کاربر خبر
            // نداشته. تنها کارتِ کشفی که با تپ صفحه باز می‌کند.
            if (stats.detectedSubscriptions.isNotEmpty()) {
                add(
                    Discovery("subscriptions", 2) {
                        DiscoveryCard(
                            icon = Icons.Filled.Autorenew,
                            title = "${(stats.detectedSubscriptions.size).toFa()} خرجِ تکرارشونده پیدا شد",
                            subtitle = "ماهی ${(stats.detectedMonthly).rialToFaCompact()} تومان — لمس کن ببین چی‌ان",
                            bg = DiscoverWarnBg,
                            border = DiscoverWarnBorder,
                            pill = DiscoverWarnPill,
                            ink = DiscoverWarnInk,
                            subInk = DiscoverWarnSubInk,
                            iconInk = DiscoverWarnIconInk,
                            onClick = { showSubscriptionFinder = true },
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
                            subtitle = "ماهی ${(stats.recurringMonthly).rialToFaCompact()} تومان",
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
        item { ExportRow(onClick = onOpenExport) }
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
) {
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
            Box(
                modifier = Modifier
                    .padding(start = 3.dp)
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
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
        // چهار میله‌ی خط‌چینِ خالی که یکی‌شون سبزِ توپره - «شکلِ نمودارِ پیشاپیش».
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.height(92.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                listOf(30.dp to false, 51.dp to false, 39.dp to true, 69.dp to false).forEach { (h, filled) ->
                    val shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                    Box(
                        modifier = Modifier
                            .width(23.dp)
                            .height(h)
                            .clip(shape)
                            .background(if (filled) SkeletonBarFill else SkeletonBarBg)
                            .then(
                                if (filled) {
                                    Modifier.border(1.5.dp, AppPrimary, shape)
                                } else {
                                    Modifier.dashedBorder(6.dp, width = 1.5.dp)
                                }
                            ),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(2.dp)
                    .background(SkeletonBaseline),
            )
        }
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
            .clip(RoundedCornerShape(18.dp))
            .background(GoldHintBg)
            .border(1.5.dp, GoldHintBorder, RoundedCornerShape(18.dp))
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppSurface)
            .border(2.dp, AppLineRow, RoundedCornerShape(18.dp))
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            "وقتی داده داشته باشی اینها را می‌بینی",
            color = AppMuted,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        listOf(
            AppDanger to "سهمِ هر دسته از خرجِ ماه",
            AppInfo to "مقایسه‌ی این ماه با ماهِ قبل و پارسال",
            AppPurple to "تفکیکِ خرجِ ثابت از متغیر",
        ).forEach { (dot, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(dot),
                )
                Text(label, color = AppMuted, fontSize = 11.5.sp)
            }
        }
    }
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
    @Composable get() = AppGoldPillSoft
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .hardShadow(PurpleShadow, 5.dp, AppRadius.card)
            .clip(RoundedCornerShape(AppRadius.card))
            .background(Brush.linearGradient(listOf(AppPurple, PurpleDeep)))
            // این کارت `AppHeroCard` نیست (گرادیانِ بنفشِ خودش را دارد)، پس نقشِ برگ را
            // باید صریح بگیرد - وگرنه تنها کارتِ قهرمانِ برنامه بود که نداشت.
            .drawBehind { drawHeroLeaves(bothSides = true) }
            .padding(16.dp),
    ) {
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
            InteractiveBars(
                values = bars,
                labels = barLabels,
                valueLabel = { value -> "${value.rialToFaCompact()} تومان" },
                currentIndex = currentBarIndex,
                barColor = Color.White.copy(alpha = 0.30f),
                currentBarColor = Color.White,
                tooltipBackground = PurpleDeep,
                tooltipTitleColor = Color.White.copy(alpha = 0.75f),
                tooltipValueColor = Color.White,
                modifier = Modifier.padding(top = 12.dp),
                // ۳۱ میله در عرضِ یک کارت با فاصله‌ی ۳ جا نمی‌شود؛ فاصله با تعدادِ
                // میله‌ها کم می‌شود تا هر دو نما تمیز بمانند.
                spacing = if (bars.size > 12) 1.5.dp else 3.dp,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(firstLabel, color = Color.White.copy(alpha = 0.62f), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                Text(lastLabel, color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.ExtraBold)
            }
        } else {
            Text(
                "هنوز خرجی ثبت نکردی - با اولین تراکنش، روندِ ماه‌ها همین‌جا ساخته می‌شه",
                color = Color.White.copy(alpha = 0.72f),
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
        Text(unit, color = AppLabel, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
        Text(
            footer,
            color = footerTint,
            fontSize = 8.5.sp,
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
    val top = remember(byCategory) { byCategory.entries.sortedByDescending { it.value }.take(3) }
    val colors = listOf(AppDanger, AppPurple, AppInfo)
    val (centerNumber, centerUnit) = total.rialToFaCompactParts()
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
    Text("خرجِ $periodLabel", color = AppLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        CategoryDonut(
            slices = top.mapIndexed { i, e -> DonutSlice(e.value, colors[i % colors.size]) },
            size = 74.dp,
            strokeWidth = 13.dp,
        ) {
            // قطرِ داخلیِ دونات ۷۴ − ۲×۱۳ = ۴۸dp است. یک خطِ «۱۰۲٫۶ میلیون» در ۱۱sp
            // حدودِ ۵۲dp عرض می‌گیرد و به لبه‌ی رینگ می‌چسبد. عدد و واحد دو خطِ کوتاه شدند.
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        maskIfPrivate(masked, centerNumber),
                        color = AppText,
                        fontSize = 13.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                    )
                }
                Text(
                    centerUnit,
                    color = AppLabel,
                    fontSize = 7.5.sp,
                    lineHeight = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f).padding(start = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            top.forEachIndexed { i, entry ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(colors[i % colors.size]),
                    )
                    Text(
                        entry.key,
                        color = AppText,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).padding(start = 7.dp),
                    )
                    Text(
                        // total صفر → NaN٪. کارت با جمعِ صفر نمی‌آید، ولی نگهبانش یک خط است.
                        if (total <= 0.0) "—" else "${((entry.value / total * 100).toInt()).toFa()}٪",
                        color = AppMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
    // «قسط/چک» خرجِ تعهدی‌اند؛ گزارش جزئی‌شان از داده‌های وام و چک می‌آید، نه از دسته‌بندی حساب.
    // ⚠️ **بندِ ۴ی بخشِ ۸۱**: فاصله‌ی بالا نصف است تا به دونات بچسبد. فاصله‌ی مساوی بود که
    // این دو ردیف را «یتیم» نشان می‌داد.
    CommitmentRows(
        monthlyInstallmentRial = monthlyInstallmentRial,
        chequesThisMonth = chequesThisMonth,
        privacyMode = privacyMode,
        onOpenLoanStats = onOpenLoanStats,
        onOpenChequeReport = onOpenChequeReport,
    )
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
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, bottom = 7.dp),
        ) {
            Box(
                modifier = Modifier.size(9.dp).clip(RoundedCornerShape(999.dp)).background(AppDanger),
            )
            Text(
                "تعهدهای مالی",
                color = AppMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 7.dp),
            )
        }
        AppCard {
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
            modifier = Modifier.size(30.dp).clip(RoundedCornerShape(10.dp)).background(AppPurplePill),
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
) {
    val shape = RoundedCornerShape(AppRadius.card)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg)
            .border(2.dp, border, shape)
            .then(if (onClick != null) Modifier.pressScaleClickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Box(
            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(pill),
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
}

/**
 * حالتِ «این ماه چیزی پیدا نشد» - فریمِ `52a`.
 *
 * بی این کارت، ماهی که هیچ شرطی برقرار نبود بخشِ کشف را **کاملاً غیب** می‌کرد و کاربر
 * فرق «بررسی شد، چیزی نبود» با «کار نمی‌کند» را نمی‌فهمید.
 */
@Composable
private fun NoDiscoveryCard(checkedCount: Int, monthsOfHistory: Int) {
    val shape = RoundedCornerShape(AppRadius.card)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppSurface)
            .border(2.dp, AppLineRow, shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Box(
            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(AppPrimaryPill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = AppPrimary,
                modifier = Modifier.size(16.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("چیزِ غیرعادی‌ای پیدا نشد", color = AppText, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
            Text(
                if (monthsOfHistory < 4) {
                    // مقایسه‌ی سه‌ماهه به سه ماهِ کاملِ گذشته نیاز دارد (`detectOverspend`).
                    "${(checkedCount).toFa()} تراکنشِ این ماه بررسی شد · " +
                        "مقایسه‌ی سه‌ماهه با ${(4 - monthsOfHistory).toFa()} ماهِ دیگر داده فعال می‌شود"
                } else {
                    "${(checkedCount).toFa()} تراکنشِ این ماه با میانگینِ سه ماهِ گذشته سنجیده شد"
                },
                color = AppMuted,
                fontSize = 9.5.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

private val DiscoverWarnBg: Color
    @Composable get() = AppWarningPill
private val DiscoverWarnBorder: Color
    @Composable get() = AppGoldPillSoft
private val DiscoverWarnPill = Color(0xFFFFE3B8)
private val DiscoverWarnInk: Color
    @Composable get() = AppGoldInkSoft
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
// ═══ ۷ · خروجی ═════════════════════════════════════════════════════════════════
@Composable
private fun ExportRow(onClick: () -> Unit) {
    val shape = RoundedCornerShape(AppRadius.row)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppSurface)
            .border(2.dp, AppLineRow, shape)
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Box(
            modifier = Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(AppPrimaryPill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Download, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(14.dp))
        }
        Text(
            "خروجیِ اکسل و PDF",
            color = AppText,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f),
        )
        Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = ChevronInk, modifier = Modifier.size(13.dp))
    }
}
