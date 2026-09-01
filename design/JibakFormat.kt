package ir.sadteam.loancalc.ui.jibak

/**
 * قاعده‌ی عدد — از بخشِ ۲ فایلِ سیستمِ طراحی:
 * ارقام همیشه فارسی و با جداکنندهٔ «٬». مبلغ‌ها بدون واحد در ردیف‌ها؛
 * «تومان» فقط در کارتِ خلاصه. عددِ منفی با «−» می‌آید نه پرانتز.
 */

private val faDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

/** U+066C — جداکننده‌ی هزارگانِ فارسی. نه کاما، نه نقطه. */
const val FA_GROUP_SEPARATOR = '٬'

/** U+2212 — علامتِ منها. نه hyphen، نه پرانتز. */
const val FA_MINUS = '−'

fun Int.toFa(): String = toLong().toFa()
fun Long.toFa(): String = buildString {
    val negative = this@toFa < 0
    val digits = kotlin.math.abs(this@toFa).toString()
    if (negative) append(FA_MINUS)
    digits.forEach { append(faDigits[it - '0']) }
}

/** ۸۴۰۰۰۰ → «۸۴۰٬۰۰۰» · ‎-۸۴۰۰۰۰ → «−۸۴۰٬۰۰۰» */
fun Long.toFaMoney(): String {
    val negative = this < 0
    val digits = kotlin.math.abs(this).toString()
    val grouped = digits.reversed().chunked(3).joinToString(FA_GROUP_SEPARATOR.toString()).reversed()
    val fa = buildString { grouped.forEach { append(if (it.isDigit()) faDigits[it - '0'] else it) } }
    return if (negative) "$FA_MINUS$fa" else fa
}

fun Int.toFaMoney(): String = toLong().toFaMoney()

/** مبلغِ ردیفِ فهرست — با علامتِ + برای درآمد، − برای خرج. */
fun Long.toFaSignedMoney(): String =
    if (this >= 0) "+${kotlin.math.abs(this).toFaMoney()}" else toFaMoney()

/** ۶۵ → «۶۵٪» */
fun Int.toFaPercent(): String = "${toFa()}٪"

/** ۹۲۰۰۰۰۰ → «۹٫۲M» — فقط برای مرکزِ حلقه، جایی که جا نیست. */
fun Long.toFaCompact(): String {
    val m = this / 1_000_000.0
    val s = String.format("%.1f", m).replace('.', '٫')
    return buildString { s.forEach { append(if (it.isDigit()) faDigits[it - '0'] else it) } } + "M"
}

/** حالتِ حریمِ خصوصی: همیشه پنج نقطه، مستقل از تعدادِ رقم. */
const val MASKED_AMOUNT = "•••••"

fun Long.toFaMoneyMasked(hidden: Boolean): String =
    if (hidden) MASKED_AMOUNT else toFaMoney()
