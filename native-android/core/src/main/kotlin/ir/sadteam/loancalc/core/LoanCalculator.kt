package ir.sadteam.loancalc.core

import java.math.BigDecimal

enum class LoanMethod { STANDARD, QARZ, FLAT }

data class LoanRow(
    val month: Int,
    val installment: Double,
    val interest: Double,
    val principal: Double,
    val balance: Double,
)

data class LoanResult(
    val installment: Double,
    val totalInterest: Double,
    val totalPaid: Double,
    val principal: Double,
    val rows: List<LoanRow>,
    val graceMonths: Int,
    val originalPrincipal: Double,
    val intervalDays: Int,
)

/**
 * پورت مو‌به‌موی computeLoan تو www/index.html - دست نزن مگر با تست دقیق رو چند مثال واقعی
 * (بنگرید CLAUDE.md، بخش «فرمول‌های حساسِ محاسبه»).
 */
object LoanCalculator {
    fun compute(
        principalAmount: Double,
        annualRatePct: Double,
        n: Int,
        method: LoanMethod,
        graceMonths: Int = 0,
        intervalDays: Int = 30,
    ): LoanResult {
        val i = annualRatePct / 100.0 * (intervalDays / 365.0)
        var principal = principalAmount
        if (graceMonths > 0) {
            principal = principalAmount + principalAmount * (annualRatePct / 100.0 / 365.0) * (graceMonths * 30)
        }
        val rows = mutableListOf<LoanRow>()
        val installment: Double
        val totalInterest: Double
        val totalPaid: Double

        when (method) {
            LoanMethod.STANDARD -> {
                installment = if (i == 0.0) {
                    principal / n
                } else {
                    // فرمولِ اصلی (پورتِ computeLoan تو www/index.html) با Double همینه؛ اینجا فقط
                    // خودِ عبارتِ حساس (تقسیمِ دو عددِ نزدیک‌به‌هم) با BigDecimal حساب می‌شه تا خطای
                    // گردکردنِ باینری تو محاسبه‌ی قسط (که همه‌ی ردیف‌های جدول رو ازش می‌سازیم) نباشه؛
                    // نتیجه بلافاصله به Double برمی‌گرده چون بقیه‌ی اپ (fmt، Room، UI) با Double کار
                    // می‌کنن و همه‌جا نهایتاً به نزدیک‌ترین ریال گرد می‌شه.
                    val iBd = i.toBd()
                    val pow = (BigDecimal.ONE + iBd).powBd(n)
                    val numerator = principal.toBd() * iBd * pow
                    numerator.divide(pow - BigDecimal.ONE, FINANCIAL_MC).toDouble()
                }
                var balance = principal
                for (m in 1..n) {
                    val interestPart = balance * i
                    val principalPart = installment - interestPart
                    balance -= principalPart
                    rows.add(LoanRow(m, installment, interestPart, principalPart, maxOf(balance, 0.0)))
                }
                totalPaid = installment * n
                totalInterest = totalPaid - principal
            }

            LoanMethod.QARZ -> {
                // قرض‌الحسنه: بدون سود مرکب. قسط‌های ۱، ۱۳، ۲۵... (ابتدای هر سال) کاملاً
                // مخصوص کارمزد سالانه‌ان (روی مانده‌ی همون لحظه) و هیچ اصل وامی توشون نیست؛
                // اصل وام فقط بین بقیه‌ی اقساط (غیرکارمزدی) مساوی تقسیم می‌شه.
                val feeInstallments = Math.ceil(n / 12.0).toInt()
                val principalInstallments = n - feeInstallments
                val principalPart = principal.toBd().divide(BigDecimal(principalInstallments), FINANCIAL_MC).toDouble()
                var balance = principal
                var totalFee = 0.0
                for (m in 1..n) {
                    if ((m - 1) % 12 == 0) {
                        val k = (m - 1) / 12
                        val feePart = (principal - k * 11 * principalPart) * (annualRatePct / 100.0)
                        totalFee += feePart
                        rows.add(LoanRow(m, feePart, feePart, 0.0, maxOf(balance, 0.0)))
                    } else {
                        balance -= principalPart
                        rows.add(LoanRow(m, principalPart, 0.0, principalPart, maxOf(balance, 0.0)))
                    }
                }
                installment = principalPart
                totalInterest = totalFee
                totalPaid = principal + totalFee
            }

            LoanMethod.FLAT -> {
                totalInterest = (principal.toBd() * annualRatePct.toBd() * BigDecimal(n + 1))
                    .divide(BigDecimal(2400), FINANCIAL_MC).toDouble()
                installment = (principal + totalInterest).toBd().divide(BigDecimal(n), FINANCIAL_MC).toDouble()
                val flatInterest = totalInterest / n
                val flatPrincipal = principal / n
                var balance = principal
                for (m in 1..n) {
                    balance -= flatPrincipal
                    rows.add(LoanRow(m, installment, flatInterest, flatPrincipal, maxOf(balance, 0.0)))
                }
                totalPaid = installment * n
            }
        }

        return LoanResult(
            installment = installment,
            totalInterest = totalInterest,
            totalPaid = totalPaid,
            principal = principal,
            rows = rows,
            graceMonths = graceMonths,
            originalPrincipal = principalAmount,
            intervalDays = intervalDays,
        )
    }
}
