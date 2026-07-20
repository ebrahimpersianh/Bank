package ir.sadteam.loancalc.core

/** تاریخ شمسی ساده (بدون تبدیل واقعی از/به میلادی) - دقیقاً هم‌راستا با addDaysFa/monthLength تو www/index.html */
data class PersianDate(val y: Int, val m: Int, val d: Int)

object PersianCalendar {
    fun monthLength(m: Int): Int = when {
        m <= 6 -> 31
        m <= 11 -> 30
        else -> 29
    }

    fun addDays(date: PersianDate, days: Int): PersianDate {
        var y = date.y
        var m = date.m
        var d = date.d
        repeat(days) {
            d++
            if (d > monthLength(m)) {
                d = 1
                m++
                if (m > 12) {
                    m = 1
                    y++
                }
            }
        }
        return PersianDate(y, m, d)
    }

    /** روزِ ماه ثابت می‌مونه و فقط clamp می‌شه به طولِ واقعیِ ماهِ مقصد (با کبیسه‌ی واقعیِ اسفند از
     * [JalaliCalendar.daysInMonth]، نه فرضِ ساده‌ی همیشه-۲۹ی [monthLength]) - این تابع پایه‌ی
     * سررسیدِ ماهانه‌ی اقساطه (رجوع کن به LoanRepository.getRows) و «۳۰ اسفندِ سالِ کبیسه» نباید
     * اشتباهی به ۲۹ برگرده. */
    fun addMonths(date: PersianDate, count: Int): PersianDate {
        var y = date.y
        var m = date.m
        repeat(count) {
            m++
            if (m > 12) {
                m = 1
                y++
            }
        }
        val d = minOf(date.d, JalaliCalendar.daysInMonth(y, m))
        return PersianDate(y, m, d)
    }
}
