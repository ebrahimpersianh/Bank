package ir.sadteam.loancalc.ui.asset

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.data.AssetCatalogEntry
import ir.sadteam.loancalc.data.assetCatalogGroups
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_CRYPTO
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_CUSTOM
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_FIAT
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_GOLD
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.jibak.toFa
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

/**
 * **۴۳a · بازچیدمانِ بخشِ ۶۲** - قیمتِ روزِ همه‌ی نمادها، یک‌جا.
 *
 * ```
 * ۱ سرصفحه: بازگشت + «قیمتِ روز» + دکمه‌ی به‌روزرسانی
 * ۲ جست‌وجو + دو قرص: «دارایی‌های من» (پیش‌فرض روشن) و «فقط قیمتِ زنده»
 * ۳ چهار سرگروهِ جمع‌شونده: طلا و سکه · ارز · رمزارز · قیمتِ دستی
 * ```
 *
 * 🚨 **چرا هر سه با هم**: ۵۶ ردیف را نه گروه‌بندیِ تنها کوتاه می‌کند و نه جست‌وجوی تنها.
 * قرصِ «دارایی‌های من» فهرست را به سه‌چهار ردیف می‌رسانَد و گروه‌بندی برای وقتی است که
 * کاربر خاموشش می‌کند.
 *
 * 🚨 **«قیمتِ دستی» سرگروهِ خودش است** (بخشِ ۶۲، جایگزینِ قاعده‌ی ۴ِ بخشِ ۵۴ که گفته بود
 * زیرِ طلا/ارز بمانند): آن هشت نماد یک **جنسِ** متفاوت‌اند نه یک صفتِ متفاوت - عددشان از
 * کاربر می‌آید، تازه نمی‌شود، و Servix زیرشان نمی‌آید.
 *
 * ⚠️ **ماسکِ حریمِ خصوصی نداره.** هیچ عددِ این صفحه مالِ کاربر نیست؛ نرخِ بازاره.
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
    var query by remember { mutableStateOf("") }
    // پیش‌فرضِ روشن: کسی که هفت قلم دارد نباید برای دیدنشان از ۵۶ ردیف بگذرد.
    var onlyMine by remember { mutableStateOf(true) }
    var liveOnly by remember { mutableStateOf(false) }
    // رمزارز **بسته** باز می‌شود - ۳۸ ردیفش تنهایی همان فهرستِ قبلی است و سرگروهِ
    // «قیمتِ دستی» را زیرِ چهار صفحه اسکرول می‌بَرد.
    var collapsed by remember { mutableStateOf(setOf(ASSET_CATEGORY_CRYPTO)) }
    val openAssetEntity = owned.firstOrNull { it.id == openAsset }

    BackHandler(enabled = openAssetEntity != null || buySymbol != null) {
        // فرمِ خرید روی جزئیات می‌شینه، پس اول بسته می‌شه.
        if (buySymbol != null) buySymbol = null else openAsset = null
    }

    // 🚨 **تصمیمِ عوض‌شده (بخشِ ۵۴)**: هشت نمادِ بی‌سرویس از این صفحه بیرون گذاشته
    // **نمی‌شوند**؛ به‌جای حذف، بجِ «قیمتِ دستی» می‌گیرند.
    //
    // ولی فیلترِ دومِ قبلی می‌ماند: نمادی که پرچمش `hasLivePrice = true` است و سرویس در
    // این لحظه قیمتش را نمی‌دهد (ADA، DOT، SHIB و…) همچنان حذف می‌شود - کاربر یک‌بار
    // ~۲۰ ردیفِ «—» دید و درست هم بود که شکایت کرد.
    val allEntries = remember { assetCatalogGroups.flatMap { it.second } }
    val entries = if (prices.isEmpty()) {
        allEntries
    } else {
        allEntries.filter { !it.hasLivePrice || prices[it.symbol] != null }
    }
    val ownedSymbols = remember(owned) { owned.map { it.symbol }.toSet() }

    val trimmed = query.trim()
    val searching = trimmed.isNotEmpty()
    val visible = entries.filter { entry ->
        (!onlyMine || searching || entry.symbol in ownedSymbols) &&
            (!liveOnly || entry.hasLivePrice) &&
            (!searching || entry.name.contains(trimmed, true) || entry.symbol.contains(trimmed, true))
    }

    fun priceOf(entry: AssetCatalogEntry): Double? =
        prices[entry.symbol] ?: owned.firstOrNull { it.symbol == entry.symbol }?.unitPriceRial

    fun openRow(entry: AssetCatalogEntry) {
        // داره → جزئیاتِ داراییِ خودش. نداره → فرمِ خرید با نمادِ پرشده.
        val mine = owned.firstOrNull { it.symbol == entry.symbol }
        if (mine != null) openAsset = mine.id else buySymbol = entry.symbol
    }

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
                            // واحد یک‌بار در سرصفحه می‌آید، نه کنارِ هر سطر.
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

            item {
                PriceSearchField(value = query, onChange = { query = it })
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    FilterPill("دارایی‌های من", onlyMine && !searching) { onlyMine = !onlyMine }
                    FilterPill("فقط قیمتِ زنده", liveOnly) { liveOnly = !liveOnly }
                }
            }

            if (visible.isEmpty()) {
                item {
                    Text(
                        if (searching) "نمادی با این نام پیدا نشد." else "هنوز داراییی ثبت نکرده‌ای. قرص را خاموش کن تا همه را ببینی.",
                        color = AppMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }

            // **در حالتِ جست‌وجو سرگروه‌ها حذف می‌شوند** - فهرستِ تخت، چون سرگروهِ
            // یک‌ردیفی فضا می‌گیرد و چیزی نمی‌گوید.
            if (searching || (onlyMine && visible.isNotEmpty())) {
                items(visible, key = { "f_${it.symbol}" }) { entry ->
                    PriceRow(
                        entry = entry,
                        price = priceOf(entry),
                        changePercent = changes[entry.symbol],
                        owned = entry.symbol in ownedSymbols,
                        onClick = { openRow(entry) },
                    )
                }
                if (onlyMine && !searching) {
                    item {
                        Text(
                            "${visible.size.toFa()} نماد از ${entries.size.toFa()}. قرص را خاموش کن تا همه را ببینی.",
                            color = AppMuted,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            } else {
                priceGroupOrder.forEach { (key, title) ->
                    val rows = visible.filter { groupKeyOf(it) == key }
                    if (rows.isEmpty()) return@forEach
                    val isCollapsed = key in collapsed
                    // گروهِ بسته دو ردیفِ اولش را بیرون می‌گذارد (پرمعامله‌ترها) تا
                    // سرگروه یک دیوارِ بسته نباشد.
                    val shown = if (isCollapsed) rows.take(2) else rows
                    item(key = "h_$key") {
                        PriceGroupHeader(
                            title = title,
                            count = rows.size,
                            collapsed = isCollapsed,
                            onToggle = {
                                collapsed = if (isCollapsed) collapsed - key else collapsed + key
                            },
                        )
                    }
                    items(shown, key = { "p_${it.symbol}" }) { entry ->
                        PriceRow(
                            entry = entry,
                            price = priceOf(entry),
                            changePercent = changes[entry.symbol],
                            owned = entry.symbol in ownedSymbols,
                            onClick = { openRow(entry) },
                        )
                    }
                    if (isCollapsed && rows.size > shown.size) {
                        item(key = "m_$key") {
                            MoreRow(
                                label = "${(rows.size - shown.size).toFa()} $title‌ِ دیگر",
                                onClick = { collapsed = collapsed - key },
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    // اسنادِ منبع، پای همان فهرستی که عددهایش از سرویس آمده - با استثنای
                    // گروهی که عددش مالِ خودِ کاربر است.
                    "داده‌ی قیمت از Servix.cc — به‌جز گروهِ «قیمتِ دستی» که عددش مالِ خودت است.",
                    color = AppMuted,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp),
                )
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

/** کلیدِ سرگروهِ ساختگیِ نمادهای بی‌سرویس - دسته‌ی واقعی‌شان هرچه باشد. */
private const val GROUP_MANUAL = "manual"

/**
 * چهار سرگروهِ صفحه‌ی قیمت. «قیمتِ دستی» **دسته‌ی دیتابیسی نیست**، فقط یک سرگروهِ
 * نمایشی است که روی `hasLivePrice == false` بسته می‌شود.
 */
private val priceGroupOrder: List<Pair<String, String>> = listOf(
    ASSET_CATEGORY_GOLD to "طلا و سکه",
    ASSET_CATEGORY_FIAT to "ارز",
    ASSET_CATEGORY_CRYPTO to "رمزارز",
    GROUP_MANUAL to "قیمتِ دستی",
)

private fun groupKeyOf(entry: AssetCatalogEntry): String = when {
    !entry.hasLivePrice -> GROUP_MANUAL
    entry.category == ASSET_CATEGORY_CUSTOM -> GROUP_MANUAL
    else -> entry.category
}

@Composable
private fun PriceSearchField(value: String, onChange: (String) -> Unit) {
    val shape = RoundedCornerShape(AppRadius.row)
    val ink = AppText
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppSurface)
            .border(2.dp, AppLineRow, shape)
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = AppMuted, modifier = Modifier.size(16.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text("جست‌وجوی نماد یا نام", color = AppMuted, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                cursorBrush = SolidColor(AppPrimary),
                textStyle = TextStyle(color = ink, fontSize = 11.5.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (value.isNotEmpty()) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "پاک‌کردن",
                tint = AppMuted,
                modifier = Modifier.size(16.dp).pressScaleClickable { onChange("") },
            )
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(AppRadius.button)
    Text(
        label,
        color = if (selected) AppPrimaryInk else AppMuted,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier
            .clip(shape)
            .background(if (selected) AppPrimaryPill else AppIconFrame)
            .border(2.dp, if (selected) AppPrimaryBorder else AppLine, shape)
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 6.dp),
    )
}

@Composable
private fun PriceGroupHeader(title: String, count: Int, collapsed: Boolean, onToggle: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.icon))
            .pressScaleClickable(onClick = onToggle)
            .padding(top = 4.dp, bottom = 2.dp),
    ) {
        Text(title, color = AppMuted, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
        Text(
            count.toFa(),
            color = AppMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .clip(RoundedCornerShape(AppRadius.icon))
                .background(AppIconFrame)
                .padding(horizontal = 6.dp, vertical = 1.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            if (collapsed) "همه را ببین" else "جمع کن",
            color = AppPrimaryInk,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun MoreRow(label: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(AppRadius.row)
    Text(
        label,
        color = AppPrimaryInk,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppIconFrame)
            .border(2.dp, AppLine, shape)
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    )
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
    owned: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(AppRadius.row)
    val dot = AppPrimary
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    entry.name,
                    color = AppText,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // نقطه‌ی سبز یعنی «داری» - در حالتِ خاموشِ قرص تنها راهِ تشخیصِ
                // دارایی‌های خودت در فهرستِ بلند است.
                if (owned) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(dot))
                }
            }
            // نمادِ لاتین عمداً لاتین می‌مونه - جزوِ هفت استثنای `Numerals-global-handoff.md`.
            Ltr {
                Text(
                    entry.symbol,
                    color = AppMuted,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                price?.rialToFaCompact() ?: "—",
                color = if (price == null) AppMuted else AppText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
            )
            // 🚨 ستونِ تغییر **یک واژگانِ رنگی** دارد: سبز بالا، قرمز پایین، خاکستری
            // بی‌تغییر. هر رنگِ پنجمی در همان ستون یک معنیِ پنجم خوانده می‌شود - به همین
            // دلیل بجِ بنفشِ «تازه» برداشته شد (بنفش رنگِ «جدید/تبلیغ» است و خوانده
            // می‌شد «این نماد تازه اضافه شده»، نه «داده نداریم»). جایش متنِ خاکستری در
            // همان جای درصد است، بی بج و بی فلش: نبودِ فلش خودش پیام است.
            when {
                changePercent != null ->
                    PriceChangeBadge(changePercent, modifier = Modifier.padding(top = 3.dp))
                !entry.hasLivePrice ->
                    PriceStateBadge(
                        label = "قیمتِ دستی",
                        fill = AppIconFrame,
                        border = AppLine,
                        ink = AppMuted,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                price != null ->
                    Text(
                        "تازه — هنوز داده نداریم",
                        color = AppMuted,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 3.dp),
                    )
            }
        }
    }
}

/**
 * بجِ حالتِ قیمت - جای فلشِ تغییر وقتی تغییری برای نشان‌دادن نیست (فریمِ `54b`).
 *
 * فقط یک مصرف دارد: «قیمتِ دستی» یعنی سرویس این نماد را اصلاً ندارد و عدد را خودِ
 * کاربر زده - **دائمی** است و خودش نمی‌رود. حالتِ «تازه» عمداً بج نیست (بخشِ ۶۲).
 */
@Composable
private fun PriceStateBadge(
    label: String,
    fill: Color,
    border: Color,
    ink: Color,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(AppRadius.icon)
    Text(
        label,
        color = ink,
        fontSize = 8.5.sp,
        fontWeight = FontWeight.Black,
        modifier = modifier
            .clip(shape)
            .background(fill)
            .border(1.dp, border, shape)
            .padding(horizontal = 6.dp, vertical = 1.dp),
    )
}
