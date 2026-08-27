package ir.sadteam.loancalc.ui.settings

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
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
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.auth.GateState
import ir.sadteam.loancalc.ui.auth.LoginScreen
import ir.sadteam.loancalc.ui.calendar.FinancialCalendarScreen
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppCardVariant
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.AppHeroRow
import ir.sadteam.loancalc.ui.components.AvatarPicker
import ir.sadteam.loancalc.ui.components.AvatarView
import ir.sadteam.loancalc.ui.components.GoldSheenBox
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.InAppBannerState
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
import ir.sadteam.loancalc.ui.profile.CoinWalletScreen
import ir.sadteam.loancalc.ui.security.AppLockViewModel
import ir.sadteam.loancalc.ui.security.biometricAvailable
import ir.sadteam.loancalc.ui.stats.StatsScreen
import ir.sadteam.loancalc.ui.subscription.SubscriptionScreen
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
import ir.sadteam.loancalc.ui.theme.AppPrimaryBorder
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
                    "coins" -> CoinWalletScreen(onBack = { tool = null })
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

            // ── ردیفِ پروفایل (فریمِ `27d`) ────────────────────────────────────────
            if (searchQuery.isBlank()) {
                AppCard(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .pressScaleClickable(scale = 0.99f) { onOpen(SettingsRoute.ACCOUNT) },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(AppPrimaryPill)
                                .border(2.dp, AppPrimary, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                tint = AppPrimaryInk,
                                modifier = Modifier.size(30.dp),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (gateState == GateState.LOGGED_IN) "حساب کاربری" else "وارد نشدی",
                                color = AppText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                            )
                            if (gateState == GateState.LOGGED_IN && phone != null) {
                                // شماره ذاتاً چپ‌به‌راسته ولی جای خودش راست‌چین می‌مونه.
                                Ltr {
                                    Text(
                                        toFa(phone ?: ""),
                                        color = AppMuted,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 2.dp),
                                    )
                                }
                            }
                        }
                        Icon(
                            Icons.Filled.KeyboardArrowLeft,
                            contentDescription = null,
                            tint = AppLabel,
                            modifier = Modifier.size(13.dp),
                        )
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

            SettingsSectionLabel("ثبتِ خودکار")
            SettingsGroup {
                if (matches(SettingsRoute.SMS)) {
                    SettingsRow(
                        Icons.Filled.Sms,
                        SettingsRoute.SMS,
                        tone = SettingsTone.GREEN,
                    ) { onOpen(SettingsRoute.SMS) }
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

            SettingsSectionLabel("برنامه")
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
        status = status ?: value,
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
private fun SettingsSectionLabel(text: String) = SettingsGroupLabel(text)

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
    onOpenRules: () -> Unit,
) {
    val banner = rememberInAppBanner()
    Box(modifier = Modifier.fillMaxSize()) {
        SettingsSubPageScaffold(title = route.title, onBack = onBack) {
            when (route) {
                SettingsRoute.ACCOUNT -> AccountSettings(authViewModel, banner, onShowLoginPrompt, onShowSubscription)
                SettingsRoute.APPEARANCE -> AppearanceSettings(themeViewModel)
                SettingsRoute.REMINDERS -> ReminderToggles(notificationsViewModel, onShowReminderSettings)
                SettingsRoute.DATA -> DataSettings(authViewModel, autoBackupViewModel, banner)
                SettingsRoute.SMS -> SmsSettings(smsAutoImportViewModel, onOpenRules = { onOpenRules() })
                SettingsRoute.TOOLS -> ToolsSettings(onOpenTool)
                // دیگه تو یه AppCardِ بیرونی پیچیده نمی‌شه - خودش گروه‌های خودشو داره.
                SettingsRoute.SECURITY -> SecuritySettings(appLockViewModel)
                SettingsRoute.PARSING_RULES -> ParsingRulesScreen()
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

    var showAvatarSheet by remember { mutableStateOf(false) }
    var showNameSheet by remember { mutableStateOf(false) }

    if (gateState == GateState.LOGGED_IN) {
        // ── کارتِ هویت (فریمِ حساب کاربری) ────────────────────────────────────
        // دکمه‌ی تمام‌عرضِ «تغییرِ آدمک» عمداً حذف شد - مدادِ روی خودِ آدمک همون کاره و
        // دکمه‌ی تمام‌عرض بالای صفحه وزنِ بی‌دلیل می‌گیره.
        val avatarViewModel: AvatarViewModel = hiltViewModel()
        val avatar by avatarViewModel.avatar.collectAsState()
        AppCard(modifier = Modifier.padding(top = 8.dp), contentPadding = 20.dp) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box {
                    AvatarView(avatar, size = 72.dp)
                    // ناحیه‌ی لمس ۴۴ه ولی خودِ مداد ۲۶ - قاعده‌ی «هدفِ لمسی بزرگ‌تر از نشانه».
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .size(AppSpacing.minTouchTarget)
                            .pressScaleClickable { showAvatarSheet = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(AppSurface)
                                .border(1.5.dp, AppLine, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "تغییرِ آدمک",
                                tint = AppMuted,
                                modifier = Modifier.size(12.dp),
                            )
                        }
                    }
                }
                Text(
                    if (savedName.isNullOrBlank()) "بی‌نام" else savedName!!,
                    color = AppText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Ltr {
                    Text(
                        toFa(phone ?: ""),
                        color = AppMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 3.dp),
                    )
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
                tone = SettingsTone.GREEN,
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
        // ── خروج ─────────────────────────────────────────────────────────────
        // فاصله‌ی **دو برابرِ** فاصله‌ی معمولِ کارت‌ها - تنها جای برنامه که فاصله‌ی
        // غیرِتوکن مجازه، چون دکمه‌ی مخرب نباید تو ریتمِ عادیِ صفحه بشینه.
        var showLogoutConfirm by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppSpacing.betweenCards * 2)
                .height(48.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(AppDangerPill)
                .border(2.dp, AppDanger, RoundedCornerShape(999.dp))
                .pressScaleClickable { showLogoutConfirm = true },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = AppDanger,
                modifier = Modifier.size(15.dp),
            )
            Text(
                "خروج از حساب",
                color = AppDanger,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 7.dp),
            )
        }
        // الزامِ فروشگاه‌ها: راهِ داخل‌برنامه‌ای برای حذفِ کاملِ حساب. **ظاهرِ کم‌وزن،
        // مسیرِ سخت** - برعکسِ خروج که ظاهرِ پروزن و مسیرِ آسون داره.
        Text(
            "حذفِ کاملِ حساب کاربری",
            color = AppLabel,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
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

    // ── سه‌حالتیِ روشن · تیره · سیستم ─────────────────────────────────────────
    AppCard(modifier = Modifier.padding(top = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            themeModeOptions.forEach { (mode, label) ->
                val selected = themeMode == mode
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = AppSpacing.minTouchTarget)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (selected) AppSurface2 else Color.Transparent)
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
                        tint = if (selected) AppText else AppMuted,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        label,
                        color = if (selected) AppText else AppMuted,
                        fontSize = 11.sp,
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
    SettingsGroupLabel("خواندن")
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

    // ── کارتِ وضعیتِ پشتیبان - «یک نگاه، جواب می‌گیرد» ────────────────────────
    SettingsGroup(modifier = Modifier.padding(top = 8.dp)) {
        SettingsRowItem(
            title = "پشتیبان‌گیریِ خودکارِ روزانه",
            icon = Icons.Filled.CloudUpload,
            tone = SettingsTone.GREEN,
            status = when {
                !autoBackupEnabled -> "خاموش - هیچ نسخه‌ی پشتیبانی ساخته نمی‌شود"
                lastBackupLabel != null -> "آخرین پشتیبان: $lastBackupLabel"
                else -> "روشن - هنوز پشتیبانی ساخته نشده"
            },
            statusTone = when {
                !autoBackupEnabled -> StatusTone.NEUTRAL
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
                onClick = {
                    autoBackupViewModel.restoreFromAutoBackup { ok ->
                        banner.show(
                            if (ok) "بازیابی از پشتیبان خودکار انجام شد" else "پشتیبانی برای بازیابی پیدا نشد",
                            isSuccess = ok,
                        )
                    }
                },
            )
            // بازیابی از سرور فقط برای کاربرِ واردشده‌ی مشترکه - پوش به سرور هم فقط برای همونه.
            if (gateState == GateState.LOGGED_IN && subscribed) {
                SettingsDivider()
                SettingsRowItem(
                    title = "بازیابی از سرورِ ابری",
                    icon = Icons.Filled.CloudDownload,
                    tone = SettingsTone.GREEN,
                    status = "برای وقتی گوشی عوض شده یا برنامه پاک شده",
                    onClick = {
                        autoBackupViewModel.restoreFromCloud { ok ->
                            banner.show(
                                if (ok) "بازیابی از سرور ابری انجام شد" else "پشتیبانی رو سرور ابری پیدا نشد",
                                isSuccess = ok,
                            )
                        }
                    },
                )
            }
        }
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

    if (showParseTest) {
        SmsParseTestScreen(onBack = { showParseTest = false })
        return
    }

    // ── کارتِ وضعیت - تنها کارتِ برجسته‌ی صفحه، سه حالت ──────────────────────
    // تصمیمِ تاییدشده‌ی طراح: اگه اجازه قطع شده، **کلید حالتِ چهارم نمی‌گیره** - همین کارت
    // به حالتِ خطا می‌ره و دکمه‌ی «اجازه بده» می‌گیره.
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
                    onClick = if (hasSender) null else ({}),
                )
                if (index != ordered.lastIndex) SettingsDivider()
            }
        }
    }

    // ── گروهِ ابزارها ────────────────────────────────────────────────────────
    SettingsGroupLabel("ابزارها")
    SettingsGroup {
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
        // **راهنمای سه‌قدمیِ کارتِ `35c`** - صفحه‌ای که باز می‌شه مالِ اندروزیده نه جیبک، پس
        // کاربر باید از قبل بدونه اونجا دنبالِ چی بگرده.
        NotificationPermissionSteps(modifier = Modifier.padding(top = 12.dp))
        GradientButton(
            onClick = { openListenerSettings() },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) {
            Text("بریم به تنظیماتِ گوشی")
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
}

/** آیا اندروید واقعاً مجوزِ خواندنِ اعلان‌ها رو به این اپ داده؟ (قاعده‌ی «سوئیچ دروغ نمی‌گوید».) */
private fun notificationListenerGranted(context: Context): Boolean =
    NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

/** سه قدمِ کارتِ `35c` - همون‌طور که طرح می‌خواد: شماره‌دار، کوتاه، و با لحنِ «چی می‌بینی». */
@Composable
private fun NotificationPermissionSteps(modifier: Modifier = Modifier) {
    val steps = listOf(
        "تو لیستِ «دسترسی به اعلان‌ها» دنبالِ «جیبک» بگرد - لیست الفبایی نیست، تا پایین برو.",
        "کلیدِ کنارِ اسمش رو روشن کن.",
        "تو پنجره‌ی تاییدِ اندروید، «اجازه» رو بزن.",
    )
    Column(modifier = modifier) {
        steps.forEachIndexed { index, step ->
            Row(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    toFa(index + 1),
                    color = AppPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    step,
                    color = AppMuted,
                    fontSize = 11.5.sp,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
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
        // کیفِ سکه (کارتِ `20d`) - طرح می‌گه «تبِ جدید در نوارِ پایین اضافه نشد؛ پنج تب سقفِ
        // خوانایی است»، پس از همین‌جا باز می‌شه.
        ToolRow(Icons.Filled.Savings, "کیفِ سکه", "موجودی و تاریخچه‌ی سکه‌هایی که جمع کردی") { onOpenTool("coins") }
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
    SettingsGroupLabel("قفلِ برنامه")
    SettingsGroup {
        SettingsRowItem(
            title = "قفل با رمزِ عددی",
            icon = Icons.Filled.Lock,
            tone = SettingsTone.RED,
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
            tone = SettingsTone.RED,
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
    }

    // ── کارتِ توضیحِ ته صفحه ──────────────────────────────────────────────────
    // جمله‌ی سوم مهم‌ترینه: بی اون، کاربرِ فراموش‌کار فکر می‌کنه داده‌ش رفته و اپ رو پاک می‌کنه.
    AppCard(
        backgroundColor = AppSurface2,
        borderColor = AppLineRow,
        shadow = false,
        modifier = Modifier.padding(top = AppSpacing.betweenCards),
    ) {
        Text(
            "قفلِ برنامه فقط جلوی بازشدنِ برنامه رو همین گوشی رو می‌گیره. داده‌هات رو سرور با " +
                "حسابِ کاربریت محافظت می‌شه، نه با این رمز. اگه رمز رو فراموش کنی، با ورودِ " +
                "دوباره به حساب بازش می‌کنی.",
            color = AppMuted,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 20.sp,
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
