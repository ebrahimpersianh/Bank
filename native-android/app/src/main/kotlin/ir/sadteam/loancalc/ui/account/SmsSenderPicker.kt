package ir.sadteam.loancalc.ui.account

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.core.toFa
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import ir.sadteam.loancalc.core.BankSmsParser
import ir.sadteam.loancalc.core.ParsedBankSms
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** یه فرستنده‌ی یکتا تو صندوقِ پیامک + آخرین متنی که ازش اومده (برای اینکه کاربر بشناسه مالِ کدوم
 * بانکه) و اینکه اصلاً شبیهِ پیامکِ بانکی هست یا نه. */
data class SmsSenderInfo(
    val address: String,
    val sample: String,
    val looksBanky: Boolean,
)

/**
 * فرستنده‌های یکتای صندوقِ پیامک رو (جدیدترین اول) می‌خونه - خواسته‌ی صریحِ کاربر: به‌جای تایپِ دستیِ
 * سرشماره‌ی بانک تو فرمِ حساب، بره تو پیامک‌های گوشی و همون‌جا انتخاب کنه.
 *
 * فقط `ADDRESS`/`BODY` آخرین [limit] پیامکِ **inbox** خونده می‌شه، همه‌چیز رو خودِ گوشی می‌مونه و
 * هیچ‌جا فرستاده نمی‌شه. فرستنده‌هایی که آخرین پیامکشون با [BankSmsParser] پارس می‌شه (یعنی واقعاً
 * الگوی برداشت/واریز دارن) اول لیست میان تا کاربر بینِ ده‌ها سرشماره‌ی تبلیغاتی گم نشه.
 */
suspend fun readSmsSenders(context: Context, limit: Int = 400): List<SmsSenderInfo> =
    withContext(Dispatchers.IO) {
        val result = LinkedHashMap<String, SmsSenderInfo>()
        runCatching {
            context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY),
                null,
                null,
                "${Telephony.Sms.DATE} DESC LIMIT $limit",
            )?.use { cursor ->
                val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
                while (cursor.moveToNext()) {
                    val address = cursor.getString(addressIndex)?.trim().orEmpty()
                    if (address.isEmpty() || result.containsKey(address)) continue
                    val body = cursor.getString(bodyIndex)?.trim().orEmpty()
                    result[address] = SmsSenderInfo(
                        address = address,
                        sample = body.replace('\n', ' ').take(90),
                        looksBanky = BankSmsParser.parse(body) != null,
                    )
                }
            }
        }
        result.values.sortedByDescending { it.looksBanky }
    }

/**
 * صفحه‌ی انتخاب از پیامک‌ها (خواسته‌ی کاربر، ۳ مهر): **خودِ پیامک‌ها** مثلِ صندوقِ گوشی، جدیدترین
 * اول؛ تپ روی هر پیامک شماره‌ی فرستنده‌اش را برمی‌دارد. پیامک‌هایی که شبیهِ پیامکِ بانکی‌اند
 * برچسبِ «بانکی» دارند. جست‌وجو هم روی شماره و هم روی متن.
 *
 * خودش مجوزِ `READ_SMS` را (فقط همین لحظه) می‌گیرد؛ اگر کاربر رد کند، تایپِ دستی همچنان راهِ کار است.
 * هیچ پیامکی از گوشی بیرون نمی‌رود.
 */
@Composable
fun SmsSenderPickerDialog(onDismiss: () -> Unit, onPick: (String) -> Unit) {
    val context = LocalContext.current
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var permissionDenied by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf<List<SmsInboxMessage>?>(null) }
    var query by remember { mutableStateOf("") }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionGranted = granted
        permissionDenied = !granted
    }

    LaunchedEffect(Unit) {
        if (!permissionGranted) permissionLauncher.launch(Manifest.permission.READ_SMS)
    }
    LaunchedEffect(permissionGranted) {
        if (permissionGranted) messages = readSmsInbox(context, limit = 300)
    }

    ir.sadteam.loancalc.ui.settings.FullScreenDialog(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxSize().background(AppBg)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText)
                }
                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Text("انتخاب از پیامک‌ها", color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text(
                        "روی پیامکِ بانکت بزن تا شماره‌اش برداشته شود",
                        color = AppMuted,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                placeholder = { Text("جست‌وجو در شماره یا متن", color = AppMuted, fontSize = 12.sp) },
                shape = RoundedCornerShape(AppRadius.button),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
            )
            val list = messages
            when {
                permissionDenied -> CenterNote("بدونِ اجازه‌ی خوندنِ پیامک نمی‌شه پیامک‌ها رو نشون داد. می‌تونی شماره رو دستی هم وارد کنی.")
                list == null -> CenterNote("در حالِ خوندنِ پیامک‌ها…")
                list.isEmpty() -> CenterNote("پیامکی تو صندوقِ گوشیت پیدا نشد")
                else -> {
                    val q = query.trim()
                    val shown = if (q.isEmpty()) list else list.filter { it.address.contains(q, true) || it.body.contains(q) }
                    LazyColumn(
                        contentPadding = PaddingValues(start = 10.dp, end = 14.dp, top = 4.dp, bottom = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        items(shown, key = { it.id }) { msg ->
                            AppCard(modifier = Modifier.pressScaleClickable(scale = 0.99f) { onPick(msg.address) }) {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Ltr {
                                        Text(msg.address, color = AppText, fontSize = 13.5.sp, fontWeight = FontWeight.Black)
                                    }
                                    if (msg.parsed != null) {
                                        Text(
                                            "بانکی",
                                            color = AppPrimaryInk,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier
                                                .padding(start = 8.dp)
                                                .clip(RoundedCornerShape(999.dp))
                                                .background(AppPrimaryPill)
                                                .padding(horizontal = 7.dp, vertical = 2.dp),
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(smsDateLabel(msg.dateMs), color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    msg.body.replace('\n', ' '),
                                    color = AppMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 18.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CenterNote(text: String) {
    Text(
        text,
        color = AppMuted,
        fontSize = 12.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(24.dp),
    )
}

/** «۷/۳ · ۱۴:۰۵» - روز/ماهِ جلالی + ساعت. */
private fun smsDateLabel(ms: Long): String = runCatching {
    val zoned = java.time.Instant.ofEpochMilli(ms).atZone(java.time.ZoneId.systemDefault())
    val j = ir.sadteam.loancalc.core.JalaliCalendar.fromGregorian(zoned.year, zoned.monthValue, zoned.dayOfMonth)
    "${toFa(j.m)}/${toFa(j.d)} · ${toFa(zoned.hour)}:${toFa(zoned.minute).padStart(2, '۰')}"
}.getOrDefault("")

/** یک پیامکِ صندوقِ ورودی - برای صفحه‌ی «افزودن از پیامک‌ها». */
data class SmsInboxMessage(
    val id: Long,
    val address: String,
    val body: String,
    val dateMs: Long,
    /** اگر با [BankSmsParser] خوانده شد، همان نتیجه؛ وگرنه `null`. */
    val parsed: ParsedBankSms?,
)

/**
 * آخرین [limit] پیامکِ صندوقِ ورودی، جدیدترین اول.
 *
 * برخلافِ [readSmsSenders] که فقط فرستنده‌های یکتا را می‌دهد، این‌جا **خودِ پیامک‌ها** لازم‌اند:
 * کاربر می‌خواهد پیامکی را که خودکار خوانده نشده دستی انتخاب و ثبت کند. همه‌چیز روی گوشی
 * می‌ماند و هیچ‌جا فرستاده نمی‌شود.
 */
suspend fun readSmsInbox(context: Context, limit: Int = 200): List<SmsInboxMessage> =
    withContext(Dispatchers.IO) {
        val result = mutableListOf<SmsInboxMessage>()
        runCatching {
            context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE),
                null,
                null,
                "${Telephony.Sms.DATE} DESC LIMIT $limit",
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
                val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
                while (cursor.moveToNext()) {
                    val body = cursor.getString(bodyIndex)?.trim().orEmpty()
                    if (body.isEmpty()) continue
                    result += SmsInboxMessage(
                        id = cursor.getLong(idIndex),
                        address = cursor.getString(addressIndex)?.trim().orEmpty(),
                        body = body,
                        dateMs = cursor.getLong(dateIndex),
                        parsed = BankSmsParser.parse(body),
                    )
                }
            }
        }
        result
    }
