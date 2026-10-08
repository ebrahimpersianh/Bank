package ir.sadteam.loancalc.ui.home

import ir.sadteam.loancalc.data.netDangShares
import ir.sadteam.loancalc.ui.components.guideTarget
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.MonthForecast
import ir.sadteam.loancalc.core.PersianCalendar
import ir.sadteam.loancalc.ui.jibak.toFa
import ir.sadteam.loancalc.core.ChequeStatus
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.accounting.ReportPeriod
import ir.sadteam.loancalc.ui.account.CompactTransactionRow
import ir.sadteam.loancalc.ui.accounting.NewTransactionSheet
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.components.SkeletonRowList
import ir.sadteam.loancalc.ui.cheque.ChequeViewModel
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppFab
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.ConfirmPayDialog
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.coin.CoinHubScreen
import ir.sadteam.loancalc.ui.profile.BadgeRetroSheet
import ir.sadteam.loancalc.ui.profile.GamificationViewModel
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.jibak.toFaMoney
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.inbox.InboxViewModel

/**
 * تبِ **خانه** - بازسازیِ کاملِ فریمِ `15a` (حالتِ عادی) و `15b` (روزِ اول / خالی).
 *
 * ⚠️ **این صفحه از نو نوشته شده، نه اصلاح.** دورِ قبل سبکِ جدید روی چیدمانِ اپِ قدیمی پوشیده
 * شد و نتیجه‌ش این بود که کاربر گفت «مثلِ برنامه‌ی قبلیه، فقط چهارتا آیتم اضافه شده». حالا
 * ترتیبِ کارت‌ها **عیناً** از خودِ فریم درآمده:
 *
 * ```
 * ۱ هدر: تاریخ + سلام + قرصِ فعال + شمارنده‌ی سکه
 * ۲ کارتِ سبز: خرجِ امروز + نمودارِ ۷ روزه
 * ۳ بودجهٔ ماه با سکه‌ی طلایی رو نوار
 * ۴ قسطِ سررسیدشده (فقط اگه باشه)
 * ۵ دوناتِ دسته‌بندی‌های همین ماه
 * ۶ مرورِ هفته - سه ستون
 * ```
 *
 * برای درآوردنِ همین لیست:
 * `python3 native-android/tools/static-checks/design-frames.py "design/Duolingo Redesign.dc.html" 15a`
 *
 * **چیزهایی که عمداً حذف شدن** (تو فریم نیستن): کارتِ «امروز» با میان‌برهای قسط/چک/یادداشت،
 * کارتِ «سرشماره اضافه نکردی»، و لیستِ «تراکنش‌های اخیر». هیچ‌کدوم قابلیتی رو از بین نمی‌برن -
 * تراکنش‌ها تو تبِ گزارش و سرشماره تو تنظیماتن.
 *
 * **رفتارهایی که از نسخه‌ی قبل حفظ شدن**: حالتِ خصوصی (`PrivacyCrossfade`)، شمارشِ بالارونده‌ی
 * عدد، بازکردنِ شیتِ تراکنشِ جدید با دکمه‌ی +، و تپ رو کارتِ قهرمان → تبِ دارایی.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRoute: (String) -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenInbox: () -> Unit = {},
    /** جستجوی کلیِ برنامه (۸ مهر). */
    onOpenSearch: () -> Unit = {},
    onOpenTransactions: () -> Unit = {},
    onOpenLoan: (Long) -> Unit = { onNavigateToRoute("loan") },
    /**
     * کارتِ پیشنهادِ نوارِ پایین (`41a`) - به‌صورتِ یه اسلاتِ آماده‌ی رندر پاس داده می‌شه، نه
     * داده‌ی خام. دلیل: چیدمانِ نوار و `ViewModel`ش تو `MainActivity` زندگی می‌کنن (همون‌جا که
     * ویرایشگر هم باز می‌شه)؛ اگه `HomeScreen` خودش `hiltViewModel()` می‌گرفت یه **نمونه‌ی
     * دومِ** جدا می‌ساخت. `null` یعنی پیشنهادی در کار نیست.
     */
    navSuggestionSlot: (@Composable () -> Unit)? = null,
    accountViewModel: AccountViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    urgentDueViewModel: UrgentDueViewModel = hiltViewModel(),
    gamificationViewModel: GamificationViewModel = hiltViewModel(),
    inboxViewModel: InboxViewModel = hiltViewModel(),
    // فقط برای شمارنده‌ی کارتِ «چک‌ها»ی ردیفِ میان‌بر - همان ViewModelی که تبِ گزارش دارد.
    chequeViewModel: ChequeViewModel = hiltViewModel(),
) {
    // 🧾 قبض‌های نزدیکِ موعد (۷ مهر): کارتِ کوچک فقط وقتی چیزی نزدیک است؛ صفحه‌ی خالی شلوغ نمی‌شود.
    val homeExtrasVm: ir.sadteam.loancalc.ui.extras.ExtrasViewModel = hiltViewModel()
    val homeBills by homeExtrasVm.bills.collectAsState()
    var showHomeBills by remember { mutableStateOf(false) }
    if (showHomeBills) {
        androidx.activity.compose.BackHandler { showHomeBills = false }
        ir.sadteam.loancalc.ui.extras.BillsScreen(onBack = { showHomeBills = false }, viewModel = homeExtrasVm)
        return
    }
    val billsToday = remember { ir.sadteam.loancalc.core.JalaliCalendar.today() }
    val dueBills = homeBills.filter { with(ir.sadteam.loancalc.ui.extras.BillsDue) { it.dueSoon(billsToday.y, billsToday.m, billsToday.d) } }
    val simple = ir.sadteam.loancalc.ui.privacy.LocalSimpleMode.current
    val startGuideVm: StartGuideViewModel = hiltViewModel()
    val startGuide by startGuideVm.state.collectAsState()
    val inboxCount by inboxViewModel.actionableCount.collectAsState()
    val inboxUnreadNews by inboxViewModel.unreadNews.collectAsState()
    val userName by authViewModel.userName.collectAsState()
    val urgentDue by urgentDueViewModel.urgent.collectAsState()
    val upcoming7d by urgentDueViewModel.upcoming7d.collectAsState()
    val recurringForInsights by accountViewModel.recurringPayments.collectAsState()
    val transactions by accountViewModel.transactions.collectAsState()
    val transactionsLoaded by accountViewModel.transactionsLoaded.collectAsState()
    val budgets by accountViewModel.budgets.collectAsState()
    val activeDays by gamificationViewModel.activeDays.collectAsState()
    val coins by gamificationViewModel.coins.collectAsState()
    val privacyMode = LocalPrivacyMode.current
    val today = remember { JalaliCalendar.today() }
    // شرطِ برگشتنِ قرصِ «فعال» به هدر (`55a`). همان تعریفی که یادآورِ روزانه استفاده می‌کند:
    // امروز تراکنشی ثبت شده یا نه.
    val todayHasEntry = remember(transactions, today) {
        transactions.any { it.year == today.y && it.month == today.m && it.day == today.d }
    }

    // سنجشِ نشان‌ها فقط از اینجا (تبِ خانه = اولین صفحه‌ی بعدِ ورود) صدا زده می‌شه تا
    // بازشدنِ گذشته دقیقاً یه‌بار و صامت انجام بشه.
    LaunchedEffect(Unit) {
        gamificationViewModel.syncBadges()
    }
    val retroBadges by gamificationViewModel.retroUnlocked.collectAsState()
    var showNewTransaction by remember { mutableStateOf(false) }
    var showCoinWallet by remember { mutableStateOf(false) }
    // 🚨 **آدمک حالا مقصد دارد** (خواسته‌ی صریحِ کاربر: «رو آدمک می‌زنم، به جایی برود»).
    // فریمِ `55a` عمداً بی‌مقصدش کرده بود تا با چرخ‌دنده دو درِ یک اتاق نشوند - ولی مقصدِ
    // درست تنظیمات نبود: **نشان‌ها** جای طبیعیِ آدمک‌اند (همان‌جا که شخصی‌سازی و پیشرفتِ
    // شخصی نشان داده می‌شود). پس تنظیمات هنوز یک در دارد و آدمک درِ دیگری به اتاقِ دیگر.
    var showProfile by remember { mutableStateOf(false) }
    // «پرداخت شد» بازگشت‌ناپذیر است و روی کارتِ قهرمانِ خانه یک تپِ اشتباه راحت رخ می‌دهد.
    // ⚠️ این تایید یک‌بار اضافه شده بود و بسته‌ی بازطراحیِ خانه رویش را نوشت - اگر دوباره
    // فایل را از طراح گرفتید، همین‌جا را چک کنید.
    var confirmPayDue by remember { mutableStateOf<UrgentDueViewModel.UrgentRow?>(null) }
    var pickPayAccountFor by remember { mutableStateOf<UrgentDueViewModel.UrgentRow?>(null) }

    // ⚠️ هر دو شیت قبلاً با `return` صدا زده می‌شدند و کلِ Box از کامپوزیشن بیرون می‌رفت:
    // پشتِ شیت سفیدِ خالی بود و اسکرولِ صفحه‌ی اول با بستنش صفر می‌شد. حالا **روی** صفحه
    // می‌نشینند، ته همان Box.
    BackHandler(enabled = showNewTransaction) { showNewTransaction = false }

    // خرجِ ۷ روزِ گذشته (قدیمی‌ترین → امروز) برای نمودارِ میله‌ایِ کارتِ قهرمان.
    val netTx = remember(transactions) { transactions.netDangShares() }
    val weekSpend = remember(netTx) {
        (6 downTo 0).map { back ->
            val d = PersianCalendar.addDays(today, -back)
            netTx.filter { it.isExpenseOn(d.y, d.m, d.d) }.sumOf { it.amount }
        }
    }
    // درآمدِ امروز - برای جفتِ «درآمد/خرجِ امروز»ِ کارتِ قهرمان (خواسته‌ی کاربر با طرحِ
    // مرجع، ۳۱ شهریور). خرج از `weekSpend.last()` می‌آید، پس فقط این یکی تازه است.
    val todayIncome = remember(transactions) {
        transactions.filter {
            it.type == "DEPOSIT" && it.year == today.y && it.month == today.m && it.day == today.d &&
                ir.sadteam.loancalc.data.countsInReports(it)
        }
            .sumOf { it.amount }
    }
    val weekTotal = remember(weekSpend) { weekSpend.sum() }
    // 🚨 **بازه‌ی کارتِ قهرمان قابلِ انتخاب شد** (خواسته‌ی کاربر، ۳۱ شهریور).
    // همان چهار بازه‌ی تبِ گزارش، پس `ReportPeriod` دوباره تعریف نمی‌شود - دو enum برای
    // یک مفهوم یعنی روزی که یکی عوض می‌شود و دیگری جا می‌مانَد.
    var heroPeriod by rememberSaveable { mutableStateOf(ReportPeriod.WEEK) }
    val effectivePeriod = if (simple) ReportPeriod.MONTH else heroPeriod
    val heroSeries = remember(transactions, effectivePeriod, today) {
        buildHeroSeries(transactions, today, effectivePeriod)
    }
    val prevWeekTotal = remember(netTx) {
        (13 downTo 7).sumOf { back ->
            val d = PersianCalendar.addDays(today, -back)
            netTx.filter { it.isExpenseOn(d.y, d.m, d.d) }.sumOf { it.amount }
        }
    }
    val monthSpendByCategory = remember(transactions) {
        accountViewModel.spendByCategory(transactions, today.y, today.m)
    }
    val monthSpend = remember(monthSpendByCategory) { monthSpendByCategory.values.sum() }
    val monthCap = remember(budgets) { budgets.sumOf { it.monthlyCap } }

    // پیش‌بینیِ «تا آخرِ ماه کم میاری» - رجوع کن به MonthForecast تو :core.
    // موجودی = جمعِ موجودیِ همه‌ی حساب‌کتاب‌ها (همون تعریفی که تبِ دارایی نشون می‌ده).
    val accounts by accountViewModel.accounts.collectAsState()
    val insightCtx = androidx.compose.ui.platform.LocalContext.current
    val smartInsights = remember(transactions, accounts, upcoming7d, recurringForInsights) {
        val today = ir.sadteam.loancalc.core.JalaliCalendar.today()
        val txs = transactions.netDangShares().filter { ir.sadteam.loancalc.data.countsInReports(it) && it.confirmed }.map {
            ir.sadteam.loancalc.core.SmartInsights.Tx(
                isExpense = it.type == ir.sadteam.loancalc.core.TransactionType.WITHDRAWAL.name,
                amountRial = it.amount, y = it.year, m = it.month, d = it.day,
                category = it.category, description = it.description,
            )
        }
        ir.sadteam.loancalc.core.SmartInsights.compute(
            txs, today, ir.sadteam.loancalc.core.JalaliCalendar.daysInMonth(today.y, today.m),
            upcomingDues7d = upcoming7d,
            totalBalance = if (accounts.isEmpty()) null else accounts.sumOf { accountViewModel.balanceOf(it, transactions) },
            existingRecurringNames = recurringForInsights.map { it.name }.toSet(),
        )
    }
    val cheques by chequeViewModel.cheques.collectAsState()
    /** چکِ «باز» = بایگانی‌نشده و هنوز وصول/برگشت نخورده. */
    val openChequeCount = remember(cheques) {
        cheques.count { !it.archived && it.status == ChequeStatus.PENDING.name }
    }
    var showTodaySpend by rememberSaveable { mutableStateOf(false) }
    val monthForecast = remember(transactions, accounts, monthSpend) {
        MonthForecast.compute(
            spentSoFarRial = monthSpend,
            dayOfMonth = today.d,
            daysInMonth = JalaliCalendar.daysInMonth(today.y, today.m),
            balanceRial = accounts.sumOf { accountViewModel.balanceOf(it, transactions) },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // کشیدنِ صفحه به پایین = تازه‌سازی از سرور (اشتراک، شماره‌ی کاربری، پیام‌ها) - خواسته‌ی کاربر ۱۰ مهر.
        var homeRefreshing by remember { mutableStateOf(false) }
        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
            isRefreshing = homeRefreshing,
            onRefresh = {
                homeRefreshing = true
                urgentDueViewModel.refresh(); authViewModel.refreshStatus { homeRefreshing = false }
            },
            modifier = Modifier.fillMaxSize(),
        ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // حاشیه‌ی صفحه ۱۶ طبقِ بندِ ۳ سیستمِ طراحی، فاصله‌ی بینِ کارت‌ها ۱۳ طبقِ خودِ فریم.
            contentPadding = PaddingValues(16.dp, 14.dp, 16.dp, 170.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            item {
                HomeHeader(
                    today = today,
                    userName = userName,
                    activeDays = activeDays,
                    onOpenCoins = { showCoinWallet = true },
                    onOpenShop = { onNavigateToRoute("shop") },
                    onOpenProfile = { showProfile = true },
                    coins = coins,
                    onOpenSettings = onOpenSettings,
                    inboxCount = inboxCount,
                    inboxUnreadNews = inboxUnreadNews,
                    onOpenInbox = onOpenInbox,
                    onOpenSearch = onOpenSearch,
                    todayHasEntry = todayHasEntry,
                )
            }

            // ── پیشنهادِ خودکارِ نوارِ پایین (فریمِ `41a`) ────────────────────────────────
            // «بالای صفحه‌ی خانه، **زیرِ هدر**. هرگز مودال نمی‌شود» - قاعده‌ی صریحِ `41c`.
            if (!simple) navSuggestionSlot?.let { slot -> item { slot() } }

            // ── ترمیمِ زنجیر: **از خانه برداشته شد** (تصمیمِ طراح، فریمِ `56b`) ─────────────
            // کارتش دو جا بود - این‌جا و کیفِ سکه. سندِ طراح صریح است که جایش کیفِ سکه
            // است: «آنچه نیست فقط صفحه‌ای برای دیده‌شدنشان است» و آن صفحه ساخته شد.
            //
            // 🚨 و همین کارت بود که کاربر با چهار اسکرین‌شات گزارشش کرد: بعدِ یک‌دو روز
            // غیبت، یک کارتِ سفید به بلندیِ کلِ صفحه بالای کارتِ قهرمان می‌نشست و اولین
            // چیزی که از اپ دیده می‌شد همان بود. علتش هیچ‌وقت پیدا نشد (فقط با
            // `heightIn` مهار شده بود)؛ با برداشتنش هم باگ می‌رود هم دوگانگی.
            //
            // درِ ورودی: قرصِ سکه‌ی هدرِ خانه → کیفِ سکه، که کارتِ ترمیم **بالای** ردیفِ
            // «فعال» در همان صفحه می‌نشیند.

            // ── اسکلتِ لودینگ ────────────────────────────────────────────────────────────
            // 🚨 تا اولین خواندنِ دیتابیس برنگشته، «خالی» نشان داده نمی‌شود: دیتابیس رمزنگاری‌شده
            // است و چند فریم طول می‌کشد، و در آن فاصله حالتِ خالی (کارتِ خط‌چینِ بلند) رندر
            // می‌شد و کاربر باید از رویش رد می‌شد تا محتوای واقعی را ببیند.
            if (!transactionsLoaded) {
                item { SkeletonRowList(rows = 4) }
            }

            // ── «چهار قدم تا شروع» (۱۳ مهر) ─────────────────────────────────────────────
            if (transactionsLoaded && startGuide.visible) {
                item(key = "start-guide") {
                    StartGuideCard(
                        state = startGuide,
                        onAddAccount = { onNavigateToRoute("accounts-add") },
                        onAddTransaction = { showNewTransaction = true },
                        onAddLoan = { onNavigateToRoute("loan") },
                        onLinkBank = {
                            ir.sadteam.loancalc.ui.admin.AdminSignals.openSettingsRoute.value = "SMS"
                            onOpenSettings()
                        },
                        onDismiss = { startGuideVm.dismiss() },
                    )
                }
            }

            // ── حالتِ خالی (فریمِ `15b`) - وقتی هنوز هیچ تراکنشی ثبت نشده ────────────────
            if (transactionsLoaded && transactions.isEmpty()) {
                item {
                    HomeEmptyHero(onAddFirst = { showNewTransaction = true })
                }
                item {
                    Text(
                        "با این ۳ تا شروع کن",
                        color = AppMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StarterTile(
                            iconRes = ir.sadteam.loancalc.R.drawable.empty_icon_shortcut_budget,
                            label = "ساختِ بودجه",
                            subtitle = "هزینه‌ها رو مدیریت کن",
                            onClick = { onNavigateToRoute("budget") },
                        )
                        StarterTile(
                            iconRes = ir.sadteam.loancalc.R.drawable.empty_icon_shortcut_check,
                            label = "افزودنِ چک",
                            subtitle = "چک‌ها رو پیگیری کن",
                            onClick = { onNavigateToRoute("cheque") },
                        )
                        StarterTile(
                            iconRes = ir.sadteam.loancalc.R.drawable.empty_icon_shortcut_loan,
                            label = "ثبتِ وام",
                            subtitle = "قسط‌ها رو دنبال کن",
                            onClick = { onNavigateToRoute("loan") },
                        )
                    }
                }
                item { FirstRewardNote() }
                return@LazyColumn
            }

            // ── حالتِ عادی (فریمِ `15a`) ───────────────────────────────────────────────
            item {
                TodaySpendHero(
                    period = effectivePeriod,
                    onPeriod = { heroPeriod = it },
                    periodLabel = heroSeries.label,
                    periodSpend = heroSeries.total,
                    todayIncome = todayIncome,
                    yesterdaySpend = weekSpend[weekSpend.lastIndex - 1],
                    todaySpend = weekSpend.last(),
                    weekSpend = heroSeries.bars,
                    privacyMode = privacyMode,
                    // `71d`: عددی که جلوی چشم است و لمس می‌شود ولی جواب نمی‌دهد یک بن‌بست
                    // است. مقصدش **شیت** است نه صفحه - یک نگاهِ دوثانیه‌ای، و زمینه
                    // (خودِ عددِ هیرو) بالای شیت می‌مانَد.
                    onClick = { showTodaySpend = true },
                    weekChangePercent = if (prevWeekTotal > 0.0) (((weekTotal - prevWeekTotal) / prevWeekTotal) * 100).toInt() else null,
                )
            }
            // 🚨 **چهار درِ همیشه‌درمعرض** (طرحِ مرجعِ کاربر). کشوی میان‌بر قدرتمندتر است
            // ولی باید کشیده شود؛ این ردیف بی هیچ کنشی هم **خبر** می‌دهد: تعدادِ حساب،
            // تعدادِ تراکنشِ ماه، تعدادِ چکِ باز. عدد همان چیزی است که کارتِ بی‌عدد ندارد.
            // ردیفِ چهار کاشی (حساب/تراکنش/گزارش/چک) به خواسته‌ی کاربر (۱۳ مهر) برداشته شد - تکراریِ نوارِ پایین.
            // «نیاز به توجه» (۸ مهر): قسطِ عقب‌افتاده **اول**، بعد بودجه/پیش‌بینی - زیرِ یک
            // عنوان، تا کارت‌های هشدار با هم رقابت نکنند و ترتیبِ اهمیت روشن باشد.
            if (urgentDue != null || (!simple && monthCap > 0.0 && monthSpend > monthCap)) {
                item {
                    Text(
                        "نیاز به توجه",
                        color = AppText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 4.dp, start = 2.dp),
                    )
                }
            }
            urgentDue?.let { due ->
                item {
                    UrgentDueCard(
                        title = "قسطِ ${due.loan.name}",
                        amount = due.amount,
                        daysOverdue = due.daysOverdue,
                        privacyMode = privacyMode,
                        onPay = { confirmPayDue = due },
                        onOpen = { onOpenLoan(due.loan.id) },
                    )
                }
            }
            if (!simple && monthCap > 0.0) {
                item {
                    MonthBudgetCard(
                        monthLabel = persianMonthName(today.m),
                        spent = monthSpend,
                        cap = monthCap,
                        dayOfMonth = today.d,
                        daysInMonth = JalaliCalendar.daysInMonth(today.y, today.m),
                        privacyMode = privacyMode,
                        onClick = { onNavigateToRoute("budget") },
                    )
                }
            }
            // «تا آخرِ ماه کم میاری» - رجوع کن به MonthForecast. عمداً **بالای** کارت‌های
            // تحلیلی و زیرِ بودجه می‌شینه: یه هشدارِ عملیه، نه یه آمار.
            // ۱۴ مهر: وقتی کارتِ بودجه هست، کارتِ دومِ «بودجه تمام شده» تکراری و گاهی متناقض بود
            // (نوار ۵۰٪ ولی «تمام شده»). کارتِ بودجه خودش «با این روند کم میاری» را می‌گوید.
            if (!simple && monthCap <= 0.0) monthForecast?.let { forecast ->
                item {
                    if (forecast.willRunShort) {
                        ShortfallForecastCard(
                            runsOutOnDay = forecast.runsOutOnDay,
                            daysLeft = forecast.daysLeft,
                            perDaySpend = forecast.perDayRial,
                            balance = forecast.balanceRial,
                            safePerDay = forecast.safePerDayRial,
                            privacyMode = privacyMode,
                            onClick = { onNavigateToRoute("report") },
                        )
                    } else {
                        // حالتِ سالم **کارت نمی‌گیرد** (فریمِ `63b`) - یک خطِ آرام بس است؛
                        // کارتِ «همه‌چیز خوب است» فضای کارتِ هشدار را می‌گیرد و بی‌اثر است.
                        Text(
                            "با این سرعت تا آخرِ ماه می‌رسی",
                            color = AppMuted,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            // اگر همه‌ی پیشنهادها بسته شده باشند کارت چیزی نمی‌کشد، ولی خودِ آیتم ۱۳dp فاصله‌ی
            // اضافه می‌ساخت (فاصله‌ی دوبرابرِ بودجه تا «خرجِ این ماه»). پس اینجا چک می‌کنیم.
            if (!simple && smartInsights.any { !InsightDismissals.hidden(insightCtx, it.key) }) {
                item {
                    SmartInsightsCard(smartInsights) { ins ->
                        when (ins.kind) {
                            ir.sadteam.loancalc.core.SmartInsights.Kind.RECURRING,
                            ir.sadteam.loancalc.core.SmartInsights.Kind.SAVE_SURPLUS -> onNavigateToRoute("budget")
                            ir.sadteam.loancalc.core.SmartInsights.Kind.DUES_OVER_BALANCE -> onNavigateToRoute("due")
                            ir.sadteam.loancalc.core.SmartInsights.Kind.SALARY_MISSING -> onNavigateToRoute("assets")
                            else -> onNavigateToRoute("report")
                        }
                    }
                }
            }
            if (!simple && dueBills.isNotEmpty()) {
                item {
                    ir.sadteam.loancalc.ui.components.AppCard(modifier = Modifier.clickable { showHomeBills = true }) {
                        Text(
                            "🧾 " + if (dueBills.size == 1) "قبضِ ${ir.sadteam.loancalc.ui.extras.billKindLabel(dueBills[0].kind)} نزدیکِ موعده"
                            else "${dueBills.size.toFa()} قبض نزدیکِ موعدن",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                        )
                        Text(
                            dueBills.joinToString("، ") { it.name } + " · لمس کن و پرداخت‌شده بزن",
                            color = ir.sadteam.loancalc.ui.theme.AppMuted,
                            fontSize = 11.5.sp,
                        )
                    }
                }
            }
            if (!simple && monthSpend > 0.0) {
                item {
                    CategoryBreakdownCard(
                        byCategory = monthSpendByCategory,
                        total = monthSpend,
                        privacyMode = privacyMode,
                        onClick = { onNavigateToRoute("report") },
                    )
                }
            }
            // 🚨 **سه تراکنشِ آخر، ته صفحه** (خواسته‌ی کاربر با طرحِ مرجع، ۳۱ شهریور).
            // بالاتر نمی‌نشیند: کارت‌های بالای صفحه «وضعیت» را می‌گویند و این «تاریخچه»
            // است؛ و در حالتِ خالی اصلاً نمی‌آید تا صفحه‌ی اولِ کاربرِ تازه شلوغ نشود.
            if (transactions.isNotEmpty()) {
                item {
                    RecentTransactionsCard(
                        transactions = transactions,
                        privacyMode = privacyMode,
                        onSeeAll = onOpenTransactions,
                    )
                }
            }
            // «مرورِ هفته» حذف شد (۸ مهر): همان عددِ کارتِ سبزِ بالا بود؛ درصدِ تغییرش حالا
            // برچسبِ کوچکِ همان کارت است.
        }
        }

        // فریمِ `15b` دکمه‌ی شناور نداره: تو حالتِ خالی اقدامِ اصلی همون دکمه‌ی تمام‌عرضِ
        // «ثبتِ اولین خرج»ه و دو تا دکمه‌ی هم‌کار گیج‌کننده‌ست.
        if (transactions.isNotEmpty()) AppFab(
            onClick = { showNewTransaction = true },
            contentDescription = "ثبتِ تراکنش",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp)
                .then(Modifier.guideTarget("add_tx")),
        )

        if (showNewTransaction) {
            NewTransactionSheet(onDismiss = { showNewTransaction = false })
        }
        // نشانِ تازه بازگشتِ سیستمی نمی‌گیرد: باید دیده و تأیید شود، وگرنه بی‌صدا رد
        // می‌شود و کاربر هیچ‌وقت نمی‌فهمد چه گرفته.
        if (retroBadges.isNotEmpty()) {
            BadgeRetroSheet(retroBadges) { gamificationViewModel.consumeRetro() }
        }
        // تپ روی قرصِ سکه کیفِ سکه را باز می‌کند (خواسته‌ی صریحِ کاربر). به‌صورتِ روکش
        // رندر می‌شود نه با `return` - همان باگی که شش جای دیگر صفحه را سفید می‌کرد.
        // تا وقتی سکه مقصدِ `NavHost` نشده، این کوتاه‌ترین راهِ درست است؛ روکش پس‌زمینه‌ی
        // مات دارد پس صفحه‌ی زیرش دیده نمی‌شود.
        // جشنِ سر زدنِ روزانه - روزی یک‌بار (۸ مهر).
        ir.sadteam.loancalc.ui.coin.DailyCheckInHost()
        if (showCoinWallet) {
            CoinHubScreen(onBack = { showCoinWallet = false }, todayHasEntry = todayHasEntry)
        }
        if (showProfile) {
            // آدمکِ سربرگ → صفحه‌ی «حسابِ کاربری» (خواسته‌ی کاربر، ۳ مهر). نشان‌ها از تنظیمات در دسترس‌اند.
            // 🚨 خودِ صفحه‌ی تنظیمات پس‌زمینه ندارد (در کشوی قدیمی داخلِ Surface بود)؛ بی این روکشِ مات،
            // خانه از پشتش دیده می‌شد و لمس‌ها به کارت‌های زیرش می‌رسید (گزارشِ کاربر، ۱۶ مهر).
            androidx.activity.compose.BackHandler { showProfile = false }
            androidx.compose.material3.Surface(
                color = ir.sadteam.loancalc.ui.theme.AppBg,
                modifier = Modifier.fillMaxSize(),
            ) {
                ir.sadteam.loancalc.ui.settings.SettingsScreen(onBack = { showProfile = false }, startAtAccount = false)
            }
        }
        if (showTodaySpend) {
            TodaySpendSheet(
                transactions = transactions,
                accountNameOf = { id -> accounts.firstOrNull { it.id == id }?.name },
                onDismiss = { showTodaySpend = false },
            )
        }
        pickPayAccountFor?.let { due ->
            ir.sadteam.loancalc.ui.components.AccountPickerDialog(
                accounts = accounts,
                onSelect = { acc -> urgentDueViewModel.markPaid(due, acc.id); pickPayAccountFor = null },
                onDismiss = { pickPayAccountFor = null },
            )
        }
        confirmPayDue?.let { due ->
            ConfirmPayDialog(
                title = "تاییدِ پرداخت",
                text = "این قسط پرداخت‌شده علامت بخوره؟",
                onConfirm = {
                    // بیش از یک حساب ← بپرس از کدام؛ یکی ← همان؛ هیچ ← فقط علامت می‌خورد.
                    if (accounts.size > 1) pickPayAccountFor = due else urgentDueViewModel.markPaid(due, null)
                    confirmPayDue = null
                },
                onDismiss = { confirmPayDue = null },
            )
        }
    }
}
/**
 * شیتِ «خرجِ امروز» - فریمِ `71d`.
 *
 * **واریزهای امروز این‌جا نمی‌آیند**: عددِ هیرو «خرج» است نه تراز، و فهرستی که واریز هم
 * داشته باشد با آن عدد نمی‌خواند.
 *
 * ردیف همان ردیفِ `71a` است با یک چیزِ اضافه: **نامِ حساب پیشِ منبع** - خرجِ امروز چند
 * حساب را قطع می‌کند و بی نامِ حساب دو ردیفِ هم‌مبلغ از هم جدا نمی‌شوند. سرگروهِ روز هم
 * نیست، همه‌اش امروز است.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodaySpendSheet(
    transactions: List<AccountTransactionEntity>,
    accountNameOf: (Long) -> String?,
    onDismiss: () -> Unit,
) {
    val today = remember { JalaliCalendar.today() }
    val rows = remember(transactions, today) {
        transactions.netDangShares().filter { it.isExpenseOn(today.y, today.m, today.d) }
            .sortedByDescending { it.createdAt }
    }
    val total = remember(rows) { rows.sumOf { it.amount } }
    val privacyMode = LocalPrivacyMode.current
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = AppBg) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AppHeroCard {
                // شیتِ «خرجِ امروز» همیشه مالِ **امروز** است، مستقل از بازه‌ی کارتِ قهرمان -
                // ردیف‌های زیرش هم تراکنش‌های امروزند.
                Text("خرجِ امروز", color = HeroMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        maskIfPrivate(masked, rialToToman(total.toLong()).toFaMoney()) + " تومان",
                        color = Color.White,
                        fontSize = 26.sp,
                        letterSpacing = (-0.5).sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
            if (rows.isEmpty()) {
                Text(
                    "امروز هنوز خرجی ثبت نشده.",
                    color = AppMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            } else {
                rows.forEach { tx ->
                    CompactTransactionRow(tx = tx, accountName = accountNameOf(tx.accountId))
                }
            }
        }
    }
}
/** خرجِ همون روز؟ (واریز خرج نیست.) */
private fun AccountTransactionEntity.isExpenseOn(y: Int, m: Int, d: Int): Boolean =
    type != "DEPOSIT" && year == y && month == m && day == d &&
        ir.sadteam.loancalc.data.countsInReports(this)
