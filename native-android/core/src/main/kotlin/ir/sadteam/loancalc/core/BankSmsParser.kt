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
