package ir.sadteam.loancalc.ui.settings

import ir.sadteam.loancalc.ui.support.ContactSupportContent
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.WorkspacePremium
import ir.sadteam.loancalc.ui.theme.AppPurple
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import ir.sadteam.loancalc.ui.theme.AppGoldInk
import ir.sadteam.loancalc.ui.theme.AppGoldPillSoft
import ir.sadteam.loancalc.ui.components.AppHeroCard
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.produceState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import ir.sadteam.loancalc.BuildConfig
import ir.sadteam.loancalc.core.BankAppMatcher
import ir.sadteam.loancalc.core.BankSmsParser
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.ParsedBankSms
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.ACCOUNT_TYPE_BANK
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.account.SmsImportScreen
import ir.sadteam.loancalc.ui.coin.CoinHubScreen
import ir.sadteam.loancalc.ui.account.SmsSenderPickerDialog
import ir.sadteam.loancalc.ui.profile.GamificationViewModel
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.auth.GateState
import ir.sadteam.loancalc.ui.auth.LoginScreen
import ir.sadteam.loancalc.notifications.DeepLinkViewModel
import ir.sadteam.loancalc.ui.calendar.FinancialCalendarScreen
import ir.sadteam.loancalc.ui.components.AppButtonVariant
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.components.AppCardVariant
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.AppHeroRow
import ir.sadteam.loancalc.ui.components.AvatarPicker
import ir.sadteam.loancalc.ui.components.FramedAvatar
import ir.sadteam.loancalc.ui.components.GoldSheenBox
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.goal.SavingsGoalScreen
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.InAppBannerState
import ir.sadteam.loancalc.ui.components.JibakLogo
import ir.sadteam.loancalc.ui.components.LottieSpinner
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.PulseGlowBox
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.haptics.HapticsViewModel
import ir.sadteam.loancalc.ui.history.CalculationHistoryScreen
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyModeViewModel
import ir.sadteam.loancalc.ui.profile.AvatarViewModel
import ir.sadteam.loancalc.ui.profile.BadgesScreen
import ir.sadteam.loancalc.ui.security.AppLockViewModel
import ir.sadteam.loancalc.ui.security.biometricAvailable
import ir.sadteam.loancalc.ui.subscription.SubscriptionScreen
import ir.sadteam.loancalc.ui.support.BugReportScreen
import ir.sadteam.loancalc.ui.support.SUPPORT_EMAIL
import ir.sadteam.loancalc.ui.subscription.parseSubscribedUntil
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppDisabledText
import ir.sadteam.loancalc.ui.theme.AppIsDark
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppPrimaryBorder
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppSpacing
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppUrgentShadow
import ir.sadteam.loancalc.ui.theme.LocalThemeReveal
import ir.sadteam.loancalc.ui.theme.Motion
import ir.sadteam.loancalc.ui.theme.ThemeMode
import ir.sadteam.loancalc.ui.theme.ThemeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

private val fontSizeOptions = listOf(0.9f to "کوچک", 1f to "متوسط", 1.15f to "بزرگ")
private val themeModeOptions =
    listOf(ThemeMode.LIGHT to "روشن", ThemeMode.DARK to "تاریک", ThemeMode.SYSTEM to "خودکار")

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
    notificationsViewModel: NotificationsViewModel = hiltViewModel(),
    appLockViewModel: AppLockViewModel = hiltViewModel(),
    autoBackupViewModel: AutoBackupViewModel = hiltViewModel(),
    hapticsViewModel: HapticsViewModel = hiltViewModel(),
    smsAutoImportViewModel: SmsAutoImportViewModel = hiltViewModel(),
    deepLinkViewModel: DeepLinkViewModel = hiltViewModel(),
) {
    var route by remember { mutableStateOf(SettingsRoute.MAIN) }
    // زیرصفحه‌های «فیچری» (تقویم/آمار/تاریخچه) از رو خودِ صفحه‌ی «ابزارها» باز می‌شن، پس یه استیتِ
    // جدا لازم دارن تا با برگشت، به «ابزارها» برگردن نه به ریشه‌ی تنظیمات.
    var tool by remember { mutableStateOf<String?>(null) }
    var showLoginPrompt by remember { mutableStateOf(false) }
    var showSubscription by remember { mutableStateOf(false) }
    var showReminderSettings by remember { mutableStateOf(false) }
    // 🐞 گزارشِ مشکل - زیرصفحه‌ی تمام‌صفحه، مثلِ بقیه‌ی زیرصفحه‌های تنظیمات.
    var showBugReport by remember { mutableStateOf(false) }

    val screenKey = when {
        showLoginPrompt -> "login"
        showSubscription -> "subscription"
        showReminderSettings -> "reminderSettings"
        showBugReport -> "bugReport"
        tool != null -> "tool"
        route != SettingsRoute.MAIN -> "sub"
        else -> "main"
    }

    AnimatedContent(
        targetState = screenKey,
        transitionSpec = { Motion.contentEnter togetherWith Motion.contentExit },
        label = "settingsScreen",
    ) { key ->
        when (key) {
            // هر زیرصفحه‌ی تنظیمات (که خودش پنلی با عرضِ ۸۵٪ صفحه‌ست، رجوع کن به AnimatedVisibility
            // تو MainActivity.kt) باید کاملاً فول‌اسکرین باشه - رجوع کن به [FullScreenDialog].
            "login" -> FullScreenDialog(onDismissRequest = { showLoginPrompt = false }) {
                LoginScreen(onDismiss = { showLoginPrompt = false }, onLoginSuccess = { showLoginPrompt = false })
            }
            "subscription" -> FullScreenDialog(onDismissRequest = { showSubscription = false }) {
                SubscriptionScreen(
                    onBack = { showSubscription = false },
                    onSubscribed = { showSubscription = false },
                    onNeedsLogin = { showLoginPrompt = true },
                )
            }
            "reminderSettings" -> FullScreenDialog(onDismissRequest = { showReminderSettings = false }) {
                ReminderSettingsScreen(onBack = { showReminderSettings = false })
            }
            "bugReport" -> FullScreenDialog(onDismissRequest = { showBugReport = false }) {
                BugReportScreen(onBack = { showBugReport = false })
            }
            "tool" -> FullScreenDialog(onDismissRequest = { tool = null }) {
                when (tool) {
                    // تپ روی ردیفِ تقویم باید به خودِ وام/چک برود (دورِ ۹). همان مسیرِ
                    // دیپ‌لینکی که اعلان و ردیفِ تبِ سررسید از آن می‌گذرند - پس منطقِ ناوبری
                    // یک‌جاست. بستنِ ابزار و تنظیمات لازم است وگرنه مقصد زیرِ پوششِ
                    // تمام‌صفحه‌ی تنظیمات باز می‌شود و دیده نمی‌شود.
                    "calendar" -> FinancialCalendarScreen(
                        onBack = { tool = null },
                        onOpenLoan = { id -> tool = null; deepLinkViewModel.openLoan(id); onBack() },
                        onOpenCheque = { id -> tool = null; deepLinkViewModel.openCheque(id); onBack() },
                    )
                    "goals" -> SavingsGoalScreen(onBack = { tool = null })
                    else -> CalculationHistoryScreen(onBack = { tool = null })
                }
            }
            "sub" -> FullScreenDialog(onDismissRequest = { route = SettingsRoute.MAIN }) {
                SettingsSubPage(
                    route = route,
                    onBack = { route = SettingsRoute.MAIN },
                    authViewModel = authViewModel,
                    themeViewModel = themeViewModel,
                    notificationsViewModel = notificationsViewModel,
                    appLockViewModel = appLockViewModel,
                    autoBackupViewModel = autoBackupViewModel,
                    smsAutoImportViewModel = smsAutoImportViewModel,
                    onShowLoginPrompt = { showLoginPrompt = true },
                    onShowSubscription = { showSubscription = true },
                    onShowReminderSettings = { showReminderSettings = true },
                    onOpenTool = { tool = it },
                    onOpenRules = { route = SettingsRoute.PARSING_RULES },
                    onOpenBugReport = { showBugReport = true },
                )
            }
            else -> SettingsMainContent(
                onBack = onBack,
                authViewModel = authViewModel,
                hapticsViewModel = hapticsViewModel,
                onOpen = { route = it },
                onShowSubscription = { showSubscription = true },
            )
        }
    }
}

/** ردیف‌های ریشه‌ی تنظیمات؛ هر کدوم یه زیرصفحه‌ی تمام‌صفحه باز می‌کنه (بازطراحیِ خواسته‌ی کاربر طبقِ
 * اپِ مرجع - قبلاً همه‌ی سوییچ‌ها/فرم‌ها مستقیم تو خودِ لیستِ ریشه باز بودن و صفحه شلوغ بود). */
private enum class SettingsRoute(val title: String, val keywords: List<String>) {
    MAIN("تنظیمات", emptyList()),
    ACCOUNT("حساب کاربری", listOf("حساب", "اشتراک", "خروج", "شماره موبایل")),
    APPEARANCE("ظاهر برنامه", listOf("تم", "رنگ", "اندازه فونت", "روشن", "تاریک")),
    REMINDERS("یادآورها", listOf("یادآوری سررسید", "یادآوری روزانه", "نوتیف")),
    DATA("مدیریت داده‌های من", listOf("پشتیبان", "بکاپ", "بازیابی")),
    SMS("پیامک‌های بانکی", listOf("پیامک", "بانک", "خواندن خودکار")),
    BACKGROUND(
        "اجرا در پس‌زمینه",
        listOf("پس زمینه", "همیشه روشن", "اجرای خودکار", "autostart", "باتری", "ری استارت", "بسته شدن"),
    ),
    TOOLS("ابزارها", listOf("تقویم مالی", "آمار", "گزارش", "تاریخچه محاسبات")),
    SECURITY("امنیت", listOf("قفل", "PIN", "اثر انگشت")),
    // ⚠️ نامش از «فروشگاهِ سکه» به «تمِ رنگی» رفت (`75c`): فروشگاه حالا یک در دارد
    // (سکه‌ی هدرِ خانه) و این ردیف فقط **لینکی** به همان است، نه درِ دوم. خودِ «ظاهر
    // برنامه» در تنظیمات می‌ماند چون سه چیز دارد و فقط یکی‌اش خریدنی است - اندازه‌ی
    // متن و انیمیشنِ کم دسترس‌پذیری‌اند و کاربری که متن برایش ریز است نباید برای
    // بزرگ‌کردنش وارد ویترین شود.
    COLOR_THEME("تمِ رنگی", listOf("تم", "رنگ", "پوسته", "سکه", "فروشگاه", "آیکون", "قلم", "فونت")),
    BADGES("نشان‌ها", listOf("نشان", "دستاورد", "مدال", "سکه")),
    PARSING_RULES("قاعده‌های تشخیص", listOf("قاعده", "دسته‌بندی خودکار", "تشخیص")),
    ABOUT("درباره‌ی برنامه", listOf("درباره", "پشتیبانی", "حریم خصوصی", "نسخه")),
}

@Composable
private fun SettingsMainContent(
    onBack: () -> Unit,
    authViewModel: AuthViewModel,
    hapticsViewModel: HapticsViewModel,
    onOpen: (SettingsRoute) -> Unit,
    onShowSubscription: () -> Unit,
) {
    val gateState by authViewModel.gateState.collectAsState()
    val phone by authViewModel.phone.collectAsState()
    val subscribed by authViewModel.subscribed.collectAsState()
    val trialDaysLeft by authViewModel.trialDaysLeft.collectAsState()
    val vibrationEnabled by hapticsViewModel.enabled.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    fun matches(route: SettingsRoute) = searchQuery.isBlank() ||
        route.title.contains(searchQuery.trim()) ||
        route.keywords.any { it.contains(searchQuery.trim()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
    ) {
        // ── سربرگ (بسته‌ی تنظیماتِ ChatGPT): برگشتِ گرد، عنوان و زیرنویس، چرخ‌دنده‌ی کوچک ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppSurface)
                    .border(1.dp, AppLine, RoundedCornerShape(14.dp))
                    .pressScaleClickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText)
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text("تنظیمات", color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(
                    "مدیریت حساب و شخصی‌سازی برنامه",
                    color = AppMuted,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(AppPrimaryPill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Settings, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(24.dp))
            }
        }

        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
            // جستجوی واقعیِ همین صفحه - فقط شکلش قرصِ گرد شد.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppSurface)
                    .border(1.dp, AppLine, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (searchQuery.isEmpty()) {
                        Text("جستجو در تنظیمات…", color = AppLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Bold),
                        cursorBrush = SolidColor(AppPrimary),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (searchQuery.isNotEmpty()) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "پاک‌کردن",
                        tint = AppMuted,
                        modifier = Modifier.size(20.dp).clip(CircleShape).pressScaleClickable { searchQuery = "" },
                    )
                } else {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = AppMuted, modifier = Modifier.size(22.dp))
                }
            }

            // ── کارتِ حساب: همان کارتِ رنگیِ بالای بقیه‌ی صفحه‌ها (با تم عوض می‌شود) ──────
            // آدمک و قاب **همان** `FramedAvatar`ِ هدرِ خانه است، از همان `AvatarViewModel`؛
            // پس هر تغییرِ آدمک/قاب/رنگِ قاب همان لحظه این‌جا هم دیده می‌شود.
            if (searchQuery.isBlank()) {
                val avatarViewModel: AvatarViewModel = hiltViewModel()
                val avatar by avatarViewModel.avatar.collectAsState()
                val avatarFrame by avatarViewModel.frame.collectAsState()
                AppHeroCard(
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .pressScaleClickable(scale = 0.99f) { onOpen(SettingsRoute.ACCOUNT) },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            FramedAvatar(avatar, size = 52.dp, frame = avatarFrame)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    if (gateState == GateState.LOGGED_IN) "حساب کاربری" else "وارد نشدی",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                )
                                // حلقه‌ی طلاییِ قبلیِ سربرگ تنها نشانه‌ی اشتراک بود؛ حالا این‌جاست.
                                if (subscribed) {
                                    Text(
                                        "اشتراکی",
                                        color = AppGoldInk,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier
                                            .padding(start = 8.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(AppGoldPillSoft)
                                            .padding(horizontal = 8.dp, vertical = 2.dp),
                                    )
                                }
                            }
                            if (gateState == GateState.LOGGED_IN && phone != null) {
                                // شماره ذاتاً چپ‌به‌راسته ولی جای خودش راست‌چین می‌مونه.
                                Ltr {
                                    Text(
                                        toFa(phone ?: ""),
                                        color = Color.White.copy(alpha = 0.92f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 3.dp),
                                    )
                                }
                            }
                            Text(
                                "ویرایشِ اطلاعات، اشتراک و خروج",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 3.dp),
                            )
                        }
                        Box(
                            modifier = Modifier.size(34.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.20f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.KeyboardArrowLeft,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            } else if (matches(SettingsRoute.ACCOUNT)) {
                SettingsGroup(modifier = Modifier.padding(top = 8.dp)) {
                    SettingsRow(
                        icon = Icons.Filled.Person,
                        route = SettingsRoute.ACCOUNT,
                        value = if (gateState == GateState.LOGGED_IN) toFa(phone ?: "") else "وارد نشدی",
                        onClick = { onOpen(SettingsRoute.ACCOUNT) },
                    )
                }
            }

            // ── کارتِ اشتراک - `AppHeroRow` (سایه‌ی ۴، پدینگِ ۱۴؛ عمداً `AppHeroCard` نیست) ──
            // همون منطقِ قبلی: به کاربرِ ازقبل‌مشترک نشون داده نمی‌شه، این باگ نیست.
            val onlyTrialSubscribed = subscribed && trialDaysLeft != null && trialDaysLeft in 1..7
            if ((!subscribed || onlyTrialSubscribed) && searchQuery.isBlank()) {
                AppHeroRow(
                    icon = Icons.Filled.Star,
                    title = "ارتقا به نسخه اشتراکی",
                    subtitle = "وام و چکِ نامحدود، همگام‌سازیِ چند دستگاه و بیشتر",
                    actionLabel = "مشاهده پلن‌ها",
                    onAction = onShowSubscription,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }

            SettingsSectionLabel("ثبتِ خودکار", "مدیریتِ ورود و ثبتِ اطلاعات")
            SettingsGroup {
                if (matches(SettingsRoute.SMS)) {
                    SettingsRow(
                        Icons.Filled.Sms,
                        SettingsRoute.SMS,
                        tone = SettingsTone.GREEN,
                    ) { onOpen(SettingsRoute.SMS) }
                    SettingsDivider()
                }
                if (matches(SettingsRoute.BACKGROUND)) {
                    SettingsRow(
                        Icons.Filled.BatterySaver,
                        SettingsRoute.BACKGROUND,
                        tone = SettingsTone.GREEN,
                    ) { onOpen(SettingsRoute.BACKGROUND) }
                    SettingsDivider()
                }
                if (matches(SettingsRoute.DATA)) {
                    SettingsRow(
                        Icons.Filled.CloudUpload,
                        SettingsRoute.DATA,
                        tone = SettingsTone.GREEN,
                    ) { onOpen(SettingsRoute.DATA) }
                }
            }

            SettingsSectionLabel("برنامه", "شخصی‌سازی و ظاهرِ برنامه", AppPurple)
            SettingsGroup {
                if (matches(SettingsRoute.REMINDERS)) {
                    SettingsRow(
                        Icons.Filled.Notifications,
                        SettingsRoute.REMINDERS,
                        tone = SettingsTone.ORANGE,
                    ) { onOpen(SettingsRoute.REMINDERS) }
                    SettingsDivider()
                }
                if (matches(SettingsRoute.APPEARANCE)) {
                    SettingsRow(
                        Icons.Filled.Palette,
                        SettingsRoute.APPEARANCE,
                        tone = SettingsTone.PURPLE,
                    ) { onOpen(SettingsRoute.APPEARANCE) }
                    SettingsDivider()
                }
                if (matches(SettingsRoute.BADGES)) {
                    SettingsRow(
                        Icons.Filled.MilitaryTech,
                        SettingsRoute.BADGES,
                        tone = SettingsTone.ORANGE,
                        status = "کارهایی که انجام داده‌ای و سکه‌ای که گرفته‌ای",
                    ) { onOpen(SettingsRoute.BADGES) }
                    SettingsDivider()
                }
                if (matches(SettingsRoute.COLOR_THEME)) {
                    SettingsRow(
                        Icons.Filled.ColorLens,
                        SettingsRoute.COLOR_THEME,
                        tone = SettingsTone.PURPLE,
                        status = "در فروشگاهِ سکه",
                    ) { onOpen(SettingsRoute.COLOR_THEME) }
                    SettingsDivider()
                }
                // تنها سوییچی که عمداً تو ریشه موند - یه گزینه‌ی تک‌حالته‌ست و زیرصفحه‌ی جدا
                // براش یه تپِ اضافه می‌شد.
                if (searchQuery.isBlank() || "ویبره".contains(searchQuery.trim()) || "هپتیک".contains(searchQuery.trim())) {
                    SettingsRowItem(
                        title = "ویبره‌ی لمسی",
                        icon = Icons.Filled.Vibration,
                        tone = SettingsTone.PURPLE,
                        status = "موقعِ لمسِ دکمه‌ها یه لرزشِ کوتاه",
                        checked = vibrationEnabled,
                        onCheckedChange = { hapticsViewModel.setEnabled(it) },
                    )
                    SettingsDivider()
                }
                if (matches(SettingsRoute.SECURITY)) {
                    SettingsRow(
                        Icons.Filled.Lock,
                        SettingsRoute.SECURITY,
                        tone = SettingsTone.RED,
                    ) { onOpen(SettingsRoute.SECURITY) }
                    SettingsDivider()
                }
                if (matches(SettingsRoute.TOOLS)) {
                    SettingsRow(
                        Icons.Filled.Assessment,
                        SettingsRoute.TOOLS,
                    ) { onOpen(SettingsRoute.TOOLS) }
                }
            }

            if (matches(SettingsRoute.ABOUT)) {
                SettingsGroup(modifier = Modifier.padding(top = AppSpacing.betweenCards)) {
                    SettingsRow(
                        icon = Icons.Filled.Info,
                        route = SettingsRoute.ABOUT,
                        value = "نسخه ${toFa(BuildConfig.VERSION_NAME)}",
                        onClick = { onOpen(SettingsRoute.ABOUT) },
                    )
                }
            }
            Box(modifier = Modifier.padding(bottom = 16.dp))
        }
    }
}

/**
 * ردیفِ تنظیمات - حالا فقط پوسته‌ای رو [SettingsRowItem]ِ واژگانِ مشترکه (فریمِ `27d`، بخشِ ب).
 * قبلاً هر ردیف کارتِ جدای خودش رو داشت؛ طرح ردیف‌ها رو **داخلِ یه کارتِ گروه** می‌خواد.
 */
@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    route: SettingsRoute,
    value: String? = null,
    tone: SettingsTone = SettingsTone.NEUTRAL,
    status: String? = null,
    statusTone: StatusTone = StatusTone.NEUTRAL,
    onClick: () -> Unit,
) {
    SettingsRowItem(
        title = route.title,
        icon = icon,
        tone = tone,
        status = status ?: value ?: routeHint(route),
        statusTone = statusTone,
        onClick = onClick,
    )
}

@Composable
private fun SettingsSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    tone: SettingsTone = SettingsTone.NEUTRAL,
    onCheckedChange: (Boolean) -> Unit,
) {
    // زیرصفحه‌ها هنوز سوییچ‌های تکی دارن که تو گروه نیستن - همون‌جا کارتِ خودشون رو نگه می‌دارن.
    SettingsGroup(modifier = Modifier.padding(top = 8.dp)) {
        SettingsRowItem(
            title = title,
            icon = icon,
            tone = tone,
            status = subtitle,
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun SettingsSectionLabel(text: String, subtitle: String? = null, accent: Color = AppPrimary) =
    SettingsGroupLabel(text, subtitle, accent)

/** زیرنویسِ ردیف‌های ریشه - فقط کاری را که همان زیرصفحه واقعاً دارد می‌گوید. */
private fun routeHint(route: SettingsRoute): String? = when (route) {
    SettingsRoute.SMS -> "خواندن و ثبتِ خودکارِ پیامک‌های بانکی"
    SettingsRoute.BACKGROUND -> "برای ثبتِ خودکار و به‌روز ماندنِ اطلاعات"
    SettingsRoute.DATA -> "پشتیبان‌گیری، بازیابی و پاک‌سازیِ داده‌ها"
    SettingsRoute.REMINDERS -> "یادآوریِ سررسید و ثبتِ روزانه"
    SettingsRoute.APPEARANCE -> "حالتِ روشن و تیره، اندازه‌ی متن و انیمیشن"
    SettingsRoute.SECURITY -> "قفل با رمزِ عددی و اثرِ انگشت"
    SettingsRoute.TOOLS -> "تقویمِ مالی، آمار و گزارش"
    else -> null
}

/** سرآیندِ مشترکِ همه‌ی زیرصفحه‌های تنظیمات (عنوان وسط + ضربدرِ بستن) - هم‌شکلِ اپِ مرجع. */
@Composable
private fun SettingsSubPageScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppSurface)
                    .border(1.dp, AppLine, RoundedCornerShape(14.dp))
                    .pressScaleClickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Close, contentDescription = "بستن", tint = AppMuted)
            }
            Text(
                title,
                color = AppText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            // هم‌عرضِ دکمه‌ی بستن، تا عنوان دقیقاً وسط بمونه.
            Box(modifier = Modifier.size(52.dp))
        }
        Column(modifier = Modifier.padding(horizontal = 14.dp)) { content() }
        Box(modifier = Modifier.padding(bottom = 20.dp))
    }
}

@Composable
private fun SettingsSubPage(
    route: SettingsRoute,
    onBack: () -> Unit,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel,
    notificationsViewModel: NotificationsViewModel,
    appLockViewModel: AppLockViewModel,
    autoBackupViewModel: AutoBackupViewModel,
    smsAutoImportViewModel: SmsAutoImportViewModel,
    onShowLoginPrompt: () -> Unit,
    onShowSubscription: () -> Unit,
    onShowReminderSettings: () -> Unit,
    onOpenTool: (String) -> Unit,
    onOpenRules: () -> Unit,
    onOpenBugReport: () -> Unit,
) {
    val banner = rememberInAppBanner()
    // فقط برای ردیفِ آزمایشیِ سکه - نمونه‌ی خودِ همین زیرصفحه، نه پارامترِ تازه‌ی امضا.
    val gamification: GamificationViewModel = hiltViewModel()

    // 🚨 **عمداً بیرونِ `SettingsSubPageScaffold`**: آن اسکافولد یک `Column(verticalScroll)`
    // است و فروشگاه خودش فهرستِ تنبل دارد - اسکرولِ تودرتو با ارتفاعِ بی‌نهایت اپ را
    // می‌کشد (همان کرشِ «افزودن از پیامک‌ها»). هدرِ خودش را دارد.
    if (route == SettingsRoute.COLOR_THEME) {
        Box(modifier = Modifier.fillMaxSize().background(AppBg)) {
            // همان صفحه‌ی ادغام‌شده‌ی سکه، روی تبِ فروشگاه (پیش‌فرض) - نه یک نسخه‌ی دوم.
            CoinHubScreen(onBack = onBack, todayHasEntry = gamification.todayLogged.collectAsState().value)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SettingsSubPageScaffold(title = route.title, onBack = onBack) {
            when (route) {
                SettingsRoute.ACCOUNT -> AccountSettings(authViewModel, banner, onShowLoginPrompt, onShowSubscription)
                SettingsRoute.APPEARANCE -> AppearanceSettings(themeViewModel)
                SettingsRoute.REMINDERS -> ReminderToggles(notificationsViewModel, onShowReminderSettings)
                SettingsRoute.DATA -> DataSettings(authViewModel, autoBackupViewModel, banner)
                SettingsRoute.SMS -> SmsSettings(smsAutoImportViewModel, onOpenRules = { onOpenRules() })
                SettingsRoute.BACKGROUND -> BackgroundRunSettings()
                SettingsRoute.TOOLS -> ToolsSettings(onOpenTool = onOpenTool)
                // دیگه تو یه AppCardِ بیرونی پیچیده نمی‌شه - خودش گروه‌های خودشو داره.
                SettingsRoute.SECURITY -> SecuritySettings(appLockViewModel)
                SettingsRoute.PARSING_RULES -> ParsingRulesScreen()
                SettingsRoute.BADGES -> BadgesScreen()
                SettingsRoute.ABOUT -> AboutSettings(banner, onOpenBugReport)
                // بالاتر زودتر return شده - این شاخه فقط برای کاملِ‌بودنِ `when` است.
                SettingsRoute.COLOR_THEME -> Unit
                SettingsRoute.MAIN -> Unit
            }
        }
        InAppBannerHost(banner, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun AccountSettings(
    authViewModel: AuthViewModel,
    banner: InAppBannerState,
    onShowLoginPrompt: () -> Unit,
    onShowSubscription: () -> Unit,
) {
    val gateState by authViewModel.gateState.collectAsState()
    val phone by authViewModel.phone.collectAsState()
    val subscribed by authViewModel.subscribed.collectAsState()
    val trialDaysLeft by authViewModel.trialDaysLeft.collectAsState()
    val subscribedUntil by authViewModel.subscribedUntil.collectAsState()
    val subscriptionTier by authViewModel.subscriptionTier.collectAsState()
    val savedName by authViewModel.userName.collectAsState()
    var showDeleteAccountConfirm by remember { mutableStateOf(false) }
    var deleteAccountInProgress by remember { mutableStateOf(false) }

    var showAvatarSheet by remember { mutableStateOf(false) }
    var showNameSheet by remember { mutableStateOf(false) }

    if (gateState == GateState.LOGGED_IN) {
        // ── کارتِ هویت (فریمِ حساب کاربری) ────────────────────────────────────
        // دکمه‌ی تمام‌عرضِ «تغییرِ آدمک» عمداً حذف شد - مدادِ روی خودِ آدمک همون کاره و
        // دکمه‌ی تمام‌عرض بالای صفحه وزنِ بی‌دلیل می‌گیره.
        val avatarViewModel: AvatarViewModel = hiltViewModel()
        val avatar by avatarViewModel.avatar.collectAsState()
        val avatarFrame by avatarViewModel.frame.collectAsState()
        // کارتِ هویتِ فشرده: صفحه با «خودِ کاربر» شروع می‌شود، نه یک فضای خالیِ بزرگ.
        // همان کارتِ رنگیِ بالای بقیه‌ی صفحه‌ها (با تم عوض می‌شود)؛ آدمک و قاب همان هدرِ خانه.
        AppHeroCard(modifier = Modifier.padding(top = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
                    Box(
                        modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        FramedAvatar(avatar, size = 68.dp, frame = avatarFrame)
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AppSurface)
                            .border(1.5.dp, AppPrimaryBorder, CircleShape)
                            .pressScaleClickable { showAvatarSheet = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "تغییرِ آدمک",
                            tint = AppPrimaryInk,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f).padding(start = 13.dp)) {
                    Text(
                        if (savedName.isNullOrBlank()) "بی‌نام" else savedName!!,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Ltr {
                        Text(
                            toFa(phone ?: ""),
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                    Text(
                        if (subscribed) "اشتراک فعال" else "حساب معمولی",
                        color = if (subscribed) AppAccent else Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 7.dp),
                    )
                }
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Badge, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
        }

        // ── گروهِ مشخصات ─────────────────────────────────────────────────────
        // ⚠️ ردیفِ **ایمیل** پیاده نشد: نه اپ و نه سرور هیچ‌جا ایمیل نگه نمی‌دارن و بازیابی
        // فقط با شماره‌ی موبایله. فیلدی که هیچ‌جا استفاده نمی‌شه بدتر از نبودنشه.
        SettingsGroupLabel("مشخصات")
        SettingsGroup {
            SettingsRowItem(
                title = "نام",
                icon = Icons.Filled.Badge,
                tone = SettingsTone.NEUTRAL,
                status = if (savedName.isNullOrBlank()) "ثبت نشده" else savedName,
                onClick = { showNameSheet = true },
            )
            SettingsDivider()
            SettingsRowItem(
                title = "شماره‌ی موبایل",
                icon = Icons.Filled.Person,
                tone = SettingsTone.BLUE,
                status = toFa(phone ?: ""),
                statusTone = StatusTone.HEALTHY,
                value = "تأییدشده",
            )
        }

        if (showAvatarSheet) {
            AlertDialog(
                onDismissRequest = { showAvatarSheet = false },
                confirmButton = { TextButton(onClick = { showAvatarSheet = false }) { Text("تمام") } },
                title = { Text("آدمکت را انتخاب کن", fontWeight = FontWeight.Black) },
                text = { AvatarPicker(avatar = avatar, onChange = { avatarViewModel.save(it) }) },
            )
        }
        if (showNameSheet) {
            var nameDraft by remember(savedName) { mutableStateOf(savedName ?: "") }
            AlertDialog(
                onDismissRequest = { showNameSheet = false },
                confirmButton = {
                    TextButton(
                        onClick = { authViewModel.updateName(nameDraft); showNameSheet = false },
                    ) {
                        Text(if (nameDraft.isBlank()) "حذفِ نام" else "ذخیره")
                    }
                },
                dismissButton = { TextButton(onClick = { showNameSheet = false }) { Text("بی‌خیال") } },
                title = { Text("نام", fontWeight = FontWeight.Black) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = nameDraft,
                            onValueChange = { if (it.length <= 30) nameDraft = it },
                            singleLine = true,
                            placeholder = { Text("مثلاً ابراهیم") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            "رو سربرگِ خروجیِ PDF و اکسل نوشته می‌شه. خالی گذاشتنش هیچ مشکلی نداره.",
                            color = AppMuted,
                            fontSize = 10.5.sp,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                },
            )
        }
        AppCard(
            backgroundColor = AppPrimaryPill,
            borderColor = AppPrimaryBorder,
            modifier = Modifier.padding(top = AppSpacing.betweenCards),
            contentPadding = 16.dp,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (subscribed) AppGoldPillSoft else AppSurface),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.WorkspacePremium,
                        contentDescription = null,
                        tint = if (subscribed) AppGoldInk else AppMuted,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    // متنِ نوعِ اشتراک - رجوع کن به توضیحِ کاملِ همین منطق تو AuthViewModel/سرور:
                    // subscriptionTier فقط برای خریدِ واقعیِ زمان‌دار پر می‌شه.
                    val tierLabel = when (subscriptionTier) {
                        "1m" -> "اشتراک یک‌ماهه"
                        "3m" -> "اشتراک سه‌ماهه"
                        "6m" -> "اشتراک شش‌ماهه"
                        "1y" -> "اشتراک یک‌ساله"
                        else -> null
                    }
                    Text(
                        when {
                            !subscribed -> "نسخه‌ی عادی"
                            tierLabel != null -> tierLabel
                            else -> "مشترک"
                        },
                        color = AppText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                    )
                    // تاریخِ انقضا + شمارشِ روزِ باقی‌مونده (خواسته‌ی صریحِ کاربر، هم‌الگو با
                    // اپِ رفرنس: «تا ۱۳ شهریور ۱۴۰۵ (۲۸ روز دیگر)») - قبلاً فقط بجِ آزمایشی/دائمی بود.
                    val expiry = remember(subscribedUntil) { parseSubscribedUntil(subscribedUntil) }
                    if (subscribed && trialDaysLeft != null && trialDaysLeft in 1..7) {
                        Text(
                            "دوره‌ی آزمایشی رایگان: ${toFa(trialDaysLeft.toString())} روز مانده",
                            color = AppAccent,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    } else if (subscribed && expiry != null) {
                        Text(
                            "تا ${toFa(expiry.date.d)} ${persianMonthName(expiry.date.m)} ${toFa(expiry.date.y)} " +
                                "(${toFa(expiry.daysLeft)} روزِ دیگه)",
                            color = AppAccent,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    } else if (subscribed && subscribedUntil == null) {
                        Text("اشتراک دائمی", color = AppAccent, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(AppSurface)
                    .border(1.dp, AppPrimaryBorder, RoundedCornerShape(999.dp))
                    .pressScaleClickable(scale = 0.98f, onClick = onShowSubscription)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (subscribed) "مدیریت اشتراک" else "مشاهده پلن‌ها",
                    color = AppPrimaryInk,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(AppPrimaryPill),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = null, tint = AppPrimaryInk, modifier = Modifier.size(20.dp))
                }
            }
        }
        // ── خروج ─────────────────────────────────────────────────────────────
        // فاصله‌ی **دو برابرِ** فاصله‌ی معمولِ کارت‌ها - تنها جای برنامه که فاصله‌ی
        // غیرِتوکن مجازه، چون دکمه‌ی مخرب نباید تو ریتمِ عادیِ صفحه بشینه.
        var showLogoutConfirm by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppSpacing.betweenCards * 2)
                .height(76.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(AppDangerPill)
                .border(1.dp, AppDanger.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                .pressScaleClickable { showLogoutConfirm = true }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)).background(AppDanger.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = AppDanger,
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                "خروج از حساب",
                color = AppDanger,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f).padding(start = 12.dp),
            )
            Box(
                modifier = Modifier.size(30.dp).clip(CircleShape).background(AppDanger.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = null, tint = AppDanger, modifier = Modifier.size(18.dp))
            }
        }
        // الزامِ فروشگاه‌ها: راهِ داخل‌برنامه‌ای برای حذفِ کاملِ حساب. **ظاهرِ کم‌وزن،
        // مسیرِ سخت** - برعکسِ خروج که ظاهرِ پروزن و مسیرِ آسون داره.
        Text(
            "حذفِ کاملِ حساب کاربری",
            color = AppMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp)
                .pressScaleClickable { showDeleteAccountConfirm = true },
        )
        if (showLogoutConfirm) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirm = false },
                confirmButton = {
                    TextButton(onClick = { showLogoutConfirm = false; authViewModel.logout() }) {
                        Text("خروج", color = AppDanger)
                    }
                },
                dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text("بمانم") } },
                title = { Text("از حساب خارج می‌شوی؟", fontWeight = FontWeight.Black) },
                // جمله‌ی دوم لازمه: کاربری که برای رفعِ یه اشکال خارج می‌شه باید بدونه تا
                // ورودِ بعدی تراکنش‌هاش خودکار ثبت نمی‌شن.
                text = {
                    Text(
                        "داده‌هات رو سرور می‌مونه و با ورودِ دوباره برمی‌گرده. ثبتِ خودکارِ " +
                            "پیامک تا وقتی خارج باشی کار نمی‌کنه.",
                        lineHeight = 21.sp,
                    )
                },
            )
        }
    } else {
        AppCard(modifier = Modifier.padding(top = 8.dp)) {
            Text("ورود به حساب انجام نشده", color = AppText, fontSize = 15.sp)
            Text(
                "برای همگام‌سازی ابری وارد شو",
                color = AppMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
            GradientButton(onClick = onShowLoginPrompt, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Text("ورود")
            }
        }
        AppCard(modifier = Modifier.padding(top = 8.dp)) {
            OutlinedButton(onClick = onShowSubscription, modifier = Modifier.fillMaxWidth()) {
                Text("مشاهده پلن‌های اشتراک")
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
                TextButton(onClick = { showDeleteAccountConfirm = false }, enabled = !deleteAccountInProgress) {
                    Text("انصراف")
                }
            },
        )
    }
}

@Composable
private fun AppearanceSettings(themeViewModel: ThemeViewModel) {
    val themeMode by themeViewModel.themeMode.collectAsState()
    val fontScale by themeViewModel.fontScale.collectAsState()
    val reducedMotion by themeViewModel.reducedMotion.collectAsState()
    val isDark = AppIsDark
    // همون افکتِ دایره‌ایِ تعویضِ تم که از مرکزِ خودِ گزینه‌ی زده‌شده باز می‌شه (ThemeReveal.kt).
    val themeReveal = LocalThemeReveal.current
    val chipCenters = remember { mutableStateMapOf<ThemeMode, Offset>() }
    val themeToggleScope = rememberCoroutineScope()

    SettingsHero(
        Icons.Filled.Palette,
        "ظاهرِ برنامه",
        "پوسته، اندازه‌ی متن و حرکت",
        badge = themeModeOptions.firstOrNull { it.first == themeMode }?.second,
    )
    // ── سه‌حالتیِ روشن · تیره · سیستم ─────────────────────────────────────────
    SettingsGroupLabel("پوسته")
    SettingsGroup {
        Row(
            modifier = Modifier.fillMaxWidth().padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            themeModeOptions.forEach { (mode, label) ->
                val selected = themeMode == mode
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) AppPrimary else AppSurface2)
                        .pressScaleClickable {
                            if (mode != themeMode && !themeReveal.inProgress) {
                                val origin = chipCenters[mode] ?: Offset.Zero
                                themeToggleScope.launch {
                                    themeReveal.startReveal(origin = origin, currentKey = themeMode)
                                    themeViewModel.setThemeMode(mode)
                                }
                            } else {
                                themeViewModel.setThemeMode(mode)
                            }
                        }
                        .onGloballyPositioned { chipCenters[mode] = it.boundsInRoot().center },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        when (mode) {
                            ThemeMode.LIGHT -> Icons.Filled.LightMode
                            ThemeMode.DARK -> Icons.Filled.DarkMode
                            ThemeMode.SYSTEM -> Icons.Filled.BrightnessAuto
                        },
                        contentDescription = null,
                        tint = if (selected) Color.White else AppMuted,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        label,
                        color = if (selected) Color.White else AppMuted,
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        fontWeight = if (selected) FontWeight.Black else FontWeight.ExtraBold,
                        modifier = Modifier.padding(start = 5.dp),
                    )
                }
            }
        }
    }
    // خطِ خبری - **فقط** تو حالتِ سیستم دیده می‌شه.
    if (themeMode == ThemeMode.SYSTEM) {
        Text(
            if (isDark) "الان تیره است، چون گوشی‌ات تیره است." else "الان روشن است، چون گوشی‌ات روشن است.",
            color = AppMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 6.dp, end = 4.dp),
        )
    }

    // ── گروهِ خواندن ──────────────────────────────────────────────────────────
    SettingsGroupLabel("خوانایی")
    SettingsGroup {
        SettingsRowItem(
            title = "اندازه‌ی متن",
            icon = Icons.Filled.FormatSize,
            tone = SettingsTone.PURPLE,
            status = fontSizeOptions.firstOrNull { it.first == fontScale }?.second ?: "معمولی",
            onClick = null,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            fontSizeOptions.forEach { (scale, label) ->
                AppChip(
                    label = label,
                    selected = fontScale == scale,
                    onClick = { themeViewModel.setFontScale(scale) },
                )
            }
        }
        // پیش‌نمایشِ زنده - اندازه‌ی انتخابی همین حالا روی کلِ برنامه نشسته، پس همین متن نمونه‌اش است.
        Text(
            "نمونه: امروز ۲۵۰٬۰۰۰ تومان خرجِ خوراک ثبت شد.",
            color = AppText,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AppSurface2)
                .padding(12.dp),
        )
    }

    // ── گروهِ حرکت ────────────────────────────────────────────────────────────
    SettingsGroupLabel("حرکت")
    SettingsGroup {
        SettingsRowItem(
            title = "انیمیشنِ کم",
            icon = Icons.Filled.Animation,
            tone = SettingsTone.NEUTRAL,
            status = "برای گوشی‌های کم‌قدرت",
            checked = reducedMotion,
            onCheckedChange = { themeViewModel.setReducedMotion(it) },
        )
    }
}

@Composable
private fun ReminderToggles(
    notificationsViewModel: NotificationsViewModel,
    onShowReminderSettings: () -> Unit,
) {
    val context = LocalContext.current
    val notificationsEnabled by notificationsViewModel.enabled.collectAsState()
    val dailyExpenseReminderEnabled by notificationsViewModel.dailyExpenseReminderEnabled.collectAsState()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) notificationsViewModel.enable() }
    val dailyPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) notificationsViewModel.enableDailyExpenseReminder() }

    /** مجوزِ نوتیفیکیشن فقط از اندروید ۱۳ (TIRAMISU) به بعد لازمه. */
    fun withNotificationPermission(onGranted: () -> Unit, launcher: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            onGranted()
        } else {
            launcher()
        }
    }

    var permissionGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }

    val anyReminderOn = notificationsEnabled || dailyExpenseReminderEnabled
    SettingsHero(
        Icons.Filled.NotificationsActive,
        "یادآورها",
        "سررسیدها و یادآورِ ثبتِ روزانه",
        badge = if (!permissionGranted) "اجازه نیست" else if (anyReminderOn) "فعال" else "خاموش",
    )
    // ── کارتِ اجازه - **بالای صفحه، نه پایین** ────────────────────────────────
    // بی اجازه هیچ‌کدوم از این کلیدها کار نمی‌کنه، و کاربری که کلید رو روشن می‌کنه و
    // خبری نمی‌شه به برنامه بی‌اعتماد می‌شه. وقتی اجازه هست، کارت **کلاً نیست** -
    // نه کارتِ سبزِ «همه‌چیز خوبه».
    //
    // ⚠️ این اجازه‌ی **اعلانِ عادی**ه (`POST_NOTIFICATIONS`)، نه دسترسیِ خواندنِ
    // اعلانِ بانک‌ها. دو چیزِ جدا با دو مسیرِ جدا - متن‌هاشون قاطی نشه.
    if (!permissionGranted) {
        AppCard(variant = AppCardVariant.URGENT, modifier = Modifier.padding(top = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppDanger),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.NotificationsOff,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("اجازه‌ی اعلان نیست", color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Black)
                    Text(
                        "بی اجازه، هیچ یادآوری‌ای نمی‌رسه.",
                        color = AppDangerInk,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            GradientButton(
                onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                modifier = Modifier.fillMaxWidth().padding(top = 11.dp),
            ) {
                Text("اجازه بده")
            }
        }
    }

    // ── گروهِ یادآورِ روزانه (عادت‌سازی) - خاموشِ پیش‌فرض ───────────────────────
    SettingsGroupLabel("روزانه", accent = AppWarningInk)
    SettingsGroup {
        SettingsRowItem(
            title = "یادآورِ ثبتِ روزانه",
            icon = Icons.Filled.Notifications,
            tone = SettingsTone.ORANGE,
            // یادآورِ هوشمند از قبل **همیشه روشنه**: اگه امروز چیزی ثبت کرده باشی
            // اصلاً فرستاده نمی‌شه. کلیدِ جدا براش نذاشتم - کسی «اعلانِ بی‌معنی» نمی‌خواد.
            status = if (dailyExpenseReminderEnabled) "فقط روزهایی که چیزی ثبت نکردی" else "خاموش",
            statusTone = if (dailyExpenseReminderEnabled) StatusTone.HEALTHY else StatusTone.NEUTRAL,
            checked = dailyExpenseReminderEnabled,
            onCheckedChange = { checked ->
                if (!checked) {
                    notificationsViewModel.disableDailyExpenseReminder()
                } else {
                    withNotificationPermission(
                        onGranted = { notificationsViewModel.enableDailyExpenseReminder() },
                        launcher = { dailyPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                    )
                }
            },
        )
    }

    // ── گروهِ سررسیدها (خطر) - روشنِ پیش‌فرض ───────────────────────────────────
    SettingsGroupLabel("سررسیدها", accent = AppDanger)
    SettingsGroup {
        SettingsRowItem(
            title = "یادآوریِ سررسید",
            icon = Icons.Filled.Event,
            tone = SettingsTone.RED,
            status = if (notificationsEnabled) "قسط، چک و پرداختِ تکرارشونده" else "خاموش",
            statusTone = if (notificationsEnabled) StatusTone.HEALTHY else StatusTone.NEUTRAL,
            checked = notificationsEnabled,
            onCheckedChange = { checked ->
                if (!checked) {
                    notificationsViewModel.disable()
                } else {
                    withNotificationPermission(
                        onGranted = { notificationsViewModel.enable() },
                        launcher = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                    )
                }
            },
        )
        if (notificationsEnabled) {
            SettingsDivider()
            SettingsRowItem(
                title = "زمان‌بندی، صدا و ویبره",
                icon = Icons.Filled.Schedule,
                tone = SettingsTone.NEUTRAL,
                status = "چند روز قبل خبر بده، با چه صدایی",
                onClick = onShowReminderSettings,
            )
        }
    }
}

@Composable
private fun DataSettings(
    authViewModel: AuthViewModel,
    autoBackupViewModel: AutoBackupViewModel,
    banner: InAppBannerState,
) {
    val gateState by authViewModel.gateState.collectAsState()
    val subscribed by authViewModel.subscribed.collectAsState()
    val autoBackupEnabled by autoBackupViewModel.enabled.collectAsState()
    val lastAutoBackupAt by autoBackupViewModel.lastBackupAt.collectAsState()
    val cloudBackupFailed by autoBackupViewModel.cloudBackupFailed.collectAsState()

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

    // بازیابیِ در انتظارِ تایید («local»/«cloud») و قفلِ ضدِ دوبار زدن.
    var pendingRestore by remember { mutableStateOf<String?>(null) }
    var restoring by remember { mutableStateOf(false) }

    SettingsHero(
        Icons.Filled.CloudQueue,
        "وضعیتِ پشتیبان‌گیری",
        when {
            !autoBackupEnabled -> "پشتیبانِ خودکار خاموش است"
            cloudBackupFailed && lastBackupLabel != null -> "آخرین نسخه فقط روی گوشی ذخیره شد"
            lastBackupLabel != null -> "آخرین نسخه: $lastBackupLabel"
            else -> "هنوز نسخه‌ای ساخته نشده"
        },
        badge = if (autoBackupEnabled) "روشن" else "خاموش",
    )

    // ── کارتِ وضعیتِ پشتیبان - «یک نگاه، جواب می‌گیرد» ────────────────────────
    SettingsGroupLabel("پشتیبان‌گیری")
    SettingsGroup {
        SettingsRowItem(
            title = "پشتیبان‌گیریِ خودکارِ روزانه",
            icon = Icons.Filled.CloudUpload,
            tone = SettingsTone.GREEN,
            // ⚠️ «آخرین پشتیبان: امروز» وقتی آپلود شکست خورده، دروغِ خطرناکی است -
            // کاربر خیال می‌کند داده‌اش جای امنی هست. حالا شکستِ ابری صریح گفته می‌شود.
            status = when {
                !autoBackupEnabled -> "خاموش - هیچ نسخه‌ی پشتیبانی ساخته نمی‌شود"
                cloudBackupFailed && lastBackupLabel != null ->
                    "روی گوشی ذخیره شد ($lastBackupLabel) - ولی به فضای ابری نرفت"
                lastBackupLabel != null -> "آخرین پشتیبان: $lastBackupLabel"
                else -> "روشن - هنوز پشتیبانی ساخته نشده"
            },
            statusTone = when {
                !autoBackupEnabled -> StatusTone.NEUTRAL
                cloudBackupFailed && lastBackupLabel != null -> StatusTone.BROKEN
                lastBackupLabel != null -> StatusTone.HEALTHY
                else -> StatusTone.NEUTRAL
            },
            checked = autoBackupEnabled,
            onCheckedChange = { checked ->
                if (checked) autoBackupViewModel.enable() else autoBackupViewModel.disable()
            },
        )
    }

    // ── گروهِ بازیابی ─────────────────────────────────────────────────────────
    if (lastAutoBackupAt != null) {
        SettingsGroupLabel("بازیابی")
        SettingsGroup {
            SettingsRowItem(
                title = "بازیابی از پشتیبانِ همین گوشی",
                icon = Icons.Filled.Restore,
                tone = SettingsTone.NEUTRAL,
                status = lastBackupLabel?.let { "نسخه‌ی $it" },
                onClick = { if (!restoring) pendingRestore = "local" },
            )
            // بازیابی از سرور فقط برای کاربرِ واردشده‌ی مشترکه - پوش به سرور هم فقط برای همونه.
            if (gateState == GateState.LOGGED_IN && subscribed) {
                SettingsDivider()
                SettingsRowItem(
                    title = "بازیابی از سرورِ ابری",
                    icon = Icons.Filled.CloudDownload,
                    tone = SettingsTone.GREEN,
                    status = "برای وقتی گوشی عوض شده یا برنامه پاک شده",
                    onClick = { if (!restoring) pendingRestore = "cloud" },
                )
            }
        }
    }

    // 🚨 بازیابی داده‌های فعلیِ گوشی را جایگزین می‌کند - پس اول تاییدِ صریح.
    pendingRestore?.let { source ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text(if (source == "cloud") "بازیابی از سرورِ ابری؟" else "بازیابی از پشتیبانِ گوشی؟", fontWeight = FontWeight.Black) },
            text = {
                Text(
                    "اطلاعاتِ فعلیِ برنامه با " +
                        (if (source == "cloud") "آخرین نسخه‌ی روی سرور" else "نسخه‌ی ${lastBackupLabel ?: "ذخیره‌شده"}") +
                        " جایگزین می‌شه. هر چیزی که بعد از اون نسخه ثبت کردی از بین می‌ره.",
                    lineHeight = 21.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingRestore = null
                    restoring = true
                    val done: (Boolean) -> Unit = { ok ->
                        restoring = false
                        banner.show(
                            when {
                                ok && source == "cloud" -> "بازیابی از سرور ابری انجام شد"
                                ok -> "بازیابی از پشتیبان خودکار انجام شد"
                                source == "cloud" -> "پشتیبانی رو سرور ابری پیدا نشد"
                                else -> "پشتیبانی برای بازیابی پیدا نشد"
                            },
                            isSuccess = ok,
                        )
                    }
                    if (source == "cloud") autoBackupViewModel.restoreFromCloud(done) else autoBackupViewModel.restoreFromAutoBackup(done)
                }) { Text("بازیابی کن", color = AppDanger) }
            },
            dismissButton = { TextButton(onClick = { pendingRestore = null }) { Text("بی‌خیال") } },
        )
    }
}

@Composable
private fun SmsSettings(
    smsAutoImportViewModel: SmsAutoImportViewModel,
    onOpenRules: () -> Unit,
    accountViewModel: AccountViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val enabled by smsAutoImportViewModel.enabled.collectAsState()
    val accounts by accountViewModel.accounts.collectAsState()
    val scope = rememberCoroutineScope()
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionGranted = granted
        if (granted) smsAutoImportViewModel.enable()
    }
    var showParseTest by remember { mutableStateOf(false) }
    var showSmsImport by remember { mutableStateOf(false) }

    // ⚠️ **این‌جا `return` نذار.** نسخه‌ی قبلی صفحه‌ی آزمایش رو همین‌جا (به‌جای بقیه‌ی محتوا)
    // رندر می‌کرد، ولی `SmsSettings` خودش داخلِ `SettingsSubPageScaffold` (یه
    // `Column(verticalScroll)`) رندر می‌شه و `SmsParseTestScreen` هم اسکافولدِ اسکرول‌دارِ
    // خودش رو می‌سازه - اسکرولِ عمودیِ تودرتو با ارتفاعِ بی‌نهایت اندازه‌گیری می‌شه و اپ کرش
    // می‌کنه (گزارشِ واقعیِ کاربر: «رو تست می‌زنم، از برنامه می‌پره بیرون»). دیالوگِ
    // تمام‌صفحه ویندوی جداگانه دارد، پس اسکرولش تو اسکرولِ والد نمی‌افته.
    if (showParseTest) {
        FullScreenDialog(onDismissRequest = { showParseTest = false }) {
            Box(modifier = Modifier.fillMaxSize().background(AppBg)) {
                SmsParseTestScreen(onBack = { showParseTest = false })
            }
        }
    }

    // همان قاعده‌ی بالا: فهرستِ پیامک‌ها اسکرولِ خودش را دارد، پس داخلِ اسکرولِ تنظیمات
    // رندر نمی‌شود بلکه دیالوگِ تمام‌صفحه می‌گیرد.
    if (showSmsImport) {
        FullScreenDialog(onDismissRequest = { showSmsImport = false }) {
            // 🚨 **عمداً بدونِ `SettingsSubPageScaffold`**: آن اسکافولد یک
            // `Column(verticalScroll)` است و `SmsImportScreen` خودش فهرستِ تنبل دارد -
            // اسکرولِ عمودیِ تودرتو با ارتفاعِ بی‌نهایت اندازه‌گیری می‌شود و اپ **کرش می‌کند**
            // (گزارشِ واقعیِ کاربر: «روی افزودن پیامک می‌زنم، برنامه یهو بسته می‌شود» - دقیقاً
            // همان باگی که یک‌بار برای «آزمایشِ تشخیص» رخ داد). هدرِ خودش را دارد.
            Box(modifier = Modifier.fillMaxSize().background(AppBg)) {
                SmsImportScreen(onBack = { showSmsImport = false })
            }
        }
    }

    // ── کارتِ وضعیت - تنها کارتِ برجسته‌ی صفحه، سه حالت ──────────────────────
    // تصمیمِ تاییدشده‌ی طراح: اگه اجازه قطع شده، **کلید حالتِ چهارم نمی‌گیره** - همین کارت
    // به حالتِ خطا می‌ره و دکمه‌ی «اجازه بده» می‌گیره.
    var senderPickerFor by remember { mutableStateOf<AccountEntity?>(null) }
    val listed = accounts.filter { it.type == ACCOUNT_TYPE_BANK }
    val activeCount = listed.count { it.smsEnabled && !it.smsSender.isNullOrBlank() }
    SmsStatusCard(
        enabled = enabled,
        permissionGranted = permissionGranted,
        activeCount = activeCount,
        totalCount = listed.size,
        onToggle = { checked ->
            if (!checked) {
                smsAutoImportViewModel.disable()
            } else if (permissionGranted) {
                smsAutoImportViewModel.enable()
            } else {
                smsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
            }
        },
        onGrant = { smsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS) },
    )

    // ── گروهِ بانک‌ها ─────────────────────────────────────────────────────────
    // حساب‌کتابِ بی‌سرشماره هم میاد (ته فهرست) - تنها جاییه که کاربر می‌فهمه وصل نیست.
    if (listed.isNotEmpty()) {
        SettingsGroupLabel("بانک‌ها")
        val ordered = listed.sortedBy { it.smsSender.isNullOrBlank() }
        SettingsGroup(
            modifier = Modifier
                .then(if (enabled) Modifier else Modifier.alpha(0.5f)),
        ) {
            ordered.forEachIndexed { index, account ->
                val hasSender = !account.smsSender.isNullOrBlank()
                SettingsRowItem(
                    title = account.name,
                    icon = Icons.Filled.AccountBalance,
                    tone = if (hasSender) SettingsTone.GREEN else SettingsTone.NEUTRAL,
                    status = smsStatusText(account),
                    statusTone = smsStatusTone(account),
                    // سرشماره داره → کلید. نداره → شِورون. **هیچ‌وقت هر دو.**
                    checked = if (hasSender && enabled) account.smsEnabled else null,
                    onCheckedChange = if (hasSender && enabled) {
                        { checked ->
                            scope.launch {
                                accountViewModel.updateAccount(account.copy(smsEnabled = checked))
                            }
                        }
                    } else {
                        null
                    },
                    // 🚨 بی این، ردیفِ بانکِ بی‌سرشماره شِورون داشت ولی تپش **هیچ کاری
                    // نمی‌کرد** - و تنها راهِ ثبتِ سرشماره تهِ فرمِ حسابِ بانکی بود، جایی که
                    // کاربر دنبالش نمی‌گردد (گزارشِ ۶.۱ی بازخوردِ دوم: «خواندنِ پیامک از رو
                    // گوشی اصلاً تو تنظیمات نیست»). حالا همین‌جا انتخابگر باز می‌شود.
                    onClick = if (hasSender) null else ({ senderPickerFor = account }),
                )
                if (index != ordered.lastIndex) SettingsDivider()
            }
        }
    }

    // ── گروهِ ابزارها ────────────────────────────────────────────────────────
    SettingsGroupLabel("ابزارها")
    SettingsGroup {
        SettingsRowItem(
            title = "افزودن از پیامک‌ها",
            icon = Icons.Filled.Sms,
            tone = SettingsTone.GREEN,
            status = "پیامکی که خودکار خوانده نشده را دستی ثبت کن",
            onClick = { showSmsImport = true },
        )
        SettingsDivider()
        SettingsRowItem(
            title = "آزمایشِ تشخیص",
            icon = Icons.Filled.Science,
            tone = SettingsTone.GREEN,
            status = "متنِ یک پیامک را امتحان کن",
            onClick = { showParseTest = true },
        )
        SettingsDivider()
        SettingsRowItem(
            title = "قاعده‌های تشخیص",
            icon = Icons.Filled.Rule,
            tone = SettingsTone.NEUTRAL,
            status = "دسته‌بندیِ خودکار بر اساسِ متنِ پیامک",
            onClick = { onOpenRules() },
        )
    }

    NotificationImportSettings(smsAutoImportViewModel)

    senderPickerFor?.let { account ->
        SmsSenderPickerDialog(
            onDismiss = { senderPickerFor = null },
            onPick = { sender ->
                scope.launch {
                    // سرشماره که ثبت شد، خواندن هم همان لحظه روشن می‌شود - وگرنه کاربر
                    // سرشماره را می‌دهد و هیچ اتفاقی نمی‌افتد تا کلیدِ دومی را پیدا کند.
                    accountViewModel.updateAccount(account.copy(smsSender = sender, smsEnabled = true))
                }
                senderPickerFor = null
            },
        )
    }
}

/**
 * زیرنویسِ ردیفِ هر بانک - **چهار حالته، مرزِ ۳۰ روز عمدیه** (قاعده‌ی صریحِ طراح: کوتاه‌ترش
 * کاربرِ کم‌تراکنش رو بی‌دلیل می‌ترسونه، بلندترش خرابیِ واقعی رو دیر می‌گه).
 */
private fun smsStatusText(account: AccountEntity): String {
    if (account.smsSender.isNullOrBlank()) return "سرشماره ثبت نشده"
    val last = account.lastSmsAt ?: return "هنوز پیامکی نیامده"
    val days = ((System.currentTimeMillis() - last) / 86_400_000L).toInt()
    return when {
        days > 30 -> "۳۰ روز پیامکی نیامده"
        days <= 0 -> "آخرین پیامک: امروز"
        days == 1 -> "آخرین پیامک: دیروز"
        else -> "آخرین پیامک: ${toFa(days)} روز پیش"
    }
}

private fun smsStatusTone(account: AccountEntity): StatusTone {
    if (account.smsSender.isNullOrBlank()) return StatusTone.NEUTRAL
    val last = account.lastSmsAt ?: return StatusTone.BROKEN
    val days = (System.currentTimeMillis() - last) / 86_400_000L
    return if (days > 30) StatusTone.BROKEN else StatusTone.NEUTRAL
}

@Composable
private fun SmsStatusCard(
    enabled: Boolean,
    permissionGranted: Boolean,
    activeCount: Int,
    totalCount: Int,
    onToggle: (Boolean) -> Unit,
    onGrant: () -> Unit,
) {
    val missingPermission = enabled && !permissionGranted
    AppCard(
        variant = if (missingPermission) AppCardVariant.URGENT else AppCardVariant.DEFAULT,
        backgroundColor = if (!missingPermission && enabled) AppPrimaryPill else null,
        borderColor = if (!missingPermission && enabled) AppPrimaryBorder else null,
        shadow = missingPermission,
        modifier = Modifier.padding(top = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            missingPermission -> AppUrgentShadow
                            enabled -> AppPrimary
                            else -> AppChipBg
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (missingPermission) Icons.Filled.Warning else Icons.Filled.Sms,
                    contentDescription = null,
                    tint = when {
                        missingPermission -> AppDanger
                        enabled -> Color.White
                        else -> AppLabel
                    },
                    modifier = Modifier.size(19.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    when {
                        missingPermission -> "اجازه‌ی خواندنِ پیامک قطع است"
                        enabled -> "ثبتِ خودکار روشن است"
                        else -> "ثبتِ خودکار خاموش است"
                    },
                    color = AppText,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    when {
                        missingPermission -> "بدونِ این اجازه پیامکِ بانک خوانده نمی‌شود."
                        enabled -> "${toFa(activeCount)} از ${toFa(totalCount)} بانک فعال"
                        else -> "تراکنش‌ها را دستی وارد می‌کنی"
                    },
                    color = when {
                        missingPermission -> AppDangerInk
                        enabled -> AppPrimaryInk
                        else -> AppMuted
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            // تو حالتِ «اجازه ندارد» کلید جاش رو به دکمه‌ی تمام‌عرضِ پایین می‌ده.
            if (!missingPermission) AppSwitch(checked = enabled, onCheckedChange = onToggle)
        }
        if (missingPermission) {
            GradientButton(
                onClick = onGrant,
                modifier = Modifier.fillMaxWidth().padding(top = 11.dp),
            ) {
                Text("اجازه بده")
            }
        }
    }
}

/**
 * صفحه‌ی **آزمایشِ تشخیص** - زیرصفحه‌ی خودش، نه شیت (خروجیش خونده می‌شه و شیت جا کم داره).
 *
 * ⚠️ حالتِ «ج»ی طرح (این متن اصلاً پیامکِ بانکی نیست) پیاده **نشد**، چون
 * `BankSmsParser.parse` برای هر دو حالت `null` برمی‌گردونه و از هم تفکیکشون نمی‌کنه -
 * خودِ طراح گفت اگه موتور تفکیک نمی‌کنه ولش کن.
 */
@Composable
private fun SmsParseTestScreen(onBack: () -> Unit) {
    var text by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<ParsedBankSms?>(null) }
    var checked by remember { mutableStateOf(false) }

    SettingsSubPageScaffold(title = "آزمایشِ تشخیص", onBack = onBack) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it; checked = false },
            placeholder = { Text("متنِ پیامکِ بانک را اینجا بچسبان") },
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 100.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, lineHeight = 20.sp),
        )
        Text(
            "متن ذخیره نمی‌شود.",
            color = AppLabel,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 6.dp),
        )
        GradientButton(
            onClick = { result = BankSmsParser.parse(text); checked = true },
            enabled = text.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) {
            Text("بررسی")
        }
        if (checked) {
            val parsed = result
            if (parsed != null) {
                AppCard(
                    backgroundColor = AppPrimaryPill,
                    borderColor = AppPrimaryBorder,
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = AppPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            "شناسایی شد",
                            color = AppText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                    ParseResultRow(
                        label = "مبلغ",
                        value = "${fmt(parsed.amountRial)} ریال",
                        valueColor = if (parsed.type == TransactionType.WITHDRAWAL) AppDangerInk else AppPrimaryInk,
                    )
                    ParseResultRow(
                        label = "نوع",
                        value = if (parsed.type == TransactionType.WITHDRAWAL) "برداشت" else "واریز",
                    )
                    ParseResultRow(
                        label = "کارت",
                        value = parsed.cardSuffix?.let { toFa(it) },
                    )
                }
            } else {
                AppCard(
                    variant = AppCardVariant.URGENT,
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    Text("شناسایی نشد", color = AppText, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Text(
                        "موتور از این متن مبلغ و نوعِ تراکنش درنیاورد. اگه این پیامکِ واقعیِ " +
                            "بانکته، متنش رو برای ما بفرست تا موتور بهترش کنیم.",
                        color = AppDangerInk,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}

/** یه ردیفِ کلید-مقدارِ نتیجه‌ی آزمایش. مقدارِ درنیامده «—»ی کم‌رنگ می‌شه، نه خالی. */

/**
 * **اجرا در پس‌زمینه** - جوابِ خواسته‌ی «برنامه همیشه باز باشه و بعدِ ری‌استارت خودش بیاد».
 *
 * ⚠️ عمداً چیزی را که شدنی نیست وعده نمی‌دهد: اندروید اجازه‌ی «همیشه باز ماندن» به هیچ اپی
 * نمی‌دهد. این صفحه دقیقاً همان سه شرطی را نشان می‌دهد که پس‌زمینه را زنده نگه می‌دارند و هر
 * کدام را با یک دکمه به صفحه‌ی مربوطه‌ی خودِ گوشی می‌برد - رجوع کن به [BackgroundRunHelp].
 */
@Composable
private fun BackgroundRunSettings() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // وضعیت باید بعدِ برگشتن از تنظیماتِ گوشی تازه بشه، وگرنه کاربر اجازه را می‌دهد و اینجا
    // هنوز «داده نشده» می‌بیند - همان قاعده‌ی «سوئیچ دروغ نمی‌گوید».
    var batteryOk by remember { mutableStateOf(BackgroundRunHelp.batteryUnrestricted(context)) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                batteryOk = BackgroundRunHelp.batteryUnrestricted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ═══ بازطراحی (دورِ ۹) ═══
    //
    // سه کارتِ بلندِ متنی بودند با دکمه‌ی جدا زیرشان؛ کاربر گفت «بهتر طراحی کن و تمامِ
    // باکس‌ها کلیک‌شدنی باشند». حالا هر مرحله یک ردیفِ فشرده است: شماره در دایره،
    // عنوان، یک خطِ توضیح، و قرصِ وضعیت - و **خودِ کارت** مقصدش را باز می‌کند.
    //
    // ⚠️ مرحله‌ی سوم عمداً کلیک‌پذیر **نیست**: قفلِ فهرستِ برنامه‌های اخیر از داخلِ هیچ
    // برنامه‌ای شدنی نیست و هیچ صفحه‌ای در گوشی برایش وجود ندارد. کارتی که تپ را قبول
    // کند و هیچ اتفاقی نیفتد بدتر از کارتِ بی‌تپ است، پس نشانه‌ی تپ هم نمی‌گیرد.
    // خلاصه‌ی وضعیت: قبل از متنِ راهنما، کاربر در یک نگاه می‌فهمد چه چیزی باقی مانده.
    // «آماده» فقط یعنی معافیتِ باتری داده شده - تضمینِ رسیدنِ هر پیامک نیست، و متن هم همین را می‌گوید.
    SettingsHero(
        Icons.Filled.BatterySaver,
        if (batteryOk) "پس‌زمینه برای کار آماده است" else "برای اجرای مطمئن، دو دقیقه وقت بگذار",
        if (batteryOk) "معافیتِ باتری فعال است؛ مراحلِ دستی را هم یک‌بار بررسی کن."
        else "چند مرحله‌ی کوتاه تا یادآورها و ثبتِ خودکار پایدار بمانند.",
        badge = if (batteryOk) "آماده" else "نیازمندِ اقدام",
    )
    SettingsGroupLabel("مراحل")

    BackgroundStepCard(
        step = 1,
        title = "معافیت از بهینه‌سازیِ باتری",
        body = "بدونِ این، گوشی بعد از چند دقیقه کارهای پس‌زمینه را متوقف می‌کند و یادآورِ " +
            "سررسید دیر می‌رسد یا اصلاً نمی‌رسد.",
        done = batteryOk,
        onClick = { runCatching { context.startActivity(BackgroundRunHelp.batteryIntent(context)) } },
    )

    if (BackgroundRunHelp.needsAutostartSetting()) {
        BackgroundStepCard(
            step = 2,
            title = "اجرای خودکار بعد از روشن‌شدنِ گوشی",
            body = "سازنده‌ی این گوشی «اجرای خودکار» را پیش‌فرض خاموش می‌گذارد؛ تا روشن نشود، " +
                "بعد از خاموش‌وروشن‌کردنِ گوشی پیامکِ بانکی خودکار ثبت نمی‌شود.\n" +
                BackgroundRunHelp.autostartHint(),
            // وضعیتش از بیرون خواندنی نیست (هر سازنده جای خودش را دارد)، پس ادعای
            // «انجام شده» نمی‌کنیم - قرص خاکستریِ «باز کن» می‌مانَد.
            done = false,
            onClick = {
                // اگر صفحه‌ی مخصوصِ سازنده پیدا نشد، صفحه‌ی اطلاعاتِ خودِ برنامه باز می‌شود -
                // بن‌بست بهتر از کرشِ ActivityNotFound.
                val intent = BackgroundRunHelp.autostartIntent(context)
                    ?: BackgroundRunHelp.appDetailsIntent(context)
                runCatching { context.startActivity(intent) }
            },
        )
    }

    // بندِ ۷ِ دورِ ۹: این مرحله از داخلِ هیچ برنامه‌ای شدنی نیست، پس مقصد ندارد - ولی
    // کارتی که تپ نمی‌گیرد باید **بگوید** دستورالعمل است، وگرنه کاربر تپ می‌زند و فکر
    // می‌کند خراب است. پس بجِ «دستی»، و متن با **فعل** شروع می‌شود.
    BackgroundStepCard(
        step = 3,
        title = "در فهرستِ برنامه‌های اخیر، جیبک را قفل کن",
        body = "کلیدِ مربع (برنامه‌های اخیر) را بزن، روی کارتِ جیبک نگه دار و گزینه‌ی قفل را بزن. " +
            "بعد از آن، بستنِ همه‌ی برنامه‌ها دیگر جیبک را نمی‌بندد.",
        done = false,
        onClick = null,
        manualBadge = true,
    )
}

/**
 * یک مرحله‌ی «اجرا در پس‌زمینه» - خودِ کارت مقصد را باز می‌کند (دورِ ۹).
 *
 * [onClick] اگر `null` باشد کارت هیچ نشانه‌ی تپی نمی‌گیرد؛ مرحله‌ای که از داخلِ برنامه
 * شدنی نیست نباید وانمود کند دکمه است.
 */
@Composable
private fun BackgroundStepCard(
    step: Int,
    title: String,
    body: String,
    done: Boolean,
    onClick: (() -> Unit)?,
    /** مرحله‌ای که خودِ کاربر باید بیرونِ برنامه انجامش بدهد - بجِ «دستی» می‌گیرد. */
    manualBadge: Boolean = false,
) {
    AppCard(
        modifier = Modifier
            .padding(top = 10.dp)
            .then(
                if (onClick != null) Modifier.pressScaleClickable(scale = 0.99f, onClick = onClick) else Modifier,
            ),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (done) AppPrimaryPill else AppIconFrame),
                contentAlignment = Alignment.Center,
            ) {
                if (done) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = AppPrimaryInk, modifier = Modifier.size(15.dp))
                } else {
                    Text(toFa(step), color = AppMuted, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                Text(
                    title,
                    color = if (done) AppPrimaryInk else AppText,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    body,
                    color = AppMuted,
                    fontSize = 11.sp,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (onClick != null) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = AppMuted,
                    modifier = Modifier.padding(start = 8.dp).size(13.dp),
                )
            } else if (manualBadge) {
                Text(
                    "دستی",
                    color = AppMuted,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(AppIconFrame)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun ParseResultRow(label: String, value: String?, valueColor: Color = AppText) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = AppMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(
            value ?: "—",
            color = if (value == null) AppDisabledText else valueColor,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

/**
 * سوییچِ دومِ همین بخش: خوندنِ خودکارِ **اعلانِ** بانکی - برای بانک‌های دیجیتال (بلوبانک و…) که
 * اصلاً پیامک نمی‌فرستن. خواسته‌ی صریحِ کاربر، با این شرط که «اجباری نباشه و توضیح بدی کجا بره».
 *
 * مجوزِ خواندنِ اعلان‌ها دیالوگِ Runtime نداره؛ تنها راهش بازکردنِ صفحه‌ی مخصوصِ خودِ اندروید با
 * [Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS]ه - دکمه‌ی زیرِ سوییچ همون رو باز می‌کنه.
 */
@Composable
private fun NotificationImportSettings(viewModel: SmsAutoImportViewModel) {
    val context = LocalContext.current
    val notifEnabled by viewModel.notifEnabled.collectAsState()

    // ⚠️ **قاعده‌ی صریحِ کارتِ `35f`: «سوئیچ دروغ نمی‌گوید».** وضعیتِ واقعیِ مجوز از خودِ اندروید
    // (`NotificationManagerCompat.getEnabledListenerPackages`) تو هر `onResume` دوباره خونده
    // می‌شه. قبلاً فقط پرچمِ خواستِ کاربر (DataStore) نشون داده می‌شد، پس اگه کاربر مجوز رو از
    // تنظیماتِ گوشی برمی‌داشت، سوییچ همچنان «روشن» می‌موند و قابلیت بی‌صدا مرده بود.
    var listenerGranted by remember { mutableStateOf(notificationListenerGranted(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                listenerGranted = notificationListenerGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun openListenerSettings() {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    // حالتِ سومِ کارتِ `35f`: کاربر روشنش کرده ولی اندروید مجوز رو نداره/پس گرفته.
    val revoked = notifEnabled && !listenerGranted

    SettingsSwitchRow(
        icon = Icons.Filled.NotificationsActive,
        title = "خوندنِ خودکارِ اعلانِ بانکی",
        subtitle = when {
            revoked -> "اجازه در تنظیماتِ گوشی برداشته شده"
            notifEnabled -> "برای بانک‌هایی که پیامک نمی‌دن و فقط اعلان می‌فرستن"
            else -> "خرج‌ها را دستی وارد می‌کنی"
        },
        checked = notifEnabled && listenerGranted,
        onCheckedChange = { checked ->
            viewModel.setNotifEnabled(checked)
            // روشن‌کردنِ سوییچ بدونِ مجوزِ اندروید بی‌فایده‌ست - همون لحظه می‌بریمش سرِ صفحه‌ی
            // درست (قاعده‌ی `35b`: «متنِ دکمه صریح می‌گوید کاربر از برنامه بیرون می‌رود»).
            if (checked && !listenerGranted) openListenerSettings()
        },
    )
    if (revoked) {
        AppCard(variant = AppCardVariant.URGENT, modifier = Modifier.padding(top = 8.dp)) {
            Text(
                "اجازه‌ی خواندنِ اعلان از تنظیماتِ گوشی برداشته شده، پس هیچ تراکنشی خودکار ثبت نمی‌شه.",
                color = AppText,
                fontSize = 12.sp,
                lineHeight = 20.sp,
            )
            GradientButton(
                onClick = { openListenerSettings() },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            ) { Text("درستش کن") }
        }
    }
    AppCard(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            "بعضی بانک‌ها (مثلِ بلوبانک) اصلاً پیامکِ برداشت/واریز نمی‌فرستن و فقط تو خودِ گوشی " +
                "اعلان می‌دن. اگه حسابی داری که این‌طوریه، این گزینه رو روشن کن تا اپ از رو همون " +
                "اعلان تراکنش رو ثبت کنه. اگه بانکت پیامک می‌فرسته، لازم نیست روشنش کنی.",
            color = AppMuted,
            fontSize = 12.sp,
            lineHeight = 20.sp,
        )
        // **قاعده‌ی صریحِ سندِ `35c`**: اگه مجوز از قبل روشنه، راهنما اصلاً نشون داده نشه و
        // جاش حالتِ «فعال شد» (`35d`) بیاد. سه قدمِ راهنما برای کسی که کارش تمومه فقط نویزه.
        if (listenerGranted) {
            Text(
                "اجازه‌ی خواندنِ اعلان از قبل داده شده - این بخش کارِ دیگه‌ای ازت نمی‌خواد.",
                color = AppPrimaryDim,
                fontSize = 12.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp),
            )
        } else {
            // **راهنمای سه‌قدمیِ کارتِ `35c`** - صفحه‌ای که باز می‌شه مالِ اندرویده نه جیبک، پس
            // کاربر باید از قبل بدونه اونجا دنبالِ چی بگرده.
            NotificationPermissionSteps(modifier = Modifier.padding(top = 12.dp))
            GradientButton(
                onClick = { openListenerSettings() },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) {
                Text("بازکردنِ تنظیمات")
            }
        }
        Text(
            "فیلتر روی خودِ گوشیه، نه سرور: فقط اعلانِ همون بانکی که خودت انتخاب کردی خونده " +
                "می‌شه و متنِ خامِ هیچ اعلانِ دیگه‌ای هیچ‌وقت از گوشی بیرون نمی‌ره.",
            color = AppMuted,
            fontSize = 11.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
    // انتخابِ خودِ اپ‌ها - **بدونِ این، کلِ قابلیت بی‌اثره** (رجوع کن به notifPackages).
    NotificationAppPicker(viewModel = viewModel, modifier = Modifier.padding(top = 8.dp))
}

/**
 * انتخابِ اپ‌هایی که اعلانشون خونده می‌شه.
 *
 * 🚨 **این بخش قبلاً وجود نداشت و همین باگ بود**: `BankNotificationListener` فقط اعلانِ
 * بسته‌نام‌های داخلِ [UiPrefs.notifAutoImportPackages] رو می‌خونه، ولی هیچ‌جای اپ اون لیست رو
 * **نمی‌نوشت**. پس لیست همیشه خالی بود و هر اعلانی - از جمله بلوبانک - بی‌صدا دور انداخته
 * می‌شد، حتی وقتی کاربر هم سوییچ رو روشن کرده بود هم مجوزِ اندروید رو داده بود.
 *
 * فهرست از خودِ گوشی خونده می‌شه (اپ‌های دارای آیکونِ لانچر) و **هیچ‌جا فرستاده نمی‌شه**.
 */
@Composable
private fun NotificationAppPicker(viewModel: SmsAutoImportViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val selected by viewModel.notifPackages.collectAsState()
    var query by remember { mutableStateOf("") }

    // خوندنِ لیستِ اپ‌ها یه‌بار انجام می‌شه (رو گوشیِ پرِ اپ چند صد میلی‌ثانیه طول می‌کشه، پس
    // نباید هر بار recomposition تکرار بشه).
    val apps by produceState(initialValue = emptyList<Pair<String, String>>(), context) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val pm = context.packageManager
                pm.getInstalledApplications(0)
                    .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
                    .filter { it.packageName != context.packageName }
                    .map { it.packageName to pm.getApplicationLabel(it).toString() }
                    .sortedBy { it.second.lowercase() }
            }.getOrDefault(emptyList())
        }
    }

    // 🚨 **گزارشِ صریحِ کاربر**: «اپ‌هایی که باید اعلانشون خونده بشه خیلی کم‌ان» - ولی این
    // لیست همه‌ی اپ‌های گوشی رو الفبایی می‌ریخت (دوربین، قطب‌نما، رادیو، سیم‌کارت…) و اپِ
    // بانکیِ واقعی لای ده‌ها اپِ بی‌ربط گم می‌شد. حالا اپ‌های بانکی/پرداختی جدا و **اول**
    // می‌آن؛ بقیه پشتِ یه دکمه‌ی «نمایشِ همه‌ی اپ‌ها» می‌مونن - حذف نمی‌شن، فقط جلوی چشم نیستن.
    var showAllApps by remember { mutableStateOf(false) }
    val (bankApps, otherApps) = remember(apps) { BankAppMatcher.split(apps) }

    val shown = remember(apps, bankApps, otherApps, query, selected, showAllApps) {
        val q = query.trim()
        // موقعِ جستجو کلِ اپ‌ها گشته می‌شن (کاربر داره دنبالِ یه اسمِ مشخص می‌گرده).
        val pool = when {
            q.isNotEmpty() -> apps
            showAllApps -> bankApps + otherApps
            // ⚠️ اپِ انتخاب‌شده همیشه دیده می‌شه، حتی اگه تشخیص داده نشده باشه - وگرنه
            // کاربر سوییچی رو که خودش روشن کرده گم می‌کنه.
            else -> bankApps + otherApps.filter { it.first in selected }
        }
        val filtered = if (q.isEmpty()) pool else pool.filter { it.second.contains(q, ignoreCase = true) }
        // انتخاب‌شده‌ها همیشه بالا می‌مونن تا کاربر ببینه چی روشنه، حتی وقتی داره جستجو می‌کنه.
        filtered.sortedByDescending { it.first in selected }
    }

    AppCard(modifier = modifier) {
        Text(
            "اعلانِ کدوم اپ‌ها خونده بشه؟",
            color = AppText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
        )
        Text(
            if (selected.isEmpty()) {
                "هیچ اپی انتخاب نشده - تا وقتی حداقل یکی رو انتخاب نکنی، هیچ تراکنشی خودکار ثبت نمی‌شه."
            } else {
                "${toFa(selected.size)} اپ انتخاب شده."
            },
            color = if (selected.isEmpty()) AppDanger else AppMuted,
            fontSize = 11.5.sp,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp),
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("جستجوی اسمِ اپ", color = AppMuted, fontSize = 12.sp) },
            singleLine = true,
            colors = appFieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )
        // ارتفاعِ کرانه‌دار: این کارت خودش داخلِ یه صفحه‌ی اسکرول‌شونده‌ست، پس لیست نباید
        // بی‌نهایت رشد کنه.
        Column(modifier = Modifier.padding(top = 8.dp)) {
            shown.take(40).forEach { (pkg, label) ->
                val isOn = pkg in selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScaleClickable { viewModel.setNotifPackageSelected(pkg, !isOn) }
                        .padding(vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        label,
                        color = AppText,
                        fontSize = 12.5.sp,
                        fontWeight = if (isOn) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = isOn,
                        onCheckedChange = { viewModel.setNotifPackageSelected(pkg, it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = AppPrimary),
                    )
                }
            }
            if (shown.isEmpty()) {
                Text(
                    when {
                        apps.isEmpty() -> "در حالِ خواندنِ فهرستِ اپ‌ها…"
                        query.isNotBlank() -> "اپی با این اسم پیدا نشد."
                        // هیچ اپِ بانکی‌ای شناخته نشد - نباید بن‌بست بشه.
                        else -> "اپِ بانکی‌ای شناخته نشد. «نمایشِ همه‌ی اپ‌ها» رو بزن یا اسمش رو جستجو کن."
                    },
                    color = AppMuted,
                    fontSize = 11.5.sp,
                    modifier = Modifier.padding(vertical = 10.dp),
                )
            } else if (shown.size > 40) {
                Text(
                    "فقط ۴۰ اپِ اول نشون داده شده - برای بقیه از جستجو استفاده کن.",
                    color = AppMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            // درِ خروجیِ لیستِ کوتاه: اگه بانکِ کاربر تو تشخیص نیومده، از اینجا پیداش می‌کنه.
            if (query.isBlank() && !showAllApps && otherApps.isNotEmpty()) {
                OutlinedButton(
                    onClick = { showAllApps = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Text("نمایشِ همه‌ی اپ‌ها (${toFa(otherApps.size)} تای دیگه)", fontSize = 12.sp)
                }
            }
        }
    }
}

/** آیا اندروید واقعاً مجوزِ خواندنِ اعلان‌ها رو به این اپ داده؟ (قاعده‌ی «سوئیچ دروغ نمی‌گوید».) */
private fun notificationListenerGranted(context: Context): Boolean =
    NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

/**
 * سه قدمِ کارتِ `35c` (نسخه‌ی نهایی، فریمِ `37e`).
 *
 * **دو چیز عمداً از طرح بیرون رفت** و برنگردونشون:
 * - **عکسِ صفحه‌ی اندروید**: عنوان و زبانِ اون صفحه بینِ سازنده‌ها فرق داره (رو شیائومیِ کاربر
 *   `Device & app notifications` بود و کلاً انگلیسی)، پس عکسِ یه گوشی برای بقیه گمراه‌کننده‌ست.
 * - **متنِ مسیر** («تنظیمات ← برنامه‌ها ← …»): `ACTION_NOTIFICATION_LISTENER_SETTINGS` کاربر رو
 *   **مستقیم رو صفحه‌ی مقصد** فرود میاره، پس اون مسیر چیزی رو توضیح می‌داد که کاربر هیچ‌وقت
 *   نمی‌بینه. (تاییدِ عملیِ کاربر رو گوشیِ واقعی.)
 *
 * ⚠️ **قدمِ ۱ و ۲ عمداً جدان.** نسخه‌ی قبلیِ همین تابع می‌گفت «کلیدِ کنارِ اسمش رو روشن کن» که
 * **غلط بود**: تو فهرست کلیدی نیست، باید رو اسم زد تا صفحه‌ی خودش باز بشه. طراح تاکید کرد
 * بیشترِ کاربرها دقیقاً همین‌جا گیر می‌کنن.
 *
 * رشته‌های انگلیسی (`NOT ALLOWED`, `ALLOWED`, `Allow notification access`, `Allow`) **ترجمه
 * نمی‌شن** و با [Ltr] می‌شینن - رابطِ تنظیمات حتی رو گوشیِ فارسی هم انگلیسیه.
 */
@Composable
private fun NotificationPermissionSteps(modifier: Modifier = Modifier) {
    // هر قدم: متنِ فارسی، و رشته‌ی انگلیسیِ لنگر (اگه داشته باشه) که عیناً دیده می‌شه.
    val steps: List<Triple<String, String?, String>> = listOf(
        Triple(
            "تو فهرست دنبالِ «جیبک» بگرد",
            "NOT ALLOWED",
            "ممکنه زیرِ این سرگروه باشه. اگه از قبل زیرِ ALLOWED بود، کار تمومه.",
        ),
        Triple(
            "روش بزن",
            null,
            "صفحه‌ی خودش باز می‌شه - کلید اونجاست، نه تو فهرست.",
        ),
        Triple(
            "این کلید رو روشن کن",
            "Allow notification access",
            "بعدش اندروید یه پنجره‌ی تایید میاره؛ Allow رو بزن.",
        ),
    )
    Column(modifier = modifier) {
        steps.forEachIndexed { index, (title, anchor, hint) ->
            Row(modifier = Modifier.padding(bottom = 10.dp)) {
                Text(
                    toFa(index + 1),
                    color = AppPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                )
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        title,
                        color = AppText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    if (anchor != null) {
                        // کارتِ سفیدِ لنگر - عیناً همون رشته‌ای که کاربر رو صفحه‌ی اندروید می‌بینه.
                        Ltr {
                            Text(
                                anchor,
                                color = AppText,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .padding(top = 5.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AppSurface)
                                    .border(1.5.dp, AppLine, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                            )
                        }
                    }
                    Text(
                        hint,
                        color = AppMuted,
                        fontSize = 11.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

/**
 * **«ابزارها» - دو گروهِ نام‌دار** (بندِ ۵ فریمِ `75c`، و خواسته‌ی کاربر: «خیلی ساده است»).
 *
 * قبلاً چهار ردیفِ بی‌عنوان در دو کارتِ بی‌اسم بود، یعنی گروه‌بندی‌اش هیچ حرفی نمی‌زد.
 * حالا هر گروه **کاری** را نام می‌برد که کاربر آمده انجام دهد، نه جنسِ فنیِ ابزار را:
 * کسی که دنبالِ تقویم است «پولم را کِی باید بدهم» در ذهنش است، نه «ابزارِ تقویمی».
 */
@Composable
private fun ToolsSettings(onOpenTool: (String) -> Unit) {
    SettingsHero(Icons.Filled.Build, "ابزارهای مالی", "برنامه‌ریزی، پس‌انداز و مرورِ محاسبه‌ها")
    SettingsGroupLabel("برنامه‌ریزی", accent = AppInfo)
    SettingsGroup {
        SettingsRowItem("تقویمِ مالی", Icons.Filled.DateRange, SettingsTone.BLUE, status = "سررسیدِ اقساط و چک‌ها روی تقویم") { onOpenTool("calendar") }
        SettingsDivider()
        SettingsRowItem("هدف‌های پس‌انداز", Icons.Filled.Savings, SettingsTone.GREEN, status = "پول کنار بذار و پیشرفتش رو ببین") { onOpenTool("goals") }
    }
    SettingsGroupLabel("بررسی و محاسبه", accent = AppPurple)
    SettingsGroup {
        SettingsRowItem("تاریخچه‌ی محاسبات", Icons.Filled.History, SettingsTone.PURPLE, status = "محاسبه‌های قبلیِ وام، سقفِ وام و سودِ سپرده") { onOpenTool("history") }
    }
    // ⚠️ ردیفِ «آمار و گزارشات» از این‌جا **حذف** شد (گزارشِ ۶.۵ی کاربر: «پرتی هست»).
    // محتوایش دربارهٔ وام است، پس به تبِ گزارش رفت (دورِ ۱۲). عمداً این‌جا یک ردیفِ
    // لینک‌دهنده نماند: ردیفی که فقط کاربر را جای دیگری می‌فرستد یک پرش است.
    //
    // ⚠️ **ردیفِ «شارژِ آزمایشیِ سکه» حذف شد** (خواسته‌ی صریحِ کاربر، دورِ ۱۳). ابزارِ
    // تستِ فروشگاه بود و کارش تمام شد؛ ماندنش در نسخه‌ی عمومی یعنی هر کاربری می‌توانست
    // موجودی‌اش را یک‌میلیون کند و کلِ اقتصادِ سکه بی‌معنی می‌شد.
    //
    // ⏳ سه ابزارِ دیگرِ فریمِ `75c` (اشتراک‌یاب · دنگ · استعلامِ صیادی) هنوز این‌جا
    // نیامده‌اند: هر سه از جای دیگری پارامتر می‌گیرند و بردنشان به این‌جا یک لایه‌ی
    // داده‌ی تازه می‌خواهد، نه یک ردیف.
}

/** سرگروهِ «ابزارها» - متنِ ریزِ خاکستری بالای هر کارت، نه عنوانِ داخلِ کارت. */
@Composable
private fun ToolGroupTitle(text: String) {
    Text(
        text,
        color = AppMuted,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(top = 14.dp, start = 4.dp),
    )
}

@Composable
private fun ToolRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().pressScaleClickable(scale = 0.99f, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
            Text(title, color = AppText, fontSize = 14.sp)
            Text(subtitle, color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = null, tint = AppMuted, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun AboutSettings(banner: InAppBannerState, onOpenBugReport: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var showContact by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }

    // ── بلوکِ نشان - **تنها جای تنظیمات که چیزی بی‌کارت رو بستر می‌شینه** ───────
    // همین تفاوته که این صفحه رو «صفحه‌ی هویت» می‌کنه نه یه فهرستِ دیگه.
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        JibakLogo(width = 80.dp)
        Text(
            "جیبک",
            color = AppText,
            fontSize = 19.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 14.dp),
        )
        // ⚠️ **ضربه‌ی طولانی رو نسخه، اطلاعاتِ فنی رو کپی می‌کنه.** ارزون‌ترین کاری که
        // می‌شه برای پشتیبانی کرد - بی این، هر گفت‌وگو با سه پرسشِ اضافه شروع می‌شه.
        // ضربه‌ی معمولی عمداً هیچ کاری نمی‌کنه.
        Ltr {
            Text(
                "${toFa(BuildConfig.VERSION_NAME)} (${toFa(BuildConfig.VERSION_CODE)})",
                color = AppMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 3.dp)
                    .pressScaleClickable(scale = 0.98f) {
                        clipboard.setText(AnnotatedString(diagnosticsText()))
                        banner.show("اطلاعاتِ فنی کپی شد", isSuccess = true)
                    },
            )
        }
    }

    // ── گروهِ کمک ─────────────────────────────────────────────────────────────
    // ⚠️ ردیفِ «راهنما» عمداً نیست: محتوای پرسش‌های پرتکرار هنوز نوشته نشده، و ردیفی
    // که به صفحه‌ی خالی می‌ره بدتر از نبودنشه (همون قاعده‌ی ردیفِ ایمیل).
    SettingsGroupLabel("کمک")
    SettingsGroup(modifier = Modifier.padding(top = 28.dp)) {
        // 🐞 **بالای «تماس با ما»** و نه زیرش: کسی که مشکل دارد اول این را می‌خواهد،
        // و این مسیر گزارش را روی سرور هم ثبت می‌کند (شرطِ هدیه‌ی اشتراک).
        SettingsRowItem(
            title = "گزارشِ مشکل",
            icon = Icons.Filled.BugReport,
            tone = SettingsTone.ORANGE,
            status = "ثبت می‌شود و کدِ پیگیری می‌گیری",
            onClick = { onOpenBugReport() },
        )
        SettingsDivider()
        SettingsRowItem(
            title = "تماس با ما",
            icon = Icons.Filled.SupportAgent,
            tone = SettingsTone.GREEN,
            onClick = { showContact = true },
        )
        SettingsDivider()
        SettingsRowItem(
            // مقصد از **فلیورِ نصب‌شده** میاد، نه یه لینکِ ثابت - ردیفی که به فروشگاهِ
            // اشتباه بره بدتر از نبودنشه.
            title = if (BuildConfig.FLAVOR == "myket") "امتیاز در مایکت" else "امتیاز در کافه‌بازار",
            icon = Icons.Filled.Star,
            tone = SettingsTone.ORANGE,
            onClick = {
                val storeUrl = if (BuildConfig.FLAVOR == "myket") {
                    "https://myket.ir/app/ir.sadteam.loancalc"
                } else {
                    "https://cafebazaar.ir/app/ir.sadteam.loancalc"
                }
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(storeUrl)))
                } catch (e: ActivityNotFoundException) {
                    banner.show("اپِ فروشگاه رو گوشیت پیدا نشد")
                }
            },
        )
    }

    // ── گروهِ متن‌ها ──────────────────────────────────────────────────────────
    SettingsGroupLabel("متن‌ها")
    SettingsGroup {
        SettingsRowItem(
            title = "حریمِ خصوصی",
            icon = Icons.Filled.Shield,
            tone = SettingsTone.NEUTRAL,
            onClick = { showPrivacy = true },
        )
        // ذکرِ منبعِ قیمت (Servix) به تصمیمِ صریحِ کاربر (۳ مهر) از کلِ برنامه حذف شد.
    }

    Text(
        "ساخته‌شده در ایران · ۱۴۰۵",
        color = AppLabel,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp),
    )

    if (showContact) {
        FullScreenDialog(onDismissRequest = { showContact = false }) {
            ContactSupportContent(onClose = { showContact = false })
        }
    }
    if (showPrivacy) {
        FullScreenDialog(onDismissRequest = { showPrivacy = false }) {
            PrivacyPolicyScreen(onBack = { showPrivacy = false })
        }
    }
}

/**
 * اطلاعاتِ فنی برای پشتیبانی - نسخه، بیلد، مدلِ گوشی، اندروید، و فلیور.
 * عمداً **شناسه‌ی کاربر توش نیست**: شماره‌ی موبایل داده‌ی شخصیه و کپیِ ناخواسته‌ش
 * می‌تونه جایی که نباید پیست بشه.
 */
private fun diagnosticsText(): String = buildString {
    append("نسخه: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n")
    append("فروشگاه: ${BuildConfig.FLAVOR}\n")
    append("گوشی: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}\n")
    append("اندروید: ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})")
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
internal fun FullScreenDialog(onDismissRequest: () -> Unit, content: @Composable () -> Unit) {
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


// ⚠️ نشانی **یک‌جا** تعریف شده (`ui/support`)؛ دو کپی یعنی روزی که یکی عوض می‌شود و
// نیمی از پیام‌ها به صندوقِ قدیمی می‌روند.

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
private fun SecuritySettings(
    appLockViewModel: AppLockViewModel,
    privacyViewModel: PrivacyModeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val pinHash by appLockViewModel.pinHash.collectAsState()
    val biometricEnabled by appLockViewModel.biometricEnabled.collectAsState()
    val autoLockTimeoutMinutes by appLockViewModel.autoLockTimeoutMinutes.collectAsState()
    val privacyMode = LocalPrivacyMode.current
    var showPinDialog by remember { mutableStateOf(false) }
    val hasLock = pinHash != null || biometricEnabled

    if (showPinDialog) {
        PinSetupDialog(
            onDismiss = { showPinDialog = false },
            onConfirm = { pin ->
                appLockViewModel.setPin(pin)
                showPinDialog = false
            },
        )
    }

    // ── گروهِ قفل ─────────────────────────────────────────────────────────────
    // قاعده‌ی صریحِ طراح: **قفل که خاموشه، ردیف‌های زیرش پنهان می‌شن، نه خاکستری.**
    // «سه ردیفِ خاکستریِ بی‌کار بدتر از یه ردیفِ تنهاست.»
    SettingsHero(
        Icons.Filled.Shield,
        "امنیت و حریمِ خصوصی",
        "قفلِ برنامه روی همین گوشی و پنهان‌کردنِ مبلغ‌ها",
        badge = if (hasLock) "قفل فعال" else "بی‌قفل",
    )
    SettingsGroupLabel("قفلِ برنامه")
    SettingsGroup {
        SettingsRowItem(
            title = "قفل با رمزِ عددی",
            icon = Icons.Filled.Lock,
            tone = SettingsTone.BLUE,
            status = if (pinHash != null) "فعال است" else "خاموش",
            statusTone = if (pinHash != null) StatusTone.HEALTHY else StatusTone.NEUTRAL,
            checked = pinHash != null,
            onCheckedChange = { checked ->
                if (checked) showPinDialog = true else appLockViewModel.clearPin()
            },
        )
        if (pinHash != null) {
            SettingsDivider()
            SettingsRowItem(
                title = "تغییرِ رمزِ عددی",
                icon = Icons.Filled.Password,
                tone = SettingsTone.NEUTRAL,
                onClick = { showPinDialog = true },
            )
        }
        if (pinHash != null) SettingsDivider()
        if (pinHash != null) SettingsRowItem(
            title = "قفل با اثرِ انگشت",
            icon = Icons.Filled.Fingerprint,
            tone = SettingsTone.BLUE,
            status = if (biometricAvailable(context)) null else "این گوشی اثرِ انگشتِ ثبت‌شده ندارد",
            statusTone = StatusTone.BROKEN,
            checked = if (biometricAvailable(context)) biometricEnabled else null,
            onCheckedChange = if (biometricAvailable(context)) {
                { appLockViewModel.setBiometricEnabled(it) }
            } else {
                null
            },
            onClick = if (biometricAvailable(context)) null else ({}),
        )
    }

    // ردیفِ «قفل بعد از» فقط وقتی معنی داره که اصلاً قفلی هست.
    if (hasLock) {
        SettingsGroup(modifier = Modifier.padding(top = 8.dp)) {
            SettingsRowItem(
                title = "قفل بعد از",
                icon = Icons.Filled.Timer,
                tone = SettingsTone.NEUTRAL,
                status = autoLockTimeoutOptions.firstOrNull { it.first == autoLockTimeoutMinutes }?.second,
                onClick = null,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 13.dp),
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

    // ── گروهِ حریمِ خصوصی ─────────────────────────────────────────────────────
    // ⚠️ «پنهان در فهرستِ برنامه‌های اخیر» (`FLAG_SECURE`) عمداً پیاده **نشد** - قبلاً بود و
    // به تصمیمِ صریحِ کاربر حذف شد (رجوع کن به CLAUDE.md). این ردیفِ طرح رو نادیده می‌گیریم.
    SettingsGroupLabel("حریمِ خصوصی")
    SettingsGroup {
        SettingsRowItem(
            title = "پنهان‌کردنِ مبلغ‌ها",
            icon = Icons.Filled.VisibilityOff,
            tone = SettingsTone.NEUTRAL,
            status = if (privacyMode) {
                "مبلغ‌ها پشتِ ••••• پنهان‌اند - متنِ اعلان‌ها هم بی‌مبلغ می‌شود"
            } else {
                "مبلغ‌ها دیده می‌شوند"
            },
            checked = privacyMode,
            onCheckedChange = { privacyViewModel.toggle() },
        )
        // پیش‌نمایشِ دو حالت با عددِ نمونه - مبلغِ واقعیِ کاربر این‌جا نشان داده نمی‌شود.
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(false to "۱۲٬۳۴۵٬۶۷۸", true to "••••••").forEach { (hidden, sample) ->
                val active = hidden == privacyMode
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (active) AppPrimaryPill else AppSurface2)
                        .border(if (active) 1.5.dp else 0.dp, if (active) AppPrimary else Color.Transparent, RoundedCornerShape(14.dp))
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(sample, color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    Text(if (hidden) "پنهان" else "نمایان", color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // ── کارتِ توضیحِ ته صفحه ──────────────────────────────────────────────────
    // جمله‌ی سوم مهم‌ترینه: بی اون، کاربرِ فراموش‌کار فکر می‌کنه داده‌ش رفته و اپ رو پاک می‌کنه.
    SettingsDisclosure(title = "قفلِ برنامه چه کاری می‌کند؟") {
        SettingsParagraph(
            "قفلِ برنامه فقط جلوی بازشدنِ برنامه رو همین گوشی رو می‌گیره. داده‌هات رو سرور با " +
                "حسابِ کاربریت محافظت می‌شه، نه با این رمز. اگه رمز رو فراموش کنی، با ورودِ " +
                "دوباره به حساب بازش می‌کنی.",
        )
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


// نسخه‌ی «۱» قبلاً هاردکد بود (همیشه ثابت، هیچ‌وقت آپدیت نمی‌شد) - خواسته‌ی کاربر: نسخه‌ی واقعیِ
// نصب‌شده رو نشون بده. BuildConfig.VERSION_NAME همون versionNameِ CI (مثلاً "1.0.332") ئه.
private const val privacyText = "چه اطلاعاتی ذخیره می‌شه؟\n" +
    "وام‌ها، تنظیمات و یادآوری‌هایی که تو اپ می‌سازی، فقط روی گوشی خودت ذخیره می‌شن. این اپ هیچ " +
    "تبلیغ، ابزار ردیابی (analytics) یا کد شخص ثالثی نداره و اطلاعاتت رو به‌جایی نمی‌فروشه.\n\n" +
    "ورود با شماره تلفن\n" +
    "بدون ورود هم می‌تونی از اپ به‌عنوان مهمان استفاده کنی. اگه با شماره موبایل وارد بشی، فقط " +
    "شماره‌ت و لیست وام‌هات (برای همگام‌سازی بین گوشی‌هات) روی سرور اختصاصی همین اپ ذخیره می‌شه؛ " +
    "این اطلاعات جای دیگه‌ای فرستاده نمی‌شه و در اختیار شرکت یا سرویس ثالثی قرار نمی‌گیره.\n\n" +
    "اشتراک\n" +
    "بدون اشتراک فقط یک وام قابل ذخیره‌ست؛ برای ذخیره‌ی وام بیشتر اول باید وارد بشی و بعد اشتراک " +
    "تهیه کنی.\n\n" +
    // ⚠️ این بند **اجباریه**: اپ هم پیامک می‌خونه هم اعلان، و کاربری که مجوزِ عجیبی داده و
    // توضیحش رو تو متن پیدا نمی‌کنه، اپ رو پاک می‌کنه. مسئله‌ی اعتماده، نه حقوقی.
    "خواندنِ پیامک و اعلانِ بانکی\n" +
    "اگه خودت این قابلیت رو روشن کنی (پیش‌فرض خاموشه)، اپ متنِ پیامک‌های بانکی و اعلان‌های " +
    "بانک‌ها رو می‌خونه تا مبلغ و نوعِ تراکنش رو دربیاره و خودکار ثبتش کنه. این خوندن " +
    "**کاملاً روی خودِ گوشیه**: هیچ پیامکی، هیچ اعلانی و هیچ تکه‌ای از متنشون به هیچ سروری " +
    "— نه سرورِ ما، نه جای دیگه — فرستاده نمی‌شه. هر لحظه می‌تونی از تنظیمات خاموشش کنی."
