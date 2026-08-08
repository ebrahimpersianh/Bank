package ir.sadteam.loancalc.ui.account

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
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
 * دیالوگِ انتخابِ فرستنده. خودش مجوزِ `READ_SMS` رو (فقط همین لحظه) می‌گیره؛ اگه کاربر رد کنه، یه
 * پیامِ ساده می‌ده و تایپِ دستی همچنان راهِ کار می‌مونه.
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
    var senders by remember { mutableStateOf<List<SmsSenderInfo>?>(null) }
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
        if (permissionGranted) senders = readSmsSenders(context)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("انتخاب از پیامک‌ها") },
        text = {
            Column {
                when {
                    permissionDenied -> Text(
                        "بدونِ اجازه‌ی خوندنِ پیامک نمی‌شه لیست رو نشون داد. می‌تونی شماره رو دستی هم وارد کنی.",
                        color = AppMuted,
                        fontSize = 12.sp,
                    )
                    senders == null -> Text("در حالِ خوندنِ پیامک‌ها…", color = AppMuted, fontSize = 12.sp)
                    senders!!.isEmpty() -> Text("پیامکی تو صندوقِ گوشیت پیدا نشد", color = AppMuted, fontSize = 12.sp)
                    else -> {
                        Text(
                            "فرستنده‌ی پیامکِ بانکت رو انتخاب کن (اونایی که شبیهِ پیامکِ بانکی‌ان اول لیستن)",
                            color = AppMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 380.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            items(senders!!, key = { it.address }) { sender ->
                                AppCard {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .pressScaleClickable(scale = 0.99f) { onPick(sender.address) },
                                    ) {
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Ltr {
                                                Text(
                                                    sender.address,
                                                    color = AppText,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                )
                                            }
                                            if (sender.looksBanky) {
                                                Text(
                                                    "بانکی",
                                                    color = AppPrimary,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(start = 8.dp),
                                                )
                                            }
                                        }
                                        Text(
                                            sender.sample,
                                            color = AppMuted,
                                            fontSize = 11.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(top = 2.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("بستن") }
        },
    )
}
