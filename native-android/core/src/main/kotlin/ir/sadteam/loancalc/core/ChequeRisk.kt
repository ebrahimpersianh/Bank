package ir.sadteam.loancalc.core

/**
 * امتیازِ ریسکِ برگشتِ یه طرفِ‌حساب - از رو تاریخچه‌ی پاس/برگشتِ چک‌های لینک‌شده به همون
 * طرفِ‌حساب (جوابِ سوالِ ۲ی design/MESSAGE-round4-to-design.md: فیچرِ واقعیه نه تبلیغاتی).
 *
 * ⚠️ **فرمولِ موقته.** کاربر گفته «طرحِ دقیقِ فرمولِ امتیاز جدا مطرح می‌شود» - این نسخه‌ی ساده
 * (نسبتِ چکِ پاس‌شده به مجموعِ پاس+برگشت، روی ۱۰۰) تا رسیدنِ فرمولِ نهایی جای‌گیره. همه‌جا فقط
 * از همین تابع صدا زده بشه تا جایگزینیِ فرمول یه‌جا باشه، نه پخش تو چند فایل.
 */
data class ChequeRiskScore(val score: Int, val passedCount: Int, val bouncedCount: Int) {
    /** تعدادِ کلِ چکِ وضع‌شده (پاس یا برگشت). صفر یعنی هنوز سابقه‌ای نیست - نمایشِ امتیاز بی‌معنیه. */
    val hasHistory: Boolean get() = passedCount + bouncedCount > 0
}

fun computeChequeRiskScore(passedCount: Int, bouncedCount: Int): ChequeRiskScore {
    val total = passedCount + bouncedCount
    val score = if (total == 0) 100 else ((passedCount.toDouble() / total) * 100).toInt()
    return ChequeRiskScore(score, passedCount, bouncedCount)
}
