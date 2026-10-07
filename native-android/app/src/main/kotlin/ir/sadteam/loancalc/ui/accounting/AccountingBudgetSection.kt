package ir.sadteam.loancalc.ui.accounting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.outlined.PieChart
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianCalendar
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.CategoryEntry
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.db.BudgetEntity
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.category.CategoryViewModel
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppFab
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.AppProgressBar
import ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.components.ProgressRing
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppInfoPill
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * بازطراحیِ تبِ بودجه (خواسته‌ی صریحِ کاربر با اسکرین‌شاتِ رفرنسِ Poolaki): ناوبرِ ماهانه (‹ ماه/سال ›)
 * + یه ردیفِ خلاصه‌ی «همه دسته‌بندی‌ها» + هر ردیفِ دسته مستقیم نوارِ پیشرفت/خرج‌شده/باقی‌مانده رو نشون
 * می‌ده (بدونِ نیاز به تپ). سقف (BudgetEntity.monthlyCap) عمداً مستقل از ماهه (رجوع کن به CLAUDE.md) -
 * فقط عددِ خرج‌شده‌ست که با تغییرِ ماهِ ناوبری‌شده عوض می‌شه.
 */
@Composable
private fun BudgetSection(
    viewModel: AccountViewModel,
    categoryViewModel: CategoryViewModel,
    onOpenCategories: () -> Unit,
    onOpenRecurring: () -> Unit,
) {
    val budgets by viewModel.budgets.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val recurringPayments by viewModel.recurringPayments.collectAsState()
    val today = remember { JalaliCalendar.today() }
    var viewYear by remember { mutableStateOf(today.y) }
    var viewMonth by remember { mutableStateOf(today.m) }
    val spend = remember(allTransactions, viewYear, viewMonth) {
        viewModel.spendByCategory(allTransactions, viewYear, viewMonth)
    }
    val expenseCats by categoryViewModel.expenseCategories.collectAsState()

    var editingCategory by remember { mutableStateOf<CategoryEntry?>(null) }
    var deletingBudget by remember { mutableStateOf<ir.sadteam.loancalc.data.db.BudgetEntity?>(null) }
    deletingBudget?.let { b ->
        ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog(
            title = "حذفِ بودجه",
            text = "سقفِ بودجه‌ی این دسته برداشته شود؟ تراکنش‌ها دست نمی‌خورند.",
            onConfirm = { viewModel.deleteBudget(b); deletingBudget = null },
            onDismiss = { deletingBudget = null },
        )
    }
    var capText by remember { mutableStateOf("") }
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var budgetSuggestion by remember { mutableStateOf<CategoryEntry?>(null) }

    fun stepMonth(delta: Int) {
        var m = viewMonth + delta
        var y = viewYear
        while (m > 12) { m -= 12; y += 1 }
        while (m < 1) { m += 12; y -= 1 }
        viewMonth = m
        viewYear = y
    }

    // فقط دسته‌هایی که کاربر واقعاً براشون سقف تعیین کرده - قبلاً همه‌ی دسته‌های هزینه (حتی
    // «سقفی تعیین نشده») تو لیست بودن که صفحه رو الکی پر می‌کرد (خواسته‌ی صریحِ کاربر با تطبیق با
    // یه اپِ رفرنس: «اگه چیزی هست رو بزار الکی صحفه رو پر نکن»).
    val budgetedCats = remember(expenseCats, budgets) { expenseCats.filter { cat -> budgets.any { it.categoryName == cat.name } } }
    val unbudgetedCats = remember(expenseCats, budgetedCats) { expenseCats - budgetedCats.toSet() }
    val totalCap = remember(budgets, budgetedCats) { budgetedCats.sumOf { cat -> budgets.first { it.categoryName == cat.name }.monthlyCap } }
    val totalSpent = remember(spend, budgetedCats) { budgetedCats.sumOf { spend[it.name] ?: 0.0 } }

    // ── داده‌ی کارتِ قهرمانِ بودجه (کارتِ `27c`ی طرح) ─────────────────────────────────
    // «امروز می‌تونی خرج کنی» = مانده‌ی سقفِ ماه تقسیم بر روزهای باقی‌مانده‌ی ماه. فقط برای
    // **ماهِ جاری** معنی داره؛ برای ماهِ گذشته/آینده عددش نمایش داده نمی‌شه.
    val isCurrentMonth = viewYear == today.y && viewMonth == today.m
    val daysInViewMonth = remember(viewYear, viewMonth) { JalaliCalendar.daysInMonth(viewYear, viewMonth) }
    val daysLeft = if (isCurrentMonth) (daysInViewMonth - today.d + 1).coerceAtLeast(1) else daysInViewMonth
    val dailyAllowance = if (totalCap > 0) ((totalCap - totalSpent) / daysLeft).coerceAtLeast(0.0) else 0.0
    // سهمِ منصفانه‌ی هر روز (سقف تقسیم بر کلِ روزهای ماه) - مبنای نقطه‌های ۷ روزِ اخیر.
    val fairShare = if (totalCap > 0) totalCap / daysInViewMonth else 0.0
    // هفت روزِ اخیر: هر روزی که خرجش زیرِ سهمِ منصفانه بوده یه نقطه‌ی سفیدِ توپر می‌گیره.
    val weekUnderShare = remember(allTransactions, today, fairShare, isCurrentMonth) {
        if (!isCurrentMonth || fairShare <= 0.0) {
            emptyList()
        } else {
            (6 downTo 0).map { back ->
                val d = PersianCalendar.addDays(today, -back)
                val daySpend = allTransactions
                    .filter { it.type == TransactionType.WITHDRAWAL.name && it.year == d.y && it.month == d.m && it.day == d.d }
                    .sumOf { it.amount }
                daySpend <= fairShare
            }
        }
    }
    val savedSoFar = if (isCurrentMonth && fairShare > 0) {
        (fairShare * today.d - totalSpent).coerceAtLeast(0.0)
    } else {
        0.0
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(14.dp, 14.dp, 14.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                // هیرویِ ماه - ناوبر + خرج‌شده/سقفِ کل، جایگزینِ ردیفِ لختِ قبلی و BudgetRowِ «همه
                // دسته‌بندی‌ها» (که همون اطلاعات رو تکراری نشون می‌داد). عددها از همون
                // totalSpent/totalCap که از قبل محاسبه می‌شد.
                // کارتِ قهرمانِ تبِ بودجه - کارتِ `27c`ی طرح. عددِ قهرمان **«امروز می‌تونی خرج
                // کنی»**ه (سهمِ روزانه)، نه «خرجِ ماه» - همون چیزی که طرح می‌خواد.
                AppHeroCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MonthNavArrow(icon = Icons.Filled.ChevronRight, contentDescription = "ماهِ قبل") { stepMonth(-1) }
                        Text(
                            "${faMonthNamesAccounting[viewMonth - 1]} ${toFa(viewYear)}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                        )
                        MonthNavArrow(icon = Icons.Filled.ChevronLeft, contentDescription = "ماهِ بعد") { stepMonth(1) }
                    }
                    Text(
                        if (isCurrentMonth) "امروز می‌تونی خرج کنی" else "خرجِ این ماه",
                        color = HeroMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                    Text(
                        fmt(if (isCurrentMonth && totalCap > 0) dailyAllowance else totalSpent),
                        color = Color.White,
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    // هفت نقطه‌ی ۶ پیکسلی - هر روزِ زیرِ سهم سفیدِ توپر، هر روزِ بالای سهم
                    // سفیدِ ۳۵٪. طبقِ طرح.
                    if (weekUnderShare.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            weekUnderShare.forEach { under ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(AppRadius.button))
                                        .background(if (under) Color.White else Color.White.copy(alpha = 0.35f)),
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                "${toFa(weekUnderShare.count { it })} روز زیرِ سهم موندی",
                                color = Color.White.copy(alpha = 0.82f),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            if (savedSoFar > 0) {
                                Text(
                                    "+${fmt(savedSoFar)} ذخیره",
                                    color = Color.White,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                )
                            }
                        }
                    }
                }
            }
            // کارتِ سفیدِ «کلِ ماه» - جدا از کارتِ قهرمان، دقیقاً مثلِ طرح.
            item {
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("کلِ ماه", color = AppText, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        Text(
                            "${toFa(if (totalCap > 0) ((totalSpent / totalCap) * 100).toInt() else 0)}٪",
                            color = AppPrimaryInk,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    AppProgressBar(
                        fraction = if (totalCap > 0) (totalSpent / totalCap).toFloat().coerceIn(0f, 1f) else 0f,
                        color = AppPrimary,
                        trackColor = AppChipBg,
                        modifier = Modifier.padding(top = 10.dp).height(14.dp),
                    )
                    Text(
                        "${fmt((totalSpent) / 10)} از سقفِ ${fmt((totalCap) / 10)} تومان",
                        color = AppLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BudgetToolTile(
                        icon = Icons.Filled.EventRepeat,
                        title = "پرداختِ تکراری",
                        subtitle = "${toFa(recurringPayments.size)} مورد",
                        onClick = onOpenRecurring,
                        modifier = Modifier.weight(1f),
                    )
                    BudgetToolTile(
                        icon = Icons.Outlined.PieChart,
                        title = "دسته‌بندی‌ها",
                        subtitle = "${toFa(expenseCats.size)} دسته",
                        onClick = onOpenCategories,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (budgetedCats.isEmpty()) {
                item {
                    Column {
                        EmptyState(
                            icon = Icons.Outlined.PieChart,
                            title = "هنوز بودجه‌ای تعیین نکردی",
                            description = "برای هر دسته‌ی هزینه یه سقفِ ماهانه بذار تا خرجت رو زیرِ نظر داشته باشی. با دکمه‌ی + پایینِ صفحه شروع کن.",
                        )
                        // میان‌برِ همون FABِ پایین - دو پرخرج‌ترینِ دسته‌ی بی‌بودجه، تپ همون
                        // NewBudgetSheet رو با دسته‌ی پیش‌انتخاب‌شده باز می‌کنه. قابلیتِ جدید نیست.
                        val suggestions = remember(spend, unbudgetedCats) {
                            unbudgetedCats
                                .filter { (spend[it.name] ?: 0.0) > 0.0 }
                                .sortedByDescending { spend[it.name] ?: 0.0 }
                                .take(2)
                        }
                        if (suggestions.isNotEmpty()) {
                            Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                suggestions.forEach { cat ->
                                    BudgetSuggestionRow(
                                        cat = cat,
                                        spent = spend[cat.name] ?: 0.0,
                                        onClick = { budgetSuggestion = cat },
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                items(budgetedCats, key = { it.name }) { cat ->
                    val budget = budgets.firstOrNull { it.categoryName == cat.name }
                    val spent = spend[cat.name] ?: 0.0
                    Column(modifier = Modifier.animateItem()) {
                        BudgetRow(
                            icon = cat.icon,
                            iconTint = cat.color,
                            name = cat.name,
                            spent = spent,
                            cap = budget?.monthlyCap,
                            onClick = {
                                editingCategory = cat
                                capText = budget?.monthlyCap?.toLong()?.div(10)?.toString() ?: ""
                            },
                        )
                        if (editingCategory?.name == cat.name) {
                            // باگِ رفع‌شده: قبلاً فیلدِ مبلغ کنارِ دکمه‌ی «ذخیره» تو یه Rowِ باریک
                            // جا می‌شد (weight(1f) کنارِ یه دکمه‌ی هم‌ردیف) و عملاً جایی برای تایپِ
                            // عدد نمی‌موند - کاربر با اسکرین‌شات گزارش داد. الان فیلد تمامِ عرضِ
                            // کارت رو می‌گیره (هم‌الگو با بقیه‌ی فرم‌های اپ، مثلِ AddTransactionForm)،
                            // دکمه‌ها زیرش تو یه ردیفِ جدا اومدن.
                            AppCard(label = "سقفِ ماهانه", modifier = Modifier.padding(top = 6.dp)) {
                                OutlinedTextField(
                                    value = capText,
                                    onValueChange = { capText = cleanNum(it) },
                                    visualTransformation = ThousandsSeparatorTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = appFieldColors(),
                                    suffix = { Text("تومان", color = AppMuted, fontSize = 13.sp) }, shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                                val capRial = (capText.toLongOrNull() ?: 0L) * 10
                                if (capRial > 0) {
                                    Text(
                                        "${numberToWordsFa((capRial / 10).toDouble())} تومان",
                                        color = AppMuted,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    if (budget != null) {
                                        OutlinedButton(
                                            onClick = {
                                                deletingBudget = budget
                                                editingCategory = null
                                            },
                                            modifier = Modifier.weight(1f),
                                        ) { Text("حذفِ سقف", fontSize = 12.sp, color = AppDanger) }
                                    }
                                    GradientButton(
                                        onClick = {
                                            val cap = (capText.toDoubleOrNull() ?: 0.0) * 10
                                            if (cap > 0) viewModel.setBudget(cat.name, cap, budget?.id)
                                            editingCategory = null
                                        },
                                        enabled = (capText.toDoubleOrNull() ?: 0.0) > 0,
                                        modifier = Modifier.weight(1f),
                                    ) { Text("ذخیره", fontSize = 12.sp) }
                                }
                            }
                        }
                    }
                }
            }
        }

        // دکمه‌ی «+» شناور - دقیقاً هم‌الگو با FAB افزودنِ وامِ دستی تو MyLoansScreen.kt (BottomEnd
        // که تو RTL یعنی گوشه‌ی پایین-چپ، پاپِ فنری). تنها وقتی نشون داده می‌شه که حداقل یه دسته‌ی
        // بی‌بودجه مونده باشه - اگه همه‌ی دسته‌ها بودجه گرفتن، دیگه چیزی برای افزودن نیست.
        AnimatedVisibility(
            visible = unbudgetedCats.isNotEmpty(),
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(150)),
            exit = scaleOut(tween(120)) + fadeOut(tween(120)),
            // همان گوشه‌ی FABِ دارایی/وام (خواسته‌ی کاربر ۱۳ مهر: «مثلِ بقیه‌جاها»).
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 16.dp),
        ) {
            AppFab(
                onClick = { showAddBudgetDialog = true },
                contentDescription = "افزودنِ بودجه",
            )
        }
    }

    if (showAddBudgetDialog || budgetSuggestion != null) {
        // شیتِ «بودجه‌ی جدید» طبقِ اپِ مرجع: مقدار → حساب‌کتاب (با گزینه‌ی «همه حساب‌کتاب‌ها») →
        // دسته‌بندی. جایگزینِ دیالوگِ دومرحله‌ای قبلی که فقط دسته و مبلغ می‌گرفت. وقتی از میان‌برِ
        // پیشنهادِ حالتِ خالی باز شده باشه، دسته‌ش از قبل انتخاب‌شده میاد.
        NewBudgetSheet(
            categories = unbudgetedCats,
            accounts = accounts,
            initialCategory = budgetSuggestion,
            onDismiss = { showAddBudgetDialog = false; budgetSuggestion = null },
            onSave = { cat, cap, accId ->
                viewModel.setBudget(cat.name, cap, null, accId)
                showAddBudgetDialog = false
                budgetSuggestion = null
            },
        )
    }
}
/** دیالوگِ دومرحله‌ای افزودنِ بودجه‌ی جدید (از رو FABِ بالا) - اول یه دسته‌ی بی‌بودجه انتخاب می‌شه،
 * بعد سقفِ ماهانه‌ش وارد می‌شه. الگوی «انتخاب → مقدار» هم‌راستا با AccountPickerDialogِ موجود تو
 * ui/components. */
@Composable
private fun AddBudgetDialog(
    categories: List<CategoryEntry>,
    onDismiss: () -> Unit,
    onSave: (CategoryEntry, Double) -> Unit,
) {
    ir.sadteam.loancalc.ui.subscription.PremiumBlock(blocked = true, key = "budget", label = "بودجه", onBlocked = onDismiss)
    var selected by remember { mutableStateOf<CategoryEntry?>(null) }
    var capText by remember { mutableStateOf("") }
    val cat = selected

    JibakAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (cat == null) "برای کدوم دسته بودجه بزاریم؟" else "سقفِ ماهانه‌ی «${cat.name}»") },
        text = {
            if (cat == null) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    categories.forEach { c ->
                        TextButton(
                            onClick = { selected = c },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(28.dp).background(c.color.copy(alpha = 0.16f), CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(c.icon, contentDescription = null, tint = c.color, modifier = Modifier.size(15.dp))
                                }
                                Text(c.name, color = AppText, fontSize = 14.sp, modifier = Modifier.padding(start = 10.dp))
                            }
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
 textStyle = ir.sadteam.loancalc.ui.components.appFieldTextStyle(),
                        value = capText,
                        onValueChange = { capText = cleanNum(it) },
                        visualTransformation = ThousandsSeparatorTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = appFieldColors(),
                        suffix = { Text("تومان", color = AppMuted, fontSize = 13.sp) }, shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                    val capRial = (capText.toLongOrNull() ?: 0L) * 10
                    if (capRial > 0) {
                        Text(
                            "${numberToWordsFa((capRial / 10).toDouble())} تومان",
                            color = AppMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (cat != null) {
                TextButton(onClick = {
                    val capVal = (capText.toDoubleOrNull() ?: 0.0) * 10
                    if (capVal > 0) onSave(cat, capVal)
                }, enabled = (capText.toDoubleOrNull() ?: 0.0) > 0) { Text("ذخیره") }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (cat != null) selected = null else onDismiss() }) {
                Text(if (cat != null) "بازگشت" else "انصراف")
            }
        },
    )
}
/** دو کارتِ آبیِ ابزاری (پرداختِ تکراری/دسته‌بندی‌ها) - جایگزینِ دو OutlinedButtonِ معلقِ قبلی،
 * هم‌الگو با کارتِ آبیِ گزارشِ سفارشی تو ReportSection. */
@Composable
private fun BudgetToolTile(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier.pressScaleClickable(onClick = onClick), borderColor = AppInfo.copy(alpha = 0.30f)) {
        Box(
            modifier = Modifier.size(32.dp).background(AppInfoPill, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = AppInfo, modifier = Modifier.size(16.dp))
        }
        Text(title, color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        Text(subtitle, color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
    }
}
/** میان‌برِ پیشنهادِ سقف تو حالتِ خالی - همون [BudgetRow]ِ بی‌سقف با دکمه‌ی «+ سقف». */
@Composable
private fun BudgetSuggestionRow(cat: CategoryEntry, spent: Double, onClick: () -> Unit) {
    AppCard(modifier = Modifier.pressScaleClickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(28.dp).background(cat.color.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(cat.icon, contentDescription = null, tint = cat.color, modifier = Modifier.size(14.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                Text(cat.name, color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                Text("${fmt((spent) / 10)} تومان خرج شده", color = AppMuted, fontSize = 10.5.sp)
            }
            Text("+ سقف", color = AppPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
        }
    }
}
/** یه ردیفِ بودجه‌ی تک (رجوع کن به BudgetSection بالا) - حلقه‌ی پیشرفت + خرج‌شده/باقی‌مانده
 * همیشه‌نمایان، تپ رو ردیف (اگه [onClick] داده شده) فرمِ تعیین/ویرایشِ سقف رو باز می‌کنه. */
@Composable
private fun BudgetRow(
    icon: ImageVector,
    iconTint: Color,
    name: String,
    spent: Double,
    cap: Double?,
    onClick: (() -> Unit)?,
) {
    val fraction = if (cap != null && cap > 0) (spent / cap).toFloat().coerceIn(0f, 1f) else 0f
    val over = cap != null && spent > cap
    val ringColor = if (over) AppDanger else AppPrimary
    AppCard(modifier = if (onClick != null) Modifier.pressScaleClickable(onClick = onClick) else Modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = fraction,
                size = 40.dp,
                strokeWidth = 4.dp,
                colors = listOf(ringColor, ringColor),
            ) {
                Text("${toFa((fraction * 100).toInt())}", color = ringColor, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(name, color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("${fmt((spent) / 10)} تومان", color = ringColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    if (cap == null) "سقفی تعیین نشده" else if (over) "بیشتر از سقف!" else "از ${fmt(cap)} — ${fmt(cap - spent)} مانده",
                    color = if (over) AppDanger else AppMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
/**
 * شیتِ «بودجه‌ی جدید» - خواسته‌ی صریحِ کاربر طبقِ اپِ مرجع: سه بخشِ پشتِ‌هم، نه یه دیالوگِ
 * دومرحله‌ای. حساب‌کتاب پیش‌فرض «همه حساب‌کتاب‌ها»ست (یعنی `null`)، دقیقاً مثلِ رفتارِ قبلیِ
 * بودجه‌ها - پس کسی که این فیلد رو دست نزنه، همون چیزی رو می‌گیره که قبلاً می‌گرفت.
 */
@Composable
internal fun NewBudgetSheet(
    categories: List<CategoryEntry>,
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSave: (CategoryEntry, Double, Long?) -> Unit,
    initialCategory: CategoryEntry? = null,
) {
    ir.sadteam.loancalc.ui.subscription.PremiumBlock(blocked = true, key = "budget", label = "بودجه", onBlocked = onDismiss)
    var capText by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf(initialCategory) }
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var showAccountPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    if (showAccountPicker) {
        JibakAlertDialog(
            onDismissRequest = { showAccountPicker = false },
            title = { Text("حساب‌کتاب‌ها", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = { selectedAccountId = null; showAccountPicker = false },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("همه حساب‌کتاب‌ها", modifier = Modifier.fillMaxWidth()) }
                    accounts.forEach { acc ->
                        TextButton(
                            onClick = { selectedAccountId = acc.id; showAccountPicker = false },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(acc.name, modifier = Modifier.fillMaxWidth()) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showAccountPicker = false }) { Text("انصراف") } },
        )
    }

    if (showCategoryPicker) {
        JibakAlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = { Text("دسته‌بندی", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    categories.forEach { c ->
                        TextButton(
                            onClick = { selectedCat = c; showCategoryPicker = false },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(c.icon, contentDescription = null, tint = c.color, modifier = Modifier.size(18.dp))
                                Text(c.name, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showCategoryPicker = false }) { Text("انصراف") } },
        )
    }

    val accountLabel = accounts.firstOrNull { it.id == selectedAccountId }?.name ?: "همه حساب‌کتاب‌ها"

    JibakAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("بودجه‌ی جدید", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("مقدار بودجه", color = AppMuted, fontSize = 11.sp)
                OutlinedTextField(
 textStyle = ir.sadteam.loancalc.ui.components.appFieldTextStyle(),
                    value = capText,
                    onValueChange = { capText = cleanNum(it) },
                    visualTransformation = ThousandsSeparatorTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    suffix = { Text("تومان", color = AppMuted, fontSize = 13.sp) }, colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
                val rial = (capText.toLongOrNull() ?: 0L) * 10
                if (rial > 0) {
                    Text("${numberToWordsFa((rial / 10).toDouble())} تومان", color = AppMuted, fontSize = 11.sp)
                }

                Text("حساب", color = AppMuted, fontSize = 11.sp)
                OutlinedButton(onClick = { showAccountPicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(accountLabel, modifier = Modifier.fillMaxWidth(), fontSize = 13.sp)
                }

                Text("دسته‌بندی", color = AppMuted, fontSize = 11.sp)
                OutlinedButton(onClick = { showCategoryPicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(selectedCat?.name ?: "دسته‌بندی", modifier = Modifier.fillMaxWidth(), fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val cap = (capText.toDoubleOrNull() ?: 0.0) * 10
                    val cat = selectedCat
                    if (cap > 0 && cat != null) onSave(cat, cap, selectedAccountId)
                },
                enabled = (capText.toDoubleOrNull() ?: 0.0) > 0 && selectedCat != null,
            ) { Text("ثبت بودجه") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
    )
}
