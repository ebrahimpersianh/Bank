package ir.sadteam.loancalc.ui.admin

import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.AuthRepository
import ir.sadteam.loancalc.data.network.AdminAbGroup
import ir.sadteam.loancalc.data.network.AdminMoneyResponse
import ir.sadteam.loancalc.data.network.AdminUserTimeline
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppCardVariant
import ir.sadteam.loancalc.ui.components.AppFieldShape
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppGoldBorder
import ir.sadteam.loancalc.ui.theme.AppGoldInk
import ir.sadteam.loancalc.ui.theme.AppGoldInk2
import ir.sadteam.loancalc.ui.theme.AppGoldInkSoft
import ir.sadteam.loancalc.ui.theme.AppGoldPillSoft
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMarkOff
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AdminProViewModel @Inject constructor(val repo: AuthRepository) : ViewModel() {
    private val _money = MutableStateFlow<AdminMoneyResponse?>(null)
    val money: StateFlow<AdminMoneyResponse?> = _money
    private val _moneyFailed = MutableStateFlow(false)
    val moneyFailed: StateFlow<Boolean> = _moneyFailed

    fun loadMoney() = viewModelScope.launch {
        _moneyFailed.value = false
        val r = repo.adminMoney()
        _money.value = r
        _moneyFailed.value = r == null
    }
}

private fun pct(a: Int, b: Int): String = if (b <= 0) "—" else "${toFa(a * 100 / b)}٪"

/** 💰 پول (بخشِ ۸۲): هیروی طلایی با کل + ۳۰ روز، دو نسبت، درآمد به‌ازای نفر، آزمایشِ A/B. */
@Composable
fun AdminMoneyScreen(onBack: () -> Unit, vm: AdminProViewModel = hiltViewModel()) {
    val m by vm.money.collectAsState()
    val failed by vm.moneyFailed.collectAsState()
    // ۳۰ روزِ اخیر: دقیق از ِ سرور (خالص با سهمِ واقعیِ هر استور).
    val gross30 = m?.daily?.takeIf { it.isNotEmpty() }?.sumOf { it.gross }
    val net30 = m?.daily?.takeIf { it.isNotEmpty() }?.sumOf { it.net }
    LaunchedEffect(Unit) { vm.loadMoney() }
    AdminPage("پول و فروش", "از کلِ عمرِ برنامه", onBack) {
        val d = m
        when {
            failed -> Text("نرسید؛ اینترنت را چک کن.", color = AppDangerInk)
            d == null -> Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AppPrimary) }
            else -> {
                AppCard(variant = AppCardVariant.GOLD) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.WorkspacePremium, null, tint = AppGoldInk2, modifier = Modifier.size(18.dp))
                        Text("فروشِ خالص · سهمِ تو", color = AppGoldInk2, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 6.dp))
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        MoneyColumn("کلِ عمرِ برنامه", adminNum(d.netTotal.toLong()), "ناخالص ${adminNum(d.grossTotal.toLong())}", Modifier.weight(1.25f))
                        Box(Modifier.padding(horizontal = 12.dp).width(1.5.dp).height(64.dp).background(AppGoldBorder))
                        MoneyColumn(
                            "۳۰ روزِ اخیر",
                            net30?.let { adminNum(it) } ?: "…",
                            gross30?.let { "ناخالص ${adminNum(it)}" } ?: "",
                            Modifier.weight(1f),
                        )
                    }
                    if (d.daily.any { it.net > 0 }) {
                        AdminColumnChart(values = d.daily.map { (it.net / 1000).toInt() }, color = AppGoldInk, height = 64.dp)
                    }
                    AdminNote("همه به تومان · خالص = بعد از کارمزدِ استور. ۳۰ روز = ۳۰ روزِ اخیرِ ایران.", gold = true)
                }
                AppCard {
                    RatioRow("مجانی ← خرید", d.converted, d.trialEnded, "${toFa(d.converted)} از ${toFa(d.trialEnded)} نفری که ۳۰ روزشان تمام شد")
                    Spacer(Modifier.height(14.dp))
                    RatioRow("تمدید", d.renewed, d.expiredPayers, "${toFa(d.renewed)} از ${toFa(d.expiredPayers)} اشتراکِ تمام‌شده")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiTile("درآمد به‌ازای هر کاربر", if (d.users > 0) adminNum((d.netTotal / d.users).toLong()) else "—", Modifier.weight(1f), unit = "تومان", prevText = "خالص · ${adminNum(d.users)} کاربر", standalone = true)
                    KpiTile("درآمد به‌ازای هر خریدار", if (d.payers > 0) adminNum((d.netTotal / d.payers).toLong()) else "—", Modifier.weight(1f), unit = "تومان", prevText = "خالص · ${adminNum(d.payers)} خریدار", standalone = true)
                }
                AbCard(d)
            }
        }
    }
}

@Composable
private fun MoneyColumn(label: String, value: String, sub: String, modifier: Modifier) {
    Column(modifier) {
        Text(label, color = AppGoldInk2, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
        Text(value, color = AppGoldInk, fontSize = 24.sp, fontWeight = FontWeight.Black, maxLines = 1)
        Text(sub, color = AppGoldInk2, fontSize = 11.5.sp, maxLines = 1)
    }
}

@Composable
private fun RatioRow(label: String, part: Int, whole: Int, sub: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(label, color = AppText, fontSize = 13.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
        Text(pct(part, whole), color = AppPrimaryInk, fontSize = 22.sp, fontWeight = FontWeight.Black)
    }
    Box(Modifier.fillMaxWidth().padding(top = 6.dp).height(10.dp).clip(RoundedCornerShape(99.dp)).background(AppSurface2)) {
        Box(Modifier.fillMaxWidth(if (whole > 0) (part.toFloat() / whole).coerceIn(0f, 1f) else 0f).height(10.dp).clip(RoundedCornerShape(99.dp)).background(AppPrimary))
    }
    Text(sub, color = AppMuted, fontSize = 11.5.sp, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun AbCard(d: AdminMoneyResponse) {
    val ra = if (d.abA.installs > 0) d.abA.purchases * 1000 / d.abA.installs else 0
    val rb = if (d.abB.installs > 0) d.abB.purchases * 1000 / d.abB.installs else 0
    val early = d.abA.installs + d.abB.installs < 100
    val verdict = when {
        early -> "هنوز زود است"
        ra == rb -> "فعلاً فرقی ندارند"
        ra > rb -> "فعلاً A بهتر می‌فروشد"
        else -> "فعلاً B بهتر می‌فروشد"
    }
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("آزمایشِ A/B · جمله‌ی صفحه‌ی اشتراک", color = AppMuted, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
            Text(verdict, color = AppPrimaryInk, fontSize = 11.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(AppPrimaryPill).padding(horizontal = 10.dp, vertical = 4.dp))
        }
        val max = maxOf(ra, rb).coerceAtLeast(1)
        AbRow("A", "«امکاناتِ بیشتر، تجربه‌ی کامل‌تر»", d.abA, ra, max, winner = !early && ra > rb)
        AbRow("B", "«همه‌ی امکانات، بدونِ هیچ محدودیتی»", d.abB, rb, max, winner = !early && rb > ra)
        if (early) AdminNote("دست‌کم ۱۰۰ نفر لازم است تا نتیجه قابلِ اعتماد شود.")
    }
}

@Composable
private fun AbRow(key: String, quote: String, g: AdminAbGroup, rate: Int, max: Int, winner: Boolean) {
    Column(Modifier.padding(top = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(26.dp).clip(RoundedCornerShape(9.dp)).background(AppSurface2), contentAlignment = Alignment.Center) {
                Text(key, color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
            Text(quote, color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f).padding(start = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(pct(g.purchases, g.installs), color = if (winner) AppPrimaryInk else AppMuted, fontSize = 15.sp, fontWeight = FontWeight.Black)
        }
        Box(Modifier.fillMaxWidth().padding(top = 5.dp).height(10.dp).clip(RoundedCornerShape(99.dp)).background(AppSurface2)) {
            Box(Modifier.fillMaxWidth(rate.toFloat() / max).height(10.dp).clip(RoundedCornerShape(99.dp)).background(if (winner) AppPrimary else AppMarkOff))
        }
        Text("${adminNum(g.installs)} نفر دیدند · ${adminNum(g.paywallViews)} بازدید · ${adminNum(g.purchases)} خرید", color = AppLabel, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
    }
}

/** 👥 کاربران: فهرست + جستجو با شماره‌ی موبایل یا Uid؛ تپ = تاریخچه‌ی همان کاربر (۱۳ مهر). */
@Composable
fun AdminUserScreen(onBack: () -> Unit, vm: AdminProViewModel = hiltViewModel()) {
    var query by remember { mutableStateOf("") }
    var users by remember { mutableStateOf<List<ir.sadteam.loancalc.data.network.AdminUserRow>?>(null) }
    var listFailed by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<AdminUserTimeline?>(null) }
    var failed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(query) {
        kotlinx.coroutines.delay(if (query.isEmpty()) 0 else 400)
        val r = vm.repo.adminUsers(query.filter { it.isDigit() })
        listFailed = r == null
        users = r
    }
    AdminPage(
        if (result == null) "کاربران" else "تاریخچه‌ی کاربر",
        if (result == null) "جستجو با شماره‌ی موبایل یا شماره‌ی کاربری" else "Uid:${result?.code.orEmpty().removePrefix("Uid:")}",
        { if (result != null) result = null else onBack() },
    ) {
        val r = result
        if (r != null) {
            if (!r.found) Text("کاربری با این شماره پیدا نشد.", color = AppDangerInk) else UserResult(r)
            return@AdminPage
        }
        Ltr {
            OutlinedTextField(
                value = query, onValueChange = { query = it.take(20) }, singleLine = true,
                placeholder = { Text("مثلاً 0912… یا 7405024") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = adminFieldColors(), shape = AdminFieldShape, modifier = Modifier.fillMaxWidth(),
            )
        }
        if (loading) Text("در حالِ گرفتن…", color = AppMuted, fontSize = 12.sp)
        if (failed) Text("نرسید؛ اینترنت را چک کن.", color = AppDangerInk)
        val list = users
        when {
            listFailed -> Text("فهرست نرسید؛ اینترنت را چک کن.", color = AppDangerInk)
            list == null -> Text("…", color = AppMuted)
            list.isEmpty() -> Text("کاربری پیدا نشد.", color = AppMuted)
            else -> {
                Text("${toFa(list.size)} کاربر" + if (list.size >= 300) " (۳۰۰ تای آخر)" else "", color = AppMuted, fontSize = 12.sp)
                list.forEach { u ->
                    val active = u.subscribedUntil?.let { runCatching { java.time.Instant.parse(it).isAfter(java.time.Instant.now()) }.getOrNull() } == true
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                loading = true; failed = false
                                scope.launch { result = vm.repo.adminUser(u.code); failed = result == null; loading = false }
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Ltr { Text(toFa(u.phone), color = AppText, fontSize = 13.5.sp, fontWeight = FontWeight.Bold) }
                            Text(
                                "Uid:${u.code} · " + (if (u.store == "myket") "مایکت" else if (u.store == "cafebazaar") "کافه‌بازار" else "—") +
                                    (u.lastDay?.let { " · آخرین بار $it" } ?: ""),
                                color = AppMuted, fontSize = 11.sp,
                            )
                        }
                        Text(
                            when { u.paid -> "خریدار"; active -> "اشتراکِ فعال"; else -> "رایگان" },
                            color = if (u.paid) AppPrimary else AppMuted,
                            fontSize = 11.5.sp, fontWeight = FontWeight.Black,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoLine(icon: androidx.compose.ui.graphics.vector.ImageVector, color: androidx.compose.ui.graphics.Color, label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Text(label, color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(start = 10.dp))
        Text(value, color = AppMuted, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun UserResult(r: AdminUserTimeline) {
    val subscribed = r.subscribedUntil != null
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Ltr { Text("Uid:${r.code}", color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Black) }
                Text("ثبت‌نام ${adminDay(r.createdAt)}", color = AppMuted, fontSize = 11.5.sp)
            }
            Text(
                if (subscribed) "اشتراک تا ${adminDay(r.subscribedUntil)}" else "بی اشتراک",
                color = if (subscribed) AppGoldInkSoft else AppMuted, fontSize = 11.5.sp, fontWeight = FontWeight.Black,
                modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(if (subscribed) AppGoldPillSoft else AppSurface2).padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
        Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiTile("روزِ فعال", adminNum(r.installs.sumOf { it.activeDays }), Modifier.weight(1f))
            KpiTile("خرید", adminNum(r.purchases.size), Modifier.weight(1f))
            KpiTile("پیامِ پشتیبانی", adminNum(r.supportCount), Modifier.weight(1f))
        }
        r.lastSupport?.let { AdminNote("آخرین پیام: $it") }
    }
    // زبانه‌ها مثلِ طرحِ ChatGPT (۱۰ مهر).
    var tab by remember(r.code) { mutableStateOf(0) }
    AdminTabs(tabs = listOf("خلاصه", "خرید", "فعالیت", "گوشی"), selected = tab, onSelect = { tab = it })
    if (tab == 0) AppCard(label = "اطلاعاتِ کلی") {
        val lastDay = r.installs.mapNotNull { it.lastDay }.maxOrNull()
        InfoLine(androidx.compose.material.icons.Icons.Filled.Person, androidx.compose.ui.graphics.Color(0xFF22C55E), "آخرین حضور", lastDay?.let { adminDay(it) } ?: "—")
        InfoLine(androidx.compose.material.icons.Icons.Filled.ShoppingCart, androidx.compose.ui.graphics.Color(0xFFF59E0B), "خریدِ اشتراک", toFa(r.purchases.size))
        InfoLine(androidx.compose.material.icons.Icons.Filled.CalendarMonth, androidx.compose.ui.graphics.Color(0xFF3B82F6), "آخرین تاریخِ خرید", r.purchases.maxByOrNull { it.at }?.let { adminDay(it.at) } ?: "—")
        InfoLine(androidx.compose.material.icons.Icons.Filled.Event, androidx.compose.ui.graphics.Color(0xFFA855F7), "روزهای فعال", toFa(r.installs.sumOf { it.activeDays }))
        InfoLine(androidx.compose.material.icons.Icons.Filled.Mail, androidx.compose.ui.graphics.Color(0xFFEF4444), "پیام‌های ارسالی", toFa(r.supportCount))
    }
    if (tab == 2 && r.days.isNotEmpty()) AppCard(label = "۱۴ روزِ آخر · صفحه‌های دیده‌شده") {
        AdminColumnChart(r.days.map { it.screens }, height = 90.dp)
        Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Text(adminDay(r.days.first().day), color = AppLabel, fontSize = 11.sp, modifier = Modifier.weight(1f))
            Text(adminDay(r.days.last().day), color = AppLabel, fontSize = 11.sp)
        }
        AdminNote("جمعِ ${adminNum(r.days.sumOf { it.events })} رویداد در این ۱۴ روز.")
    }
    if (tab == 1 && r.purchases.isEmpty()) AdminNote("هنوز خریدی نکرده.")
    if (tab == 1 && r.purchases.isNotEmpty()) AppCard(label = "خریدها") {
        r.purchases.forEachIndexed { i, p ->
            Row(Modifier.fillMaxWidth().padding(top = if (i > 0) 10.dp else 0.dp), verticalAlignment = Alignment.Top) {
                Box(Modifier.padding(top = 5.dp).size(10.dp).clip(CircleShape).background(AppGoldInk))
                Column(Modifier.padding(start = 10.dp)) {
                    Text("${PLAN_NAMES[p.product] ?: p.product} · ${if (p.store == "myket") "مایکت" else "کافه‌بازار"}", color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
                    Text("${adminDay(p.at)} تا ${adminDay(p.until)}", color = AppMuted, fontSize = 11.5.sp)
                }
            }
        }
    }
    if (tab == 3 && r.installs.isNotEmpty()) {
        val body: @Composable () -> Unit = {
            r.installs.forEachIndexed { i, ins ->
                if (i > 0) Box(Modifier.fillMaxWidth().padding(vertical = 6.dp).height(1.5.dp).background(AppLineRow))
                Text(ins.model ?: "گوشی", color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    "نسخه ${ins.version ?: "?"} · ${toFa(ins.activeDays)} روزِ فعال · ${adminDay(ins.firstDay)} تا ${adminDay(ins.lastDay)}",
                    color = AppMuted, fontSize = 11.5.sp,
                )
            }
        }
        if (r.installs.size > 1) AdminSection("گوشی‌ها (${toFa(r.installs.size)})", r.installs.mapNotNull { it.model }.joinToString("، ")) { body() }
        else AppCard(label = "گوشی") { body() }
    }
    if (r.topScreens.isNotEmpty()) AppCard(label = "بیشترین صفحه‌ها") {
        val max = r.topScreens.maxOf { it.count }.coerceAtLeast(1)
        TopList(r.topScreens) { AdminBarRow(screenLabel(it.name), it.count.toFloat() / max, adminNum(it.count)) }
    }
}

private val PLAN_NAMES = mapOf("1m" to "یک‌ماهه", "3m" to "سه‌ماهه", "6m" to "شش‌ماهه", "1y" to "یک‌ساله")

private val SEGMENT_LABELS = linkedMapOf(
    "inactive7" to "۷ روز است نیامده‌اند",
    "trial_ending" to "مجانی‌شان ۳ روزِ دیگر تمام می‌شود",
    "expired" to "اشتراکشان تمام شده و تمدید نکرده‌اند",
    "free" to "هیچ‌وقت نخریده‌اند",
    "paid" to "اشتراکِ فعال دارند",
    "inactive30" to "۳۰ روز است نیامده‌اند",
    "new7" to "تازه‌واردهای این هفته",
    "old_version" to "نسخه‌ی قدیمی دارند",
    "cafebazaar" to "کاربرانِ کافه‌بازار",
    "myket" to "کاربرانِ مایکت",
    "all" to "همه‌ی کاربران",
    "one" to "یک نفر (با Uid)",
)

private val BROADCAST_ACTION_LABELS = linkedMapOf(
    null to "بی دکمه",
    "shop" to "برو به فروشگاه",
    "subscription" to "دیدنِ اشتراک‌ها",
    "update" to "آپدیت کن",
)

@Composable
private fun PickChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(AppRadius.button)
    Text(
        label,
        color = if (selected) AppPrimaryInk else AppText,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clip(shape).background(if (selected) AppPrimaryPill else AppSurface)
            .border(1.5.dp, if (selected) AppPrimary else AppLine, shape)
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

/** 📣 پیامِ هدفمند به یک گروه (بخشِ ۸۲) - در «پیام‌های جیبک»ِ همان کاربرها. */
@Composable
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
fun AdminBroadcastScreen(onBack: () -> Unit, vm: AdminProViewModel = hiltViewModel()) {
    var segment by remember { mutableStateOf("inactive7") }
    var count by remember { mutableStateOf<Int?>(null) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val banner = rememberInAppBanner()
    var counts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var days by remember { mutableStateOf(0) }
    var coins by remember { mutableStateOf(0) }
    var action by remember { mutableStateOf<String?>(null) }
    var oneUser by remember { mutableStateOf("") }
    var history by remember { mutableStateOf<List<ir.sadteam.loancalc.data.network.AdminBroadcastHistoryItem>>(emptyList()) }
    var reload by remember { mutableStateOf(0) }
    LaunchedEffect(reload) { counts = vm.repo.adminBroadcastCounts(); history = vm.repo.adminBroadcastHistory() }
    LaunchedEffect(segment, oneUser) {
        count = null
        count = if (segment == "one") { if (oneUser.isBlank()) 0 else vm.repo.adminBroadcast(segment, "", "", dryRun = true, user = oneUser.trim())?.count }
        else vm.repo.adminBroadcast(segment, "", "", dryRun = true)?.count
    }
    Box(Modifier.fillMaxSize()) {
        AdminPage("پیامِ گروهی", "فقط به یک گروهِ خاص", onBack) {
            AdminGroupLabel("به چه کسانی")
            SEGMENT_LABELS.entries.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { (key, label) -> SegmentCard(label, key == segment, if (key == segment) count else counts[key], Modifier.weight(1f)) { segment = key } }
                }
            }
            if (segment == "one") {
                OutlinedTextField(value = oneUser, onValueChange = { oneUser = it.take(20) }, label = { Text("شماره‌ی کاربری (Uid)") }, singleLine = true, colors = adminFieldColors(), shape = AdminFieldShape, modifier = Modifier.fillMaxWidth())
                if (oneUser.isNotBlank() && count == 0) Text("این شماره پیدا نشد", color = ir.sadteam.loancalc.ui.theme.AppDangerInk, fontSize = 12.sp)
            }
            AdminGroupLabel("پیام")
            OutlinedTextField(value = title, onValueChange = { title = it.take(80) }, label = { Text("عنوان") }, singleLine = true, colors = adminFieldColors(), shape = AdminFieldShape, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = body, onValueChange = { body = it.take(600) }, label = { Text("متنِ پیام") }, minLines = 3, colors = adminFieldColors(), shape = AdminFieldShape, modifier = Modifier.fillMaxWidth())
            AdminGroupLabel("هدیه همراهِ پیام (اختیاری)")
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 7, 30).forEach { d -> PickChip(if (d == 0) "بی اشتراک" else "${toFa(d)} روز اشتراک", days == d) { days = d } }
            }
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 100, 500, 1000).forEach { c -> PickChip(if (c == 0) "بی سکه" else "${toFa(c)} سکه", coins == c) { coins = c } }
            }
            AdminGroupLabel("دکمه‌ی داخلِ پیام")
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BROADCAST_ACTION_LABELS.forEach { (k, l) -> PickChip(l, action == k) { action = k } }
            }
            if (title.isNotBlank() || body.isNotBlank()) {
                AdminGroupLabel("همین‌طور در «پیام‌های جیبک» می‌نشیند")
                AppCard(borderColor = if (days > 0 || coins > 0) ir.sadteam.loancalc.ui.theme.AppGoldBorder else null) {
                    Text(title.ifBlank { "عنوان" }, color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    Text(body.ifBlank { "متنِ پیام" }, color = AppMuted, fontSize = 12.5.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 4.dp))
                    BROADCAST_ACTION_LABELS[action]?.takeIf { action != null }?.let {
                        Text(it, color = androidx.compose.ui.graphics.Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 10.dp).clip(RoundedCornerShape(AppRadius.button)).background(AppPrimary).padding(horizontal = 14.dp, vertical = 8.dp))
                    }
                    if (days > 0 || coins > 0) Text(
                        "🎁 " + listOfNotNull(days.takeIf { it > 0 }?.let { "${toFa(it)} روز اشتراک" }, coins.takeIf { it > 0 }?.let { "${toFa(it)} سکه" }).joinToString(" + "),
                        color = ir.sadteam.loancalc.ui.theme.AppGoldInk, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            GradientButton(onClick = { confirm = true }, enabled = title.isNotBlank() && body.isNotBlank() && (count ?: 0) > 0, modifier = Modifier.fillMaxWidth()) {
                Text(count?.let { "بفرست به ${toFa(it)} نفر" } ?: "بفرست")
            }
            if (history.isNotEmpty()) {
                AdminGroupLabel("پیام‌های قبلی · چند نفر دیدند")
                AppCard {
                    history.forEachIndexed { i, h ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(h.title, color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("${SEGMENT_LABELS[h.segment] ?: h.segment} · ${h.sentAt.take(10)}", color = AppMuted, fontSize = 11.sp, maxLines = 1)
                            }
                            val pct = if (h.sent > 0) h.opened * 100 / h.sent else 0
                            Text("${toFa(h.opened)} از ${toFa(h.sent)} · ${toFa(pct)}٪", color = AppPrimaryInk, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                        if (i < history.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(AppLine))
                    }
                }
                AdminNote("«دیدند» فقط از نسخه‌ی ۷۰۶ به بعد شمرده می‌شود.")
            }
        }
        InAppBannerHost(state = banner, modifier = Modifier.align(Alignment.BottomCenter))
    }
    if (confirm) {
        ir.sadteam.loancalc.ui.components.JibakAlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("فرستادن به ${toFa(count ?: 0)} نفر؟", fontWeight = FontWeight.Black) },
            text = { Text("«$title» در «پیام‌های جیبک»ِ همه‌ی این کاربرها می‌نشیند و پس گرفته نمی‌شود." + if (days > 0 || coins > 0) " هدیه هم همین الان به همه داده می‌شود." else "") },
            confirmButton = {
                GradientButton(onClick = {
                    confirm = false
                    scope.launch {
                        val r = vm.repo.adminBroadcast(segment, title.trim(), body.trim(), dryRun = false, days = days, coins = coins, action = action, user = oneUser.trim().takeIf { segment == "one" })
                        val ok = r?.sent == true
                        banner.show(if (ok) "برای ${toFa(r!!.count)} نفر فرستاده شد" else "فرستاده نشد؛ دوباره امتحان کن", isSuccess = ok)
                        if (ok) { title = ""; body = ""; days = 0; coins = 0; action = null; reload++ }
                    }
                }) { Text("بفرست") }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { confirm = false }) { Text("انصراف") } },
        )
    }
}

/** کارتِ گروه؛ تعداد فقط روی گروهِ انتخاب‌شده (شمارش یک درخواستِ آزمایشی است، نه شش تا). */
@Composable
private fun SegmentCard(label: String, selected: Boolean, count: Int?, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(AppRadius.card)
    Column(
        modifier.heightIn(min = 78.dp).clip(shape).background(if (selected) AppPrimaryPill else AppSurface)
            .border(2.dp, if (selected) AppPrimary else AppLine, shape).clickable(onClick = onClick).padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(label, color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 19.sp, modifier = Modifier.weight(1f))
            if (selected) Icon(Icons.Filled.CheckCircle, null, tint = AppPrimaryInk, modifier = Modifier.size(18.dp))
        }
        Text(count?.let { "${adminNum(it)} نفر" } ?: "…", color = if (selected) AppPrimaryInk else AppMuted, fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 4.dp))
    }
}
