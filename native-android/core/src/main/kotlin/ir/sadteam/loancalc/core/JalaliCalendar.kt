package ir.sadteam.loancalc.core

import java.util.Calendar

/** تاریخ میلادی ساده - از `java.time` عمداً استفاده نشده چون اون فقط از Android API 26+ در دسترسه
 * (بدون core library desugaring) و minSdk این پروژه ۲۴ هست. */
data class GregorianDate(val y: Int, val m: Int, val d: Int)

/**
 * تبدیل واقعی جلالی↔میلادی (الگوریتم دقیق نجومی jalaali-js/Borkowski، بر پایه‌ی جدول واقعی
 * سال‌های کبیسه - نه یه چرخه‌ی ثابت ساده). برخلاف [PersianCalendar] بالا که یه تقویم ساده‌شده‌ست
 * (فقط برای جمع‌زدن فاصله‌ی روزهای بین اقساط از رو یه startDate دلخواه، نه گرفتن «امروزِ» واقعی)،
 * این یکی برای یادآوری سررسید لازمه که واقعاً بدونیم امروز چه تاریخ شمسی‌ایه. الگوریتم با ده‌ها
 * هزار تاریخ تصادفی و چند بازه‌ی کامل روزانه (شامل مرزهای اسفند ۲۹/۳۰ برای چندین سال) در برابر
 * کتابخونه‌ی پایتون jdatetime تایید شده (نمونه‌هاش تو JalaliCalendarTest.kt) - دستکاریش نکن مگر با
 * همون سطح تست.
 */
object JalaliCalendar {
    private val breaks = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178,
    )

    private data class JalCal(val leap: Int, val gy: Int, val march: Int)

    private fun jalCal(jy: Int): JalCal {
        val gy = jy + 621
        var leapJ = -14
        var jp = breaks[0]
        var jump = 0
        for (i in 1 until breaks.size) {
            val jm = breaks[i]
            jump = jm - jp
            if (jy < jm) break
            leapJ = leapJ + jump / 33 * 8 + jump % 33 / 4
            jp = jm
        }
        var n = jy - jp
        leapJ = leapJ + n / 33 * 8 + (n % 33 + 3) / 4
        if (jump % 33 == 4 && jump - n == 4) leapJ += 1
        val leapG = gy / 4 - (gy / 100 + 1) * 3 / 4 - 150
        val march = 20 + leapJ - leapG
        if (jump - n < 6) n = n - jump + jump / 33 * 33
        var leap = (n + 1) % 33 - 1
        leap %= 4
        if (leap == -1) leap = 4
        return JalCal(leap, gy, march)
    }

    private fun g2d(gy: Int, gm: Int, gd: Int): Int {
        val d = (gy + (gm - 8) / 6 + 100100) * 1461 / 4 + (153 * ((gm + 9) % 12) + 2) / 5 + gd - 34840408
        val x = (gy + 100100 + (gm - 8) / 6) / 100 * 3 / 4
        return d - x + 752
    }

    private data class G(val gy: Int, val gm: Int, val gd: Int)

    private fun d2g(jdn: Int): G {
        var j = 4 * jdn + 139361631
        j += (4 * jdn + 183187720) / 146097 * 3 / 4 * 4 - 3908
        val i = j % 1461 / 4 * 5 + 308
        val gd = i % 153 / 5 + 1
        val gm = i / 153 % 12 + 1
        val gy = j / 1461 - 100100 + (8 - gm) / 6
        return G(gy, gm, gd)
    }

    private fun j2d(jy: Int, jm: Int, jd: Int): Int {
        val r = jalCal(jy)
        return g2d(r.gy, 3, r.march) + (jm - 1) * 31 - jm / 7 * (jm - 7) + jd - 1
    }

    private fun d2j(jdn: Int): PersianDate {
        val gy = d2g(jdn).gy
        var jy = gy - 621
        val r = jalCal(jy)
        val jdn1f = g2d(r.gy, 3, r.march)
        var k = jdn - jdn1f
        if (k >= 0) {
            if (k <= 185) {
                return PersianDate(jy, 1 + k / 31, k % 31 + 1)
            }
            k -= 186
        } else {
            jy -= 1
            k += 179
            if (r.leap == 1) k += 1
        }
        return PersianDate(jy, 7 + k / 30, k % 30 + 1)
    }

    fun fromGregorian(gy: Int, gm: Int, gd: Int): PersianDate = d2j(g2d(gy, gm, gd))

    fun toGregorian(date: PersianDate): GregorianDate {
        val g = d2g(j2d(date.y, date.m, date.d))
        return GregorianDate(g.gy, g.gm, g.gd)
    }

    fun today(): PersianDate {
        val cal = Calendar.getInstance()
        return fromGregorian(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }

    /** فاصله‌ی روزهای واقعی بین دو تاریخ شمسی (برای «چند روز تا سررسید») - مستقیم از رو شماره روز
     * ژولینی داخلی الگوریتم حساب می‌شه، بدون نیاز به تبدیل رفت‌وبرگشت به گرگوری. */
    fun daysBetween(from: PersianDate, to: PersianDate): Int =
        j2d(to.y, to.m, to.d) - j2d(from.y, from.m, from.d)
}
