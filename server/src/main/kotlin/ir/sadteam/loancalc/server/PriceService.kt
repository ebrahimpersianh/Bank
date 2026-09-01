package ir.sadteam.loancalc.server

/*
 * قیمتِ روزِ طلا/سکه/ارز/رمزارز - منبع: وب‌سرویسِ «قیمت آنلاین» (gheymat.online).
 *
 * مستنداتِ رسمی (OpenAPI 3.1، کاربر ۱۱ شهریور فرستاد - دیگه حدسی نیست):
 *   GET https://backend.gheymat.online/api/prices/all
 *   Header:  x-api-token: <کلید>        ← اسمِ هدر از `securitySchemes.apiKey` تاییده
 *   Header:  X-Country-Code: IR         (اجباری)
 *   جواب: {"data": [GlobalPriceResource, ...]}
 *
 * ⚠️ **سه چیزی که قبلاً غلط فرض شده بود و مستندات نشون داد:**
 *
 * ۱. `symbol` یه **بازار**ه نه یه ارز - نمونه‌ی خودِ مستندات: `GOLD-TMN`. یعنی جفتِ
 *    «چی به چی». پس تطبیق باید رو `base_currency.symbol` باشه (که از enumِ `Currency`
 *    میاد) و `quote_currency` هم باید ریال/تومن باشه، وگرنه قیمتِ بیت‌کوین به **دلار**
 *    هم قاطیِ نتیجه می‌شد.
 *
 * ۲. `prices/all` یعنی «هر قیمتی که داریم، از **همه‌ی منبع‌ها**» - برای یه بازار چند
 *    ردیف از چند منبع (نوبیتکس، تجارت‌نیوز، میانگین...) برمی‌گرده. پس باید یه منبع
 *    انتخاب بشه، وگرنه هر بار قیمتِ یه منبعِ تصادفی می‌نشست.
 *
 * ۳. اسمِ سکه‌ها هیچ‌کدوم اونی نبود که حدس زده بودم (`EMAMI`/`AZADI`/`NIM`...)؛ enumِ
 *    واقعی `SEKE`/`SEKB`/`SEKEN`/`SEKER`/`SEKG`ه.
 *
 * ⚠️ سهمیه: `all` همه‌ی نمادها رو با هم می‌ده. تازه‌سازی *زمان‌محور*ه نه *درخواست‌محور*:
 * فقط اگه بیشتر از یک ساعت از آخرین fetch گذشته باشه دوباره می‌گیریم.
 *
 * 🔑 **نگاشتِ نمادها عمداً اینجاست، نه تو اپ** - اصلاحش با یه دیپلویِ سرور می‌شه، ولی اگه
 * تو اپ بود هر اصلاح یه انتشارِ جدیدِ اپ می‌خواست. اپ فقط `prices["BTC"]` رو می‌خونه.
 *
 * 💱 **واحد: ریال.** `quote_currency` سرویس تومنه (`IRT`)، پس ×۱۰ می‌شه تا با کلِ اپ یکی باشه.
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
     * نمادِ کاتالوگِ اپ → `base_currency.symbol`ِ سرویس.
     *
     * مقادیرِ سمتِ راست همه از enumِ `Currency`ِ مستنداتِ رسمی‌ان (نه حدس). فهرستِ کاملِ enum:
     * `IRT, SEKB, SEKE, SEKEN, SEKER, SEKEB86, SEKEB86N, SEKEB86R, SEKG, GOLD18M, GOLDO,
     * GOLD18, GOLD24, SILVER999, USD, EUR, GBP, CHF, CAD, AUD, ..., USDT, BTC, ETH, BNB,
     * XRP, BCH, LTC, EOS, XAUT, PAXG`
     *
     * ⚠️ **پنج سکه تنها جاییه که هنوز صددرصد قطعی نیست.** مستندات فقط اسمِ کوتاه رو داده و
     * نگفته کدوم کدومه؛ نگاشتِ زیر از قراردادِ رایجِ سرویس‌های قیمتِ ایرانی اومده
     * (SEK+E=امامی، SEK+B=بهار آزادی، N=نیم، R=ربع، G=گرمی). جوابِ سرویس **نامِ فارسیِ هر
     * نماد** (`name.fa`) رو هم داره و [Snapshot.raw] چاپش می‌کنه - بعدِ اولین fetchِ موفق
     * یه نگاه به همون کافیه تا تایید یا اصلاح بشه.
     *
     * مقصدهایی که تو کاتالوگِ اپ هستن ولی سرویس **اصلاً نداره**: `TRX`, `SOL`, `DOGE`, `TON`.
     * برای این‌ها قیمت نمیاد و اپ «—» نشون می‌ده - این درسته، نه باگ.
     */
    private val CATALOG_TO_BASE: Map<String, String> = mapOf(
        // رمزارز
        "BTC" to "BTC",
        "ETH" to "ETH",
        "USDT" to "USDT",
        "XAUT" to "XAUT",
        "BNB" to "BNB",
        "LTC" to "LTC",
        "XRP" to "XRP",
        // ارز
        "USD" to "USD",
        "EUR" to "EUR",
        "CAD" to "CAD",
        "GBP" to "GBP",
        "TRY" to "TRY",
        "AED" to "AED",
        // طلا و نقره
        "GOLD_24" to "GOLD24",
        "GOLD_18" to "GOLD18",
        "SILVER_999" to "SILVER999",
        // سکه - رجوع کن به هشدارِ بالا
        "SEKKE_EMAMI" to "SEKE",
        "SEKKE_AZADI" to "SEKB",
        "NIM_SEKKE" to "SEKEN",
        "ROB_SEKKE" to "SEKER",
        "SEKKE_GERAMI" to "SEKG",
    )

    /**
     * ترتیبِ اولویتِ منبعِ قیمت.
     *
     * `prices/all` یه بازار رو از چند منبع می‌ده؛ بدونِ ترتیبِ مشخص، قیمتِ نشون‌داده‌شده به
     * ترتیبِ اتفاقیِ آرایه بستگی داشت و هر ساعت می‌پرید. اولویت با **میانگینِ منصفانه**ست،
     * بعد منبعِ خودِ سرویس، بعد بازارهای واقعی. هر منبعی که اینجا نباشه آخرین انتخابه.
     */
    private val SOURCE_PRIORITY = listOf(
        "FAIR_PRICE_AVERAGE", "GHEYMAT", "NAVASAN", "TGJU", "NOBITEX", "BONBAST",
    )

    /** واحدهایی که یعنی «قیمت به پولِ ایران». `IRT` تومنه پس ×۱۰ می‌شه. */
    private val RIAL_QUOTES = setOf("IRT", "TMN", "IRR")

    /** یه ردیفِ `GlobalPriceResource` بعد از پارس. */
    internal data class Quote(
        val base: String,
        val quote: String,
        val source: String,
        val faName: String,
        val sellPrice: Double,
    )

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
            val response = httpClient.get(GHEYMAT_URL) {
                header("x-api-token", PRICE_API_KEY)
                header("X-Country-Code", "IR")
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
     * آرایه‌ی `data` → فهرستِ [Quote].
     *
     * ردیفی که `sell_price`ِ عددی نداره یا `base`/`quote` نداره بی‌صدا رد می‌شه؛ یه ردیفِ
     * خرابِ سرویس نباید کلِ تازه‌سازی رو بی‌نتیجه کنه.
     */
    internal fun parseQuotes(rawJson: String): List<Quote> {
        val root = runCatching { json.parseToJsonElement(rawJson).jsonObject }.getOrNull() ?: return emptyList()
        val items: JsonArray = runCatching { root["data"]?.jsonArray }.getOrNull() ?: return emptyList()
        val out = mutableListOf<Quote>()
        for (item in items) {
            val obj = runCatching { item.jsonObject }.getOrNull() ?: continue
            val base = obj["base_currency"]?.jsonObject?.get("symbol")?.jsonPrimitive?.content ?: continue
            val quote = obj["quote_currency"]?.jsonObject?.get("symbol")?.jsonPrimitive?.content ?: continue
            // `sell_price` تو مستندات **رشته**ست (`"7000"`) نه عدد.
            val price = obj["sell_price"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: continue
            val source = obj["price_source"]?.jsonObject?.get("symbol")?.jsonPrimitive?.content ?: ""
            val faName = obj["name"]?.jsonObject?.get("fa")?.jsonPrimitive?.content ?: ""
            out.add(Quote(base.uppercase(), quote.uppercase(), source.uppercase(), faName, price))
        }
        return out
    }

    /**
     * [Quote]ها → `نمادِ کاتالوگِ اپ ← قیمت به ریال`.
     *
     * سه مرحله: فقط بازارهای ریالی/تومنی می‌مونن · برای هر ارز **یک** منبع طبقِ
     * [SOURCE_PRIORITY] انتخاب می‌شه · تومن به ریال تبدیل می‌شه.
     */
    internal fun mapToCatalog(quotes: List<Quote>): Map<String, Double> {
        if (quotes.isEmpty()) return emptyMap()
        val rialOnly = quotes.filter { it.quote in RIAL_QUOTES }
        val bestByBase = rialOnly
            .groupBy { it.base }
            .mapValues { (_, rows) ->
                rows.minByOrNull { row ->
                    val rank = SOURCE_PRIORITY.indexOf(row.source)
                    if (rank >= 0) rank else SOURCE_PRIORITY.size
                }!!
            }
        val out = mutableMapOf<String, Double>()
        for ((catalogSymbol, baseSymbol) in CATALOG_TO_BASE) {
            val row = bestByBase[baseSymbol] ?: continue
            out[catalogSymbol] = if (row.quote == "IRR") row.sellPrice else row.sellPrice * 10
        }
        return out
    }

    /**
     * چیزی که `GET /api/prices` تو فیلدِ `raw` برمی‌گردونه - **فقط برای دیباگ**.
     *
     * کلید `«ارز | منبع»` و نامِ فارسی هم توشه، چون تنها راهِ تاییدِ نگاشتِ سکه‌ها همینه:
     * بعدِ اولین fetchِ موفق باید دید `SEKE` واقعاً «سکه امامی»ه یا نه.
     */
    private fun debugRaw(quotes: List<Quote>): Map<String, Double> =
        quotes.filter { it.quote in RIAL_QUOTES }
            .associate { "${it.base} | ${it.source} | ${it.faName}" to it.sellPrice }
}
