package ir.sadteam.roozegar.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.sadteam.roozegar.BuildConfig
import ir.sadteam.roozegar.notif.DayNotification
import ir.sadteam.roozegar.prefs.Prefs
import ir.sadteam.roozegar.prefs.Settings
import ir.sadteam.roozegar.ui.glass.AuroraGlassBackground
import ir.sadteam.roozegar.ui.glass.GlassCard
import ir.sadteam.roozegar.ui.theme.Teal
import ir.sadteam.roozegar.ui.theme.TextMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** تنظیمات - تمرکز فاز ۱ رو شخصی‌سازی اعلان دائمیه (خواسته‌ی صریح پرامپت). */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val settings by Prefs.flow(context).collectAsState(initial = Settings())
    val scope = rememberCoroutineScope()

    /** اجرای تغییر + تازه‌کردن اعلان با تنظیمات جدید. */
    fun apply(change: suspend () -> Unit) {
        scope.launch(Dispatchers.Default) {
            change()
            DayNotification.refresh(context)
        }
    }

    AuroraGlassBackground {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت", tint = TextMuted)
                }
                Text("تنظیمات", style = MaterialTheme.typography.titleLarge)
            }

            GlassCard(Modifier.fillMaxWidth()) {
                Text("اعلان دائمی تاریخ", style = MaterialTheme.typography.titleMedium, color = Teal)
                Spacer(Modifier.height(4.dp))
                ToggleRow("نمایش اعلان و عدد روز بالای گوشی", settings.notifEnabled) {
                    apply { Prefs.setNotifEnabled(context, it) }
                }
                ToggleRow(
                    "حالت مینیمال (فقط تاریخ شمسی)",
                    settings.notifMinimal,
                    enabled = settings.notifEnabled,
                ) { apply { Prefs.setNotifMinimal(context, it) } }
                val subEnabled = settings.notifEnabled && !settings.notifMinimal
                ToggleRow("نمایش تاریخ میلادی", settings.notifShowGregorian, enabled = subEnabled) {
                    apply { Prefs.setNotifShowGregorian(context, it) }
                }
                ToggleRow("نمایش تاریخ قمری", settings.notifShowHijri, enabled = subEnabled) {
                    apply { Prefs.setNotifShowHijri(context, it) }
                }
                ToggleRow("نمایش مناسبت روز", settings.notifShowOccasion, enabled = subEnabled) {
                    apply { Prefs.setNotifShowOccasion(context, it) }
                }
            }

            Spacer(Modifier.height(12.dp))

            GlassCard(Modifier.fillMaxWidth()) {
                Text("جلوه‌های بصری", style = MaterialTheme.typography.titleMedium, color = Teal)
                Spacer(Modifier.height(4.dp))
                ToggleRow("افکت روزهای خاص (شکوفه‌ی نوروز، برف، یلدا و...)", settings.effectsEnabled) {
                    apply { Prefs.setEffectsEnabled(context, it) }
                }
            }

            Spacer(Modifier.height(12.dp))

            GlassCard(Modifier.fillMaxWidth()) {
                Text("درباره", style = MaterialTheme.typography.titleMedium, color = Teal)
                Spacer(Modifier.height(4.dp))
                Text(
                    "تقویم من - تقویم فارسی\nنسخه‌ی ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else TextMuted,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(checkedTrackColor = Teal),
        )
    }
}
