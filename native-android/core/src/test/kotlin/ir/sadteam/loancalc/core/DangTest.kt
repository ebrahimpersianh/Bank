package ir.sadteam.loancalc.core

import kotlin.test.Test
import kotlin.test.assertEquals

class DangTest {
    @Test
    fun equalDangShares_sumsExactlyToTotal_evenWhenNotDivisible() {
        val shares = equalDangShares(100000.0, 3)
        assertEquals(3, shares.size)
        assertEquals(100000.0, shares.sum(), 0.0)
    }

    @Test
    fun equalDangShares_splitsEvenlyWhenDivisible() {
        val shares = equalDangShares(90000.0, 3)
        assertEquals(listOf(30000.0, 30000.0, 30000.0), shares)
    }

    @Test
    fun equalDangShares_zeroCount_returnsEmpty() {
        assertEquals(emptyList(), equalDangShares(1000.0, 0))
    }

    @Test
    fun percentageDangShares_appliesEachPercentage() {
        val shares = percentageDangShares(200000.0, listOf(50.0, 30.0, 20.0))
        assertEquals(listOf(100000.0, 60000.0, 40000.0), shares)
    }
}
