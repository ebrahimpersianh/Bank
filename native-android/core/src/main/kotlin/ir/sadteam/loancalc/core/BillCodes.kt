package ir.sadteam.loancalc.core

/**
 * شناسه‌ی قبض و شناسه‌ی پرداختِ استانداردِ ایران (مشترک بینِ آب، برق، گاز، تلفن…).
 *
 * - **شناسه‌ی قبض**: رقمِ آخر رقمِ کنترلی است؛ رقمِ یکی‌مانده‌به‌آخر **نوعِ خدمت** را می‌گوید.
 * - **شناسه‌ی پرداخت**: `مبلغ (هزار ریال) | رقمِ آخرِ سال | دوره (۲ رقم) | کنترل۱ | کنترل۲`.
 *   کنترل۱ رقم‌های پیش از خودش را می‌پاید، کنترل۲ الحاقِ «شناسه‌ی قبض + شناسه‌ی پرداخت بی کنترل۲» را.
 * رقمِ کنترلی: وزن‌های ۲..۷ از راست، باقی‌مانده بر ۱۱؛ ۰ یا ۱ → ۰، وگرنه ۱۱ منهای آن.
 */
object BillCodes {
    /** کلیدِ نوع همان کلیدهای `BILL_KINDS`ِ برنامه. */
    private val SERVICE = mapOf(
        '1' to "water", '2' to "power", '3' to "gas", '4' to "phone", '5' to "mobile",
        '6' to "other", '7' to "other", '8' to "other", '9' to "other",
    )

    fun checkDigit(digits: String): Int {
        var sum = 0
        var w = 2
        for (i in digits.indices.reversed()) {
            sum += (digits[i] - '0') * w
            w = if (w == 7) 2 else w + 1
        }
        val r = sum % 11
        return if (r < 2) 0 else 11 - r
    }

    private fun onlyDigits(s: String) = normalizeDigits(s).filter { it.isDigit() }.trimStart('0')

    /** نوعِ قبض از شناسه، یا `null` اگر شناسه معتبر نیست. */
    fun billIdKind(raw: String): String? {
        val id = onlyDigits(raw)
        if (id.length !in 6..13) return null
        if (checkDigit(id.dropLast(1)) != id.last() - '0') return null
        return SERVICE[id[id.length - 2]]
    }

    fun isValidBillId(raw: String) = billIdKind(raw) != null

    data class Payment(val amountRial: Long, val yearDigit: Int, val period: Int)

    /** مبلغ و دوره از شناسه‌ی پرداخت؛ اگر [billId] داده شود کنترل۲ هم سنجیده می‌شود. */
    fun parsePaymentId(raw: String, billId: String? = null): Payment? {
        val p = onlyDigits(raw)
        if (p.length !in 6..13) return null
        val body = p.dropLast(2)
        if (checkDigit(body) != p[p.length - 2] - '0') return null
        if (billId != null) {
            val b = onlyDigits(billId)
            if (checkDigit(b + p.dropLast(1)) != p.last() - '0') return null
        }
        val amountK = body.dropLast(3).toLongOrNull() ?: return null
        return Payment(
            amountRial = amountK * 1000,
            yearDigit = body[body.length - 3] - '0',
            period = body.takeLast(2).toInt(),
        )
    }

    data class SmsBill(val billId: String?, val paymentId: String?, val kind: String?, val amountRial: Long?)

    private val BILL_RE = Regex("شناسه\\s*(?:ی\\s*)?قبض\\s*[:：]?\\s*(\\d{6,13})")
    private val PAY_RE = Regex("شناسه\\s*(?:ی\\s*)?پرداخت\\s*[:：]?\\s*(\\d{6,13})")

    /** شناسه‌ها را از متنِ پیامکِ قبض (آب/برق/گاز/…) درمی‌آورد؛ `null` اگر چیزی نبود. */
    fun parseBillSms(text: String): SmsBill? {
        val t = normalizeDigits(text)
        val bill = BILL_RE.find(t)?.groupValues?.get(1)?.trimStart('0')
        val pay = PAY_RE.find(t)?.groupValues?.get(1)?.trimStart('0')
        if (bill == null && pay == null) return null
        val kind = bill?.let { billIdKind(it) } ?: guessKind(t)
        val amount = pay?.let { parsePaymentId(it, bill)?.amountRial ?: parsePaymentId(it)?.amountRial }
        return SmsBill(bill, pay, kind, amount)
    }

    /** نوع از کلمه‌های متن (برای پیامکِ بانک یا پیامکِ بی‌شناسه). */
    fun guessKind(text: String): String? = when {
        "برق" in text -> "power"
        "آبفا" in text || "آب " in text || "آب\n" in text -> "water"
        "گاز" in text -> "gas"
        "همراه" in text || "ایرانسل" in text || "رایتل" in text || "موبایل" in text -> "mobile"
        "مخابرات" in text || "تلفن" in text -> "phone"
        else -> null
    }

    fun normalizeDigits(s: String): String = buildString(s.length) {
        for (c in s) append(
            when (c) {
                in '۰'..'۹' -> '0' + (c - '۰')
                in '٠'..'٩' -> '0' + (c - '٠')
                else -> c
            },
        )
    }
}
