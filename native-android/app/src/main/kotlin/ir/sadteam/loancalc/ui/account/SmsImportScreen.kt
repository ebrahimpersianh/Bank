package ir.sadteam.loancalc.ui.account

import android.Manifest
import android.content.pm.PackageManager
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
import ir.sadteam.loancalc.core.smsSenderMatches
import ir.sadteam.loancalc.ui.components.AccountPickerDialog
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * **افزودن از پیامک‌ها** - خواسته‌ی صریحِ کاربر: «بروی توی پیام‌ها، آن پیام را انتخاب کنی و
 * اضافه کنی».
 *
 * تشخیصِ خودکار فقط پیامکِ حساب‌های ثبت‌شده را می‌گیرد و گاهی هم اشتباه می‌کند؛ این صفحه راهِ
 * دستی است: صندوقِ ورودیِ گوشی خوانده می‌شود و هر پیامکی که مبلغِ قابلِ‌خواندن دارد یک دکمه‌ی
 * «افزودن» می‌گیرد.
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
    var pending by remember { mutableStateOf<SmsInboxMessage?>(null) }
    var addedIds by remember { mutableStateOf(setOf<Long>()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted = it }

    LaunchedEffect(Unit) {
        if (!granted) permissionLauncher.launch(Manifest.permission.READ_SMS)
    }
    LaunchedEffect(granted) {
        if (granted) messages = readSmsInbox(context)
    }

    // ⚠️ این صفحه **هدر و اسکرولِ خودش** را دارد و نباید داخلِ اسکافولدِ اسکرول‌دارِ تنظیمات
    // رندر شود (رجوع کن به کامنتِ محلِ فراخوانی در `SettingsScreen`).
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText)
            }
            Text(
                "افزودن از پیامک‌ها",
                color = AppText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        Text(
            "هر پیامکی که مبلغ داشته باشد این‌جا می‌آید. آن‌که می‌خواهی را انتخاب کن تا ثبت شود.",
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

        // پیامکی که پارس نمی‌شود اصلاً مبلغ ندارد، پس افزودنی هم نیست - نشان دادنش فقط
        // فهرست را شلوغ می‌کند.
        val usable = messages.filter { it.parsed != null }
        if (usable.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Sms,
                title = "پیامکِ مبلغ‌داری پیدا نشد",
                description = "در دویست پیامکِ آخر چیزی که شبیهِ تراکنش باشد نبود.",
            )
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(usable, key = { it.id }) { sms ->
                SmsRow(
                    sms = sms,
                    added = sms.id in addedIds,
                    onAdd = { pending = sms },
                )
            }
        }
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

@Composable
private fun SmsRow(sms: SmsInboxMessage, added: Boolean, onAdd: () -> Unit) {
    val parsed = sms.parsed ?: return
    val isWithdrawal = parsed.type == TransactionType.WITHDRAWAL
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${if (isWithdrawal) "برداشت" else "واریز"} " +
                        "${fmt(rialToToman(parsed.amountRial.toLong()).toDouble()).faDigits()} تومان",
                    color = AppText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    sms.body.replace('\n', ' ').take(110),
                    color = AppMuted,
                    fontSize = 10.5.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 3.dp),
                )
                Text(
                    sms.address,
                    color = AppPrimaryDim,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Text(
                if (added) "ثبت شد" else "افزودن",
                color = if (added) AppMuted else AppPrimaryDim,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .then(if (added) Modifier else Modifier.pressScaleClickable(onClick = onAdd))
                    .padding(horizontal = 8.dp, vertical = 10.dp),
            )
        }
    }
}
