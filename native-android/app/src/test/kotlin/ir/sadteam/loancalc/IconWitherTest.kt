package ir.sadteam.loancalc

import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.ui.widget.IconWither
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * قاعده‌ی چهار حالتِ پژمردگی (تصمیمِ کاربر، ۳۱ شهریور): امروز ۰ · یک روز سرنزده ۱ ·
 * دو روز ۲ · سه روز و بیشتر ۳. ارفاقِ «یک روز» دیگر نیست.
 */
class IconWitherTest {
    private val wither = IconWither()
    private val today = PersianDate(1405, 6, 20)

    @Test
    fun `today is step zero`() {
        assertEquals(0, wither.stepFor(today, today))
    }

    @Test
    fun `one and two days away are steps one and two`() {
        assertEquals(1, wither.stepFor(PersianDate(1405, 6, 19), today))
        assertEquals(2, wither.stepFor(PersianDate(1405, 6, 18), today))
    }

    @Test
    fun `steps stop at three`() {
        assertEquals(3, wither.stepFor(PersianDate(1405, 6, 17), today))
        assertEquals(3, wither.stepFor(PersianDate(1405, 4, 1), today))
    }

    @Test
    fun `no history means fresh icon`() {
        assertEquals(0, wither.stepFor(null, today))
    }
}
