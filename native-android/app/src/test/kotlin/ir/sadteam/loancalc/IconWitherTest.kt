package ir.sadteam.loancalc

import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.ui.widget.IconWither
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * قاعده‌ی پله‌ی پژمردگی - **از روزِ دوم شروع می‌شود** (تصحیحِ صریحِ کاربر: «نزدیک هشتا
 * طراحیه و دوتا نیست»). یک روز نرفتن هنوز غیبت نیست.
 */
class IconWitherTest {
    private val wither = IconWither()
    private val today = PersianDate(1405, 6, 20)

    @Test
    fun `today and yesterday are step zero`() {
        assertEquals(0, wither.stepFor(today, today))
        assertEquals(0, wither.stepFor(PersianDate(1405, 6, 19), today))
    }

    @Test
    fun `second day away is step one`() {
        assertEquals(1, wither.stepFor(PersianDate(1405, 6, 18), today))
    }

    @Test
    fun `steps stop at eight`() {
        assertEquals(8, wither.stepFor(PersianDate(1405, 6, 11), today))
        assertEquals(8, wither.stepFor(PersianDate(1405, 4, 1), today))
    }

    @Test
    fun `no history means fresh icon`() {
        assertEquals(0, wither.stepFor(null, today))
    }
}
