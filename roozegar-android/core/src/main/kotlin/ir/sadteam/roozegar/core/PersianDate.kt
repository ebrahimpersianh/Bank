package ir.sadteam.roozegar.core

/** یه روز تو تقویم جلالی (شمسی). */
data class PersianDate(val y: Int, val m: Int, val d: Int) : Comparable<PersianDate> {
    override fun compareTo(other: PersianDate): Int =
        compareValuesBy(this, other, { it.y }, { it.m }, { it.d })
}

/** تاریخ میلادی ساده - از `java.time` عمداً استفاده نشده چون فقط از Android API 26+ در دسترسه
 * (بدون core library desugaring) و minSdk این پروژه ۲۴ هست. */
data class GregorianDate(val y: Int, val m: Int, val d: Int)

object PersianNames {
    val months = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
    )

    /** روزهای هفته با شمارش ایرانی: شنبه=۰ تا جمعه=۶ (هماهنگ با
     * [JalaliCalendar.dayOfWeekSaturdayFirst]). */
    val weekdays = listOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")

    /** حرف اول روزهای هفته برای سطر بالای گرید تقویم/ویجت. */
    val weekdayInitials = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

    val gregorianMonths = listOf(
        "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن",
        "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر",
    )

    val hijriMonths = listOf(
        "محرم", "صفر", "ربیع‌الاول", "ربیع‌الثانی", "جمادی‌الاول", "جمادی‌الثانی",
        "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه",
    )
}

private const val PERSIAN_ZERO = '۰'

/** تبدیل ارقام لاتین یه رشته به ارقام فارسی («123» → «۱۲۳») - بقیه‌ی کاراکترها دست نمی‌خورن. */
fun String.toPersianDigits(): String = buildString(length) {
    for (ch in this@toPersianDigits) append(if (ch in '0'..'9') PERSIAN_ZERO + (ch - '0') else ch)
}

fun Int.toPersianDigits(): String = toString().toPersianDigits()

/** «۳۰ تیر ۱۴۰۵» یا با [withWeekday] «دوشنبه ۳۰ تیر ۱۴۰۵». */
fun PersianDate.format(withWeekday: Boolean = false): String {
    val base = "${d.toPersianDigits()} ${PersianNames.months[m - 1]} ${y.toPersianDigits()}"
    return if (withWeekday) {
        "${PersianNames.weekdays[JalaliCalendar.dayOfWeekSaturdayFirst(this)]} $base"
    } else {
        base
    }
}
