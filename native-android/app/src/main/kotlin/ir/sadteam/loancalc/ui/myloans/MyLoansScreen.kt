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
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.ui.stats.StatsScreen
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.auth.GateState
import ir.sadteam.loancalc.ui.auth.LoginScreen
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppCardVariant
import ir.sadteam.loancalc.ui.components.AppFab
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.PaidRing
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.SettledMedal
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.components.rememberIsScrollingUp
import ir.sadteam.loancalc.ui.haptics.rememberBuzz
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.subscription.SubscriptionScreen
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.Motion
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** پورت لیبل «فیلتر» بالا-چپِ لیست وام‌های رقیب (VAMMAN) - فقط مرتب‌سازی محلی لیست، بدون تغییر
 * داده؛ پیش‌فرض «جدیدترین» (همون ترتیب قبلی createdAt نزولی که قبلاً بدون این کنترل هم اعمال می‌شد). */
internal enum class LoanSortOption(val label: String) {
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
internal fun isLoanSettled(loan: LoanEntity) = loan.n > 0 && loan.paidCount >= loan.n
private val jalaliMonthNames = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)
/** «۲۸ شهریور» - سطرِ دومِ ردیفِ وامِ باز، طبقِ فریمِ `27a`. */
internal fun jalaliShortOf(date: PersianDate): String =
    "${toFa(date.d)} ${jalaliMonthNames.getOrElse(date.m - 1) { "" }}"
/** «تیر ۱۴۰۵» - سطرِ دومِ وامِ تسویه‌شده. */
internal fun jalaliMonthYearOf(date: PersianDate): String =
    "${jalaliMonthNames.getOrElse(date.m - 1) { "" }} ${toFa(date.y)}"
private fun isSameJalaliMonth(a: PersianDate, b: PersianDate) = a.y == b.y && a.m == b.m
/** «امروز» / «فردا» / «۳ روزِ دیگر» - حالتِ سررسیدِ نزدیکِ فریم. */
internal fun dueSoonLabel(days: Int): String = when (days) {
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
internal fun Rect.heroOriginIn(container: Rect): TransformOrigin {
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
    /** سربرگِ واحد (۸ مهر): وقتی جزئیاتِ یک وام باز است، سربرگِ «وام»/تب‌ها پنهان می‌شود. */
    onDetailOpenChanged: (Boolean) -> Unit = {},
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
    val openedLoanIdState = remember { mutableStateOf<Long?>(null) }
    var openedLoanId by openedLoanIdState
    // ویرایشِ مشخصاتِ کلیِ یه وام (اسم/بانک/مبلغ/تعدادِ اقساط) - عمداً openedLoanId رو پاک نمی‌کنیم
    // وقتی ویرایش باز می‌شه، فقط اولویتِ مسیریابی رو تو screenKey بالاتر می‌بریم؛ این‌طوری بعدِ
    // ذخیره/انصرافِ ویرایش (editingLoanId = null)، خودکار برمی‌گرده به همون صفحه‌ی جزئیاتِ وام،
    // نه لیست.
    var editingLoanId by remember { mutableStateOf<Long?>(null) }
    val showLoginPromptState = remember { mutableStateOf(false) }
    var showLoginPrompt by showLoginPromptState
    var showStats by remember { mutableStateOf(false) }
    // «کشیدن به پایین برای همگام‌سازی» - رجوع کن به MyLoansViewModel.syncNow برای اینکه
    // چرا این ژست عمداً فقط پوش می‌کنه و داده‌ی محلی رو با سرور جایگزین نمی‌کنه.
    var syncing by remember { mutableStateOf(false) }
    val showSubscriptionScreenState = remember { mutableStateOf(false) }
    var showSubscriptionScreen by showSubscriptionScreenState

    val rawLoans by viewModel.loans.collectAsState()
    // پیش‌فرض «نزدیک‌ترین سررسید» (۱۵ مهر): وامِ عقب‌افتاده/نزدیک اول، تسویه‌شده آخر - فوری‌ترین
    // وام همیشه بالای لیست است. هر وقت خواستی از منوی «فیلتر» عوضش کن.
    val sortOptionState = remember { mutableStateOf(LoanSortOption.NEXT_DUE) }
    var sortOption by sortOptionState
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
    // انتخابِ فیلتر بعد از بستنِ برنامه هم بماند (خواسته‌ی کاربر ۱۳ مهر).
    val filterPrefs = androidx.compose.ui.platform.LocalContext.current.getSharedPreferences("loan_list", android.content.Context.MODE_PRIVATE)
    var loanFilter by remember {
        mutableStateOf(runCatching { LoanFilter.valueOf(filterPrefs.getString("filter", null) ?: "ACTIVE") }.getOrDefault(LoanFilter.ACTIVE))
    }
    val showSettled = loanFilter == LoanFilter.SETTLED
    var searchQuery by remember { mutableStateOf("") }
    // بستنِ فیلدِ جست‌وجو باید فیلتر را هم بردارد - وگرنه فهرست فیلترشده می‌مانَد و
    // دلیلش دیگر روی صفحه دیده نمی‌شود، یعنی کاربر فکر می‌کند وام‌هایش گم شده‌اند.
    LaunchedEffect(searchOpen) { if (!searchOpen) searchQuery = "" }
    // تاریخِ تسویه = تاریخِ پرداختِ آخرین قسط (ستونِ تازه لازم نیست - همون استدلالی که
    // settledAt رو منتفی کرد). دو کاربرد: سطرِ دومِ ردیفِ تسویه‌شده، و شرطِ «همین ماه».
    val settledDatesState = remember { mutableStateOf(emptyMap<Long, PersianDate>()) }
    var settledDates by settledDatesState
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
    val draggingLoanIdState = remember { mutableStateOf<Long?>(null) }
    var draggingLoanId by draggingLoanIdState
    val dragOffsetYState = remember { mutableStateOf(0f) }
    var dragOffsetY by dragOffsetYState
    val orderedLoansState = remember { mutableStateOf(visibleLoans) }
    var orderedLoans by orderedLoansState
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
    val detailOpen = openedLoan != null
    LaunchedEffect(detailOpen) { onDetailOpenChanged(detailOpen) }
    androidx.compose.runtime.DisposableEffect(Unit) { onDispose { onDetailOpenChanged(false) } }
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
            // ۱۰ مهر: «افزودنِ وام» = فرمِ «قسط و سود» (نتیجه‌اش دکمه‌ی «ذخیره وام» دارد).
            canSaveAnotherLoan -> onOpenCalculator()
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
    val heroOriginState = remember { mutableStateOf(TransformOrigin.Center) }
    var heroOrigin by heroOriginState
    val listBoundsState = remember { mutableStateOf(Rect.Zero) }
    var listBounds by listBoundsState

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
                    // حالتِ خالی: کارتِ «۰ تومان / ۰٪» بی‌معناست (بازطراحیِ ۷ مهر) - فقط وقتی وامی هست.
                    if (loans.isNotEmpty()) item {
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
                            ir.sadteam.loancalc.ui.components.PillSearchField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = "جستجو تو وام‌ها (اسم/بانک)...",
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
                                        onFilter = { loanFilter = it; filterPrefs.edit().putString("filter", it.name).apply() },
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
                                // بازطراحیِ ChatGPT (۷ مهر، دورِ دوم).
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    ir.sadteam.loancalc.ui.components.EmptyHeroCard(
                                        illustration = ir.sadteam.loancalc.R.drawable.empty_illu_loan,
                                        tint = ir.sadteam.loancalc.ui.theme.AppInfoPill.copy(alpha = 0.6f),
                                        title = "هنوز وامی ثبت نکردی",
                                        description = "وام‌هات رو اینجا نگه دار تا قسط‌ها، سررسیدها و پرداخت‌ها رو راحت مدیریت کنی.",
                                        action = "افزودنِ وام",
                                        onAction = { onAddLoanClick() },
                                    )
                                    ir.sadteam.loancalc.ui.components.EmptyFeatureList(
                                        "با ثبتِ وام چه چیزهایی می‌بینی؟",
                                        listOf(
                                            ir.sadteam.loancalc.R.drawable.empty_icon_loan_installment to "مبلغ و اقساط",
                                            ir.sadteam.loancalc.R.drawable.empty_icon_loan_reminder to "سررسید و یادآوری",
                                            ir.sadteam.loancalc.R.drawable.empty_icon_loan_status to "وضعیتِ پرداخت و باقی‌مانده",
                                            ir.sadteam.loancalc.R.drawable.empty_icon_loan_progress to "نمودارِ پیشرفتِ بازپرداخت",
                                        ),
                                    )
                                }
                            }
                        }
                    } else {
                        items(orderedLoans, key = { it.id }) { loan ->
                            MyLoanListItem(
                                loan = loan,
                                viewModel = viewModel,
                                loanCardHeights = loanCardHeights,
                                buzz = buzz,
                                gateState = gateState,
                                isLoanLocked = ::isLoanLocked,
                                openedLoanIdState = openedLoanIdState,
                                showLoginPromptState = showLoginPromptState,
                                showSubscriptionScreenState = showSubscriptionScreenState,
                                sortOptionState = sortOptionState,
                                settledDatesState = settledDatesState,
                                draggingLoanIdState = draggingLoanIdState,
                                dragOffsetYState = dragOffsetYState,
                                orderedLoansState = orderedLoansState,
                                heroOriginState = heroOriginState,
                                listBoundsState = listBoundsState,
                            )
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
            // FABِ ماشین‌حساب رفت (طرحِ ChatGPT): محاسبه‌گر در تبِ بالا هست. قاب می‌ماند
            // چون موقعیتش برای تورِ اپ گزارش می‌شود.
            // ۱۰ مهر (خواسته‌ی کاربر): «+»ِ افزودنِ وام، هم‌شکلِ «+»ِ خانه. با فهرستِ خالی
            // دکمه‌ی «افزودنِ وام»ِ حالتِ خالی هست، پس آن‌جا نمی‌آید.
            visible = screenKey == "list" && loans.isNotEmpty(),
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(150)),
            exit = scaleOut(tween(120)) + fadeOut(tween(120)),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp),
        ) {
            AppFab(
                // مقصدش عوض شد: محاسبه‌گر، نه فرمِ دستی (خواسته‌ی صریحِ کاربر، ۲۶ شهریور).
                onClick = { onAddLoanClick() },
                contentDescription = "افزودنِ وام",
                modifier = Modifier.onGloballyPositioned {
                    onManualAddFabPositioned(it.boundsInRoot())
                },
            )
        }

        InAppBannerHost(banner, modifier = Modifier.align(Alignment.BottomCenter))
    }
}
/** سه‌حالتِ فیلترِ فهرستِ وام - طرحِ مرجعِ کاربر. */
internal enum class LoanFilter { ALL, ACTIVE, SETTLED }
