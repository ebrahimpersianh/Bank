package ir.sadteam.loancalc.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * ═══════════ نمودارِ هفت‌روزه در هفته‌ی خالی - فریمِ `48b` ═══════════
 *
 * سه چیز کارت را از «خرابی» جدا می‌کند. هر سه لازم‌اند؛ با دو تا، کاربر هنوز فکر
 * می‌کند بار نشده.
 *
 * ۱. **خطِ پایه در تمامِ عرض.** بی آن، ناحیه‌ی نمودار خالیِ محض است.
 * ۲. **میله‌ی ۵px برای هر روز، حتی صفر.** صفرِ دیده‌شدنی با نبودن فرق دارد.
 * ۳. **حاشیه روی میله‌ی امروز.** جای «الان» را نشان می‌دهد، پس هفت میله‌ی هم‌شکل
 *    بی‌معنا نیستند.
 *
 * و یک خطِ متن: «این هفته هنوز خرجی ثبت نکردی» - جمله‌ی خبری، نه تشویق. کاربری که
 * تازه نصب کرده تقصیری ندارد.
 *
 * [values] هفت مقدارِ خرجِ روز به ریال. [todayIndex] از ۰ تا ۶.
 * روی زمینه‌ی سبزِ کارتِ هیرو می‌نشیند، پس رنگ‌ها سفیدِ نیم‌شفاف‌اند نه توکنِ تم.
 */
@Composable
fun WeekBars(
    values: List<Long>,
    todayIndex: Int,
    modifier: Modifier = Modifier,
) {
    val maxValue = values.maxOrNull() ?: 0L
    Box(modifier = modifier.fillMaxWidth().height(52.dp)) {
        // ۱ · خطِ پایه
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(2.dp)
                .background(Color.White.copy(alpha = 0.28f), RoundedCornerShape(99.dp)),
        )
        Row(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            values.forEachIndexed { i, v ->
                // ۲ · کفِ ۵dp. نسبت روی **باقیِ** ارتفاع حساب می‌شود، نه کلِ آن، وگرنه
                // بلندترین میله ۵dp کوتاه‌تر از سقف می‌ماند.
                val h = if (maxValue <= 0L) 5.dp
                else 5.dp + (47.dp * (v.toFloat() / maxValue))
                val isToday = i == todayIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(h)
                        .background(
                            Color.White.copy(alpha = if (isToday) 0.40f else 0.22f),
                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                        )
                        // ۳ · حاشیه‌ی امروز
                        .then(
                            if (isToday) Modifier.border(
                                1.5.dp,
                                Color.White.copy(alpha = 0.5f),
                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                            ) else Modifier
                        ),
                )
            }
        }
    }
}

/** متنِ زیرِ نمودار. خبری، نه تشویقی. */
fun weekChartCaption(values: List<Long>): String =
    if (values.all { it == 0L }) "این هفته هنوز خرجی ثبت نکردی" else ""
