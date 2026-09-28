package ir.sadteam.loancalc.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NameMatchTest {

    @Test
    fun `نرمال سازی ي و ك عربی به ی و ک فارسی`() {
        assertEquals("علی رضایی", normalizePersianName("علي رضايي"))
    }

    @Test
    fun `نیم فاصله و فاصله های چندتایی یکسان می شوند`() {
        assertEquals("علی رضایی", normalizePersianName("علی‌  رضایی"))
    }

    @Test
    fun `دو نام دقیقا یکسان بعد از نرمال سازی تطبیق دارند`() {
        assertTrue(namesLikelyMatch("علی رضایی", "علی  رضایی"))
    }

    @Test
    fun `پیشوند آقای با نام خالی تطبیق دارد`() {
        assertTrue(namesLikelyMatch("آقای رضایی", "رضایی"))
    }

    @Test
    fun `دو نام بی ربط تطبیق ندارند`() {
        assertFalse(namesLikelyMatch("رضایی", "احمدی"))
    }

    @Test
    fun `نام خالی یا نال تطبیق ندارد`() {
        assertFalse(namesLikelyMatch(null, "رضایی"))
        assertFalse(namesLikelyMatch("", "رضایی"))
    }

    @Test
    fun `guessCounterparty نزدیک ترین کاندید را برمی گرداند`() {
        val candidates = listOf("رضایی", "احمدی", "محمدی")
        val guess = guessCounterparty("آقای رضایی", candidates) { it }
        assertEquals("رضایی", guess)
    }

    @Test
    fun `guessCounterparty بدون تطبیق null برمی گرداند`() {
        val candidates = listOf("رضایی", "احمدی")
        assertEquals(null, guessCounterparty("ناشناس", candidates) { it })
    }
}
