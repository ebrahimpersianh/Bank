package ir.sadteam.loancalc.ui.accounting

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.REMINDER_OFFSET_OPTIONS
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.DebtType
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
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.saveable.rememberSaveable
import ir.sadteam.loancalc.ui.asset.AssetSection
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppFab
import ir.sadteam.loancalc.ui.components.HeroPillBg
import ir.sadteam.loancalc.ui.components.HeroSmallPill
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.HeroTone
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.AppProgressBar
import ir.sadteam.loancalc.ui.components.BankBadge
import ir.sadteam.loancalc.ui.components.CategoryDonut
import ir.sadteam.loancalc.ui.components.DonutSlice
import ir.sadteam.loancalc.ui.components.countUpAmount
import ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.InlineJalaliDateRow
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.components.ProgressRing
import ir.sadteam.loancalc.ui.components.ReminderOverrideCard
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
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppRadius
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
    // تبِ «دارایی» دو نما داره: حساب‌کتاب‌ها (نقدی/بانکی) و دارایی‌های غیرنقدی (طلا/ارز/رمزارز).
    // خواسته‌ی صریحِ کاربر: بخشِ دارایی‌های غیرنقدی به اپ اضافه بشه - رجوع کن به ui/asset/.
    var showNonCash by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = showNonCash) { showNonCash = false }

    Column(modifier = Modifier.fillMaxSize()) {
        SegmentedToggle(
            options = listOf("حساب‌کتاب‌ها", "دارایی‌ها"),
            selectedIndex = if (showNonCash) 1 else 0,
            onSelect = { showNonCash = it == 1 },
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
        if (showNonCash) {
            AssetSection()
        } else {
            MainSection(viewModel = viewModel, categoryViewModel = categoryViewModel)
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
    var showSearch by remember { mutableStateOf(false) }
    var showAddForm by remember { mutableStateOf(false) }
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
        // کارتِ بزرگِ موجودیِ کل - هم‌الگو با کارتِ خانه، به‌علاوه‌ی آیکونِ جستجو که فیلدِ جستجو
        // رو باز/بسته می‌کنه (خواسته‌ی کاربر: «کلا شبیهِ اون بشه» - عکسِ مرجعش دقیقاً همین ترکیب رو
        // داشت: مانده‌ی کل بزرگ بالا + آیکونِ جستجو کنارش).
        item {
            StaggerIn(0) {
                // کارتِ قهرمانِ **تبِ دارایی** - سبزِ توپر طبقِ کارتِ `26b`ی طرح.
                // (یه دور اشتباهاً بنفش شد چون فکر کردم این بخش تبِ گزارشه؛ `MainSection`
                // در واقع نمای «حساب‌کتاب‌ها»ی تبِ داراییه - رجوع کن به `AssetsScreen`.)
                AppHeroCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column {
                            Text("داراییِ کل", color = HeroMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            // شمارشِ بالارونده - تو حالتِ خصوصی خاموشه (عدد پشتِ ••• مخفیه).
                            val shownBalance = countUpAmount(totalBalance, enabled = !privacyMode)
                            PrivacyCrossfade(privacyMode) { masked ->
                                Text(
                                    maskIfPrivate(masked, fmt(shownBalance)),
                                    color = Color.White,
                                    fontSize = 27.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(top = 3.dp),
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(HeroPillBg, CircleShape)
                                .pressScaleClickable(onClick = { showSearch = !showSearch }),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.Search, contentDescription = "جستجو", tint = Color.White, modifier = Modifier.size(17.dp))
                        }
                    }
                    // قرص‌های تفکیکِ زیرِ عدد - طرح سه‌تا داره (نقد/طلا/ارز). اینجا تفکیکِ واقعیِ
                    // دمِ‌دستی «تعدادِ حساب‌کتاب»ه؛ تفکیکِ دسته‌های دارایی تو نمای «دارایی‌ها»ی
                    // همین تب (AssetSection) هست.
                    Row(
                        modifier = Modifier.padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        HeroSmallPill("${toFa(accounts.size)} حساب‌کتاب")
                    }
                }
            }
        }

        if (showSearch) {
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
        }

        // خواسته‌ی صریحِ کاربر: مدیریت/افزودنِ حساب مستقیم همینجا (نه فقط تنظیمات)، به‌شکلِ کارت
        // (نه چیپِ ساده) با بجِ بانک/لوگو و مانده‌ی رنگیِ خودِ همون حساب - عیناً هم‌الگو با عکسِ
        // مرجعِ کاربر. یه ردیفِ افقیِ اسکرول‌شونده: اول دکمه‌ی «+ حساب جدید»، بعد کارتِ هر حساب.
        item {
            StaggerIn(1) {
                // هر دو نوعِ کارت (افزودن/حساب) از همون AppCardِ مشترک ساخته می‌شن و ارتفاعِ ثابتِ
                // یکسان دارن، تا با هم و با بقیه‌ی کارت‌های صفحه کاملاً یک‌دست باشن.
                val accountCardHeight = 116.dp
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AppCard(
                        modifier = Modifier
                            .width(126.dp)
                            .height(accountCardHeight)
                            .pressScaleClickable(onClick = { accountsAddMode = true; showAccountsScreen = true }),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Box(
                                modifier = Modifier.size(32.dp).background(AppPrimary.copy(alpha = 0.16f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(18.dp))
                            }
                            Text(
                                "حساب جدید",
                                color = AppText,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                    accounts.forEach { acc ->
                        val accBalance = remember(acc, allTransactions) { viewModel.balanceOf(acc, allTransactions) }
                        AppCard(
                            modifier = Modifier
                                .width(158.dp)
                                .height(accountCardHeight)
                                .pressScaleClickable(onClick = { showAccountsScreen = true }),
                        ) {
                            BankBadge(bankName = acc.bankName, size = 32.dp)
                            Text(
                                acc.name,
                                color = AppText,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                            PrivacyCrossfade(privacyMode) { masked ->
                                Text(
                                    "${maskIfPrivate(masked, fmt(accBalance))} ریال",
                                    color = if (accBalance < 0) AppDanger else AppPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            StaggerIn(2) {
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

        // ورودیِ «طلب و بدهی» - به‌جای یه کاشیِ صرفاً لینک، خودِ عددها رو هم نشون می‌ده تا با یه
        // نگاه معلوم باشه چقدر طلبکاری و چقدر بدهکار. تپ روش صفحه‌ی کاملش رو باز می‌کنه.
        item {
            StaggerIn(3) {
                AppCard(modifier = Modifier.pressScaleClickable(onClick = { showDebtScreen = true })) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(30.dp).background(AppPrimary.copy(alpha = 0.16f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.Handshake, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(17.dp))
                            }
                            Text("طلب و بدهی", color = AppText, fontSize = 13.sp, modifier = Modifier.padding(start = 10.dp))
                        }
                        Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = AppMuted, modifier = Modifier.size(18.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text("ازم طلبکارن", color = AppMuted, fontSize = 11.sp)
                            PrivacyCrossfade(privacyMode) { masked ->
                                Text(
                                    "${maskIfPrivate(masked, fmt(iOwe))} ریال",
                                    color = if (iOwe > 0) AppDanger else AppMuted,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("ازشون طلبکارم", color = AppMuted, fontSize = 11.sp)
                            PrivacyCrossfade(privacyMode) { masked ->
                                Text(
                                    "${maskIfPrivate(masked, fmt(owedToMe))} ریال",
                                    color = if (owedToMe > 0) AppPrimary else AppMuted,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
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
                        MonthNavArrow(icon = Icons.Filled.ChevronLeft, contentDescription = "ماهِ قبل") { stepMonth(-1) }
                        Text(
                            "${faMonthNamesAccounting[viewMonth - 1]} ${toFa(viewYear)}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                        )
                        MonthNavArrow(icon = Icons.Filled.ChevronRight, contentDescription = "ماهِ بعد") { stepMonth(1) }
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
                        "${fmt(totalSpent)} از سقفِ ${fmt(totalCap)} ریال",
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
                                capText = budget?.monthlyCap?.toLong()?.toString() ?: ""
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
                                    suffix = { Text("ریال", color = AppMuted, fontSize = 13.sp) },
                                )
                                val capRial = capText.toLongOrNull() ?: 0L
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
                                                viewModel.deleteBudget(budget)
                                                editingCategory = null
                                            },
                                            modifier = Modifier.weight(1f),
                                        ) { Text("حذفِ سقف", fontSize = 12.sp, color = AppDanger) }
                                    }
                                    GradientButton(
                                        onClick = {
                                            val cap = capText.toDoubleOrNull() ?: 0.0
                                            if (cap > 0) viewModel.setBudget(cat.name, cap, budget?.id)
                                            editingCategory = null
                                        },
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
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
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
    var selected by remember { mutableStateOf<CategoryEntry?>(null) }
    var capText by remember { mutableStateOf("") }
    val cat = selected

    AlertDialog(
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
                        value = capText,
                        onValueChange = { capText = cleanNum(it) },
                        visualTransformation = ThousandsSeparatorTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = appFieldColors(),
                        suffix = { Text("ریال", color = AppMuted, fontSize = 13.sp) },
                    )
                    val capRial = capText.toLongOrNull() ?: 0L
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
                    val capVal = capText.toDoubleOrNull() ?: 0.0
                    if (capVal > 0) onSave(cat, capVal)
                }) { Text("ذخیره") }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (cat != null) selected = null else onDismiss() }) {
                Text(if (cat != null) "بازگشت" else "انصراف")
            }
        },
    )
}

@Composable
private fun MonthNavArrow(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    // ⚠️ این فلش فقط داخلِ کارتِ **قهرمانِ رنگیِ** بودجه استفاده می‌شه، پس عمداً سفیده نه سبز -
    // سبز رو زمینه‌ی سبز اصلاً دیده نمی‌شد.
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(HeroPillBg)
            .pressScaleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = Color.White, modifier = Modifier.size(15.dp))
    }
}

/** دو کارتِ آبیِ ابزاری (پرداختِ تکراری/دسته‌بندی‌ها) - جایگزینِ دو OutlinedButtonِ معلقِ قبلی،
 * هم‌الگو با کارتِ آبیِ گزارشِ سفارشی تو ReportSection. */
@Composable
private fun BudgetToolTile(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier.pressScaleClickable(onClick = onClick), borderColor = AppInfo.copy(alpha = 0.30f)) {
        Box(
            modifier = Modifier.size(32.dp).background(AppInfo.copy(alpha = 0.20f), CircleShape),
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
                Text("${fmt(spent)} ریال خرج شده", color = AppMuted, fontSize = 10.5.sp)
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
                    Text("${fmt(spent)} ریال", color = ringColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
            SegmentedToggle(
                options = listOf("هزینه", "درآمد"),
                selectedIndex = if (type == TransactionType.WITHDRAWAL) 0 else 1,
                onSelect = { index ->
                    type = if (index == 0) TransactionType.WITHDRAWAL else TransactionType.DEPOSIT
                    selectedCategory = null
                },
            )
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

/** ردیفِ راهنمای رنگیِ کارتِ اختلافِ دخل/خرج - مربعِ ۸.dp + برچسب + مبلغ. */
@Composable
private fun IncomeExpenseLegendRow(color: Color, label: String, amount: Double) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(label, color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(start = 6.dp).weight(1f))
        Text("${fmt(amount)} ریال", color = AppText, fontSize = 11.sp)
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
                DateRibbonArrow(icon = Icons.Filled.ChevronLeft, contentDescription = "روزِ قبل") {
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
                DateRibbonArrow(icon = Icons.Filled.ChevronRight, contentDescription = "روزِ بعد") {
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
                            .clip(RoundedCornerShape(13.dp))
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
            .size(28.dp)
            .clip(CircleShape)
            .background(AppPrimary.copy(alpha = 0.14f))
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
            Text(firstMonthLabel, color = Color.White.copy(alpha = 0.62f), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
            Text(monthLabel, color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun ReportSection(viewModel: AccountViewModel, categoryViewModel: CategoryViewModel) {
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
                                "${if (net < 0) "-" else ""}${fmt(kotlin.math.abs(net))} ریال",
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
                                            maskIfPrivate(masked, fmt(breakdownTotal)),
                                            color = AppText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                    Text("ریال", color = AppMuted, fontSize = 9.sp)
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
                                    Text("${fmt(amount)} ریال", color = AppMuted, fontSize = 12.5.sp)
                                }
                            }
                        }
                    }
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
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(11.dp)).background(AppInfo.copy(alpha = 0.14f)),
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


/**
 * شیتِ «بودجه‌ی جدید» - خواسته‌ی صریحِ کاربر طبقِ اپِ مرجع: سه بخشِ پشتِ‌هم، نه یه دیالوگِ
 * دومرحله‌ای. حساب‌کتاب پیش‌فرض «همه حساب‌کتاب‌ها»ست (یعنی `null`)، دقیقاً مثلِ رفتارِ قبلیِ
 * بودجه‌ها - پس کسی که این فیلد رو دست نزنه، همون چیزی رو می‌گیره که قبلاً می‌گرفت.
 */
@Composable
private fun NewBudgetSheet(
    categories: List<CategoryEntry>,
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSave: (CategoryEntry, Double, Long?) -> Unit,
    initialCategory: CategoryEntry? = null,
) {
    var capText by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf(initialCategory) }
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var showAccountPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    if (showAccountPicker) {
        AlertDialog(
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
        AlertDialog(
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("بودجه‌ی جدید", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("مقدار بودجه", color = AppMuted, fontSize = 11.sp)
                OutlinedTextField(
                    value = capText,
                    onValueChange = { capText = cleanNum(it) },
                    visualTransformation = ThousandsSeparatorTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    suffix = { Text("ریال", color = AppMuted, fontSize = 13.sp) },
                )
                val rial = capText.toLongOrNull() ?: 0L
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
                    val cap = capText.toDoubleOrNull() ?: 0.0
                    val cat = selectedCat
                    if (cap > 0 && cat != null) onSave(cat, cap, selectedAccountId)
                },
                enabled = (capText.toDoubleOrNull() ?: 0.0) > 0 && selectedCat != null,
            ) { Text("ثبت بودجه") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
    )
}
