package ir.sadteam.loancalc.ui.auth

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import android.widget.Toast
import java.security.MessageDigest

private const val TAG = "SmsRetrieverHash"
private const val HASH_TYPE = "SHA-256"
private const val NUM_HASHED_BYTES = 9
private const val NUM_BASE64_CHAR = 11

/**
 * محاسبه‌ی هشِ ۱۱کاراکتریِ مخصوصِ SMS Retriever API (پیاده‌سازیِ استانداردِ گوگل - همون الگوریتمِ
 * نمونه‌ی `AppSignatureHelper`ی رسمی). این هش به **بسته‌نام + گواهیِ امضای دقیقاً همون APKی که رو
 * گوشی نصبه** وابسته‌ست - یعنی بیلدِ دیباگ (امضاشده با `ci-debug.keystore`ی ثابتِ خودِ ریپو) و بیلدِ
 * ریلیزِ واقعی (امضاشده با کلیدِ ریلیزِ کافه‌بازار/مایکت) **دو هشِ متفاوت** دارن.
 *
 * چون این کلیدِ ریلیز رو سندباکس در دسترس نیست (نه تو ریپو، نه به‌عنوان یه فایل قابل‌خوندن از اینجا)،
 * این هش از *داخلِ خودِ اپِ نصب‌شده رو گوشیِ واقعی* محاسبه/لاگ می‌شه - رجوع کن به
 * `LoanCalcApplication.onCreate()`. کاربر باید هر بیلدی که واقعاً می‌خواد باهاش تست/منتشر کنه (فعلاً
 * حتی یه بیلدِ دیباگِ CI هم برای تستِ اولیه کافیه) رو نصب کنه، با
 * `adb logcat -s SmsRetrieverHash` (یا فیلترکردنِ لاگ‌کت رو همین تگ) هشِ چاپ‌شده رو بگیره، و دقیقاً
 * همون رشته رو (بدونِ فاصله‌ی اضافه) به یه خطِ جدیدِ آخرِ متنِ پترنِ ملی‌پیامک اضافه کنه - رجوع کن به
 * CLAUDE.md برای جزئیاتِ کامل.
 *
 * چون کاربرِ این پروژه معمولاً به adb/کامپیوتر دسترسی نداره، علاوه بر لاگ، هش رو خودکار تو
 * کلیپ‌بورد هم کپی می‌کنه و یه Toast نشون می‌ده - یعنی فقط با باز کردنِ اپ رو گوشی (بدونِ نیاز به
 * لاگ‌کت) می‌شه هش رو گرفت (پیست کرد جای دیگه).
 */
object SmsRetrieverHash {
    fun logForDebugging(context: Context) {
        runCatching {
            val packageName = context.packageName
            @Suppress("DEPRECATION")
            val packageInfo = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            @Suppress("DEPRECATION")
            val signatures = packageInfo.signatures ?: return
            for (signature in signatures) {
                val hash = hash(packageName, signature.toCharsString())
                if (hash != null) {
                    Log.i(TAG, "هشِ SMS Retriever برای این نصب: $hash")
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    clipboard?.setPrimaryClip(ClipData.newPlainText("SMS Retriever Hash", hash))
                    Toast.makeText(context, "هشِ پیامک کپی شد: $hash", Toast.LENGTH_LONG).show()
                }
            }
        }.onFailure { e -> Log.e(TAG, "محاسبه‌ی هش شکست خورد", e) }
    }

    private fun hash(packageName: String, signature: String): String? {
        val appInfo = "$packageName $signature"
        return runCatching {
            val digest = MessageDigest.getInstance(HASH_TYPE)
            digest.update(appInfo.toByteArray(Charsets.UTF_8))
            val hashSignature = digest.digest().copyOfRange(0, NUM_HASHED_BYTES)
            Base64.encodeToString(hashSignature, Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE)
                .substring(0, NUM_BASE64_CHAR)
        }.getOrNull()
    }
}
