package ir.sadteam.loancalc.ui.asset

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_CRYPTO
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_FIAT
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_GOLD
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.db.AssetEntity
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.account.AccountsScreen
import ir.sadteam.loancalc.ui.components.dashedBorder
import ir.sadteam.loancalc.ui.components.CoinIcon
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.PrivacyModeViewModel
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppWarningPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.hardShadow

/**
 * تبِ **دارایی** - بازسازیِ کاملِ فریمِ `26b`.
 *
 * ⚠️ **از نو نوشته شده.** نسخه‌ی قبلی دو نمای جدا با یه تاگل بود («حساب‌کتاب‌ها» / «دارایی‌ها»)؛
 * فریم **یک صفحه‌ی پیوسته**ست:
 *
 * ```
 * ۱ عنوان + کلیدِ خصوصی + دکمه‌ی +
 * ۲ هیرویِ سبز: داراییِ کل + سه قرصِ نقد/طلا/ارز
 * ۳ «حساب‌های بانکی» + ردیفِ هر حساب
 * ۴ «طلا و ارز» - کارتِ گروهیِ طلایی با سودِ کل
 * ۵ هدف‌های پس‌انداز
 * ```
 *
 * لیستِ کارت‌ها:
 * `design-frames.py "design/Duolingo Redesign.dc.html" 26b`
 *
 * کلیدِ خصوصی طبقِ `design/ANSWERS-section-37.md` بندِ ۳ اینجاست (و تو گزارش) - «همان دو
 * صفحه‌ای که کارشان نشان‌دادنِ موجودی است».
 */
@Composable
fun AssetsTabScreen(
    onOpenAsset: (AssetEntity) -> Unit = {},
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
    if (showAddAccount) {
        AccountsScreen(
            onBack = { showAddAccount = false },
            startInAddMode = true,
            viewModel = accountViewModel,
        )
        return
    }
    if (showAddAsset) {
        AssetTradeSheet(onDismiss = { showAddAsset = false }, viewModel = assetViewModel)
        return
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
    val goldTotal = holdings.filter { it.asset.category == ASSET_CATEGORY_GOLD }.sumOf { it.value ?: 0.0 }
    val fxTotal = holdings.filter {
        it.asset.category == ASSET_CATEGORY_FIAT || it.asset.category == ASSET_CATEGORY_CRYPTO
    }.sumOf { it.value ?: 0.0 }
    val grandTotal = cashTotal + goldTotal + fxTotal

    // نه حسابی، نه دارایی‌ای → **فریمِ `21a`**: فقط عنوان، کارتِ خط‌چین و سه کاشیِ شروع.
    // کارتِ قهرمانِ صفر عمداً نشون داده نمی‌شه (فریمِ خالی اصلاً هیرو نداره).
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
                    fx = fxTotal,
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
                    )
                }
            }

            if (holdings.isNotEmpty()) {
                item { SectionLabel("طلا و ارز") }
                item {
                    GoldAndCurrencyCard(
                        holdings = holdings,
                        privacyMode = privacyMode,
                        onOpen = onOpenAsset,
                    )
                }
            } else {
                item { StarterAssetTiles(onPick = { showAddAsset = true }) }
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
    /** سود/زیان - تا وقتی قیمتِ روز نیومده `null`ه. */
    val profit: Double? get() = value?.let { it - spentRial }
}

// ═══ ۱ · هدر ═══════════════════════════════════════════════════════════════════
/**
 * عنوانِ ۱۸/۹۰۰ سمتِ راست، و سمتِ چپ **کلیدِ خصوصی** و **دکمه‌ی +** - هر دو ۳۲×۳۲ با گوشه‌ی ۱۰.
 *
 * رنگ‌های کلیدِ خصوصی از `ANSWERS-section-37.md` بندِ ۳:
 * خاموش `#F5F8F6`/حاشیه `#E3ECE7`/خط `#5b6a63` · روشن `#FFF1DC`/حاشیه `#F0CE9B`/خط `#B45F00`.
 */
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
        // تو حالتِ خالی چیزی برای پنهان‌کردن یا افزودن از این دو دکمه نیست - فریمِ `21a` هم
        // هدرش لخته و همه‌ی کار با دکمه‌ی «افزودنِ حساب»ِ خودِ کارت انجام می‌شه.
        if (!showActions) return@Row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeaderSquareButton(
                icon = if (privacyMode) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                description = "پنهان‌کردنِ مبلغ‌ها",
                fill = if (privacyMode) PrivacyOnBg else PrivacyOffBg,
                border = if (privacyMode) PrivacyOnBorder else AppLine,
                ink = if (privacyMode) PrivacyOnInk else AppMuted,
                onClick = onTogglePrivacy,
            )
            HeaderSquareButton(
                icon = Icons.Filled.Add,
                description = "افزودنِ دارایی",
                fill = AppPrimaryPill,
                border = AppPrimaryBorderLine,
                ink = AppPrimaryDim,
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
            .clip(RoundedCornerShape(10.dp))
            .background(fill)
            .border(1.5.dp, border, RoundedCornerShape(10.dp))
            .pressScaleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = ink, modifier = Modifier.size(16.dp))
    }
}

private val PrivacyOffBg = Color(0xFFF5F8F6)
private val PrivacyOnBg = Color(0xFFFFF1DC)
private val PrivacyOnBorder = Color(0xFFF0CE9B)
private val PrivacyOnInk = Color(0xFFB45F00)
private val AppPrimaryBorderLine = Color(0xFF9FE0BC)

// ═══ ۱ب · کارتِ خط‌چینِ «هنوز حسابی اضافه نکردی» (فریمِ `21a`) ══════════════════════
/**
 * تصویرِ کارت: **دو کارتِ حسابِ واقعی که یکی‌شان خط‌چین است - یعنی «جای خالیِ تو»**
 * (یادداشتِ خودِ فریم)، به‌علاوه‌ی یه سکه‌ی طلایی بالای سرشون.
 */
@Composable
private fun NoAccountCard(onAddAccount: () -> Unit) {
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
        Box(modifier = Modifier.size(width = 140.dp, height = 88.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 7.dp)
                    .size(width = 74.dp, height = 46.dp)
                    .hardShadow(CardShadowSoft, 3.dp, 10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppSurface)
                    .border(2.dp, CardBorderLine, RoundedCornerShape(10.dp)),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 9.dp, bottom = 9.dp)
                    .size(width = 74.dp, height = 46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AddTileBg)
                    .dashedBorder(10.dp, AddTileBorder)
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
            modifier = Modifier.padding(top = (-8).dp),
        )
        Text(
            "افزودنِ حساب",
            color = Color.White,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .hardShadow(HeroShadow, 4.dp, 999.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(AppPrimary)
                .pressScaleClickable(onClick = onAddAccount)
                .padding(vertical = 15.dp),
        )
    }
}

/** «یا اینها را ثبت کن» - سه کاشیِ طلا / ارز / نقد. */
@Composable
private fun StarterAssetTiles(onPick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        SectionLabel("یا اینها را ثبت کن")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StarterTile("طلا", AppWarningPill, modifier = Modifier.weight(1f), onClick = onPick) {
                CoinIcon(17.dp)
            }
            StarterTile("ارز", AppPrimaryPill, modifier = Modifier.weight(1f), onClick = onPick) {
                Text("$", color = AppPrimaryInk, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
            StarterTile("نقد", NeutralTileBg, modifier = Modifier.weight(1f), onClick = onPick) {
                Icon(
                    Icons.Filled.AccountBalanceWallet,
                    contentDescription = null,
                    tint = AppMuted,
                    modifier = Modifier.size(16.dp),
                )
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
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) { icon() }
        Text(label, color = AppText, fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold)
    }
}

private val CardShadowSoft = Color(0xFFEEF3F0)
private val CardBorderLine = Color(0xFFE3ECE7)
private val AddTileBg = Color(0xFFE9F7EF)
private val AddTileBorder = Color(0xFF9FE0BC)
private val NeutralTileBg = Color(0xFFF5F8F6)

// ═══ ۲ · هیرویِ داراییِ کل ══════════════════════════════════════════════════════
/**
 * کارتِ سبز با **هاله‌ی نرمِ گوشه‌ی بالا-چپ** و سه قرصِ نیمه‌شفاف.
 *
 * مقادیرِ فریم: گوشه ۲۰ · پدینگ ۱۶ · گرادیانِ `160deg #0EA968→#0B8C57` · سایه‌ی `0 5px 0 #096F45`
 * · هاله‌ی ۹۲ در `-22,-22` · برچسبِ ۱۰/۷۰۰ با سفیدِ ۸۰٪ · عددِ **۲۷** با وزنِ ۹۰۰ · قرص‌ها ۹/۹۰۰.
 */
@Composable
private fun TotalWealthHero(
    total: Double,
    cash: Double,
    gold: Double,
    fx: Double,
    privacyMode: Boolean,
) {
    val glow = Color.White.copy(alpha = 0.16f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .hardShadow(HeroShadow, 5.dp, AppRadius.card)
            .clip(RoundedCornerShape(AppRadius.card))
            .background(Brush.linearGradient(listOf(AppPrimary, AppPrimaryDim)))
            .drawBehind {
                // هاله‌ی نرمِ گوشه - تنها گرادیانِ شعاعیِ مجازِ این کارت، عیناً از فریم.
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glow, Color.Transparent),
                        center = Offset(-22.dp.toPx() + 46.dp.toPx(), -22.dp.toPx() + 46.dp.toPx()),
                        radius = 46.dp.toPx(),
                    ),
                    radius = 46.dp.toPx(),
                    center = Offset(24.dp.toPx(), 24.dp.toPx()),
                )
            }
            .padding(16.dp),
    ) {
        Text("داراییِ کل", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                maskIfPrivate(masked, fmt(total)),
                color = Color.White,
                fontSize = 27.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Row(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            HeroPill("نقد", cash, privacyMode)
            HeroPill("طلا", gold, privacyMode)
            HeroPill("ارز", fx, privacyMode)
        }
    }
}

private val HeroShadow = Color(0xFF096F45)

/** قرصِ `rgba(255,255,255,.2)` با متنِ ۹/۹۰۰ - مقدارِ فشرده، نه کاملِ ریال. */
@Composable
private fun HeroPill(label: String, value: Double, privacyMode: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                "$label ${maskIfPrivate(masked, compact(value))}",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

// ═══ ۳ · حساب‌های بانکی ═════════════════════════════════════════════════════════
/** برچسبِ بخش - ۱۱٫۵/۹۰۰ با رنگِ متنِ دوم. */
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
 * ردیفِ حساب - گوشه ۱۸ · پدینگ ۱۳×۱۴ · حاشیه‌ی ۲ `#E3ECE7` · فاصله ۱۱ ·
 * بجِ دایره‌ای ۳۸ با حاشیه‌ی ۱٫۵ سبز و متنِ ۱۰/۹۰۰.
 */
@Composable
private fun AccountRow(account: AccountEntity, balance: Double, privacyMode: Boolean) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppSurface)
            .border(2.dp, AppLine, shape)
            .padding(horizontal = 14.dp, vertical = 13.dp),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(AppPrimaryPill)
                .border(1.5.dp, AppPrimary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                account.bankName?.take(3) ?: "نقد",
                color = AppPrimaryDim,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(account.name, color = AppText, fontSize = 12.sp, fontWeight = FontWeight.Black)
            val meta = account.smsSender?.let { "پیامک $it" } ?: account.bankName ?: "منبعِ نقدی"
            Text(meta, color = AppMuted, fontSize = 9.sp, modifier = Modifier.padding(top = 2.dp))
        }
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                maskIfPrivate(masked, compact(balance)),
                color = AppText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

// ═══ ۴ · طلا و ارز ═════════════════════════════════════════════════════════════
/**
 * کارتِ **گروهیِ طلایی**: همه‌ی دارایی‌های غیرنقدی زیرِ یه کارت، با «سودِ کل» بالای اون.
 *
 * مقادیرِ فریم: گوشه ۲۰ · پدینگ ۱۴ · گرادیانِ `165deg #FFFCF4→#F5EBD6` · حاشیه‌ی ۱٫۵ `#EBD9B4`
 * · فاصله ۱۱ · متنِ تیره‌ی `#5A3E12`.
 */
@Composable
private fun GoldAndCurrencyCard(
    holdings: List<AssetHolding>,
    privacyMode: Boolean,
    onOpen: (AssetEntity) -> Unit,
) {
    val shape = RoundedCornerShape(AppRadius.card)
    val totalProfit = holdings.mapNotNull { it.profit }.takeIf { it.isNotEmpty() }?.sum()
    Column(
        verticalArrangement = Arrangement.spacedBy(11.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(GoldPaperFrom, GoldPaperTo)))
            .border(1.5.dp, GoldPaperBorder, shape)
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text("سودِ کل", color = GoldInkDeep, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
            Text(
                if (totalProfit == null) "—" else {
                    (if (totalProfit >= 0) "+" else "−") + compact(kotlin.math.abs(totalProfit))
                },
                color = if (totalProfit != null && totalProfit < 0) ProfitDown else ProfitUp,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black,
            )
        }
        holdings.forEach { holding ->
            HoldingRow(holding, privacyMode, onOpen)
        }
    }
}

private val GoldPaperFrom = Color(0xFFFFFCF4)
private val GoldPaperTo = Color(0xFFF5EBD6)
private val GoldPaperBorder = Color(0xFFEBD9B4)
private val GoldInkDeep = Color(0xFF5A3E12)
private val GoldInkSoft = Color(0xFF8B6F3D)
private val ProfitUp = Color(0xFF0B8C57)
private val ProfitDown = Color(0xFFD93838)

/** یه ردیف از کارتِ طلایی - سکه‌ی ۳۲، اسم ۱۱/۹۰۰، فراداده ۸٫۵، ارزش و سود سمتِ چپ. */
@Composable
private fun HoldingRow(holding: AssetHolding, privacyMode: Boolean, onOpen: (AssetEntity) -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White.copy(alpha = 0.55f))
            .pressScaleClickable { onOpen(holding.asset) }
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) {
        CoinIcon(size = 32.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${formatQuantity(holding.quantity)} ${holding.asset.name}",
                color = GoldInkDeep,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                if (holding.quantity > 0) "خرید ${compact(holding.spentRial / holding.quantity)} هر واحد" else "",
                color = GoldInkSoft,
                fontSize = 8.5.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Column(horizontalAlignment = Alignment.Start) {
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    maskIfPrivate(masked, holding.value?.let { compact(it) } ?: "—"),
                    color = GoldInkDeep,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black,
                )
            }
            holding.profit?.let { p ->
                Text(
                    (if (p >= 0) "+" else "−") + compact(kotlin.math.abs(p)),
                    color = if (p >= 0) ProfitUp else ProfitDown,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}

/** «۹۳٫۹M» - فرمِ فشرده‌ای که فریم همه‌جای این صفحه استفاده می‌کنه. */
internal fun compact(value: Double): String = when {
    value >= 1_000_000 -> toFa("%.1f".format(value / 1_000_000).trimEnd('0').trimEnd('.')) + "M"
    value >= 1_000 -> toFa((value / 1_000).toInt()) + "K"
    else -> toFa(value.toInt())
}
