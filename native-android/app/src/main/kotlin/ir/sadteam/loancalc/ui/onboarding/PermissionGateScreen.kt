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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
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
import ir.sadteam.loancalc.ui.components.JibakBrandMark
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppGoldFrom
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

private fun notificationsGranted(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

/**
 * سازنده‌هایی که علاوه بر معافیتِ باتریِ استاندارد، یه **کشندهِ‌ی پس‌زمینه‌ی اختصاصی** هم دارند -
 * قاعده‌ی `35e`ی فایلِ طراحی («بی این‌ها سرویس روی شیائومی و سامسونگ بی‌صدا می‌میرد»).
 *
 * این مورد **شرطِ گیت نیست و نمی‌تواند باشد**: «اجرای خودکار» (Autostart) هیچ API عمومی برای
 * خواندن ندارد، پس اگر شرطش می‌کردیم کاربر برای همیشه در گیت حبس می‌شد. پس فقط یه ردیفِ
 * راهنماست، و فقط روی همین سازنده‌ها نشان داده می‌شود تا برای کاربرِ پیکسل/نوکیا شلوغی نکند.
 */
private val AGGRESSIVE_OEMS = setOf(
    "xiaomi", "redmi", "poco", "samsung", "huawei", "honor", "oppo", "vivo", "realme", "meizu",
)

private fun hasAggressiveBackgroundKiller(): Boolean =
    Build.MANUFACTURER.lowercase() in AGGRESSIVE_OEMS || Build.BRAND.lowercase() in AGGRESSIVE_OEMS

private fun batteryUnrestricted(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}

/**
 * هر دو شرطِ گیت از قبل برقرارن؟ - **هم‌زمان (بدونِ رندر)** حساب می‌شه.
 *
 * باگی که با این رفع شد: `AppRoot` گیت رو با مقدارِ اولیه‌ی `false` شروع می‌کرد، پس حتی وقتی
 * کاربر هر دو مجوز رو از قبل داده بود یه فریم از صفحه‌ی مجوز رندر می‌شد و بعد `LaunchedEffect`
 * ردش می‌کرد - کاربر این رو به‌عنوانِ «هر بار بعدِ اسپلش یه صفحه سریع میاد» گزارش داد.
 */
fun permissionGateSatisfied(context: Context): Boolean =
    notificationsGranted(context) && batteryUnrestricted(context)

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
    // بدونِ این، لحظه‌ی گرفتنِ آخرین مجوز یه فریمِ اضافه از خودِ صفحه دیده می‌شه.
    if (notifOk && batteryOk) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(40.dp))
        // نشانِ برند: این گیت **هر بار** باز شدنِ اپ ارزیابی می‌شود، پس صفحه‌ای است که کاربر
        // زیاد می‌بیند - و تا الان تنها صفحه‌ی مسیرِ ورود بود که شبیهِ دیالوگِ سیستمی بود.
        JibakBrandMark(width = 48.dp)
        Spacer(Modifier.height(24.dp))
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
                title = "اجازه‌ی اعلان",
                granted = notifOk,
                // ⚠️ قبلاً "Allow" بود - انگلیسیِ خام در برنامه‌ای که سراسر فارسی است، و کاربرِ
                // هدفِ ما (فارسی‌زبانِ ایرانی) لازم نیست بداند دکمه‌ی سیستمی چه اسمی دارد.
                actionLabel = "اجازه می‌دهم",
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
            )
            HorizontalDivider(color = AppLine)
            PermissionRow(
                title = "باتری بدونِ محدودیت",
                granted = batteryOk,
                // ⚠️ قبلاً "Open" بود. عنوان هم از «باتری: نامحدود / بدون بهینه‌سازی» ساده شد -
                // دو اصطلاحِ فنیِ هم‌معنی با یه اسلش بینشان.
                actionLabel = "تنظیمات",
                onClick = {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    batteryLauncher.launch(intent)
                },
            )
        }

        Text(
            "تا وقتی هر دو مورد فعال نشن، ورود به برنامه انجام نمی‌شه.",
            color = AppMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp),
        )

        // ── تله‌ی دومِ قاعده‌ی `35e` ──
        // اختیاری و بی‌گیت (بالا توضیح داده شد چرا نمی‌تواند شرط باشد). عمداً `ACTION_APPLICATION`
        // `_DETAILS_SETTINGS`ِ استاندارد را باز می‌کند و نه Intentِ اختصاصیِ برند (`miui.intent`,
        // `com.samsung...`): آن‌ها روی نسخه‌های مختلفِ همان سازنده هم ثابت نیستند و
        // `ActivityNotFoundException` می‌دهند. صفحه‌ی استاندارد همه‌جا هست و «باتری» و «اجرای
        // خودکار» هر دو یک تپ داخلش‌اند.
        if (hasAggressiveBackgroundKiller()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .background(AppGoldFrom, RoundedCornerShape(14.dp))
                    .padding(14.dp),
            ) {
                Text(
                    "یک قدمِ اختیاری",
                    color = AppAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "گوشیِ تو علاوه بر بهینه‌سازیِ باتری، یک «اجرای خودکار» جدا هم دارد. اگر خاموش " +
                        "باشد یادآوری‌ها می‌رسند ولی خواندنِ خودکارِ پیامک و اعلانِ بانک بعد از چند " +
                        "ساعت قطع می‌شود. از صفحه‌ی تنظیماتِ برنامه روشنش کن.",
                    color = AppText,
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
                OutlinedButton(
                    onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                },
                            )
                        }
                    },
                    modifier = Modifier.padding(top = 10.dp),
                ) {
                    Text("تنظیماتِ برنامه")
                }
            }
        }

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
                // ⚠️ قبلاً «فعال است ✅» بود - ایموجی به‌جای وضعیت. حالا تیکِ واقعی سمتِ دیگرِ
                // ردیف می‌نشیند (جای دکمه)، پس چشم یک ستونِ وضعیت می‌بیند نه دو نشانه‌ی پراکنده.
                if (granted) "فعال است" else "باید فعال شود",
                color = if (granted) AppPrimary else AppMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (granted) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(AppPrimary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = AppBg,
                    modifier = Modifier.size(15.dp),
                )
            }
        } else {
            GradientButton(onClick = onClick) {
                Text(actionLabel)
            }
        }
    }
}
