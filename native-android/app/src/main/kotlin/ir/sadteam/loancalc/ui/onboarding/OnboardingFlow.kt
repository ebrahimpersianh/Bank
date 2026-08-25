package ir.sadteam.loancalc.ui.onboarding

import android.Manifest
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
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.data.DEFAULT_ACCOUNT_ICON_KEY
import ir.sadteam.loancalc.data.accountIconChoices
import ir.sadteam.loancalc.data.accountIconForKey
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.GradientButton
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
 * چهار مرحله‌ی داخلِ خودِ این فایل، و مرحله‌ی پنجم (**ورود با شماره‌موبایل**) همون گیتِ موجودِ
 * `LoginScreen` تو `AppRoot`ه که بلافاصله بعدِ [onFinished] میاد - عمداً اینجا تکرار نشده.
 *
 * 1. خوش‌آمد
 * 2. انتخابِ تم (روشن/تاریک)
 * 3. یادآورِ روزانه + مجوزِ اعلان
 * 4. ساختِ اولین «حساب‌کتاب» (پیش‌فرض: **نقدی**)
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    onNext()
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
 * **مرحله‌ی مجوزهای خواندنِ خودکار** - کارتِ `35a` فایلِ طراحی.
 *
 * دو اجازه‌ی کاملاً متفاوت، تو **یک** صفحه:
 * - **پیامکِ بانک** - مجوزِ Runtimeِ عادی (`RECEIVE_SMS`)، پس همون‌جا دیالوگ می‌ده.
 * - **اعلانِ گوشی** - مجوزِ `NotificationListener`ه که دیالوگِ Runtime **نداره**؛ تنها راهش
 *   صفحه‌ی خودِ اندرویده. برای همین متنِ دکمه صریحاً می‌گه کاربر از برنامه بیرون می‌ره
 *   (قاعده‌ی `35b`).
 *
 * ⚠️ هیچ‌کدوم اجباری نیستن - «بعداً» هم‌عرضِ دکمه‌ی اصلیه، دقیقاً مثلِ طرح.
 */
@Composable
private fun BankReadingPermissionsStep(onNext: () -> Unit) {
    val context = LocalContext.current
    val smsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { /* نتیجه مسیر رو قفل نمی‌کنه - کاربر بعداً هم می‌تونه از تنظیمات روشنش کنه. */ }

    Column(modifier = Modifier.fillMaxSize()) {
        StepHeader(
            title = "دو اجازه لازم دارم",
            subtitle = "تا خرج‌ها را خودم ثبت کنم و تو مجبور نباشی دستی وارد کنی.",
        )
        Spacer(Modifier.height(24.dp))
        PermissionExplainCard(
            icon = Icons.Default.Sms,
            title = "پیامکِ بانک",
            description = "مبلغ و نامِ فروشنده از پیامکِ خرید خوانده می‌شود",
        )
        Spacer(Modifier.height(10.dp))
        PermissionExplainCard(
            icon = Icons.Default.Notifications,
            title = "اعلانِ گوشی",
            description = "بانک‌هایی مثلِ بلوبانک پیامک نمی‌دهند، فقط اعلان",
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "فقط پیامک و اعلانِ بانک‌های پشتیبانی‌شده خوانده می‌شود. باقیِ پیام‌ها نه خوانده و نه ذخیره می‌شوند.",
            color = AppMuted,
            fontSize = 11.sp,
            lineHeight = 19.sp,
        )
        Spacer(Modifier.weight(1f))
        GradientButton(
            onClick = {
                smsLauncher.launch(
                    arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS),
                )
                runCatching {
                    context.startActivity(
                        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
                onNext()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("اجازه می‌دهم")
        }
        TextButton(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text("بعداً", color = AppMuted, fontSize = 13.sp)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PermissionExplainCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AppPrimaryPill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = AppPrimaryInk)
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(title, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    description,
                    color = AppMuted,
                    fontSize = 12.sp,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

/**
 * مرحله‌ی پنجم: اولین حساب‌کتاب. عمداً فقط «منبعِ غیربانکی» (نقدی/کیفِ پول…) ساخته می‌شه، نه
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
