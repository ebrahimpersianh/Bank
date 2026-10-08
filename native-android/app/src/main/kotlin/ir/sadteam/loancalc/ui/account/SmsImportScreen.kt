package ir.sadteam.loancalc.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.CheckCircle
import ir.sadteam.loancalc.data.db.ACCOUNT_TYPE_BANK
import ir.sadteam.loancalc.ui.components.AppChip
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.AccountBalance
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.MerchantCategoryGuesser
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.core.smsSenderMatches
import ir.sadteam.loancalc.ui.components.AccountPickerDialog
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.ConfirmDialog
import ir.sadteam.loancalc.ui.components.ConfirmTone
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * **افزودن از پیامک‌ها** - خواسته‌ی صریحِ کاربر: «بروی توی صفحه‌ی پیامک‌ها و خودت آن پیام را
 * انتخاب کنی».
 *
 * پس صفحه **دو طبقه** است، دقیقاً مثلِ برنامه‌ی پیامکِ خودِ گوشی:
 * ۱. فهرستِ فرستنده‌ها (آن‌هایی که پیامکشان شبیهِ بانکی است اول می‌آیند)،
 * ۲. با زدنِ هر فرستنده، **همه‌ی** پیامک‌های همان فرستنده - نه فقط آن‌هایی که مبلغ دارند،
 *    چون کاربر باید خودِ پیام را ببیند و بشناسد. پیامکِ بدونِ مبلغ فقط دکمه‌ی «افزودن» ندارد.
 *
 * ⚠️ مجوزِ `READ_SMS` **فقط** لحظه‌ی باز شدنِ همین صفحه گرفته می‌شود، هیچ متنی ذخیره یا
 * فرستاده نمی‌شود، و تراکنشِ ساخته‌شده **تاییدشده** است چون خودِ کاربر انتخابش کرده.
 */
@Composable
fun SmsImportScreen(
    onBack: () -> Unit,
    accountViewModel: AccountViewModel = hiltViewModel(),
) {
    // بازبینیِ ۹ مهر: ورودِ انبوهِ پیامک‌ها سقفِ ۳۰ تراکنشِ نسخه‌ی رایگان را دور می‌زد.
    ir.sadteam.loancalc.ui.subscription.PremiumBlock(blocked = true, key = "sms_import", label = "واردکردنِ پیامک‌های قدیمی", onBlocked = onBack)
    val context = LocalContext.current
    val accounts by accountViewModel.accounts.collectAsState()
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var messages by remember { mutableStateOf<List<SmsInboxMessage>>(emptyList()) }
    var openSender by remember { mutableStateOf<String?>(null) }
    var pending by remember { mutableStateOf<SmsInboxMessage?>(null) }
    var addedIds by remember { mutableStateOf(setOf<Long>()) }
    // خواسته‌ی کاربر: «یک شماره را کامل بتوانم افزودن بزنم، نه دانه‌دانه‌ی پیام‌ها».
    var bulkSender by remember { mutableStateOf<String?>(null) }
    var bulkAccountPickFor by remember { mutableStateOf<List<SmsInboxMessage>?>(null) }
    // ۱۰ مهر (خواسته‌ی کاربر): تپ روی سرشماره **خودِ سرشماره را برای یک حسابِ بانکی انتخاب می‌کند**
    // (نه اینکه وارد پیام‌ها شود)؛ از آن به بعد پیامکِ همان سرشماره سریع شناخته می‌شود. دیدنِ
    // پیام‌ها / ثبتِ گروهیِ قدیمی‌ها با دکمه‌ی «پیام‌ها» همان‌جا می‌ماند.
    val bankAccounts = accounts.filter { it.type == ACCOUNT_TYPE_BANK }
    var pickedAccountId by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<Long?>(null) }
    val targetAccount = bankAccounts.firstOrNull { it.id == pickedAccountId }
        ?: bankAccounts.firstOrNull { it.smsSender.isNullOrBlank() }
        ?: bankAccounts.firstOrNull()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted = it }

    LaunchedEffect(Unit) {
        if (!granted) permissionLauncher.launch(Manifest.permission.READ_SMS)
    }
    LaunchedEffect(granted) {
        if (granted) messages = readSmsInbox(context, limit = 500)
    }

    // دکمه‌ی برگشتِ گوشی: اول از طبقه‌ی دوم به فهرستِ فرستنده‌ها، بعد بیرون.
    BackHandler(enabled = openSender != null) { openSender = null }

    // ⚠️ این صفحه **هدر و اسکرولِ خودش** را دارد و نباید داخلِ اسکافولدِ اسکرول‌دارِ تنظیمات
    // رندر شود (رجوع کن به کامنتِ محلِ فراخوانی در `SettingsScreen`).
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        val sender = openSender
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { if (sender != null) openSender = null else onBack() }) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText)
            }
            Text(
                sender ?: "افزودن از پیامک‌ها",
                color = AppText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f).padding(start = 4.dp),
            )
            // 🚨 دکمه‌ی «بازکردنِ برنامه‌ی پیامک» از این‌جا **برداشته شد** (بندِ ۱۰ِ دورِ ۹).
            // هدر جای **کنشِ اصلیِ** صفحه است و کنشِ اصلیِ این صفحه «از این فهرست انتخاب
            // کن» است، نه «برو جای دیگر» - کاربر آن را می‌زد، از برنامه بیرون می‌رفت و
            // فهرستِ داخلی را اصلاً نمی‌دید. حالا تهِ صفحه است، به‌عنوانِ راهِ فرار.
        }
        Text(
            if (sender == null) {
                "اول حسابِ بانکی را بالا انتخاب کن، بعد روی سرشماره‌ی همان بانک بزن. " +
                    "از آن به بعد پیامک‌های این سرشماره خودکار برای آن حساب شناخته می‌شوند."
            } else {
                "پیامی را که می‌خواهی ثبت شود انتخاب کن. از برنامه‌ی پیامکِ خودِ گوشی هم " +
                    "می‌توانی پیام را «اشتراک‌گذاری» کنی و جیبک را بزنی."
            },
            color = AppMuted,
            fontSize = 13.sp,
            lineHeight = 21.sp,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        if (!granted) {
            EmptyState(
                icon = Icons.Filled.Sms,
                title = "اجازه‌ی خواندنِ پیامک لازم است",
                description = "بی این اجازه نمی‌توانم فهرستِ پیامک‌ها را نشان بدهم. همه‌چیز روی گوشیِ خودت می‌ماند.",
            )
            GradientButton(onClick = { permissionLauncher.launch(Manifest.permission.READ_SMS) }) {
                Text("اجازه می‌دهم")
            }
            return@Column
        }

        if (messages.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Sms,
                title = "پیامکی پیدا نشد",
                description = "صندوقِ ورودیِ گوشی خالی است.",
            )
            return@Column
        }

        if (sender == null) {
            // فرستنده‌ها به‌ترتیبِ تازگی (چون خودِ فهرست جدیدترین-اول است)، ولی آن‌هایی که
            // پیامکِ مبلغ‌دار دارند بالا می‌آیند تا بینِ ده‌ها سرشماره‌ی تبلیغاتی گم نشوند.
            val groups = messages.groupBy { it.address }.entries
                .sortedByDescending { entry -> entry.value.any { it.parsed != null } }
            if (bankAccounts.isEmpty()) {
                AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Text(
                        "هنوز حسابِ بانکی نساخته‌ای. اول در «دارایی» یک حسابِ بانکی اضافه کن، بعد این‌جا سرشماره‌اش را انتخاب کن.",
                        color = AppMuted, fontSize = 12.sp, lineHeight = 20.sp,
                    )
                }
            } else {
                Text("سرشماره‌ها برای کدام حساب؟", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 10.dp),
                ) {
                    items(bankAccounts, key = { it.id }) { acc ->
                        AppChip(
                            label = acc.name,
                            selected = acc.id == targetAccount?.id,
                            onClick = { pickedAccountId = acc.id },
                        )
                    }
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(groups.toList(), key = { it.key }) { entry ->
                    val owner = bankAccounts.firstOrNull { smsSenderMatches(it.smsSender, entry.key) }
                    SenderRow(
                        address = entry.key,
                        sample = entry.value.first().body,
                        count = entry.value.size,
                        dateMs = entry.value.first().dateMs,
                        banky = entry.value.any { it.parsed != null },
                        ownerName = owner?.name,
                        onClick = {
                            val t = targetAccount
                            if (t != null) accountViewModel.updateAccount(t.copy(smsSender = entry.key, smsEnabled = true))
                        },
                        onShowMessages = { openSender = entry.key },
                    )
                }
                item { OpenPhoneSmsRow(onClick = { openPhoneSmsApp(context) }) }
            }
        } else {
            val ofSender = messages.filter { it.address == sender }
            val addable = ofSender.filter { it.parsed != null && it.id !in addedIds }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(ofSender, key = { it.id }) { sms ->
                    SmsRow(
                        sms = sms,
                        added = sms.id in addedIds,
                        onAdd = { pending = sms },
                    )
                }
                item { OpenPhoneSmsRow(onClick = { openPhoneSmsApp(context) }) }
            }
            // 🚨 **نوارِ چسبانِ پایین، نه دکمه‌ی بالای فهرست** (جوابِ طراح، بخشِ ۷۲) - همان
            // الگوی نوارِ گروهیِ `64`: کاربر اول فهرست را می‌خوانَد و بعد تصمیم می‌گیرد؛
            // دکمه‌ی بالای فهرست پیش از دیدنِ N فشار می‌آورد. و عددِ N روی **خودِ دکمه** است.
            if (addable.isNotEmpty()) {
                GradientButton(
                    onClick = { bulkSender = sender },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
                ) {
                    Text("افزودنِ همه‌ی ${toFa(addable.size)} پیامِ مبلغ‌دار")
                }
            }
        }
    }

    // ثبتِ گروهی: چون چند تراکنش پشتِ‌هم ساخته می‌شود، **idِ صریحِ شمارنده‌دار** پاس داده
    // می‌شود - پیش‌فرضِ `System.currentTimeMillis()` در حلقه‌ی سریع یکی درمی‌آید و `@Upsert`
    // بی‌صدا رویشان می‌نویسد (باگِ ثبت‌شده در CLAUDE.md).
    bulkSender?.let { address ->
        val rows = messages.filter { it.address == address && it.parsed != null && it.id !in addedIds }
        ConfirmDialog(
            tone = ConfirmTone.HEAVY_CHANGE,
            title = "${toFa(rows.size)} پیام یک‌جا ثبت شود؟",
            consequence = "همه‌ی پیام‌های مبلغ‌دارِ این فرستنده به یک حساب‌کتاب اضافه می‌شوند. " +
                "هر کدام را بعداً می‌توانی جدا ویرایش یا حذف کنی.",
            actionLabel = "ثبت کن",
            onConfirm = {
                bulkSender = null
                val guessed = accounts.firstOrNull { smsSenderMatches(it.smsSender, address) }
                if (guessed != null) {
                    addAllFromSender(accountViewModel, guessed.id, rows)
                    addedIds = addedIds + rows.map { it.id }
                } else {
                    bulkAccountPickFor = rows
                }
            },
            onDismiss = { bulkSender = null },
        )
    }

    bulkAccountPickFor?.let { rows ->
        AccountPickerDialog(
            accounts = accounts,
            title = "روی کدام حساب‌کتاب ثبت شوند؟",
            onDismiss = { bulkAccountPickFor = null },
            onSelect = { account ->
                addAllFromSender(accountViewModel, account.id, rows)
                addedIds = addedIds + rows.map { it.id }
                bulkAccountPickFor = null
            },
        )
    }

    pending?.let { sms ->
        val parsed = sms.parsed ?: return@let
        // حسابِ پیش‌فرض: همانی که سرشماره‌اش با فرستنده می‌خواند. اگر پیدا نشد، خودِ کاربر
        // انتخاب می‌کند - حدسِ «اولین حساب» همان اشتباهی است که یک‌بار پیامکِ بانکِ B را روی
        // حسابِ بانکِ A نشاند.
        val guessed = accounts.firstOrNull { smsSenderMatches(it.smsSender, sms.address) }
        if (guessed != null) {
            LaunchedEffect(sms.id) {
                val today = smsJalali(sms.dateMs)
                if (!alreadyRecorded(accountViewModel, guessed.id, parsed, today)) accountViewModel.addTransaction(
                    accountId = guessed.id,
                    type = parsed.type,
                    amount = parsed.amountRial,
                    description = "از پیامکِ ${sms.address}",
                    year = today.y,
                    month = today.m,
                    day = today.d,
                    category = MerchantCategoryGuesser.guess(sms.body, parsed.type == TransactionType.WITHDRAWAL),
                    originLabel = "پیامکِ ${sms.address}",
                )
                addedIds = addedIds + sms.id
                pending = null
            }
        } else {
            AccountPickerDialog(
                accounts = accounts,
                title = "روی کدام حساب‌کتاب ثبت شود؟",
                onDismiss = { pending = null },
                onSelect = { account ->
                    val today = smsJalali(sms.dateMs)
                    if (!alreadyRecorded(accountViewModel, account.id, parsed, today)) accountViewModel.addTransaction(
                        accountId = account.id,
                        type = parsed.type,
                        amount = parsed.amountRial,
                        description = "از پیامکِ ${sms.address}",
                        year = today.y,
                        month = today.m,
                        day = today.d,
                        originLabel = "پیامکِ ${sms.address}",
                        category = MerchantCategoryGuesser.guess(
                            sms.body,
                            parsed.type == TransactionType.WITHDRAWAL,
                        ),
                    )
                    addedIds = addedIds + sms.id
                    pending = null
                },
            )
        }
    }
}

/**
 * راهِ فرار، نه راهِ اصلی (بندِ ۱۰ِ دورِ ۹): اگر پیامِ موردِ نظر این‌جا نبود، برنامه‌ی
 * پیامکِ گوشی باز می‌شود. **تهِ فهرست** است چون کنشِ اصلیِ این صفحه انتخاب از همین
 * فهرست است؛ در هدر بودنش کاربر را قبل از دیدنِ فهرست بیرون می‌فرستاد.
 */
@Composable
private fun OpenPhoneSmsRow(onClick: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().pressScaleClickable(scale = 0.99f, onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.OpenInNew,
                contentDescription = null,
                tint = AppMuted,
                modifier = Modifier.size(16.dp),
            )
            Text(
                "پیامِ موردِ نظرت این‌جا نیست؟ برنامه‌ی پیامک را باز کن",
                color = AppMuted,
                fontSize = 11.sp,
                lineHeight = 19.sp,
                modifier = Modifier.weight(1f).padding(start = 8.dp),
            )
        }
    }
}

/** طبقه‌ی اول: یک فرستنده با نمونه‌ی آخرین پیامش. */
@Composable
private fun SenderRow(
    address: String,
    sample: String,
    count: Int,
    dateMs: Long,
    banky: Boolean,
    ownerName: String?,
    onClick: () -> Unit,
    onShowMessages: () -> Unit,
) {
    // طرحِ Claude Design (۸ مهر): کاشیِ آیکون، حاشیه‌ی سبز برای فرستنده‌ی بانکی.
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (ownerName != null) AppPrimary else if (banky) AppPrimary.copy(alpha = 0.45f) else null,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().pressScaleClickable(scale = 0.99f, onClick = onClick),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(AppRadius.icon))
                    .background(if (banky) AppPrimary.copy(alpha = 0.12f) else AppMuted.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (banky) Icons.Filled.AccountBalance else Icons.Filled.Sms,
                    contentDescription = null,
                    tint = if (banky) AppPrimary else AppMuted,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Ltr {
                        Text(address, color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                    if (banky) {
                        Text(
                            "بانکی",
                            color = AppPrimary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(AppPrimary.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 1.dp),
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${count.toString().faDigits()} پیام ›",
                        color = AppPrimaryDim,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.pressScaleClickable(onClick = onShowMessages).padding(vertical = 4.dp, horizontal = 4.dp),
                    )
                }
                if (ownerName != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(14.dp))
                        Text(
                            "سرشماره‌ی $ownerName",
                            color = AppPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
                Text(
                    sample.replace('\n', ' ').take(90),
                    color = AppMuted,
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
                // تاریخ و ساعتِ همین پیام (۱۶ مهر، خواسته‌ی کاربر).
                Text(
                    smsFullDateLabel(dateMs),
                    color = AppMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
        }
    }
}

/** «۱۴۰۵/۷/۱۶ · ۱۵:۰۹» - تاریخِ جلالیِ کامل + ساعت. */
private fun smsFullDateLabel(ms: Long): String = runCatching {
    val zoned = java.time.Instant.ofEpochMilli(ms).atZone(java.time.ZoneId.systemDefault())
    val j = ir.sadteam.loancalc.core.JalaliCalendar.fromGregorian(zoned.year, zoned.monthValue, zoned.dayOfMonth)
    "${toFa(j.y)}/${toFa(j.m)}/${toFa(j.d)} · ${toFa(zoned.hour)}:${toFa(zoned.minute).padStart(2, '۰')}"
}.getOrDefault("")

/** طبقه‌ی دوم: یک پیامکِ همان فرستنده. بدونِ مبلغِ قابلِ‌خواندن، دکمه‌ی افزودن ندارد. */
@Composable
private fun SmsRow(sms: SmsInboxMessage, added: Boolean, onAdd: () -> Unit) {
    val parsed = sms.parsed
    val isWithdrawal = parsed?.type == TransactionType.WITHDRAWAL
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                if (parsed != null) {
                    Text(
                        "${if (isWithdrawal) "برداشت" else "واریز"} " +
                            "${fmt(rialToToman(parsed.amountRial.toLong()).toDouble()).faDigits()} تومان",
                        color = AppText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
                Text(
                    sms.body.replace('\n', ' ').take(160),
                    color = if (parsed != null) AppMuted else AppText,
                    fontSize = 10.5.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = if (parsed != null) 3.dp else 0.dp),
                )
            }
            Text(
                when {
                    parsed == null -> "بدونِ مبلغ"
                    added -> "ثبت شد"
                    else -> "افزودن"
                },
                color = if (parsed == null || added) AppMuted else AppPrimaryDim,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .then(if (parsed == null || added) Modifier else Modifier.pressScaleClickable(onClick = onAdd))
                    .padding(horizontal = 8.dp, vertical = 10.dp),
            )
        }
    }
}


/**
 * بازکردنِ برنامه‌ی پیامکِ خودِ گوشی (خواسته‌ی کاربر).
 *
 * اول برنامه‌ی **پیش‌فرضِ پیامک** را می‌گیرد؛ اگر نشد `sms:`ِ عمومی، که هر برنامه‌ی پیامکی
 * جوابش را می‌دهد. هیچ‌کدام نشد، بی‌صدا رد می‌شود - این یک میان‌بر است نه مسیرِ اصلی.
 */
private fun openPhoneSmsApp(context: android.content.Context) {
    val pkg = runCatching { Telephony.Sms.getDefaultSmsPackage(context) }.getOrNull()
    val intent = pkg?.let { context.packageManager.getLaunchIntentForPackage(it) }
        ?: Intent(Intent.ACTION_VIEW, android.net.Uri.parse("sms:"))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

/** ثبتِ گروهیِ پیام‌های یک فرستنده روی یک حساب - با شمارنده‌ی صریحِ id. */
private fun addAllFromSender(
    accountViewModel: AccountViewModel,
    accountId: Long,
    rows: List<SmsInboxMessage>,
) {
    val base = System.currentTimeMillis()
    rows.forEachIndexed { index, sms ->
        val parsed = sms.parsed ?: return@forEachIndexed
        val today = smsJalali(sms.dateMs)
        if (alreadyRecorded(accountViewModel, accountId, parsed, today)) return@forEachIndexed
        accountViewModel.addTransaction(
            accountId = accountId,
            type = parsed.type,
            amount = parsed.amountRial,
            description = "از پیامکِ ${sms.address}",
            year = today.y,
            month = today.m,
            day = today.d,
            category = MerchantCategoryGuesser.guess(sms.body, parsed.type == TransactionType.WITHDRAWAL),
            id = base + index,
            originLabel = "پیامکِ ${sms.address}",
        )
    }
}

/** تاریخِ خودِ پیامک (بازبینیِ ۹ مهر) - پیامکِ ماهِ پیش با تاریخِ امروز ثبت می‌شد و گزارش‌ها را بهم می‌ریخت. */
private fun smsJalali(ms: Long): ir.sadteam.loancalc.core.PersianDate {
    if (ms <= 0) return JalaliCalendar.today()
    val c = java.util.Calendar.getInstance().apply { timeInMillis = ms }
    return JalaliCalendar.fromGregorian(c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH))
}

/** همان مبلغ و نوع در همان روز روی همان حساب از قبل هست (ورودِ دوباره، یا ثبتِ خودکارِ قبلی)؟ */
private fun alreadyRecorded(
    vm: AccountViewModel,
    accountId: Long,
    parsed: ir.sadteam.loancalc.core.ParsedBankSms,
    d: ir.sadteam.loancalc.core.PersianDate,
): Boolean = vm.transactions.value.any {
    it.accountId == accountId && it.type == parsed.type.name && it.amount == parsed.amountRial &&
        it.year == d.y && it.month == d.m && it.day == d.d
}
