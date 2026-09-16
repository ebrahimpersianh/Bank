package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppPrimary

/**
 * ═══════════ قابِ آواتار - بندِ ۲ی `72a` ═══════════
 *
 * آواتار **تنها جای شخصیِ برنامه** است و تا امروز هیچ قلمی نداشت. قاب در هدرِ خانه دیده
 * می‌شود، یعنی خرید همان‌جا نتیجه می‌دهد که کاربر خریدش را انجام داده - برخلافِ تمی که
 * اثرش پخش است.
 *
 * ⚠️ **قاب جای آواتار را نمی‌گیرد، دورش می‌نشیند.** آواتار کوچک می‌شود تا قاب بیرونش جا
 * شود؛ اگر قاب روی آواتار بیفتد، شانه‌ها بریده دیده می‌شوند.
 *
 * هیچ هگزی این‌جا نیست: هر پنج قاب از توکن‌های موجود (`AppPrimary`/`AppAccent`/`AppLine`)
 * می‌آیند، پس با تمِ خریداری‌شده‌ی کاربر هم‌قدم عوض می‌شوند.
 */
enum class AvatarFrameStyle(val id: String, val label: String, val blurb: String) {
    RING("ring", "حلقه‌ی ساده", "یک خطِ هم‌رنگِ تم دورِ آواتار"),
    DOUBLE("double", "حلقه‌ی دوجداره", "دو خط با فاصله‌ی نازک"),
    DOTTED("dotted", "حلقه‌ی نقطه‌چین", "خطِ بریده‌بریده، آرام‌تر"),
    NOTCHED("notched", "حلقه‌ی چهارتکه", "چهار کمان با چهار شکاف"),

    /** قلمِ نشان‌قفلِ این نوع (`72c`): با هیچ مقدار سکه‌ای خریدنی نیست. */
    LAUREL("laurel", "حلقه‌ی طلاییِ منظم", "با نشانِ «زیرِ بودجه» باز می‌شود"),
    ;

    companion object {
        /** `frame:double` به سبک. `null` یعنی بی‌قاب - همان حالتی که تا امروز بود. */
        fun fromItemId(itemId: String?): AvatarFrameStyle? =
            entries.firstOrNull { itemId == "frame:${it.id}" }
    }
}

/**
 * آواتار با قابِ اختیاری. [size] **قطرِ بیرونیِ قاب** است، پس جایگزینیِ `AvatarView` با این
 * چیدمانِ اطراف را جابه‌جا نمی‌کند.
 */
@Composable
fun FramedAvatar(
    avatar: Avatar,
    size: Dp,
    frame: AvatarFrameStyle?,
    modifier: Modifier = Modifier,
    photoContent: (@Composable (String) -> Unit)? = null,
) {
    if (frame == null) {
        AvatarView(avatar, size = size, modifier = modifier, photoContent = photoContent)
        return
    }
    // ⚠️ توکن‌های رنگ `@Composable`ان و داخلِ `DrawScope` صدا زده نمی‌شوند - قاعده‌ی مستندِ
    // پروژه: قبل از `Canvas` در یک `val` محلی خوانده شوند.
    val primary = AppPrimary
    val accent = AppAccent
    val line = AppLine
    val inset = size * 0.14f
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        AvatarView(avatar, size = size - inset, photoContent = photoContent)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.minDimension
            fun ring(color: Color, strokeDp: Float, padRatio: Float, effect: PathEffect? = null) {
                val stroke = w * strokeDp
                val pad = w * padRatio + stroke / 2f
                drawArc(
                    color = color,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(pad, pad),
                    size = Size(w - pad * 2f, w - pad * 2f),
                    style = Stroke(width = stroke, pathEffect = effect),
                )
            }
            when (frame) {
                AvatarFrameStyle.RING -> ring(primary, 0.055f, 0f)
                AvatarFrameStyle.DOUBLE -> {
                    ring(primary, 0.045f, 0f)
                    ring(line, 0.030f, 0.085f)
                }
                AvatarFrameStyle.DOTTED -> ring(
                    primary,
                    0.055f,
                    0f,
                    PathEffect.dashPathEffect(floatArrayOf(w * 0.09f, w * 0.07f)),
                )
                AvatarFrameStyle.NOTCHED -> {
                    val stroke = w * 0.06f
                    val pad = stroke / 2f
                    repeat(4) { i ->
                        drawArc(
                            color = primary,
                            startAngle = 90f * i + 8f,
                            sweepAngle = 74f,
                            useCenter = false,
                            topLeft = Offset(pad, pad),
                            size = Size(w - pad * 2f, w - pad * 2f),
                            style = Stroke(width = stroke),
                        )
                    }
                }
                // طلایی فقط نشانِ پرمیوم/دستاورد است - قاعده‌ی ثبت‌شده‌ی پروژه، و همین
                // است که این قاب را از چهار تای دیگر جدا می‌کند.
                AvatarFrameStyle.LAUREL -> {
                    ring(accent, 0.075f, 0f)
                    ring(accent, 0.025f, 0.115f)
                }
            }
        }
    }
}

/** پیش‌نمایشِ ۳۸px ویترین - آواتارِ خاکستریِ خنثی داخلِ همان قاب (`72b`). */
@Composable
fun AvatarFramePreview(frame: AvatarFrameStyle?, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(38.dp).padding(1.dp), contentAlignment = Alignment.Center) {
        FramedAvatar(Avatar(), size = 36.dp, frame = frame)
    }
}
