package ir.sadteam.loancalc.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BankAppMatcherTest {
    @Test
    fun matches_real_bank_apps() {
        assertTrue(BankAppMatcher.looksLikeBankApp("بلوبانک", "com.blubank.mobile"))
        assertTrue(BankAppMatcher.looksLikeBankApp("Blu Bank", "ir.sample.blu"))
        assertTrue(BankAppMatcher.looksLikeBankApp("همراه بانک ملت", "com.example.mellat"))
        assertTrue(BankAppMatcher.looksLikeBankApp("آسان پرداخت", "ir.asanpardakht.android"))
        assertTrue(BankAppMatcher.looksLikeBankApp("کیف پول من", "com.example.something"))
    }

    @Test
    fun matches_by_package_when_label_is_english_brandname() {
        // اسمِ اپ هیچ کلیدواژه‌ای نداره، ولی بسته‌نام بانک رو لو می‌ده.
        assertTrue(BankAppMatcher.looksLikeBankApp("Sepehr", "ir.sepah.mobilebank"))
        assertTrue(BankAppMatcher.looksLikeBankApp("Hamrah", "com.pasargad.mobile"))
    }

    @Test
    fun rejects_the_junk_that_filled_the_old_list() {
        // دقیقاً همون‌هایی که کاربر تو اسکرین‌شات دید.
        val junk = listOf(
            "Camera" to "com.android.camera",
            "Compass" to "com.miui.compass",
            "FM Radio" to "com.miui.fmradio",
            "SIM toolkit" to "com.android.stk",
            "Screen Recorder" to "com.miui.screenrecorder",
            "Gallery" to "com.miui.gallery",
            "Game Centre" to "com.xiaomi.glgm",
            "Google Play Store" to "com.android.vending",
            "Mi Video" to "com.miui.videoplayer",
            "Notes" to "com.miui.notes",
            "ShareMe" to "com.xiaomi.midrop",
        )
        junk.forEach { (label, pkg) ->
            assertFalse(BankAppMatcher.looksLikeBankApp(label, pkg), "نباید بانکی حساب بشه: $label")
        }
    }

    @Test
    fun short_names_need_exact_match_not_contains() {
        // ⚠️ «آپ» اسمِ یه اپِ پرداختِ واقعیه؛ ولی نباید آپارات رو بگیره.
        assertTrue(BankAppMatcher.looksLikeBankApp("آپ", "ir.asanpardakht.ap"))
        assertFalse(BankAppMatcher.looksLikeBankApp("آپارات", "com.aparat"))
        // «تاپ» در برابرِ «لپ‌تاپ»
        assertTrue(BankAppMatcher.looksLikeBankApp("تاپ", "ir.top.example"))
        assertFalse(BankAppMatcher.looksLikeBankApp("لپ تاپ", "com.example.laptopshop"))
    }

    @Test
    fun split_puts_banks_first_and_keeps_everything() {
        val apps = listOf(
            "com.miui.compass" to "Compass",
            "com.blubank.mobile" to "بلوبانک",
            "com.miui.gallery" to "Gallery",
            "ir.bmi.bam" to "بام",
        )
        val (suggested, rest) = BankAppMatcher.split(apps)
        assertEquals(listOf("بلوبانک", "بام"), suggested.map { it.second })
        assertEquals(listOf("Compass", "Gallery"), rest.map { it.second })
        // هیچ اپی گم نمی‌شه - کاربر باید بتونه هرچی خواست انتخاب کنه.
        assertEquals(apps.size, suggested.size + rest.size)
    }
}
