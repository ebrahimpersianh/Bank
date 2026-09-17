package ir.sadteam.loancalc.ui.background

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/*
 * پس‌زمینه‌ی زنده — فریم‌های `78a`..`78c`.
 *
 * سه قیدِ طرح، و هر سه در همین فایل تحمیل می‌شوند:
 *
 *   ۱. صفر فایلِ تصویری. هر چهار طرح فقط DrawScope است. چهار webpِ تمام‌صفحه برای
 *      هر چگالی چند مگابایت به apk اضافه می‌کرد.
 *   ۲. رنگ از پالتِ فعال می‌آید، نه هگزِ خودی. پس ۱۰ تم × ۴ طرح = ۴۰ ترکیب با
 *      چهار تابع. هیچ Color(0xFF..) در این فایل نیست — عمدی است.
 *   ۳. آلفا زیرِ ۰٫۰۹. باید حس شود، نه دیده. پررنگ‌تر همان شلوغیِ بصریِ
 *      پس‌زمینه‌ی متحرکِ قبلی می‌شد که کلِ بازطراحی برای فرار از آن برداشتش.
 *
 * ⚠️ این را در Scaffoldِ **ریشه** بکشید، نه در هر صفحه. با کشیدن در هر صفحه،
 *    انیمیشن در هر تعویضِ تب از صفر شروع می‌شود و پرشِ محسوس می‌دهد.
 */

enum class LiveBackground(val id: String, val nameFa: String, val darkOnly: Boolean) {
    AURORA("aurora", "شفق", darkOnly = false),
    NIGHT_SWIRL("night_swirl", "چرخشِ شب", darkOnly = true),
    SOFT_MESH("soft_mesh", "تورِ نرم", darkOnly = false),
    TIDE("tide", "جزر و مد", darkOnly = false);

    companion object {
        /** قیمتِ **بسته‌ای**. با چهار قیمتِ جدا کاربر سه طرح را هیچ‌وقت نمی‌دید. */
        const val BUNDLE_PRICE = 250
        fun byId(id: String?) = entries.firstOrNull { it.id == id }
    }
}

/**
 * ⚠️ دوره‌ها عمداً **نامساوی و اول‌نسبت‌به‌هم** هستند (۱۳/۱۴/۱۷/۱۹/۲۴/۷۲ ثانیه).
 * با دوره‌های مساوی، دو لایه هر چند ثانیه هم‌فاز می‌شوند و چشم تکرار را می‌گیرد.
 */
@Composable
private fun phase(periodMs: Int): Float {
    val t = rememberInfiniteTransition(label = "lb")
    return t.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(periodMs, easing = LinearEasing)),
        label = "p"
    ).value
}

private fun breathe(p: Float) = sin(p * 2f * Math.PI.toFloat())

@Composable
fun LiveBackgroundLayer(
    background: LiveBackground?,
    primary: Color,
    primaryLight: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    if (background == null) return
    // چرخشِ شب روی زمینه‌ی سفید معنی ندارد؛ در تمِ روشن اصلاً کشیده نمی‌شود.
    if (background.darkOnly && !isDark) return

    val pA = phase(14_000); val pB = phase(19_000)
    val pC = phase(72_000); val pD = phase(48_000)
    val pE = phase(24_000)
    val pF = phase(13_000); val pG = phase(17_000)

    Box(modifier.fillMaxSize()) {
        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
            when (background) {
                LiveBackground.AURORA -> drawAurora(primary, primaryLight, pA, pB)
                LiveBackground.NIGHT_SWIRL -> drawNightSwirl(primary, primaryLight, pC, pD)
                LiveBackground.SOFT_MESH -> drawSoftMesh(primary, pE)
                LiveBackground.TIDE -> drawTide(primary, primaryLight, pF, pG)
            }
        }
    }
}

/** دو هاله که نفس می‌کشند. هاله‌ی اول primary، دومی primaryLight. */
private fun DrawScope.drawAurora(primary: Color, light: Color, pA: Float, pB: Float) {
    fun halo(c: Color, cx: Float, cy: Float, r: Float, a: Float) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(c.copy(alpha = a), Color.Transparent),
                center = Offset(cx, cy), radius = r
            ),
            radius = r, center = Offset(cx, cy)
        )
    }
    val w = size.width; val h = size.height
    halo(primary, w * (0.32f + 0.07f * breathe(pA)), h * (0.34f + 0.05f * breathe(pA)),
        w * (0.85f + 0.09f * breathe(pA)), 0.085f)
    halo(light, w * (0.70f - 0.06f * breathe(pB)), h * (0.70f - 0.05f * breathe(pB)),
        w * (0.80f + 0.10f * breathe(pB)), 0.070f)
}

/** دو چرخشِ هم‌مرکزِ مخالف‌جهت + چهار نقطه‌ی نورِ ثابت. */
private fun DrawScope.drawNightSwirl(primary: Color, light: Color, pC: Float, pD: Float) {
    val c = Offset(size.width * 0.5f, size.height * 0.42f)
    val r = maxOf(size.width, size.height) * 1.5f
    fun rays(color: Color, count: Int, turn: Float, alpha: Float, width: Float) {
        rotate(degrees = turn * 360f, pivot = c) {
            repeat(count) { i ->
                rotate(degrees = i * (360f / count), pivot = c) {
                    drawRect(
                        color = color.copy(alpha = alpha),
                        topLeft = Offset(c.x, c.y - width / 2f),
                        size = Size(r, width)
                    )
                }
            }
        }
    }
    rays(primary, 30, pC, 0.055f, size.width * 0.012f)
    rays(light, 20, -pD, 0.040f, size.width * 0.008f)
    // نقطه‌های نور ثابت‌اند: با چرخش، به ستاره‌ی دنباله‌دار تبدیل می‌شدند.
    listOf(0.18f to 0.26f, 0.34f to 0.62f, 0.74f to 0.41f, 0.62f to 0.78f)
        .forEachIndexed { i, (x, y) ->
            drawCircle(
                color = Color.White.copy(alpha = 0.055f - i * 0.004f),
                radius = size.width * 0.004f,
                center = Offset(size.width * x, size.height * y)
            )
        }
}

/**
 * دو شبکه‌ی خطِ ۱px که آرام می‌لغزند.
 * ⚠️ زاویه ۲۸° است نه ۹۰°: شبکه‌ی راست‌گوشه با لبه‌ی کارت‌ها هم‌راستا می‌شد و
 * شبیهِ خطای رندر به‌نظر می‌رسید.
 */
private fun DrawScope.drawSoftMesh(primary: Color, pE: Float) {
    val step = step28()
    val off = -pE * step * 2f
    val c = primary.copy(alpha = 0.075f)
    val span = (size.width + size.height) * 1.5f
    translate(off, off) {
        listOf(28f, 118f).forEach { deg ->
            rotate(degrees = deg, pivot = Offset(0f, 0f)) {
                var y = -span
                while (y < span) {
                    drawLine(c, Offset(-span, y), Offset(span, y), strokeWidth = 1f)
                    y += step
                }
            }
        }
    }
}

/** دو کمانِ عریض از پایینِ صفحه. تنها طرحی که جهت دارد، پس لنگرِ پایین می‌شود. */
private fun DrawScope.drawTide(primary: Color, light: Color, pF: Float, pG: Float) {
    fun arc(c: Color, alpha: Float, riseFrac: Float, widthFrac: Float) {
        val w = size.width * widthFrac
        val h = size.height * 0.86f
        drawOval(
            color = c.copy(alpha = alpha),
            topLeft = Offset(-(w - size.width) / 2f, size.height - h * riseFrac),
            size = Size(w, h * 2f)
        )
    }
    arc(primary, 0.070f, 0.52f + 0.06f * breathe(pF), 1.48f)
    arc(light, 0.055f, 0.44f - 0.05f * breathe(pG), 1.60f)
}

/**
 * ⚠️ بندِ ۳ی READMEِ بخشِ ۷۸: نسخه‌ی بسته یک جای‌نگه‌دار بود (`this * 2.75f`) که روی هر
 * چگالیِ غیر از xhdpi فاصله‌ی شبکه را غلط می‌داد. `DrawScope` خودش `Density` است، پس
 * تبدیل واقعی انجام می‌شود.
 */
private fun DrawScope.step28(): Float = 28.dp.toPx()


/**
 * پس‌زمینه‌ی فعال - همان الگوی `SymbolTheme`/`AppFontState`: تنها نقطه‌ی نوشتنش
 * `ThemeViewModel.init` است و مصرف‌کننده‌اش بیرونِ کامپوزیشن ساخته می‌شود، پس
 * `CompositionLocal` جواب نمی‌دهد.
 */
object LiveBackgroundState {
    var active: LiveBackground? by mutableStateOf(null)
}
