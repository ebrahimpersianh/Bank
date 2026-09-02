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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import ir.sadteam.loancalc.ui.components.BankBadge
import ir.sadteam.loancalc.ui.components.CoinIcon
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.dashedBorder
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.toFaCompact
import ir.sadteam.loancalc.ui.jibak.toFaSignedCompact
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

/**
 * تبِ **دارایی** - فریمِ `26b` / `26bd`، با چهار دسته‌ی جدا (نقد از حساب‌ها، طلا/ارز/رمزارز/سایر
 * از `assetGroupOrder`).
 *
 * ```
 * ۱ عنوان + کلیدِ خصوصی + دکمه‌ی +
 * ۲ هیرویِ سبز: داراییِ کل + قرص‌های نقد/طلا/ارز/رمزارز/سایر (فقط آن‌هایی که صفر نیستند)
 * ۳ «حساب‌های بانکی» + ردیفِ هر حساب - کلیک‌پذیر
 * ۴ هر دسته‌ی دارایی، جدا: سرگروه (جمع+سود) + کارتِ گروه
 * ```
 *
 * زیرصفحه‌ها **روی** تب می‌نشینند نه به‌جایش - قبلاً با `return` صدا زده می‌شدن و صفحه‌ی زیرین
 * اصلاً رندر نمی‌شد (پشتِ شیت سفیدِ خالی بود و اسکرولِ LazyColumn با بستنش صفر می‌شد).
 * `BackHandler` هر چهار لایه رو با ترتیبِ درست می‌گیره (فرمِ ویرایش روی جزئیاتِ حساب می‌شینه،
 * پس اول بسته می‌شه).
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
    var showAddAccount by remember { mutableStateOf(false) }
    var detailAsset by rememberSaveable(stateSaver = idSaver) { mutableStateOf<Long?>(null) }
    var detailAccount by rememberSaveable(stateSaver = idSaver) { mutableStateOf<Long?>(null) }
    var editAccount by rememberSaveable(stateSaver = idSaver) { mutableStateOf<Long?>(null) }

    val openAsset = assets.firstOrNull { it.id == detailAsset }
    val openAccount = accounts.firstOrNull { it.id == detailAccount }
    val openEditAccount = accounts.firstOrNull { it.id == editAccount }

    BackHandler(
        enabled = openAsset != null || openAccount != null || openEditAccount != null ||
            showAddAsset || showAddAccount,
    ) {
        when {
            showAddAsset -> showAddAsset = false
            showAddAccount -> showAddAccount = false
            openEditAccount != null -> editAccount = null
            openAccount != null -> detailAccount = null
            else -> detailAsset = null
        }
    }

    val cashTotal = remember(accounts, transactions) {
        accounts.sumOf { acc -> accountViewModel.balanceOf(acc, transactions) }
    }
    val holdings = remember(assets, trades) {
        assets.map { asset ->
            val qty = trades.filter { it.assetId == asset.id }
                .sumOf { if (it.isBuy) it.quantity else -it.quantity }
            val spent = trades.filter { it.assetId == asset.id }
                .sumOf { if (it.isBuy) it.totalRial else -it.totalRial }
            AssetHolding(asset, qty, spent, asset.unitPriceRial?.let { it * qty })
        }.filter { it.quantity > 0.0 }
    }
    val byCategory = remember(holdings) { holdings.groupBy { it.asset.category } }
    val totalOf: (String) -> Double = { cat -> byCategory[cat]?.sumOf { it.value ?: 0.0 } ?: 0.0 }
    val goldTotal = totalOf(ASSET_CATEGORY_GOLD)
    val fiatTotal = totalOf(ASSET_CATEGORY_FIAT)
    val cryptoTotal = totalOf(ASSET_CATEGORY_CRYPTO)
    val otherTotal = totalOf(ASSET_CATEGORY_CUSTOM)
    val grandTotal = cashTotal + goldTotal + fiatTotal + cryptoTotal + otherTotal
    val nothingYet = accounts.isEmpty() && holdings.isEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 14.dp, 16.dp, 110.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            item {
                AssetsHeader(
                    privacyMode = privacyMode,
                    onTogglePrivacy = { privacyViewModel.toggle() },
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
                    privacyMode = privacyMode,
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
                        total = rows.sumOf { it.value ?: 0.0 },
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
    }
}

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

@Composable
private fun HeaderSquareButton(
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
    privacyMode: Boolean,
) {
    AppHeroCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("داراییِ کل", color = HeroMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    maskIfPrivate(masked, total.toLong().toFaCompact()),
                    color = Color.White,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
            // پنج قرص تو یه ردیفِ عادی جا نمی‌شن؛ FlowRow خطِ دوم می‌سازه.
            FlowRow(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (cash > 0) HeroPill("نقد", cash, privacyMode)
                if (gold > 0) HeroPill("طلا", gold, privacyMode)
                if (fiat > 0) HeroPill("ارز", fiat, privacyMode)
                if (crypto > 0) HeroPill("رمز ارز", crypto, privacyMode)
                if (other > 0) HeroPill("سایر", other, privacyMode)
            }
        }
    }
}

@Composable
private fun HeroPill(label: String, value: Double, privacyMode: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(AppRadius.button))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                "$label ${maskIfPrivate(masked, value.toLong().toFaCompact())}",
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
                maskIfPrivate(masked, balance.toLong().toFaCompact()),
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
private fun GroupHeader(title: String, total: Double, profit: Double?, privacyMode: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, color = AppMuted, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                maskIfPrivate(masked, total.toLong().toFaCompact()),
                color = AppText,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f),
            )
        }
        if (profit != null) {
            Text(
                profit.toLong().toFaSignedCompact(),
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
                holding.asset.unitPriceRial
                    ?.let { "قیمتِ روز ${it.toLong().toFaCompact()}" }
                    ?: "قیمتِ روز —",
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
                    maskIfPrivate(masked, holding.value?.toLong()?.toFaCompact() ?: "—"),
                    color = p.ink,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black,
                )
            }
            holding.profit?.let { profit ->
                Text(
                    profit.toLong().toFaSignedCompact(),
                    color = if (profit >= 0) AppPrimaryInk else AppDangerInk,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}

/** پوششِ سازگاری برای صدازننده‌های قدیمی («۹٫۲M») - خودِ فرمول تو `toFaCompact` یکی شده. */
internal fun compact(value: Double): String = value.toLong().toFaCompact()
