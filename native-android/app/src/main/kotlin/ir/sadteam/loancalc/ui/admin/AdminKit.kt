package ir.sadteam.loancalc.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppCardVariant
import ir.sadteam.loancalc.ui.theme.AppAssetBorder
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppChartGrid
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppGoldBorder
import ir.sadteam.loancalc.ui.theme.AppGoldInk
import ir.sadteam.loancalc.ui.theme.AppGoldInk2
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMarkOff
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPurple
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import ir.sadteam.loancalc.ui.theme.AppWarningPill

/**
 * 🧰 قطعه‌های مشترکِ پنلِ ادمین (بخشِ ۸۲). هر صفحه‌ی ادمین فقط از این‌ها می‌سازد تا سربرگ، کاشی،
 * بخشِ جمع‌شونده و نمودار در همه یک شکل باشند. رنگ فقط از توکن‌ها - هیچ هگزی در این فایل نیست.
 * طلایی فقط وقتی `gold = true` است، و فقط برای فروش/اشتراک/هدیه‌ی اشتراک.
 */

// ── عدد ─────────────────────────────────────────────────────────────────────────
/** عددِ فارسی با جداکننده‌ی `٬`. `fmt()` لاتین است، پس همیشه از این رد شود. */
internal fun adminNum(v: Long): String = toFa(fmt(v.toDouble())).replace(',', '٬')
internal fun adminNum(v: Int): String = adminNum(v.toLong())
internal fun adminPct(part: Int, whole: Int): Int = if (whole <= 0) 0 else Math.round(part * 100f / whole)
internal fun adminPct(part: Long, whole: Long): Int = if (whole <= 0L) 0 else Math.round(part * 100f / whole)
/** خالصِ تقریبی بعد از کارمزدِ استور (هر دو استور ۷۶٫۹٪ - همان عددِ گزارشِ روز). */
internal fun adminNet(gross: Long): Long = gross * 769 / 1000

/** `2026-09-29` یا `2026-09-29T10:00` → «۷ مهر». */
internal fun adminDay(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return runCatching {
        val (y, m, d) = iso.take(10).split('-').map { it.toInt() }
        val p = ir.sadteam.loancalc.core.JalaliCalendar.fromGregorian(y, m, d)
        "${toFa(p.d)} ${ir.sadteam.loancalc.ui.components.persianMonthName(p.m)}"
    }.getOrDefault(toFa(iso.take(10)))
}

/** تغییر نسبت به دوره‌ی قبل. [tone]: ۱ خوب، −۱ بد، ۰ بی‌تغییر. */
internal data class AdminDelta(val text: String, val tone: Int)

internal fun adminDelta(now: Long, prev: Long, badWhenUp: Boolean = false): AdminDelta {
    val diff = now - prev
    if (diff == 0L) return AdminDelta("بدونِ تغییر", 0)
    val arrow = if (diff > 0) "▲ " else "▼ "
    val body = if (prev > 0) "${toFa((kotlin.math.abs(diff) * 100 / prev).toInt())}٪" else adminNum(kotlin.math.abs(diff))
    return AdminDelta(arrow + body, if ((diff > 0) != badWhenUp) 1 else -1)
}

@Composable
internal fun AdminDelta.color(): Color = when (tone) { 1 -> AppPrimaryInk; -1 -> AppDangerInk; else -> AppMuted }

/** کادرهای ادمین: گوشه‌ی ۱۶dp تا متنِ چندخطی راحت نوشته شود (کپسولِ ۲۸dp برای جستجوی یک‌خطی است). */
// کادرهای ادمین (۱۰ مهر، کاربر: «این باکس‌ها حالمو بهم می‌زنن»): گوشه‌ی ملایم، حاشیه‌ی خنثی، بی قابِ آبیِ کلفت.
internal val AdminFieldShape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)

@Composable
internal fun adminFieldColors() = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AppPrimary,
    unfocusedBorderColor = AppLine,
    focusedContainerColor = AppSurface,
    unfocusedContainerColor = AppSurface,
    cursorColor = AppPrimary,
    focusedLabelColor = AppPrimary,
    unfocusedLabelColor = AppMuted,
)

// ── سربرگ و صفحه ────────────────────────────────────────────────────────────────
@Composable
internal fun AdminHeader(title: String, subtitle: String, onBack: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowForward, "بازگشت", tint = AppText) }
        Column(Modifier.weight(1f).padding(start = 4.dp)) {
            Text(title, color = AppText, fontSize = 19.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = AppMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        actions()
    }
}

/** دکمه‌ی آیکونیِ سربرگ - ۴۴dp، حاشیه‌ی ۲ پیکسلی. */
@Composable
internal fun AdminHeaderAction(icon: ImageVector, description: String, tint: Color = AppPrimaryInk, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        Modifier.padding(start = 6.dp).size(44.dp).clip(shape).background(AppSurface).border(2.dp, AppLine, shape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, description, tint = tint, modifier = Modifier.size(22.dp)) }
}

/** صفحه‌ی اسکرولیِ ساده‌ی ادمین. صفحه‌های `LazyColumn`دار فقط [AdminHeader] را برمی‌دارند. */
@Composable
internal fun AdminPage(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    BackHandler(onBack = onBack)
    Column(
        Modifier.fillMaxSize().background(AppBg).verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AdminHeader(title, subtitle, onBack, actions)
        content()
        Spacer(Modifier.height(28.dp))
    }
}

/** برچسبِ گروه بیرونِ کارت («دیدن»، «کار کردن»). */
@Composable
internal fun AdminGroupLabel(text: String) {
    Text(text, color = AppMuted, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
}

/** عنوانِ فرعیِ داخلِ کارت. */
@Composable
internal fun AdminSubTitle(text: String, gold: Boolean = false) {
    Text(text, color = if (gold) AppGoldInk else AppPrimaryInk, fontSize = 11.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 12.dp))
}

@Composable
internal fun AdminNote(text: String, gold: Boolean = false) {
    Text(text, color = if (gold) AppGoldInk2 else AppMuted, fontSize = 11.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 8.dp))
}

// ── زبانه ───────────────────────────────────────────────────────────────────────
@Composable
internal fun AdminTabs(tabs: List<String>, selected: Int, onSelect: (Int) -> Unit, dots: Set<Int> = emptySet()) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(tabs.size) { i ->
            val sel = i == selected
            Row(
                Modifier.heightIn(min = 44.dp).clip(RoundedCornerShape(999.dp)).background(if (sel) AppPrimary else AppChipBg)
                    .clickable { onSelect(i) }.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(tabs[i], color = if (sel) Color.White else AppText, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                if (i in dots) Box(Modifier.padding(start = 5.dp).size(7.dp).clip(CircleShape).background(AppDanger))
            }
        }
    }
}

// ── هشدار ───────────────────────────────────────────────────────────────────────
@Composable
internal fun AdminAlerts(lines: List<Pair<ImageVector, String>>, onClick: (() -> Unit)? = null) {
    if (lines.isEmpty()) return
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(AppWarningPill)) {
        lines.forEachIndexed { i, (icon, text) ->
            if (i > 0) Box(Modifier.fillMaxWidth().height(1.5.dp).background(AppBg))
            Row(
                Modifier.fillMaxWidth().heightIn(min = 44.dp)
                    .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, null, tint = AppWarningInk, modifier = Modifier.size(20.dp))
                Text(text, color = AppWarningInk, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f).padding(start = 10.dp))
                if (onClick != null) Icon(Icons.Filled.ChevronLeft, null, tint = AppWarningInk, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ── کاشیِ عدد ───────────────────────────────────────────────────────────────────
/**
 * عددِ اصلی + واحد + فلش + (اختیاری) دو میله‌ی «حالا/قبلی».
 * [standalone] = خودش یک `AppCard` است؛ وگرنه کاشیِ `surface2` داخلِ کارتِ دیگر.
 */
@Composable
internal fun KpiTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    delta: AdminDelta? = null,
    compare: Pair<Long, Long>? = null,
    prevText: String? = null,
    standalone: Boolean = false,
    gold: Boolean = false,
    icon: ImageVector? = null,
    accent: Color? = null,
) {
    val body: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) Box(Modifier.padding(end = 8.dp).size(30.dp).clip(RoundedCornerShape(10.dp)).background((accent ?: AppPrimary).copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = accent ?: AppPrimary, modifier = Modifier.size(18.dp))
            }
            Text(label, color = if (gold) AppGoldInk2 else AppMuted, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 2.dp)) {
            Text(value, color = if (gold) AppGoldInk else AppText, fontSize = 20.sp, fontWeight = FontWeight.Black, maxLines = 1)
            if (unit != null) Text(unit, color = if (gold) AppGoldInk2 else AppLabel, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, bottom = 3.dp))
        }
        if (delta != null) Text(delta.text, color = delta.color(), fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
        if (compare != null) {
            val max = maxOf(compare.first, compare.second).coerceAtLeast(1L).toFloat()
            MiniBar(compare.first / max, accent ?: AppPrimary, Modifier.padding(top = 6.dp))
            MiniBar(compare.second / max, AppMarkOff, Modifier.padding(top = 3.dp))
        }
        if (prevText != null) Text(prevText, color = AppLabel, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
    }
    if (standalone) {
        AppCard(modifier = modifier, horizontalPadding = 12.dp, contentPadding = 12.dp) { body() }
    } else {
        Column(modifier.clip(RoundedCornerShape(14.dp)).background(AppSurface2).padding(10.dp)) { body() }
    }
}

@Composable
private fun MiniBar(fraction: Float, color: Color, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(99.dp)).background(AppSurface2)) {
        Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(99.dp)).background(color))
    }
}

// ── ردیفِ فهرست (هاب) ───────────────────────────────────────────────────────────
@Composable
internal fun AdminListRow(icon: ImageVector, title: String, subtitle: String, pill: Color, ink: Color, divider: Boolean, onClick: () -> Unit) {
    Column {
        if (divider) Box(Modifier.fillMaxWidth().height(1.5.dp).background(AppLineRow))
        Row(Modifier.fillMaxWidth().heightIn(min = 60.dp).clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(AppRadius.icon)).background(pill), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = ink, modifier = Modifier.size(21.dp))
            }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(title, color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Text(subtitle, color = AppMuted, fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Filled.ChevronLeft, null, tint = AppLabel, modifier = Modifier.size(20.dp))
        }
    }
}

// ── بخشِ جمع‌شونده ───────────────────────────────────────────────────────────────
/** کارتِ جمع‌شونده. در حالتِ بسته [summary] یک خط زیرِ عنوان می‌نشیند. */
@Composable
internal fun AdminSection(
    title: String,
    summary: String? = null,
    modifier: Modifier = Modifier,
    initiallyOpen: Boolean = false,
    gold: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var open by rememberSaveable(title) { mutableStateOf(initiallyOpen) }
    AppCard(modifier = modifier, variant = if (gold) AppCardVariant.GOLD else AppCardVariant.DEFAULT, contentPadding = 2.dp) {
        AdminSectionBody(title, summary, open, { open = !open }, gold, content)
    }
}

/** همان بخشِ جمع‌شونده بی کارت - برای ردیف‌های داخلِ یک کارتِ دیگر (فروش). */
@Composable
internal fun AdminSubSection(title: String, summary: String? = null, gold: Boolean = false, divider: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    var open by rememberSaveable(title) { mutableStateOf(false) }
    if (divider) Box(Modifier.fillMaxWidth().height(1.5.dp).background(if (gold) AppGoldBorder else AppLineRow))
    AdminSectionBody(title, summary, open, { open = !open }, gold, content)
}

/** آیکون و رنگِ کاشیِ هر بخش از روی عنوانش (طرحِ ChatGPT، ۱۰ مهر). */
private fun sectionIcon(title: String): Pair<androidx.compose.ui.graphics.vector.ImageVector, androidx.compose.ui.graphics.Color> {
    val I = Icons.Filled
    return when {
        "پیامک" in title -> I.Sms to androidx.compose.ui.graphics.Color(0xFF16A34A)
        "آپدیت" in title || "نسخه‌ی تازه" in title -> I.SystemUpdate to androidx.compose.ui.graphics.Color(0xFF0D9488)
        "اشتراک" in title -> I.WorkspacePremium to androidx.compose.ui.graphics.Color(0xFFD97706)
        "سکه" in title -> I.MonetizationOn to androidx.compose.ui.graphics.Color(0xFFCA8A04)
        "نظرسنجی" in title -> I.Poll to androidx.compose.ui.graphics.Color(0xFF7C3AED)
        "دسته" in title -> I.Category to androidx.compose.ui.graphics.Color(0xFF2563EB)
        "کارت" in title -> I.CreditCard to androidx.compose.ui.graphics.Color(0xFF0EA5E9)
        "مجانی" in title || "رایگان" in title -> I.CardGiftcard to androidx.compose.ui.graphics.Color(0xFFC026D3)
        "خاموش" in title -> I.PowerSettingsNew to androidx.compose.ui.graphics.Color(0xFFDC2626)
        "اعلان" in title -> I.Notifications to androidx.compose.ui.graphics.Color(0xFFEA580C)
        "پرسش" in title -> I.HelpOutline to androidx.compose.ui.graphics.Color(0xFF4F46E5)
        "تم" in title || "پس‌زمینه" in title -> I.Palette to androidx.compose.ui.graphics.Color(0xFF9333EA)
        "تخفیف" in title || "ویژه" in title -> I.LocalOffer to androidx.compose.ui.graphics.Color(0xFFE11D48)
        else -> I.Tune to androidx.compose.ui.graphics.Color(0xFF475569)
    }
}

@Composable
private fun AdminSectionBody(title: String, summary: String?, open: Boolean, toggle: () -> Unit, gold: Boolean, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth().heightIn(min = 60.dp).clickable(onClick = toggle), verticalAlignment = Alignment.CenterVertically) {
            val (ic, col) = sectionIcon(title)
            Box(Modifier.padding(end = 12.dp).size(42.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)).background(col.copy(alpha = 0.85f)), contentAlignment = Alignment.Center) {
                Icon(ic, null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, color = if (gold) AppGoldInk else AppText, fontSize = 13.5.sp, fontWeight = FontWeight.Black)
                if (!open && summary != null) {
                    Text(summary, color = if (gold) AppGoldInk2 else AppMuted, fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Icon(if (open) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, if (open) "بستن" else "باز کردن", tint = if (gold) AppGoldInk2 else AppMuted)
        }
        AnimatedVisibility(open) { Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)) { content() } }
    }
}

// ── فهرستِ کوتاه ────────────────────────────────────────────────────────────────
/** فقط [visible] ردیفِ اول؛ بقیه پشتِ «بقیه (n)». */
@Composable
internal fun <T> TopList(items: List<T>, visible: Int = 5, row: @Composable (T) -> Unit) {
    var all by remember(items.size) { mutableStateOf(false) }
    (if (all) items else items.take(visible)).forEach { row(it) }
    if (items.size > visible) {
        Box(Modifier.fillMaxWidth().heightIn(min = 44.dp).clickable { all = !all }, contentAlignment = Alignment.Center) {
            Text(if (all) "کمتر" else "بقیه (${toFa(items.size - visible)})", color = AppPrimaryInk, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

// ── میله ────────────────────────────────────────────────────────────────────────
@Composable
internal fun AdminBarRow(label: String, fraction: Float, trailing: String, sub: String? = null, color: Color = AppPrimary, gold: Boolean = false) {
    Column(Modifier.fillMaxWidth().padding(top = 9.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = if (gold) AppGoldInk else AppText, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Text(trailing, color = if (gold) AppGoldInk2 else AppMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(start = 8.dp))
        }
        Box(Modifier.fillMaxWidth().padding(top = 4.dp).height(7.dp).clip(RoundedCornerShape(99.dp)).background(AppSurface2)) {
            Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(99.dp)).background(if (gold) AppGoldInk else color))
        }
        if (sub != null) Text(sub, color = if (gold) AppGoldInk2 else AppLabel, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

/** نوارِ ۱۰۰٪ چندتکه. سه قلمِ اول جدا، بقیه «سایر». [sort] = false برای ترتیبِ ثابت (سرعتِ باز شدن). */
internal data class SplitPart(val label: String, val value: Int)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SplitBar(title: String?, parts: List<SplitPart>, top: Int = 3, sort: Boolean = true, gold: Boolean = false) {
    val clean = parts.filter { it.value > 0 }
    if (clean.isEmpty()) return
    val ordered = if (sort) clean.sortedByDescending { it.value } else clean
    val rest = ordered.drop(top).sumOf { it.value }
    val shown = ordered.take(top) + if (rest > 0) listOf(SplitPart("سایر", rest)) else emptyList()
    val total = shown.sumOf { it.value }.coerceAtLeast(1)
    val palette = if (gold) listOf(AppGoldInk, AppGoldInk2, AppAssetBorder) else listOf(AppPrimary, AppInfo, AppPurple)
    val others = AppMarkOff
    fun colorOf(i: Int, p: SplitPart) = if (p.label == "سایر" && rest > 0 && i == shown.lastIndex) others else palette.getOrElse(i) { others }
    Column(Modifier.fillMaxWidth().padding(top = 10.dp)) {
        if (title != null) Text(title, color = if (gold) AppGoldInk else AppText, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
        Row(Modifier.fillMaxWidth().padding(top = 6.dp).height(10.dp).clip(RoundedCornerShape(99.dp)), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            shown.forEachIndexed { i, p -> Box(Modifier.weight(p.value.toFloat()).fillMaxHeight().background(colorOf(i, p))) }
        }
        FlowRow(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            shown.forEachIndexed { i, p ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(colorOf(i, p)))
                    Text("${p.label} ${toFa(adminPct(p.value, total))}٪", color = if (gold) AppGoldInk2 else AppMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 5.dp))
                }
            }
        }
    }
}

/** قیفِ کاهشی - ریزش **بینِ** هر دو مرحله با قرمز. */
@Composable
internal fun FunnelChart(steps: List<Pair<String, Int>>, unit: String = "نفر", gold: Boolean = false) {
    val base = steps.firstOrNull()?.second?.coerceAtLeast(1) ?: return
    val bar = if (gold) AppGoldInk else AppPrimary
    Column(Modifier.fillMaxWidth()) {
        steps.forEachIndexed { i, (label, n) ->
            if (i > 0) {
                val prev = steps[i - 1].second
                val lost = adminPct(prev - n, prev)
                if (prev > 0 && lost > 0) Text("↓ ${toFa(lost)}٪ ادامه ندادند", color = AppDangerInk, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 6.dp, start = 6.dp))
            }
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(label, color = if (gold) AppGoldInk else AppText, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${adminNum(n)} $unit · ${toFa(adminPct(n, base))}٪", color = if (gold) AppGoldInk2 else AppMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(start = 8.dp))
            }
            Box(Modifier.padding(top = 3.dp).fillMaxWidth((n.toFloat() / base).coerceIn(0.02f, 1f)).height(12.dp).clip(RoundedCornerShape(6.dp)).background(bar))
        }
    }
}

// ── نمودار ──────────────────────────────────────────────────────────────────────
/**
 * خط + ناحیه برای عددِ روزانه، با ستونِ اختیاریِ دوم. راست‌به‌چپ: قدیمی‌ترین راست، امروز چپ (نقطه).
 * ⚠️ رنگ‌ها `@Composable`اند، پس بیرونِ `Canvas` خوانده می‌شوند.
 */
@Composable
internal fun AdminLineChart(values: List<Int>, bars: List<Int> = emptyList(), height: Dp = 110.dp, gold: Boolean = false, maxValue: Int? = null, modifier: Modifier = Modifier.fillMaxWidth()) {
    if (values.isEmpty()) return
    val line = if (gold) AppGoldInk else AppPrimary
    val barC = AppInfo
    val grid = AppChartGrid
    val max = maxValue ?: (values + bars).maxOrNull()?.coerceAtLeast(1) ?: 1
    Canvas(modifier.height(height).padding(top = 6.dp)) {
        val n = values.size
        val step = if (n > 1) size.width / (n - 1) else 0f
        fun x(i: Int) = size.width - i * step
        fun y(v: Int) = size.height - 4f - (size.height - 8f) * v / max
        drawLine(grid, Offset(0f, size.height - 1f), Offset(size.width, size.height - 1f), 2f)
        val bw = (size.width / n.coerceAtLeast(1)) * 0.45f
        bars.forEachIndexed { i, b ->
            if (b > 0 && i < n) {
                val h = (size.height - 8f) * b / max
                drawRoundRect(barC, Offset((x(i) - bw / 2).coerceIn(0f, size.width - bw), size.height - h), Size(bw, h), CornerRadius(3f, 3f))
            }
        }
        val path = Path().apply { values.forEachIndexed { i, v -> if (i == 0) moveTo(x(i), y(v)) else lineTo(x(i), y(v)) } }
        val area = Path().apply { addPath(path); lineTo(x(n - 1), size.height); lineTo(x(0), size.height); close() }
        drawPath(area, line.copy(alpha = 0.14f))
        drawPath(path, line, style = Stroke(width = 2.5.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round))
        drawCircle(line, 4.dp.toPx(), Offset(x(n - 1), y(values.last())))
    }
}

/** ستون‌های هم‌عرض. در RTL اولین مقدار راست می‌نشیند (شنبه، ساعتِ ۰، قدیمی‌ترین روز). */
@Composable
internal fun AdminColumnChart(values: List<Int>, labels: List<String> = emptyList(), height: Dp = 80.dp, color: Color = AppPrimary, highlight: Int? = null, maxValue: Int? = null) {
    if (values.isEmpty()) return
    val max = maxValue ?: values.maxOrNull()?.coerceAtLeast(1) ?: 1
    val track = AppSurface2
    val dim = AppMarkOff
    Row(Modifier.fillMaxWidth().height(height).padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
        values.forEachIndexed { i, v ->
            Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(track), contentAlignment = Alignment.BottomCenter) {
                if (v > 0) Box(Modifier.fillMaxWidth().fillMaxHeight(v.toFloat() / max).background(if (highlight == null || highlight == i) color else dim))
            }
        }
    }
    if (labels.isNotEmpty()) {
        Row(Modifier.fillMaxWidth().padding(top = 3.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            labels.forEach { Text(it, color = AppLabel, fontSize = 11.sp, textAlign = TextAlign.Center, maxLines = 1, modifier = Modifier.weight(1f)) }
        }
    }
}

@Composable
internal fun AdminLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, color = AppMuted, fontSize = 11.sp)
    }
}
