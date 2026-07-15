package ir.sadteam.loancalc.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.auth.GateState
import ir.sadteam.loancalc.ui.auth.LoginScreen
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
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
) {
    var showLoginPrompt by remember { mutableStateOf(false) }
    val gateState by authViewModel.gateState.collectAsState()
    val phone by authViewModel.phone.collectAsState()
    val subscribed by authViewModel.subscribed.collectAsState()
    val fontScale by themeViewModel.fontScale.collectAsState()
    val notificationsEnabled by notificationsViewModel.enabled.collectAsState()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) notificationsViewModel.enable() }

    if (showLoginPrompt) {
        LoginScreen(onDismiss = { showLoginPrompt = false }, onLoginSuccess = { showLoginPrompt = false })
        return
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
            }
            Text("تنظیمات", color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
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
                        Button(
                            onClick = { showLoginPrompt = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
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
            .clickable(onClick = onClick)
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
                .clickable { expanded = !expanded },
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
