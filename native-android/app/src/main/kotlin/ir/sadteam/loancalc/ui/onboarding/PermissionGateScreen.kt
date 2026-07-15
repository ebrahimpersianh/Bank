package ir.sadteam.loancalc.ui.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

private fun notificationsGranted(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

private fun batteryUnrestricted(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}

/**
 * پورت گیت مجوز اولیه‌ی اپ رقیب (VAMMAN) - قبل از هر چیز دیگه‌ای (حتی گیت ورود/مهمان) نشون داده
 * می‌شه. برخلاف رقیب که ۳ شرط داشت (یکیش «Exact Alarm» که برای معماری ما - WorkManager، نه
 * AlarmManager خام - اصلاً لازم نیست)، اینجا فقط دو شرطی که واقعاً رو کارکرد یادآوری سررسید تاثیر
 * دارن چک می‌شه: مجوز نوتیفیکیشن، و معافیت از بهینه‌سازی باتری (وگرنه خیلی از گوشی‌ها اجرای
 * پس‌زمینه‌ی WorkManager رو می‌کشن و یادآوری‌ها اصلاً نمی‌رسن). این گیت هر بار اپ باز می‌شه دوباره
 * چک می‌شه (نه فقط یه‌بار مثل گیت ورود)، چون کاربر می‌تونه بعداً از تنظیمات گوشی این مجوزها رو
 * دستی خاموش کنه.
 */
@Composable
fun PermissionGateScreen(onAllGranted: () -> Unit) {
    val context = LocalContext.current
    var notifOk by remember { mutableStateOf(notificationsGranted(context)) }
    var batteryOk by remember { mutableStateOf(batteryUnrestricted(context)) }

    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notifOk = notificationsGranted(context)
    }
    val batteryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        batteryOk = batteryUnrestricted(context)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notifOk = notificationsGranted(context)
                batteryOk = batteryUnrestricted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(notifOk, batteryOk) {
        if (notifOk && batteryOk) onAllGranted()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(40.dp))
        Text(
            "برای کارکرد درست یادآوری‌ها",
            color = AppText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            "لطفاً این دسترسی‌ها رو فعال کن:",
            color = AppMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 6.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .background(AppSurface, RoundedCornerShape(14.dp))
                .border(1.dp, AppLine, RoundedCornerShape(14.dp)),
        ) {
            PermissionRow(
                title = "اجازه نوتیفیکیشن",
                granted = notifOk,
                actionLabel = "Allow",
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
            )
            HorizontalDivider(color = AppLine)
            PermissionRow(
                title = "باتری: نامحدود / بدون بهینه‌سازی",
                granted = batteryOk,
                actionLabel = "Open",
                onClick = {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    batteryLauncher.launch(intent)
                },
            )
        }

        Text(
            "تا وقتی هر دو مورد ✅ نشن، ورود به برنامه انجام نمی‌شه.",
            color = AppMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp),
        )

        OutlinedButton(
            onClick = {
                notifOk = notificationsGranted(context)
                batteryOk = batteryUnrestricted(context)
            },
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("بررسی دوباره")
        }
    }
}

@Composable
private fun PermissionRow(title: String, granted: Boolean, actionLabel: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(title, color = AppText, fontSize = 14.sp)
            Text(
                if (granted) "فعال است ✅" else "باید فعال شود",
                color = if (granted) AppPrimary else AppMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (!granted) {
            GradientButton(onClick = onClick) {
                Text(actionLabel)
            }
        }
    }
}
