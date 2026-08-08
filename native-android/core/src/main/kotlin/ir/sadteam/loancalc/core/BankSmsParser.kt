package ir.sadteam.loancalc.core

/** نتیجه‌ی پارسِ یه پیامکِ بانکی - [amountRial] همیشه به ریال (نه تومان) نرمال شده، [cardSuffix]
 * (اگه پیدا بشه) ۴ رقمِ آخرِ کارت برای تطبیق با [ir.sadteam.loancalc.data.db.AccountEntity.cardNumber]. */
data class ParsedBankSms(
    val amountRial: Double,
    val type: TransactionType,
    val cardSuffix: String?,
)

/**
 * پارسِ متنِ پیامکِ بانکی به یه تراکنشِ ساختاریافته - رجوع کن به CLAUDE.md، «خوندنِ خودکارِ پیامکِ
 * بانکی». چون فرمتِ پیامکِ هر بانکِ ایرانی فرق داره، عمداً بر اساسِ کلیدواژه‌های رایج (نه فرمتِ دقیقِ
 * یه بانکِ خاص) کار می‌کنه - قابلِ‌اعتمادتر از یه‌سری regexِ خیلی سخت‌گیرانه، ولی همیشه هم درست
 * تشخیص نمی‌ده؛ به همین خاطر تراکنشِ ساخته‌شده باید همیشه قابلِ‌حذف/ویرایشِ دستی بمونه (رجوع کن به
 * BankSmsReceiver - هیچ‌وقت داده رو جایی نمی‌فرسته، فقط محلی یه تراکنش می‌سازه).
 */
object BankSmsParser {
    // مبلغ: یه رشته‌ی رقمی (با یا بدون جداکننده‌ی هزارگان) بلافاصله قبل از «ریال»/«تومان». حداقل
    // ۴ رقم چون مبلغ‌های بانکی واقعی همیشه حداقل هزارتومانی‌ان - جلوگیری از قاپیدنِ اعدادِ کوچیکِ
    // بی‌ربط (مثلاً شماره‌ی پیگیری).
    private val amountRegex = Regex("([\\d,٬۰-۹]{4,})\\s*(ریال|ريال|تومان)")
    private val cardSuffixRegex = Regex("(?:\\*+|منتهی به|کارت)\\D{0,6}(\\d{4})(?!\\d)")
    private val depositKeywords = listOf("واریز", "بستانکار", "افزایش موجودی")
    private val withdrawalKeywords = listOf("برداشت", "خرید", "بدهکار", "کاهش موجودی", "انتقال وجه", "پرداخت")

    fun parse(body: String): ParsedBankSms? {
        val amountMatch = amountRegex.find(body) ?: return null
        val digitsOnly = toEnDigits(amountMatch.groupValues[1]).replace(",", "").replace("٬", "")
        val amount = digitsOnly.toDoubleOrNull() ?: return null
        if (amount <= 0) return null
        val amountRial = if (amountMatch.groupValues[2] == "تومان") amount * 10 else amount

        val type = when {
            depositKeywords.any { body.contains(it) } -> TransactionType.DEPOSIT
            withdrawalKeywords.any { body.contains(it) } -> TransactionType.WITHDRAWAL
            else -> return null
        }

        val cardSuffix = cardSuffixRegex.find(body)?.groupValues?.get(1)?.let { toEnDigits(it) }
        return ParsedBankSms(amountRial, type, cardSuffix)
    }
}

/**
 * نرمال‌سازیِ فرستنده‌ی پیامک برای تطبیق با [ir.sadteam.loancalc.data.db.AccountEntity.smsSender]:
 * ارقامِ فارسی به لاتین، حذفِ فاصله/خط‌تیره/پرانتز، حروف بزرگ. پیش‌شماره‌ی ایران هم یکدست می‌شه
 * (`+98…`/`0098…`/`98…` → `0…`) چون یه سرشماره‌ی یکسان بعضی گوشی‌ها با پیش‌شماره و بعضی بدونش
 * نشون داده می‌شه و کاربر هر کدوم رو ببینه همون رو وارد می‌کنه.
 */
fun normalizeSmsSender(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    var s = toEnDigits(raw).uppercase().filter { it.isLetterOrDigit() || it == '+' }
    s = s.removePrefix("+")
    if (s.startsWith("0098")) s = s.removePrefix("0098")
    if (s.length > 10 && s.startsWith("98")) s = s.removePrefix("98")
    if (s.isNotEmpty() && s.first().isDigit() && !s.startsWith("0")) s = "0$s"
    return s
}

/** آیا فرستنده‌ی این پیامک همونیه که کاربر برای این حساب ثبت کرده؟ برای اینکه یه سرشماره‌ی
 * ۱۰-۱۴رقمی که اپراتورها گاهی با چند رقمِ اضافه تحویل می‌دن هم بگیره، تطبیقِ «یکی پسوندِ اون یکی
 * باشه» هم قبوله (با حداقلِ ۴ کاراکتر، تا دو سرشماره‌ی بی‌ربط تصادفی جور در نیان). */
fun smsSenderMatches(accountSender: String?, incoming: String?): Boolean {
    val a = normalizeSmsSender(accountSender)
    val b = normalizeSmsSender(incoming)
    if (a.isEmpty() || b.isEmpty()) return false
    if (a == b) return true
    val min = minOf(a.length, b.length)
    return min >= 4 && (a.endsWith(b) || b.endsWith(a))
}
