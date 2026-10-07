package ir.sadteam.loancalc.ui.settings

import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.material.icons.filled.Settings
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.produceState
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
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import ir.sadteam.loancalc.core.BankAppMatcher
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppCardVariant
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * سوییچِ دومِ همین بخش: خوندنِ خودکارِ **اعلانِ** بانکی - برای بانک‌های دیجیتال (بلوبانک و…) که
 * اصلاً پیامک نمی‌فرستن. خواسته‌ی صریحِ کاربر، با این شرط که «اجباری نباشه و توضیح بدی کجا بره».
 *
 * مجوزِ خواندنِ اعلان‌ها دیالوگِ Runtime نداره؛ تنها راهش بازکردنِ صفحه‌ی مخصوصِ خودِ اندروید با
 * [Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS]ه - دکمه‌ی زیرِ سوییچ همون رو باز می‌کنه.
 */
@Composable
internal fun NotificationImportSettings(viewModel: SmsAutoImportViewModel) {
    val context = LocalContext.current
    val notifEnabled by viewModel.notifEnabled.collectAsState()

    // ⚠️ **قاعده‌ی صریحِ کارتِ `35f`: «سوئیچ دروغ نمی‌گوید».** وضعیتِ واقعیِ مجوز از خودِ اندروید
    // (`NotificationManagerCompat.getEnabledListenerPackages`) تو هر `onResume` دوباره خونده
    // می‌شه. قبلاً فقط پرچمِ خواستِ کاربر (DataStore) نشون داده می‌شد، پس اگه کاربر مجوز رو از
    // تنظیماتِ گوشی برمی‌داشت، سوییچ همچنان «روشن» می‌موند و قابلیت بی‌صدا مرده بود.
    var listenerGranted by remember { mutableStateOf(notificationListenerGranted(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                listenerGranted = notificationListenerGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun openListenerSettings() {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    // حالتِ سومِ کارتِ `35f`: کاربر روشنش کرده ولی اندروید مجوز رو نداره/پس گرفته.
    val revoked = notifEnabled && !listenerGranted

    // ۱۴ مهر (خواسته‌ی کاربر: «ادغامش کن»): انتخابِ اپ حالا زیرِ خودِ هر بانک است؛ این‌جا فقط کلید و اجازه.
    SettingsGroupLabel("اعلانِ اپ‌های بانکی")
    SettingsSwitchRow(
        icon = Icons.Filled.NotificationsActive,
        title = "خوندنِ خودکارِ اعلانِ بانکی",
        subtitle = when {
            revoked -> "اجازه در تنظیماتِ گوشی برداشته شده"
            notifEnabled -> "برای بانک‌هایی که پیامک نمی‌دن و فقط اعلان می‌فرستن"
            else -> "خرج‌ها را دستی وارد می‌کنی"
        },
        checked = notifEnabled && listenerGranted,
        onCheckedChange = { checked ->
            viewModel.setNotifEnabled(checked)
            // روشن‌کردنِ سوییچ بدونِ مجوزِ اندروید بی‌فایده‌ست - همون لحظه می‌بریمش سرِ صفحه‌ی
            // درست (قاعده‌ی `35b`: «متنِ دکمه صریح می‌گوید کاربر از برنامه بیرون می‌رود»).
            if (checked && !listenerGranted) openListenerSettings()
        },
    )
    if (revoked) {
        AppCard(variant = AppCardVariant.URGENT, modifier = Modifier.padding(top = 8.dp)) {
            Text(
                "اجازه‌ی خواندنِ اعلان از تنظیماتِ گوشی برداشته شده، پس هیچ تراکنشی خودکار ثبت نمی‌شه.",
                color = AppText,
                fontSize = 12.sp,
                lineHeight = 20.sp,
            )
            GradientButton(
                onClick = { openListenerSettings() },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            ) { Text("درستش کن") }
        }
    }
    if (!listenerGranted) {
        AppCard(modifier = Modifier.padding(top = 8.dp)) {
            NotificationPermissionSteps()
            GradientButton(
                onClick = { openListenerSettings() },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) {
                Text("بازکردنِ تنظیمات")
            }
        }
    }
    // 🔎 آخرین اعلان‌های بانکی و دلیلِ ثبت‌شدن/نشدن - برای وقتی «نخواند» (۱۴ مهر).
    var showNotifLog by remember { mutableStateOf(false) }
    Text(
        if (showNotifLog) "بستنِ آخرین اعلان‌ها" else "آخرین اعلان‌های بانکی که دیده شد ›",
        color = AppPrimary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.fillMaxWidth().clickable { showNotifLog = !showNotifLog }.padding(horizontal = 4.dp, vertical = 12.dp),
    )
    if (showNotifLog) {
        val entries = remember { ir.sadteam.loancalc.notifications.NotifDebugLog.read(context) }
        AppCard {
            Column {
                val live = remember { ir.sadteam.loancalc.notifications.BankNotificationListener.connected }
                Text(
                    if (live) "سرویسِ خواندنِ اعلان همین حالا وصل است ✓" else "⚠️ سرویسِ خواندنِ اعلان هنوز وصل نشده - مجوزِ «دسترسی به اعلان» را یک بار خاموش و روشن کن.",
                    color = if (live) AppPrimary else AppText, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                )
                if (entries.isEmpty()) Text("هنوز اعلانی از اپ‌های بانکیِ وصل‌شده نیامده.", color = AppMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                entries.forEach { e ->
                    Text(
                        "${e.pkg} · ${ir.sadteam.loancalc.notifications.NotifDebugLog.label(e.result)}",
                        color = if (e.result == "ok") AppPrimary else AppText, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Text(e.text, color = AppMuted, fontSize = 11.sp, lineHeight = 17.sp)
                }
            }
        }
    }
    Text(
        "اپِ هر بانک را بالا، زیرِ همان بانک وصل کن. فقط اعلانِ همان اپ‌ها روی گوشی خوانده می‌شود.",
        color = AppMuted,
        fontSize = 11.sp,
        lineHeight = 18.sp,
        modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp),
    )
}
/**
 * انتخابِ اپ‌هایی که اعلانشون خونده می‌شه.
 *
 * 🚨 **این بخش قبلاً وجود نداشت و همین باگ بود**: `BankNotificationListener` فقط اعلانِ
 * بسته‌نام‌های داخلِ [UiPrefs.notifAutoImportPackages] رو می‌خونه، ولی هیچ‌جای اپ اون لیست رو
 * **نمی‌نوشت**. پس لیست همیشه خالی بود و هر اعلانی - از جمله بلوبانک - بی‌صدا دور انداخته
 * می‌شد، حتی وقتی کاربر هم سوییچ رو روشن کرده بود هم مجوزِ اندروید رو داده بود.
 *
 * فهرست از خودِ گوشی خونده می‌شه (اپ‌های دارای آیکونِ لانچر) و **هیچ‌جا فرستاده نمی‌شه**.
 */
/** آیکونِ واقعیِ اپِ نصب‌شده روی گوشی (خواسته‌ی کاربر ۸ مهر: فقط در همین فهرست لوگو باشد). */
@Composable
private fun InstalledAppIcon(pkg: String) {
    val context = LocalContext.current
    val icon by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, pkg) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.packageManager.getApplicationIcon(pkg).toBitmap(96, 96).asImageBitmap()
            }.getOrNull()
        }
    }
    Box(modifier = Modifier.padding(end = 10.dp).size(30.dp).clip(RoundedCornerShape(8.dp))) {
        icon?.let { androidx.compose.foundation.Image(it, contentDescription = null, modifier = Modifier.fillMaxSize()) }
    }
}
@Composable
private fun NotificationAppPicker(viewModel: SmsAutoImportViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val selected by viewModel.notifPackages.collectAsState()
    var query by remember { mutableStateOf("") }

    // خوندنِ لیستِ اپ‌ها یه‌بار انجام می‌شه (رو گوشیِ پرِ اپ چند صد میلی‌ثانیه طول می‌کشه، پس
    // نباید هر بار recomposition تکرار بشه).
    val apps by produceState(initialValue = emptyList<Pair<String, String>>(), context) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val pm = context.packageManager
                pm.getInstalledApplications(0)
                    .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
                    .filter { it.packageName != context.packageName }
                    .map { it.packageName to pm.getApplicationLabel(it).toString() }
                    .sortedBy { it.second.lowercase() }
            }.getOrDefault(emptyList())
        }
    }

    // 🚨 **گزارشِ صریحِ کاربر**: «اپ‌هایی که باید اعلانشون خونده بشه خیلی کم‌ان» - ولی این
    // لیست همه‌ی اپ‌های گوشی رو الفبایی می‌ریخت (دوربین، قطب‌نما، رادیو، سیم‌کارت…) و اپِ
    // بانکیِ واقعی لای ده‌ها اپِ بی‌ربط گم می‌شد. حالا اپ‌های بانکی/پرداختی جدا و **اول**
    // می‌آن؛ بقیه پشتِ یه دکمه‌ی «نمایشِ همه‌ی اپ‌ها» می‌مونن - حذف نمی‌شن، فقط جلوی چشم نیستن.
    var showAllApps by remember { mutableStateOf(false) }
    val (bankApps, otherApps) = remember(apps) { BankAppMatcher.split(apps) }

    val shown = remember(apps, bankApps, otherApps, query, selected, showAllApps) {
        val q = query.trim()
        // موقعِ جستجو کلِ اپ‌ها گشته می‌شن (کاربر داره دنبالِ یه اسمِ مشخص می‌گرده).
        val pool = when {
            q.isNotEmpty() -> apps
            showAllApps -> bankApps + otherApps
            // ⚠️ اپِ انتخاب‌شده همیشه دیده می‌شه، حتی اگه تشخیص داده نشده باشه - وگرنه
            // کاربر سوییچی رو که خودش روشن کرده گم می‌کنه.
            else -> bankApps + otherApps.filter { it.first in selected }
        }
        val filtered = if (q.isEmpty()) pool else pool.filter { it.second.contains(q, ignoreCase = true) }
        // انتخاب‌شده‌ها همیشه بالا می‌مونن تا کاربر ببینه چی روشنه، حتی وقتی داره جستجو می‌کنه.
        filtered.sortedByDescending { it.first in selected }
    }

    AppCard(modifier = modifier) {
        Text(
            "اعلانِ کدوم اپ‌ها خونده بشه؟",
            color = AppText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
        )
        Text(
            if (selected.isEmpty()) {
                "هیچ اپی انتخاب نشده - تا وقتی حداقل یکی رو انتخاب نکنی، هیچ تراکنشی خودکار ثبت نمی‌شه."
            } else {
                "${toFa(selected.size)} اپ انتخاب شده."
            },
            color = if (selected.isEmpty()) AppDanger else AppMuted,
            fontSize = 11.5.sp,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp),
        )
        ir.sadteam.loancalc.ui.components.PillSearchField(value = query, onValueChange = { query = it }, placeholder = "جستجوی اسمِ اپ")
        // ارتفاعِ کرانه‌دار: این کارت خودش داخلِ یه صفحه‌ی اسکرول‌شونده‌ست، پس لیست نباید
        // بی‌نهایت رشد کنه.
        Column(modifier = Modifier.padding(top = 8.dp)) {
            shown.take(40).forEach { (pkg, label) ->
                val isOn = pkg in selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScaleClickable { viewModel.setNotifPackageSelected(pkg, !isOn) }
                        .padding(vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InstalledAppIcon(pkg)
                    Text(
                        label,
                        color = AppText,
                        fontSize = 12.5.sp,
                        fontWeight = if (isOn) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = isOn,
                        onCheckedChange = { viewModel.setNotifPackageSelected(pkg, it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = AppPrimary),
                    )
                }
            }
            if (shown.isEmpty()) {
                Text(
                    when {
                        apps.isEmpty() -> "در حالِ خواندنِ فهرستِ اپ‌ها…"
                        query.isNotBlank() -> "اپی با این اسم پیدا نشد."
                        // هیچ اپِ بانکی‌ای شناخته نشد - نباید بن‌بست بشه.
                        else -> "اپِ بانکی‌ای شناخته نشد. «نمایشِ همه‌ی اپ‌ها» رو بزن یا اسمش رو جستجو کن."
                    },
                    color = AppMuted,
                    fontSize = 11.5.sp,
                    modifier = Modifier.padding(vertical = 10.dp),
                )
            } else if (shown.size > 40) {
                Text(
                    "فقط ۴۰ اپِ اول نشون داده شده - برای بقیه از جستجو استفاده کن.",
                    color = AppMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            // درِ خروجیِ لیستِ کوتاه: اگه بانکِ کاربر تو تشخیص نیومده، از اینجا پیداش می‌کنه.
            if (query.isBlank() && !showAllApps && otherApps.isNotEmpty()) {
                OutlinedButton(
                    onClick = { showAllApps = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Text("نمایشِ همه‌ی اپ‌ها (${toFa(otherApps.size)} تای دیگه)", fontSize = 12.sp)
                }
            }
        }
    }
}
/** آیا اندروید واقعاً مجوزِ خواندنِ اعلان‌ها رو به این اپ داده؟ (قاعده‌ی «سوئیچ دروغ نمی‌گوید».) */
private fun notificationListenerGranted(context: Context): Boolean =
    NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
/**
 * سه قدمِ کارتِ `35c` (نسخه‌ی نهایی، فریمِ `37e`).
 *
 * **دو چیز عمداً از طرح بیرون رفت** و برنگردونشون:
 * - **عکسِ صفحه‌ی اندروید**: عنوان و زبانِ اون صفحه بینِ سازنده‌ها فرق داره (رو شیائومیِ کاربر
 *   `Device & app notifications` بود و کلاً انگلیسی)، پس عکسِ یه گوشی برای بقیه گمراه‌کننده‌ست.
 * - **متنِ مسیر** («تنظیمات ← برنامه‌ها ← …»): `ACTION_NOTIFICATION_LISTENER_SETTINGS` کاربر رو
 *   **مستقیم رو صفحه‌ی مقصد** فرود میاره، پس اون مسیر چیزی رو توضیح می‌داد که کاربر هیچ‌وقت
 *   نمی‌بینه. (تاییدِ عملیِ کاربر رو گوشیِ واقعی.)
 *
 * ⚠️ **قدمِ ۱ و ۲ عمداً جدان.** نسخه‌ی قبلیِ همین تابع می‌گفت «کلیدِ کنارِ اسمش رو روشن کن» که
 * **غلط بود**: تو فهرست کلیدی نیست، باید رو اسم زد تا صفحه‌ی خودش باز بشه. طراح تاکید کرد
 * بیشترِ کاربرها دقیقاً همین‌جا گیر می‌کنن.
 *
 * رشته‌های انگلیسی (`NOT ALLOWED`, `ALLOWED`, `Allow notification access`, `Allow`) **ترجمه
 * نمی‌شن** و با [Ltr] می‌شینن - رابطِ تنظیمات حتی رو گوشیِ فارسی هم انگلیسیه.
 */
@Composable
private fun NotificationPermissionSteps(modifier: Modifier = Modifier) {
    // هر قدم: متنِ فارسی، و رشته‌ی انگلیسیِ لنگر (اگه داشته باشه) که عیناً دیده می‌شه.
    val steps: List<Triple<String, String?, String>> = listOf(
        Triple(
            "تو فهرست دنبالِ «جیبک» بگرد",
            "NOT ALLOWED",
            "ممکنه زیرِ این سرگروه باشه. اگه از قبل زیرِ ALLOWED بود، کار تمومه.",
        ),
        Triple(
            "روش بزن",
            null,
            "صفحه‌ی خودش باز می‌شه - کلید اونجاست، نه تو فهرست.",
        ),
        Triple(
            "این کلید رو روشن کن",
            "Allow notification access",
            "بعدش اندروید یه پنجره‌ی تایید میاره؛ Allow رو بزن.",
        ),
    )
    Column(modifier = modifier) {
        steps.forEachIndexed { index, (title, anchor, hint) ->
            Row(modifier = Modifier.padding(bottom = 10.dp)) {
                Text(
                    toFa(index + 1),
                    color = AppPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                )
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        title,
                        color = AppText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    if (anchor != null) {
                        // کارتِ سفیدِ لنگر - عیناً همون رشته‌ای که کاربر رو صفحه‌ی اندروید می‌بینه.
                        Ltr {
                            Text(
                                anchor,
                                color = AppText,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .padding(top = 5.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AppSurface)
                                    .border(1.5.dp, AppLine, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                            )
                        }
                    }
                    Text(
                        hint,
                        color = AppMuted,
                        fontSize = 11.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}
