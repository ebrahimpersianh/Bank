package ir.sadteam.loancalc.core

import java.math.BigDecimal

/**
 * پورت مو‌به‌موی calculateDeposit تو www/index.html («سود سپرده»): سود به روش ساده (غیرمرکب)
 * طبق روال معمول بانک‌ها، روی مبلغ اصل سپرده محاسبه می‌شه (نه سود مرکب روزشمار).
 */
data class DepositResult(
    val dailyInterest: Double,
    val monthlyInterest: Double,
    val totalInterest: Double,
    val finalAmount: Double,
)

object DepositCalculator {
    fun compute(principal: Double, annualRatePct: Double, months: Int): DepositResult {
        val dailyInterest = principal.toBd().multiply(annualRatePct.toBd(), FINANCIAL_MC)
            .divide(BigDecimal(100), FINANCIAL_MC)
            .divide(BigDecimal(365), FINANCIAL_MC)
            .toDouble()
        val monthlyInterest = dailyInterest * 30
        val totalDays = months * 30
        val totalInterest = dailyInterest * totalDays
        val finalAmount = principal + totalInterest
        return DepositResult(dailyInterest, monthlyInterest, totalInterest, finalAmount)
    }
}
