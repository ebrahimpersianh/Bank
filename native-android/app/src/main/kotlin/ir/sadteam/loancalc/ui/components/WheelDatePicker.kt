package ir.sadteam.loancalc.ui.components

import android.view.SoundEffectConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText
import kotlin.math.abs
import kotlin.math.roundToInt

private val wheelMonthNames = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

/**
 * انتخابگر تاریخ شمسی به‌صورت چرخونه‌ی اسکرولی (spinner) - تپ روی تاریخ این صفحه رو باز می‌کنه،
 * بدون کلیک با بالا/پایین اسکرول‌کردنِ سه ستون (روز/ماه/سال) تنظیم می‌شه و هر «کلیک» چرخونه
 * یه لرزش خفیف + صدای تیکِ سیستم می‌ده (خواسته‌ی کاربر). دکمه‌ی «تأیید» تاریخ رو برمی‌گردونه.
 */
@Composable
fun WheelDatePickerScreen(
    initial: PersianDate,
    onConfirm: (PersianDate) -> Unit,
    onBack: () -> Unit,
    yearRange: IntRange = 1350..1410,
) {
    val years = remember(yearRange) { yearRange.toList() }
    var year by remember { mutableIntStateOf(initial.y.coerceIn(yearRange.first, yearRange.last)) }
    var month by remember { mutableIntStateOf(initial.m.coerceIn(1, 12)) }
    var day by remember { mutableIntStateOf(initial.d.coerceAtLeast(1)) }

    val maxDay = JalaliCalendar.daysInMonth(year, month)
    if (day > maxDay) day = maxDay
    val days = remember(maxDay) { (1..maxDay).toList() }

    Surface(color = AppBg, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                }
                Text("انتخاب تاریخ", color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
            }

            Spacer(Modifier.weight(1f))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                // نوار هایلایتِ ردیفِ وسط (ردیفِ انتخاب‌شده)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WheelItemHeight)
                        .background(AppPrimary.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
                        .border(1.dp, AppPrimary.copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    WheelColumn(
                        items = days.map { toFa(it) },
                        selectedIndex = days.indexOf(day).coerceAtLeast(0),
                        onCentered = { day = days[it] },
                        modifier = Modifier.weight(1f),
                    )
                    WheelColumn(
                        items = wheelMonthNames,
                        selectedIndex = month - 1,
                        onCentered = { month = it + 1 },
                        modifier = Modifier.weight(1.4f),
                    )
                    WheelColumn(
                        items = years.map { toFa(it) },
                        selectedIndex = years.indexOf(year).coerceAtLeast(0),
                        onCentered = { year = years[it] },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            GradientButton(
                onClick = {
                    val safeDay = day.coerceAtMost(JalaliCalendar.daysInMonth(year, month))
                    onConfirm(PersianDate(year, month, safeDay))
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("تأیید", fontWeight = FontWeight.Bold)
            }
        }
    }
}

internal val WheelItemHeight = 46.dp

/**
 * ستونِ چرخونه‌ی اسکرولی - [itemHeight]/[visibleRows] پارامتری شدن (پیش‌فرض همون مقادیرِ چرخونه‌ی
 * تمام‌صفحه) تا همین کامپوننت رو نسخه‌ی جمع‌وجورِ اینلاینِ [InlineWheelDateRow] هم بشه استفاده کرد،
 * بدون تکرارِ منطقِ اسنپ/هپتیک/محو‌شدنِ لبه‌ها.
 */
@Composable
internal fun WheelColumn(
    items: List<String>,
    selectedIndex: Int,
    onCentered: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = WheelItemHeight,
    visibleRows: Int = 5,
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex.coerceAtLeast(0))
    val fling = rememberSnapFlingBehavior(lazyListState = listState)
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val itemPx = with(LocalDensity.current) { itemHeight.toPx() }

    // چرخونه یه کنترلِ خودکفا و محدوده‌ست - نباید اسکرول/فلینگِ ته‌مونده‌ش (وقتی سریع می‌چرخونی و
    // به مرز لیست می‌رسه) به صفحه‌ی بیرونی درز کنه و کل صفحه رو هم یه‌کم پایین بیاره. با مصرفِ کاملِ
    // مقدارِ باقی‌مونده تو postScroll/postFling جلوی این «درز» رو می‌گیریم.
    val blockOverscrollLeak = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
                available

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity =
                available
        }
    }

    val centered by remember {
        derivedStateOf {
            (listState.firstVisibleItemIndex + (listState.firstVisibleItemScrollOffset / itemPx).roundToInt())
                .coerceIn(0, items.lastIndex.coerceAtLeast(0))
        }
    }

    LaunchedEffect(centered) {
        if (centered != selectedIndex) {
            onCentered(centered)
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            view.playSoundEffect(SoundEffectConstants.CLICK)
        }
    }

    LazyColumn(
        state = listState,
        flingBehavior = fling,
        modifier = modifier
            .height(itemHeight * visibleRows)
            .nestedScroll(blockOverscrollLeak),
        contentPadding = PaddingValues(vertical = itemHeight * (visibleRows / 2)),
    ) {
        itemsIndexed(items, key = { i, _ -> i }) { i, label ->
            val distance = abs(i - centered)
            val itemAlpha = when (distance) {
                0 -> 1f
                1 -> 0.55f
                2 -> 0.3f
                else -> 0.15f
            }
            Box(
                modifier = Modifier.fillMaxWidth().height(itemHeight),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = AppText,
                    fontSize = if (distance == 0) 17.sp else 13.sp,
                    fontWeight = if (distance == 0) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(itemAlpha),
                )
            }
        }
    }
}
