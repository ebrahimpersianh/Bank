package ir.sadteam.loancalc.ui.auth

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

/**
 * پرکردنِ خودکارِ فیلدِ کدِ تایید با Google **SMS Retriever API** - کاملاً بی‌صدا، بدونِ نیاز به
 * مجوزِ READ_SMS و **بدونِ هیچ دیالوگِ سیستمی/تاییدِ دستیِ کاربر** (خواسته‌ی صریحِ کاربر: «این خودش
 * بدون هیچ اجازه‌ای کد که اومد وارد کرد»؛ نسخه‌ی قبلیِ این فایل از SMS **User Consent** API استفاده
 * می‌کرد که یه دیالوگِ «اجازه می‌دی بخونم؟» نشون می‌داد - اون رفتار عمداً حذف شد).
 *
 * **محدودیتِ مهمِ گوگل (باید سمتِ سرور/پترنِ پیامک هم اعمال بشه)**: برخلافِ User Consent API که با
 * هر متنِ پیامکی کار می‌کرد، SMS Retriever فقط پیامکی رو می‌گیره که دقیقاً این فرمت رو داشته باشه:
 * یه خطِ آخرِ جدا («\n» قبلش) شاملِ یه هش ۱۱کاراکتریِ مخصوصِ همین اپ (به بسته‌نام + گواهیِ امضا
 * وابسته‌ست، پس نسخه‌ی دیباگ/ریلیز هشِ متفاوت دارن). تا این هش به انتهای متنِ پترنِ ملی‌پیامک اضافه
 * نشه، این تابع هیچ‌وقت هیچ پیامکی نمی‌گیره (نه کرش، نه خطا - فقط هیچ‌وقت `onCodeReceived` صدا زده
 * نمی‌شه؛ کاربر همیشه می‌تونه دستی هم کد رو تو باکس‌ها تایپ کنه، پس هیچ مسیری بسته نمی‌شه). رجوع کن
 * به `core/AppSignatureHelper.kt` برای گرفتنِ همین هش، و CLAUDE.md برای مراحلِ دقیقِ هماهنگی با پترن.
 *
 * [active] باید فقط وقتی true بشه که کاربر تو مرحله‌ی وارد کردنِ کد تاییده (نه از اول باز شدنِ
 * صفحه‌ی ورود) - وگرنه گوش‌دادن به پیامک زودتر از موقع (قبل از این‌که کاربر اصلاً درخواستِ کد داده
 * باشه) شروع می‌شه.
 */
@Composable
fun SmsRetrieverEffect(active: Boolean, onCodeReceived: (String) -> Unit) {
    val context = LocalContext.current
    val onCodeReceivedState = rememberUpdatedState(onCodeReceived)

    DisposableEffect(active) {
        if (!active) return@DisposableEffect onDispose {}

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action != SmsRetriever.SMS_RETRIEVED_ACTION) return
                val status = intent.getParcelableSafe<Status>(SmsRetriever.EXTRA_STATUS) ?: return
                if (status.statusCode != CommonStatusCodes.SUCCESS) return
                val message = intent.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE) ?: return
                Regex("\\d{5}").find(message)?.value?.let { onCodeReceivedState.value(it) }
            }
        }

        val filter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, SmsRetriever.SEND_PERMISSION, null, ContextCompat.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter, SmsRetriever.SEND_PERMISSION, null)
        }

        SmsRetriever.getClient(context).startSmsRetriever()

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
