package ir.sadteam.loancalc.core

/**
 * پورت مو‌به‌موی calculateRateFinder/installmentForRate تو www/index.html («یا برعکس: نرخ سود رو
 * پیدا کن» تو تب محاسبه‌گر): از رو مبلغ وام، قسط ماهانه و تعداد ماه، نرخ سود سالانه‌ی تقریبی رو با
 * جستجوی دودویی (۶۰ تکرار، رو بازه‌ی ۰ تا ۱۰۰ درصد) پیدا می‌کنه.
 */
object RateFinderCalculator {
    fun installmentForRate(principal: Double, annualRatePct: Double, months: Int): Double {
        val i = annualRatePct / 1200.0
        if (i == 0.0) return principal / months
        return principal * i * Math.pow(1 + i, months.toDouble()) / (Math.pow(1 + i, months.toDouble()) - 1)
    }

    /** null یعنی ورودی‌ها نامعتبرن (مبلغ کل قسط‌ها از اصل وام کمتره یا صفر/منفی). */
    fun findRate(principal: Double, installment: Double, months: Int): Double? {
        if (principal <= 0 || installment <= 0 || months <= 0) return null
        if (installment * months < principal) return null
        var lo = 0.0
        var hi = 100.0
        repeat(60) {
            val mid = (lo + hi) / 2
            val inst = installmentForRate(principal, mid, months)
            if (inst > installment) hi = mid else lo = mid
        }
        return (lo + hi) / 2
    }
}
