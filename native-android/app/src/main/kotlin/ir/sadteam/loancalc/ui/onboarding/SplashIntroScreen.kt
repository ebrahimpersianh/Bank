package ir.sadteam.loancalc.ui.onboarding

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import ir.sadteam.loancalc.R
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay

/**
 * ═══════════ اسپلش ═══════════
 *
 * **خودِ تصویرِ طراح، نه بازسازیِ دستی‌اش.** چند دورِ بازسازیِ کدیِ اسپلش شکست خورد تا این
 * قاعده نوشته شد: وقتی طرحِ مرجع یک رندرِ سه‌بعدی/نوری است، فایلش مستقیم گذاشته می‌شود.
 * نامِ «جیبک»، زیرنویس و حلقه‌ی بارگذاری همه بخشی از خودِ تصویرند، نه `Text`.
 *
 * ⚠️ یک دور این صفحه با `Text`ِ واقعی و نشانِ بریده‌شده از آیکون ساخته شد و کاربر رد کرد
 * («خوشم نیومد») - خالی و بی‌جان بود. تصویرِ کامل برگشت.
 *
 * تنها چیزی که کد اضافه می‌کند **برق‌زدنِ نقطه‌های پس‌زمینه** است (خواسته‌ی کاربر): چند ذره‌ی
 * ریز که آرام روشن و خاموش می‌شوند و کمی بالا می‌روند، پس صفحه‌ی اول زنده دیده می‌شود نه یک
 * عکسِ ثابت.
 *
 * ⚠️ **مکانِ ذره‌ها ثابت است، نه تصادفیِ هر فریم**: `Random` با دانه‌ی ثابت در `remember`
 * صدا زده می‌شود. بی این، ذره‌ها هر فریم جای تازه‌ای می‌پریدند و نتیجه برفک بود نه درخشش.
 *
 * ⚠️ زمانِ ماندن ~۲٫۳ ثانیه است - **تاییدِ صریحِ کاربر** («همین تایم خوبه»). کوتاهش نکن.
 */
private const val SPLASH_MS = 2300L
private const val SPARKLE_COUNT = 26

private data class Sparkle(val x: Float, val y: Float, val radius: Float, val phase: Float, val drift: Float)

@Composable
fun SplashIntroScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(SPLASH_MS)
        onDone()
    }

    val sparkles = remember {
        val rnd = Random(20260922)
        List(SPARKLE_COUNT) {
            Sparkle(
                x = rnd.nextFloat(),
                // بالای ۰٫۶۵ نگه داشته می‌شوند: پایینِ تصویر متن و نوار دارد و ذره رویشان
                // شلوغی است، نه درخشش.
                y = rnd.nextFloat() * 0.65f,
                radius = 1.2f + rnd.nextFloat() * 2.4f,
                phase = rnd.nextFloat(),
                drift = 6f + rnd.nextFloat() * 14f,
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "splashSparkle")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sparklePhase",
    )

    // زمینه هم‌رنگِ گوشه‌ی خودِ تصویر است، پس روی نسبت‌های مختلفِ صفحه لبه‌ی روشن دیده نمی‌شود.
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF013D1F))) {
        Image(
            painter = painterResource(R.drawable.jibak_splash_art),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            sparkles.forEach { s ->
                // موجِ سینوسی: هر ذره فازِ خودش را دارد، پس همه با هم چشمک نمی‌زنند.
                val wave = sin(((t + s.phase) % 1f) * 2f * PI.toFloat())
                val alpha = (0.18f + 0.42f * (wave * 0.5f + 0.5f)).coerceIn(0f, 1f)
                val y = s.y * size.height - ((t + s.phase) % 1f) * s.drift
                val center = Offset(s.x * size.width, y)
                drawCircle(
                    color = Color(0xFFFFD98A).copy(alpha = alpha * 0.55f),
                    radius = s.radius * 2.6f,
                    center = center,
                )
                drawCircle(
                    color = Color(0xFFFFF3D0).copy(alpha = alpha),
                    radius = s.radius,
                    center = center,
                )
            }
        }
    }
}
