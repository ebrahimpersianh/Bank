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
import androidx.compose.runtime.mutableStateOf
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
import ir.sadteam.loancalc.ui.theme.Motion
import kotlinx.coroutines.delay

/** آیا هر پنج تب ساخته شده‌اند؟ اسپلش تا این `true` شدن (حداکثر ۶ ثانیه) می‌ماند. */
internal object TabWarmup {
    val done = mutableStateOf(false)
}

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
    deepLinkLoanId: Long?,
    requestedLoanSubTabState: MutableState<LoanSubTab?>,
    bottomBarVisibleState: MutableState<Boolean>,
) {
    var showSettings by showSettingsState
    var showInbox by showInboxState
    var showGlobalSearch by showGlobalSearchState
    var showAllTransactions by showAllTransactionsState
    var shortcutDrawerOpen by shortcutDrawerOpenState
    val requestedLoanSubTab by requestedLoanSubTabState
    var bottomBarVisible by bottomBarVisibleState

    val composed = remember { mutableStateListOf<String>() }
    LaunchedEffect(currentRoute) {
        if ((currentRoute == LOAN_ROUTE || BottomTab.entries.any { it.route == currentRoute }) && currentRoute !in composed) composed.add(currentRoute)
    }
    LaunchedEffect(Unit) {
        // زیرِ اسپلش اجرا می‌شود (رجوع کن به AppRoot)؛ هر تب جدا و با فاصله تا فریم‌ها نپرند.
        delay(250)
        for (route in BottomTab.entries.map { it.route } + LOAN_ROUTE) {
            if (route !in composed) {
                composed.add(route)
                delay(350)
            }
        }
        delay(250)
        TabWarmup.done.value = true
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

    // جهتِ لغزش: همان قاعده‌ی NavHost (از تبِ قبلی به تبِ تازه).
    val routeMemo = remember { object { var cur = currentRoute; var prev = currentRoute } }
    if (routeMemo.cur != currentRoute) {
        routeMemo.prev = routeMemo.cur
        routeMemo.cur = currentRoute
    }
    val slideDir = slideDirection(routeMemo.prev, routeMemo.cur)

    BottomTab.entries.forEach { tab ->
        val active = tab.route == currentRoute
        if (active || tab.route in composed) {
            key(tab.route) {
                TabLayer(
                    feel = feelOf(tab.route),
                    dir = slideDir,
                    active = active,
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

    // «وام» (صفحه‌ی پوش‌شده) هم زنده می‌ماند: پرداده‌ترین صفحه‌ی کاربر است و ساختنِ دوباره‌اش هر بار
    // لحظه‌ی خالی می‌داد. بعد از بسته‌شدن، نسخه‌ی تازه‌اش در پس‌زمینه ساخته می‌شود تا دفعه‌ی بعد
    // مثلِ قبل از «وام‌های من» شروع شود و آماده باشد.
    val loanActive = currentRoute == LOAN_ROUTE
    if (loanActive || LOAN_ROUTE in composed) {
        key(LOAN_ROUTE) {
            var loanKey by remember { androidx.compose.runtime.mutableIntStateOf(0) }
            val wasActive = remember { androidx.compose.runtime.mutableStateOf(false) }
            LaunchedEffect(loanActive) {
                if (loanActive) {
                    wasActive.value = true
                } else if (wasActive.value) {
                    wasActive.value = false
                    bottomBarVisible = true
                    delay(400)
                    loanKey++
                }
            }
            TabLayer(feel = Motion.Feel.SOLID, dir = slideDir, active = loanActive) {
                CompositionLocalProvider(LocalOnBackPressedDispatcherOwner provides (if (loanActive) (realOwner ?: dummyOwner) else dummyOwner)) {
                    holder.SaveableStateProvider(LOAN_ROUTE) {
                        key(loanKey) {
                            LoanTab(
                                onBack = { navigateTo(BottomTab.HOME.route) },
                                requestedSubTab = requestedLoanSubTab,
                                onManualAddFabPositioned = {},
                                onBottomBarVisibilityChanged = { visible -> if (loanActive) bottomBarVisible = visible },
                                deepLinkLoanId = deepLinkLoanId,
                                onDeepLinkConsumed = { deepLinkViewModel.consume() },
                                onOpenSettings = { showSettings = true },
                            )
                        }
                    }
                }
            }
        }
    }
}


/**
 * حرکتِ تعویضِ تب (برگشتِ انیمیشن‌های هر بخش، ۱۶ مهر): چون تب‌ها دیگر از `NavHost` نمی‌گذرند،
 * همان حس‌های [Motion.Feel] این‌جا با یک پیشرفتِ `Animatable` پیاده شده‌اند. ورود: از شفافیت/لغزش/مقیاسِ
 * اولیه به حالتِ عادی؛ خروج: محو + لغزشِ کوتاه. تبِ پنهان (پیشرفت ۰) اصلاً جا نمی‌گیرد.
 */
@Composable
private fun TabLayer(
    feel: Motion.Feel,
    dir: Int,
    active: Boolean,
    content: @Composable () -> Unit,
) {
    val progress = remember { androidx.compose.animation.core.Animatable(if (active) 1f else 0f) }
    LaunchedEffect(active) {
        if (active) {
            val spec: androidx.compose.animation.core.AnimationSpec<Float> = when (feel) {
                Motion.Feel.PLAYFUL -> androidx.compose.animation.core.spring(0.62f, androidx.compose.animation.core.Spring.StiffnessMediumLow)
                Motion.Feel.FLOW -> androidx.compose.animation.core.spring(0.9f, androidx.compose.animation.core.Spring.StiffnessLow)
                Motion.Feel.INSIGHT, Motion.Feel.SOLID -> androidx.compose.animation.core.spring(0.9f, androidx.compose.animation.core.Spring.StiffnessLow)
                Motion.Feel.CALM -> androidx.compose.animation.core.tween(200)
            }
            progress.animateTo(1f, spec)
        } else {
            val ms = when (feel) {
                Motion.Feel.PLAYFUL -> 160
                Motion.Feel.FLOW -> 240
                Motion.Feel.INSIGHT -> 160
                Motion.Feel.SOLID -> 220
                Motion.Feel.CALM -> 140
            }
            progress.animateTo(0f, androidx.compose.animation.core.tween(ms))
        }
    }
    val inFrac = when (feel) {
        Motion.Feel.PLAYFUL -> 1f / 6f
        Motion.Feel.FLOW -> 1f / 2f
        Motion.Feel.INSIGHT -> 0f
        Motion.Feel.SOLID -> 1f / 5f
        Motion.Feel.CALM -> 0f
    }
    val outFrac = when (feel) {
        Motion.Feel.FLOW -> 1f / 3f
        Motion.Feel.SOLID -> 1f / 6f
        else -> 0f
    }
    val inScale = when (feel) {
        Motion.Feel.PLAYFUL -> 0.92f
        Motion.Feel.INSIGHT -> 0.96f
        else -> 1f
    }
    val outScale = if (feel == Motion.Feel.PLAYFUL) 0.96f else 1f
    Box(
        modifier = Modifier
            .fillMaxSize()
            .layout { measurable, constraints ->
                val pl = measurable.measure(constraints)
                layout(pl.width, pl.height) {
                    val warm = !TabWarmup.done.value
                    val pr = progress.value
                    if (active || pr > 0.001f || warm) {
                        pl.placeWithLayer(0, 0) {
                            if (warm && !active && pr <= 0.001f) {
                                alpha = 1f
                            } else {
                                val a = pr.coerceIn(0f, 1f)
                                alpha = a
                                if (active) {
                                    translationX = dir * size.width * inFrac * (1f - pr)
                                    val sc = inScale + (1f - inScale) * pr
                                    scaleX = sc
                                    scaleY = sc
                                } else {
                                    translationX = -dir * size.width * outFrac * (1f - pr)
                                    val sc = outScale + (1f - outScale) * pr
                                    scaleX = sc
                                    scaleY = sc
                                }
                            }
                        }
                    }
                }
            },
    ) {
        content()
    }
}
