package ir.sadteam.loancalc.ui.nav

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppStroke
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.hardShadow

/**
 * **ویرایشگرِ نوارِ پایین** - فریمِ `41b` («خودم می‌چینم»).
 *
 * ⚠️ **دو فاصله‌ی آگاهانه با سندِ طراح، هر دو با تصمیمِ صریحِ کاربر (۱۱ شهریور):**
 *
 * ۱. طراح نوشته «جابه‌جایی **با کشیدن نیست**: یک ضربه به جای خالی، یک ضربه به مقصد» و کشیدن
 *    رو فقط برای مرتب‌کردن نگه داشته. کاربر صریحاً **کشیدن** خواست. پس اینجا **کشیدن راهِ
 *    اصلیه** و ضربه **حذف نشده** بلکه به‌عنوانِ راهِ دوم موند - نگرانیِ خودِ طراح (کشیدنِ
 *    آیکونِ ۴۴dp با یک دست سخته) با نگه‌داشتنِ هر دو مسیر جواب می‌گیره، نه با حذفِ یکی.
 *
 * ۲. این ویرایشگر عمداً **داخلِ محتوای `Scaffold`** رندر می‌شه، نه `FullScreenDialog`، چون
 *    قاعده‌ی مرکزیِ `41b` اینه که «**نوارِ واقعی** پایینِ صفحه می‌مانَد و همان لحظه عوض
 *    می‌شود». دیالوگِ تمام‌صفحه دقیقاً همون نوار رو می‌پوشونه و کلِ ایده رو از بین می‌بره.
 *
 * دو کشیدنِ متفاوت پشتیبانی می‌شه:
 * - کشیدنِ یه **اسلات** روی اسلاتِ دیگه → جای دوتاشون عوض می‌شه (مرتب‌کردن).
 * - کشیدنِ یه **مقصد از مخزن** روی یه اسلات → می‌شینه جای اون اسلات.
 */
@Composable
fun NavEditorSheet(
    slots: List<NavDestination>,
    customized: Boolean,
    onSlotsChange: (List<String>) -> Unit,
    onReset: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    // اسلاتی که با ضربه «مسلح» شده و منتظرِ انتخابِ مقصده. -۱ یعنی هیچ‌کدوم.
    var armedSlot by remember { mutableIntStateOf(-1) }
    // مختصاتِ ریشه‌ی هر اسلات - برای اینکه بفهمیم انگشت رو کدوم خونه ول شد.
    val slotBounds = remember { mutableStateMapOf<Int, Rect>() }

    fun assign(slotIndex: Int, dest: NavDestination) {
        if (slotIndex <= 0) return // اسلاتِ ۰ قفله
        val next = slots.map { it.id }.toMutableList()
        // اگه مقصد از قبل جای دیگه‌ای تو نواره، جای اون دو تا عوض می‌شه (نه تکراری‌شدن).
        val existing = next.indexOf(dest.id)
        if (existing == 0) return // خانه از جاش تکون نمی‌خوره
        if (existing > 0) next[existing] = next[slotIndex]
        next[slotIndex] = dest.id
        onSlotsChange(next)
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        armedSlot = -1
    }

    /** اسلاتی که نقطه‌ی رهاشدنِ انگشت داخلشه. */
    fun slotAt(x: Float, y: Float): Int? =
        slotBounds.entries.firstOrNull { it.value.contains(androidx.compose.ui.geometry.Offset(x, y)) }?.key

    Box(
        modifier = modifier
            .fillMaxSize()
            // پرده‌ی پشت. تپ روش می‌بنده - ولی نوارِ واقعیِ پایین **بیرونِ این پرده‌ست** و
            // دیده می‌شه، چون این Box داخلِ محتوای Scaffoldه نه روش.
            .background(Color.Black.copy(alpha = 0.34f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClose,
            ),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = AppRadius.sheet, topEnd = AppRadius.sheet))
                .background(AppSurface)
                // تپِ داخلِ پنل نباید به پرده برسه و پنل رو ببنده.
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "نوار پایینت را بچین",
                    color = AppText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "بستن",
                    tint = AppMuted,
                    modifier = Modifier.size(20.dp).clickable(onClick = onClose),
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "برای باز کردن این بخش، روی هر دکمه‌ی نوار پایین یک لحظه نگه‌دار. " +
                    "اینجا مقصد را بکش یا اول جایش را بزن و بعد مقصدش را انتخاب کن؛ تغییر همان لحظه پایین صفحه دیده می‌شود.",
                color = AppMuted,
                fontSize = 11.5.sp,
                lineHeight = 19.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppRadius.row))
                    .background(AppPrimaryPill)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            )

            Spacer(Modifier.height(14.dp))
            Text("پنج جای نوار", color = AppLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                slots.forEachIndexed { index, dest ->
                    SlotCell(
                        dest = dest,
                        locked = index == 0,
                        armed = index == armedSlot,
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { slotBounds[index] = it.boundsInRoot() },
                        onTap = { armedSlot = if (armedSlot == index) -1 else index },
                        onDropAt = { x, y -> slotAt(x, y)?.let { target -> assign(target, dest) } },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            // **قرصِ شست** - «تصمیم است نه تزئین» (سندِ ۴۱). تو RTL راست‌ترین اسلات نزدیک‌ترین
            // نقطه به شسته، و ترتیبِ پیشنهادیِ کارتِ `41a` هم از همین قاعده میاد.
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(AppRadius.button))
                    .background(AppLineRow)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.TouchApp,
                    contentDescription = null,
                    tint = AppLabel,
                    modifier = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "راستِ نوار به شستِ راست نزدیک‌تر است — پرکاربردترین را همان‌جا بگذار",
                    color = AppLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "${toFa(NavDestination.entries.size)} مقصد",
                    color = AppText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "هرچه اینجا نباشد در کشو می‌مانَد",
                    color = AppLabel,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(8.dp))
            // مخزن: چهارتایی، نه `LazyVerticalGrid` (قاعده‌ی ماندگارِ پروژه - شبکه‌ی تنبل
            // داخلِ ظرفِ اسکرول‌شونده wrap-content نمی‌شه).
            NavDestination.entries.chunked(4).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    row.forEach { dest ->
                        PoolCell(
                            dest = dest,
                            inBar = dest in slots,
                            modifier = Modifier.weight(1f),
                            onTap = { if (armedSlot > 0) assign(armedSlot, dest) },
                            onDropAt = { x, y -> slotAt(x, y)?.let { target -> assign(target, dest) } },
                        )
                    }
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }

            if (customized) {
                Spacer(Modifier.height(6.dp))
                // «بی‌مودالِ تأیید — کاری که یک ضربه برمی‌گردد، تأیید نمی‌خواهد» (قاعده‌ی `41c`).
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppRadius.row))
                        .clickable { onReset(); armedSlot = -1 }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.Restore,
                        contentDescription = null,
                        tint = AppMuted,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("نوارِ پیش‌فرض", color = AppMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/** یه خانه‌ی نوار داخلِ ویرایشگر. */
@Composable
private fun SlotCell(
    dest: NavDestination,
    locked: Boolean,
    armed: Boolean,
    onTap: () -> Unit,
    onDropAt: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    // «اسلاتِ منتظرِ انتخاب: translateY(-4dp)، حاشیه‌ی ۲px AppPrimary، سایه‌ی سختِ ۴dp» - `41b`.
    val lift by animateFloatAsState(if (armed) -4f else 0f, label = "slotLift")
    DraggableCell(
        modifier = modifier.graphicsLayer { translationY = lift * density },
        enabled = !locked,
        onTap = { if (!locked) onTap() },
        onDropAt = onDropAt,
    ) { dragging ->
        Box {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (armed) {
                            Modifier.hardShadow(AppPrimary, 4.dp, AppRadius.icon)
                        } else {
                            Modifier
                        },
                    )
                    .clip(RoundedCornerShape(AppRadius.icon))
                    .background(if (armed || dragging) AppPrimaryPill else AppLineRow)
                    .then(
                        if (armed) {
                            Modifier.border(AppStroke.card, AppPrimary, RoundedCornerShape(AppRadius.icon))
                        } else {
                            Modifier
                        },
                    )
                    .padding(vertical = 9.dp, horizontal = 2.dp),
            ) {
                Icon(
                    dest.icon,
                    contentDescription = dest.label,
                    tint = if (armed || dragging) AppPrimaryInk else AppMuted,
                    modifier = Modifier.size(19.dp),
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    dest.label,
                    color = if (armed || dragging) AppPrimaryInk else AppMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
            if (locked) {
                // قفلِ ۹px گوشه - «می‌گوید چرا» (قاعده‌ی `41c`).
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = "این جا ثابت است",
                    tint = AppLabel,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(3.dp)
                        .size(9.dp),
                )
            }
        }
    }
}

/** یه مقصد از مخزنِ پایین. */
@Composable
private fun PoolCell(
    dest: NavDestination,
    inBar: Boolean,
    onTap: () -> Unit,
    onDropAt: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    DraggableCell(modifier = modifier, enabled = true, onTap = onTap, onDropAt = onDropAt) { dragging ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppRadius.button))
                .background(if (dragging) AppPrimaryPill else AppSurface)
                .border(
                    AppStroke.row,
                    if (inBar) AppPrimary else AppLine,
                    RoundedCornerShape(AppRadius.button),
                )
                .padding(vertical = 8.dp, horizontal = 4.dp),
        ) {
            Icon(
                dest.icon,
                contentDescription = null,
                tint = if (inBar) AppPrimaryInk else AppMuted,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                dest.label,
                color = if (inBar) AppPrimaryInk else AppText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

/**
 * پوسته‌ی مشترکِ «بگیر و بکش» - همون‌جایی که خواسته‌ی کشیدنیِ کاربر پیاده می‌شه.
 *
 * انگشت که بلند شد، مختصاتِ **ریشه‌ی صفحه** حساب می‌شه و به [onDropAt] می‌ره تا صدازننده
 * تصمیم بگیره روی کدوم اسلات افتاده. خودِ خانه سرِ جاش می‌مونه و فقط یه کپیِ شناور جابه‌جا
 * می‌شه (`graphicsLayer`), پس چیدمان حینِ کشیدن نمی‌پره.
 */
@Composable
private fun DraggableCell(
    enabled: Boolean,
    onTap: () -> Unit,
    onDropAt: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (dragging: Boolean) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    var dragging by remember { mutableStateOf(false) }
    var dx by remember { mutableFloatStateOf(0f) }
    var dy by remember { mutableFloatStateOf(0f) }
    var origin by remember { mutableStateOf(Rect.Zero) }

    Box(
        modifier = modifier
            .onGloballyPositioned { origin = it.boundsInRoot() }
            .graphicsLayer {
                if (dragging) {
                    translationX = dx
                    translationY = dy
                    scaleX = 1.06f
                    scaleY = 1.06f
                    shadowElevation = 8f
                }
            }
            .pointerInput(enabled, onDropAt) {
                if (!enabled) return@pointerInput
                // کشیدنِ **بی‌درنگ**: نگه‌داشتنِ طولانی لازم نیست، چون این خانه‌ها اسکرول
                // افقی ندارن و هیچ ژستِ دیگه‌ای باهاش تصادف نمی‌کنه.
                detectDragGestures(
                    onDragStart = {
                        dragging = true
                        dx = 0f
                        dy = 0f
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        dx += amount.x
                        dy += amount.y
                    },
                    onDragEnd = {
                        onDropAt(origin.center.x + dx, origin.center.y + dy)
                        dragging = false
                        dx = 0f
                        dy = 0f
                    },
                    onDragCancel = {
                        dragging = false
                        dx = 0f
                        dy = 0f
                    },
                )
            }
            // ضربه به‌عنوانِ راهِ دوم - نگرانیِ دسترس‌پذیریِ خودِ طراح.
            .clickable(onClick = onTap),
    ) {
        content(dragging)
    }
}
