package ir.sadteam.loancalc.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.network.AdminStatsResponse
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppText
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ir.sadteam.loancalc.ui.theme.AppDanger

private val STATS_TABS = listOf("خلاصه", "کاربران", "ماندگاری", "زمان", "بخش‌ها", "گوشی‌ها", "سلامت")
private const val PRIVACY_NOTE = "هیچ مبلغ، عنوان، نامِ حساب، شماره‌ی کارت یا موبایل، و هیچ متنِ پیامکی جمع نمی‌شود. " +
    "همه‌ی عددها بی‌نام‌اند و به‌ازای هر نصب شمرده می‌شوند، نه هر آدم. " +
    "گوشیِ بی‌اینترنت آمارش را بعداً می‌فرستد، پس عددهای امروز ممکن است کمی عقب باشند."
/**
 * **گزارشِ برنامه** - فقط برای صاحبِ برنامه (بازطراحیِ بخشِ ۸۲). هفت زبانه‌ی چسبان؛ در هر زبانه
 * اول عدد و نمودار، بعد جزئیات در بخش‌های جمع‌شونده و فهرست‌های کوتاه («بقیه (n)»).
 * همه‌ی عددها بی‌نام‌اند (به‌ازای نصب، نه آدم) - رجوع کن به `data.UsageStats`.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AdminStatsScreen(onBack: () -> Unit, viewModel: AdminStatsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }
    BackHandler(onBack = onBack)
    var tab by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableIntStateOf(0) }
    var privacy by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(AppBg)) {
        LazyColumn(
            contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                AdminHeader("گزارشِ برنامه", "بی‌نام · به‌ازای هر نصب، نه هر آدم", onBack, refreshKey = state) {
                    IconButton(onClick = { privacy = true }) { Icon(Icons.Filled.Info, "چه چیزی جمع می‌شود", tint = AppMuted) }
                    AdminHeaderAction(Icons.Filled.Refresh, "تازه‌سازی") { viewModel.load() }
                }
            }
            when (val s = state) {
                AdminStatsViewModel.State.Loading -> item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppPrimary)
                    }
                }
                AdminStatsViewModel.State.Failed -> item {
                    EmptyState(
                        icon = Icons.Outlined.CloudOff,
                        title = "آمار نرسید",
                        description = "اینترنت را چک کن و دوباره تازه کن. اگر درست نشد، شاید دسترسیِ این حساب برداشته شده.",
                    )
                }
                is AdminStatsViewModel.State.Ready -> {
                    item { RangePill(s.stats) }
                    stickyHeader {
                        Box(Modifier.fillMaxWidth().background(AppBg).padding(vertical = 4.dp)) {
                            StatsIconTabs(tab, { tab = it }, crashDot = s.stats.crashes30 > 0)
                        }
                    }
                    statsContent(s.stats, tab)
                }
            }
        }
    }
    if (privacy) {
        ir.sadteam.loancalc.ui.components.JibakAlertDialog(
            onDismissRequest = { privacy = false },
            title = { Text("چه چیزی جمع نمی‌شود", fontWeight = FontWeight.Black) },
            text = { Text(PRIVACY_NOTE, lineHeight = 22.sp) },
            confirmButton = { androidx.compose.material3.TextButton(onClick = { privacy = false }) { Text("فهمیدم") } },
            dismissButton = {},
        )
    }
}
private fun androidx.compose.foundation.lazy.LazyListScope.statsContent(st: AdminStatsResponse, tab: Int) {
    val actions = st.actions.orEmpty()
    when (tab) {
        0 -> {
            item { LiveCard(st) }
            item { Insights(st) }
            item { SalesCard(st) }
        }
        1 -> item { UsersCard(st) }
        2 -> {
            item { RetentionCard(st) }
            val funnel = st.funnel.orEmpty()
            if (funnel.isNotEmpty()) item {
                AppCard(label = "مسیرِ کاربرِ تازه") { FunnelChart(funnel.map { (FUNNEL_LABELS[it.name] ?: it.name) to it.count }, unit = "نصب") }
            }
            item { OnboardingCard(actions) }
            val cohorts = st.cohorts.orEmpty()
            if (cohorts.any { it.size > 0 }) item { CohortCard(cohorts) }
            item { NotificationCard(actions) }
        }
        3 -> {
            item { TimeCard(st) }
            val flows = st.flows.orEmpty().sortedByDescending { it.total }
            if (flows.isNotEmpty()) item {
                AppCard(label = "مسیرِ حرکت بینِ صفحه‌ها (۳۰ روز)") {
                    val max = flows.first().total.coerceAtLeast(1)
                    TopList(flows.take(25)) { f ->
                        val (from, to) = f.name.split("--", limit = 2).let { it[0] to it.getOrElse(1) { "" } }
                        AdminBarRow("${screenLabel(from)} ← ${screenLabel(to)}", f.total.toFloat() / max, "${adminNum(f.total)} بار · ${adminNum(f.users)} نفر")
                    }
                }
            }
            val screenTime = st.screenTime.orEmpty().sortedByDescending { it.total }
            if (screenTime.isNotEmpty()) item {
                AppCard(label = "بیشترین زمان روی کدام صفحه (۳۰ روز)") {
                    val max = screenTime.first().total.coerceAtLeast(1)
                    TopList(screenTime.take(20)) { f ->
                        AdminBarRow(
                            label = screenLabel(f.name),
                            fraction = f.total.toFloat() / max,
                            trailing = "${formatDuration(f.total)} · ${adminNum(f.users)} نفر",
                            sub = if (f.users > 0) "میانگینِ هر نفر ${formatDuration(f.total / f.users)}" else null,
                        )
                    }
                }
            }
        }
        4 -> {
            item { FeatureCard("صفحه‌ها (۳۰ روز)", st.screens.orEmpty(), st.active30, ::screenLabel, SCREEN_LABELS.keys) }
            item { FeatureCard("کارها (۳۰ روز)", actions, st.active30, ::actionLabel, ACTION_LABELS.keys) }
            val adoption = st.adoption.orEmpty().sortedByDescending { it.installs }
            if (adoption.isNotEmpty()) item {
                AppCard(label = "از هر بخش چند نفر واقعاً استفاده می‌کنند") {
                    val base = st.profiledInstalls.coerceAtLeast(1)
                    TopList(adoption, visible = 10) { a ->
                        AdminBarRow(
                            label = ADOPTION_LABELS[a.key] ?: a.key,
                            fraction = a.installs.toFloat() / base,
                            trailing = "${adminNum(a.installs)} نفر · ${toFa(percent(a.installs, base))}٪",
                            sub = if (a.installs > 0) "میانگینِ هر نفر: ${faDecimal(a.avg)} مورد" else null,
                        )
                    }
                    AdminNote("از روی ${adminNum(st.profiledInstalls)} نصبِ فعالِ ماه - فقط تعداد، نه محتوا.")
                }
            }
        }
        5 -> {
            val splits = st.profileSplits.orEmpty()
            if (splits.isNotEmpty()) {
                val groups = SPLIT_GROUPS.map { (title, keys) -> title to splits.filter { it.key in keys } } +
                    ("تنظیماتِ برنامه" to splits.filter { sp -> SPLIT_GROUPS.none { sp.key in it.second } })
                groups.filter { it.second.isNotEmpty() }.forEach { (title, list) ->
                    item {
                        AdminSection(title, "${toFa(list.size)} مورد · " + list.take(3).joinToString("، ") { PROFILE_LABELS[it.key] ?: it.key }) {
                            list.forEach { sp -> SplitBar(PROFILE_LABELS[sp.key] ?: sp.key, sp.values.orEmpty().map { SplitPart(profileValue(it.name), it.count) }) }
                        }
                    }
                }
            }
            item {
                AppCard(label = "نسخه، استور و اندروید (فعال‌های ماه)") {
                    SplitBar("نسخه", st.versions.orEmpty().map { SplitPart(it.name, it.count) })
                    SplitBar("استور", st.stores.orEmpty().map { SplitPart(STORE_LABELS[it.name] ?: it.name, it.count) })
                    SplitBar("اندروید", st.sdks.orEmpty().map { SplitPart(sdkLabel(it.name), it.count) })
                }
            }
            val installs = st.installsList.orEmpty()
            if (installs.isNotEmpty()) item {
                AppCard(label = "آخرین نصب‌ها (هر ردیف = یک گوشیِ بی‌نام)") { TopList(installs) { InstallRowView(it) } }
            }
        }
        6 -> item { HealthCard(st) }
    }
}
private val SPLIT_GROUPS = listOf(
    "گوشی" to setOf("installer", "device_brand", "device_model", "android", "screen_dp", "lang", "system_dark", "font_scale_sys"),
    "مجوزها" to setOf("perm_notifications", "perm_sms", "perm_calendar", "notif_listener", "battery_unrestricted"),
)
internal val WEEKDAY_SHORT = mapOf(7 to "ش", 1 to "ی", 2 to "د", 3 to "س", 4 to "چ", 5 to "پ", 6 to "ج")
/** بازه‌ی زمانیِ آمار - همیشه ۳۰ روزِ اخیر (سرور همین را می‌دهد). */
@Composable
private fun RangePill(st: AdminStatsResponse) {
    val daily = st.daily.orEmpty()
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(AppSurface)
            .border(1.dp, AppLineRow, RoundedCornerShape(18.dp)).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.CalendarMonth, null, tint = AppMuted, modifier = Modifier.size(24.dp))
        Text("۳۰ روزِ گذشته", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 10.dp).weight(1f))
        if (daily.size >= 2) {
            val days = daily.map { it.day }.sorted()
            Text("${days.first().faDigitsAscii()} - ${days.last().faDigitsAscii()}", color = AppMuted, fontSize = 11.sp)
        }
    }
}
private val STATS_TAB_ICONS = listOf(
    Icons.Filled.Dashboard, Icons.Filled.Groups, Icons.Filled.AccessTime, Icons.Filled.FormatListBulleted,
    Icons.Filled.ViewInAr, Icons.Filled.PhoneAndroid, Icons.Filled.MonitorHeart,
)
/** تب‌های آیکون‌دار در یک قابِ گرد (طرحِ ۱۶ مهر). */
@Composable
private fun StatsIconTabs(selected: Int, onSelect: (Int) -> Unit, crashDot: Boolean) {
    androidx.compose.foundation.lazy.LazyRow(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(AppSurface)
            .border(1.dp, AppLineRow, RoundedCornerShape(22.dp)).padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(STATS_TABS.size) { i ->
            val sel = i == selected
            Column(
                Modifier.width(72.dp).clip(RoundedCornerShape(18.dp)).background(if (sel) AppPrimary else Color.Transparent)
                    .clickable { onSelect(i) }.padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box {
                    Icon(STATS_TAB_ICONS[i], null, tint = if (sel) Color.White else AppMuted, modifier = Modifier.size(24.dp))
                    if (i == 6 && crashDot) Box(Modifier.align(Alignment.TopEnd).size(7.dp).clip(CircleShape).background(AppDanger))
                }
                Text(STATS_TABS[i], color = if (sel) Color.White else AppMuted, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
