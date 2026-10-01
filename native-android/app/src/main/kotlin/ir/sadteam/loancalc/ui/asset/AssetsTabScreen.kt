package ir.sadteam.loancalc.ui.asset

import ir.sadteam.loancalc.ui.components.guideTarget
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import ir.sadteam.loancalc.ui.components.SubScreen
import ir.sadteam.loancalc.ui.components.AppFab
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import ir.sadteam.loancalc.ui.components.AppCard
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.heightIn
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.data.AssetCatalogEntry
import ir.sadteam.loancalc.data.assetCatalogGroups
import ir.sadteam.loancalc.ui.components.HeroChart
import ir.sadteam.loancalc.ui.components.HeroChartStyle
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.Spacer
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianCalendar
import ir.sadteam.loancalc.data.accountIconForKey
import ir.sadteam.loancalc.data.db.ACCOUNT_TYPE_BANK
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_CRYPTO
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_CUSTOM
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_FIAT
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_GOLD
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.db.AssetEntity
import ir.sadteam.loancalc.ui.account.AccountDetailScreen
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.account.AccountsScreen
import ir.sadteam.loancalc.ui.account.AddEditAccountScreen
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.HeroExpense
import ir.sadteam.loancalc.ui.components.HeroIncome
import ir.sadteam.loancalc.ui.components.TrendLineChart
import ir.sadteam.loancalc.ui.components.BankBadge
import ir.sadteam.loancalc.ui.components.CoinIcon
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.dashedBorder
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.jibak.rialToFaSignedCompact
import ir.sadteam.loancalc.ui.jibak.toFa
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.PrivacyModeViewModel
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppAssetBorder
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryBorder
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import ir.sadteam.loancalc.ui.theme.AppWarningPill
import ir.sadteam.loancalc.ui.theme.hardShadow
import kotlinx.coroutines.launch

/**
 * تبِ **دارایی** - فریمِ `26b` / `26bd`، با چهار دسته‌ی جدا (نقد از حساب‌ها، طلا/ارز/رمز ارز/سایر
 * از `assetGroupOrder`).
 *
 * ```
 * ۱ عنوان + کلیدِ خصوصی + قیمتِ روز + دکمه‌ی +
 * ۲ هیرویِ سبز: داراییِ کل + واحد + قرص‌های کلیک‌پذیرِ نقد/طلا/ارز/رمز ارز/سایر
 * ۳ «حساب‌های بانکی» + ردیفِ هر حساب - کلیک‌پذیر
 * ۴ هر دسته‌ی دارایی، جدا: سرگروه (جمع+سود) + کارتِ گروه
 * ```
 *
 * زیرصفحه‌ها **روی** تب می‌نشینند نه به‌جایش - قبلاً با `return` صدا زده می‌شدن و صفحه‌ی زیرین
 * اصلاً رندر نمی‌شد (پشتِ شیت سفیدِ خالی بود و اسکرولِ LazyColumn با بستنش صفر می‌شد).
 * `BackHandler` هر شش لایه رو با ترتیبِ درست می‌گیره: بازترین لایه اول بسته می‌شه و
 * `showPrices` (که خودش سه لایه‌ی تودرتو داره) **آخر**.
 */
private val idSaver = androidx.compose.runtime.saveable.Saver<Long?, Long>(
    save = { it ?: -1L },
    restore = { if (it == -1L) null else it },
)

@Composable
fun AssetsTabScreen(
    accountViewModel: AccountViewModel = hiltViewModel(),
    assetViewModel: AssetViewModel = hiltViewModel(),
    privacyViewModel: PrivacyModeViewModel = hiltViewModel(),
    statsViewModel: ir.sadteam.loancalc.ui.stats.StatsViewModel = hiltViewModel(),
    debtViewModel: ir.sadteam.loancalc.ui.debt.DebtViewModel = hiltViewModel(),
) {
    val accounts by accountViewModel.accounts.collectAsState()
    // بدهی‌ها (پیشنهادِ گزارشِ پولکی): اقساطِ پرداخت‌نشده‌ی وام‌ها + بدهی‌های تسویه‌نشده به دیگران.
    // «کلِ دارایی» دست نمی‌خورد؛ «خالص» جدا زیرش می‌آید.
    val loans by statsViewModel.loans.collectAsState()
    val debts by debtViewModel.debts.collectAsState()
    var loanRemaining by remember { mutableStateOf(0.0) }
    LaunchedEffect(loans) { loanRemaining = statsViewModel.summarize(loans).remainingAmount }
    // خالصِ هر طرف‌حساب (با پرداخت‌های بخشی)، فقط آن‌هایی که من بدهکارم.
    val iOwe = debts.filter { !it.settled }.groupBy { it.counterpartyId }.values.sumOf { rows ->
        (-rows.sumOf { if (it.type == ir.sadteam.loancalc.core.DebtType.OWED_TO_ME.name) it.amount else -it.amount }).coerceAtLeast(0.0)
    }
    val liabilities = loanRemaining + iOwe
    val transactions by accountViewModel.transactions.collectAsState()
    val assets by assetViewModel.assets.collectAsState()
    val trades by assetViewModel.trades.collectAsState()
    val privacyMode = LocalPrivacyMode.current

    var showAddAsset by remember { mutableStateOf(false) }
    // دو پرچمِ جدا و عمدی: `showAddAccount` فرمِ **بازِ** افزودنه، `showAccountList` لیستِ
    // انتخاب. یکی‌کردنشون یعنی کاربرِ چندحسابی که رو قرصِ «نقد» زده به فرمِ افزودن پرت بشه.
    var showAddAccount by remember { mutableStateOf(false) }
    var showAccountList by remember { mutableStateOf(false) }
    var showPrices by remember { mutableStateOf(false) }
    var detailAsset by rememberSaveable(stateSaver = idSaver) { mutableStateOf<Long?>(null) }
    var detailAccount by rememberSaveable(stateSaver = idSaver) { mutableStateOf<Long?>(null) }
    var editAccount by rememberSaveable(stateSaver = idSaver) { mutableStateOf<Long?>(null) }

    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf<String?>(null) }
    var buyEntry by remember { mutableStateOf<AssetCatalogEntry?>(null) }
    val changes by assetViewModel.monthChange.collectAsState()
    val updatedClock by assetViewModel.pricesUpdatedClock.collectAsState()
    val openAsset = assets.firstOrNull { it.id == detailAsset }
    val openAccount = accounts.firstOrNull { it.id == detailAccount }
    val openEditAccount = accounts.firstOrNull { it.id == editAccount }

    BackHandler(
        enabled = openAsset != null || openAccount != null || openEditAccount != null ||
            showAddAsset || showAddAccount || showAccountList || showPrices || buyEntry != null,
    ) {
        when {
            buyEntry != null -> buyEntry = null
            showAddAsset -> showAddAsset = false
            showAddAccount -> showAddAccount = false
            showAccountList -> showAccountList = false
            openEditAccount != null -> editAccount = null
            openAccount != null -> detailAccount = null
            openAsset != null -> detailAsset = null
            // آخر، چون خودش سه لایه‌ی داخلی داره که BackHandlerِ خودش می‌بنده.
            else -> showPrices = false
        }
    }

    // امروزِ جلالی - مبنای نمودارِ روند.
    val today = remember { JalaliCalendar.today() }
    val cashTotal = remember(accounts, transactions) {
        accounts.sumOf { acc -> accountViewModel.balanceOf(acc, transactions) }
    }
    val marketPrices by assetViewModel.marketPrices.collectAsState()
    // marketPrices کلیدِ remember است: بی آن، داراییِ تازه‌ثبت‌شده تا رفتن و برگشتن به تب
    // «—» می‌ماند - همان چیزی که کاربر دید.
    val holdings = remember(assets, trades, marketPrices) {
        assets.map { asset ->
            val qty = trades.filter { it.assetId == asset.id }
                .sumOf { if (it.isBuy) it.quantity else -it.quantity }
            val spent = trades.filter { it.assetId == asset.id }
                .sumOf { if (it.isBuy) it.totalRial else -it.totalRial }
            AssetHolding(asset, qty, spent, assetViewModel.priceOf(asset)?.let { it * qty })
        }.filter { it.quantity > 0.0 }
    }
    val byCategory = remember(holdings) { holdings.groupBy { it.asset.category } }
    val totalOf: (String) -> Double = { cat -> byCategory[cat]?.sumOf { it.value ?: 0.0 } ?: 0.0 }
    val goldTotal = totalOf(ASSET_CATEGORY_GOLD)
    val fiatTotal = totalOf(ASSET_CATEGORY_FIAT)
    val cryptoTotal = totalOf(ASSET_CATEGORY_CRYPTO)
    val otherTotal = totalOf(ASSET_CATEGORY_CUSTOM)
    val grandTotal = cashTotal + goldTotal + fiatTotal + cryptoTotal + otherTotal
    // 🚨 **روندِ ۳۰ روزِ گذشته** (طرحِ مرجعِ کاربر، ۳۱ شهریور).
    //
    // رو به عقب از موجودیِ امروز ساخته می‌شود: موجودیِ دیروز = موجودیِ امروز منهای
    // اثرِ تراکنش‌های امروز. هیچ ستون یا جدولِ تاریخچه‌ای لازم نیست و عددِ امروز هم
    // همان `cashTotal` می‌مانَد، پس با کارت ناهماهنگ نمی‌شود.
    //
    // ⚠️ فقط **نقد** است نه کلِ دارایی: قیمتِ طلا و ارز در گذشته را نداریم و بازسازی‌اش
    // یعنی نموداری که عددهایش ساختگی است.
    // 🚨 **از امروز به بعد، روند از عکسِ روزانه می‌آید نه بازسازی** (خواسته‌ی کاربر،
    // ۳۱ شهریور: «قیمت‌ها را هر روز ذخیره کن»). تا وقتی کمتر از دو روز ردیف باشد،
    // همان بازسازیِ رو-به-عقبِ نقدی کار می‌کند - پس کاربرِ تازه هم نمودار دارد.
    val wealthSnapshots by assetViewModel.wealthTrend.collectAsState()
    val cashTrend = remember(transactions, cashTotal, today) {
        val days = (0 until 30).map { back -> PersianCalendar.addDays(today, -back) }
        var running = cashTotal
        val series = ArrayList<Double>(30)
        days.forEach { day ->
            series.add(running)
            val delta = transactions
                .filter { it.confirmed && it.year == day.y && it.month == day.m && it.day == day.d }
                .sumOf { if (it.type == "DEPOSIT") it.amount else -it.amount }
            running -= delta
        }
        series.reversed()
    }
    // سریِ نهایی: عکسِ واقعی اگر هست، وگرنه بازسازیِ نقدی.
    val trendSeries = remember(wealthSnapshots, cashTrend) {
        // ⚠️ عکسِ روزانه تا یک هفته جمع نشده، دو-سه نقطه بیشتر ندارد و نمودار یک خطِ صاف
        // می‌شد (گزارشِ کاربر: «نقطه ندارد»). تا آن موقع بازسازیِ ۳۰روزه‌ی نقدی بهتر است.
        if (wealthSnapshots.size >= 2) {
            wealthSnapshots.takeLast(30).map { it.totalRial }
        } else {
            cashTrend
        }
    }
    val trendIsReal = wealthSnapshots.size >= 2
    // درصدِ تغییر نسبت به ابتدای همان سری. مبنای صفر یعنی درصد بی‌معنی، پس `null`.
    val cashTrendPercent = remember(trendSeries) {
        val first = trendSeries.firstOrNull() ?: 0.0
        val last = trendSeries.lastOrNull() ?: 0.0
        if (kotlin.math.abs(first) > 0.0) (((last - first) / kotlin.math.abs(first)) * 100).toInt() else null
    }
    // ثبتِ عکسِ امروز. `marketPrices` در کلید هست تا وقتی قیمت‌ها رسیدند ردیف با
    // مقدارِ درست **به‌روز** شود، نه اینکه صفرِ لحظه‌ی اول بماند.
    val assetsValueNow = goldTotal + fiatTotal + cryptoTotal + otherTotal
    LaunchedEffect(cashTotal, assetsValueNow, marketPrices, today) {
        assetViewModel.recordWealthSnapshot(
            today = today,
            cashRial = cashTotal,
            assetsRial = assetsValueNow,
            // بی قیمت، ارزشِ دارایی صفر درمی‌آید و یک دره‌ی دروغ در نمودار می‌سازد.
            pricesReady = holdings.isEmpty() || marketPrices.isNotEmpty(),
        )
    }
    val nothingYet = accounts.isEmpty() && holdings.isEmpty()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    // کلیدِ گروه‌هایی که واقعاً تو لیست هستن، به ترتیبِ نمایش - مبنای اسکرولِ قرص‌ها.
    val groupKeys = remember(byCategory) {
        assetGroupOrder.mapNotNull { (cat, _) -> if (byCategory[cat].isNullOrEmpty()) null else cat }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 14.dp, 16.dp, 110.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            item {
                AssetsHeader(
                    privacyMode = privacyMode,
                    onTogglePrivacy = { privacyViewModel.toggle() },
                    onPrices = { showPrices = true },
                    onAdd = { showAddAsset = true },
                    showActions = !nothingYet,
                )
            }
            if (nothingYet) {
                item { NoAccountCard(onAddAccount = { showAddAccount = true }) }
                return@LazyColumn
            }
            item {
                TotalWealthHero(
                    total = grandTotal,
                    cash = cashTotal,
                    gold = goldTotal,
                    fiat = fiatTotal,
                    crypto = cryptoTotal,
                    other = otherTotal,
                    trend = trendSeries,
                    trendIsReal = trendIsReal,
                    trendPercent = cashTrendPercent,
                    privacyMode = privacyMode,
                    onPill = { key ->
                        if (key == PILL_CASH) {
                            // پولِ نقد از حساب‌ها میاد، پس «اصلاحِ رقم» یعنی رفتن به حسابِ نقدی.
                            val cashAccounts = accounts.filter { it.type != ACCOUNT_TYPE_BANK }
                            when (cashAccounts.size) {
                                1 -> editAccount = cashAccounts.first().id  // مستقیم فرمِ ویرایش
                                0 -> showAddAccount = true                  // چیزی نیست، بساز
                                else -> showAccountList = true              // انتخاب لازمه
                            }
                        } else {
                            // قرص‌های دسته حذف شدند (خواسته‌ی کاربر، ۶ مهر)؛ فقط می‌رویم پایین سراغِ دارایی‌ها.
                            scope.launch { listState.animateScrollToItem(3) }
                        }
                    },
                )
            }

            if (liabilities > 0.0) {
                item { NetWorthCard(grandTotal, liabilities, loanRemaining, iOwe, privacyMode) }
            }
            // حساب‌های بانکی بالای بازار (خواسته‌ی کاربر، ۷ مهر: «حساب بانکی بره بالا بعد نمودار»).
            if (accounts.isNotEmpty() && query.isBlank()) {
                item {
                    SectionHeader(
                        title = "حساب‌های بانکی",
                        icon = Icons.Filled.AccountBalance,
                        count = accounts.size,
                        actionLabel = "مدیریتِ حساب‌ها",
                        onAction = { showAccountList = true },
                    )
                }
                items(accounts.size) { index ->
                    val account = accounts[index]
                    AccountRow(
                        account = account,
                        balance = accountViewModel.balanceOf(account, transactions),
                        privacyMode = privacyMode,
                        onClick = { detailAccount = account.id },
                    )
                }
            }

            item { AssetSearchBar(query) { query = it } }

            val q = query.trim()
            val browsing = filter == null && q.isEmpty()
            fun matches(name: String, symbol: String, category: String) =
                (filter == null || category == filter) &&
                    (q.isEmpty() || name.contains(q, true) || symbol.contains(q, true))
            fun openEntry(e: AssetCatalogEntry) {
                val mine = assets.firstOrNull { it.symbol == e.symbol }
                if (mine != null) detailAsset = mine.id else buyEntry = e
            }

            if (browsing) {
                item {
                    MarketOverviewSection(marketPrices, assetViewModel, updatedClock, onOpen = ::openEntry)
                }
            }

            val myRows = holdings.filter { matches(it.asset.name, it.asset.symbol, it.asset.category) }
            if (myRows.isNotEmpty()) {
                item {
                    MyAssetsSection(
                        rows = myRows,
                        changes = changes,
                        privacyMode = privacyMode,
                        viewModel = assetViewModel,
                        onSeeAll = { showPrices = true },
                        onOpen = { asset -> detailAsset = asset.id },
                    )
                }
            }
            // روندِ کلِ دارایی در طولِ زمان (۸ مهر) - آخرِ فهرست تا شاخصِ اسکرولِ بالا جابه‌جا نشود.
            if (browsing && wealthSnapshots.size >= 5) {
                item { WealthHistoryCard(wealthSnapshots, privacyMode) }
            }
        }

        // دکمه‌ی «+» شناور (طرحِ ChatGPT) - بالای نوارِ پایین.
        // همان FABِ خانه - همان شکل و همان گوشه (خواسته‌ی کاربر، ۶ مهر).
        if (!nothingYet) AppFab(
            onClick = { showAddAsset = true },
            contentDescription = "افزودنِ دارایی",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp),
        )
        SubScreen(buyEntry) { e ->
            AssetTradeSheet(
                onDismiss = { buyEntry = null },
                viewModel = assetViewModel,
                presetSymbol = e.symbol,
                presetName = e.name,
                presetCategory = e.category,
            )
        }

        // زیرصفحه‌ها **روی** تب می‌نشینند، نه به‌جایش.
        SubScreen(openAsset) { a ->
                AssetDetailScreen(
                    asset = a,
                    onBack = { detailAsset = null },
                    viewModel = assetViewModel,
                )
        }
        SubScreen(openAccount) { acc ->
                AccountDetailScreen(
                    account = acc,
                    onBack = { detailAccount = null },
                    // ویرایش هست، حذف نیست: کاربر همین‌جا می‌بینه اسم/فرستنده‌ی پیامک غلطه و
                    // باید بتونه درستش کنه، ولی حذف از مسیرِ تماشا جای درستی نیست.
                    onEdit = { editAccount = acc.id },
                    onDelete = null,
                    viewModel = accountViewModel,
                )
        }
        SubScreen(openEditAccount) { ed ->
                AddEditAccountScreen(
                    existing = ed,
                    onSaved = { editAccount = null },
                    onCancel = { editAccount = null },
                    viewModel = accountViewModel,
                )
        }
        SubScreen(if (showAccountList) Unit else null) { _ ->
                AccountsScreen(
                    onBack = { showAccountList = false },
                    startInAddMode = false,
                    viewModel = accountViewModel,
                )
        }
        SubScreen(if (showAddAccount) Unit else null) { _ ->
                AccountsScreen(
                    onBack = { showAddAccount = false },
                    startInAddMode = true,
                    viewModel = accountViewModel,
                )
        }
        SubScreen(if (showAddAsset) Unit else null) { _ ->
                ir.sadteam.loancalc.ui.subscription.PremiumBlock(
                    blocked = true, key = "assets", label = "ثبتِ دارایی", onBlocked = { showAddAsset = false },
                )
                AssetTradeSheet(onDismiss = { showAddAsset = false }, viewModel = assetViewModel)
        }
        SubScreen(if (showPrices) Unit else null) { _ ->
                MarketPricesScreen(onBack = { showPrices = false }, viewModel = assetViewModel)
        }
    }
}

/** کلیدِ قرصِ نقد. بقیه‌ی قرص‌ها کلیدشون نامِ دسته‌ست. */
private const val PILL_CASH = "cash"

/** یه دارایی به‌همراهِ مقدار و ارزشِ محاسبه‌شده‌ش. */
data class AssetHolding(
    val asset: AssetEntity,
    val quantity: Double,
    val spentRial: Double,
    val value: Double?,
) {
    val profit: Double? get() = value?.let { it - spentRial }
}

// ═══ ۱ · هدر ═══════════════════════════════════════════════════════════════════
@Composable
private fun AssetsHeader(
    privacyMode: Boolean,
    onTogglePrivacy: () -> Unit,
    onPrices: () -> Unit,
    onAdd: () -> Unit,
    showActions: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("دارایی", color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(
                "نمای کلیِ دارایی‌های شما",
                color = AppMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (!showActions) return@Row
        // سه کنشِ گرد با برچسبِ زیرش (طرحِ ChatGPT) - همان سه کارِ قبلی، فقط خواناتر.
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HeaderRoundAction(
                icon = if (privacyMode) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                label = if (privacyMode) "پنهان" else "نمایش",
                description = "پنهان‌کردنِ مبلغ‌ها",
                fill = if (privacyMode) AppWarningPill else AppIconFrame,
                ink = if (privacyMode) AppWarningInk else AppMuted,
                onClick = onTogglePrivacy,
            )
            HeaderRoundAction(
                icon = Icons.Filled.TrendingUp,
                label = "نمودار",
                description = "قیمتِ روز",
                fill = AppIconFrame,
                ink = AppMuted,
                onClick = onPrices,
            )
            HeaderRoundAction(
                icon = Icons.Filled.Add,
                label = "افزودن",
                description = "افزودنِ دارایی",
                fill = AppPrimaryPill,
                ink = AppPrimaryInk,
                onClick = onAdd,
            )
        }
    }
}

@Composable
private fun HeaderRoundAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    fill: Color,
    ink: Color,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.pressScaleClickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(fill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = description, tint = ink, modifier = Modifier.size(19.dp))
        }
        Text(
            label,
            color = AppMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/** `internal` چون `MarketPricesScreen` هم سرصفحه‌ی هم‌شکل می‌خواد. */
@Composable
internal fun HeaderSquareButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    fill: Color,
    border: Color,
    ink: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(AppRadius.icon))
            .background(fill)
            .border(1.5.dp, border, RoundedCornerShape(AppRadius.icon))
            .pressScaleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = ink, modifier = Modifier.size(16.dp))
    }
}

// ═══ ۱ب · حالتِ خالی (فریمِ `21a`) ═══════════════════════════════════════════════
@Composable
private fun NoAccountCard(onAddAccount: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.card))
            .background(AppSurface)
            .dashedBorder(AppRadius.card)
            .padding(horizontal = 16.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(modifier = Modifier.size(width = 140.dp, height = 88.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 7.dp)
                    .size(width = 74.dp, height = 46.dp)
                    .hardShadow(AppLineRow, 3.dp, 10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppSurface)
                    .border(2.dp, AppLine, RoundedCornerShape(10.dp)),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 9.dp, bottom = 9.dp)
                    .size(width = 74.dp, height = 46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppPrimaryPill)
                    .dashedBorder(10.dp, AppPrimaryBorder)
                    .then(Modifier.guideTarget("add_account"))
                    .pressScaleClickable(onClick = onAddAccount),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    tint = AppPrimary,
                    modifier = Modifier.size(21.dp),
                )
            }
            CoinIcon(26.dp, Modifier.align(Alignment.TopEnd).padding(end = 25.dp))
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "هنوز حسابی اضافه نکردی",
                color = AppText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Text(
                "با اضافه‌کردنِ حسابِ بانکی، موجودی و خرج‌هایت خودکار از پیامک خوانده می‌شود.",
                color = AppMuted,
                fontSize = 12.5.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            "افزودنِ حساب",
            color = Color.White,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .hardShadow(ir.sadteam.loancalc.ui.theme.AppPrimaryDim, 4.dp, AppRadius.button)
                .clip(RoundedCornerShape(AppRadius.button))
                .background(AppPrimary)
                .pressScaleClickable(onClick = onAddAccount)
                .padding(vertical = 15.dp),
        )
    }
}

// ═══ ۲ · هیرویِ داراییِ کل ══════════════════════════════════════════════════════
/** گرادیان/سایه/گوشه/هاله همه داخلِ `AppHeroCard`ن - همون‌جا با توکنِ تیره درست می‌چرخن. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TotalWealthHero(
    total: Double,
    cash: Double,
    gold: Double,
    fiat: Double,
    crypto: Double,
    other: Double,
    trend: List<Double>,
    trendIsReal: Boolean,
    trendPercent: Int?,
    privacyMode: Boolean,
    onPill: (String) -> Unit,
) {
    AppHeroCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp),
                    )
                }
                Text(
                    "ارزشِ کلِ دارایی‌ها",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).padding(start = 9.dp),
                )
                if (trendPercent != null && trendPercent != 0) {
                    val up = trendPercent > 0
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                    ) {
                        Icon(
                            if (up) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            contentDescription = null,
                            tint = if (up) HeroIncome else HeroExpense,
                            modifier = Modifier.size(10.dp),
                        )
                        Text(
                            "${kotlin.math.abs(trendPercent).toFa()}٪ نسبت به ماهِ قبل",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }
            // مبلغ و «تومان» در یک خط (خواسته‌ی کاربر: کارت هم‌قدِ کارتِ خانه، نه کشیده).
            Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.Bottom) {
                val shownTotal = ir.sadteam.loancalc.ui.components.countUpDouble(total)
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        maskIfPrivate(masked, shownTotal.rialToFaCompact()),
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
                // واحد تو کارتِ خلاصه میاد - قاعده‌ی عددِ TOKENS.md، مثلِ AccountsTotalHero.
                Text(
                    "تومان",
                    color = HeroMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 6.dp, bottom = 6.dp),
                )
            }
            // نمودارِ روندِ نقدی - فقط وقتی داده‌ی واقعی هست. در حالتِ خصوصی هم می‌مانَد:
            // شکلِ روند مبلغ لو نمی‌دهد، و همان چیزی است که کارت برایش ساخته شده.
            if (trend.size >= 2 && trend.any { it != trend.first() }) {
                HeroChart(
                    values = trend,
                    // دو روز داده = دو نقطه در جای واقعی‌شان روی محورِ ۳۰روزه (خواسته‌ی کاربر).
                    slots = 30,
                    natural = HeroChartStyle.LINE,
                    currentIndex = trend.lastIndex,
                    height = 38.dp,
                    modifier = Modifier.padding(top = 6.dp),
                    labels = trend.indices.map { index ->
                        val ago = trend.lastIndex - index
                        if (ago == 0) "امروز" else "${ago.toFa()} روز پیش"
                    },
                    valueLabel = { value -> "${value.rialToFaCompact()} تومان" },
                )
                // برچسبِ دو سرِ محور، مثلِ کارتِ خانه - جای قرصِ بزرگِ قبلی که کارت را بلند می‌کرد.
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Text("امروز", color = HeroMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.weight(1f))
                    Text(
                        // صادقانه: تا وقتی عکسِ روزانه جمع نشده، نمودار فقط نقد را می‌گوید.
                        if (trendIsReal) "۳۰ روزِ گذشته" else "نقدِ ۳۰ روزِ گذشته",
                        color = HeroMuted,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            // پنج قرص تو یه ردیفِ عادی جا نمی‌شن؛ FlowRow خطِ دوم می‌سازه.
            FlowRow(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (cash > 0) HeroPill("نقد", cash, privacyMode) { onPill(PILL_CASH) }
                if (gold > 0) HeroPill("طلا", gold, privacyMode) { onPill(ASSET_CATEGORY_GOLD) }
                if (fiat > 0) HeroPill("ارز", fiat, privacyMode) { onPill(ASSET_CATEGORY_FIAT) }
                if (crypto > 0) HeroPill("رمز ارز", crypto, privacyMode) { onPill(ASSET_CATEGORY_CRYPTO) }
                if (other > 0) HeroPill("سایر", other, privacyMode) { onPill(ASSET_CATEGORY_CUSTOM) }
            }
        }
    }
}

/**
 * قرصِ تفکیک. «نقد» می‌بره به فرمی که رقمش اونجا عوض می‌شه؛ بقیه به سرگروهِ خودشون اسکرول می‌کنن.
 * `onClick`ِ null یعنی بازخوردِ لمس هم نداره - قرصِ بی‌مقصد نباید کلیک‌پذیر به‌نظر بیاد.
 */
@Composable
private fun HeroPill(
    label: String,
    value: Double,
    privacyMode: Boolean,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(AppRadius.button))
            .background(Color.White.copy(alpha = 0.2f))
            .then(if (onClick != null) Modifier.pressScaleClickable(onClick = onClick) else Modifier)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                "$label ${maskIfPrivate(masked, value.rialToFaCompact())}",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
            )
        }
    }
}

// ═══ ۳ · حساب‌های بانکی ═════════════════════════════════════════════════════════
@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        color = AppMuted,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(top = 4.dp),
    )
}

/**
 * ردیفِ حساب - کلیک‌پذیر.
 *
 * ⚠️ `bankName` **رشته‌ی خالی** است نه `null` (فرم برای منبعِ غیربانکی `""`
 * می‌نویسد)، پس `?:` هیچ‌وقت آتش نمی‌کرد و حسابِ نقدی بجِ خالی می‌گرفت. حالا رو
 * `type` تصمیم می‌گیریم و `iconKey`ی که کاربر انتخاب کرده استفاده می‌شه.
 */
@Composable
private fun AccountRow(
    account: AccountEntity,
    balance: Double,
    privacyMode: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, shape, clip = false)
            .clip(shape)
            .background(AppSurface)
            .border(1.dp, AppLine, shape)
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) {
        if (account.type == ACCOUNT_TYPE_BANK) {
            BankBadge(bankName = account.bankName, size = 40.dp)
        } else {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(AppIconFrame),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    accountIconForKey(account.iconKey),
                    contentDescription = null,
                    tint = AppMuted,
                    modifier = Modifier.size(21.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                account.name,
                color = AppText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val meta = account.smsSender?.takeIf { it.isNotBlank() }?.let { "پیامک $it" }
                ?: account.bankName.ifBlank { null }
                ?: "منبعِ نقدی"
            Text(
                meta,
                color = AppMuted,
                fontSize = 10.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("موجودی", color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    maskIfPrivate(masked, balance.rialToFaCompact()) + " تومان",
                    // موجودیِ منفیِ کارتِ اعتباری وضعِ عادیه نه خطا: فقط عدد قرمز می‌شه.
                    color = if (balance < 0) AppDangerInk else AppText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            // چهار رقمِ آخرِ کارت، اگر ثبت شده - بقیه ستاره (شماره‌ی کامل هیچ‌جا نشان داده نمی‌شود).
            val digits = account.cardNumber?.filter { it.isDigit() }.orEmpty()
            if (digits.length >= 8) {
                Text(
                    "${digits.takeLast(4).faNum()} **** **** ${digits.take(4).faNum()}",
                    color = AppMuted,
                    fontSize = 9.5.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Box(
            modifier = Modifier.size(26.dp).clip(CircleShape).background(AppIconFrame),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.ChevronLeft,
                contentDescription = null,
                tint = AppMuted,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/** سرِ بخش (طرحِ ChatGPT): کاشیِ آیکون + عنوان + شمارنده، و قرصِ کنش در سمتِ چپ. */
@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    count: Int? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier.size(30.dp).clip(RoundedCornerShape(10.dp)).background(AppPrimaryPill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = AppPrimaryInk, modifier = Modifier.size(16.dp))
            }
        }
        Text(
            title,
            color = AppText,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = if (icon != null) 8.dp else 0.dp),
        )
        if (count != null) {
            Text(
                count.toFa(),
                color = AppPrimaryInk,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(CircleShape)
                    .background(AppPrimaryPill)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(AppIconFrame)
                    .pressScaleClickable(onClick = onAction)
                    .padding(start = 11.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
            ) {
                Text(actionLabel, color = AppText, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                Icon(
                    Icons.Filled.ChevronLeft,
                    contentDescription = null,
                    tint = AppMuted,
                    modifier = Modifier.padding(start = 4.dp).size(16.dp),
                )
            }
        }
    }
}

// ═══ ۴ · دسته‌های دارایی ═══════════════════════════════════════════════════════
/** سرگروه: عنوان + جمعِ همون دسته + سودِ همون دسته (طلا و ارز دیگه با هم جمع نمی‌شن). */
@Composable
private fun GroupHeader(title: String, total: Double?, profit: Double?, privacyMode: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, color = AppMuted, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                maskIfPrivate(masked, total?.rialToFaCompact() ?: "—"),
                color = if (total == null) AppMuted else AppText,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f),
            )
        }
        if (profit != null) {
            Text(
                profit.rialToFaSignedCompact(),
                color = if (profit < 0) AppDangerInk else AppPrimaryInk,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

/** طلا داخلِ کاغذِ طلایی؛ بقیه ردیفِ ساده روی صفحه. `groupPalette` تصمیم می‌گیره. */
@Composable
private fun AssetGroup(
    category: String,
    rows: List<AssetHolding>,
    privacyMode: Boolean,
    onOpen: (AssetEntity) -> Unit,
) {
    val p = groupPalette(category)
    val body: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(if (p.paper != null) 9.dp else 8.dp)) {
            rows.forEach { HoldingRow(it, p, privacyMode, onOpen) }
        }
    }
    if (p.paper == null) {
        body()
    } else {
        val shape = RoundedCornerShape(AppRadius.card)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(p.paper)
                .border(1.5.dp, p.rowBorder, shape)
                .padding(12.dp),
        ) { body() }
    }
}

/**
 * ردیفِ موجودی. سه ستون: نشان · اسم و **قیمتِ روزِ واحد** · ارزشِ کل و سود.
 *
 * ⚠️ قیمتِ واحد ماسکِ خصوصی نمی‌گیره - قیمتِ اونسِ طلا داراییِ کاربر نیست، نرخِ
 * بازاره. ارزشِ کل و سود ماسک می‌گیرن، مثلِ قبل.
 */
@Composable
private fun HoldingRow(
    holding: AssetHolding,
    p: GroupPalette,
    privacyMode: Boolean,
    onOpen: (AssetEntity) -> Unit,
) {
    // ارزشِ ردیف از قبل با همان جانشین حساب شده، پس قیمتِ واحد را از آن برمی‌گردانیم
    // و به یک منبعِ دوم نیاز نیست.
    val unitPrice = holding.value?.takeIf { holding.quantity > 0.0 }?.div(holding.quantity)
    val shape = RoundedCornerShape(AppRadius.row)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(p.rowSurface)
            .border(if (p.paper != null) 1.dp else 2.dp, p.rowBorder, shape)
            .pressScaleClickable { onOpen(holding.asset) }
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) {
        AssetBadge(holding.asset.symbol, holding.asset.category, 32.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${formatQuantity(holding.quantity)} ${holding.asset.name}",
                color = p.ink,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                // قیمتِ روز از همان جانشینِ ViewModel می‌آید، پس ردیفِ تازه هم عدد دارد.
                unitPrice?.let { "قیمتِ روز ${it.rialToFaCompact()} تومان" } ?: "قیمتِ روز —",
                color = p.subInk,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Column(horizontalAlignment = Alignment.Start) {
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    maskIfPrivate(masked, holding.value?.rialToFaCompact() ?: "—"),
                    color = p.ink,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black,
                )
            }
            holding.profit?.let { profit ->
                Text(
                    profit.rialToFaSignedCompact(),
                    color = if (profit >= 0) AppPrimaryInk else AppDangerInk,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}

/** رقم‌های لاتین → فارسی، بی جداکننده‌ی هزارگان (برای شماره‌ی کارت). */
private fun String.faNum(): String = map { if (it in '0'..'9') '۰' + (it - '0') else it }.joinToString("")

// ═══ بازطراحیِ صفحه‌ی دارایی (بسته‌ی ChatGPT، ۳ مهر) ═════════════════════════════
// ترتیب: هیرو → جست‌وجو → قرصِ دسته → نمای کلیِ بازار → دارایی‌های من → حساب‌ها → بازار.
// همه‌ی عددها از داده‌ی واقعی (قیمتِ روز، تغییرِ ۳۰روزه، تاریخچه‌ی سرور) - هیچ عددِ طرح.
// شکلِ نمودارهای کوچک از همان سبکِ نمودارِ خریده‌شده در فروشگاه می‌آید (`HeroChart`).

/**
 * درصدِ تغییرِ **امروز** نسبت به آخرین قیمتِ ثبت‌شده‌ی روزهای قبل (خواسته‌ی کاربر، ۳ مهر).
 * از تاریخچه‌ی روزانه‌ی سرور + قیمتِ همین لحظه ساخته می‌شود؛ بی تاریخچه = `null` («—»).
 */
@Composable
internal fun rememberDailyChange(symbol: String, viewModel: AssetViewModel): Double? {
    val history by remember(symbol) { viewModel.historyOf(symbol) }.collectAsState(initial = emptyList())
    val prices by viewModel.marketPrices.collectAsState()
    val today = remember { JalaliCalendar.today() }
    val prev = history.lastOrNull { !(it.year == today.y && it.month == today.m && it.day == today.d) }?.priceRial
    val cur = prices[symbol] ?: history.lastOrNull()?.priceRial
    return if (prev != null && cur != null && prev > 0.0) (cur - prev) / prev * 100.0 else null
}

/** نمودارِ کوچکِ روندِ یک نماد - هرچقدر تاریخچه هست؛ کمتر از ۲ نقطه = خطِ صاف. */
@Composable
internal fun MiniTrend(symbol: String, change: Double?, viewModel: AssetViewModel, modifier: Modifier = Modifier, height: androidx.compose.ui.unit.Dp = 26.dp) {
    val history by remember(symbol) { viewModel.historyOf(symbol) }.collectAsState(initial = emptyList())
    val ink = if ((change ?: 0.0) < 0.0) AppDangerInk else AppPrimaryInk
    HeroChart(
        values = history.map { it.priceRial },
        labels = emptyList(),
        valueLabel = { "" },
        currentIndex = (history.size - 1).coerceAtLeast(0),
        natural = HeroChartStyle.LINE,
        modifier = modifier,
        height = height,
        ink = ink,
    )
}

@Composable
private fun AssetSearchBar(value: String, onChange: (String) -> Unit) {
    val shape = RoundedCornerShape(999.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .clip(shape)
            .background(AppSurface)
            .border(1.dp, AppLine, shape)
            .padding(horizontal = 16.dp),
    ) {
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(color = AppText, fontSize = 12.5.sp),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(AppPrimary),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text("جستجو (مثلاً دلار، طلا، بیت‌کوین…)", color = AppMuted, fontSize = 12.sp)
                }
                inner()
            },
        )
        Icon(
            Icons.Filled.Search,
            contentDescription = null,
            tint = AppMuted,
            modifier = Modifier.padding(start = 8.dp).size(20.dp),
        )
    }
}

/** سرگروهِ «نمای کلیِ بازار» / «دارایی‌های من» / «بازار» با «مشاهده‌ی همه». */
@Composable
private fun MarketSectionTitle(title: String, onSeeAll: (() -> Unit)?) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
        if (onSeeAll != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.heightIn(min = 44.dp).pressScaleClickable(onClick = onSeeAll).padding(horizontal = 4.dp),
            ) {
                Text("مشاهده‌ی همه", color = AppPrimaryInk, fontSize = 11.sp, fontWeight = FontWeight.Black)
                Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = AppPrimaryInk, modifier = Modifier.size(16.dp))
            }
        }
    }
}

/**
 * سه کارتِ طلا/دلار/بیت‌کوین. «مشاهده‌ی همه» همین‌جا باز می‌شود: زیرِ طلا بقیه‌ی طلا و سکه،
 * زیرِ دلار بقیه‌ی ارزها، زیرِ بیت‌کوین بقیه‌ی رمزارزها (خواسته‌ی کاربر، ۶ مهر). پیش‌فرض بسته.
 */
@Composable
private fun MarketOverviewSection(
    prices: Map<String, Double>,
    viewModel: AssetViewModel,
    updatedClock: String?,
    onOpen: (AssetCatalogEntry) -> Unit,
) {
    val all = remember { assetCatalogGroups.flatMap { it.second } }
    val picks = listOf("GOLD_18", "USD", "BTC").mapNotNull { s -> all.firstOrNull { it.symbol == s } }
    var expanded by rememberSaveable { mutableStateOf(false) }
    AppCard(contentPadding = 12.dp) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("نمای کلیِ بازار", color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Black)
                if (updatedClock != null) {
                    Text("به‌روزرسانی در ساعتِ $updatedClock", color = AppMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.heightIn(min = 44.dp).pressScaleClickable { expanded = !expanded }.padding(horizontal = 4.dp),
            ) {
                Text(if (expanded) "بستن" else "مشاهده‌ی همه", color = AppPrimaryInk, fontSize = 11.sp, fontWeight = FontWeight.Black)
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = AppPrimaryInk,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(top = 8.dp).animateContentSize(),
        ) {
            picks.forEach { pick ->
                Box(modifier = Modifier.weight(1f)) { MarketMiniCard(pick, prices, viewModel, onOpen) }
            }
        }
        // «مشاهده‌ی همه»: به‌جای سه ستونِ ناهم‌قد، ردیف‌های مستطیلیِ تمام‌عرض، گروه‌به‌گروه
        // (خواسته‌ی کاربر، ۷ مهر: «به‌جای مربع مستطیل»).
        if (expanded) {
            Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                picks.forEach { pick ->
                    val rest = all.filter { it.category == pick.category && it.symbol != pick.symbol && prices[it.symbol] != null }
                    if (rest.isNotEmpty()) {
                        Text(
                            when (pick.symbol) { "GOLD_18" -> "طلا و سکه"; "USD" -> "ارز"; else -> "رمزارز" },
                            color = AppMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                        rest.forEach { MarketWideRow(it, prices, viewModel, onOpen) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarketMiniCard(
    e: AssetCatalogEntry,
    prices: Map<String, Double>,
    viewModel: AssetViewModel,
    onOpen: (AssetCatalogEntry) -> Unit,
) {
    val change = rememberDailyChange(e.symbol, viewModel)
    val shape = RoundedCornerShape(16.dp)
    val usd by viewModel.usdPrices.collectAsState()
    // هر سه کارت **هم‌قد** (بسته‌ی ChatGPT، ۷ مهر): ارتفاعِ ثابت و جای خطِ دوم (دلارِ رمزارز)
    // همیشه نگه داشته می‌شود، حتی خالی - وگرنه بیت‌کوین از دو کارتِ دیگر بلندتر می‌شد.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(shape)
            .border(1.dp, AppLine, shape)
            .pressScaleClickable { onOpen(e) }
            .padding(9.dp),
    ) {
        AssetBadge(e.symbol, e.category, 30.dp)
        Text(
            e.name,
            color = AppText,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            (prices[e.symbol]?.rialToFaCompact() ?: "—") + " تومان",
            color = AppText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            usd[e.symbol]?.takeIf { e.category == ASSET_CATEGORY_CRYPTO }?.let { "$" + formatUsd(it) } ?: "",
            color = AppMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.height(14.dp),
        )
        Spacer(Modifier.weight(1f))
        MiniTrend(e.symbol, change, viewModel, modifier = Modifier.fillMaxWidth(), height = 22.dp)
        Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), contentAlignment = Alignment.CenterEnd) {
            change?.let { PriceChangeBadge(it) }
        }
    }
}

/** دلار با ارقامِ فارسی: بزرگ‌ها با جداکننده، ریزها (مثلِ PEPE) با رقم‌های معنادار. */
internal fun formatUsd(v: Double): String {
    val raw = when {
        v >= 1000 -> String.format(java.util.Locale.US, "%,.0f", v).replace(',', '٬')
        v >= 1 -> String.format(java.util.Locale.US, "%.2f", v)
        else -> java.math.BigDecimal(v).round(java.math.MathContext(3)).stripTrailingZeros().toPlainString()
    }
    return raw.replace('.', '٫').map { if (it in '0'..'9') '۰' + (it - '0') else it }.joinToString("")
}

/** «دارایی‌های من» - ردیفِ فشرده: نشان | نام و نماد | نمودار | ارزش و تغییر | فلش. */
@Composable
private fun MyAssetsSection(
    rows: List<AssetHolding>,
    changes: Map<String, Double>,
    privacyMode: Boolean,
    viewModel: AssetViewModel,
    onSeeAll: () -> Unit,
    onOpen: (AssetEntity) -> Unit,
) {
    AppCard(contentPadding = 12.dp) {
        MarketSectionTitle("دارایی‌های من", onSeeAll)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            rows.forEach { h ->
                val change = rememberDailyChange(h.asset.symbol, viewModel)
                val shape = RoundedCornerShape(18.dp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 76.dp)
                        .clip(shape)
                        .border(1.dp, AppLine, shape)
                        .pressScaleClickable { onOpen(h.asset) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    AssetBadge(h.asset.symbol, h.asset.category, 40.dp)
                    Column(modifier = Modifier.weight(1.2f).padding(start = 10.dp)) {
                        Text(
                            "${formatQuantity(h.quantity)} ${h.asset.name}",
                            color = AppText,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Ltr { Text(h.asset.symbol.removePrefix("CUSTOM_"), color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                    }
                    MiniTrend(h.asset.symbol, change, viewModel, modifier = Modifier.weight(1f).padding(horizontal = 6.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        PrivacyCrossfade(privacyMode) { masked ->
                            Text(
                                maskIfPrivate(masked, h.value?.rialToFaCompact() ?: "—"),
                                color = AppText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                            )
                        }
                        Text("تومان", color = AppMuted, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                        change?.let { PriceChangeBadge(it, modifier = Modifier.padding(top = 2.dp)) }
                    }
                    Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = AppMuted, modifier = Modifier.padding(start = 4.dp).size(18.dp))
                }
            }
        }
    }
}


/** «خالص پس از بدهی‌ها» - دارایی منهای اقساطِ باقی‌مانده و بدهی به دیگران. */
@Composable
private fun NetWorthCard(total: Double, liabilities: Double, loans: Double, owe: Double, privacyMode: Boolean) {
    val net = total - liabilities
    AppCard(contentPadding = 12.dp) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("خالص پس از بدهی‌ها", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black)
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        buildString {
                            if (loans > 0) append("اقساطِ مانده ").append(maskIfPrivate(masked, loans.rialToFaCompact()))
                            if (loans > 0 && owe > 0) append(" · ")
                            if (owe > 0) append("بدهی ").append(maskIfPrivate(masked, owe.rialToFaCompact()))
                        },
                        color = AppMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    maskIfPrivate(masked, (if (net < 0) "−" else "") + kotlin.math.abs(net).rialToFaCompact()) + " تومان",
                    color = if (net < 0) AppDanger else AppPrimaryInk,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}

/** ردیفِ مستطیلیِ بازار - همان [MarketGridRow]ِ صفحه‌ی «قیمتِ روز». */
@Composable
private fun MarketWideRow(
    e: AssetCatalogEntry,
    prices: Map<String, Double>,
    viewModel: AssetViewModel,
    onOpen: (AssetCatalogEntry) -> Unit,
) {
    val change = rememberDailyChange(e.symbol, viewModel)
    val usd by viewModel.usdPrices.collectAsState()
    MarketGridRow(
        symbol = e.symbol,
        category = e.category,
        name = e.name,
        price = prices[e.symbol]?.rialToFaCompact(),
        secondLine = usd[e.symbol]?.takeIf { e.category == ASSET_CATEGORY_CRYPTO }?.let { "$" + formatUsd(it) },
        trend = { MiniTrend(e.symbol, change, viewModel, modifier = Modifier.fillMaxWidth(), height = 22.dp) },
        trailing = { change?.let { PriceChangeBadge(it) } },
        onClick = { onOpen(e) },
    )
}

/**
 * ردیفِ هم‌ترازِ بازار (بسته‌ی ChatGPT، ۷ مهر) - **ستون‌های ثابت**: نشان · نام/نماد · نمودار ·
 * قیمت در قابِ ملایم · درصد. عرضِ ثابتِ سه ستونِ آخر یعنی همه‌ی نمودارها و قیمت‌ها زیرِ هم.
 */
@Composable
internal fun MarketGridRow(
    symbol: String,
    category: String,
    name: String,
    price: String?,
    onClick: () -> Unit,
    subtitle: String? = null,
    secondLine: String? = null,
    owned: Boolean = false,
    trend: @Composable () -> Unit,
    trailing: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(shape)
            .background(AppSurface)
            .border(1.dp, AppLine, shape)
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AssetBadge(symbol, category, 32.dp)
        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    name,
                    color = AppText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (owned) Box(modifier = Modifier.padding(start = 4.dp).size(6.dp).clip(CircleShape).background(AppPrimary))
            }
            if (subtitle != null) {
                Ltr { Text(subtitle, color = AppMuted, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(top = 2.dp)) }
            }
        }
        Box(modifier = Modifier.width(58.dp).padding(horizontal = 4.dp), contentAlignment = Alignment.Center) { trend() }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(92.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AppPrimary.copy(alpha = 0.06f))
                .padding(horizontal = 4.dp, vertical = 6.dp),
        ) {
            // «۶۹۰٫۹ میلیون تو…» نصفه می‌شد - حالا فونت کوچک می‌شود تا کلِ قیمت جا شود.
            ir.sadteam.loancalc.ui.components.AutoShrinkText(
                text = price?.let { "$it تومان" } ?: "—",
                color = if (price == null) AppMuted else AppText,
                maxFontSize = 11.sp,
                minFontSize = 7.sp,
                fontWeight = FontWeight.Black,
            )
            if (secondLine != null) Text(secondLine, color = AppMuted, fontSize = 8.5.sp, maxLines = 1)
        }
        Box(modifier = Modifier.width(62.dp).padding(start = 6.dp), contentAlignment = Alignment.Center) { trailing() }
    }
}
