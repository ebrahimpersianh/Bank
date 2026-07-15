package ir.sadteam.loancalc.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
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
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.auth.GateState
import ir.sadteam.loancalc.ui.auth.LoginScreen
import ir.sadteam.loancalc.ui.calendar.FinancialCalendarScreen
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.security.AppLockViewModel
import ir.sadteam.loancalc.ui.security.biometricAvailable
import ir.sadteam.loancalc.ui.stats.StatsScreen
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.ThemeViewModel

private val fontSizeOptions = listOf(0.9f to "کوچک", 1f to "متوسط", 1.15f to "بزرگ")

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
) {
    var showLoginPrompt by remember { mutableStateOf(false) }
    var showFinancialCalendar by remember { mutableStateOf(false) }
    var showStats by remember { mutableStateOf(false) }

    // پورت حس تعویض نرم بین حالت‌های مختلف پنل تنظیمات (اصلی/ورود/تقویم مالی/آمار) - قبلاً هرکدوم
    // با یه return زودهنگام یهو جایگزین بقیه می‌شد؛ حالا با AnimatedContent (fade ظریف) عوض می‌شه.
    val screenKey = when {
        showLoginPrompt -> "login"
        showFinancialCalendar -> "calendar"
        showStats -> "stats"
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
            else -> SettingsMainContent(
                onBack = onBack,
                authViewModel = authViewModel,
                themeViewModel = themeViewModel,
                notificationsViewModel = notificationsViewModel,
                appLockViewModel = appLockViewModel,
                onShowLoginPrompt = { showLoginPrompt = true },
                onShowFinancialCalendar = { showFinancialCalendar = true },
                onShowStats = { showStats = true },
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
    onShowLoginPrompt: () -> Unit,
    onShowFinancialCalendar: () -> Unit,
    onShowStats: () -> Unit,
) {
    val gateState by authViewModel.gateState.collectAsState()
    val phone by authViewModel.phone.collectAsState()
    val subscribed by authViewModel.subscribed.collectAsState()
    val fontScale by themeViewModel.fontScale.collectAsState()
    val notificationsEnabled by notificationsViewModel.enabled.collectAsState()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) notificationsViewModel.enable() }

    Column(modifier = Modifier.fillMaxWidth()) {
        // پورت پروفایل بالای پنل تنظیمات اپ رقیب (VAMMAN): آواتار + برچسب وضعیت («نسخه عادی»/«نسخه
        // اشتراکی»)؛ اگه مشترک باشیم، کادر دور آواتار طلایی و ضخیم‌تر می‌شه.
        Row(
            modifier = Modifier
                .fillMaxWidth()
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

            AccordionCard(title = "امنیت", modifier = Modifier.padding(top = 10.dp)) {
                SecuritySettings(appLockViewModel)
            }

            AccordionCard(title = "پشتیبانی", modifier = Modifier.padding(top = 10.dp)) {
                SupportContacts()
            }

            AccordionCard(title = "درباره برنامه", modifier = Modifier.padding(top = 10.dp)) {
                Text(aboutText, color = AppMuted, fontSize = 12.sp, lineHeight = 20.sp)
            }

            AccordionCard(title = "حریم خصوصی", modifier = Modifier.padding(top = 10.dp)) {
                Text(privacyText, color = AppMuted, fontSize = 12.sp, lineHeight = 20.sp)
            }
        }
    }
}

// TODO: این سه تا مقدار موقتی/جای‌گیرنده‌ان - کاربر باید با ایمیل و آیدی واقعی تلگرام/بله خودش
// عوضشون کنه قبل از انتشار.
private const val SUPPORT_EMAIL = "support@example.com"
private const val SUPPORT_TELEGRAM_URL = "https://t.me/example_support"
private const val SUPPORT_BALE_URL = "https://ble.ir/example_support"

/** پورت مودال پشتیبانی اپ رقیب (VAMMAN) - ایمیل/تلگرام/بله، هرکدوم با تپ یه اپ خارجی باز می‌کنه. */
@Composable
private fun SupportContacts() {
    val context = LocalContext.current
    Column {
        SupportRow(label = "ایمیل", value = SUPPORT_EMAIL) {
            openOrToast(context) {
                Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$SUPPORT_EMAIL"))
            }
        }
        SupportRow(label = "تلگرام", value = SUPPORT_TELEGRAM_URL, modifier = Modifier.padding(top = 8.dp)) {
            openOrToast(context) { Intent(Intent.ACTION_VIEW, Uri.parse(SUPPORT_TELEGRAM_URL)) }
        }
        SupportRow(label = "پیام‌رسان بله", value = SUPPORT_BALE_URL, modifier = Modifier.padding(top = 8.dp)) {
            openOrToast(context) { Intent(Intent.ACTION_VIEW, Uri.parse(SUPPORT_BALE_URL)) }
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
    }
}

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

private fun openOrToast(context: android.content.Context, buildIntent: () -> Intent) {
    try {
        context.startActivity(buildIntent())
    } catch (e: Exception) {
        Toast.makeText(context, "اپ مناسبی برای باز کردن این لینک پیدا نشد", Toast.LENGTH_SHORT).show()
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
