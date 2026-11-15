package ir.sadteam.loancalc.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChequeRiskTest {

    @Test
    fun `بدون سابقه امتیاز صد و hasHistory نادرست است`() {
        val r = computeChequeRiskScore(0, 0)
        assertEquals(100, r.score)
        assertFalse(r.hasHistory)
    }

    @Test
    fun `همه پاس شده امتیاز صد است`() {
        val r = computeChequeRiskScore(5, 0)
        assertEquals(100, r.score)
        assertTrue(r.hasHistory)
    }

    @Test
    fun `همه برگشت خورده امتیاز صفر است`() {
        val r = computeChequeRiskScore(0, 3)
        assertEquals(0, r.score)
    }

    @Test
    fun `نسبت مساوی امتیاز پنجاه است`() {
        val r = computeChequeRiskScore(2, 2)
        assertEquals(50, r.score)
    }
}
