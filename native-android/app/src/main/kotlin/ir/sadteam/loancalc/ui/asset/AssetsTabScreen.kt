package ir.sadteam.loancalc.ui.asset

import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowForward
import ir.sadteam.loancalc.ui.components.guideTarget
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import ir.sadteam.loancalc.ui.components.SubScreen
import ir.sadteam.loancalc.ui.components.AppFab
import ir.sadteam.loancalc.ui.components.AppCard
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.heightIn
import ir.sadteam.loancalc.data.AssetCatalogEntry
import ir.sadteam.loancalc.data.assetCatalogGroups
import androidx.compose.material.icons.filled.AccountBalance
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianCalendar
import ir.sadteam.loancalc.data.db.ACCOUNT_TYPE_BANK
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_CRYPTO
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_CUSTOM
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_FIAT
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_GOLD
import ir.sadteam.loancalc.data.db.AssetEntity
import ir.sadteam.loancalc.ui.account.AccountDetailScreen
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.account.AccountsScreen
import ir.sadteam.loancalc.ui.account.AddEditAccountScreen
import ir.sadteam.loancalc.ui.components.CoinIcon
import ir.sadteam.loancalc.ui.components.dashedBorder
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyModeViewModel
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryBorder
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
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
    // ۱۶ مهر: نوارِ جستجو یک ردیفِ کامل می‌گرفت؛ حالا پشتِ ذره‌بینِ سربرگ است.
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var showWealthDetail by rememberSaveable { mutableStateOf(false) }
    var filter by rememberSaveable { mutableStateOf<String?>(null) }
    var buyEntry by remember { mutableStateOf<AssetCatalogEntry?>(null) }
    // «+»: اول فهرستِ «کدام دارایی؟» (یک صفحه‌ی جدا)، بعد صفحه‌ی خرید. برگشت از خرید دوباره فهرست را
    // نشان می‌دهد (۱۶ مهر؛ قبلاً فهرست زیرِ صفحه‌ی خرید مدفون می‌ماند).
    var buyFromChooser by remember { mutableStateOf(false) }
    fun closeBuy() {
        buyEntry = null
        if (buyFromChooser) { buyFromChooser = false; showAddAsset = true }
    }
    val changes by assetViewModel.monthChange.collectAsState()
    val updatedClock by assetViewModel.pricesUpdatedClock.collectAsState()
    // هر بار که تب باز می‌شود قیمت‌ها را دوباره بپرس (قبلاً فقط نیم‌ساعتی یک‌بار).
    LaunchedEffect(Unit) { assetViewModel.refreshPrices() }
    val openAsset = assets.firstOrNull { it.id == detailAsset }
    val openAccount = accounts.firstOrNull { it.id == detailAccount }
    val openEditAccount = accounts.firstOrNull { it.id == editAccount }

    BackHandler(
        enabled = openAsset != null || openAccount != null || openEditAccount != null ||
            showAddAsset || showAddAccount || showAccountList || showPrices || buyEntry != null,
    ) {
        when {
            buyEntry != null -> closeBuy()
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
            item(key = "header") {
                AssetsHeader(
                    privacyMode = privacyMode,
                    onTogglePrivacy = { privacyViewModel.toggle() },
                    onPrices = { showPrices = true },
                    searching = searchOpen,
                    onSearch = {
                        searchOpen = !searchOpen
                        if (!searchOpen) query = ""
                    },
                    showActions = !nothingYet,
                )
            }
            if (nothingYet) {
                item { NoAccountCard(onAddAccount = { showAddAccount = true }, formOpen = showAddAccount) }
                return@LazyColumn
            }
            item(key = "hero") {
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
                    onOpen = if (wealthSnapshots.size >= 5) ({ showWealthDetail = true }) else null,
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

            // کارتِ «خالص پس از بدهی‌ها» به خواسته‌ی کاربر برداشته شد (۱۴ مهر).
            // حساب‌های بانکی بالای بازار (خواسته‌ی کاربر، ۷ مهر: «حساب بانکی بره بالا بعد نمودار»).
            if (accounts.isNotEmpty() && query.isBlank()) {
                item(key = "accounts-header") {
                    SectionHeader(
                        title = "حساب‌های بانکی",
                        icon = Icons.Filled.AccountBalance,
                        count = accounts.size,
                        actionLabel = "مدیریتِ حساب‌ها",
                        onAction = { showAccountList = true },
                    )
                }
                items(accounts.size, key = { "acc-${accounts[it].id}" }) { index ->
                    val account = accounts[index]
                    AccountRow(
                        account = account,
                        balance = accountViewModel.balanceOf(account, transactions),
                        privacyMode = privacyMode,
                        onClick = { detailAccount = account.id },
                    )
                }
            }

            // 🐞 (۱۵ مهر) کلیدِ ثابت: بی کلید، با اولین حرف فهرستِ حساب‌ها بالای این ردیف حذف می‌شد،
            // جای ردیف عوض می‌شد و کادر از نو ساخته می‌شد - کیبورد می‌رفت.
            if (searchOpen || query.isNotEmpty()) item(key = "search") { AssetSearchBar(query) { query = it } }

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
            // 🔎 (۱۵ مهر) جستجو قبلاً فقط دارایی‌های خودت را می‌گشت؛ حالا کلِ بازار (طلا، ارز، رمزارز) و حساب‌ها هم.
            if (q.isNotEmpty()) {
                val mine = myRows.map { it.asset.symbol }.toSet()
                val market = assetCatalogGroups.flatMap { it.second }
                    .filter { it.symbol !in mine && matches(it.name, it.symbol, it.category) }
                val accHits = accounts.filter { it.name.contains(q, true) || it.bankName.contains(q, true) }
                accHits.forEach { acc ->
                    item(key = "s-acc-${acc.id}") {
                        AccountRow(
                            account = acc,
                            balance = accountViewModel.balanceOf(acc, transactions),
                            privacyMode = privacyMode,
                            onClick = { detailAccount = acc.id },
                        )
                    }
                }
                market.forEach { e ->
                    item(key = "s-mkt-${e.symbol}") {
                        AppCard(modifier = Modifier.pressScaleClickable { openEntry(e) }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(e.name, color = AppText, fontSize = 13.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                                marketPrices[e.symbol]?.let { p ->
                                    Text("${p.rialToFaCompact()} تومان", color = AppMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                if (myRows.isEmpty() && market.isEmpty() && accHits.isEmpty()) {
                    item(key = "s-none") {
                        Text("چیزی با «$q» پیدا نشد.", color = AppMuted, fontSize = 12.sp, modifier = Modifier.fillMaxWidth().padding(16.dp))
                    }
                }
            }
            // روندِ کلِ دارایی در طولِ زمان (۸ مهر) - آخرِ فهرست تا شاخصِ اسکرولِ بالا جابه‌جا نشود.
            // ۱۶ مهر: کارتِ جدا حذف شد (نمودارِ بالا را تکرار می‌کرد)؛ با زدنِ کارتِ بالا باز می‌شود.
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
                onDismiss = { closeBuy() },
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
                    onEdit = { editAccount = acc.id },
                    // خواسته‌ی کاربر (۱۰ مهر): حذف هم باشد - پنجره‌ی تأیید دارد.
                    onDelete = { accountViewModel.deleteAccount(acc); detailAccount = null },
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
                AssetPickerSheet(
                    prices = marketPrices,
                    changes = changes,
                    selectedSymbol = null,
                    onPick = { entry -> showAddAsset = false; buyFromChooser = true; buyEntry = entry },
                    onDismiss = { showAddAsset = false },
                )
        }
        SubScreen(if (showWealthDetail) Unit else null) { _ ->
            androidx.activity.compose.BackHandler { showWealthDetail = false }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ir.sadteam.loancalc.ui.theme.AppBg)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.IconButton(onClick = { showWealthDetail = false }) {
                        Icon(Icons.Filled.ArrowForward, "بازگشت", tint = AppText)
                    }
                    Text("روندِ کلِ دارایی", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
                WealthHistoryCard(wealthSnapshots, privacyMode)
            }
        }
        SubScreen(if (showPrices) Unit else null) { _ ->
                MarketPricesScreen(onBack = { showPrices = false }, viewModel = assetViewModel)
        }
    }
}
/** کلیدِ قرصِ نقد. بقیه‌ی قرص‌ها کلیدشون نامِ دسته‌ست. */
internal const val PILL_CASH = "cash"
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
    searching: Boolean = false,
    onSearch: () -> Unit = {},
    showActions: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = ir.sadteam.loancalc.ui.components.PageHeaderHeight),
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
        // سربرگِ یکدست (۱۵ مهر): دکمه‌های هم‌شکلِ همه‌ی صفحه‌ها؛ چشمِ مبلغ چپ‌ترین.
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ir.sadteam.loancalc.ui.components.HeaderIconButton(
                icon = Icons.Filled.Search,
                description = "جستجوی دارایی",
                onClick = onSearch,
                active = searching,
            )
            ir.sadteam.loancalc.ui.components.HeaderIconButton(
                icon = Icons.Filled.TrendingUp,
                description = "قیمتِ روز",
                onClick = onPrices,
            )
            ir.sadteam.loancalc.ui.components.PrivacyEyeHeaderButton(privacyMode = privacyMode, onToggle = onTogglePrivacy)
            // «افزودن» اینجا نیست: دکمه‌ی شناورِ «+» همین کار را می‌کند (تکراری بود، ۱۵ مهر).
        }
    }
}
@Composable
internal fun HeaderRoundAction(
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
private fun NoAccountCard(onAddAccount: () -> Unit, formOpen: Boolean = false) {
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
                    // وقتی فرمِ افزودن باز است، نشانه‌ی راهنما برداشته می‌شود؛ وگرنه کادرِ سبز روی فرم می‌ماند (۱۰ مهر).
                    .then(if (formOpen) Modifier else Modifier.guideTarget("add_account"))
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
