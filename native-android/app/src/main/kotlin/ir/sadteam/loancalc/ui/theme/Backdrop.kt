package ir.sadteam.loancalc.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

/**
 * **پس‌زمینه‌ی زنده - قلمِ تازه‌ی فروشگاه (دورِ ۱۳).**
 *
 * چرا این و نه «ده تمِ دیگر»: تشخیصِ خودِ طراح در `72a` این بود که **فروشگاه با تنوعِ
 * نوعِ قلم کامل می‌شود، نه با تعدادِ ردیف**. تمِ پانزدهم ویترین را بلندتر می‌کند بی
 * این‌که چیزی حل کند؛ پس‌زمینه‌ی زنده یک **نوع** است: تنها قلمی که برنامه را
 * *حرکت‌دار* می‌کند، و روی هر تمی می‌نشیند پس با خریدِ قبلیِ کاربر رقابت نمی‌کند.
 *
 * 🚨 **هیچ فایلِ تصویری ندارد.** همه با `Brush`ِ برنامه‌ای کشیده می‌شود، پس نه حجمِ APK
 * بالا می‌رود نه آیکونی از جایی باید بیاید - همان راهی که طراح برای «بافتِ تمِ هنری»
 * پیشنهاد داد و گفت خودش تصویرش را نمی‌تواند بکشد.
 *
 * ⚠️ **آلفا عمداً زیرِ ۰٫۰۹ است.** این لایه زیرِ کارت‌های ماتِ برنامه می‌نشیند؛ هر چیزی
 * پررنگ‌تر از این، همان «شلوغیِ بصری»ای است که بازطراحیِ جیبک `AuroraBackground` را
 * برای فرار از آن برداشت. باید **حس شود، نه دیده شود**.
 *
 * ⚠️ رنگ‌ها از توکنِ تمِ فعال می‌آیند (`AppPrimary`/`AppAccent`)، نه هگزِ ثابت - پس
 * پس‌زمینه با تمِ خریداری‌شده هم‌قدم می‌شود و کاربری که تمِ لاجورد دارد شفقِ سبز
 * نمی‌بیند.
 */
enum class Backdrop(
    val id: String,
    val label: String,
    val blurb: String,
) {
    AURORA("backdrop:aurora", "شفق", "دو هاله‌ی آرام که نفس می‌کشند"),
    STARRY("backdrop:starry", "چرخشِ شب", "چرخش‌های کم‌رنگِ «شبِ پرستاره»"),
    MESH("backdrop:mesh", "تورِ نرم", "شبکه‌ای که آرام می‌لغزد"),
    TIDE("backdrop:tide", "جزر و مد", "موجی که از پایین بالا می‌آید"),
    ;

    companion object {
        fun fromId(id: String?): Backdrop? = entries.firstOrNull { it.id == id }
    }
}

/**
 * پس‌زمینه‌ی فعال. فقط `ThemeViewModel` می‌نویسد - همان الگوی `SymbolTheme`/`AppFontState`:
 * مصرف‌کننده‌اش بیرونِ کامپوزیشن ساخته می‌شود، پس `CompositionLocal` جواب نمی‌دهد.
 */
object BackdropState {
    var active: Backdrop? by mutableStateOf(null)
}

/**
 * لایه‌ی پس‌زمینه. زیرِ کلِ محتوای برنامه کشیده می‌شود و **هیچ تپی نمی‌گیرد**
 * (`Canvas` کلیک‌پذیر نیست)، پس روی ناوبری اثری ندارد.
 */
@Composable
fun LiveBackdrop(backdrop: Backdrop, modifier: Modifier = Modifier) {
    // 🚨 توکنِ رنگ `@Composable` است و داخلِ `Canvas` (که `DrawScope` است) خوانده
    // نمی‌شود - قاعده‌ی مستندِ پروژه: قبلش در یک `val` محلی بخوانش.
    val primary = AppPrimary
    val accent = AppAccent
    val transition = rememberInfiniteTransition(label = "backdrop")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            // ۱۸ ثانیه برای یک دور: کندتر از آن یعنی ثابت به نظر می‌رسد، تندتر یعنی
            // چشم را می‌گیرد و متنِ روی کارت‌ها را رقیب پیدا می‌کند.
            animation = tween(18_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "backdropPhase",
    )
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val angle = phase * 2f * Math.PI.toFloat()
        when (backdrop) {
            Backdrop.AURORA -> {
                val dx = cos(angle) * w * 0.12f
                val dy = sin(angle) * h * 0.06f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primary.copy(alpha = 0.085f), Color.Transparent),
                        center = Offset(w * 0.22f + dx, h * 0.18f + dy),
                        radius = w * 0.75f,
                    ),
                    radius = w * 0.75f,
                    center = Offset(w * 0.22f + dx, h * 0.18f + dy),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.07f), Color.Transparent),
                        center = Offset(w * 0.82f - dx, h * 0.72f - dy),
                        radius = w * 0.65f,
                    ),
                    radius = w * 0.65f,
                    center = Offset(w * 0.82f - dx, h * 0.72f - dy),
                )
            }
            Backdrop.STARRY -> {
                // پنج چرخشِ هم‌مرکزِ جابه‌جا - همان چیزی که طراح برای تمِ هنری خواست:
                // «چند radialGradientِ هم‌مرکزِ جابه‌جا با آلفای پایین».
                repeat(5) { i ->
                    val t = angle + i * 1.15f
                    val cx = w * (0.2f + 0.15f * i) + cos(t) * w * 0.08f
                    val cy = h * (0.15f + 0.17f * i) + sin(t * 1.3f) * h * 0.05f
                    val r = w * (0.30f + 0.05f * (i % 3))
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                (if (i % 2 == 0) primary else accent).copy(alpha = 0.06f),
                                Color.Transparent,
                            ),
                            center = Offset(cx, cy),
                            radius = r,
                        ),
                        radius = r,
                        center = Offset(cx, cy),
                    )
                }
            }
            Backdrop.MESH -> {
                // خطوطِ نازکِ لغزان. ضخامت ۱ و آلفای ۰٫۰۴: از یک قدمی دیده نمی‌شود،
                // ولی سطح را از «سفیدِ خالی» درمی‌آورد.
                val step = w / 7f
                val shift = (phase * step) % step
                for (i in -1..8) {
                    val x = i * step + shift
                    drawLine(
                        color = primary.copy(alpha = 0.045f),
                        start = Offset(x, 0f),
                        end = Offset(x + w * 0.12f, h),
                        strokeWidth = 1f,
                    )
                }
                for (j in 0..10) {
                    val y = j * (h / 10f) - shift
                    drawLine(
                        color = accent.copy(alpha = 0.03f),
                        start = Offset(0f, y),
                        end = Offset(w, y + h * 0.02f),
                        strokeWidth = 1f,
                    )
                }
            }
            Backdrop.TIDE -> {
                val lift = (sin(angle) * 0.5f + 0.5f) * h * 0.08f
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, primary.copy(alpha = 0.09f)),
                        startY = h * 0.45f - lift,
                        endY = h,
                    ),
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(accent.copy(alpha = 0.05f), Color.Transparent),
                        startY = 0f,
                        endY = h * 0.3f + lift,
                    ),
                )
            }
        }
    }
}
