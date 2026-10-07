package ir.sadteam.loancalc.ui.admin

import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MarkChatUnread
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.AuthRepository
import ir.sadteam.loancalc.data.network.AdminDigestResponse
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppCardVariant
import ir.sadteam.loancalc.ui.components.SegmentedToggle
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppGoldInk
import ir.sadteam.loancalc.ui.theme.AppGoldInk2
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AdminDigestViewModel @Inject constructor(private val repo: AuthRepository) : ViewModel() {
    private val _store = MutableStateFlow<String?>(null)
    val store: StateFlow<String?> = _store
    fun setStore(s: String?) { _store.value = s; load() }
    private val _period = MutableStateFlow("day")
    val period: StateFlow<String> = _period
    private val _data = MutableStateFlow<AdminDigestResponse?>(null)
    val data: StateFlow<AdminDigestResponse?> = _data
    private val _failed = MutableStateFlow(false)
    val failed: StateFlow<Boolean> = _failed

    fun load(p: String = _period.value) {
        _period.value = p
        _data.value = null
        _failed.value = false
        viewModelScope.launch {
            val r = repo.adminDigest(p, _store.value)
            _data.value = r
            _failed.value = r == null
        }
    }

    /** هاب همین نمونه را برای «امروز» می‌خواند؛ بعد از برگشت از گزارش دوره و استور به پیش‌فرض برمی‌گردند. */
    fun resetToToday() {
        if (_period.value == "day" && _store.value == null) return
        _store.value = null
        load("day")
    }
}

internal val METRIC_LABELS = linkedMapOf(
    "active" to "کاربرِ فعال",
    "new_installs" to "نصبِ تازه",
    "new_users" to "ثبت‌نامِ تازه",
    "transactions" to "تراکنشِ ثبت‌شده",
    "purchases" to "خریدِ اشتراک",
    "revenue" to "فروشِ ناخالص (تومان)",
    "revenue_net" to "فروشِ خالص تقریبی (تومان)",
    "revenue_cafebazaar" to "فروشِ کافه‌بازار",
    "revenue_myket" to "فروشِ مایکت",
    "support" to "پیامِ پشتیبانی",
    "crashes" to "کرش",
)

/** متریک‌هایی که بالا رفتنشان **بد** است (قرمز). */
private val BAD_WHEN_UP = setOf("crashes", "support")

/** چهار کاشیِ بالا - با دو میله‌ی حالا/قبلی. */
private val HERO_KEYS = listOf("active", "new_installs", "purchases", "new_users")
/** کارتِ طلایی. */
private val MONEY_KEYS = setOf("revenue", "revenue_net", "revenue_cafebazaar", "revenue_myket")

internal fun digestNoteText(n: String): String = when {
    n.startsWith("open_support:") -> "${toFa(n.removePrefix("open_support:"))} پیامِ بی‌جواب منتظرِ توست"
    n == "crashes_up" -> "کرش‌ها نسبت به دوره‌ی قبل بیشتر شده"
    n == "active_down" -> "کاربرِ فعال بیش از ۲۰٪ کم شده"
    else -> n
}

internal fun digestNoteIcon(n: String): ImageVector = when {
    n.startsWith("open_support:") -> Icons.Filled.MarkChatUnread
    n == "crashes_up" -> Icons.Filled.Error
    n == "active_down" -> Icons.Filled.TrendingDown
    else -> Icons.Filled.Warning
}

/**
 * 📰 **گزارشِ روز/هفته/ماه** (بازطراحیِ بخشِ ۸۲). روز از ۷ صبح تا ۷ صبحِ فردا.
 * ترتیب: هشدار → چهار عددِ اصلی → پول (طلایی) → بقیه‌ی شاخص‌ها (جمع‌شونده) → بیشترین کارها/صفحه‌ها.
 * سرور برای این صفحه فقط «حالا/قبلی» می‌دهد، پس نمودارِ زمانی ندارد؛ مقایسه با دو میله است.
 */
@Composable
fun AdminDigestScreen(onBack: () -> Unit, vm: AdminDigestViewModel = hiltViewModel()) {
    val period by vm.period.collectAsState()
    val store by vm.store.collectAsState()
    val data by vm.data.collectAsState()
    val failed by vm.failed.collectAsState()
    LaunchedEffect(Unit) { if (vm.data.value == null) vm.load() }
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val d = data

    AdminPage(
        "گزارشِ روز", "هر روز از ۷ صبح تا ۷ صبحِ فردا", onBack,
        actions = { if (d != null) AdminHeaderAction(Icons.Filled.Download, "خروجیِ اکسل (CSV)") { shareCsv(ctx, d) } },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val periods = listOf("day", "week", "month")
            Box(Modifier.weight(1f)) {
                SegmentedToggle(
                    options = listOf("امروز", "۷ روز", "۳۰ روز"),
                    selectedIndex = periods.indexOf(period).coerceAtLeast(0),
                    onSelect = { vm.load(periods[it]) },
                )
            }
            Spacer(Modifier.width(8.dp))
            StorePicker(store) { vm.setStore(it) }
        }
        when {
            failed -> Text("گزارش نرسید؛ اینترنت را چک کن.", color = AppDangerInk, fontSize = 13.sp)
            d == null -> Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AppPrimary) }
            else -> DigestBody(d)
        }
    }
}

@Composable
private fun DigestBody(d: AdminDigestResponse) {
    // ۱۶ مهر: بازطراحی طبقِ طرح (کاشی‌های آیکون‌دار، نمودار با محور، ردیف‌های رنگی).
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
        Icon(Icons.Filled.CalendarMonth, null, tint = AppMuted, modifier = Modifier.size(18.dp))
        Text("${toFa(d.fromIran)} تا ${toFa(d.toIran)} · مقایسه با دوره‌ی قبل", color = AppMuted, fontSize = 11.5.sp, modifier = Modifier.padding(start = 6.dp))
    }
    d.notes.forEach { n ->
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(AppGoldInk.copy(alpha = 0.14f))
                .border(1.5.dp, AppGoldInk.copy(alpha = 0.55f), RoundedCornerShape(20.dp)).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(AppGoldInk.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(digestNoteIcon(n), null, tint = AppGoldInk, modifier = Modifier.size(24.dp))
            }
            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                Text(digestNoteText(n), color = AppGoldInk, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Text("در مقایسه با دوره‌ی قبل", color = AppGoldInk2, fontSize = 11.sp)
            }
        }
    }

    val byKey = d.metrics.associateBy { it.key }
    HERO_KEYS.mapNotNull { byKey[it] }.chunked(2).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            row.forEach { m ->
                DigestTile(
                    label = METRIC_LABELS[m.key] ?: m.key,
                    now = m.now,
                    prev = m.prev,
                    delta = adminDelta(m.now, m.prev, m.key in BAD_WHEN_UP),
                    icon = when (m.key) {
                        "active" -> Icons.Filled.Person
                        "new_installs" -> Icons.Filled.InstallMobile
                        "purchases" -> Icons.Filled.ShoppingCart
                        else -> Icons.Filled.PersonAdd
                    },
                    accent = when (m.key) {
                        "active" -> androidx.compose.ui.graphics.Color(0xFF00E89A)
                        "new_installs" -> androidx.compose.ui.graphics.Color(0xFF1478FF)
                        "purchases" -> androidx.compose.ui.graphics.Color(0xFFFF5E6C)
                        else -> androidx.compose.ui.graphics.Color(0xFFA855F7)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }

    // روزهای خالیِ قبل از شروعِ آمار نشان داده نمی‌شوند - وگرنه نمودار تا ته صاف روی صفر می‌خوابید
    // و گمراه‌کننده بود (کاربر ۱۰ مهر).
    val trend = d.series.dropWhile { it.active == 0 && it.installs == 0 }
    if (trend.size > 1) {
        AppCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.BarChart, null, tint = androidx.compose.ui.graphics.Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                Text("روندِ ${toFa(trend.size)} روزِ اخیر", color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 8.dp))
            }
            val maxV = (trend.map { it.active } + trend.map { it.installs }).maxOrNull()?.coerceAtLeast(3) ?: 3
            val top = ((maxV + 2) / 3) * 3
            Row(Modifier.padding(top = 8.dp)) {
                AdminLineChart(values = trend.map { it.active }, bars = trend.map { it.installs }, height = 130.dp, maxValue = top, modifier = Modifier.weight(1f))
                Column(Modifier.height(130.dp).padding(start = 6.dp, top = 6.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    listOf(top, top * 2 / 3, top / 3, 0).forEach { Text(toFa(it), color = AppLabel, fontSize = 9.5.sp) }
                }
            }
            // ⚠️ AdminLineChart اولین مقدار را سمتِ راست می‌کشد؛ سری از قدیم به جدید است.
            val last = trend.size - 1
            Row(Modifier.fillMaxWidth().padding(top = 4.dp, end = 18.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf(last, last * 3 / 4, last / 2, last / 4, 0).distinct().forEach { back ->
                    Text(if (back == 0) "امروز" else "${toFa(back)} روز پیش", color = AppLabel, fontSize = 9.5.sp)
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(AppPrimary))
                Text("چند نفر آن روز برنامه را باز کردند", color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(start = 5.dp, end = 14.dp))
                Box(Modifier.size(10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(ir.sadteam.loancalc.ui.theme.AppInfo))
                Text("نصبِ تازه‌ی روز", color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(start = 5.dp))
            }
        }
    }

    val net = byKey["revenue_net"]
    val gross = byKey["revenue"]
    if (net != null || gross != null) {
        AppCard(variant = AppCardVariant.GOLD) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text("فروشِ خالص · سهمِ تو", color = AppGoldInk2, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(adminNum(net?.now ?: 0L), color = AppGoldInk, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text("تومان", color = AppGoldInk2, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
                    }
                }
                net?.let { val dl = adminDelta(it.now, it.prev); Text(dl.text, color = dl.color(), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 4.dp)) }
            }
            Text(
                listOfNotNull(gross?.let { "ناخالص ${adminNum(it.now)} تومان" }, net?.let { "قبلی ${adminNum(it.prev)}" }).joinToString(" · "),
                color = AppGoldInk2, fontSize = 12.sp,
            )
            SplitBar(
                title = null,
                parts = listOfNotNull(
                    byKey["revenue_cafebazaar"]?.let { SplitPart("کافه‌بازار ${adminNum(it.now)}", it.now.toInt()) },
                    byKey["revenue_myket"]?.let { SplitPart("مایکت ${adminNum(it.now)}", it.now.toInt()) },
                ),
                gold = true,
            )
            AdminNote("خالص = سهمِ تو بعد از کارمزدِ استور (کافه‌بازار و مایکت هر دو ۷۶٫۹٪).", gold = true)
        }
    }

    val rest = METRIC_LABELS.keys.filter { it !in HERO_KEYS && it !in MONEY_KEYS }.mapNotNull { byKey[it] }
    if (rest.isNotEmpty()) {
        AdminSection(
            title = "بقیه‌ی شاخص‌ها (${toFa(rest.size)})",
            summary = rest.joinToString(" · ") { "${(METRIC_LABELS[it.key] ?: it.key).substringBefore(' ')} ${adminNum(it.now)}" },
        ) {
            val maxRest = rest.maxOf { maxOf(it.now, it.prev) }.coerceAtLeast(1L)
            rest.forEach { m ->
                val dl = adminDelta(m.now, m.prev, m.key in BAD_WHEN_UP)
                val (ic, col) = restIcon(m.key)
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(16.dp)).background(AppSurface)
                        .border(1.dp, AppLineRow, RoundedCornerShape(16.dp)).padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(col.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
                        Icon(ic, null, tint = col, modifier = Modifier.size(20.dp))
                    }
                    Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                        Text(METRIC_LABELS[m.key] ?: m.key, color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                        Box(Modifier.padding(top = 5.dp).fillMaxWidth().height(7.dp).clip(RoundedCornerShape(99.dp)).background(AppLineRow)) {
                            Box(Modifier.fillMaxWidth((m.now.toFloat() / maxRest).coerceIn(0.03f, 1f)).height(7.dp).clip(RoundedCornerShape(99.dp)).background(col))
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(adminNum(m.now), color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Text("قبل: ${adminNum(m.prev)} · ${dl.text}", color = dl.color(), fontSize = 10.sp, maxLines = 1)
                    }
                }
            }
        }
    }

    if (d.topActions.isNotEmpty()) AppCard(label = "بیشترین کارها") {
        val max = d.topActions.maxOf { it.count }.coerceAtLeast(1)
        TopList(d.topActions) { AdminBarRow(actionLabel(it.name), it.count.toFloat() / max, adminNum(it.count)) }
    }
    if (d.topScreens.isNotEmpty()) {
        AdminSection("بیشترین صفحه‌ها", "${toFa(d.topScreens.size)} صفحه · بیشترین: ${screenLabel(d.topScreens.first().name)}") {
            val max = d.topScreens.maxOf { it.count }.coerceAtLeast(1)
            TopList(d.topScreens, visible = 8) { AdminBarRow(screenLabel(it.name), it.count.toFloat() / max, adminNum(it.count)) }
        }
    }
}

/** استور در یک قرصِ کوچک با منو - تا دوره و استور در یک ردیف بنشینند. */
@Composable
private fun StorePicker(store: String?, onPick: (String?) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val options = listOf(null to "همه", "cafebazaar" to "کافه‌بازار", "myket" to "مایکت")
    val shape = RoundedCornerShape(999.dp)
    Box {
        Row(
            Modifier.heightIn(min = 44.dp).clip(shape).background(AppSurface).border(2.dp, AppLine, shape).clickable { open = true }.padding(start = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(options.first { it.first == store }.second, color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
            Icon(Icons.Filled.ExpandMore, "انتخابِ استور", tint = AppMuted)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { (key, label) -> DropdownMenuItem(text = { Text(label) }, onClick = { open = false; onPick(key) }) }
        }
    }
}

/** خروجیِ ساده‌ی CSV (با BOM تا اکسل فارسی را درست نشان دهد). عددها لاتین - ماشین می‌خواندش. */
private fun shareCsv(ctx: android.content.Context, d: AdminDigestResponse) {
    val csv = buildString {
        append('\uFEFF')
        append("شاخص,این دوره,دوره‌ی قبل\n")
        d.metrics.forEach { append("${METRIC_LABELS[it.key] ?: it.key},${it.now},${it.prev}\n") }
        append("\nکار,تعداد\n")
        d.topActions.forEach { append("${actionLabel(it.name)},${it.count}\n") }
        append("\nصفحه,تعداد\n")
        d.topScreens.forEach { append("${screenLabel(it.name)},${it.count}\n") }
    }
    val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(android.content.Intent.EXTRA_SUBJECT, "گزارشِ جیبک ${d.fromIran}")
        putExtra(android.content.Intent.EXTRA_TEXT, csv)
    }
    runCatching { ctx.startActivity(android.content.Intent.createChooser(send, "خروجیِ گزارش")) }
}


/** کاشیِ اصلیِ گزارشِ روز (طرحِ ۱۶ مهر): آیکونِ رنگی، عدد، تغییر، یک نوار و «قبل». */
@Composable
private fun DigestTile(label: String, now: Long, prev: Long, delta: AdminDelta, icon: ImageVector, accent: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(20.dp)).background(AppSurface)
            .border(1.5.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(20.dp)).padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(accent.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(26.dp))
            }
            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                Text(label, color = AppMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(adminNum(now), color = AppText, fontSize = 24.sp, fontWeight = FontWeight.Black)
            }
        }
        Text(delta.text, color = delta.color(), fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 2.dp))
        Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            val frac = if (maxOf(now, prev) <= 0L) 0f else now.toFloat() / maxOf(now, prev)
            Box(Modifier.weight(1f).height(7.dp).clip(RoundedCornerShape(99.dp)).background(AppLineRow)) {
                if (frac > 0f) Box(Modifier.fillMaxWidth(frac.coerceAtLeast(0.04f)).height(7.dp).clip(RoundedCornerShape(99.dp)).background(accent))
            }
            Text("قبل: ${adminNum(prev)}", color = AppLabel, fontSize = 10.5.sp, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

private fun restIcon(key: String): Pair<ImageVector, androidx.compose.ui.graphics.Color> = when (key) {
    "transactions" -> Icons.Filled.TouchApp to androidx.compose.ui.graphics.Color(0xFFF59E0B)
    "support" -> Icons.Filled.Chat to androidx.compose.ui.graphics.Color(0xFF3B82F6)
    "crashes" -> Icons.Filled.Error to androidx.compose.ui.graphics.Color(0xFFEF4444)
    "paywall_view" -> Icons.Filled.Visibility to androidx.compose.ui.graphics.Color(0xFFA855F7)
    else -> Icons.Filled.Insights to androidx.compose.ui.graphics.Color(0xFF14B8A6)
}
