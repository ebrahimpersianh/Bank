package ir.sadteam.loancalc.ui.settings

import androidx.compose.material.icons.filled.Build
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppCardVariant
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.launch

@Composable
internal fun ReminderToggles(
    notificationsViewModel: NotificationsViewModel,
    onShowReminderSettings: () -> Unit,
) {
    val context = LocalContext.current
    val notificationsEnabled by notificationsViewModel.enabled.collectAsState()
    val dailyExpenseReminderEnabled by notificationsViewModel.dailyExpenseReminderEnabled.collectAsState()
    val dangReminderEnabled by notificationsViewModel.dangReminderEnabled.collectAsState()
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

    // ── دنگ: یادآورِ سهمِ نیامده - روشنِ پیش‌فرض، کلیدِ خاموش‌کردن ───────────────
    SettingsGroupLabel("دنگ", accent = AppWarningInk)
    SettingsGroup {
        SettingsRowItem(
            title = "یادآورِ سهمِ دوست‌ها",
            icon = Icons.Filled.Notifications,
            tone = SettingsTone.ORANGE,
            status = if (dangReminderEnabled) "دنگی که ۳ روز گذشته و سهمِ کسی نیامده" else "خاموش",
            statusTone = if (dangReminderEnabled) StatusTone.HEALTHY else StatusTone.NEUTRAL,
            checked = dangReminderEnabled,
            onCheckedChange = { notificationsViewModel.setDangReminderEnabled(it) },
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
