package ir.sadteam.loancalc.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VisibilityOff
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyModeViewModel
import ir.sadteam.loancalc.ui.security.AppLockViewModel
import ir.sadteam.loancalc.ui.security.biometricAvailable
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * پورت قفل امنیتی PIN+اثر انگشت اپ رقیب (VAMMAN) - برگردوندن تصمیم قبلی حذف بایومتریک، با درخواست
 * صریح جدید کاربر (رجوع کن به CLAUDE.md). سوییچ اثر انگشت فقط اگه گوشی سخت‌افزار/داده‌ی بایومتریک
 * ثبت‌شده داشته باشه ([biometricAvailable]) فعال می‌شه.
 */
@Composable
internal fun SecuritySettings(
    appLockViewModel: AppLockViewModel,
    privacyViewModel: PrivacyModeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val pinHash by appLockViewModel.pinHash.collectAsState()
    val biometricEnabled by appLockViewModel.biometricEnabled.collectAsState()
    val autoLockTimeoutMinutes by appLockViewModel.autoLockTimeoutMinutes.collectAsState()
    val privacyMode = LocalPrivacyMode.current
    var showPinDialog by remember { mutableStateOf(false) }
    var showPatternDialog by remember { mutableStateOf(false) }
    var isPattern by remember { mutableStateOf(ir.sadteam.loancalc.ui.security.LockType.isPattern(context)) }
    // 🔒 بازبینیِ ۹ مهر: خاموش‌کردن/عوض‌کردنِ قفل اول رمز یا الگوی فعلی را می‌خواهد - وگرنه هر کس
    // گوشیِ باز را برمی‌داشت قفل را برمی‌داشت.
    var verifyThen by remember { mutableStateOf<(() -> Unit)?>(null) }
    verifyThen?.let { action ->
        var entered by remember { mutableStateOf("") }
        var wrong by remember { mutableStateOf(false) }
        fun check(code: String) {
            if (appLockViewModel.verifyPin(code)) { verifyThen = null; action() } else { wrong = true; entered = "" }
        }
        ir.sadteam.loancalc.ui.components.JibakAlertDialog(
            onDismissRequest = { verifyThen = null },
            title = { Text(if (isPattern) "الگوی فعلی را بکش" else "رمزِ فعلی را بزن") },
            text = {
                Column {
                    if (isPattern) {
                        ir.sadteam.loancalc.ui.security.PatternPad(onComplete = { check(it) })
                    } else {
                        OutlinedTextField(
 textStyle = ir.sadteam.loancalc.ui.components.appFieldTextStyle(),
                            value = entered,
                            onValueChange = { entered = ir.sadteam.loancalc.core.cleanNum(it).take(8); wrong = false },
                            singleLine = true,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            colors = ir.sadteam.loancalc.ui.components.appFieldColors(),
                            shape = ir.sadteam.loancalc.ui.components.AppFieldShape,
                        )
                    }
                    if (wrong) Text(if (isPattern) "الگو اشتباه است" else "رمز اشتباه است", color = AppDanger, fontSize = 12.sp)
                }
            },
            confirmButton = {
                if (!isPattern) TextButton(onClick = { check(entered) }) { Text("تأیید") }
            },
            dismissButton = { TextButton(onClick = { verifyThen = null }) { Text("انصراف") } },
        )
    }
    val hasLock = pinHash != null || biometricEnabled

    if (showPinDialog) {
        PinSetupDialog(
            onDismiss = { showPinDialog = false },
            onConfirm = { pin ->
                appLockViewModel.setPin(pin)
                ir.sadteam.loancalc.ui.security.LockType.setPattern(context, false)
                isPattern = false
                showPinDialog = false
            },
        )
    }
    if (showPatternDialog) {
        ir.sadteam.loancalc.ui.subscription.PremiumBlock(
            blocked = true, key = "pattern_lock", label = "قفلِ الگویی", onBlocked = { showPatternDialog = false },
        )
        PatternSetupDialog(
            onDismiss = { showPatternDialog = false },
            onConfirm = { seq ->
                appLockViewModel.setPin(seq)
                ir.sadteam.loancalc.ui.security.LockType.setPattern(context, true)
                isPattern = true
                showPatternDialog = false
            },
        )
    }

    // ── گروهِ قفل ─────────────────────────────────────────────────────────────
    // قاعده‌ی صریحِ طراح: **قفل که خاموشه، ردیف‌های زیرش پنهان می‌شن، نه خاکستری.**
    // «سه ردیفِ خاکستریِ بی‌کار بدتر از یه ردیفِ تنهاست.»
    SettingsHero(
        Icons.Filled.Shield,
        "امنیت و حریمِ خصوصی",
        "قفلِ برنامه روی همین گوشی و پنهان‌کردنِ مبلغ‌ها",
        badge = if (hasLock) "قفل فعال" else "بی‌قفل",
    )
    SettingsGroupLabel("قفلِ برنامه")
    SettingsGroup {
        SettingsRowItem(
            title = "قفل با رمزِ عددی",
            icon = Icons.Filled.Lock,
            tone = SettingsTone.BLUE,
            status = if (pinHash != null && !isPattern) "فعال است" else "خاموش",
            statusTone = if (pinHash != null && !isPattern) StatusTone.HEALTHY else StatusTone.NEUTRAL,
            checked = pinHash != null && !isPattern,
            onCheckedChange = { checked ->
                if (checked) showPinDialog = true else verifyThen = { appLockViewModel.clearPin() }
            },
        )
        SettingsDivider()
        // قفلِ الگویی - همان جای PIN ذخیره می‌شود، پس روشن‌کردنِ یکی دیگری را جایگزین می‌کند.
        SettingsRowItem(
            title = "قفل با الگو",
            icon = Icons.Filled.Lock,
            tone = SettingsTone.BLUE,
            status = if (pinHash != null && isPattern) "فعال است" else "خاموش",
            statusTone = if (pinHash != null && isPattern) StatusTone.HEALTHY else StatusTone.NEUTRAL,
            checked = pinHash != null && isPattern,
            onCheckedChange = { checked ->
                if (checked) {
                    showPatternDialog = true
                } else {
                    verifyThen = {
                        appLockViewModel.clearPin()
                        ir.sadteam.loancalc.ui.security.LockType.setPattern(context, false)
                        isPattern = false
                    }
                }
            },
        )
        if (pinHash != null) {
            SettingsDivider()
            SettingsRowItem(
                title = if (isPattern) "تغییرِ الگو" else "تغییرِ رمزِ عددی",
                icon = Icons.Filled.Password,
                tone = SettingsTone.NEUTRAL,
                onClick = { verifyThen = { if (isPattern) showPatternDialog = true else showPinDialog = true } },
            )
        }
        if (pinHash != null) SettingsDivider()
        if (pinHash != null) SettingsRowItem(
            title = "قفل با اثرِ انگشت",
            icon = Icons.Filled.Fingerprint,
            tone = SettingsTone.BLUE,
            status = if (biometricAvailable(context)) null else "این گوشی اثرِ انگشتِ ثبت‌شده ندارد",
            statusTone = StatusTone.BROKEN,
            checked = if (biometricAvailable(context)) biometricEnabled else null,
            onCheckedChange = if (biometricAvailable(context)) {
                { appLockViewModel.setBiometricEnabled(it) }
            } else {
                null
            },
            onClick = if (biometricAvailable(context)) null else ({}),
        )
    }

    // ردیفِ «قفل بعد از» فقط وقتی معنی داره که اصلاً قفلی هست.
    if (hasLock) {
        SettingsGroup(modifier = Modifier.padding(top = 8.dp)) {
            SettingsRowItem(
                title = "قفل بعد از",
                icon = Icons.Filled.Timer,
                tone = SettingsTone.NEUTRAL,
                status = autoLockTimeoutOptions.firstOrNull { it.first == autoLockTimeoutMinutes }?.second,
                onClick = null,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 13.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                autoLockTimeoutOptions.forEach { (minutes, label) ->
                    AppChip(
                        label = label,
                        selected = autoLockTimeoutMinutes == minutes,
                        onClick = { appLockViewModel.setAutoLockTimeoutMinutes(minutes) },
                    )
                }
            }
        }
    }

    // ── گروهِ حریمِ خصوصی ─────────────────────────────────────────────────────
    // ⚠️ «پنهان در فهرستِ برنامه‌های اخیر» (`FLAG_SECURE`) عمداً پیاده **نشد** - قبلاً بود و
    // به تصمیمِ صریحِ کاربر حذف شد (رجوع کن به CLAUDE.md). این ردیفِ طرح رو نادیده می‌گیریم.
    SettingsGroupLabel("حریمِ خصوصی")
    SettingsGroup {
        SettingsRowItem(
            title = "پنهان‌کردنِ مبلغ‌ها",
            icon = Icons.Filled.VisibilityOff,
            tone = SettingsTone.NEUTRAL,
            status = if (privacyMode) {
                "مبلغ‌ها پشتِ ••••• پنهان‌اند - متنِ اعلان‌ها هم بی‌مبلغ می‌شود"
            } else {
                "مبلغ‌ها دیده می‌شوند"
            },
            checked = privacyMode,
            onCheckedChange = { privacyViewModel.toggle() },
        )
        // پیش‌نمایشِ دو حالت با عددِ نمونه - مبلغِ واقعیِ کاربر این‌جا نشان داده نمی‌شود.
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(false to "۱۲٬۳۴۵٬۶۷۸", true to "••••••").forEach { (hidden, sample) ->
                val active = hidden == privacyMode
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (active) AppPrimaryPill else AppSurface2)
                        .border(if (active) 1.5.dp else 0.dp, if (active) AppPrimary else Color.Transparent, RoundedCornerShape(14.dp))
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(sample, color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    Text(if (hidden) "پنهان" else "نمایان", color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        // کلیدِ «ارسالِ آمار» به خواسته‌ی صاحبِ برنامه (۷ مهر) از تنظیمات برداشته شد؛ آمار بی‌نام است و
        // در سیاستِ حریمِ خصوصیِ استور اعلام می‌شود. `UsageStats.setEnabled` برای برگرداندنش مانده.
    }

    // ── کارتِ توضیحِ ته صفحه ──────────────────────────────────────────────────
    // جمله‌ی سوم مهم‌ترینه: بی اون، کاربرِ فراموش‌کار فکر می‌کنه داده‌ش رفته و اپ رو پاک می‌کنه.
    SettingsDisclosure(title = "قفلِ برنامه چه کاری می‌کند؟") {
        SettingsParagraph(
            "قفلِ برنامه فقط جلوی بازشدنِ برنامه رو همین گوشی رو می‌گیره. داده‌هات رو سرور با " +
                "حسابِ کاربریت محافظت می‌شه، نه با این رمز. اگه رمز رو فراموش کنی، با ورودِ " +
                "دوباره به حساب بازش می‌کنی.",
        )
    }
}
private val autoLockTimeoutOptions = listOf(0 to "بی‌درنگ", 1 to "۱ دقیقه", 5 to "۵ دقیقه", 15 to "۱۵ دقیقه")
@Composable
private fun PatternSetupDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var first by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    JibakAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (first == null) "الگو را بکش" else "دوباره همان الگو را بکش") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (first == null) "مرحله ۱ از ۲ · دست‌کم ۴ نقطه را به هم وصل کن" else "مرحله ۲ از ۲ · برای تأیید دوباره بکش",
                    color = AppMuted,
                    fontSize = 12.sp,
                )
                ir.sadteam.loancalc.ui.security.PatternPad(
                    onComplete = { seq ->
                        val f = first
                        when {
                            f == null -> { first = seq; error = null }
                            f == seq -> onConfirm(seq)
                            else -> { first = null; error = "دو الگو یکی نبود؛ از اول بکش." }
                        }
                    },
                    modifier = Modifier.padding(top = 12.dp),
                )
                error?.let { Text(it, color = AppDanger, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp)) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
    )
}
/**
 * فریمِ `26a`ِ ChatGPT: دو مرحله («PIN جدید» ← «تکرارش») با خانه‌های PIN و صفحه‌کلیدِ خودِ اپ،
 * هم‌شکلِ صفحه‌ی قفل - جای دو فیلدِ متنی.
 */
@Composable
private fun PinSetupDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var first by remember { mutableStateOf<String?>(null) }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    fun next() {
        val f = first
        when {
            pin.length < 4 -> error = "PIN باید حداقل ۴ رقم باشه"
            f == null -> { first = pin; pin = ""; error = null }
            f == pin -> onConfirm(pin)
            else -> { first = null; pin = ""; error = "دو PIN یکی نبود؛ از اول بزن." }
        }
    }

    JibakAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تنظیم PIN") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (first == null) "مرحله ۱ از ۲ · PIN جدید (۴ تا ۸ رقم)" else "مرحله ۲ از ۲ · همان PIN را تکرار کن",
                    color = AppMuted,
                    fontSize = 12.sp,
                )
                Box(Modifier.padding(vertical = 14.dp)) {
                    ir.sadteam.loancalc.ui.security.PinBoxes(length = pin.length, error = error != null && pin.isEmpty(), boxWidth = 28)
                }
                ir.sadteam.loancalc.ui.security.NumberPad(
                    onDigit = { c -> if (pin.length < ir.sadteam.loancalc.ui.security.PIN_MAX) { pin += c; error = null } },
                    onBackspace = { pin = pin.dropLast(1) },
                    onDone = { next() },
                    keyWidth = 70,
                )
                error?.let { Text(it, color = AppDanger, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
            }
        },
        confirmButton = {
            TextButton(onClick = { next() }) { Text(if (first == null) "بعدی" else "ذخیره") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        },
    )
}
