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

/** خروجی خامِ هر Strategy، قبل از پیچیده‌شدن تو LoanResult نهایی (که فیلدهای مشترک بین همه‌ی
 * روش‌ها - principal, graceMonths, originalPrincipal, intervalDays - رو هم داره). */
private data class FormulaOutput(
    val installment: Double,
    val totalInterest: Double,
    val totalPaid: Double,
    val rows: List<LoanRow>,
)

/**
 * Strategy Pattern برای فرمول‌های وام: هر روش محاسبه (ساده/قرض‌الحسنه/پلکانیِ ثابت) پیاده‌سازیِ
 * جدای خودش رو داره بجای یه when تودرتو. هیچ‌کدوم از فرمول‌ها عوض نشدن - فقط جابه‌جا شدن، دقیقاً
 * همون منطقِ قبلی (پورتِ computeLoan تو www/index.html)، رجوع کن به core/src/test برای رگرسیون.
 */
private interface LoanFormula {
    fun compute(principal: Double, annualRatePct: Double, n: Int, i: Double): FormulaOutput
}

private object StandardFormula : LoanFormula {
    override fun compute(principal: Double, annualRatePct: Double, n: Int, i: Double): FormulaOutput {
        val installment = if (i == 0.0) {
            principal / n
        } else {
            // فرمولِ اصلی با Double همینه؛ اینجا فقط خودِ عبارتِ حساس (تقسیمِ دو عددِ نزدیک‌به‌هم) با
            // BigDecimal حساب می‌شه تا خطای گردکردنِ باینری تو محاسبه‌ی قسط (که همه‌ی ردیف‌های جدول
            // رو ازش می‌سازیم) نباشه؛ نتیجه بلافاصله به Double برمی‌گرده چون بقیه‌ی اپ (fmt، Room، UI)
            // با Double کار می‌کنن و همه‌جا نهایتاً به نزدیک‌ترین ریال گرد می‌شه.
            val iBd = i.toBd()
            val pow = (BigDecimal.ONE + iBd).powBd(n)
            val numerator = principal.toBd() * iBd * pow
            numerator.divide(pow - BigDecimal.ONE, FINANCIAL_MC).toDouble()
        }
        val rows = mutableListOf<LoanRow>()
        var balance = principal
        for (m in 1..n) {
            val interestPart = balance * i
            val principalPart = installment - interestPart
            balance -= principalPart
            rows.add(LoanRow(m, installment, interestPart, principalPart, maxOf(balance, 0.0)))
        }
        val totalPaid = installment * n
        val totalInterest = totalPaid - principal
        return FormulaOutput(installment, totalInterest, totalPaid, rows)
    }
}

private object QarzFormula : LoanFormula {
    override fun compute(principal: Double, annualRatePct: Double, n: Int, i: Double): FormulaOutput {
        // قرض‌الحسنه: بدون سود مرکب. قسط‌های ۱، ۱۳، ۲۵... (ابتدای هر سال) کاملاً مخصوص کارمزد
        // سالانه‌ان (روی مانده‌ی همون لحظه) و هیچ اصل وامی توشون نیست؛ اصل وام فقط بین بقیه‌ی
        // اقساط (غیرکارمزدی) مساوی تقسیم می‌شه.
        val feeInstallments = Math.ceil(n / 12.0).toInt()
        val principalInstallments = n - feeInstallments
        val principalPart = principal.toBd().divide(BigDecimal(principalInstallments), FINANCIAL_MC).toDouble()
        val rows = mutableListOf<LoanRow>()
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
        return FormulaOutput(
            installment = principalPart,
            totalInterest = totalFee,
            totalPaid = principal + totalFee,
            rows = rows,
        )
    }
}

private object FlatFormula : LoanFormula {
    override fun compute(principal: Double, annualRatePct: Double, n: Int, i: Double): FormulaOutput {
        val totalInterest = (principal.toBd() * annualRatePct.toBd() * BigDecimal(n + 1))
            .divide(BigDecimal(2400), FINANCIAL_MC).toDouble()
        val installment = (principal + totalInterest).toBd().divide(BigDecimal(n), FINANCIAL_MC).toDouble()
        val flatInterest = totalInterest / n
        val flatPrincipal = principal / n
        val rows = mutableListOf<LoanRow>()
        var balance = principal
        for (m in 1..n) {
            balance -= flatPrincipal
            rows.add(LoanRow(m, installment, flatInterest, flatPrincipal, maxOf(balance, 0.0)))
        }
        val totalPaid = installment * n
        return FormulaOutput(installment, totalInterest, totalPaid, rows)
    }
}

private fun formulaFor(method: LoanMethod): LoanFormula = when (method) {
    LoanMethod.STANDARD -> StandardFormula
    LoanMethod.QARZ -> QarzFormula
    LoanMethod.FLAT -> FlatFormula
}

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

        val output = formulaFor(method).compute(principal, annualRatePct, n, i)

        return LoanResult(
            installment = output.installment,
            totalInterest = output.totalInterest,
            totalPaid = output.totalPaid,
            principal = principal,
            rows = output.rows,
            graceMonths = graceMonths,
            originalPrincipal = principalAmount,
            intervalDays = intervalDays,
        )
    }
}
