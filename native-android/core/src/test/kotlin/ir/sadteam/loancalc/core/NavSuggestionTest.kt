package ir.sadteam.loancalc.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * تستِ سه شرطِ `41c`. ⚠️ `:core` رو کلاس‌پثِ تستش **JUnit4 نداره** - `kotlin.test`.
 */
class NavSuggestionTest {

    private val day = 24L * 60 * 60 * 1000
    private val now = 1_800_000_000_000L
    private val slots = listOf("home", "assets", "report", "budget", "due")

    /**
     * همه‌ی تب‌های داخلِ نوار یه پایه‌ی شمارش می‌گیرن، وگرنه چند تبِ صفر با هم کم‌کاربردترین
     * می‌شن و انتخابِ نامزدِ پایین‌رفتن به ترتیبِ لیست بستگی پیدا می‌کنه نه به عدد.
     */
    private val baseline = mapOf("home" to 300, "assets" to 30, "report" to 25, "due" to 20)

    private fun compute(
        counts: Map<String, Int>,
        firstDay: Long = now - 30 * day,
        lastSuggestedAt: Long = 0L,
        dismissed: Set<String> = emptySet(),
    ) = NavSuggestion.compute(slots, baseline + counts, firstDay, lastSuggestedAt, dismissed, now)

    @Test
    fun `مثالِ خودِ فریم - وام ۴۱ بار، بودجه ۲ بار`() {
        val r = assertNotNull(compute(mapOf("loan" to 41, "budget" to 2)))
        assertEquals("loan", r.promote)
        assertEquals("budget", r.demote)
        assertEquals(41, r.promoteCount)
        assertEquals(2, r.demoteCount)
        assertEquals(slots.indexOf("budget"), r.slotIndex)
        assertEquals(listOf("home", "assets", "report", "loan", "due"), NavSuggestion.apply(slots, r))
    }

    @Test
    fun `زیرِ ۲۱ روز داده پیشنهاد نمی‌ده`() {
        assertNull(compute(mapOf("loan" to 41, "budget" to 2), firstDay = now - 20 * day))
    }

    @Test
    fun `نسبتِ کمتر از ۵ به ۱ رد می‌شه`() {
        assertNull(compute(mapOf("loan" to 9, "budget" to 2)))
        assertNotNull(compute(mapOf("loan" to 10, "budget" to 2)))
    }

    @Test
    fun `مقصدِ بی‌استفاده هم باید حداقل ۵ بار باز شده باشه`() {
        assertNull(compute(mapOf("loan" to 4, "budget" to 0)))
        assertNotNull(compute(mapOf("loan" to 5, "budget" to 0)))
    }

    @Test
    fun `فاصله‌ی کمتر از ۶۰ روز از پیشنهادِ قبلی رد می‌شه`() {
        assertNull(compute(mapOf("loan" to 41, "budget" to 2), lastSuggestedAt = now - 59 * day))
        assertNotNull(compute(mapOf("loan" to 41, "budget" to 2), lastSuggestedAt = now - 61 * day))
    }

    @Test
    fun `«نه» فقط همون جفت رو می‌بنده نه کلِ قابلیت`() {
        assertNull(compute(mapOf("loan" to 41, "budget" to 2), dismissed = setOf("loan>budget")))
        // همون کاربر، جفتِ دیگه - هنوز پیشنهاد می‌گیره.
        val r = assertNotNull(
            compute(mapOf("cheque" to 41, "budget" to 2), dismissed = setOf("loan>budget")),
        )
        assertEquals("cheque", r.promote)
    }

    @Test
    fun `اسلاتِ صفر - خانه - هیچ‌وقت نامزدِ پایین‌رفتن نیست`() {
        // خانه صفر بار باز شده و کم‌کاربردترینه، ولی قفله؛ پس بودجه انتخاب می‌شه.
        val r = assertNotNull(compute(mapOf("loan" to 41, "home" to 0, "budget" to 3)))
        assertEquals("budget", r.demote)
        assertEquals(3, r.demoteCount)
    }

    @Test
    fun `مقصدی که از قبل تو نواره پیشنهاد نمی‌شه`() {
        assertNull(compute(mapOf("report" to 500, "budget" to 1)))
    }

    @Test
    fun `پنجره سه هفته‌ی پشتِ‌سرهمه`() {
        val w = NavSuggestion.windowWeeks(now)
        assertEquals(3, w.size)
        assertEquals(NavSuggestion.weekKey(now), w[0])
        assertEquals(w[0] - 1, w[1])
        assertEquals(w[0] - 2, w[2])
        assertEquals(w[1], NavSuggestion.weekKey(now - 7 * day))
    }
}
