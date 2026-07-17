package ir.sadteam.loancalc.ui.auth

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

/**
 * پرکردن خودکار فیلدِ کد تایید با Google SMS User Consent API - بدون نیاز به مجوز READ_SMS
 * (که اپ‌های نصب‌شده رو ناامن/مشکوک نشون می‌ده). وقتی پیامکِ ورودی رو تشخیص بده که یه کد داره
 * (هر متنی، از هر فرستنده‌ای - `startSmsUserConsent(null)`)، یه دیالوگِ سیستمی («اجازه می‌دی وام
 * من این پیامک رو بخونه؟») نشون می‌ده؛ فقط بعد از تاییدِ دستیِ کاربر، متنِ کامل پیامک در اختیار
 * اپ قرار می‌گیره - این یعنی اپ هیچ‌وقت بدونِ اطلاعِ کاربر پیامک نمی‌خونه.
 *
 * [active] باید فقط وقتی true بشه که کاربر تو مرحله‌ی وارد کردنِ کد تاییده (نه از اول باز شدنِ
 * صفحه‌ی ورود) - وگرنه دیالوگِ سیستمی زودتر از موقع (قبل از این‌که کاربر اصلاً درخواستِ کد داده
 * باشه) ظاهر می‌شه.
 */
@Composable
fun SmsUserConsentEffect(active: Boolean, onCodeReceived: (String) -> Unit) {
    val context = LocalContext.current
    val onCodeReceivedState = rememberUpdatedState(onCodeReceived)

    val consentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val message = result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE) ?: return@rememberLauncherForActivityResult
            Regex("\\d{5}").find(message)?.value?.let { onCodeReceivedState.value(it) }
        }
    }

    DisposableEffect(active) {
        if (!active) return@DisposableEffect onDispose {}

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action != SmsRetriever.SMS_CONSENT_ACTION) return
                val status = intent.getParcelableSafe<Status>(SmsRetriever.EXTRA_STATUS) ?: return
                if (status.statusCode == CommonStatusCodes.SUCCESS) {
                    val consentIntent = intent.getParcelableSafe<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                    if (consentIntent != null) {
                        runCatching {
                            consentLauncher.launch(IntentSenderRequest.Builder(consentIntent).build())
                        }
                    }
                }
            }
        }

        val filter = IntentFilter(SmsRetriever.SMS_CONSENT_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, SmsRetriever.SEND_PERMISSION, null, ContextCompat.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter, SmsRetriever.SEND_PERMISSION, null)
        }

        SmsRetriever.getClient(context).startSmsUserConsent(null)

        onDispose { runCatching { context.unregisterReceiver(receiver) } }
    }
}

/** `Intent.getParcelableExtra` قدیمی رو Android 13+ deprecated کرده (بدونِ کلاس صریح) - این یه
 * wrapper کوچیکه که هر دو مسیر رو پوشش می‌ده، بدون نیاز به `@Suppress` رو خودِ کدِ صدازننده. */
private inline fun <reified T : android.os.Parcelable> Intent.getParcelableSafe(key: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(key)
    }
