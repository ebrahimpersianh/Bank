package ir.sadteam.loancalc.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SmartInsightsTest {
    private fun tx(exp: Boolean, amountToman: Long, y: Int, m: Int, d: Int, cat: String? = null, desc: String = "") =
        SmartInsights.Tx(exp, amountToman * 10.0, y, m, d, cat, desc)

    @Test fun unusualSpendDetected() {
        val txs = listOf(
            tx(true, 1_000_000, 1405, 4, 5, "رستوران"), tx(true, 1_000_000, 1405, 5, 5, "رستوران"),
            tx(true, 1_000_000, 1405, 6, 5, "رستوران"), tx(true, 3_000_000, 1405, 7, 5, "رستوران"),
        )
        val r = SmartInsights.compute(txs, PersianDate(1405, 7, 15), 30)
        assertTrue(r.any { it.kind == SmartInsights.Kind.UNUSUAL_SPEND })
    }

    @Test fun recurringDetected() {
        val txs = (4..6).map { tx(true, 250_000, 1405, it, 3, "اینترنت", "اینترنت خانه") }
        val r = SmartInsights.compute(txs, PersianDate(1405, 7, 2), 30)
        assertTrue(r.any { it.kind == SmartInsights.Kind.RECURRING })
        val r2 = SmartInsights.compute(txs, PersianDate(1405, 7, 2), 30, existingRecurringNames = setOf("اینترنت خانه"))
        assertTrue(r2.none { it.kind == SmartInsights.Kind.RECURRING })
    }

    @Test fun salaryMissing() {
        val txs = (4..6).map { tx(false, 30_000_000, 1405, it, 25) }
        val r = SmartInsights.compute(txs, PersianDate(1405, 7, 29), 30)
        assertTrue(r.any { it.kind == SmartInsights.Kind.SALARY_MISSING })
    }

    @Test fun duesOverBalance() {
        val r = SmartInsights.compute(emptyList(), PersianDate(1405, 7, 10), 30, upcomingDues7d = 100.0, totalBalance = 50.0)
        assertEquals(SmartInsights.Kind.DUES_OVER_BALANCE, r.single().kind)
    }

    @Test fun categorySuggestion() {
        val h = listOf(tx(true, 1, 1405, 7, 1, "حمل‌ونقل", "اسنپ"), tx(true, 1, 1405, 7, 2, "حمل‌ونقل", "اسنپ"), tx(true, 1, 1405, 7, 3, "تفریح", "اسنپ"))
        assertEquals("حمل‌ونقل", SmartInsights.suggestCategory(h, " اسنپ", true))
    }
}
