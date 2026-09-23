package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text

/**
 * حلقه‌ی درصدِ پیشرفت با دو خطِ متن در مرکزش - طرحِ مرجعِ کاربر (۳۱ شهریور).
 *
 * 🚨 **در `components` است نه کنارِ یکی از دو صفحه**: هم کارتِ خلاصه‌ی فهرستِ وام‌ها و هم
 * صفحه‌ی جزئیاتِ وام همین را می‌خواهند، و دو کپیِ هم‌شکل یعنی روزی که یکی عوض می‌شود و
 * دیگری جا می‌مانَد.
 *
 * ⚠️ حلقه **پادساعت‌گرد** پر می‌شود چون برنامه راست‌به‌چپ است؛ ساعت‌گرد در این چیدمان
 * خلافِ جهتِ خواندن حس می‌شود.
 */
@Composable
fun PaidRing(
    fraction: Float,
    ringColor: Color,
    trackColor: Color,
    centerTop: String,
    centerBottom: String,
    centerTopColor: Color,
    centerBottomColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 74.dp,
    stroke: Dp = 9.dp,
    centerTopSize: Int = 15,
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = stroke.toPx()
            val inset = strokePx / 2
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(this.size.width - strokePx, this.size.height - strokePx),
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
            if (fraction > 0f) {
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = -360f * fraction.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(this.size.width - strokePx, this.size.height - strokePx),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }
        }
        // 🚨 فاصله‌ی داخلیِ فونت (وزیرمتن بالا و پایینِ هر خط جای خالیِ نامساوی دارد) حذف و
        // خط‌ها روی مرکز بریده می‌شوند - وگرنه متن در دایره بالا می‌نشست (گزارشِ کاربر).
        val tight = TextStyle(
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                centerTop,
                color = centerTopColor,
                fontSize = centerTopSize.sp,
                lineHeight = centerTopSize.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                style = tight,
            )
            Text(
                centerBottom,
                color = centerBottomColor,
                fontSize = 7.5.sp,
                lineHeight = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                style = tight,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
