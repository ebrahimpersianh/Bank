package ir.sadteam.loancalc.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.R
import ir.sadteam.loancalc.core.REMINDER_OFFSET_OPTIONS
import ir.sadteam.loancalc.core.reminderOffsetLabel
import ir.sadteam.loancalc.notifications.ReminderChannels
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
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) notificationsViewModel.enable() }

    // «تستِ نوتیفیکیشن»: خواسته‌ی کاربر که بدونِ صبرکردن برای اجرای روزانه‌ی DueDateReminderWorker
    // (که تا ۲۴ ساعت طول می‌کشه) بتونه همون لحظه صدا/ویبره/مجوز رو تایید کنه. دقیقاً از همون
    // ReminderChannels.ensure استفاده می‌کنه که خودِ Worker استفاده می‌کنه، پس نتیجه‌ش واقعاً
    // نشون‌دهنده‌ی نوتیفِ واقعیه، نه یه نمونه‌ی جداگانه با تنظیماتِ متفاوت.
    val testPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) sendTestReminderNotification(context) }

    fun fireTestNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            testPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            sendTestReminderNotification(context)
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
            AppCard(label = "تستِ نوتیفیکیشن") {
                Column {
                    Text(
                        "بدونِ نیاز به صبرکردن (چک‌کردنِ روزانه تا ۲۴ ساعت طول می‌کشه)، همین الان یه " +
                            "نوتیفِ نمونه با همین صدا/ویبره‌ی تنظیم‌شده بفرست تا مطمئن بشی درست کار می‌کنه.",
                        color = AppMuted,
                        fontSize = 12.sp,
                    )
                    OutlinedButton(
                        onClick = { fireTestNotification() },
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Text("ارسالِ نوتیفِ آزمایشی")
                    }
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
            // 🚨 انتخابگرِ صدا و کلیدِ ویبره از این‌جا **برداشته شدند**، نه چون اضافه بودند:
            // با سه کانالِ ثابت (رجوع کن به ReminderChannels)، اندروید صدا/ویبره را فقط
            // لحظه‌ی ساختِ اولِ کانال می‌خواند و تغییرِ بعدی هیچ اثری ندارد. کنترلی که
            // می‌چرخد و کاری نمی‌کند از نبودنش بدتر است - حالا همان تنظیم، در جای واقعی‌اش
            // یعنی تنظیماتِ خودِ اندروید، باز می‌شود.
            AppCard(label = "صدا و ویبره") {
                Column {
                    Text(
                        "صدا و ویبره‌ی یادآورها را اندروید نگه می‌دارد، نه برنامه - از آن‌جا " +
                            "می‌توانی برای هر نوع اعلان جدا تنظیمش کنی.",
                        color = AppMuted,
                        fontSize = 12.sp,
                        lineHeight = 20.sp,
                    )
                    OutlinedButton(
                        onClick = { ReminderChannels.openChannelSettings(context) },
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Text("تنظیماتِ صدا و ویبره")
                    }
                }
            }
        }
    }
}

/** دقیقاً هم‌الگو با notifyLoan/notifyCheque تو DueDateReminderWorker.kt - همون کانال، همون سبک؛
 * فقط عنوان/متن مشخص می‌کنه که این یه تستِ دستیه، نه یه یادآوریِ واقعی. */
private fun sendTestReminderNotification(context: android.content.Context) {
    // کانالِ ثابتِ سررسید؛ صدا/ویبره دیگر از این‌جا نمی‌آید (رجوع کن به کارتِ «صدا و ویبره»).
    ReminderChannels.ensureAll(context)
    val channelId = ReminderChannels.CHANNEL_DUE_DATES
    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_notification)
        .setLargeIcon(ReminderChannels.largeIcon(context))
        .setContentTitle("یادآوریِ آزمایشی")
        .setContentText("این یه نوتیفِ نمونه‌ست - دقیقاً با همین صدا/ویبره، یادآوریِ واقعیِ سررسید هم میاد")
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()
    NotificationManagerCompat.from(context).notify(TEST_NOTIFICATION_ID, notification)
}

private const val TEST_NOTIFICATION_ID = 999999
