package ir.sadteam.loancalc.core

/**
 * **پیش‌بینیِ آخرِ ماه** - «با این سرعتِ خرج، تا آخرِ ماه کم میاری».
 *
 * قاعده‌ی ساده و قابلِ‌دفاع (عمداً نه مدلِ پیچیده): سرعتِ خرجِ روزانه‌ی همین ماه × روزهای
 * باقیمانده + قسط/چکِ سررسیدشده‌ی همین ماه + اشتراک‌های ماهانه‌ای که هنوز کسر نشدن.
 *
 * ⚠️ اگه هنوز چند روز از ماه گذشته، سرعتِ روزانه بی‌معنیه (یه خریدِ بزرگ کلِ پیش‌بینی رو
 * منفجر می‌کنه)، برای همین زیرِ [MIN_DAYS_FOR_FORECAST] روز اصلاً پیش‌بینی نمی‌ده.
 */
data class MonthForecast(
    /** خرجِ واقعیِ تا امروز. */
    val spentSoFarRial: Double,
    /** تخمینِ خرجِ باقیمانده‌ی ماه (سرعتِ روزانه × روزهای مونده + تعهدهای مشخص). */
    val projectedRemainingRial: Double,
    /** موجودیِ فعلی همه‌ی حساب‌ها. */
    val balanceRial: Double,
    val daysLeft: Int,
) {
    /** تخمینِ کلِ خرجِ ماه. */
    val projectedTotalRial: Double get() = spentSoFarRial + projectedRemainingRial

    /** چقدر کم میاری؟ عددِ مثبت یعنی کسری. */
    val shortfallRial: Double get() = projectedRemainingRial - balanceRial

    /** آیا باید هشدار داد؟ */
    val willRunShort: Boolean get() = shortfallRial > 0

    companion object {
        /** زیرِ این تعدادِ روزِ گذشته از ماه، سرعتِ روزانه قابلِ‌اتکا نیست. */
        const val MIN_DAYS_FOR_FORECAST = 7

        /**
         * @param spentSoFarRial جمعِ خرجِ تاییدشده‌ی این ماه تا امروز
         * @param dayOfMonth امروز چندمِ ماهه (۱..۳۱)
         * @param daysInMonth طولِ همین ماهِ شمسی
         * @param balanceRial جمعِ موجودیِ همه‌ی حساب‌کتاب‌ها
         * @param committedRial تعهدهای **قطعیِ** باقیمانده‌ی ماه (قسط، چک، اشتراکِ کسرنشده)
         * @return null یعنی هنوز داده‌ی کافی برای پیش‌بینی نیست
         */
        fun compute(
            spentSoFarRial: Double,
            dayOfMonth: Int,
            daysInMonth: Int,
            balanceRial: Double,
            committedRial: Double = 0.0,
        ): MonthForecast? {
            if (dayOfMonth < MIN_DAYS_FOR_FORECAST) return null
            if (dayOfMonth >= daysInMonth) return null
            if (spentSoFarRial <= 0.0) return null
            val daysLeft = daysInMonth - dayOfMonth
            val perDay = spentSoFarRial / dayOfMonth
            return MonthForecast(
                spentSoFarRial = spentSoFarRial,
                projectedRemainingRial = perDay * daysLeft + committedRial,
                balanceRial = balanceRial,
                daysLeft = daysLeft,
            )
        }
    }
}
