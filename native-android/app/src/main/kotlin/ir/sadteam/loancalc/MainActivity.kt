package ir.sadteam.loancalc

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import ir.sadteam.loancalc.ui.AffordScreen
import ir.sadteam.loancalc.ui.BankLoanOutcome
import ir.sadteam.loancalc.ui.BankLoanScreen
import ir.sadteam.loancalc.ui.DepositScreen
import ir.sadteam.loancalc.ui.ResultScreen
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.auth.GateState
import ir.sadteam.loancalc.ui.auth.LoginScreen
import ir.sadteam.loancalc.ui.components.AuroraBackground
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.onboarding.AnimatedAppEntrance
import ir.sadteam.loancalc.ui.onboarding.BenefitsScreen
import ir.sadteam.loancalc.ui.onboarding.PermissionGateScreen
import ir.sadteam.loancalc.ui.onboarding.SplashIntroScreen
import ir.sadteam.loancalc.ui.onboarding.WelcomeMessageScreen
import ir.sadteam.loancalc.ui.update.AppUpdateViewModel
import ir.sadteam.loancalc.ui.security.AppLockViewModel
import ir.sadteam.loancalc.ui.security.LockScreen
import ir.sadteam.loancalc.subscription.LocalSubscriptionManager
import ir.sadteam.loancalc.subscription.SubscriptionManager
import ir.sadteam.loancalc.ui.myloans.MyLoansScreen
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyModeViewModel
import ir.sadteam.loancalc.ui.settings.SettingsScreen
import ir.sadteam.loancalc.ui.haptics.rememberBuzz
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.LocalThemeReveal
import ir.sadteam.loancalc.ui.theme.ThemeRevealHost
import ir.sadteam.loancalc.ui.theme.ThemeRevealState
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.LoanCalcTheme
import ir.sadteam.loancalc.ui.theme.ThemeMode
import ir.sadteam.loancalc.ui.theme.ThemeViewModel
import kotlinx.coroutines.delay

// آیکون‌های نوار پایین: حالت عادی outline (مینیمال، مثل نسخه‌ی وب)، تب فعال پُر (filled). آیکونِ
// «سود سپرده» قبلاً Savings بود که رو گوشی شکل یه خوکِ قلک درمیاد (گزارش کاربر) - با TrendingUp
// (رشد/سود، مینیمال‌تر) عوض شد.
private enum class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    BANK_LOAN("bank_loan", "وام بانکی", Icons.Outlined.Payments, Icons.Filled.Payments),
    AFFORD("afford", "محاسبه‌گر", Icons.Outlined.RequestQuote, Icons.Filled.RequestQuote),
    DEPOSIT("deposit", "سود سپرده", Icons.Outlined.TrendingUp, Icons.Filled.TrendingUp),
    MY_LOANS("my_loans", "وام‌های من", Icons.Outlined.FolderOpen, Icons.Filled.Folder),
}

// ترتیب/محتوای کاملِ تورِ راهنمای اولین ورود (AppTourOverlay) - رجوع کن به همون کامپوننت پایین‌تر
// برای جزئیاتِ فنیِ اسپاتلایت. هر مرحله یه المانِ *واقعیِ* رو صفحه رو هدف می‌گیره (مختصاتش تو
// tourBounds تو LoanCalcApp اندازه‌گیری می‌شه) - نه یه توضیحِ مستقلِ بدونِ هدف.
private enum class TourTarget(val title: String, val hint: String) {
    PRIVACY(
        "حالت خصوصی",
        "با این آیکون همه‌ی مبلغ‌های صفحه رو پشتِ ••• مخفی کن - برای وقتی گوشیتو دستِ کسی می‌دی.",
    ),
    DARK_MODE(
        "تمِ روشن/تاریک",
        "با این آیکون بینِ تمِ روشن و تاریک جابه‌جا شو.",
    ),
    SETTINGS(
        "تنظیمات",
        "از اینجا به یادآوریِ سررسید، پشتیبان‌گیری، آمار و گزارشات، امنیت و حساب‌های بانکی هم " +
            "دسترسی داری.",
    ),
    // این دو قدم، برخلافِ بقیه، پنلِ تنظیمات رو خودکار باز می‌کنن و با فیلترِ جستجوی خودِ همون پنل
    // (matches() تو SettingsScreen.kt) دقیقاً ردیفِ خودشون رو نشون می‌دن - رجوع کن به
    // tourHighlightQuery تو SettingsScreen.kt.
    SETTINGS_CALENDAR(
        "تقویم مالی",
        "همینجا تو تنظیماته - سررسیدِ اقساطِ همه‌ی وام‌هات رو رو یه تقویمِ جلالی یه‌جا ببین.",
    ),
    SETTINGS_CHEQUE(
        "امور چک",
        "این‌م همینجا تو تنظیماته - چک‌های دریافتی/پرداختی و دسته‌چک‌هات رو مدیریت کن.",
    ),
    BANK_LOAN(
        "وام بانکی",
        "قسطِ وام‌های بانکی و قرض‌الحسنه رو اینجا دقیق محاسبه کن.",
    ),
    AFFORD(
        "محاسبه‌گر",
        "با پرداختِ ماهانه‌ای که مقدوره، ببین چقدر وام می‌تونی بگیری.",
    ),
    DEPOSIT(
        "سود سپرده",
        "قبل از سپرده‌گذاری، سودِ نهایی رو از قبل حساب کن.",
    ),
    MY_LOANS(
        "وام‌های من",
        "وام‌ها و چک‌هات رو یه‌جا ذخیره کن و وضعیتِ هر قسط رو پیگیری کن.",
    ),
    MANUAL_ADD(
        "افزودن دستی وام",
        "با این دکمه یه وام رو دستی (بدونِ محاسبه) اضافه کن و اقساطش رو خودت پیگیری کن - آخرین قدمِ تور!",
    ),
}

// نگاشتِ TourTarget های تبی → BottomTab واقعی (برای این‌که AppTourOverlay بدونه با کدوم تب باید
// هماهنگ بشه - هم گرفتنِ مختصات از BottomNavItem هم ناوبریِ خودکار موقعِ قدمِ MANUAL_ADD).
private fun TourTarget.asBottomTab(): BottomTab? = when (this) {
    TourTarget.BANK_LOAN -> BottomTab.BANK_LOAN
    TourTarget.AFFORD -> BottomTab.AFFORD
    TourTarget.DEPOSIT -> BottomTab.DEPOSIT
    TourTarget.MY_LOANS, TourTarget.MANUAL_ADD -> BottomTab.MY_LOANS
    else -> null
}

private fun BottomTab.asTourTarget(): TourTarget = when (this) {
    BottomTab.BANK_LOAN -> TourTarget.BANK_LOAN
    BottomTab.AFFORD -> TourTarget.AFFORD
    BottomTab.DEPOSIT -> TourTarget.DEPOSIT
    BottomTab.MY_LOANS -> TourTarget.MY_LOANS
}

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    // پورت مو‌به‌موی الگوی نمونه‌ی رسمی Poolakey: connect تو onCreate، disconnect تو onDestroy.
    // خودِ SubscriptionManager به Activity نیاز داره (activityResultRegistry)، برای همین
    // Hilt-managed نیست و اینجا مستقیم ساخته می‌شه.
    private lateinit var subscriptionManager: SubscriptionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        subscriptionManager = SubscriptionManager(this)
        subscriptionManager.connect { }
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()
            val fontScale by themeViewModel.fontScale.collectAsState()
            val baseDensity = LocalDensity.current
            // عمداً بیرونِ LoanCalcTheme: این state باید از تعویضِ خودِ تم جونِ سالم به‌در ببره،
            // چون دقیقاً وسطِ همون تعویض داره کار می‌کنه (رجوع کن به ThemeReveal.kt).
            val themeReveal = remember { ThemeRevealState() }
            LoanCalcTheme(themeMode = themeMode) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Rtl,
                    LocalSubscriptionManager provides subscriptionManager,
                    LocalThemeReveal provides themeReveal,
                    // پورت .app.fs-small/fs-medium/fs-large (CSS zoom) تو www/index.html - هم
                    // فونت هم فاصله‌ها (dp) با هم مقیاس می‌شن، دقیقاً مثل زوم کل کانتینر .app.
                    LocalDensity provides Density(
                        density = baseDensity.density * fontScale,
                        fontScale = baseDensity.fontScale * fontScale,
                    ),
                ) {
                    ThemeRevealHost(state = themeReveal, revealKey = themeMode) {
                        AppRoot()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        subscriptionManager.disconnect()
        super.onDestroy()
    }

    // فلیورِ myket برخلافِ cafebazaar (که با ActivityResultRegistryِ مدرن کار می‌کنه) هنوز الگوی
    // کلاسیکِ IAB v3 رو داره، پس نتیجه‌ی خریدش از همین onActivityResultِ خام برمی‌گرده - رجوع کن به
    // SubscriptionManager.handleActivityResult (تو فلیورِ cafebazaar یه no-op بی‌ضرره).
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        subscriptionManager.handleActivityResult(requestCode, resultCode, data)
    }
}

/**
 * پورت checkLoginGateOnStart تو www/index.html: اولین بار که اپ باز می‌شه (و هنوز نه لاگین کرده نه
 * «مهمان» رو انتخاب کرده) LoginScreen اجباریه؛ گیت هیچ‌وقت دوباره نشون داده نمی‌شه (نه بعد از ورود،
 * نه بعد از انتخاب مهمان). تا اولین مقدار واقعی از DataStore برسه (gateState == null) چیزی نشون
 * نمی‌دیم که یه فلش اشتباهی صفحه‌ی ورود قبل از لاگین واقعی دیده نشه.
 *
 * قبل از این گیت هم، [PermissionGateScreen] چک می‌شه - برخلاف گیت ورود، این یکی هر بار اپ باز
 * می‌شه دوباره ارزیابی می‌شه (نه فقط یه‌بار)، چون کاربر می‌تونه مجوزها رو از تنظیمات گوشی خاموش کنه.
 * بعد از گیت مجوز و قبل از گیت ورود، [BenefitsScreen] هم فقط یه‌بار تو کل عمر نصب نشون داده می‌شه.
 * بعد از حل شدن گیت ورود/مهمان، هر بار [WelcomeMessageScreen] (پیام خوش‌آمد شبیه چت‌بات) نشون داده
 * می‌شه، بعد صفحه‌ی اصلی با یه افکت swoosh سریع (`AnimatedAppEntrance`) از بالا-چپ میاد تو.
 *
 * قبل از همه‌ی این‌ها هم [LockScreen] چک می‌شه، فقط اگه کاربر از تنظیمات قفل PIN/اثر انگشت رو فعال
 * کرده باشه (پیش‌فرض خاموشه، هیچ‌کس رفتار قبلی رو نمی‌بینه) - رجوع کن به [AppLockViewModel].
 */
@Composable
private fun AppRoot(
    authViewModel: AuthViewModel = hiltViewModel(),
    appLockViewModel: AppLockViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
) {
    // اینتروِ دوناتی (پورت اسپلشِ اپ وب) - یه‌بار در هر بار باز شدن اپ، قبل از همه‌چیز. رنگِ زمینه‌ش
    // با دارک/لایت‌مودِ فعلی هماهنگه (خواسته‌ی کاربر) - برای همین themeMode هم اینجا لازمه.
    var introDone by remember { mutableStateOf(false) }
    if (!introDone) {
        val themeMode by themeViewModel.themeMode.collectAsState()
        SplashIntroScreen(isDarkTheme = themeMode == ThemeMode.DARK, onDone = { introDone = true })
        return
    }

    val pinHash by appLockViewModel.pinHash.collectAsState()
    val biometricEnabled by appLockViewModel.biometricEnabled.collectAsState()
    val unlocked by appLockViewModel.unlocked.collectAsState()
    val securityEnabled = pinHash != null || biometricEnabled
    if (securityEnabled && !unlocked) {
        LockScreen(
            pinHash = pinHash,
            biometricEnabled = biometricEnabled,
            attemptPin = { appLockViewModel.attemptPin(it) },
            onUnlock = { appLockViewModel.unlock() },
        )
        return
    }

    var permissionsOk by remember { mutableStateOf(false) }
    if (!permissionsOk) {
        PermissionGateScreen(onAllGranted = { permissionsOk = true })
        return
    }

    val benefitsSeen by authViewModel.benefitsSeen.collectAsState()
    if (benefitsSeen != true) {
        if (benefitsSeen == false) {
            BenefitsScreen(onContinue = { authViewModel.markBenefitsSeen() })
        } else {
            Surface(modifier = Modifier.fillMaxSize(), color = AppSurface) {}
        }
        return
    }

    val gateState by authViewModel.gateState.collectAsState()
    when (gateState) {
        null -> Surface(modifier = Modifier.fillMaxSize(), color = AppSurface) {}
        GateState.NEEDS_LOGIN -> LoginScreen()
        GateState.GUEST, GateState.LOGGED_IN -> {
            var welcomeDone by remember { mutableStateOf(false) }
            if (!welcomeDone) {
                val phone by authViewModel.phone.collectAsState()
                val name = if (gateState == GateState.LOGGED_IN && !phone.isNullOrEmpty()) {
                    toFa(phone!!)
                } else {
                    "مهمان"
                }
                WelcomeMessageScreen(name = name, onDone = { welcomeDone = true })
            } else {
                // تورِ راهنمای اولین ورود دیگه یه گیتِ جداگانه‌ی قبل از ورود نیست - کاربر خواستِ
                // «تو خود برنامه بگه کجا بری»، پس حالا یه اورلیِ spotlight داخلِ خودِ LoanCalcApp
                // (رو المان‌های واقعیِ چیدمان) نشون داده می‌شه - رجوع کن به AppTourOverlay اونجا.
                AnimatedAppEntrance { LoanCalcApp() }
            }
        }
    }
}

/**
 * پورت پنل تنظیمات اپ رقیب (VAMMAN): به‌جای این‌که کلاً صفحه‌ی فعلی رو با یه صفحه‌ی جدا جایگزین کنه
 * (return زودهنگام قبلی)، حالا یه overlay روی همون صفحه‌ست - یه scrim نیمه‌شفاف (تپ روش می‌بنده) +
 * پنل با عرض ۸۵٪ صفحه که از سمت گیره‌ی تنظیمات (سمت «End»، تو RTL همون چپ) با اسلاید سریع میاد تو،
 * طوری که یه لبه‌ی نازک از صفحه‌ی زیرش (سمت راست) همیشه دیده بمونه.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun LoanCalcApp(
    themeViewModel: ThemeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    privacyModeViewModel: PrivacyModeViewModel = hiltViewModel(),
    appUpdateViewModel: AppUpdateViewModel = hiltViewModel(),
) {
    var showSettings by remember { mutableStateOf(false) }
    // قدمِ فعلیِ تور (اگه تور در حالِ اجراست) - AppTourOverlay.onStepChanged پرش می‌کنه؛ برای
    // قدم‌های SETTINGS_CALENDAR/SETTINGS_CHEQUE لازمه بدونیم کدوم ردیفِ پنلِ تنظیمات رو باید
    // فیلتر/اسپاتلایت کنیم - رجوع کن به SettingsScreen(tourHighlightQuery = ...) پایین‌تر.
    var currentTourTarget by remember { mutableStateOf<TourTarget?>(null) }
    val updateUrl by appUpdateViewModel.updateUrl.collectAsState()

    val themeMode by themeViewModel.themeMode.collectAsState()
    val privacyMode by privacyModeViewModel.enabled.collectAsState()
    val buzz = rememberBuzz()
    // افکتِ دایره‌ایِ تعویضِ تم (سبکِ تلگرام) - خودِ اورلی تو MainActivity.setContent نصب شده،
    // اینجا فقط ماشه‌ش کشیده می‌شه. رجوع کن به ThemeReveal.kt.
    val themeReveal = LocalThemeReveal.current

    // وضعیت اشتراک/دوره‌ی آزمایشی رو هر بار اپ باز می‌شه از سرور تازه می‌کنیم (نه فقط لحظه‌ی ورود) -
    // وگرنه اگه اپ لاگین‌شده بمونه، دقیقاً روزی که دوره‌ی آزمایشی تموم می‌شه هیچ‌وقت خودش رو به‌روز
    // نمی‌کرد. اجرای واقعیِ محدودیت (۱ وام رایگان) همیشه سمت سرور (routes/loans.js) دفاعی چک می‌شه؛
    // این فقط UI رو هم‌زمان با واقعیت نگه می‌داره.
    LaunchedEffect(Unit) { authViewModel.refreshStatus() }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: BottomTab.BANK_LOAN.route

    // تپ دوباره رو تب «وام بانکی» وقتی از قبل انتخابه باید فرم رو ریست کنه (دقیقاً رفتار قبلی،
    // قبل از معرفی Navigation) — چون launchSingleTop جلوی navigate دوباره به همون مقصد رو می‌گیره،
    // این ریست از طریق یه کلید جدا اعمال می‌شه.
    var bankLoanResetKey by remember { mutableIntStateOf(0) }

    // مختصاتِ واقعیِ هر هدفِ تور رو صفحه (چهارتا تبِ نوارِ پایین + سه‌تا آیکونِ TopAppBar + دکمه‌ی
    // افزودنِ دستیِ MyLoansScreen) - برای اینکه AppTourOverlay بتونه دقیقاً دورِ المانِ واقعی یه
    // سوراخِ نورانی بکشه، نه یه مختصاتِ حدسی. رجوع کن به onGloballyPositioned رو هر کدوم پایین‌تر.
    val tourBounds = remember { mutableStateMapOf<TourTarget, Rect>() }

    // پورت رفتار «یه‌بار برگشت بزنی هشدار بده، دوباره بزنی خارج شو» - فقط رو تب پیش‌فرض (وام بانکی)
    // فعاله، چون تو بقیه‌ی تب‌ها/تنظیمات دکمه‌ی برگشت باید همون رفتار عادیش (برگشت به تب قبلی/بستن
    // تنظیمات) رو داشته باشه.
    val context = LocalContext.current
    var lastBackPressAt by remember { mutableStateOf(0L) }
    // هینت خروج به‌صورت overlay داخلِ اپ نشون داده می‌شه، نه Toast سیستمی - چون بعضی رام‌ها
    // (مثل MIUI) کنار هر Toast آیکون لانچرِ اپ رو می‌چسبونن، که کاربر خواست حذف بشه.
    var showExitHint by remember { mutableStateOf(false) }
    LaunchedEffect(showExitHint) {
        if (showExitHint) {
            delay(2000)
            showExitHint = false
        }
    }
    BackHandler(enabled = currentRoute == BottomTab.BANK_LOAN.route) {
        val now = System.currentTimeMillis()
        if (now - lastBackPressAt < 2000) {
            (context as? Activity)?.finish()
        } else {
            lastBackPressAt = now
            showExitHint = true
        }
    }

    CompositionLocalProvider(LocalPrivacyMode provides privacyMode) {
    Box(modifier = Modifier.fillMaxSize()) {
        // پس‌زمینه‌ی زنده‌ی شفق (سبزآبی+طلایی، رجوع کن به AuroraBackground) پشتِ کل تب‌ها -
        // containerColor خودِ Scaffold و TopAppBar عمداً Transparent شدن تا این دیده بشه؛ هیچ‌کدوم
        // از ۴ تبِ اصلی پس‌زمینه‌ی مات ندارن (فقط کارت‌هاشون Surface دارن) پس بینِ کارت‌ها پیداست.
        AuroraBackground(modifier = Modifier.fillMaxSize())
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = { Text("وام من") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                buzz()
                                // ترتیب مهمه: اول اسنپ‌شاتِ تمِ فعلی، بعد عوض‌کردنِ تم - رجوع کن
                                // به ThemeReveal.kt. دایره از مرکزِ خودِ همین دکمه باز می‌شه، برای
                                // همین از همون مستطیلی که پایین برای تور ثبت می‌شه استفاده می‌کنیم.
                                if (!themeReveal.inProgress) {
                                    themeReveal.startReveal(
                                        origin = tourBounds[TourTarget.DARK_MODE]?.center ?: Offset.Zero,
                                        currentKey = themeMode,
                                    )
                                    themeViewModel.cycleThemeMode()
                                }
                            },
                            // مختصاتِ واقعیِ این آیکون رو گزارش می‌ده - برای قدمِ TourTarget.DARK_MODE
                            // تو AppTourOverlay، رجوع کن به onPositioned مشابه رو BottomNavItem.
                            modifier = Modifier.onGloballyPositioned {
                                tourBounds[TourTarget.DARK_MODE] = it.boundsInRoot()
                            },
                        ) {
                            // پورت sunIcon/moonIcon تو www/index.html: آیکون وضعیت *فعلی* رو نشون
                            // می‌ده، نه نتیجه‌ی تپ‌کردن. تمِ تاریک الان برای همه رایگانه.
                            Icon(
                                if (themeMode == ThemeMode.DARK) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                                contentDescription = "تغییر تم",
                            )
                        }
                    },
                    actions = {
                        // حالت خصوصی: مخفی‌کردن سریع همه‌ی مبلغ‌های صفحه پشت «•••» (برای وقتی
                        // گوشیتو دستِ کسی می‌دی)، بدون نیاز به رفتن تو تنظیمات.
                        IconButton(
                            onClick = { buzz(); privacyModeViewModel.toggle() },
                            modifier = Modifier.onGloballyPositioned {
                                tourBounds[TourTarget.PRIVACY] = it.boundsInRoot()
                            },
                        ) {
                            Icon(
                                if (privacyMode) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = "حالت خصوصی",
                            )
                        }
                        IconButton(
                            onClick = { showSettings = true },
                            modifier = Modifier.onGloballyPositioned {
                                tourBounds[TourTarget.SETTINGS] = it.boundsInRoot()
                            },
                        ) {
                            Icon(Icons.Filled.Settings, contentDescription = "تنظیمات")
                        }
                    },
                )
            },
            bottomBar = {
                Surface(color = AppSurface) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BottomTab.entries.forEach { tab ->
                            BottomNavItem(
                                tab = tab,
                                selected = currentRoute == tab.route,
                                onPositioned = { rect -> tourBounds[tab.asTourTarget()] = rect },
                                onClick = {
                                    if (tab.route == currentRoute && tab == BottomTab.BANK_LOAN) {
                                        bankLoanResetKey++
                                    } else {
                                        navController.navigate(tab.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = BottomTab.BANK_LOAN.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                // پورت کاملِ اسلاید جهت‌دار بین ۴ تب اصلی وب (switchTab: slide-l/slide-r): جهت از
                // رو فاصله‌ی ایندکس تب قبلی/جدید تو ترتیب تب‌ها حساب می‌شه و صفحه‌ی جدید با یه
                // اسلاید فنری از همون سمتِ حرکت میاد تو - حس «پریمیوم»تر از fade+scale قبلی.
                enterTransition = {
                    val dir = slideDirection(initialState.destination.route, targetState.destination.route)
                    slideInHorizontally(
                        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
                    ) { dir * it / 3 } + fadeIn(tween(220))
                },
                exitTransition = {
                    val dir = slideDirection(initialState.destination.route, targetState.destination.route)
                    slideOutHorizontally(animationSpec = tween(180)) { -dir * it / 4 } + fadeOut(tween(150))
                },
                popEnterTransition = {
                    val dir = slideDirection(initialState.destination.route, targetState.destination.route)
                    slideInHorizontally(
                        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
                    ) { dir * it / 3 } + fadeIn(tween(220))
                },
                popExitTransition = {
                    val dir = slideDirection(initialState.destination.route, targetState.destination.route)
                    slideOutHorizontally(animationSpec = tween(180)) { -dir * it / 4 } + fadeOut(tween(150))
                },
            ) {
                composable(BottomTab.BANK_LOAN.route) {
                    key(bankLoanResetKey) { BankLoanTab() }
                }
                composable(BottomTab.AFFORD.route) { AffordScreen() }
                composable(BottomTab.DEPOSIT.route) { DepositScreen() }
                composable(BottomTab.MY_LOANS.route) {
                    MyLoansScreen(
                        onManualAddFabPositioned = { rect -> tourBounds[TourTarget.MANUAL_ADD] = rect },
                    )
                }
            }
        }
    
        AnimatedVisibility(
            visible = showSettings,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { showSettings = false },
            )
        }
    
        AnimatedVisibility(
            visible = showSettings,
            enter = slideInHorizontally(animationSpec = tween(250)) { -it } + fadeIn(tween(250)),
            exit = slideOutHorizontally(animationSpec = tween(200)) { -it } + fadeOut(tween(200)),
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.85f)
                .align(Alignment.CenterEnd),
        ) {
            Surface(color = AppSurface, modifier = Modifier.fillMaxSize()) {
                SettingsScreen(
                    onBack = { showSettings = false },
                    tourHighlightQuery = when (currentTourTarget) {
                        TourTarget.SETTINGS_CALENDAR -> "تقویم مالی"
                        TourTarget.SETTINGS_CHEQUE -> "امور چک"
                        else -> null
                    },
                    onTourRowPositioned = { rect -> currentTourTarget?.let { tourBounds[it] = rect } },
                )
            }
        }

        // باگِ رفع‌شده: پنلِ تنظیمات فقط ۸۵٪ عرض می‌گیره؛ چون خودِ پنل (Surface رنگِ AppSurface) و
        // لایه‌ی تیره‌ی پشتش (اسکرمِ ۴۵٪ سیاه) هر دو تا زیرِ نوارِ وضعیتِ گوشی هم کشیده می‌شن، این
        // دو رنگِ متفاوت درست زیرِ آیکون‌های نوارِ وضعیت (بینِ آنتن و باتری) به‌هم می‌رسیدن و یه خطِ
        // دیدنی می‌ساختن (گزارشِ کاربر: «بالا بین آنتن‌ها یه خطه»). این نوارِ باریک، فقط به‌ارتفاعِ
        // نوارِ وضعیت و تمام‌عرض، رو همه‌چیزِ دیگه (هم اسکرم هم پنل) می‌شینه تا زیرِ ساعت/آنتن/باتری
        // همیشه یه‌دست/یه‌تیکه بمونه.
        AnimatedVisibility(visible = showSettings, enter = fadeIn(tween(200)), exit = fadeOut(tween(200))) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(AppSurface),
            )
        }

        AnimatedVisibility(
            visible = showExitHint,
            enter = fadeIn(tween(180)) + slideInVertically(tween(180)) { it / 2 },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(180)) { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp),
        ) {
            Surface(
                color = AppText.copy(alpha = 0.92f),
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 6.dp,
            ) {
                Text(
                    "برای خروج، دوباره دکمه‌ی برگشت رو بزن",
                    color = AppSurface,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                )
            }
        }

        // بنرِ آپدیتِ خودکار - رجوع کن به AppUpdateViewModel. برخلافِ هینتِ خروج، خودش محو نمی‌شه؛
        // تا کاربر یا بزنه «بروزرسانی» (بازکردنِ صفحه‌ی استور) یا خودش با ضربدر ببندتش.
        AnimatedVisibility(
            visible = updateUrl != null,
            enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { -it },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp, start = 14.dp, end = 14.dp),
        ) {
            Surface(
                color = AppPrimary,
                shape = RoundedCornerShape(14.dp),
                shadowElevation = 6.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "نسخه‌ی جدیدِ اپ موجوده",
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = {
                        updateUrl?.let { url ->
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                        }
                    }) {
                        Text("بروزرسانی", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    IconButton(onClick = { appUpdateViewModel.dismiss() }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "بستن", tint = Color.White)
                    }
                }
            }
        }

        // تورِ راهنمای اولین ورود - «تو خود برنامه بگه کجا بری» (خواسته‌ی صریح کاربر، به‌جای صفحه‌ی
        // جدای قبلی) - رجوع کن به AppTourOverlay پایین‌تر. آخرین بچه‌ی Box تا رو همه‌چیز دیگه بشینه.
        val tourSeen by authViewModel.tourSeen.collectAsState()
        if (tourSeen == false) {
            AppTourOverlay(
                steps = TourTarget.entries.toList(),
                bounds = tourBounds,
                onStepChanged = { target ->
                    currentTourTarget = target
                    if (target == TourTarget.SETTINGS_CALENDAR || target == TourTarget.SETTINGS_CHEQUE) {
                        // این دو قدم رو خودِ پنلِ تنظیمات زندگی می‌کنن - خودکار بازش کن، وگرنه ردیفِ
                        // هدف اصلاً رندر نمی‌شه.
                        showSettings = true
                    } else {
                        showSettings = false
                        // قدم‌هایی که رو یه تبِ خاص زندگی می‌کنن (مثلاً افزودنِ دستی که فقط رو تبِ
                        // «وام‌های من» وجود داره) خودکار به همون تب می‌رن - وگرنه المانِ هدف اصلاً
                        // رندر/قابل‌اندازه‌گیری نیست.
                        target.asBottomTab()?.let { tab ->
                            if (currentRoute != tab.route) {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                },
                onDone = {
                    showSettings = false
                    authViewModel.markTourSeen()
                },
            )
        }
    }
    }
}

/** جهت اسلاید تعویض تب (پورت محاسبه‌ی جهت switchTab تو www/index.html): تو RTL رفتن به تبِ با
 * ایندکس بالاتر یعنی حرکت به سمت چپ، پس صفحه‌ی جدید از چپ (آفست منفی) میاد تو؛ برگشتن برعکس. */
private fun slideDirection(fromRoute: String?, toRoute: String?): Int {
    val from = BottomTab.entries.indexOfFirst { it.route == fromRoute }
    val to = BottomTab.entries.indexOfFirst { it.route == toRoute }
    return if (to >= from) -1 else 1
}

/**
 * تورِ راهنمای اولین ورود، به‌صورتِ یه اورلیِ spotlight واقعی رو خودِ چیدمانِ اپ - نه یه صفحه‌ی
 * جدای قبل از ورود. یه لایه‌ی تیره‌ی نیمه‌شفاف کلِ صفحه رو می‌پوشونه با یه «سوراخِ» گردگوشه دقیقاً
 * دورِ هدفِ فعلی (از رو [bounds]، مختصاتِ واقعیِ اندازه‌گیری‌شده - نه حدسی)، + یه کارتِ توضیح که
 * همیشه بالای نوارِ تب می‌شینه (موقعیتش ثابته، فقط سوراخ جابه‌جا می‌شه). قدم‌هایی که رو یه تبِ
 * خاص زندگی می‌کنن (رجوع کن به [TourTarget.asBottomTab]) موقعِ رسیدن، [onStepChanged] رو صدا
 * می‌زنن تا LoanCalcApp خودش به همون تب ناوبری کنه - این‌جوری المانِ هدف (مثلاً دکمه‌ی افزودنِ
 * دستی که فقط رو تبِ «وام‌های من» وجود داره) همیشه واقعاً رندر و قابل‌اندازه‌گیریه. لمسِ هرجای
 * دیگه‌ی صفحه (به‌جز دکمه‌های خودِ کارت) قدمِ بعد رو فعال می‌کنه؛ آخرین قدم «متوجه شدم» تور رو تموم
 * می‌کنه.
 */
@Composable
private fun AppTourOverlay(
    steps: List<TourTarget>,
    bounds: Map<TourTarget, Rect>,
    onStepChanged: (TourTarget) -> Unit,
    onDone: () -> Unit,
) {
    var stepIndex by remember { mutableIntStateOf(0) }
    val currentStep = steps.getOrNull(stepIndex) ?: return
    LaunchedEffect(currentStep) { onStepChanged(currentStep) }
    val rect = bounds[currentStep]
    val isLastStep = stepIndex == steps.lastIndex
    val advance: () -> Unit = { if (isLastStep) onDone() else stepIndex++ }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { advance() },
        ) {
            val scrimPath = Path().apply { addRect(Rect(Offset.Zero, size)) }
            if (rect != null) {
                val holePath = Path().apply {
                    addRoundRect(RoundRect(rect.inflate(8f), CornerRadius(18f, 18f)))
                }
                scrimPath.op(scrimPath, holePath, PathOperation.Difference)
            }
            drawPath(scrimPath, color = Color.Black.copy(alpha = 0.72f))
        }

        Surface(
            color = AppSurface,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 110.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(currentStep.title, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    currentStep.hint,
                    color = AppMuted,
                    fontSize = 12.5.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDone) {
                        Text("رد کن", color = AppMuted, fontSize = 12.5.sp)
                    }
                    GradientButton(onClick = advance) {
                        Text(if (isLastStep) "متوجه شدم" else "بعدی")
                    }
                }
            }
        }
    }
}

@Composable
private fun BankLoanTab() {
    var loanOutcome by remember { mutableStateOf<BankLoanOutcome?>(null) }
    // اسلاید جهت‌دار فرم→نتیجه (هم‌خانواده‌ی اسلاید تب‌های پایین): نتیجه از چپ میاد تو و فرم به
    // راست می‌ره؛ برگشت به فرم برعکس - به‌جای fade+scale قبلی.
    AnimatedContent(
        targetState = loanOutcome,
        transitionSpec = {
            val dir = if (targetState != null) -1 else 1
            (
                slideInHorizontally(
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
                ) { dir * it / 3 } + fadeIn(tween(220))
                ).togetherWith(
                    slideOutHorizontally(animationSpec = tween(180)) { -dir * it / 4 } + fadeOut(tween(150)),
                )
        },
        label = "bankLoanTab",
    ) { outcome ->
        if (outcome == null) {
            BankLoanScreen(onCalculated = { loanOutcome = it })
        } else {
            ResultScreen(outcome = outcome)
        }
    }
}

@Composable
private fun RowScope.BottomNavItem(
    tab: BottomTab,
    selected: Boolean,
    onClick: () -> Unit,
    onPositioned: (Rect) -> Unit = {},
) {
    val color = if (selected) AppPrimary else AppMuted
    // پورت easing فنری تب فعال تو وب (cubic-bezier(.34,1.56,.64,1) رو .nav-item .ic svg) - قبلاً
    // آیکون تب انتخاب‌شده هیچ افکتی نداشت، فقط رنگش عوض می‌شد.
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "navIconScale",
    )
    // «قرصِ» پس‌زمینه‌ی تب فعال حالا نرم fade می‌شه (قبلاً یهو ظاهر/محو می‌شد).
    val pillAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(250),
        label = "navPillAlpha",
    )
    val pillColor = AppPrimary
    val buzz = rememberBuzz()
    Column(
        modifier = Modifier
            .weight(1f)
            .background(pillColor.copy(alpha = 0.10f * pillAlpha), RoundedCornerShape(14.dp))
            .clickable(onClick = { buzz(); onClick() })
            .padding(vertical = 6.dp)
            // مختصاتِ ریشه‌ی خودِ تب رو گزارش می‌ده - برای AppTourOverlay که دقیقاً همین محدوده رو
            // نورانی می‌کنه، نه یه مختصاتِ حدسی/هاردکد.
            .onGloballyPositioned { coordinates -> onPositioned(coordinates.boundsInRoot()) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            if (selected) tab.selectedIcon else tab.icon,
            contentDescription = tab.label,
            tint = color,
            modifier = Modifier
                .height(22.dp)
                .graphicsLayer { scaleX = iconScale; scaleY = iconScale },
        )
        Text(
            tab.label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
