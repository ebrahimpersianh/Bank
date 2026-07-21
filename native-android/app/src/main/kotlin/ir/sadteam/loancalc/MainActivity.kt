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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.onboarding.AnimatedAppEntrance
import ir.sadteam.loancalc.ui.onboarding.BenefitsScreen
import ir.sadteam.loancalc.ui.onboarding.PermissionGateScreen
import ir.sadteam.loancalc.ui.onboarding.PersonalizationScreen
import ir.sadteam.loancalc.ui.onboarding.SplashIntroScreen
import ir.sadteam.loancalc.ui.onboarding.TourScreen
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
            LoanCalcTheme(themeMode = themeMode) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Rtl,
                    LocalSubscriptionManager provides subscriptionManager,
                    // پورت .app.fs-small/fs-medium/fs-large (CSS zoom) تو www/index.html - هم
                    // فونت هم فاصله‌ها (dp) با هم مقیاس می‌شن، دقیقاً مثل زوم کل کانتینر .app.
                    LocalDensity provides Density(
                        density = baseDensity.density * fontScale,
                        fontScale = baseDensity.fontScale * fontScale,
                    ),
                ) {
                    AppRoot()
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

    // «الان دنبالِ چی هستی؟» - فقط یه‌بار، بینِ BenefitsScreen و گیتِ ورود/مهمان. جوابش (کدوم تب)
    // فقط تو همین متغیرِ محلی نگه داشته می‌شه (نه DataStore) و مستقیم به LoanCalcApp پاس داده
    // می‌شه تا اولین باری که کاربر به صفحه‌ی اصلی می‌رسه همون تب باز باشه - رجوع کن به
    // PersonalizationScreen.kt.
    var preferredTabRoute by remember { mutableStateOf<String?>(null) }
    val personalizationSeen by authViewModel.personalizationSeen.collectAsState()
    if (personalizationSeen != true) {
        if (personalizationSeen == false) {
            PersonalizationScreen(
                onSelect = { choice ->
                    preferredTabRoute = choice.route
                    authViewModel.markPersonalizationSeen()
                },
                onSkip = { authViewModel.markPersonalizationSeen() },
            )
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
                // تورِ راهنمای اولین ورود - فقط یه‌بار تو کل عمر نصب (رجوع کن به TourScreen.kt).
                val tourSeen by authViewModel.tourSeen.collectAsState()
                when (tourSeen) {
                    null -> Surface(modifier = Modifier.fillMaxSize(), color = AppSurface) {}
                    false -> TourScreen(onDone = { authViewModel.markTourSeen() })
                    true -> AnimatedAppEntrance { LoanCalcApp(initialRoute = preferredTabRoute) }
                }
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
    initialRoute: String? = null,
    themeViewModel: ThemeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    privacyModeViewModel: PrivacyModeViewModel = hiltViewModel(),
    appUpdateViewModel: AppUpdateViewModel = hiltViewModel(),
) {
    var showSettings by remember { mutableStateOf(false) }
    val updateUrl by appUpdateViewModel.updateUrl.collectAsState()

    val themeMode by themeViewModel.themeMode.collectAsState()
    val privacyMode by privacyModeViewModel.enabled.collectAsState()
    val buzz = rememberBuzz()

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
                        IconButton(onClick = { buzz(); themeViewModel.cycleThemeMode() }) {
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
                        IconButton(onClick = { buzz(); privacyModeViewModel.toggle() }) {
                            Icon(
                                if (privacyMode) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = "حالت خصوصی",
                            )
                        }
                        IconButton(onClick = { showSettings = true }) {
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
                // اگه کاربر تو PersonalizationScreen یه تبِ خاص رو انتخاب کرده بود (خواسته‌ی «الان
                // دنبالِ چی هستی؟»)، همون اولین تبی می‌شه که می‌بینه؛ وگرنه همون پیش‌فرضِ قبلی.
                startDestination = initialRoute ?: BottomTab.BANK_LOAN.route,
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
                composable(BottomTab.MY_LOANS.route) { MyLoansScreen() }
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
                SettingsScreen(onBack = { showSettings = false })
            }
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
            .padding(vertical = 6.dp),
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
