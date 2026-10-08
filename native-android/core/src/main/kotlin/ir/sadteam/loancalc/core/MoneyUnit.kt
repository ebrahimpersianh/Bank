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
