package ir.sadteam.loancalc

import androidx.navigation.NavGraphBuilder
import ir.sadteam.loancalc.ui.components.guideTarget
import ir.sadteam.loancalc.ui.theme.AppLine
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.composable
import ir.sadteam.loancalc.notifications.DeepLinkViewModel
import ir.sadteam.loancalc.notifications.PendingChequeDeepLink
import ir.sadteam.loancalc.ui.accounting.AssetsScreen
import ir.sadteam.loancalc.ui.accounting.BudgetScreen
import ir.sadteam.loancalc.ui.accounting.ReportTabScreen
import ir.sadteam.loancalc.ui.asset.AssetsTabScreen
import ir.sadteam.loancalc.ui.cheque.ChequeScreen
import ir.sadteam.loancalc.ui.calendar.FinancialCalendarScreen
import ir.sadteam.loancalc.ui.stats.StatsScreen
import ir.sadteam.loancalc.ui.components.ShortcutDrawerHandle
import ir.sadteam.loancalc.ui.debt.DebtScreen
import ir.sadteam.loancalc.ui.due.DueTabScreen
import ir.sadteam.loancalc.ui.home.HomeScreen
import ir.sadteam.loancalc.ui.nav.NavDestination
import ir.sadteam.loancalc.ui.nav.NavSlotsViewModel
import ir.sadteam.loancalc.ui.note.NoteScreen
import ir.sadteam.loancalc.ui.support.BugReportScreen
import ir.sadteam.loancalc.ui.nav.NavSuggestionCard
import ir.sadteam.loancalc.ui.inbox.InboxScreen
import ir.sadteam.loancalc.ui.goal.SavingsGoalScreen
import ir.sadteam.loancalc.ui.category.CategoryManagementScreen
import ir.sadteam.loancalc.ui.history.CalculationHistoryScreen
import ir.sadteam.loancalc.ui.account.AccountsScreen
import ir.sadteam.loancalc.ui.cheque.SayadInquiryScreen
import ir.sadteam.loancalc.ui.tools.ToolsHubScreen
import ir.sadteam.loancalc.ui.archive.AnnualArchiveScreen
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface
import androidx.compose.runtime.MutableState

@Composable
internal fun AppBottomBar(
    currentRoute: String,
    navSlots: List<NavDestination>,
    navigateTo: (String) -> Unit,
    tabResetKeys: androidx.compose.runtime.snapshots.SnapshotStateMap<BottomTab, Int>,
    tourBounds: androidx.compose.runtime.snapshots.SnapshotStateMap<TourTarget, androidx.compose.ui.geometry.Rect>,
    shortcutDrawerOpenState: MutableState<Boolean>,
    bottomBarVisibleState: MutableState<Boolean>,
) {
    var shortcutDrawerOpen by shortcutDrawerOpenState
    var bottomBarVisible by bottomBarVisibleState
                AnimatedVisibility(
                    visible = bottomBarVisible,
                    enter = slideInVertically(tween(220)) { it } + expandVertically(tween(220)),
                    exit = slideOutVertically(tween(180)) { it } + shrinkVertically(tween(180)),
                ) {
                    // 🎨 **نوارِ شناورِ گردگوشه** (طرحِ ChatGPT، ۲ مهر): سطحِ سفید با حاشیه‌ی ظریف و
                    // سایه‌ی نرم، با فاصله از لبه‌ها؛ عرض همیشه `fillMaxWidth` منهای حاشیه، پس در
                    // هیچ عرضی بیرون نمی‌زند. پنج خانه با وزنِ برابر.
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(start = 12.dp, end = 12.dp, bottom = 1.dp),
                    ) {
                        val navShape = RoundedCornerShape(28.dp)
                        // خواسته‌ی کاربر (۳ مهر): خطِ آبی **روی خودِ نوار** بنشیند، نه شناور بالایش.
                        // ۱۴ مهر: ۱۲dp فضای لمسِ بالای نوار برای دستگیره - قبلاً فقط ۱۴dp روی لبه بود و گرفته نمی‌شد.
                        Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .fillMaxWidth()
                                .shadow(12.dp, navShape, ambientColor = AppPrimary.copy(alpha = 0.25f), spotColor = AppPrimary.copy(alpha = 0.25f))
                                .clip(navShape)
                                .background(AppSurface)
                                .border(1.dp, AppLine, navShape)
                                .padding(start = 6.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                        // **بخشِ ۴۱**: دیگه `BottomTab.entries` نیست - چیدمان از [NavSlotsViewModel]
                        // میاد. اسلاتِ ۰ همیشه «خانه»ست (قفلِ `41c`، تو `NavDestination.sanitize`).
                        navSlots.forEach { dest ->
                            BottomNavItem(
                                dest = dest,
                                selected = currentRoute == dest.route,
                                onPositioned = { rect -> registerTabTourBounds(dest.route, rect, tourBounds) },
                                onLongClick = { shortcutDrawerOpen = true },
                                onClick = {
                                    val tab = BottomTab.entries.firstOrNull { it.route == dest.route }
                                    if (dest.route == currentRoute) {
                                        if (tab != null) tabResetKeys[tab] = (tabResetKeys[tab] ?: 0) + 1
                                    } else {
                                        navigateTo(dest.route)
                                    }
                                },
                            )
                        }
                        }
                        // دستگیره‌ی کشوی میان‌بُر - کشیدنِ به بالا یا تپ بازش می‌کند (`31c`). باریک است
                        // (۱۲۰dp) تا لمسِ بالای تب‌ها را نگیرد.
                        if (!ir.sadteam.loancalc.ui.privacy.LocalSimpleMode.current) ShortcutDrawerHandle(
                            onOpen = { shortcutDrawerOpen = true },
                            modifier = Modifier.align(Alignment.TopCenter).width(150.dp).then(Modifier.guideTarget("shortcuts")),
                        )
                        }
                    }
                }
}

internal fun NavGraphBuilder.appRoutes(
    deepLinkViewModel: DeepLinkViewModel,
    navController: androidx.navigation.NavHostController,
    navSlotsViewModel: NavSlotsViewModel,
    navSlots: List<NavDestination>,
    navSuggestion: ir.sadteam.loancalc.core.NavSuggestion.Result?,
    navigateTo: (String) -> Unit,
    deepLinkLoanId: Long?,
    tabResetKeys: androidx.compose.runtime.snapshots.SnapshotStateMap<BottomTab, Int>,
    showSettingsState: MutableState<Boolean>,
    showInboxState: MutableState<Boolean>,
    showGlobalSearchState: MutableState<Boolean>,
    showAllTransactionsState: MutableState<Boolean>,
    shortcutDrawerOpenState: MutableState<Boolean>,
    requestedLoanSubTabState: MutableState<LoanSubTab?>,
    bottomBarVisibleState: MutableState<Boolean>,
) {
    var showSettings by showSettingsState
    var showInbox by showInboxState
    var showGlobalSearch by showGlobalSearchState
    var showAllTransactions by showAllTransactionsState
    var shortcutDrawerOpen by shortcutDrawerOpenState
    var requestedLoanSubTab by requestedLoanSubTabState
    var bottomBarVisible by bottomBarVisibleState
                composable(BottomTab.HOME.route) {
                    key(tabResetKeys[BottomTab.HOME] ?: 0) {
                        // route به‌عنوانِ رشته پاس داده می‌شه (نه خودِ enumِ BottomTab) چون
                        // BottomTab تویِ همین فایلِ MainActivity.kt خصوصیه و HomeScreen تو یه
                        // فایلِ جدا (ui/home/HomeScreen.kt) زندگی می‌کنه.
                        HomeScreen(
                            onNavigateToRoute = ::navigateTo,
                            onOpenSettings = { showSettings = true },
                            onOpenInbox = { showInbox = true },
                            onOpenSearch = { showGlobalSearch = true },
                            onOpenTransactions = { showAllTransactions = true },
                            onOpenLoan = { deepLinkViewModel.openLoan(it) },
                            // نوعِ صریح عمدیه: بدونش `let` لامبدا رو `() -> Unit`ِ ساده حساب
                            // می‌کنه و به `@Composable () -> Unit` نمی‌خوره.
                            navSuggestionSlot = navSuggestion?.let { suggestion ->
                                @Composable {
                                    NavSuggestionCard(
                                        suggestion = suggestion,
                                        currentSlots = navSlots,
                                        onApply = { navSlotsViewModel.applySuggestion(suggestion) },
                                        onEdit = {
                                            navSlotsViewModel.snoozeSuggestion()
                                            shortcutDrawerOpen = true
                                        },
                                        onDismiss = { navSlotsViewModel.dismissSuggestion(suggestion) },
                                    )
                                }
                            },
                        )
                    }
                }
                composable(BottomTab.ASSETS.route) {
                    // ⚠️ **بازنویسیِ فریمِ `26b`**: `AssetsScreen`ِ قدیمی دو نمای جدا با تاگل
                    // بود؛ فریم یه صفحه‌ی پیوسته‌ست - رجوع کن به `ui/asset/AssetsTabScreen.kt`.
                    key(tabResetKeys[BottomTab.ASSETS] ?: 0) { AssetsTabScreen() }
                }
                composable(BottomTab.REPORT.route) {
                    // ⚠️ **بازنویسیِ فریمِ `26a`** - رجوع کن به `ui/accounting/ReportTabScreen.kt`.
                    key(tabResetKeys[BottomTab.REPORT] ?: 0) {
                        ReportTabScreen(
                            onOpenLoanStats = { navigateTo(LOAN_STATS_ROUTE) },
                            onOpenChequeReport = { navigateTo(CHEQUE_REPORT_ROUTE) },
                        )
                    }
                }
                composable(BottomTab.BUDGET.route) {
                    key(tabResetKeys[BottomTab.BUDGET] ?: 0) { BudgetScreen() }
                }
                composable(BottomTab.DUE.route) {
                    // ⚠️ **بازنویسیِ فریمِ `3a`** - رجوع کن به `ui/due/DueTabScreen.kt`.
                    key(tabResetKeys[BottomTab.DUE] ?: 0) {
                        DueTabScreen(
                            onAddCheque = { navigateTo(CHEQUE_ROUTE) },
                            onAddLoan = { navigateTo(LOAN_ROUTE) },
                            // تپ روی ردیفِ چک همان چک را باز می‌کند. تپ روی ردیفِ
                            // طلب‌وبدهی فعلاً خودِ صفحه را باز می‌کند، نه آن طرفِ‌حسابِ
                            // مشخص - `DebtScreen` هیچ ورودیِ شناسه‌ای ندارد.
                            onOpenCheque = { id ->
                                deepLinkViewModel.openCheque(id)
                                navigateTo(CHEQUE_ROUTE)
                            },
                            onOpenDebt = { id ->
                                deepLinkViewModel.openDebt(id)
                                navigateTo(DEBT_ROUTE)
                            },
                            // تپ رو ردیفِ قسط → همون وام تو «وام‌های من» باز می‌شه. از همون
                            // مسیرِ دیپ‌لینکِ نوتیفیکیشن استفاده می‌کنه تا منطق یکی بمونه.
                            onOpenLoan = { loanId -> deepLinkViewModel.openLoan(loanId) },
                        )
                    }
                }
                // «وام» و «چک» دیگه تبِ نوارِ پایین نیستن (رجوع کن به کامنتِ بالای BottomTab) - از
                // تبِ «سررسید»/«خانه» به‌عنوانِ صفحه‌ی پوش‌شده باز می‌شن، پس خودشون یه دکمه‌ی
                // برگشتِ واقعی لازم دارن (رجوع کن به onBack پایین).
                composable(LOAN_ROUTE) {
                    LoanTab(
                        onBack = { navigateTo(BottomTab.HOME.route) },
                        requestedSubTab = requestedLoanSubTab,
                        onManualAddFabPositioned = {},
                        onBottomBarVisibilityChanged = { visible -> bottomBarVisible = visible },
                        deepLinkLoanId = deepLinkLoanId,
                        onDeepLinkConsumed = { deepLinkViewModel.consume() },
                        onOpenSettings = { showSettings = true },
                    )
                }
                composable(CHEQUE_ROUTE) {
                    // شناسه از `PendingChequeDeepLink` می‌آید - هم تپِ ردیفِ سررسید و هم
                    // اعلانِ سررسیدِ چک از همین‌جا می‌گذرند، پس منطق یکی می‌ماند.
                    val openId = deepLinkViewModel.pendingChequeId.collectAsState().value
                    LaunchedEffect(openId) { if (openId != null) deepLinkViewModel.consumeCheque() }
                    ChequeScreen(
                        onBack = { navigateTo(BottomTab.HOME.route) },
                        standalone = false,
                        initialChequeId = openId,
                    )
                }
                composable(LOAN_STATS_ROUTE) {
                    StatsScreen(onBack = { navigateTo(BottomTab.REPORT.route) })
                }
                composable(CHEQUE_REPORT_ROUTE) {
                    ChequeScreen(
                        onBack = { navigateTo(BottomTab.REPORT.route) },
                        standalone = false,
                        initialReport = true,
                    )
                }
                composable(DEBT_ROUTE) {
                    val openCounterparty = deepLinkViewModel.pendingCounterpartyId.collectAsState().value
                    LaunchedEffect(openCounterparty) {
                        if (openCounterparty != null) deepLinkViewModel.consumeDebt()
                    }
                    DebtScreen(
                        onBack = { navigateTo(BottomTab.DUE.route) },
                        initialCounterpartyId = openCounterparty,
                    )
                }
                composable(DANG_ROUTE) {
                    DebtScreen(onBack = { navigateTo(BottomTab.HOME.route) }, startInDang = true)
                }
                composable(TOOLS_ROUTE) {
                    ToolsHubScreen(
                        onBack = { navigateTo(BottomTab.HOME.route) },
                        onOpenArchive = { navigateTo(ANNUAL_ARCHIVE_ROUTE) },
                        onOpenDeng = { navigateTo(DANG_ROUTE) },
                        onOpenSayad = { navigateTo(SAYAD_INQUIRY_ROUTE) },
                        onOpenNotes = { navigateTo(NOTES_ROUTE) },
                    )
                }
                composable(NOTES_ROUTE) {
                    NoteScreen(onBack = { navController.popBackStack() })
                }
                composable(BUG_REPORT_ROUTE) {
                    BugReportScreen(onBack = { navigateTo(BottomTab.HOME.route) })
                }
                composable(SAVINGS_GOAL_ROUTE) {
                    SavingsGoalScreen(onBack = { navigateTo(BottomTab.BUDGET.route) })
                }
                composable(CATEGORIES_ROUTE) {
                    CategoryManagementScreen(onBack = { navigateTo(BottomTab.REPORT.route) })
                }
                composable(ACCOUNTS_ROUTE) {
                    AccountsScreen(onBack = { navigateTo(BottomTab.ASSETS.route) })
                }
                composable(ADD_ACCOUNT_ROUTE) {
                    AccountsScreen(onBack = { navigateTo(BottomTab.ASSETS.route) }, startInAddMode = true)
                }
                composable(STATEMENT_ROUTE) {
                    ir.sadteam.loancalc.ui.account.StatementImportScreen(onBack = { navigateTo(BottomTab.ASSETS.route) })
                }
                composable(PAYOFF_ROUTE) {
                    ir.sadteam.loancalc.ui.debt.DebtPayoffScreen(onBack = { navigateTo(LOAN_ROUTE) })
                }
                composable(BILLS_ROUTE) {
                    ir.sadteam.loancalc.ui.extras.BillsScreen(onBack = { navigateTo(BottomTab.BUDGET.route) })
                }
                composable(SHOP_ROUTE) {
                    // همان صفحه‌ی «فروشگاه/کیف» که از تنظیمات باز می‌شود (خواسته‌ی کاربر، ۷ مهر).
                    val gamificationVm: ir.sadteam.loancalc.ui.profile.GamificationViewModel = hiltViewModel()
                    ir.sadteam.loancalc.ui.coin.CoinHubScreen(
                        onBack = { navigateTo(BottomTab.HOME.route) },
                        todayHasEntry = gamificationVm.todayLogged.collectAsState().value,
                        viewModel = gamificationVm,
                    )
                }
                composable(INBOX_ROUTE) {
                    InboxScreen(onBack = { navigateTo(BottomTab.HOME.route) }, onOpenShop = { navigateTo(SHOP_ROUTE) })
                }
                composable(CALC_HISTORY_ROUTE) {
                    CalculationHistoryScreen(onBack = { navigateTo(LOAN_ROUTE) })
                }
                composable(SAYAD_INQUIRY_ROUTE) {
                    SayadInquiryScreen(
                        sayadId = null,
                        onBack = { navigateTo(TOOLS_ROUTE) },
                    )
                }
                composable(ANNUAL_ARCHIVE_ROUTE) {
                    AnnualArchiveScreen(onBack = { navigateTo(TOOLS_ROUTE) })
                }
                composable(CALENDAR_ROUTE) {
                    FinancialCalendarScreen(
                        onBack = { navigateTo(BottomTab.HOME.route) },
                        onOpenLoan = { loanId ->
                            deepLinkViewModel.openLoan(loanId)
                            navigateTo(LOAN_ROUTE)
                        },
                        onOpenCheque = { chequeId ->
                            deepLinkViewModel.openCheque(chequeId)
                            navigateTo(CHEQUE_ROUTE)
                        },
                    )
                }
}
