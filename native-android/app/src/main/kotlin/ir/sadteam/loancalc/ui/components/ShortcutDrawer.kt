package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * **کشوی میان‌بُر** - بخشِ ۳۱ فایلِ طراحی (کارت‌های `31a`..`31c`).
 *
 * «یک کشیدن از پایینِ صفحه، هشت میان‌بُر، ده ثانیه فرصت. نگه‌داشتنِ انگشت روی هر آیتم حالتِ
 * جابه‌جایی را روشن می‌کند.»
 *
 * ⚠️ قاعده‌ی صریحِ `31c` برای پیاده‌سازی: **`ModalBottomSheet` نه** - یه `Box` با `offsetY` و
 * `draggable`، «تا کشو بتواند زیرِ نوارِ پایین بنشیند و نوار همیشه دیده شود».
 *
 * ⚠️ **انحراف از طرح که باید بدونی**: طرح ترتیب رو با `PATCH /me/shortcuts` رو سرور ذخیره
 * می‌کنه. سرورِ فعلیِ ما همچین مسیری نداره، پس ترتیب فعلاً **محلی** (DataStore) ذخیره می‌شه.
 * وقتی مسیرِ سرور اضافه شد، فقط [onOrderChanged] باید به سینک وصل بشه.
 */
data class Shortcut(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val route: String,
    /**
     * میان‌بری که از کشو **حذف نمی‌شود**. فقط «ثبتِ خرج»: مقصد نیست، شیت باز می‌کند، و
     * تنها راهِ ثبتِ دستی است - کاربری که برش دارد راهِ ثبت را گم می‌کند.
     */
    val locked: Boolean = false,
)

private const val AUTO_CLOSE_MS = 10_000L
private const val COLUMNS = 5
private const val MIN_SHORTCUTS = 10

/** هر ردیفِ کشو پنج‌تایی است؛ تعدادِ میان‌برهای انتخابی محدود نیست. */

@Composable
fun ShortcutDrawer(
    /** هشت میان‌برِ انتخاب‌شده، به ترتیبِ ذخیره‌شده. */
    shortcuts: List<Shortcut>,
    visible: Boolean,
    onDismiss: () -> Unit,
    onOpenRoute: (String) -> Unit,
    onOrderChanged: (List<String>) -> Unit,
    /**
     * **همه‌ی** مقصدهای ممکن (مخزنِ ۱۴تایی) - ورودیِ حالتِ ویرایشِ فریمِ `53a`.
     *
     * قبلاً کشو فقط جابه‌جایی داشت و **انتخاب نداشت**: هشت میان‌بر در کد ثابت بودند و
     * شش مقصدِ دیگر هیچ راهی به کشو نداشتند.
     */
    allShortcuts: List<Shortcut> = shortcuts,
    /** ذخیره‌ی انتخاب. **کلیدِ جدا از ترتیب** - نوار پنج جا دارد و کشو هشت. */
    onSelectionChanged: (List<String>) -> Unit = {},
    /**
     * شناسه‌ی مقصدهایی که همین حالا در نوارِ پایین‌اند - فقط برای بجِ خبری.
     *
     * مقصدی که در نوار است از کشو **حذف نمی‌شود**: کشو از هر صفحه‌ای باز می‌شود ولی نوار
     * در صفحه‌های عمیق پنهان است، پس حذفِ خودکار مقصد را در نیمی از اپ بی‌راه می‌گذارد.
     */
    inBottomBarIds: Set<String> = emptySet(),
    modifier: Modifier = Modifier,
) {
    if (!visible) return

    var reorderMode by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf(false) }
    var order by remember(shortcuts) { mutableStateOf(shortcuts) }
    var selectedIds by remember(shortcuts) { mutableStateOf(shortcuts.map { it.id }) }
    // هر لمسی داخلِ کشو تایمر رو از صفر شروع می‌کنه (قاعده‌ی `31c`).
    var timerKey by remember { mutableIntStateOf(0) }
    var remaining by remember { mutableFloatStateOf(1f) }

    // ⏱ **ده ثانیه**: خطِ ۳ پیکسلی بالای کشو از راست به چپ خالی می‌شه. تو حالتِ جابه‌جایی
    // «تایمر کاملاً متوقف است» - قاعده‌ی صریحِ طرح.
    LaunchedEffect(timerKey, reorderMode, editMode) {
        // حالتِ ویرایش هم مثلِ جابه‌جایی تایمر را **کاملاً** متوقف می‌کند: انتخابِ هشت از
        // چهارده بیش از ده ثانیه طول می‌کشد و بسته‌شدنِ وسطِ کار انتخاب را دور می‌ریخت.
        if (reorderMode || editMode) return@LaunchedEffect
        val startedAt = System.currentTimeMillis()
        while (true) {
            val elapsed = System.currentTimeMillis() - startedAt
            remaining = 1f - (elapsed.toFloat() / AUTO_CLOSE_MS)
            if (remaining <= 0f) {
                onDismiss()
                return@LaunchedEffect
            }
            kotlinx.coroutines.delay(16)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // پرده‌ی پشت - تپ روش فوراً می‌بنده.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.34f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { if (!reorderMode && !editMode) onDismiss() },
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = AppRadius.sheet, topEnd = AppRadius.sheet))
                .background(AppSurface)
                .pointerInput(Unit) {
                    // هر لمسی داخلِ کشو تایمر رو ریست می‌کنه.
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent()
                            timerKey++
                        }
                    }
                }
                .padding(bottom = 12.dp),
        ) {
            // خطِ شمارش
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(AppLineRow),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(remaining.coerceIn(0f, 1f))
                        .height(3.dp)
                        .background(if (reorderMode || editMode) AppMuted else AppPrimary),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    when {
                        editMode -> "کدام‌ها در کشو باشند"
                        reorderMode -> "جای‌شان را عوض کن"
                        else -> "دسترسیِ سریع"
                    },
                    color = AppText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f),
                )
                if (editMode) {
                    // شمارنده‌ی «۶ از ۸» - بی آن کاربر نمی‌فهمد چرا نهمی انتخاب نمی‌شود.
                    Text(
                        "${selectedIds.size} انتخاب شده",
                        color = AppLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                } else if (!reorderMode && allShortcuts.size > shortcuts.size) {
                    // «ویرایش» فقط وقتی می‌آید که مقصدی بیرونِ کشو مانده باشد.
                    Text(
                        "ویرایش",
                        color = AppMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .clickable { editMode = true; timerKey++ }
                            .padding(end = 14.dp),
                    )
                }
                Text(
                    if (reorderMode || editMode) "تمام" else "بستن",
                    color = AppPrimaryInk,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.clickable {
                        when {
                            editMode -> {
                                editMode = false
                                onSelectionChanged(selectedIds)
                                timerKey++
                            }
                            reorderMode -> {
                                reorderMode = false
                                onOrderChanged(order.map { it.id })
                                timerKey++
                            }
                            else -> onDismiss()
                        }
                    },
                )
            }
            if (editMode) {
                SelectionGrid(
                    all = allShortcuts,
                    selectedIds = selectedIds,
                    inBottomBarIds = inBottomBarIds,
                    onToggle = { id ->
                        selectedIds = when {
                            id in selectedIds -> selectedIds - id
                            else -> selectedIds + id
                        }
                    },
                )
            } else {
                ShortcutGrid(
                    shortcuts = order,
                    reorderMode = reorderMode,
                    onEnterReorder = { reorderMode = true },
                    onMove = { from, to ->
                        order = order.toMutableList().apply { add(to, removeAt(from)) }
                    },
                    onOpen = { route ->
                        // ⚠️ ترتیبِ جابه‌جاشده فقط با دکمه‌ی «تمام» ذخیره می‌شد. کاربری که
                        // بعدِ جابه‌جایی روی یک میان‌بر می‌زد (یا پرده را لمس می‌کرد) کارش را
                        // از دست می‌داد. حالا هر خروجی ذخیره می‌کند.
                        if (order.map { it.id } != shortcuts.map { it.id }) {
                            onOrderChanged(order.map { it.id })
                        }
                        onDismiss()
                        onOpenRoute(route)
                    },
                )
                // 🚨 **خانه‌ی دائمیِ راهنمای جابه‌جایی** (بندِ ۲ی بخشِ ۸۱). زیرِ نوارِ پایین فقط
                // سه بار دیده می‌شود و بعد می‌رود؛ این‌جا سقفِ زمانی نمی‌خواهد، چون همان‌جایی
                // است که میان‌برها دیده می‌شوند یعنی همان‌جا که جابه‌جایی معنی دارد.
                // با دستگیره یکی نشد: دستگیره «بکش» می‌گوید و راهنما «نگه‌دار» — دو حرکتِ
                // متفاوت روی یک عنصر.
                if (!reorderMode) {
                    Text(
                        "روی هر میان‌بر نگه‌دار تا جابه‌جایش کنی",
                        color = AppLabel,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    )
                }
            }
        }
    }
}

/**
 * شبکه‌ی میان‌برها.
 *
 * ⚠️ عمداً `LazyVerticalGrid` **نیست**: قاعده‌ی ماندگارِ پروژه می‌گه شبکه‌ی تنبل نمی‌تونه
 * wrap-content باشه؛ برای هشت آیتمِ ثابت هم `chunked(n).forEach { Row { … } }` هم ساده‌تره هم
 * محاسبه‌ی خانه‌ی مقصدِ کشیدن روش قطعیه.
 */
@Composable
private fun ShortcutGrid(
    shortcuts: List<Shortcut>,
    reorderMode: Boolean,
    onEnterReorder: () -> Unit,
    onMove: (Int, Int) -> Unit,
    onOpen: (String) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    // اندازه‌ی یه خانه؛ برای تبدیلِ جابه‌جاییِ انگشت به ایندکسِ مقصد.
    var cellWidthPx by remember { mutableFloatStateOf(1f) }
    var cellHeightPx by remember { mutableFloatStateOf(1f) }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
        shortcuts.chunked(COLUMNS).forEachIndexed { rowIndex, rowItems ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowItems.forEachIndexed { colIndex, item ->
                    val index = rowIndex * COLUMNS + colIndex
                    val dragging = index == draggingIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer {
                                if (dragging) {
                                    translationX = dragOffsetX
                                    translationY = dragOffsetY
                                }
                            }
                            .alpha(if (dragging) 0.35f else 1f)
                            .pointerInput(reorderMode, shortcuts) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onEnterReorder()
                                        draggingIndex = index
                                        dragOffsetX = 0f
                                        dragOffsetY = 0f
                                        cellWidthPx = size.width.toFloat()
                                        cellHeightPx = size.height.toFloat()
                                    },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        dragOffsetX += amount.x
                                        dragOffsetY += amount.y
                                    },
                                    onDragEnd = {
                                        // خانه‌ی مقصد از مرکزِ انگشت: چند ستون و چند ردیف
                                        // جابه‌جا شده. RTL: حرکت به **چپ** یعنی ایندکسِ بزرگ‌تر.
                                        val cols = -(dragOffsetX / cellWidthPx).roundToInt()
                                        val rows = (dragOffsetY / cellHeightPx).roundToInt()
                                        val target = (index + cols + rows * COLUMNS)
                                            .coerceIn(0, shortcuts.lastIndex)
                                        if (target != index) onMove(index, target)
                                        draggingIndex = -1
                                        dragOffsetX = 0f
                                        dragOffsetY = 0f
                                    },
                                    onDragCancel = {
                                        draggingIndex = -1
                                        dragOffsetX = 0f
                                        dragOffsetY = 0f
                                    },
                                )
                            },
                    ) {
                        ShortcutTile(
                            shortcut = item,
                            wiggling = reorderMode && !dragging,
                            seed = index,
                            onClick = { if (!reorderMode) onOpen(item.route) },
                        )
                    }
                }
                // ردیفِ ناقص با Spacerِ وزن‌دار پر می‌شه تا خانه‌ها هم‌عرض بمونن.
                repeat(COLUMNS - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/** یه خانه‌ی میان‌بر - هدفِ لمس ۴۸dp طبقِ بندِ دسترس‌پذیریِ `31c`. */
@Composable
private fun ShortcutTile(
    shortcut: Shortcut,
    wiggling: Boolean,
    seed: Int,
    onClick: () -> Unit,
) {
    // لرزش: ±۱٫۶ درجه، ۱۶۰ms، با **فازِ تصادفی** برای هر آیتم تا هم‌زمان نلرزن.
    val transition = rememberInfiniteTransition(label = "wiggle")
    val angle by transition.animateFloat(
        initialValue = -1.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            tween(160, delayMillis = (seed * 37) % 160, easing = LinearEasing),
            RepeatMode.Reverse,
        ),
        label = "wiggleAngle",
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            // ⚠️ `.padding()` **قبل از** `.clickable()` بود و هدفِ لمسی را کوچک می‌کرد
            // (قاعده‌ی ۵). خانه‌ی ۵۲ + برچسب ~۷۲dp است؛ پدینگِ بیرونی آن را به ~۵۶ می‌رساند
            // ولی نوارِ بالا و پایینِ آن هم باید لمس‌پذیر باشد.
            .rotate(if (wiggling) angle else 0f)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
    ) {
        val isPrimaryAction = shortcut.locked
        Box(
            modifier = Modifier
                .size(if (isPrimaryAction) 56.dp else 50.dp)
                .clip(RoundedCornerShape(if (isPrimaryAction) 18.dp else AppRadius.row))
                .background(if (isPrimaryAction) AppPrimary else AppChipBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                shortcut.icon,
                contentDescription = shortcut.label,
                tint = if (isPrimaryAction) Color.White else AppPrimaryInk,
                modifier = Modifier.size(if (isPrimaryAction) 24.dp else 21.dp),
            )
        }
        Text(
            shortcut.label,
            color = if (isPrimaryAction) AppPrimaryInk else AppText,
            fontSize = if (isPrimaryAction) 10.5.sp else 10.sp,
            fontWeight = if (isPrimaryAction) FontWeight.Black else FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

/**
 * دستگیره‌ی پایینِ صفحه - نوارِ ۲۶ پیکسلی که با کشیدن به بالا (یا تپِ ساده) کشو رو باز می‌کنه.
 *
 * آستانه‌ی بازشدن **۴۸px جابه‌جایی** طبقِ `31c`.
 */
@Composable
fun ShortcutDrawerHandle(onOpen: () -> Unit, modifier: Modifier = Modifier) {
    var dragged by remember { mutableFloatStateOf(0f) }
    val thresholdPx = with(LocalDensity.current) { 48.dp.toPx() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(18.dp)
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta -> dragged += delta },
                onDragStopped = {
                    if (abs(dragged) > thresholdPx && dragged < 0) onOpen()
                    dragged = 0f
                },
            )
            .clickable(onClick = onOpen),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    // ظریف‌تر (خواسته‌ی کاربر، ۲ مهر): «بمونه ولی به برنامه بیاد».
                    .width(52.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(AppPrimary.copy(alpha = 0.85f)),
            )

        }
    }
}

/**
 * شبکه‌ی حالتِ ویرایش - **همه‌ی** چهارده مقصد، انتخاب‌شده‌ها پررنگ.
 *
 * چرا لیستِ تخت نیست: انتخاب یک کارِ بصری است («کدام آیکون‌ها کنارِ هم باشند») و ردیفِ
 * متنی همان آیکون‌ها را از چیدمانی که قرار است بسازند جدا می‌کند.
 */
@Composable
private fun SelectionGrid(
    all: List<Shortcut>,
    selectedIds: List<String>,
    inBottomBarIds: Set<String>,
    onToggle: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
        all.chunked(COLUMNS).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { item ->
                    val selected = item.id in selectedIds
                    // کشو همیشه دست‌کم دو ردیفِ پنج‌تایی دارد؛ سقفِ بالایی ندارد.
                    val atMinimum = selected && selectedIds.size <= MIN_SHORTCUTS
                    Box(modifier = Modifier.weight(1f)) {
                        SelectionTile(
                            shortcut = item,
                            selected = selected,
                            // فقط «ثبت خرج» قفل است و از کشو حذف نمی‌شود.
                            disabled = item.locked || atMinimum,
                            inBottomBar = item.id in inBottomBarIds,
                            onClick = { if (!item.locked && !atMinimum) onToggle(item.id) },
                        )
                    }
                }
                repeat(COLUMNS - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        Text(
            "مقصدی که در نوارِ پایین است از کشو برداشته نمی‌شود - نوار در صفحه‌های عمیق پنهان می‌شود.",
            color = AppLabel,
            fontSize = 9.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun SelectionTile(
    shortcut: Shortcut,
    selected: Boolean,
    disabled: Boolean,
    inBottomBar: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .alpha(if (disabled && !selected) 0.38f else 1f),
    ) {
        Box(modifier = Modifier.size(52.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(AppRadius.row))
                    .background(if (selected) AppPrimaryPill else AppChipBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    shortcut.icon,
                    contentDescription = shortcut.label,
                    tint = if (selected) AppPrimaryInk else AppMuted,
                    modifier = Modifier.size(22.dp),
                )
            }
            // نشانِ گوشه: تیک برای انتخاب‌شده، قفل برای «ثبتِ خرج».
            if (selected || shortcut.locked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (shortcut.locked) AppMuted else AppPrimary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (shortcut.locked) Icons.Filled.Lock else Icons.Filled.Check,
                        contentDescription = null,
                        tint = AppSurface,
                        modifier = Modifier.size(11.dp),
                    )
                }
            }
        }
        Text(
            shortcut.label,
            color = if (selected) AppText else AppMuted,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
        if (inBottomBar) {
            // بجِ **خبری**، نه محدودیت. مقصد همچنان می‌تواند در کشو بماند.
            Text(
                "در نوارِ پایین",
                color = AppLabel,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
