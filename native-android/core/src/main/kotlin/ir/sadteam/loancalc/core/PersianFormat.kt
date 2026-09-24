package ir.sadteam.loancalc.core

import java.util.Locale
import kotlin.math.floor
import kotlin.math.roundToLong

private val faDigits = listOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
private val faToEnDigits = ('۰'..'۹').zip('0'..'9').toMap()
private val arToEnDigits = ('٠'..'٩').zip('0'..'9').toMap()

/** پورت toFa تو www/index.html: تبدیل ارقام انگلیسی هر رشته به فارسی */
fun toFa(value: Any): String {
    val s = value.toString()
    val sb = StringBuilder(s.length)
    for (c in s) {
        if (c in '0'..'9') sb.append(faDigits[c - '0']) else sb.append(c)
    }
    return sb.toString()
}

/** پورت toEnDigits تو www/index.html: ارقام فارسی/عربی رو (از هر کیبوردی اومده باشن) به انگلیسی تبدیل می‌کنه */
fun toEnDigits(value: String): String = buildString {
    for (c in value) append(faToEnDigits[c] ?: arToEnDigits[c] ?: c)
}

/** پورت cleanNum تو www/index.html: قبل از parse کردن ورودی عددی کاربر همیشه از این استفاده کن،
 * وگرنه تایپ‌کردن با کیبورد فارسی (ارقام ۰-۹) به یه عدد صفر/خالی parse می‌شه. */
fun cleanNum(value: String): String = toEnDigits(value).filter { it in '0'..'9' }

/** پورت cleanNumDecimal تو www/index.html: مثل cleanNum ولی ممیز اعشاری رو هم نگه می‌داره */
fun cleanNumDecimal(value: String): String = toEnDigits(value).filter { it in '0'..'9' || it == '.' }

/**
 * جداکننده‌ی هزارگانِ **فارسی** (U+066C) - نه کاما.
 *
 * سیستمِ طراحی صریحاً می‌گه «عددها فارسی با جداکننده‌ی `٬`»، و هر ۷۱۶ عددِ فایلِ فریم‌ها هم
 * همینه. قبلاً کامای لاتین بود و کنارِ ارقامِ فارسی ناجور می‌نشست.
 */
const val FA_THOUSANDS_SEPARATOR = '٬'

/** عدد رو با جداکننده‌ی هزارگانِ فارسی و ارقامِ فارسی نشون می‌ده. */
fun fmt(n: Double): String {
    val rounded = n.roundToLong()
    val grouped = String.format(Locale.US, "%,d", rounded)
    return toFa(grouped).replace(',', FA_THOUSANDS_SEPARATOR)
}

private val ordinalDays = listOf(
    "", "اول", "دوم", "سوم", "چهارم", "پنجم", "ششم", "هفتم", "هشتم", "نهم", "دهم",
    "یازدهم", "دوازدهم", "سیزدهم", "چهاردهم", "پانزدهم", "شانزدهم", "هفدهم", "هجدهم", "نوزدهم", "بیستم",
    "بیست و یکم", "بیست و دوم", "بیست و سوم", "بیست و چهارم", "بیست و پنجم", "بیست و ششم",
    "بیست و هفتم", "بیست و هشتم", "بیست و نهم", "سی‌ام", "سی و یکم",
)

fun ordinalFa(n: Int): String = ordinalDays.getOrNull(n) ?: toFa(n)

private val teens = listOf("ده", "یازده", "دوازده", "سیزده", "چهارده", "پانزده", "شانزده", "هفده", "هجده", "نوزده")
private val tens = listOf("", "", "بیست", "سی", "چهل", "پنجاه", "شصت", "هفتاد", "هشتاد", "نود")
private val hundreds = listOf("", "صد", "دویست", "سیصد", "چهارصد", "پانصد", "ششصد", "هفتصد", "هشتصد", "نهصد")
private val ones = listOf("", "یک", "دو", "سه", "چهار", "پنج", "شش", "هفت", "هشت", "نه")

private fun threeDigitsToWords(n: Int): String {
    if (n == 0) return ""
    val h = n / 100
    val r = n % 100
    val parts = mutableListOf<String>()
    if (h > 0) parts.add(hundreds[h])
    if (r in 10..19) {
        parts.add(teens[r - 10])
    } else {
        val t = r / 10
        val o = r % 10
        if (t > 0) parts.add(tens[t])
        if (o > 0) parts.add(ones[o])
    }
    return parts.joinToString(" و ")
}

/** پورت numberToWordsFa تو www/index.html (تا مقیاس تریلیون) */
fun numberToWordsFa(input: Double): String {
    var num = floor(input + 0.5).toLong()
    if (num == 0L) return "صفر"
    val scales = listOf("", "هزار", "میلیون", "میلیارد", "تریلیون")
    val groups = mutableListOf<Int>()
    while (num > 0) {
        groups.add((num % 1000).toInt())
        num /= 1000
    }
    val parts = mutableListOf<String>()
    for (idx in groups.indices.reversed()) {
        val g = groups[idx]
        if (g > 0) {
            val words = threeDigitsToWords(g)
            parts.add(if (scales[idx].isNotEmpty()) "$words ${scales[idx]}" else words)
        }
    }
    return parts.joinToString(" و ")
}
