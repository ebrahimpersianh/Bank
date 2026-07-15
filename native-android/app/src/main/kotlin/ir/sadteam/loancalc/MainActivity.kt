package ir.sadteam.loancalc

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.onboarding.AnimatedAppEntrance
import ir.sadteam.loancalc.ui.onboarding.BenefitsScreen
import ir.sadteam.loancalc.ui.onboarding.PermissionGateScreen
import ir.sadteam.loancalc.ui.onboarding.WelcomeMessageScreen
import ir.sadteam.loancalc.ui.security.AppLockViewModel
import ir.sadteam.loancalc.ui.security.LockScreen
import ir.sadteam.loancalc.subscription.LocalSubscriptionManager
import ir.sadteam.loancalc.subscription.SubscriptionManager
import ir.sadteam.loancalc.ui.myloans.MyLoansScreen
import ir.sadteam.loancalc.ui.settings.SettingsScreen
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.LoanCalcTheme
import ir.sadteam.loancalc.ui.theme.ThemeViewModel

private enum class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    BANK_LOAN("bank_loan", "وام بانکی", Icons.Filled.AccountBalance),
    AFFORD("afford", "محاسبه‌گر", Icons.Filled.Calculate),
    DEPOSIT("deposit", "سود سپرده", Icons.Filled.Savings),
    MY_LOANS("my_loans", "وام‌های من", Icons.Filled.Folder),
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
            val darkTheme by themeViewModel.darkTheme.collectAsState()
            val fontScale by themeViewModel.fontScale.collectAsState()
            val baseDensity = LocalDensity.current
            LoanCalcTheme(darkTheme = darkTheme) {
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
private fun AppRoot(authViewModel: AuthViewModel = hiltViewModel(), appLockViewModel: AppLockViewModel = hiltViewModel()) {
    val pinHash by appLockViewModel.pinHash.collectAsState()
    val biometricEnabled by appLockViewModel.biometricEnabled.collectAsState()
    val unlocked by appLockViewModel.unlocked.collectAsState()
    val securityEnabled = pinHash != null || biometricEnabled
    if (securityEnabled && !unlocked) {
        LockScreen(
            pinHash = pinHash,
            biometricEnabled = biometricEnabled,
            verifyPin = { appLockViewModel.verifyPin(it) },
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
                AnimatedAppEntrance { LoanCalcApp() }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun LoanCalcApp(themeViewModel: ThemeViewModel = hiltViewModel()) {
    var showSettings by remember { mutableStateOf(false) }
    if (showSettings) {
        SettingsScreen(onBack = { showSettings = false })
        return
    }

    val darkTheme by themeViewModel.darkTheme.collectAsState()

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
    BackHandler(enabled = currentRoute == BottomTab.BANK_LOAN.route) {
        val now = System.currentTimeMillis()
        if (now - lastBackPressAt < 2000) {
            (context as? Activity)?.finish()
        } else {
            lastBackPressAt = now
            Toast.makeText(context, "برای خروج، دوباره دکمه‌ی برگشت رو بزن", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("وام من") },
                navigationIcon = {
                    IconButton(onClick = { themeViewModel.toggleTheme() }) {
                        // پورت sunIcon/moonIcon تو www/index.html: آیکون وضعیت *فعلی* رو نشون
                        // می‌ده (ماه = الان تاریکه)، نه نتیجه‌ی تپ‌کردن - قبلاً برعکس این بود.
                        Icon(
                            if (darkTheme) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                            contentDescription = "تغییر تم",
                        )
                    }
                },
                actions = {
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
            startDestination = BottomTab.BANK_LOAN.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            composable(BottomTab.BANK_LOAN.route) {
                key(bankLoanResetKey) { BankLoanTab() }
            }
            composable(BottomTab.AFFORD.route) { AffordScreen() }
            composable(BottomTab.DEPOSIT.route) { DepositScreen() }
            composable(BottomTab.MY_LOANS.route) { MyLoansScreen() }
        }
    }
}

@Composable
private fun BankLoanTab() {
    var loanOutcome by remember { mutableStateOf<BankLoanOutcome?>(null) }
    if (loanOutcome == null) {
        BankLoanScreen(onCalculated = { loanOutcome = it })
    } else {
        ResultScreen(outcome = loanOutcome!!)
    }
}

@Composable
private fun RowScope.BottomNavItem(
    tab: BottomTab,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = if (selected) AppPrimary else AppMuted
    Column(
        modifier = Modifier
            .weight(1f)
            .then(if (selected) Modifier.background(AppPrimary.copy(alpha = 0.1f)) else Modifier)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(tab.icon, contentDescription = tab.label, tint = color, modifier = Modifier.height(22.dp))
        Text(
            tab.label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
