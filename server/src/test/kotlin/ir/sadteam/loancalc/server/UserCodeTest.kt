package ir.sadteam.loancalc.server

import kotlin.test.Test
import kotlin.test.assertEquals

class UserCodeTest {
    @Test
    fun jalaliConversion() {
        // ۲ مهر ۱۴۰۵ = ۲۴ سپتامبر ۲۰۲۶؛ ۱ فروردین ۱۴۰۵ = ۲۱ مارس ۲۰۲۶.
        assertEquals(Triple(1405, 7, 2), UserCode.toJalali(2026, 9, 24))
        assertEquals(Triple(1405, 1, 1), UserCode.toJalali(2026, 3, 21))
    }
}
