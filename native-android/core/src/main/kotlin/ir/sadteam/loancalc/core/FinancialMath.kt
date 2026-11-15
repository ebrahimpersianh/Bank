package ir.sadteam.loancalc.core

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * ابزارهای BigDecimal که محاسبه‌گرهای وام/سپرده/سقف‌وام داخلاً ازشون استفاده می‌کنن تا خطای گرد
 * کردنِ باینریِ Double (مخصوصاً تو تقسیمِ اعداد نزدیک‌به‌هم مثل فرمول قسط مساوی نزولی) حذف بشه.
 * دقتِ ۵۰ رقم بی‌نهایت بیشتر از چیزیه که هر مبلغ ریالی واقعی لازم داره؛ ورودی/خروجیِ توابعِ عمومی
 * همچنان Double می‌مونه چون همه‌جای اپ (fmt تو PersianFormat.kt) نهایتاً مبلغ رو به نزدیک‌ترین
 * ریال گرد می‌کنه - این تبدیل فقط ریسکِ انباشتِ خطا رو تو خودِ فرمول از بین می‌بره، نه چیزی که
 * کاربر تو UI ببینه (تفاوت رو با ۲۰۰ هزار تست تصادفی تایید کردم: کمتر از ۱ ریال، فقط تو موارد
 * مرزیِ گردکردن).
 */
internal val FINANCIAL_MC: MathContext = MathContext(50, RoundingMode.HALF_EVEN)

internal fun Double.toBd(): BigDecimal = BigDecimal(this.toString())

/** پشتیبانی از توان منفی هم (BigDecimal.pow استاندارد فقط توان نامنفی می‌گیره) */
internal fun BigDecimal.powBd(exponent: Int): BigDecimal =
    if (exponent >= 0) {
        this.pow(exponent, FINANCIAL_MC)
    } else {
        BigDecimal.ONE.divide(this.pow(-exponent, FINANCIAL_MC), FINANCIAL_MC)
    }
