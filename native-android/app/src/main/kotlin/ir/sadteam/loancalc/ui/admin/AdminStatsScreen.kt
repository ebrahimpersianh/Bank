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

/**
 * **آمارِ جیبک** - فقط برای صاحبِ برنامه (۷ مهر). هدف: تصمیم‌گیری درباره‌ی تغییرها - کدام صفحه
 * دیده می‌شود، کدام قابلیت استفاده می‌شود، کاربرها کجا رها می‌کنند، و برمی‌گردند یا نه.
 * همه‌ی عددها بی‌نام‌اند (به‌ازای نصب، نه آدم) - رجوع کن به `data.UsageStats`.
 */
@Composable
fun AdminStatsScreen(onBack: () -> Unit, viewModel: AdminStatsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }
    BackHandler(onBack = onBack)
    // ۸ مهر: گزارش سه زبانه شد تا یک ستونِ بی‌پایان نباشد.
    var tab by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(AppBg)) {
        LazyColumn(
            contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 40.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText)
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                        Text("گزارشِ برنامه", color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Black)
                        Text(
                            "بی‌نام · به‌ازای هر نصب، نه هر آدم",
                            color = AppMuted,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "تازه‌سازی", tint = AppMuted)
                    }
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
                    item {
                        // ۸ مهر: هفت زبانه‌ی کوتاه به‌جای سه زبانه‌ی بلند - هر کدام یکی دو کارت.
                        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val tabs = listOf("خلاصه", "کاربران", "ماندگاری", "زمان", "بخش‌ها", "گوشی‌ها", "سلامت")
                            items(tabs.size) { i ->
                                val sel = i == tab
                                Text(
                                    tabs[i],
                                    color = if (sel) androidx.compose.ui.graphics.Color.White else AppText,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
                                        .background(if (sel) AppPrimary else AppSurface2)
                                        .clickable { tab = i }
                                        .padding(horizontal = 14.dp, vertical = 9.dp),
                                )
                            }
                        }
                    }
                    statsContent(s.stats, tab)
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.statsContent(st: AdminStatsResponse, tab: Int) {
    val daily = st.daily.orEmpty()
    val screens = st.screens.orEmpty()
    val actions = st.actions.orEmpty()
    val funnel = st.funnel.orEmpty()

    if (tab == 0) {
    item { LiveCard(st) }

    item { Insights(st) }

    item { SalesCard(st) }

    }
    if (tab == 1) {
    item {
        AppCard(label = "کاربرها") {
            val rows = listOf(
                Triple("امروز", st.activeToday, "فعال"),
                Triple("۷ روزِ اخیر", st.active7, "فعال"),
                Triple("۳۰ روزِ اخیر", st.active30, "فعال"),
                Triple("نصبِ تازه · ۷ روز", st.new7, "نصب"),
                Triple("نصبِ تازه · ۳۰ روز", st.new30, "نصب"),
                Triple("کلِ نصب‌ها", st.totalInstalls, "نصب"),
            )
            rows.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    row.forEach { (label, value, unit) -> StatTile(label, toFa(value), unit, Modifier.weight(1f)) }
                }
            }
            Text(
                "«فعال» یعنی دست‌کم یک بار برنامه را باز کرده. " +
                    "${toFa(percent(st.loggedInActive30, st.active30))}٪ از فعال‌های ماه وارد حساب شده‌اند.",
                color = AppMuted,
                fontSize = 10.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }

    if (daily.isNotEmpty()) {
        item {
            AppCard(label = "۳۰ روزِ اخیر") {
                DailyChart(daily.map { it.active }, daily.map { it.new })
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Legend(AppPrimary, "کاربرِ فعال")
                    Legend(AppInfo, "نصبِ تازه")
                }
                val peak = daily.maxByOrNull { it.active }
                if (peak != null && peak.active > 0) {
                    Text(
                        "شلوغ‌ترین روز: ${peak.day.faDigitsAscii()} با ${toFa(peak.active)} کاربر",
                        color = AppMuted,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
    }

    if (tab == 2) {
    item {
        AppCard(label = "ماندگاری - برمی‌گردند؟") {
            st.retention.orEmpty().forEach { r ->
                val label = when (r.afterDays) {
                    1 -> "بعد از یک روز"
                    7 -> "بعد از یک هفته"
                    30 -> "بعد از یک ماه"
                    else -> "بعد از ${toFa(r.afterDays)} روز"
                }
                BarRow(
                    label = label,
                    fraction = if (r.base == 0) 0f else r.returned.toFloat() / r.base,
                    trailing = if (r.base == 0) "هنوز زود است" else "${toFa(percent(r.returned, r.base))}٪ از ${toFa(r.base)}",
                )
            }
            Text(
                "هر نفر در ماه به‌طورِ میانگین ${faDecimal(st.avgActiveDays30)} روز برنامه را باز کرده؛ " +
                    "در ۷ روزِ اخیر ${toFa(st.sessions7)} بار استفاده (هر بار = باز کردن بعد از دست‌کم نیم ساعت).",
                color = AppMuted,
                fontSize = 10.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }

    val cohorts = st.cohorts.orEmpty()
    if (cohorts.any { it.size > 0 }) {
        item { CohortCard(cohorts) }
    }

    item { OnboardingCard(actions) }

    item { NotificationCard(actions) }
    }

    if (tab == 3) {
    val flows = st.flows.orEmpty().sortedByDescending { it.total }
    if (flows.isNotEmpty()) {
        item {
            AppCard(label = "مسیرِ حرکت بینِ صفحه‌ها (۳۰ روز)") {
                val max = flows.first().total.coerceAtLeast(1)
                flows.take(25).forEach { f ->
                    val (from, to) = f.name.split("--", limit = 2).let { it[0] to it.getOrElse(1) { "" } }
                    BarRow("${screenLabel(from)} ← ${screenLabel(to)}", f.total.toFloat() / max, "${toFa(f.total)} بار · ${toFa(f.users)} نفر")
                }
            }
        }
    }
    }

    if (tab == 6) {
    item { HealthCard(st) }
    }

    if (tab == 2) {
    if (funnel.isNotEmpty()) {
        item {
            AppCard(label = "مسیرِ کاربرِ تازه") {
                val base = funnel.first().count
                funnel.forEach { f ->
                    BarRow(
                        label = FUNNEL_LABELS[f.name] ?: f.name,
                        fraction = if (base == 0) 0f else f.count.toFloat() / base,
                        trailing = "${toFa(f.count)} · ${toFa(percent(f.count, base))}٪",
                    )
                }
            }
        }
    }
    }

    if (tab == 3) {
    item {
        AppCard(label = "زمانِ استفاده (۳۰ روز)") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile("هر بار استفاده", faDecimal(st.avgSessionMinutes), "دقیقه", Modifier.weight(1f))
                StatTile("صفحه در هر بار", faDecimal(st.avgScreensPerSession), "صفحه", Modifier.weight(1f))
                StatTile("جمعِ زمان", toFa(st.totalMinutes30), "دقیقه", Modifier.weight(1f))
            }
            val hours = st.hours.orEmpty()
            if (hours.isNotEmpty()) {
                Text("ساعتِ باز کردنِ برنامه (به وقتِ ایران)", color = AppPrimaryInk, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 12.dp))
                HourChart((0..23).map { h -> hours.firstOrNull { it.name.toIntOrNull() == h }?.count ?: 0 })
                val peak = hours.maxByOrNull { it.count }
                if (peak != null) {
                    Text("پرکارترین ساعت: ${toFa(peak.name.toIntOrNull() ?: 0)}", color = AppMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
            val days = st.weekdays.orEmpty()
            if (days.isNotEmpty()) {
                Text("روزِ هفته", color = AppPrimaryInk, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 12.dp))
                val total = days.sumOf { it.count }.coerceAtLeast(1)
                // Calendar: ۱=یکشنبه … ۷=شنبه؛ نمایش از شنبه.
                listOf(7, 1, 2, 3, 4, 5, 6).forEach { d ->
                    val c = days.firstOrNull { it.name == d.toString() }?.count ?: 0
                    BarRow(WEEKDAY_LABELS[d] ?: d.toString(), c.toFloat() / total, "${toFa(c)} · ${toFa(percent(c, total))}٪")
                }
            }
        }
    }

    val screenTime = st.screenTime.orEmpty().sortedByDescending { it.total }
    if (screenTime.isNotEmpty()) {
        item {
            AppCard(label = "بیشترین زمان روی کدام صفحه (۳۰ روز)") {
                val max = screenTime.first().total.coerceAtLeast(1)
                screenTime.take(20).forEach { f ->
                    BarRow(
                        label = screenLabel(f.name),
                        fraction = f.total.toFloat() / max,
                        trailing = "${formatDuration(f.total)} · ${toFa(f.users)} نفر",
                        sub = if (f.users > 0) "میانگینِ هر نفر ${formatDuration(f.total / f.users)}" else null,
                    )
                }
            }
        }
    }

    }
    if (tab == 4) {
    item { FeatureCard("صفحه‌ها (۳۰ روز)", screens, st.active30, ::screenLabel, SCREEN_LABELS.keys) }
    item { FeatureCard("کارها (۳۰ روز)", actions, st.active30, ::actionLabel, ACTION_LABELS.keys) }

    val adoption = st.adoption.orEmpty()
    if (adoption.isNotEmpty()) {
        item {
            AppCard(label = "از هر بخش چند نفر واقعاً استفاده می‌کنند") {
                val base = st.profiledInstalls.coerceAtLeast(1)
                adoption.forEach { a ->
                    BarRow(
                        label = ADOPTION_LABELS[a.key] ?: a.key,
                        fraction = a.installs.toFloat() / base,
                        trailing = "${toFa(a.installs)} نفر · ${toFa(percent(a.installs, base))}٪",
                        sub = if (a.installs > 0) "میانگینِ هر نفر: ${faDecimal(a.avg)} مورد" else null,
                    )
                }
                Text(
                    "از روی ${toFa(st.profiledInstalls)} نصبِ فعالِ ماه - فقط تعداد، نه محتوا.",
                    color = AppMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
    }

    if (tab == 5) {
    val splits = st.profileSplits.orEmpty()
    if (splits.isNotEmpty()) {
        item {
            AppCard(label = "گوشی‌ها و تنظیماتِ کاربرها") {
                splits.forEach { sp -> SplitRow(PROFILE_LABELS[sp.key] ?: sp.key, sp.values.orEmpty()) { profileValue(it) } }
            }
        }
    }

    val installs = st.installsList.orEmpty()
    if (installs.isNotEmpty()) {
        item {
            AppCard(label = "آخرین نصب‌ها (هر ردیف = یک گوشیِ بی‌نام)") {
                installs.forEach { r -> InstallRowView(r) }
            }
        }
    }

    item {
        AppCard(label = "نسخه، استور و اندروید (فعال‌های ماه)") {
            SplitRow("نسخه", st.versions.orEmpty()) { toFa(it) }
            SplitRow("استور", st.stores.orEmpty()) { STORE_LABELS[it] ?: it }
            SplitRow("اندروید", st.sdks.orEmpty()) { sdkLabel(it) }
        }
    }
    }

    item {
        Text(
            "چه چیزی جمع نمی‌شود: هیچ مبلغ، عنوان، نامِ حساب، شماره‌ی کارت یا موبایل، و هیچ متنِ پیامکی. " +
                "گوشیِ بی‌اینترنت آمارش را بعداً می‌فرستد، پس عددهای امروز ممکن است کمی عقب باشند.",
            color = AppLabel,
            fontSize = 9.5.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

/** جمع‌بندیِ خودکار - «از این عددها چه بفهمم». فقط توصیف است، نه حکم. */
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
        st.actions.orEmpty().firstOrNull()?.let { add("پرتکرارترین کار: ${actionLabel(it.name)} (${toFa(it.total)} بار).") }
    }
    AppCard(label = "خلاصه") {
        lines.forEach { line ->
            Row(modifier = Modifier.padding(top = 6.dp), verticalAlignment = Alignment.Top) {
                Box(Modifier.padding(top = 7.dp).size(5.dp).clip(CircleShape).background(AppPrimary))
                Text(line, color = AppText, fontSize = 11.5.sp, lineHeight = 19.sp, modifier = Modifier.padding(start = 8.dp))
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
            Text("هنوز چیزی ثبت نشده.", color = AppMuted, fontSize = 11.sp)
        } else {
            val maxUsers = items.maxOf { it.users }.coerceAtLeast(1)
            items.forEach { f ->
                BarRow(
                    label = label(f.name),
                    fraction = f.users.toFloat() / maxUsers,
                    trailing = "${toFa(f.users)} نفر · ${toFa(f.total)} بار",
                    sub = if (active30 > 0) "${toFa(percent(f.users, active30))}٪ کاربرها" else null,
                )
            }
        }
        val missing = known.filter { k -> items.none { it.name == k } }
        if (missing.isNotEmpty() && items.isNotEmpty()) {
            Text(
                "بدونِ استفاده: " + missing.joinToString("، ") { label(it) },
                color = AppDangerInk,
                fontSize = 10.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
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
        Text(label, color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 3.dp)) {
            Text(value, color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text(unit, color = AppLabel, fontSize = 9.sp, modifier = Modifier.padding(start = 3.dp, bottom = 3.dp))
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
            Text(trailing, color = AppMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
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
        if (sub != null) Text(sub, color = AppLabel, fontSize = 9.sp, modifier = Modifier.padding(top = 2.dp))
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
        Text(label, color = AppMuted, fontSize = 10.sp)
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
        listOf("۰", "۶", "۱۲", "۱۸", "۲۳").forEach { Text(it, color = AppLabel, fontSize = 9.sp, modifier = Modifier.weight(1f)) }
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
            Text("#${r.id}", color = AppLabel, fontSize = 9.sp)
        }
        Text(
            listOfNotNull(
                "نصب ${r.firstDay.faDigitsAscii()}",
                "آخرین بار ${r.lastDay.faDigitsAscii()}",
                "${toFa(r.activeDays)} روزِ فعال",
            ).joinToString(" · "),
            color = AppMuted,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 2.dp),
        )
        Text(
            listOfNotNull(
                "${toFa(r.sessions30)} بار در ماه",
                "${toFa(r.minutes30)} دقیقه",
                r.topScreen?.let { "بیشتر: ${screenLabel(it)}" },
                r.version?.let { "نسخه ${toFa(it)}" },
                r.store?.let { STORE_LABELS[it] ?: it },
                r.android?.let { "اندروید ${it.faDigitsAscii()}" },
                if (r.loggedIn) "وارد شده" + (r.userCode?.let { " · $it" } ?: "") else "مهمان",
            ).joinToString(" · "),
            color = AppLabel,
            fontSize = 9.5.sp,
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

/** 💰 فروشِ واقعی (تأییدشده‌ی سرور) + مسیرِ خرید از روی رویدادها. */
@Composable
private fun SalesCard(st: AdminStatsResponse) {
    val sales = st.sales.orEmpty()
    val actions = st.actions.orEmpty()
    fun actionSum(prefix: String) = actions.filter { it.name.startsWith(prefix) }.sumOf { it.total }
    fun actionUsers(prefix: String) = actions.filter { it.name.startsWith(prefix) }.sumOf { it.users }
    AppCard(label = "فروش و اشتراک") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile("مشترکِ فعال", toFa(st.activeSubscribers), "نفر", Modifier.weight(1f))
            StatTile("فروشِ ۳۰ روز", toFa(sales.sumOf { it.count30 }), "خرید", Modifier.weight(1f))
            StatTile("کلِ فروش", toFa(sales.sumOf { it.countAll }), "خرید", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            StatTile("درآمدِ ۳۰ روز", fmt(sales.sumOf { it.tomans30 }.toDouble()).let { toFa(it) }, "تومان", Modifier.weight(1f))
            StatTile("کلِ درآمد", fmt(sales.sumOf { it.tomansAll }.toDouble()).let { toFa(it) }, "تومان", Modifier.weight(1f))
        }
        if (sales.isNotEmpty()) {
            Text("به‌تفکیکِ پلن", color = AppPrimaryInk, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 10.dp))
            val max = sales.maxOf { it.countAll }.coerceAtLeast(1)
            sales.forEach { r ->
                BarRow(
                    label = PLAN_LABELS[r.product] ?: r.product,
                    fraction = r.countAll.toFloat() / max,
                    trailing = "${toFa(r.count30)} در ماه · ${toFa(r.countAll)} کل",
                    sub = "درآمدِ کل ${toFa(fmt(r.tomansAll.toDouble()))} تومان",
                )
            }
        }
        SplitRow("استورِ فروش‌های ماه", st.salesByStore.orEmpty()) { STORE_LABELS[it] ?: it }
        SplitRow("مشترک‌های فعال به‌تفکیکِ پلن", st.activeByTier.orEmpty()) { PLAN_LABELS[it] ?: it }
        val daily = st.salesDaily.orEmpty()
        if (daily.any { it.count > 0 }) {
            Text("فروشِ روزانه (۳۰ روز)", color = AppPrimaryInk, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 10.dp))
            DailyChart(daily.map { it.count }, emptyList())
        }
        Text("مسیرِ خرید (۳۰ روز)", color = AppPrimaryInk, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 10.dp))
        val steps = listOf(
            "صفحه‌ی اشتراک را دیدند" to actionUsers("paywall_view"),
            "روی خرید زدند" to actionUsers("purchase_start_"),
            "خریدشان موفق شد" to actionUsers("purchase_done_"),
        )
        val base = steps.first().second.coerceAtLeast(1)
        steps.forEach { (label, n) -> BarRow(label, n.toFloat() / base, "${toFa(n)} نفر · ${toFa(percent(n, base))}٪") }
        Text(
            "انصراف: ${toFa(actionSum("purchase_cancel_"))} بار · ناموفق: ${toFa(actionSum("purchase_failed_"))} بار" +
                (actionSum("purchase_verify_failed").takeIf { it > 0 }?.let { " · ⚠️ پول رفته ولی تأیید نشده: ${toFa(it)} بار" } ?: "") +
                " · کدِ هدیه‌ی استفاده‌شده: ${toFa(st.giftsUsed30)}",
            color = AppMuted,
            fontSize = 10.sp,
            lineHeight = 17.sp,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

/** ⏱ لحظه‌ای + چسبندگی (DAU/MAU) + ازدست‌رفته‌ها. */
@Composable
private fun LiveCard(st: AdminStatsResponse) {
    AppCard(label = "همین حالا") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile("داخلِ برنامه", toFa(st.activeNow), "نفر", Modifier.weight(1f))
            StatTile("یک ساعتِ اخیر", toFa(st.activeLastHour), "نفر", Modifier.weight(1f))
            StatTile("میانگینِ روزانه", faDecimal(st.avgDau30), "نفر", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            StatTile("چسبندگی", toFa(percent(Math.round(st.avgDau30 * 10).toInt(), st.active30 * 10)), "٪", Modifier.weight(1f))
            StatTile("فعالِ هفته", toFa(st.active7), "نفر", Modifier.weight(1f))
            StatTile("ازدست‌رفته", toFa(st.churned), "نصب", Modifier.weight(1f))
        }
        Text(
            "«داخلِ برنامه» یعنی در ۵ دقیقه‌ی اخیر. «چسبندگی» = میانگینِ کاربرِ روزانه تقسیم بر کاربرِ ماه " +
                "(اپ‌های خوب ۲۰٪ به بالا). «ازدست‌رفته» = نصب‌هایی که ۱۴ روز است نیامده‌اند.",
            color = AppMuted,
            fontSize = 10.sp,
            lineHeight = 17.sp,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

/** 📅 جدولِ ماندگاریِ هفتگی - هر ردیف نصب‌های یک هفته، هر ستون درصدِ برگشته در هفته‌ی بعد. */
@Composable
private fun CohortCard(cohorts: List<ir.sadteam.loancalc.data.network.AdminCohortRow>) {
    AppCard(label = "ماندگاریِ هفتگی (گروهِ نصب)") {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Text("هفته‌ی نصب", color = AppLabel, fontSize = 9.sp, modifier = Modifier.width(78.dp))
            (0..7).forEach { k -> Text(if (k == 0) "هفته‌ی ۰" else toFa(k), color = AppLabel, fontSize = 9.sp, modifier = Modifier.weight(1f)) }
        }
        cohorts.filter { it.size > 0 }.forEach { c ->
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${c.weekStart.faDigitsAscii()} (${toFa(c.size)})", color = AppText, fontSize = 9.5.sp, maxLines = 1, modifier = Modifier.width(78.dp))
                val weeks = c.weeks.orEmpty()
                (0..7).forEach { k ->
                    val v = weeks.getOrNull(k)
                    val pct = if (v == null) null else percent(v, c.size)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(1.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (pct == null) AppSurface2 else AppPrimary.copy(alpha = 0.12f + 0.8f * pct / 100f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (pct != null) Text(toFa(pct), color = if (pct > 55) Color.White else AppText, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Text("عددِ هر خانه درصدِ کسانی است که آن هفته هنوز برنامه را باز کرده‌اند.", color = AppMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 8.dp))
    }
}

/** 👋 کاربرِ تازه در کدام مرحله‌ی معرفی ول می‌کند. */
@Composable
private fun OnboardingCard(actions: List<ir.sadteam.loancalc.data.network.AdminFeatureUsage>) {
    val steps = (0..4).map { i -> actions.firstOrNull { it.name == "onboarding_step_$i" }?.users ?: 0 }
    if (steps.all { it == 0 }) return
    AppCard(label = "معرفیِ اولِ برنامه - تا کجا جلو رفتند") {
        val base = steps.first().coerceAtLeast(1)
        steps.forEachIndexed { i, n -> BarRow("مرحله‌ی ${toFa(i + 1)}", n.toFloat() / base, "${toFa(n)} نفر · ${toFa(percent(n, base))}٪") }
    }
}

/** 🔔 اعلان‌ها: چندتا فرستاده شد، چندتا باز شد، چندتا دکمه‌اش زده شد. */
@Composable
private fun NotificationCard(actions: List<ir.sadteam.loancalc.data.network.AdminFeatureUsage>) {
    val types = listOf("loan" to "قسطِ وام", "cheque" to "چک", "bill" to "قبض", "recurring" to "پرداختِ تکراری", "autotx" to "تراکنشِ خودکار", "daily" to "یادآورِ روزانه", "comeback" to "دلمون تنگ شده")
    fun total(name: String) = actions.firstOrNull { it.name == name }?.total ?: 0
    val rows = types.map { (key, label) -> Triple(label, total("notif_shown_$key"), total("notif_open_$key")) }.filter { it.second > 0 || it.third > 0 }
    val buttons = actions.filter { it.name.startsWith("notif_button_") }
    if (rows.isEmpty() && buttons.isEmpty()) return
    AppCard(label = "اعلان‌ها (۳۰ روز)") {
        rows.forEach { (label, shown, opened) ->
            BarRow(label, if (shown == 0) 0f else opened.toFloat() / shown, "${toFa(shown)} فرستاده · ${toFa(opened)} باز شد", sub = if (shown > 0) "نرخِ باز شدن ${toFa(percent(opened, shown))}٪" else null)
        }
        if (buttons.isNotEmpty()) {
            Text(
                "دکمه‌های داخلِ اعلان: " + buttons.joinToString("، ") { "${NOTIF_BUTTON_LABELS[it.name.removePrefix("notif_button_")] ?: it.name} ${toFa(it.total)}" },
                color = AppMuted,
                fontSize = 10.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

private val NOTIF_BUTTON_LABELS = mapOf("mark_paid" to "«پرداخت شد»", "snooze" to "«فردا یادم بنداز»", "confirm_tx" to "«تأیید»", "reject_tx" to "«رد»")

/** 🩺 سلامتِ برنامه: کرش، خطاهای بی‌صدا و سرعتِ بالا آمدن. */
@Composable
private fun HealthCard(st: AdminStatsResponse) {
    AppCard(label = "سلامتِ برنامه (۳۰ روز)") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile("کرش", toFa(st.crashes30), "بار", Modifier.weight(1f))
            StatTile("کرش در هر ۱۰۰ نفر", toFa(if (st.active30 == 0) 0 else Math.round(st.crashes30 * 100f / st.active30)), "", Modifier.weight(1f))
            StatTile("خطای همگام‌سازی", toFa(st.nonFatal30), "بار", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            StatTile("گوشی با ۳+ حساب", toFa(st.multiAccountDevices), "گوشی", Modifier.weight(1f))
            StatTile("بی ماهِ مجانی (گوشیِ تکراری)", toFa(st.trialBlockedUsers), "حساب", Modifier.weight(1f))
        }
        SplitRow("کرش به‌تفکیکِ نسخه", st.crashesByVersion.orEmpty()) { it.faDigitsAscii() }
        val top = st.topCrashes.orEmpty()
        if (top.isNotEmpty()) {
            Text("پرتکرارترین کرش‌ها", color = AppPrimaryInk, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 10.dp))
            top.forEach { Text("${toFa(it.count)}× ${it.name}", color = AppMuted, fontSize = 9.5.sp, lineHeight = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp)) }
        }
        val errors = st.errors.orEmpty()
        if (errors.isNotEmpty()) {
            Text("خطاهای بی‌صدا", color = AppPrimaryInk, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 10.dp))
            val max = errors.maxOf { it.total }.coerceAtLeast(1)
            errors.forEach { BarRow(ERROR_LABELS[it.name] ?: it.name, it.total.toFloat() / max, "${toFa(it.total)} بار · ${toFa(it.users)} نفر") }
        }
        val perf = st.perf.orEmpty()
        if (perf.isNotEmpty()) {
            Text("سرعتِ باز شدنِ برنامه", color = AppPrimaryInk, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 10.dp))
            val total = perf.sumOf { it.total }.coerceAtLeast(1)
            listOf("start_0_800" to "زیرِ ۰٫۸ ثانیه", "start_800_1500" to "۰٫۸ تا ۱٫۵ ثانیه", "start_1500_3000" to "۱٫۵ تا ۳ ثانیه", "start_3000_6000" to "۳ تا ۶ ثانیه", "start_6000_plus" to "بیش از ۶ ثانیه").forEach { (k, label) ->
                val n = perf.firstOrNull { it.name == k }?.total ?: 0
                if (n > 0) BarRow(label, n.toFloat() / total, "${toFa(n)} بار · ${toFa(percent(n, total))}٪")
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
