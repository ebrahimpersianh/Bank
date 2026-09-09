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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.R
import ir.sadteam.loancalc.core.REMINDER_OFFSET_OPTIONS
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.core.reminderOffsetLabel
import ir.sadteam.loancalc.notifications.ReminderChannels
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.onboarding.notificationListenerEnabled
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppLine
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
    val reminderHour by viewModel.reminderHour.collectAsState()
    val autoTxEnabled by viewModel.autoTxEnabled.collectAsState()
    val comeBackEnabled by viewModel.comeBackEnabled.collectAsState()
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
            // فریمِ `50a`: ساعت **یکی** است برای هر سه کانال، نه سه انتخابگر - تفاوتی که
            // کاربر نمی‌خواهد، در برابرِ صفحه‌ای که سه برابر می‌شود.
            AppCard(label = "ساعتِ یادآوری") {
                Column {
                    Text(
                        "همه‌ی یادآورها این ساعت می‌آیند.",
                        color = AppMuted,
                        fontSize = 12.sp,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // بازه‌ی ۶ تا ۲۳ عمدی است: کانالِ سررسید `IMPORTANCE_HIGH` است و ویبره
                        // می‌زند؛ کسی یادآورِ پول را سه بامداد نمی‌خواهد و آن را خرابی می‌بیند.
                        IconButton(onClick = { viewModel.setReminderHour(if (reminderHour <= 6) 23 else reminderHour - 1) }) {
                            Icon(Icons.Filled.Remove, contentDescription = "یک ساعت زودتر", tint = AppPrimary)
                        }
                        // بی برچسب، «۳:۰۰» یعنی سه بامداد یا سه بعدازظهر - معلوم نیست.
                        Text(
                            "${toFa(if (reminderHour % 12 == 0) 12 else reminderHour % 12)}:۰۰ ${dayPartLabel(reminderHour)}",
                            color = AppText,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { viewModel.setReminderHour(if (reminderHour >= 23) 6 else reminderHour + 1) }) {
                            Icon(Icons.Filled.Add, contentDescription = "یک ساعت دیرتر", tint = AppPrimary)
                        }
                    }
                }
            }
        }

        item {
            // فریمِ `50a`: سه کلید **یک تصمیم**‌اند («چه چیزی خبر بدهد») پس در یک کارت با
            // جداکننده می‌نشینند؛ ساعت تصمیمِ دیگری است («کِی») و کارتِ خودش را دارد. قبلاً
            // ساعت وسطِ کلیدها افتاده بود و کاربر باید تصمیمِ اول را رها می‌کرد و برمی‌گشت.
            AppCard(label = "چه چیزی خبر بدهد") {
                Column {
                    ReminderToggleRow(
                        title = "یادآوری سررسید",
                        subtitle = "برای اقساط و چک‌های نزدیک به سررسید یه نوتیف بده",
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
                    )
                    HorizontalDivider(color = AppLine, modifier = Modifier.padding(vertical = 4.dp))
                    ReminderToggleRow(
                        title = "تراکنشِ خودکار",
                        subtitle = "وقتی تراکنشی از پیامک یا اعلانِ بانک ثبت شد خبر بده",
                        checked = autoTxEnabled,
                        onCheckedChange = { viewModel.setAutoTxEnabled(it) },
                    )
                    HorizontalDivider(color = AppLine, modifier = Modifier.padding(vertical = 4.dp))
                    // ⚠️ استثنای عمدی: این یکی شب اجرا می‌شود نه سرِ ساعتِ کارتِ بالا، چون
                    // شرطش «تا حالا چیزی ثبت نشده» است و صبح همیشه درست است.
                    ReminderToggleRow(
                        title = "یادآورِ روزانه",
                        subtitle = "اگر تا شب چیزی ثبت نکردی یادم بینداز",
                        checked = comeBackEnabled,
                        onCheckedChange = { viewModel.setComeBackEnabled(it) },
                    )
                }
            }
        }

        item {
            // فریمِ `50a`: کلیدِ «تراکنشِ خودکار» بی این مجوز بی‌اثر است - کاربر روشنش می‌کند و
            // هیچ اعلانی نمی‌گیرد، چون بانکِ دیجیتالش پیامک نمی‌دهد و شنونده‌ی اعلان قطع است.
            // پس وضعیتِ مجوز باید کنارِ همان کلید دیده شود، نه در صفحه‌ای دیگر.
            // ⚠️ خواندنِ دوباره در `ON_RESUME` اجباری است: کاربر از تنظیماتِ اندروید برمی‌گردد و
            // اندروید هیچ نتیجه‌ای برنمی‌گرداند.
            val lifecycleOwner = LocalLifecycleOwner.current
            var listenerOn by remember { mutableStateOf(notificationListenerEnabled(context)) }
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        listenerOn = notificationListenerEnabled(context)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            // فقط حالتِ **قطع** ردیف می‌گیرد: کسی که مجوز را داده کارِ دیگری با این ردیف ندارد.
            if (!listenerOn) {
                AppCard(label = "خواندنِ اعلانِ بانک") {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("از تنظیماتِ گوشی قطع شده", color = AppDanger, fontSize = 12.sp)
                            Text(
                                "بانک‌هایی که پیامک نمی‌دهند (مثلِ بلوبانک) بی این اجازه خوانده نمی‌شوند.",
                                color = AppMuted,
                                fontSize = 11.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                context.startActivity(
                                    Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                                        .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK },
                                )
                            },
                            modifier = Modifier.padding(start = 8.dp),
                        ) {
                            Text("وصل کن")
                        }
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

        item {
            AppCard(label = "تستِ نوتیفیکیشن") {
                Column {
                    Text(
                        "بدونِ نیاز به صبرکردن (چک‌کردنِ روزانه تا ۲۴ ساعت طول می‌کشه)، همین الان یه " +
                            "نوتیفِ نمونه بفرست تا مطمئن بشی درست کار می‌کنه. صدا و ویبره‌اش همان چیزی " +
                            "است که در تنظیماتِ اعلانِ گوشی برای این کانال انتخاب شده.",
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

    }
}

/** برچسبِ بخشِ روز کنارِ ساعت - فریمِ `50a` «۹:۰۰ صبح» را نشان می‌دهد، نه «۹:۰۰»ِ مبهم. */
private fun dayPartLabel(hour: Int): String = when (hour) {
    in 0..11 -> "صبح"
    in 12..17 -> "بعدازظهر"
    else -> "شب"
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

/** سه ردیفِ کلید کدِ یکسان با متنِ متفاوت داشتند - فریمِ `50a` هر سه را در یک کارت می‌گذارد. */
@Composable
private fun ReminderToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = AppText, fontSize = 13.sp)
            Text(
                subtitle,
                color = AppMuted,
                fontSize = 12.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppPrimary,
                checkedTrackColor = AppPrimary.copy(alpha = 0.5f),
            ),
        )
    }
}
