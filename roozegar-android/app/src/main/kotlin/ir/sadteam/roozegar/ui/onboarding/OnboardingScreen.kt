package ir.sadteam.roozegar.ui.onboarding

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.sadteam.roozegar.core.JalaliCalendar
import ir.sadteam.roozegar.core.format
import ir.sadteam.roozegar.core.toPersianDigits
import ir.sadteam.roozegar.ui.glass.AuroraGlassBackground
import ir.sadteam.roozegar.ui.glass.GlassCard
import ir.sadteam.roozegar.ui.theme.Gold
import ir.sadteam.roozegar.ui.theme.Teal
import ir.sadteam.roozegar.ui.theme.TextMuted
import kotlinx.coroutines.launch

/**
 * اینترو + گرفتن اجازه‌ها با توضیحِ «چرا» - طبق پرامپت: اول ورود اجازه‌ی اعلان و باتری گرفته می‌شه
 * که بعداً اعلان دائمی قطع نشه. هر اجازه‌ای رد بشه اپ گیر نمی‌کنه؛ از تنظیمات قابل فعال‌سازیه.
 * (اجازه‌ی تقویم دستگاه مال فاز ۲ - همگام‌سازی رویدادهاست و الان اصلاً درخواست نمی‌شه.)
 */
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    AuroraGlassBackground {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
        ) {
            Row(Modifier.fillMaxWidth()) {
                Spacer(Modifier.weight(1f))
                if (pagerState.currentPage < 3) {
                    TextButton(onClick = onDone) { Text("رد شدن", color = TextMuted) }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> NotificationPage()
                    2 -> BatteryPage()
                    3 -> FinishPage(onDone)
                }
            }

            // نشانگر صفحه‌ها + دکمه‌ی بعدی
            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(4) { i ->
                        Box(
                            Modifier
                                .size(if (i == pagerState.currentPage) 10.dp else 7.dp)
                                .background(
                                    if (i == pagerState.currentPage) Gold else TextMuted.copy(alpha = 0.4f),
                                    CircleShape,
                                ),
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                if (pagerState.currentPage < 3) {
                    Button(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        colors = ButtonDefaults.buttonColors(containerColor = Teal),
                    ) { Text("بعدی") }
                }
            }
        }
    }
}

/** لوگوی زنده‌ی اینترو: حلقه‌ی گرادیانی چرخان + عدد امروز - اختصاصی خود اپ، نه قالب آماده. */
@Composable
private fun AnimatedLogo(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "logo")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
        label = "logoSpin",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(2200), RepeatMode.Reverse),
        label = "logoPulse",
    )
    val today = remember { JalaliCalendar.today() }
    Box(modifier.size(170.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f
            rotate(angle) {
                drawCircle(
                    brush = Brush.sweepGradient(listOf(Teal, Gold, Color.Transparent, Teal)),
                    radius = r * 0.92f * pulse,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
                )
            }
            drawCircle(
                brush = Brush.radialGradient(listOf(Teal.copy(alpha = 0.25f), Color.Transparent)),
                radius = r * 0.8f,
                center = Offset(size.width / 2f, size.height / 2f),
            )
        }
        Text(
            today.d.toPersianDigits(),
            style = MaterialTheme.typography.displayLarge,
            color = Color.White,
        )
    }
}

@Composable
private fun WelcomePage() {
    val today = remember { JalaliCalendar.today() }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        AnimatedLogo()
        Spacer(Modifier.height(24.dp))
        Text("تقویم من", style = MaterialTheme.typography.displaySmall, color = Gold)
        Spacer(Modifier.height(8.dp))
        Text(
            "تقویم فارسی، به شیوه‌ی شیشه",
            style = MaterialTheme.typography.titleMedium,
            color = TextMuted,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "امروز ${today.format(withWeekday = true)}",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NotificationPage() {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(hasNotifPermission(context)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
    }
    PermissionPage(
        title = "تاریخ، همیشه بالای گوشی",
        body = "تقویم من عدد امروز رو تو نوار وضعیت و یه اعلان دائمی (تاریخ کامل شمسی، میلادی و قمری + " +
            "مناسبت روز) نشون می‌ده. برای این کار به اجازه‌ی اعلان نیاز داره - بی‌صداست و هیچ‌وقت مزاحم نمی‌شه.",
        buttonText = if (granted) "✓ فعال شد" else "فعال‌سازی اعلان",
        buttonEnabled = !granted,
    ) {
        if (Build.VERSION.SDK_INT >= 33) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            granted = true
        }
    }
}

@Composable
private fun BatteryPage() {
    val context = LocalContext.current
    var exempt by remember { mutableStateOf(isBatteryExempt(context)) }
    PermissionPage(
        title = "که هیچ‌وقت قطع نشه",
        body = "بعضی گوشی‌ها برای صرفه‌جویی باتری، اپ‌های پس‌زمینه رو می‌کشن و اعلان تاریخ می‌پره. " +
            "با خارج‌کردن تقویم من از بهینه‌سازی باتری، تاریخ و ویجت‌ها همیشه به‌روز می‌مونن - مصرف واقعی " +
            "اپ ناچیزه چون فقط شب‌ها یه لحظه بیدار می‌شه.",
        buttonText = if (exempt) "✓ انجام شد" else "خارج‌کردن از بهینه‌سازی باتری",
        buttonEnabled = !exempt,
        onClick = {
            try {
                context.startActivity(
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:${context.packageName}"),
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            } catch (_: ActivityNotFoundException) {
                openAppDetails(context)
            }
            exempt = isBatteryExempt(context)
        },
        extra = {
            if (needsAutostartHint()) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = { openAutostartSettings(context) }, modifier = Modifier.fillMaxWidth()) {
                    Text("تنظیمات «اجرای خودکار» گوشی ${brandFa()}", color = TextMuted)
                }
                Text(
                    "گوشی شما یه تنظیم جدا برای اجرای خودکار بعد از روشن‌شدن داره - «تقویم من» رو اونجا هم فعال کن.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        },
    )
}

@Composable
private fun FinishPage(onDone: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        AnimatedLogo()
        Spacer(Modifier.height(24.dp))
        Text("همه‌چیز آماده‌ست", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "ویجت‌های تقویم من رو هم از صفحه‌ی اصلی گوشی اضافه کن:\nنگه‌داشتن رو صفحه‌ی خالی ← ویجت‌ها ← تقویم من",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onDone,
            colors = ButtonDefaults.buttonColors(containerColor = Gold),
            modifier = Modifier.fillMaxWidth(0.7f),
        ) { Text("شروع", color = Color(0xFF0B1626)) }
    }
}

@Composable
private fun PermissionPage(
    title: String,
    body: String,
    buttonText: String,
    buttonEnabled: Boolean,
    extra: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    GlassCard(Modifier.fillMaxWidth(), contentPadding = 24.dp) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = Teal)
        Spacer(Modifier.height(12.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onClick,
            enabled = buttonEnabled,
            colors = ButtonDefaults.buttonColors(containerColor = Teal, disabledContainerColor = Teal.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth(),
        ) { Text(buttonText) }
        extra?.invoke()
    }
}

private fun hasNotifPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT < 33 ||
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

private fun isBatteryExempt(context: Context): Boolean =
    context.getSystemService(PowerManager::class.java)
        ?.isIgnoringBatteryOptimizations(context.packageName) == true

private fun openAppDetails(context: Context) {
    try {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    } catch (_: ActivityNotFoundException) {
        // هیچ صفحه‌ی تنظیماتی در دسترس نیست - بی‌خیال
    }
}

// برندهایی که «اجرای خودکار» جدا دارن و بدونش اپ بعد از بوت بالا نمیاد
private val AUTOSTART_BRANDS = mapOf(
    "xiaomi" to ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
    "oppo" to ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
    "vivo" to ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),
    "huawei" to ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
    "honor" to ComponentName("com.hihonor.systemmanager", "com.hihonor.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
)

internal fun needsAutostartHint(): Boolean =
    AUTOSTART_BRANDS.keys.any { Build.MANUFACTURER.lowercase().contains(it) }

internal fun brandFa(): String = when {
    Build.MANUFACTURER.lowercase().contains("xiaomi") -> "شیائومی"
    Build.MANUFACTURER.lowercase().contains("oppo") -> "اوپو"
    Build.MANUFACTURER.lowercase().contains("vivo") -> "ویوو"
    Build.MANUFACTURER.lowercase().contains("huawei") -> "هواوی"
    Build.MANUFACTURER.lowercase().contains("honor") -> "آنر"
    else -> ""
}

internal fun openAutostartSettings(context: Context) {
    val brand = AUTOSTART_BRANDS.entries.firstOrNull { Build.MANUFACTURER.lowercase().contains(it.key) }
    if (brand != null) {
        try {
            context.startActivity(Intent().setComponent(brand.value).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return
        } catch (_: Exception) {
            // مسیر اختصاصی برند در دسترس نبود (نسخه‌ی متفاوت رابط) - برو تنظیمات خود اپ
        }
    }
    openAppDetails(context)
}
