package ir.sadteam.loancalc.server

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * تستِ پارسِ جوابِ gheymat.online.
 *
 * نمونه‌ی زیر **دقیقاً شکلِ `GlobalPriceResource`ِ مستنداتِ رسمی**ه: `symbol` بازاره
 * (`GOLD-TMN`)، `sell_price` رشته‌ست، و برای یه بازار چند منبع میاد.
 *
 * سندباکس به خودِ سرویس دسترسی نداره، پس این تست تنها راهِ مطمئن‌شدن از منطقِ پارسه.
 */
class PriceServiceTest {

    private fun row(
        base: String,
        quote: String = "IRT",
        source: String,
        sell: String,
        fa: String = "",
    ) = """
        {
          "symbol": "$base-TMN",
          "price_source": {"symbol": "$source", "fa": "", "en": "", "link": "", "image": ""},
          "name": {"symbol": "$base", "fa": "$fa", "en": "", "prefix": "", "postfix": "", "suffix": "", "png": "", "category": {"symbol": "GOLD", "fa": "", "en": "", "image": ""}},
          "base_currency": {"symbol": "$base", "fa": "$fa", "en": "", "prefix": "", "postfix": "", "suffix": "", "png": "", "category": {"symbol": "GOLD", "fa": "", "en": "", "image": ""}},
          "quote_currency": {"symbol": "$quote", "fa": "", "en": "", "prefix": "", "postfix": "", "suffix": "", "png": "", "category": {"symbol": "CURRENCY", "fa": "", "en": "", "image": ""}},
          "png": "", "sell_price": "$sell", "buy_price": "$sell", "is_tradable": true,
          "support": "0", "resistance": "0", "buy_fee": "0", "sell_fee": "0",
          "change": "1.54", "bubble": "0", "real_value": "0", "bubble_percentage": "0",
          "chart": null, "created_at": "2026-09-01T00:00:00Z", "is_up_to_date": true
        }
    """.trimIndent()

    private fun body(vararg rows: String) = """{"data": [${rows.joinToString(",")}]}"""

    @Test
    fun `تومن به ریال تبدیل می‌شود`() {
        val prices = PriceService.mapToCatalog(
            PriceService.parseQuotes(body(row("GOLD18", source = "GHEYMAT", sell = "7000000"))),
        )
        assertEquals(70_000_000.0, prices["GOLD_18"])
    }

    @Test
    fun `بین چند منبع، اولویت رعایت می‌شود`() {
        // ترتیبِ آرایه عمداً برعکسِ اولویته تا معلوم بشه انتخاب واقعاً بر اساسِ اولویته.
        val prices = PriceService.mapToCatalog(
            PriceService.parseQuotes(
                body(
                    row("BTC", source = "NOBITEX", sell = "100"),
                    row("BTC", source = "FAIR_PRICE_AVERAGE", sell = "200"),
                    row("BTC", source = "TGJU", sell = "300"),
                ),
            ),
        )
        assertEquals(2000.0, prices["BTC"])
    }

    @Test
    fun `منبعِ ناشناخته وقتی تنها گزینه است استفاده می‌شود`() {
        val prices = PriceService.mapToCatalog(
            PriceService.parseQuotes(body(row("USD", source = "ALTEX", sell = "50"))),
        )
        assertEquals(500.0, prices["USD"])
    }

    @Test
    fun `بازارِ غیرریالی کنار گذاشته می‌شود`() {
        // بیت‌کوین به دلار - نباید به‌عنوانِ قیمتِ ریالیِ بیت‌کوین ثبت بشه.
        val prices = PriceService.mapToCatalog(
            PriceService.parseQuotes(body(row("BTC", quote = "USDT", source = "BINANCE", sell = "65000"))),
        )
        assertNull(prices["BTC"])
    }

    @Test
    fun `نمادی که سرویس ندارد قیمت نمی‌گیرد`() {
        val prices = PriceService.mapToCatalog(
            PriceService.parseQuotes(body(row("GOLD18", source = "GHEYMAT", sell = "1"))),
        )
        // TON/SOL/DOGE/TRX تو enumِ سرویس نیستن.
        assertNull(prices["TON"])
        assertNull(prices["SOL"])
    }

    @Test
    fun `ردیفِ خراب کلِ جواب را از بین نمی‌برد`() {
        val broken = """{"symbol": "X-TMN", "sell_price": "خراب"}"""
        val prices = PriceService.mapToCatalog(
            PriceService.parseQuotes(body(broken, row("USD", source = "BONBAST", sell = "90000"))),
        )
        assertEquals(900_000.0, prices["USD"])
    }

    @Test
    fun `هر پنج سکه نگاشت دارند`() {
        val prices = PriceService.mapToCatalog(
            PriceService.parseQuotes(
                body(
                    row("SEKE", source = "GHEYMAT", sell = "1", fa = "سکه امامی"),
                    row("SEKB", source = "GHEYMAT", sell = "2", fa = "بهار آزادی"),
                    row("SEKEN", source = "GHEYMAT", sell = "3", fa = "نیم سکه"),
                    row("SEKER", source = "GHEYMAT", sell = "4", fa = "ربع سکه"),
                    row("SEKG", source = "GHEYMAT", sell = "5", fa = "سکه گرمی"),
                ),
            ),
        )
        assertEquals(
            listOf("SEKKE_EMAMI", "SEKKE_AZADI", "NIM_SEKKE", "ROB_SEKKE", "SEKKE_GERAMI").sorted(),
            prices.keys.sorted(),
        )
    }

    @Test
    fun `جوابِ خالی یا بی‌ربط خطا نمی‌دهد`() {
        assertTrue(PriceService.parseQuotes("""{"data": []}""").isEmpty())
        assertTrue(PriceService.parseQuotes("""{"message": "Unauthenticated."}""").isEmpty())
        assertTrue(PriceService.parseQuotes("چیزی که اصلاً JSON نیست").isEmpty())
    }
}
