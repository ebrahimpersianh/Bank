package ir.sadteam.loancalc.ui.accounting

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.findCategory
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.category.CategoryViewModel
import androidx.activity.compose.BackHandler
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.HeroSmallPill
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.HeroTone
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.CategoryDonut
import ir.sadteam.loancalc.ui.components.DonutSlice
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.InlineJalaliDateRow
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.components.SegmentedToggle
import ir.sadteam.loancalc.ui.components.StaggerIn
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import androidx.compose.material.icons.filled.Assessment
import ir.sadteam.loancalc.ui.stats.StatsScreen
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppInfoPill
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** ردیفِ راهنمای رنگیِ کارتِ اختلافِ دخل/خرج - مربعِ ۸.dp + برچسب + مبلغ. */
@Composable
private fun IncomeExpenseLegendRow(color: Color, label: String, amount: Double) {
    val privacyMode = LocalPrivacyMode.current
    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(start = 6.dp).weight(1f))
        // 🐛 قبلاً **مقدارِ خامِ ریال با برچسبِ «ریال»** بود و از حالتِ خصوصی هم رد نمی‌شد -
        // همان دو اشتباهی که در صفحه‌ی آمارِ وام هم پیدا شد (دورِ ۹).
        PrivacyCrossfade(privacyMode) { masked ->
            Text(
                "${maskIfPrivate(masked, fmt(rialToToman(amount.toLong()).toDouble()))} تومان",
                color = AppText,
                fontSize = 11.sp,
            )
        }
    }
}
/**
 * ناوبرِ تاریخِ تبِ گزارش - خواسته‌ی صریحِ کاربر («تقویم رو منحصربه‌فرد کن و سوپرایزم کن») بعدِ
 * رفعِ باگِ PersianCalendar.addDays (رجوع کن به CLAUDE.md). به‌جای دو فلشِ ساده‌ی کنارِ یه متنِ
 * ثابت، یه نوارِ هفتگیِ تعاملی: متنِ تاریخِ کامل با اسلایدِ افقیِ هم‌جهت با حرکت (AnimatedContent)،
 * و زیرش ۷ کپسولِ روزِ قابل‌تپ (سه روزِ قبل تا سه روزِ بعد، وسطی = روزِ انتخاب‌شده) که با یه تپ
 * مستقیم می‌شه به هر کدوم پرید - نه فقط قدم‌به‌قدم.
 */
@Composable
private fun DateRibbonHeader(viewDate: PersianDate, onDateChange: (PersianDate) -> Unit) {
    var previousOrdinal by remember { mutableStateOf(viewDate.ordinal()) }
    val goingForward = viewDate.ordinal() >= previousOrdinal
    SideEffect { previousOrdinal = viewDate.ordinal() }
    // ⚠️ نوارِ تاریخ عمداً **کارتِ قهرمان نیست** - یه ناوبرِ ابزاریه، نه عددِ اصلیِ صفحه. طبقِ
    // قاعده‌ی «حداکثر یک رنگِ لهجه در هر صفحه» گرادیانِ سبزش برداشته شد و کارتِ سفیدِ معمولی شد.
    AppCard {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            // سرتیتر: فقط نامِ ماه (نه تاریخِ کاملِ روز) بینِ دو فلشِ دایره‌ای - چیدمانِ هم‌الگو با
            // ناوبرِ ماهِ تبِ بودجه.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DateRibbonArrow(icon = Icons.Filled.ChevronRight, contentDescription = "روزِ قبل") {
                    onDateChange(PersianCalendar.addDays(viewDate, -1))
                }
                AnimatedContent(
                    targetState = viewDate,
                    transitionSpec = {
                        val dir = if (goingForward) 1 else -1
                        (slideInHorizontally(tween(220)) { w -> dir * w } + fadeIn(tween(220))) togetherWith
                            (slideOutHorizontally(tween(220)) { w -> -dir * w } + fadeOut(tween(220)))
                    },
                    label = "reportMonthText",
                ) { d ->
                    Text(
                        "${faMonthNamesAccounting[d.m - 1]} ${toFa(d.y)}",
                        color = AppText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
                DateRibbonArrow(icon = Icons.Filled.ChevronLeft, contentDescription = "روزِ بعد") {
                    onDateChange(PersianCalendar.addDays(viewDate, 1))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (-3..3).forEach { offset ->
                    val d = PersianCalendar.addDays(viewDate, offset)
                    val selected = offset == 0
                    val weekDayShort = faWeekDayNamesAccounting[JalaliCalendar.dayOfWeekSaturdayFirst(d)].take(1)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(if (selected) 1.25f else 1f)
                            .clip(RoundedCornerShape(AppRadius.icon))
                            .background(if (selected) AppPrimary else AppPrimary.copy(alpha = 0.10f))
                            .pressScaleClickable(onClick = { onDateChange(d) })
                            .padding(vertical = 6.dp),
                    ) {
                        Text(
                            weekDayShort,
                            color = if (selected) Color.Black else AppMuted,
                            fontSize = 10.sp,
                        )
                        Text(
                            toFa(d.d),
                            color = if (selected) Color.Black else AppText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 1.dp),
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun DateRibbonArrow(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(28.dp)
            .clip(CircleShape)
            .background(AppPrimaryPill)
            .pressScaleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = AppPrimary, modifier = Modifier.size(15.dp))
    }
}
/**
 * بازطراحیِ کاملِ تبِ گزارش (خواسته‌ی صریحِ کاربر با اسکرین‌شاتِ رفرنسِ Poolaki - «کل صفحه کن تاریخ
 * هست که»): بالای صفحه یه ناوبرِ روزانه، توگلِ دخل/خرج، کارتِ اختلاف+تعدادِ تراکنش، مبلغِ روزِ جاری با
 * اختلاف نسبت به دیروز، نمودارِ میله‌ایِ ۷روزه، و تفکیکِ دسته‌بندیِ همون روز («پولم کجا خرج شده؟»).
 * فیلترِ بازه‌ی دلخواه + خروجیِ PDF/اکسلِ قبلی (رجوع کن به CLAUDE.md، «تکمیلِ گزارش‌گیری») عمداً حذف
 * نشد - پشتِ یه دکمه‌ی «گزارشِ سفارشی و خروجی» جمع شد تا هم نمای روزانه‌ی جدید هم قابلیتِ قبلی بمونه.
 */
/**
 * کارتِ قهرمانِ تبِ گزارش - کارتِ `26a`ی فایلِ طراحی.
 *
 * **بنفش** (`#A56EFF → #7440C9`، سایه‌ی `#5C2FA8`) طبقِ توکنِ «بنفش = بودجه و آمار». مقادیرِ
 * دقیقِ طرح: برچسبِ ۱۰/۷۰۰ سفیدِ ۸۰٪ · عددِ ۲۶/۹۰۰ · قرصِ تغییر ۹٫۵/۹۰۰ · نمودارِ ۷ ماهه با
 * ارتفاعِ ۴۰ و فاصله‌ی ۳ (میله‌های گذشته سفیدِ ۳۰٪، ماهِ جاری سفیدِ توپر) · برچسب‌های ۸٫۵.
 */
@Composable
private fun ReportMonthHero(
    monthLabel: String,
    thisMonthSpend: Double,
    prevMonthSpend: Double,
    monthlySpend: List<Double>,
    firstMonthLabel: String,
    privacyMode: Boolean,
) {
    val deltaPercent: Int? = if (prevMonthSpend > 0.0) {
        (((thisMonthSpend - prevMonthSpend) / prevMonthSpend) * 100).toInt()
    } else {
        null
    }
    val max = monthlySpend.maxOrNull()?.takeIf { it > 0.0 } ?: 1.0

    AppHeroCard(tone = HeroTone.PURPLE) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text("خرجِ $monthLabel", color = HeroMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        maskIfPrivate(masked, fmt(thisMonthSpend)),
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
            if (deltaPercent != null) {
                HeroSmallPill(
                    (if (deltaPercent > 0) "▲ " else "▼ ") +
                        toFa(kotlin.math.abs(deltaPercent)) + "٪ نسبت به ماهِ قبل",
                )
            }
        }
        // ⚠️ همون حالتِ خالیِ نمودارِ خانه (رجوع کن به HomeSevenDayChart): بدونِ هیچ خرجی،
        // هفت میله ارتفاعِ صفر می‌گیرن و یه نوارِ ۴۰ پیکسلیِ خالی مثلِ سوراخ وسطِ کارت می‌مونه.
        if (monthlySpend.none { it > 0.0 }) {
            Text(
                "هنوز خرجی ثبت نکردی - با اولین تراکنش، روندِ ماه‌ها همین‌جا ساخته می‌شه",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 18.sp,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            )
            return@AppHeroCard
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .height(40.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            monthlySpend.forEachIndexed { index, value ->
                val isCurrent = index == monthlySpend.lastIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight((value / max).toFloat().coerceIn(0.06f, 1f))
                        .background(
                            color = if (isCurrent) Color.White else Color.White.copy(alpha = 0.30f),
                            shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp),
                        ),
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(firstMonthLabel, color = Color.White.copy(alpha = 0.85f), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
            Text(monthLabel, color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
@Composable
internal fun ReportSection(viewModel: AccountViewModel, categoryViewModel: CategoryViewModel) {
    val accounts by viewModel.accounts.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()
    val today = remember { JalaliCalendar.today() }
    // مبلغِ وسطِ نمودارِ دونات هم باید تو حالتِ خصوصی مخفی بشه، مثلِ بقیه‌ی مبلغ‌های اپ.
    val privacyMode = LocalPrivacyMode.current
    // برای رنگِ تکه‌های نمودارِ دونات - دسته‌های ساخته‌ی خودِ کاربر هم رنگِ درستشون رو بگیرن،
    // نه فقط دسته‌های ثابتِ findCategory.
    val reportExpenseCats by categoryViewModel.expenseCategories.collectAsState()
    val reportIncomeCats by categoryViewModel.incomeCategories.collectAsState()
    val allCategoryEntries = remember(reportExpenseCats, reportIncomeCats) { reportExpenseCats + reportIncomeCats }

    var viewDate by remember { mutableStateOf(today) }
    var showExpenseTab by remember { mutableStateOf(true) }
    var showCustomReport by remember { mutableStateOf(false) }
    var showLoanStats by remember { mutableStateOf(false) }

    fun txOn(date: PersianDate) = allTransactions.filter { it.year == date.y && it.month == date.m && it.day == date.d }

    val activeType = if (showExpenseTab) TransactionType.WITHDRAWAL else TransactionType.DEPOSIT
    val dayTx = remember(allTransactions, viewDate) { txOn(viewDate) }
    val dayIncome = remember(dayTx) { dayTx.filter { it.type == TransactionType.DEPOSIT.name }.sumOf { it.amount } }
    val dayExpense = remember(dayTx) { dayTx.filter { it.type == TransactionType.WITHDRAWAL.name }.sumOf { it.amount } }
    val activeAmount = if (showExpenseTab) dayExpense else dayIncome
    val incomeFraction = remember(dayIncome, dayExpense) {
        val sum = dayIncome + dayExpense
        if (sum > 0) (dayIncome / sum).toFloat() else 0.5f
    }

    val prevDate = remember(viewDate) { PersianCalendar.addDays(viewDate, -1) }
    val prevActiveAmount = remember(allTransactions, prevDate, showExpenseTab) {
        txOn(prevDate).filter { it.type == activeType.name }.sumOf { it.amount }
    }
    val diffFromPrev = activeAmount - prevActiveAmount

    // ── کارتِ قهرمانِ بنفشِ بالای گزارش (کارتِ `26a`ی طرح) ───────────────────────────
    // خرجِ ۷ ماهِ اخیر (قدیمی‌ترین → ماهِ جاری) برای نمودارِ میله‌ای، و مقایسه‌ی ماهِ جاری با
    // ماهِ قبل. داده‌ی جدیدی لازم نیست - از همون تراکنش‌های موجود.
    val last7Months = remember(today) {
        (6 downTo 0).map { back ->
            var y = today.y
            var m = today.m - back
            while (m <= 0) { m += 12; y -= 1 }
            y to m
        }
    }
    val monthlySpend = remember(allTransactions, last7Months) {
        last7Months.map { (y, m) ->
            allTransactions
                .filter { it.type == TransactionType.WITHDRAWAL.name && it.year == y && it.month == m }
                .sumOf { it.amount }
        }
    }
    val thisMonthSpend = monthlySpend.last()
    val prevMonthSpend = monthlySpend[monthlySpend.lastIndex - 1]

    val last7Days = remember(viewDate) { (0..6).map { PersianCalendar.addDays(viewDate, -it) }.reversed() }
    val chartValues = remember(allTransactions, last7Days, showExpenseTab) {
        last7Days.map { d -> d to txOn(d).filter { it.type == activeType.name }.sumOf { it.amount } }
    }
    val maxChartValue = remember(chartValues) { chartValues.maxOfOrNull { it.second } ?: 0.0 }

    val dayCategoryBreakdown = remember(dayTx, showExpenseTab) {
        dayTx.filter { it.type == activeType.name }
            .groupBy { it.category ?: "سایر" }
            .map { (name, txs) -> name to txs.sumOf { it.amount } }
            .sortedByDescending { it.second }
    }

    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var fromYear by remember { mutableStateOf(today.y) }
    var fromMonth by remember { mutableStateOf(today.m) }
    var fromDay by remember { mutableStateOf(1) }
    var toYear by remember { mutableStateOf(today.y) }
    var toMonth by remember { mutableStateOf(today.m) }
    var toDay by remember { mutableStateOf(today.d) }

    fun dateKey(y: Int, m: Int, d: Int) = y * 10000 + m * 100 + d

    val filtered = remember(allTransactions, selectedAccountId, fromYear, fromMonth, fromDay, toYear, toMonth, toDay) {
        val from = dateKey(fromYear, fromMonth, fromDay)
        val to = dateKey(toYear, toMonth, toDay)
        allTransactions.filter { tx ->
            (selectedAccountId == null || tx.accountId == selectedAccountId) &&
                dateKey(tx.year, tx.month, tx.day) in minOf(from, to)..maxOf(from, to)
        }.sortedWith(compareBy({ it.year }, { it.month }, { it.day }))
    }
    val income = remember(filtered) { filtered.filter { it.type == TransactionType.DEPOSIT.name }.sumOf { it.amount } }
    val expense = remember(filtered) { filtered.filter { it.type == TransactionType.WITHDRAWAL.name }.sumOf { it.amount } }
    val categoryBreakdown = remember(filtered) {
        filtered.groupBy { it.category ?: "بدونِ دسته" }
            .map { (name, txs) -> name to txs.sumOf { it.amount } }
            .sortedByDescending { it.second }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val banner = rememberInAppBanner()
    val accountLabel = accounts.firstOrNull { it.id == selectedAccountId }?.name ?: "همه‌ی حساب‌ها"
    val rangeLabel = "${toFa(fromYear)}/${toFa(fromMonth)}/${toFa(fromDay)} تا ${toFa(toYear)}/${toFa(toMonth)}/${toFa(toDay)}"

    val createPdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf"),
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val ok = runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        AccountingPdfExporter.export(rangeLabel, accountLabel, income, expense, categoryBreakdown, filtered, out)
                    }
                }.isSuccess
                withContext(Dispatchers.Main) {
                    banner.show(if (ok) "PDF ذخیره شد" else "ذخیره‌ی PDF ناموفق بود", isSuccess = ok)
                }
            }
        }
    }
    val createXlsxLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val ok = runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        AccountingXlsxExporter.export(income, expense, categoryBreakdown, filtered, out)
                    }
                }.isSuccess
                withContext(Dispatchers.Main) {
                    banner.show(if (ok) "اکسل ذخیره شد" else "ذخیره‌ی اکسل ناموفق بود", isSuccess = ok)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp, 14.dp, 14.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            StaggerIn(0) {
                ReportMonthHero(
                    monthLabel = faMonthNamesAccounting[today.m - 1],
                    thisMonthSpend = thisMonthSpend,
                    prevMonthSpend = prevMonthSpend,
                    monthlySpend = monthlySpend,
                    firstMonthLabel = faMonthNamesAccounting[last7Months.first().second - 1],
                    privacyMode = privacyMode,
                )
            }
        }
        item {
            StaggerIn(1) {
                DateRibbonHeader(viewDate = viewDate, onDateChange = { viewDate = it })
            }
        }
        item {
            StaggerIn(1) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SegmentedToggle(
                        options = listOf("دخل", "خرج"),
                        selectedIndex = if (showExpenseTab) 1 else 0,
                        onSelect = { index -> showExpenseTab = index == 1 },
                        selectedColor = if (showExpenseTab) AppDanger else AppPrimary,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(4.dp).clip(CircleShape),
                    ) {
                        Box(modifier = Modifier.weight(incomeFraction.coerceIn(0.02f, 0.98f)).fillMaxHeight().background(AppPrimary))
                        Box(modifier = Modifier.weight((1f - incomeFraction).coerceIn(0.02f, 0.98f)).fillMaxHeight().background(AppDanger))
                    }
                }
            }
        }
        item {
            StaggerIn(2) {
                val net = dayIncome - dayExpense
                AppCard {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            AppChip(label = "${toFa(dayTx.size)} تراکنش", selected = false, onClick = {})
                            Text(
                                "${if (net < 0) "-" else ""}${fmt((kotlin.math.abs(net)) / 10)} تومان",
                                color = if (net >= 0) AppPrimary else AppDanger,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(top = 10.dp),
                            )
                            Text("اختلاف دخل و خرج", color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            IncomeExpenseLegendRow(color = AppPrimary, label = "دخل", amount = dayIncome)
                            IncomeExpenseLegendRow(color = AppDanger, label = "خرج", amount = dayExpense)
                        }
                        // سهمِ دخل روی مسیرِ قرمز (خرج) - همون CategoryDonut/Canvas+Animatableِ موجودِ
                        // StatsScreen، بدونِ کتابخونه‌ی نموداریِ جدید.
                        CategoryDonut(
                            slices = listOf(DonutSlice(dayIncome, AppPrimary)),
                            size = 88.dp,
                            strokeWidth = 14.dp,
                            trackColor = AppDanger.copy(alpha = 0.28f),
                            gapDegrees = 0f,
                        )
                    }
                }
            }
        }
        item {
            StaggerIn(3) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "${if (showExpenseTab) "خرج" else "دخل"} ${toFa(viewDate.d)} ${faMonthNamesAccounting[viewDate.m - 1]} ${toFa(viewDate.y)}",
                        color = AppMuted,
                        fontSize = 12.sp,
                    )
                    // 🐛 همان باگِ ۱۰برابر: ریالِ خام با برچسبِ «ریال»، و بی ماسکِ حالتِ خصوصی.
                    PrivacyCrossfade(privacyMode) { masked ->
                        Text(
                            "${maskIfPrivate(masked, fmt(rialToToman(activeAmount.toLong()).toDouble()))} تومان",
                            color = AppText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    if (diffFromPrev != 0.0) {
                        PrivacyCrossfade(privacyMode) { masked ->
                            Text(
                                "${if (diffFromPrev > 0) "↗" else "↘"} " +
                                    maskIfPrivate(masked, fmt(rialToToman(kotlin.math.abs(diffFromPrev).toLong()).toDouble())) +
                                    " تومان اختلاف با روز قبل",
                                color = AppMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                }
            }
        }
        item {
            StaggerIn(4) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    chartValues.forEach { (d, amount) ->
                        val fraction = if (maxChartValue > 0) (amount / maxChartValue).toFloat().coerceIn(0f, 1f) else 0f
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((100 * fraction.coerceAtLeast(0.02f)).dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (d == viewDate) (if (showExpenseTab) AppDanger else AppPrimary) else AppSurface2),
                            )
                            Text(
                                "${toFa(d.d)} ${faMonthNamesAccounting[d.m - 1].take(3)}",
                                color = AppMuted,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
        }
        if (dayCategoryBreakdown.isNotEmpty()) {
            item {
                StaggerIn(4) {
                    // نمودارِ دوناتِ سهمِ دسته‌ها + راهنمای رنگی زیرش (خواسته‌ی صریحِ کاربر، بستهٔ
                    // ارتقاهای گرافیکی). قبلاً فقط یه لیستِ متنیِ ساده بود.
                    val breakdownTotal = remember(dayCategoryBreakdown) { dayCategoryBreakdown.sumOf { it.second } }
                    // رنگِ هر دسته از خودِ تعریفِ دسته میاد تا با آیکون‌های رنگیِ بقیه‌ی اپ یکی باشه؛
                    // دسته‌ی ناشناس (مثلاً «سایر») رنگِ خنثی می‌گیره.
                    val fallbackSliceColor = AppMuted
                    val donutSlices = remember(dayCategoryBreakdown, allCategoryEntries) {
                        dayCategoryBreakdown.map { (name, amount) ->
                            val c = allCategoryEntries.find { it.name == name }?.color
                                ?: findCategory(name)?.color
                                ?: fallbackSliceColor
                            DonutSlice(value = amount, color = c)
                        }
                    }
                    AppCard(label = if (showExpenseTab) "پولم کجا خرج شده؟" else "درآمدم از کجا اومده؟") {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CategoryDonut(slices = donutSlices) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        if (showExpenseTab) "کلِ خرج" else "کلِ دخل",
                                        color = AppMuted,
                                        fontSize = 10.sp,
                                    )
                                    PrivacyCrossfade(privacyMode) { masked ->
                                        Text(
                                            maskIfPrivate(masked, fmt(breakdownTotal / 10)),
                                            color = AppText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                    Text("تومان", color = AppMuted, fontSize = 9.sp)
                                }
                            }
                        }
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            dayCategoryBreakdown.forEachIndexed { index, (name, amount) ->
                                val share = if (breakdownTotal > 0) (amount / breakdownTotal * 100).toInt() else 0
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(donutSlices[index].color),
                                        )
                                        Text(
                                            name,
                                            color = AppText,
                                            fontSize = 12.5.sp,
                                            modifier = Modifier.padding(start = 8.dp),
                                        )
                                        Text(
                                            "٪${toFa(share)}",
                                            color = AppMuted,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(start = 6.dp),
                                        )
                                    }
                                    Text("${fmt((amount) / 10)} تومان", color = AppMuted, fontSize = 12.5.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
        // **آمارِ وام‌ها از تبِ وام به این‌جا آمد** (خواسته‌ی صریحِ کاربر، دورِ ۱۲).
        // جایش این‌جا درست‌تر است: زبانش زبانِ گزارش است (نمودار، روند، خروجیِ PDF) و
        // در تبِ وام یک ردیفِ کاملِ بالای فهرست را می‌خورد، جایی که کاربر آمده وام‌هایش
        // را ببیند نه نمودارشان را.
        item {
            AppCard(
                modifier = Modifier.pressScaleClickable(onClick = { showLoanStats = true }),
                borderColor = AppInfo.copy(alpha = 0.26f),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(AppRadius.icon)).background(AppInfoPill),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Assessment, contentDescription = null, tint = AppInfo, modifier = Modifier.size(17.dp))
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                        Text("آمارِ وام‌ها", color = AppText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("پیشرفتِ پرداخت · سود · خروجیِ PDF", color = AppMuted, fontSize = 11.sp)
                    }
                    Text("‹", color = AppMuted, fontSize = 15.sp)
                }
            }
        }
        item {
            // دکمه‌ی متنیِ قبلی به کارتِ آبی تبدیل شد (AppInfo، ابزار نه پول) - تپ همون
            // showCustomReport = !showCustomReport، بدونِ تغییرِ منطق/خروجی‌گیرها.
            AppCard(
                modifier = Modifier.pressScaleClickable(onClick = { showCustomReport = !showCustomReport }),
                borderColor = AppInfo.copy(alpha = 0.26f),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(AppRadius.icon)).background(AppInfoPill),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.PieChart, contentDescription = null, tint = AppInfo, modifier = Modifier.size(17.dp))
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                        Text(
                            if (showCustomReport) "بستنِ گزارشِ سفارشی" else "گزارشِ سفارشی و خروجی",
                            color = AppText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text("بازه‌ی دلخواه · PDF و اکسل", color = AppMuted, fontSize = 11.sp)
                    }
                    Text("‹", color = AppMuted, fontSize = 15.sp)
                }
            }
        }
        if (showCustomReport) {
        item {
            AppCard(label = "حساب‌کتاب") {
                AccountingDropdown(
                    options = listOf<Pair<Long?, String>>(null to "همه‌ی حساب‌ها") + accounts.map { it.id to it.name },
                    selected = selectedAccountId,
                    onSelect = { selectedAccountId = it },
                )
            }
        }
        item {
            AppCard(label = "از تاریخ") {
                InlineJalaliDateRow(
                    year = fromYear,
                    month = fromMonth,
                    day = fromDay,
                    onDateChange = { y, m, d -> fromYear = y; fromMonth = m; fromDay = d },
                )
            }
        }
        item {
            AppCard(label = "تا تاریخ") {
                InlineJalaliDateRow(
                    year = toYear,
                    month = toMonth,
                    day = toDay,
                    onDateChange = { y, m, d -> toYear = y; toMonth = m; toDay = d },
                )
            }
        }
        item {
            AppCard(label = "خلاصه‌ی بازه") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("درآمد", color = AppMuted, fontSize = 12.sp)
                        Text("${fmt((income) / 10)} تومان", color = AppPrimary, fontSize = 14.sp)
                    }
                    Column {
                        Text("هزینه", color = AppMuted, fontSize = 12.sp)
                        Text("${fmt((expense) / 10)} تومان", color = AppDanger, fontSize = 14.sp)
                    }
                    Column {
                        Text("مانده", color = AppMuted, fontSize = 12.sp)
                        Text(
                            "${fmt((income - expense) / 10)} تومان",
                            color = if (income - expense >= 0) AppText else AppDanger,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
        if (categoryBreakdown.isNotEmpty()) {
            item {
                AppCard(label = "تفکیکِ دسته‌بندی") {
                    Column {
                        categoryBreakdown.forEach { (name, amount) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(name, color = AppText, fontSize = 12.5.sp)
                                Text("${fmt((amount) / 10)} تومان", color = AppMuted, fontSize = 12.5.sp)
                            }
                        }
                    }
                }
            }
        }
        item {
            val guard = ir.sadteam.loancalc.ui.subscription.premiumGuard()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { guard("export", "خروجیِ PDF و اکسل") { createPdfLauncher.launch("gozaresh-hesabdari.pdf") } },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppPrimary),
                    enabled = filtered.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                ) { Text("دانلود PDF", fontSize = 12.sp) }
                OutlinedButton(
                    onClick = { guard("export", "خروجیِ PDF و اکسل") { createXlsxLauncher.launch("gozaresh-hesabdari.xlsx") } },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppPrimary),
                    enabled = filtered.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                ) { Text("دانلود اکسل", fontSize = 12.sp) }
            }
        }
        if (filtered.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.AccountBalanceWallet,
                    title = "تراکنشی تو این بازه نیست",
                    description = "بازه یا حساب رو عوض کن تا تراکنش‌های اون بازه اینجا دیده بشه.",
                )
            }
        }
        }
    }
    // صفحه‌ی آمارِ وام به‌عنوانِ پوششِ تمام‌صفحه روی همین تب - `LazyColumn` دارد پس
    // داخلِ فهرستِ تنبلِ بالا نمی‌رود (همان قاعده‌ی کرشِ اسکرولِ تودرتو).
    if (showLoanStats) {
        BackHandler { showLoanStats = false }
        Box(modifier = Modifier.fillMaxSize().background(AppBg)) {
            StatsScreen(onBack = { showLoanStats = false })
        }
    }
    InAppBannerHost(banner)
    }
}
