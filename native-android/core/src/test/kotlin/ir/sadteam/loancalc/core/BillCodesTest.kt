package ir.sadteam.loancalc.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BillCodesTest {
    private fun makeBillId(body: String) = body + BillCodes.checkDigit(body)

    private fun makePaymentId(amountK: String, year: Int, period: String, billId: String): String {
        val body = amountK + year + period
        val c1 = BillCodes.checkDigit(body)
        val c2 = BillCodes.checkDigit(billId + body + c1)
        return body + c1 + c2
    }

    @Test fun billIdKindFromServiceDigit() {
        assertEquals("power", BillCodes.billIdKind(makeBillId("123456782")))
        assertEquals("water", BillCodes.billIdKind(makeBillId("987654321")))
        assertEquals("gas", BillCodes.billIdKind(makeBillId("55544433")))
    }

    @Test fun wrongCheckDigitRejected() {
        val ok = makeBillId("123456782")
        val bad = ok.dropLast(1) + ((ok.last() - '0' + 1) % 10)
        assertNull(BillCodes.billIdKind(bad))
    }

    @Test fun persianDigitsAccepted() {
        val ok = makeBillId("123456782")
        val fa = ok.map { '۰' + (it - '0') }.joinToString("")
        assertEquals("power", BillCodes.billIdKind(fa))
    }

    @Test fun paymentIdAmount() {
        val bill = makeBillId("123456782")
        val pay = makePaymentId("485", 5, "07", bill)
        val p = assertNotNull(BillCodes.parsePaymentId(pay, bill))
        assertEquals(485_000L, p.amountRial)
        assertEquals(7, p.period)
    }

    @Test fun smsParsed() {
        val bill = makeBillId("123456782")
        val pay = makePaymentId("1250", 5, "04", bill)
        val sms = "مشترک گرامی مبلغ قبض برق شما ... شناسه قبض: $bill شناسه پرداخت: $pay"
        val r = assertNotNull(BillCodes.parseBillSms(sms))
        assertEquals("power", r.kind)
        assertEquals(1_250_000L, r.amountRial)
    }
}
