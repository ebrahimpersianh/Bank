package ir.sadteam.loancalc.ui.asset

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
) {
    val accounts by accountViewModel.accounts.collectAsState()
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

    val openAsset = assets.firstOrNull { it.id == detailAsset }
    val openAccount = accounts.firstOrNull { it.id == detailAccount }
    val openEditAccount = accounts.firstOrNull { it.id == editAccount }

    BackHandler(
        enabled = openAsset != null || openAccount != null || openEditAccount != null ||
            showAddAsset || showAddAccount || showAccountList || showPrices,
    ) {
        when {
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
                item { StarterAssetTiles(onPick = { showAddAsset = true }) }
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
                            val index = groupKeys.indexOf(key)
                            if (index >= 0) scope.launch {
                                listState.animateScrollToItem(
                                    // دو آیتمِ ثابتِ بالا (سرصفحه، هیرو) + برچسبِ حساب‌ها و ردیف‌هاش
                                    // + دو آیتم به‌ازای هر گروهِ قبلی. مستقیم از ساختارِ همین لیست
                                    // شمرده شده؛ اگه آیتمی بالا اضافه شد، اینجا هم عوض بشه.
                                    index = 2 +
                                        (if (accounts.isEmpty()) 0 else 1 + accounts.size) +
                                        index * 2,
                                )
                            }
                        }
                    },
                )
            }

            if (accounts.isNotEmpty()) {
                item { SectionLabel("حساب‌های بانکی") }
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

            assetGroupOrder.forEach { (category, title) ->
                val rows = byCategory[category].orEmpty()
                if (rows.isEmpty()) return@forEach
                item(key = "h_$category") {
                    GroupHeader(
                        title = title,
                        // null یعنی هیچ‌کدام قیمت ندارند - «۰» غلط بود و کاربر «ارز ۰» می‌دید
                        // در حالی که یک دلار داشت.
                        total = rows.mapNotNull { it.value }.takeIf { it.isNotEmpty() }?.sum(),
                        profit = rows.mapNotNull { it.profit }.takeIf { it.isNotEmpty() }?.sum(),
                        privacyMode = privacyMode,
                    )
                }
                item(key = "g_$category") {
                    AssetGroup(
                        category = category,
                        rows = rows,
                        privacyMode = privacyMode,
                        onOpen = { asset -> detailAsset = asset.id },
                    )
                }
            }
            if (holdings.isEmpty()) {
                item { StarterAssetTiles(onPick = { showAddAsset = true }) }
            }
        }

        // زیرصفحه‌ها **روی** تب می‌نشینند، نه به‌جایش.
        if (openAsset != null) {
            Box(modifier = Modifier.fillMaxSize().background(AppSurface)) {
                AssetDetailScreen(
                    asset = openAsset,
                    onBack = { detailAsset = null },
                    viewModel = assetViewModel,
                )
            }
        }
        if (openAccount != null) {
            Box(modifier = Modifier.fillMaxSize().background(AppSurface)) {
                AccountDetailScreen(
                    account = openAccount,
                    onBack = { detailAccount = null },
                    // ویرایش هست، حذف نیست: کاربر همین‌جا می‌بینه اسم/فرستنده‌ی پیامک غلطه و
                    // باید بتونه درستش کنه، ولی حذف از مسیرِ تماشا جای درستی نیست.
                    onEdit = { editAccount = openAccount.id },
                    onDelete = null,
                    viewModel = accountViewModel,
                )
            }
        }
        if (openEditAccount != null) {
            Box(modifier = Modifier.fillMaxSize().background(AppSurface)) {
                AddEditAccountScreen(
                    existing = openEditAccount,
                    onSaved = { editAccount = null },
                    onCancel = { editAccount = null },
                    viewModel = accountViewModel,
                )
            }
        }
        if (showAccountList) {
            Box(modifier = Modifier.fillMaxSize().background(AppSurface)) {
                AccountsScreen(
                    onBack = { showAccountList = false },
                    startInAddMode = false,
                    viewModel = accountViewModel,
                )
            }
        }
        if (showAddAccount) {
            Box(modifier = Modifier.fillMaxSize().background(AppSurface)) {
                AccountsScreen(
                    onBack = { showAddAccount = false },
                    startInAddMode = true,
                    viewModel = accountViewModel,
                )
            }
        }
        if (showAddAsset) {
            Box(modifier = Modifier.fillMaxSize().background(AppSurface)) {
                AssetTradeSheet(onDismiss = { showAddAsset = false }, viewModel = assetViewModel)
            }
        }
        if (showPrices) {
            Box(modifier = Modifier.fillMaxSize().background(AppSurface)) {
                MarketPricesScreen(onBack = { showPrices = false }, viewModel = assetViewModel)
            }
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
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("دارایی", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Black)
        if (!showActions) return@Row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeaderSquareButton(
                icon = if (privacyMode) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                description = "پنهان‌کردنِ مبلغ‌ها",
                fill = if (privacyMode) AppWarningPill else AppIconFrame,
                border = if (privacyMode) AppAssetBorder else AppLine,
                ink = if (privacyMode) AppWarningInk else AppMuted,
                onClick = onTogglePrivacy,
            )
            HeaderSquareButton(
                icon = Icons.Filled.TrendingUp,
                description = "قیمتِ روز",
                fill = AppIconFrame,
                border = AppLine,
                ink = AppMuted,
                onClick = onPrices,
            )
            HeaderSquareButton(
                icon = Icons.Filled.Add,
                description = "افزودنِ دارایی",
                fill = AppPrimaryPill,
                border = AppPrimaryBorder,
                ink = AppPrimaryInk,
                onClick = onAdd,
            )
        }
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

/** «یا اینها را ثبت کن» - سه کاشیِ طلا / ارز / رمز ارز. «نقد» رفت: کارتِ «افزودنِ حساب» بالایش
 * همون کار رو می‌کنه - دو راهِ هم‌معنی تو یه صفحه لازم نبود. */
@Composable
private fun StarterAssetTiles(onPick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        SectionLabel("یا اینها را ثبت کن")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StarterTile("طلا", AppWarningPill, modifier = Modifier.weight(1f), onClick = onPick) {
                CoinIcon(17.dp)
            }
            StarterTile("ارز", AppIconFrame, modifier = Modifier.weight(1f), onClick = onPick) {
                AssetBadge("USD", ASSET_CATEGORY_FIAT, 19.dp)
            }
            StarterTile("رمز ارز", AppIconFrame, modifier = Modifier.weight(1f), onClick = onPick) {
                AssetBadge("BTC", ASSET_CATEGORY_CRYPTO, 19.dp)
            }
        }
    }
}

@Composable
private fun StarterTile(
    label: String,
    iconBg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AppSurface)
            .border(2.dp, AppLineRow, RoundedCornerShape(16.dp))
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 13.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            modifier = Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(iconBg),
            contentAlignment = Alignment.Center,
        ) { icon() }
        Text(label, color = AppText, fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold)
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
                Text(
                    "داراییِ کل",
                    color = HeroMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
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
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    maskIfPrivate(masked, total.rialToFaCompact()),
                    color = Color.White,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
            // واحد تو کارتِ خلاصه میاد - قاعده‌ی عددِ TOKENS.md، مثلِ AccountsTotalHero.
            Text(
                "تومان",
                color = HeroMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp),
            )
            // نمودارِ روندِ نقدی - فقط وقتی داده‌ی واقعی هست. در حالتِ خصوصی هم می‌مانَد:
            // شکلِ روند مبلغ لو نمی‌دهد، و همان چیزی است که کارت برایش ساخته شده.
            if (trend.size >= 2 && trend.any { it != trend.first() }) {
                TrendLineChart(
                    values = trend,
                    // خواسته‌ی کاربر: دو روز داده = **دو نقطه در جای واقعی‌شان** روی محورِ ۳۰روزه
                    // (سمتِ راست)، نه یک خطِ کشیده از این سر تا آن سر.
                    slots = 30,
                    lineColor = Color.White,
                    // پررنگ‌تر از ۰٫۲۲ی قبلی - خواسته‌ی کاربر: «زیرِ این خط انگار پر باشد».
                    fillTop = Color.White.copy(alpha = 0.38f),
                    dotColor = Color.White,
                    modifier = Modifier.padding(top = 10.dp),
                    // برچسبِ هر نقطه = «N روز پیش»/«امروز». همین است که لمس را معنادار
                    // می‌کند؛ بی آن `TrendLineChart` خودش را غیرِتعاملی می‌گیرد.
                    labels = trend.indices.map { index ->
                        val ago = trend.lastIndex - index
                        if (ago == 0) "امروز" else "${ago.toFa()} روز پیش"
                    },
                    valueLabel = { value -> "${value.rialToFaCompact()} تومان" },
                )
                Text(
                    // صادقانه: تا وقتی عکسِ روزانه جمع نشده، نمودار فقط نقد را می‌گوید.
                    if (trendIsReal) "روندِ ۳۰ روزِ گذشته" else "روندِ نقدیِ ۳۰ روزِ گذشته",
                    color = HeroMuted,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            // پنج قرص تو یه ردیفِ عادی جا نمی‌شن؛ FlowRow خطِ دوم می‌سازه.
            FlowRow(
                modifier = Modifier.padding(top = 12.dp),
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
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppSurface)
            .border(2.dp, AppLine, shape)
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
    ) {
        if (account.type == ACCOUNT_TYPE_BANK) {
            BankBadge(bankName = account.bankName, size = 38.dp)
        } else {
            Box(
                modifier = Modifier.size(38.dp).clip(CircleShape).background(AppIconFrame),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    accountIconForKey(account.iconKey),
                    contentDescription = null,
                    tint = AppMuted,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                account.name,
                color = AppText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val meta = account.smsSender?.takeIf { it.isNotBlank() }?.let { "پیامک $it" }
                ?: account.bankName.ifBlank { null }
                ?: "منبعِ نقدی"
            Text(meta, color = AppMuted, fontSize = 9.sp, modifier = Modifier.padding(top = 2.dp))
        }
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                maskIfPrivate(masked, balance.rialToFaCompact()),
                // موجودیِ منفیِ کارتِ اعتباری وضعِ عادیه نه خطا: فقط عدد قرمز می‌شه.
                color = if (balance < 0) AppDangerInk else AppText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
            )
        }
        Icon(
            Icons.Filled.ChevronLeft,
            contentDescription = null,
            tint = AppLine,
            modifier = Modifier.size(14.dp),
        )
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
