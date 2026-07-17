package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.delay

/**
 * جایگزینِ `Toast.makeText` تو کل اپ - کاربر گزارش کرد Android 12+ (و بعضی رام‌ها مثل MIUI) خودکار
 * یه آیکونِ اپ رو کنارِ متنِ هر Toast می‌چسبونن («لوگوی آندروید خام» که جا جا تو اپ دیده)؛ این یه
 * بنرِ داخلِ خودِ اپه که همچین آرایشی نداره - پورتِ همون الگویی که قبلاً برای هینتِ خروج تو
 * MainActivity و پیامِ export تقویم تو LoanDetailScreen استفاده شده بود، این‌بار دوباره‌قابل‌استفاده
 * برای هر صفحه‌ای.
 *
 * استفاده: `val banner = rememberInAppBanner()` تو ریشه‌ی صفحه، صفحه رو تو یه `Box` بذار، و
 * `InAppBannerHost(banner)` رو به‌عنوان آخرین فرزندِ همون Box اضافه کن. هر جا قبلاً Toast بود،
 * به‌جاش `banner.show("متن")` صدا بزن.
 */
internal data class BannerMessage(val text: String, val isSuccess: Boolean)

@Composable
fun rememberInAppBanner(): InAppBannerState {
    var message by remember { mutableStateOf<BannerMessage?>(null) }
    return remember {
        InAppBannerState(
            get = { message },
            set = { text, isSuccess -> message = if (text == null) null else BannerMessage(text, isSuccess) },
        )
    }
}

class InAppBannerState internal constructor(
    private val get: () -> BannerMessage?,
    private val set: (String?, Boolean) -> Unit,
) {
    internal val current: BannerMessage? get() = get()
    val message: String? get() = get()?.text

    /** [isSuccess] یه تیکِ سبزِ متحرک کنارِ متن اضافه می‌کنه - برای پیام‌های موفقیت (ذخیره/بازیابی/...)،
     * نه خطاها. */
    fun show(text: String, isSuccess: Boolean = false) = set(text, isSuccess)
    fun clear() = set(null, false)
}

@Composable
fun InAppBannerHost(state: InAppBannerState, modifier: Modifier = Modifier) {
    val current = state.current
    LaunchedEffect(current) {
        if (current != null) {
            delay(2600)
            state.clear()
        }
    }
    AnimatedVisibility(
        visible = current != null,
        enter = fadeIn(tween(180)) + slideInVertically(tween(180)) { it / 2 },
        exit = fadeOut(tween(180)) + slideOutVertically(tween(180)) { it / 2 },
        modifier = modifier.padding(bottom = 24.dp),
    ) {
        Box(
            modifier = Modifier
                .background(AppText.copy(alpha = 0.92f), RoundedCornerShape(24.dp))
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (current?.isSuccess == true) {
                    AnimatedCheckmark(modifier = Modifier.padding(end = 8.dp))
                }
                Text(current?.text ?: "", color = AppSurface, fontSize = 13.sp)
            }
        }
    }
}

/** تیکِ سبزِ موفقیت که با یه کشیدنِ متحرکِ خط (نه فقط fade-in یهویی) ظاهر می‌شه - پورتِ حسِ «تاییدِ
 * بصری» اپ رقیب، با همون رنگ‌بندیِ خودِ اپ (AppPrimary) بجای سبزِ رقیب. */
@Composable
private fun AnimatedCheckmark(modifier: Modifier = Modifier) {
    val progress = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(420, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)))
    }
    Box(
        modifier = modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(AppPrimary.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            val w = size.width
            val h = size.height
            val start = Offset(w * 0.18f, h * 0.52f)
            val mid = Offset(w * 0.42f, h * 0.76f)
            val end = Offset(w * 0.85f, h * 0.24f)
            val firstLegLength = (mid - start).getDistance()
            val totalLength = firstLegLength + (end - mid).getDistance()
            val drawnLength = totalLength * progress.value
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(start.x, start.y)
                if (drawnLength <= firstLegLength) {
                    val t = if (firstLegLength == 0f) 0f else drawnLength / firstLegLength
                    lineTo(start.x + (mid.x - start.x) * t, start.y + (mid.y - start.y) * t)
                } else {
                    lineTo(mid.x, mid.y)
                    val remaining = drawnLength - firstLegLength
                    val secondLegLength = (end - mid).getDistance()
                    val t = if (secondLegLength == 0f) 0f else (remaining / secondLegLength).coerceAtMost(1f)
                    lineTo(mid.x + (end.x - mid.x) * t, mid.y + (end.y - mid.y) * t)
                }
            }
            drawPath(
                path = path,
                color = Color(0xFF1FA97A),
                style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}
