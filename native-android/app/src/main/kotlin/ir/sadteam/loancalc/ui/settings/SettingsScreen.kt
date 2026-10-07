package ir.sadteam.loancalc.ui.settings

import ir.sadteam.loancalc.ui.theme.AppRadius
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Storefront
import ir.sadteam.loancalc.ui.theme.AppPurple
import ir.sadteam.loancalc.ui.theme.AppGoldInk
import ir.sadteam.loancalc.ui.theme.AppGoldPillSoft
import ir.sadteam.loancalc.ui.components.AppHeroCard
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.coin.CoinHubScreen
import ir.sadteam.loancalc.ui.profile.GamificationViewModel
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.auth.GateState
import ir.sadteam.loancalc.ui.auth.LoginScreen
import ir.sadteam.loancalc.notifications.DeepLinkViewModel
import ir.sadteam.loancalc.ui.calendar.FinancialCalendarScreen
import ir.sadteam.loancalc.ui.components.AppHeroRow
import ir.sadteam.loancalc.ui.components.FramedAvatar
import ir.sadteam.loancalc.ui.goal.SavingsGoalScreen
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.haptics.HapticsViewModel
import ir.sadteam.loancalc.ui.history.CalculationHistoryScreen
import ir.sadteam.loancalc.ui.profile.AvatarViewModel
import ir.sadteam.loancalc.ui.profile.BadgesScreen
import ir.sadteam.loancalc.ui.security.AppLockViewModel
import ir.sadteam.loancalc.ui.subscription.SubscriptionScreen
import ir.sadteam.loancalc.ui.support.BugReportScreen
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSpacing
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.Motion
import ir.sadteam.loancalc.ui.theme.ThemeViewModel

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
    /** از آدمکِ سربرگِ خانه: مستقیم «حسابِ کاربری» باز شود و «بازگشت» کلِ صفحه را ببندد. */
    startAtAccount: Boolean = false,
) {
    var route by remember { mutableStateOf(if (startAtAccount) SettingsRoute.ACCOUNT else SettingsRoute.MAIN) }
    val jumpRoute by ir.sadteam.loancalc.ui.admin.AdminSignals.openSettingsRoute.collectAsState()
    LaunchedEffect(jumpRoute) {
        jumpRoute?.let { name ->
            SettingsRoute.entries.firstOrNull { it.name == name }?.let { route = it }
            ir.sadteam.loancalc.ui.admin.AdminSignals.openSettingsRoute.value = null
        }
    }
    val closeSub: () -> Unit = { if (startAtAccount) onBack() else route = SettingsRoute.MAIN }
    // زیرصفحه‌های «فیچری» (تقویم/آمار/تاریخچه) از رو خودِ صفحه‌ی «ابزارها» باز می‌شن، پس یه استیتِ
    // جدا لازم دارن تا با برگشت، به «ابزارها» برگردن نه به ریشه‌ی تنظیمات.
    var tool by remember { mutableStateOf<String?>(null) }
    var showLoginPrompt by remember { mutableStateOf(false) }
    var showSubscription by remember { mutableStateOf(false) }
    var showReminderSettings by remember { mutableStateOf(false) }
    // 🐞 گزارشِ مشکل - زیرصفحه‌ی تمام‌صفحه، مثلِ بقیه‌ی زیرصفحه‌های تنظیمات.
    var showBugReport by remember { mutableStateOf(false) }
    // «آمارِ جیبک» - فقط برای حسابِ صاحبِ برنامه (ورک‌فلوی make-admin).
    var showAdminStats by remember { mutableStateOf(false) }
    var showAdminSupport by remember { mutableStateOf(false) }
    val openAdminSignal by ir.sadteam.loancalc.ui.admin.AdminSignals.openAdmin.collectAsState()
    LaunchedEffect(openAdminSignal) {
        if (openAdminSignal) { showAdminStats = true; ir.sadteam.loancalc.ui.admin.AdminSignals.openAdmin.value = false }
    }
    LaunchedEffect(route) {
        if (route != SettingsRoute.MAIN) ir.sadteam.loancalc.data.UsageStats.screen("settings_" + route.name.lowercase())
    }

    val screenKey = when {
        showLoginPrompt -> "login"
        showSubscription -> "subscription"
        showReminderSettings -> "reminderSettings"
        showBugReport -> "bugReport"
        showAdminStats -> "adminStats"
        showAdminSupport -> "adminSupport"
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
            "adminStats" -> FullScreenDialog(onDismissRequest = { showAdminStats = false }) {
                ir.sadteam.loancalc.ui.admin.AdminHubScreen(onBack = { showAdminStats = false })
            }
            "adminSupport" -> FullScreenDialog(onDismissRequest = { showAdminSupport = false }) {
                ir.sadteam.loancalc.ui.admin.SupportInboxScreen(onBack = { showAdminSupport = false })
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
            "sub" -> FullScreenDialog(onDismissRequest = closeSub) {
                SettingsSubPage(
                    route = route,
                    onBack = closeSub,
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
                onOpenAdminStats = { showAdminStats = true },
                onOpenAdminSupport = { showAdminSupport = true },
            )
        }
    }
}
/** ردیف‌های ریشه‌ی تنظیمات؛ هر کدوم یه زیرصفحه‌ی تمام‌صفحه باز می‌کنه (بازطراحیِ خواسته‌ی کاربر طبقِ
 * اپِ مرجع - قبلاً همه‌ی سوییچ‌ها/فرم‌ها مستقیم تو خودِ لیستِ ریشه باز بودن و صفحه شلوغ بود). */
private enum class SettingsRoute(val title: String, val keywords: List<String>) {
    MAIN("تنظیمات", emptyList()),
    ACCOUNT("حساب کاربری", listOf("حساب", "اشتراک", "خروج", "شماره موبایل")),
    APPEARANCE("ظاهر برنامه", listOf("تم", "رنگ", "ویبره", "هپتیک", "لرزش", "اندازه فونت", "روشن", "تاریک", "ساده", "حالت ساده")),
    REMINDERS("یادآورها", listOf("یادآوری سررسید", "یادآوری روزانه", "نوتیف", "دنگ")),
    DATA("مدیریت داده‌ها", listOf("پشتیبان", "بکاپ", "بازیابی")),
    SMS("پیامک‌های بانکی", listOf("پیامک", "بانک", "خواندن خودکار")),
    BACKGROUND(
        "اجرای پس‌زمینه",
        listOf("پس زمینه", "همیشه روشن", "اجرای خودکار", "autostart", "باتری", "ری استارت", "بسته شدن"),
    ),
    TOOLS("ابزارها", listOf("تقویم مالی", "آمار", "گزارش", "تاریخچه محاسبات")),
    SECURITY("امنیت", listOf("قفل", "PIN", "اثر انگشت")),
    // ⚠️ نامش از «فروشگاهِ سکه» به «تمِ رنگی» رفت (`75c`): فروشگاه حالا یک در دارد
    // (سکه‌ی هدرِ خانه) و این ردیف فقط **لینکی** به همان است، نه درِ دوم. خودِ «ظاهر
    // برنامه» در تنظیمات می‌ماند چون سه چیز دارد و فقط یکی‌اش خریدنی است - اندازه‌ی
    // متن و انیمیشنِ کم دسترس‌پذیری‌اند و کاربری که متن برایش ریز است نباید برای
    // بزرگ‌کردنش وارد ویترین شود.
    COLOR_THEME("فروشگاه", listOf("تم", "رنگ", "پوسته", "سکه", "فروشگاه", "آیکون", "قلم", "فونت")),
    BADGES("نشان‌ها", listOf("نشان", "دستاورد", "مدال", "سکه")),
    PARSING_RULES("قاعده‌های تشخیص", listOf("قاعده", "دسته‌بندی خودکار", "تشخیص")),
    ABOUT("درباره‌ی جیبک", listOf("درباره", "پشتیبانی", "حریم خصوصی", "نسخه")),
}
@Composable
private fun SettingsMainContent(
    onBack: () -> Unit,
    authViewModel: AuthViewModel,
    hapticsViewModel: HapticsViewModel,
    onOpen: (SettingsRoute) -> Unit,
    onShowSubscription: () -> Unit,
    onOpenAdminStats: () -> Unit = {},
    onOpenAdminSupport: () -> Unit = {},
    adminViewModel: ir.sadteam.loancalc.ui.admin.AdminStatsViewModel = hiltViewModel(),
) {
    val isAdmin by adminViewModel.isAdmin.collectAsState()
    val gateState by authViewModel.gateState.collectAsState()
    val phone by authViewModel.phone.collectAsState()
    val subscribed by authViewModel.subscribed.collectAsState()
    val trialDaysLeft by authViewModel.trialDaysLeft.collectAsState()
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
                    .clip(RoundedCornerShape(AppRadius.icon))
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
            // درِ ادمین بالای صفحه (۸ مهر، خواسته‌ی کاربر: «هر بار تا پایین نروم»).
            if (isAdmin) {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(44.dp)
                        .clip(RoundedCornerShape(AppRadius.icon))
                        .background(ir.sadteam.loancalc.ui.theme.AppWarning.copy(alpha = 0.15f))
                        .pressScaleClickable(onClick = onOpenAdminStats),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.AdminPanelSettings, contentDescription = "ادمین", tint = ir.sadteam.loancalc.ui.theme.AppWarning, modifier = Modifier.size(24.dp))
                    val adminUnread by ir.sadteam.loancalc.ui.admin.AdminSignals.unreadSupport.collectAsState()
                    if (adminUnread > 0) ir.sadteam.loancalc.ui.admin.UnreadDot(adminUnread, Modifier.align(Alignment.TopEnd))
                }
            }
            // ۱۶ مهر: چرخ‌دنده‌ی تزئینی برداشته شد - صفحه خودش «تنظیمات» است و آن آیکن کلیک هم نداشت.
        }

        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
            // جستجوی واقعیِ همین صفحه - فقط شکلش قرصِ گرد شد.
            ir.sadteam.loancalc.ui.components.PillSearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "جستجو در تنظیمات…",
            )

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
                        // بی هاله - دقیقاً مثلِ آدمکِ سربرگِ خانه (خواسته‌ی کاربر، ۳ مهر).
                        Box(
                            modifier = Modifier.size(52.dp),
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
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false),
                                )
                                // حلقه‌ی طلاییِ قبلیِ سربرگ تنها نشانه‌ی اشتراک بود؛ حالا این‌جاست.
                                if (subscribed) {
                                    Text(
                                        "مشترک",
                                        color = AppGoldInk,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        maxLines = 1,
                                        softWrap = false,
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
                    title = "اشتراکِ ویژه",
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
                        Icons.Filled.Storefront,
                        SettingsRoute.COLOR_THEME,
                        tone = SettingsTone.PURPLE,
                        status = "تم، آیکون، قلم و نمادها",
                    ) { onOpen(SettingsRoute.COLOR_THEME) }
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
internal fun SettingsSwitchRow(
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
    SettingsRoute.REMINDERS -> "یادآوریِ سررسید، ثبتِ روزانه و دنگ"
    SettingsRoute.APPEARANCE -> "حالتِ روشن و تیره، اندازه‌ی متن، لرزشِ لمسی"
    SettingsRoute.SECURITY -> "قفل با رمزِ عددی و اثرِ انگشت"
    SettingsRoute.TOOLS -> "تقویمِ مالی و خروجی‌ها"
    else -> null
}
/** سرآیندِ مشترکِ همه‌ی زیرصفحه‌های تنظیمات (عنوان وسط + ضربدرِ بستن) - هم‌شکلِ اپِ مرجع. */
@Composable
internal fun SettingsSubPageScaffold(
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
                    .clip(RoundedCornerShape(AppRadius.icon))
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
    // ویندوی دیالوگ LocalDensity را از نو می‌سازد و «اندازه‌ی متن»ِ برنامه گم می‌شد.
    val outerDensity = androidx.compose.ui.platform.LocalDensity.current
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
            // ۱۰ مهر: ویندو زیرِ نوارِ ناوبریِ پایین نمی‌رفت و گوشه‌ی پایین‌راست لکه‌ی تیره‌ی
            // پنلِ پشتش را نشان می‌داد. حالا لبه‌تالبه است و Box پایین خودش inset را رعایت می‌کند.
            dialogWindow?.let { androidx.core.view.WindowCompat.setDecorFitsSystemWindows(it, false) }
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppSurface)
                    .windowInsetsPadding(
                        WindowInsets.systemBars.union(WindowInsets.ime),
                    ),
            ) {
                androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides outerDensity) {
                    content()
                }
            }
        }
    }
}


// ⚠️ نشانی **یک‌جا** تعریف شده (`ui/support`)؛ دو کپی یعنی روزی که یکی عوض می‌شود و
// نیمی از پیام‌ها به صندوقِ قدیمی می‌روند.
