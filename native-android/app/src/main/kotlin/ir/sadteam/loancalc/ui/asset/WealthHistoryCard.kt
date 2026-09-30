package ir.sadteam.loancalc.ui.asset

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.WealthSnapshotEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.SegmentedToggle
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppChartGrid
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

private val RANGES = listOf("۱ ماه" to 30, "۳ ماه" to 90, "۶ ماه" to 180, "۱ سال" to 366)

/**
 * 📈 **روندِ کلِ دارایی در طولِ زمان** (۸ مهر، خواسته‌ی کاربر). از همان عکسِ روزانه‌ی
 * `wealth_snapshots` (حداکثر یک سال) - خطِ سبز = کل، خطِ آبی = فقط نقد؛ فاصله‌ی دو خط = دارایی‌ها.
 */
@Composable
internal fun WealthHistoryCard(snapshots: List<WealthSnapshotEntity>, privacyMode: Boolean) {
    // با دو-سه نقطه فقط یک خطِ صاف/اریب دیده می‌شد (گزارشِ کاربر ۸ مهر)؛ از ۵ روز به بعد معنی دارد.
    if (snapshots.size < 5) return
    var range by rememberSaveable { mutableIntStateOf(0) }
    val points = remember(snapshots, range) { snapshots.sortedBy { it.dateKey }.takeLast(RANGES[range].second) }
    val first = points.first().totalRial
    val last = points.last().totalRial
    val change = last - first
    val pct = if (kotlin.math.abs(first) > 0) (change * 100 / kotlin.math.abs(first)).toInt() else null
    val up = change >= 0

    AppCard {
        Text("روندِ کلِ دارایی", color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Black)
        Text(
            "از ${dateLabel(points.first().dateKey)} تا ${dateLabel(points.last().dateKey)} · ${toFa(points.size)} روز ثبت‌شده",
            color = AppMuted, fontSize = 11.5.sp,
        )
        Spacer(Modifier.height(10.dp))
        SegmentedToggle(options = RANGES.map { it.first }, selectedIndex = range, onSelect = { range = it })
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(maskIfPrivate(privacyMode, last.rialToFaCompact()), color = AppText, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text(" تومان", color = AppMuted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 3.dp))
            Spacer(Modifier.weight(1f))
            if (!privacyMode) {
                Text(
                    (if (up) "▲ " else "▼ ") + kotlin.math.abs(change).rialToFaCompact() + (pct?.let { " (${toFa(kotlin.math.abs(it))}٪)" } ?: ""),
                    color = if (up) AppPrimary else AppDangerInk, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold,
                )
            }
        }
        // خطِ دومِ «فقط نقد» برداشته شد - کاربر نفهمید چیست؛ یک خط = ارزشِ کلِ دارایی.
        WealthLines(points.map { it.totalRial }, emptyList())
        Text("ارزشِ کلِ حساب‌ها و دارایی‌ها در هر روز", color = AppMuted, fontSize = 11.5.sp)
    }
}

@Composable
private fun Legend(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text(label, color = AppMuted, fontSize = 11.5.sp)
    }
}

@Composable
private fun WealthLines(total: List<Double>, cash: List<Double>) {
    val main = AppPrimary
    val second = AppInfo
    val grid = AppChartGrid
    val all = total + cash
    val max = all.maxOrNull() ?: 0.0
    val min = all.minOrNull() ?: 0.0
    val span = (max - min).takeIf { it > 0 } ?: 1.0
    Canvas(Modifier.fillMaxWidth().height(130.dp).padding(vertical = 10.dp)) {
        repeat(3) { i ->
            val y = size.height * i / 2f
            drawLine(grid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }
        fun path(v: List<Double>): Path = Path().apply {
            v.forEachIndexed { i, x ->
                // راست‌به‌چپ: قدیمی‌ترین سمتِ راست، امروز سمتِ چپ.
                val px = size.width - size.width * i / (v.size - 1).coerceAtLeast(1)
                val py = size.height - ((x - min) / span * size.height).toFloat()
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
        }
        val t = path(total)
        val area = path(total).apply {
            lineTo(0f, size.height)
            lineTo(size.width, size.height)
            close()
        }
        drawPath(area, Brush.verticalGradient(listOf(main.copy(alpha = 0.22f), main.copy(alpha = 0f))))
        if (cash.size >= 2) drawPath(path(cash), second, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
        drawPath(t, main, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
    }
}

private val MONTHS = listOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")

private fun dateLabel(key: String): String {
    val p = key.split('-').mapNotNull { it.toIntOrNull() }
    if (p.size != 3) return key
    return "${toFa(p[2])} ${MONTHS.getOrElse(p[1] - 1) { "" }}"
}
