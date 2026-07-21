package ir.sadteam.roozegar.core

/**
 * چیدمان یه ماه شمسی برای رسم گرید تقویم (اپ و ویجت): چند خونه‌ی خالی قبل از روز ۱ (هفته‌ی
 * ایرانی، شنبه ستون اول) و تعداد روزهای واقعی ماه (با کبیسه‌ی واقعی اسفند).
 */
data class MonthGrid(val year: Int, val month: Int, val leadingBlanks: Int, val daysInMonth: Int) {
    /** تعداد ردیف‌های لازم گرید (۴ تا ۶ ردیف ۷تایی). */
    val weeks: Int get() = (leadingBlanks + daysInMonth + 6) / 7

    companion object {
        fun of(year: Int, month: Int): MonthGrid = MonthGrid(
            year = year,
            month = month,
            leadingBlanks = JalaliCalendar.dayOfWeekSaturdayFirst(PersianDate(year, month, 1)),
            daysInMonth = JalaliCalendar.daysInMonth(year, month),
        )

        /** شماره‌ی سریالی یه ماه (برای پیجرِ بی‌نهایتِ ماه‌ها): فروردینِ سال ۰ = ۰. */
        fun indexOf(year: Int, month: Int): Int = year * 12 + (month - 1)

        /** برعکس [indexOf]: از شماره‌ی سریالی به (سال، ماه). */
        fun fromIndex(index: Int): Pair<Int, Int> = Pair(index / 12, index % 12 + 1)
    }
}
