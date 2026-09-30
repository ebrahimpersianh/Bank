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
        // بازطراحیِ ۸ مهر: کارتِ خلاصه‌ی بالا + ردیف‌های رنگی (پیام، گزارش، دو هدیه).
        ir.sadteam.loancalc.ui.components.AppHeroCard {
            Text("پیام‌های باز", color = ir.sadteam.loancalc.ui.components.HeroMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(
                if (items == null) "…" else toFa(open),
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                if (open > 0) "منتظرِ جوابِ تو" else "همه‌ی پیام‌ها جواب گرفته‌اند",
                color = ir.sadteam.loancalc.ui.components.HeroMuted,
                fontSize = 12.sp,
            )
        }
        Text("پشتیبانی و آمار", color = AppMuted, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp, start = 4.dp))
        HubRow(Icons.Filled.SupportAgent, "پیام‌های کاربران", "مشکل، طراحی، پیشنهاد · جواب و هدیه", badge = open) { page = "support" }
        HubRow(Icons.Filled.BarChart, "گزارشِ برنامه", "کاربران، استفاده، فروش، خطاها", tint = AppInfo) { page = "stats" }
        Text("هدیه", color = AppMuted, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp, start = 4.dp))
        HubRow(Icons.Filled.WorkspacePremium, "هدیه‌ی اشتراک", "چند روز اشتراک به یک شماره‌ی کاربری", tint = AppWarning) { gift = "sub" }
        HubRow(Icons.Filled.MonetizationOn, "هدیه‌ی سکه", "سکه به کیفِ یک کاربر", tint = AppPurple) { gift = "coins" }
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
