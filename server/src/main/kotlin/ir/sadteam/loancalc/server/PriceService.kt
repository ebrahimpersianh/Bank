package ir.sadteam.loancalc.server

/*
 * قیمتِ روزِ طلا/سکه/ارز/رمزارز - منبع: وب‌سرویسِ «قیمت آنلاین» (gheymat.online).
 *
 * مستنداتِ endpoint (اسکرین‌شاتِ کاربر، ۸ شهریور):
 *   GET https://backend.gheymat.online/api/prices/all
 *   Header:  x-api-token: <کلید>
 *   Header:  X-Country-Code: IR   (اجباری طبقِ مستندات)
 *   جوابِ موفق: {"data": [ {symbol, sell_price, buy_price, name:{fa,en,...}, ...}, ... ]}
 *
 * ⚠️ سهمیه **۲۵۰ درخواست در روز**ه و یه درخواستِ `all` همه‌ی نمادها رو با هم می‌ده. تازه‌سازی
 * *زمان‌محور*ه نه *درخواست‌محور*: فقط اگه بیشتر از یک ساعت از آخرین fetch گذشته باشه دوباره
 * می‌گیریم (حداکثر ۲۴ بار در روز، خیلی زیرِ سقف) - وگرنه از کشِ `price_snapshot` جواب می‌دیم.
 *
 * 🔑 **نگاشتِ نمادها عمداً اینجاست، نه تو اپ.** اسمِ نمادهای این سرویس (`GOLD-TMN`, `BTC-TMN`,
 * ...) هنوز کامل تایید نشده؛ اگه اینجا باشه با یه دیپلویِ سرور اصلاح می‌شه، ولی اگه تو اپ بود
 * هر اصلاح یه انتشارِ جدیدِ اپ می‌خواست. اپ فقط `prices["BTC"]` رو می‌خونه.
 *
 * 💱 **واحد: ریال.** نمادهای این سرویس تومنی‌ان (پسوندِ `-TMN`)، پس ×۱۰ می‌شن تا با کلِ اپ
 * (که همه‌جا ریال نگه می‌داره) یکی باشن.
 */

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

object PriceService {
    private val PRICE_API_KEY = env("PRICE_API_KEY")
    private const val GHEYMAT_URL = "https://backend.gheymat.online/api/prices/all"
    private val REFRESH_INTERVAL = Duration.ofHours(1)

    private val httpClient = HttpClient(CIO)
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * نمادِ کاتالوگِ اپ → نمادهای احتمالیِ سرویس (به ترتیبِ اولویت).
     *
     * تطبیق **بعد از نرمال‌سازی** انجام می‌شه (حروفِ بزرگ + حذفِ `-TMN`/`-IRT`/`-IRR`/`_`)، پس
     * لازم نیست همه‌ی شکل‌ها اینجا باشن. اگه بعدِ اولین fetch دیدی نمادی جا افتاده، فقط همین
     * جدول رو اصلاح کن - اپ دست نمی‌خوره.
     */
    private val SYMBOL_ALIASES: Map<String, List<String>> = mapOf(
        // رمزارز
        "BTC" to listOf("BTC", "BITCOIN"),
        "ETH" to listOf("ETH", "ETHEREUM"),
        "USDT" to listOf("USDT", "TETHER"),
        "XAUT" to listOf("XAUT", "TETHERGOLD"),
        "BNB" to listOf("BNB"),
        "TRX" to listOf("TRX", "TRON"),
        "LTC" to listOf("LTC"),
        "SOL" to listOf("SOL"),
        "XRP" to listOf("XRP"),
        "DOGE" to listOf("DOGE"),
        "TON" to listOf("TON"),
        // ارز
        "USD" to listOf("USD", "DOLLAR"),
        "EUR" to listOf("EUR"),
        "CAD" to listOf("CAD"),
        "GBP" to listOf("GBP"),
        "TRY" to listOf("TRY", "LIR"),
        "AED" to listOf("AED", "DIRHAM"),
        // طلا و سکه
        "GOLD_24" to listOf("GOLD24", "GOLD", "XAU24"),
        "GOLD_18" to listOf("GOLD18", "GOLD"),
        "SILVER_999" to listOf("SILVER999", "SILVER", "XAG"),
        "SEKKE_EMAMI" to listOf("EMAMI", "SEKEEMAMI", "COINEMAMI"),
        "SEKKE_AZADI" to listOf("AZADI", "SEKEAZADI", "COIN"),
        "NIM_SEKKE" to listOf("NIM", "HALFCOIN", "NIMSEKE"),
        "ROB_SEKKE" to listOf("ROB", "QUARTERCOIN", "ROBSEKE"),
        "SEKKE_GERAMI" to listOf("GERAMI", "GRAMCOIN", "SEKEGERAMI"),
    )

    /** حذفِ پسوندِ واحد و جداکننده‌ها تا `GOLD-TMN` و `gold_tmn` و `GOLD` یکی دیده بشن. */
    private fun normalize(symbol: String): String =
        symbol.uppercase()
            .replace("-", "")
            .replace("_", "")
            .removeSuffix("TMN")
            .removeSuffix("IRT")
            .removeSuffix("IRR")

    /** آیا این نمادِ سرویس تومنیه؟ (برای تبدیل به ریال) */
    private fun isToman(symbol: String): Boolean {
        val s = symbol.uppercase()
        return s.endsWith("TMN") || s.endsWith("IRT")
    }

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
        val raw = parseRaw(rawJson)
        return Snapshot(fetchedAt, mapToCatalog(raw), raw)
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
            val response = httpClient.get(GHEYMAT_URL) {
                header("x-api-token", PRICE_API_KEY)
                header("X-Country-Code", "IR")
            }
            if (!response.status.isSuccess()) {
                Log.info("price_fetch_failed", "دریافتِ قیمت ناموفق بود", "status" to response.status.value)
                return
            }
            val body = response.bodyAsText()
            json.parseToJsonElement(body).jsonObject // اعتبارسنجی قبل از ذخیره
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
            val mapped = mapToCatalog(parseRaw(body))
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

    /** آرایه‌ی `data` → نمادِ خامِ سرویس ← قیمت (به ریال). */
    private fun parseRaw(rawJson: String): Map<String, Double> {
        val root = runCatching { json.parseToJsonElement(rawJson).jsonObject }.getOrNull() ?: return emptyMap()
        val items: JsonArray = runCatching { root["data"]?.jsonArray }.getOrNull() ?: return emptyMap()
        val result = mutableMapOf<String, Double>()
        for (item in items) {
            val obj = runCatching { item.jsonObject }.getOrNull() ?: continue
            val symbol = obj["symbol"]?.jsonPrimitive?.content ?: continue
            val price = obj["sell_price"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: continue
            result[symbol] = if (isToman(symbol)) price * 10 else price
        }
        return result
    }

    /** نمادِ خامِ سرویس → نمادِ کاتالوگِ اپ، طبقِ [SYMBOL_ALIASES]. */
    private fun mapToCatalog(raw: Map<String, Double>): Map<String, Double> {
        if (raw.isEmpty()) return emptyMap()
        val byNormalized = raw.entries.associate { (k, v) -> normalize(k) to v }
        val out = mutableMapOf<String, Double>()
        for ((catalogSymbol, aliases) in SYMBOL_ALIASES) {
            for (alias in aliases) {
                val price = byNormalized[normalize(alias)]
                if (price != null) {
                    out[catalogSymbol] = price
                    break
                }
            }
        }
        return out
    }
}
