package ir.sadteam.loancalc

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLifecycleOwner
import ir.sadteam.loancalc.ui.accounting.BudgetScreen
import ir.sadteam.loancalc.ui.accounting.ReportTabScreen
import ir.sadteam.loancalc.ui.asset.AssetsTabScreen
import ir.sadteam.loancalc.ui.due.DueTabScreen
import ir.sadteam.loancalc.ui.home.HomeScreen
import ir.sadteam.loancalc.notifications.DeepLinkViewModel
import ir.sadteam.loancalc.ui.nav.NavDestination
import ir.sadteam.loancalc.ui.nav.NavSlotsViewModel
import ir.sadteam.loancalc.ui.nav.NavSuggestionCard
import kotlinx.coroutines.delay

/**
 * 🧱 **تب‌های اصلی زنده می‌مانند** (۱۶ مهر، گزارشِ کاربر: «هر تب را می‌زنم اول صفحه خالی می‌آید»).
 *
 * قبلاً `NavHost` با هر تعویضِ تب، صفحه‌ی قبلی را از کامپوزیشن بیرون می‌انداخت و صفحه‌ی تازه را از
 * صفر می‌ساخت؛ با داده‌ی واقعیِ کاربر (چند وام و صدها قسط/تراکنش) ساخته‌شدنِ صفحه چند فریم طول
 * می‌کشید و فقط زمینه دیده می‌شد. حالا هر تب **یک‌بار** ساخته می‌شود و بعد فقط نشان/پنهان می‌شود
 * (جای اسکرول و حالتِ صفحه هم می‌ماند).
 *
 * - تبِ پنهان **جایی گذاشته نمی‌شود** (نه کشیده می‌شود نه لمس می‌گیرد؛ انیمیشنِ بی‌پایانش هم کار
 *   نمی‌کند).
 * - `BackHandler`ِ تبِ پنهان به یک دیسپچرِ ساختگی وصل می‌شود تا دکمه‌ی برگشتِ تبِ دیگر را نگیرد.
 * - تب‌های دیگر دو ثانیه بعد از شروع، یکی‌یکی در پس‌زمینه گرم می‌شوند.
 * - صفحه‌های پوش‌شده (وام، چک، …) هنوز با `NavHost` باز می‌شوند و روی همین‌ها می‌نشینند.
 */
@Composable
internal fun PersistentTabs(
    currentRoute: String,
    deepLinkViewModel: DeepLinkViewModel,
    navSlotsViewModel: NavSlotsViewModel,
    navSlots: List<NavDestination>,
    navSuggestion: ir.sadteam.loancalc.core.NavSuggestion.Result?,
    navigateTo: (String) -> Unit,
    tabResetKeys: androidx.compose.runtime.snapshots.SnapshotStateMap<BottomTab, Int>,
    showSettingsState: MutableState<Boolean>,
    showInboxState: MutableState<Boolean>,
    showGlobalSearchState: MutableState<Boolean>,
    showAllTransactionsState: MutableState<Boolean>,
    shortcutDrawerOpenState: MutableState<Boolean>,
) {
    var showSettings by showSettingsState
    var showInbox by showInboxState
    var showGlobalSearch by showGlobalSearchState
    var showAllTransactions by showAllTransactionsState
    var shortcutDrawerOpen by shortcutDrawerOpenState

    val composed = remember { mutableStateListOf<String>() }
    LaunchedEffect(currentRoute) {
        if (BottomTab.entries.any { it.route == currentRoute } && currentRoute !in composed) composed.add(currentRoute)
    }
    LaunchedEffect(Unit) {
        delay(2500)
        for (t in BottomTab.entries) {
            if (t.route !in composed) {
                composed.add(t.route)
                delay(900)
            }
        }
    }
    val holder = rememberSaveableStateHolder()
    val realOwner = LocalOnBackPressedDispatcherOwner.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val dummyOwner = remember(lifecycleOwner) {
        object : OnBackPressedDispatcherOwner {
            override val lifecycle get() = lifecycleOwner.lifecycle
            override val onBackPressedDispatcher = OnBackPressedDispatcher()
        }
    }

    BottomTab.entries.forEach { tab ->
        val active = tab.route == currentRoute
        if (active || tab.route in composed) {
            key(tab.route) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .layout { measurable, constraints ->
                            val p = measurable.measure(constraints)
                            layout(p.width, p.height) { if (active) p.place(0, 0) }
                        },
                ) {
                    CompositionLocalProvider(LocalOnBackPressedDispatcherOwner provides (if (active) (realOwner ?: dummyOwner) else dummyOwner)) {
                        holder.SaveableStateProvider(tab.route) {
                            key(tabResetKeys[tab] ?: 0) {
                                when (tab) {
                                    BottomTab.HOME -> HomeScreen(
                                        onNavigateToRoute = navigateTo,
                                        onOpenSettings = { showSettings = true },
                                        onOpenInbox = { showInbox = true },
                                        onOpenSearch = { showGlobalSearch = true },
                                        onOpenTransactions = { showAllTransactions = true },
                                        onOpenLoan = { deepLinkViewModel.openLoan(it) },
                                        // نوعِ صریح عمدیه: بدونش `let` لامبدا رو `() -> Unit`ِ ساده حساب می‌کنه.
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
                                    BottomTab.ASSETS -> AssetsTabScreen()
                                    BottomTab.REPORT -> ReportTabScreen(
                                        onOpenLoanStats = { navigateTo(LOAN_STATS_ROUTE) },
                                        onOpenChequeReport = { navigateTo(CHEQUE_REPORT_ROUTE) },
                                    )
                                    BottomTab.BUDGET -> BudgetScreen()
                                    BottomTab.DUE -> DueTabScreen(
                                        onAddCheque = { navigateTo(CHEQUE_ROUTE) },
                                        onAddLoan = { navigateTo(LOAN_ROUTE) },
                                        onOpenCheque = { id ->
                                            deepLinkViewModel.openCheque(id)
                                            navigateTo(CHEQUE_ROUTE)
                                        },
                                        onOpenDebt = { id ->
                                            deepLinkViewModel.openDebt(id)
                                            navigateTo(DEBT_ROUTE)
                                        },
                                        onOpenLoan = { loanId -> deepLinkViewModel.openLoan(loanId) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
