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
        // باگِ رفع‌شده: قبلاً «months * 30» بود که یک سالِ کامل رو ۳۶۰ روز حساب می‌کرد (نه ۳۶۵ روزِ
        // واقعی) - نتیجه حدودِ ۱.۴٪ کمتر از انتظار می‌شد (مثلاً «سود سالانه»ی ۵۰٪ روی ۱۰،۰۰۰،۰۰۰
        // ریال باید دقیقاً ۵،۰۰۰،۰۰۰ بشه، ولی می‌شد ۴،۹۳۱،۵۰۷). برای هر چندتا سالِ کامل (هر ۱۲ ماه)
        // از ۳۶۵ روزِ واقعی استفاده می‌کنیم؛ برای باقیِ ماه‌های ناقص (کمتر از ۱۲) همچنان از تقریبِ
        // ۳۰روزه استفاده می‌شه (رویه‌ی معمولِ بانکی برای دوره‌های غیرِ یک‌ساله).
        val fullYears = months / 12
        val remainingMonths = months % 12
        val totalDays = fullYears * 365 + remainingMonths * 30
        val totalInterest = dailyInterest * totalDays
        val finalAmount = principal + totalInterest
        return DepositResult(dailyInterest, monthlyInterest, totalInterest, finalAmount)
    }
}
