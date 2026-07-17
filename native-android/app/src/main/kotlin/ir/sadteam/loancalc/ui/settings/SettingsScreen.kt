package ir.sadteam.loancalc.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
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
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.haptics.HapticsViewModel
import ir.sadteam.loancalc.ui.history.CalculationHistoryScreen
import ir.sadteam.loancalc.ui.security.AppLockViewModel
import ir.sadteam.loancalc.ui.security.biometricAvailable
import ir.sadteam.loancalc.ui.stats.StatsScreen
import ir.sadteam.loancalc.ui.subscription.SubscriptionScreen
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.ThemeMode
import ir.sadteam.loancalc.ui.theme.ThemeViewModel

private val fontSizeOptions = listOf(0.9f to "کوچک", 1f to "متوسط", 1.15f to "بزرگ")
private val themeModeOptions = listOf(ThemeMode.LIGHT to "روشن", ThemeMode.DARK to "تاریک")

/** پورت ساده‌شده‌ی view-settings تو www/index.html - کارت حساب (accountCard) + خروج/ورود، اندازه
 * فونت (fontSizeChips)، یادآوری سررسید (کاملاً native-only، وب هنوز نداره - رجوع کن به
 * notifications/)، درباره‌برنامه/حریم‌خصوصی (toggleAbout/togglePrivacy، متن عینِ وب)، و صفحه‌ی
 * پشتیبانی (ایمیل/تلگرام/بله - پورت مفهومی از اپ رقیب VAMMAN؛ مقادیر SUPPORT_EMAIL/TELEGRAM/BALE
 * فعلاً placeholder ان، باید با اطلاعات واقعی جایگزین بشن). «تنظیمات پیشرفته یادآوری» (صدای اعلان،
 * سفارشی‌سازی به‌ازای هر وام) و فرم «نظرات و مشکلات» عمداً پورت نشدن - رو خودِ وب هم صرفاً UI
 * نمایشی/localStorage-فقط بودن، هیچ‌وقت واقعاً کاربردی نبودن (رجوع کن به کامنت خودِ وب). */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
    notificationsViewModel: NotificationsViewModel = hiltViewModel(),
    appLockViewModel: AppLockViewModel = hiltViewModel(),
    autoBackupViewModel: AutoBackupViewModel = hiltViewModel(),
    hapticsViewModel: HapticsViewModel = hiltViewModel(),
) {
    var showLoginPrompt by remember { mutableStateOf(false) }
    var showFinancialCalendar by remember { mutableStateOf(false) }
    var showStats by remember { mutableStateOf(false) }
    var showCheque by remember { mutableStateOf(false) }
    var showAccounts by remember { mutableStateOf(false) }
    var showSubscription by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }

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
        else -> "main"
    }

    AnimatedContent(
        targetState = screenKey,
        transitionSpec = { fadeIn(tween(200)).togetherWith(fadeOut(tween(150))) },
        label = "settingsScreen",
    ) { key ->
        when (key) {
            "login" -> LoginScreen(onDismiss = { showLoginPrompt = false }, onLoginSuccess = { showLoginPrompt = false })
            "calendar" -> FinancialCalendarScreen(onBack = { showFinancialCalendar = false })
            "stats" -> StatsScreen(onBack = { showStats = false })
            "cheque" -> ChequeScreen(onBack = { showCheque = false })
            "accounts" -> AccountsScreen(onBack = { showAccounts = false })
            "subscription" -> SubscriptionScreen(
                onBack = { showSubscription = false },
                onSubscribed = { showSubscription = false },
            )
            "history" -> CalculationHistoryScreen(onBack = { showHistory = false })
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
            )
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
) {
    val gateState by authViewModel.gateState.collectAsState()
    val phone by authViewModel.phone.collectAsState()
    val subscribed by authViewModel.subscribed.collectAsState()
    val fontScale by themeViewModel.fontScale.collectAsState()
    val notificationsEnabled by notificationsViewModel.enabled.collectAsState()
    val autoBackupEnabled by autoBackupViewModel.enabled.collectAsState()
    val lastAutoBackupAt by autoBackupViewModel.lastBackupAt.collectAsState()
    val vibrationEnabled by hapticsViewModel.enabled.collectAsState()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) notificationsViewModel.enable() }
    var searchQuery by remember { mutableStateOf("") }
    fun matches(vararg titles: String) =
        searchQuery.isBlank() || titles.any { it.contains(searchQuery.trim()) }
    val banner = rememberInAppBanner()

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
                            Brush.horizontalGradient(listOf(AppAccent.copy(alpha = 0.16f), Color.Transparent)),
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
                        Text(
                            if (subscribed) "مشترک — وام‌های من همگام‌سازی می‌شه" else "وارد حساب شدی",
                            color = AppMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        // اگه دلیل «مشترک بودن» فعلاً فقط دوره‌ی آزمایشیِ ۷روزه‌ست (نه خرید واقعی)،
                        // مهلتِ باقی‌مونده رو نشون بده - وقتی تموم شد، خودکار (سمت سرور) به حالت
                        // عادی برمی‌گرده و همون پیامِ «باید اشتراک بگیری» جای این رو می‌گیره.
                        val trialEndsAt by authViewModel.trialEndsAt.collectAsState()
                        val trialDaysLeft = trialEndsAt?.let { end ->
                            ((end - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt() + 1
                        }
                        if (subscribed && trialDaysLeft != null && trialDaysLeft in 1..7) {
                            Text(
                                "دوره‌ی آزمایشی رایگان: ${toFa(trialDaysLeft.toString())} روز مانده",
                                color = AppAccent,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp),
                            )
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

            if (gateState == GateState.LOGGED_IN && !subscribed && matches("اشتراک", "خرید اشتراک")) {
                AppCard(modifier = Modifier.padding(top = 10.dp)) {
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
                AppCard(label = "تم", modifier = Modifier.padding(top = 10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        themeModeOptions.forEach { (mode, label) ->
                            AppChip(
                                label = label,
                                selected = themeMode == mode,
                                onClick = { themeViewModel.setThemeMode(mode) },
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
                            "هر روز، برای اقساط سررسید نزدیک (امروز/فردا) یه نوتیف بده",
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
                AppCard(modifier = Modifier.padding(top = 10.dp)) {
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
                AppCard(modifier = Modifier.padding(top = 10.dp)) {
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
        }
    }

        InAppBannerHost(banner, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

// TODO: این سه تا مقدار موقتی/جای‌گیرنده‌ان - کاربر باید با ایمیل و آیدی واقعی تلگرام/بله خودش
// عوضشون کنه قبل از انتشار.
private const val SUPPORT_EMAIL = "support@example.com"
private const val SUPPORT_TELEGRAM_URL = "https://t.me/example_support"
private const val SUPPORT_BALE_URL = "https://ble.ir/example_support"

/** پورت مودال پشتیبانی اپ رقیب (VAMMAN) - ایمیل/تلگرام/بله، هرکدوم با تپ یه اپ خارجی باز می‌کنه. */
@Composable
private fun SupportContacts(banner: InAppBannerState) {
    val context = LocalContext.current
    Column {
        SupportRow(label = "ایمیل", value = SUPPORT_EMAIL) {
            openOrShowBanner(context, banner) {
                Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$SUPPORT_EMAIL"))
            }
        }
        SupportRow(label = "تلگرام", value = SUPPORT_TELEGRAM_URL, modifier = Modifier.padding(top = 8.dp)) {
            openOrShowBanner(context, banner) { Intent(Intent.ACTION_VIEW, Uri.parse(SUPPORT_TELEGRAM_URL)) }
        }
        SupportRow(label = "پیام‌رسان بله", value = SUPPORT_BALE_URL, modifier = Modifier.padding(top = 8.dp)) {
            openOrShowBanner(context, banner) { Intent(Intent.ACTION_VIEW, Uri.parse(SUPPORT_BALE_URL)) }
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
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 8) pin = it },
                    label = { Text("PIN جدید") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 8) confirmPin = it },
                    label = { Text("تکرار PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
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

private fun openOrShowBanner(context: android.content.Context, banner: InAppBannerState, buildIntent: () -> Intent) {
    try {
        context.startActivity(buildIntent())
    } catch (e: Exception) {
        banner.show("اپ مناسبی برای باز کردن این لینک پیدا نشد")
    }
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

private const val aboutText = "وام من — نسخه ۱\n" +
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
