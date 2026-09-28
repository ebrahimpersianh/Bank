package ir.sadteam.loancalc.ui.asset

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.GridView
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_CRYPTO
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_FIAT
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_GOLD
import ir.sadteam.loancalc.ui.components.CoinIcon
import ir.sadteam.loancalc.ui.jibak.toFa
import ir.sadteam.loancalc.ui.theme.AppInfoPill
import ir.sadteam.loancalc.ui.theme.AppWarningPill
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.data.AssetCatalogEntry
import ir.sadteam.loancalc.data.assetCatalogGroups
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_CUSTOM
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
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
 * انتخابگرِ «نوع دارایی» — بازطراحیِ کاملِ دیالوگِ قبلی.
 *
 * دیالوگِ قبلی یک `AlertDialog` بود با `TextButton`های سبزِ پشتِ‌هم: نه نشان، نه قیمت،
 * ردیف‌ها هم‌شکلِ سرگروه‌ها، ارتفاعِ ۴۲۰dp که فرمِ زیرش را نصفه نشان می‌داد، و برای
 * رسیدن به رمزارز باید سه صفحه اسکرول می‌شد.
 *
 * ```
 * ۱ سرصفحه: بستن + «نوع دارایی»
 * ۲ جست‌وجو  (۲۵+ قلم را با یک کلمه پیدا می‌کند)
 * ۳ قرص‌های فیلترِ دسته: همه / طلا / ارز / رمز ارز
 * ۴ ردیف‌ها: نشان · اسم و نماد · **قیمتِ روز** و فلشِ تغییر
 * ۵ کارتِ «عنوانِ دلخواه» — جدا و آخر، چون قیمت ندارد
 * ```
 *
 * تمام‌صفحه است نه دیالوگ: فهرست بلند است و دیالوگ هم آن را می‌بُرید هم فرمِ زیرش را
 * نیمه‌دیده می‌گذاشت.
 */
@Composable
fun AssetPickerSheet(
    prices: Map<String, Double>,
    changes: Map<String, Double>,
    selectedSymbol: String?,
    onPick: (AssetCatalogEntry) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf<String?>(null) }
    var customName by remember { mutableStateOf("") }

    BackHandler(onBack = onDismiss)

    val all = remember { assetCatalogGroups.flatMap { it.second } }
    val visible = remember(query, filter, all) {
        val q = query.trim()
        all.filter { entry ->
            (filter == null || entry.category == filter) &&
                (q.isEmpty() || entry.name.contains(q) || entry.symbol.contains(q, true))
        }
    }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    Box(modifier = Modifier.fillMaxSize().background(AppSurface)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 14.dp, 16.dp, 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item(key = "head") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("نوع دارایی", color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text(
                            "دارایی موردنظرت را انتخاب کن",
                            color = AppMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppIconFrame)
                            .pressScaleClickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "بستن", tint = AppText, modifier = Modifier.size(20.dp))
                    }
                }
            }

            item(key = "search") {
                // جست‌وجوی کپسولی و سبک (طرحِ ChatGPT) - همان فیلترِ قبلی، فقط ظاهر.
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                    cursorBrush = SolidColor(AppPrimary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(AppSurface)
                                .border(1.dp, AppLine, RoundedCornerShape(999.dp))
                                .padding(horizontal = 16.dp),
                        ) {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = AppMuted, modifier = Modifier.size(20.dp))
                            Box(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                                if (query.isEmpty()) {
                                    Text(
                                        when (filter) {
                                            ASSET_CATEGORY_GOLD -> "جست‌وجو در طلا (مثلاً سکه، ۱۸ عیار…)"
                                            ASSET_CATEGORY_FIAT -> "جست‌وجو در ارزها (مثلاً دلار، یورو…)"
                                            ASSET_CATEGORY_CRYPTO -> "جست‌وجو در رمزارزها (مثلاً بیت‌کوین…)"
                                            else -> "جست‌وجو (مثلاً دلار، طلا، بیت‌کوین…)"
                                        },
                                        color = AppMuted,
                                        fontSize = 12.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                inner()
                            }
                            if (query.isNotEmpty()) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "پاک‌کردنِ جست‌وجو",
                                    tint = AppMuted,
                                    modifier = Modifier.size(18.dp).clip(CircleShape).pressScaleClickable { query = "" },
                                )
                            }
                        }
                    },
                )
            }

            item(key = "filters") {
                // اسکرولِ افقی تا در ۳۶۰dp هم دو خط نشود.
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                ) {
                    // «همه» فیلترِ null است، نه یک دسته‌ی چهارم.
                    FilterPill("همه", filter == null, icon = {
                        Icon(Icons.Filled.GridView, contentDescription = null, tint = it, modifier = Modifier.size(16.dp))
                    }) { filter = null }
                    assetGroupOrder.forEach { (category, title) ->
                        if (category == ASSET_CATEGORY_CUSTOM) return@forEach
                        FilterPill(title, filter == category, icon = { _ -> CategoryGlyph(category, 18.dp) }) {
                            filter = if (filter == category) null else category
                        }
                    }
                    // کارتِ «عنوانِ دلخواه» تهِ فهرست است (قیمت ندارد)؛ این قرص تا آن‌جا می‌بَرد
                    // تا کسی که ملک/خودرو می‌خواهد از بالای صفحه هم پیدایش کند (بازخوردِ کاربر، ۳ مهر).
                    FilterPill("عنوانِ دلخواه", false, icon = {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = it, modifier = Modifier.size(16.dp))
                    }) {
                        scope.launch {
                            val index = listState.layoutInfo.totalItemsCount - 1
                            if (index >= 0) listState.animateScrollToItem(index)
                        }
                    }
                }
            }

            // کارتِ معرفیِ دسته - فقط وقتی یک دسته انتخاب شده (طرحِ ChatGPT برای «طلا»).
            val activeFilter = filter
            if (activeFilter != null && query.isBlank()) {
                item(key = "banner_$activeFilter") { CategoryBanner(activeFilter) }
            }

            item(key = "listhead") {
                val title = when (activeFilter) {
                    null -> "همه‌ی دارایی‌ها"
                    else -> "فهرستِ " + (assetGroupOrder.firstOrNull { it.first == activeFilter }?.second ?: "")
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (query.isBlank()) title else "نتیجه‌ی جست‌وجو",
                        color = AppText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "${visible.size.toFa()} مورد",
                        color = AppMuted,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(AppIconFrame)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            // با فیلترِ فعال یا جست‌وجو، سرگروه‌ها فقط نویز اضافه می‌کنند.
            val grouped = filter == null && query.isBlank()
            if (grouped) {
                assetGroupOrder.forEach { (category, title) ->
                    if (category == ASSET_CATEGORY_CUSTOM) return@forEach
                    val rows = visible.filter { it.category == category }
                    if (rows.isEmpty()) return@forEach
                    item(key = "h_$category") { PickerGroupLabel(title) }
                    items(rows, key = { "e_${it.symbol}" }) { entry ->
                        PickerRow(entry, prices[entry.symbol], changes[entry.symbol], entry.symbol == selectedSymbol) {
                            onPick(entry)
                        }
                    }
                }
            } else {
                items(visible, key = { "e_${it.symbol}" }) { entry ->
                    PickerRow(entry, prices[entry.symbol], changes[entry.symbol], entry.symbol == selectedSymbol) {
                        onPick(entry)
                    }
                }
                if (visible.isEmpty()) {
                    item(key = "none") {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                        ) {
                            Text("موردی پیدا نشد", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            Text(
                                "می‌توانی پایین با «عنوانِ دلخواه» خودت اضافه‌اش کنی.",
                                color = AppMuted,
                                fontSize = 11.5.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }

            item(key = "custom") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(AppRadius.card))
                        .background(AppSurface)
                        .border(2.dp, AppLineRow, RoundedCornerShape(AppRadius.card))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("عنوانِ دلخواه", color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Black)
                    Text(
                        "ملک، اوراق، خودرو و هر چیزی که قیمتِ روزش را خودت وارد می‌کنی.",
                        color = AppMuted,
                        fontSize = 10.5.sp,
                        lineHeight = 19.sp,
                    )
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(AppRadius.button),
                        placeholder = { Text("اسمِ دارایی", color = AppMuted, fontSize = 12.5.sp) },
                    )
                    val trimmed = customName.trim()
                    Text(
                        "افزودن",
                        color = if (trimmed.isEmpty()) AppMuted else AppPrimaryInk,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(AppRadius.button))
                            .background(if (trimmed.isEmpty()) AppIconFrame else AppPrimaryPill)
                            .border(
                                1.5.dp,
                                if (trimmed.isEmpty()) AppLine else AppPrimaryBorder,
                                RoundedCornerShape(AppRadius.button),
                            )
                            .then(
                                if (trimmed.isEmpty()) Modifier
                                else Modifier.pressScaleClickable {
                                    // نماد از رو خودِ اسم ساخته می‌شود تا یکتا بماند و
                                    // دوباره ساخته نشود.
                                    onPick(AssetCatalogEntry("CUSTOM_$trimmed", trimmed, ASSET_CATEGORY_CUSTOM))
                                }
                            )
                            .padding(horizontal = 18.dp, vertical = 9.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerGroupLabel(title: String) {
    Text(
        title,
        color = AppMuted,
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    icon: @Composable (tint: androidx.compose.ui.graphics.Color) -> Unit,
    onClick: () -> Unit,
) {
    val ink = if (selected) androidx.compose.ui.graphics.Color.White else AppText
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(42.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) AppPrimary else AppSurface)
            .border(1.dp, if (selected) AppPrimary else AppLine, RoundedCornerShape(999.dp))
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 14.dp),
    ) {
        icon(ink)
        Text(
            label,
            color = ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            modifier = Modifier.padding(start = 7.dp),
        )
    }
}

/** نشانِ کوچکِ هر دسته، از همان منبعِ نشان‌های فهرست (سکه / دلار / بیت‌کوین). */
@Composable
private fun CategoryGlyph(category: String, size: androidx.compose.ui.unit.Dp) {
    when (category) {
        ASSET_CATEGORY_GOLD -> CoinIcon(size)
        ASSET_CATEGORY_FIAT -> AssetBadge("USD", ASSET_CATEGORY_FIAT, size)
        else -> AssetBadge("BTC", ASSET_CATEGORY_CRYPTO, size)
    }
}

/** کارتِ معرفیِ دسته‌ی انتخاب‌شده - ته‌رنگِ خودِ همان دسته، نه کلِ صفحه. */
@Composable
private fun CategoryBanner(category: String) {
    val (title, hint, bg) = when (category) {
        ASSET_CATEGORY_GOLD -> Triple("طلا", "دارایی‌های مرتبط با طلا، مثلِ سکه، طلای آب‌شده و …", AppWarningPill)
        ASSET_CATEGORY_FIAT -> Triple("ارز", "ارزهای خارجی، مثلِ دلار، یورو، درهم و …", AppPrimaryPill)
        else -> Triple("رمز ارز", "رمزارزها، مثلِ بیت‌کوین، اتریوم، تتر و …", AppInfoPill)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Black)
            Text(
                hint,
                color = AppMuted,
                fontSize = 11.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Box(
            modifier = Modifier.padding(start = 12.dp).size(64.dp).clip(CircleShape).background(AppSurface.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center,
        ) {
            CategoryGlyph(category, 40.dp)
        }
    }
}

/**
 * ردیفِ انتخاب. قیمتِ روز **کنارِ خودش** است — همان چیزی که در دیالوگِ قبلی نبود و کاربر
 * مجبور بود اسم را حدس بزند و بعد ببیند چند است.
 *
 * ردیفِ انتخاب‌شده حاشیه‌ی سبز و تیکِ کنارِ نشان می‌گیرد، تا وقتی انتخابگر دوباره باز
 * می‌شود معلوم باشد کجا بود.
 */
@Composable
private fun PickerRow(
    entry: AssetCatalogEntry,
    priceRial: Double?,
    changePercent: Double?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppSurface)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) AppPrimary else AppLine, shape)
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        AssetBadge(entry.symbol, entry.category, 42.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                entry.name,
                color = AppText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // نمادِ لاتین عمداً لاتین می‌ماند — جزوِ هفت استثنای `Numerals-global-handoff.md`.
            Text(
                entry.symbol.removePrefix("CUSTOM_"),
                color = AppMuted,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                // «—» یعنی سرور قیمت نداده؛ جای عدد سرِ جایش می‌ماند.
                priceRial?.rialToFaCompact() ?: "—",
                color = if (priceRial == null) AppMuted else AppText,
                fontSize = 14.sp,
                maxLines = 1,
                softWrap = false,
                fontWeight = FontWeight.Black,
            )
            if (changePercent != null) {
                PriceChangeBadge(changePercent, modifier = Modifier.padding(top = 3.dp))
            }
            // ⚠️ اسنادِ منبع عمداً این‌جا هم نیست - رجوع کن به کامنتِ `AssetDetailScreen`.
        }
        Icon(
            if (selected) Icons.Filled.Check else Icons.Filled.ChevronLeft,
            contentDescription = if (selected) "انتخاب‌شده" else null,
            tint = if (selected) AppPrimaryInk else AppMuted,
            modifier = Modifier.size(18.dp),
        )
    }
}
