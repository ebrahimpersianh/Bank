package ir.sadteam.roozegar.notif

import ir.sadteam.roozegar.core.PersianNames
import ir.sadteam.roozegar.core.toPersianDigits
import java.util.Date

/**
 * تاریخ قمری از ICU خودِ اندروید (android.icu، از API 24 در دسترسه - بدون هیچ کتابخونه‌ی اضافه).
 * روش محاسبه ام‌القری‌ست که به تقویم رسمی نزدیکه ولی ممکنه با اعلام رؤیت هلال ایران ±۱ روز فرق
 * کنه - برای نمایش اطلاعاتی کافیه؛ اگه بعداً دقت رسمی لازم شد (تعطیلات قمری فاز ۲)، باید جدول
 * رسمی ایران جایگزین/تصحیح بشه.
 */
object HijriDates {
    data class Hijri(val y: Int, val m: Int, val d: Int)

    fun today(): Hijri? = try {
        val cal = android.icu.util.IslamicCalendar()
        try {
            cal.calculationType = android.icu.util.IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
            cal.time = Date() // بعد از تغییر نوع محاسبه، دوباره رو «الان» ست می‌شه
        } catch (_: Throwable) {
            // اگه نوع محاسبه در دسترس نبود، همون پیش‌فرض (قمری مدنی) هم قابل قبوله
        }
        Hijri(
            y = cal.get(android.icu.util.Calendar.YEAR),
            m = cal.get(android.icu.util.Calendar.MONTH) + 1,
            d = cal.get(android.icu.util.Calendar.DAY_OF_MONTH),
        )
    } catch (_: Throwable) {
        null
    }

    /** «۶ صفر ۱۴۴۸» - یا null اگه ICU در دسترس نبود. */
    fun todayLine(): String? = today()?.let {
        "${it.d.toPersianDigits()} ${PersianNames.hijriMonths[it.m - 1]} ${it.y.toPersianDigits()}"
    }
}
