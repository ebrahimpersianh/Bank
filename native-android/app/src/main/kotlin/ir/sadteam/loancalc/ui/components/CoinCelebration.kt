package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** کل مدتِ جشن به ثانیه - بعدش [CoinCelebration.onFinished] صدا زده می‌شه که overlay برداشته بشه. */
private const val CELEBRATION_DURATION_S = 2.8f

private data class Coin(
    val x0: Float, // نقطه‌ی شروعِ افقی، نسبتِ ۰..۱ از عرض
    val delay: Float, // تاخیرِ شروعِ سقوط، نسبتِ ۰..۱ از کل مدت
    val speed: Float, // سرعتِ سقوطِ اولیه، «ارتفاعِ صفحه بر ثانیه»
    val sway: Float, // دامنه‌ی تابِ افقی، نسبتِ عرض
    val swayFreq: Float,
    val radiusDp: Float,
    val spinSpeed: Float, // سرعتِ چرخشِ سه‌بعدی‌نما (باریک/پهن شدنِ بیضی)
    val tint: Int, // ایندکسِ شیدِ طلایی
)

/**
 * بارشِ سکه‌های طلایی برای لحظه‌ی تسویه‌ی کاملِ یه وام (آخرین قسط پرداخت شد) - خواسته‌ی
 * «انیمیشن‌های منحصربه‌فرد». یه سیستمِ ذره‌ی خیلی ساده رو یه Canvas واحده: هر سکه یه بیضیه که
 * عرضش با |cos| زمان تغییر می‌کنه (توهمِ چرخشِ سه‌بعدیِ سکه) و با یه تابِ سینوسی می‌افته پایین.
 * بدونِ Lottie/تصویر - همه‌چیز برداری و سبک، لمس رو هم مصرف نمی‌کنه (Canvas بدونِ pointerInput).
 *
 * تریگر/ویبره دستِ صفحه‌ی صدازننده‌ست (رجوع کن به LoanDetailScreen) - این فقط می‌کِشه و تهِ مدت
 * [onFinished] رو صدا می‌زنه.
 */
@Composable
fun CoinCelebration(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit,
) {
    val coins = remember {
        val rnd = Random(System.currentTimeMillis())
        List(28) {
            Coin(
                x0 = rnd.nextFloat(),
                delay = rnd.nextFloat() * 0.30f,
                speed = 0.50f + rnd.nextFloat() * 0.45f,
                sway = 0.015f + rnd.nextFloat() * 0.045f,
                swayFreq = 2f + rnd.nextFloat() * 3f,
                radiusDp = 6f + rnd.nextFloat() * 7f,
                spinSpeed = 4f + rnd.nextFloat() * 6f,
                tint = rnd.nextInt(3),
            )
        }
    }
    var elapsed by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var start = 0L
        while (true) {
            val now = withFrameNanos { it }
            if (start == 0L) start = now
            elapsed = (now - start) / 1_000_000_000f
            if (elapsed >= CELEBRATION_DURATION_S) break
        }
        onFinished()
    }

    // سه شیدِ طلایی نزدیک به AppAccent تیره (#FFB020) - عمداً ثابت و مستقل از تم، چون سکه طلاست.
    val goldShades = listOf(Color(0xFFFFD54F), Color(0xFFFFB020), Color(0xFFD4AF37))
    val rimColor = Color(0xFF8A6508)

    Canvas(modifier = modifier) {
        // نیم‌ثانیه‌ی آخر همه‌چیز باهم محو می‌شه که تموم‌شدنش ناگهانی نباشه.
        val fadeOut = ((CELEBRATION_DURATION_S - elapsed) / 0.5f).coerceIn(0f, 1f)
        coins.forEach { c ->
            val t = elapsed - c.delay * CELEBRATION_DURATION_S
            if (t <= 0f) return@forEach
            // سقوط با یه شتابِ ملایم؛ شروع کمی بالای لبه‌ی صفحه.
            val y = (-0.10f + c.speed * t + 0.16f * t * t) * size.height
            if (y > size.height + 60f) return@forEach
            val x = (c.x0 + c.sway * sin(t * c.swayFreq)) * size.width
            val r = c.radiusDp.dp.toPx()
            // چرخشِ سه‌بعدی‌نما: عرضِ بیضی بین ۲۵٪ تا ۱۰۰٪ شعاع نوسان می‌کنه.
            val w = r * (0.25f + 0.75f * abs(cos(t * c.spinSpeed)))
            drawOval(
                color = goldShades[c.tint].copy(alpha = fadeOut),
                topLeft = Offset(x - w, y - r),
                size = Size(w * 2, r * 2),
            )
            drawOval(
                color = rimColor.copy(alpha = 0.55f * fadeOut),
                topLeft = Offset(x - w, y - r),
                size = Size(w * 2, r * 2),
                style = Stroke(width = 1.5.dp.toPx()),
            )
        }
    }
}
