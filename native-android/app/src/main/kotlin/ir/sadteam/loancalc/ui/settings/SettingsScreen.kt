package ir.sadteam.loancalc.ui.settings

import ir.sadteam.loancalc.BuildConfig
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.auth.GateState
import ir.sadteam.loancalc.ui.auth.LoginScreen
import ir.sadteam.loancalc.ui.account.AccountsScreen
import ir.sadteam.loancalc.ui.calendar.FinancialCalendarScreen
import ir.sadteam.loancalc.ui.cheque.ChequeScreen
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.InAppBannerState
import ir.sadteam.loancalc.ui.components.LottieSpinner
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.GoldSheenBox
import ir.sadteam.loancalc.ui.components.PulseGlowBox
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.haptics.HapticsViewModel
import ir.sadteam.loancalc.ui.history.CalculationHistoryScreen
import ir.sadteam.loancalc.ui.security.AppLockViewModel
import ir.sadteam.loancalc.ui.security.biometricAvailable
import ir.sadteam.loancalc.ui.stats.StatsScreen
import ir.sadteam.loancalc.ui.subscription.SubscriptionScreen
import ir.sadteam.loancalc.ui.theme.Motion
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.LocalThemeReveal
import ir.sadteam.loancalc.ui.theme.ThemeMode
import ir.sadteam.loancalc.ui.theme.ThemeViewModel
import kotlinx.coroutines.launch

private val fontSizeOptions = listOf(0.9f to "کوچک", 1f to "متوسط", 1.15f to "بزرگ")
private val themeModeOptions = listOf(ThemeMode.LIGHT to "روشن", ThemeMode.DARK to "تاریک")

/** پورت ساده‌شده‌ی view-settings تو www/index.html - کارت حساب (accountCard) + خروج/ورود، اندازه
 * فونت (fontSizeChips)، یادآوری سررسید (کاملاً native-only، وب هنوز نداره - رجوع کن به
 * notifications/)، درباره‌برنامه/حریم‌خصوصی (toggleAbout/togglePrivacy، متن عینِ وب)، و صفحه‌ی
 * پشتیبانی (ایمیل/تلگرام/بله - پورت مفهومی از اپ رقیب VAMMAN؛ مقادیر SUPPORT_EMAIL/TELEGRAM/BALE
 * فعلاً placeholder ان، باید با اطلاعات واقعی جایگزین بشن). فرم «نظرات و مشکلات» عمداً پورت نشده -
 * رو خودِ وب هم صرفاً UI نمایشی/localStorage-فقط بود، هیچ‌وقت واقعاً کاربردی نبود (رجوع کن به کامنت
 * خودِ وب). «تنظیمات پیشرفته یادآوری» برخلافِ اون، حالا واقعاً پیاده شده - رجوع کن به
 * [ReminderSettingsScreen] (زمان‌بندی/صدا/ویبره‌ی سراسری) و `ReminderOverrideCard` تو
 * LoanDetailScreen/ChequeDetailScreen (سفارشی‌سازیِ اختصاصیِ هر وام/چک). */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
    notificationsViewModel: NotificationsViewModel = hiltViewModel(),
    appLockViewModel: AppLockViewModel = hiltViewModel(),
    autoBackupViewModel: AutoBackupViewModel = hiltViewModel(),
    hapticsViewModel: HapticsViewModel = hiltViewModel(),
    // برای تورِ راهنمای اولین ورود (AppTourOverlay تو MainActivity.kt، قدم‌های SETTINGS_CALENDAR/
    // SETTINGS_CHEQUE): وقتی non-null باشه، جستجوی همین پنل خودکار رو همین عنوان فیلتر می‌شه (دقیقاً
    // مثلِ تایپ‌کردنِ کاربر تو «جستجو تو تنظیمات») تا ردیفِ هدف بدونِ نیاز به اسکرول پیدا بشه.
    tourHighlightQuery: String? = null,
    onTourRowPositioned: (Rect) -> Unit = {},
) {
    var showLoginPrompt by remember { mutableStateOf(false) }
    var showFinancialCalendar by remember { mutableStateOf(false) }
    var showStats by remember { mutableStateOf(false) }
    var showCheque by remember { mutableStateOf(false) }
    var showAccounts by remember { mutableStateOf(false) }
    var showSubscription by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showReminderSettings by remember { mutableStateOf(false) }

    // پورت حس تعویض نرم بین حالت‌های مختلف پنل تنظیمات (اصلی/ورود/تقویم مالی/آمار/چک/حساب) - قبلاً
    // هرکدوم با یه return زودهنگام یهو جایگزین بقیه می‌شد؛ حالا با AnimatedContent (fade ظریف) عوض می‌شه.
    val screenKey = when {
        showLoginPrompt -> "login"
        showFinancialCalendar -> "calendar"
        showStats -> "stats"
        showCheque -> "cheque"
        showAccounts -> "accounts"
        showSubscription -> "subscription"
        showHistory -> "history"
        showReminderSettings -> "reminderSettings"
        else -> "main"
    }

    AnimatedContent(
        targetState = screenKey,
        transitionSpec = { Motion.contentEnter togetherWith Motion.contentExit },
        label = "settingsScreen",
    ) { key ->
        when (key) {
            // هر صفحه‌ی زیرمجموعه‌ی تنظیمات (که خودش پنلی با عرضِ ۸۵٪ صفحه‌ست، رجوع کن به
            // AnimatedVisibility تو MainActivity.kt) باید کاملاً فول‌اسکرین باشه، نه محدود به همون
            // عرض - برای همین همه‌شون تو FullScreenDialog (پایینِ همین فایل) نشون داده می‌شن که تو
            // ویندویی جدا و مستقل از محدودیتِ عرضِ والد رندر می‌شه.
            "login" -> FullScreenDialog(onDismissRequest = { showLoginPrompt = false }) {
                LoginScreen(onDismiss = { showLoginPrompt = false }, onLoginSuccess = { showLoginPrompt = false })
            }
            "calendar" -> FullScreenDialog(onDismissRequest = { showFinancialCalendar = false }) {
                FinancialCalendarScreen(onBack = { showFinancialCalendar = false })
            }
            "stats" -> FullScreenDialog(onDismissRequest = { showStats = false }) {
                StatsScreen(onBack = { showStats = false })
            }
            "cheque" -> FullScreenDialog(onDismissRequest = { showCheque = false }) {
                ChequeScreen(onBack = { showCheque = false })
            }
            "accounts" -> FullScreenDialog(onDismissRequest = { showAccounts = false }) {
                AccountsScreen(onBack = { showAccounts = false })
            }
            "subscription" -> FullScreenDialog(onDismissRequest = { showSubscription = false }) {
                SubscriptionScreen(
                    onBack = { showSubscription = false },
                    onSubscribed = { showSubscription = false },
                    onNeedsLogin = { showLoginPrompt = true },
                )
            }
            "history" -> FullScreenDialog(onDismissRequest = { showHistory = false }) {
                CalculationHistoryScreen(onBack = { showHistory = false })
            }
            "reminderSettings" -> FullScreenDialog(onDismissRequest = { showReminderSettings = false }) {
                ReminderSettingsScreen(onBack = { showReminderSettings = false })
            }
            else -> SettingsMainContent(
                onBack = onBack,
                authViewModel = authViewModel,
                themeViewModel = themeViewModel,
                notificationsViewModel = notificationsViewModel,
                appLockViewModel = appLockViewModel,
                autoBackupViewModel = autoBackupViewModel,
                hapticsViewModel = hapticsViewModel,
                onShowLoginPrompt = { showLoginPrompt = true },
                onShowFinancialCalendar = { showFinancialCalendar = true },
                onShowStats = { showStats = true },
                onShowCheque = { showCheque = true },
                onShowAccounts = { showAccounts = true },
                onShowSubscription = { showSubscription = true },
                onShowHistory = { showHistory = true },
                onShowReminderSettings = { showReminderSettings = true },
                tourHighlightQuery = tourHighlightQuery,
                onTourRowPositioned = onTourRowPositioned,
            )
        }
    }
}

/** پوششِ مشترکِ همه‌ی زیرصفحه‌های تنظیمات (ورود، تقویمِ مالی، آمار، تاریخچه، چک، حساب‌های بانکی،
 * اشتراک، یادآوریِ سررسید) رو یه `Dialog` نشون می‌ده - چون خودِ پنلِ تنظیمات یه `AnimatedVisibility`
 * با عرضِ ۸۵٪ صفحه‌ست (`MainActivity.kt`)، بدونِ این پوشش هر زیرصفحه‌ای هم به همون عرضِ ۸۵٪ محدود
 * می‌موند. **صرفِ `usePlatformDefaultWidth = false` کافی نبود** (رو تستِ واقعیِ گوشی، ویندوی دیالوگ
 * بازم دقیقاً همون عرضِ ۸۵٪ِ پنلِ پشتش درمیومد، چون اون پنل هنوز زیرِ دیالوگ کامپوز/رندر می‌شه) -
 * برای همین صریحاً `window.setLayout(MATCH_PARENT, MATCH_PARENT)` + `setDimAmount(0f)` صدا زده
 * می‌شه، و محتوا تو یه `Box` با پس‌زمینه‌ی `AppSurface` پیچیده می‌شه (چون بیشترِ این زیرصفحه‌ها خودشون
 * پس‌زمینه‌ی مستقل ندارن، قبلاً به پس‌زمینه‌ی همون پنلِ پشتشون تکیه می‌کردن). */
@Composable
private fun FullScreenDialog(onDismissRequest: () -> Unit, content: @Composable () -> Unit) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        // usePlatformDefaultWidth=false به‌تنهایی کافی نبود - رو گوشیِ واقعی، ویندوی دیالوگ به‌جای
        // کاملِ صفحه، دقیقاً به‌همون عرضِ ۸۵٪ِ پنلِ تنظیماتِ پشتش اندازه می‌گرفت (چون خودِ اون پنل -
        // AnimatedVisibility تو MainActivity.kt - همچنان زیرِ این دیالوگ کامپوز و رندر می‌مونه). این‌جا
        // صریحاً ویندو رو به MATCH_PARENT مجبور می‌کنیم و dimAmountِ پیش‌فرضِ دیالوگ رو صفر می‌کنیم.
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            dialogWindow?.setDimAmount(0f)
        }
        // هیچ‌کدوم از این زیرصفحه‌ها (به‌جز LoginScreen) پس‌زمینه‌ی خودشون رو ست نمی‌کنن - قبلاً چون
        // تویِ همون پنلِ AppSurface پشتشون رندر می‌شدن مشکلی نبود؛ حالا که تو ویندویِ جدای خودشونن،
        // بدونِ این Box پشتِ محتوا کاملاً شفاف می‌مونه و صفحه‌ی زیرین (تبِ فعلی + پنلِ نیمه‌محوِ قدیمی)
        // ازش رد می‌شه - این Box تضمین می‌کنه همیشه کاملاً کدر و تمام‌صفحه باشه.
        // ورودِ نرم: قبلاً این زیرصفحه‌ها یهو ظاهر می‌شدن (Dialog خودش هیچ انیمیشنی نداره). حالا
        // از پایین سُر می‌خورن بالا و محو ظاهر می‌شن. خروج عمداً انیمیشن نداره - وقتی Dialog بسته
        // می‌شه ویندوش بلافاصله از بین می‌ره و هر انیمیشنِ خروجی نصفه‌کاره قطع می‌شد.
        val appear = remember { MutableTransitionState(false).apply { targetState = true } }
        AnimatedVisibility(
            visibleState = appear,
            enter = fadeIn(tween(Motion.FADE_IN_MS)) + slideInVertically(Motion.offset()) { it / 10 },
        ) {
            Box(modifier = Modifier.fillMaxSize().background(AppSurface)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsMainContent(
    onBack: () -> Unit,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel,
    notificationsViewModel: NotificationsViewModel,
    appLockViewModel: AppLockViewModel,
    autoBackupViewModel: AutoBackupViewModel,
    hapticsViewModel: HapticsViewModel,
    onShowLoginPrompt: () -> Unit,
    onShowFinancialCalendar: () -> Unit,
    onShowStats: () -> Unit,
    onShowCheque: () -> Unit,
    onShowAccounts: () -> Unit,
    onShowSubscription: () -> Unit,
    onShowHistory: () -> Unit,
    onShowReminderSettings: () -> Unit,
    tourHighlightQuery: String? = null,
    onTourRowPositioned: (Rect) -> Unit = {},
) {
    val gateState by authViewModel.gateState.collectAsState()
    val phone by authViewModel.phone.collectAsState()
    val subscribed by authViewModel.subscribed.collectAsState()
    // بالا کشیده شد (قبلاً فقط داخلِ شاخه‌ی LOGGED_IN تعریف می‌شد) چون کارتِ «خرید اشتراک» پایین‌تر
    // هم بهش نیاز داره - رجوع کن به کامنتِ همون‌جا.
    val trialDaysLeft by authViewModel.trialDaysLeft.collectAsState()
    val subscribedUntil by authViewModel.subscribedUntil.collectAsState()
    val subscriptionTier by authViewModel.subscriptionTier.collectAsState()
    val fontScale by themeViewModel.fontScale.collectAsState()
    val notificationsEnabled by notificationsViewModel.enabled.collectAsState()
    val autoBackupEnabled by autoBackupViewModel.enabled.collectAsState()
    val lastAutoBackupAt by autoBackupViewModel.lastBackupAt.collectAsState()
    val vibrationEnabled by hapticsViewModel.enabled.collectAsState()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) notificationsViewModel.enable() }
    var searchQuery by remember { mutableStateOf(tourHighlightQuery ?: "") }
    // اگه تور یه قدمِ جدید رو پنلِ تنظیمات فعال کرد (مثلاً از SETTINGS_CALENDAR به SETTINGS_CHEQUE)،
    // جستجو رو خودکار با همون فیلترِ جدید هم‌قدم کن - دقیقاً همون کاری که خودِ کاربر با تایپ می‌کرد.
    LaunchedEffect(tourHighlightQuery) {
        if (tourHighlightQuery != null) searchQuery = tourHighlightQuery
    }
    fun matches(vararg titles: String) =
        searchQuery.isBlank() || titles.any { it.contains(searchQuery.trim()) }
    val banner = rememberInAppBanner()
    var showDeleteAccountConfirm by remember { mutableStateOf(false) }
    var deleteAccountInProgress by remember { mutableStateOf(false) }

    // به‌درخواست کاربر، منوی تنظیمات دیگه زیرِ نوار وضعیتِ گوشی گم نمی‌شه (statusBarsPadding) و تا
    // ته قابل‌اسکرول‌شدنه (verticalScroll + navigationBarsPadding پایین) - قبلاً هیچ‌کدوم نبود.
    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
    ) {
        // پورت پروفایل بالای پنل تنظیمات اپ رقیب (VAMMAN): آواتار + برچسب وضعیت («نسخه عادی»/«نسخه
        // اشتراکی»)؛ اگه مشترک باشیم، کادر دور آواتار طلایی و ضخیم‌تر می‌شه + یه گرادینت طلاییِ
        // ظریف پشتِ کل ردیف (خواسته‌ی کاربر «منو تنظیمات هم همون رنگ طلایی کمی قاطیش کن»).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (subscribed) {
                        Modifier.background(
                            Brush.horizontalGradient(listOf(AppAccent.copy(alpha = 0.28f), Color.Transparent)),
                        )
                    } else {
                        Modifier
                    },
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
            }
            Box(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(40.dp)
                    .background(AppSurface2, CircleShape)
                    .border(if (subscribed) 3.dp else 1.dp, if (subscribed) AppAccent else AppLine, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = if (subscribed) AppAccent else AppMuted,
                )
            }
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text("تنظیمات", color = AppText, fontSize = 16.sp)
                Text(
                    if (subscribed) "نسخه اشتراکی" else "نسخه عادی",
                    color = if (subscribed) AppAccent else AppMuted,
                    fontSize = 11.sp,
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
            AppCard {
                when (gateState) {
                    GateState.LOGGED_IN -> {
                        Text(toFa(phone ?: ""), color = AppText, fontSize = 15.sp)
                        // خواسته‌ی کاربر: زیرِ شماره فقط نوعِ اشتراک (یک‌ماهه/سه‌ماهه/شش‌ماهه/یک‌ساله)
                        // باشه، نه متنِ قبلیِ «مشترک — وام‌های من همگام‌سازی می‌شه». subscriptionTier
                        // فقط برای خریدِ واقعیِ زمان‌دار پر می‌شه (سرور، رجوع کن به
                        // SubscriptionRoutes.kt/PRODUCT_TIER_CODE)؛ برای دوره‌ی آزمایشی/اشتراکِ دستیِ
                        // دائمی یا خریدِ قدیمی‌تر (قبل از این فیلد) که تیرش معلوم نیست، فقط «مشترک»
                        // ساده نشون داده می‌شه - جزئیاتِ بیشترش رو بجِ آزمایشی/دائمیِ پایین‌تر می‌ده.
                        val tierLabel = when (subscriptionTier) {
                            "1m" -> "اشتراک یک‌ماهه"
                            "3m" -> "اشتراک سه‌ماهه"
                            "6m" -> "اشتراک شش‌ماهه"
                            "1y" -> "اشتراک یک‌ساله"
                            else -> null
                        }
                        Text(
                            when {
                                !subscribed -> "وارد حساب شدی"
                                tierLabel != null -> tierLabel
                                else -> "مشترک"
                            },
                            color = AppMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        // اگه دلیل «مشترک بودن» فعلاً فقط دوره‌ی آزمایشیِ ۷روزه‌ست (نه خرید واقعی)،
                        // مهلتِ باقی‌مونده رو نشون بده - وقتی تموم شد، خودکار (سمت سرور) به حالت
                        // عادی برمی‌گرده و همون پیامِ «باید اشتراک بگیری» جای این رو می‌گیره. عددش
                        // مستقیم از سرور میاد (با ساعتِ سرور حساب شده)، نه از یه محاسبه‌ی محلی روی
                        // ساعتِ گوشی - وگرنه دستکاری‌کردنِ تاریخِ گوشی می‌تونست این نمایش رو
                        // (نه خودِ دسترسیِ واقعی، که همیشه سمت سرور تصمیم‌گیری می‌شه) اشتباه نشون بده.
                        // باگِ رفع‌شده: قبلاً trialDaysLeft بی‌قید و شرط از سرور می‌اومد، پس حتی یه
                        // حسابِ **دائمیِ** دستی (subscribed=1 رو دیتابیس، برای پشتیبانی/تست) که
                        // تصادفاً تو ۷ روزِ اولِ ثبت‌نامش بود، همچنان همین بجِ «آزمایشی» گمراه‌کننده
                        // رو می‌گرفت. سرور الان فقط وقتی trialDaysLeft واقعاً دلیلِ مشترک‌بودنه
                        // (نه دستی/دائمی، نه خریدِ زمان‌دار) عدد می‌ده - رجوع کن به
                        // trialDaysLeftIfApplicable سمتِ سرور.
                        if (subscribed && trialDaysLeft != null && trialDaysLeft in 1..7) {
                            // GoldSheenBox: برقِ گذرای طلایی رو بجِ آزمایشی (رجوع کن به GoldSheen.kt).
                            GoldSheenBox(
                                modifier = Modifier.padding(top = 4.dp),
                                cornerRadius = 8.dp,
                            ) {
                                Text(
                                    "دوره‌ی آزمایشی رایگان: ${toFa(trialDaysLeft.toString())} روز مانده",
                                    color = AppText,
                                    fontSize = 11.sp,
                                    modifier = Modifier
                                        .background(AppAccent.copy(alpha = 0.24f), RoundedCornerShape(8.dp))
                                        .border(1.dp, AppAccent.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }
                        } else if (subscribed && subscribedUntil == null) {
                            // subscribed=true ولی نه از دوره‌ی آزمایشی نه از یه خریدِ زمان‌دار (که
                            // subscribedUntil رو پر می‌کرد) - یعنی همون فلگِ دستیِ subscribed=1 رو
                            // دیتابیس (حسابِ تست/شخصیِ توسعه‌دهنده)، واقعاً دائمیه.
                            GoldSheenBox(
                                modifier = Modifier.padding(top = 4.dp),
                                cornerRadius = 8.dp,
                            ) {
                                Text(
                                    "اشتراک دائمی",
                                    color = AppText,
                                    fontSize = 11.sp,
                                    modifier = Modifier
                                        .background(AppAccent.copy(alpha = 0.24f), RoundedCornerShape(8.dp))
                                        .border(1.dp, AppAccent.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }
                        }
                        Button(
                            onClick = { authViewModel.logout() },
                            colors = ButtonDefaults.buttonColors(containerColor = AppDanger),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                        ) {
                            Text("خروج از حساب")
                        }
                    }
                    else -> {
                        Text("ورود به حساب انجام نشده", color = AppText, fontSize = 15.sp)
                        Text(
                            "برای همگام‌سازی ابری وارد شو",
                            color = AppMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        GradientButton(
                            onClick = onShowLoginPrompt,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                        ) {
                            Text("ورود")
                        }
                    }
                }
            }

            if (showDeleteAccountConfirm) {
                AlertDialog(
                    onDismissRequest = { if (!deleteAccountInProgress) showDeleteAccountConfirm = false },
                    title = { Text("حذف حساب کاربری") },
                    text = {
                        Text(
                            "شماره‌ی حساب و وام‌ها/پشتیبان‌های ابری‌ای که سمت سرور ذخیره شدن برای همیشه " +
                                "پاک می‌شن و قابل بازگشت نیستن. داده‌های محلیِ همین گوشی (وام‌ها/چک‌های " +
                                "ذخیره‌شده) دست‌نخورده می‌مونه. مطمئنی؟",
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                deleteAccountInProgress = true
                                authViewModel.deleteAccount(
                                    onSuccess = {
                                        deleteAccountInProgress = false
                                        showDeleteAccountConfirm = false
                                        banner.show("حساب کاربری حذف شد")
                                    },
                                    onError = {
                                        deleteAccountInProgress = false
                                        banner.show("حذف حساب ناموفق بود؛ دوباره امتحان کن")
                                    },
                                )
                            },
                            enabled = !deleteAccountInProgress,
                            colors = ButtonDefaults.buttonColors(containerColor = AppDanger),
                        ) {
                            if (deleteAccountInProgress) {
                                LottieSpinner(modifier = Modifier.size(18.dp))
                            } else {
                                Text("حذف کن")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteAccountConfirm = false },
                            enabled = !deleteAccountInProgress,
                        ) {
                            Text("انصراف")
                        }
                    },
                )
            }

            // این کارت قبلاً فقط برای LOGGED_IN نشون داده می‌شد - یعنی کاربر مهمان (GateState.GUEST)
            // اصلاً هیچ نقطه‌ی ورودی‌ای برای خرید اشتراک نمی‌دید (خواسته‌ی صریح کاربر: «تو تنظیمات
            // زیر حساب کاربری بجا باشه برای خرید اشتراک اصلا جایی نزاشتی اونو»). بعد کاربر گفت حتی
            // دیدنِ قیمتِ پلن‌ها هم نباید اول ورود بخواد («می‌خوام اشتراک‌ها قیمتشون معلوم باشه، نیاز
            // نباشه حتما ورود کرد») - پس حالا تپ‌کردن همیشه مستقیم می‌ره صفحه‌ی پلن‌ها (SubscriptionScreen
            // قیمت‌ها رو بی‌قید و شرط از کافه‌بازار می‌گیره، نیاز به توکن نداره)؛ فقط خودِ دکمه‌ی «خرید»
            // تو اون صفحه (نه اینجا) اگه کاربر لاگین نبود، اول می‌بره سراغ ورود - رجوع کن به
            // SubscriptionScreen.onNeedsLogin.
            // subscribed هم با خریدِ واقعی true می‌شه هم با دوره‌ی آزمایشیِ ۷روزه؛ اگه فقط شرطِ
            // !subscribed می‌بود، کاربرِ تو دوره‌ی آزمایشی اصلاً این کارت رو نمی‌دید و نمی‌تونست زودتر
            // از تمومِ آزمایشی، خرید کنه (خواسته‌ی صریح کاربر: «شاید یکی دوست داشت از همون اول
            // بگیره») - برای همین وقتی دلیلِ subscribed فقط دوره‌ی آزمایشیه (نه خریدِ واقعی)، همچنان
            // این کارت نشون داده می‌شه.
            val onlyTrialSubscribed = subscribed && trialDaysLeft != null && trialDaysLeft in 1..7
            if ((!subscribed || onlyTrialSubscribed) && matches("اشتراک", "خرید اشتراک")) {
                PulseGlowBox(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    // GoldSheenBox: هر چند ثانیه یه موجِ نورِ طلایی از رو کارت رد می‌شه (لهجه‌ی
                    // طلایی رو افکت/پس‌زمینه، طبق الگوی مصوب) - مکملِ هاله‌ی ضربان‌دارِ PulseGlow.
                    GoldSheenBox {
                    AppCard(backgroundColor = lerp(AppSurface, AppAccent, 0.14f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = AppAccent)
                            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                                Text("ارتقا به نسخه اشتراکی", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "وام/چک نامحدود، همگام‌سازی چند دستگاه و موارد دیگر",
                                    color = AppMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                        }
                        GradientButton(
                            onClick = onShowSubscription,
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        ) {
                            Text("مشاهده پلن‌ها")
                        }
                    }
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("جستجو تو تنظیمات") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
            )

            if (matches("تم", "رنگ برنامه")) {
                val themeMode by themeViewModel.themeMode.collectAsState()
                // همون افکتِ دایره‌ایِ نوارِ بالا، این‌بار از مرکزِ خودِ چیپی که زده شد باز می‌شه -
                // رجوع کن به ThemeReveal.kt.
                val themeReveal = LocalThemeReveal.current
                val chipCenters = remember { mutableStateMapOf<ThemeMode, Offset>() }
                // startReveal الان suspend ئه - رجوع کن به کامنتِ کاملِ ThemeReveal.kt دربارهٔ
                // اینکه چرا اسنپ‌شات و عوض‌کردنِ تم باید تویِ یه کوروتینِ واحد پشتِ‌سرهم باشن.
                val themeToggleScope = rememberCoroutineScope()
                AppCard(label = "تم", modifier = Modifier.padding(top = 10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        themeModeOptions.forEach { (mode, label) ->
                            AppChip(
                                label = label,
                                selected = themeMode == mode,
                                onClick = {
                                    // فقط وقتی واقعاً داره عوض می‌شه افکت معنی داره - زدنِ دوباره‌ی
                                    // چیپِ ازقبل‌فعال نباید کلِ صفحه رو بی‌دلیل جارو کنه.
                                    if (mode != themeMode && !themeReveal.inProgress) {
                                        val origin = chipCenters[mode] ?: Offset.Zero
                                        themeToggleScope.launch {
                                            themeReveal.startReveal(origin = origin, currentKey = themeMode)
                                            themeViewModel.setThemeMode(mode)
                                        }
                                    } else {
                                        themeViewModel.setThemeMode(mode)
                                    }
                                },
                                modifier = Modifier.onGloballyPositioned {
                                    chipCenters[mode] = it.boundsInRoot().center
                                },
                            )
                        }
                    }
                }
            }

            if (matches("اندازه فونت")) {
                AppCard(label = "اندازه فونت", modifier = Modifier.padding(top = 10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        fontSizeOptions.forEach { (scale, label) ->
                            AppChip(
                                label = label,
                                selected = fontScale == scale,
                                onClick = { themeViewModel.setFontScale(scale) },
                            )
                        }
                    }
                }
            }

            if (matches("یادآوری سررسید")) {
                AppCard(label = "یادآوری سررسید", modifier = Modifier.padding(top = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "برای اقساط و چک‌های نزدیک به سررسید یه نوتیف بده",
                            color = AppMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f),
                        )
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { checked ->
                                if (!checked) {
                                    notificationsViewModel.disable()
                                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                    notificationsViewModel.enable()
                                } else if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS,
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    notificationsViewModel.enable()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = AppPrimary, checkedTrackColor = AppPrimary.copy(alpha = 0.5f)),
                        )
                    }
                    TextButton(onClick = onShowReminderSettings, modifier = Modifier.padding(top = 2.dp)) {
                        Text("زمان‌بندی، صدا و ویبره رو شخصی‌سازی کن", fontSize = 12.sp)
                    }
                }
            }

            if (matches("هپتیک فیدبک", "ویبره")) {
                AppCard(label = "هپتیک فیدبک", modifier = Modifier.padding(top = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "موقع لمس دکمه‌ها و اسلایدرها یه لرزش کوتاه حس کن",
                            color = AppMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f),
                        )
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = { hapticsViewModel.setEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = AppPrimary, checkedTrackColor = AppPrimary.copy(alpha = 0.5f)),
                        )
                    }
                }
            }

            if (matches("پشتیبان‌گیری خودکار روزانه")) {
                AppCard(label = "پشتیبان‌گیری خودکار روزانه", modifier = Modifier.padding(top = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "هر روز یه اسنپ‌شات از وام/چک/حساب رو خودکار ذخیره کن",
                                color = AppMuted,
                                fontSize = 12.sp,
                            )
                            val lastBackupLabel = remember(lastAutoBackupAt) {
                                lastAutoBackupAt?.let { iso ->
                                    runCatching {
                                        val date = JalaliCalendar.fromGregorian(
                                            iso.substring(0, 4).toInt(),
                                            iso.substring(5, 7).toInt(),
                                            iso.substring(8, 10).toInt(),
                                        )
                                        "${toFa(date.d)}/${toFa(date.m)}/${toFa(date.y)}"
                                    }.getOrNull()
                                }
                            }
                            if (lastBackupLabel != null) {
                                Text(
                                    "آخرین پشتیبان: $lastBackupLabel",
                                    color = AppMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                        }
                        Switch(
                            checked = autoBackupEnabled,
                            onCheckedChange = { checked ->
                                if (checked) autoBackupViewModel.enable() else autoBackupViewModel.disable()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = AppPrimary, checkedTrackColor = AppPrimary.copy(alpha = 0.5f)),
                        )
                    }
                    if (lastAutoBackupAt != null) {
                        OutlinedButton(
                            onClick = {
                                autoBackupViewModel.restoreFromAutoBackup { ok ->
                                    val message = if (ok) "بازیابی از پشتیبان خودکار انجام شد" else "پشتیبانی برای بازیابی پیدا نشد"
                                    banner.show(message, isSuccess = ok)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                        ) {
                            Text("بازیابی از پشتیبان خودکار")
                        }
                    }
                    // برخلاف دکمه‌ی بالا (که فقط رو همون گوشی کار می‌کنه)، این از سرور می‌گیره - برای
                    // وقتی گوشی عوض شده یا اپ پاک/نصب شده. فقط برای کاربر لاگین‌شده‌ی مشترک نشون داده
                    // می‌شه چون پوش‌شدن به سرور هم فقط برای همین گروه فعاله (رجوع کن به AutoBackupWorker).
                    if (gateState == GateState.LOGGED_IN && subscribed) {
                        OutlinedButton(
                            onClick = {
                                autoBackupViewModel.restoreFromCloud { ok ->
                                    val message = if (ok) "بازیابی از سرور ابری انجام شد" else "پشتیبانی رو سرور ابری پیدا نشد"
                                    banner.show(message, isSuccess = ok)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                        ) {
                            Text("بازیابی از سرور ابری")
                        }
                    }
                }
            }

            if (matches("تقویم مالی")) {
                AppCard(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        // برای قدمِ SETTINGS_CALENDARِ AppTourOverlay - رجوع کن به
                        // onTourRowPositioned/tourHighlightQuery بالا.
                        .onGloballyPositioned { onTourRowPositioned(it.boundsInRoot()) },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("تقویم مالی", color = AppText, fontSize = 13.sp)
                            Text(
                                "سررسید اقساط همه‌ی وام‌هات رو رو تقویم ببین",
                                color = AppMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        OutlinedButton(onClick = onShowFinancialCalendar) {
                            Text("مشاهده")
                        }
                    }
                }
            }

            if (matches("آمار و گزارشات")) {
                AppCard(modifier = Modifier.padding(top = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("آمار و گزارشات", color = AppText, fontSize = 13.sp)
                            Text(
                                "آمار کلی وام‌هات + خروجی PDF",
                                color = AppMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        OutlinedButton(onClick = onShowStats) {
                            Text("مشاهده")
                        }
                    }
                }
            }

            if (matches("تاریخچه محاسبات")) {
                AppCard(modifier = Modifier.padding(top = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("تاریخچه محاسبات", color = AppText, fontSize = 13.sp)
                            Text(
                                "مرور و جستجوی محاسبه‌های قبلی وام/سقف وام/سود سپرده",
                                color = AppMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        OutlinedButton(onClick = onShowHistory) {
                            Text("مشاهده")
                        }
                    }
                }
            }

            if (matches("امور چک")) {
                AppCard(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        // برای قدمِ SETTINGS_CHEQUEِ AppTourOverlay - رجوع کن به
                        // onTourRowPositioned/tourHighlightQuery بالا.
                        .onGloballyPositioned { onTourRowPositioned(it.boundsInRoot()) },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("امور چک", color = AppText, fontSize = 13.sp)
                            Text(
                                "چک‌های دریافتی/پرداختی و دسته‌چک‌هات رو مدیریت کن",
                                color = AppMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        OutlinedButton(onClick = onShowCheque) {
                            Text("مشاهده")
                        }
                    }
                }
            }

            if (matches("حساب‌های بانکی")) {
                AppCard(modifier = Modifier.padding(top = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("حساب‌های بانکی", color = AppText, fontSize = 13.sp)
                            Text(
                                "موجودی و تراکنش‌های واریز/برداشت هر حساب رو دنبال کن",
                                color = AppMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        OutlinedButton(onClick = onShowAccounts) {
                            Text("مشاهده")
                        }
                    }
                }
            }

            if (matches("امنیت", "قفل", "PIN", "اثر انگشت")) {
                AccordionCard(title = "امنیت", modifier = Modifier.padding(top = 10.dp)) {
                    SecuritySettings(appLockViewModel)
                }
            }

            if (matches("پشتیبانی")) {
                AccordionCard(title = "پشتیبانی", modifier = Modifier.padding(top = 10.dp)) {
                    SupportContacts(banner)
                }
            }

            if (matches("درباره برنامه")) {
                AccordionCard(title = "درباره برنامه", modifier = Modifier.padding(top = 10.dp)) {
                    Text(aboutText, color = AppMuted, fontSize = 12.sp, lineHeight = 20.sp)
                }
            }

            if (matches("حریم خصوصی")) {
                AccordionCard(title = "حریم خصوصی", modifier = Modifier.padding(top = 10.dp)) {
                    Text(privacyText, color = AppMuted, fontSize = 12.sp, lineHeight = 20.sp)
                }
            }

            // خواسته‌ی کاربر: «حذف حساب کاربری» بیاد پایینِ پایینِ لیستِ تنظیمات - قبلاً بالای لیست،
            // تویِ کارتِ وضعیتِ حساب بود. الزامِ استانداردِ فروشگاه‌های اپ: راهِ داخل‌برنامه‌ای برای
            // حذفِ کاملِ حساب، نه فقط خروج - عمداً OutlinedButton (نه پرشده مثلِ خروج) - شدتِ بصریِ
            // کمتر برای یه عملِ به‌مراتب جدی‌تر و غیرقابل‌بازگشت، تا اشتباهی باهاش قاطی نشه.
            if (gateState == GateState.LOGGED_IN && matches("حذف حساب")) {
                AppCard(modifier = Modifier.padding(top = 10.dp)) {
                    OutlinedButton(
                        onClick = { showDeleteAccountConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppDanger),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("حذف حساب کاربری")
                    }
                }
            }
        }
    }

        InAppBannerHost(banner, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

private const val SUPPORT_EMAIL = "vamman.pbs@gmail.com"

/** پشتیبانی فقط با ایمیل - با تپ مستقیم Gmail (نه یه چوزر عمومی) با گیرنده‌ی از قبل پرشده باز
 * می‌شه تا کاربر فقط متن رو بنویسه و بزنه ارسال؛ اگه Gmail نصب نباشه mailto عادی (هر اپ ایمیلی)
 * جایگزین می‌شه. چون هیچ callback مستقیمی برای «کاربر واقعاً ایمیل رو فرستاد» وجود نداره، این با
 * ActivityResultContracts.StartActivityForResult پیاده شده: هر بار که کاربر از صفحه‌ی
 * ارسال/کامپوز برگرده (چه با زدنِ ارسال، چه با دکمه‌ی برگشت)، یه بنرِ تشکر نشون داده می‌شه. */
@Composable
private fun SupportContacts(banner: InAppBannerState) {
    val context = LocalContext.current
    val emailLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        banner.show("ممنون از پیامت! در اسرع وقت جوابت رو می‌دیم", isSuccess = true)
    }
    Column {
        SupportRow(label = "ایمیل", value = SUPPORT_EMAIL) {
            val gmailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
                setPackage("com.google.android.gm")
            }
            try {
                emailLauncher.launch(gmailIntent)
            } catch (e: ActivityNotFoundException) {
                try {
                    emailLauncher.launch(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$SUPPORT_EMAIL")))
                } catch (e: ActivityNotFoundException) {
                    banner.show("اپ مناسبی برای ارسال ایمیل پیدا نشد")
                }
            }
        }
    }
}

/**
 * پورت قفل امنیتی PIN+اثر انگشت اپ رقیب (VAMMAN) - برگردوندن تصمیم قبلی حذف بایومتریک، با درخواست
 * صریح جدید کاربر (رجوع کن به CLAUDE.md). سوییچ اثر انگشت فقط اگه گوشی سخت‌افزار/داده‌ی بایومتریک
 * ثبت‌شده داشته باشه ([biometricAvailable]) فعال می‌شه.
 */
@Composable
private fun SecuritySettings(appLockViewModel: AppLockViewModel) {
    val context = LocalContext.current
    val pinHash by appLockViewModel.pinHash.collectAsState()
    val biometricEnabled by appLockViewModel.biometricEnabled.collectAsState()
    val autoLockTimeoutMinutes by appLockViewModel.autoLockTimeoutMinutes.collectAsState()
    var showPinDialog by remember { mutableStateOf(false) }

    if (showPinDialog) {
        PinSetupDialog(
            onDismiss = { showPinDialog = false },
            onConfirm = { pin ->
                appLockViewModel.setPin(pin)
                showPinDialog = false
            },
        )
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "قفل با اثر انگشت",
                color = AppText,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = biometricEnabled,
                enabled = biometricAvailable(context),
                onCheckedChange = { appLockViewModel.setBiometricEnabled(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = AppPrimary, checkedTrackColor = AppPrimary.copy(alpha = 0.5f)),
            )
        }
        if (!biometricAvailable(context)) {
            Text(
                "این گوشی سنسور یا اثر انگشت ثبت‌شده‌ای نداره",
                color = AppMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("قفل با PIN", color = AppText, fontSize = 13.sp)
                Text(
                    if (pinHash != null) "فعال است" else "غیرفعال",
                    color = if (pinHash != null) AppPrimary else AppMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            OutlinedButton(onClick = { showPinDialog = true }) {
                Text(if (pinHash != null) "تغییر PIN" else "تنظیم PIN")
            }
        }
        if (pinHash != null) {
            TextButton(
                onClick = { appLockViewModel.clearPin() },
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Text("حذف قفل PIN", color = AppDanger)
            }
        }

        if (pinHash != null || biometricEnabled) {
            Text(
                "قفل خودکار بعد از رفتن به پس‌زمینه",
                color = AppText,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 14.dp),
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                autoLockTimeoutOptions.forEach { (minutes, label) ->
                    AppChip(
                        label = label,
                        selected = autoLockTimeoutMinutes == minutes,
                        onClick = { appLockViewModel.setAutoLockTimeoutMinutes(minutes) },
                    )
                }
            }
        }
    }
}

private val autoLockTimeoutOptions = listOf(0 to "بی‌درنگ", 1 to "۱ دقیقه", 5 to "۵ دقیقه", 15 to "۱۵ دقیقه")

@Composable
private fun PinSetupDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تنظیم PIN") },
        text = {
            Column {
                // Ltr: همون باگِ فیلدهای شماره‌موبایل/کدِ تاییدِ LoginScreen (تایپِ عدد زیرِ RTL از
                // سمتِ راست جا می‌گرفت) - رجوع کن به کامنتِ Ltr.kt.
                Ltr {
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { val cleaned = cleanNum(it); if (cleaned.length <= 8) pin = cleaned },
                        label = { Text("PIN جدید") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Ltr {
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { val cleaned = cleanNum(it); if (cleaned.length <= 8) confirmPin = cleaned },
                        label = { Text("تکرار PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                }
                if (error != null) {
                    Text(error ?: "", color = AppDanger, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when {
                    pin.length < 4 -> error = "PIN باید حداقل ۴ رقم باشه"
                    pin != confirmPin -> error = "دو PIN یکی نیستن"
                    else -> onConfirm(pin)
                }
            }) { Text("ذخیره") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        },
    )
}

@Composable
private fun SupportRow(label: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .pressScaleClickable(scale = 0.98f, onClick = onClick)
            .background(AppBg, RoundedCornerShape(8.dp))
            .padding(10.dp),
    ) {
        Text(label, color = AppText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text(value, color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

/** پورت toggleAbout/toggleAbout (آکاردئون settings-item + grace-box تو www/index.html). */
@Composable
private fun AccordionCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    AppCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pressScaleClickable(scale = 0.99f) { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, color = AppText, fontSize = 13.sp)
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = AppMuted,
            )
        }
        if (expanded) {
            Column(modifier = Modifier.padding(top = 8.dp)) { content() }
        }
    }
}

// نسخه‌ی «۱» قبلاً هاردکد بود (همیشه ثابت، هیچ‌وقت آپدیت نمی‌شد) - خواسته‌ی کاربر: نسخه‌ی واقعیِ
// نصب‌شده رو نشون بده. BuildConfig.VERSION_NAME همون versionNameِ CI (مثلاً "1.0.332") ئه.
private val aboutText = "حسابدار من — نسخه ${BuildConfig.VERSION_NAME}\n" +
    "این اپ برای محاسبه سریع و شفاف اقساط وام، سود سپرده و برنامه‌ریزی مالی طراحی شده.\n" +
    "Powered By Sad Team"

private const val privacyText = "چه اطلاعاتی ذخیره می‌شه؟\n" +
    "وام‌ها، تنظیمات و یادآوری‌هایی که تو اپ می‌سازی، فقط روی گوشی خودت ذخیره می‌شن. این اپ هیچ " +
    "تبلیغ، ابزار ردیابی (analytics) یا کد شخص ثالثی نداره و اطلاعاتت رو به‌جایی نمی‌فروشه.\n\n" +
    "ورود با شماره تلفن\n" +
    "بدون ورود هم می‌تونی از اپ به‌عنوان مهمان استفاده کنی. اگه با شماره موبایل وارد بشی، فقط " +
    "شماره‌ت و لیست وام‌هات (برای همگام‌سازی بین گوشی‌هات) روی سرور اختصاصی همین اپ ذخیره می‌شه؛ " +
    "این اطلاعات جای دیگه‌ای فرستاده نمی‌شه و در اختیار شرکت یا سرویس ثالثی قرار نمی‌گیره.\n\n" +
    "اشتراک\n" +
    "بدون اشتراک فقط یک وام قابل ذخیره‌ست؛ برای ذخیره‌ی وام بیشتر اول باید وارد بشی و بعد اشتراک " +
    "تهیه کنی."
