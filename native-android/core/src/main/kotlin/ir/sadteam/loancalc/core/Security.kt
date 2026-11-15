package ir.sadteam.loancalc.core

import java.security.MessageDigest
import java.util.Locale

/** برای مقایسه‌ی امن PIN قفل امنیتی - PIN خام هیچ‌وقت ذخیره نمی‌شه، فقط هش SHA-256 (رجوع کن به
 * SecurityPrefs تو :data). */
fun sha256Hex(input: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { String.format(Locale.US, "%02x", it) }
}
