package ir.sadteam.loancalc

import ir.sadteam.loancalc.ui.subscription.SubscriptionExpiryReminder
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ir.sadteam.loancalc.notifications.DeepLinkTarget
import ir.sadteam.loancalc.notifications.DeepLinkViewModel
import ir.sadteam.loancalc.notifications.EXTRA_OPEN_CHEQUE_ID
import ir.sadteam.loancalc.ui.widget.EXTRA_OPEN_DUE_TAB
import ir.sadteam.loancalc.notifications.EXTRA_OPEN_LOAN_ID
import ir.sadteam.loancalc.notifications.EXTRA_OPEN_TX_ID
import ir.sadteam.loancalc.notifications.EXTRA_PICK_CATEGORY
import ir.sadteam.loancalc.notifications.PendingChequeDeepLink
import ir.sadteam.loancalc.notifications.PendingSharedSms
import ir.sadteam.loancalc.ui.account.SharedSmsDialog
import ir.sadteam.loancalc.notifications.PendingTxDeepLink
import ir.sadteam.loancalc.subscription.LocalSubscriptionManager
import ir.sadteam.loancalc.subscription.SubscriptionManager
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.auth.GateState
import ir.sadteam.loancalc.ui.auth.LoginScreen
import ir.sadteam.loancalc.ui.components.HeroChartStyle
import ir.sadteam.loancalc.ui.components.LocalHeroChartStyle
import ir.sadteam.loancalc.ui.components.LocalReducedMotion
import ir.sadteam.loancalc.ui.components.ShortcutDrawer
import ir.sadteam.loancalc.ui.haptics.rememberBuzz
import ir.sadteam.loancalc.ui.myloans.MyLoansScreen
import ir.sadteam.loancalc.ui.onboarding.AnimatedAppEntrance
import ir.sadteam.loancalc.ui.onboarding.OnboardingFlow
import ir.sadteam.loancalc.ui.onboarding.PermissionGateScreen
import ir.sadteam.loancalc.ui.onboarding.PostLoginSheets
import ir.sadteam.loancalc.ui.onboarding.SplashIntroScreen
import ir.sadteam.loancalc.ui.onboarding.permissionGateSatisfied
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyModeViewModel
import ir.sadteam.loancalc.ui.nav.NavDestination
import ir.sadteam.loancalc.ui.nav.NavEditorSheet
import ir.sadteam.loancalc.ui.nav.NavSlotsViewModel
import ir.sadteam.loancalc.ui.profile.ShortcutViewModel
import ir.sadteam.loancalc.ui.rating.RatePromptDialog
import ir.sadteam.loancalc.ui.rating.RatePromptViewModel
import ir.sadteam.loancalc.ui.security.AppLockViewModel
import ir.sadteam.loancalc.ui.security.LockScreen
import ir.sadteam.loancalc.ui.settings.SettingsScreen
import ir.sadteam.loancalc.ui.inbox.InboxScreen
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.LocalAppColors
import ir.sadteam.loancalc.ui.theme.AppPrimaryInkLight
import ir.sadteam.loancalc.ui.background.ArtTextureLayer
import ir.sadteam.loancalc.ui.background.ArtTextureState
import ir.sadteam.loancalc.ui.background.LiveBackgroundLayer
import ir.sadteam.loancalc.ui.background.LiveBackgroundState
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.ColorTheme
import ir.sadteam.loancalc.ui.shop.ShopTrial
import ir.sadteam.loancalc.data.coin.themeById
import ir.sadteam.loancalc.ui.theme.LoanCalcTheme
import ir.sadteam.loancalc.ui.theme.LocalThemeReveal
import ir.sadteam.loancalc.ui.theme.Motion
import ir.sadteam.loancalc.ui.theme.ThemeRevealHost
import ir.sadteam.loancalc.ui.theme.ThemeRevealState
import ir.sadteam.loancalc.ui.theme.ThemeViewModel
import ir.sadteam.loancalc.ui.update.AppUpdateViewModel
import ir.sadteam.loancalc.ui.update.UpdateSheet
import ir.sadteam.loancalc.data.SymbolStyle
import ir.sadteam.loancalc.data.SymbolTheme
import ir.sadteam.loancalc.data.prefs.UiPrefs
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    // پورت مو‌به‌موی الگوی نمونه‌ی رسمی Poolakey: connect تو onCreate، disconnect تو onDestroy.
    // خودِ SubscriptionManager به Activity نیاز داره (activityResultRegistry)، برای همین
    // Hilt-managed نیست و اینجا مستقیم ساخته می‌شه.
    private lateinit var subscriptionManager: SubscriptionManager

    // زدنِ نوتیفیکیشنِ یادآوریِ قسط باید مستقیم همون وام رو باز کنه (مورد ۵ تو CLAUDE.md) - رجوع
    // کن به کامنتِ DeepLinkTarget. این Activityِ ساده‌ست، کامپوزیبل نیست، پس field-injection.
    @Inject
    lateinit var deepLinkTarget: DeepLinkTarget

    @Inject
    lateinit var pendingChequeDeepLink: PendingChequeDeepLink

    @Inject
    lateinit var pendingTxDeepLink: PendingTxDeepLink

    @Inject
    lateinit var pendingSharedSms: PendingSharedSms

    private fun handleDeepLinkIntent(intent: Intent?) {
        // «اشتراک‌گذاری» از برنامه‌ی پیامکِ خودِ گوشی - تنها راهِ رسمیِ اندروید برای
        // انتخابِ یک پیام از آن‌جا. رجوع کن به [PendingSharedSms].
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("text/") == true) {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.takeIf { it.isNotBlank() }?.let {
                pendingSharedSms.set(it)
            }
        }

        val loanId = intent?.getLongExtra(EXTRA_OPEN_LOAN_ID, -1L) ?: -1L
        if (loanId > 0) {
            deepLinkTarget.setLoanId(loanId)
            ir.sadteam.loancalc.data.UsageStats.action("notif_open_loan")
        }
        // اعلانِ چک تا امروز هیچ مقصدی نداشت (نه باز می‌شد نه بسته) - رجوع کن به
        // PendingChequeDeepLink. جدا از وام نگه داشته شده چون اعلانِ گروه‌شده می‌تواند
        // هم‌زمان یکی از هر کدام داشته باشد.
        val chequeId = intent?.getLongExtra(EXTRA_OPEN_CHEQUE_ID, -1L) ?: -1L
        if (chequeId > 0) {
            pendingChequeDeepLink.setChequeId(chequeId)
            ir.sadteam.loancalc.data.UsageStats.action("notif_open_cheque")
        }
        // اعلانِ تراکنشِ خودکار (فریمِ `50b`) - هم تپ روی بدنه، هم دکمه‌ی دسته.
        val txId = intent?.getLongExtra(EXTRA_OPEN_TX_ID, -1L) ?: -1L
        if (txId > 0) {
            ir.sadteam.loancalc.data.UsageStats.action("notif_open_autotx")
            pendingTxDeepLink.set(txId, intent?.getBooleanExtra(EXTRA_PICK_CATEGORY, false) == true)
        }
        // تپ روی ویجت → تبِ سررسید (قاعده‌ی ۳ی فریمِ `57c`: ویجت درباره‌ی سررسید حرف
        // می‌زند، پس بردنِ کاربر به خانه یک قدم عقب است). پلِ تازه لازم نبود - همان
        // میان‌برِ «سررسید» دقیقاً همین کار را می‌کند.
        if (intent?.getBooleanExtra(EXTRA_OPEN_DUE_TAB, false) == true) {
            ir.sadteam.loancalc.data.UsageStats.action("widget_open")
            deepLinkTarget.setShortcut(DeepLinkTarget.SHORTCUT_DUE)
        }
        if (intent?.getBooleanExtra(ir.sadteam.loancalc.notifications.AdminAlertWorker.EXTRA_OPEN_ADMIN, false) == true) {
            ir.sadteam.loancalc.ui.admin.AdminSignals.openAdmin.value = true
        }
        // میان‌برِ فشارِ طولانی رو آیکونِ اپ - رجوع کن به res/xml/shortcuts.xml
        intent?.getStringExtra("jibak_shortcut")?.let {
            ir.sadteam.loancalc.data.UsageStats.action("shortcut_" + it.lowercase())
            deepLinkTarget.setShortcut(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleDeepLinkIntent(intent)
        subscriptionManager = SubscriptionManager(this)
        subscriptionManager.connect { }
        // زمانِ بالا آمدنِ برنامه (از شروعِ پروسه تا اولین فریم) - فقط بازه، نه عددِ دقیق.
        if (savedInstanceState == null) {
            window.decorView.post {
                val ms = android.os.SystemClock.uptimeMillis() - android.os.Process.getStartUptimeMillis()
                val bucket = when {
                    ms < 800 -> "0_800"
                    ms < 1500 -> "800_1500"
                    ms < 3000 -> "1500_3000"
                    ms < 6000 -> "3000_6000"
                    else -> "6000_plus"
                }
                ir.sadteam.loancalc.data.UsageStats.track("perf:start_$bucket")
            }
        }
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()
            val appContext = LocalContext.current.applicationContext
            val symbolPrefs = remember(appContext) { UiPrefs(appContext) }
            val activeSymbolSet by symbolPrefs.activeSymbolSet.collectAsState(initial = null)
            // سبکِ نمودارِ خریده‌شده - یک مقدار برای همه‌ی کارت‌های بالای صفحه.
            val activeChartStyle by symbolPrefs.activeChartStyle.collectAsState(initial = null)
    LaunchedEffect(activeSymbolSet) {
        SymbolTheme.style = SymbolStyle.fromItemId(activeSymbolSet)
    }
            val fontScale by themeViewModel.fontScale.collectAsState()
            val colorTheme by themeViewModel.colorTheme.collectAsState()
            val catalogTheme by themeViewModel.catalogTheme.collectAsState()
            val baseDensity = LocalDensity.current
            // عمداً بیرونِ LoanCalcTheme: این state باید از تعویضِ خودِ تم جونِ سالم به‌در ببره،
            // چون دقیقاً وسطِ همون تعویض داره کار می‌کنه (رجوع کن به ThemeReveal.kt).
            val themeReveal = remember { ThemeRevealState() }
            // «امتحان کن»ِ فروشگاه: تمِ امتحانی فقط در حافظه جای تمِ ذخیره‌شده می‌نشیند.
            val trialTheme = ShopTrial.themeId
            LoanCalcTheme(
                themeMode = themeMode,
                colorTheme = if (trialTheme != null) ColorTheme.fromId(trialTheme) else colorTheme,
                catalogTheme = if (trialTheme != null) themeById(trialTheme) else catalogTheme,
            ) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Rtl,
                    LocalSubscriptionManager provides subscriptionManager,
                    LocalThemeReveal provides themeReveal,
                    LocalHeroChartStyle provides HeroChartStyle.fromItemId(activeChartStyle),
                    // پورت .app.fs-small/fs-medium/fs-large (CSS zoom) تو www/index.html - هم
                    // فونت هم فاصله‌ها (dp) با هم مقیاس می‌شن، دقیقاً مثل زوم کل کانتینر .app.
                    // 🚨 فقط `fontScale` ضرب می‌شود، نه `density`. قبلاً هر دو ضرب می‌شدند،
                    // یعنی dpها هم بزرگ می‌شدند و این با فونت‌اسکیلِ خودِ اندروید هم جمع
                    // می‌شد: کاربری که در گوشی ۱٫۳ گذاشته و در اپ «بزرگ» را انتخاب کند به
                    // بزرگ‌نماییِ کل صفحه می‌رسید و چیدمان‌های شلوغ (کارتِ خانه، جدولِ اقساط)
                    // می‌شکستند.
                    LocalDensity provides Density(
                        density = baseDensity.density,
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

    // وقتی اپ از قبل باز/تو پس‌زمینه‌ست و کاربر رو نوتیفیکیشن می‌زنه، onCreate دوباره صدا زده نمی‌شه -
    // اینتنتِ جدید از همین‌جا می‌رسه (launchMode="singleTask" تو AndroidManifest.xml همین رو تضمین
    // می‌کنه).
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLinkIntent(intent)
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
 * بعد از گیت مجوز و قبل از گیت ورود، [OnboardingFlow] (مسیرِ ۴مرحله‌ایِ اولین ورود) فقط یه‌بار تو
 * کل عمر نصب نشون داده می‌شه؛ مرحله‌ی پنجمش همون گیتِ ورودِ پایینه. صفحه‌ی «امکانات» (BenefitsScreen)
 * به‌خواستِ صریحِ کاربر کاملاً حذف شد.
 * بعد از حل شدن گیت ورود/مهمان، صفحه‌ی اصلی مستقیم با یه افکت swoosh سریع (`AnimatedAppEntrance`) از
 * بالا-چپ میاد تو - پیامِ خوش‌آمدِ جداگانه‌ای (که قبلاً هر بار نشون داده می‌شد) به‌خواستِ کاربر حذف شد.
 *
 * قبل از همه‌ی این‌ها هم [LockScreen] چک می‌شه، فقط اگه کاربر از تنظیمات قفل PIN/اثر انگشت رو فعال
 * کرده باشه (پیش‌فرض خاموشه، هیچ‌کس رفتار قبلی رو نمی‌بینه) - رجوع کن به [AppLockViewModel].
 */
@Composable
private fun AppRoot(
    authViewModel: AuthViewModel = hiltViewModel(),
    appLockViewModel: AppLockViewModel = hiltViewModel(),
) {
    // اینتروِ باز شدن اپ - یه‌بار در هر بار باز شدن، قبل از همه‌چیز. دیگه به تمِ فعلی وابسته نیست
    // (زمینه‌ش همیشه مشکیه، چون خودِ تصویرِ اسپلش زمینه‌ی مشکی داره) - رجوع کن به SplashIntroScreen.
    //
    // **باگِ رفع‌شده (فلاشِ سفید)**: قبلاً به‌محضِ تموم‌شدنِ تایمرِ اسپلش این گیت رد می‌شد، ولی
    // `onboardingDone`/`gateState` (که از DataStore میان) هنوز `null` بودن - پس یکی از اون دوتا
    // `Surface(color = AppSurface)`ِ پایین رندر می‌شد که تو تمِ روشن **سفیدِ خالیه**. نتیجه:
    // اسپلشِ مشکی → یه فریمِ سفید → صفحه‌ی اصلی. کاربر این رو به‌عنوانِ «یه لحظه سفید می‌شه» گزارش داد.
    // رفع: تا وقتی این دوتا حاضر نشدن اسپلش سرِ جاش می‌مونه (هر دو محلی‌ان و سریع میان، پس ریسکِ
    // گیرکردن نداره - شبکه توش دخیل نیست).
    val onboardingDone by authViewModel.onboardingDone.collectAsState()
    val postLoginSheetsSeen by authViewModel.postLoginSheetsSeen.collectAsState()
    val trialDaysLeft by authViewModel.trialDaysLeft.collectAsState()
    val legacyGift by authViewModel.legacyGift.collectAsState()
    val userName by authViewModel.userName.collectAsState()
    val gateState by authViewModel.gateState.collectAsState()
    // اسپلشِ تازه (طرحِ مرجعِ کاربر، ۳۱ شهریور) - خودِ تصویر، با نقطه‌های برق‌زن.
    // فریمِ اولِ سیستمی عمداً بی‌نشان مانده (`splash_none`)، پس تنها چیزی که کاربر می‌بیند
    // همین است، نه یک تصویرِ بریده و بعد این.
    var introTimerDone by remember { mutableStateOf(false) }
    if (!introTimerDone || onboardingDone == null || gateState == null) {
        SplashIntroScreen(onDone = { introTimerDone = true })
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

    val permissionContext = LocalContext.current
    // مقدارِ اولیه **هم‌زمان** خونده می‌شه، نه `false` - وگرنه هر بار باز شدنِ اپ یه فریم از
    // صفحه‌ی مجوز فلش می‌زنه حتی وقتی کاربر قبلاً هر دو مجوز رو داده.
    var permissionsOk by remember { mutableStateOf(permissionGateSatisfied(permissionContext)) }
    // 🚨 هر دو مجوز اختیاری‌اند، ولی تا امروز راهی برای ردشدن نبود و این گیت **بن‌بست** بود:
    // اندروید بعد از دو بار ردکردنِ اعلان دیالوگ را برای همیشه خاموش می‌کند و از آن لحظه
    // کاربر اصلاً نمی‌توانست وارد برنامه‌ی خودش شود. ردکردن ذخیره می‌شود، وگرنه چون گیت هر
    // بار باز شدنِ اپ ارزیابی می‌شود دوباره سرِ راه می‌آمد.
    val permissionGateSkipped by authViewModel.permissionGateSkipped.collectAsState()
    if (!permissionsOk && permissionGateSkipped == false) {
        PermissionGateScreen(
            onAllGranted = { permissionsOk = true },
            onSkip = { authViewModel.skipPermissionGate() },
        )
        return
    }
    // تا وقتی معلوم نیست کاربر این صفحه را قبلاً رد کرده، هیچ‌چیز نشان نده (چند میلی‌ثانیه).
    if (!permissionsOk && permissionGateSkipped == null) return

    // این‌جا `onboardingDone`/`gateState` قطعاً non-nullن (گیتِ اسپلشِ بالا تضمینش می‌کنه)، پس دیگه
    // شاخه‌ی «هنوز لود نشده» با صفحه‌ی خالیِ سفید لازم نیست.
    if (onboardingDone != true) {
        val onboardingNavVm: NavSlotsViewModel = hiltViewModel()
        OnboardingFlow(onFinished = {
            // کاربرِ تازه با «حالتِ ساده» شروع می‌کند (۱۳ مهر) - از تنظیمات ← ظاهر خاموش می‌شود.
            onboardingNavVm.setSimpleMode(true)
            ir.sadteam.loancalc.data.UsageStats.track(ir.sadteam.loancalc.data.UsageStats.ONBOARDING_COMPLETED)
            authViewModel.markOnboardingDone()
        })
        return
    }

    // دو شیتِ «هدیه‌ی اشتراک» + «نگرانِ داده‌هات نباش» - فقط یه‌بار، بلافاصله بعد از اولین ورودِ
    // موفق. عمداً برای حالتِ مهمان نشون داده نمی‌شه (نه هدیه‌ای گرفته، نه داده‌ای رو سرور داره).
    // عمداً یه `if`ِ جدا قبل از `when`ه و نه یه شاخه‌ی نگهبان‌دار (`when` با `if`)، چون اون سینتکس
    // تو نسخه‌ی Kotlinِ این پروژه هنوز پایدار نیست.
    if (gateState == GateState.LOGGED_IN && postLoginSheetsSeen == false) {
        // `legacyGift` فقط از GET /api/auth/me میاد (نه از پاسخِ verify-otp)، پس قبل از نشون‌دادنِ
        // شیتِ هدیه یه‌بار تازه‌سازی می‌شه - وگرنه جمله‌ی «۱۵ روز اضافه» برای کاربرِ قدیمی نمی‌اومد.
        LaunchedEffect(Unit) { authViewModel.refreshStatus() }
        PostLoginSheets(
            trialDaysLeft = trialDaysLeft,
            legacyGift = legacyGift,
            currentName = userName,
            onSaveName = { authViewModel.updateName(it) },
            onFinished = { authViewModel.markPostLoginSheetsSeen() },
        )
        return
    }

    when (gateState) {
        null -> Unit // غیرممکن (گیتِ اسپلش)، فقط برای کاملی‌ی when
        GateState.NEEDS_LOGIN -> LoginScreen()
        GateState.GUEST, GateState.LOGGED_IN -> {
            // پیامِ خوش‌آمدِ «خوش اومدی، [شماره]» که هر بار باز شدنِ اپ نشون داده می‌شد (WelcomeMessageScreen)
            // به‌خواستِ صریحِ کاربر حذف شد - بعدِ حلِ گیتِ ورود/مهمان مستقیم می‌ره سراغِ صفحه‌ی اصلی.
            // تورِ راهنمای اولین ورود دیگه یه گیتِ جداگانه‌ی قبل از ورود نیست - کاربر خواستِ
            // «تو خود برنامه بگه کجا بری»، پس یه اورلیِ spotlight داخلِ خودِ LoanCalcApp
            // (رو المان‌های واقعیِ چیدمان) نشون داده می‌شه - رجوع کن به AppTourOverlay اونجا.
            AnimatedAppEntrance { LoanCalcApp() }
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
internal fun LoanCalcApp(
    themeViewModel: ThemeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    privacyModeViewModel: PrivacyModeViewModel = hiltViewModel(),
    appUpdateViewModel: AppUpdateViewModel = hiltViewModel(),
    deepLinkViewModel: DeepLinkViewModel = hiltViewModel(),
) {
    val showSettingsState = remember { mutableStateOf(false) }
    var showSettings by showSettingsState
    val openAdminSignal by ir.sadteam.loancalc.ui.admin.AdminSignals.openAdmin.collectAsState()
    LaunchedEffect(openAdminSignal) { if (openAdminSignal) showSettings = true }
    val showInboxState = remember { mutableStateOf(false) }
    var showInbox by showInboxState
    val showGlobalSearchState = remember { mutableStateOf(false) }
    var showGlobalSearch by showGlobalSearchState
    val showAllTransactionsState = remember { mutableStateOf(false) }
    var showAllTransactions by showAllTransactionsState
    val updateUrl by appUpdateViewModel.updateUrl.collectAsState()
    val appUpdateChanges by appUpdateViewModel.changelog.collectAsState()
    // تورِ راهنمای اولین ورود (پایین‌تر) - رجوع کن به رفعِ تداخلِ بنرِ آپدیت/تور: بنر فقط بعدِ تمومِ
    // تور نشون داده می‌شه، وگرنه هم‌زمان با اسپاتلایتِ تور بالای صفحه شلوغ/رو هم می‌افتادن.
    val tourSeen by authViewModel.tourSeen.collectAsState()

    val themeMode by themeViewModel.themeMode.collectAsState()
    val privacyMode by privacyModeViewModel.enabled.collectAsState()
    val buzz = rememberBuzz()
    // افکتِ دایره‌ایِ تعویضِ تم (سبکِ تلگرام) - خودِ اورلی تو MainActivity.setContent نصب شده،
    // اینجا فقط ماشه‌ش کشیده می‌شه. رجوع کن به ThemeReveal.kt.
    val themeReveal = LocalThemeReveal.current
    // startReveal الان suspend ئه (رجوع کن به ThemeReveal.kt) - برای تضمینِ اینکه اسنپ‌شات
    // *قبل* از عوض‌شدنِ واقعیِ تم گرفته می‌شه، هر دو کار باید تویِ یه کوروتینِ واحد و پشتِ‌سرهم
    // اجرا بشن، نه دو تا launch جدا (که ترتیبشون تضمین‌شده نیست).
    val themeToggleScope = rememberCoroutineScope()

    // وضعیت اشتراک/دوره‌ی آزمایشی رو هر بار اپ باز می‌شه از سرور تازه می‌کنیم (نه فقط لحظه‌ی ورود) -
    // وگرنه اگه اپ لاگین‌شده بمونه، دقیقاً روزی که دوره‌ی آزمایشی تموم می‌شه هیچ‌وقت خودش رو به‌روز
    // نمی‌کرد. اجرای واقعیِ محدودیت (۱ وام رایگان) همیشه سمت سرور (routes/loans.js) دفاعی چک می‌شه؛
    // این فقط UI رو هم‌زمان با واقعیت نگه می‌داره.
    LaunchedEffect(Unit) { authViewModel.refreshStatus() }
    // برگشت به برنامه (ON_RESUME) هم تازه می‌کند: هدیه/خریدِ تازه بی بستن و بازکردنِ برنامه دیده شود.
    val resumeOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(resumeOwner) {
        val obs = androidx.lifecycle.LifecycleEventObserver { _, e ->
            if (e == androidx.lifecycle.Lifecycle.Event.ON_RESUME) authViewModel.refreshStatus()
        }
        resumeOwner.lifecycle.addObserver(obs)
        onDispose { resumeOwner.lifecycle.removeObserver(obs) }
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: BottomTab.HOME.route
    // آمارِ بی‌نام: کدام صفحه‌ها واقعاً دیده می‌شوند (۷ مهر) - فقط الگوی مسیر، بی شناسه.
    LaunchedEffect(currentRoute) { ir.sadteam.loancalc.data.UsageStats.screen(currentRoute) }

    // **شخصی‌سازیِ نوارِ پایین** (بخشِ ۴۱). عمداً از کشوی میان‌بُرِ بالا **جداست**: «یک فهرست،
    // دو نمایش» - مخزنِ مقصدها مشترکه ولی ترتیبِ ذخیره‌شده نه، پس تغییرِ نوار کشو رو دست نمی‌زنه.
    val navSlotsViewModel: NavSlotsViewModel = hiltViewModel()
    val navSlots by navSlotsViewModel.slots.collectAsState()
    val navCustomized by navSlotsViewModel.customized.collectAsState()
    val navSuggestion by navSlotsViewModel.suggestion.collectAsState()
    val showReorderHint by navSlotsViewModel.showReorderHint.collectAsState()
    // یک بار در هر اجرا شمرده می‌شود، نه در هر بازسازیِ نوار.
    LaunchedEffect(showReorderHint) { if (showReorderHint) navSlotsViewModel.noteReorderHintShown() }
    var navEditorOpen by remember { mutableStateOf(false) }

    // ناوبریِ مشترک - همون الگویی که قبلاً تو ۴+ جا تکرار شده بود (تبِ پایین، دیپ‌لینک، تور،
    // برگشتن از «وام»/«چک»...) یه جا جمع شد. popUpTo+saveState+restoreState یعنی هر مقصد مثلِ یه
    // «تبِ هم‌سطح» رفتار می‌کنه - حتی «وام»/«چک» که دیگه عضوِ BottomTab نیستن (رجوع کن به کامنتِ
    // بالای BottomTab).
    fun navigateTo(route: String) {
        // **بخشِ ۴۱** - قاعده‌ی `41c`: «**رویدادِ ورودِ صفحه** شمرده می‌شود، نه بازگشتِ دکمه‌ی
        // back». شمارش عمداً اینجاست و نه تو `onClick`ِ نوار: مقصدی که نامزدِ **آمدن** به نواره
        // اصلاً از نوار باز نمی‌شه (از کشوی میان‌بُر و کاشی‌ها باز می‌شه)، پس شمارشِ نوارمحور
        // هیچ‌وقت هیچ پیشنهادی نمی‌ساخت. `navigateTo` تنها قیفِ ناوبریِ **رو به جلو**ی اپه.
        NavDestination.byRoute(route)?.let(navSlotsViewModel::recordOpen)
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // **کشوی میان‌بُر** (بخشِ ۳۱ فایلِ طراحی). هشت میان‌برِ ثابت که کاربر فقط می‌تونه
    // **جابه‌جاشون** کنه، نه حذف («غیرقابلِ حذف در نسخه‌ی اول» - قاعده‌ی `31c`).
    val shortcutViewModel: ShortcutViewModel = hiltViewModel()
    val savedShortcutOrder by shortcutViewModel.order.collectAsState()
    // **انتخاب** جدا از **ترتیب** ذخیره می‌شه (فریمِ `53a`) - خالی یعنی کاربر هنوز چیزی
    // انتخاب نکرده، پس همون هشتِ پیش‌فرض می‌مونه، نه کشوی خالی.
    val savedShortcutSelection by shortcutViewModel.selection.collectAsState()
    val shortcutDrawerOpenState = remember { mutableStateOf(false) }
    var shortcutDrawerOpen by shortcutDrawerOpenState
    // ۱۴ مهر (خواسته‌ی کاربر): میان‌برهایی که فقط یک تبِ نوارِ پایین را باز می‌کردند («ثبتِ خرج»،
    // «گزارشِ ماه»، «انتقال»، «طلا»، «بودجه» و…) یک بار به «همه‌ی ابزارها» می‌روند و جایشان را
    // ابزارهای واقعی می‌گیرند. حذف نمی‌شوند و کاربر با نگه‌داشتن می‌تواند برشان گرداند.
    val shortcutCtx = LocalContext.current
    val shortcutPrefs = remember { shortcutCtx.getSharedPreferences("shortcut_migrations", android.content.Context.MODE_PRIVATE) }
    LaunchedEffect(Unit) {
        if (shortcutPrefs.getBoolean("demote_tab_dupes_v1", false)) return@LaunchedEffect
        val first = listOf("due", "cheque", "calendar", "debts", "bills", "debt", "savings-goal", "loan-stats")
        val demoted = setOf("expense", "transfer", "report", "gold", "budget", "home", "assets", "loan")
        val rest = allShortcutPool.map { it.id }.filterNot { it in first || it in demoted }
        val newOrder = first + rest + demoted.toList()
        shortcutViewModel.save(newOrder)
        shortcutViewModel.saveSelection(allShortcutPool.map { it.id })
        shortcutPrefs.edit().putBoolean("demote_tab_dupes_v1", true).apply()
    }
    val shortcuts = remember(savedShortcutOrder, savedShortcutSelection) {
        val byId = allShortcutPool.associateBy { it.id }
        val stored = savedShortcutSelection.mapNotNull { byId[it] }
        // انتخابِ پنج‌تاییِ نسخه‌های قبلی، فقط یک ردیفِ اولیه بود؛ حالا همه‌ی مقصدها نمایش داده می‌شوند.
        // کشو همیشه حداقل دو ردیفِ پنج‌تایی دارد؛ انتخابِ قدیمیِ پنج‌تایی با مقصدهای تازه کامل می‌شود.
        val selected = (if (stored.isEmpty() || stored.map { it.id } == defaultShortcuts.map { it.id }) allShortcutPool else stored)
            // کفِ نمایش با بزرگ‌شدنِ مخزن بالا رفت: انتخابِ قدیمیِ کوچک با مقصدهای تازه
            // کامل می‌شود تا کشو خالی‌تر از چیزی که هست به‌نظر نرسد.
            .let { chosen -> if (chosen.size >= 12) chosen else chosen + allShortcutPool.filterNot { it.id in chosen.map { item -> item.id } }.take(12 - chosen.size) }
        // ترتیبِ ذخیره‌شده اول میاد؛ شناسه‌ی ناشناخته نادیده و میان‌برِ تازه ته لیست اضافه می‌شه.
        val ordered = savedShortcutOrder.mapNotNull { id -> selected.firstOrNull { it.id == id } }
        (ordered + selected.filterNot { it.id in savedShortcutOrder }).filterNot { it.id in hiddenShortcutIds }
    }

    // «وام‌های من» دیگه تبِ جداگانه‌ی خودش نیست، یه زیرصفحه‌ی داخلِ تبِ «وام»ه (رجوع کن به
    // [LoanTab]/[LoanSubTab]) - این state بهش می‌گه کدوم زیرصفحه رو باز کنه، مستقل از اینکه کاربر
    // خودش دستی رو کدوم زیرصفحه بوده.
    val requestedLoanSubTabState = remember { mutableStateOf<LoanSubTab?>(null) }
    var requestedLoanSubTab by requestedLoanSubTabState

    // زدنِ نوتیفیکیشنِ یادآوریِ قسط (مورد ۵) - رجوع کن به کامنتِ DeepLinkTarget. اگه رو تبِ «وام»
    // نیستیم، اول باید بریم اونجا و زیرصفحه‌ی «وام‌های من» رو باز کنیم؛ خودِ بازکردنِ وامِ خاص تو
    // MyLoansScreen انجام می‌شه (پارامترِ deepLinkLoanId پایین‌تر).
    ir.sadteam.loancalc.ui.shop.TrialHost(onOpenShop = { navigateTo(SHOP_ROUTE) })

    val deepLinkLoanId by deepLinkViewModel.pendingLoanId.collectAsState()
    LaunchedEffect(deepLinkLoanId) {
        if (deepLinkLoanId != null) {
            requestedLoanSubTab = LoanSubTab.MY_LOANS
            if (currentRoute != LOAN_ROUTE) navigateTo(LOAN_ROUTE)
        }
    }

    // تپ روی اعلانِ تراکنشِ خودکار (فریمِ `50b`) - مرکزِ پیام‌ها باز می‌شود، چون کارتِ اقدامِ
    // همان تراکنش (تایید / انتخابِ دسته) همان‌جاست. دکمه‌ی «دسته» هم به همین‌جا می‌رسد؛
    // ⏳ نشستنِ مستقیم روی شیتِ دسته هنوز نیست و کاربر یک تپِ اضافه می‌زند.
    val pendingTx by deepLinkViewModel.pendingTx.collectAsState()
    // ۱۴ مهر: دکمه‌ی «انتخابِ دسته/بقیه…» مستقیم پنجره‌ی دسته را باز می‌کند، نه فقط پیام‌ها را.
    var quickCategoryTxId by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(pendingTx) {
        val p = pendingTx ?: return@LaunchedEffect
        if (p.second) quickCategoryTxId = p.first else showInbox = true
        deepLinkViewModel.consumeTx()
    }
    quickCategoryTxId?.let { id ->
        ir.sadteam.loancalc.ui.inbox.QuickCategoryDialog(txId = id, onDismiss = { quickCategoryTxId = null })
    }

    // پیامی که از برنامه‌ی پیامکِ خودِ گوشی «اشتراک‌گذاری» شده - خواسته‌ی صریحِ کاربر:
    // «وقتی رفت داخلِ پیامک‌های گوشی، از اون‌جا بتونم انتخاب کنم». اندروید اجازه‌ی
    // دکمه‌گذاشتن داخلِ آن برنامه را نمی‌دهد، پس این تنها راهِ رسمی است.
    val sharedSms by deepLinkViewModel.sharedSmsText.collectAsState()
    sharedSms?.let { body ->
        SharedSmsDialog(text = body, onDone = { deepLinkViewModel.consumeSharedSms() })
    }

    // میان‌برِ فشارِ طولانی رو آیکونِ اپ (مثلِ دولینگو) - رجوع کن به res/xml/shortcuts.xml.
    // ⚠️ «تراکنشِ تازه» فعلاً فقط تبِ خانه رو باز می‌کنه (دکمه‌ی + همون‌جاست)؛ بازکردنِ
    // مستقیمِ شیت یه پرچمِ سراسری لازم داره که هنوز نداریم.
    val pendingShortcut by deepLinkViewModel.pendingShortcut.collectAsState()
    LaunchedEffect(pendingShortcut) {
        when (pendingShortcut) {
            DeepLinkTarget.SHORTCUT_ADD_TRANSACTION -> navigateTo(BottomTab.HOME.route)
            DeepLinkTarget.SHORTCUT_DUE -> navigateTo(BottomTab.DUE.route)
            DeepLinkTarget.SHORTCUT_REPORT -> navigateTo(BottomTab.REPORT.route)
            "budget" -> navigateTo(BottomTab.BUDGET.route)
            else -> return@LaunchedEffect
        }
        deepLinkViewModel.consumeShortcut()
    }

    // یادآوریِ دوره‌ایِ امتیازدادن تو استور (مورد ۲۵) - رجوع کن به RatePromptViewModel برای منطقِ
    // زمان‌بندی. onAppOpened فقط یه‌بار به‌ازای هر ورودِ موفق به LoanCalcApp صدا زده می‌شه.
    val context = LocalContext.current
    val ratePromptViewModel: RatePromptViewModel = hiltViewModel()
    val showRatePrompt by ratePromptViewModel.shouldShow.collectAsState()
    LaunchedEffect(Unit) { ratePromptViewModel.onAppOpened() }
    // 🗳 نظرسنجیِ یک‌سؤاله از سرور (۸ مهر) - آخرِ صفِ پنجره‌ها، هر نظرسنجی یک بار.
    ir.sadteam.loancalc.data.RemoteApp.config.survey?.takeIf { it.id.isNotBlank() && it.options.isNotEmpty() }?.let { sv ->
        val prefs = remember { context.getSharedPreferences("survey", android.content.Context.MODE_PRIVATE) }
        var done by remember(sv.id) { mutableStateOf(prefs.getBoolean(sv.id, false)) }
        val scope = rememberCoroutineScope()
        if (!done && !showRatePrompt && ir.sadteam.loancalc.ui.components.StartupPopups.canShowRate) {
            val close = { prefs.edit().putBoolean(sv.id, true).apply(); done = true }
            ir.sadteam.loancalc.ui.components.JibakAlertDialog(
                onDismissRequest = close,
                title = { Text(sv.question) },
                text = {
                    androidx.compose.foundation.layout.Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                        sv.options.forEach { opt ->
                            ir.sadteam.loancalc.ui.components.AppChip(label = opt, selected = false, onClick = {
                                scope.launch { authViewModel.sendSurvey(sv.id, opt) }
                                close()
                            }, modifier = Modifier.fillMaxWidth())
                        }
                    }
                },
                confirmButton = { androidx.compose.material3.TextButton(onClick = close) { Text("بعداً نه") } },
            )
        }
    }
    if (showRatePrompt && ir.sadteam.loancalc.ui.components.StartupPopups.canShowRate) {
        RatePromptDialog(
            onRateNow = {
                ratePromptViewModel.onRateNow()
                val storeUrl = if (BuildConfig.FLAVOR == "myket") {
                    "https://myket.ir/app/ir.sadteam.loancalc"
                } else {
                    "https://cafebazaar.ir/app/ir.sadteam.loancalc"
                }
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(storeUrl))) }
            },
            onLater = { ratePromptViewModel.onLater() },
            onDismissForever = { ratePromptViewModel.onDismissForever() },
        )
    }

    // نوارِ پایینِ تب‌ها موقعِ اسکرولِ رو‌به‌پایینِ لیستِ «وام‌های من» جمع می‌شه، با اسکرولِ رو‌به‌بالا
    // دوباره ظاهر می‌شه - فقط MyLoansScreen این callback رو صدا می‌زنه (رجوع کن به onBottomBar
    // VisibilityChanged اونجا)؛ بقیه‌ی تب‌ها/زیرصفحه‌ها همیشه نوار رو نشون می‌دن. چون «وام‌های من»
    // الان زیرصفحه‌ی داخلِ تبِ «وام»ه (نه یه route جدا)، ریست‌شدنِ موقعِ تغییرِ تبِ اصلی اینجا کافیه؛
    // ریست‌شدنِ موقعِ سوییچِ بینِ زیرصفحه‌ها (مثلاً «وام‌های من» ← «بانکی») تو خودِ [LoanTab] انجام
    // می‌شه.
    val bottomBarVisibleState = remember { mutableStateOf(true) }
    var bottomBarVisible by bottomBarVisibleState
    LaunchedEffect(currentRoute) {
        if (currentRoute != LOAN_ROUTE) bottomBarVisible = true
    }

    // تپ دوباره رو هر تبی که از قبل انتخابه باید به صفحه‌ی اصلیِ همون تب ریست کنه (قبلاً فقط «وام
    // بانکی» این رفتار رو داشت؛ الان رو هر ۴ تب یکسانه) — چون launchSingleTop جلوی navigate دوباره
    // به همون مقصد رو می‌گیره، این ریست از طریق یه کلیدِ جدا به‌ازای هر تب اعمال می‌شه (remount کامل
    // یعنی هر state داخلیِ خودِ اسکرین - فرم‌های نیمه‌پرشده، جزئیاتِ بازشده‌ی یه وام، و... - به مقدارِ
    // اولیه برمی‌گرده).
    val tabResetKeys = remember { mutableStateMapOf<BottomTab, Int>() }

    // مختصاتِ واقعیِ هر هدفِ تور رو صفحه (چهارتا تبِ نوارِ پایین + سه‌تا آیکونِ TopAppBar + دکمه‌ی
    // افزودنِ دستیِ MyLoansScreen) - برای اینکه AppTourOverlay بتونه دقیقاً دورِ المانِ واقعی یه
    // سوراخِ نورانی بکشه، نه یه مختصاتِ حدسی. رجوع کن به onGloballyPositioned رو هر کدوم پایین‌تر.
    val tourBounds = remember { mutableStateMapOf<TourTarget, Rect>() }
    // مرکزِ آیکونِ تم - مبدأ دایره‌ی بازشونده‌ی ThemeReveal. قبلاً از tourBounds خونده می‌شد،
    // ولی اون قدمِ تور حذف شد (رجوع کن به TourTarget).

    // پورت رفتار «یه‌بار برگشت بزنی هشدار بده، دوباره بزنی خارج شو» - فقط رو تب پیش‌فرض (وام بانکی)
    // فعاله، چون تو بقیه‌ی تب‌ها/تنظیمات دکمه‌ی برگشت باید همون رفتار عادیش (برگشت به تب قبلی/بستن
    // تنظیمات) رو داشته باشه.
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
    // اگه رو تبِ اصلی (خانه) نیستیم، اول باید برگردیم به همون تب - نه اینکه یهو از کلِ اپ خارج
    // بشیم. زیرصفحه‌های داخلِ خودِ هر تب (مثلاً جزئیاتِ وام تو «وام‌های من»، یا خودِ سوییچِ بینِ
    // زیرصفحه‌های تبِ «وام» تو [LoanTab]) اول با BackHandlerِ خودشون (اولویتِ بالاتر، چون دیرتر
    // رجیستر می‌شن) بسته می‌شن؛ این فقط وقتی به کار میاد که همون تب رو ریشه‌ی خودشه. «وام»/«چک»
    // (که دیگه عضوِ BottomTab نیستن) هم از همینجا رد می‌شن - برگشت ازشون یعنی برو خونه، دقیقاً
    // مثلِ برگشت از هر تبِ دیگه‌ای.
    BackHandler(enabled = true) {
        if (currentRoute == BottomTab.HOME.route) {
            val now = System.currentTimeMillis()
            if (now - lastBackPressAt < 2000) {
                (context as? Activity)?.finish()
            } else {
                lastBackPressAt = now
                showExitHint = true
            }
        } else {
            navigateTo(BottomTab.HOME.route)
        }
    }

    val reducedMotion by themeViewModel.reducedMotion.collectAsState()
    val isPremium by authViewModel.subscribed.collectAsState()
    CompositionLocalProvider(
        LocalPrivacyMode provides privacyMode,
        LocalReducedMotion provides reducedMotion,
        ir.sadteam.loancalc.ui.subscription.LocalIsPremium provides isPremium,
        ir.sadteam.loancalc.ui.privacy.LocalSimpleMode provides navSlotsViewModel.simpleMode.collectAsState().value,
    ) {
    ir.sadteam.loancalc.ui.subscription.PremiumPaywallHost()
    // صاحبِ برنامه: خبرِ پیامِ تازه‌ی کاربران (برای بقیه همان اولِ کار بی‌صدا تمام می‌شود).
    val supportInboxVm: ir.sadteam.loancalc.ui.admin.SupportInboxViewModel = hiltViewModel()
    LaunchedEffect(Unit) { supportInboxVm.watch() }
    val subUntil by authViewModel.subscribedUntil.collectAsState()
    val trialLeft by authViewModel.trialDaysLeft.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        // ⚠️ **بازطراحیِ سبکِ «جیبک»**: پس‌زمینه‌ی زنده‌ی «شفق» (`AuroraBackground` - دو هاله‌ی
        // گرادیانیِ سبزآبی/طلایی که آروم نفس می‌کشیدن) **حذف شد**. سبکِ جدید یه زمینه‌ی
        // **تختِ تک‌رنگ** می‌خواد (`#F5FBFF` روشن / `#10181F` تیره) تا کارت‌های ماتِ سفید و
        // سایه‌های سختشون رو یه سطحِ آروم بشینن؛ هاله‌ی متحرک پشتشون همون «شلوغیِ» بصری‌ای بود
        // که این بازطراحی می‌خواد ازش فاصله بگیره.
        // `AuroraBackground.kt` عمداً پاک نشد (ممکنه برای اسپلش/صفحه‌ی خوش‌آمد لازم بشه).
        // **پس‌زمینه‌ی زنده‌ی خریدنی** (قلمِ تازه‌ی فروشگاه، دورِ ۱۳). زیرِ `Scaffold` که
        // خودش `containerColor` دارد نمی‌شود کشیدش، پس یک `Box` بیرونی می‌گیرد: رنگِ تخت
        // + لایه‌ی متحرک + محتوا. `Scaffold` بعدش شفاف می‌شود وگرنه لایه را می‌پوشاند.
        val backdrop = LiveBackgroundState.active
        Box(modifier = Modifier.fillMaxSize().background(AppBg)) {
            // 🚨 **در `Scaffold`ِ ریشه کشیده می‌شود، نه در هر صفحه** (تاکیدِ صریحِ بخشِ ۷۸):
            // با کشیدن در هر صفحه، انیمیشن در هر تعویضِ تب از صفر شروع می‌شود و پرشِ
            // محسوس می‌دهد.
            LiveBackgroundLayer(
                background = backdrop,
                primary = AppPrimary,
                primaryLight = AppPrimaryInkLight,
                isDark = LocalAppColors.current.isDark,
                modifier = Modifier.fillMaxSize(),
            )
            // بافتِ تمِ هنری. ایستاست، پس روی همان لایه می‌نشیند و با پس‌زمینه‌ی زنده جمع
            // می‌شود؛ زیرِ کارت‌ها هیچ‌وقت نمی‌آید، فقط زمینه‌ی صفحه.
            ArtTextureLayer(texture = ArtTextureState.active, modifier = Modifier.fillMaxSize())
        Scaffold(
            containerColor = if (backdrop != null) Color.Transparent else AppBg,
            topBar = {
                // ⚠️ **تبِ خانه نوارِ بالا نداره.** فریمِ `15a`/`15b` هیچ نوارِ بالایی نشون نمی‌ده
                // و کاربر هم صریحاً گفت «اون تنظیمات بالا نباشن، حالت شب نباشه». راهِ رفتن به
                // تنظیمات از **آدمکِ داخلِ هدرِ خودِ خانه**ست (کارتِ `32c`)، و تم رفته داخلِ
                // تنظیمات (ردیفِ «ظاهر برنامه»).
                //
                // **هر پنج تب** حالا هدرِ درون‌صفحه‌ی خودشو داره (فریم‌های `15a`/`26b`/`26a`/
                // `27c`/`3a`)، پس نوارِ بالا فقط رو صفحه‌های پوش‌شده‌ی «وام»/«چک» می‌مونه -
                // وگرنه عنوان دو بار پشتِ‌هم دیده می‌شد (گزارشِ کاربر با اسکرین‌شات).
                // ⚠️ **نوارِ بالای سراسری کاملاً حذف شد** (بازخوردِ دورِ ۹).
                //
                // این نوار فقط روی صفحه‌های پوش‌شده (وام/چک/طلب‌وبدهی) دیده می‌شد و هر سه
                // خودشان هدر و دکمه‌ی بازگشتِ خودشان را دارند - یعنی یک نوارِ **بی‌عنوان** با
                // سه آیکون بالای هدرِ واقعی می‌نشست و تقریباً یک‌سومِ صفحه‌ی وام را می‌خورد.
                //
                // هر سه آیکون جای دیگری در دسترس‌اند و این‌جا **تکرار** بودند: چرخ‌دنده در
                // هدرِ تبِ خانه · حالتِ خصوصی در هدرِ خانه و گزارش · تغییرِ تم در هدرِ خانه و
                // در «تنظیمات ← ظاهر برنامه».
            },
            bottomBar = {
                // نوارِ ناوبریِ پایین - **پنج تب و فقط همین پنج**: خانه، دارایی، گزارش، بودجه،
                // سررسید. «وام» و «چک» عمداً تب نیستن و از داخلِ صفحه‌های دیگه باز می‌شن، و
                // **دکمه‌ی شناورِ میانی هم نداریم** (یه‌بار اضافه و به‌خواستِ کاربر برداشته شد؛
                // سیستمِ طراحی هم صریحاً همینو می‌گه) - افزودنِ تراکنش از دکمه‌ی درونِ تبِ خانه‌ست.
                // ⚠️ محوِ آنی بود و کاربر گفت می‌خواهد نرم باشد. علتِ آن حذفِ آنی هم واقعی
                // بود: `AnimatedVisibility`ِ **فقط محوشونده** تا پایانِ خروج ارتفاعِ نوار را
                // برای `Scaffold` نگه می‌داشت و روی اسکرولِ سریع یک نوارِ سفیدِ موقت می‌ساخت.
                // `shrinkVertically` همان ارتفاع را هم‌قدمِ لغزش جمع می‌کند، پس جای خالی
                // نمی‌مانَد.
                AppBottomBar(
                    currentRoute = currentRoute,
                    navSlots = navSlots,
                    navigateTo = ::navigateTo,
                    tabResetKeys = tabResetKeys,
                    tourBounds = tourBounds,
                    shortcutDrawerOpenState = shortcutDrawerOpenState,
                    bottomBarVisibleState = bottomBarVisibleState,
                )
            },
        ) { padding ->
            // ⚠️ **بخشِ ۴۱**: محتوا تو یه `Box` پیچیده شد تا ویرایشگرِ نوار بتونه **داخلِ همین
            // ناحیه** (بالای نوارِ پایین) بشینه. قاعده‌ی مرکزیِ `41b` اینه که «نوارِ واقعی
            // پایینِ صفحه می‌مانَد و همان لحظه عوض می‌شود» - یه دیالوگِ تمام‌صفحه همون نوار رو
            // می‌پوشوند و کلِ ایده رو خراب می‌کرد.
            // بازشدنِ کیبورد قبلاً محتوا را بالا نمی‌برد و فیلدِ فعال زیرِ کیبورد گم می‌شد
            // (`adjustResize` در مانیفست + `imePadding` این‌جا، با هم). `consumeWindowInsets`
            // لازم است وگرنه پدینگِ Scaffold دو بار حساب می‌شود.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .imePadding(),
            ) {
            NavHost(
                navController = navController,
                startDestination = BottomTab.HOME.route,
                modifier = Modifier.fillMaxSize(),
                // پورت کاملِ اسلاید جهت‌دار بین ۴ تب اصلی وب (switchTab: slide-l/slide-r): جهت از
                // رو فاصله‌ی ایندکس تب قبلی/جدید تو ترتیب تب‌ها حساب می‌شه و صفحه‌ی جدید با یه
                // اسلاید فنری از همون سمتِ حرکت میاد تو - حس «پریمیوم»تر از fade+scale قبلی.
                // حسِ هر بخش - رجوع کن به Motion.Feel. مقصد تعیین می‌کند (ورود) و مبدأ (خروج).
                enterTransition = {
                    Motion.enterFor(feelOf(targetState.destination.route), slideDirection(initialState.destination.route, targetState.destination.route))
                },
                exitTransition = {
                    Motion.exitFor(feelOf(initialState.destination.route), slideDirection(initialState.destination.route, targetState.destination.route))
                },
                popEnterTransition = {
                    Motion.enterFor(feelOf(targetState.destination.route), slideDirection(initialState.destination.route, targetState.destination.route))
                },
                popExitTransition = {
                    Motion.exitFor(feelOf(initialState.destination.route), slideDirection(initialState.destination.route, targetState.destination.route))
                },
            ) {
                appRoutes(
                    deepLinkViewModel = deepLinkViewModel,
                    navController = navController,
                    navSlotsViewModel = navSlotsViewModel,
                    navSlots = navSlots,
                    navSuggestion = navSuggestion,
                    navigateTo = ::navigateTo,
                    deepLinkLoanId = deepLinkLoanId,
                    tabResetKeys = tabResetKeys,
                    showSettingsState = showSettingsState,
                    showInboxState = showInboxState,
                    showGlobalSearchState = showGlobalSearchState,
                    showAllTransactionsState = showAllTransactionsState,
                    shortcutDrawerOpenState = shortcutDrawerOpenState,
                    requestedLoanSubTabState = requestedLoanSubTabState,
                    bottomBarVisibleState = bottomBarVisibleState,
                )
            }
            if (navEditorOpen) {
                NavEditorSheet(
                    slots = navSlots,
                    customized = navCustomized,
                    onSlotsChange = navSlotsViewModel::setSlots,
                    onReset = navSlotsViewModel::resetToDefault,
                    onClose = { navEditorOpen = false },
                )
            }
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
                SettingsScreen(onBack = { showSettings = false })
            }
        }

        // دکمه‌ی برگشتِ گوشی اول مرکزِ پیام‌ها رو می‌بنده، نه اینکه بره خونه. چون این
        // BackHandler **دیرتر** از BackHandlerِ اصلیِ بالای همین تابع رجیستر می‌شه، اولویتش
        // بالاتره - همون الگویی که زیرصفحه‌های تبِ وام هم ازش استفاده می‌کنن.
        BackHandler(enabled = showInbox) { showInbox = false }

        // مرکزِ پیام‌ها (بخشِ ۴۰) - برخلافِ تنظیمات که پنلِ ۸۵٪ه، این یه صفحه‌ی تمام‌صفحه‌ست
        // چون فهرستِ بلند داره و کارت‌های اقدام‌دارش دکمه‌ی افقی دارن.
        AnimatedVisibility(
            visible = showInbox,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.fillMaxSize(),
        ) {
            Surface(color = AppBg, modifier = Modifier.fillMaxSize()) {
                InboxScreen(onBack = { showInbox = false }, onOpenShop = { showInbox = false; navigateTo(SHOP_ROUTE) })
            }
        }

        BackHandler(enabled = showAllTransactions) { showAllTransactions = false }
        AnimatedVisibility(
            visible = showAllTransactions,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.fillMaxSize(),
        ) {
            Surface(color = AppBg, modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                ir.sadteam.loancalc.ui.accounting.AllTransactionsScreen(onBack = { showAllTransactions = false })
            }
        }

        BackHandler(enabled = showGlobalSearch) { showGlobalSearch = false }
        AnimatedVisibility(
            visible = showGlobalSearch,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.fillMaxSize(),
        ) {
            Surface(color = AppBg, modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                ir.sadteam.loancalc.ui.search.GlobalSearchScreen(
                    onClose = { showGlobalSearch = false },
                    onOpenLoan = { id ->
                        showGlobalSearch = false
                        deepLinkViewModel.openLoan(id)
                    },
                    onOpenCheque = { id ->
                        showGlobalSearch = false
                        deepLinkViewModel.openCheque(id)
                        navigateTo(CHEQUE_ROUTE)
                    },
                    onOpenPerson = { id ->
                        showGlobalSearch = false
                        deepLinkViewModel.openDebt(id)
                        navigateTo(DEBT_ROUTE)
                    },
                    onOpenBills = {
                        showGlobalSearch = false
                        navigateTo(BottomTab.BUDGET.route)
                    },
                    onOpenNotes = {
                        showGlobalSearch = false
                        navigateTo(NOTES_ROUTE)
                    },
                    onOpenRoute = { key ->
                        showGlobalSearch = false
                        navigateTo(
                            when (key) {
                                "assets" -> BottomTab.ASSETS.route
                                "goal" -> SAVINGS_GOAL_ROUTE
                                "debt" -> DEBT_ROUTE
                                else -> BottomTab.BUDGET.route
                            },
                        )
                    },
                )
            }
        }

        // کشوی میان‌بُر رو کلِ صفحه می‌شینه (پرده‌ی تیره + خودِ کشو) ولی **زیرِ** نوارِ پایین
        // نمی‌ره - طرح صریحاً می‌خواد نوار همیشه دیده بشه.
        // دکمه‌ی برگشتِ گوشی کشو را ببندد، نه از برنامه بیرون برود (خواسته‌ی کاربر ۱۳ مهر).
        androidx.activity.compose.BackHandler(enabled = shortcutDrawerOpen) { shortcutDrawerOpen = false }
        ShortcutDrawer(
            // خاموش کردنِ اضطراری از سرور (۸ مهر).
            shortcuts = shortcuts.filterNot { ir.sadteam.loancalc.data.RemoteApp.isDisabled(it.id) },
            visible = shortcutDrawerOpen,
            onDismiss = { shortcutDrawerOpen = false },
            onOpenRoute = { route ->
                when (route) {
                    "settings" -> showSettings = true
                    "search" -> showGlobalSearch = true
                    else -> navigateTo(route)
                }
            },
            onOrderChanged = { ids -> shortcutViewModel.save(ids) },
            allShortcuts = allShortcutPool.filterNot { ir.sadteam.loancalc.data.RemoteApp.isDisabled(it.id) || it.id in hiddenShortcutIds },
            onSelectionChanged = { ids -> shortcutViewModel.saveSelection(ids) },
            inBottomBarIds = navSlots.map { it.id }.toSet(),
        )

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
                shape = RoundedCornerShape(AppRadius.sheet),
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
        // برگه‌ی پایینِ آپدیت (هم‌شکلِ برگه‌ی بازار) - جای بنرِ باریکِ قبلی. رجوع کن به UpdateSheet.
        // ⛔ حداقل نسخه‌ی مجاز از سرور (۸ مهر): پنجره‌ای که بسته نمی‌شود تا آپدیت شود.
        ir.sadteam.loancalc.data.RemoteApp.config.minVersion?.takeIf { BuildConfig.VERSION_CODE < it }?.let {
            val storeUrl = if (BuildConfig.FLAVOR == "myket") "https://myket.ir/app/ir.sadteam.loancalc" else "https://cafebazaar.ir/app/ir.sadteam.loancalc"
            ir.sadteam.loancalc.ui.components.JibakAlertDialog(
                onDismissRequest = {},
                title = { Text("نسخه‌ی تازه لازم است") },
                text = { Text(ir.sadteam.loancalc.data.RemoteApp.config.minVersionText ?: "این نسخه دیگر پشتیبانی نمی‌شود. برای ادامه، برنامه را از استور به‌روز کن.") },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(storeUrl))) } }) { Text("به‌روزرسانی") }
                },
            )
        }
        // اگر پنجره‌ی سکه‌ی روزانه زودتر باز شده، برگه‌ی آپدیت بعد از بستنش می‌آید (نه رویش).
        val updateVisible = updateUrl != null && tourSeen != false && !ir.sadteam.loancalc.ui.components.StartupPopups.checkInOpen
        ir.sadteam.loancalc.ui.components.StartupPopups.tourOrUpdate = tourSeen == false || updateVisible
        UpdateSheet(
            visible = updateVisible,
            changes = appUpdateChanges,
            onUpdate = {
                updateUrl?.let { url -> runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } }
                appUpdateViewModel.dismiss()
            },
            onDismiss = { appUpdateViewModel.dismiss() },
        )
        // هشدارِ پایانِ اشتراک: اگر برگه‌ی آپدیت باز است، اول آن؛ هشدار بعد از بستنش می‌آید
        // (روزی یک بار شمرده می‌شود فقط وقتی واقعاً دیده شد).
        if (isPremium && updateUrl == null && tourSeen != false) {
            SubscriptionExpiryReminder(
                ir.sadteam.loancalc.ui.subscription.parseSubscribedUntil(subUntil)?.daysLeft ?: trialLeft,
            )
        }

        // تورِ راهنمای اولین ورود - «تو خود برنامه بگه کجا بری» (خواسته‌ی صریح کاربر، به‌جای صفحه‌ی
        // جدای قبلی) - رجوع کن به AppTourOverlay پایین‌تر. آخرین بچه‌ی Box تا رو همه‌چیز دیگه بشینه.
        // (tourSeen بالاتر جمع‌آوری شده، برای گیت‌کردنِ بنرِ آپدیت هم استفاده می‌شه)
        if (tourSeen == false) {
            // 🧭 راهنمای تعاملی (۹ مهر): هر قدم روی دکمه‌ی واقعی نور می‌اندازد و با کارِ واقعیِ کاربر جلو می‌رود.
            val guideAccountVm: ir.sadteam.loancalc.ui.account.AccountViewModel = hiltViewModel()
            val guideAccounts by guideAccountVm.accounts.collectAsState()
            val guideTxs by guideAccountVm.transactions.collectAsState()
            val guideCoinVm: ir.sadteam.loancalc.ui.profile.GamificationViewModel = hiltViewModel()
            val steps = remember { guideSteps() }
            var guideIndex by rememberSaveable { mutableIntStateOf(0) }
            val finish = {
                ir.sadteam.loancalc.data.UsageStats.action("guide_done_$guideIndex")
                authViewModel.markTourSeen()
            }
            val step = steps.getOrNull(guideIndex)
            // قدمی که کارش از قبل انجام شده خودش رد می‌شود (مثلاً کسی که حساب دارد).
            LaunchedEffect(Unit) { showSettings = false }
            LaunchedEffect(guideIndex, currentRoute, guideAccounts.size, guideTxs.size, shortcutDrawerOpen) {
                val done = when (step?.target) {
                    "tab_assets" -> currentRoute == BottomTab.ASSETS.route
                    "add_account" -> guideAccounts.isNotEmpty()
                    "tab_home" -> currentRoute == BottomTab.HOME.route
                    "add_tx" -> guideTxs.isNotEmpty()
                    "tab_loan" -> currentRoute == LOAN_ROUTE
                    "shortcuts" -> shortcutDrawerOpen
                    else -> false
                }
                if (done) guideIndex++
                ir.sadteam.loancalc.data.UsageStats.action("guide_step_$guideIndex")
            }
            if (step == null) {
                LaunchedEffect(Unit) { finish() }
            } else {
                ir.sadteam.loancalc.ui.components.GuideOverlay(
                    step = step,
                    index = guideIndex,
                    total = steps.size,
                    onNext = {
                        if (step.target == "shortcuts") shortcutDrawerOpen = false
                        if (guideIndex >= steps.lastIndex) { guideCoinVm.awardGuideDone(); finish() } else guideIndex++
                    },
                    onClose = { shortcutDrawerOpen = false; finish() },
                )
            }
        }
    }
    }
}
