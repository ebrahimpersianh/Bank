package ir.sadteam.loancalc.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.REMINDER_OFFSET_OPTIONS
import ir.sadteam.loancalc.core.reminderOffsetLabel
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * تنظیماتِ حرفه‌ای/پیش‌فرضِ سراسریِ یادآوری - جایگزینِ سوییچِ ساده‌ی قبلی که فقط «امروز/فردا»ی ثابت
 * داشت. از اینجا هم زمان‌بندی (چندتا انتخابِ هم‌زمان از ۱/۳/۷ روز قبل) هم صدا هم ویبره تنظیم می‌شه؛
 * هر وام/چکی که تنظیمِ اختصاصیِ خودش رو نداشته باشه (رجوع کن به LoanDetailScreen/ChequeDetailScreen)
 * از همین پیش‌فرض استفاده می‌کنه.
 */
@Composable
fun ReminderSettingsScreen(
    onBack: () -> Unit,
    viewModel: ReminderSettingsViewModel = hiltViewModel(),
    notificationsViewModel: NotificationsViewModel = hiltViewModel(),
) {
    val enabled by notificationsViewModel.enabled.collectAsState()
    val dayOffsets by viewModel.dayOffsets.collectAsState()
    val soundUri by viewModel.soundUri.collectAsState()
    val vibrate by viewModel.vibrate.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) notificationsViewModel.enable() }

    val soundPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        @Suppress("DEPRECATION")
        val uri = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI) as? Uri
        viewModel.setSoundUri(uri?.toString())
    }

    val soundName = remember(soundUri) {
        val uriString = soundUri
        if (uriString == null) {
            "پیش‌فرض سیستم"
        } else {
            runCatching { RingtoneManager.getRingtone(context, Uri.parse(uriString))?.getTitle(context) }
                .getOrNull() ?: "صدای انتخابی"
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 40.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                }
                Text("تنظیمات یادآوری", color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
            }
        }

        item {
            AppCard(label = "یادآوری سررسید") {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "برای اقساط و چک‌های نزدیک به سررسید یه نوتیف بده",
                        color = AppMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = enabled,
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

        item {
            AppCard(label = "زمان‌بندیِ پیش‌فرض") {
                Column {
                    Text(
                        "چند روز قبل از سررسید یادآوری بگیری؟ می‌تونی چندتا رو هم‌زمان انتخاب کنی - "
                            + "هر وام یا چک هم می‌تونه از تنظیمِ اختصاصیِ خودش (تو صفحه‌ی جزئیاتش) این پیش‌فرض رو رد کنه.",
                        color = AppMuted,
                        fontSize = 12.sp,
                    )
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        REMINDER_OFFSET_OPTIONS.forEach { offset ->
                            AppChip(
                                label = reminderOffsetLabel(offset),
                                selected = offset in dayOffsets,
                                onClick = { viewModel.toggleOffset(offset) },
                            )
                        }
                    }
                }
            }
        }

        item {
            AppCard(label = "صدای اعلان") {
                Column {
                    Text(soundName, color = AppText, fontSize = 13.sp)
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                putExtra(
                                    RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                    soundUri?.let { Uri.parse(it) }
                                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                                )
                            }
                            soundPickerLauncher.launch(intent)
                        },
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Text("تغییر صدا")
                    }
                }
            }
        }

        item {
            AppCard(label = "ویبره") {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "موقعِ نوتیفِ یادآوری ویبره هم بره",
                        color = AppMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = vibrate,
                        onCheckedChange = viewModel::setVibrate,
                        colors = SwitchDefaults.colors(checkedThumbColor = AppPrimary, checkedTrackColor = AppPrimary.copy(alpha = 0.5f)),
                    )
                }
            }
        }
    }
}
