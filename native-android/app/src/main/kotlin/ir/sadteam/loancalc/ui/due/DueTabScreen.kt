package ir.sadteam.loancalc.ui.due

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianCalendar
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.HeroPillBg
import ir.sadteam.loancalc.ui.components.dashedBorder
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppChartGrid
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppUrgentBorder
import ir.sadteam.loancalc.ui.theme.AppUrgentShadow
import ir.sadteam.loancalc.ui.theme.AppWarning
import ir.sadteam.loancalc.ui.theme.AppWarningPill
import ir.sadteam.loancalc.ui.theme.hardShadow

/**
 * تبِ **سررسید** - بازسازیِ کاملِ فریمِ `3a`.
 *
 * ```
 * ۱ عنوان (۱۷/۹۰۰)
 * ۲ تاگلِ سه‌تایی اقساط/چک/طلب‌وبدهی + بجِ قرمزِ عددی
 * ۳ هیرویِ سبز: حلقه‌ی پیشرفت + مجموعِ سررسیدهای معلق
 * ۴ گروهِ «عقب‌افتاده» - ردیفِ حاشیه‌قرمز با سایه‌ی سخت و دکمه‌ی «پرداخت کن»
 * ۵ گروهِ «این هفته»
 * ۶ گروهِ «پرداخت‌شده» (نیمه‌محو)
 * ```
 *
 * محاسبه‌ی داده تو [DueListViewModel]ه - `LoanRepository.getRows()` **suspend**ه و نباید تو
 * `remember{}` صدا زده بشه.
 */
@Composable
fun DueTabScreen(
    onAddCheque: () -> Unit = {},
    onAddLoan: () -> Unit = {},
    /**
     * 🚨 **گزارشِ کاربر: «رو اینا می‌زنم جزئیاتِ وام نمیاد».** ردیف‌های این صفحه اصلاً
     * کلیک‌پذیر نبودن - فقط دکمه‌ی «پرداخت کن» کار می‌کرد. حالا تپ رو خودِ ردیف همون
     * وام رو تو صفحه‌ی «وام‌های من» باز می‌کنه.
     */
    onOpenLoan: (Long) -> Unit = {},
    /**
     * ⚠️ همون باگ برای **چک** و **طلب‌وبدهی** هم زنده بود: `row.loan` فقط برای قسط پر
     * می‌شه، پس `onOpen` این دو تب هیچ کاری نمی‌کرد - ولی ردیف `pressScaleClickable`
     * داشت، یعنی فشرده می‌شد و برمی‌گشت و هیچ اتفاقی نمی‌افتاد. این از ردیفِ
     * کلیک‌ناپذیر **بدتر**ه، چون بازخوردِ لمسی می‌گه کاری شد.
     *
     * اگه مقصدی برای تپِ چک/بدهی ندارید، این دو رو **خالی نگذارید** - بگید تا ردیف رو
     * کلیک‌ناپذیر کنم.
     */
    onOpenCheque: (Long) -> Unit = {},
    onOpenDebt: (Long) -> Unit = {},
    viewModel: DueListViewModel = hiltViewModel(),
) {
    var tab by remember { mutableStateOf(DueTab.INSTALLMENTS) }
    val installments by viewModel.installments.collectAsState()
    val cheques by viewModel.cheques.collectAsState()
    val debts by viewModel.debts.collectAsState()
    val privacyMode = LocalPrivacyMode.current

    val buckets = when (tab) {
        DueTab.INSTALLMENTS -> installments
        DueTab.CHEQUES -> cheques
        DueTab.DEBTS -> debts
    }

    // هیچ ردیفی تو هیچ‌کدوم از سه تب نیست → **فریمِ `21e`**: تقویمِ ماه + کارتِ خط‌چین.
    // نه تاگل، نه کارتِ قهرمانِ صفر - عیناً همون چیزی که فریمِ خالی نشون می‌ده.
    val everythingEmpty = listOf(installments, cheques, debts)
        .all { it.overdue.isEmpty() && it.thisWeek.isEmpty() && it.paid.isEmpty() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 110.dp),
        verticalArrangement = Arrangement.spacedBy(if (everythingEmpty) 13.dp else 9.dp),
    ) {
        item { Text("سررسید", color = AppText, fontSize = if (everythingEmpty) 18.sp else 17.sp, fontWeight = FontWeight.Black) }
        if (everythingEmpty) {
            item { MonthStripCard(dueDays = emptySet()) }
            item { NothingDueCard(onAddCheque = onAddCheque, onAddLoan = onAddLoan) }
            return@LazyColumn
        }
        item {
            DueTabBar(
                selected = tab,
                onSelect = { tab = it },
                // فریم بجِ قرمز رو برای «سررسیدِ توجه‌خواه» گذاشته، نه مخصوصِ چک. قبلاً فقط
                // تبِ چک بج می‌گرفت، پس کاربری که سه قسطِ عقب‌افتاده و صفر چک داشت هیچ
                // نشانه‌ای روی تاگل نمی‌دید.
                badges = mapOf(
                    DueTab.INSTALLMENTS to installments.overdue.size,
                    DueTab.CHEQUES to cheques.overdue.size,
                    DueTab.DEBTS to debts.overdue.size,
                ),
            )
        }
        item {
            PendingHero(
                count = buckets.pendingCount,
                total = buckets.pendingAmount,
                progress = buckets.paidShare,
                privacyMode = privacyMode,
            )
        }
        // این تب خالیه ولی تبِ دیگه‌ای داده داره - کارتِ خالیِ کوچیک، نه کلِ صفحه‌ی `21e`.
        if (buckets.overdue.isEmpty() && buckets.thisWeek.isEmpty() && buckets.paid.isEmpty()) {
            item { NothingDueCard(onAddCheque = onAddCheque, onAddLoan = onAddLoan) }
        }
        if (buckets.overdue.isNotEmpty()) {
            item { GroupLabel("عقب‌افتاده", OverdueInk) }
            items(buckets.overdue, key = { it.id }) { row ->
                OverdueRow(
                    row = row,
                    privacyMode = privacyMode,
                    onOpen = { row.open(onOpenLoan, onOpenCheque, onOpenDebt) },
                )
            }
        }
        if (buckets.thisWeek.isNotEmpty()) {
            item { GroupLabel("این هفته", AppMuted) }
            items(buckets.thisWeek, key = { it.id }) { row ->
                PlainRow(row, privacyMode, onOpen = { row.open(onOpenLoan, onOpenCheque, onOpenDebt) })
            }
        }
        if (buckets.paid.isNotEmpty()) {
            item { GroupLabel("پرداخت‌شده", AppMuted) }
            items(buckets.paid, key = { it.id }) { row ->
                Box(modifier = Modifier.alpha(0.6f)) {
                    PlainRow(row, privacyMode, paid = true, onOpen = { row.open(onOpenLoan, onOpenCheque, onOpenDebt) })
                }
            }
        }
    }
}

/**
 * تپ روی ردیف → جزئیاتِ **همون** تعهد (تصمیمِ ۱ی README).
 *
 * قبلاً فقط `row.loan` چک می‌شد که برای چک و بدهی `null`ه، پس دو تب از سه تب بی‌کنش
 * بودن. حالا هر سه شناسه‌ی خودشون رو دارن.
 */
private fun DueListViewModel.DueRow.open(
    onOpenLoan: (Long) -> Unit,
    onOpenCheque: (Long) -> Unit,
    onOpenDebt: (Long) -> Unit,
) {
    loan?.let { onOpenLoan(it.id); return }
    chequeId?.let { onOpenCheque(it); return }
    debtId?.let { onOpenDebt(it) }
}

/**
 * ذخیره و محاسبه **ریال**ه و نمایش **تومان** (قاعده‌ی واحدِ README).
 *
 * قبلاً `fmt()`ِ خام با پسوندِ «ریال» و رقمِ لاتین در پنج جای این فایل بود.
 */
private fun amountToman(rial: Double): String = fmt(rialToToman(rial.toLong()).toDouble()).faDigits()

enum class DueTab(val label: String) {
    INSTALLMENTS("اقساط"),
    CHEQUES("چک"),
    // کاشیِ `DueScreen` «طلب و بدهی» (با فاصله) می‌نویسد و این enum «طلب‌وبدهی» - یک چیز
    // با دو نوشتار. کاشی ملاک شد.
    DEBTS("طلب و بدهی"),
}

/** سهمِ پرداخت‌شده از کلِ ردیف‌ها - عددِ داخلِ حلقه‌ی هیرو. */
private val DueListViewModel.DueBuckets.paidShare: Float
    get() {
        // ⚠️ `paid` تو ViewModel `take(10)` داره (سقفِ **نمایش**). قبلاً همون لیستِ
        // بریده تو ریاضیِ درصد می‌نشست، پس کاربرِ ۴۰ قسطِ پرداخت‌شده و ۲ معلق ۸۳٪
        // می‌دید نه ۹۵٪. `paidTotalCount` بی‌سقفه.
        val total = overdue.size + thisWeek.size + paidTotalCount
        return if (total == 0) 0f else paidTotalCount.toFloat() / total
    }

// ═══ ۱ب · تقویمِ ماه (فقط حالتِ خالی، فریمِ `21e`) ═══════════════════════════════
/**
 * نوارِ تقویمِ ماهِ جاری - دو ردیفِ هفت‌تایی از **چهارده روزِ پیشِ رو** با امروزِ حاشیه‌دار.
 *
 * تو فریم فقط هفت خانه‌ی اول شماره دارن و بقیه خالی‌ان؛ اون یه طرحِ نمادینه، پس اینجا هر
 * چهارده خانه شماره‌ی واقعیِ روز رو دارن. یادداشتِ خودِ فریم می‌گه «تقویم پنهان نمی‌شود -
 * یکدست روشن می‌ماند» و «هیچ سررسیدی نداری» خبرِ خوبه نه خطا.
 */
@Composable
private fun MonthStripCard(dueDays: Set<Int>) {
    val today = remember { JalaliCalendar.today() }
    val days = remember(today) { (0 until 14).map { PersianCalendar.addDays(today, it) } }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppSurface)
            .border(2.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${persianMonthName(today.m)} ${toFa(today.y)}",
                color = AppText,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                if (dueDays.isEmpty()) "هیچ سررسیدی نداری" else "${toFa(dueDays.size)} سررسید",
                color = DueGreenDeep,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        // ⚠️ گریدِ تنبل داخلِ لیستِ تنبل نمی‌شه - قاعده‌ی ماندگارِ پروژه: chunked + Row.
        days.chunked(7).forEachIndexed { rowIndex, week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (rowIndex == 0) 13.dp else 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                week.forEach { day ->
                    val isToday = day.y == today.y && day.m == today.m && day.d == today.d
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isToday) AppSurface else DayCellBg)
                            .then(
                                if (isToday) {
                                    Modifier.border(2.dp, DueGreen, RoundedCornerShape(9.dp))
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            toFa(day.d),
                            color = if (isToday) DueGreenDeep else DayCellInk,
                            fontSize = 10.5.sp,
                            fontWeight = if (isToday) FontWeight.Black else FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

// ═══ ۱ج · کارتِ خط‌چینِ «چیزی در راه نیست» ═════════════════════════════════════════
@Composable
private fun NothingDueCard(onAddCheque: () -> Unit, onAddLoan: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppSurface)
            .dashedBorder(20.dp)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(GreenIconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.EventAvailable,
                contentDescription = null,
                tint = DueGreen,
                modifier = Modifier.size(28.dp),
            )
        }
        // ⚠️ عنوان و توضیح **یه بلوکِ واحد**ن با فاصله‌ی ۶ (مثلِ `margin-top`ی فریم)، نه دو
        // آیتمِ جدا با فاصله‌ی منفی - `Modifier.padding` عددِ منفی رو قبول نمی‌کنه و همون
        // لحظه‌ی رسم کرش می‌ده (کرشِ نسخه‌ی ۱.۰.۴۷۷: «Padding must be non-negative»).
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "هیچ چک و قسطی در راه نیست",
                color = AppText,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Text(
                "وقتی چک یا وامی ثبت کنی، سررسیدهایش اینجا و روی ویجت دیده می‌شود.",
                color = AppMuted,
                fontSize = 12.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "افزودنِ چک",
                color = Color.White,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .hardShadow(DueGreenDeep, 3.dp, 999.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(DueGreen)
                    .pressScaleClickable(onClick = onAddCheque)
                    .padding(vertical = 13.dp),
            )
            Text(
                "افزودنِ وام",
                color = AppMuted,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(999.dp))
                    .background(AppSurface)
                    .border(1.5.dp, OutlineBorder, RoundedCornerShape(999.dp))
                    .pressScaleClickable(onClick = onAddLoan)
                    .padding(vertical = 13.dp),
            )
        }
    }
}

// ═══ ۲ · تاگلِ سه‌تایی ═══════════════════════════════════════════════════════════
@Composable
private fun DueTabBar(selected: DueTab, onSelect: (DueTab) -> Unit, badges: Map<DueTab, Int>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TabTrack)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        DueTab.entries.forEach { entry ->
            val active = entry == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (active) DueGreen else Color.Transparent)
                    .pressScaleClickable { onSelect(entry) }
                    // ۸dp + متنِ ۱۱٫۵sp ≈ ۳۶dp بود، زیرِ حداقلِ ۴۴ِ خودِ README. ۱۳ می‌شه ~۴۶.
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    entry.label,
                    color = if (active) Color.White else AppMuted,
                    fontSize = 11.5.sp,
                    fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Bold,
                )
                val badge = badges[entry] ?: 0
                if (badge > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 2.dp, end = 14.dp)
                            .size(13.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(DangerSolid),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            toFa(badge),
                            color = Color.White,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

// ═══ ۳ · هیرویِ سبز ═════════════════════════════════════════════════════════════
/**
 * هیرویِ سبز - حلقه‌ی پیشرفت + مجموعِ معلق.
 *
 * ⚠️ گرادیان **دستی ساخته نمی‌شه**: `AppHeroCard` همون کارتیه که `DueScreen` و
 * `AccountsTotalHero` استفاده می‌کنن؛ دو پیاده‌سازیِ موازیِ یک کارت همون موردی بود که
 * در تبِ دارایی هم گفتم (`TotalWealthHero`).
 *
 * ⚠️ **سپرِ گوشه از دست رفت**: `drawShieldWatermark` با `Modifier.drawBehind` روی
 * همون `Row`ِ گرادیانی سوار بود. اگه `AppHeroCard` پارامترِ `modifier` می‌گیره،
 * `.drawBehind { drawShieldWatermark() }` رو روش بگذارید؛ تابعش رو نگه داشتم و صداش
 * نزدم، پس کامپایل نمی‌شکنه.
 */
@Composable
private fun PendingHero(count: Int, total: Double, progress: Float, privacyMode: Boolean) {
    AppHeroCard {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.size(46.dp), contentAlignment = Alignment.Center) {
            val track = HeroPillBg
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = size.minDimension * 4f / 40f
                val inset = stroke / 2f + size.minDimension * 2f / 40f
                val diameter = size.minDimension - inset * 2f
                drawArc(
                    color = track,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(diameter, diameter),
                    style = Stroke(width = stroke),
                )
                drawArc(
                    color = Color.White,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(diameter, diameter),
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
            Text(toFa(count), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "مجموعِ سررسیدهای معلق",
                color = HeroMuted,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
            )
            PrivacyCrossfade(privacyMode) { masked ->
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(top = 2.dp),
                ) {
                    Text(
                        maskIfPrivate(masked, amountToman(total)),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        " تومان",
                        color = HeroMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 3.dp, bottom = 1.dp),
                    )
                }
            }
        }
    }
    }
}

// ═══ ۴ · برچسبِ گروه ════════════════════════════════════════════════════════════
@Composable
private fun GroupLabel(text: String, color: Color) {
    Text(
        text,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(start = 2.dp, end = 2.dp, top = 2.dp),
    )
}

// ═══ ۵ · ردیفِ عقب‌افتاده ════════════════════════════════════════════════════════
@Composable
private fun OverdueRow(
    row: DueListViewModel.DueRow,
    privacyMode: Boolean,
    onOpen: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pressScaleClickable(scale = 0.99f, onClick = onOpen)
            .hardShadow(OverdueBorder, 4.dp, 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppSurface)
            .border(2.dp, OverdueBorder, RoundedCornerShape(16.dp)),
    ) {
        // ⚠️ دو `CoinIcon`ِ گوشه برداشته شد. سکه در این برنامه واحدِ **پاداش**ه (بخشِ ۴۵)؛
        // گذاشتنش روی قرمزترین ردیفِ صفحه دو معنا رو قاطی می‌کنه. اگه «ریبونِ رسید» رو
        // عمداً می‌خواید، برگردونید - ولی نه با آیکونِ سکه.
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RowIcon(Icons.Filled.CreditCard, DangerSolid, OverdueIconBg)
            Column(modifier = Modifier.weight(1f)) {
                Text(row.title, color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                Text(
                    overdueText(row.daysOverdue),
                    color = OverdueInk,
                    fontSize = 10.5.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    maskIfPrivate(masked, amountToman(row.amount)),
                    color = AppText,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            // 🚨 دکمه‌ی «پرداخت کن» **حذف شد** - تصمیمِ ۲ی README و فریمِ `36i`.
            // ارتفاعِ لمسی‌اش ~۲۰dp بود (`vertical = 2.dp`)، نصفِ حداقلِ ۴۴ِ خودتون، و
            // پس‌گرفتنِ «پرداخت شد» کارِ سختیه. پرداخت جایش در جزئیاتِ قسط و در اعلانه.
            // شِوران می‌گه ردیف مقصد داره، وگرنه بی‌کنش به‌نظر می‌رسه.
            Icon(
                Icons.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = AppLabel,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// ═══ ۶ · ردیفِ ساده (این هفته / پرداخت‌شده) ═══════════════════════════════════════
@Composable
private fun PlainRow(
    row: DueListViewModel.DueRow,
    privacyMode: Boolean,
    paid: Boolean = false,
    onOpen: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScaleClickable(scale = 0.99f, onClick = onOpen)
            .clip(RoundedCornerShape(16.dp))
            .background(AppSurface)
            .border(2.dp, PlainBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (paid) {
            RowIcon(Icons.Filled.Check, DueGreen, GreenIconBg)
        } else if (row.daysOverdue >= -3) {
            RowIcon(Icons.Filled.Schedule, WarnSolid, WarnIconBg)
        } else {
            RowIcon(Icons.Filled.CalendarMonth, DueGreen, GreenIconBg)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(row.title, color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            Text(
                if (paid) "پرداخت‌شده" else
                    "${toFa(row.date.d)} ${persianMonthName(row.date.m)} — ${toFa(-row.daysOverdue)} روز مانده",
                color = AppMuted,
                fontSize = 10.5.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                maskIfPrivate(masked, amountToman(row.amount)),
                color = AppText,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.ExtraBold,
                textDecoration = if (paid) TextDecoration.LineThrough else null,
            )
        }
    }
}

/**
 * سپرِ کم‌رنگِ گوشه‌ی هیرو - `<svg width=60 ... opacity:.15 left:-10 bottom:-12>`ی فریم.
 * نقشه‌ی مسیر: `M12 2l7 4v6c0 5-3 8-7 10-4-2-7-5-7-10V6z` رو ویوباکسِ ۲۴.
 */
@Suppress("unused")
private fun DrawScope.drawShieldWatermark() {
    val box = 60.dp.toPx()
    val k = box / 24f
    // ۱۰ و ۱۲ پیکسلِ بیرونِ لبه‌ی چپ-پایین، مثلِ فریم.
    val left = -10.dp.toPx()
    val top = size.height - box + 12.dp.toPx()
    fun x(v: Float) = left + v * k
    fun y(v: Float) = top + v * k
    val path = Path().apply {
        moveTo(x(12f), y(2f))
        lineTo(x(19f), y(6f))
        lineTo(x(19f), y(12f))
        cubicTo(x(19f), y(17f), x(16f), y(20f), x(12f), y(22f))
        cubicTo(x(8f), y(20f), x(5f), y(17f), x(5f), y(12f))
        lineTo(x(5f), y(6f))
        close()
    }
    drawPath(path, Color.White.copy(alpha = 0.15f))
}

@Composable
private fun RowIcon(icon: ImageVector, tint: Color, bg: Color) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
    }
}

/** «امروز سررسید» یا «X روز عقب» - عیناً جمله‌بندیِ فریم. */
private fun overdueText(daysOverdue: Int): String =
    if (daysOverdue == 0) "امروز سررسید" else "${toFa(daysOverdue)} روز عقب"

private val DueGreen: Color
    @Composable get() = AppPrimary
private val DueGreenDeep: Color
    @Composable get() = AppPrimaryDim
private val TabTrack: Color
    @Composable get() = AppChipBg
private val DangerSolid: Color
    @Composable get() = AppDanger
private val WarnSolid: Color
    @Composable get() = AppWarning
private val OverdueInk: Color
    @Composable get() = AppDangerInk
private val OverdueBorder: Color
    @Composable get() = AppUrgentBorder
private val OverdueIconBg: Color
    @Composable get() = AppUrgentShadow
private val PlainBorder: Color
    @Composable get() = AppLineRow
private val GreenIconBg: Color
    @Composable get() = AppPrimaryPill
private val CardBorder: Color
    @Composable get() = AppLine
private val DayCellBg: Color
    @Composable get() = AppIconFrame
private val DayCellInk: Color
    @Composable get() = AppLabel
private val OutlineBorder: Color
    @Composable get() = AppChartGrid
private val WarnIconBg: Color
    @Composable get() = AppWarningPill