package ir.sadteam.loancalc.ui.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.network.AdminHealthDetail
import ir.sadteam.loancalc.data.network.AdminMoneyResponse
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppGoldInk
import ir.sadteam.loancalc.ui.theme.AppGoldInk2
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText

/*
 * 📊 نمودارهای لمسیِ ادمین (۱۶ مهر، خواسته‌ی کاربر): مثلِ نمودارِ خانه - **امروز سمتِ راست**،
 * زمان از چپ به راست، لمسِ هر ستون = روز و عددش بالای نمودار، زیرش روز/ماه.
 */

/** `2026-10-08` → «۱۶/۷» (روز/ماهِ شمسی) برای محورِ زیرِ نمودار. */
internal fun adminDayShort(iso: String?): String = runCatching {
    val (y, m, d) = iso.orEmpty().take(10).split('-').map { it.toInt() }
    val p = ir.sadteam.loancalc.core.JalaliCalendar.fromGregorian(y, m, d)
    "${toFa(p.d)}/${toFa(p.m)}"
}.getOrDefault("")

/**
 * ستونیِ لمسی. [labels] برچسبِ کاملِ هر ستون (مثلاً «۱۶ مهر») و [axis] برچسبِ کوتاهِ محور.
 * پیش‌فرض آخرین ستون (امروز) انتخاب است.
 */
@Composable
internal fun AdminTapBars(
    values: List<Int>,
    labels: List<String>,
    axis: List<String> = labels,
    color: Color = AppPrimary,
    height: Dp = 100.dp,
    valueText: (Int) -> String = { adminNum(it) },
    title: String? = null,
) {
    if (values.isEmpty()) return
    var sel by remember(values.size) { mutableStateOf(values.lastIndex) }
    val max = (values.maxOrNull() ?: 0).coerceAtLeast(1)
    val track = AppSurface2
    val dim = color.copy(alpha = 0.45f)
    val i = sel.coerceIn(0, values.lastIndex)
    Row(
        Modifier.fillMaxWidth().padding(top = 6.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.10f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(labels.getOrElse(i) { "" }, color = AppText, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
        if (title != null) Text("$title: ", color = AppMuted, fontSize = 11.5.sp)
        Text(valueText(values[i]), color = color, fontSize = 13.sp, fontWeight = FontWeight.Black)
    }
    Ltr {
        Canvas(
            Modifier.fillMaxWidth().height(height).padding(top = 8.dp).pointerInput(values.size) {
                detectTapGestures { off -> sel = (off.x / size.width * values.size).toInt().coerceIn(0, values.lastIndex) }
            },
        ) {
            val n = values.size
            val slot = size.width / n
            val bw = slot * 0.66f
            values.forEachIndexed { k, v ->
                val x = k * slot + (slot - bw) / 2
                drawRoundRect(track, Offset(x, 0f), Size(bw, size.height), CornerRadius(4f, 4f))
                val h = size.height * v / max
                if (h > 0f) drawRoundRect(if (k == i) color else dim, Offset(x, size.height - h), Size(bw, h), CornerRadius(4f, 4f))
            }
        }
    }
    AdminAxis(axis)
}

/** برچسب‌های محور: حداکثر ۶ تا، آخری (امروز) همیشه هست. چپ‌به‌راست، هم‌جهتِ نمودار. */
@Composable
internal fun AdminAxis(axis: List<String>) {
    if (axis.isEmpty()) return
    val n = axis.size
    val every = ((n + 5) / 6).coerceAtLeast(1)
    Ltr {
        Row(Modifier.fillMaxWidth().padding(top = 3.dp)) {
            axis.forEachIndexed { k, s ->
                val show = k == n - 1 || ((n - 1 - k) % every == 0 && n - 1 - k >= every / 2)
                Text(
                    if (show) s else "", color = AppLabel, fontSize = 9.sp, maxLines = 1, softWrap = false,
                    textAlign = TextAlign.Center, modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** 🧾 «چه کسی خرید» + فروشِ امروز/۷/۳۰ روز به‌تفکیکِ استور. */
@Composable
internal fun AdminSalesCard(d: AdminMoneyResponse) {
    if (d.periods.isEmpty() && d.recent.isEmpty()) return
    AppCard {
        Text("فروش", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black)
        if (d.periods.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                d.periods.forEach { p ->
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(AppGoldInk.copy(alpha = 0.08f)).padding(8.dp),
                    ) {
                        Text(p.label, color = AppGoldInk2, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${toFa(p.count)} خرید", color = AppGoldInk, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Text("${adminNum(p.net)} ت خالص", color = AppGoldInk2, fontSize = 10.sp, maxLines = 1)
                        Text("بازار ${toFa(p.cafebazaar)} · مایکت ${toFa(p.myket)}", color = AppMuted, fontSize = 9.5.sp, maxLines = 1)
                    }
                }
            }
        }
        if (d.recent.isNotEmpty()) {
            AdminSubTitle("خریدهای اخیر")
            d.recent.take(30).forEach { r ->
                Row(
                    Modifier.fillMaxWidth().padding(top = 6.dp).clip(RoundedCornerShape(12.dp)).background(AppSurface2)
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Uid ${toFa(r.code)}", color = AppText, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            Ltr { Text("  ${r.phone}", color = AppMuted, fontSize = 11.sp) }
                        }
                        Text(
                            "${STORE_LABELS[r.store] ?: r.store} · ${PLAN_LABELS[r.product] ?: r.product} · ${adminDay(r.at)} ${adminHourOf(r.at)}",
                            color = AppMuted, fontSize = 10.5.sp, maxLines = 1,
                        )
                    }
                    if (r.price > 0) Text(adminNum(r.price), color = AppGoldInk, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

/** `2026-10-08 07:30:00` (UTC) → «۱۱:۰۰» به وقتِ ایران. */
internal fun adminHourOf(utc: String?): String = runCatching {
    val t = java.time.LocalDateTime.parse(utc.orEmpty().take(19).replace(' ', 'T')).plusMinutes(210)
    "${toFa(t.hour)}:${toFa(t.minute).padStart(2, '۰')}"
}.getOrDefault("")

/** صفحه‌ی جزئیاتِ یک کاشیِ سلامت (کرش / همگام‌سازی / چندحسابه / ماهِ رایگان). */
@Composable
internal fun AdminHealthDetailPage(kind: String, title: String, onBack: () -> Unit, vm: AdminProViewModel = hiltViewModel()) {
    var data by remember { mutableStateOf<AdminHealthDetail?>(null) }
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(kind) {
        val r = vm.repo.adminHealth(kind)
        data = r
        failed = r == null
    }
    ir.sadteam.loancalc.ui.settings.FullScreenDialog(onDismissRequest = onBack) {
        Column(Modifier.fillMaxSize().background(AppBg)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText) }
                Text(title, color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
            val d = data
            when {
                failed -> Text("نرسید؛ اینترنت را چک کن (یا سرور هنوز به‌روز نشده).", color = AppMuted, modifier = Modifier.padding(20.dp))
                d == null -> Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AppPrimary) }
                d.rows.isEmpty() -> Text("موردی نیست.", color = AppMuted, modifier = Modifier.padding(20.dp))
                else -> LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp, 4.dp, 14.dp, 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(d.rows) { r ->
                        AppCard {
                            Text(r.title, color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp)
                            if (r.sub.isNotBlank()) Text(r.sub, color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                            if (r.at.isNotBlank()) Text("${adminDay(r.at)} ${adminHourOf(r.at)}", color = AppLabel, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }
        }
    }
}
