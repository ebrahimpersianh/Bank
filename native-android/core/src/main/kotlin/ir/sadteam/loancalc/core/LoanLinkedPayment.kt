package ir.sadteam.loancalc.core

/**
 * شناسه‌ی پیوندِ پرداختِ قسطِ وام: `"<loanId>:<m1,m2,...>"` — همان قالبی که
 * `LoanDetailScreen.commitPayment` موقعِ ثبتِ پرداخت می‌سازد و
 * `AccountTransactionEntity.sourceId` نگه می‌دارد (رجوع کن به کامنتِ sourceId همان
 * انتیتی). پرداختِ تکی `"<loanId>:<m>"` است و پرداختِ گروهی چند ماه را با کاما کنار
 * هم می‌گذارد (`"<loanId>:<m1,m2>"`).
 *
 * تجزیه‌ی **دقیقِ** این قالب — نه تطابقِ پیشوندیِ رشته‌ای. این تابع قلبِ اصلاحِ باگِ
 * ۲۳ نوامبر ۲۰۲۶ است (حذفِ وام با «پاک شود» یک تراکنشِ نامربوط را هم پاک کرد):
 * حذفِ بازپرداختِ وام فقط باید تراکنشی را بزند که (۱) sourceType آن `"loan"` باشد و
 * (۲) شناسه‌ی وامِ تجزیه‌شده از sourceId آن **دقیقاً** با شناسه‌ی همین وام برابر باشد
 * و (۳) همه‌ی ماه‌هایش جزوِ ماه‌های پرداخت‌شده‌ی همین وام باشند. با تطابقِ پیشوندی
 * (`startsWith("$id:")`) هر تراکنشی که شناسه‌اش با همان پیشوند شروع شود در خطر بود؛
 * با تجزیه‌ی دقیق، تراکنشِ طلب/بدهی (`sourceId` بدونِ `:`)، انتقال، یا وامِ دیگر
 * هیچ‌وقت جور نمی‌شود.
 *
 * برمی‌گرداند `(loanId, months)` یا `null` اگر قالب جور نباشد.
 */
private val LOAN_PAYMENT_SOURCE_ID_RE = Regex("^(\\d+):(\\d+(?:,\\d+)*)$")

fun parseLoanPaymentSourceId(sourceId: String?): Pair<Long, List<Int>>? {
    if (sourceId.isNullOrBlank()) return null
    val match = LOAN_PAYMENT_SOURCE_ID_RE.matchEntire(sourceId.trim()) ?: return null
    val loanId = match.groupValues[1].toLongOrNull() ?: return null
    val months = match.groupValues[2].split(',').mapNotNull { it.toIntOrNull() }
    if (months.isEmpty()) return null
    return loanId to months
}
