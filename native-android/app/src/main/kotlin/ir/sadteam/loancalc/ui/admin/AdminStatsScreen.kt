package ir.sadteam.loancalc.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
                        Text("آمارِ جیبک", color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Black)
                        Text(
                            "بی‌نام · به‌ازای هر نصب، نه هر آدم · فقط تو می‌بینی",
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
                is AdminStatsViewModel.State.Ready -> statsContent(s.stats)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.statsContent(st: AdminStatsResponse) {
    val daily = st.daily.orEmpty()
    val screens = st.screens.orEmpty()
    val actions = st.actions.orEmpty()
    val funnel = st.funnel.orEmpty()

    item { Insights(st) }

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

    item { FeatureCard("صفحه‌ها (۳۰ روز)", screens, st.active30, ::screenLabel, SCREEN_LABELS.keys) }
    item { FeatureCard("کارها (۳۰ روز)", actions, st.active30, ::actionLabel, ACTION_LABELS.keys) }

    item {
        AppCard(label = "نسخه، استور و اندروید (فعال‌های ماه)") {
            SplitRow("نسخه", st.versions.orEmpty()) { toFa(it) }
            SplitRow("استور", st.stores.orEmpty()) { STORE_LABELS[it] ?: it }
            SplitRow("اندروید", st.sdks.orEmpty()) { sdkLabel(it) }
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

private fun screenLabel(key: String): String = SCREEN_LABELS[key] ?: key

private fun actionLabel(key: String): String = ACTION_LABELS[key]
    ?: when {
        key.startsWith("purchase_start_") -> "شروعِ خریدِ اشتراکِ ${key.removePrefix("purchase_start_")}"
        key.startsWith("purchase_done_") -> "خریدِ موفقِ اشتراکِ ${key.removePrefix("purchase_done_")}"
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
