package ir.sadteam.loancalc.server

import java.time.Instant

/**
 * لاگِ ساده‌ی ساختاریافته - هدفش این بود که وقتی کاربری گزارشِ باگ می‌ده، بشه از رو خروجیِ pm2
 * (`pm2 logs loan-calc-api`) دقیقاً فهمید کجا و چرا خطا خورده.
 *
 * ⚠️ **قاعده‌ی سفت‌وسخت: هیچ داده‌ی حساسی نباید تو لاگ بره.** یعنی:
 * - **کدِ OTP** هیچ‌وقت، تحتِ هیچ شرایطی
 * - **شماره‌ی موبایلِ کامل** هیچ‌وقت - همیشه با [maskPhone]
 * - **توکنِ ورود (JWT) و رسیدِ خرید** هیچ‌وقت - حداکثر ۶ کاراکترِ اولش با [maskToken]
 * - **مبالغ/جزئیاتِ مالیِ کاربر** (محتوای وام‌ها، تراکنش‌ها، بکاپ‌ها) هیچ‌وقت
 *
 * قبلاً تنها لاگِ سرور یه `println` بود که **شماره‌ی موبایلِ کامل** رو چاپ می‌کرد - همون چیزی که
 * این فایل اومده جلوش رو بگیره.
 */
object Log {
    private fun emit(level: String, event: String, message: String, fields: Array<out Pair<String, Any?>>) {
        val extras = fields.joinToString(" ") { (k, v) -> "$k=${v ?: "-"}" }
        println("${Instant.now()} [$level] $event | $message${if (extras.isEmpty()) "" else " | $extras"}")
    }

    fun info(event: String, message: String, vararg fields: Pair<String, Any?>) =
        emit("INFO", event, message, fields)

    fun warn(event: String, message: String, vararg fields: Pair<String, Any?>) =
        emit("WARN", event, message, fields)

    fun error(event: String, message: String, vararg fields: Pair<String, Any?>) =
        emit("ERROR", event, message, fields)
}

/** `09123456789` → `0912***6789` - برای پیگیریِ یه گزارشِ مشخص کافیه، ولی خودِ شماره لو نمی‌ره. */
fun maskPhone(phone: String): String =
    if (phone.length < 8) "***" else "${phone.take(4)}***${phone.takeLast(4)}"

/** فقط ۶ کاراکترِ اولِ توکن/رسید - برای تطبیق‌دادنِ دو لاگ با هم کافیه، برای سوءاستفاده نه. */
fun maskToken(token: String?): String =
    if (token.isNullOrEmpty()) "-" else "${token.take(6)}…(${token.length})"

/** `1.2.3.4` → `1.2.3.x` - برای تشخیصِ حمله کافیه بدونِ نگه‌داشتنِ IPِ کاملِ کاربر. */
fun maskIp(ip: String): String {
    val parts = ip.split(".")
    return if (parts.size == 4) "${parts[0]}.${parts[1]}.${parts[2]}.x" else "***"
}
