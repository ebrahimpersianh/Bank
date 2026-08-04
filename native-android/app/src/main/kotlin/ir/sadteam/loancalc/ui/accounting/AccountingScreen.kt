package ir.sadteam.loancalc.ui.accounting

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.REMINDER_OFFSET_OPTIONS
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.formatReminderOffsets
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.CategoryEntry
import ir.sadteam.loancalc.data.categoriesFor
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.data.db.BudgetEntity
import ir.sadteam.loancalc.data.db.RecurringPaymentEntity
import ir.sadteam.loancalc.data.findCategory
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.ReminderOverrideCard
import ir.sadteam.loancalc.ui.components.StaggerIn
import ir.sadteam.loancalc.ui.components.SwipeToDeleteRow
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.components.lazyColumnScrollbar
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText

private val faMonthNamesAccounting = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

/**
 * تبِ «حسابداری» - نسخه‌ی اولِ ماژولِ حسابداریِ شخصیِ «حسابدار من» (رجوع کن به CLAUDE.md، بخشِ
 * «تغییرِ نامِ اپ + افزودنِ ماژولِ حسابداریِ شخصی»). رو همون [AccountRepository]/[AccountEntity]ِ
 * موجودِ «حساب بانکی» ساخته شده - حساب‌ها همونا هستن، فقط تراکنش‌ها الان دسته‌بندی هم دارن.
 */
@Composable
fun AccountingScreen(viewModel: AccountViewModel = hiltViewModel()) {
    var screenKey by remember { mutableStateOf("main") }
    AnimatedContent(
        targetState = screenKey,
        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
        label = "accountingScreen",
    ) { key ->
        when (key) {
            "budget" -> BudgetSection(viewModel = viewModel, onBack = { screenKey = "main" })
            "recurring" -> RecurringSection(viewModel = viewModel, onBack = { screenKey = "main" })
            else -> MainSection(
                viewModel = viewModel,
                onOpenBudget = { screenKey = "budget" },
                onOpenRecurring = { screenKey = "recurring" },
            )
        }
    }
}

@Composable
private fun MainSection(
    viewModel: AccountViewModel,
    onOpenBudget: () -> Unit,
    onOpenRecurring: () -> Unit,
) {
    val accounts by viewModel.accounts.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()
    val privacyMode = LocalPrivacyMode.current
    val today = remember { JalaliCalendar.today() }
    val (income, expense) = remember(allTransactions) { viewModel.monthlyTotals(allTransactions, today.y, today.m) }

    var searchQuery by remember { mutableStateOf("") }
    var showAddForm by remember { mutableStateOf(false) }
    var deletingTx by remember { mutableStateOf<AccountTransactionEntity?>(null) }

    val filtered = remember(allTransactions, searchQuery) {
        if (searchQuery.isBlank()) {
            allTransactions
        } else {
            val q = searchQuery.trim()
            allTransactions.filter {
                it.description.contains(q, ignoreCase = true) || (it.category?.contains(q, ignoreCase = true) == true)
            }
        }
    }

    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxWidth().lazyColumnScrollbar(listState, AppPrimary),
        contentPadding = PaddingValues(14.dp, 14.dp, 14.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            StaggerIn(0) {
                AppCard(label = "گزارشِ ${faMonthNamesAccounting[today.m - 1]}") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("درآمد", color = AppMuted, fontSize = 12.sp)
                            PrivacyCrossfade(privacyMode) { masked ->
                                Text("${maskIfPrivate(masked, fmt(income))} ریال", color = AppPrimary, fontSize = 15.sp)
                            }
                        }
                        Column {
                            Text("هزینه", color = AppMuted, fontSize = 12.sp)
                            PrivacyCrossfade(privacyMode) { masked ->
                                Text("${maskIfPrivate(masked, fmt(expense))} ریال", color = AppDanger, fontSize = 15.sp)
                            }
                        }
                        Column {
                            Text("مانده", color = AppMuted, fontSize = 12.sp)
                            PrivacyCrossfade(privacyMode) { masked ->
                                Text(
                                    "${maskIfPrivate(masked, fmt(income - expense))} ریال",
                                    color = if (income - expense >= 0) AppText else AppDanger,
                                    fontSize = 15.sp,
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            StaggerIn(1) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onOpenBudget, modifier = Modifier.weight(1f)) { Text("بودجه‌بندی") }
                    OutlinedButton(onClick = onOpenRecurring, modifier = Modifier.weight(1f)) { Text("پرداختِ تکراری") }
                }
            }
        }

        item {
            StaggerIn(2) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("جستجو تو تراکنش‌ها...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = appFieldColors(),
                )
            }
        }

        item {
            if (accounts.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.AccountBalanceWallet,
                    title = "اول یه حساب بساز",
                    description = "برای ثبتِ تراکنش، اول از تنظیمات → «حساب‌های بانکی» یه حساب " +
                        "(نقدی یا بانکی) بساز، بعد برگرد اینجا.",
                )
            } else if (showAddForm) {
                AddTransactionForm(
                    accounts = accounts,
                    onCancel = { showAddForm = false },
                    onSubmit = { accountId, type, amount, category, desc, y, m, d ->
                        viewModel.addTransaction(accountId, type, amount, desc, y, m, d, category)
                        showAddForm = false
                    },
                )
            } else {
                GradientButton(onClick = { showAddForm = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("+ ثبتِ تراکنش")
                }
            }
        }

        if (filtered.isEmpty() && accounts.isNotEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.AccountBalanceWallet,
                    title = if (searchQuery.isBlank()) "هنوز تراکنشی ثبت نشده" else "چیزی پیدا نشد",
                    description = if (searchQuery.isBlank()) {
                        "دخل و خرجِ روزمره‌ت که با دسته‌بندی ثبت بشه، همین‌جا می‌بینیشون."
                    } else {
                        "تراکنشی با این توضیح/دسته پیدا نشد."
                    },
                )
            }
        } else {
            items(filtered, key = { it.id }) { tx ->
                val accountName = accounts.firstOrNull { it.id == tx.accountId }?.name ?: "—"
                SwipeToDeleteRow(onDelete = { deletingTx = tx }, modifier = Modifier.animateItem()) {
                    AccountingTransactionRow(tx = tx, accountName = accountName, privacyMode = privacyMode)
                }
            }
        }
    }

    deletingTx?.let { tx ->
        ConfirmDeleteDialog(
            title = "حذفِ تراکنش",
            text = "این تراکنش حذف بشه؟ این کار قابلِ‌برگشت نیست.",
            onConfirm = { viewModel.deleteTransaction(tx); deletingTx = null },
            onDismiss = { deletingTx = null },
        )
    }
}

@Composable
private fun AccountingTransactionRow(tx: AccountTransactionEntity, accountName: String, privacyMode: Boolean) {
    val category = findCategory(tx.category)
    val isIncome = tx.type == TransactionType.DEPOSIT.name
    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (category != null) {
                    Icon(
                        category.icon,
                        contentDescription = null,
                        tint = category.color,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(category.color.copy(alpha = 0.14f))
                            .padding(7.dp),
                    )
                }
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    Text(category?.name ?: (if (isIncome) "واریز" else "برداشت"), color = AppText, fontSize = 13.sp)
                    if (tx.description.isNotBlank()) {
                        Text(tx.description, color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                    Text(
                        "$accountName — ${toFa(tx.day)}/${toFa(tx.month)}/${toFa(tx.year)}",
                        color = AppMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    "${if (isIncome) "+" else "-"}${maskIfPrivate(masked, fmt(tx.amount))}",
                    color = if (isIncome) AppPrimary else AppDanger,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun AddTransactionForm(
    accounts: List<AccountEntity>,
    onCancel: () -> Unit,
    onSubmit: (accountId: Long, type: TransactionType, amount: Double, category: String?, desc: String, y: Int, m: Int, d: Int) -> Unit,
) {
    var type by remember { mutableStateOf(TransactionType.WITHDRAWAL) }
    var selectedAccountId by remember { mutableStateOf(accounts.first().id) }
    var selectedCategory by remember { mutableStateOf<CategoryEntry?>(null) }
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val today = remember { JalaliCalendar.today() }
    var txYear by remember { mutableStateOf(today.y) }
    var txMonth by remember { mutableStateOf(today.m) }
    var txDay by remember { mutableStateOf(today.d) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AppCard(label = "نوع تراکنش") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppChip(
                    label = "هزینه",
                    selected = type == TransactionType.WITHDRAWAL,
                    onClick = { type = TransactionType.WITHDRAWAL; selectedCategory = null },
                )
                AppChip(
                    label = "درآمد",
                    selected = type == TransactionType.DEPOSIT,
                    onClick = { type = TransactionType.DEPOSIT; selectedCategory = null },
                )
            }
        }

        AppCard(label = "دسته‌بندی") {
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categoriesFor(type), key = { it.name }) { cat ->
                    AppChip(label = cat.name, selected = selectedCategory?.name == cat.name, onClick = { selectedCategory = cat })
                }
            }
        }

        if (accounts.size > 1) {
            AppCard(label = "حساب") {
                AccountingDropdown(
                    options = accounts.map { it.id to it.name },
                    selected = selectedAccountId,
                    onSelect = { selectedAccountId = it },
                )
            }
        }

        AppCard(label = "مبلغ (ریال)") {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = cleanNum(it) },
                visualTransformation = ThousandsSeparatorTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = appFieldColors(),
            )
        }

        AppCard(label = "توضیح (اختیاری)") {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = appFieldColors(),
            )
        }

        AppCard(label = "تاریخ") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AccountingDropdown(
                    options = (1350..1410).map { it to toFa(it) },
                    selected = txYear,
                    onSelect = { txYear = it },
                    modifier = Modifier.weight(1f),
                )
                AccountingDropdown(
                    options = faMonthNamesAccounting.mapIndexed { idx, name -> (idx + 1) to name },
                    selected = txMonth,
                    onSelect = { txMonth = it },
                    modifier = Modifier.weight(1f),
                )
                AccountingDropdown(
                    options = (1..31).map { it to toFa(it) },
                    selected = txDay,
                    onSelect = { txDay = it },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (error != null) {
            Text(text = error ?: "", color = AppDanger, fontSize = 12.sp)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GradientButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    error = if (amount <= 0) "مبلغ رو وارد کن" else null
                    if (error == null) {
                        onSubmit(selectedAccountId, type, amount, selectedCategory?.name, description.trim(), txYear, txMonth, txDay)
                    }
                },
                modifier = Modifier.weight(1f),
            ) { Text("ثبت") }
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("انصراف") }
        }
    }
}

@Composable
private fun BudgetSection(viewModel: AccountViewModel, onBack: () -> Unit) {
    val budgets by viewModel.budgets.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()
    val today = remember { JalaliCalendar.today() }
    val spend = remember(allTransactions) { viewModel.spendByCategory(allTransactions, today.y, today.m) }
    val expenseCats = remember { ir.sadteam.loancalc.data.expenseCategories }

    var editingCategory by remember { mutableStateOf<CategoryEntry?>(null) }
    var capText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp, 14.dp, 14.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت") }
                Text("بودجه‌بندیِ ماهانه", color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
            }
        }
        items(expenseCats, key = { it.name }) { cat ->
            val budget = budgets.firstOrNull { it.categoryName == cat.name }
            val spent = spend[cat.name] ?: 0.0
            AppCard(modifier = Modifier.animateItem()) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(cat.icon, contentDescription = null, tint = cat.color, modifier = Modifier.size(20.dp))
                            Text(cat.name, color = AppText, fontSize = 13.sp, modifier = Modifier.padding(start = 6.dp))
                        }
                        if (editingCategory?.name != cat.name) {
                            OutlinedButton(onClick = {
                                editingCategory = cat
                                capText = budget?.monthlyCap?.toLong()?.toString() ?: ""
                            }) {
                                Text(if (budget != null) "ویرایشِ سقف" else "تعیینِ سقف", fontSize = 11.sp)
                            }
                        }
                    }
                    if (editingCategory?.name == cat.name) {
                        Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = capText,
                                onValueChange = { capText = cleanNum(it) },
                                visualTransformation = ThousandsSeparatorTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = appFieldColors(),
                            )
                            GradientButton(
                                onClick = {
                                    val cap = capText.toDoubleOrNull() ?: 0.0
                                    if (cap > 0) viewModel.setBudget(cat.name, cap, budget?.id)
                                    editingCategory = null
                                },
                                modifier = Modifier.padding(start = 8.dp),
                            ) { Text("ذخیره", fontSize = 12.sp) }
                        }
                    } else if (budget != null) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            val fraction = if (budget.monthlyCap > 0) (spent / budget.monthlyCap).toFloat().coerceIn(0f, 1f) else 0f
                            val over = spent > budget.monthlyCap
                            LinearProgressIndicator(
                                progress = fraction,
                                modifier = Modifier.fillMaxWidth().clip(CircleShape),
                                color = if (over) AppDanger else AppPrimary,
                                trackColor = AppSurface2,
                            )
                            Text(
                                "${fmt(spent)} از ${fmt(budget.monthlyCap)} ریال" + if (over) " — بیشتر از سقف!" else "",
                                color = if (over) AppDanger else AppMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    } else {
                        Text("سقفی تعیین نشده", color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RecurringSection(viewModel: AccountViewModel, onBack: () -> Unit) {
    val payments by viewModel.recurringPayments.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    var showAddForm by remember { mutableStateOf(false) }
    var deletingPayment by remember { mutableStateOf<RecurringPaymentEntity?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp, 14.dp, 14.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت") }
                Text("پرداخت‌های تکراری", color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
            }
        }
        item {
            Text(
                "مثلِ اجاره‌خونه یا قسطِ ثابتِ ماهانه - تو روزِ مشخص‌شده هر ماه یادآوری می‌گیری، ولی " +
                    "خودش خودکار تراکنش ثبت نمی‌کنه.",
                color = AppMuted,
                fontSize = 11.5.sp,
            )
        }
        item {
            if (showAddForm) {
                AddRecurringForm(
                    accounts = accounts,
                    onCancel = { showAddForm = false },
                    onSubmit = { name, amount, type, category, accountId, day, offsets ->
                        viewModel.addRecurringPayment(name, amount, type, category, accountId, day, offsets)
                        showAddForm = false
                    },
                )
            } else {
                GradientButton(onClick = { showAddForm = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("+ پرداختِ تکراریِ جدید")
                }
            }
        }
        if (payments.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.AccountBalanceWallet,
                    title = "هنوز چیزی ثبت نشده",
                    description = "پرداخت‌های ثابتِ ماهانه‌ت (مثلِ اجاره) رو اینجا ثبت کن تا هر ماه یادآوری بگیری.",
                )
            }
        } else {
            items(payments, key = { it.id }) { p ->
                SwipeToDeleteRow(onDelete = { deletingPayment = p }, modifier = Modifier.animateItem()) {
                    AppCard {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(p.name, color = AppText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "هرماه، روزِ ${toFa(p.dayOfMonth)}" + (p.categoryName?.let { " — $it" } ?: ""),
                                    color = AppMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                            Text(
                                "${fmt(p.amount)} ریال",
                                color = if (p.type == TransactionType.DEPOSIT.name) AppPrimary else AppDanger,
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }
        }
    }

    deletingPayment?.let { p ->
        ConfirmDeleteDialog(
            title = "حذفِ پرداختِ تکراری",
            text = "«${p.name}» حذف بشه؟",
            onConfirm = { viewModel.deleteRecurringPayment(p); deletingPayment = null },
            onDismiss = { deletingPayment = null },
        )
    }
}

@Composable
private fun AddRecurringForm(
    accounts: List<AccountEntity>,
    onCancel: () -> Unit,
    onSubmit: (name: String, amount: Double, type: TransactionType, category: String?, accountId: Long?, dayOfMonth: Int, offsets: String?) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.WITHDRAWAL) }
    var selectedCategory by remember { mutableStateOf<CategoryEntry?>(null) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id) }
    var dayOfMonth by remember { mutableStateOf(1) }
    var offsets by remember { mutableStateOf<String?>(formatReminderOffsets(setOf(REMINDER_OFFSET_OPTIONS.first()))) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AppCard(label = "اسم (مثلاً «اجاره‌خونه»)") {
            OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = appFieldColors())
        }
        AppCard(label = "مبلغ (ریال)") {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = cleanNum(it) },
                visualTransformation = ThousandsSeparatorTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = appFieldColors(),
            )
        }
        AppCard(label = "نوع") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppChip(label = "هزینه", selected = type == TransactionType.WITHDRAWAL, onClick = { type = TransactionType.WITHDRAWAL; selectedCategory = null })
                AppChip(label = "درآمد", selected = type == TransactionType.DEPOSIT, onClick = { type = TransactionType.DEPOSIT; selectedCategory = null })
            }
        }
        AppCard(label = "دسته‌بندی (اختیاری)") {
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categoriesFor(type), key = { it.name }) { cat ->
                    AppChip(label = cat.name, selected = selectedCategory?.name == cat.name, onClick = { selectedCategory = cat })
                }
            }
        }
        AppCard(label = "روزِ ماه") {
            AccountingDropdown(options = (1..31).map { it to toFa(it) }, selected = dayOfMonth, onSelect = { dayOfMonth = it })
        }
        ReminderOverrideCard(currentOffsets = offsets, onChange = { offsets = it })
        if (error != null) Text(error ?: "", color = AppDanger, fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GradientButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    error = when {
                        name.isBlank() -> "اسم رو وارد کن"
                        amount <= 0 -> "مبلغ رو وارد کن"
                        else -> null
                    }
                    if (error == null) {
                        onSubmit(name.trim(), amount, type, selectedCategory?.name, selectedAccountId, dayOfMonth, offsets)
                    }
                },
                modifier = Modifier.weight(1f),
            ) { Text("ثبت") }
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("انصراف") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> AccountingDropdown(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selected }?.second ?: ""
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            modifier = Modifier.menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = appFieldColors(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, label) ->
                DropdownMenuItem(text = { Text(label) }, onClick = { onSelect(value); expanded = false })
            }
        }
    }
}
