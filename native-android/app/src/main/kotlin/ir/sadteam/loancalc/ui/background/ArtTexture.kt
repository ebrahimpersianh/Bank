package ir.sadteam.loancalc.ui.background

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.LocalAppColors

/*
 * بافتِ تمِ هنری — بندِ ۶ی بخشِ ۸۱.
 *
 * طراح اول گفت «تصویر تولید نمی‌کنم» و قیمتِ تم از ۸۰۰ به ۴۰۰ آمد. دورِ ۱۴ خودش پس گرفت:
 * بافت **لازم نیست عکس باشد**؛ همان راهِ `78a` (`DrawScope`) کافی است. پس قیمت به ۸۰۰
 * برگشت، و صفر فایلِ تصویری به برنامه اضافه شد.
 *
 * سه قیدِ طرح:
 *   ۱. رنگ از پالتِ فعال، نه هگزِ خودی — مثلِ پس‌زمینه‌ی زنده.
 *   ۲. بافت **حرکت نمی‌کند**. برای همین با پس‌زمینه‌ی زنده جمع می‌شود و قیدِ «فقط در
 *      Scaffoldِ ریشه» را هم ندارد.
 *   ۳. آلفا ۰٫۱۰–۰٫۱۴ روی زمینه‌ی روشن، و در شب **سفیدِ ۰٫۰۶**: بافتِ تیره روی زمینه‌ی
 *      تیره چرک به‌نظر می‌رسد نه بافت.
 *
 * زیرِ کارت‌ها هیچ‌وقت نمی‌آید، فقط زمینه‌ی صفحه.
 */

enum class ArtTexture(val id: String, val nameFa: String) {
    /** دو دسته خطِ اریب با زاویه و گامِ نامساوی. */
    BRUSH("brush", "ضربه‌ی قلم‌مو"),

    /**
     * سه شبکه‌ی نقطه با گامِ اول‌نسبت‌به‌هم (۱۳/۱۷/۲۳) — پس الگو تا صدها پیکسل تکرارِ
     * آشکار نمی‌گیرد.
     */
    PARCHMENT("parchment", "کاغذِ پوستی"),

    /** شبکه‌ی راست‌گوشه‌ی چگال. تنها جایی که زاویه‌ی قائمه اشکال ندارد، چون خودش پارچه است. */
    LINEN("linen", "تارِ کتان"),
}

/**
 * بافتِ فعال. مثلِ [LiveBackgroundState] و `SymbolTheme` نگه‌دارنده‌ی سراسری است نه
 * `CompositionLocal`: مصرف‌کننده‌اش یک `Canvas` در ریشه است و امضای هیچ صفحه‌ای نباید عوض شود.
 */
object ArtTextureState {
    var active: ArtTexture? by mutableStateOf(null)
}

/** بافت را روی زمینه‌ی صفحه می‌کشد. با `null` هیچ‌چیز نمی‌کشد. */
@Composable
fun ArtTextureLayer(texture: ArtTexture?, modifier: Modifier = Modifier) {
    if (texture == null) return
    // ⚠️ توکن‌های رنگ `@Composable`اند و داخلِ `Canvas` (که `DrawScope` است) صدا زده
    // نمی‌شوند — قاعده‌ی ماندگارِ پروژه. پس این‌جا در یک `val` خوانده می‌شوند.
    val dark = LocalAppColors.current.isDark
    val ink = if (dark) Color.White else AppPrimary
    val alpha = if (dark) 0.06f else 0.12f
    Canvas(modifier = modifier.fillMaxSize()) {
        when (texture) {
            ArtTexture.BRUSH -> drawBrush(ink, alpha)
            ArtTexture.PARCHMENT -> drawParchment(ink, alpha)
            ArtTexture.LINEN -> drawLinen(ink, alpha)
        }
    }
}

private fun DrawScope.drawBrush(ink: Color, alpha: Float) {
    val w = size.width
    val h = size.height
    val stroke = 1.6.dp.toPx()
    // دو دسته با گام و زاویه‌ی نامساوی؛ مساوی بودنشان الگو را به راه‌راهِ ماشینی می‌بَرد.
    var x = -h
    while (x < w + h) {
        drawLine(ink.copy(alpha = alpha), Offset(x, 0f), Offset(x + h * 0.55f, h), stroke)
        x += 37.dp.toPx()
    }
    var x2 = -h
    while (x2 < w + h) {
        drawLine(ink.copy(alpha = alpha * 0.6f), Offset(x2, 0f), Offset(x2 + h * 0.34f, h), stroke * 0.7f)
        x2 += 23.dp.toPx()
    }
}

private fun DrawScope.drawParchment(ink: Color, alpha: Float) {
    val r = 1.1.dp.toPx()
    // گام‌های اول‌نسبت‌به‌هم: کوچک‌ترین مضربِ مشترکشان ۱۳×۱۷×۲۳ است، پس چشم تکرار نمی‌بیند.
    listOf(13f to 1f, 17f to 0.75f, 23f to 0.5f).forEachIndexed { index, (stepDp, weight) ->
        val step = stepDp.dp.toPx()
        val offset = index * step / 3f
        var y = offset
        while (y < size.height) {
            var x = offset
            while (x < size.width) {
                drawCircle(ink.copy(alpha = alpha * weight), r, Offset(x, y))
                x += step
            }
            y += step
        }
    }
}

private fun DrawScope.drawLinen(ink: Color, alpha: Float) {
    val step = 7.dp.toPx()
    val stroke = 0.9.dp.toPx()
    var x = 0f
    while (x < size.width) {
        drawLine(ink.copy(alpha = alpha * 0.8f), Offset(x, 0f), Offset(x, size.height), stroke)
        x += step
    }
    var y = 0f
    while (y < size.height) {
        drawLine(ink.copy(alpha = alpha * 0.55f), Offset(0f, y), Offset(size.width, y), stroke)
        y += step
    }
    // یک قابِ بسیار کم‌رنگ تا لبه‌ها با زمینه یکی نشوند.
    drawRect(ink.copy(alpha = alpha * 0.25f), style = Stroke(stroke))
}
