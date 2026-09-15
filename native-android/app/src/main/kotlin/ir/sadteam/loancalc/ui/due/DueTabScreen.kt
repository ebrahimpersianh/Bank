package ir.sadteam.loancalc.ui.due

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Receipt
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import ir.sadteam.loancalc.ui.components.HeroTone
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
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
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
    // 🚨 **بخشِ ۵۱**: تاگلِ سه‌تاییِ «اقساط/چک/طلب‌وبدهی» جایش را به یک فهرستِ یکپارچه داد.
    // سه منبعِ بدهی تا حالا هرکدام صفحه‌ی خودشان را داشتند، ولی کاربر اولِ ماه یک سوال دارد
    // نه سه: چه چیزی، چه وقت. پس گروه‌بندی روی **فوریت** است نه روی نوعِ بدهی، و قرص‌های
    // بالا فقط **فیلتر**ند - کسی که فقط چک می‌خواهد یک تپ فاصله دارد.
    var filter by remember { mutableStateOf<DueListViewModel.DueSource?>(null) }
    val all by viewModel.all.collectAsState()
    val privacyMode = LocalPrivacyMode.current
    val today = remember { JalaliCalendar.today() }

    fun List<DueListViewModel.DueRow>.filtered() = filter { row -> filter == null || row.kind == filter }

    val overdue = all.overdue.filtered()
    val thisWeek = all.thisWeek.filtered()
    val later = all.later.filtered()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 110.dp),
        verticalArrangement = Arrangement.spacedBy(if (all.isEmpty) 13.dp else 9.dp),
    ) {
        item { Text("سررسید", color = AppText, fontSize = if (all.isEmpty) 18.sp else 17.sp, fontWeight = FontWeight.Black) }
        if (all.isEmpty) {
            // کاربرِ تازه یا کسی که همه را پرداخت کرده - فریمِ `21e`/`51b`.
            item { MonthStripCard(dueDays = emptySet()) }
            item { NothingDueCard(onAddCheque = onAddCheque, onAddLoan = onAddLoan) }
            return@LazyColumn
        }
        item {
            DueHero(
                total = all.monthTotal(today),
                overdueCount = all.overdue.size,
                upcomingCount = all.upcomingCount,
                privacyMode = privacyMode,
            )
        }
        item {
            DueFilterRow(selected = filter, onSelect = { filter = it })
        }
        if (overdue.isEmpty() && thisWeek.isEmpty() && later.isEmpty()) {
            item {
                Text(
                    "در این دسته چیزی نداری.",
                    color = AppMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
        // هر سه سرگروه **همیشه به همین ترتیب**اند و گروهِ خالی حذف می‌شود، نه اینکه خالی
        // نشان داده شود.
        if (overdue.isNotEmpty()) {
            item { GroupLabel("عقب‌افتاده", OverdueInk) }
            items(overdue, key = { it.id }) { row ->
                DueRowCard(row, privacyMode, onOpen = { row.open(onOpenLoan, onOpenCheque, onOpenDebt) })
            }
        }
        if (thisWeek.isNotEmpty()) {
            item { GroupLabel("همین هفته", AppMuted) }
            items(thisWeek, key = { it.id }) { row ->
                DueRowCard(row, privacyMode, onOpen = { row.open(onOpenLoan, onOpenCheque, onOpenDebt) })
            }
        }
        if (later.isNotEmpty()) {
            item { GroupLabel("بعد از این", AppMuted) }
            items(later, key = { it.id }) { row ->
                DueRowCard(row, privacyMode, onOpen = { row.open(onOpenLoan, onOpenCheque, onOpenDebt) })
            }
        }
    }
}

/**
 * هیرویِ `51a`. **قرمز فقط وقتی موردِ عقب‌افتاده هست** - اگر قرمز بماند، کاربری که همه‌چیز را
 * پرداخت کرده هم حسِ بدهی می‌گیرد.
 */
@Composable
private fun DueHero(
    total: Double,
    overdueCount: Int,
    upcomingCount: Int,
    privacyMode: Boolean,
) {
    AppHeroCard(tone = if (overdueCount > 0) HeroTone.RED else HeroTone.GREEN) {
        Text("تا آخرِ ماه باید بدهی", color = HeroMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                maskIfPrivate(masked, amountToman(total)),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Text("تومان", color = HeroMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 1.dp))
        Row(
            modifier = Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            // قرص‌ها فقط شمارش‌اند و کلیک‌پذیر نیستند (قاعده‌ی `51c`).
            if (overdueCount > 0) HeroCountPill("${toFa(overdueCount)} عقب‌افتاده")
            HeroCountPill("${toFa(upcomingCount)} پیشِ‌رو")
        }
    }
}

@Composable
private fun HeroCountPill(label: String) {
    Text(
        label,
        color = Color.White,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(HeroPillBg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

/** قرص‌های فیلترِ `51a` - «همه» یعنی `null`. */
@Composable
private fun DueFilterRow(
    selected: DueListViewModel.DueSource?,
    onSelect: (DueListViewModel.DueSource?) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        DueFilterChip("همه", selected == null) { onSelect(null) }
        DueListViewModel.DueSource.entries.forEach { kind ->
            DueFilterChip(kind.label, selected == kind) { onSelect(kind) }
        }
    }
}

@Composable
private fun DueFilterChip(label: String, active: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(999.dp)
    Text(
        label,
        color = if (active) AppPrimary else AppMuted,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier
            .clip(shape)
            .background(if (active) AppPrimaryPill else AppChipBg)
            .border(2.dp, if (active) AppPrimary else AppLine, shape)
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 6.dp),
    )
}

/**
 * یک ردیفِ سررسید - `51a`.
 *
 * 🚨 **رنگ حاملِ جهت است**: «۹ روز» در گروهِ عقب‌افتاده یعنی نه روز **گذشته** و همان عدد در
 * گروهِ پیشِ‌رو یعنی نه روز **مانده**. بی این قاعده یک عدد دو معنی می‌دهد.
 *
 * ⚠️ این تب **صفحه‌ی کنش نیست، صفحه‌ی دیدن است** - دکمه‌ی «پرداخت شد» ندارد. پرداخت در
 * مقصد ثبت می‌شود تا عکسِ رسید و تاریخِ واقعی هم بگیرد.
 */
@Composable
private fun DueRowCard(
    row: DueListViewModel.DueRow,
    privacyMode: Boolean,
    onOpen: () -> Unit,
) {
    val overdue = row.daysOverdue > 0
    val shape = RoundedCornerShape(16.dp)
    val icon = when (row.kind) {
        DueListViewModel.DueSource.LOAN -> Icons.Filled.CreditCard
        DueListViewModel.DueSource.CHEQUE -> Icons.Filled.Receipt
        DueListViewModel.DueSource.RECURRING -> Icons.Filled.Autorenew
        DueListViewModel.DueSource.DEBT -> Icons.Filled.Schedule
    }
    val dayLabel = when {
        overdue -> "${toFa(row.daysOverdue)} روز"
        row.daysOverdue == 0 -> "امروز"
        row.daysOverdue == -1 -> "فردا"
        else -> "${toFa(-row.daysOverdue)} روز"
    }
    val dayInk = if (overdue || row.daysOverdue == 0) AppDangerInk else AppWarning
    val dayBg = if (overdue || row.daysOverdue == 0) AppDangerPill else AppWarningPill
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppSurface)
            .border(if (overdue) 2.dp else 2.dp, if (overdue) AppUrgentBorder else AppLineRow, shape)
            .pressScaleClickable(onClick = onOpen)
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) {
        // تفاوتِ نوعِ تعهد از **آیکون** می‌آید نه از رنگ - همان تصمیمِ نشان‌ها.
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(AppIconFrame),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = AppMuted, modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    row.title,
                    color = AppText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                )
                Text(
                    dayLabel,
                    color = dayInk,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(dayBg)
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                )
            }
            Text(
                "سررسید ${toFa(row.date.d)} ${persianMonthName(row.date.m)} · ${row.subtitle}",
                color = AppMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                maskIfPrivate(masked, amountToman(row.amount)),
                color = AppText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
            )
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

private val DueGreen: Color
    @Composable get() = AppPrimary
private val DueGreenDeep: Color
    @Composable get() = AppPrimaryDim
private val OverdueInk: Color
    @Composable get() = AppDangerInk
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