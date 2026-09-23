package ir.sadteam.loancalc.ui.accounting

import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import ir.sadteam.loancalc.ui.components.InteractiveBars
import ir.sadteam.loancalc.ui.components.HeroIncome
import ir.sadteam.loancalc.ui.components.HeroExpense
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.AbsoluteAlignment
import ir.sadteam.loancalc.ui.components.ChartTooltipHost
import ir.sadteam.loancalc.ui.components.ChartTooltip
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianCalendar
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.CategoryEntry
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.category.CategoryViewModel
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.CoinIcon
import ir.sadteam.loancalc.ui.components.UndoBar
import ir.sadteam.loancalc.ui.components.dashedBorder
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppGoldInkSoft
import ir.sadteam.loancalc.ui.theme.AppGoldPillSoft
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryBorder
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppPrimaryInkLight
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppWarning
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import ir.sadteam.loancalc.ui.theme.AppWarningPill
import ir.sadteam.loancalc.ui.theme.hardShadow
import kotlin.math.roundToLong

/**
 * تبِ **بودجه** - بازسازیِ کاملِ فریمِ `27c`.
 *
 * ```
 * ۱ هدر: عنوان ۱۸/۹۰۰ + دکمه‌ی «+»ِ ۳۲×۳۲
 * ۲ هیرویِ سبز: «امروز می‌توانی خرج کنی» + ۷ میله‌ی روزهای هفته + خطِ ذخیره
 * ۳ کارتِ «کلِ ماه»: نوارِ ۱۴ پیکسلی با سکه‌ی طلاییِ سرِ نوار + پیش‌بینیِ آخرِ ماه
 * ۴ ردیفِ هر دسته: سقف/خرج + قرصِ درصد + نوارِ ۹ پیکسلی (ردشده = هاشورِ مورب)
 * ۵ کارتِ نارنجی: پیشنهادِ جابه‌جاییِ بودجه بینِ دو دسته
 * ```
 *
 * ⚠️ فریم ناوبرِ ماه نداره - عمداً حذف شد و صفحه فقط **ماهِ جاری** رو نشون می‌ده (سقفِ بودجه
 * ذاتاً ماهانه‌ست). اگه کاربر فلش‌های ماه رو خواست، برگردوندنش ساده‌ست.
 */
@Composable
fun BudgetTabScreen(
    onOpenCategories: () -> Unit,
    onOpenRecurring: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
) {
    val budgets by viewModel.budgets.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val recurringPayments by viewModel.recurringPayments.collectAsState()
    val expenseCats by categoryViewModel.expenseCategories.collectAsState()
    val privacyMode = LocalPrivacyMode.current
    val today = remember { JalaliCalendar.today() }

    var showAddBudget by remember { mutableStateOf(false) }
    var suggestionCategory by remember { mutableStateOf<CategoryEntry?>(null) }

    val spend = remember(allTransactions, today) {
        viewModel.spendByCategory(allTransactions, today.y, today.m)
    }
    val budgetedCats = remember(expenseCats, budgets) {
        expenseCats.filter { cat -> budgets.any { it.categoryName == cat.name } }
    }
    val unbudgetedCats = remember(expenseCats, budgetedCats) { expenseCats - budgetedCats.toSet() }

    val rows = remember(budgetedCats, budgets, spend) {
        budgetedCats.map { cat ->
            BudgetRowData(
                category = cat,
                cap = budgets.first { it.categoryName == cat.name }.monthlyCap,
                spent = spend[cat.name] ?: 0.0,
            )
        }
    }
    val totalCap = rows.sumOf { it.cap }
    val totalSpent = rows.sumOf { it.spent }

    val daysInMonth = remember(today) { JalaliCalendar.daysInMonth(today.y, today.m) }
    val daysLeft = (daysInMonth - today.d + 1).coerceAtLeast(1)
    val dailyAllowance = if (totalCap > 0) ((totalCap - totalSpent) / daysLeft).coerceAtLeast(0.0) else 0.0
    /** سهمِ منصفانه‌ی هر روز - مبنای میله‌های هفته و عددِ «ذخیره». */
    val fairShare = if (totalCap > 0) totalCap / daysInMonth else 0.0
    val weekUnderShare = remember(allTransactions, today, fairShare) {
        if (fairShare <= 0.0) emptyList() else (6 downTo 0).map { back ->
            val d = PersianCalendar.addDays(today, -back)
            allTransactions
                .filter { it.type == TransactionType.WITHDRAWAL.name && it.year == d.y && it.month == d.m && it.day == d.d }
                .sumOf { it.amount } <= fairShare
        }
    }
    /** خرجِ هر یک از هفت روزِ اخیر (قدیمی→امروز) - برای حبابِ لمسِ نوارِ هفته. */
    val weekSpent = remember(allTransactions, today) {
        (6 downTo 0).map { back ->
            val d = PersianCalendar.addDays(today, -back)
            allTransactions
                .filter { it.type == TransactionType.WITHDRAWAL.name && it.year == d.y && it.month == d.m && it.day == d.d }
                .sumOf { it.amount }
        }
    }
    // خرجِ هر روزِ همین ماه (نمودارِ میله‌ایِ کارتِ بالا) + درآمد و خرجِ کلِ ماه (دو باکسِ کنارش).
    val monthDailySpent = remember(allTransactions, today, daysInMonth) {
        (1..daysInMonth).map { day ->
            allTransactions
                .filter { it.type == TransactionType.WITHDRAWAL.name && it.year == today.y && it.month == today.m && it.day == day }
                .sumOf { it.amount }
        }
    }
    val monthIncome = remember(allTransactions, today) {
        allTransactions
            .filter { it.type == TransactionType.DEPOSIT.name && it.year == today.y && it.month == today.m }
            .sumOf { it.amount }
    }
    val monthExpense = remember(monthDailySpent) { monthDailySpent.sum() }
    val savedSoFar = if (fairShare > 0) (fairShare * today.d - totalSpent).coerceAtLeast(0.0) else 0.0
    /** پیش‌بینیِ سرِ ماه با همین سرعتِ خرج - خطِ طلاییِ کارتِ «کلِ ماه». */
    val projectedLeft = if (today.d > 0 && totalCap > 0) {
        (totalCap - totalSpent / today.d * daysInMonth).coerceAtLeast(0.0)
    } else {
        0.0
    }
    val transfer = remember(rows) { suggestTransfer(rows) }
    // مقادیرِ قبل از «قرض‌دادنِ بودجه»، برای نوارِ واگرد. `null` یعنی چیزی برای برگرداندن نیست.
    var undoTransfer by remember { mutableStateOf<UndoableTransfer?>(null) }

    // پیشنهادِ حالتِ خالی (فریمِ `21d`): دو دسته‌ی پرخرجِ **ماهِ قبل**. اگه تاریخچه‌ای نباشه،
    // خودِ فریم می‌گه این بخش اصلاً نشون داده نمی‌شه.
    val prevMonth = remember(today) { if (today.m == 1) 12 to today.y - 1 else today.m - 1 to today.y }
    val lastMonthSpend = remember(allTransactions, prevMonth) {
        viewModel.spendByCategory(allTransactions, prevMonth.second, prevMonth.first)
    }
    val starterSuggestions = remember(lastMonthSpend, expenseCats) {
        expenseCats
            .mapNotNull { cat -> (lastMonthSpend[cat.name] ?: 0.0).takeIf { it > 0 }?.let { cat to it } }
            .sortedByDescending { it.second }
            .take(2)
            .map { (cat, spent) -> BudgetStarter(cat, spent, suggestedCap(spent)) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 110.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { BudgetHeader(onAdd = { showAddBudget = true }, showAdd = rows.isNotEmpty()) }
            if (rows.isEmpty()) {
                item { NoBudgetCard(onCreate = { showAddBudget = true }) }
                if (starterSuggestions.isNotEmpty()) {
                    item {
                        StarterSuggestions(
                            starters = starterSuggestions,
                            privacyMode = privacyMode,
                            onAcceptAll = {
                                starterSuggestions.forEach { viewModel.setBudget(it.category.name, it.cap) }
                            },
                        )
                    }
                }
            } else {
                item {
                    DailyAllowanceHero(
                        allowance = dailyAllowance,
                        monthDaily = monthDailySpent,
                        monthIncome = monthIncome,
                        monthExpense = monthExpense,
                        week = weekUnderShare,
                        weekSpent = weekSpent,
                        saved = savedSoFar,
                        behind = fairShare > 0 && fairShare * today.d < totalSpent,
                        dayOfMonth = today.d,
                        daysInMonth = daysInMonth,
                        daysLeft = daysLeft,
                        privacyMode = privacyMode,
                    )
                }
            }
            if (totalCap > 0) {
                item {
                    MonthTotalCard(
                        cap = totalCap,
                        spent = totalSpent,
                        percent = ((totalSpent / totalCap) * 100).roundToLong().toInt(),
                        fraction = (totalSpent / totalCap).toFloat(),
                        projectedLeft = projectedLeft,
                        privacyMode = privacyMode,
                    )
                }
            }
            items(rows, key = { it.category.name }) { row ->
                CategoryBudgetRow(row, privacyMode)
            }
            transfer?.let { t ->
                item {
                    TransferSuggestionCard(
                        text = "بودجه‌ی ${t.to.category.name} را ${(t.amount).rialToFaCompact()} تومان از " +
                            "${t.from.category.name} قرض بدهم تا ماه تراز شود؟",
                        // کنشِ بازگشت‌پذیر دیالوگ نمی‌گیرد، `UndoBar` می‌گیرد - قاعده‌ی `46b`.
                        // این تپ دو بودجه را هم‌زمان عوض می‌کند، پس بی راهِ برگشت نمی‌ماند.
                        onAccept = {
                            viewModel.setBudget(
                                t.to.category.name,
                                t.to.cap + t.amount,
                                budgets.first { it.categoryName == t.to.category.name }.id,
                            )
                            viewModel.setBudget(
                                t.from.category.name,
                                t.from.cap - t.amount,
                                budgets.first { it.categoryName == t.from.category.name }.id,
                            )
                            undoTransfer = UndoableTransfer(
                                toName = t.to.category.name,
                                toCap = t.to.cap,
                                fromName = t.from.category.name,
                                fromCap = t.from.cap,
                            )
                        },
                    )
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BudgetToolCard(
                        icon = Icons.Filled.EventRepeat,
                        title = "پرداختِ تکراری",
                        subtitle = "${toFa(recurringPayments.size)} مورد",
                        onClick = onOpenRecurring,
                        modifier = Modifier.weight(1f),
                    )
                    BudgetToolCard(
                        icon = Icons.Outlined.PieChart,
                        title = "دسته‌بندی‌ها",
                        subtitle = "${toFa(expenseCats.size)} دسته",
                        onClick = onOpenCategories,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        undoTransfer?.let { undo ->
            UndoBar(
                message = "بودجه‌ها جابه‌جا شد",
                onUndo = {
                    viewModel.setBudget(
                        undo.toName,
                        undo.toCap,
                        budgets.firstOrNull { it.categoryName == undo.toName }?.id,
                    )
                    viewModel.setBudget(
                        undo.fromName,
                        undo.fromCap,
                        budgets.firstOrNull { it.categoryName == undo.fromName }?.id,
                    )
                    undoTransfer = null
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 96.dp),
            )
            // نوار خودش بعد از چند ثانیه می‌رود - همان عمرِ نوارِ واگردِ بقیه‌ی اپ.
            LaunchedEffect(undo) {
                kotlinx.coroutines.delay(6_000)
                undoTransfer = null
            }
        }
    }

    if (showAddBudget || suggestionCategory != null) {
        NewBudgetSheet(
            categories = unbudgetedCats,
            accounts = accounts,
            initialCategory = suggestionCategory,
            onDismiss = { showAddBudget = false; suggestionCategory = null },
            onSave = { cat, cap, accId ->
                viewModel.setBudget(cat.name, cap, null, accId)
                showAddBudget = false
                suggestionCategory = null
            },
        )
    }
}

/** مقادیرِ پیش از پذیرفتنِ پیشنهادِ جابه‌جاییِ بودجه - ورودیِ نوارِ واگرد. */
private data class UndoableTransfer(
    val toName: String,
    val toCap: Double,
    val fromName: String,
    val fromCap: Double,
)

data class BudgetRowData(val category: CategoryEntry, val cap: Double, val spent: Double) {
    val fraction: Float get() = if (cap > 0) (spent / cap).toFloat() else 0f
    val percent: Int get() = if (cap > 0) ((spent / cap) * 100).roundToLong().toInt() else 0
    val over: Boolean get() = spent > cap
}

/** پیشنهادِ جابه‌جاییِ بودجه: از بیشترین مازاد به بیشترین کسری. */
data class BudgetTransfer(val from: BudgetRowData, val to: BudgetRowData, val amount: Double)

/**
 * قاعده‌ی کارتِ نارنجیِ فریم - «به‌جای اینکه فقط تخلف را اعلام کند، پیشنهادِ جابه‌جایی می‌دهد».
 * دسته‌ای که ردکرده گیرنده‌ست، دسته‌ای که بیشترین مانده رو داره دهنده. مبلغ = کمترینِ
 * (کسری، نصفِ ماندهٔ دهنده) تا دهنده خودش به تنگنا نیفته.
 */
private fun suggestTransfer(rows: List<BudgetRowData>): BudgetTransfer? {
    val over = rows.filter { it.over }.maxByOrNull { it.spent - it.cap } ?: return null
    val donor = rows.filter { !it.over && it.cap - it.spent > 0 }.maxByOrNull { it.cap - it.spent } ?: return null
    val need = over.spent - over.cap
    val spare = (donor.cap - donor.spent) / 2
    val amount = minOf(need, spare)
    if (amount < 1000) return null
    return BudgetTransfer(donor, over, amount)
}

// ═══ ۱ · هدر ═══════════════════════════════════════════════════════════════════
@Composable
private fun BudgetHeader(onAdd: () -> Unit, showAdd: Boolean = true) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("بودجه", color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(
                "مدیریتِ درآمد و هزینه‌های ماهانه",
                color = AppMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (!showAdd) return@Row
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AppPrimaryPill)
                .pressScaleClickable(onClick = onAdd),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "افزودنِ بودجه",
                tint = AppPrimaryInk,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

// ═══ ۱ب · کارتِ خط‌چینِ «بودجه‌ای تعیین نشده» (فریمِ `21d`) ═════════════════════════
@Composable
private fun NoBudgetCard(onCreate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppSurface)
            .dashedBorder(20.dp)
            .padding(horizontal = 16.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // سه نوارِ خط‌چینِ خالی با یه سکه‌ی طلایی رو سرِ نوارِ اول - «شکلِ نمودارِ پیشاپیش».
        Column(
            modifier = Modifier.fillMaxWidth(0.55f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier.fillMaxWidth().height(20.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(15.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(RailTrack)
                            .dashedBorder(999.dp, width = 1.5.dp),
                    )
                    // تو RTL «سرِ نوار» سمتِ راسته - سکه نصفش بیرونِ نوار می‌شینه.
                    if (index == 0) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            CoinIcon(20.dp)
                        }
                    }
                }
            }
        }
        // ⚠️ عنوان و توضیح **یه بلوکِ واحد**ن با فاصله‌ی ۶ (مثلِ `margin-top`ی فریم)، نه دو
        // آیتمِ جدا با فاصله‌ی منفی - `Modifier.padding` عددِ منفی رو قبول نمی‌کنه و همون
        // لحظه‌ی رسم کرش می‌ده (کرشِ نسخه‌ی ۱.۰.۴۷۷: «Padding must be non-negative»).
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "بودجه‌ای تعیین نشده",
                color = AppText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Text(
                "یک سقفِ ماهانه بگذار تا جیبک بگوید امروز چقدر می‌توانی خرج کنی.",
                color = AppMuted,
                fontSize = 12.5.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            "ساختنِ بودجه",
            color = Color.White,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .hardShadow(BudgetGreenDeep, 4.dp, 999.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(BudgetGreen)
                .pressScaleClickable(onClick = onCreate)
                .padding(vertical = 15.dp),
        )
    }
}

/** یه دسته‌ی پرخرجِ ماهِ قبل با سقفِ پیشنهادی. */
data class BudgetStarter(val category: CategoryEntry, val lastMonth: Double, val cap: Double)

/**
 * سقفِ پیشنهادی از رو خرجِ ماهِ قبل - کمی **زیرِ** خودِ خرج، تا پیشنهاد یه قدمِ رو به جلو باشه
 * نه تاییدِ وضعِ موجود (فریم: ماهِ قبل ۴٫۵M → پیشنهاد ۴٫۲M). گرد می‌شه به صد هزار ریال.
 */
private fun suggestedCap(lastMonth: Double): Double {
    val step = 100_000.0
    return ((lastMonth * 0.94) / step).toLong().toDouble() * step
}

@Composable
private fun StarterSuggestions(
    starters: List<BudgetStarter>,
    privacyMode: Boolean,
    onAcceptAll: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(
            "پیشنهادِ جیبک بر پایه‌ی خرجِ ماهِ قبلت",
            color = AppMuted,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        starters.forEach { starter ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppSurface)
                    .border(2.dp, RailTrack, RoundedCornerShape(18.dp))
                    .padding(horizontal = 15.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(starter.category.color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        starter.category.icon,
                        contentDescription = null,
                        tint = starter.category.color,
                        modifier = Modifier.size(15.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        starter.category.name,
                        color = AppText,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    PrivacyCrossfade(privacyMode) { masked ->
                        Text(
                            "ماهِ قبل ${maskIfPrivate(masked, (starter.lastMonth).rialToFaCompact())} تومان",
                            color = AppMuted,
                            fontSize = 10.5.sp,
                            modifier = Modifier.padding(top = 1.dp),
                        )
                    }
                }
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        maskIfPrivate(masked, (starter.cap).rialToFaCompact()),
                        color = BudgetGreenDeep,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        }
        Text(
            if (starters.size == 1) "پذیرشِ پیشنهاد" else "پذیرشِ هر دو پیشنهاد",
            color = BudgetGreenDeep,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(999.dp))
                .background(AppSurface)
                .border(1.5.dp, BudgetGreen, RoundedCornerShape(999.dp))
                .pressScaleClickable(onClick = onAcceptAll)
                .padding(vertical = 13.dp),
        )
    }
}

// ═══ ۲ · هیرویِ سهمِ روزانه ═══════════════════════════════════════════════════════
@Composable
private fun DailyAllowanceHero(
    allowance: Double,
    monthDaily: List<Double>,
    monthIncome: Double,
    monthExpense: Double,
    week: List<Boolean>,
    weekSpent: List<Double>,
    saved: Double,
    /** خرجِ تا امروز از سهمِ منصفانه‌ی همین روزها بیشتر شده. */
    behind: Boolean,
    dayOfMonth: Int,
    daysInMonth: Int,
    daysLeft: Int,
    privacyMode: Boolean,
) {
    // چیدمانِ طرحِ ChatGPT (۲ مهر): دو قرصِ بالا، عددِ درشت راست و دو باکسِ درآمد/خرج چپ،
    // نمودارِ روزهای ماه با «امروز»، و دو خطِ پایین. `week`/`weekSpent` دیگر نمایش داده
    // نمی‌شوند - نمودارِ ماه همان خبر را کامل‌تر می‌دهد.
    AppHeroCard {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            HeroPillLabel(icon = Icons.Filled.CalendarMonth, text = "ماهِ جاری")
            Spacer(modifier = Modifier.weight(1f))
            HeroPillLabel(icon = Icons.Filled.Today, text = "روزِ ${toFa(dayOfMonth)} از ${toFa(daysInMonth)}")
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "امروز می‌توانی خرج کنی",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        maskIfPrivate(masked, allowance.rialToFaCompact()),
                        color = Color.White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Text("تومان", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(start = 10.dp)) {
                HeroMiniStat(up = true, label = "درآمدِ این ماه", value = monthIncome, privacyMode = privacyMode)
                HeroMiniStat(up = false, label = "خرجِ این ماه", value = monthExpense, privacyMode = privacyMode)
            }
        }
        if (monthDaily.isNotEmpty()) {
            InteractiveBars(
                values = monthDaily,
                labels = monthDaily.indices.map { i -> if (i + 1 == dayOfMonth) "امروز" else "${toFa(i + 1)} این ماه" },
                valueLabel = { value -> "${value.rialToFaCompact()} تومان" },
                currentIndex = (dayOfMonth - 1).coerceIn(0, monthDaily.lastIndex),
                barColor = Color.White.copy(alpha = 0.28f),
                currentBarColor = Color.White,
                tooltipBackground = Color.Black.copy(alpha = 0.35f),
                tooltipTitleColor = Color.White.copy(alpha = 0.75f),
                tooltipValueColor = Color.White,
                modifier = Modifier.padding(top = 14.dp),
                height = 44.dp,
                spacing = 1.5.dp,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${toFa(dayOfMonth)} روز از ${toFa(daysInMonth)} روز گذشته",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "تا پایانِ ماه ${toFa(daysLeft)} روز مانده",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (saved > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 3.dp)) {
                        Text(
                            "+${saved.rialToFaCompact()} ذخیره",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                        )
                        Icon(
                            Icons.Filled.Savings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(start = 5.dp).size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroPillLabel(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(horizontal = 11.dp, vertical = 6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        Text(
            text,
            color = Color.White,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

/** باکسِ کوچکِ درآمد/خرج روی کارتِ بالا - فلشِ رنگی + عدد. */
@Composable
private fun HeroMiniStat(up: Boolean, label: String, value: Double, privacyMode: Boolean) {
    val ink = if (up) HeroIncome else HeroExpense
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 9.dp, vertical = 7.dp),
    ) {
        Box(
            modifier = Modifier.size(26.dp).clip(RoundedCornerShape(9.dp)).background(Color.White.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (up) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                contentDescription = null,
                tint = ink,
                modifier = Modifier.size(15.dp),
            )
        }
        Column(modifier = Modifier.padding(start = 7.dp)) {
            Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    maskIfPrivate(masked, value.rialToFaCompact()) + " تومان",
                    color = ink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

// ═══ ۳ · کارتِ «کلِ ماه» ══════════════════════════════════════════════════════════
@Composable
private fun MonthTotalCard(
    cap: Double,
    spent: Double,
    percent: Int,
    fraction: Float,
    projectedLeft: Double,
    privacyMode: Boolean,
) {
    val over = percent > 100
    val barColor = if (over) OverInk else AppPrimary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AppSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.TrackChanges, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(20.dp))
            Text(
                "کلِ ماه",
                color = AppText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 8.dp).weight(1f),
            )
            // ماهِ ردشده قرمز، نه رنگِ «خوب».
            Text("${toFa(percent)}٪", color = barColor, fontSize = 17.sp, fontWeight = FontWeight.Black)
        }
        val clamped = fraction.coerceIn(0f, 1f)
        Box(
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp).height(22.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(999.dp)).background(RailTrack),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(clamped)
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Brush.horizontalGradient(listOf(barColor.copy(alpha = 0.45f), barColor))),
                )
            }
            // دستگیره‌ی گردِ سرِ نوار (طرحِ ChatGPT) به‌جای سکه.
            Box(modifier = Modifier.fillMaxWidth(clamped), contentAlignment = Alignment.CenterEnd) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(AppSurface)
                        .border(4.dp, barColor, CircleShape),
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(maskIfPrivate(masked, cap.rialToFaCompact()), color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Black)
                }
                Text("بودجه‌ی ماهانه", color = AppMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(maskIfPrivate(masked, spent.rialToFaCompact()), color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Black)
                }
                Text("خرج شده", color = AppMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AppIconFrame)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = GoldInk, modifier = Modifier.size(16.dp))
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    "با این روند ${maskIfPrivate(masked, projectedLeft.rialToFaCompact())} تومان تا آخرِ ماه می‌مونه.",
                    color = AppMuted,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 7.dp),
                )
            }
        }
    }
}

// ═══ ۴ · ردیفِ دسته ═════════════════════════════════════════════════════════════
@Composable
private fun CategoryBudgetRow(row: BudgetRowData, privacyMode: Boolean) {
    val tint = if (row.over) OverInk else row.category.color
    val soft = tint.copy(alpha = 0.12f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppSurface)
            .border(2.dp, CardBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(soft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    row.category.icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(14.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(row.category.name, color = AppText, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        "${maskIfPrivate(masked, row.spent.rialToFaCompact())} از " +
                            "${maskIfPrivate(masked, row.cap.rialToFaCompact())} تومان",
                        color = AppMuted,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            Text(
                "${toFa(row.percent)}٪",
                color = tint,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(soft)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        // نوارِ ۹ پیکسلی. دسته‌ی ردشده به‌جای رنگِ تخت **هاشورِ موربِ ۱۳۵ درجه** می‌گیره -
        // همون چیزی که یادداشتِ فریم صریحاً می‌خواد.
        BudgetBar(
            fraction = row.fraction,
            color = tint,
            striped = row.over,
            modifier = Modifier.fillMaxWidth().padding(top = 9.dp),
        )
    }
}

@Composable
private fun BudgetBar(fraction: Float, color: Color, striped: Boolean, modifier: Modifier = Modifier) {
    // ⚠️ توکن‌های رنگ `@Composable`ان - قبل از Canvas تو یه val محلی خونده می‌شن.
    val track = RailTrackSoft
    val dark = color.copy(alpha = 0.82f)
    Canvas(modifier = modifier.height(9.dp)) {
        val h = size.height
        val r = h / 2f
        drawRoundRect(
            color = track,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
        )
        val w = size.width * fraction.coerceIn(0f, 1f)
        if (w <= 0f) return@Canvas
        // تو RTL نوار از سمتِ راست پر می‌شه.
        val left = size.width - w
        clipRect(left = left, right = size.width) {
            drawRoundRect(
                color = color,
                topLeft = Offset(left, 0f),
                size = androidx.compose.ui.geometry.Size(w, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
            )
            if (striped) {
                // هاشورِ ۱۳۵ درجه: ۶ پیکسل روشن، ۶ پیکسل تیره.
                val step = 12.dp.toPx()
                var x = left - h
                while (x < size.width + h) {
                    drawLine(
                        color = dark,
                        start = Offset(x, h),
                        end = Offset(x + h, 0f),
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Butt,
                    )
                    x += step
                }
            }
        }
    }
}

// ═══ ۵ · کارتِ نارنجیِ پیشنهاد ════════════════════════════════════════════════════
@Composable
private fun TransferSuggestionCard(text: String, onAccept: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GoldBg)
            .border(1.5.dp, GoldBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = GoldInk,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text,
            color = GoldTextInk,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            "بله",
            color = Color.White,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(GoldButton)
                .pressScaleClickable(onClick = onAccept)
                .padding(horizontal = 11.dp, vertical = 6.dp),
        )
    }
}

// ═══ ۶ · دو کاشیِ ابزار ══════════════════════════════════════════════════════════
@Composable
private fun BudgetToolCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(AppSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(AddTileBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = BudgetGreen, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text(subtitle, color = AppMuted, fontSize = 10.5.sp, modifier = Modifier.padding(top = 3.dp))
        }
        Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = AppMuted, modifier = Modifier.size(18.dp))
    }
}

private val BudgetGreen: Color
    @Composable get() = AppPrimary
private val BudgetGreenDeep: Color
    @Composable get() = AppPrimaryDim
private val BudgetGreenLight: Color
    @Composable get() = AppPrimaryInkLight
private val BudgetGreenShadow = Color(0xFF096F45)
private val AddTileBg: Color
    @Composable get() = AppPrimaryPill
private val AddTileBorder: Color
    @Composable get() = AppPrimaryBorder
private val CardBorder: Color
    @Composable get() = AppLine
private val RailTrack: Color
    @Composable get() = AppLineRow
private val RailTrackSoft: Color
    @Composable get() = AppChipBg
private val OverInk: Color
    @Composable get() = AppDanger
private val GoldBg: Color
    @Composable get() = AppWarningPill
private val GoldBorder: Color
    @Composable get() = AppGoldPillSoft
private val GoldInk: Color
    @Composable get() = AppWarningInk
private val GoldTextInk: Color
    @Composable get() = AppGoldInkSoft
private val GoldButton: Color
    @Composable get() = AppWarning

/**
 * نوارِ هفت‌روزه‌ی کارتِ بودجه - **لمس‌پذیر** (گزارشِ کاربر، ۱ مهر: «بودجه اصلاً نمی‌گیرد»).
 * لمس، نگه‌داشتن و کشیدن مثلِ بقیه‌ی نمودارها؛ حباب روز و خرجِ آن روز را می‌گوید.
 * فیزیکی چپ‌به‌راست است: امروز سمتِ راست، مثلِ بقیه‌ی نمودارها.
 */
@Composable
private fun WeekShareStrip(week: List<Boolean>, spent: List<Double>, privacyMode: Boolean) {
    var touched by remember(week) { mutableStateOf<Int?>(null) }
    var widthPx by remember { mutableFloatStateOf(0f) }
    fun slotAt(x: Float, w: Float) = if (w <= 0f) 0 else ((x / w) * week.size).toInt().coerceIn(0, week.size - 1)
    Box(modifier = Modifier.fillMaxWidth().padding(top = 13.dp)) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .pointerInput(week) {
                        widthPx = size.width.toFloat()
                        detectDragGestures(
                            onDragStart = { touched = slotAt(it.x, size.width.toFloat()) },
                            onDragEnd = { touched = null },
                            onDragCancel = { touched = null },
                            onDrag = { change, _ ->
                                change.consume()
                                touched = slotAt(change.position.x, size.width.toFloat())
                            },
                        )
                    }
                    .pointerInput(week) {
                        widthPx = size.width.toFloat()
                        detectTapGestures(onPress = {
                            touched = slotAt(it.x, size.width.toFloat())
                            tryAwaitRelease()
                            touched = null
                        })
                    },
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                week.forEachIndexed { index, under ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(if (index == touched) 9.dp else 6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (under) Color.White else Color.White.copy(alpha = 0.35f)),
                    )
                }
            }
        }
        ChartTooltipHost(visible = touched != null, modifier = Modifier.align(AbsoluteAlignment.TopLeft)) {
            val index = touched ?: week.lastIndex
            val ago = week.lastIndex - index
            val amount = spent.getOrElse(index) { 0.0 }
            ChartTooltip(
                title = when (ago) { 0 -> "امروز"; 1 -> "دیروز"; else -> "${toFa(ago)} روز پیش" },
                value = if (privacyMode) "•••" else
                    "${amount.rialToFaCompact()} تومان · ${if (week.getOrElse(index) { true }) "زیرِ سهم" else "بیشتر از سهم"}",
                centerX = widthPx * (index + 0.5f) / week.size,
                containerWidth = widthPx,
                background = Color.Black.copy(alpha = 0.45f),
                titleColor = Color.White.copy(alpha = 0.75f),
                valueColor = Color.White,
            )
        }
    }
}
