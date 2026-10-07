package ir.sadteam.loancalc.ui.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.network.AdminFeatureUsage
import ir.sadteam.loancalc.data.network.AdminNamedCount
import ir.sadteam.loancalc.data.network.AdminStatsResponse
import ir.sadteam.loancalc.data.network.AdminInstallRow
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppText
import androidx.compose.material.icons.filled.Info
import ir.sadteam.loancalc.ui.components.AppCardVariant
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppGoldInk
import ir.sadteam.loancalc.ui.theme.AppGoldInk2
import ir.sadteam.loancalc.ui.theme.AppGoldBorder
import ir.sadteam.loancalc.ui.theme.AppMarkOff
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import ir.sadteam.loancalc.ui.theme.AppWarningPill

@Composable
internal fun UsersCard(st: AdminStatsResponse) {
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
internal fun RetentionCard(st: AdminStatsResponse) {
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
internal fun TimeCard(st: AdminStatsResponse) {
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
/** جمع‌بندیِ خودکار - «از این عددها چه بفهمم». فقط توصیف است، نه حکم. سه خطِ اول، بقیه پشتِ «بقیه». */
@Composable
internal fun Insights(st: AdminStatsResponse) {
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
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Lightbulb, null, tint = Color(0xFFFACC15), modifier = Modifier.size(22.dp))
            Text("بینش‌ها", color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 6.dp))
        }
        TopList(lines, visible = 3) { line ->
            Row(modifier = Modifier.padding(top = 6.dp), verticalAlignment = Alignment.Top) {
                Box(Modifier.padding(top = 8.dp).size(6.dp).clip(CircleShape).background(AppPrimary))
                Text(line, color = AppText, fontSize = 12.5.sp, lineHeight = 21.sp, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
@Composable
internal fun FeatureCard(
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
internal fun InstallRowView(r: AdminInstallRow) {
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
/** 💰 فروشِ واقعی (تأییدشده‌ی سرور) + مسیرِ خرید - کاغذِ طلایی، جزئیات در چهار زیربخشِ جمع‌شونده. */
@Composable
internal fun SalesCard(st: AdminStatsResponse) {
    val sales = st.sales.orEmpty()
    val actions = st.actions.orEmpty()
    fun actionSum(prefix: String) = actions.filter { it.name.startsWith(prefix) }.sumOf { it.total }
    fun actionUsers(prefix: String) = actions.filter { it.name.startsWith(prefix) }.sumOf { it.users }
    // ۱۶ مهر: بازطراحی طبقِ طرح - کارتِ طلاییِ خلاصه + هر بخش در کارتِ جدا.
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    AppCard(variant = AppCardVariant.GOLD) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("فروش و اشتراک", color = AppGoldInk, fontSize = 17.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Row(
                Modifier.clip(RoundedCornerShape(999.dp)).border(1.dp, AppGoldBorder, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.CalendarMonth, null, tint = AppGoldInk2, modifier = Modifier.size(16.dp))
                Text("۳۰ روزِ گذشته", color = AppGoldInk2, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 5.dp))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
            // فقط پولی‌ها؛ رایگان/هدیه جدا زیرِ کاشی‌ها گفته می‌شود (قبلاً ۷۹۷ با رایگان‌ها قاطی بود).
            val freeTier = st.activeByTier.orEmpty().filter { it.name == "?" }.sumOf { it.count }
            GoldStat("مشترکِ پولی", adminNum(st.activeSubscribers - freeTier), "نفر", Modifier.weight(1f), Icons.Filled.Groups)
            GoldStat("فروشِ ۳۰ روز", adminNum(sales.sumOf { it.count30 }), "خرید", Modifier.weight(1f), Icons.Filled.ShoppingCart)
            GoldStat("درآمدِ ۳۰ روز", adminNum(sales.sumOf { it.tomans30.toLong() }), "تومان", Modifier.weight(1.2f), Icons.Filled.Savings)
        }
        Row(
            Modifier.padding(top = 10.dp).fillMaxWidth().clip(RoundedCornerShape(14.dp)).border(1.dp, AppGoldBorder, RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("${adminNum(st.activeByTier.orEmpty().filter { it.name == "?" }.sumOf { it.count })} نفر در دوره‌ی رایگان/هدیه · کلِ عمر: ${adminNum(sales.sumOf { it.countAll })} خرید · ${adminNum(sales.sumOf { it.tomansAll.toLong() })} تومان", color = AppGoldInk2, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.Info, null, tint = AppGoldInk2, modifier = Modifier.size(20.dp))
        }
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalBoxedSection provides true) {
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
        val inst = st.installsByStore.orEmpty()
        if (inst.isNotEmpty()) {
            val logged = st.loggedByStore.orEmpty().associate { it.name to it.count }
            AdminSubSection(
                "کاربران از هر فروشگاه",
                inst.joinToString(" · ") { "${STORE_LABELS[it.name] ?: it.name} ${adminNum(it.count)}" },
            ) {
                SplitBar("نصب‌ها", inst.map { SplitPart(STORE_LABELS[it.name] ?: it.name, it.count) })
                AdminNote(inst.joinToString(" · ") { "${STORE_LABELS[it.name] ?: it.name}: ${adminNum(it.count)} نصب، ${adminNum(logged[it.name] ?: 0)} واردِ حساب شدند" })
            }
        }
        // ۱۶ مهر: پیامک‌های کدِ ورود به‌تفکیکِ استور (شمارش از همین نسخه‌ی سرور شروع شد).
        val smsAll = st.smsAllByStore.orEmpty()
        run {
            fun storeName(n: String) = STORE_LABELS[n] ?: if (n == "unknown") "هنوز وارد نشده" else n
            fun total(l: List<ir.sadteam.loancalc.data.network.AdminNamedCount>) = l.sumOf { it.count }
            AdminSubSection("پیامکِ کدِ ورود", "امروز ${adminNum(total(st.smsTodayByStore.orEmpty()))} · ۳۰ روز ${adminNum(total(st.sms30ByStore.orEmpty()))}") {
                AdminNote("هر بار که کسی برای واردشدن به حساب «کدِ تأیید» خواست، یک پیامک برایش فرستاده شد. این‌جا شمارِ همین پیامک‌هاست.")
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmsCount("امروز", total(st.smsTodayByStore.orEmpty()), Modifier.weight(1f))
                    SmsCount("۳۰ روزِ اخیر", total(st.sms30ByStore.orEmpty()), Modifier.weight(1f))
                    SmsCount("از اول", total(smsAll), Modifier.weight(1f))
                }
                if (smsAll.isNotEmpty()) {
                    Text("از کدام فروشگاه (از اول)", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
                    smsAll.sortedByDescending { it.count }.forEach {
                        Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(storeName(it.name), color = AppText, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text("${adminNum(it.count)} پیامک", color = AppMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if ((st.smsFailed30 ?: 0) > 0) AdminNote("در ۳۰ روزِ اخیر ${adminNum(st.smsFailed30 ?: 0)} پیامک نرسید (خطای سرویسِ پیامک).")
                AdminNote("«هنوز وارد نشده» = کسی که کد گرفته ولی هنوز واردِ حساب نشده، برای همین معلوم نیست از کدام فروشگاه است؛ بعد از ورود خودکار درست می‌شود. شمارش از ۱۶ مهر شروع شده.")
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
}
@Composable
private fun GoldStat(label: String, value: String, unit: String, modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(AppGoldInk.copy(alpha = 0.08f)).border(1.dp, AppGoldBorder, RoundedCornerShape(16.dp)).padding(vertical = 8.dp, horizontal = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = AppGoldInk, modifier = Modifier.size(16.dp))
            Text(label, color = AppGoldInk2, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 4.dp))
        }
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 2.dp)) {
            Text(value, color = AppGoldInk, fontSize = 18.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text(unit, color = AppGoldInk2, fontSize = 10.sp, maxLines = 1, modifier = Modifier.padding(start = 4.dp, bottom = 3.dp))
        }
    }
}
/** ⏱ لحظه‌ای + خطِ ۳۰ روزه + چسبندگی (DAU/MAU) + ازدست‌رفته‌ها. */
@Composable
internal fun LiveCard(st: AdminStatsResponse) {
    // ۱۶ مهر: بازطراحی طبقِ طرحِ ChatGPT - کاشی‌های رنگی با آیکون، نمودار با محور و تاریخ.
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("نمای کلیِ عملکرد", color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Black)
            Icon(Icons.Filled.Info, null, tint = AppMuted, modifier = Modifier.padding(start = 6.dp).size(18.dp))
            Spacer(Modifier.weight(1f))
            Row(
                Modifier.clip(RoundedCornerShape(999.dp)).background(AppSurface2).padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("کاربرِ فعالِ ۳۰ روز: ${adminNum(st.active30)}", color = AppText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Box(Modifier.padding(start = 6.dp).size(8.dp).clip(CircleShape).background(Color(0xFF22C55E)))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
            ColorStat("داخلِ برنامه", adminNum(st.activeNow), "نفر", Icons.Filled.Groups, Color(0xFF10B981), Modifier.weight(1f))
            ColorStat("یک ساعتِ اخیر", adminNum(st.activeLastHour), "نفر", Icons.Filled.CalendarMonth, Color(0xFF3B82F6), Modifier.weight(1f))
            ColorStat("میانگینِ روزانه", faDecimal(st.avgDau30), "نفر", Icons.Filled.AccessTime, Color(0xFF8B5CF6), Modifier.weight(1f))
        }
        val daily = st.daily.orEmpty()
        if (daily.isNotEmpty()) {
            Column(
                Modifier.padding(top = 12.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .border(1.dp, AppLineRow, RoundedCornerShape(16.dp)).padding(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("کاربرِ فعالِ ۳۰ روزِ اخیر", color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Black)
                    Icon(Icons.Filled.TrendingUp, null, tint = AppPrimary, modifier = Modifier.padding(start = 6.dp).size(18.dp))
                }
                val values = daily.map { it.active }
                val max = (values.maxOrNull() ?: 0).coerceAtLeast(4)
                val top = ((max + 3) / 4) * 4
                Row(Modifier.padding(top = 8.dp)) {
                    AdminLineChart(values, height = 96.dp, maxValue = top, modifier = Modifier.weight(1f))
                    Column(Modifier.height(96.dp).padding(start = 6.dp, top = 6.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        listOf(top, top * 3 / 4, top / 2, top / 4, 0).forEach { Text(toFa(it), color = AppLabel, fontSize = 9.sp) }
                    }
                }
                val idx = listOf(0, daily.size / 4, daily.size / 2, daily.size * 3 / 4, daily.size - 1).distinct()
                Row(Modifier.fillMaxWidth().padding(top = 4.dp, end = 18.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    idx.forEach { Text(daily[it].day.faDigitsAscii(), color = AppLabel, fontSize = 9.5.sp) }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
            ColorStat("چسبندگی", toFa(percent(Math.round(st.avgDau30 * 10).toInt(), st.active30 * 10)), "٪", Icons.Filled.MonitorHeart, Color(0xFFF59E0B), Modifier.weight(1f))
            ColorStat("فعالِ هفته", adminNum(st.active7), "نفر", Icons.Filled.CalendarToday, Color(0xFF3B82F6), Modifier.weight(1f))
            ColorStat("از دست رفته", adminNum(st.churned), "نصب", Icons.Filled.Download, Color(0xFF8B5CF6), Modifier.weight(1f))
        }
        Row(
            Modifier.padding(top = 12.dp).fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .border(1.dp, AppLineRow, RoundedCornerShape(14.dp)).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "«داخلِ برنامه» یعنی در ۵ دقیقه‌ی اخیر. «چسبندگی» = میانگینِ کاربرِ روزانه تقسیم بر کاربرِ ماه " +
                    "(اپ‌های خوب ۲۰٪ به بالا). «از دست رفته» = نصب‌هایی که ۱۴ روز است نیامده‌اند.",
                color = AppMuted, fontSize = 11.sp, lineHeight = 19.sp, modifier = Modifier.weight(1f),
            )
            Icon(Icons.Filled.Info, null, tint = AppMuted, modifier = Modifier.padding(start = 8.dp).size(20.dp))
        }
    }
}
/** کاشیِ رنگیِ آماری با آیکون (طرحِ ۱۶ مهر). */
@Composable
private fun ColorStat(label: String, value: String, unit: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(color.copy(alpha = 0.28f), color.copy(alpha = 0.10f))))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.weight(1f))
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        }
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 2.dp)) {
            Text(value, color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text(unit, color = AppMuted, fontSize = 10.sp, maxLines = 1, modifier = Modifier.padding(start = 4.dp, bottom = 3.dp))
        }
    }
}
/** 📅 جدولِ ماندگاریِ هفتگی - جمع‌شونده؛ در حالتِ بسته میانگینِ هفته‌ی ۱. */
@Composable
internal fun CohortCard(cohorts: List<ir.sadteam.loancalc.data.network.AdminCohortRow>) {
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
internal fun OnboardingCard(actions: List<AdminFeatureUsage>) {
    val steps = (0..4).map { i -> actions.firstOrNull { it.name == "onboarding_step_$i" }?.users ?: 0 }
    if (steps.all { it == 0 }) return
    AppCard(label = "معرفیِ اولِ برنامه · تا کجا جلو رفتند") {
        FunnelChart(steps.mapIndexed { i, n -> "مرحله‌ی ${toFa(i + 1)}" to n })
    }
}
/** 🔔 اعلان‌ها: دو میله برای هر نوع - فرستاده (خاکستری) و باز شده (سبز). */
@Composable
internal fun NotificationCard(actions: List<AdminFeatureUsage>) {
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
/** 🩺 سلامتِ برنامه: وضعیتِ یک‌خطی، کرش به‌تفکیکِ نسخه، سرعتِ باز شدن، خطاهای بی‌صدا. */
@Composable
internal fun HealthCard(st: AdminStatsResponse) {
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
        KpiTile("ماهِ رایگان نگرفت (گوشیِ تکراری)", adminNum(st.trialBlockedUsers), Modifier.fillMaxWidth().padding(top = 8.dp), unit = "حساب")
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
@Composable
private fun SmsCount(label: String, value: Int, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).background(AppSurface2).padding(vertical = 8.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(adminNum(value), color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text(label, color = AppMuted, fontSize = 11.sp, maxLines = 1)
    }
}
