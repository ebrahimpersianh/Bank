package ir.sadteam.loancalc.core

/** تاریخ شمسی ساده (بدون تبدیل واقعی از/به میلادی) - دقیقاً هم‌راستا با addDaysFa/monthLength تو www/index.html */
data class PersianDate(val y: Int, val m: Int, val d: Int)

object PersianCalendar {
    fun monthLength(m: Int): Int = when {
        m <= 6 -> 31
        m <= 11 -> 30
        else -> 29
    }

    // نکته‌ی مهم: Kotlinِ `repeat(n)` برای n منفی هیچ تکراری اجرا نمی‌کنه (نه خطا، فقط سکوت) - قبلاً
    // این تابع مستقیم `repeat(days)` صدا می‌زد، یعنی addDays(date, -1) بی‌اثر بود (همون تاریخ رو
    // برمی‌گردوند). این باگِ واقعی بود: فلشِ «روزِ قبل» تو گزارش/خانه/سررسید و نمودارِ ۷روزه‌ی گزارش
    // (که با addDays منفی می‌سازتشون) خراب بودن. رجوع کن به CLAUDE.md.
    fun addDays(date: PersianDate, days: Int): PersianDate {
        var y = date.y
        var m = date.m
        var d = date.d
        if (days >= 0) {
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
        } else {
            repeat(-days) {
                d--
                if (d < 1) {
                    m--
                    if (m < 1) {
                        m = 12
                        y--
                    }
                    d = monthLength(m)
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
        // هم‌الگو با رفعِ باگِ addDays - همینجا هم `repeat(count)`ِ خام برای count منفی بی‌اثر بود.
        // فعلاً هیچ‌جای کد addMonths رو با عددِ منفی صدا نمی‌زنه، ولی برای جلوگیریِ همون کلاس‌باگ تو
        // آینده اینجا هم رفع شد.
        if (count >= 0) {
            repeat(count) {
                m++
                if (m > 12) {
                    m = 1
                    y++
                }
            }
        } else {
            repeat(-count) {
                m--
                if (m < 1) {
                    m = 12
                    y--
                }
            }
        }
        val d = minOf(date.d, JalaliCalendar.daysInMonth(y, m))
        return PersianDate(y, m, d)
    }
}
