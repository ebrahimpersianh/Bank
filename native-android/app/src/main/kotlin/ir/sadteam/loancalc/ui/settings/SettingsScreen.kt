package ir.sadteam.loancalc.ui.settings

import ir.sadteam.loancalc.BuildConfig
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Vibration
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
import androidx.compose.ui.text.style.TextAlign
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
import ir.sadteam.loancalc.ui.calendar.FinancialCalendarScreen
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.subscription.parseSubscribedUntil
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
) {
    var route by remember { mutableStateOf(SettingsRoute.MAIN) }
    // زیرصفحه‌های «فیچری» (تقویم/آمار/تاریخچه) از رو خودِ صفحه‌ی «ابزارها» باز می‌شن، پس یه استیتِ
    // جدا لازم دارن تا با برگشت، به «ابزارها» برگردن نه به ریشه‌ی تنظیمات.
    var tool by remember { mutableStateOf<String?>(null) }
    var showLoginPrompt by remember { mutableStateOf(false) }
    var showSubscription by remember { mutableStateOf(false) }
    var showReminderSettings by remember { mutableStateOf(false) }

    val screenKey = when {
        showLoginPrompt -> "login"
        showSubscription -> "subscription"
        showReminderSettings -> "reminderSettings"
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
            "tool" -> FullScreenDialog(onDismissRequest = { tool = null }) {
                when (tool) {
                    "calendar" -> FinancialCalendarScreen(onBack = { tool = null })
                    "stats" -> StatsScreen(onBack = { tool = null })
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
    TOOLS("ابزارها", listOf("تقویم مالی", "آمار", "گزارش", "تاریخچه محاسبات")),
    SECURITY("امنیت", listOf("قفل", "PIN", "اثر انگشت")),
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
            // برچسبِ «نسخه اشتراکی/عادی» زیرِ عنوان به‌خواستِ صریحِ کاربر حذف شد - اضافه بود، چون
            // همون اطلاعات (با جزئیاتِ بیشتر) تو ردیفِ «حساب کاربری» هست. حلقه‌ی طلاییِ دورِ آیکونِ
            // بالا عمداً موند: تنها نشانه‌ی بصریِ باقی‌مونده‌ی وضعیتِ اشتراک تو خودِ هدره.
            Text("تنظیمات", color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp))
        }

        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("جستجو تو تنظیمات") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (matches(SettingsRoute.ACCOUNT)) {
                SettingsRow(
                    icon = Icons.Filled.Person,
                    route = SettingsRoute.ACCOUNT,
                    value = if (gateState == GateState.LOGGED_IN) toFa(phone ?: "") else "وارد نشدی",
                    onClick = { onOpen(SettingsRoute.ACCOUNT) },
                )
            }

            // همون کارتِ خریدِ اشتراک با همون منطقِ قبلی (رجوع کن به CLAUDE.md: عمداً به کاربرِ
            // ازقبل‌مشترک نشون داده نمی‌شه، این باگ نیست) - فقط جاش زیرِ ردیفِ حسابه.
            val onlyTrialSubscribed = subscribed && trialDaysLeft != null && trialDaysLeft in 1..7
            if ((!subscribed || onlyTrialSubscribed) && searchQuery.isBlank()) {
                PulseGlowBox(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
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

            SettingsSectionLabel("شخصی‌سازی")
            if (matches(SettingsRoute.APPEARANCE)) {
                SettingsRow(Icons.Filled.Palette, SettingsRoute.APPEARANCE) { onOpen(SettingsRoute.APPEARANCE) }
            }
            // تنها سوییچی که عمداً تو ریشه موند: یه گزینه‌ی تک‌حالته‌ست، زیرصفحه‌ی جدا براش
            // بی‌خودی یه تپِ اضافه می‌شد.
            if (searchQuery.isBlank() || "ویبره".contains(searchQuery.trim()) || "هپتیک".contains(searchQuery.trim())) {
                SettingsSwitchRow(
                    icon = Icons.Filled.Vibration,
                    title = "ویبره‌ی لمسی",
                    subtitle = "موقع لمس دکمه‌ها یه لرزش کوتاه",
                    checked = vibrationEnabled,
                    onCheckedChange = { hapticsViewModel.setEnabled(it) },
                )
            }

            SettingsSectionLabel("یادآوری و داده‌ها")
            if (matches(SettingsRoute.REMINDERS)) {
                SettingsRow(Icons.Filled.Notifications, SettingsRoute.REMINDERS) { onOpen(SettingsRoute.REMINDERS) }
            }
            if (matches(SettingsRoute.DATA)) {
                SettingsRow(Icons.Filled.CloudUpload, SettingsRoute.DATA) { onOpen(SettingsRoute.DATA) }
            }
            if (matches(SettingsRoute.SMS)) {
                SettingsRow(Icons.Filled.Sms, SettingsRoute.SMS) { onOpen(SettingsRoute.SMS) }
            }

            SettingsSectionLabel("بیشتر")
            if (matches(SettingsRoute.TOOLS)) {
                SettingsRow(Icons.Filled.Assessment, SettingsRoute.TOOLS) { onOpen(SettingsRoute.TOOLS) }
            }
            if (matches(SettingsRoute.SECURITY)) {
                SettingsRow(Icons.Filled.Lock, SettingsRoute.SECURITY) { onOpen(SettingsRoute.SECURITY) }
            }
            if (matches(SettingsRoute.ABOUT)) {
                SettingsRow(
                    icon = Icons.Filled.Info,
                    route = SettingsRoute.ABOUT,
                    value = "نسخه ${toFa(BuildConfig.VERSION_NAME)}",
                    onClick = { onOpen(SettingsRoute.ABOUT) },
                )
            }
            Box(modifier = Modifier.padding(bottom = 16.dp))
        }
    }
}

/** ردیفِ استانداردِ تنظیمات: آیکون + عنوان (+ مقدارِ فعلی) + فلشِ ورود به زیرصفحه. طبقِ قاعده‌ی
 * پروژه هیچ کارتی دستی ساخته نمی‌شه - همیشه [AppCard]. */
@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    route: SettingsRoute,
    value: String? = null,
    onClick: () -> Unit,
) {
    AppCard(modifier = Modifier.padding(top = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().pressScaleClickable(scale = 0.99f, onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(22.dp))
            Text(
                route.title,
                color = AppText,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f).padding(start = 10.dp),
            )
            if (value != null) {
                Text(value, color = AppMuted, fontSize = 12.sp, modifier = Modifier.padding(end = 6.dp))
            }
            // تو RTL فلشِ «برو تو» سمتِ چپِ ردیفه و رو به چپ - KeyboardArrowLeft خودش آینه نمی‌شه.
            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = null, tint = AppMuted, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    AppCard(modifier = Modifier.padding(top = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                Text(title, color = AppText, fontSize = 14.sp)
                if (subtitle != null) {
                    Text(subtitle, color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = AppPrimary, checkedTrackColor = AppPrimary.copy(alpha = 0.5f)),
            )
        }
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text,
        color = AppMuted,
        fontSize = 12.sp,
        modifier = Modifier.padding(top = 16.dp, start = 4.dp),
    )
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
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.Close, contentDescription = "بستن")
            }
            Text(
                title,
                color = AppText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            // هم‌عرضِ دکمه‌ی بستن، تا عنوان دقیقاً وسط بمونه.
            Box(modifier = Modifier.size(48.dp))
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
) {
    val banner = rememberInAppBanner()
    Box(modifier = Modifier.fillMaxSize()) {
        SettingsSubPageScaffold(title = route.title, onBack = onBack) {
            when (route) {
                SettingsRoute.ACCOUNT -> AccountSettings(authViewModel, banner, onShowLoginPrompt, onShowSubscription)
                SettingsRoute.APPEARANCE -> AppearanceSettings(themeViewModel)
                SettingsRoute.REMINDERS -> ReminderToggles(notificationsViewModel, onShowReminderSettings)
                SettingsRoute.DATA -> DataSettings(authViewModel, autoBackupViewModel, banner)
                SettingsRoute.SMS -> SmsSettings(smsAutoImportViewModel)
                SettingsRoute.TOOLS -> ToolsSettings(onOpenTool)
                SettingsRoute.SECURITY -> AppCard { SecuritySettings(appLockViewModel) }
                SettingsRoute.ABOUT -> AboutSettings(banner)
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

    if (gateState == GateState.LOGGED_IN) {
        // نامِ اختیاریِ کاربر - خواسته‌ی صریحِ کاربر: «اجباری نباشه و بشه اسکیپ کرد، ولی اولِ
        // برنامه نه، بعد از ورود و تو بخشِ حساب کاربری». خالی‌گذاشتنش کاملاً عادیه؛ با پاک‌کردنِ
        // فیلد هم اسم حذف می‌شه. جای مصرفش سربرگِ خروجیِ PDF/اکسله - عمداً برای پیامِ خوش‌آمد
        // استفاده نمی‌شه (قبلاً ساخته و به‌خواستِ صریحِ کاربر حذف شد).
        var nameDraft by remember(savedName) { mutableStateOf(savedName ?: "") }
        AppCard(modifier = Modifier.padding(top = 8.dp)) {
            Text("نام (اختیاری)", color = AppMuted, fontSize = 11.sp)
            OutlinedTextField(
                value = nameDraft,
                onValueChange = { nameDraft = it },
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                singleLine = true,
                placeholder = { Text("مثلاً ابراهیم", color = AppMuted, fontSize = 13.sp) },
            )
            if (nameDraft.trim() != (savedName ?: "")) {
                GradientButton(
                    onClick = { authViewModel.updateName(nameDraft) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) { Text(if (nameDraft.isBlank()) "حذفِ نام" else "ذخیره‌ی نام") }
            }
            Text(
                "روی سربرگِ خروجیِ PDF و اکسل نوشته می‌شه. خالی گذاشتنش هیچ مشکلی نداره.",
                color = AppMuted,
                fontSize = 10.5.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        AppCard(modifier = Modifier.padding(top = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(22.dp))
                Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                    Text("شماره موبایل", color = AppMuted, fontSize = 11.sp)
                    Ltr { Text(toFa(phone ?: ""), color = AppText, fontSize = 15.sp) }
                }
            }
        }
        AppCard(modifier = Modifier.padding(top = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = if (subscribed) AppAccent else AppMuted, modifier = Modifier.size(22.dp))
                Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
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
                        fontSize = 14.sp,
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
            OutlinedButton(onClick = onShowSubscription, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                Text(if (subscribed) "مدیریت اشتراک" else "مشاهده پلن‌ها")
            }
        }
        AppCard(modifier = Modifier.padding(top = 8.dp)) {
            Button(
                onClick = { authViewModel.logout() },
                colors = ButtonDefaults.buttonColors(containerColor = AppDanger),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("خروج از حساب")
            }
            // الزامِ استانداردِ فروشگاه‌ها: راهِ داخل‌برنامه‌ای برای حذفِ کاملِ حساب. عمداً
            // OutlinedButton (نه پرشده مثلِ خروج) - شدتِ بصریِ کمتر برای یه عملِ جدی‌تر.
            OutlinedButton(
                onClick = { showDeleteAccountConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppDanger),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Text("حذف حساب کاربری")
            }
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
    // همون افکتِ دایره‌ایِ تعویضِ تم که از مرکزِ خودِ چیپِ زده‌شده باز می‌شه - رجوع کن به ThemeReveal.kt.
    val themeReveal = LocalThemeReveal.current
    val chipCenters = remember { mutableStateMapOf<ThemeMode, Offset>() }
    val themeToggleScope = rememberCoroutineScope()

    AppCard(label = "تم", modifier = Modifier.padding(top = 8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            themeModeOptions.forEach { (mode, label) ->
                AppChip(
                    label = label,
                    selected = themeMode == mode,
                    onClick = {
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
                    modifier = Modifier.onGloballyPositioned { chipCenters[mode] = it.boundsInRoot().center },
                )
            }
        }
    }
    AppCard(label = "اندازه فونت", modifier = Modifier.padding(top = 8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            fontSizeOptions.forEach { (scale, label) ->
                AppChip(label = label, selected = fontScale == scale, onClick = { themeViewModel.setFontScale(scale) })
            }
        }
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

    SettingsSwitchRow(
        icon = Icons.Filled.Notifications,
        title = "یادآوری سررسید",
        subtitle = "برای اقساط و چک‌های نزدیک به سررسید نوتیف بده",
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
    AppCard(modifier = Modifier.padding(top = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().pressScaleClickable(scale = 0.99f, onClick = onShowReminderSettings),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Schedule, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(22.dp))
            Text(
                "زمان‌بندی، صدا و ویبره",
                color = AppText,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f).padding(start = 10.dp),
            )
            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = null, tint = AppMuted, modifier = Modifier.size(18.dp))
        }
    }
    SettingsSwitchRow(
        icon = Icons.Filled.Notifications,
        title = "یادآوری روزانه‌ی دخل‌وخرج",
        subtitle = "اگه یه روز چیزی ثبت نکردی، یادت بندازه",
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

    SettingsSwitchRow(
        icon = Icons.Filled.CloudUpload,
        title = "پشتیبان‌گیری خودکار روزانه",
        subtitle = "هر روز یه اسنپ‌شات از وام/چک/حساب ذخیره کن",
        checked = autoBackupEnabled,
        onCheckedChange = { checked -> if (checked) autoBackupViewModel.enable() else autoBackupViewModel.disable() },
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
    if (lastAutoBackupAt != null) {
        AppCard(modifier = Modifier.padding(top = 8.dp)) {
            if (lastBackupLabel != null) {
                Text("آخرین پشتیبان: $lastBackupLabel", color = AppMuted, fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = {
                    autoBackupViewModel.restoreFromAutoBackup { ok ->
                        banner.show(
                            if (ok) "بازیابی از پشتیبان خودکار انجام شد" else "پشتیبانی برای بازیابی پیدا نشد",
                            isSuccess = ok,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Text("بازیابی از پشتیبان خودکار")
            }
            // برخلافِ دکمه‌ی بالا (فقط همین گوشی)، این از سرور می‌گیره - برای وقتی گوشی عوض شده یا
            // اپ پاک/نصب شده. فقط برای کاربرِ لاگین‌شده‌ی مشترک، چون پوش به سرور هم فقط برای همینه.
            if (gateState == GateState.LOGGED_IN && subscribed) {
                OutlinedButton(
                    onClick = {
                        autoBackupViewModel.restoreFromCloud { ok ->
                            banner.show(
                                if (ok) "بازیابی از سرور ابری انجام شد" else "پشتیبانی رو سرور ابری پیدا نشد",
                                isSuccess = ok,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Text("بازیابی از سرور ابری")
                }
            }
        }
    }
}

@Composable
private fun SmsSettings(smsAutoImportViewModel: SmsAutoImportViewModel) {
    val context = LocalContext.current
    val enabled by smsAutoImportViewModel.enabled.collectAsState()
    val lastImportAt by smsAutoImportViewModel.lastImportAt.collectAsState()
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) smsAutoImportViewModel.enable() }

    SettingsSwitchRow(
        icon = Icons.Filled.Sms,
        title = "خوندنِ خودکارِ پیامکِ بانکی",
        subtitle = "با رسیدنِ پیامکِ برداشت/واریز، خودکار یه تراکنش ثبت کن",
        checked = enabled,
        onCheckedChange = { checked ->
            if (!checked) {
                smsAutoImportViewModel.disable()
            } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                smsAutoImportViewModel.enable()
            } else {
                smsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
            }
        },
    )
    AppCard(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            "برای اینکه هر پیامک رو حسابِ درستش بشینه، تو صفحه‌ی «دارایی» شماره‌ی پیامکِ هر بانک رو " +
                "تو خودِ همون حساب وارد کن. هیچ پیامکی هیچ‌جا فرستاده نمی‌شه - همه‌چی رو خودِ گوشیه.",
            color = AppMuted,
            fontSize = 12.sp,
            lineHeight = 20.sp,
        )
        if (lastImportAt != null) {
            Text("آخرین ثبتِ خودکار: $lastImportAt", color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
        }
    }

    NotificationImportSettings(smsAutoImportViewModel)
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

    SettingsSwitchRow(
        icon = Icons.Filled.NotificationsActive,
        title = "خوندنِ خودکارِ اعلانِ بانکی",
        subtitle = "برای بانک‌هایی که پیامک نمی‌دن و فقط اعلان می‌فرستن",
        checked = notifEnabled,
        onCheckedChange = { checked -> viewModel.setNotifEnabled(checked) },
    )
    AppCard(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            "بعضی بانک‌ها (مثلِ بلوبانک) اصلاً پیامکِ برداشت/واریز نمی‌فرستن و فقط تو خودِ گوشی " +
                "اعلان می‌دن. اگه حسابی داری که این‌طوریه، این گزینه رو روشن کن تا اپ از رو همون " +
                "اعلان تراکنش رو ثبت کنه. اگه بانکت پیامک می‌فرسته، لازم نیست روشنش کنی.",
            color = AppMuted,
            fontSize = 12.sp,
            lineHeight = 20.sp,
        )
        Text(
            "این قابلیت یه اجازه‌ی جداگانه لازم داره که اندروید فقط از تنظیماتِ خودش می‌ده. " +
                "دکمه‌ی زیر رو بزن، تو لیستی که باز می‌شه «جیبک» رو پیدا کن و روشنش کن.",
            color = AppMuted,
            fontSize = 12.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(top = 8.dp),
        )
        OutlinedButton(
            onClick = {
                runCatching {
                    context.startActivity(
                        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        ) {
            Text("بازکردنِ تنظیماتِ دسترسی به اعلان‌ها")
        }
        Text(
            "هیچ اعلانی هیچ‌جا فرستاده نمی‌شه - همه‌چی رو خودِ گوشیه، و فقط اعلانِ همون بانکی " +
                "خونده می‌شه که خودت انتخاب کردی.",
            color = AppMuted,
            fontSize = 11.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

/** «ابزارها»: تقویمِ مالی/آمار/تاریخچه‌ی محاسبات. این‌ها فیچرن نه تنظیمات، ولی هیچ نقطه‌ی ورودیِ
 * دیگه‌ای تو اپ ندارن - پس به‌جای حذف از تنظیمات (که یعنی گم‌شدنشون)، زیرِ یه ردیفِ واحد جمع شدن. */
@Composable
private fun ToolsSettings(onOpenTool: (String) -> Unit) {
    AppCard(modifier = Modifier.padding(top = 8.dp)) {
        ToolRow(Icons.Filled.DateRange, "تقویم مالی", "سررسیدِ اقساطِ همه‌ی وام‌هات رو رو تقویم ببین") { onOpenTool("calendar") }
    }
    AppCard(modifier = Modifier.padding(top = 8.dp)) {
        ToolRow(Icons.Filled.Assessment, "آمار و گزارشات", "آمارِ کلیِ وام‌هات + خروجی PDF") { onOpenTool("stats") }
    }
    AppCard(modifier = Modifier.padding(top = 8.dp)) {
        ToolRow(Icons.Filled.History, "تاریخچه‌ی محاسبات", "مرورِ محاسبه‌های قبلیِ وام/سقف وام/سود سپرده") { onOpenTool("history") }
    }
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
private fun AboutSettings(banner: InAppBannerState) {
    val context = LocalContext.current
    AppCard(modifier = Modifier.padding(top = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Info, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text("جیبک", color = AppText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("نسخه ${toFa(BuildConfig.VERSION_NAME)}", color = AppMuted, fontSize = 12.sp)
            }
        }
        Text(aboutText, color = AppMuted, fontSize = 12.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 10.dp))
    }
    AppCard(label = "امتیازدهی", modifier = Modifier.padding(top = 8.dp)) {
        OutlinedButton(
            onClick = {
                // همون لینکِ فلیورِ فعلی که بنرِ آپدیت هم استفاده می‌کنه (MainActivity.kt).
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
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("به برنامه امتیاز بده")
        }
    }
    AppCard(label = "پشتیبانی", modifier = Modifier.padding(top = 8.dp)) {
        SupportContacts(banner)
    }
    AppCard(label = "حریم خصوصی", modifier = Modifier.padding(top = 8.dp)) {
        Text(privacyText, color = AppMuted, fontSize = 12.sp, lineHeight = 20.sp)
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


// نسخه‌ی «۱» قبلاً هاردکد بود (همیشه ثابت، هیچ‌وقت آپدیت نمی‌شد) - خواسته‌ی کاربر: نسخه‌ی واقعیِ
// نصب‌شده رو نشون بده. BuildConfig.VERSION_NAME همون versionNameِ CI (مثلاً "1.0.332") ئه.
private val aboutText = "جیبک — نسخه ${BuildConfig.VERSION_NAME}\n" +
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
