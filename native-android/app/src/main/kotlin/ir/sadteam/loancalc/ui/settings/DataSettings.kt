package ir.sadteam.loancalc.ui.settings

import ir.sadteam.loancalc.ui.theme.AppWarningPill
import androidx.compose.material.icons.filled.CloudQueue
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Warning
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.auth.GateState
import ir.sadteam.loancalc.ui.components.InAppBannerState
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.core.toFa

@Composable
internal fun DataSettings(
    authViewModel: AuthViewModel,
    autoBackupViewModel: AutoBackupViewModel,
    banner: InAppBannerState,
) {
    val gateState by authViewModel.gateState.collectAsState()
    val subscribed by authViewModel.subscribed.collectAsState()
    val autoBackupEnabled by autoBackupViewModel.enabled.collectAsState()
    val lastAutoBackupAt by autoBackupViewModel.lastBackupAt.collectAsState()
    val cloudBackupFailed by autoBackupViewModel.cloudBackupFailed.collectAsState()

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

    // بازیابیِ در انتظارِ تایید («local»/«cloud») و قفلِ ضدِ دوبار زدن.
    var pendingRestore by remember { mutableStateOf<String?>(null) }
    var restoring by remember { mutableStateOf(false) }

    SettingsHero(
        Icons.Filled.CloudQueue,
        "وضعیتِ پشتیبان‌گیری",
        when {
            !autoBackupEnabled -> "پشتیبانِ خودکار خاموش است"
            cloudBackupFailed && lastBackupLabel != null -> "آخرین نسخه فقط روی گوشی ذخیره شد"
            lastBackupLabel != null -> "آخرین نسخه: $lastBackupLabel"
            else -> "هنوز نسخه‌ای ساخته نشده"
        },
        badge = if (autoBackupEnabled) "روشن" else "خاموش",
    )

    // ── کارتِ وضعیتِ پشتیبان - «یک نگاه، جواب می‌گیرد» ────────────────────────
    SettingsGroupLabel("پشتیبان‌گیری")
    SettingsGroup {
        SettingsRowItem(
            title = "پشتیبان‌گیریِ خودکارِ روزانه",
            icon = Icons.Filled.CloudUpload,
            tone = SettingsTone.GREEN,
            // ⚠️ «آخرین پشتیبان: امروز» وقتی آپلود شکست خورده، دروغِ خطرناکی است -
            // کاربر خیال می‌کند داده‌اش جای امنی هست. حالا شکستِ ابری صریح گفته می‌شود.
            status = when {
                !autoBackupEnabled -> "خاموش - هیچ نسخه‌ی پشتیبانی ساخته نمی‌شود"
                cloudBackupFailed && lastBackupLabel != null ->
                    "روی گوشی ذخیره شد ($lastBackupLabel) - ولی به فضای ابری نرفت"
                lastBackupLabel != null -> "آخرین پشتیبان: $lastBackupLabel"
                else -> "روشن - هنوز پشتیبانی ساخته نشده"
            },
            statusTone = when {
                !autoBackupEnabled -> StatusTone.NEUTRAL
                cloudBackupFailed && lastBackupLabel != null -> StatusTone.BROKEN
                lastBackupLabel != null -> StatusTone.HEALTHY
                else -> StatusTone.NEUTRAL
            },
            checked = autoBackupEnabled,
            onCheckedChange = { checked ->
                if (checked) autoBackupViewModel.enable() else autoBackupViewModel.disable()
            },
        )
    }

    // ── گروهِ بازیابی ─────────────────────────────────────────────────────────
    if (lastAutoBackupAt != null) {
        SettingsGroupLabel("بازیابی")
        SettingsGroup {
            SettingsRowItem(
                title = "بازیابی از پشتیبانِ همین گوشی",
                icon = Icons.Filled.Restore,
                tone = SettingsTone.NEUTRAL,
                status = lastBackupLabel?.let { "نسخه‌ی $it" },
                onClick = { if (!restoring) pendingRestore = "local" },
            )
            // بازیابی از سرور فقط برای کاربرِ واردشده‌ی مشترکه - پوش به سرور هم فقط برای همونه.
            if (gateState == GateState.LOGGED_IN && subscribed) {
                SettingsDivider()
                SettingsRowItem(
                    title = "بازیابی از سرورِ ابری",
                    icon = Icons.Filled.CloudDownload,
                    tone = SettingsTone.GREEN,
                    status = "برای وقتی گوشی عوض شده یا برنامه پاک شده",
                    onClick = { if (!restoring) pendingRestore = "cloud" },
                )
            }
        }
    }

    // 🚨 بازیابی داده‌های فعلیِ گوشی را جایگزین می‌کند - پس اول تاییدِ صریح.
    pendingRestore?.let { source ->
        JibakAlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text(if (source == "cloud") "بازیابی از سرورِ ابری؟" else "بازیابی از پشتیبانِ گوشی؟", fontWeight = FontWeight.Black) },
            text = {
                Column {
                    Text(
                        "اطلاعاتِ فعلیِ برنامه با " +
                            (if (source == "cloud") "آخرین نسخه‌ی روی سرور" else "نسخه‌ی ${lastBackupLabel ?: "ذخیره‌شده"}") +
                            " جایگزین می‌شه.",
                        lineHeight = 21.sp,
                    )
                    // کادرِ هشدارِ نارنجی (فریمِ `19`) - پیامدِ برگشت‌ناپذیر از متنِ عادی جدا دیده شود.
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppWarningPill)
                            .border(1.dp, AppWarningInk.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = AppWarningInk, modifier = Modifier.size(18.dp))
                        Text(
                            "هر چیزی که بعد از اون نسخه ثبت کردی از بین می‌ره و برنمی‌گرده.",
                            color = AppWarningInk,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 19.sp,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingRestore = null
                    restoring = true
                    val done: (Boolean) -> Unit = { ok ->
                        restoring = false
                        banner.show(
                            when {
                                ok && source == "cloud" -> "بازیابی از سرور ابری انجام شد"
                                ok -> "بازیابی از پشتیبان خودکار انجام شد"
                                source == "cloud" -> "پشتیبانی رو سرور ابری پیدا نشد"
                                else -> "پشتیبانی برای بازیابی پیدا نشد"
                            },
                            isSuccess = ok,
                        )
                    }
                    if (source == "cloud") autoBackupViewModel.restoreFromCloud(done) else autoBackupViewModel.restoreFromAutoBackup(done)
                }) { Text("بازیابی کن", color = AppDanger) }
            },
            dismissButton = { TextButton(onClick = { pendingRestore = null }) { Text("بی‌خیال") } },
        )
    }
}
