package ir.sadteam.loancalc.ui.account

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
            // خواسته‌ی کاربر: «می‌خوام واردِ پیامکِ گوشی بشه» - برنامه‌ی پیامکِ پیش‌فرضِ خودِ
            // گوشی باز می‌شود. اگر پیدا نشد، `sms:`ِ عمومی که هر برنامه‌ی پیامکی می‌گیردش.
            IconButton(onClick = { openPhoneSmsApp(context) }) {
                Icon(Icons.Filled.OpenInNew, contentDescription = "بازکردنِ برنامه‌ی پیامک", tint = AppMuted)
            }
        }
        Text(
            if (sender == null) {
                "فرستنده را انتخاب کن تا پیامک‌هایش را ببینی."
            } else {
                "پیامی را که می‌خواهی ثبت شود انتخاب کن."
            },
            color = AppMuted,
            fontSize = 11.sp,
            lineHeight = 19.sp,
            modifier = Modifier.padding(bottom = 10.dp),
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
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(groups.toList(), key = { it.key }) { entry ->
                    SenderRow(
                        address = entry.key,
                        sample = entry.value.first().body,
                        count = entry.value.size,
                        banky = entry.value.any { it.parsed != null },
                        onClick = { openSender = entry.key },
                    )
                }
            }
        } else {
            val ofSender = messages.filter { it.address == sender }
            val addable = ofSender.filter { it.parsed != null && it.id !in addedIds }
            if (addable.isNotEmpty()) {
                GradientButton(
                    onClick = { bulkSender = sender },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                ) {
                    Text("افزودنِ همه‌ی ${toFa(addable.size)} پیامِ مبلغ‌دار")
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ofSender, key = { it.id }) { sms ->
                    SmsRow(
                        sms = sms,
                        added = sms.id in addedIds,
                        onAdd = { pending = sms },
                    )
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
                val today = JalaliCalendar.today()
                accountViewModel.addTransaction(
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
                    val today = JalaliCalendar.today()
                    accountViewModel.addTransaction(
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

/** طبقه‌ی اول: یک فرستنده با نمونه‌ی آخرین پیامش. */
@Composable
private fun SenderRow(
    address: String,
    sample: String,
    count: Int,
    banky: Boolean,
    onClick: () -> Unit,
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().pressScaleClickable(scale = 0.99f, onClick = onClick)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Ltr {
                    Text(address, color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
                if (banky) {
                    Text(
                        "بانکی",
                        color = AppPrimary,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                Text(
                    "${count.toString().faDigits()} پیام",
                    color = AppMuted,
                    fontSize = 9.5.sp,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Text(
                sample.replace('\n', ' ').take(90),
                color = AppMuted,
                fontSize = 10.5.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}

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
    val today = JalaliCalendar.today()
    val base = System.currentTimeMillis()
    rows.forEachIndexed { index, sms ->
        val parsed = sms.parsed ?: return@forEachIndexed
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
