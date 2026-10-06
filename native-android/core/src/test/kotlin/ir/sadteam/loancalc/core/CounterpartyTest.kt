package ir.sadteam.loancalc.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CounterpartyTest {
    @Test
    fun bluTransferRecipient() {
        assertEquals(
            "مبینا فتحی مقدم لاکانی",
            Counterparty.extract("برداشت پول ابراهیم عزیز، 10,000 ریال از حساب شما به مبینا فتحی مقدم لاکانی منتقل شد", true),
        )
    }

    @Test
    fun shopAndSender() {
        assertEquals("فروشگاه رفاه", Counterparty.extract("خرید 250,000 ریال فروشگاه رفاه کارت 1234", true))
        assertEquals("علی رضایی", Counterparty.extract("واریز 500,000 تومان به حساب شما از علی رضایی", false))
    }

    @Test
    fun noNameReturnsNull() {
        assertNull(Counterparty.extract("برداشت 100,000 ریال از حساب شما. مانده 2,000,000", true))
        assertNull(Counterparty.extract("واریز 100,000 ریال به حساب شما", false))
    }
}
