package ir.sadteam.loancalc.core

/**
 * 💱 واحدِ **نمایشِ** مبلغ (۱۶ مهر): تومان (پیش‌فرض) یا ریال. دیتابیس همیشه ریال است؛ این فقط لبه‌ی نمایش و
 * ورودی را عوض می‌کند. خودِ برنامه‌ی اندروید این را از تنظیمات می‌خواند و این‌جا می‌نویسد تا ماژولِ خالصِ
 * `:core` (مثلِ پیشنهادهای هوشمند) هم هم‌واحد بماند.
 */
object CoreMoneyUnit {
    @Volatile var rial: Boolean = false

    /** ریال ÷ این = مبلغ در واحدِ نمایش. */
    val div: Long get() = if (rial) 1L else 10L

    val label: String get() = if (rial) "ریال" else "تومان"
}

private val MONEY_IN_TEXT = Regex("([0-9۰-۹][0-9۰-۹٬,]*)(\s+)(تومان|ریال)")

/**
 * ۱۱ آبان (تست): متنِ پیام‌ها لحظه‌ی ساختن با واحدِ همان موقع نوشته می‌شود («۶۰٬۰۰۰ تومان»)؛ بعد از
 * رفتن به ریال، پیام‌های قدیمی هنوز تومان می‌گفتند و یک صفحه دو واحد داشت. این تابع هنگامِ **نمایش**
 * هر «عدد + تومان/ریال» را به واحدِ فعلی برمی‌گرداند. حروف («پنج میلیون تومان») دست نمی‌خورد.
 */
fun localizeMoneyUnits(text: String, rial: Boolean = CoreMoneyUnit.rial): String {
    if (!text.contains("تومان") && !text.contains("ریال")) return text
    return MONEY_IN_TEXT.replace(text) { m ->
        val unit = m.groupValues[3]
        if ((unit == "ریال") == rial) return@replace m.value
        val digits = m.groupValues[1].map { c -> if (c in '۰'..'۹') '0' + (c - '۰') else c }.filter { it.isDigit() }.joinToString("")
        val n = digits.toLongOrNull() ?: return@replace m.value
        val converted = if (rial) n * 10 else n / 10
        val persian = "%,d".format(java.util.Locale.US, converted).replace(',', '٬').map { c -> if (c in '0'..'9') '۰' + (c - '0') else c }.joinToString("")
        persian + m.groupValues[2] + (if (rial) "ریال" else "تومان")
    }
}
