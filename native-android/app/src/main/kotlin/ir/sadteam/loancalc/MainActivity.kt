package ir.sadteam.loancalc

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.SwapHoriz
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import ir.sadteam.loancalc.ui.AffordScreen
import ir.sadteam.loancalc.ui.BankLoanOutcome
import ir.sadteam.loancalc.ui.BankLoanScreen
import ir.sadteam.loancalc.ui.CalculatorHostScreen
import ir.sadteam.loancalc.ui.DepositScreen
import ir.sadteam.loancalc.ui.ResultScreen
import ir.sadteam.loancalc.ui.accounting.AssetsScreen
import ir.sadteam.loancalc.ui.accounting.BudgetScreen
import ir.sadteam.loancalc.ui.accounting.ReportScreen
import ir.sadteam.loancalc.ui.accounting.ReportTabScreen
import ir.sadteam.loancalc.ui.asset.AssetsTabScreen
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.auth.GateState
import ir.sadteam.loancalc.ui.auth.LoginScreen
import ir.sadteam.loancalc.ui.cheque.ChequeScreen
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.LocalReducedMotion
import ir.sadteam.loancalc.ui.components.Shortcut
import ir.sadteam.loancalc.ui.components.ShortcutDrawer
import ir.sadteam.loancalc.ui.components.ShortcutDrawerHandle
import ir.sadteam.loancalc.ui.debt.DebtScreen
import ir.sadteam.loancalc.ui.due.DueTabScreen
import ir.sadteam.loancalc.ui.haptics.rememberBuzz
import ir.sadteam.loancalc.ui.home.HomeScreen
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
import ir.sadteam.loancalc.ui.nav.NavSuggestionCard
import ir.sadteam.loancalc.ui.profile.ShortcutViewModel
import ir.sadteam.loancalc.ui.rating.RatePromptDialog
import ir.sadteam.loancalc.ui.rating.RatePromptViewModel
import ir.sadteam.loancalc.ui.security.AppLockViewModel
import ir.sadteam.loancalc.ui.security.LockScreen
import ir.sadteam.loancalc.ui.settings.SettingsScreen
import ir.sadteam.loancalc.ui.inbox.InboxScreen
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.LiveBackdrop
import ir.sadteam.loancalc.ui.theme.BackdropState
import ir.sadteam.loancalc.ui.theme.AppDisabledText
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryBorder
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.hardShadow
import ir.sadteam.loancalc.ui.theme.ColorTheme
import ir.sadteam.loancalc.ui.theme.LoanCalcTheme
import ir.sadteam.loancalc.ui.theme.LocalThemeReveal
import ir.sadteam.loancalc.ui.theme.Motion
import ir.sadteam.loancalc.ui.theme.ThemeMode
import ir.sadteam.loancalc.ui.theme.ThemeRevealHost
import ir.sadteam.loancalc.ui.theme.ThemeRevealState
import ir.sadteam.loancalc.ui.theme.ThemeViewModel
import ir.sadteam.loancalc.ui.update.AppUpdateViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// آیکون‌های نوار پایین: حالت عادی outline (مینیمال، مثل نسخه‌ی وب)، تب فعال پُر (filled).
//
// بازطراحیِ تب‌بندی (فازِ اولِ بازسازیِ جامع، رجوع کن به CLAUDE.md): قبلاً ۴ تبِ جدا (وام بانکی/
// محاسبه‌گر/سود سپرده/وام‌های من) + حسابداری بودن. الان زیرِ یه تبِ واحدِ «وام» ادغام شدن (رجوع کن
// به [LoanTab]/[LoanSubTab])، «چک» که قبلاً فقط زیرمجموعه‌ی تبِ وام بانکی/تنظیمات بود ترفیع گرفته
// به تبِ مستقل، و دو تبِ کاملاً جدید («خانه»، «سررسید») اضافه شدن.
//
// دورِ دومِ بازطراحی (خواسته‌ی صریحِ کاربر: «تب‌های پایین دقیقاً مثل اون برنامه [رفرنس] باشه») - نوارِ
// پایین دیگه «وام»/«چک»/«حسابداری» نداره؛ به‌جاش «دارایی»/«گزارش»/«بودجه» (دقیقاً هم‌الگو با
// رفرنس). «وام» و «چک» دیگه تبِ بالانوارِ پایین نیستن - از تبِ «سررسید» (میان‌برهای «قسط و وام»/
// «چک») به‌عنوانِ یه صفحه‌ی پوش‌شده (با دکمه‌ی برگشتِ خودشون - رجوع کن به [LOAN_ROUTE]/
// [CHEQUE_ROUTE]) باز می‌شن. «حسابداری»ِ قبلی سه‌جا شد: لیستِ حساب‌ها/تراکنش‌ها → «دارایی»
// (AssetsScreen)، گزارش‌گیری → تبِ مستقلِ «گزارش» (ReportScreen)، بودجه‌بندی → تبِ مستقلِ «بودجه»
// (BudgetScreen). «پرداختِ تکراری» و «دسته‌بندی‌ها» هر دو رفتن زیرِ «بودجه» (رجوع کن به CLAUDE.md،
// «تصمیمِ کاشیِ پرداختِ تکراری» - اول رفته بود زیرِ «سررسید»، بعداً از اونجا به اینجا منتقل شد).
private enum class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    HOME("home", "خانه", Icons.Outlined.Home, Icons.Filled.Home),
    ASSETS("assets", "دارایی", Icons.Outlined.AccountBalanceWallet, Icons.Filled.AccountBalanceWallet),
    REPORT("report", "گزارش", Icons.Outlined.BarChart, Icons.Filled.BarChart),
    BUDGET("budget", "بودجه", Icons.Outlined.Savings, Icons.Filled.Savings),
    DUE("due", "سررسید", Icons.Outlined.EventNote, Icons.Filled.EventNote),
}

/** «وام» و «چک» دیگه تبِ نوارِ پایین نیستن (رجوع کن به کامنتِ بالای [BottomTab]) - این دو route
 * مستقیم به‌عنوانِ رشته تعریف شدن (نه عضوِ enumِ BottomTab) چون فقط از تبِ «سررسید»/«خانه» به‌عنوانِ
 * صفحه‌ی پوش‌شده باز می‌شن، تو نوارِ پایین رندر نمی‌شن. */
/**
 * هشت میان‌برِ پیش‌فرضِ **کشوی میان‌بُر** - عیناً همون‌هایی که کارتِ `31b` نشون می‌ده.
 * ترتیبِ اینجا فقط پیش‌فرضه؛ ترتیبِ واقعی از [ShortcutViewModel] میاد.
 */
private val defaultShortcuts = listOf(
    Shortcut("expense", "ثبتِ خرج", Icons.Outlined.Payments, "home", locked = true),
    Shortcut("transfer", "انتقال", Icons.Outlined.SwapHoriz, "assets"),
    Shortcut("report", "گزارشِ ماه", Icons.Outlined.BarChart, "report"),
    Shortcut("cheque", "چک‌ها", Icons.Outlined.Description, "cheque"),
    Shortcut("gold", "طلا", Icons.Outlined.AccountBalanceWallet, "assets"),
    Shortcut("budget", "بودجه", Icons.Outlined.Savings, "budget"),
    Shortcut("due", "سررسید", Icons.Outlined.EventNote, "due"),
    Shortcut("debt", "دنگ", Icons.Outlined.Groups, "due"),
)

/**
 * **مخزنِ مقصدهای کشو** - ورودیِ حالتِ ویرایشِ فریمِ `53a`. هشتِ بالا انتخابِ پیش‌فرض‌اند،
 * این فهرست همه‌ی چیزهایی است که کاربر می‌تواند بینشان عوض کند.
 *
 * ⚠️ **فاصله‌ی آگاهانه با طرح**، عیناً همان دلیلِ [NavDestination]: فریم از «۱۴ مقصد» حرف
 * می‌زند و «پیام‌ها»/«هدفِ پس‌انداز»/«تقویم»/«سکه‌ها» را هم می‌شمرد؛ آن‌ها در `NavHost`ِ
 * فعلی مقصدِ ناوبری **نیستند**. با اضافه‌شدنِ هر route، فقط یک ردیف این‌جا اضافه می‌شود.
 */
private val allShortcutPool = defaultShortcuts + listOf(
    Shortcut("loan", "وام", Icons.Outlined.Payments, LOAN_ROUTE),
    Shortcut("home", "خانه", Icons.Outlined.Home, "home"),
    Shortcut("assets", "دارایی", Icons.Outlined.AccountBalanceWallet, "assets"),
)

private const val LOAN_ROUTE = "loan"
private const val CHEQUE_ROUTE = "cheque"
private const val DEBT_ROUTE = "debt"

/** زیرصفحه‌های داخلِ تبِ «وام» - جایگزینِ ۴ تبِ جداگانه‌ی قبلی. رجوع کن به [LoanTab]. */
private enum class LoanSubTab(val label: String) {
    // سه تب، طبقِ فریمِ `27a`. تبِ چهارمِ «بانکی» **حذف نشد، ادغام شد**: تصمیمِ کلاد دیزاین
    // (۹ شهریور) این بود که با «محاسبه‌گر» یکی بشه و به‌جاش داخلِ همون تب یه سگمنتِ دوحالته
    // بیاد - رجوع کن به [CalculatorHostScreen] و فریمِ `27f`.
    MY_LOANS("وام‌های من"),
    DEPOSIT("سپرده"),
    CALCULATOR("محاسبه‌گر"),
}

// ترتیب/محتوای کاملِ تورِ راهنمای اولین ورود (AppTourOverlay) - رجوع کن به همون کامپوننت پایین‌تر
// برای جزئیاتِ فنیِ اسپاتلایت. هر مرحله یه المانِ *واقعیِ* رو صفحه رو هدف می‌گیره (مختصاتش تو
// tourBounds تو LoanCalcApp اندازه‌گیری می‌شه) - نه یه توضیحِ مستقلِ بدونِ هدف.
private enum class TourTarget(val title: String, val hint: String) {
    // ⚠️ قدم‌های «حالت خصوصی»/«تمِ روشن-تاریک»/«تنظیمات» حذف شدن: بعدِ بازطراحیِ Duolingo دیگه
    // نوارِ بالای ثابتی وجود نداره که این سه آیکون توش بشینن (هر تب هدرِ خودشو داره، تم رفته
    // داخلِ تنظیمات، و درِ ورودیِ تنظیمات آدمکِ هدرِ خانه‌ست). قدمِ توری که المانِ واقعی نداره
    // فقط یه اسپاتلایتِ خالی می‌شه.
    ASSETS(
        "دارایی",
        "حساب‌ها و تراکنش‌هات رو اینجا ثبت و پیگیری کن.",
    ),
    REPORT(
        "گزارش",
        "با فیلترِ حساب و بازه‌ی دلخواه، گزارشِ دخل‌وخرجت رو ببین و PDF/اکسل بگیر.",
    ),
    BUDGET(
        "بودجه",
        "برای هر دسته‌بندی یه سقفِ ماهانه بذار تا هزینه‌هات دستت باشه - آخرین قدمِ تور!",
    ),
}

// نگاشتِ TourTarget های تبی → BottomTab واقعی (برای این‌که AppTourOverlay بدونه با کدوم تب باید
// هماهنگ بشه - هم گرفتنِ مختصات از BottomNavItem هم ناوبریِ خودکار). «وام»/«چک» دیگه تبِ نوارِ
// پایین نیستن (رجوع کن به کامنتِ بالای BottomTab)، پس دیگه قدمِ تورِ مستقل ندارن.
private fun TourTarget.asBottomTab(): BottomTab? = when (this) {
    TourTarget.ASSETS -> BottomTab.ASSETS
    TourTarget.REPORT -> BottomTab.REPORT
    TourTarget.BUDGET -> BottomTab.BUDGET
    else -> null
}

/** مختصاتِ آیکونِ نوارِ پایینِ یه تب رو تویِ [tourBounds] برای قدمِ تورِ مربوط به همون تب ثبت
 * می‌کنه. */
private fun registerTabTourBounds(
    route: String,
    rect: Rect,
    tourBounds: MutableMap<TourTarget, Rect>,
) {
    // ⚠️ از **route** کلید می‌گیره نه از `BottomTab`، چون بعدِ بخشِ ۴۱ نوار دیگه لزوماً همون پنج
    // تبِ ثابت نیست؛ تبی که کاربر برداشته باشه اصلاً رندر نمی‌شه و مختصاتش ثبت نمی‌شه (تور هم
    // برای همون قدم به‌درستی چیزی اسپاتلایت نمی‌کنه، به‌جای اینکه یه مستطیلِ کهنه نشون بده).
    when (route) {
        BottomTab.ASSETS.route -> tourBounds[TourTarget.ASSETS] = rect
        BottomTab.REPORT.route -> tourBounds[TourTarget.REPORT] = rect
        BottomTab.BUDGET.route -> tourBounds[TourTarget.BUDGET] = rect
    }
}

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
        if (loanId > 0) deepLinkTarget.setLoanId(loanId)
        // اعلانِ چک تا امروز هیچ مقصدی نداشت (نه باز می‌شد نه بسته) - رجوع کن به
        // PendingChequeDeepLink. جدا از وام نگه داشته شده چون اعلانِ گروه‌شده می‌تواند
        // هم‌زمان یکی از هر کدام داشته باشد.
        val chequeId = intent?.getLongExtra(EXTRA_OPEN_CHEQUE_ID, -1L) ?: -1L
        if (chequeId > 0) pendingChequeDeepLink.setChequeId(chequeId)
        // اعلانِ تراکنشِ خودکار (فریمِ `50b`) - هم تپ روی بدنه، هم دکمه‌ی دسته.
        val txId = intent?.getLongExtra(EXTRA_OPEN_TX_ID, -1L) ?: -1L
        if (txId > 0) {
            pendingTxDeepLink.set(txId, intent?.getBooleanExtra(EXTRA_PICK_CATEGORY, false) == true)
        }
        // تپ روی ویجت → تبِ سررسید (قاعده‌ی ۳ی فریمِ `57c`: ویجت درباره‌ی سررسید حرف
        // می‌زند، پس بردنِ کاربر به خانه یک قدم عقب است). پلِ تازه لازم نبود - همان
        // میان‌برِ «سررسید» دقیقاً همین کار را می‌کند.
        if (intent?.getBooleanExtra(EXTRA_OPEN_DUE_TAB, false) == true) {
            deepLinkTarget.setShortcut(DeepLinkTarget.SHORTCUT_DUE)
        }
        // میان‌برِ فشارِ طولانی رو آیکونِ اپ - رجوع کن به res/xml/shortcuts.xml
        intent?.getStringExtra("jibak_shortcut")?.let { deepLinkTarget.setShortcut(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleDeepLinkIntent(intent)
        subscriptionManager = SubscriptionManager(this)
        subscriptionManager.connect { }
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()
            val fontScale by themeViewModel.fontScale.collectAsState()
            val colorTheme by themeViewModel.colorTheme.collectAsState()
            val catalogTheme by themeViewModel.catalogTheme.collectAsState()
            val baseDensity = LocalDensity.current
            // عمداً بیرونِ LoanCalcTheme: این state باید از تعویضِ خودِ تم جونِ سالم به‌در ببره،
            // چون دقیقاً وسطِ همون تعویض داره کار می‌کنه (رجوع کن به ThemeReveal.kt).
            val themeReveal = remember { ThemeRevealState() }
            LoanCalcTheme(themeMode = themeMode, colorTheme = colorTheme, catalogTheme = catalogTheme) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Rtl,
                    LocalSubscriptionManager provides subscriptionManager,
                    LocalThemeReveal provides themeReveal,
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
    if (!permissionsOk && !permissionGateSkipped) {
        PermissionGateScreen(
            onAllGranted = { permissionsOk = true },
            onSkip = { authViewModel.skipPermissionGate() },
        )
        return
    }

    // این‌جا `onboardingDone`/`gateState` قطعاً non-nullن (گیتِ اسپلشِ بالا تضمینش می‌کنه)، پس دیگه
    // شاخه‌ی «هنوز لود نشده» با صفحه‌ی خالیِ سفید لازم نیست.
    if (onboardingDone != true) {
        OnboardingFlow(onFinished = { authViewModel.markOnboardingDone() })
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
private fun LoanCalcApp(
    themeViewModel: ThemeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    privacyModeViewModel: PrivacyModeViewModel = hiltViewModel(),
    appUpdateViewModel: AppUpdateViewModel = hiltViewModel(),
    deepLinkViewModel: DeepLinkViewModel = hiltViewModel(),
) {
    var showSettings by remember { mutableStateOf(false) }
    var showInbox by remember { mutableStateOf(false) }
    val updateUrl by appUpdateViewModel.updateUrl.collectAsState()
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

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: BottomTab.HOME.route

    // **شخصی‌سازیِ نوارِ پایین** (بخشِ ۴۱). عمداً از کشوی میان‌بُرِ بالا **جداست**: «یک فهرست،
    // دو نمایش» - مخزنِ مقصدها مشترکه ولی ترتیبِ ذخیره‌شده نه، پس تغییرِ نوار کشو رو دست نمی‌زنه.
    val navSlotsViewModel: NavSlotsViewModel = hiltViewModel()
    val navSlots by navSlotsViewModel.slots.collectAsState()
    val navCustomized by navSlotsViewModel.customized.collectAsState()
    val navSuggestion by navSlotsViewModel.suggestion.collectAsState()
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
    var shortcutDrawerOpen by remember { mutableStateOf(false) }
    val shortcuts = remember(savedShortcutOrder, savedShortcutSelection) {
        val byId = allShortcutPool.associateBy { it.id }
        val selected = savedShortcutSelection.mapNotNull { byId[it] }.ifEmpty { defaultShortcuts }
        // ترتیبِ ذخیره‌شده اول میاد؛ شناسه‌ی ناشناخته نادیده و میان‌برِ تازه ته لیست اضافه می‌شه.
        val ordered = savedShortcutOrder.mapNotNull { id -> selected.firstOrNull { it.id == id } }
        ordered + selected.filterNot { it.id in savedShortcutOrder }
    }

    // «وام‌های من» دیگه تبِ جداگانه‌ی خودش نیست، یه زیرصفحه‌ی داخلِ تبِ «وام»ه (رجوع کن به
    // [LoanTab]/[LoanSubTab]) - این state بهش می‌گه کدوم زیرصفحه رو باز کنه، مستقل از اینکه کاربر
    // خودش دستی رو کدوم زیرصفحه بوده.
    var requestedLoanSubTab by remember { mutableStateOf<LoanSubTab?>(null) }

    // زدنِ نوتیفیکیشنِ یادآوریِ قسط (مورد ۵) - رجوع کن به کامنتِ DeepLinkTarget. اگه رو تبِ «وام»
    // نیستیم، اول باید بریم اونجا و زیرصفحه‌ی «وام‌های من» رو باز کنیم؛ خودِ بازکردنِ وامِ خاص تو
    // MyLoansScreen انجام می‌شه (پارامترِ deepLinkLoanId پایین‌تر).
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
    LaunchedEffect(pendingTx) {
        if (pendingTx != null) {
            showInbox = true
            deepLinkViewModel.consumeTx()
        }
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
    if (showRatePrompt) {
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
    var bottomBarVisible by remember { mutableStateOf(true) }
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
    CompositionLocalProvider(
        LocalPrivacyMode provides privacyMode,
        LocalReducedMotion provides reducedMotion,
    ) {
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
        val backdrop = BackdropState.active
        Box(modifier = Modifier.fillMaxSize().background(AppBg)) {
            if (backdrop != null) {
                LiveBackdrop(backdrop = backdrop, modifier = Modifier.fillMaxSize())
            }
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
                AnimatedVisibility(
                    visible = bottomBarVisible,
                    enter = slideInVertically(Motion.standard()) { it },
                    exit = slideOutVertically(Motion.standard()) { it },
                ) {
                    // ⚠️ **بازطراحیِ سبکِ «جیبک»**: نوارِ «شناورِ شیشه‌ای»ِ دورِ قبل (کارتِ گردگوشه‌ی
                    // جدا از لبه با گرادیانِ نوری و سایه‌ی تارِ سبز + نشانگرِ قرصیِ لغزنده) کاملاً
                    // حذف شد. طبقِ بخشِ «۹ · نویگیشنِ پایین»ِ سیستمِ طراحی نوار حالا:
                    // - به لبه‌ی پایین **چسبیده**، سطحِ **مات** (`#FFFFFF` / `#1B2530`)
                    // - فقط یه **خطِ بالایی ۲ پیکسلی** داره (`#EEF3F0` / `#232E38`) - نه سایه، نه گرادیان
                    // - **هیچ نشانگرِ قرصی/لغزنده‌ای نداره** - تبِ فعال فقط با رنگ، ضخامتِ آیکون و
                    //   وزنِ ۹۰۰ِ برچسب مشخص می‌شه
                    //
                    // ⚠️ توکن‌های رنگ `@Composable`ان و داخلِ `drawBehind` (که `DrawScope`ه) صدا
                    // زده نمی‌شن - قاعده‌ی ماندگارِ پروژه. برای همین اینجا تو یه `val` محلی خونده می‌شه.
                    val navTopLine = AppLineRow
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppSurface)
                            .drawBehind {
                                // خطِ بالاییِ ۲ پیکسلی. `drawBehind` (نه `border`) چون فقط یه ضلعه.
                                val h = 2.dp.toPx()
                                drawRect(
                                    color = navTopLine,
                                    topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                                    size = androidx.compose.ui.geometry.Size(size.width, h),
                                )
                            }
                            .navigationBarsPadding(),
                    ) {
                        // دستگیره‌ی کشوی میان‌بُر - نوارِ ۲۶ پیکسلیِ بالای تب‌ها. کشیدنِ به بالا
                        // یا تپِ ساده بازش می‌کنه (قاعده‌ی `31c`).
                        ShortcutDrawerHandle(onOpen = { shortcutDrawerOpen = true })
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 6.dp, end = 6.dp, bottom = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                        // **بخشِ ۴۱**: دیگه `BottomTab.entries` نیست - چیدمان از [NavSlotsViewModel]
                        // میاد. اسلاتِ ۰ همیشه «خانه»ست (قفلِ `41c`، تو `NavDestination.sanitize`).
                        navSlots.forEach { dest ->
                            BottomNavItem(
                                dest = dest,
                                selected = currentRoute == dest.route,
                                onPositioned = { rect -> registerTabTourBounds(dest.route, rect, tourBounds) },
                                onLongClick = { navEditorOpen = true },
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
                    }
                }
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
                composable(BottomTab.HOME.route) {
                    key(tabResetKeys[BottomTab.HOME] ?: 0) {
                        // route به‌عنوانِ رشته پاس داده می‌شه (نه خودِ enumِ BottomTab) چون
                        // BottomTab تویِ همین فایلِ MainActivity.kt خصوصیه و HomeScreen تو یه
                        // فایلِ جدا (ui/home/HomeScreen.kt) زندگی می‌کنه.
                        HomeScreen(
                            onNavigateToRoute = ::navigateTo,
                            onOpenSettings = { showSettings = true },
                            onOpenInbox = { showInbox = true },
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
                                            navEditorOpen = true
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
                    key(tabResetKeys[BottomTab.REPORT] ?: 0) { ReportTabScreen() }
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
                InboxScreen(onBack = { showInbox = false })
            }
        }

        // کشوی میان‌بُر رو کلِ صفحه می‌شینه (پرده‌ی تیره + خودِ کشو) ولی **زیرِ** نوارِ پایین
        // نمی‌ره - طرح صریحاً می‌خواد نوار همیشه دیده بشه.
        ShortcutDrawer(
            shortcuts = shortcuts,
            visible = shortcutDrawerOpen,
            onDismiss = { shortcutDrawerOpen = false },
            onOpenRoute = { route -> navigateTo(route) },
            onOrderChanged = { ids -> shortcutViewModel.save(ids) },
            allShortcuts = allShortcutPool,
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
        AnimatedVisibility(
            visible = updateUrl != null && tourSeen != false,
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
                        "نسخه‌ی جدیدِ برنامه‌ی جیبک موجوده",
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
        // (tourSeen بالاتر جمع‌آوری شده، برای گیت‌کردنِ بنرِ آپدیت هم استفاده می‌شه)
        if (tourSeen == false) {
            AppTourOverlay(
                steps = TourTarget.entries.toList(),
                bounds = tourBounds,
                onStepChanged = { target ->
                    showSettings = false
                    // قدم‌هایی که رو یه تبِ خاص زندگی می‌کنن خودکار به همون تب می‌رن - وگرنه
                    // المانِ هدف اصلاً رندر/قابل‌اندازه‌گیری نیست.
                    target.asBottomTab()?.let { tab ->
                        if (currentRoute != tab.route) navigateTo(tab.route)
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

/**
 * میزبانِ تبِ «محاسبه‌گر» - همون [BankLoanTab]ِ قبلی، ولی حالا [CalculatorHostScreen] رو
 * به‌جای [BankLoanScreen] تو حالتِ فرم می‌ذاره تا سگمنتِ دوحالته‌ی `27f` بالاش بشینه.
 * منطقِ فرم↔نتیجه و حفظِ حالتِ فرم عیناً همون قبلیه.
 */
@Composable
private fun CalculatorHostTab(onAddManualLoan: () -> Unit = {}) {
    BankLoanTab(useCalculatorHost = true, onAddManualLoan = onAddManualLoan)
}

@Composable
private fun BankLoanTab(useCalculatorHost: Boolean = false, onAddManualLoan: () -> Unit = {}) {
    var loanOutcome by remember { mutableStateOf<BankLoanOutcome?>(null) }
    // نگه‌دارنده‌ی حالتِ ذخیره‌پذیر (SaveableStateHolder): وقتی loanOutcome پر می‌شه، BankLoanScreen
    // کاملاً از کامپوزیشن بیرون می‌ره (جایگزینِ ResultScreen می‌شه) - remember/rememberSaveableِ
    // معمولیِ توش با این کار پاک می‌شد (خواسته‌ی کاربر: «اگه اشتباه زده باشم باید از نو بزنم»).
    // با پیچوندنِ BankLoanScreen تو SaveableStateProvider با یه کلیدِ ثابت، حالتِ rememberSaveableِ
    // فیلدهاش (مبلغ/نرخ/ماه/تاریخ/بانکِ‌انتخابی) حتی بعدِ بیرون‌رفتن از کامپوزیشن حفظ می‌شه و با
    // برگشتن (دکمه‌ی «ویرایش» تو ResultScreen) دوباره برمی‌گرده - نه از صفر.
    val formStateHolder = rememberSaveableStateHolder()
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
            formStateHolder.SaveableStateProvider("bankLoanForm") {
                if (useCalculatorHost) {
                    CalculatorHostScreen(
                        onCalculated = { loanOutcome = it },
                        onAddManualLoan = onAddManualLoan,
                    )
                } else {
                    BankLoanScreen(onCalculated = { loanOutcome = it }, onAddManualLoan = onAddManualLoan)
                }
            }
        } else {
            ResultScreen(
                outcome = outcome,
                // قرصِ منبعِ `68`/`69b`: حالا خودِ `BankLoanScreen` می‌گوید نرخ از سرور آمده
                // («سرویسِ اعتباری») یا کاربر دستی زده («وامِ بانکی»).
                sourceLabel = outcome.rateSourceLabel,
                // ویرایش دیگه اینجا (BankLoanTab) مدیریت نمی‌شه - مورد ۴، ResultScreen خودش با
                // یه پنلِ اینلاین ویرایش می‌کنه، دیگه نیازی به onEdit/برگشتن به فرم نیست.
                // بعدِ ذخیره‌ی موفقِ وام، حالتِ ذخیره‌شده‌ی فرم (مبلغ/بانک/...) صریحاً پاک می‌شه -
                // وگرنه فرم برای وامِ *بعدی* هنوز اعدادِ وامِ قبلاً ذخیره‌شده رو نشون می‌داد (باگِ
                // گزارش‌شده‌ی کاربر: «ذخیره که می‌کنم بازم اعداد و بانک هستن»).
                onNewCalculation = {
                    formStateHolder.removeState("bankLoanForm")
                    loanOutcome = null
                },
            )
        }
    }
}

/**
 * تبِ ادغام‌شده‌ی «وام» - جایگزینِ ۴ تبِ جداگانه‌ی قبلی (وام بانکی/محاسبه‌گر/سود سپرده/وام‌های من).
 * یه انتخابگرِ افقیِ ساده بالای صفحه بینِ چهار زیرصفحه‌ی موجود سوییچ می‌کنه - خودِ صفحه‌ها
 * ([BankLoanTab]/[AffordScreen]/[DepositScreen]/[MyLoansScreen]) دست‌نخورده می‌مونن.
 * [requestedSubTab] برای ناوبریِ خارجی (تور/نوتیفیکیشنِ دیپ‌لینک) استفاده می‌شه - وقتی مقدارش عوض
 * می‌شه، زیرصفحه‌ی متناظر باز می‌شه.
 */
@Composable
private fun LoanTab(
    onBack: () -> Unit,
    requestedSubTab: LoanSubTab?,
    onManualAddFabPositioned: (Rect) -> Unit,
    onBottomBarVisibilityChanged: (Boolean) -> Unit,
    deepLinkLoanId: Long?,
    onDeepLinkConsumed: () -> Unit,
) {
    var subTab by remember { mutableStateOf(LoanSubTab.MY_LOANS) }
    // خواسته‌ی کاربر (۲۶ شهریور): دکمه‌ی «+»ِ «وام‌های من» دیگر مستقیم فرمِ دستی را باز نمی‌کند،
    // **محاسبه‌گر** را باز می‌کند؛ و «افزودنِ وامِ دستی» به تهِ همان محاسبه‌گر رفت. این پرچم
    // همان مسیرِ برگشت است: محاسبه‌گر می‌گوید «فرمِ دستی را باز کن» و تبِ «وام‌های من» بازش می‌کند.
    var openManualAdd by remember { mutableStateOf(false) }
    // **فریمِ ۷۶**: جست‌وجو و «تحلیل درآمد» از داخلِ فهرست به دو آیکونِ هم‌ردیفِ عنوان آمدند،
    // پس حالتشان این‌جاست و به [MyLoansScreen] پاس داده می‌شود. فقط در زیرتبِ «وام‌های من»
    // معنی دارند.
    var searchOpen by remember { mutableStateOf(false) }
    LaunchedEffect(requestedSubTab) {
        requestedSubTab?.let { subTab = it }
    }
    // نوارِ پایینِ تب‌ها (که فقط MyLoansScreen موقعِ اسکرول جمعش می‌کنه) باید موقعِ سوییچ به هر
    // زیرصفحه‌ی دیگه‌ای دوباره نمایان بشه - چون از دیدِ ناوبریِ بیرونی، این سوییچ اصلاً route عوض
    // نمی‌کنه که خودش این ریست رو انجام بده.
    LaunchedEffect(subTab) {
        if (subTab != LoanSubTab.MY_LOANS) onBottomBarVisibilityChanged(true)
    }
    // برگشتن از یه زیرصفحه‌ی غیرِ«بانکی» به «بانکی» (زیرصفحه‌ی پیش‌فرض) - قبل از رسیدن به
    // BackHandlerِ بیرونیِ LoanCalcApp (که دیگه معنیش برگشتن به «خانه»ست). زیرصفحه‌های داخلیِ
    // خودِ هر اسکرین (مثلاً جزئیاتِ وام تو MyLoansScreen) اولویتِ بالاتری دارن چون دیرتر رجیستر می‌شن.
    BackHandler(enabled = subTab != LoanSubTab.MY_LOANS) { subTab = LoanSubTab.MY_LOANS }

    Column(modifier = Modifier.fillMaxSize()) {
        // «وام» دیگه تبِ نوارِ پایین نیست (رجوع کن به کامنتِ بالای BottomTab تو این فایل) - چون از
        // «سررسید»/«خانه» به‌عنوانِ صفحه‌ی پوش‌شده باز می‌شه، یه دکمه‌ی برگشتِ واقعی لازم داره.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
            }
            // فریمِ `27a`: عنوانِ ۱۸ با وزنِ ۹۰۰، و دکمه‌ی افزودن سمتِ مقابل تو قابِ ۳۲ی سبز.
            Text(
                "وام",
                color = AppText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f),
            )
            // فریمِ ۷۶a: دو آیکونِ ۳۲ی هم‌ردیفِ عنوان - **صفر پیکسل ارتفاعِ تازه**.
            // ⚠️ انحراف از بندِ ۵ فریمِ `76c`: دکمه‌ی بازگشت **می‌مانَد**. طراح فرض کرده
            // «وام» تبِ سطحِ اول است و نوارِ پایین جای برگشتن، ولی در این برنامه وام
            // **تبِ نوارِ پایین نیست** - صفحه‌ای پوش‌شده از «خانه»/«سررسید» است، پس
            // برداشتنِ دکمه تنها راهِ برگشت را به دکمه‌ی سخت‌افزاری محدود می‌کرد.
            // چون در همان ردیف است، ارتفاعی هم اضافه نمی‌کند.
            if (subTab == LoanSubTab.MY_LOANS) {
                LoanHeaderIcon(
                    icon = Icons.Filled.Search,
                    label = "جست‌وجو در وام‌ها",
                    active = searchOpen,
                    onClick = { searchOpen = !searchOpen },
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // فریمِ `27a`: تبِ فعال یه **قرصِ سبزِ توپر با سایه‌ی سخت** و متنِ سفیده؛ بقیه فقط
            // متنِ خاکستریِ بی‌زمینه‌ان. (نسخه‌ی قبلی هر چهارتا رو یه Surfaceِ کم‌آلفا می‌کرد.)
            LoanSubTab.entries.forEach { entry ->
                val selected = entry == subTab
                val shape = RoundedCornerShape(999.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (selected) {
                                Modifier
                                    .hardShadow(AppPrimaryDim, offsetY = 3.dp, cornerRadius = 999.dp)
                                    .clip(shape)
                                    .background(AppPrimary)
                            } else {
                                Modifier.clip(shape)
                            },
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { subTab = entry }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        entry.label,
                        color = if (selected) Color.White else AppMuted,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            when (subTab) {
                LoanSubTab.CALCULATOR -> CalculatorHostTab(
                    onAddManualLoan = {
                        openManualAdd = true
                        subTab = LoanSubTab.MY_LOANS
                    },
                )
                LoanSubTab.DEPOSIT -> DepositScreen()
                LoanSubTab.MY_LOANS -> MyLoansScreen(
                    searchOpen = searchOpen,
                    onOpenCalculator = { subTab = LoanSubTab.CALCULATOR },
                    openManualAddSignal = openManualAdd,
                    onManualAddSignalConsumed = { openManualAdd = false },
                    onManualAddFabPositioned = onManualAddFabPositioned,
                    onBottomBarVisibilityChanged = onBottomBarVisibilityChanged,
                    deepLinkLoanId = deepLinkLoanId,
                    onDeepLinkConsumed = onDeepLinkConsumed,
                )
            }
        }
    }
}

/**
 * آیکونِ ۳۲یِ هم‌ردیفِ عنوانِ تبِ وام - فریمِ `76a`.
 *
 * هدفِ لمسی ۴۴dp است ولی **قاب** ۳۲ - همان الگوی بندِ ۸ سیستمِ طراحی: فضای لمسی بزرگ‌تر
 * از فضای دیده‌شده. حالتِ فعال قرصِ سبز می‌گیرد تا کاربر بداند فیلد/کارتِ پایین مالِ
 * کدام دکمه است.
 */
@Composable
private fun LoanHeaderIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(11.dp)
    Box(
        modifier = Modifier
            .size(44.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(shape)
                .background(if (active) AppPrimaryPill else AppSurface)
                .border(1.5.dp, if (active) AppPrimaryBorder else AppLineRow, shape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (active) AppPrimaryInk else AppMuted,
                modifier = Modifier.size(15.dp),
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun RowScope.BottomNavItem(
    dest: NavDestination,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onPositioned: (Rect) -> Unit = {},
) {
    // **بازطراحیِ سبکِ «جیبک»** - مقادیر مو‌به‌مو از کارتِ `15a`ی فایلِ طراحی (نه از حدس):
    //   تبِ فعال    → قرصِ #E9F7EF پشتِ آیکون (۴۲×۲۸، گوشه‌ی ۱۱) · آیکونِ ۱۸ سبز · برچسبِ ۹٫۵/۹۰۰ سبز
    //   تبِ غیرفعال → بدونِ قرص · آیکونِ ۱۸ خاکستری · برچسبِ ۹٫۵/۷۰۰ خاکستری
    //   فاصله‌ی آیکون تا برچسب ۴ · عرضِ هر تب ۵۲ · پدینگِ نوار ۹×۶
    //
    // ⚠️ قرصِ پشتِ آیکون یه دورِ اشتباهاً حذف شده بود (فرضِ غلط: «طرح نشانگر نداره»). خودِ طرح
    // داره - فقط به‌جای نشانگرِ **لغزنده**ی دورِ قبل، یه قرصِ ثابتِ پشتِ آیکونِ همون تبه.
    val ink = if (selected) AppPrimaryInk else AppLabel
    // پورت easing فنری تب فعال تو وب (cubic-bezier(.34,1.56,.64,1) رو .nav-item .ic svg).
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "navIconScale",
    )
    val buzz = rememberBuzz()
    Column(
        modifier = Modifier
            .weight(1f)
            // **بخشِ ۴۱**: فشارِ طولانی رو هر خانه‌ی نوار، ویرایشگرِ چیدمان رو باز می‌کنه.
            // ⚠️ این تو سندِ طراح **نیست** - طرح فقط دکمه‌ی «خودم می‌چینم»ِ کارتِ `41a` رو
            // به‌عنوانِ درِ ورودی داره، ولی اون کارت تا ۲۱ روز داده جمع نشه اصلاً نمیاد. بدونِ
            // این، قابلیت تو سه هفته‌ی اولِ نصب هیچ راهِ دسترسی‌ای نداشت.
            .combinedClickable(
                onClick = { buzz(); onClick() },
                onLongClick = { buzz(); onLongClick() },
            )
            .padding(vertical = 2.dp)
            // مختصاتِ ریشه‌ی خودِ تب رو گزارش می‌ده - برای AppTourOverlay که دقیقاً همین محدوده رو
            // نورانی می‌کنه، نه یه مختصاتِ حدسی/هاردکد.
            .onGloballyPositioned { coordinates -> onPositioned(coordinates.boundsInRoot()) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(width = 42.dp, height = 28.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(if (selected) AppPrimaryPill else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            // **بخشِ ۴۱**: وقتی کاربر مقصدِ این خانه رو عوض می‌کنه، آیکون با «۱۸۰ms محو +
            // scale .9→1» جا عوض می‌کنه - **نه جابه‌جاییِ افقی** و نه لرزشِ کلِ نوار (`41c`).
            // برچسبِ متن عمداً بی‌انیمیشنه، پس بیرونِ این بلوکه.
            AnimatedContent(
                targetState = dest,
                transitionSpec = {
                    (fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.9f))
                        .togetherWith(fadeOut(tween(180)))
                },
                label = "navIconSwap",
            ) { current ->
                Icon(
                    if (selected) current.selectedIcon else current.icon,
                    contentDescription = current.label,
                    tint = ink,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer { scaleX = iconScale; scaleY = iconScale },
                )
            }
        }
        Text(
            dest.label,
            color = ink,
            fontSize = 9.5.sp,
            fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
