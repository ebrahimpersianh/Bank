package ir.sadteam.roozegar.ui.effects

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import ir.sadteam.roozegar.core.EffectType
import kotlin.math.sin
import kotlin.random.Random

/**
 * موتور افکت روزهای خاص (data-driven از [ir.sadteam.roozegar.core.Occasions]):
 * بارش شکوفه (نوروز)، برف (زمستون)، ستاره‌ی چشمک‌زن (یلدا/سده) و برگ پاییزی (مهرگان).
 *
 * همه‌چیز یه Canvas ساده‌ست: موقعیت هر ذره تابعِ قطعیِ زمانه (هیچ آبجکتی per-frame ساخته/جهش داده
 * نمی‌شه)، بدون blur و بدون بیت‌مپ - رو گوشی ضعیف هم ۶۰ فریم می‌مونه. تعداد ذره‌ها عمداً کمه (~۶۰).
 */
@Composable
fun ParticleOverlay(type: EffectType, modifier: Modifier = Modifier) {
    if (type == EffectType.NONE) return

    class Particle(
        val x0: Float, val y0: Float, val speed: Float,
        val size: Float, val phase: Float, val drift: Float,
    )

    val particles = remember(type) {
        val rnd = Random(42)
        val count = if (type == EffectType.STARS) 80 else 60
        List(count) {
            Particle(
                x0 = rnd.nextFloat(),
                y0 = rnd.nextFloat(),
                speed = 0.04f + rnd.nextFloat() * 0.08f,
                size = 0.5f + rnd.nextFloat(),
                phase = rnd.nextFloat() * 6.2832f,
                drift = 0.01f + rnd.nextFloat() * 0.03f,
            )
        }
    }

    var frameNanos by remember { mutableLongStateOf(0L) }
    LaunchedEffect(type) {
        while (true) withFrameNanos { frameNanos = it }
    }

    Canvas(modifier.fillMaxSize()) {
        val t = frameNanos / 1_000_000_000f
        val w = size.width
        val h = size.height
        particles.forEach { p ->
            when (type) {
                EffectType.STARS -> {
                    // ستاره‌ها نمی‌بارن - سر جاشون با ریتم خودشون چشمک می‌زنن (شب یلدا)
                    val tw = 0.25f + 0.75f * ((sin(t * (0.8f + p.speed * 8f) + p.phase) + 1f) / 2f)
                    val gold = p.phase > 3.1f
                    drawCircle(
                        color = (if (gold) Color(0xFFF2B04B) else Color.White).copy(alpha = 0.65f * tw),
                        radius = (1.2f + p.size * 1.6f) * density,
                        center = Offset(p.x0 * w, p.y0 * h),
                    )
                }

                else -> {
                    // ذره‌های بارشی: y از رو زمان جلو می‌ره و دور می‌زنه، x یه رانش سینوسی ملایم داره
                    val y = ((p.y0 + t * p.speed) % 1.08f) - 0.04f
                    val x = p.x0 + sin(t * (0.4f + p.drift * 10f) + p.phase) * p.drift
                    val cx = x * w
                    val cy = y * h
                    when (type) {
                        EffectType.SNOW -> drawCircle(
                            color = Color.White.copy(alpha = 0.55f),
                            radius = (1.5f + p.size * 1.8f) * density,
                            center = Offset(cx, cy),
                        )

                        EffectType.BLOSSOM, EffectType.LEAVES -> {
                            val color = if (type == EffectType.BLOSSOM) Color(0xFFF8AFC5) else Color(0xFFE8974F)
                            val r = (2f + p.size * 2.4f) * density
                            // گلبرگ/برگ = یه بیضی چرخان - چرخش هر ذره هم تابع زمانه
                            rotate(degrees = (t * 40f + p.phase * 57f) % 360f, pivot = Offset(cx, cy)) {
                                drawOval(
                                    color = color.copy(alpha = 0.65f),
                                    topLeft = Offset(cx - r, cy - r * 0.55f),
                                    size = androidx.compose.ui.geometry.Size(r * 2f, r * 1.1f),
                                )
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }
}
