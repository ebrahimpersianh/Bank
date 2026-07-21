package ir.sadteam.roozegar.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.roozegar.core.JalaliCalendar
import ir.sadteam.roozegar.core.MonthGrid
import ir.sadteam.roozegar.core.Occasions
import ir.sadteam.roozegar.core.PersianDate
import ir.sadteam.roozegar.core.PersianNames
import ir.sadteam.roozegar.core.format
import ir.sadteam.roozegar.core.toPersianDigits
import ir.sadteam.roozegar.notif.HijriDates
import ir.sadteam.roozegar.ui.effects.ParticleOverlay
import ir.sadteam.roozegar.ui.glass.AuroraGlassBackground
import ir.sadteam.roozegar.ui.glass.GlassCard
import ir.sadteam.roozegar.ui.theme.Danger
import ir.sadteam.roozegar.ui.theme.Gold
import ir.sadteam.roozegar.ui.theme.Teal
import ir.sadteam.roozegar.ui.theme.TextMuted
import kotlinx.coroutines.launch

/** صفحه‌ی اصلی: سربرگ امروز + پیجر ماه‌ها + پنل روز انتخاب‌شده، همه رو پس‌زمینه‌ی شفق شیشه‌ای. */
@Composable
fun CalendarScreen(today: PersianDate, effectsEnabled: Boolean, onOpenSettings: () -> Unit) {
    val todayIndex = MonthGrid.indexOf(today.y, today.m)
    // پیجر «بی‌نهایت» ماه‌ها: صفحه = شماره‌ی سریالی ماه؛ محدوده‌ی عملی ۱۲۰۰ سال کافیه.
    val pagerState = rememberPagerState(initialPage = todayIndex, pageCount = { 12 * 2400 })
    var selected by remember(today) { mutableStateOf(today) }
    val scope = rememberCoroutineScope()

    AuroraGlassBackground {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            // نوار بالا: نام اپ + دکمه‌ی تنظیمات
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("تقویم من", style = MaterialTheme.typography.titleLarge, color = Gold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "تنظیمات", tint = TextMuted)
                }
            }

            // سربرگ امروز
            GlassCard(Modifier.fillMaxWidth()) {
                Text(
                    PersianNames.weekdays[JalaliCalendar.dayOfWeekSaturdayFirst(today)],
                    style = MaterialTheme.typography.titleMedium,
                    color = Teal,
                )
                Text(
                    today.format(),
                    style = MaterialTheme.typography.headlineLarge,
                )
                Spacer(Modifier.height(6.dp))
                val g = JalaliCalendar.toGregorian(today)
                val gLine =
                    "${g.d.toPersianDigits()} ${PersianNames.gregorianMonths[g.m - 1]} ${g.y.toPersianDigits()}"
                val hLine = HijriDates.todayLine()
                Text(
                    if (hLine != null) "$gLine میلادی  ·  $hLine قمری" else "$gLine میلادی",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
                Occasions.titlesOf(today)?.let { titles ->
                    Spacer(Modifier.height(8.dp))
                    OccasionBadge(titles)
                }
            }

            Spacer(Modifier.height(12.dp))

            // کارت ماه
            GlassCard(Modifier.fillMaxWidth(), contentPadding = 12.dp) {
                val (year, month) = MonthGrid.fromIndex(pagerState.currentPage)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // تو RTL فلش «راست» یعنی ماه قبل - جهت‌ها با آفست صفحه کار می‌کنن، نه چپ/راست بصری
                    IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }) {
                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "ماه قبل", tint = TextMuted)
                    }
                    Text(
                        "${PersianNames.months[month - 1]} ${year.toPersianDigits()}",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }) {
                        Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "ماه بعد", tint = TextMuted)
                    }
                }
                if (pagerState.currentPage != todayIndex) {
                    TextButton(
                        onClick = {
                            selected = today
                            scope.launch { pagerState.animateScrollToPage(todayIndex) }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) { Text("برگرد به امروز", color = Gold, fontSize = 13.sp) }
                }

                // سطر حروف روزهای هفته
                Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 6.dp)) {
                    PersianNames.weekdayInitials.forEachIndexed { i, init ->
                        Text(
                            init,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (i == 6) Danger.copy(alpha = 0.8f) else TextMuted,
                        )
                    }
                }

                HorizontalPager(state = pagerState, verticalAlignment = Alignment.Top) { page ->
                    val (py, pm) = MonthGrid.fromIndex(page)
                    MonthGridView(
                        year = py,
                        month = pm,
                        today = today,
                        selected = selected,
                        onSelect = { selected = it },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // پنل روز انتخاب‌شده
            GlassCard(Modifier.fillMaxWidth()) {
                Text(
                    selected.format(withWeekday = true),
                    style = MaterialTheme.typography.titleMedium,
                )
                val sg = JalaliCalendar.toGregorian(selected)
                Text(
                    "${sg.d.toPersianDigits()} ${PersianNames.gregorianMonths[sg.m - 1]} ${sg.y.toPersianDigits()} میلادی",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
                Spacer(Modifier.height(8.dp))
                val occasions = Occasions.of(selected)
                if (occasions.isEmpty()) {
                    Text("مناسبتی برای این روز ثبت نشده.", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                } else {
                    occasions.forEach { occ ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            Box(
                                Modifier
                                    .size(7.dp)
                                    .background(if (occ.holiday) Danger else Gold, CircleShape),
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(
                                occ.title + if (occ.holiday) " (تعطیل رسمی)" else "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (occ.holiday) Danger else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // افکت تمام‌صفحه‌ی روز خاص (فقط برای «امروز»، مستقل از ماهی که کاربر داره می‌بینه)
        if (effectsEnabled) {
            ParticleOverlay(Occasions.effectOf(today))
        }
    }

    // با عوض‌شدن روز (نیمه‌شب/برگشت به اپ) پیجر و انتخاب رو برگردون رو امروز
    LaunchedEffect(today) {
        selected = today
        pagerState.scrollToPage(MonthGrid.indexOf(today.y, today.m))
    }
}

@Composable
private fun MonthGridView(
    year: Int,
    month: Int,
    today: PersianDate,
    selected: PersianDate,
    onSelect: (PersianDate) -> Unit,
) {
    val grid = remember(year, month) { MonthGrid.of(year, month) }
    Column(Modifier.fillMaxWidth()) {
        for (week in 0 until grid.weeks) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val dayNum = week * 7 + col - grid.leadingBlanks + 1
                    if (dayNum < 1 || dayNum > grid.daysInMonth) {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = PersianDate(year, month, dayNum)
                        DayCell(
                            date = date,
                            isToday = date == today,
                            isSelected = date == selected,
                            isFriday = col == 6,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelect(date) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: PersianDate,
    isToday: Boolean,
    isSelected: Boolean,
    isFriday: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val occasions = remember(date) { Occasions.of(date) }
    val isHoliday = isFriday || occasions.any { it.holiday }
    Box(
        modifier
            .aspectRatio(1f)
            .padding(3.dp)
            .let {
                when {
                    isSelected -> it.background(Teal.copy(alpha = 0.28f), CircleShape)
                    else -> it
                }
            }
            .let {
                if (isToday) {
                    // حلقه‌ی گرادیانی فیروزه‌ای→طلایی دور امروز - امضای بصری اپ
                    it.border(2.dp, Brush.sweepGradient(listOf(Teal, Gold, Teal)), CircleShape)
                } else {
                    it
                }
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                date.d.toPersianDigits(),
                style = MaterialTheme.typography.bodyLarge,
                color = when {
                    isHoliday -> Danger
                    else -> MaterialTheme.colorScheme.onSurface
                },
            )
            if (occasions.isNotEmpty()) {
                Box(
                    Modifier
                        .size(4.dp)
                        .background(if (occasions.any { it.holiday }) Danger else Gold, CircleShape),
                )
            }
        }
    }
}

@Composable
private fun OccasionBadge(text: String) {
    // «لهجه‌ی طلایی» رو پس‌زمینه/حاشیه، نه رو فونت (همون الگوی تاییدشده‌ی پروژه‌ی قبلی)
    Box(
        Modifier
            .background(Gold.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
            .border(1.dp, Gold.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}
