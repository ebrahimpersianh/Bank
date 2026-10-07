package ir.sadteam.loancalc.ui.accounting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.minimumInteractiveComponentSize
import ir.sadteam.loancalc.ui.components.AutoShrinkText
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.DebtType
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.CategoryEntry
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.data.findCategory
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.account.AccountsScreen
import ir.sadteam.loancalc.ui.category.CategoryManagementScreen
import ir.sadteam.loancalc.ui.category.CategoryViewModel
import androidx.activity.compose.BackHandler
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppFab
import ir.sadteam.loancalc.ui.components.HeroPillBg
import ir.sadteam.loancalc.ui.components.HeroSmallPill
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.components.SegmentedToggle
import ir.sadteam.loancalc.ui.components.StaggerIn
import ir.sadteam.loancalc.ui.components.SwipeToDeleteRow
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.components.lazyColumnScrollbar
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.debt.DebtScreen
import ir.sadteam.loancalc.ui.debt.DebtViewModel
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.RevealOnTap
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppText

internal val faMonthNamesAccounting = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)
internal val faWeekDayNamesAccounting = listOf("شنبه", "یک‌شنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")
/**
 * تبِ «دارایی» (قبلاً «حسابداری») - لیستِ حساب‌ها/تراکنش‌ها + جستجو + گزارشِ ماهانه‌ی خلاصه.
 * «بودجه‌بندی»/«گزارش‌گیری» تبِ مستقلِ خودشون شدن ([BudgetScreen]/[ReportScreen]) - رجوع کن به
 * CLAUDE.md، «بازسازیِ نوارِ پایین به ۵ تبِ رفرنس». «پرداختِ تکراری» هم تویِ همون [BudgetScreen]ه
 * (رجوع کن به CLAUDE.md، «تصمیمِ کاشیِ پرداختِ تکراری»).
 */
@Composable
fun AssetsScreen(
    onOpenAssetTab: () -> Unit = {},
    viewModel: AccountViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    assetViewModel: ir.sadteam.loancalc.ui.asset.AssetViewModel = hiltViewModel(),
) {
    // «دارایی‌های غیرنقدی» (طلا/ارز/رمزارز) دیگه اینجا نیست - تبِ مستقلِ «دارایی» خودِ
    // ir.sadteam.loancalc.ui.asset.AssetsTabScreen همون کار رو دقیق‌تر انجام می‌ده (مبلغ‌ها
    // تو هیرویِ خودِ اون تبن)؛ نگه‌داشتنِ دو لیستِ هم‌معنی یعنی هر تغییرِ آینده دوبار اعمال بشه.
    // جاش یه میان‌برِ کوچیک: فقط شمارشِ دارایی، نه مبلغ.
    val assets by assetViewModel.assets.collectAsState()
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            AssetTabShortcutCard(assetCount = assets.size, onClick = onOpenAssetTab)
        }
        MainSection(viewModel = viewModel, categoryViewModel = categoryViewModel)
    }
}
/**
 * «همه‌ی تراکنش‌ها» - دفترِ کامل با جستجو/فیلتر. از کاشیِ «تراکنش‌ها» و «مشاهده‌ی همه»ِ خانه باز
 * می‌شود؛ قبلاً هر دو به تبِ دارایی می‌رفتند که فهرستِ تراکنش ندارد (گزارشِ کاربر، ۸ مهر).
 */
@Composable
fun AllTransactionsScreen(
    onBack: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText) }
            Text("همه‌ی تراکنش‌ها", color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
        MainSection(viewModel = viewModel, categoryViewModel = categoryViewModel)
    }
}
/** میان‌برِ کوچیک به تبِ دارایی - فقط شمارشِ دارایی، بدونِ مبلغ (مبلغ تو هیرویِ خودِ اون تبه). */
@Composable
private fun AssetTabShortcutCard(assetCount: Int, onClick: () -> Unit) {
    AppCard(modifier = Modifier.pressScaleClickable(onClick = onClick)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).background(AppPrimaryPill, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Diamond, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                Text("دارایی من", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (assetCount == 0) "هنوز دارایی ثبت نکردی" else "${toFa(assetCount)} دارایی",
                    color = AppMuted,
                    fontSize = 11.sp,
                )
            }
            Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = AppMuted, modifier = Modifier.size(18.dp))
        }
    }
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
            // ⚠️ **بازنویسیِ فریمِ `27c`** - رجوع کن به `ui/accounting/BudgetTabScreen.kt`.
            else -> BudgetTabScreen(
                onOpenCategories = { screenKey = "categories" },
                onOpenRecurring = { screenKey = "recurring" },
                viewModel = viewModel,
                categoryViewModel = categoryViewModel,
            )
        }
    }
}
/** تبِ مستقلِ «گزارش» - قبلاً زیرصفحه‌ی حسابداری بود، حالا خودش یه تبه، پس دیگه دکمه‌ی برگشت
 * نداره (رجوع کن به [ReportSection]). */
@Composable
fun ReportScreen(
    viewModel: AccountViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
) {
    // خواسته‌ی صریحِ کاربر: «گزارش خوبه ولی اون پایین به‌اضافه باشه که با زدنش همون صفحه‌ی
    // دخل/خرج/جابجایی بیاد» - همون شیتِ مشترکِ تبِ خانه، نه یه فرمِ جدا.
    var showNewTransaction by remember { mutableStateOf(false) }
    if (showNewTransaction) {
        NewTransactionSheet(onDismiss = { showNewTransaction = false })
        return
    }
    Box(modifier = Modifier.fillMaxSize()) {
        ReportSection(viewModel = viewModel, categoryViewModel = categoryViewModel)
        AppFab(
            onClick = { showNewTransaction = true },
            contentDescription = "افزودنِ تراکنش",
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        )
    }
}
@Composable
private fun MainSection(
    viewModel: AccountViewModel,
    categoryViewModel: CategoryViewModel,
    debtViewModel: DebtViewModel = hiltViewModel(),
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
    // جستجوی پیشرفته (پیشنهادِ گزارشِ مقایسه با پولکی): نوع، حساب، بازه‌ی مبلغ (تومان).
    var typeFilter by remember { mutableStateOf<String?>(null) } // WITHDRAWAL / DEPOSIT / transfer
    var accountFilter by remember { mutableStateOf<Long?>(null) }
    var minToman by remember { mutableStateOf("") }
    var maxToman by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var showAddForm by remember { mutableStateOf(false) }
    var showNewTx by remember { mutableStateOf(false) }
    var deletingTx by remember { mutableStateOf<AccountTransactionEntity?>(null) }
    val totalBalance = remember(accounts, allTransactions) { accounts.sumOf { viewModel.balanceOf(it, allTransactions) } }
    // خواسته‌ی صریحِ کاربر: افزودن/مدیریتِ حساب دیگه فقط از تنظیمات نباشه، مستقیم از همین تبِ «دارایی»
    // هم در دسترس باشه. `accountsAddMode` تعیین می‌کنه AccountsScreen مستقیم با فرمِ باز بیاد یا لیستِ عادی.
    var showAccountsScreen by remember { mutableStateOf(false) }
    var accountsAddMode by remember { mutableStateOf(false) }
    // «طلب و بدهی» تا الان فقط از یه کاشی تو تبِ «سررسید» باز می‌شد و کاربر گفت اونجا پیدا نمی‌شه.
    // حالا از همین تبِ «دارایی» هم باز می‌شه - مفهوماً هم اینجا درست‌تره (طلب یه دارایی و بدهی یه
    // تعهده). کاشیِ تبِ سررسید عمداً حذف نشد: اون گریدِ ۶کاشیه با رفرنسِ کاربر مو‌به‌مو تطبیق داده
    // شده بود و حذفِ یه کاشی به‌همش می‌ریخت.
    var showDebtScreen by remember { mutableStateOf(false) }

    val banner = rememberInAppBanner()

    if (showAccountsScreen) {
        AccountsScreen(
            onBack = { showAccountsScreen = false },
            startInAddMode = accountsAddMode,
            viewModel = viewModel,
        )
        return
    }

    if (showDebtScreen) {
        DebtScreen(onBack = { showDebtScreen = false })
        return
    }

    // خلاصه‌ی طلب/بدهی برای کارتِ پایین‌تر - فقط ردیف‌های تسویه‌نشده.
    val debtRows by debtViewModel.debts.collectAsState()
    val owedToMe = remember(debtRows) {
        debtRows.filter { !it.settled && it.type == DebtType.OWED_TO_ME.name }.sumOf { it.amount }
    }
    val iOwe = remember(debtRows) {
        debtRows.filter { !it.settled && it.type == DebtType.I_OWE.name }.sumOf { it.amount }
    }

    val filtered = remember(allTransactions, searchQuery, typeFilter, accountFilter, minToman, maxToman) {
        val q = searchQuery.trim()
        val minRial = minToman.toLongOrNull()?.let { it * 10.0 }
        val maxRial = maxToman.toLongOrNull()?.let { it * 10.0 }
        allTransactions.filter {
            (q.isEmpty() || it.description.contains(q, ignoreCase = true) || (it.category?.contains(q, ignoreCase = true) == true) || (it.tags?.contains(q.removePrefix("#"), ignoreCase = true) == true)) &&
                when (typeFilter) {
                    null -> true
                    ir.sadteam.loancalc.data.SOURCE_TYPE_TRANSFER -> it.sourceType == ir.sadteam.loancalc.data.SOURCE_TYPE_TRANSFER
                    else -> it.type == typeFilter && it.sourceType != ir.sadteam.loancalc.data.SOURCE_TYPE_TRANSFER
                } &&
                (accountFilter == null || it.accountId == accountFilter) &&
                (minRial == null || it.amount >= minRial) &&
                (maxRial == null || it.amount <= maxRial)
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
        // بازطراحیِ ۸ مهر: این بخش حالا فقط صفحه‌ی «همه‌ی تراکنش‌ها» است - کارت‌های حساب، طلب/بدهی
        // و موجودیِ کل جایشان در تبِ دارایی است. ⚠️ عددِ هیرو قبلاً **ریال** بود (۱۰ برابر).
        item {
            StaggerIn(0) {
                AppHeroCard {
                    Text("خرجِ ${faMonthNamesAccounting[today.m - 1]}", color = HeroMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    RevealOnTap(privacyMode) { masked ->
                        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 4.dp)) {
                            Text(maskIfPrivate(masked, fmt(expense / 10)), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, maxLines = 1)
                            Text("تومان", color = HeroMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp, bottom = 5.dp))
                        }
                    }
                    Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        HeroSmallPill("درآمد ${if (privacyMode) "•••" else toFa(fmt(income / 10))}")
                        HeroSmallPill("${toFa(allTransactions.count { it.year == today.y && it.month == today.m })} تراکنش این ماه")
                    }
                }
            }
        }
        item {
            ir.sadteam.loancalc.ui.components.PillSearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "جستجو تو تراکنش‌ها…",
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    listOf(
                        null to "همه",
                        TransactionType.WITHDRAWAL.name to "خرج",
                        TransactionType.DEPOSIT.name to "درآمد",
                        ir.sadteam.loancalc.data.SOURCE_TYPE_TRANSFER to "جابه‌جایی",
                    ).forEach { (key, label) ->
                        AppChip(label, typeFilter == key, onClick = { typeFilter = key })
                    }
                    AppChip("فیلترِ مبلغ", showSearch || minToman.isNotEmpty() || maxToman.isNotEmpty(), onClick = { showSearch = !showSearch })
                }
                if (accounts.size > 1) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        AppChip("همه‌ی حساب‌ها", accountFilter == null, onClick = { accountFilter = null })
                        accounts.forEach { acc ->
                            AppChip(acc.name, accountFilter == acc.id, onClick = { accountFilter = acc.id })
                        }
                    }
                }
                if (showSearch) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = minToman,
                            onValueChange = { minToman = cleanNum(it).take(13) },
                            placeholder = { Text("از مبلغ (تومان)") },
                            visualTransformation = ThousandsSeparatorTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = ir.sadteam.loancalc.ui.components.AppFieldShape,
                            colors = appFieldColors(),
                        )
                        OutlinedTextField(
                            value = maxToman,
                            onValueChange = { maxToman = cleanNum(it).take(13) },
                            placeholder = { Text("تا مبلغ (تومان)") },
                            visualTransformation = ThousandsSeparatorTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = ir.sadteam.loancalc.ui.components.AppFieldShape,
                            colors = appFieldColors(),
                        )
                    }
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
                // بازبینیِ ۹ مهر: همان فرمِ مشترکِ برنامه (با سقفِ نسخه‌ی رایگان و برچسب/رسید)، نه فرمِ قدیمیِ ساده.
                GradientButton(onClick = { showNewTx = true }, modifier = Modifier.fillMaxWidth()) {
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
                        onUnpair = { viewModel.unpairTransfer(tx) },
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
    if (showNewTx) {
        androidx.activity.compose.BackHandler { showNewTx = false }
        NewTransactionSheet(onDismiss = { showNewTx = false })
    }
    }
}
@Composable
private fun AccountingTransactionRow(
    tx: AccountTransactionEntity,
    accountName: String,
    privacyMode: Boolean,
    categories: List<CategoryEntry>,
    onUnpair: () -> Unit = {},
) {
    val category = categories.find { it.name == tx.category } ?: findCategory(tx.category)
    val isIncome = tx.type == TransactionType.DEPOSIT.name
    AppCard {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val tint = category?.color ?: if (isIncome) AppPrimary else AppDanger
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(AppRadius.icon)).background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    category?.icon ?: if (isIncome) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(category?.name ?: (if (isIncome) "واریز" else "برداشت"), color = AppText, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (tx.description.isNotBlank()) {
                    Text(tx.description, color = AppMuted, fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
                }
                Text(
                    listOfNotNull("${toFa(tx.day)} ${faMonthNamesAccounting[tx.month - 1]}", accountName, ir.sadteam.loancalc.ui.account.timeOfTransaction(tx)).joinToString(" · "),
                    color = AppLabel,
                    fontSize = 11.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 2.dp),
                )
                // برچسب‌ها، رسید و بازپرداخت (قابلیت‌های ۶ مهر).
                val extras = buildList {
                    tx.tags?.split(',')?.filter { it.isNotBlank() }?.forEach { add("#$it") }
                    if (tx.receiptPath != null) add("📎 رسید")
                    if (tx.reimbursable) add("↩ بازپرداختی")
                }
                if (extras.isNotEmpty()) {
                    Text(extras.joinToString("  "), color = AppPrimary, fontSize = 10.5.sp, modifier = Modifier.padding(top = 3.dp))
                }
                // جابه‌جاییِ تشخیصِ خودکار اشتباه‌پذیر است؛ یک تپ برای برگرداندنش.
                if (tx.sourceType == ir.sadteam.loancalc.data.SOURCE_TYPE_TRANSFER && tx.description.contains("تشخیصِ خودکار")) {
                    Text(
                        "نه، این جابه‌جایی نبود",
                        color = AppPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .heightIn(min = 32.dp)
                            .clickable(onClick = onUnpair),
                    )
                }
            }
            // ⚠️ مبلغ قبلاً **ریال** و بی‌واحد بود و در دو خط می‌شکست.
            PrivacyCrossfade(privacyMode) { masked ->
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 8.dp)) {
                    AutoShrinkText(
                        "${if (isIncome) "+" else "−"}${maskIfPrivate(masked, fmt(tx.amount / 10))}",
                        color = if (isIncome) AppPrimary else AppDanger,
                        maxFontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text("تومان", color = AppMuted, fontSize = 10.sp)
                }
            }
        }
    }
}
@Composable
internal fun AddTransactionForm(
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
            SegmentedToggle(
                options = listOf("هزینه", "درآمد"),
                selectedIndex = if (type == TransactionType.WITHDRAWAL) 0 else 1,
                onSelect = { index ->
                    type = if (index == 0) TransactionType.WITHDRAWAL else TransactionType.DEPOSIT
                    selectedCategory = null
                },
            )
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
                suffix = { Text("تومان", color = AppMuted, fontSize = 13.sp) }, shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
            val amountRial = (amountText.toLongOrNull() ?: 0L) * 10
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
                colors = appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
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
                    val amount = (amountText.toDoubleOrNull() ?: 0.0) * 10
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
internal fun MonthNavArrow(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    // ⚠️ این فلش فقط داخلِ کارتِ **قهرمانِ رنگیِ** بودجه استفاده می‌شه، پس عمداً سفیده نه سبز -
    // سبز رو زمینه‌ی سبز اصلاً دیده نمی‌شد.
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(28.dp)
            .clip(CircleShape)
            .background(HeroPillBg)
            .pressScaleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = Color.White, modifier = Modifier.size(15.dp))
    }
}
/** ترتیبِ خطیِ روز/ماه/سالِ شمسی، فقط برای مقایسه‌ی «جلوتر/عقب‌تر» (نه محاسبه‌ی تقویمیِ واقعی) -
 * ماه‌های شمسی حداکثر ۳۱ روزن، پس ضریبِ ۳۲ برای day و ۴۰۰ برای year کاملاً کافیه. */
internal fun PersianDate.ordinal(): Int = y * 400 + m * 32 + d
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> AccountingDropdown(
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
            colors = appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, label) ->
                DropdownMenuItem(text = { Text(label) }, onClick = { onSelect(value); expanded = false })
            }
        }
    }
}
