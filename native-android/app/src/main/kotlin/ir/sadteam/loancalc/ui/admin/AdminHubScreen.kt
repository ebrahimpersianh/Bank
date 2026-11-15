package ir.sadteam.loancalc.ui.admin

import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import ir.sadteam.loancalc.ui.text.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppGoldInkSoft
import ir.sadteam.loancalc.ui.theme.AppGoldPillSoft
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppInfoPill
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppPurple
import ir.sadteam.loancalc.ui.theme.AppPurplePill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * 🛠 **ادمین** (بازطراحیِ بخشِ ۸۲). فقط برای حسابِ `is_admin`.
 * بالا «امروز» در یک نگاه (از همان `adminDigest("day")`)، بعد هشدارها، بعد دو گروهِ «دیدن» و
 * «کار کردن». ابزارِ تازه‌ی ادمین = یک ردیف/کاشیِ تازه در یکی از همین دو گروه.
 */
@Composable
fun AdminHubScreen(
    onBack: () -> Unit,
    supportVm: SupportInboxViewModel = hiltViewModel(),
    digestVm: AdminDigestViewModel = hiltViewModel(),
    proVm: AdminProViewModel = hiltViewModel(),
) {
    var page by remember { mutableStateOf<String?>(null) }
    var gift by remember { mutableStateOf<String?>(null) }
    // برگشتِ گوشی از زیرصفحه‌ی ادمین = برگشت به هاب، نه بستنِ کلِ پنل.
    androidx.activity.compose.BackHandler(enabled = page != null) {
        if (page == "digest") digestVm.resetToToday()
        page = null
    }
    when (page) {
        "stats" -> { AdminStatsScreen(onBack = { page = null }); return }
        "support" -> { SupportInboxScreen(onBack = { page = null }, viewModel = supportVm); return }
        // همان نمونه‌ی ViewModel؛ برگشت دوره را به «امروز/همه» برمی‌گرداند تا هیروی هاب درست بماند.
        "digest" -> { AdminDigestScreen(onBack = { page = null; digestVm.resetToToday() }, vm = digestVm); return }
        "money" -> { AdminMoneyScreen(onBack = { page = null }); return }
        "user" -> { AdminUserScreen(onBack = { page = null }); return }
        "broadcast" -> { AdminBroadcastScreen(onBack = { page = null }); return }
        "shop" -> { AdminShopScreen(onBack = { page = null }); return }
        "remote" -> { AdminRemoteScreen(onBack = { page = null }); return }
    }
    val items by supportVm.items.collectAsState()
    val digest by digestVm.data.collectAsState()
    LaunchedEffect(Unit) {
        supportVm.load()
        if (digestVm.data.value == null) digestVm.load("day")
    }
    val open = items?.count { it.status == "open" } ?: 0

    AdminPage("ادمین", "ابزارهای مدیریتِ جیبک · فقط برای تو", onBack) {
        TodayHero(digest) { page = "digest" }
        AdminAlerts(digest?.notes.orEmpty().map { digestNoteIcon(it) to digestNoteText(it) }) { page = if (digest?.notes.orEmpty().any { it.startsWith("open_support:") }) "support" else "digest" }

        // ۱۳ آبان (خواسته‌ی کاربر: «خیلی خر تو خره»، و «یک خرید شد و نمی‌دانستم کیست»):
        // ۱) آخرین خریدها همین بالا - چه کسی، کی، کدام پلن، بازار یا مایکت.
        // ۲) شش کاشیِ شش‌رنگ → دو فهرستِ مرتب به سبکِ تنظیمات (یک رنگ، یک الگو).
        LatestPurchasesCard(proVm) { page = "money" }

        ir.sadteam.loancalc.ui.settings.SettingsGroupLabel("گزارش‌ها")
        ir.sadteam.loancalc.ui.settings.SettingsGroup {
            ir.sadteam.loancalc.ui.settings.SettingsRowItem("گزارشِ امروز", Icons.Filled.Today, status = "فعال، نصب، خرید - مقایسه با دیروز", onClick = { page = "digest" })
            ir.sadteam.loancalc.ui.settings.SettingsDivider()
            ir.sadteam.loancalc.ui.settings.SettingsRowItem("پول و فروش", Icons.Filled.AccountBalanceWallet, status = "همه‌ی خریدها، تمدید، بازار و مایکت", onClick = { page = "money" })
            ir.sadteam.loancalc.ui.settings.SettingsDivider()
            ir.sadteam.loancalc.ui.settings.SettingsRowItem("آمارِ برنامه", Icons.Filled.BarChart, status = "کاربران، استفاده، سلامت", onClick = { page = "stats" })
        }
        ir.sadteam.loancalc.ui.settings.SettingsGroupLabel("کارها")
        ir.sadteam.loancalc.ui.settings.SettingsGroup {
            ir.sadteam.loancalc.ui.settings.SettingsRowItem(
                "پیام‌های کاربران", Icons.Filled.SupportAgent,
                status = if (open > 0) "${toFa(open)} پیامِ بی‌جواب" else "همه جواب گرفته‌اند",
                statusTone = if (open > 0) ir.sadteam.loancalc.ui.settings.StatusTone.BROKEN else ir.sadteam.loancalc.ui.settings.StatusTone.NEUTRAL,
                onClick = { page = "support" },
            )
            ir.sadteam.loancalc.ui.settings.SettingsDivider()
            ir.sadteam.loancalc.ui.settings.SettingsRowItem("کاربران", Icons.Filled.PersonSearch, status = "جستجو با شماره یا Uid", onClick = { page = "user" })
            ir.sadteam.loancalc.ui.settings.SettingsDivider()
            ir.sadteam.loancalc.ui.settings.SettingsRowItem("پیامِ گروهی", Icons.Filled.Forum, status = "به همه یا یک گروهِ خاص", onClick = { page = "broadcast" })
            ir.sadteam.loancalc.ui.settings.SettingsDivider()
            ir.sadteam.loancalc.ui.settings.SettingsRowItem("هدیه‌ی سکه", Icons.Filled.CardGiftcard, status = "سکه به یک کاربر", onClick = { gift = "coins" })
            ir.sadteam.loancalc.ui.settings.SettingsDivider()
            ir.sadteam.loancalc.ui.settings.SettingsRowItem("هدیه‌ی اشتراک", Icons.Filled.Bookmark, status = "چند روز اشتراک به یک کاربر", onClick = { gift = "sub" })
        }
        ir.sadteam.loancalc.ui.settings.SettingsGroupLabel("تنظیمات")
        ir.sadteam.loancalc.ui.settings.SettingsGroup {
            ir.sadteam.loancalc.ui.settings.SettingsRowItem("فروشگاه", Icons.Filled.Storefront, status = "قیمت، پنهان‌کردن، تمِ تازه، تخفیف", onClick = { page = "shop" })
            ir.sadteam.loancalc.ui.settings.SettingsDivider()
            ir.sadteam.loancalc.ui.settings.SettingsRowItem("تنظیماتِ از راهِ دور", Icons.Filled.Tune, status = "رایگان، پیامکِ بانک، نسخه، نظرسنجی", onClick = { page = "remote" })
        }

        val recent = items.orEmpty().take(4)
        if (recent.isNotEmpty()) {
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("پیام‌های اخیرِ کاربران", color = AppMuted, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                    Text("همه", color = AppPrimaryInk, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.clickable { page = "support" }.padding(8.dp))
                }
                recent.forEachIndexed { i, m ->
                    if (i > 0) Box(Modifier.fillMaxWidth().height(1.5.dp).background(AppLineRow))
                    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).clickable { page = "support" }, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Ltr { Text(supportUid(m.userId), color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Black) }
                                Text(supportTime(m.createdAt), color = AppLabel, fontSize = 11.sp, modifier = Modifier.padding(start = 8.dp))
                            }
                            Text(m.message, color = AppMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        if (m.status == "open") Box(Modifier.padding(start = 8.dp).size(8.dp).clip(RoundedCornerShape(99.dp)).background(AppDanger))
                    }
                }
            }
        }
    }
    gift?.let { mode ->
        AdminGiftDialog(
            coinsMode = mode == "coins",
            onDismiss = { gift = null },
            onSend = { user, n, text, done ->
                supportVm.giftUser(user, if (mode == "sub") n else 0, if (mode == "coins") n else 0, text, done)
            },
        )
    }
}

/** «امروز» در یک نگاه: فعال، خرید، فروشِ خالص - هر کدام با فلشِ نسبت به دیروز. کلِ کارت = گزارشِ امروز. */
@Composable
private fun TodayHero(d: ir.sadteam.loancalc.data.network.AdminDigestResponse?, onOpen: () -> Unit) {
    val by = d?.metrics.orEmpty().associateBy { it.key }
    AppCard(modifier = Modifier.clickable(onClick = onOpen)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("امروز", color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text("از ساعتِ ۰۰:۰۰ تا حالا · مقایسه با دیروز", color = AppMuted, fontSize = 11.5.sp)
            }
            Text("گزارشِ امروز", color = AppPrimaryInk, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
            Icon(Icons.Filled.ChevronLeft, null, tint = AppPrimaryInk, modifier = Modifier.size(18.dp))
        }
        Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("active" to "کاربرِ فعال", "purchases" to "خرید", "revenue_net" to "فروشِ خالص").forEachIndexed { i, (key, label) ->
                val m = by[key]
                KpiTile(
                    label = label,
                    value = m?.let { adminNum(it.now) } ?: "…",
                    delta = m?.let { adminDelta(it.now, it.prev) },
                    modifier = Modifier.weight(if (i == 2) 1.35f else 1f),
                )
            }
        }
    }
}

/**
 * «آخرین خریدها» بالای هابِ ادمین (۱۳ آبان). داده همان `recent`ِ گزارشِ پول است (سرور، ۵۰ خریدِ
 * آخر)؛ این‌جا سه‌تای اول، «همه» = صفحه‌ی پول و فروش.
 */
@Composable
private fun LatestPurchasesCard(vm: AdminProViewModel, onAll: () -> Unit) {
    val m by vm.money.collectAsState()
    LaunchedEffect(Unit) { if (vm.money.value == null) vm.loadMoney() }
    val recent = m?.recent.orEmpty().take(3)
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.ShoppingBag, null, tint = AppPrimaryInk, modifier = Modifier.size(18.dp))
            Text("آخرین خریدها", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 6.dp).weight(1f))
            Text("همه", color = AppPrimaryInk, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.clickable(onClick = onAll).padding(8.dp))
        }
        when {
            m == null -> Text("در حالِ گرفتن…", color = AppMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
            recent.isEmpty() -> Text("هنوز خریدی ثبت نشده.", color = AppMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
            else -> recent.forEachIndexed { i, r ->
                if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(AppLineRow))
                Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Uid ${toFa(r.code)}", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black)
                            Ltr { Text("  ${r.phone}", color = AppMuted, fontSize = 11.sp) }
                        }
                        Text(
                            "${PLAN_LABELS[r.product] ?: r.product} | ${STORE_LABELS[r.store] ?: r.store} | ${adminDay(r.at)} ${adminHourOf(r.at)}",
                            color = AppMuted, fontSize = 11.sp, maxLines = 1,
                        )
                    }
                    if (r.price > 0) Text("${adminNum(r.price)} ت", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
