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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.WorkspacePremium
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
) {
    var page by remember { mutableStateOf<String?>(null) }
    var gift by remember { mutableStateOf<String?>(null) }
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

        // طرحِ ChatGPT (۱۰ مهر): شش کاشیِ رنگی، بعد ردیف‌های فروشگاه/تنظیمات/پیام‌ها.
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ColorTile(Icons.Filled.BarChart, "گزارشِ برنامه", "کاربران، آمار، سلامت", Color(0xFF007950), Color(0xFF00E89A), Modifier.weight(1f)) { page = "stats" }
            ColorTile(Icons.Filled.AccountBalanceWallet, "پول و فروش", "تمدید، A/B", Color(0xFF4D3B11), Color(0xFFFFD34F), Modifier.weight(1f)) { page = "money" }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ColorTile(Icons.Filled.PersonAdd, "تاریخچه‌ی کاربر", "با شماره‌ی کاربری (Uid)", Color(0xFF451E90), Color(0xFFB46CFF), Modifier.weight(1f)) { page = "user" }
            ColorTile(Icons.Filled.Forum, "پیامِ گروهی", "به یک گروهِ خاص", Color(0xFF062955), Color(0xFF20BFFF), Modifier.weight(1f)) { page = "broadcast" }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ColorTile(Icons.Filled.CardGiftcard, "هدیه‌ی سکه", "سکه به یک کاربر", Color(0xFF6C2E61), Color(0xFFF05BDA), Modifier.weight(1f)) { gift = "coins" }
            ColorTile(Icons.Filled.Bookmark, "هدیه‌ی اشتراک", "روز به یک کاربر", Color(0xFF564314), Color(0xFFFFE49A), Modifier.weight(1f)) { gift = "sub" }
        }
        HubRow(Icons.Filled.SupportAgent, "پیام‌های کاربران", if (open > 0) "${toFa(open)} بی‌جواب" else "همه جواب گرفته‌اند", Color(0xFF19C9FF), badge = open) { page = "support" }
        HubRow(Icons.Filled.Storefront, "مدیریتِ فروشگاه", "قیمت، پنهان کردن، تمِ تازه، تخفیف", Color(0xFF00E89A)) { page = "shop" }
        HubRow(Icons.Filled.Tune, "تنظیماتِ از راهِ دور", "رایگان، پیامکِ بانک، نسخه، نظرسنجی…", Color(0xFF19C9FF)) { page = "remote" }

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
                Text("از ۷ صبح تا حالا · مقایسه با دیروز", color = AppMuted, fontSize = 11.5.sp)
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

/** کاشیِ رنگیِ هاب - رنگ‌ها از SPEC.mdِ ChatGPT (۱۰ مهر): زمینه‌ی [tile]، آیکون و حاشیه [accent]. */
@Composable
private fun ColorTile(icon: ImageVector, title: String, subtitle: String, tile: Color, accent: Color, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier
            .heightIn(min = 128.dp)
            .clip(shape)
            .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(tile, androidx.compose.ui.graphics.lerp(tile, Color(0xFF0A2129), 0.55f))))
            .border(1.5.dp, accent.copy(alpha = 0.35f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(40.dp))
        Text(title, color = Color(0xFFF5F7F8), fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 10.dp))
        Text(subtitle, color = Color(0xFFF5F7F8).copy(alpha = 0.72f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** ردیفِ هاب با کاشیِ آیکونِ رنگی. */
@Composable
private fun HubRow(icon: ImageVector, title: String, subtitle: String, color: Color, badge: Int = 0, onClick: () -> Unit) {
    AppCard(modifier = Modifier.clickable(onClick = onClick), contentPadding = 10.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(color.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(title, color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Text(subtitle, color = AppMuted, fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (badge > 0) Text(
                toFa(badge), color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Black,
                modifier = Modifier.padding(end = 6.dp).clip(RoundedCornerShape(999.dp)).background(AppDanger).padding(horizontal = 7.dp, vertical = 2.dp),
            )
            Icon(Icons.Filled.ChevronLeft, null, tint = AppMuted, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun HubTile(icon: ImageVector, title: String, subtitle: String, pill: Color, ink: Color, modifier: Modifier, badge: Int = 0, onClick: () -> Unit) {
    Box(modifier) {
        AppCard(modifier = Modifier.clickable(onClick = onClick), horizontalPadding = 14.dp, contentPadding = 12.dp) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(AppRadius.icon)).background(pill), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = ink, modifier = Modifier.size(21.dp))
            }
            Text(title, color = AppText, fontSize = 13.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 8.dp))
            Text(subtitle, color = AppMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (badge > 0) {
            Text(
                toFa(badge), color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).clip(RoundedCornerShape(999.dp)).background(AppDanger).padding(horizontal = 7.dp, vertical = 2.dp),
            )
        }
    }
}
