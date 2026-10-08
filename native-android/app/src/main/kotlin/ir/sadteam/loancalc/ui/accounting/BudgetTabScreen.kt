package ir.sadteam.loancalc.ui.accounting

import ir.sadteam.loancalc.data.netDangShares
import androidx.compose.material.icons.filled.ReceiptLong
import ir.sadteam.loancalc.ui.extras.isDueSoon
import ir.sadteam.loancalc.ui.theme.AppDueNextBorder
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import ir.sadteam.loancalc.ui.components.UndoBar
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppGoldInkSoft
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryBorder
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppPrimaryInkLight
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppWarning
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import ir.sadteam.loancalc.ui.theme.AppWarningPill
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

    // قبض‌ها (۷ مهر): قبلاً فقط در صفحه‌ی «سررسید» بود که دیگر در نوارِ پایین نیست.
    var showBills by remember { mutableStateOf(false) }
    val extrasViewModel: ir.sadteam.loancalc.ui.extras.ExtrasViewModel = hiltViewModel()
    val bills by extrasViewModel.bills.collectAsState()
    if (showBills) {
        androidx.activity.compose.BackHandler { showBills = false }
        ir.sadteam.loancalc.ui.extras.BillsScreen(onBack = { showBills = false }, viewModel = extrasViewModel)
        return
    }

    var showAddBudget by remember { mutableStateOf(false) }
    // بازبینیِ ۹ مهر: بودجه مالِ اشتراک است ولی از همین تب بی‌قفل ساخته می‌شد.
    val premium = ir.sadteam.loancalc.ui.subscription.LocalIsPremium.current
    fun openAddBudget() {
        if (premium) showAddBudget = true
        else ir.sadteam.loancalc.ui.subscription.PremiumPaywall.ask("budget", "بودجه")
    }
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
    // ویرایش/حذفِ بودجه‌ی یک دسته: روی هر ردیفِ دسته بزن (۱۵ مهر).
    var editingRow by remember { mutableStateOf<BudgetRowData?>(null) }
    editingRow?.let { r ->
        val entity = budgets.firstOrNull { it.categoryName == r.category.name }
        var capText by remember(r) { mutableStateOf((r.cap / ir.sadteam.loancalc.ui.jibak.unitDiv).toLong().toString()) }
        ir.sadteam.loancalc.ui.components.JibakAlertDialog(
            onDismissRequest = { editingRow = null },
            title = { androidx.compose.material3.Text("بودجه‌ی ${r.category.name}") },
            text = {
                androidx.compose.foundation.layout.Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.Text("سقفِ ماهانه")
                    androidx.compose.material3.OutlinedTextField(
                        value = capText,
                        onValueChange = { capText = ir.sadteam.loancalc.core.cleanNum(it).take(13) },
                        visualTransformation = ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        suffix = { androidx.compose.material3.Text("${ir.sadteam.loancalc.ui.jibak.unitFa()}") },
                        textStyle = ir.sadteam.loancalc.ui.components.appFieldTextStyle(),
                        colors = ir.sadteam.loancalc.ui.components.appFieldColors(),
                        shape = ir.sadteam.loancalc.ui.components.AppFieldShape,
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    val toman = capText.toLongOrNull() ?: 0L
                    if (toman > 0L && entity != null) {
                        viewModel.setBudget(r.category.name, toman * ir.sadteam.loancalc.ui.jibak.unitDivD, entity.id, entity.accountId)
                    }
                    editingRow = null
                }) { androidx.compose.material3.Text("ذخیره") }
            },
            dismissButton = {
                androidx.compose.foundation.layout.Row {
                    androidx.compose.material3.TextButton(onClick = {
                        entity?.let { viewModel.deleteBudget(it) }
                        editingRow = null
                    }) { androidx.compose.material3.Text("حذفِ بودجه", color = ir.sadteam.loancalc.ui.theme.AppDanger) }
                    androidx.compose.material3.TextButton(onClick = { editingRow = null }) { androidx.compose.material3.Text("انصراف") }
                }
            },
        )
    }
    val totalCap = rows.sumOf { it.cap }
    val totalSpent = rows.sumOf { it.spent }

    val daysInMonth = remember(today) { JalaliCalendar.daysInMonth(today.y, today.m) }
    val daysLeft = (daysInMonth - today.d + 1).coerceAtLeast(1)
    val dailyAllowance = if (totalCap > 0) ((totalCap - totalSpent) / daysLeft).coerceAtLeast(0.0) else 0.0
    /** سهمِ منصفانه‌ی هر روز - مبنای میله‌های هفته و عددِ «ذخیره». */
    val fairShare = if (totalCap > 0) totalCap / daysInMonth else 0.0
    val netAll = remember(allTransactions) { allTransactions.netDangShares() }
    val weekUnderShare = remember(netAll, today, fairShare) {
        if (fairShare <= 0.0) emptyList() else (6 downTo 0).map { back ->
            val d = PersianCalendar.addDays(today, -back)
            netAll
                .filter { ir.sadteam.loancalc.data.countsInReports(it) && it.type == TransactionType.WITHDRAWAL.name && it.year == d.y && it.month == d.m && it.day == d.d }
                .sumOf { it.amount } <= fairShare
        }
    }
    /** خرجِ هر یک از هفت روزِ اخیر (قدیمی→امروز) - برای حبابِ لمسِ نوارِ هفته. */
    val weekSpent = remember(netAll, today) {
        (6 downTo 0).map { back ->
            val d = PersianCalendar.addDays(today, -back)
            netAll
                .filter { ir.sadteam.loancalc.data.countsInReports(it) && it.type == TransactionType.WITHDRAWAL.name && it.year == d.y && it.month == d.m && it.day == d.d }
                .sumOf { it.amount }
        }
    }
    // خرجِ هر روزِ همین ماه (نمودارِ میله‌ایِ کارتِ بالا) + درآمد و خرجِ کلِ ماه (دو باکسِ کنارش).
    val monthDailySpent = remember(netAll, today, daysInMonth) {
        (1..daysInMonth).map { day ->
            netAll
                .filter { ir.sadteam.loancalc.data.countsInReports(it) && it.type == TransactionType.WITHDRAWAL.name && it.year == today.y && it.month == today.m && it.day == day }
                .sumOf { it.amount }
        }
    }
    val monthIncome = remember(allTransactions, today) {
        allTransactions
            .filter { ir.sadteam.loancalc.data.countsInReports(it) && it.type == TransactionType.DEPOSIT.name && it.year == today.y && it.month == today.m }
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
            item { BudgetHeader(onAdd = { openAddBudget() }, showAdd = false) }
            if (rows.isEmpty()) {
                item { NoBudgetCard(onCreate = { openAddBudget() }) }
                if (starterSuggestions.isNotEmpty()) {
                    item {
                        StarterSuggestions(
                            starters = starterSuggestions,
                            privacyMode = privacyMode,
                            onAcceptAll = {
                                if (!premium) {
                                    ir.sadteam.loancalc.ui.subscription.PremiumPaywall.ask("budget", "بودجه")
                                    return@StarterSuggestions
                                }
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
            // ۱۶ مهر: بودجه‌ی بزرگ‌تر از درآمد، «امروز می‌توانی X خرج کنی» را گمراه می‌کند.
            if (totalCap > 0 && monthIncome > 0.0 && totalCap > monthIncome) {
                item {
                    val warnCtx = ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode.current
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AppRadius.card))
                            .background(GoldBg)
                            .border(1.5.dp, GoldBorder, RoundedCornerShape(AppRadius.card))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = ir.sadteam.loancalc.ui.theme.AppWarningInk, modifier = Modifier.size(16.dp))
                        Text(
                            "بودجه‌ات (${ir.sadteam.loancalc.ui.privacy.maskIfPrivate(warnCtx, totalCap.rialToFaCompact())}) از درآمدِ ثبت‌شده‌ی این ماهت (${ir.sadteam.loancalc.ui.privacy.maskIfPrivate(warnCtx, monthIncome.rialToFaCompact())}) بیشتر است؛ مطمئن شو بودجه را درست گذاشته‌ای.",
                            color = ir.sadteam.loancalc.ui.theme.AppWarningInk,
                            fontSize = 10.5.sp,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
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
            if (rows.isNotEmpty()) {
                item(key = "budgets-title") {
                    Text(
                        "بودجه‌ی دسته‌ها · برای ویرایش یا حذف روی هر ردیف بزن",
                        color = AppMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    )
                }
            }
            items(rows, key = { it.category.name }) { row ->
                CategoryBudgetRow(row, privacyMode, onClick = { editingRow = row })
            }
            transfer?.let { t ->
                item {
                    TransferSuggestionCard(
                        text = "بودجه‌ی ${t.to.category.name} را ${ir.sadteam.loancalc.ui.privacy.maskIfPrivate(ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode.current, (t.amount).rialToFaCompact())} ${ir.sadteam.loancalc.ui.jibak.unitFa()} از " +
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
            // ۱۴ مهر: سه کاشیِ ناهم‌اندازه → یک کارت با سه ردیفِ مرتب (همه‌ی درها سرِ جایشان).
            item {
                val dueSoon = bills.count { it.isDueSoon(today.y, today.m, today.d) }
                ir.sadteam.loancalc.ui.components.AppCard {
                    BudgetToolRow(
                        icon = Icons.Filled.ReceiptLong,
                        title = "قبض‌ها",
                        subtitle = when {
                            bills.isEmpty() -> "آب، برق، گاز، موبایل…"
                            dueSoon > 0 -> "${toFa(dueSoon)} قبض نزدیکِ موعد"
                            else -> "${toFa(bills.size)} قبض · همه پرداخت شده"
                        },
                        onClick = { showBills = true },
                    )
                    BudgetToolRow(
                        icon = Icons.Filled.EventRepeat,
                        title = "پرداختِ تکراری",
                        subtitle = "${toFa(recurringPayments.size)} مورد",
                        onClick = onOpenRecurring,
                    )
                    BudgetToolRow(
                        icon = Icons.Outlined.PieChart,
                        title = "دسته‌بندی‌ها",
                        subtitle = "${toFa(expenseCats.size)} دسته",
                        onClick = onOpenCategories,
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
        // ۱۶ مهر: «+» مثلِ خانه و دارایی پایینِ صفحه (خواسته‌ی کاربر)، نه بالای هدر.
        if (rows.isNotEmpty()) ir.sadteam.loancalc.ui.components.AppFab(
            onClick = { openAddBudget() },
            contentDescription = "افزودنِ بودجه",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp),
        )
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
internal val BudgetGreen: Color
    @Composable get() = AppPrimary
internal val BudgetGreenDeep: Color
    @Composable get() = AppPrimaryDim
private val BudgetGreenLight: Color
    @Composable get() = AppPrimaryInkLight
internal val AddTileBg: Color
    @Composable get() = AppPrimaryPill
private val AddTileBorder: Color
    @Composable get() = AppPrimaryBorder
internal val CardBorder: Color
    @Composable get() = AppLine
internal val RailTrack: Color
    @Composable get() = AppLineRow
internal val RailTrackSoft: Color
    @Composable get() = AppChipBg
internal val OverInk: Color
    @Composable get() = AppDanger
internal val GoldBg: Color
    @Composable get() = AppWarningPill
internal val GoldBorder: Color
    @Composable get() = AppDueNextBorder
internal val GoldInk: Color
    @Composable get() = AppWarningInk
internal val GoldTextInk: Color
    @Composable get() = AppGoldInkSoft
internal val GoldButton: Color
    @Composable get() = AppWarning
