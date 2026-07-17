package ir.sadteam.loancalc.core

import java.math.BigDecimal

/**
 * پورت مو‌به‌موی calculateAffordability تو www/index.html («چقدر وام می‌تونم بگیرم؟»):
 * از رو قسط ماهانه‌ی دلخواه، نرخ سود و تعداد ماه، سقف اصل وام قابل‌دریافت رو حساب می‌کنه
 * (فرمول معکوس همون فرمول اقساط مساوی نزولی).
 */
object AffordabilityCalculator {
    fun computeMaxPrincipal(monthlyPayment: Double, annualRatePct: Double, months: Int): Double {
        val i = annualRatePct / 1200.0
        return if (i == 0.0) {
            monthlyPayment * months
        } else {
            val iBd = i.toBd()
            val pow = (BigDecimal.ONE + iBd).powBd(-months)
            monthlyPayment.toBd().multiply(BigDecimal.ONE - pow, FINANCIAL_MC)
                .divide(iBd, FINANCIAL_MC).toDouble()
        }
    }
}
