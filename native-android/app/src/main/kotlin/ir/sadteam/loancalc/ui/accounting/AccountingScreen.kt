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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.REMINDER_OFFSET_OPTIONS
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.formatReminderOffsets
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.CategoryEntry
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.data.db.BudgetEntity
import ir.sadteam.loancalc.data.db.RecurringPaymentEntity
import ir.sadteam.loancalc.data.findCategory
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.account.AccountsScreen
import ir.sadteam.loancalc.ui.category.CategoryManagementScreen
import ir.sadteam.loancalc.ui.category.CategoryViewModel
import ir.sadteam.loancalc.ui.components.AccountPickerDialog
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.InlineJalaliDateRow
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.components.ReminderOverrideCard
import ir.sadteam.loancalc.ui.components.StaggerIn
import ir.sadteam.loancalc.ui.components.SwipeToDeleteRow
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.components.lazyColumnScrollbar
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val faMonthNamesAccounting = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

private val faWeekDayNamesAccounting = listOf("شنبه", "یک‌شنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")

/**
 * تبِ «دارایی» (قبلاً «حسابداری») - لیستِ حساب‌ها/تراکنش‌ها + جستجو + گزارشِ ماهانه‌ی خلاصه.
 * «بودجه‌بندی»/«گزارش‌گیری» تبِ مستقلِ خودشون شدن ([BudgetScreen]/[ReportScreen]) - رجوع کن به
 * CLAUDE.md، «بازسازیِ نوارِ پایین به ۵ تبِ رفرنس». «پرداختِ تکراری» هم تویِ همون [BudgetScreen]ه
 * (رجوع کن به CLAUDE.md، «تصمیمِ کاشیِ پرداختِ تکراری»).
 */
@Composable
fun AssetsScreen(
    viewModel: AccountViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
) {
    MainSection(viewModel = viewModel, categoryViewModel = categoryViewModel)
}

/** تبِ مستقلِ «بودجه» - قبلاً زیرصفحه‌ی حسابداری بود. «دسته‌بندی‌ها» هم اینجا زیرمجموعه‌ست (رجوع
 * کن به CLAUDE.md) چون مفهوماً به بودجه نزدیک‌تره تا دارایی. «پرداختِ تکراری» هم از تبِ «سررسید»
 * به اینجا منتقل شد (رجوع کن به CLAUDE.md، «تصمیمِ کاشیِ پرداختِ تکراری») - مفهوماً یه هزینه/درآمدِ
 * برنامه‌ریزی‌شده‌ی ماهانه‌ست، دقیقاً همون چیزی که بودجه‌بندی درباره‌شه؛ اینجا هم دیگه لازم نیست
 * تنها تو ردیفِ سومِ گریدِ «سررسید» بشینه. */
@Composable
fun BudgetScreen(
    viewModel: AccountViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
) {
    var screenKey by remember { mutableStateOf("main") }
    AnimatedContent(
        targetState = screenKey,
        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
        label = "budgetScreen",
    ) { key ->
        when (key) {
            "categories" -> CategoryManagementScreen(onBack = { screenKey = "main" }, viewModel = categoryViewModel)
            "recurring" -> RecurringSection(
                viewModel = viewModel,
                categoryViewModel = categoryViewModel,
                onBack = { screenKey = "main" },
            )
            else -> BudgetSection(
                viewModel = viewModel,
                categoryViewModel = categoryViewModel,
                onOpenCategories = { screenKey = "categories" },
                onOpenRecurring = { screenKey = "recurring" },
            )
        }
    }
}

/** تبِ مستقلِ «گزارش» - قبلاً زیرصفحه‌ی حسابداری بود، حالا خودش یه تبه، پس دیگه دکمه‌ی برگشت
 * نداره (رجوع کن به [ReportSection]). */
@Composable
fun ReportScreen(viewModel: AccountViewModel = hiltViewModel()) {
    ReportSection(viewModel = viewModel)
}

@Composable
private fun MainSection(
    viewModel: AccountViewModel,
    categoryViewModel: CategoryViewModel,
) {
    val accounts by viewModel.accounts.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()
    val privacyMode = LocalPrivacyMode.current
    val today = remember { JalaliCalendar.today() }
    val (income, expense) = remember(allTransactions) { viewModel.monthlyTotals(allTransactions, today.y, today.m) }
    val expenseCategories by categoryViewModel.expenseCategories.collectAsState()
    val incomeCategories by categoryViewModel.incomeCategories.collectAsState()
    val allCategoryEntries = remember(expenseCategories, incomeCategories) { expenseCategories + incomeCategories }

    var searchQuery by remember { mutableStateOf("") }
    var showAddForm by remember { mutableStateOf(false) }
    var deletingTx by remember { mutableStateOf<AccountTransactionEntity?>(null) }
    // خواسته‌ی صریحِ کاربر: افزودن/مدیریتِ حساب دیگه فقط از تنظیمات نباشه، مستقیم از همین تبِ «دارایی»
    // هم در دسترس باشه. `accountsAddMode` تعیین می‌کنه AccountsScreen مستقیم با فرمِ باز بیاد یا لیستِ عادی.
    var showAccountsScreen by remember { mutableStateOf(false) }
    var accountsAddMode by remember { mutableStateOf(false) }

    val banner = rememberInAppBanner()

    if (showAccountsScreen) {
        AccountsScreen(
            onBack = { showAccountsScreen = false },
            startInAddMode = accountsAddMode,
            viewModel = viewModel,
        )
        return
    }

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
    Box(modifier = Modifier.fillMaxSize()) {
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

        // خواسته‌ی صریحِ کاربر: مدیریت/افزودنِ حساب مستقیم همینجا (نه فقط تنظیمات) - یه ردیفِ چیپیِ
        // حساب‌های موجود + یه چیپِ «+ حساب جدید» که همیشه (حتی وقتی از قبل حساب داری) در دسترسه.
        item {
            StaggerIn(1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    accounts.forEach { acc ->
                        AppChip(label = acc.name, selected = false, onClick = { showAccountsScreen = true })
                    }
                    AppChip(
                        label = "+ حساب جدید",
                        selected = true,
                        onClick = { accountsAddMode = true; showAccountsScreen = true },
                    )
                }
            }
        }

        item {
            if (accounts.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.AccountBalanceWallet,
                    title = "اول یه حساب بساز",
                    description = "برای ثبتِ تراکنش، اول یه حساب (نقدی یا بانکی) بساز - قسط/چکِ " +
                        "پرداخت‌شده‌ت هم خودکار همون‌موقع میاد اینجا.",
                    actionLabel = "افزودنِ حساب",
                    onAction = { accountsAddMode = true; showAccountsScreen = true },
                )
            } else if (showAddForm) {
                AddTransactionForm(
                    accounts = accounts,
                    categoryViewModel = categoryViewModel,
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
                SwipeToDeleteRow(onDelete = { deletingTx = tx }, confirmDismiss = false, modifier = Modifier.animateItem()) {
                    AccountingTransactionRow(
                        tx = tx,
                        accountName = accountName,
                        privacyMode = privacyMode,
                        categories = allCategoryEntries,
                    )
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
    InAppBannerHost(banner)
    }
}

@Composable
private fun AccountingTransactionRow(
    tx: AccountTransactionEntity,
    accountName: String,
    privacyMode: Boolean,
    categories: List<CategoryEntry>,
) {
    val category = categories.find { it.name == tx.category } ?: findCategory(tx.category)
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
    categoryViewModel: CategoryViewModel,
    onCancel: () -> Unit,
    onSubmit: (accountId: Long, type: TransactionType, amount: Double, category: String?, desc: String, y: Int, m: Int, d: Int) -> Unit,
) {
    var type by remember { mutableStateOf(TransactionType.WITHDRAWAL) }
    val expenseCategories by categoryViewModel.expenseCategories.collectAsState()
    val incomeCategories by categoryViewModel.incomeCategories.collectAsState()
    val categoriesForType = if (type == TransactionType.WITHDRAWAL) expenseCategories else incomeCategories
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
                items(categoriesForType, key = { it.name }) { cat ->
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

        AppCard(label = "مبلغ") {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = cleanNum(it) },
                visualTransformation = ThousandsSeparatorTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = appFieldColors(),
                suffix = { Text("ریال", color = AppMuted, fontSize = 13.sp) },
            )
            val amountRial = amountText.toLongOrNull() ?: 0L
            if (amountRial > 0) {
                Text(
                    "${numberToWordsFa((amountRial / 10).toDouble())} تومان",
                    color = AppMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
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
    val today = remember { JalaliCalendar.today() }
    var viewYear by remember { mutableStateOf(today.y) }
    var viewMonth by remember { mutableStateOf(today.m) }
    val spend = remember(allTransactions, viewYear, viewMonth) {
        viewModel.spendByCategory(allTransactions, viewYear, viewMonth)
    }
    val expenseCats by categoryViewModel.expenseCategories.collectAsState()

    var editingCategory by remember { mutableStateOf<CategoryEntry?>(null) }
    var capText by remember { mutableStateOf("") }

    fun stepMonth(delta: Int) {
        var m = viewMonth + delta
        var y = viewYear
        while (m > 12) { m -= 12; y += 1 }
        while (m < 1) { m += 12; y -= 1 }
        viewMonth = m
        viewYear = y
    }

    val budgetedCats = remember(expenseCats, budgets) { expenseCats.filter { cat -> budgets.any { it.categoryName == cat.name } } }
    val totalCap = remember(budgets, budgetedCats) { budgetedCats.sumOf { cat -> budgets.first { it.categoryName == cat.name }.monthlyCap } }
    val totalSpent = remember(spend, budgetedCats) { budgetedCats.sumOf { spend[it.name] ?: 0.0 } }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp, 14.dp, 14.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { stepMonth(-1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "ماهِ قبل")
                }
                Text(
                    "${faMonthNamesAccounting[viewMonth - 1]} ${toFa(viewYear)}",
                    color = AppText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = { stepMonth(1) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "ماهِ بعد")
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.End),
            ) {
                OutlinedButton(onClick = onOpenRecurring) { Text("پرداختِ تکراری", fontSize = 12.sp) }
                OutlinedButton(onClick = onOpenCategories) { Text("دسته‌بندی‌ها", fontSize = 12.sp) }
            }
        }
        if (budgetedCats.isNotEmpty()) {
            item {
                BudgetRow(
                    icon = Icons.Filled.Add,
                    iconTint = AppPrimary,
                    name = "همه دسته‌بندی‌ها",
                    spent = totalSpent,
                    cap = totalCap,
                    onClick = null,
                )
            }
        }
        items(expenseCats, key = { it.name }) { cat ->
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
                        capText = budget?.monthlyCap?.toLong()?.toString() ?: ""
                    },
                )
                if (editingCategory?.name == cat.name) {
                    AppCard(label = "سقفِ ماهانه", modifier = Modifier.padding(top = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = capText,
                                onValueChange = { capText = cleanNum(it) },
                                visualTransformation = ThousandsSeparatorTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = appFieldColors(),
                                suffix = { Text("ریال", color = AppMuted, fontSize = 13.sp) },
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
                        val capRial = capText.toLongOrNull() ?: 0L
                        if (capRial > 0) {
                            Text(
                                "${numberToWordsFa((capRial / 10).toDouble())} تومان",
                                color = AppMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** یه ردیفِ بودجه‌ی تک (رجوع کن به BudgetSection بالا) - نوارِ پیشرفت + خرج‌شده/باقی‌مانده همیشه‌نمایان،
 * تپ رو ردیف (اگه [onClick] داده شده) فرمِ تعیین/ویرایشِ سقف رو باز می‌کنه. */
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
    AppCard(modifier = if (onClick != null) Modifier.pressScaleClickable(onClick = onClick) else Modifier) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                Text(name, color = AppText, fontSize = 13.sp, modifier = Modifier.padding(start = 6.dp))
            }
            LinearProgressIndicator(
                progress = fraction,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clip(CircleShape),
                color = if (over) AppDanger else AppPrimary,
                trackColor = AppSurface2,
            )
            if (cap != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("${fmt(spent)} ریال", color = AppMuted, fontSize = 11.sp)
                    Text(
                        if (over) "بیشتر از سقف!" else "باقی‌مانده ${fmt(cap - spent)} ریال",
                        color = if (over) AppDanger else AppText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            } else {
                Text("سقفی تعیین نشده", color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun RecurringSection(viewModel: AccountViewModel, categoryViewModel: CategoryViewModel, onBack: () -> Unit) {
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
                    categoryViewModel = categoryViewModel,
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
                SwipeToDeleteRow(onDelete = { deletingPayment = p }, confirmDismiss = false, modifier = Modifier.animateItem()) {
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
    categoryViewModel: CategoryViewModel,
    onCancel: () -> Unit,
    onSubmit: (name: String, amount: Double, type: TransactionType, category: String?, accountId: Long?, dayOfMonth: Int, offsets: String?) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.WITHDRAWAL) }
    val expenseCategories by categoryViewModel.expenseCategories.collectAsState()
    val incomeCategories by categoryViewModel.incomeCategories.collectAsState()
    val categoriesForType = if (type == TransactionType.WITHDRAWAL) expenseCategories else incomeCategories
    var selectedCategory by remember { mutableStateOf<CategoryEntry?>(null) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id) }
    var dayOfMonth by remember { mutableStateOf(1) }
    var offsets by remember { mutableStateOf<String?>(formatReminderOffsets(setOf(REMINDER_OFFSET_OPTIONS.first()))) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AppCard(label = "اسم (مثلاً «اجاره‌خونه»)") {
            OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = appFieldColors())
        }
        AppCard(label = "مبلغ") {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = cleanNum(it) },
                visualTransformation = ThousandsSeparatorTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = appFieldColors(),
                suffix = { Text("ریال", color = AppMuted, fontSize = 13.sp) },
            )
            val amountRial = amountText.toLongOrNull() ?: 0L
            if (amountRial > 0) {
                Text(
                    "${numberToWordsFa((amountRial / 10).toDouble())} تومان",
                    color = AppMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        AppCard(label = "نوع") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppChip(label = "هزینه", selected = type == TransactionType.WITHDRAWAL, onClick = { type = TransactionType.WITHDRAWAL; selectedCategory = null })
                AppChip(label = "درآمد", selected = type == TransactionType.DEPOSIT, onClick = { type = TransactionType.DEPOSIT; selectedCategory = null })
            }
        }
        AppCard(label = "دسته‌بندی (اختیاری)") {
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categoriesForType, key = { it.name }) { cat ->
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

/** ترتیبِ خطیِ روز/ماه/سالِ شمسی، فقط برای مقایسه‌ی «جلوتر/عقب‌تر» (نه محاسبه‌ی تقویمیِ واقعی) -
 * ماه‌های شمسی حداکثر ۳۱ روزن، پس ضریبِ ۳۲ برای day و ۴۰۰ برای year کاملاً کافیه. */
private fun PersianDate.ordinal(): Int = y * 400 + m * 32 + d

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

    AppCard {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedContent(
                targetState = viewDate,
                transitionSpec = {
                    val dir = if (goingForward) 1 else -1
                    (slideInHorizontally(tween(220)) { w -> dir * w } + fadeIn(tween(220))) togetherWith
                        (slideOutHorizontally(tween(220)) { w -> -dir * w } + fadeOut(tween(220)))
                },
                label = "reportDateText",
            ) { d ->
                val weekDay = faWeekDayNamesAccounting[JalaliCalendar.dayOfWeekSaturdayFirst(d)]
                Text(
                    "$weekDay، ${toFa(d.d)} ${faMonthNamesAccounting[d.m - 1]} ${toFa(d.y)}",
                    color = AppText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { onDateChange(PersianCalendar.addDays(viewDate, -1)) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "روزِ قبل")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (-3..3).forEach { offset ->
                        val d = PersianCalendar.addDays(viewDate, offset)
                        val selected = offset == 0
                        val weekDayShort = faWeekDayNamesAccounting[JalaliCalendar.dayOfWeekSaturdayFirst(d)].take(1)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) AppPrimary else Color.Transparent)
                                .pressScaleClickable(onClick = { onDateChange(d) })
                                .padding(vertical = 6.dp),
                        ) {
                            Text(
                                weekDayShort,
                                color = if (selected) Color.White else AppMuted,
                                fontSize = 10.sp,
                            )
                            Text(
                                toFa(d.d),
                                color = if (selected) Color.White else AppText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 1.dp),
                            )
                        }
                    }
                }
                IconButton(onClick = { onDateChange(PersianCalendar.addDays(viewDate, 1)) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "روزِ بعد")
                }
            }
        }
    }
}

/**
 * بازطراحیِ کاملِ تبِ گزارش (خواسته‌ی صریحِ کاربر با اسکرین‌شاتِ رفرنسِ Poolaki - «کل صفحه کن تاریخ
 * هست که»): بالای صفحه یه ناوبرِ روزانه، توگلِ دخل/خرج، کارتِ اختلاف+تعدادِ تراکنش، مبلغِ روزِ جاری با
 * اختلاف نسبت به دیروز، نمودارِ میله‌ایِ ۷روزه، و تفکیکِ دسته‌بندیِ همون روز («پولم کجا خرج شده؟»).
 * فیلترِ بازه‌ی دلخواه + خروجیِ PDF/اکسلِ قبلی (رجوع کن به CLAUDE.md، «تکمیلِ گزارش‌گیری») عمداً حذف
 * نشد - پشتِ یه دکمه‌ی «گزارشِ سفارشی و خروجی» جمع شد تا هم نمای روزانه‌ی جدید هم قابلیتِ قبلی بمونه.
 */
@Composable
private fun ReportSection(viewModel: AccountViewModel) {
    val accounts by viewModel.accounts.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()
    val today = remember { JalaliCalendar.today() }

    var viewDate by remember { mutableStateOf(today) }
    var showExpenseTab by remember { mutableStateOf(true) }
    var showCustomReport by remember { mutableStateOf(false) }

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
                DateRibbonHeader(viewDate = viewDate, onDateChange = { viewDate = it })
            }
        }
        item {
            StaggerIn(1) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppChip(label = "دخل", selected = !showExpenseTab, onClick = { showExpenseTab = false }, modifier = Modifier.weight(1f))
                        AppChip(label = "خرج", selected = showExpenseTab, onClick = { showExpenseTab = true }, modifier = Modifier.weight(1f))
                    }
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
                AppCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        AppChip(label = "${toFa(dayTx.size)} تراکنش", selected = false, onClick = {})
                        Column(horizontalAlignment = Alignment.End) {
                            Text("اختلاف دخل و خرج", color = AppMuted, fontSize = 11.sp)
                            val net = dayIncome - dayExpense
                            Text(
                                "${if (net < 0) "-" else ""}${fmt(kotlin.math.abs(net))} ریال",
                                color = if (net < 0) AppDanger else AppPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
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
                    Text(
                        "${fmt(activeAmount)} ریال",
                        color = AppText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    if (diffFromPrev != 0.0) {
                        Text(
                            "${if (diffFromPrev > 0) "↗" else "↘"} ${fmt(kotlin.math.abs(diffFromPrev))} ریال اختلاف با روز قبل",
                            color = AppMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp),
                        )
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
                    AppCard(label = if (showExpenseTab) "پولم کجا خرج شده؟" else "درآمدم از کجا اومده؟") {
                        Column {
                            dayCategoryBreakdown.forEach { (name, amount) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(name, color = AppText, fontSize = 12.5.sp)
                                    Text("${fmt(amount)} ریال", color = AppMuted, fontSize = 12.5.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            OutlinedButton(
                onClick = { showCustomReport = !showCustomReport },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (showCustomReport) "بستنِ گزارشِ سفارشی" else "گزارشِ سفارشی و خروجیِ PDF/اکسل", fontSize = 12.5.sp)
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
                        Text("${fmt(income)} ریال", color = AppPrimary, fontSize = 14.sp)
                    }
                    Column {
                        Text("هزینه", color = AppMuted, fontSize = 12.sp)
                        Text("${fmt(expense)} ریال", color = AppDanger, fontSize = 14.sp)
                    }
                    Column {
                        Text("مانده", color = AppMuted, fontSize = 12.sp)
                        Text(
                            "${fmt(income - expense)} ریال",
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
                                Text("${fmt(amount)} ریال", color = AppMuted, fontSize = 12.5.sp)
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { createPdfLauncher.launch("gozaresh-hesabdari.pdf") },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppPrimary),
                    enabled = filtered.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                ) { Text("دانلود PDF", fontSize = 12.sp) }
                OutlinedButton(
                    onClick = { createXlsxLauncher.launch("gozaresh-hesabdari.xlsx") },
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
    InAppBannerHost(banner)
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
