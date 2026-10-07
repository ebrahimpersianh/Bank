package ir.sadteam.loancalc.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.data.db.ACCOUNT_TYPE_BANK
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.account.SmsImportScreen
import ir.sadteam.loancalc.ui.account.SmsSenderPickerDialog
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppCardVariant
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryBorder
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppUrgentShadow
import kotlinx.coroutines.launch

@Composable
internal fun SmsSettings(
    smsAutoImportViewModel: SmsAutoImportViewModel,
    onOpenRules: () -> Unit,
    accountViewModel: AccountViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val enabled by smsAutoImportViewModel.enabled.collectAsState()
    val accounts by accountViewModel.accounts.collectAsState()
    val scope = rememberCoroutineScope()
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionGranted = granted
        if (granted) smsAutoImportViewModel.enable()
    }
    var showParseTest by remember { mutableStateOf(false) }
    var showSmsImport by remember { mutableStateOf(false) }

    // ⚠️ **این‌جا `return` نذار.** نسخه‌ی قبلی صفحه‌ی آزمایش رو همین‌جا (به‌جای بقیه‌ی محتوا)
    // رندر می‌کرد، ولی `SmsSettings` خودش داخلِ `SettingsSubPageScaffold` (یه
    // `Column(verticalScroll)`) رندر می‌شه و `SmsParseTestScreen` هم اسکافولدِ اسکرول‌دارِ
    // خودش رو می‌سازه - اسکرولِ عمودیِ تودرتو با ارتفاعِ بی‌نهایت اندازه‌گیری می‌شه و اپ کرش
    // می‌کنه (گزارشِ واقعیِ کاربر: «رو تست می‌زنم، از برنامه می‌پره بیرون»). دیالوگِ
    // تمام‌صفحه ویندوی جداگانه دارد، پس اسکرولش تو اسکرولِ والد نمی‌افته.
    if (showParseTest) {
        FullScreenDialog(onDismissRequest = { showParseTest = false }) {
            Box(modifier = Modifier.fillMaxSize().background(AppBg)) {
                SmsParseTestScreen(onBack = { showParseTest = false })
            }
        }
    }

    // همان قاعده‌ی بالا: فهرستِ پیامک‌ها اسکرولِ خودش را دارد، پس داخلِ اسکرولِ تنظیمات
    // رندر نمی‌شود بلکه دیالوگِ تمام‌صفحه می‌گیرد.
    if (showSmsImport) {
        FullScreenDialog(onDismissRequest = { showSmsImport = false }) {
            // 🚨 **عمداً بدونِ `SettingsSubPageScaffold`**: آن اسکافولد یک
            // `Column(verticalScroll)` است و `SmsImportScreen` خودش فهرستِ تنبل دارد -
            // اسکرولِ عمودیِ تودرتو با ارتفاعِ بی‌نهایت اندازه‌گیری می‌شود و اپ **کرش می‌کند**
            // (گزارشِ واقعیِ کاربر: «روی افزودن پیامک می‌زنم، برنامه یهو بسته می‌شود» - دقیقاً
            // همان باگی که یک‌بار برای «آزمایشِ تشخیص» رخ داد). هدرِ خودش را دارد.
            Box(modifier = Modifier.fillMaxSize().background(AppBg)) {
                SmsImportScreen(onBack = { showSmsImport = false })
            }
        }
    }

    // ── کارتِ وضعیت - تنها کارتِ برجسته‌ی صفحه، سه حالت ──────────────────────
    // تصمیمِ تاییدشده‌ی طراح: اگه اجازه قطع شده، **کلید حالتِ چهارم نمی‌گیره** - همین کارت
    // به حالتِ خطا می‌ره و دکمه‌ی «اجازه بده» می‌گیره.
    var senderPickerFor by remember { mutableStateOf<AccountEntity?>(null) }
    var notifPickerFor by remember { mutableStateOf<AccountEntity?>(null) }
    notifPickerFor?.let { acc ->
        ir.sadteam.loancalc.ui.account.NotifAppPickerDialog(
            onDismiss = { notifPickerFor = null },
            onPick = { pkg ->
                val kept = acc.smsSender.orEmpty().split(',').map { it.trim() }.filter { it.isNotEmpty() && '.' !in it }
                scope.launch { accountViewModel.updateAccount(acc.copy(smsSender = (kept + pkg).joinToString(","), smsEnabled = true)) }
                smsAutoImportViewModel.setNotifPackageSelected(pkg, true)
                notifPickerFor = null
            },
        )
    }
    val listed = accounts.filter { it.type == ACCOUNT_TYPE_BANK }
    val activeCount = listed.count { it.smsEnabled && !it.smsSender.isNullOrBlank() }
    val smsPremium = ir.sadteam.loancalc.ui.subscription.LocalIsPremium.current
    SmsHero(
        ir.sadteam.loancalc.R.drawable.sms_illu_phone,
        "ثبتِ خودکار از پیامک",
        "با روشن کردنش، پیامک‌های بانکی‌ات خودکار در جیبک ثبت می‌شوند.",
        AppPrimaryPill,
    )
    SmsStatusCard(
        enabled = enabled,
        permissionGranted = permissionGranted,
        activeCount = activeCount,
        totalCount = listed.size,
        onToggle = { checked ->
            if (checked && !smsPremium) {
                ir.sadteam.loancalc.ui.subscription.PremiumPaywall.ask("sms_auto", "خوندنِ خودکارِ پیامکِ بانک")
            } else if (!checked) {
                smsAutoImportViewModel.disable()
            } else if (permissionGranted) {
                smsAutoImportViewModel.enable()
            } else {
                smsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
            }
        },
        onGrant = { smsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS) },
    )

    // ── گروهِ بانک‌ها ─────────────────────────────────────────────────────────
    // حساب‌کتابِ بی‌سرشماره هم میاد (ته فهرست) - تنها جاییه که کاربر می‌فهمه وصل نیست.
    if (listed.isNotEmpty()) {
        SettingsGroupLabel("بانک‌ها")
        val ordered = listed.sortedBy { it.smsSender.isNullOrBlank() }
        SettingsGroup(
            modifier = Modifier
                .then(if (enabled) Modifier else Modifier.alpha(0.5f)),
        ) {
            ordered.forEachIndexed { index, account ->
                val hasSender = !account.smsSender.isNullOrBlank()
                SettingsRowItem(
                    title = account.name,
                    icon = Icons.Filled.AccountBalance,
                    tone = if (hasSender) SettingsTone.GREEN else SettingsTone.NEUTRAL,
                    status = smsStatusText(account),
                    statusTone = smsStatusTone(account),
                    // سرشماره داره → کلید. نداره → شِورون. **هیچ‌وقت هر دو.**
                    checked = if (hasSender && enabled) account.smsEnabled else null,
                    onCheckedChange = if (hasSender && enabled) {
                        { checked ->
                            scope.launch {
                                accountViewModel.updateAccount(account.copy(smsEnabled = checked))
                            }
                        }
                    } else {
                        null
                    },
                    // 🚨 بی این، ردیفِ بانکِ بی‌سرشماره شِورون داشت ولی تپش **هیچ کاری
                    // نمی‌کرد** - و تنها راهِ ثبتِ سرشماره تهِ فرمِ حسابِ بانکی بود، جایی که
                    // کاربر دنبالش نمی‌گردد (گزارشِ ۶.۱ی بازخوردِ دوم: «خواندنِ پیامک از رو
                    // گوشی اصلاً تو تنظیمات نیست»). حالا همین‌جا انتخابگر باز می‌شود.
                    onClick = if (hasSender) null else ({ senderPickerFor = account }),
                )
                // اپِ اعلانِ این بانک - وصل/تغییر/برداشتن از همین‌جا (خواسته‌ی کاربر ۱۴ مهر).
                val linkedPkg = account.smsSender.orEmpty().split(',').map { it.trim() }.firstOrNull { '.' in it }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { notifPickerFor = account }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (linkedPkg != null) {
                        ir.sadteam.loancalc.ui.account.InstalledAppIconSmall(linkedPkg)
                        Text(
                            "اعلانِ «${ir.sadteam.loancalc.ui.account.appLabel(linkedPkg)}»",
                            color = AppText, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f).padding(start = 8.dp),
                        )
                        Text("تغییر", color = AppPrimary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        Text(
                            "برداشتن",
                            color = AppMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 14.dp).clickable {
                                val kept = account.smsSender.orEmpty().split(',').map { it.trim() }.filter { it.isNotEmpty() && '.' !in it }
                                scope.launch { accountViewModel.updateAccount(account.copy(smsSender = kept.joinToString(",").ifBlank { null })) }
                            },
                        )
                    } else {
                        Text(
                            "＋ وصلِ اعلانِ اپِ این بانک",
                            color = AppPrimary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }
                if (index != ordered.lastIndex) SettingsDivider()
            }
        }
    }

    // ── گروهِ ابزارها ────────────────────────────────────────────────────────
    SettingsGroupLabel("ابزارها")
    SettingsGroup {
        SettingsRowItem(
            title = "افزودن از پیامک‌ها",
            icon = Icons.Filled.Sms,
            tone = SettingsTone.GREEN,
            status = "پیامکی که خودکار خوانده نشده را دستی ثبت کن",
            onClick = { showSmsImport = true },
        )
        // «آزمایشِ تشخیص» و «قاعده‌های تشخیص» به خواسته‌ی کاربر (۱۴ مهر) از این‌جا برداشته شدند:
        // تشخیص باید خودش هوشمند باشد. قاعده‌های قبلیِ کاربر همچنان اعمال می‌شوند.
    }

    NotificationImportSettings(smsAutoImportViewModel)

    senderPickerFor?.let { account ->
        SmsSenderPickerDialog(
            onDismiss = { senderPickerFor = null },
            onPick = { sender ->
                scope.launch {
                    // سرشماره که ثبت شد، خواندن هم همان لحظه روشن می‌شود - وگرنه کاربر
                    // سرشماره را می‌دهد و هیچ اتفاقی نمی‌افتد تا کلیدِ دومی را پیدا کند.
                    accountViewModel.updateAccount(account.copy(smsSender = sender, smsEnabled = true))
                }
                senderPickerFor = null
            },
        )
    }
}
/**
 * زیرنویسِ ردیفِ هر بانک - **چهار حالته، مرزِ ۳۰ روز عمدیه** (قاعده‌ی صریحِ طراح: کوتاه‌ترش
 * کاربرِ کم‌تراکنش رو بی‌دلیل می‌ترسونه، بلندترش خرابیِ واقعی رو دیر می‌گه).
 */
private fun smsStatusText(account: AccountEntity): String {
    // smsSender = «سرشماره,بسته‌نامِ اپ» - هر دو جدا نشان داده می‌شوند (خواسته‌ی کاربر ۱۴ مهر).
    val parts = account.smsSender.orEmpty().split(',').map { it.trim() }.filter { it.isNotEmpty() }
    val hasSms = parts.any { '.' !in it }
    val hasNotif = parts.any { '.' in it }
    if (!hasSms && !hasNotif) return "پیامک و اعلان وصل نشده - لمس کن"
    val link = listOf(
        if (hasSms) "پیامک ✓" else "پیامک ✗",
        if (hasNotif) "اعلانِ اپ ✓" else "اعلانِ اپ ✗",
    ).joinToString(" · ")
    val last = account.lastSmsAt ?: return "$link · هنوز چیزی نیامده"
    val days = ((System.currentTimeMillis() - last) / 86_400_000L).toInt()
    return link + " · " + when {
        days > 30 -> "۳۰ روز چیزی نیامده"
        days <= 0 -> "آخرین: امروز"
        days == 1 -> "آخرین: دیروز"
        else -> "آخرین: ${toFa(days)} روز پیش"
    }
}
private fun smsStatusTone(account: AccountEntity): StatusTone {
    if (account.smsSender.isNullOrBlank()) return StatusTone.NEUTRAL
    val last = account.lastSmsAt ?: return StatusTone.BROKEN
    val days = (System.currentTimeMillis() - last) / 86_400_000L
    return if (days > 30) StatusTone.BROKEN else StatusTone.NEUTRAL
}
/** سرِ کارتِ پیامک/اعلان با تصویرِ سه‌بعدی (طرحِ ChatGPT، ۸ مهر). */
@Composable
private fun SmsHero(@androidx.annotation.DrawableRes image: Int, title: String, body: String, tint: Color) {
    AppCard(backgroundColor = tint, modifier = Modifier.padding(top = 12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            androidx.compose.foundation.Image(
                androidx.compose.ui.res.painterResource(image),
                contentDescription = null,
                modifier = Modifier.size(110.dp),
            )
            Text(title, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 6.dp))
            Text(
                body,
                color = AppMuted,
                fontSize = 11.5.sp,
                lineHeight = 19.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
@Composable
private fun SmsStatusCard(
    enabled: Boolean,
    permissionGranted: Boolean,
    activeCount: Int,
    totalCount: Int,
    onToggle: (Boolean) -> Unit,
    onGrant: () -> Unit,
) {
    val missingPermission = enabled && !permissionGranted
    AppCard(
        variant = if (missingPermission) AppCardVariant.URGENT else AppCardVariant.DEFAULT,
        backgroundColor = if (!missingPermission && enabled) AppPrimaryPill else null,
        borderColor = if (!missingPermission && enabled) AppPrimaryBorder else null,
        shadow = missingPermission,
        modifier = Modifier.padding(top = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            missingPermission -> AppUrgentShadow
                            enabled -> AppPrimary
                            else -> AppChipBg
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (missingPermission) Icons.Filled.Warning else Icons.Filled.Sms,
                    contentDescription = null,
                    tint = when {
                        missingPermission -> AppDanger
                        enabled -> Color.White
                        else -> AppLabel
                    },
                    modifier = Modifier.size(19.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    when {
                        missingPermission -> "اجازه‌ی خواندنِ پیامک قطع است"
                        enabled -> "ثبتِ خودکار روشن است"
                        else -> "ثبتِ خودکار خاموش است"
                    },
                    color = AppText,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    when {
                        missingPermission -> "بدونِ این اجازه پیامکِ بانک خوانده نمی‌شود."
                        enabled -> "${toFa(activeCount)} از ${toFa(totalCount)} بانک فعال"
                        else -> "تراکنش‌ها را دستی وارد می‌کنی"
                    },
                    color = when {
                        missingPermission -> AppDangerInk
                        enabled -> AppPrimaryInk
                        else -> AppMuted
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            // تو حالتِ «اجازه ندارد» کلید جاش رو به دکمه‌ی تمام‌عرضِ پایین می‌ده.
            if (!missingPermission) AppSwitch(checked = enabled, onCheckedChange = onToggle)
        }
        if (missingPermission) {
            GradientButton(
                onClick = onGrant,
                modifier = Modifier.fillMaxWidth().padding(top = 11.dp),
            ) {
                Text("اجازه بده")
            }
        }
    }
}
