package ir.sadteam.loancalc.ui.onboarding

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.DEFAULT_ACCOUNT_ICON_KEY
import ir.sadteam.loancalc.data.accountIconChoices
import ir.sadteam.loancalc.data.accountIconForKey
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.JibakBrandMark
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppGoldFrom
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Sms
import android.provider.Settings
import android.content.Intent

/**
 * مسیرِ اولین ورود، عیناً به سبکِ اپِ مرجعِ کاربر (پولکی) - خواسته‌ی صریحِ تسکِ #28.
 *
 * ⚠️ **شمارشِ مرحله‌ها اصلاح شد.** این کامنت (و کامنتِ `AppRoot` و `OnboardingViewModel`) می‌گفت
 * «مسیرِ ۴ مرحله‌ای» و «مرحله‌ی چهارم: اولین حساب‌کتاب»، ولی از وقتی مرحله‌ی مجوزهای بانکی
 * (`35a`) اضافه شد داخلِ همین فایل **پنج** مرحله هست و `lastStep = 4` هم همین را می‌گوید -
 * پس `StepDots` پنج نقطه می‌کشید در حالی که هر سه کامنت چهار مرحله را شرح می‌دادند.
 *
 * پنج مرحله‌ی داخلِ خودِ این فایل، و مرحله‌ی ششم (**ورود با شماره‌موبایل**) همون گیتِ موجودِ
 * `LoginScreen` تو `AppRoot`ه که بلافاصله بعدِ [onFinished] میاد - عمداً اینجا تکرار نشده و
 * نقطه‌ای هم در `StepDots` نمی‌گیرد (چون گیتِ جدایی است، نه مرحله‌ای که بشود ازش رد شد).
 *
 * 1. خوش‌آمد
 * 2. انتخابِ تم (روشن/تاریک)
 * 3. یادآورِ روزانه + مجوزِ اعلان
 * 4. مجوزِ خواندنِ پیامک و اعلانِ بانک (کارتِ `35a`)
 * 5. ساختِ اولین «حساب‌کتاب» (پیش‌فرض: **نقدی**)
 *
 * ⚠️ **`BenefitsScreen` (صفحه‌ی امکاناتِ رایگان/اشتراکی) کاملاً حذف شد** - خواسته‌ی صریحِ کاربر
 * («دیگه نمی‌خوام اون صفحه امکانات بیان»). محتوای مشابهش حالا فقط تو صفحه‌ی اشتراک زندگی می‌کنه.
 */
@Composable
fun OnboardingFlow(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    var step by remember { mutableIntStateOf(0) }
    val lastStep = 4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(28.dp))
        StepDots(current = step, total = lastStep + 1)

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val forward = targetState > initialState
                val dir = if (forward) 1 else -1
                (slideInHorizontally(tween(280)) { it * dir } + fadeIn(tween(280)))
                    .togetherWith(slideOutHorizontally(tween(280)) { -it * dir } + fadeOut(tween(280)))
            },
            modifier = Modifier.weight(1f),
            label = "onboarding-step",
        ) { current ->
            when (current) {
                0 -> WelcomeStep(onNext = { step = 1 })
                1 -> ThemeStep(
                    onPick = { viewModel.setThemeMode(it) },
                    onNext = { step = 2 },
                )
                2 -> ReminderStep(
                    onChoose = { viewModel.setDailyReminder(it) },
                    onNext = { step = 3 },
                )
                // **کارتِ `35a`**: «یک صفحه برای هر دو مجوز، نه دو صفحه. پیامک اول چون کاربر
                // انتظارش را دارد؛ اعلان دوم با دلیلِ روشن.» و «بعداً» هم‌عرضِ دکمه‌ی اصلیه چون
                // «کارِ برنامه بدونِ این دو هم راه می‌افتد».
                3 -> BankReadingPermissionsStep(onNext = { step = 4 })
                else -> FirstAccountStep(
                    onSubmit = { name, balance, icon ->
                        viewModel.addFirstAccount(name, "", balance, icon)
                        onFinished()
                    },
                    onSkip = onFinished,
                )
            }
        }
    }
}

@Composable
private fun StepDots(current: Int, total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(total) { index ->
            val active = index <= current
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(6.dp)
                    .width(if (index == current) 22.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (active) AppPrimary else AppLine),
            )
        }
    }
}

/** سربرگِ مشترکِ هر چهار مرحله - عنوانِ درشت + یه خطِ توضیح، دقیقاً مثلِ اپِ مرجع. */
@Composable
private fun StepHeader(title: String, subtitle: String) {
    Spacer(Modifier.height(36.dp))
    Text(
        title,
        color = AppText,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        subtitle,
        color = AppMuted,
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
    )
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        // نشانِ برند: این **همان صفحه‌ی معرفیِ اولیه** است که فقط یک‌بار در عمرِ نصب دیده می‌شود،
        // و تا الان جمله‌ی «به جیبک خوش اومدی» را می‌نوشت بی این‌که جیبک را نشان بدهد.
        // `BenefitsScreen` حذف شده، پس این تنها جایی است که برند معرفی می‌شود.
        Spacer(Modifier.height(34.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            JibakBrandMark(width = 64.dp)
        }
        StepHeader(
            title = "به جیبک خوش اومدی",
            subtitle = "دخل و خرجت رو ساده ثبت کن، وام و چک و بودجه‌ت رو یک‌جا داشته باش.",
        )
        Spacer(Modifier.height(28.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            WelcomeBullet("ثبتِ سریعِ دخل و خرج")
            WelcomeBullet("بودجه‌بندی و گزارشِ ماهانه")
            WelcomeBullet("یادآوریِ قسط، چک و سررسید")
            WelcomeBullet("پشتیبان‌گیریِ امن از اطلاعاتت")
        }
        Spacer(Modifier.weight(1f))
        GradientButton(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text("بزن بریم")
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun WelcomeBullet(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(AppPrimaryPill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(14.dp))
        }
        Text(text, color = AppText, fontSize = 14.sp, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
private fun ThemeStep(onPick: (String) -> Unit, onNext: () -> Unit) {
    var picked by remember { mutableStateOf("light") }
    Column(modifier = Modifier.fillMaxSize()) {
        StepHeader(
            title = "تمِ برنامه رو انتخاب کن",
            subtitle = "هر وقت خواستی می‌تونی از تنظیمات عوضش کنی.",
        )
        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ThemeChoiceCard(
                label = "روشن",
                selected = picked == "light",
                dark = false,
                modifier = Modifier.weight(1f),
                onClick = { picked = "light"; onPick("light") },
            )
            ThemeChoiceCard(
                label = "تاریک",
                selected = picked == "dark",
                dark = true,
                modifier = Modifier.weight(1f),
                onClick = { picked = "dark"; onPick("dark") },
            )
        }
        Spacer(Modifier.weight(1f))
        GradientButton(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text("ادامه")
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ThemeChoiceCard(
    label: String,
    selected: Boolean,
    dark: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) AppPrimary else AppLine
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(AppSurface)
            .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(18.dp))
            .pressScaleClickable(onClick = onClick)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (dark) Color(0xFF13161C) else Color(0xFFF2F4F8)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (dark) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = null,
                tint = if (dark) Color(0xFFE7E9EE) else Color(0xFF2B2F36),
            )
        }
        Text(
            label,
            color = AppText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun ReminderStep(onChoose: (Boolean) -> Unit, onNext: () -> Unit) {
    val context = LocalContext.current
    // ⚠️ **باگِ ترتیبِ گیت‌ها.** `PermissionGateScreen` **قبل از** این مسیر اجرا می‌شود و
    // `POST_NOTIFICATIONS` را اجباری می‌گیرد - یعنی وقتی کاربر به این مرحله می‌رسد مجوز از قبل
    // داده شده. `launch()`ِ بی‌شرطِ قبلی روی مجوزِ داده‌شده دیالوگی نشان نمی‌دهد ولی همان مسیرِ
    // رفت‌وبرگشتِ Activity Result را طی می‌کند، پس دکمه یک لحظه بی‌جواب می‌ماند و بعد می‌پرد.
    // حالا فقط وقتی واقعاً لازم است پرسیده می‌شود.
    val notifAlreadyGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        // نتیجه‌ی مجوز عمداً مسیر رو قفل نمی‌کنه: اگه کاربر رد کنه هم می‌ره مرحله‌ی بعد و بعداً
        // می‌تونه از تنظیمات روشنش کنه. گیتِ اجباریِ مجوز جای دیگه‌ایه (PermissionGateScreen).
        onNext()
    }
    Column(modifier = Modifier.fillMaxSize()) {
        StepHeader(
            title = "یادت بندازم؟",
            subtitle = "هر روز یه یادآوریِ کوتاه می‌فرستم تا ثبتِ دخل‌وخرج از یادت نره.",
        )
        Spacer(Modifier.height(28.dp))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AppGoldFrom),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = AppAccent)
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text("یادآورِ روزانه", color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "روزی یک اعلان، بدونِ مزاحمت.",
                        color = AppMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
        Spacer(Modifier.weight(1f))
        GradientButton(
            onClick = {
                onChoose(true)
                if (notifAlreadyGranted) {
                    onNext()
                } else {
                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("آره، یادم بنداز")
        }
        TextButton(
            onClick = { onChoose(false); onNext() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("فعلاً نه", color = AppMuted, fontSize = 13.sp)
        }
        Spacer(Modifier.height(16.dp))
    }
}

/**
 * تشخیصِ زنده‌ی دو مجوز - هر دو **بی رندر** خوانده می‌شوند، چون هر بار برگشتن به این مرحله
 * باید وضعیتِ واقعیِ همان لحظه را نشان بدهد.
 */
private fun smsReadingGranted(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) ==
        PackageManager.PERMISSION_GRANTED

/**
 * `NotificationListener` روشن است؟ - مجوزِ Runtime نیست، پس `checkSelfPermission` جواب نمی‌دهد.
 * `NotificationManagerCompat` همان رشته‌ی `Settings.Secure.enabled_notification_listeners` را
 * می‌خواند ولی امضای پایدارِ androidx را دارد.
 */
internal fun notificationListenerEnabled(context: Context): Boolean =
    NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

/**
 * **مرحله‌ی مجوزهای خواندنِ خودکار** - کارتِ `35a` فایلِ طراحی، به‌همراهِ راهنمای `35c` و
 * حالتِ بازگشتِ `35d`.
 *
 * دو اجازه‌ی کاملاً متفاوت، تو **یک** صفحه:
 * - **پیامکِ بانک** - مجوزِ Runtimeِ عادی (`RECEIVE_SMS`)، پس همون‌جا دیالوگ می‌ده.
 * - **اعلانِ گوشی** - مجوزِ `NotificationListener`ه که دیالوگِ Runtime **نداره**؛ تنها راهش
 *   صفحه‌ی خودِ اندرویده، و کاربر باید **خودش** برنامه را از یک لیست پیدا و روشن کند.
 *
 * ⚠️ **باگِ اصلیِ رفع‌شده - هر سه اشکال از یک `onClick` می‌آمدند.** نسخه‌ی قبلی در یک تپ:
 * `smsLauncher.launch()` می‌زد، **بلافاصله** `startActivity(NOTIFICATION_LISTENER_SETTINGS)` را
 * روی آن سوار می‌کرد، و بعد `onNext()`. نتیجه:
 *
 * 1. دیالوگِ پیامک زیرِ صفحه‌ی تنظیماتِ اندروید دفن می‌شد - کاربر هیچ‌وقت نمی‌دیدش، و اندروید
 *    درخواستِ دیده‌نشده را «رد» حساب می‌کند. یعنی مجوزِ پیامک عملاً هیچ‌وقت گرفته نمی‌شد.
 * 2. مرحله قبل از برگشتنِ کاربر رد می‌شد، پس `35d` («فعال شد» / «برگشتی ولی روشن نشد») هیچ
 *    جایی برای دیده شدن نداشت.
 * 3. کاربر بی هیچ راهنمایی وسطِ یک لیستِ بلندِ برنامه‌ها رها می‌شد - همان چیزی که `35c` برایش
 *    نوشته شده بود.
 *
 * رفع: **زنجیره‌ای، نه هم‌زمان.** هر ردیف دکمه و وضعیتِ خودش را دارد؛ پیامک اول (چون دیالوگش
 * درجا جواب می‌دهد)، و رفتن به تنظیماتِ اعلان فقط بعدِ نمایشِ راهنمای سه‌قدمی. وضعیت با
 * `ON_RESUME` دوباره خوانده می‌شود، چون اندروید از آن صفحه نتیجه‌ای برنمی‌گرداند.
 *
 * ⚠️ هیچ‌کدوم اجباری نیستن - «بعداً» هم‌عرضِ دکمه‌ی اصلیه، دقیقاً مثلِ طرح.
 */
@Composable
private fun BankReadingPermissionsStep(onNext: () -> Unit) {
    val context = LocalContext.current
    var smsOk by remember { mutableStateOf(smsReadingGranted(context)) }
    var listenerOk by remember { mutableStateOf(notificationListenerEnabled(context)) }
    // راهنمای `35c` تا وقتی کاربر روی ردیفِ اعلان نزده نمایش داده نمی‌شود - وگرنه سه قدمِ
    // بی‌ربط بالای صفحه می‌نشست.
    var guideOpen by remember { mutableStateOf(false) }
    // `35d`: «رفتی و برگشتی ولی روشن نشد». فقط بعدِ یک رفتنِ واقعی معنی دارد.
    var returnedFromSettings by remember { mutableStateOf(false) }

    val smsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { smsOk = smsReadingGranted(context) }

    // اندروید از صفحه‌ی «دسترسی به اعلان‌ها» **نتیجه‌ای برنمی‌گرداند** (قاعده‌ی `35d`)، پس تنها
    // راهِ فهمیدنش این است که موقعِ برگشتِ کاربر به برنامه خودمان دوباره بخوانیم.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                smsOk = smsReadingGranted(context)
                listenerOk = notificationListenerEnabled(context)
                if (guideOpen) returnedFromSettings = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        StepHeader(
            title = "دو اجازه لازم دارم",
            subtitle = "تا خرج‌ها را خودم ثبت کنم و تو مجبور نباشی دستی وارد کنی.",
        )
        Spacer(Modifier.height(24.dp))
        PermissionExplainCard(
            icon = Icons.Default.Sms,
            title = "پیامکِ بانک",
            description = "مبلغ و نامِ فروشنده از پیامکِ خرید خوانده می‌شود",
            granted = smsOk,
            actionLabel = "اجازه می‌دهم",
            onClick = {
                smsLauncher.launch(
                    arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS),
                )
            },
        )
        Spacer(Modifier.height(10.dp))
        PermissionExplainCard(
            icon = Icons.Default.Notifications,
            title = "اعلانِ گوشی",
            description = "بانک‌هایی مثلِ بلوبانک پیامک نمی‌دهند، فقط اعلان",
            granted = listenerOk,
            // متنِ دکمه صریحاً می‌گوید کاربر از برنامه بیرون می‌رود (قاعده‌ی `35b`).
            actionLabel = "راهنما",
            onClick = { guideOpen = true },
        )

        // ── راهنمای سه‌قدمیِ `35c` ──
        // ⚠️ **جایِ عکس خالی است.** طرح سه عکسِ واقعیِ صفحه‌ی «دسترسی به اعلان‌ها» می‌خواهد؛ تا
        // آمدنشان همین سه خطِ متنی می‌نشیند. عکس که آمد، هر قدم یک `Image` بالای متنش می‌گیرد.
        if (guideOpen && !listenerOk) {
            Spacer(Modifier.height(14.dp))
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        "الان می‌بری‌ت به صفحه‌ی اندروید",
                        color = AppText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "اینجا برنامه نمی‌تواند خودش روشنش کند - سه قدمِ کوتاه با خودت است:",
                        color = AppMuted,
                        fontSize = 12.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                    )
                    GuideStep(1, "توی لیستِ برنامه‌ها «جیبک» را پیدا کن")
                    GuideStep(2, "کلیدِ کنارش را روشن کن")
                    GuideStep(3, "پیامِ تاییدِ اندروید را قبول کن و برگرد")
                    GradientButton(
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                    ) {
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Text("رفتن به تنظیماتِ اندروید", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }

        // ── `35d`، حالتِ دوم: رفت و برگشت ولی روشن نشد ──
        if (returnedFromSettings && !listenerOk) {
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppGoldFrom, RoundedCornerShape(14.dp))
                    .padding(14.dp),
            ) {
                Text(
                    "هنوز روشن نشده",
                    color = AppAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "اگر کلید را زدی ولی پیامِ تاییدِ اندروید را رد کردی، خاموش می‌ماند. یک‌بارِ " +
                        "دیگر امتحان کن - یا بعداً از تنظیماتِ جیبک روشنش کن.",
                    color = AppText,
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "فقط پیامک و اعلانِ بانک‌های پشتیبانی‌شده خوانده می‌شود. باقیِ پیام‌ها نه خوانده و نه ذخیره می‌شوند.",
            color = AppMuted,
            fontSize = 11.sp,
            lineHeight = 19.sp,
        )
        Spacer(Modifier.height(20.dp))
        GradientButton(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text(if (smsOk && listenerOk) "بریم" else "ادامه")
        }
        // «بعداً» فقط وقتی معنی دارد که چیزی مانده باشد - وگرنه دو دکمه‌ی هم‌کار زیرِ هم.
        if (!smsOk || !listenerOk) {
            TextButton(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text("بعداً", color = AppMuted, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

/** یک قدمِ راهنمای `35c` - شماره‌ی گردِ کوچک + متن. */
@Composable
private fun GuideStep(number: Int, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(AppPrimaryPill),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                toFa(number),
                color = AppPrimaryInk,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(text, color = AppText, fontSize = 12.5.sp, modifier = Modifier.padding(start = 10.dp))
    }
}

/**
 * ردیفِ یک مجوز - آیکون، عنوان، توضیح، و **وضعیت یا دکمه‌ی خودش**.
 *
 * قبلاً فقط توضیح‌دهنده بود و کارِ گرفتنِ مجوز به یک دکمه‌ی مشترکِ پایینِ صفحه سپرده شده بود؛
 * از آنجا که آن دکمه هر دو مجوز را هم‌زمان می‌زد (باگِ بالا)، دکمه به خودِ ردیف آمد.
 */
@Composable
private fun PermissionExplainCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    granted: Boolean,
    actionLabel: String,
    onClick: () -> Unit,
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (granted) AppPrimary else AppPrimaryPill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (granted) Icons.Default.Check else icon,
                    contentDescription = null,
                    tint = if (granted) AppBg else AppPrimaryInk,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(title, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (granted) "فعال است" else description,
                    color = if (granted) AppPrimary else AppMuted,
                    fontSize = 12.sp,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            if (!granted) {
                TextButton(onClick = onClick) {
                    Text(actionLabel, color = AppPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * مرحله‌ی پنجمِ همین فایل: اولین حساب‌کتاب. عمداً فقط «منبعِ غیربانکی» (نقدی/کیفِ پول…) ساخته می‌شه، نه
 * کارتِ بانکی - تو اپِ مرجع هم قدمِ اول همون «نقدی»ه؛ کارتِ بانکی با جزئیاتِ کاملش بعداً از
 * تبِ دارایی اضافه می‌شه.
 */
@Composable
private fun FirstAccountStep(
    onSubmit: (name: String, initialBalanceRial: Double, iconKey: String) -> Unit,
    onSkip: () -> Unit,
) {
    var name by remember { mutableStateOf("نقدی") }
    var balanceText by remember { mutableStateOf("") }
    var iconKey by remember { mutableStateOf(DEFAULT_ACCOUNT_ICON_KEY) }
    val balanceRial = balanceText.toDoubleOrNull() ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        StepHeader(
            title = "اولین حساب‌کتابت رو بساز",
            subtitle = "مثلاً پولِ نقدِ توی جیبت. موجودیِ الانش رو وارد کن.",
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("نام") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = balanceText,
            onValueChange = { balanceText = cleanNum(it) },
            label = { Text("موجودی اولیه") },
            singleLine = true,
            visualTransformation = ThousandsSeparatorTransformation(),
            suffix = { Text("ریال") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        if (balanceRial > 0) {
            Text(
                "${numberToWordsFa(balanceRial / 10)} تومان",
                color = AppMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        // هشدارِ ثبت‌شده در قرص‌های هیروِ تبِ دارایی، همان‌جا که «موجودی اولیه» پرسیده می‌شود:
        // این عدد **نقطه‌ی شروع** است، نه موجودیِ همیشگی؛ بعدش با تراکنش‌ها جابه‌جا می‌شود.
        Text(
            "این عدد فقط رقمِ شروع است - بعد از آن با دخل و خرج‌هایی که ثبت می‌کنی جابه‌جا می‌شود.",
            color = AppMuted,
            fontSize = 11.sp,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 8.dp),
        )

        Text(
            "آیکون",
            color = AppMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 18.dp, bottom = 8.dp),
        )
        // ⚠️ FlowRow تو نسخه‌ی Composeِ این پروژه experimentalه - الگوی chunked+Row طبقِ CLAUDE.md.
        accountIconChoices.map { it.first }.chunked(5).forEach { rowKeys ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowKeys.forEach { key ->
                    val selected = key == iconKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) AppPrimary.copy(alpha = 0.14f) else AppSurface)
                            .border(
                                if (selected) 2.dp else 1.dp,
                                if (selected) AppPrimary else AppLine,
                                RoundedCornerShape(14.dp),
                            )
                            .pressScaleClickable(onClick = { iconKey = key }),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = accountIconForKey(key),
                            contentDescription = null,
                            tint = if (selected) AppPrimary else AppMuted,
                        )
                    }
                }
                repeat(5 - rowKeys.size) { Spacer(Modifier.weight(1f)) }
            }
        }

        Spacer(Modifier.height(20.dp))
        GradientButton(
            onClick = { onSubmit(name.trim().ifBlank { "نقدی" }, balanceRial, iconKey) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("ثبت و ادامه")
        }
        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text("بعداً می‌سازم", color = AppMuted, fontSize = 13.sp)
        }
        Spacer(Modifier.height(24.dp))
    }
}
