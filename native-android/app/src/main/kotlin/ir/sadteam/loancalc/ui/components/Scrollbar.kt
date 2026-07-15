package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
            color = color,
            topLeft = Offset(size.width - w, thumbY),
            size = Size(w, thumbHeight),
            cornerRadius = CornerRadius(w / 2, w / 2),
        )
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
            color = color,
            topLeft = Offset(size.width - w, thumbY),
            size = Size(w, thumbHeight),
            cornerRadius = CornerRadius(w / 2, w / 2),
        )
    }
}
