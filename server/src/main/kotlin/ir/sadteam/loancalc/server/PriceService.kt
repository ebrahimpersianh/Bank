package ir.sadteam.loancalc.server

/*
 * قیمتِ روزِ طلا/سکه/ارز/رمزارز - منبع: وب‌سرویسِ «سرویکس» (servix.cc).
 *
 * جایگزینِ سومِ این کارکرد - نوسان (۴۰۱، کلید رد شد) و gheymat.online (کار می‌کرد ولی
 * نگاشت/تبدیلِ منبع پیچیده بود) قبلاً امتحان شدن. مستنداتِ رسمی: `servix.cc/docs/endpoints`.
 *
 * GET https://servix.cc/api/v1/assets
 * Header: X-API-Key: <کلید>
 * جواب: آرایه‌ای از `{code, name, slug, labelEn, labelFa, quoteUnit, value, businessTime}`.
 *
 * 🎯 **مزیتِ اصلی نسبت به سرویسِ قبلی**: خودِ سرویس جفت‌های `..._RLS` رو مستقیم به **ریال**
 * می‌ده (نه تومن) - نیازی به انتخابِ منبع یا ضرب‌درِ ۱۰ نیست. یه درخواستِ `GET /api/v1/assets`
 * همه‌ی نمادها رو با هم می‌ده و فقط **۱ واحد از سهمیه‌ی روزانه** کم می‌کنه.
 *
 * ⚠️ سهمیه: تازه‌سازی *زمان‌محور*ه نه *درخواست‌محور* - فقط اگه بیشتر از یک ساعت از آخرین
 * fetch گذشته باشه دوباره می‌گیریم (۲۴ درخواست در روز، خیلی زیرِ سقفِ ۵۰).
 *
 * 🔑 **نگاشتِ نمادها عمداً اینجاست، نه تو اپ** - اصلاحش با یه دیپلویِ سرور می‌شه، ولی اگه
 * تو اپ بود هر اصلاح یه انتشارِ جدیدِ اپ می‌خواست. اپ فقط `prices["BTC"]` رو می‌خونه.
 *
 * مقصدهایی که تو کاتالوگِ اپ هستن ولی سرویس معادلِ ریالیِ مستقیم نداره: `SILVER_999`
 * (فقط `SILVER_OUNCE_USD` داره، نه یه جفتِ `..._RLS`)، `TRX`, `SOL`, `DOGE`, `TON`.
 * برای این‌ها قیمت نمیاد و اپ «—» نشون می‌ده - این درسته، نه باگ.
 */

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

object PriceService {
    private val PRICE_API_KEY = env("PRICE_API_KEY")
    private const val SERVIX_URL = "https://servix.cc/api/v1/assets"
    private val REFRESH_INTERVAL = Duration.ofHours(1)

    private val httpClient = HttpClient(CIO)
    private val json = Json { ignoreUnknownKeys = true }

    /** نمادِ کاتالوگِ اپ → کدِ نمادِ سرویکس. همه‌ی مقصدها مستقیم به ریال (`_RLS`) هستن. */
    private val CATALOG_TO_CODE: Map<String, String> = mapOf(
        // رمزارز
        "BTC" to "BTC_RLS",
        "ETH" to "ETH_RLS",
        "USDT" to "USDT_RLS",
        "XAUT" to "XAUT_RLS",
        "BNB" to "BNB_RLS",
        "LTC" to "LTC_RLS",
        "XRP" to "XRP_RLS",
        // ارز
        "USD" to "USD_RLS",
        "EUR" to "EUR_RLS",
        "CAD" to "CAD_RLS",
        "GBP" to "GBP_RLS",
        "TRY" to "TRY_RLS",
        "AED" to "AED_RLS",
        // طلا
        "GOLD_24" to "GOLD_24_RLS",
        "GOLD_18" to "GOLD_18_RLS",
        // سکه
        "SEKKE_EMAMI" to "SEKKEH_RLS",
        "SEKKE_AZADI" to "BAHAR_RLS",
        "NIM_SEKKE" to "NIM_SEKKEH_RLS",
        "ROB_SEKKE" to "ROB_SEKKEH_RLS",
        "SEKKE_GERAMI" to "GERAMI_SEKKEH_RLS",
    )

    /** یه ردیفِ جوابِ سرویکس بعد از پارس. */
    internal data class Quote(val code: String, val faName: String, val value: Double)

    data class Snapshot(
        val updatedAt: String?,
        /** کلیدها نمادِ **کاتالوگِ اپ**ن (`BTC`, `GOLD_18`, ...) و مقدارها **ریال**ن. */
        val prices: Map<String, Double>,
        /** همون چیزی که سرویس داد، دست‌نخورده - فقط برای دیباگِ اسمِ نمادها. */
        val raw: Map<String, Double>,
    )

    suspend fun currentPrices(): Snapshot {
        refreshIfStale()
        val (rawJson, fetchedAt) = Db.withConnection { conn ->
            conn.createStatement().use { st ->
                st.executeQuery("SELECT raw_json, fetched_at FROM price_snapshot WHERE id = 1").use { rs ->
                    if (rs.next()) rs.getString("raw_json") to rs.getString("fetched_at") else null to null
                }
            }
        }
        if (rawJson == null) return Snapshot(null, emptyMap(), emptyMap())
        val quotes = parseQuotes(rawJson)
        return Snapshot(fetchedAt, mapToCatalog(quotes), debugRaw(quotes))
    }

    /** تاریخچه‌ی یه نماد - از **اسنپ‌شاتِ روزانه‌ی خودمون**، نه سرویس (سهمیه‌ی اضافه نمی‌خواد). */
    fun history(symbol: String, days: Int): List<Pair<String, Double>> =
        Db.withConnection { conn ->
            conn.prepareStatement(
                "SELECT date, price FROM price_history WHERE symbol = ? ORDER BY date DESC LIMIT ?",
            ).use { ps ->
                ps.setString(1, symbol)
                ps.setInt(2, days.coerceIn(1, 400))
                ps.executeQuery().use { rs ->
                    val out = mutableListOf<Pair<String, Double>>()
                    while (rs.next()) out.add(rs.getString("date") to rs.getDouble("price"))
                    out.reversed() // قدیمی → جدید، همون چیزی که نمودار می‌خواد
                }
            }
        }

    private suspend fun refreshIfStale() {
        if (PRICE_API_KEY.isEmpty()) return // کلید ست نشده - endpoint خالی برمی‌گرده، خطا نه
        val lastFetch = Db.withConnection { conn ->
            conn.createStatement().use { st ->
                st.executeQuery("SELECT fetched_at FROM price_snapshot WHERE id = 1").use { rs ->
                    if (rs.next()) rs.getString("fetched_at") else null
                }
            }
        }
        val stale = lastFetch == null ||
            Duration.between(Instant.parse(lastFetch), Instant.now()) >= REFRESH_INTERVAL
        if (!stale) return

        runCatching {
            val response = httpClient.get(SERVIX_URL) {
                header("X-API-Key", PRICE_API_KEY)
            }
            if (!response.status.isSuccess()) {
                // ⚠️ **بدنه‌ی جواب هم لاگ می‌شه.** قبلاً فقط عددِ وضعیت لاگ می‌شد و یه
                // `status=401` تنها نمی‌گفت چرا - یه نشستِ کامل صرفِ حدس‌زدنش شد.
                Log.info(
                    "price_fetch_failed",
                    "دریافتِ قیمت ناموفق بود",
                    "status" to response.status.value,
                    "body" to response.bodyAsText().take(200),
                )
                return
            }
            val body = response.bodyAsText()
            json.parseToJsonElement(body).jsonArray // اعتبارسنجی قبل از ذخیره
            Db.withConnection { conn ->
                conn.prepareStatement(
                    """
                    INSERT INTO price_snapshot (id, raw_json, fetched_at) VALUES (1, ?, ?)
                    ON CONFLICT(id) DO UPDATE SET raw_json = excluded.raw_json, fetched_at = excluded.fetched_at
                    """.trimIndent(),
                ).use { ps ->
                    ps.setString(1, body)
                    ps.setString(2, Instant.now().toString())
                    ps.executeUpdate()
                }
            }
            val mapped = mapToCatalog(parseQuotes(body))
            writeDailyHistory(mapped)
            Log.info("price_fetch_ok", "قیمت‌ها تازه‌سازی شد", "symbols" to mapped.size)
        }.onFailure { e ->
            Log.info("price_fetch_error", "خطا در دریافتِ قیمت", "error" to (e.message ?: "?"))
        }
    }

    /**
     * یه ردیف در روز برای هر نماد. کلیدِ یکتای `(symbol, date)` یعنی fetchهای بعدیِ همون روز
     * قیمتِ همون روز رو **به‌روز** می‌کنن (آخرین قیمتِ روز می‌مونه)، نه اینکه ردیفِ تکراری بسازن.
     */
    private fun writeDailyHistory(prices: Map<String, Double>) {
        if (prices.isEmpty()) return
        val today = LocalDate.now().toString()
        Db.withConnection { conn ->
            conn.prepareStatement(
                """
                INSERT INTO price_history (symbol, date, price) VALUES (?, ?, ?)
                ON CONFLICT(symbol, date) DO UPDATE SET price = excluded.price
                """.trimIndent(),
            ).use { ps ->
                for ((symbol, price) in prices) {
                    ps.setString(1, symbol)
                    ps.setString(2, today)
                    ps.setDouble(3, price)
                    ps.addBatch()
                }
                ps.executeBatch()
            }
        }
    }

    /**
     * آرایه‌ی ریشه → فهرستِ [Quote].
     *
     * ردیفی که `value`ِ عددی نداره یا `code` نداره بی‌صدا رد می‌شه؛ یه ردیفِ خرابِ سرویس
     * نباید کلِ تازه‌سازی رو بی‌نتیجه کنه.
     */
    internal fun parseQuotes(rawJson: String): List<Quote> {
        val items = runCatching { json.parseToJsonElement(rawJson).jsonArray }.getOrNull() ?: return emptyList()
        val out = mutableListOf<Quote>()
        for (item in items) {
            val obj = runCatching { item.jsonObject }.getOrNull() ?: continue
            val code = obj["code"]?.jsonPrimitive?.content ?: continue
            // `value` ممکنه رشته یا عدد باشه بسته به نسخه‌ی سرویس؛ هر دو رو می‌پذیریم.
            val value = obj["value"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: continue
            val faName = obj["labelFa"]?.jsonPrimitive?.content ?: ""
            out.add(Quote(code.uppercase(), faName, value))
        }
        return out
    }

    /** [Quote]ها → `نمادِ کاتالوگِ اپ ← قیمت به ریال`. */
    internal fun mapToCatalog(quotes: List<Quote>): Map<String, Double> {
        if (quotes.isEmpty()) return emptyMap()
        val byCode = quotes.associateBy { it.code }
        val out = mutableMapOf<String, Double>()
        for ((catalogSymbol, code) in CATALOG_TO_CODE) {
            val row = byCode[code] ?: continue
            out[catalogSymbol] = row.value
        }
        return out
    }

    /** چیزی که `GET /api/prices` تو فیلدِ `raw` برمی‌گردونه - **فقط برای دیباگ**. */
    private fun debugRaw(quotes: List<Quote>): Map<String, Double> =
        quotes.associate { "${it.code} | ${it.faName}" to it.value }
}
