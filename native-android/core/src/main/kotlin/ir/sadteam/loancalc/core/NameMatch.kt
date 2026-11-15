package ir.sadteam.loancalc.core

/**
 * نرمال‌سازی و تطبیقِ نامِ فارسی - برای حدسِ خودکارِ طرفِ‌حساب موقعِ اتصالِ چک/وامِ قدیمی
 * (بدونِ `counterpartyId`) به یه طرفِ‌حسابِ موجود، طبقِ جوابِ کاربر به سوالِ ۱۰ی
 * design/MESSAGE-round4-to-design.md.
 *
 * ⚠️ فریمِ ۱۷b (ادغامِ خودکارِ دسته‌های تکراری) که قرار بود این منطق ازش دوباره‌استفاده بشه هنوز
 * ساخته نشده (بررسی شد، تو کد وجود نداره) - این تابع از صفر نوشته شد تا هم الان برای طرفِ‌حساب
 * هم بعداً موقعِ ساختِ ۱۷b قابلِ‌استفاده‌ی مجدد باشه.
 */

/** ي/ك عربی → ی/ک فارسی، نیم‌فاصله و فاصله‌های چندتایی → یه فاصله، حذفِ فاصله‌ی ابتدا/انتها. */
fun normalizePersianName(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    var s = raw.trim()
    s = s.replace('ي', 'ی').replace('ك', 'ک') // ي→ی، ك→ک
    s = s.replace('‌', ' ') // نیم‌فاصله
    s = s.replace(Regex("\\s+"), " ")
    return s.trim().lowercase()
}

/**
 * آیا دو نام به‌احتمالِ زیاد همون آدمن؟ برابریِ دقیق بعدِ نرمال‌سازی، یا یکی پیشوند/پسوندِ اون
 * یکیه (مثلاً «آقای رضایی» تو «رضایی» یا برعکس) - حداقلِ ۳ کاراکتر تا دو نامِ کوتاهِ بی‌ربط
 * تصادفی جور در نیان.
 */
fun namesLikelyMatch(a: String?, b: String?): Boolean {
    val na = normalizePersianName(a)
    val nb = normalizePersianName(b)
    if (na.isEmpty() || nb.isEmpty()) return false
    if (na == nb) return true
    if (na.length >= 3 && nb.length >= 3 && (na.contains(nb) || nb.contains(na))) return true
    return false
}

/**
 * از رو یه نامِ خام، نزدیک‌ترین طرفِ‌حسابِ موجود رو پیدا می‌کنه (اگه تطبیقِ قطعی باشه).
 * برای حدسِ خودکارِ چک/وامِ قدیمی استفاده می‌شه - `null` یعنی مبهم/بی‌نام، باید طرفِ‌حسابِ
 * موقتِ «نامشخص» بگیره.
 */
fun <T> guessCounterparty(rawName: String?, candidates: List<T>, nameOf: (T) -> String): T? {
    if (rawName.isNullOrBlank()) return null
    return candidates.firstOrNull { namesLikelyMatch(rawName, nameOf(it)) }
}
