package ir.sadteam.loancalc.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Campaign
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppPurple
import ir.sadteam.loancalc.ui.theme.AppWarning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * 🛠 **ادمین** - همه‌ی ابزارهای مخصوصِ صاحبِ برنامه یک‌جا (خواسته‌ی کاربر، ۷ مهر: «منسجم و منظم»).
 * فقط برای حسابِ `is_admin` در تنظیمات دیده می‌شود. ابزارِ تازه‌ی ادمین = یک ردیفِ تازه همین‌جا.
 */
@Composable
fun AdminHubScreen(onBack: () -> Unit, supportVm: SupportInboxViewModel = hiltViewModel()) {
    var page by remember { mutableStateOf<String?>(null) }
    var gift by remember { mutableStateOf<String?>(null) }
    when (page) {
        "stats" -> { AdminStatsScreen(onBack = { page = null }); return }
        "support" -> { SupportInboxScreen(onBack = { page = null }, viewModel = supportVm); return }
        "digest" -> { AdminDigestScreen(onBack = { page = null }); return }
        "money" -> { AdminMoneyScreen(onBack = { page = null }); return }
        "user" -> { AdminUserScreen(onBack = { page = null }); return }
        "broadcast" -> { AdminBroadcastScreen(onBack = { page = null }); return }
    }
    val items by supportVm.items.collectAsState()
    LaunchedEffect(Unit) { supportVm.load() }
    val open = items?.count { it.status == "open" } ?: 0
    BackHandler(onBack = onBack)
    Column(
        Modifier.fillMaxSize().background(AppBg).verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowForward, "بازگشت", tint = AppText) }
            Column(Modifier.padding(start = 4.dp)) {
                Text("ادمین", color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text("ابزارهای مدیریتِ جیبک - فقط برای تو", color = AppMuted, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            }
        }
        // بازطراحیِ ۸ مهر (طرحِ ChatGPT): گزارشِ روز بالا، چهار کاشیِ ۲×۲، پیام‌های اخیر.
        HubTile(Icons.Filled.Today, "گزارشِ امروز", "از ۷ صبح تا حالا · مقایسه با دیروز", AppPrimary, Modifier.fillMaxWidth()) { page = "digest" }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HubTile(Icons.Filled.SupportAgent, "پیام‌های کاربران", if (open > 0) "${toFa(open)} بی‌جواب" else "همه جواب گرفته‌اند", AppPrimary, Modifier.weight(1f), badge = open) { page = "support" }
            HubTile(Icons.Filled.BarChart, "گزارشِ برنامه", "کاربران، زمان، بخش‌ها", AppInfo, Modifier.weight(1f)) { page = "stats" }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HubTile(Icons.Filled.WorkspacePremium, "هدیه‌ی اشتراک", "روز به یک Uid", AppWarning, Modifier.weight(1f)) { gift = "sub" }
            HubTile(Icons.Filled.MonetizationOn, "هدیه‌ی سکه", "سکه به یک Uid", AppPurple, Modifier.weight(1f)) { gift = "coins" }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HubTile(Icons.Filled.Payments, "پول و فروش", "تبدیل، تمدید، A/B", AppPrimary, Modifier.weight(1f)) { page = "money" }
            HubTile(Icons.Filled.PersonSearch, "تاریخچه‌ی کاربر", "با شماره‌ی کاربری", AppInfo, Modifier.weight(1f)) { page = "user" }
        }
        HubTile(Icons.Filled.Campaign, "پیامِ گروهی", "مثلاً به نیامده‌ها یا کسانی که مجانی‌شان تمام می‌شود", AppWarning, Modifier.fillMaxWidth()) { page = "broadcast" }
        val recent = items.orEmpty().take(4)
        if (recent.isNotEmpty()) {
            AppCard(label = "پیام‌های اخیرِ کاربران") {
                recent.forEach { m ->
                    Row(
                        Modifier.fillMaxWidth().clickable { page = "support" }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(AppPrimaryPill),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Filled.SupportAgent, null, tint = AppPrimaryInk, modifier = Modifier.size(18.dp)) }
                        Column(Modifier.weight(1f).padding(start = 10.dp)) {
                            Text(m.userId?.let { "کاربر #${toFa(it)}" } ?: "مهمان", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(m.message, color = AppMuted, fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                        if (m.status == "open") Box(Modifier.size(8.dp).clip(RoundedCornerShape(99.dp)).background(AppDanger))
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

@Composable
private fun HubRow(icon: ImageVector, title: String, subtitle: String, badge: Int = 0, tint: androidx.compose.ui.graphics.Color? = null, onClick: () -> Unit) {
    AppCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val t = tint ?: AppPrimaryInk
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(if (tint == null) AppPrimaryPill else t.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) { Icon(icon, null, tint = t, modifier = Modifier.size(22.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text(subtitle, color = AppMuted, fontSize = 12.5.sp)
            }
            if (badge > 0) {
                Text(
                    toFa(badge), color = androidx.compose.ui.graphics.Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black,
                    modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(AppDanger).padding(horizontal = 9.dp, vertical = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun HubTile(
    icon: ImageVector, title: String, subtitle: String, tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier, badge: Int = 0, onClick: () -> Unit,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .background(tint.copy(alpha = 0.10f))
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Column {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(tint.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
            }
            Text(title, color = AppText, fontSize = 14.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 10.dp))
            Text(subtitle, color = AppMuted, fontSize = 11.5.sp, maxLines = 1)
        }
        if (badge > 0) {
            Text(
                toFa(badge), color = androidx.compose.ui.graphics.Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.TopEnd).clip(RoundedCornerShape(999.dp)).background(AppDanger).padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
    }
}
