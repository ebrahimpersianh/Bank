package ir.sadteam.loancalc.ui.cheque

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.settings.NotificationsViewModel
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * «تنظیمات یادآوری چک» - همون سوییچِ سراسریِ نوتیفِ سررسید که تو تنظیمات اپه (اقساط وام + چک‌ها با
 * یه کلید مشترک، `DueDateReminderWorker` هر روز هردوتا رو چک می‌کنه)، فقط با ورودی/توضیح مخصوصِ
 * چک - چون منوی «امور چک» یه آیتم جدا برای این می‌خواست، نه اینکه کاربر رو مجبور کنیم بره تنظیماتِ
 * کلیِ اپ. تغییر این سوییچ دقیقاً همون [NotificationsViewModel] رو صدا می‌زنه، دو تا کپیِ جدا از
 * تنظیمات نیست.
 */
@Composable
fun ChequeReminderSettingsScreen(onBack: () -> Unit, viewModel: NotificationsViewModel = hiltViewModel()) {
    val enabled by viewModel.enabled.collectAsState()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) viewModel.enable() }

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
                Text("تنظیمات یادآوری چک", color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
            }
        }
        item {
            AppCard(label = "یادآوری سررسید چک") {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "هر روز، برای چک‌های وضع‌نشده‌ای که سررسیدشون امروز/فرداست یه نوتیف بده",
                        color = AppMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = enabled,
                        onCheckedChange = { checked ->
                            if (!checked) {
                                viewModel.disable()
                            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                viewModel.enable()
                            } else if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS,
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                viewModel.enable()
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
            Column {
                Text(
                    "این سوییچ با «یادآوری سررسید» تو تنظیماتِ کلیِ اپ یکیه - روشن/خاموش کردنش هرجا، "
                        + "اون‌یکی رو هم عوض می‌کنه. یادآوریِ اقساطِ وام رو هم همین کنترل می‌کنه.",
                    color = AppMuted,
                    fontSize = 11.sp,
                )
            }
        }
    }
}
