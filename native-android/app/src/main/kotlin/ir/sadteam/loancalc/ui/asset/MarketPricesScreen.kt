package ir.sadteam.loancalc.ui.asset

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.data.AssetCatalogEntry
import ir.sadteam.loancalc.data.assetCatalogGroups
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_CUSTOM
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.toFa
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryBorder
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * **۴۳a** - قیمتِ روزِ همه‌ی نمادها، یک‌جا. از قرصِ نمودارِ سرصفحه‌ی تبِ دارایی باز می‌شه.
 *
 * ```
 * ۱ سرصفحه: بازگشت + «قیمتِ روز» + دکمه‌ی به‌روزرسانی
 * ۲ هر دسته: سرگروه + ردیفِ هر نماد (نشان · اسم و نماد · قیمت و درصدِ ۳۰روزه)
 * ```
 *
 * ⚠️ صفحه‌ی **جدا** از تب، نه کارتی داخلش: تبِ دارایی «مالِ من چقدره» رو جواب می‌ده و این
 * فهرست «بازار امروز چنده» - ده‌ها سطره و اگه داخلِ تب بشینه دارایی‌های خودِ کاربر زیرش
 * گم می‌شن.
 *
 * ⚠️ همه‌ی نمادهای **کاتالوگ** میان، نه فقط دارایی‌های کاربر - وگرنه «قیمتِ روز» می‌شد
 * «قیمتِ دارایی‌های من» و ارزشش رو از دست می‌داد.
 *
 * ⚠️ **ماسکِ حریمِ خصوصی نداره.** هیچ عددِ این صفحه مالِ کاربر نیست؛ نرخِ بازاره و
 * پنهان‌کردنش معنا نداره. همون قاعده‌ی خطِ «قیمتِ روز»ِ `HoldingRow`.
 */
@Composable
fun MarketPricesScreen(
    onBack: () -> Unit,
    viewModel: AssetViewModel,
) {
    val prices by viewModel.marketPrices.collectAsState()
    val changes by viewModel.monthChange.collectAsState()
    val updatedClock by viewModel.pricesUpdatedClock.collectAsState()
    val owned by viewModel.assets.collectAsState()

    // قیمتِ بازار **یک درخواسته**، پس هر بار باز شدن تازه می‌کنه. قیمتِ کهنه بی‌فایده‌ست.
    LaunchedEffect(Unit) { viewModel.refreshPrices() }

    var openAsset by remember { mutableStateOf<Long?>(null) }
    var buySymbol by remember { mutableStateOf<String?>(null) }
    val openAssetEntity = owned.firstOrNull { it.id == openAsset }

    BackHandler(enabled = openAssetEntity != null || buySymbol != null) {
        // فرمِ خرید روی جزئیات می‌شینه، پس اول بسته می‌شه.
        if (buySymbol != null) buySymbol = null else openAsset = null
    }

    // نمادی که سرویسِ قیمت اصلاً نداردش اینجا نمی‌آید - یک ستونِ پُر از «—» این صفحه را
    // بی‌فایده می‌کند. کاربر همچنان می‌تواند ثبتش کند و قیمتِ واحد را دستی بزند.
    // دو لایه‌ی فیلتر، چون پرچمِ `hasLivePrice` دستیه و همیشه با واقعیتِ سرویس جور نیست:
    // کاربر رو گوشیِ واقعی ~۲۰ ردیفِ «—» دید (ADA، DOT، SHIB، استیبل‌کوین‌ها و…) که پرچمشون
    // true بود ولی سرویس قیمتشون رو نمی‌ده. حالا **نبودِ قیمت در همین لحظه** هم ردیف رو حذف
    // می‌کنه، پس این صفحه هیچ‌وقت ستونِ خالی نشون نمی‌ده. کاربر همچنان می‌تونه از فرمِ خرید
    // ثبتش کنه و قیمتِ واحد رو دستی بزنه.
    val allEntries = remember {
        assetCatalogGroups.flatMap { it.second }.filter { it.hasLivePrice }
    }
    // تا اولین fetch، `prices` خالیه - اون‌موقع فهرستِ کامل نشون داده می‌شه (نه صفحه‌ی خالی)
    // و به‌محضِ رسیدنِ قیمت‌ها به ردیف‌های واقعاً قیمت‌دار جمع می‌شه.
    val entries = if (prices.isEmpty()) allEntries else allEntries.filter { prices[it.symbol] != null }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 14.dp, 16.dp, 24.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    HeaderSquareButton(
                        icon = Icons.Filled.ArrowForward,
                        description = "بازگشت",
                        fill = AppIconFrame,
                        border = AppLine,
                        ink = AppMuted,
                        onClick = onBack,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text("قیمتِ روز", color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Text(
                            // null یعنی هنوز یک‌بار هم گرفته نشده، نه «همین الان».
                            // واحد یک‌بار در سرصفحه می‌آید، نه کنارِ هر سطر - وگرنه «تومان»
                            // سی بار تکرار می‌شود و ستون را می‌شکند.
                            (updatedClock?.let { "$it به‌روز شد" } ?: "هنوز به‌روز نشده") +
                                " · تومان",
                            color = AppMuted,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 1.dp),
                        )
                    }
                    HeaderSquareButton(
                        icon = Icons.Filled.Refresh,
                        description = "به‌روزرسانی",
                        fill = AppPrimaryPill,
                        border = AppPrimaryBorder,
                        ink = AppPrimaryInk,
                        onClick = { viewModel.refreshPrices() },
                    )
                }
            }

            // ترتیبِ دسته‌ها همون `assetGroupOrder`ه، منهای «سایر» - داراییِ دلخواهِ کاربر
            // قیمتِ بازار نداره.
            assetGroupOrder.forEach { (category, title) ->
                if (category == ASSET_CATEGORY_CUSTOM) return@forEach
                val rows = entries.filter { it.category == category }
                if (rows.isEmpty()) return@forEach
                item(key = "h_$category") {
                    Text(
                        title,
                        color = AppMuted,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                items(rows, key = { "p_${it.symbol}" }) { entry ->
                    PriceRow(
                        entry = entry,
                        price = prices[entry.symbol],
                        changePercent = changes[entry.symbol],
                        onClick = {
                            // داره → جزئیاتِ داراییِ خودش (نمودار، تاریخچه، سود).
                            // نداره → فرمِ خرید با نمادِ پرشده. کاربری که قیمت رو دید و
                            // خواست ثبت کنه نباید برگرده و از + دوباره نماد رو پیدا کنه.
                            val mine = owned.firstOrNull { it.symbol == entry.symbol }
                            if (mine != null) openAsset = mine.id else buySymbol = entry.symbol
                        },
                    )
                }
            }
        }

        if (openAssetEntity != null) {
            Box(modifier = Modifier.fillMaxSize().background(AppSurface)) {
                AssetDetailScreen(
                    asset = openAssetEntity,
                    onBack = { openAsset = null },
                    viewModel = viewModel,
                )
            }
        }
        buySymbol?.let { symbol ->
            val entry = entries.firstOrNull { it.symbol == symbol }
            if (entry != null) {
                Box(modifier = Modifier.fillMaxSize().background(AppSurface)) {
                    AssetTradeSheet(
                        onDismiss = { buySymbol = null },
                        viewModel = viewModel,
                        presetSymbol = entry.symbol,
                        presetName = entry.name,
                        presetCategory = entry.category,
                        startWithSell = false,
                    )
                }
            }
        }
    }
}

/**
 * ردیفِ قیمت. کلیک‌پذیره (رجوع کن به `onClick`ِ بالا) - فهرستِ فقط‌خواندنی کاربر رو
 * مجبور می‌کرد برگرده و نماد رو دوباره پیدا کنه.
 */
@Composable
private fun PriceRow(
    entry: AssetCatalogEntry,
    price: Double?,
    changePercent: Double?,
    onClick: () -> Unit,
) {
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
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        AssetBadge(entry.symbol, entry.category, 30.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                entry.name,
                color = AppText,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // نمادِ لاتین عمداً لاتین می‌مونه - جزوِ هفت استثنای `Numerals-global-handoff.md`.
            Text(
                entry.symbol,
                color = AppMuted,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                price?.rialToFaCompact() ?: "—",
                color = if (price == null) AppMuted else AppText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
            )
            // فلشِ سبز/قرمزِ جهت. جای متنِ «+۲٫۴٪» که جهت را فقط با رنگ می‌گفت.
            if (changePercent != null) {
                PriceChangeBadge(changePercent, modifier = Modifier.padding(top = 3.dp))
            }
        }
    }
}

