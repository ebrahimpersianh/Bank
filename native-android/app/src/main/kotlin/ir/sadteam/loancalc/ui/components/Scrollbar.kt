package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** دستگیره‌ی اسکرول‌بار به‌جای رنگِ صافِ یک‌دست، یه گرادینت داره: وسطش پررنگ (خودِ [color])، دو سرش
 * یه‌کم روشن‌تر/کم‌رنگ‌تر - دقیقاً خواسته‌ی کاربر «رنگش مرکزش پررنگ‌تر باشه و بغل کمی کم‌رنگ‌تر». */
private fun thumbBrush(color: Color, horizontal: Boolean): Brush {
    val edge = lerp(color, Color.White, 0.3f)
    return if (horizontal) {
        Brush.horizontalGradient(listOf(edge, color, edge))
    } else {
        Brush.verticalGradient(listOf(edge, color, edge))
    }
}

/**
 * اسکرول‌بار نازک سبز برای ستون‌هایی که با `verticalScroll(state)` اسکرول می‌شن - به‌درخواست کاربر
 * «تو هر صفحه‌ای که اسکرول داره، بغلش یه اسکرول‌بار سبز خیلی نازک باشه که بدونیم کجاییم».
 */
fun Modifier.verticalScrollbar(
    state: ScrollState,
    color: Color,
    width: Dp = 3.dp,
): Modifier = drawWithContent {
    drawContent()
    val max = state.maxValue
    if (max > 0) {
        val viewport = size.height
        val contentHeight = viewport + max
        val thumbHeight = (viewport / contentHeight) * viewport
        val thumbY = (state.value.toFloat() / max) * (viewport - thumbHeight)
        val w = width.toPx()
        drawRoundRect(
            brush = thumbBrush(color, horizontal = false),
            topLeft = Offset(size.width - w, thumbY),
            size = Size(w, thumbHeight),
            cornerRadius = CornerRadius(w / 2, w / 2),
        )
    }
}

/**
 * نشانگر اسکرول افقی: یه خط نازک سبز که کجای ردیفِ افقی هستیم و چقدر تا آخرش مونده رو نشون می‌ده.
 * پس‌زمینه‌ی خطْ یه شیارِ کم‌رنگ با دو سرِ محو (fade) هست، و روش یه دستگیره‌ی سبزِ پررنگ - دقیقاً
 * چیزی که کاربر برای ردیف بانک‌ها/خدمات اعتباری خواست («یه خط ظریف سبز که کناراش رنگش پریده»).
 * روی یه `Box` نازک (مثلاً `height(4.dp)`) زیر همون ردیف اعمالش کن، با همون `ScrollState` ردیف.
 */
fun Modifier.horizontalScrollbar(
    state: ScrollState,
    color: Color,
    thickness: Dp = 3.dp,
): Modifier = drawWithContent {
    drawContent()
    val viewport = size.width
    val t = thickness.toPx()
    val top = (size.height - t) / 2f
    // شیارِ کم‌رنگ با دو سرِ محو
    drawRoundRect(
        brush = Brush.horizontalGradient(
            0f to Color.Transparent,
            0.12f to color.copy(alpha = 0.16f),
            0.88f to color.copy(alpha = 0.16f),
            1f to Color.Transparent,
        ),
        topLeft = Offset(0f, top),
        size = Size(viewport, t),
        cornerRadius = CornerRadius(t / 2, t / 2),
    )
    val max = state.maxValue
    if (max > 0) {
        val content = viewport + max
        val thumbW = ((viewport / content) * viewport).coerceAtLeast(24f)
        val thumbX = (state.value.toFloat() / max) * (viewport - thumbW)
        drawRoundRect(
            brush = thumbBrush(color, horizontal = true),
            topLeft = Offset(thumbX, top),
            size = Size(thumbW, t),
            cornerRadius = CornerRadius(t / 2, t / 2),
        )
    }
}

/**
 * نسخه‌ی [horizontalScrollbar] برای `LazyRow` (بر اساس ایندکس آیتم، نه پیکسل) - بعد از تبدیل
 * ردیف‌های بانک/خدمات به LazyRow (برای پرفورمنس) لازم شد؛ همون شیار کم‌رنگ + دستگیره‌ی سبز.
 * [shimmerPhase] اختیاریه (۰..۱، از `rememberInfiniteTransition` تو خودِ صفحه‌ی صدازننده حساب
 * می‌شه) - اگه داده بشه، یه هایلایتِ نوریِ محو مدام رو خودِ دستگیره سر می‌خوره (خواسته‌ی کاربر «اون
 * اسکرول زیر بانک‌ها رو یکم شیک‌تر بکن»)؛ null یعنی بدون شیمر (رفتار قبلی).
 */
fun Modifier.lazyRowScrollbar(
    state: LazyListState,
    color: Color,
    thickness: Dp = 3.dp,
    shimmerPhase: Float? = null,
): Modifier = drawWithContent {
    drawContent()
    val viewport = size.width
    val t = thickness.toPx()
    val top = (size.height - t) / 2f
    drawRoundRect(
        brush = Brush.horizontalGradient(
            0f to Color.Transparent,
            0.12f to color.copy(alpha = 0.16f),
            0.88f to color.copy(alpha = 0.16f),
            1f to Color.Transparent,
        ),
        topLeft = Offset(0f, top),
        size = Size(viewport, t),
        cornerRadius = CornerRadius(t / 2, t / 2),
    )
    val layout = state.layoutInfo
    val total = layout.totalItemsCount
    val visible = layout.visibleItemsInfo
    if (total > 0 && visible.isNotEmpty() && visible.size < total) {
        val thumbW = ((visible.size.toFloat() / total) * viewport).coerceAtLeast(24f)
        val scrollable = (total - visible.size).coerceAtLeast(1)
        val progress = visible.first().index.toFloat() / scrollable
        // تو RTL، آیتم اول سمت راسته - دستگیره از راست به چپ حرکت می‌کنه.
        val thumbX = (viewport - thumbW) * (1f - progress)
        drawRoundRect(
            brush = thumbBrush(color, horizontal = true),
            topLeft = Offset(thumbX, top),
            size = Size(thumbW, t),
            cornerRadius = CornerRadius(t / 2, t / 2),
        )
        if (shimmerPhase != null) {
            val streakW = (thumbW * 0.4f).coerceAtLeast(1f)
            val streakX = thumbX + (thumbW - streakW) * shimmerPhase
            clipRect(left = thumbX, top = top, right = thumbX + thumbW, bottom = top + t) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.65f), Color.Transparent),
                        startX = streakX,
                        endX = streakX + streakW,
                    ),
                    topLeft = Offset(thumbX, top),
                    size = Size(thumbW, t),
                    cornerRadius = CornerRadius(t / 2, t / 2),
                )
            }
        }
    }
}

/**
 * همون اسکرول‌بار نازک سبز، ولی برای `LazyColumn`/`LazyList` (که به‌جای پیکسل، بر اساس ایندکس آیتم
 * کار می‌کنه). تخمینیه (ارتفاع آیتم‌ها یکسان فرض می‌شه) ولی برای نشون‌دادن «کجای لیستیم» کافیه.
 */
fun Modifier.lazyColumnScrollbar(
    state: LazyListState,
    color: Color,
    width: Dp = 3.dp,
): Modifier = drawWithContent {
    drawContent()
    val layout = state.layoutInfo
    val total = layout.totalItemsCount
    val visible = layout.visibleItemsInfo
    if (total > 0 && visible.isNotEmpty() && visible.size < total) {
        val viewport = size.height
        val thumbHeight = ((visible.size.toFloat() / total) * viewport).coerceAtLeast(24f)
        val scrollable = (total - visible.size).coerceAtLeast(1)
        val progress = visible.first().index.toFloat() / scrollable
        val thumbY = progress * (viewport - thumbHeight)
        val w = width.toPx()
        drawRoundRect(
            brush = thumbBrush(color, horizontal = false),
            topLeft = Offset(size.width - w, thumbY),
            size = Size(w, thumbHeight),
            cornerRadius = CornerRadius(w / 2, w / 2),
        )
    }
}
