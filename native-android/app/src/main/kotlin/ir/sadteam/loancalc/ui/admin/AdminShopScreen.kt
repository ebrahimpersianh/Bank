package ir.sadteam.loancalc.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.coin.RemoteShop
import ir.sadteam.loancalc.data.coin.RemoteShopConfig
import ir.sadteam.loancalc.data.coin.RemoteTheme
import ir.sadteam.loancalc.data.coin.SHOP_CATALOG
import ir.sadteam.loancalc.data.coin.ShopCategory
import ir.sadteam.loancalc.data.coin.ShopItem
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.AppFieldShape
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.launch

/**
 * 🛍 **مدیریتِ فروشگاه از سرور** (۸ مهر): پنهان/نمایش، قیمت، نام، «تازه»، تمِ رنگیِ تازه و
 * تخفیفِ امروز. «ذخیره» روی سرور می‌رود و گوشیِ همه‌ی کاربران دفعه‌ی بعدی که برنامه را باز کنند می‌گیرد.
 */
@Composable
fun AdminShopScreen(onBack: () -> Unit, vm: AdminProViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val banner = rememberInAppBanner()
    var cfg by remember { mutableStateOf(RemoteShop.config) }
    var tab by remember { mutableStateOf(ShopCategory.THEME) }
    var editing by remember { mutableStateOf<ShopItem?>(null) }
    var addTheme by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    val todayKey = remember { ir.sadteam.loancalc.core.JalaliCalendar.today().let { "%04d-%02d-%02d".format(java.util.Locale.US, it.y, it.m, it.d) } }

    val remoteThemeItems = cfg.themes.map { t ->
        ShopItem("theme:r_${t.id}", ir.sadteam.loancalc.data.coin.CoinSpend.THEME_PALETTE, "تمِ ${t.label}", "از سرور", priceOverride = t.price)
    }
    val items = (SHOP_CATALOG + remoteThemeItems).filter { it.kind.category == tab && !it.base }

    Box(Modifier.fillMaxSize()) {
        AdminPage("مدیریتِ فروشگاه", "تغییرات بعد از «ذخیره» برای همه اعمال می‌شود", onBack) {
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(ShopCategory.entries.size) { i ->
                    val c = ShopCategory.entries[i]
                    AppChip(label = c.tab, selected = c == tab, onClick = { tab = c })
                }
            }
            cfg.dealId?.let { id ->
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocalOffer, null, tint = AppPrimary, modifier = Modifier.size(18.dp))
                        Text(
                            "تخفیفِ امروز: ${(SHOP_CATALOG + remoteThemeItems).firstOrNull { it.id == id }?.label ?: id} · ${toFa(cfg.dealPercent ?: 30)}٪",
                            color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(start = 6.dp),
                        )
                        TextButton(onClick = { cfg = cfg.copy(dealId = null, dealPercent = null) }) { Text("برداشتن") }
                    }
                }
            }
            if (tab == ShopCategory.THEME) {
                GradientButton(onClick = { addTheme = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Add, null)
                    Text("تمِ رنگیِ تازه", modifier = Modifier.padding(start = 6.dp))
                }
            }
            AppCard(contentPadding = 4.dp) {
                items.forEachIndexed { i, item ->
                    val hidden = item.id in cfg.hidden
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable { editing = item }.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // همان پیش‌نمایشِ فروشگاهِ اصلی (خواسته‌ی کاربر ۱۰ مهر).
                        Box(Modifier.padding(vertical = 6.dp).alpha(if (hidden) 0.45f else 1f)) {
                            ir.sadteam.loancalc.ui.shop.previewFor(item)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(cfg.labels[item.id] ?: item.label, color = if (hidden) AppMuted else AppText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            val price = cfg.prices[item.id] ?: item.price
                            Text(
                                buildString {
                                    append("${toFa(price)} سکه")
                                    if (cfg.prices.containsKey(item.id)) append(" · قیمتِ سرور")
                                    if (cfg.newOn.containsKey(item.id)) append(" · تازه")
                                    if (item.id == cfg.dealId) append(" · تخفیفِ امروز")
                                    if (item.comingSoon) append(" · به‌زودی")
                                },
                                color = AppMuted, fontSize = 11.5.sp,
                            )
                        }
                        if (hidden) Icon(Icons.Filled.VisibilityOff, null, tint = AppMuted, modifier = Modifier.size(18.dp).padding(end = 4.dp))
                        Switch(
                            checked = !hidden,
                            onCheckedChange = { show -> cfg = cfg.copy(hidden = if (show) cfg.hidden - item.id else cfg.hidden + item.id) },
                        )
                    }
                    if (i < items.lastIndex) Box(Modifier.fillMaxWidth().padding(horizontal = 10.dp).heightIn(min = 1.dp).background(AppLine))
                }
            }
            AdminNote("کلیدِ خاموش = از ویترین برداشته می‌شود؛ کسی که خریده، نگهش می‌دارد. برای تغییرِ قیمت/نام/تخفیف روی ردیف بزن.")
            GradientButton(
                enabled = !saving && cfg != RemoteShop.config,
                onClick = {
                    saving = true
                    scope.launch {
                        val json = RemoteShop.toJson(cfg)
                        val ok = vm.repo.adminSetRemoteConfig("shop", json)
                        if (ok) RemoteShop.update(ctx, json)
                        banner.show(if (ok) "ذخیره شد؛ برای همه اعمال می‌شود" else "نرسید؛ اینترنت را چک کن", isSuccess = ok)
                        saving = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (cfg == RemoteShop.config) "تغییری نیست" else "ذخیره برای همه") }
        }
        InAppBannerHost(state = banner, modifier = Modifier.align(Alignment.BottomCenter))
    }

    editing?.let { item ->
        var price by remember(item) { mutableStateOf((cfg.prices[item.id] ?: item.price).toString()) }
        var label by remember(item) { mutableStateOf(cfg.labels[item.id] ?: item.label) }
        var isNew by remember(item) { mutableStateOf(cfg.newOn.containsKey(item.id)) }
        var deal by remember(item) { mutableStateOf(cfg.dealId == item.id) }
        var pct by remember(item) { mutableStateOf((cfg.dealPercent ?: 30).toString()) }
        JibakAlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(item.label) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(label, { label = it.take(40) }, label = { Text("نام") }, singleLine = true, shape = AdminFieldShape, colors = appFieldColors(), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(price, { price = cleanNum(it).take(6) }, label = { Text("قیمت (سکه)") }, singleLine = true, shape = AdminFieldShape, colors = appFieldColors(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("نشانِ «تازه» (۱۴ روز)", color = AppText, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Switch(checked = isNew, onCheckedChange = { isNew = it })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("تخفیفِ امروز", color = AppText, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Switch(checked = deal, onCheckedChange = { deal = it })
                    }
                    if (deal) OutlinedTextField(pct, { pct = cleanNum(it).take(2) }, label = { Text("درصدِ تخفیف") }, singleLine = true, shape = AdminFieldShape, colors = appFieldColors(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val p = price.toIntOrNull()
                    var c = cfg
                    c = c.copy(prices = if (p == null || p == item.price) c.prices - item.id else c.prices + (item.id to p))
                    c = c.copy(labels = if (label.isBlank() || label == item.label) c.labels - item.id else c.labels + (item.id to label))
                    c = c.copy(newOn = if (isNew) c.newOn + (item.id to (c.newOn[item.id] ?: todayKey)) else c.newOn - item.id)
                    c = if (deal) c.copy(dealId = item.id, dealPercent = pct.toIntOrNull()?.coerceIn(5, 90) ?: 30)
                    else if (c.dealId == item.id) c.copy(dealId = null, dealPercent = null) else c
                    // تمِ سرور: قیمت در خودِ تم است.
                    if (item.id.startsWith("theme:r_") && p != null) {
                        val tid = item.id.removePrefix("theme:r_")
                        c = c.copy(themes = c.themes.map { if (it.id == tid) it.copy(price = p, label = label.removePrefix("تمِ ")) else it }, prices = c.prices - item.id, labels = c.labels - item.id)
                    }
                    cfg = c
                    editing = null
                }) { Text("باشه") }
            },
            dismissButton = {
                if (item.id.startsWith("theme:r_")) {
                    TextButton(onClick = {
                        val tid = item.id.removePrefix("theme:r_")
                        cfg = cfg.copy(themes = cfg.themes.filter { it.id != tid })
                        editing = null
                    }) { Text("حذفِ تم", color = ir.sadteam.loancalc.ui.theme.AppDangerInk) }
                } else {
                    TextButton(onClick = { editing = null }) { Text("انصراف") }
                }
            },
        )
    }

    if (addTheme) {
        var name by remember { mutableStateOf("") }
        var primary by remember { mutableStateOf("#") }
        var dark by remember { mutableStateOf("#") }
        var light by remember { mutableStateOf("#") }
        var price by remember { mutableStateOf("150") }
        val ok = name.isNotBlank() && RemoteShop.parseHex(primary) != null
        JibakAlertDialog(
            onDismissRequest = { addTheme = false },
            title = { Text("تمِ رنگیِ تازه") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it.take(24) }, label = { Text("نام (مثلاً «یاقوتی»)") }, singleLine = true, shape = AdminFieldShape, colors = appFieldColors(), modifier = Modifier.fillMaxWidth())
                    ColorField("رنگِ اصلی", primary) { primary = it }
                    ColorField("تیره (پس‌زمینه‌ی تمِ تیره)", dark) { dark = it }
                    ColorField("روشن (برای تمِ تیره)", light) { light = it }
                    OutlinedTextField(price, { price = cleanNum(it).take(5) }, label = { Text("قیمت (سکه)") }, singleLine = true, shape = AdminFieldShape, colors = appFieldColors(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Text("رنگ‌ها را از ChatGPT بگیر: «سه hex برای تمِ … : اصلی، خیلی تیره، خیلی روشن».", color = AppMuted, fontSize = 11.sp)
                }
            },
            confirmButton = {
                TextButton(enabled = ok, onClick = {
                    val id = "t" + System.currentTimeMillis().toString(36)
                    cfg = cfg.copy(
                        themes = cfg.themes + RemoteTheme(
                            id = id, label = name.trim(), primary = primary, dark = dark.takeIf { RemoteShop.parseHex(it) != null } ?: primary,
                            light = light.takeIf { RemoteShop.parseHex(it) != null } ?: primary, ink = dark.takeIf { RemoteShop.parseHex(it) != null } ?: primary,
                            price = price.toIntOrNull() ?: 150, addedOn = todayKey,
                        ),
                    )
                    addTheme = false
                }) { Text("افزودن") }
            },
            dismissButton = { TextButton(onClick = { addTheme = false }) { Text("انصراف") } },
        )
    }
}

@Composable
private fun ColorField(label: String, value: String, onChange: (String) -> Unit) {
    val c = RemoteShop.parseHex(value)
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value, { onChange(("#" + it.removePrefix("#").filter { ch -> ch.isLetterOrDigit() }.take(6)).uppercase()) },
            label = { Text(label) }, singleLine = true, shape = AdminFieldShape, colors = appFieldColors(), modifier = Modifier.weight(1f),
        )
        Box(Modifier.padding(start = 8.dp).size(34.dp).clip(CircleShape).background(if (c != null) Color(c) else AppLine))
    }
}
