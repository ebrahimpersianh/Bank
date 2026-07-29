package ir.sadteam.loancalc.ui.myloans

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.IncomeType
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.IncomeEntity
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.auth.GateState
import ir.sadteam.loancalc.ui.auth.LoginScreen
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.components.AutoShrinkText
import ir.sadteam.loancalc.ui.components.BankBadge
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.haptics.rememberBuzz
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.components.countUpDouble
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.components.ProgressRing
import ir.sadteam.loancalc.ui.components.rememberIsScrollingUp
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.subscription.SubscriptionScreen
import ir.sadteam.loancalc.ui.theme.Motion
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/** پورت لیبل «فیلتر» بالا-چپِ لیست وام‌های رقیب (VAMMAN) - فقط مرتب‌سازی محلی لیست، بدون تغییر
 * داده؛ پیش‌فرض «جدیدترین» (همون ترتیب قبلی createdAt نزولی که قبلاً بدون این کنترل هم اعمال می‌شد). */
private enum class LoanSortOption(val label: String) {
    NEWEST("جدیدترین"),
    OLDEST("قدیمی‌ترین"),
    NAME("نام (الفبا)"),
    AMOUNT_DESC("بیشترین مبلغ"),
    PROGRESS_DESC("بیشترین پیشرفت پرداخت"),
    NEXT_DUE("نزدیک‌ترین سررسید"),
    // با نگه‌داشتن+کشیدنِ کارتِ یه وام فعال می‌شه (رجوع کن به orderedLoans/reorderLoans تو
    // MyLoansScreen) - تو منوی «فیلتر» هم انتخاب‌پذیره تا کاربر بتونه دستی برگرده روش.
    CUSTOM("دلخواه (کشیدن و رهاکردن)"),
}

private fun List<LoanEntity>.sortedByOption(option: LoanSortOption, viewModel: MyLoansViewModel): List<LoanEntity> = when (option) {
    LoanSortOption.NEWEST -> sortedByDescending { it.createdAt }
    LoanSortOption.OLDEST -> sortedBy { it.createdAt }
    LoanSortOption.NAME -> sortedBy { it.name }
    LoanSortOption.AMOUNT_DESC -> sortedByDescending { it.amount }
    LoanSortOption.PROGRESS_DESC -> sortedByDescending { if (it.n > 0) it.paidCount.toDouble() / it.n else 0.0 }
    // وامِ تسویه‌شده/بدونِ قسطِ پرداخت‌نشده (getLoanNextDueDate == null) همیشه آخرِ لیست می‌افته
    // (Int.MAX_VALUE به‌جای null، تا مقایسه‌ی ساده‌ی sortedBy بدونِ کامپریتورِ جدا کافی باشه).
    LoanSortOption.NEXT_DUE -> sortedBy { loan ->
        viewModel.getLoanNextDueDate(loan)?.let { it.y * 10000 + it.m * 100 + it.d } ?: Int.MAX_VALUE
    }
    // وامِ بدونِ sortOrderِ ذخیره‌شده (هنوز هیچ‌وقت دستی جابه‌جا نشده) همیشه آخر می‌افته.
    LoanSortOption.CUSTOM -> sortedBy { loan -> viewModel.getLoanSortOrder(loan) ?: Long.MAX_VALUE }
}

/** وامی که همه‌ی اقساطش پرداخت شده - رجوع کن به بخشِ «وام‌های تسویه‌شده» تو MyLoansScreen. عمداً از
 * رو paidCount/n مشتق می‌شه (نه یه ستونِ جداگانه تو دیتابیس)؛ همون منطقی که سکه‌بارونِ
 * LoanDetailScreen (wasFullyPaid) هم استفاده می‌کنه. */
private fun isLoanSettled(loan: LoanEntity) = loan.n > 0 && loan.paidCount >= loan.n

/**
 * مرکزِ یه کارت رو به [TransformOrigin] (کسرِ ۰..۱ از کلِ ظرف) تبدیل می‌کنه - ورودیِ لازمِ
 * `scaleIn/scaleOut` تا صفحه‌ی جزئیات از روی همون کارت باز بشه، نه از وسطِ صفحه.
 *
 * اگه ظرف هنوز اندازه‌گیری نشده (عرض/ارتفاعِ صفر، مثلاً اولین فریم)، برمی‌گرده به مرکز - وگرنه
 * تقسیم بر صفر یه origin نامعتبر می‌ساخت.
 */
private fun Rect.heroOriginIn(container: Rect): TransformOrigin {
    if (container.width <= 0f || container.height <= 0f) return TransformOrigin.Center
    return TransformOrigin(
        pivotFractionX = ((center.x - container.left) / container.width).coerceIn(0f, 1f),
        pivotFractionY = ((center.y - container.top) / container.height).coerceIn(0f, 1f),
    )
}

/**
 * لیست محلی Room + افزودن دستی/حذف/بازکردن جزئیات (پرداخت قسط)، پشتیبان‌گیری/بازیابی رو نشون می‌ده.
 *
 * پورت canSaveAnotherLoan/handleLoanLimitReached تو www/index.html: بعد از اولین وام، مهمون‌ها
 * باید وارد بشن (LoginScreen غیراجباری، با دکمه‌ی بازگشت)، کاربرهای واردشده‌ی بدون اشتراک به
 * [SubscriptionScreen] (خرید واقعی با Poolakey) می‌رن.
 */
// PullToRefreshBox تو material3 هنوز experimental ئه (BOM 2024.09) - تنها API رسمیِ
// «کشیدن برای تازه‌سازی» تو Compose همینه.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLoansScreen(
    viewModel: MyLoansViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    // مختصاتِ واقعیِ دکمه‌ی «افزودن دستی وام» رو گزارش می‌ده - برای قدمِ آخرِ AppTourOverlay
    // (TourTarget.MANUAL_ADD تو MainActivity.kt) که این دکمه رو اسپاتلایت می‌کنه.
    onManualAddFabPositioned: (Rect) -> Unit = {},
    // نوارِ پایینِ ۴تبی (تویِ MainActivity.kt) موقعِ اسکرولِ رو‌به‌پایینِ این لیست جمع می‌شه - این
    // فقط جهتِ اسکرول رو گزارش می‌ده، خودِ نوارِ پایین رو نمی‌بینه (اونجا تو یه کامپوزیبلِ کاملاً
    // دیگه‌ست، رجوع کن به LoanCalcApp).
    onBottomBarVisibilityChanged: (visible: Boolean) -> Unit = {},
) {
    var showAddForm by remember { mutableStateOf(false) }
    var openedLoanId by remember { mutableStateOf<Long?>(null) }
    // ویرایشِ مشخصاتِ کلیِ یه وام (اسم/بانک/مبلغ/تعدادِ اقساط) - عمداً openedLoanId رو پاک نمی‌کنیم
    // وقتی ویرایش باز می‌شه، فقط اولویتِ مسیریابی رو تو screenKey بالاتر می‌بریم؛ این‌طوری بعدِ
    // ذخیره/انصرافِ ویرایش (editingLoanId = null)، خودکار برمی‌گرده به همون صفحه‌ی جزئیاتِ وام،
    // نه لیست.
    var editingLoanId by remember { mutableStateOf<Long?>(null) }
    var showLoginPrompt by remember { mutableStateOf(false) }
    // «کشیدن به پایین برای همگام‌سازی» - رجوع کن به MyLoansViewModel.syncNow برای اینکه
    // چرا این ژست عمداً فقط پوش می‌کنه و داده‌ی محلی رو با سرور جایگزین نمی‌کنه.
    var syncing by remember { mutableStateOf(false) }
    var showSubscriptionScreen by remember { mutableStateOf(false) }

    val rawLoans by viewModel.loans.collectAsState()
    var sortOption by remember { mutableStateOf(LoanSortOption.NEWEST) }
    val loans = remember(rawLoans, sortOption) { rawLoans.sortedByOption(sortOption, viewModel) }
    // «وام‌های تسویه‌شده»: هم‌الگو با showArchived تو ChequeScreen - وامی که تسویه شده (isLoanSettled)
    // خودکار از لیستِ فعال بیرون میره، پشتِ همین تاگل نمایش داده می‌شه. لیستِ اصلی (loans، برای
    // openedLoan/editingLoan/canSaveAnotherLoan/DashboardSummary) عمداً فیلتر نمی‌شه - فقط لیستِ
    // نمایشیِ پایینِ صفحه (visibleLoans).
    var showSettled by remember { mutableStateOf(false) }
    val visibleLoans = remember(loans, showSettled) { loans.filter { isLoanSettled(it) == showSettled } }
    val settledCount = remember(loans) { loans.count { isLoanSettled(it) } }

    // جابه‌جاییِ دستیِ کارت‌های وام (نگه‌داشتنِ چندثانیه‌ای + کشیدن بالا/پایین) - orderedLoans یه
    // کپیِ محلیِ visibleLoans ئه که حینِ کشیدن زنده جابه‌جا می‌شه؛ وقتی کشیدن تمومه (draggingLoanId
    // == null) دوباره از visibleLoانsِ واقعی (بعدِ هر سورت/فیلترِ جدید) پر می‌شه. شروعِ کشیدن خودکار
    // sortOption رو به CUSTOM می‌بره - وگرنه با فیلترهای دیگه (جدیدترین/بیشترین مبلغ...) بلافاصله
    // ترتیبِ دستی زیر پا گذاشته می‌شد.
    var draggingLoanId by remember { mutableStateOf<Long?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    var orderedLoans by remember { mutableStateOf(visibleLoans) }
    LaunchedEffect(visibleLoans) {
        if (draggingLoanId == null) orderedLoans = visibleLoans
    }
    val loanCardHeights = remember { mutableStateMapOf<Long, Int>() }
    val buzz = rememberBuzz()
    val incomes by viewModel.incomes.collectAsState()
    val gateState by authViewModel.gateState.collectAsState()
    val subscribed by authViewModel.subscribed.collectAsState()
    val canSaveAnotherLoan = loans.isEmpty() || (gateState == GateState.LOGGED_IN && subscribed)
    val openedLoan = openedLoanId?.let { id -> loans.firstOrNull { it.id == id } }
    val editingLoan = editingLoanId?.let { id -> loans.firstOrNull { it.id == id } }

    fun onAddLoanClick() {
        when {
            canSaveAnotherLoan -> showAddForm = true
            gateState == null -> Unit // هنوز از DataStore خونده نشده، صبر کن
            gateState != GateState.LOGGED_IN -> showLoginPrompt = true
            else -> showSubscriptionScreen = true
        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingExportJson by remember { mutableStateOf<String?>(null) }
    val banner = rememberInAppBanner()

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        val json = pendingExportJson
        if (uri != null && json != null) {
            scope.launch(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                }
                withContext(Dispatchers.Main) {
                    banner.show("پشتیبان‌گیری انجام شد", isSuccess = true)
                }
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            val json = runCatching {
                context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() }
            }.getOrNull()
            withContext(Dispatchers.Main) {
                if (json == null) {
                    banner.show("فایل قابل خوندن نبود")
                } else {
                    viewModel.importBackup(json) { ok ->
                        val message = if (ok) "بازیابی شد" else "فایل معتبر نیست"
                        banner.show(message, isSuccess = ok)
                    }
                }
            }
        }
    }

    // پورت حس تعویض نرم بین حالت‌های مختلف این صفحه (لیست/ورود/اشتراک/افزودن/جزئیات) - قبلاً هرکدوم
    // با یه return زودهنگام یهو جایگزین بقیه می‌شد؛ حالا با AnimatedContent (fade ظریف) عوض می‌شه.
    val screenKey = when {
        showLoginPrompt -> "login"
        showSubscriptionScreen -> "subscription"
        showAddForm -> "add"
        editingLoan != null -> "edit"
        openedLoan != null -> "detail"
        else -> "list"
    }

    // دکمه‌ی برگشتِ سیستمی/سخت‌افزاری: تا وقتی رو زیرصفحه‌ای غیر از لیستیم (جزئیاتِ وام/ویرایش/
    // افزودن/ورود/اشتراک)، اول باید همون زیرصفحه بسته بشه و برگردیم به سطحِ قبلی - نه اینکه از
    // کلِ تب یا کل اپ خارج بشیم. هر شاخه دقیقاً همون onBack/onCancel رو صدا می‌زنه که خودِ دکمه‌ی
    // بازگشتِ داخلِ صفحه هم صدا می‌زنه.
    BackHandler(enabled = screenKey != "list") {
        when (screenKey) {
            "login" -> showLoginPrompt = false
            "subscription" -> showSubscriptionScreen = false
            "add" -> showAddForm = false
            "edit" -> editingLoanId = null
            "detail" -> openedLoanId = null
        }
    }

    // ترنزیشنِ «هیرو»: صفحه‌ی جزئیات به‌جای اینکه از وسطِ صفحه باز بشه، از روی همون کارتی که
    // زده شد باز/بسته می‌شه. عمداً SharedTransitionLayout (المانِ مشترکِ واقعی) استفاده نشده -
    // اون کلِ ساختارِ این AnimatedContent رو می‌خواست عوض کنه و پرریسک بود؛ این‌جوری با فقط
    // جابه‌جا کردنِ مرکزِ بزرگ‌شدن (transformOrigin) تقریباً همون حس رو می‌ده.
    var heroOrigin by remember { mutableStateOf(TransformOrigin.Center) }
    var listBounds by remember { mutableStateOf(Rect.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { listBounds = it.boundsInRoot() },
    ) {
    AnimatedContent(
        targetState = screenKey,
        transitionSpec = {
            if (targetState == "detail" || initialState == "detail") {
                // فقط برای گذرِ لیست↔جزئیات؛ بقیه‌ی گذرها همون تعویضِ استانداردِ Motion رو دارن.
                (fadeIn(tween(Motion.FADE_IN_MS)) +
                    scaleIn(animationSpec = Motion.standard(), initialScale = 0.86f, transformOrigin = heroOrigin)
                    ) togetherWith (
                    fadeOut(tween(Motion.FADE_OUT_MS)) +
                        scaleOut(animationSpec = Motion.standard(), targetScale = 0.94f, transformOrigin = heroOrigin)
                    )
            } else {
                Motion.contentEnter togetherWith Motion.contentExit
            }
        },
        label = "myLoansScreen",
    ) { key ->
        when (key) {
            "login" -> LoginScreen(
                onDismiss = { showLoginPrompt = false },
                onLoginSuccess = { showLoginPrompt = false },
            )
            "subscription" -> SubscriptionScreen(
                onBack = { showSubscriptionScreen = false },
                onSubscribed = { showSubscriptionScreen = false },
                onNeedsLogin = { showLoginPrompt = true },
            )
            "add" -> AddManualLoanScreen(
                onSaved = { showAddForm = false },
                onCancel = { showAddForm = false },
                viewModel = viewModel,
            )
            "edit" -> editingLoan?.let { loan ->
                AddManualLoanScreen(
                    editingLoan = loan,
                    onSaved = { editingLoanId = null },
                    onCancel = { editingLoanId = null },
                    viewModel = viewModel,
                )
            }
            "detail" -> openedLoan?.let { loan ->
                LoanDetailScreen(
                    loan = loan,
                    onBack = { openedLoanId = null },
                    onDelete = { viewModel.deleteLoan(loan.id); openedLoanId = null },
                    onEdit = { editingLoanId = loan.id },
                    viewModel = viewModel,
                )
            }
            else -> PullToRefreshBox(
                isRefreshing = syncing,
                onRefresh = {
                    syncing = true
                    viewModel.syncNow {
                        syncing = false
                        banner.show("همگام‌سازی انجام شد", isSuccess = true)
                    }
                },
                modifier = Modifier.fillMaxSize(),
            ) {
                val loansListState = rememberLazyListState()
                val isScrollingUp by rememberIsScrollingUp(loansListState)
                LaunchedEffect(isScrollingUp) { onBottomBarVisibilityChanged(isScrollingUp) }
                // اگه از این تب بریم بیرون درحالی‌که نوار پایین جمع‌شده بود (لیست اسکرول‌شده به
                // پایین)، باید دوباره ظاهر بشه - وگرنه رو تبِ بعدی/جزئیاتِ وام جمع‌شده می‌موند.
                DisposableEffect(Unit) { onDispose { onBottomBarVisibilityChanged(true) } }
                LazyColumn(
                    state = loansListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        DashboardSummary(
                            loans = loans,
                            incomes = incomes,
                            onAddIncome = { label, amount, type -> viewModel.addIncome(label, amount, type) },
                            onDeleteIncome = { viewModel.deleteIncome(it) },
                        )
                    }

                    if (loans.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (settledCount > 0) {
                                    SettledLoansToggle(
                                        showSettled = showSettled,
                                        settledCount = settledCount,
                                        onToggle = { showSettled = !showSettled },
                                    )
                                } else {
                                    Box {}
                                }
                                if (!showSettled && loans.isNotEmpty()) {
                                    LoanSortMenu(selected = sortOption, onSelect = { sortOption = it })
                                }
                            }
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { openDocumentLauncher.launch(arrayOf("application/json")) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppPrimary),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("بازیابی", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    viewModel.exportBackup { json ->
                                        pendingExportJson = json
                                        createDocumentLauncher.launch("loans-backup.json")
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppPrimary),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("پشتیبان‌گیری", fontSize = 12.sp)
                            }
                        }
                    }

                    if (visibleLoans.isEmpty()) {
                        item {
                            if (showSettled) {
                                EmptyState(
                                    icon = Icons.Filled.CheckCircle,
                                    title = "هنوز وامی تسویه نشده",
                                    description = "وقتی آخرین قسطِ یه وام رو پرداخت کنی، " +
                                        "خودکار میاد همین‌جا.",
                                )
                            } else {
                                EmptyState(
                                    icon = Icons.Outlined.AccountBalanceWallet,
                                    title = "هنوز وامی ذخیره نشده",
                                    description = "وام‌هات رو اینجا نگه دار تا سررسیدِ هر قسط، " +
                                        "مبلغِ باقی‌مونده و پیشرفتِ پرداختت همیشه جلوی چشمت باشه.",
                                    actionLabel = "افزودن وام",
                                    onAction = { onAddLoanClick() },
                                )
                            }
                        }
                    } else {
                        items(orderedLoans, key = { it.id }) { loan ->
                            // انیمیشنِ فلیپِ کارت (خواسته‌ی «انیمیشن‌های سفارشی») - آیکونِ اطلاعات، کارت رو
                            // مثل یه چکِ فیزیکی می‌چرخونه و خلاصه‌ی پرداخت رو پشتش نشون می‌ده؛ ضربه‌ی اصلیِ
                            // کارت هنوز باز کردنِ جزئیاتِ وامه، این فقط یه لایه‌ی جدا و مستقله.
                            var flipped by remember { mutableStateOf(false) }
                            val density = LocalDensity.current
                            val rotation by animateFloatAsState(
                                targetValue = if (flipped) 180f else 0f,
                                animationSpec = tween(500),
                                label = "loanCardFlip",
                            )
                            // animateItem: اضافه/حذف/جابه‌جایی وام‌ها با انیمیشن نرم (نه پرش یهویی).
                            var cardBounds by remember { mutableStateOf(Rect.Zero) }
                            val isDragging = loan.id == draggingLoanId
                            // بازپرداختِ عقب‌افتاده: سررسیدِ اولین قسطِ پرداخت‌نشده از امروز گذشته -
                            // خودِ کارت حاشیه‌ی قرمز می‌گیره + یه بجِ «!» کنارِ اسمِ وام.
                            val overdue = remember(loan) { viewModel.isLoanOverdue(loan) }
                            AppCard(
                                borderColor = if (overdue) AppDanger else null,
                                modifier = Modifier
                                    .zIndex(if (isDragging) 1f else 0f)
                                    .then(if (isDragging) Modifier else Modifier.animateItem())
                                    .onGloballyPositioned {
                                        cardBounds = it.boundsInRoot()
                                        loanCardHeights[loan.id] = it.size.height
                                    }
                                    // نگه‌داشتنِ چندثانیه‌ای رو کارت، بعد کشیدن بالا/پایین برای
                                    // جابه‌جاییِ دستیِ ترتیبِ لیست - خواسته‌ی صریحِ کاربر. تپِ سریعِ
                                    // معمولی (بدونِ نگه‌داشتن) دستِ detectDragGesturesAfterLongPress
                                    // رو نمی‌رسه، همون pressScaleClickable پایین‌تر جواب می‌ده.
                                    .pointerInput(loan.id) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                draggingLoanId = loan.id
                                                dragOffsetY = 0f
                                                buzz()
                                                if (sortOption != LoanSortOption.CUSTOM) sortOption = LoanSortOption.CUSTOM
                                            },
                                            onDragEnd = {
                                                draggingLoanId = null
                                                dragOffsetY = 0f
                                                viewModel.reorderLoans(orderedLoans)
                                            },
                                            onDragCancel = {
                                                draggingLoanId = null
                                                dragOffsetY = 0f
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffsetY += dragAmount.y
                                                val currentIndex = orderedLoans.indexOfFirst { it.id == loan.id }
                                                val step = (loanCardHeights[loan.id] ?: 200) + 10
                                                if (dragOffsetY > step / 2 && currentIndex < orderedLoans.lastIndex) {
                                                    orderedLoans = orderedLoans.toMutableList().apply {
                                                        add(currentIndex + 1, removeAt(currentIndex))
                                                    }
                                                    dragOffsetY -= step
                                                } else if (dragOffsetY < -step / 2 && currentIndex > 0) {
                                                    orderedLoans = orderedLoans.toMutableList().apply {
                                                        add(currentIndex - 1, removeAt(currentIndex))
                                                    }
                                                    dragOffsetY += step
                                                }
                                            },
                                        )
                                    }
                                    .pressScaleClickable {
                                        // مرکزِ همین کارت رو به کسرِ ۰..۱ از کلِ صفحه تبدیل می‌کنیم تا
                                        // بزرگ‌شدنِ صفحه‌ی جزئیات دقیقاً از همین‌جا شروع بشه.
                                        heroOrigin = cardBounds.heroOriginIn(listBounds)
                                        openedLoanId = loan.id
                                    }
                                    .graphicsLayer {
                                        rotationY = rotation
                                        cameraDistance = 12f * density.density
                                        translationY = if (isDragging) dragOffsetY else 0f
                                    },
                            ) {
                                if (rotation <= 90f) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        // لوگوی بانک سمت راست کارت (لبه‌ی leading در RTL) - از رو اسم بانک.
                                        BankBadge(bankName = loan.bank)
                                        Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(loan.name, color = AppText, fontSize = 15.sp)
                                                if (overdue) {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(start = 6.dp)
                                                            .size(16.dp)
                                                            .background(AppDanger, CircleShape),
                                                        contentAlignment = Alignment.Center,
                                                    ) {
                                                        Icon(
                                                            Icons.Filled.PriorityHigh,
                                                            contentDescription = "بازپرداخت عقب‌افتاده",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(11.dp),
                                                        )
                                                    }
                                                }
                                            }
                                            Text(loan.bank, color = AppMuted, fontSize = 12.sp)
                                            Text(
                                                if (overdue) "عقب‌افتاده" else "${loan.paidCount} از ${loan.n} قسط پرداخت‌شده",
                                                color = if (overdue) AppDanger else AppPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = if (overdue) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.padding(top = 2.dp),
                                            )
                                        }
                                        // حلقه‌ی پیشرفتِ گرادیانی (سبزآبی→طلایی) با درصدِ اقساطِ
                                        // پرداخت‌شده - رجوع کن به ProgressRing؛ هرچی به تسویه نزدیک‌تر،
                                        // نوکِ قوس طلایی‌تر.
                                        val paidPct = if (loan.n > 0) loan.paidCount.toFloat() / loan.n else 0f
                                        ProgressRing(
                                            progress = paidPct,
                                            size = 40.dp,
                                            strokeWidth = 4.dp,
                                        ) {
                                            Text(
                                                "${toFa((paidPct * 100).roundToInt())}٪",
                                                color = AppText,
                                                fontSize = 9.sp,
                                            )
                                        }
                                        IconButton(onClick = { flipped = true }) {
                                            Icon(Icons.Filled.Info, contentDescription = "خلاصه پرداخت", tint = AppMuted)
                                        }
                                        IconButton(onClick = { viewModel.deleteLoan(loan.id) }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "حذف وام", tint = AppDanger)
                                        }
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .graphicsLayer { rotationY = 180f },
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(loan.name, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                            IconButton(onClick = { flipped = false }) {
                                                Icon(Icons.Filled.Info, contentDescription = "بستن خلاصه", tint = AppMuted)
                                            }
                                        }
                                        PrivacyCrossfade(LocalPrivacyMode.current) { masked ->
                                            Text(
                                                "باقی‌مانده: ${maskIfPrivate(masked, fmt(loan.installment * (loan.n - loan.paidCount)))} ریال",
                                                color = AppPrimary,
                                                fontSize = 13.sp,
                                                modifier = Modifier.padding(top = 6.dp),
                                            )
                                        }
                                        Text(
                                            "${toFa(loan.n - loan.paidCount)} قسط باقیمانده از ${toFa(loan.n)}",
                                            color = AppMuted,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(top = 2.dp),
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
        // دکمه‌ی «+» دایره‌ای سبز گوشه‌ی سمت چپ (در RTL: BottomEnd) - جایگزین دکمه‌ی تمام‌عرضِ
        // «افزودن دستی وام»؛ فقط رو خودِ لیست نشون داده می‌شه، نه رو فرم افزودن/جزئیات.
        // با یه pop فنری ظاهر/محو می‌شه (نه یهو).
        androidx.compose.animation.AnimatedVisibility(
            visible = screenKey == "list",
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(150)),
            exit = scaleOut(tween(120)) + fadeOut(tween(120)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        ) {
            FloatingActionButton(
                onClick = { onAddLoanClick() },
                containerColor = AppPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.onGloballyPositioned {
                    onManualAddFabPositioned(it.boundsInRoot())
                },
            ) {
                Icon(Icons.Filled.Add, contentDescription = "افزودن دستی وام")
            }
        }

        InAppBannerHost(banner, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/**
 * پورت داشبورد اصلی اپ رقیب (VAMMAN): سه‌تا کارت بزرگ و خوانا - وضعیت کلی بدهی‌ها (مجموع مانده‌ی
 * همه‌ی وام‌ها، از رو installment×(n−paidCount) هر وام)، مجموع اقساط ماهانه (جمع installment همه‌ی
 * وام‌ها - تقریبی، فرض دوره‌ی ماهانه)، و تحلیل درآمد (نسبت اقساط به جمع چند منبع درآمد مستقل -
 * ثابت/متغیر - با آستانه‌ی «منطقه‌ی امن» ۶۵٪ که از رشته‌های واقعی رقیب استخراج شد؛ نسخه‌ی قبلی این
 * پروژه اشتباهاً دو آستانه‌ی ۳۰٪/۵۰٪ حدسی داشت که تو هیچ‌جای رقیب پیدا نشد). برخلاف رقیب که این یه
 * صفحه‌ی جدا (home) بود، چون معماری تب‌های این اپ (رجوع کن به CLAUDE.md) ثابته، بالای همین «وام‌های
 * من» اضافه شده - جایی که داده‌ی وام‌ها از قبل در دسترسه.
 */
@Composable
private fun DashboardSummary(
    loans: List<LoanEntity>,
    incomes: List<IncomeEntity>,
    onAddIncome: (label: String, amount: Double, type: IncomeType) -> Unit,
    onDeleteIncome: (IncomeEntity) -> Unit,
) {
    val totalRemainingDebt = remember(loans) {
        loans.sumOf { it.installment * (it.n - it.paidCount) }
    }
    val totalMonthlyInstallment = remember(loans) { loans.sumOf { it.installment } }
    val totalIncome = remember(incomes) { incomes.sumOf { it.amount } }
    val ratio = if (totalIncome > 0) totalMonthlyInstallment / totalIncome else 0.0
    val statusLabel: String?
    val statusColor: Color
    when {
        totalIncome <= 0 -> {
            statusLabel = null
            statusColor = AppMuted
        }
        ratio <= 0.65 -> {
            statusLabel = "وضعیت مطلوب"
            statusColor = AppPrimary
        }
        else -> {
            statusLabel = "فشار مالی بالا"
            statusColor = AppDanger
        }
    }

    var showAddIncome by remember { mutableStateOf(false) }
    var label by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(IncomeType.FIXED) }

    // شمارش صعودی اعداد بزرگ داشبورد (پورت animateNumber وب) - حس «پریمیوم» موقع ورود به تب.
    val animatedDebt = countUpDouble(totalRemainingDebt)
    val animatedMonthly = countUpDouble(totalMonthlyInstallment)
    val privacyMode = LocalPrivacyMode.current

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PrivacyCrossfade(privacyMode) { masked ->
            DashboardStatCard(
                title = "وضعیت کلی بدهی‌ها",
                value = "${maskIfPrivate(masked, fmt(animatedDebt))} ریال",
                valueColor = AppText,
            )
        }
        PrivacyCrossfade(privacyMode) { masked ->
            DashboardStatCard(
                title = "مجموع اقساط ماهانه",
                value = "${maskIfPrivate(masked, fmt(animatedMonthly))} ریال",
                valueColor = AppPrimary,
            )
        }

        // یه پس‌زمینه‌ی سبزِ اختصاصیِ نیمه‌شفاف اینجا امتحان شده بود، ولی رو Surface (که خودش
        // tonalElevation داره) رنگ‌ها بهم می‌ریخت و دوتُنی/کثیف به‌نظر می‌رسید. کاربر خواست دقیقاً
        // مثل بقیه‌ی کارت‌های داشبورد (DashboardStatCard بالا) باشه - پس همون پیش‌فرضِ AppCard.
        AppCard(label = "تحلیل درآمد") {
            if (incomes.isNotEmpty()) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    incomes.forEach { income ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(income.label, color = AppText, fontSize = 13.sp)
                                Text(
                                    if (income.type == IncomeType.FIXED.name) "ثابت" else "متغیر",
                                    color = AppMuted,
                                    fontSize = 11.sp,
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PrivacyCrossfade(privacyMode) { masked ->
                                    Text(
                                        "${maskIfPrivate(masked, fmt(income.amount))} ریال",
                                        color = AppMuted,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(end = 6.dp),
                                    )
                                }
                                IconButton(onClick = { onDeleteIncome(income) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "حذف منبع درآمد", tint = AppDanger)
                                }
                            }
                        }
                    }
                    PrivacyCrossfade(privacyMode) { masked ->
                        Text(
                            "جمع درآمد: ${maskIfPrivate(masked, fmt(totalIncome))} ریال",
                            color = AppText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }

            if (showAddIncome) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("اسم منبع درآمد (مثلاً حقوق)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    // مبلغ با جداکننده‌ی هزارگان نشون داده می‌شه و زیرش معادل حروفی (مثل «مبلغ وام»).
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = cleanNum(it) },
                        visualTransformation = ThousandsSeparatorTransformation(),
                        label = { Text("مبلغ ماهانه (ریال)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    val incomeRial = amountText.toLongOrNull() ?: 0L
                    if (incomeRial > 0) {
                        AutoShrinkText(
                            text = "${numberToWordsFa((incomeRial / 10).toDouble())} تومان",
                            color = AppMuted,
                            maxFontSize = 11.sp,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppChip(label = "ثابت", selected = type == IncomeType.FIXED, onClick = { type = IncomeType.FIXED })
                        AppChip(label = "متغیر", selected = type == IncomeType.VARIABLE, onClick = { type = IncomeType.VARIABLE })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GradientButton(
                            onClick = {
                                val amount = amountText.toDoubleOrNull() ?: 0.0
                                if (label.trim().isNotEmpty() && amount > 0) {
                                    onAddIncome(label.trim(), amount, type)
                                    label = ""
                                    amountText = ""
                                    showAddIncome = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("افزودن")
                        }
                        OutlinedButton(onClick = { showAddIncome = false }, modifier = Modifier.weight(1f)) {
                            Text("انصراف")
                        }
                    }
                }
            } else {
                OutlinedButton(onClick = { showAddIncome = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("+ افزودن منبع درآمد")
                }
            }

            if (totalIncome > 0) {
                // گیجِ «سلامتِ مالی»: درصدِ درآمدی که صرفِ اقساط می‌شه، به‌شکل یه حلقه‌ی انیمیشنی -
                // تو حالتِ فشارِ بالا (ratio > 0.65، همون آستانه‌ی statusColor بالا) کلِ حلقه قرمز
                // می‌شه، وگرنه همون گرادیانِ سبزآبی→طلاییِ استانداردِ ProgressRing.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 10.dp),
                ) {
                    ProgressRing(
                        progress = ratio.toFloat(),
                        size = 64.dp,
                        strokeWidth = 7.dp,
                        colors = if (ratio > 0.65) listOf(AppDanger, AppDanger) else listOf(AppPrimaryDim, AppPrimary, AppAccent),
                    ) {
                        Text(
                            "${toFa((ratio * 100).roundToInt())}٪",
                            color = AppText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            "از درآمدت صرف اقساط می‌شه",
                            color = AppMuted,
                            fontSize = 12.sp,
                        )
                        if (statusLabel != null) {
                            Text(
                                statusLabel,
                                color = statusColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardStatCard(title: String, value: String, valueColor: Color) {
    AppCard(label = title) {
        Text(value, color = valueColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

/** تاگلِ «وام‌های تسویه‌شده» بالای لیستِ وام‌ها - هم‌الگو با دکمه‌ی بایگانیِ ChequeScreen. وقتی رو
 * لیستِ فعاله دکمه‌ی ورود به تسویه‌شده‌ها رو نشون می‌ده (با تعدادشون)؛ وقتی رو تسویه‌شده‌هاست، برعکس. */
@Composable
private fun SettledLoansToggle(showSettled: Boolean, settledCount: Int, onToggle: () -> Unit) {
    TextButton(onClick = onToggle) {
        Icon(
            if (showSettled) Icons.Filled.ArrowForward else Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = AppPrimary,
            modifier = Modifier.padding(end = 4.dp),
        )
        Text(
            if (showSettled) "بازگشت به وام‌های فعال" else "وام‌های تسویه‌شده (${toFa(settledCount)})",
            color = AppPrimary,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun LoanSortMenu(selected: LoanSortOption, onSelect: (LoanSortOption) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.FilterList, contentDescription = null, tint = AppPrimary, modifier = Modifier.padding(end = 4.dp))
            Text("فیلتر", color = AppPrimary, fontSize = 13.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            LoanSortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option.label,
                            color = if (option == selected) AppPrimary else AppText,
                        )
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
