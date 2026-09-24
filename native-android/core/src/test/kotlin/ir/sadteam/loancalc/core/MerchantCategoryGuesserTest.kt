package ir.sadteam.loancalc.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MerchantCategoryGuesserTest {

    @Test
    fun `کلیدواژه‌ی رستوران خوراک تشخیص داده می‌شود`() {
        assertEquals("خوراک", MerchantCategoryGuesser.guess("خرید از رستوران شاندیز", isWithdrawal = true))
    }

    @Test
    fun `اسنپ‌فود خوراک است نه رفت‌وآمد`() {
        // «اسنپ‌فود» باید قبل از «اسنپ»ِ عمومی چک بشه وگرنه اشتباه رفت‌وآمد می‌شد.
        assertEquals("خوراک", MerchantCategoryGuesser.guess("پرداخت به اسنپ‌فود", isWithdrawal = true))
    }

    @Test
    fun `اسنپِ تنها رفت‌وآمد است`() {
        assertEquals("رفت‌وآمد", MerchantCategoryGuesser.guess("پرداخت به اسنپ", isWithdrawal = true))
    }

    @Test
    fun `متنِ بدونِ کلیدواژه نامطمئن است`() {
        assertNull(MerchantCategoryGuesser.guess("برداشت از حساب شما", isWithdrawal = true))
    }

    @Test
    fun `واریزیِ حقوق تشخیص داده می‌شود`() {
        assertEquals("حقوق", MerchantCategoryGuesser.guess("واریز حقوق شهریورماه", isWithdrawal = false))
    }

    @Test
    fun `کلیدواژه‌ی برداشت برای واریزی استفاده نمی‌شود`() {
        assertNull(MerchantCategoryGuesser.guess("خرید از رستوران شاندیز", isWithdrawal = false))
    }
}
