package ir.sadteam.loancalc.server

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * تستِ پارسِ جوابِ servix.cc.
 *
 * نمونه‌ی زیر دقیقاً شکلِ آرایه‌ی `GET /api/v1/assets`ِ مستنداتِ رسمیه: هر ردیف
 * `{code, name, slug, labelEn, labelFa, quoteUnit, value, businessTime}` داره و `value`
 * مستقیم به ریال (برای کدهای `..._RLS`).
 *
 * سندباکس به خودِ سرویس دسترسی نداره، پس این تست تنها راهِ مطمئن‌شدن از منطقِ پارسه.
 */
class PriceServiceTest {

    private fun row(code: String, value: String, fa: String = "") = """
        {
          "code": "$code",
          "name": "$code",
          "slug": "${code.lowercase()}",
          "labelEn": "",
          "labelFa": "$fa",
          "quoteUnit": "RLS",
          "value": "$value",
          "businessTime": "2026-09-01T00:00:00Z"
        }
    """.trimIndent()

    private fun body(vararg rows: String) = "[${rows.joinToString(",")}]"

    @Test
    fun `قیمت مستقیم به ریال خونده می شود`() {
        val prices = PriceService.mapToCatalog(PriceService.parseQuotes(body(row("GOLD_18_RLS", "70000000"))))
        assertEquals(70_000_000.0, prices["GOLD_18"])
    }

    @Test
    fun `نمادی که سرویس ندارد قیمت نمی گیرد`() {
        val prices = PriceService.mapToCatalog(PriceService.parseQuotes(body(row("GOLD_18_RLS", "1"))))
        // TON/SOL/DOGE/TRX/SILVER_999 تو نگاشتِ کاتالوگ نیستن.
        assertNull(prices["TON"])
        assertNull(prices["SOL"])
        assertNull(prices["SILVER_999"])
    }

    @Test
    fun `ردیفِ خراب کلِ جواب را از بین نمی‌برد`() {
        val broken = """{"code": "X_RLS", "value": "خراب"}"""
        val prices = PriceService.mapToCatalog(PriceService.parseQuotes(body(broken, row("USD_RLS", "900000"))))
        assertEquals(900_000.0, prices["USD"])
    }

    /**
     * فقط دو سکه‌ای که خودِ سرویس دارد نگاشت می‌شوند.
     *
     * ⚠️ این تست قبلاً هر پنج سکه را انتظار داشت، ولی نیم‌سکه/ربع‌سکه/سکه‌ی گرمی به کدهای
     * **ناموجود** نگاشته شده بودند و بی‌صدا «—» می‌دادند؛ در به‌روزرسانیِ ۵۶ نماد عمداً از
     * نگاشت برداشته شدند - رجوع کن به کامنتِ بالای [PriceService]. پس انتظارِ تست غلط بود،
     * نه کد.
     */
    @Test
    fun `فقط سکه‌هایی که سرویس دارد نگاشت می‌شوند`() {
        val prices = PriceService.mapToCatalog(
            PriceService.parseQuotes(
                body(
                    row("SEKKEH_RLS", "1", "سکه امامی"),
                    row("BAHAR_RLS", "2", "سکه بهار آزادی"),
                    row("NIM_SEKKEH_RLS", "3", "نیم سکه"),
                    row("ROB_SEKKEH_RLS", "4", "ربع سکه"),
                    row("GERAMI_SEKKEH_RLS", "5", "سکه گرمی"),
                ),
            ),
        )
        assertEquals(listOf("SEKKE_AZADI", "SEKKE_EMAMI"), prices.keys.sorted())
    }

    @Test
    fun `جوابِ خالی یا بی‌ربط خطا نمی‌دهد`() {
        assertTrue(PriceService.parseQuotes("[]").isEmpty())
        assertTrue(PriceService.parseQuotes("""{"message": "Unauthenticated."}""").isEmpty())
        assertTrue(PriceService.parseQuotes("چیزی که اصلاً JSON نیست").isEmpty())
    }
}
