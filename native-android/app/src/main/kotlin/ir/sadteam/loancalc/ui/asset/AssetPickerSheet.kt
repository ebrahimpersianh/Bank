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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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

    Box(modifier = Modifier.fillMaxSize().background(AppSurface)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 28.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            item(key = "head") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    HeaderSquareButton(
                        icon = Icons.Filled.Close,
                        description = "بستن",
                        fill = AppIconFrame,
                        border = AppLine,
                        ink = AppMuted,
                        onClick = onDismiss,
                    )
                    Text(
                        "نوع دارایی",
                        color = AppText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item(key = "search") {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(AppRadius.button),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = AppMuted,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    placeholder = { Text("جست‌وجو — دلار، سکه، بیت…", color = AppMuted, fontSize = 12.5.sp) },
                )
            }

            item(key = "filters") {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    // «همه» فیلترِ null است، نه یک دسته‌ی چهارم.
                    FilterPill("همه", filter == null) { filter = null }
                    assetGroupOrder.forEach { (category, title) ->
                        if (category == ASSET_CATEGORY_CUSTOM) return@forEach
                        FilterPill(title, filter == category) {
                            filter = if (filter == category) null else category
                        }
                    }
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
                        Text(
                            "چیزی با این اسم پیدا نشد. می‌توانی پایین با «عنوانِ دلخواه» خودت " +
                                "اضافه‌اش کنی.",
                            color = AppMuted,
                            fontSize = 12.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(vertical = 10.dp),
                        )
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
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(top = 6.dp),
    )
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        color = if (selected) AppPrimaryInk else AppMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier
            .clip(RoundedCornerShape(AppRadius.button))
            .background(if (selected) AppPrimaryPill else AppIconFrame)
            .border(
                1.5.dp,
                if (selected) AppPrimaryBorder else AppLine,
                RoundedCornerShape(AppRadius.button),
            )
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp),
    )
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
    val shape = RoundedCornerShape(AppRadius.row)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppSurface)
            .border(if (selected) 2.dp else 2.dp, if (selected) AppPrimary else AppLineRow, shape)
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        AssetBadge(entry.symbol, entry.category, 30.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                entry.name,
                color = AppText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // نمادِ لاتین عمداً لاتین می‌ماند — جزوِ هفت استثنای `Numerals-global-handoff.md`.
            Text(
                entry.symbol.removePrefix("CUSTOM_"),
                color = AppMuted,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                // «—» یعنی سرور قیمت نداده؛ جای عدد سرِ جایش می‌ماند.
                priceRial?.rialToFaCompact() ?: "—",
                color = if (priceRial == null) AppMuted else AppText,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black,
            )
            if (changePercent != null) {
                PriceChangeBadge(changePercent, modifier = Modifier.padding(top = 3.dp))
            }
            // ⚠️ اسنادِ منبع عمداً این‌جا هم نیست - رجوع کن به کامنتِ `AssetDetailScreen`.
        }
        if (selected) {
            Icon(
                Icons.Filled.Check,
                contentDescription = "انتخاب‌شده",
                tint = AppPrimaryInk,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
