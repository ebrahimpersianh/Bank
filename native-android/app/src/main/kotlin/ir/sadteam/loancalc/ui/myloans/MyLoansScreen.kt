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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.IncomeType
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.IncomeEntity
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.ui.stats.StatsScreen
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.auth.GateState
import ir.sadteam.loancalc.ui.auth.LoginScreen
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppCardVariant
import ir.sadteam.loancalc.ui.components.AppChip
import androidx.compose.material.icons.filled.Calculate
import ir.sadteam.loancalc.ui.components.AppFab
import ir.sadteam.loancalc.ui.components.AutoShrinkText
import ir.sadteam.loancalc.ui.components.BankBadge
import ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.PaidRing
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.ProgressRing
import ir.sadteam.loancalc.ui.components.SettledMedal
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.countUpDouble
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.components.rememberIsScrollingUp
import ir.sadteam.loancalc.ui.haptics.rememberBuzz
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.jibak.tomanToRial
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.subscription.SubscriptionScreen
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppElevation
import ir.sadteam.loancalc.ui.theme.AppGoldInk
import ir.sadteam.loancalc.ui.theme.AppGoldInk2
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.Motion
import ir.sadteam.loancalc.ui.theme.hardShadow
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

/** ذخیره ریال است و نمایش تومان (بندِ ۲ی README) - تبدیل فقط همین‌جا، لبه‌ی UI. */
private fun amountToman(rial: Double): String = fmt(rialToToman(rial.toLong()).toDouble()).faDigits()

private val jalaliMonthNames = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

/** «۲۸ شهریور» - سطرِ دومِ ردیفِ وامِ باز، طبقِ فریمِ `27a`. */
private fun jalaliShortOf(date: PersianDate): String =
    "${toFa(date.d)} ${jalaliMonthNames.getOrElse(date.m - 1) { "" }}"

/** «تیر ۱۴۰۵» - سطرِ دومِ وامِ تسویه‌شده. */
private fun jalaliMonthYearOf(date: PersianDate): String =
    "${jalaliMonthNames.getOrElse(date.m - 1) { "" }} ${toFa(date.y)}"

private fun isSameJalaliMonth(a: PersianDate, b: PersianDate) = a.y == b.y && a.m == b.m

/** «امروز» / «فردا» / «۳ روزِ دیگر» - حالتِ سررسیدِ نزدیکِ فریم. */
private fun dueSoonLabel(days: Int): String = when (days) {
    0 -> "امروز سررسید"
    1 -> "فردا سررسید"
    else -> "${toFa(days)} روزِ دیگر"
}

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
    // زدنِ نوتیفیکیشنِ یادآوریِ قسط باید مستقیم همون وام رو باز کنه (مورد ۵) - رجوع کن به
    // DeepLinkTarget/DeepLinkViewModel تو MainActivity.kt. غیرِnull یعنی «این وام رو باز کن»؛
    // بعدِ مصرف [onDeepLinkConsumed] صدا زده می‌شه تا با چرخشِ صفحه/رفرش دوباره تریگر نشه.
    deepLinkLoanId: Long? = null,
    onDeepLinkConsumed: () -> Unit = {},
    /**
     * خواسته‌ی کاربر (۲۶ شهریور): دکمه‌ی «+» **محاسبه‌گر** را باز می‌کند، نه فرمِ دستی را.
     * دلیلش هم روشن است - کسی که وام می‌گیرد اول می‌خواهد قسطش را ببیند؛ ثبتِ دستیِ وامِ
     * قدیمی کارِ کمتری است و حالا تهِ همان محاسبه‌گر ردیفِ خودش را دارد.
     */
    onOpenCalculator: () -> Unit = {},
    /** `true` یعنی محاسبه‌گر گفته «فرمِ دستی را باز کن» (رجوع کن به `LoanTab`). */
    openManualAddSignal: Boolean = false,
    onManualAddSignalConsumed: () -> Unit = {},
    /**
     * **فریمِ ۷۶ - جست‌وجو و تحلیلِ درآمد از فهرست به دو آیکونِ هم‌ردیفِ عنوان رفتند.**
     *
     * هر دو در ردیفِ «وام» (تو `LoanTab`) می‌نشینند، پس **صفر پیکسل** ارتفاعِ تازه
     * می‌گیرند - در حالی که فیلدِ همیشه‌بازِ تمام‌عرض و کارتِ ثابتِ درآمد با هم ~۱۳۰dp
     * از بالای فهرست می‌خوردند، برای فهرستی که معمولاً سه ردیف است.
     *
     * ⚠️ فقط جست‌وجو از بالا می‌آید. «تحلیل درآمد» در دورِ ۱۲ درِ ورودیِ خودش را گرفت
     * (قرصِ درصد داخلِ کارتِ طلایی) و آیکونِ نمودارِ هدر حذف شد.
     */
    searchOpen: Boolean = false,
) {
    var showAddForm by remember { mutableStateOf(false) }
    var openedLoanId by remember { mutableStateOf<Long?>(null) }
    // ویرایشِ مشخصاتِ کلیِ یه وام (اسم/بانک/مبلغ/تعدادِ اقساط) - عمداً openedLoanId رو پاک نمی‌کنیم
    // وقتی ویرایش باز می‌شه، فقط اولویتِ مسیریابی رو تو screenKey بالاتر می‌بریم؛ این‌طوری بعدِ
    // ذخیره/انصرافِ ویرایش (editingLoanId = null)، خودکار برمی‌گرده به همون صفحه‌ی جزئیاتِ وام،
    // نه لیست.
    var editingLoanId by remember { mutableStateOf<Long?>(null) }
    var showLoginPrompt by remember { mutableStateOf(false) }
    var showStats by remember { mutableStateOf(false) }
    // «کشیدن به پایین برای همگام‌سازی» - رجوع کن به MyLoansViewModel.syncNow برای اینکه
    // چرا این ژست عمداً فقط پوش می‌کنه و داده‌ی محلی رو با سرور جایگزین نمی‌کنه.
    var syncing by remember { mutableStateOf(false) }
    var showSubscriptionScreen by remember { mutableStateOf(false) }

    val rawLoans by viewModel.loans.collectAsState()
    var sortOption by remember { mutableStateOf(LoanSortOption.NEWEST) }
    val loans = remember(rawLoans, sortOption) { rawLoans.sortedByOption(sortOption, viewModel) }
    // جمعِ کلِ اقساطِ معوق (مورد ۱۹) - نیازمندِ کوئریِ suspend رو ردیف‌های واقعیِ هر وام، برای همین
    // با LaunchedEffect جدا از بقیه‌ی مبالغِ سینکرونِ داشبورد حساب می‌شه.
    var totalOverdue by remember { mutableStateOf(0.0) }
    LaunchedEffect(loans) { totalOverdue = viewModel.totalOverdueAmount(loans) }
    // **تعداد** هم لازم است، نه فقط مبلغ (خواسته‌ی کاربر، دورِ ۹): «۲۰ تا اقساطِ معوق».
    // یک قسطِ بزرگ و بیست قسطِ کوچک جمعشان یکی است ولی دو وضعیتِ کاملاً متفاوت‌اند.
    // ⚠️ نامش عمداً `overdueCount` **نیست**: چند ده خط پایین‌تر یک `overdueCount`ِ دیگر
    // هست که تعدادِ **وام**‌های عقب‌افتاده را می‌شمارد. این یکی تعدادِ **قسط** است -
    // دو عددِ متفاوت. هم‌نام‌بودنشان بیلدِ ۵۵۳ را شکست («Conflicting declarations»).
    var overdueInstallments by remember { mutableStateOf(0) }
    LaunchedEffect(loans) { overdueInstallments = viewModel.totalOverdueCount(loans) }
    // «مجموع اقساط ماهانه» (مورد ۱۴/۳۵) - قبلاً از loan.installmentِ کهنه حساب می‌شد که بعدِ
    // ویرایشِ تکیِ یه قسط دیگه درست نبود؛ الان از رو مبلغِ واقعیِ قسطِ همینِ الانِ هر وام.
    var totalMonthlyInstallment by remember { mutableStateOf(0.0) }
    LaunchedEffect(loans) { totalMonthlyInstallment = viewModel.totalCurrentInstallment(loans) }
    // «وام‌های تسویه‌شده»: هم‌الگو با showArchived تو ChequeScreen - وامی که تسویه شده (isLoanSettled)
    // خودکار از لیستِ فعال بیرون میره، پشتِ همین تاگل نمایش داده می‌شه. لیستِ اصلی (loans، برای
    // openedLoan/editingLoan/canSaveAnotherLoan/DashboardSummary) عمداً فیلتر نمی‌شه - فقط لیستِ
    // نمایشیِ پایینِ صفحه (visibleLoans).
    // 🚨 **سه‌حالته شد** (طرحِ مرجعِ کاربر، ۳۱ شهریور): پیش از این یک کلیدِ دوحالته بود و
    // هیچ راهی نبود هر دو گروه را با هم دید. `showSettled` برای بقیه‌ی صفحه مشتق می‌ماند،
    // پس شرط‌های موجود دست‌نخورده کار می‌کنند.
    var loanFilter by remember { mutableStateOf(LoanFilter.ACTIVE) }
    val showSettled = loanFilter == LoanFilter.SETTLED
    var searchQuery by remember { mutableStateOf("") }
    // بستنِ فیلدِ جست‌وجو باید فیلتر را هم بردارد - وگرنه فهرست فیلترشده می‌مانَد و
    // دلیلش دیگر روی صفحه دیده نمی‌شود، یعنی کاربر فکر می‌کند وام‌هایش گم شده‌اند.
    LaunchedEffect(searchOpen) { if (!searchOpen) searchQuery = "" }
    // تاریخِ تسویه = تاریخِ پرداختِ آخرین قسط (ستونِ تازه لازم نیست - همون استدلالی که
    // settledAt رو منتفی کرد). دو کاربرد: سطرِ دومِ ردیفِ تسویه‌شده، و شرطِ «همین ماه».
    var settledDates by remember { mutableStateOf(emptyMap<Long, PersianDate>()) }
    LaunchedEffect(loans) {
        settledDates = viewModel.lastPaidDates(loans.filter { isLoanSettled(it) })
    }
    val today = remember { viewModel.todayJalali() }
    // وامی که همین ماهِ جاری تسویه شده، تو حالتِ «فعال» هم دیده می‌شه (با مدالِ روبان‌دار) -
    // تصمیمِ ۲ی تحویلِ 27a. لحظه‌ی پرداختِ آخرین قسط لحظه‌ی دستاورده؛ بدترین وقت برای
    // غیب‌شدنِ کارت. از ماهِ بعد فقط زیرِ فیلترِ دوم.
    val visibleLoans = remember(loans, loanFilter, searchQuery, settledDates, today) {
        val q = searchQuery.trim()
        loans.filter { loan ->
            val settled = isLoanSettled(loan)
            val keep = when (loanFilter) {
                LoanFilter.ALL -> true
                LoanFilter.SETTLED -> settled
                LoanFilter.ACTIVE ->
                    !settled || settledDates[loan.id]?.let { isSameJalaliMonth(it, today) } == true
            }
            keep && (
                q.isBlank() || loan.name.contains(q, ignoreCase = true) ||
                    loan.bank.contains(q, ignoreCase = true)
                )
        }
    }
    val settledCount = remember(loans) { loans.count { isLoanSettled(it) } }

    // شمارشِ وام‌های عقب‌افتاده‌ی همین نما. روی `visibleLoans` حساب می‌شود نه `loans`، تا
    // وقتی کاربر جستجو کرده عدد با چیزی که جلوی چشمش است بخواند.
    val overdueCount = remember(visibleLoans, showSettled, today) {
        if (showSettled) 0 else visibleLoans.count { viewModel.isLoanOverdue(it) }
    }

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
    // قفلِ وام‌ها بعدِ اتمامِ دوره‌ی آزمایشی (مورد ۱۱): همون منطقِ canSaveAnotherLoan (فقط کاربرِ
    // مشترک/تو دوره‌ی آزمایشی می‌تونه بیشتر از یه وام داشته باشه)، ولی برعکس - این‌جا برای وام‌های
    // *ازقبل‌موجود* (نه محدودیتِ ساختنِ وامِ جدید). قدیمی‌ترین وام (بر اساسِ createdAt) همیشه رایگان/
    // بازه؛ بقیه اگه subscribed=false شدن (چه اصلاً مشترک نبوده چه دوره‌ی آزمایشیش تموم شده) قفل
    // می‌شن - نه حذف/پاک، فقط غیرقابلِ‌بازشدن، و به‌محضِ subscribed=true شدن (خریدِ واقعی یا حتی
    // دوباره واردِ دوره‌ی آزمایشی) خودکار باز می‌شن چون این فقط یه محاسبه‌ی مشتق‌شده‌ست، نه یه
    // فلگِ ذخیره‌شده.
    val oldestLoanId = remember(loans) { loans.minByOrNull { it.createdAt }?.id }
    fun isLoanLocked(loan: LoanEntity): Boolean = !subscribed && loan.id != oldestLoanId

    // زدنِ نوتیفیکیشنِ یادآوریِ قسط (مورد ۵) نباید بتونه یه وامِ قفل‌شده رو دور بزنه - این افکت باید
    // بعدِ محاسبه‌ی isLoanLocked باشه (اینجا، نه بالای فایل جایی که loans/subscribed هنوز مقداردهی
    // نشدن).
    LaunchedEffect(deepLinkLoanId) {
        val target = deepLinkLoanId ?: return@LaunchedEffect
        val loan = loans.firstOrNull { it.id == target }
        if (loan != null && isLoanLocked(loan)) {
            if (gateState != GateState.LOGGED_IN) showLoginPrompt = true else showSubscriptionScreen = true
        } else {
            openedLoanId = target
        }
        onDeepLinkConsumed()
    }

    // سیگنالِ ورودی از محاسبه‌گر: همان گیتِ اشتراک/ورود را می‌خورد که دکمه‌ی خودِ این صفحه
    // می‌خورد - وگرنه یک مسیرِ دورزننده‌ی سقفِ وام ساخته می‌شد.
    fun openManualAddForm() {
        when {
            canSaveAnotherLoan -> showAddForm = true
            gateState == null -> Unit
            gateState != GateState.LOGGED_IN -> showLoginPrompt = true
            else -> showSubscriptionScreen = true
        }
    }

    LaunchedEffect(openManualAddSignal, canSaveAnotherLoan, gateState) {
        if (openManualAddSignal && gateState != null) {
            openManualAddForm()
            onManualAddSignalConsumed()
        }
    }

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
        showStats -> "stats"
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
            "stats" -> showStats = false
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
            "stats" -> StatsScreen(onBack = { showStats = false })
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
                    contentPadding = PaddingValues(22.dp, 12.dp, 22.dp, 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        DashboardSummary(
                            loans = loans,
                            incomes = incomes,
                            totalOverdue = totalOverdue,
                            overdueCount = overdueInstallments,
                            totalMonthlyInstallment = totalMonthlyInstallment,
                            onAddIncome = { label, amount, type -> viewModel.addIncome(label, amount, type) },
                            onDeleteIncome = { viewModel.deleteIncome(it) },
                        )
                    }

                    if (loans.isNotEmpty() && searchOpen) {
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("جستجو تو وام‌ها (اسم/بانک)...") },
                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                        }
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
                                        filter = loanFilter,
                                        allCount = loans.size,
                                        settledCount = settledCount,
                                        activeCount = loans.size - settledCount,
                                        overdueLoanCount = overdueCount,
                                        onFilter = { loanFilter = it },
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

                    // ⚠️ نوارِ «N وام عقب‌افتاده» **حذف شد** (جوابِ دورِ ۱۲): همان خبر را
                    // کارتِ طلایی با واحدِ «قسط» و این نوار با واحدِ «وام» می‌گفت، یعنی یک
                    // موضوع با دو عدد. عددش حالا روی قرصِ «فعال» می‌نشیند - صفر پیکسلِ تازه.


                    if (visibleLoans.isEmpty()) {
                        item {
                            if (searchQuery.isNotBlank()) {
                                EmptyState(
                                    icon = Icons.Filled.Search,
                                    title = "چیزی پیدا نشد",
                                    description = "وامی با این اسم/بانک پیدا نشد.",
                                )
                            } else if (showSettled) {
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
                            // فریمِ 27a سه حالتِ ردیف داره و کارتِ **فوری** مالِ «سررسیدِ نزدیک»ه
                            // (تو خودِ فریم: وامی که فردا قسط داره)، نه فقط عقب‌افتاده. آستانه‌ی
                            // «نزدیک» سه روزه، همون آستانه‌ی یادآورِ اپ.
                            val nextDue = remember(loan) { viewModel.getLoanNextDueDate(loan) }
                            val daysToDue = remember(nextDue) { nextDue?.let { viewModel.daysUntilToday(it) } }
                            val dueSoon = daysToDue != null && daysToDue in 0..3
                            val isLocked = isLoanLocked(loan)
                            val attention = (overdue || dueSoon) && !isLocked
                            // وامِ عقب‌افتاده گونه‌ی **فوریِ** کارت رو می‌گیره (زمینه‌ی صورتیِ کم‌رنگ
                            // + حاشیه و سایه‌ی قرمز)، نه فقط یه حاشیه‌ی قرمز رو کارتِ سفید -
                            // طبقِ گونه‌ی «فوری»ِ بخشِ ۵ سیستمِ طراحی.
                            AppCard(
                                variant = when {
                                    isLoanSettled(loan) -> AppCardVariant.DONE
                                    attention -> AppCardVariant.URGENT
                                    else -> AppCardVariant.DEFAULT
                                },
                                // مدالِ تسویه نباید با بقیه‌ی محتوا محو بشه.
                                dimContent = false,
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
                                        if (isLocked) {
                                            // نه واردِ جزئیات می‌شه نه چیزی پاک/عوض می‌کنه - فقط
                                            // مستقیم می‌بره سراغِ خریدِ اشتراک، چون تنها راهِ بازشدنِ
                                            // این وام همونه.
                                            if (gateState != GateState.LOGGED_IN) {
                                                showLoginPrompt = true
                                            } else {
                                                showSubscriptionScreen = true
                                            }
                                            return@pressScaleClickable
                                        }
                                        // مرکزِ همین کارت رو به کسرِ ۰..۱ از کلِ صفحه تبدیل می‌کنیم تا
                                        // بزرگ‌شدنِ صفحه‌ی جزئیات دقیقاً از همین‌جا شروع بشه.
                                        heroOrigin = cardBounds.heroOriginIn(listBounds)
                                        openedLoanId = loan.id
                                    }
                                    .graphicsLayer {
                                        rotationY = rotation
                                        cameraDistance = 12f * density.density
                                        translationY = if (isDragging) dragOffsetY else 0f
                                        alpha = if (isLocked) 0.55f else 1f
                                    },
                            ) {
                                if (rotation <= 90f) {
                                    // ردیفِ وام طبقِ فریمِ `27a` - سه حالت: سررسیدِ نزدیک (کارتِ
                                    // فوری + دکمه‌ی پرداخت)، در جریان (کارتِ معمولی + شِورون)،
                                    // تسویه‌شده (کارتِ تمام‌شده + مدال). حلقه همیشه سمتِ راست.
                                    val settled = isLoanSettled(loan)
                                    val paidPct = if (loan.n > 0) loan.paidCount.toFloat() / loan.n else 0f
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        // 🚨 **کاشیِ نشان جای حلقه، سمتِ راست** (طرحِ مرجعِ
                                        // کاربر: «باکسِ هر وام مثلِ همین عکس با همان آیکون‌ها»).
                                        // حلقه رفت سمتِ چپ و ریزتر شد؛ چیزی که چشم اول باید
                                        // بگیرد **نوعِ وام** است نه درصدش، و ده ردیفِ حلقه‌دارِ
                                        // هم‌شکل دقیقاً همان بی‌روحی‌ای بود که کاربر گفت.
                                        LoanGlyphTile(
                                            glyph = loanGlyphFor(loan.name, viewModel.categoryOf(loan)),
                                            tint = when {
                                                settled -> AppPrimaryInk
                                                attention -> AppDangerInk
                                                else -> AppPrimaryInk
                                            },
                                            background = when {
                                                settled -> AppPrimaryPill
                                                attention -> AppDangerPill
                                                else -> AppPrimaryPill
                                            },
                                        )
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .then(if (settled) Modifier.alpha(0.7f) else Modifier),
                                            verticalArrangement = Arrangement.spacedBy(3.dp),
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    loan.name,
                                                    color = AppText,
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Black,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false),
                                                )
                                                // بجِ وضعیت کنارِ اسم (طرحِ مرجع) - رنگِ کارت
                                                // همین را می‌گفت ولی بی کلمه، و کسی که رنگ را
                                                // نمی‌خوانَد هیچ‌وقت نمی‌فهمید کدام عقب‌افتاده است.
                                                val stateLabel = when {
                                                    settled -> "تسویه شده"
                                                    overdue -> "معوق"
                                                    dueSoon -> "نزدیک"
                                                    else -> null
                                                }
                                                if (stateLabel != null) {
                                                    Text(
                                                        stateLabel,
                                                        color = if (settled) AppPrimaryInk else AppDangerInk,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Black,
                                                        modifier = Modifier
                                                            .padding(start = 6.dp)
                                                            .clip(RoundedCornerShape(999.dp))
                                                            .background(if (settled) AppPrimaryPill else AppDangerPill)
                                                            .padding(horizontal = 7.dp, vertical = 2.dp),
                                                    )
                                                }
                                                if (isLocked) {
                                                    Icon(
                                                        Icons.Filled.Lock,
                                                        contentDescription = "این وام قفله - برای بازکردنش مشترک شو",
                                                        tint = AppMuted,
                                                        modifier = Modifier.padding(start = 6.dp).size(13.dp),
                                                    )
                                                }
                                            }
                                            // سطرِ دوم طبقِ فریم: **تاریخِ سررسید** + مبلغِ قسط
                                            // («۲۸ شهریور · ۹۵۰٬۰۰۰»)، و برای تسویه‌شده تاریخِ
                                            // تسویه («تسویه شد · تیر ۱۴۰۵»). کلمه‌ی «در جریان»
                                            // اطلاعِ صفر داشت - همه‌ی ردیف‌های لیستِ فعال در جریان‌اند.
                                            PrivacyCrossfade(LocalPrivacyMode.current) { masked ->
                                                Text(
                                                    buildString {
                                                        if (settled) {
                                                            append("تسویه شد · ")
                                                            append(
                                                                settledDates[loan.id]
                                                                    ?.let { jalaliMonthYearOf(it) } ?: "—",
                                                            )
                                                        } else {
                                                            append(
                                                                when {
                                                                    overdue -> "عقب‌افتاده"
                                                                    dueSoon -> dueSoonLabel(daysToDue!!)
                                                                    else -> nextDue?.let { jalaliShortOf(it) } ?: "—"
                                                                },
                                                            )
                                                            append(" · ")
                                                            append(maskIfPrivate(masked, amountToman(loan.installment)))
                                                            append(" تومان")
                                                        }
                                                    },
                                                    color = when {
                                                        settled -> AppPrimaryInk
                                                        overdue -> AppDangerInk
                                                        dueSoon -> AppDangerInk
                                                        else -> AppMuted
                                                    },
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                )
                                            }
                                            // سطرِ سوم فقط برای وامِ بازه - وامِ تسویه‌شده نداردش.
                                            if (!settled) {
                                                // دو نشانِ ریز جای نقطه‌ی جداکننده (طرحِ مرجع):
                                                // چشم «۲ از ۱۲» و نامِ بانک را جدا می‌بیند، نه
                                                // یک رشته‌ی طولانیِ یکدست.
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    modifier = Modifier.padding(top = 2.dp),
                                                ) {
                                                    Icon(
                                                        Icons.Outlined.EventNote,
                                                        contentDescription = null,
                                                        tint = AppLabel,
                                                        modifier = Modifier.size(10.dp),
                                                    )
                                                    Text(
                                                        "${toFa(loan.paidCount)} از ${toFa(loan.n)} قسط",
                                                        color = AppMuted,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                    )
                                                    Icon(
                                                        Icons.Outlined.AccountBalance,
                                                        contentDescription = null,
                                                        tint = AppLabel,
                                                        modifier = Modifier.padding(start = 3.dp).size(10.dp),
                                                    )
                                                    Text(
                                                        loan.bank,
                                                        color = AppMuted,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                    )
                                                }
                                            }
                                        }
                                        // ستونِ چپ: درصد بالا، کنشِ ردیف پایین - همان چیدمانِ
                                        // طرحِ مرجع. حلقه این‌جا **ریز** است چون خبرِ درجه‌دوم
                                        // است؛ خبرِ اول مبلغِ عقب‌افتاده‌ی وسطِ ردیف است.
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            if (settled) {
                                                SettledMedal(diskSize = 34.dp)
                                            } else {
                                                PaidRing(
                                                    fraction = paidPct,
                                                    ringColor = if (attention) AppDangerInk else AppPrimary,
                                                    trackColor = (if (attention) AppDangerInk else AppPrimary)
                                                        .copy(alpha = 0.2f),
                                                    centerTop = "${toFa((paidPct * 100).roundToInt())}٪",
                                                    centerBottom = "",
                                                    centerTopColor = if (attention) AppDangerInk else AppPrimary,
                                                    centerBottomColor = AppMuted,
                                                    size = 40.dp,
                                                    stroke = 5.dp,
                                                    centerTopSize = 10,
                                                )
                                            }
                                            if (attention) {
                                                LoanPayButton(
                                                    onClick = {
                                                        heroOrigin = cardBounds.heroOriginIn(listBounds)
                                                        openedLoanId = loan.id
                                                    },
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Filled.ChevronLeft,
                                                    contentDescription = null,
                                                    tint = AppLabel,
                                                    modifier = Modifier.padding(top = 4.dp).size(14.dp),
                                                )
                                            }
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
                                                "باقی‌مانده: ${maskIfPrivate(masked, amountToman(loan.installment * (loan.n - loan.paidCount)))} تومان",
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
                    // **پشتیبان‌گیری به تهِ فهرست رفت** (خواسته‌ی صریحِ کاربر، دورِ ۱۲):
                    // کاری است که کاربر سالی چند بار می‌کند، ولی بالای فهرست می‌نشست و
                    // جای وام‌ها را می‌گرفت - و شکایتِ اصلی همین بود که «وام‌های من پیدا
                    // نیستند». پایینِ فهرست هم پیداست هم سرِ راه نیست.
                    item {
                        BackupRestoreRow(
                            onBackup = {
                                viewModel.exportBackup { json ->
                                    pendingExportJson = json
                                    createDocumentLauncher.launch("loans-backup.json")
                                }
                            },
                            onRestore = { openDocumentLauncher.launch(arrayOf("application/json")) },
                        )
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
                // ۱۶dp از لبه، مثلِ FABِ خانه/گزارش/دارایی.
                .padding(16.dp),
        ) {
            AppFab(
                // مقصدش عوض شد: محاسبه‌گر، نه فرمِ دستی (خواسته‌ی صریحِ کاربر، ۲۶ شهریور).
                onClick = { onOpenCalculator() },
                // 🚨 **نماد دیگر «+»ِ خالی نیست** (جوابِ طراح، دورِ ۸): «+» وعده‌ی افزودن
                // می‌دهد و صفحه‌ی محاسبه باز می‌کند، پس کاربری که وامش را از قبل ثبت کرده
                // انتظارِ فرم دارد و ماشین‌حساب می‌بیند. ولی ماشین‌حسابِ تنها هم بد است، چون
                // FAB در هر پنج تبِ دیگر جای «اضافه کردن» است و یک‌دستی می‌شکند.
                // پس ماشین‌حساب با یک «+»ِ ریز روی گوشه: مقصد را می‌گوید (حساب می‌کنی) و
                // کارِ نهایی را هم (چیزی اضافه می‌شود).
                icon = Icons.Filled.Calculate,
                plusBadge = true,
                contentDescription = "محاسبه‌ی قسط و سود",
                modifier = Modifier.onGloballyPositioned {
                    onManualAddFabPositioned(it.boundsInRoot())
                },
            )
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
/**
 * حلقه‌ی ۵۲dpیِ ردیفِ وام - فریمِ `27a`. ضخامتِ ۶٫۵، سرِ گرد، از ساعتِ ۱۲ پادساعت‌گرد.
 * وسطش درصد می‌نویسه، مگر وامِ تسویه‌شده که تیک می‌گیره.
 *
 * @param showCoin سکه‌ی ۱۱dpیِ «نزدیک‌ترین قسط» رو گوشه‌ی بالا-راستِ حلقه (فقط حالتِ فوری).
 */
/**
 * **ردیفِ پشتیبان‌گیری و بازیابی - فریمِ `27a`.**
 *
 * یه ردیفِ فهرستِ ساده با قابِ آیکونِ ۳۰ی. تپ روش یه شیتِ دوگزینه‌ای باز می‌کنه، چون فریم
 * **یک** ردیف داره ولی ما دو تا کار داریم (گرفتن و برگردوندن) - جاسازیِ دو دکمه تو یه ردیف
 * هدفِ لمسی رو زیرِ ۴۴dp می‌برد که خلافِ بندِ ۸ سیستمِ طراحیه.
 */
/**
 * ردیفِ فهرستِ سبکِ `27a` - قابِ آیکونِ ۳۰ + عنوان + فلش. دو مصرف دارد (پشتیبان‌گیری و
 * آمار)، پس یک‌بار نوشته شد نه دوبار.
 */
@Composable
private fun LoanListActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppSurface)
            .border(2.dp, AppLineRow, RoundedCornerShape(16.dp))
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(AppSurface2),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = AppMuted, modifier = Modifier.size(16.dp))
        }
        Text(
            label,
            color = AppText,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = null,
            tint = AppMuted,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun BackupRestoreRow(onBackup: () -> Unit, onRestore: () -> Unit) {
    var showSheet by remember { mutableStateOf(false) }

    if (showSheet) {
        AlertDialog(
            onDismissRequest = { showSheet = false },
            title = { Text("پشتیبان‌گیری و بازیابیِ وام‌ها") },
            text = { Text("یه فایلِ پشتیبان از همه‌ی وام‌هات بساز، یا یه فایلِ قبلی رو برگردون.") },
            confirmButton = {
                TextButton(onClick = {
                    showSheet = false
                    onBackup()
                }) { Text("گرفتنِ پشتیبان", color = AppPrimary) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSheet = false
                    onRestore()
                }) { Text("بازیابی از فایل", color = AppMuted) }
            },
        )
    }

    LoanListActionRow(
        icon = Icons.Filled.CloudUpload,
        label = "پشتیبان‌گیری و بازیابیِ وام‌ها",
        onClick = { showSheet = true },
    )
}


/**
 * خطِ «N وام عقب‌افتاده» بالای فهرست.
 *
 * عمداً **کارتِ کامل نیست و دکمه ندارد**: کاری برای انجام‌دادن پیشنهاد نمی‌کند، فقط عدد را
 * می‌گوید. خودِ کارتِ هر وام دکمه‌ی پرداختش را دارد.
 */
@Composable
private fun OverdueSummaryRow(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.row))
            .background(AppDangerPill)
            .padding(horizontal = 13.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = AppDangerInk,
            modifier = Modifier.size(16.dp),
        )
        Text(
            "${toFa(count)} وام عقب‌افتاده",
            color = AppDangerInk,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

/** دکمه‌ی «پرداخت»ِ ردیفِ سررسیدِ نزدیک - فریمِ `27a`. */
@Composable
private fun LoanPayButton(onClick: () -> Unit) {
    // 🚨 قابِ بیرونی **هیچ‌وقت `size` ثابت نگیرد.** قبلاً `Modifier.size(44.dp)` بود و
    // چون خودِ کپسول پهن‌تر از ۴۴ است، متن بریده می‌شد و روی گوشیِ کاربر «پردا» دیده
    // می‌شد. ارتفاعِ هدفِ لمسی با `defaultMinSize` تامین می‌شود، نه با بریدنِ عرض.
    Box(
        modifier = Modifier.defaultMinSize(minHeight = 44.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .hardShadow(AppPrimaryDim, AppElevation.inRow, 999.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(AppPrimary)
                .pressScaleClickable(onClick = onClick)
                .padding(horizontal = 13.dp, vertical = 9.dp),
        ) {
            Text(
                "پرداخت",
                color = Color.White,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

@Composable
private fun DashboardSummary(
    loans: List<LoanEntity>,
    incomes: List<IncomeEntity>,
    // جمعِ مبلغِ اقساطِ معوق (مورد ۱۹) + مجموعِ اقساطِ ماهانه‌ی واقعی (مورد ۱۴/۳۵) - هردو از بیرون
    // پاس داده می‌شن چون محاسبه‌شون suspend ئه (رجوع کن به LaunchedEffect(loans) تو MyLoansScreen)؛
    // totalMonthlyInstallment قبلاً همین‌جا از loan.installmentِ کهنه حساب می‌شد.
    totalOverdue: Double,
    overdueCount: Int,
    totalMonthlyInstallment: Double,
    onAddIncome: (label: String, amount: Double, type: IncomeType) -> Unit,
    onDeleteIncome: (IncomeEntity) -> Unit,
) {
    val totalRemainingDebt = remember(loans) {
        loans.sumOf { it.installment * (it.n - it.paidCount) }
    }
    val totalIncome = remember(incomes) { incomes.sumOf { it.amount } }
    val ratio = if (totalIncome > 0) totalMonthlyInstallment / totalIncome else 0.0
    // `null` یعنی هیچ منبعِ درآمدی ثبت نشده - و آن‌جا به‌جای «۰٪»ِ گمراه‌کننده، درِ ورودی
    // نشان داده می‌شود.
    val incomeRatio: Float? = if (totalIncome > 0) ratio.toFloat() else null
    // 🚨 **آیکونِ نمودارِ هدر حذف شد** (جوابِ دورِ ۱۲): درِ ورودیِ درآمد حالا همان قرصِ
    // درصد است، یعنی همان‌جایی که عدد دیده می‌شود. آیکونِ هدر یک درِ دومِ بی‌نشانه بود.
    var showIncome by remember { mutableStateOf(false) }
    val onOpenIncome: () -> Unit = { showIncome = !showIncome }
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
    var incomePendingDelete by remember { mutableStateOf<IncomeEntity?>(null) }

    // ماهِ جاری برای برچسبِ نقطه‌ی موج. `remember` بی‌کلید کافی است - روزِ تقویم وسطِ
    // یک ترکیب عوض نمی‌شود.
    val todayForWave = remember { JalaliCalendar.today() }

    // شمارش صعودی اعداد بزرگ داشبورد (پورت animateNumber وب) - حس «پریمیوم» موقع ورود به تب.
    val animatedDebt = countUpDouble(totalRemainingDebt)
    val animatedMonthly = countUpDouble(totalMonthlyInstallment)
    val privacyMode = LocalPrivacyMode.current

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // ⚠️ **بازطراحیِ سبکِ «جیبک»** - کارتِ خلاصه‌ی وام (کارتِ `27a`ی فایلِ طراحی).
        // قبلاً سه کارتِ سفیدِ جدا بود (بدهی/قسطِ ماهانه/معوق). طرح یه **کارتِ کاغذِ طلاییِ
        // واحد** با سه ردیفِ جداشده می‌خواد - طلایی اینجا موجهه چون این کارتِ «پول»ه، همون
        // کاربردی که قاعده‌ی «طلایی فقط پرمیوم/پول» اجازه می‌ده.
        // 🚨 **کارتِ طلایی سه لایه‌ی اطلاعاتی دارد، نه چهار ردیفِ هم‌وزن** (بندِ ۳ فریمِ `76c`
        // و خواسته‌ی دوباره‌ی کاربر در دورِ ۱۲: «اصلاً وام‌های من پیدا نیستند»).
        //
        // چهار ردیفِ تمام‌عرضِ برچسب/مقدار با سه خطِ جداکننده ~۲۲۰dp می‌خورد، یعنی کاربر
        // برای رسیدن به اولین وام باید اسکرول کند. حالا:
        //   بالا  - بجِ معوق و درصدِ پرداخت‌شده، هر دو ریز و در یک ردیف
        //   وسط  - **قسطِ این ماه** درشت (تنها عددِ قابلِ‌اقدام)
        //   پایین - ماندهٔ کل و تا آزادی، کنارِ هم و ریز
        // ترتیب **دلیل** دارد: از «چقدر عقبم» به «حالا چقدر بدهم» به «کلِ ماجرا».
        val monthsLeft = remember(loans) { loans.maxOfOrNull { it.n - it.paidCount } ?: 0 }
        val paidPct = remember(loans) {
            val totalRows = loans.sumOf { it.n }
            if (totalRows > 0) loans.sumOf { it.paidCount } * 100 / totalRows else 0
        }
        AppCard(variant = AppCardVariant.GOLD) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (overdueCount > 0) {
                    Text(
                        "${toFa(overdueCount)} قسطِ معوق",
                        color = AppDangerInk,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(AppDangerPill)
                            .padding(horizontal = 9.dp, vertical = 3.dp),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            // 🚨 **حلقه جای نوارِ تخت** (طرحِ مرجعِ کاربر، ۳۱ شهریور): نوار درصد را
            // بی‌عدد می‌گفت و «چند قسط مانده» هیچ‌جای این کارت نبود. حلقه هر دو را
            // می‌دهد و ارتفاعِ تازه‌ای هم نمی‌گیرد چون کنارِ عددِ قهرمان می‌نشیند.
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "قسطِ این ماه",
                    color = AppGoldInk.copy(alpha = 0.65f),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black,
                )
                PrivacyCrossfade(privacyMode) { masked ->
                    Text(
                        "${maskIfPrivate(masked, amountToman(animatedMonthly))} تومان",
                        color = AppGoldInk,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.4).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
                val rowsLeft = remember(loans) { loans.sumOf { it.n - it.paidCount }.coerceAtLeast(0) }
                val rowsAll = remember(loans) { loans.sumOf { it.n } }
                // 🚨 **حلقه ضخیم‌تر شد و «٪ پرداخت‌شده» زیرش نشست** (طرحِ مرجعِ کاربر).
                // پیش از این حلقه فقط «چند قسط مانده» را می‌گفت و درصد هیچ‌جای کارت نبود؛
                // آن دو یک جفت‌اند - «چقدر مانده» بی «چقدر رفته» نصفِ خبر است.
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    PaidRing(
                        fraction = paidPct / 100f,
                        ringColor = AppGoldInk,
                        trackColor = AppGoldInk.copy(alpha = 0.22f),
                        centerTop = toFa(rowsLeft),
                        centerBottom = "از ${toFa(rowsAll)} قسط",
                        centerTopColor = AppGoldInk,
                        centerBottomColor = AppGoldInk.copy(alpha = 0.7f),
                        size = 78.dp,
                        stroke = 11.dp,
                        centerTopSize = 19,
                    )
                    Row(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(AppGoldInk.copy(alpha = 0.12f))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(
                            Icons.Filled.Autorenew,
                            contentDescription = null,
                            tint = AppGoldInk,
                            modifier = Modifier.size(9.dp),
                        )
                        Text(
                            "${toFa(paidPct)}٪ پرداخت‌شده",
                            color = AppGoldInk,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                        )
                    }
                }
            }
            // 🚨 **نسبتِ قسط به درآمد آمد داخلِ کارت** (بندِ ۱ جوابِ دورِ ۱۲).
            //
            // این عدد کسرِ همین «قسطِ این ماه» بر درآمد است، پس جایش زیرِ همان عدد است نه
            // در تهِ یک کارتِ جمع‌شونده - و وقتی از ۱۰۰ بگذرد یعنی قسط از درآمد بیشتر
            // است، که مهم‌ترین خبرِ کلِ صفحه است.
            //
            // ⚠️ در عوض «٪ پرداخت‌شده» از کارت رفت: **یک کارت، یک عددِ درصددار**. دو
            // درصد کنارِ هم روی یک کارت، هر دو را بی‌معنی می‌کند (کدام مالِ کدام؟).
            // خودِ پیشرفت با نوارِ زیر گفته می‌شود، بی عدد.
            if (incomeRatio != null) {
                val over = incomeRatio > 1f
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (over) AppDangerPill else AppGoldInk.copy(alpha = 0.12f))
                        .pressScaleClickable(onClick = onOpenIncome)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        "${toFa((incomeRatio * 100).roundToInt())}٪ از درآمدت صرفِ اقساط می‌شه",
                        color = if (over) AppDangerInk else AppGoldInk,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Icon(
                        Icons.Filled.ChevronLeft,
                        contentDescription = null,
                        tint = if (over) AppDangerInk else AppGoldInk,
                        modifier = Modifier.size(12.dp),
                    )
                }
            } else {
                // بی منبعِ درآمد، درصدی وجود ندارد - پس به‌جای عددِ دروغ، درِ ورودی.
                Text(
                    "درآمدِ ماهانه‌ات را ثبت کن ‹",
                    color = AppGoldInk.copy(alpha = 0.75f),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .pressScaleClickable(onClick = onOpenIncome)
                        .padding(vertical = 3.dp),
                )
            }
            // 🌊 **موجِ ماندهٔ بدهی** - خواسته‌ی کاربر با طرحِ مرجع: «آن خطِ پایینِ رقم».
            // شش نقطه = ماندهٔ بدهی در شش ماهِ پیشِ رو، و نقطه‌ی برجسته ماهِ جاری است.
            // پس شیبِ خط دقیقاً همان چیزی را می‌گوید که کاربر می‌خواهد بداند: دارد کم می‌شود.
            val debtCurve = remember(loans, totalMonthlyInstallment, totalRemainingDebt) {
                val monthly = totalMonthlyInstallment
                (0 until 6).map { month ->
                    (totalRemainingDebt - monthly * month).coerceAtLeast(0.0).toFloat()
                }
            }
            if (debtCurve.any { it > 0f } && totalMonthlyInstallment > 0) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    DebtTrendWave(
                        points = debtCurve,
                        lineColor = AppGoldInk.copy(alpha = 0.45f),
                        dotColor = AppGoldInk,
                    )
                    Text(
                        persianMonthName(todayForWave.m),
                        color = AppGoldInk,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(999.dp))
                            .background(AppGoldInk.copy(alpha = 0.14f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
            // ⚠️ نوارِ تختِ پیشرفت **حذف شد**: همان درصد حالا در حلقه است و دو گرافیک
            // برای یک عدد، همان چیزی است که این صفحه یک‌بار از آن پاک شد.
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Column {
                    Text(
                        "ماندهٔ کل",
                        color = AppGoldInk.copy(alpha = 0.65f),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black,
                    )
                    PrivacyCrossfade(privacyMode) { masked ->
                        Text(
                            maskIfPrivate(masked, amountToman(animatedDebt)),
                            color = AppGoldInk,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 1.dp),
                        )
                    }
                }
                if (monthsLeft > 0) {
                    Column {
                        Text(
                            "تا آزادی",
                            color = AppGoldInk.copy(alpha = 0.65f),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            "${toFa(monthsLeft)} ماه",
                            color = AppGoldInk,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 1.dp),
                        )
                    }
                }
                // مبلغِ معوق فقط وقتی واقعاً هست - وگرنه ستونِ صفرِ همیشگی.
                if (totalOverdue > 0) {
                    Column {
                        Text(
                            "معوق",
                            color = AppDangerInk.copy(alpha = 0.75f),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                        )
                        PrivacyCrossfade(privacyMode) { masked ->
                            Text(
                                maskIfPrivate(masked, amountToman(totalOverdue)),
                                color = AppDangerInk,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(top = 1.dp),
                            )
                        }
                    }
                }
            }
        }

        // یه پس‌زمینه‌ی سبزِ اختصاصیِ نیمه‌شفاف اینجا امتحان شده بود، ولی رو Surface (که خودش
        // tonalElevation داره) رنگ‌ها بهم می‌ریخت و دوتُنی/کثیف به‌نظر می‌رسید. کاربر خواست دقیقاً
        // مثل بقیه‌ی کارت‌های داشبورد (DashboardStatCard بالا) باشه - پس همون پیش‌فرضِ AppCard.
        // 🚨 **بندِ ۴ فریمِ ۷۶c**: این کارت قبلاً همیشه بود، حتی وقتی هیچ منبعِ درآمدی ثبت
        // نشده بود - یعنی یک کارتِ تمام‌عرض که تنها محتوایش یک دکمه‌ی «+ افزودن» بود.
        // کارتی که هیچ‌وقت پر نیست، وزنِ محتوا می‌گیرد برای محتوایی که وجود ندارد.
        if (showIncome) AppCard(label = "تحلیل درآمد") {
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
                                        "${maskIfPrivate(masked, amountToman(income.amount))} تومان",
                                        color = AppMuted,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(end = 6.dp),
                                    )
                                }
                                IconButton(onClick = { incomePendingDelete = income }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "حذف منبع درآمد", tint = AppDanger)
                                }
                            }
                        }
                    }
                    incomePendingDelete?.let { income ->
                        ConfirmDeleteDialog(
                            title = "حذف منبع درآمد",
                            text = "منبعِ درآمدِ «${income.label}» حذف بشه؟",
                            onConfirm = { onDeleteIncome(income) },
                            onDismiss = { incomePendingDelete = null },
                        )
                    }
                    PrivacyCrossfade(privacyMode) { masked ->
                        Text(
                            "جمع درآمد: ${maskIfPrivate(masked, amountToman(totalIncome))} تومان",
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
                        label = { Text("مبلغ ماهانه (تومان)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    // ورودی تومان است، پس معادلِ حروفی هم مستقیم از همین عدد - تقسیمِ دستیِ
                    // «/ ۱۰» رفت؛ تنها مرجعِ تبدیل tomanToRial/rialToToman است.
                    val incomeToman = amountText.toLongOrNull() ?: 0L
                    if (incomeToman > 0) {
                        AutoShrinkText(
                            text = "${numberToWordsFa(incomeToman.toDouble())} تومان",
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
                                    // ذخیره ریال است، ورودی تومان.
                                    onAddIncome(label.trim(), tomanToRial(amount.toLong()).toDouble(), type)
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

/**
 * یه ردیفِ کارتِ خلاصه‌ی وام - کارتِ `27a`ی طرح: برچسبِ ۱۰٫۵/۸۰۰ با جوهرِ طلایی و زیرش عدد.
 * عددِ ردیفِ اول بزرگ‌تره ([big])، بقیه ۱۳/۹۰۰.
 */
@Composable
private fun LoanSummaryRow(label: String, value: String, valueColor: Color, big: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // برچسب طبقِ فریم `AppGoldInk` با آلفای ۰٫۶۵ه، نه `AppGoldInk2`.
        Text(label, color = AppGoldInk.copy(alpha = 0.65f), fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold)
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 2.dp)) {
            Text(
                value,
                color = valueColor,
                fontSize = if (big) 15.sp else 13.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                " تومان",
                color = AppGoldInk2,
                fontSize = if (big) 10.sp else 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 3.dp, bottom = 1.dp),
            )
        }
    }
}

/**
 * جداکننده‌ی **نقطه‌چینِ** ردیف‌های کارتِ طلایی - `rgba(139,111,61,.32)` طبقِ قاعده‌ی صریحِ
 * گونه‌ی «پول و دستاورد» تو بخشِ ۵ سیستمِ طراحی.
 */
@Composable
private fun LoanSummaryDivider() {
    // ⚠️ توکنِ رنگ `@Composable`ه و داخلِ `drawBehind` صدا زده نمی‌شه - تو `val` محلی خونده می‌شه.
    val ink = AppGoldInk.copy(alpha = 0.32f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp)
            .height(1.dp)
            .drawBehind {
                drawLine(
                    color = ink,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    strokeWidth = size.height,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        floatArrayOf(5f, 5f),
                        0f,
                    ),
                )
            },
    )
}

/**
 * **فیلترِ دوتاییِ «فعال / تسویه‌شده» - فریمِ `27a`.**
 *
 * جایگزینِ دکمه‌ی متنیِ قبلی («وام‌های تسویه‌شده (۳)» / «بازگشت به وام‌های فعال»). طبقِ تصمیمِ
 * کلاد دیزاین (۹ شهریور) این فیلتر **جای خالیِ هدر** رو پر می‌کنه - همون جایی که قبلاً قرار بود
 * دکمه‌ی + بشینه ولی حذف شد تا الگوی «افزودن همیشه با FAB» نشکنه.
 *
 * پیش‌فرض «فعال»ه. تعدادِ تسویه‌شده‌ها رو خودِ چیپ نشون می‌ده تا اگه صفر بود کاربر بیخود
 * روش نزنه.
 */
@Composable
private fun SettledLoansToggle(
    filter: LoanFilter,
    allCount: Int,
    settledCount: Int,
    activeCount: Int,
    overdueLoanCount: Int,
    onFilter: (LoanFilter) -> Unit,
) {
    val shape = RoundedCornerShape(999.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(AppSurface2)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        FilterChipHalf(
            label = "همه (${toFa(allCount)})",
            selected = filter == LoanFilter.ALL,
        ) { onFilter(LoanFilter.ALL) }
        FilterChipHalf(
            // بندِ ۳ جوابِ دورِ ۱۲: نوارِ «۹ وام عقب‌افتاده» **رفت** و عددش این‌جا نشست -
            // صفر پیکسلِ ارتفاعِ تازه، چون این ردیف از قبل بود.
            label = buildString {
                append("فعال (${toFa(activeCount)})")
                if (overdueLoanCount > 0) append(" · ${toFa(overdueLoanCount)} عقب")
            },
            selected = filter == LoanFilter.ACTIVE,
        ) { onFilter(LoanFilter.ACTIVE) }
        FilterChipHalf(
            label = "تسویه‌شده (${toFa(settledCount)})",
            selected = filter == LoanFilter.SETTLED,
        ) { onFilter(LoanFilter.SETTLED) }
    }
}

/** سه‌حالتِ فیلترِ فهرستِ وام - طرحِ مرجعِ کاربر. */
private enum class LoanFilter { ALL, ACTIVE, SETTLED }

/** یه نیمه‌ی فیلترِ دوتایی - انتخاب‌شده قرصِ سفیدِ سایه‌دار می‌گیره، بقیه فقط متنِ خاکستری. */
@Composable
private fun FilterChipHalf(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .then(if (selected) Modifier.background(AppPrimary) else Modifier)
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (selected) Color.White else AppMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
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
