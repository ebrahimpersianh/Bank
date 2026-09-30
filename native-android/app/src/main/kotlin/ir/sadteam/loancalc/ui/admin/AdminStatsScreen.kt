package ir.sadteam.loancalc.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.network.AdminFeatureUsage
import ir.sadteam.loancalc.data.network.AdminNamedCount
import ir.sadteam.loancalc.data.network.AdminStatsResponse
import ir.sadteam.loancalc.data.network.AdminInstallRow
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ir.sadteam.loancalc.ui.components.AppCardVariant
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppGoldInk
import ir.sadteam.loancalc.ui.theme.AppGoldInk2
import ir.sadteam.loancalc.ui.theme.AppMarkOff
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import ir.sadteam.loancalc.ui.theme.AppWarningPill

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
                AdminHeader("گزارشِ برنامه", "بی‌نام · به‌ازای هر نصب، نه هر آدم", onBack) {
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
                    stickyHeader {
                        Box(Modifier.fillMaxWidth().background(AppBg).padding(vertical = 4.dp)) {
                            AdminTabs(STATS_TABS, tab, { tab = it }, dots = if (s.stats.crashes30 > 0) setOf(6) else emptySet())
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

@Composable
private fun UsersCard(st: AdminStatsResponse) {
    val daily = st.daily.orEmpty()
    AppCard(label = "کاربرها · ۳۰ روزِ اخیر") {
        if (daily.isNotEmpty()) {
            AdminLineChart(daily.map { it.active }, daily.map { it.new })
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(top = 8.dp)) {
                AdminLegend(AppPrimary, "کاربرِ فعال")
                AdminLegend(AppInfo, "نصبِ تازه")
            }
            daily.maxByOrNull { it.active }?.takeIf { it.active > 0 }?.let {
                AdminNote("شلوغ‌ترین روز: ${it.day.faDigitsAscii()} با ${adminNum(it.active)} کاربر")
            }
        }
        AdminSubTitle("کاربرِ فعال")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
            KpiTile("امروز", adminNum(st.activeToday), Modifier.weight(1f))
            KpiTile("۷ روز", adminNum(st.active7), Modifier.weight(1f))
            KpiTile("۳۰ روز", adminNum(st.active30), Modifier.weight(1f))
        }
        AdminSubTitle("نصب")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
            KpiTile("تازه · ۷ روز", adminNum(st.new7), Modifier.weight(1f))
            KpiTile("تازه · ۳۰ روز", adminNum(st.new30), Modifier.weight(1f))
            KpiTile("کلِ نصب‌ها", adminNum(st.totalInstalls), Modifier.weight(1f))
        }
        AdminNote(
            "«فعال» یعنی دست‌کم یک بار برنامه را باز کرده. " +
                "${toFa(percent(st.loggedInActive30, st.active30))}٪ از فعال‌های ماه وارد حساب شده‌اند.",
        )
    }
}

@Composable
private fun RetentionCard(st: AdminStatsResponse) {
    val ret = st.retention.orEmpty()
    fun label(d: Int) = when (d) { 1 -> "بعد از یک روز"; 7 -> "بعد از یک هفته"; 30 -> "بعد از یک ماه"; else -> "بعد از ${toFa(d)} روز" }
    fun short(d: Int) = when (d) { 1 -> "روزِ ۱"; 7 -> "هفته‌ی ۱"; 30 -> "ماهِ ۱"; else -> "روزِ ${toFa(d)}" }
    AppCard(label = "ماندگاری · برمی‌گردند؟") {
        if (ret.isNotEmpty()) {
            AdminColumnChart(ret.map { if (it.base == 0) 0 else percent(it.returned, it.base) }, ret.map { short(it.afterDays) }, height = 90.dp, maxValue = 100)
            ret.forEach { r ->
                Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    Text(label(r.afterDays), color = AppText, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                    Text(
                        if (r.base == 0) "هنوز زود است" else "${toFa(percent(r.returned, r.base))}٪ از ${adminNum(r.base)}",
                        color = AppMuted, fontSize = 11.5.sp, fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        AdminNote(
            "هر نفر در ماه به‌طورِ میانگین ${faDecimal(st.avgActiveDays30)} روز برنامه را باز کرده؛ " +
                "در ۷ روزِ اخیر ${adminNum(st.sessions7)} بار استفاده (هر بار = باز کردن بعد از دست‌کم نیم ساعت).",
        )
    }
}

@Composable
private fun TimeCard(st: AdminStatsResponse) {
    AppCard(label = "زمانِ استفاده (۳۰ روز)") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiTile("هر بار استفاده", faDecimal(st.avgSessionMinutes), Modifier.weight(1f), unit = "دقیقه")
            KpiTile("صفحه در هر بار", faDecimal(st.avgScreensPerSession), Modifier.weight(1f), unit = "صفحه")
            KpiTile("جمعِ زمان", adminNum(st.totalMinutes30), Modifier.weight(1f), unit = "دقیقه")
        }
        val hours = st.hours.orEmpty()
        if (hours.isNotEmpty()) {
            val counts = (0..23).map { h -> hours.firstOrNull { it.name.toIntOrNull() == h }?.count ?: 0 }
            val peak = counts.indices.maxByOrNull { counts[it] }
            AdminSubTitle("ساعتِ باز کردنِ برنامه (به وقتِ ایران)")
            AdminColumnChart(counts, height = 70.dp, highlight = peak)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 3.dp)) {
                listOf("۰", "۶", "۱۲", "۱۸", "۲۳").forEach { Text(it, color = AppLabel, fontSize = 11.sp, modifier = Modifier.weight(1f)) }
            }
            if (peak != null) AdminNote("پرکارترین ساعت: ${toFa(peak)}")
        }
        val days = st.weekdays.orEmpty()
        if (days.isNotEmpty()) {
            // Calendar: ۱=یکشنبه … ۷=شنبه؛ نمایش از شنبه (راست).
            val order = listOf(7, 1, 2, 3, 4, 5, 6)
            val counts = order.map { d -> days.firstOrNull { it.name == d.toString() }?.count ?: 0 }
            AdminSubTitle("روزِ هفته")
            AdminColumnChart(counts, order.map { WEEKDAY_SHORT[it] ?: "" }, height = 70.dp, highlight = counts.indices.maxByOrNull { counts[it] })
        }
    }
}

private val WEEKDAY_SHORT = mapOf(7 to "ش", 1 to "ی", 2 to "د", 3 to "س", 4 to "چ", 5 to "پ", 6 to "ج")

/** جمع‌بندیِ خودکار - «از این عددها چه بفهمم». فقط توصیف است، نه حکم. سه خطِ اول، بقیه پشتِ «بقیه». */
@Composable
private fun Insights(st: AdminStatsResponse) {
    val lines = buildList {
        if (st.totalInstalls == 0) {
            add("هنوز داده‌ای نرسیده. آمار از وقتی پر می‌شود که نسخه‌ی تازه دستِ کاربرها برسد.")
            return@buildList
        }
        val funnel = st.funnel.orEmpty()
        funnel.zipWithNext().filter { (a, _) -> a.count > 0 }
            .maxByOrNull { (a, b) -> (a.count - b.count).toFloat() / a.count }
            ?.let { (a, b) ->
                val lost = percent(a.count - b.count, a.count)
                if (lost > 0) add("بیشترین ریزش: از «${FUNNEL_LABELS[a.name] ?: a.name}» تا «${FUNNEL_LABELS[b.name] ?: b.name}» - ${toFa(lost)}٪ ادامه نداده‌اند.")
            }
        st.retention.orEmpty().firstOrNull { it.afterDays == 7 && it.base >= 5 }?.let {
            add("${toFa(percent(it.returned, it.base))}٪ از نصب‌ها بعد از یک هفته هنوز برنامه را باز می‌کنند.")
        }
        val screens = st.screens.orEmpty()
        screens.firstOrNull()?.let { add("پربازدیدترین صفحه: ${screenLabel(it.name)} (${toFa(percent(it.users, st.active30))}٪ کاربرها).") }
        if (st.active30 >= 10) {
            val seen = screens.map { it.name }.toSet()
            val unseen = SCREEN_LABELS.keys.filter { it !in seen && !it.startsWith("settings_") }
            if (unseen.isNotEmpty()) add("این صفحه‌ها در ۳۰ روز هیچ بازدیدی نداشتند: " + unseen.joinToString("، ") { screenLabel(it) } + ".")
            val rare = screens.filter { percent(it.users, st.active30) < 5 && !it.name.startsWith("settings_") }
            if (rare.isNotEmpty()) add("کمتر از ۵٪ کاربرها سراغِ این‌ها رفته‌اند: " + rare.take(6).joinToString("، ") { screenLabel(it.name) } + ".")
        }
        st.actions.orEmpty().firstOrNull()?.let { add("پرتکرارترین کار: ${actionLabel(it.name)} (${adminNum(it.total)} بار).") }
    }
    AppCard(label = "بینش‌ها") {
        TopList(lines, visible = 3) { line ->
            Row(modifier = Modifier.padding(top = 6.dp), verticalAlignment = Alignment.Top) {
                Box(Modifier.padding(top = 8.dp).size(6.dp).clip(CircleShape).background(AppPrimary))
                Text(line, color = AppText, fontSize = 12.5.sp, lineHeight = 21.sp, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    items: List<AdminFeatureUsage>,
    active30: Int,
    label: (String) -> String,
    known: Set<String>,
) {
    AppCard(label = title) {
        if (items.isEmpty()) {
            Text("هنوز چیزی ثبت نشده.", color = AppMuted, fontSize = 12.sp)
        } else {
            val maxUsers = items.maxOf { it.users }.coerceAtLeast(1)
            TopList(items, visible = 8) { f ->
                AdminBarRow(
                    label = label(f.name),
                    fraction = f.users.toFloat() / maxUsers,
                    trailing = "${adminNum(f.users)} نفر · ${adminNum(f.total)} بار",
                    sub = if (active30 > 0) "${toFa(percent(f.users, active30))}٪ کاربرها" else null,
                )
            }
        }
        val missing = known.filter { k -> items.none { it.name == k } }
        if (missing.isNotEmpty() && items.isNotEmpty()) {
            AdminSubSection("بدونِ استفاده (${toFa(missing.size)})", missing.take(3).joinToString("، ") { label(it) }) {
                Text(missing.joinToString("، ") { label(it) }, color = AppDangerInk, fontSize = 12.sp, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AppSurface2)
            .padding(horizontal = 9.dp, vertical = 8.dp),
    ) {
        Text(label, color = AppMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 3.dp)) {
            Text(value, color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text(unit, color = AppLabel, fontSize = 11.sp, modifier = Modifier.padding(start = 3.dp, bottom = 3.dp))
        }
    }
}

@Composable
private fun BarRow(label: String, fraction: Float, trailing: String, sub: String? = null) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 9.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                color = AppText,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(trailing, color = AppMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .height(7.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(AppSurface2),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(7.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(AppPrimary),
            )
        }
        if (sub != null) Text(sub, color = AppLabel, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun DailyChart(active: List<Int>, fresh: List<Int>) {
    val primary = AppPrimary
    val accent = AppInfo
    val track = AppSurface2
    val max = (active + fresh).maxOrNull()?.coerceAtLeast(1) ?: 1
    Canvas(modifier = Modifier.fillMaxWidth().height(120.dp).padding(top = 8.dp)) {
        val n = active.size.coerceAtLeast(1)
        val slot = size.width / n
        val barW = slot * 0.62f
        // RTL: زمان از راست به چپ جلو می‌رود - قدیمی‌ترین روز راست، امروز چپ‌ترین میله.
        active.forEachIndexed { i, a ->
            val x = size.width - (i + 1) * slot + (slot - barW) / 2
            drawRoundRect(track, Offset(x, 0f), Size(barW, size.height), CornerRadius(4f, 4f))
            val h = size.height * a / max
            drawRoundRect(primary, Offset(x, size.height - h), Size(barW, h), CornerRadius(4f, 4f))
            val nh = size.height * (fresh.getOrElse(i) { 0 }) / max
            if (nh > 0f) drawRoundRect(accent, Offset(x, size.height - nh), Size(barW, nh), CornerRadius(4f, 4f))
        }
    }
}

@Composable
private fun Legend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, color = AppMuted, fontSize = 11.sp)
    }
}

@Composable
private fun SplitRow(title: String, items: List<AdminNamedCount>, label: (String) -> String) {
    if (items.isEmpty()) return
    val total = items.sumOf { it.count }.coerceAtLeast(1)
    Text(title, color = AppPrimaryInk, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 8.dp))
    items.forEach { BarRow(label(it.name), it.count.toFloat() / total, "${toFa(it.count)} · ${toFa(percent(it.count, total))}٪") }
}

private fun percent(part: Int, whole: Int): Int = if (whole <= 0) 0 else Math.round(part * 100f / whole)

private fun faDecimal(v: Double): String = toFa(v.toString().removeSuffix(".0")).replace('.', '٫')

/** `2026-09-29` → «۷ مهر». */
private fun String.faDigitsAscii(): String = runCatching {
    val (y, m, d) = split('-').map { it.toInt() }
    val p = ir.sadteam.loancalc.core.JalaliCalendar.fromGregorian(y, m, d)
    "${toFa(p.d)} ${ir.sadteam.loancalc.ui.components.persianMonthName(p.m)}"
}.getOrDefault(toFa(this))

internal fun screenLabel(key: String): String = SCREEN_LABELS[key] ?: key

internal fun actionLabel(key: String): String = ACTION_LABELS[key]
    ?: when {
        key.startsWith("purchase_start_") -> "شروعِ خریدِ اشتراکِ ${key.removePrefix("purchase_start_")}"
        key.startsWith("purchase_done_") -> "خریدِ موفقِ اشتراکِ ${key.removePrefix("purchase_done_")}"
        key.startsWith("purchase_failed_") -> "خریدِ ناموفقِ اشتراکِ ${key.removePrefix("purchase_failed_")}"
        key.startsWith("purchase_cancel_") -> "انصراف از خریدِ اشتراکِ ${key.removePrefix("purchase_cancel_")}"
        key == "paywall_view" -> "دیدنِ صفحه‌ی اشتراک"
        key.startsWith("notif_shown_") -> "اعلانِ فرستاده‌شده: ${key.removePrefix("notif_shown_")}"
        key.startsWith("notif_open_") -> "باز کردنِ اعلان: ${key.removePrefix("notif_open_")}"
        key.startsWith("notif_button_") -> "دکمه‌ی اعلان: ${NOTIF_BUTTON_LABELS[key.removePrefix("notif_button_")] ?: key}"
        key.startsWith("onboarding_step_") -> "معرفی: مرحله‌ی ${toFa((key.removePrefix("onboarding_step_").toIntOrNull() ?: 0) + 1)}"
        key.startsWith("shortcut_") -> "میان‌برِ آیکون: ${key.removePrefix("shortcut_")}"
        key == "widget_open" -> "باز کردن از ویجت"
        key == "purchase_verify_failed" -> "پول رفت ولی تأیید نشد (پیگیری کن!)"
        else -> key
    }

private fun sdkLabel(sdk: String): String = when (sdk.toIntOrNull()) {
    null -> sdk
    in 35..99 -> "۱۵+"
    34 -> "۱۴"
    33 -> "۱۳"
    32, 31 -> "۱۲"
    30 -> "۱۱"
    29 -> "۱۰"
    28 -> "۹"
    27, 26 -> "۸"
    else -> "۷ و قدیمی‌تر"
}

private val STORE_LABELS = mapOf("cafebazaar" to "کافه‌بازار", "myket" to "مایکت")

private val FUNNEL_LABELS = mapOf(
    "installed" to "نصب و باز کرد",
    "onboarding_completed" to "مراحلِ شروع را تمام کرد",
    "account_created" to "حساب ساخت",
    "transaction_created" to "تراکنش ثبت کرد",
    "report_viewed" to "گزارش را دید",
)

/** کلیدها همان مسیرهای ناوبری‌اند، با «-» به «_» (رجوع کن به `UsageStats.clean`). */
private val SCREEN_LABELS = linkedMapOf(
    "home" to "خانه",
    "assets" to "دارایی",
    "report" to "گزارش",
    "budget" to "بودجه",
    "due" to "سررسید",
    "loan" to "وام",
    "cheque" to "چک",
    "loan_stats" to "آمارِ وام‌ها",
    "cheque_report" to "گزارشِ چک",
    "debt" to "طلب و بدهی",
    "tools" to "ابزارها",
    "notes" to "یادداشت‌ها",
    "bug_report" to "گزارشِ مشکل",
    "savings_goal" to "هدفِ پس‌انداز",
    "categories" to "دسته‌بندی‌ها",
    "accounts" to "حساب‌ها",
    "shop" to "فروشگاه",
    "inbox" to "پیام‌ها",
    "calc_history" to "تاریخچه‌ی محاسبه",
    "sayad_inquiry" to "استعلامِ صیادی",
    "annual_archive" to "بایگانیِ سالانه",
    "financial_calendar" to "تقویمِ مالی",
    "settings_account" to "تنظیمات · حسابِ کاربری",
    "settings_appearance" to "تنظیمات · ظاهر",
    "settings_reminders" to "تنظیمات · یادآورها",
    "settings_data" to "تنظیمات · داده و پشتیبان",
    "settings_sms" to "تنظیمات · پیامکِ بانکی",
    "settings_background" to "تنظیمات · پس‌زمینه",
    "settings_tools" to "تنظیمات · ابزارها",
    "settings_security" to "تنظیمات · امنیت",
    "settings_color_theme" to "تنظیمات · تمِ رنگی",
    "settings_badges" to "تنظیمات · نشان‌ها",
    "settings_parsing_rules" to "تنظیمات · قاعده‌های پیامک",
    "settings_about" to "تنظیمات · درباره",
)

private val ACTION_LABELS = linkedMapOf(
    "transaction_added" to "ثبتِ تراکنش",
    "anr" to "هنگ‌کردنِ برنامه (ANR)",
    "ab_paywall_a" to "آزمایشِ اشتراک - گروهِ A",
    "ab_paywall_b" to "آزمایشِ اشتراک - گروهِ B",
    "login" to "ورود به حساب",
    "onboarding_first_account" to "اولین حساب در شروع",
    "loan_added_manual" to "ثبتِ وامِ دستی",
    "loan_saved_from_calc" to "ذخیره‌ی وام از محاسبه‌گر",
    "installment_paid" to "پرداختِ قسط",
    "installment_paid_late" to "پرداختِ قسط با تأخیر",
    "installments_paid_bulk" to "پرداختِ گروهیِ قسط",
    "installments_paid_bulk_late" to "پرداختِ گروهیِ قسط با تأخیر",
    "installment_receipt" to "عکسِ رسیدِ قسط",
    "installment_note" to "یادداشتِ قسط",
    "installment_amount_edited" to "ویرایشِ مبلغِ قسط",
    "home_mark_paid" to "پرداخت از کارتِ خانه",
    "loan_photo" to "عکسِ وام",
    "calendar_export" to "افزودن به تقویمِ گوشی",
    "income_added" to "ثبتِ درآمد (توانِ بازپرداخت)",
    "cheque_added" to "ثبتِ چک",
    "cheque_status_changed" to "تغییرِ وضعیتِ چک",
    "cheque_photo" to "عکسِ چک",
    "transfer_added" to "انتقال بینِ حساب‌ها",
    "budget_set" to "تعیینِ بودجه",
    "recurring_added" to "پرداختِ تکراری",
    "template_saved" to "ذخیره‌ی الگوی تراکنش",
    "bill_saved" to "ثبتِ قبض",
    "bill_paid" to "پرداختِ قبض",
    "category_added" to "دسته‌ی دلخواه",
    "categories_reordered" to "مرتب‌کردنِ دسته‌ها",
    "goal_added" to "هدفِ پس‌انداز",
    "asset_trade" to "خرید/فروشِ دارایی",
    "counterparty_added" to "طرفِ حسابِ تازه",
    "debt_added" to "ثبتِ طلب/بدهی",
    "debt_settled" to "تسویه‌ی طلب/بدهی",
    "dang_created" to "دنگ",
    "note_added" to "یادداشت",
    "sms_auto_tx" to "تراکنشِ خودکار از پیامک",
    "notif_auto_tx" to "تراکنشِ خودکار از اعلان",
    "sms_rule_saved" to "قاعده‌ی پیامک",
    "notif_import_toggled" to "روشن/خاموشِ خواندنِ اعلان",
    "auto_tx_toggled" to "روشن/خاموشِ اعلانِ تراکنش",
    "backup_exported" to "گرفتنِ پشتیبان",
    "restore_local" to "بازیابی از گوشی",
    "restore_cloud" to "بازیابی از سرور",
    "export_loans_pdf" to "خروجیِ PDFِ وام",
    "export_loans_excel" to "خروجیِ اکسلِ وام",
    "export_accounting_pdf" to "خروجیِ PDFِ حساب‌ها",
    "export_accounting_excel" to "خروجیِ اکسلِ حساب‌ها",
    "export_cheques_pdf" to "خروجیِ PDFِ چک",
    "export_cheques_excel" to "خروجیِ اکسلِ چک",
    "pin_set" to "تنظیمِ قفل",
    "biometric_toggled" to "اثرِ انگشت",
    "theme_mode_changed" to "تغییرِ تمِ روشن/تیره",
    "font_scale_changed" to "اندازه‌ی متن",
    "shop_buy" to "خرید از فروشگاهِ سکه",
    "coin_goal_set" to "هدفِ سکه",
)

@Composable
private fun HourChart(counts: List<Int>) {
    val primary = AppPrimary
    val track = AppSurface2
    val max = counts.maxOrNull()?.coerceAtLeast(1) ?: 1
    Canvas(modifier = Modifier.fillMaxWidth().height(70.dp).padding(top = 6.dp)) {
        val slot = size.width / 24
        val barW = slot * 0.6f
        // RTL: ساعتِ ۰ راست، ۲۳ چپ.
        counts.forEachIndexed { i, c ->
            val x = size.width - (i + 1) * slot + (slot - barW) / 2
            drawRoundRect(track, Offset(x, 0f), Size(barW, size.height), CornerRadius(3f, 3f))
            val h = size.height * c / max
            if (h > 0f) drawRoundRect(primary, Offset(x, size.height - h), Size(barW, h), CornerRadius(3f, 3f))
        }
    }
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("۰", "۶", "۱۲", "۱۸", "۲۳").forEach { Text(it, color = AppLabel, fontSize = 11.sp, modifier = Modifier.weight(1f)) }
    }
}

@Composable
private fun InstallRowView(r: AdminInstallRow) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppSurface2)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(r.device ?: "گوشیِ نامشخص", color = AppText, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Text("#${r.id}", color = AppLabel, fontSize = 11.sp)
        }
        Text(
            listOfNotNull(
                "نصب ${r.firstDay.faDigitsAscii()}",
                "آخرین بار ${r.lastDay.faDigitsAscii()}",
                "${toFa(r.activeDays)} روزِ فعال",
            ).joinToString(" · "),
            color = AppMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp),
        )
        Text(
            listOfNotNull(
                "${toFa(r.sessions30)} بار در ماه",
                "${toFa(r.minutes30)} دقیقه",
                r.topScreen?.let { "بیشتر: ${screenLabel(it)}" },
                r.version?.let { "نسخه $it" },
                r.store?.let { STORE_LABELS[it] ?: it },
                r.android?.let { "اندروید ${it.faDigitsAscii()}" },
                if (r.loggedIn) "وارد شده" + (r.userCode?.let { " · $it" } ?: "") else "مهمان",
            ).joinToString(" · "),
            color = AppLabel,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

private fun formatDuration(seconds: Int): String = when {
    seconds < 60 -> "${toFa(seconds)} ثانیه"
    seconds < 3600 -> "${toFa(seconds / 60)} دقیقه"
    else -> "${toFa(seconds / 3600)} ساعت و ${toFa((seconds % 3600) / 60)} دقیقه"
}

private fun profileValue(v: String): String = when (v) {
    "true" -> "بله"
    "false" -> "خیر"
    "com.farsitel.bazaar" -> "کافه‌بازار"
    "ir.mservices.market" -> "مایکت"
    "com.android.vending" -> "گوگل‌پلی"
    "unknown" -> "نامعلوم (فایلِ مستقیم)"
    "none" -> "ندارد"
    "dark" -> "تیره"
    "light" -> "روشن"
    "system" -> "مطابقِ گوشی"
    "default" -> "پیش‌فرض"
    "?" -> "نامشخص"
    else -> v.faDigitsAscii()
}

private val WEEKDAY_LABELS = mapOf(7 to "شنبه", 1 to "یکشنبه", 2 to "دوشنبه", 3 to "سه‌شنبه", 4 to "چهارشنبه", 5 to "پنجشنبه", 6 to "جمعه")

private val PROFILE_LABELS = mapOf(
    "installer" to "منبعِ نصب",
    "subscription" to "اشتراک",
    "device_brand" to "برندِ گوشی",
    "device_model" to "مدلِ گوشی",
    "android" to "نسخه‌ی اندروید",
    "screen_dp" to "اندازه‌ی صفحه",
    "lang" to "زبانِ گوشی",
    "system_dark" to "گوشی در حالتِ تیره",
    "theme_mode" to "تمِ برنامه",
    "color_theme" to "رنگِ تم",
    "font_scale_app" to "اندازه‌ی متنِ برنامه",
    "font_scale_sys" to "اندازه‌ی متنِ گوشی",
    "lock" to "قفلِ برنامه",
    "biometric" to "اثرِ انگشت",
    "privacy_mode" to "پنهان‌کردنِ مبلغ‌ها",
    "perm_notifications" to "اجازه‌ی اعلان",
    "perm_sms" to "اجازه‌ی پیامک",
    "perm_calendar" to "اجازه‌ی تقویم",
    "notif_listener" to "خواندنِ اعلانِ بانک",
    "battery_unrestricted" to "باتریِ بدونِ محدودیت",
    "sms_import" to "ثبتِ خودکار از پیامک",
    "notif_import" to "ثبتِ خودکار از اعلان",
    "reminders" to "یادآوری‌ها",
    "reminder_hour" to "ساعتِ یادآوری",
    "daily_reminder" to "یادآورِ روزانه",
    "auto_backup" to "پشتیبانِ خودکار",
    "vibration" to "لرزش",
    "reduced_motion" to "انیمیشنِ کم",
    "owned_themes" to "تعدادِ تمِ خریده‌شده",
    "owned_items" to "تعدادِ آیتمِ فروشگاه",
)

private val ADOPTION_LABELS = mapOf(
    "loans" to "وام",
    "cheques" to "چک",
    "cheque_books" to "دسته‌چک",
    "accounts" to "حساب",
    "account_transactions" to "تراکنش",
    "tx_auto" to "تراکنشِ خودکار (پیامک/اعلان)",
    "budgets" to "بودجه",
    "assets" to "دارایی",
    "asset_trades" to "خرید و فروشِ دارایی",
    "debts" to "طلب و بدهی",
    "counterparties" to "طرف‌حساب",
    "notes" to "یادداشت",
    "incomes" to "درآمد",
    "recurring_payments" to "پرداختِ تکراری",
    "savings_goals" to "هدفِ پس‌انداز",
    "tx_templates" to "الگوی تراکنش",
    "bills" to "قبض",
    "parsing_rules" to "قانونِ پیامک",
    "custom_categories" to "دسته‌ی دلخواه",
    "dang_events" to "دنگ",
    "inbox_messages" to "پیامِ مرکزِ پیام‌ها",
    "calculation_history" to "محاسبه‌ی ذخیره‌شده",
    "achievements" to "نشان",
)

private val PLAN_LABELS = mapOf("1m" to "یک‌ماهه", "3m" to "سه‌ماهه", "6m" to "شش‌ماهه", "1y" to "یک‌ساله")

/** 💰 فروشِ واقعی (تأییدشده‌ی سرور) + مسیرِ خرید - کاغذِ طلایی، جزئیات در چهار زیربخشِ جمع‌شونده. */
@Composable
private fun SalesCard(st: AdminStatsResponse) {
    val sales = st.sales.orEmpty()
    val actions = st.actions.orEmpty()
    fun actionSum(prefix: String) = actions.filter { it.name.startsWith(prefix) }.sumOf { it.total }
    fun actionUsers(prefix: String) = actions.filter { it.name.startsWith(prefix) }.sumOf { it.users }
    AppCard(variant = AppCardVariant.GOLD, label = "فروش و اشتراک") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GoldStat("مشترکِ فعال", adminNum(st.activeSubscribers), "نفر", Modifier.weight(1f))
            GoldStat("فروشِ ۳۰ روز", adminNum(sales.sumOf { it.count30 }), "خرید", Modifier.weight(1f))
            GoldStat("درآمدِ ۳۰ روز", adminNum(sales.sumOf { it.tomans30.toLong() }), "تومان", Modifier.weight(1.4f))
        }
        AdminNote("کلِ عمر: ${adminNum(sales.sumOf { it.countAll })} خرید · ${adminNum(sales.sumOf { it.tomansAll.toLong() })} تومان", gold = true)
        Spacer(Modifier.height(6.dp))
        if (sales.isNotEmpty()) {
            val top = sales.maxByOrNull { it.count30 }
            AdminSubSection("به‌تفکیکِ پلن", top?.let { "بیشترین: ${PLAN_LABELS[it.product] ?: it.product} · ${toFa(it.count30)} در ماه" }, gold = true) {
                val max = sales.maxOf { it.countAll }.coerceAtLeast(1)
                sales.forEach { r ->
                    AdminBarRow(
                        label = PLAN_LABELS[r.product] ?: r.product,
                        fraction = r.countAll.toFloat() / max,
                        trailing = "${toFa(r.count30)} در ماه · ${adminNum(r.countAll)} کل",
                        sub = "درآمدِ کل ${adminNum(r.tomansAll.toLong())} تومان",
                        gold = true,
                    )
                }
            }
        }
        val byStore = st.salesByStore.orEmpty()
        val byTier = st.activeByTier.orEmpty()
        if (byStore.isNotEmpty() || byTier.isNotEmpty()) {
            val storeTotal = byStore.sumOf { it.count }.coerceAtLeast(1)
            AdminSubSection(
                "استور و پلنِ مشترک‌ها",
                byStore.sortedByDescending { it.count }.take(2).joinToString(" · ") { "${STORE_LABELS[it.name] ?: it.name} ${toFa(percent(it.count, storeTotal))}٪" },
                gold = true,
            ) {
                SplitBar("استورِ فروش‌های ماه", byStore.map { SplitPart(STORE_LABELS[it.name] ?: it.name, it.count) }, gold = true)
                SplitBar("مشترک‌های فعال به‌تفکیکِ پلن", byTier.map { SplitPart(PLAN_LABELS[it.name] ?: it.name, it.count) }, gold = true)
            }
        }
        val daily = st.salesDaily.orEmpty()
        if (daily.any { it.count > 0 }) {
            AdminSubSection("فروشِ روزانه", "۳۰ روز · بیشترین ${toFa(daily.maxOf { it.count })} خرید در یک روز", gold = true) {
                AdminLineChart(daily.map { it.count }, height = 90.dp, gold = true)
            }
        }
        val steps = listOf(
            "صفحه‌ی اشتراک را دیدند" to actionUsers("paywall_view"),
            "روی خرید زدند" to actionUsers("purchase_start_"),
            "خریدشان موفق شد" to actionUsers("purchase_done_"),
        )
        AdminSubSection("مسیرِ خرید", "${adminNum(steps[0].second)} دیدند · ${adminNum(steps[2].second)} خریدند", gold = true) {
            FunnelChart(steps, gold = true)
            AdminNote(
                "انصراف: ${adminNum(actionSum("purchase_cancel_"))} بار · ناموفق: ${adminNum(actionSum("purchase_failed_"))} بار" +
                    (actionSum("purchase_verify_failed").takeIf { it > 0 }?.let { " · پول رفته ولی تأیید نشده (پیگیری کن): ${adminNum(it)} بار" } ?: "") +
                    " · کدِ هدیه‌ی استفاده‌شده: ${adminNum(st.giftsUsed30)}",
                gold = true,
            )
        }
    }
}

@Composable
private fun GoldStat(label: String, value: String, unit: String, modifier: Modifier) {
    Column(modifier) {
        Text(label, color = AppGoldInk2, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, color = AppGoldInk, fontSize = 18.sp, fontWeight = FontWeight.Black, maxLines = 1)
        Text(unit, color = AppGoldInk2, fontSize = 11.sp)
    }
}

/** ⏱ لحظه‌ای + خطِ ۳۰ روزه + چسبندگی (DAU/MAU) + ازدست‌رفته‌ها. */
@Composable
private fun LiveCard(st: AdminStatsResponse) {
    AppCard(label = "همین حالا") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiTile("داخلِ برنامه", adminNum(st.activeNow), Modifier.weight(1f), unit = "نفر")
            KpiTile("یک ساعتِ اخیر", adminNum(st.activeLastHour), Modifier.weight(1f), unit = "نفر")
            KpiTile("میانگینِ روزانه", faDecimal(st.avgDau30), Modifier.weight(1f), unit = "نفر")
        }
        val daily = st.daily.orEmpty()
        if (daily.isNotEmpty()) {
            AdminSubTitle("کاربرِ فعال · ۳۰ روز")
            AdminLineChart(daily.map { it.active }, height = 64.dp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp)) {
            KpiTile("چسبندگی", toFa(percent(Math.round(st.avgDau30 * 10).toInt(), st.active30 * 10)), Modifier.weight(1f), unit = "٪")
            KpiTile("فعالِ هفته", adminNum(st.active7), Modifier.weight(1f), unit = "نفر")
            KpiTile("ازدست‌رفته", adminNum(st.churned), Modifier.weight(1f), unit = "نصب")
        }
        AdminNote(
            "«داخلِ برنامه» یعنی در ۵ دقیقه‌ی اخیر. «چسبندگی» = میانگینِ کاربرِ روزانه تقسیم بر کاربرِ ماه " +
                "(اپ‌های خوب ۲۰٪ به بالا). «ازدست‌رفته» = نصب‌هایی که ۱۴ روز است نیامده‌اند.",
        )
    }
}

/** 📅 جدولِ ماندگاریِ هفتگی - جمع‌شونده؛ در حالتِ بسته میانگینِ هفته‌ی ۱. */
@Composable
private fun CohortCard(cohorts: List<ir.sadteam.loancalc.data.network.AdminCohortRow>) {
    val rows = cohorts.filter { it.size > 0 }
    val week1 = rows.mapNotNull { c -> c.weeks.orEmpty().getOrNull(1)?.let { percent(it, c.size) } }
    val summary = if (week1.isEmpty()) "${toFa(rows.size)} هفته" else "هفته‌ی ۱: میانگینِ ${toFa(week1.average().toInt())}٪ · ${toFa(rows.size)} هفته"
    AdminSection("ماندگاریِ هفتگی (گروهِ نصب)", summary) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Text("هفته‌ی نصب", color = AppLabel, fontSize = 11.sp, modifier = Modifier.width(84.dp))
            (0..7).forEach { k -> Text(toFa(k), color = AppLabel, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.weight(1f)) }
        }
        rows.forEach { c ->
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${c.weekStart.faDigitsAscii()} (${toFa(c.size)})", color = AppText, fontSize = 11.sp, maxLines = 1, modifier = Modifier.width(84.dp))
                val weeks = c.weeks.orEmpty()
                (0..7).forEach { k ->
                    val v = weeks.getOrNull(k)
                    val pct = if (v == null) null else percent(v, c.size)
                    Box(
                        modifier = Modifier.weight(1f).padding(1.dp).height(24.dp).clip(RoundedCornerShape(4.dp))
                            .background(if (pct == null) AppSurface2 else AppPrimary.copy(alpha = 0.12f + 0.8f * pct / 100f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (pct != null) Text(toFa(pct), color = if (pct > 55) Color.White else AppText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        AdminNote("عددِ هر خانه درصدِ کسانی است که آن هفته هنوز برنامه را باز کرده‌اند.")
    }
}

/** 👋 کاربرِ تازه در کدام مرحله‌ی معرفی ول می‌کند - قیف. */
@Composable
private fun OnboardingCard(actions: List<AdminFeatureUsage>) {
    val steps = (0..4).map { i -> actions.firstOrNull { it.name == "onboarding_step_$i" }?.users ?: 0 }
    if (steps.all { it == 0 }) return
    AppCard(label = "معرفیِ اولِ برنامه · تا کجا جلو رفتند") {
        FunnelChart(steps.mapIndexed { i, n -> "مرحله‌ی ${toFa(i + 1)}" to n })
    }
}

/** 🔔 اعلان‌ها: دو میله برای هر نوع - فرستاده (خاکستری) و باز شده (سبز). */
@Composable
private fun NotificationCard(actions: List<AdminFeatureUsage>) {
    val types = listOf("loan" to "قسطِ وام", "cheque" to "چک", "bill" to "قبض", "recurring" to "پرداختِ تکراری", "autotx" to "تراکنشِ خودکار", "daily" to "یادآورِ روزانه", "comeback" to "دلمون تنگ شده")
    fun total(name: String) = actions.firstOrNull { it.name == name }?.total ?: 0
    val rows = types.map { (key, label) -> Triple(label, total("notif_shown_$key"), total("notif_open_$key")) }.filter { it.second > 0 || it.third > 0 }
    val buttons = actions.filter { it.name.startsWith("notif_button_") }
    if (rows.isEmpty() && buttons.isEmpty()) return
    val sent = AppMarkOff
    val opened = AppPrimary
    val track = AppSurface2
    AppCard(label = "اعلان‌ها (۳۰ روز)") {
        if (rows.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) { AdminLegend(sent, "فرستاده"); AdminLegend(opened, "باز شد") }
            val max = rows.maxOf { maxOf(it.second, it.third) }.coerceAtLeast(1)
            rows.forEach { (label, shown, open) ->
                Column(Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    Row {
                        Text(label, color = AppText, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                        if (shown > 0) Text("${toFa(percent(open, shown))}٪ باز شد", color = AppPrimaryInk, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                    listOf(shown to sent, open to opened).forEach { (n, c) ->
                        Box(Modifier.fillMaxWidth().padding(top = 3.dp).height(6.dp).clip(RoundedCornerShape(99.dp)).background(track)) {
                            Box(Modifier.fillMaxWidth(n.toFloat() / max).height(6.dp).clip(RoundedCornerShape(99.dp)).background(c))
                        }
                    }
                    Text("${adminNum(shown)} فرستاده · ${adminNum(open)} باز شد", color = AppLabel, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
        if (buttons.isNotEmpty()) {
            AdminNote("دکمه‌های داخلِ اعلان: " + buttons.joinToString("، ") { "${NOTIF_BUTTON_LABELS[it.name.removePrefix("notif_button_")] ?: it.name} ${adminNum(it.total)}" })
        }
    }
}

private val NOTIF_BUTTON_LABELS = mapOf("mark_paid" to "«پرداخت شد»", "snooze" to "«فردا یادم بنداز»", "confirm_tx" to "«تأیید»", "reject_tx" to "«رد»")

/** 🩺 سلامتِ برنامه: وضعیتِ یک‌خطی، کرش به‌تفکیکِ نسخه، سرعتِ باز شدن، خطاهای بی‌صدا. */
@Composable
private fun HealthCard(st: AdminStatsResponse) {
    val per100 = if (st.active30 == 0) 0 else Math.round(st.crashes30 * 100f / st.active30)
    AppCard(label = "سلامتِ برنامه (۳۰ روز)") {
        val (pill, ink, text) = when {
            st.crashes30 == 0 -> Triple(AppPrimaryPill, AppPrimaryInk, "پایدار · بی کرش در ۳۰ روز")
            per100 <= 1 -> Triple(AppWarningPill, AppWarningInk, "${adminNum(st.crashes30)} کرش · کمتر از ۱ در هر ۱۰۰ نفر")
            else -> Triple(AppDangerPill, AppDangerInk, "${toFa(per100)} کرش در هر ۱۰۰ نفر")
        }
        Text(text, color = ink, fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(pill).padding(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp)) {
            KpiTile("کرش", adminNum(st.crashes30), Modifier.weight(1f), unit = "بار")
            KpiTile("خطای همگام‌سازی", adminNum(st.nonFatal30), Modifier.weight(1f), unit = "بار")
            KpiTile("گوشی با ۳+ حساب", adminNum(st.multiAccountDevices), Modifier.weight(1f), unit = "گوشی")
        }
        KpiTile("بی ماهِ مجانی (گوشیِ تکراری)", adminNum(st.trialBlockedUsers), Modifier.fillMaxWidth().padding(top = 8.dp), unit = "حساب")
        val byVersion = st.crashesByVersion.orEmpty().take(6)
        if (byVersion.isNotEmpty()) {
            AdminSubTitle("کرش به‌تفکیکِ نسخه")
            AdminColumnChart(byVersion.map { it.count }, byVersion.map { it.name }, height = 70.dp, color = AppDanger)
        }
        val perf = st.perf.orEmpty()
        if (perf.isNotEmpty()) {
            val buckets = listOf("start_0_800" to "زیرِ ۰٫۸ث", "start_800_1500" to "۰٫۸ تا ۱٫۵", "start_1500_3000" to "۱٫۵ تا ۳", "start_3000_6000" to "۳ تا ۶", "start_6000_plus" to "بیش از ۶")
            SplitBar("سرعتِ باز شدنِ برنامه (ثانیه)", buckets.map { (k, label) -> SplitPart(label, perf.firstOrNull { it.name == k }?.total ?: 0) }, top = 5, sort = false)
        }
        val errors = st.errors.orEmpty()
        if (errors.isNotEmpty()) {
            AdminSubTitle("خطاهای بی‌صدا")
            val max = errors.maxOf { it.total }.coerceAtLeast(1)
            TopList(errors) { AdminBarRow(ERROR_LABELS[it.name] ?: it.name, it.total.toFloat() / max, "${adminNum(it.total)} بار · ${adminNum(it.users)} نفر") }
        }
        val top = st.topCrashes.orEmpty()
        if (top.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            AdminSubSection("پرتکرارترین کرش‌ها (${toFa(top.size)})", top.first().name) {
                top.forEach { Text("${toFa(it.count)}× ${it.name}", color = AppMuted, fontSize = 11.sp, lineHeight = 17.sp, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp)) }
            }
        }
    }
}

private val ERROR_LABELS = mapOf(
    "accounts_import" to "برنگشتنِ حساب‌ها از سرور",
    "sync_conflict" to "تداخلِ دو گوشی",
    "sync_push_http" to "نرسیدنِ پشتیبان به سرور",
    "otp_rate_limited" to "کدِ ورود: درخواستِ زیاد",
    "otp_sms_send_failed" to "کدِ ورود: پیامک نرفت",
    "otp_invalid_phone" to "کدِ ورود: شماره‌ی اشتباه",
    "otp_unknown" to "کدِ ورود: خطای نامشخص",
)
