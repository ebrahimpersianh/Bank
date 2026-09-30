package ir.sadteam.loancalc.ui.admin

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
    Text("${toFa(d.fromIran)} تا ${toFa(d.toIran)} · مقایسه با دوره‌ی قبل", color = AppMuted, fontSize = 11.5.sp, modifier = Modifier.padding(horizontal = 4.dp))
    AdminAlerts(d.notes.map { digestNoteIcon(it) to digestNoteText(it) })

    val byKey = d.metrics.associateBy { it.key }
    HERO_KEYS.mapNotNull { byKey[it] }.chunked(2).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            row.forEach { m ->
                KpiTile(
                    label = METRIC_LABELS[m.key] ?: m.key,
                    value = adminNum(m.now),
                    delta = adminDelta(m.now, m.prev, m.key in BAD_WHEN_UP),
                    compare = m.now to m.prev,
                    prevText = "قبلی ${adminNum(m.prev)}",
                    standalone = true,
                    modifier = Modifier.weight(1f),
                )
            }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }

    if (d.series.size > 1) {
        AppCard {
            AdminSubTitle("روند · ${toFa(d.series.size)} روزِ اخیر")
            AdminLineChart(values = d.series.map { it.active }, bars = d.series.map { it.installs })
            AdminNote("خط = فعال · ستون = نصبِ تازه · هر روز از ۷ صبح")
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
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                listOf("شاخص" to 1f, "حالا" to 0.45f, "قبلی" to 0.45f, "تغییر" to 0.45f).forEach { (h, w) ->
                    Text(h, color = AppLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(w))
                }
            }
            rest.forEach { m ->
                val dl = adminDelta(m.now, m.prev, m.key in BAD_WHEN_UP)
                Box(Modifier.fillMaxWidth().height(1.5.dp).background(AppLineRow))
                Row(Modifier.fillMaxWidth().heightIn(min = 38.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(METRIC_LABELS[m.key] ?: m.key, color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                    Text(adminNum(m.now), color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(0.45f))
                    Text(adminNum(m.prev), color = AppMuted, fontSize = 12.5.sp, modifier = Modifier.weight(0.45f))
                    Text(dl.text, color = dl.color(), fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, modifier = Modifier.weight(0.45f))
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
