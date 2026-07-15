package ir.sadteam.loancalc.core

/** پورت مفهومی «امور چک» اپ رقیب (VAMMAN) - دریافتی (چکی که به ما داده شده) در برابر پرداختی
 * (چکی که خودمون صادر کردیم). */
enum class ChequeType(val label: String) {
    RECEIVED("دریافتی"),
    PAID("پرداختی"),
}

/** وضعیت چک - پیش‌فرض «وضع‌نشده»، بعداً به یکی از سه حالت دیگه تغییر می‌کنه. */
enum class ChequeStatus(val label: String) {
    PENDING("وضع‌نشده"),
    PASSED("پاس‌شده"),
    BOUNCED("برگشت‌خورده"),
    REFUNDED("مسترد شده"),
}
