package ir.sadteam.loancalc.ui.asset

import androidx.compose.ui.focus.focusRequester
import ir.sadteam.loancalc.ui.components.AutoShrinkText
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import ir.sadteam.loancalc.ui.components.AppCard
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.layout.heightIn
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.data.AssetCatalogEntry
import ir.sadteam.loancalc.data.assetCatalogGroups
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_CRYPTO
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

@Composable
internal fun AssetSearchBar(value: String, onChange: (String) -> Unit) {
    val shape = RoundedCornerShape(999.dp)
    val focus = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
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
            modifier = Modifier.weight(1f).focusRequester(focus),
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
internal fun MarketSectionTitle(title: String, onSeeAll: (() -> Unit)?) {
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
internal fun MarketOverviewSection(
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
        // ۱۶ مهر: سه کارتِ بلند (۱۷۶dp، نام‌های بریده) جایشان را به سه ردیفِ فشرده‌ی هم‌شکلِ فهرستِ
        // قیمت داد - نامِ کامل، قیمت و درصد، و کلِ بخش حدودِ نصف شد.
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(top = 8.dp).animateContentSize(),
        ) {
            picks.forEach { pick -> MarketWideRow(pick, prices, viewModel, onOpen) }
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
            .height(176.dp) // ۱۵۰ بجِ درصد را از پایین می‌برید (اسکرین‌شاتِ کاربر، ۹ مهر)
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
            // ۱۶ مهر: فشرده‌تر (خواسته‌ی کاربر) تا نام‌ها بریده نشوند و همه‌ی ردیف‌ها در یک نگاه جا شوند.
            .heightIn(min = 52.dp)
            .clip(shape)
            .background(AppSurface)
            .border(1.dp, AppLine, shape)
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AssetBadge(symbol, category, 28.dp)
        Column(modifier = Modifier.weight(1f).padding(start = 7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    name,
                    color = AppText,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (owned) Box(modifier = Modifier.padding(start = 4.dp).size(6.dp).clip(CircleShape).background(AppPrimary))
            }
            if (subtitle != null) {
                Ltr { Text(subtitle, color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(top = 2.dp)) }
            }
        }
        Box(modifier = Modifier.width(46.dp).padding(horizontal = 2.dp), contentAlignment = Alignment.Center) { trend() }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(84.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AppPrimary.copy(alpha = 0.06f))
                .padding(horizontal = 3.dp, vertical = 4.dp),
        ) {
            // «۶۹۰٫۹ میلیون تو…» نصفه می‌شد - حالا فونت کوچک می‌شود تا کلِ قیمت جا شود.
            ir.sadteam.loancalc.ui.components.AutoShrinkText(
                text = price?.let { "$it تومان" } ?: "—",
                color = if (price == null) AppMuted else AppText,
                maxFontSize = 11.sp,
                minFontSize = 9.5.sp,
                fontWeight = FontWeight.Black,
            )
            if (secondLine != null) Text(secondLine, color = AppMuted, fontSize = 9.5.sp, maxLines = 1)
        }
        Box(modifier = Modifier.width(58.dp).padding(start = 4.dp), contentAlignment = Alignment.Center) { trailing() }
    }
}
