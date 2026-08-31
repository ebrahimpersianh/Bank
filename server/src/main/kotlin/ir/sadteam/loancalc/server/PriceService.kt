package ir.sadteam.loancalc.server

/*
 * قیمتِ روزِ طلا/سکه/ارز/رمزارز - منبع: وب‌سرویسِ «قیمت آنلاین» (gheymat.online).
 *
 * مستنداتِ endpoint (اسکرین‌شاتِ کاربر، ۶ شهریور):
 *   GET https://backend.gheymat.online/api/prices/all
 *   Header:  x-api-token: <کلید>
 *   Header:  X-Country-Code: IR   (اجباری طبقِ مستندات)
 *   جوابِ موفق: {"data": [ {symbol, sell_price, buy_price, name:{fa,en,...}, ...}, ... ]}
 *
 * ⚠️ سهمیه‌ی کلیدِ رایگان **۲۵۰ درخواست در روز**ه، ولی چون یه درخواستِ `all` همه‌ی نمادها رو با
 * هم می‌ده، نیازی به زیاد صدازدنش نیست. تازه‌سازی *زمان‌محور*ه: `/api/prices` هر بار صدا زده
 * بشه، فقط اگه بیشتر از یک ساعت از آخرین fetch گذشته باشه دوباره از سرویس می‌گیریم (حداکثر
 * ۲۴ بار در روز - خیلی زیرِ سقفِ ۲۵۰) - وگرنه از کشِ جدولِ price_snapshot جواب می‌دیم.
 *
 * پارس: از آرایه‌ی `data`، برای هر آیتم `symbol` (مثلاً `GOLD-TMN`, `USD-TMN`) و `sell_price`
 * (رشته‌ی عددی) استخراج و به Double تبدیل می‌شه. `sell_price` انتخاب شد چون معنیِ «الان می‌تونی
 * به این قیمت بفروشی»ه - همونی که برای نمایشِ ارزشِ داراییِ کاربر لازمه.
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

object PriceService {
    private val PRICE_API_KEY = env("PRICE_API_KEY")
    private const val GHEYMAT_URL = "https://backend.gheymat.online/api/prices/all"
    private val REFRESH_INTERVAL = Duration.ofHours(1)

    private val httpClient = HttpClient(CIO)
    private val json = Json { ignoreUnknownKeys = true }

    /** جوابِ نهایی برای `/api/prices` - فقط اونی که تو کشه، هیچ‌وقت مستقیم بلاک نمی‌کنه رو سرویسِ بیرونی. */
    suspend fun currentPrices(): Pair<String?, Map<String, Double>> {
        refreshIfStale()
        val (rawJson, fetchedAt) = Db.withConnection { conn ->
            conn.createStatement().use { st ->
                st.executeQuery("SELECT raw_json, fetched_at FROM price_snapshot WHERE id = 1").use { rs ->
                    if (rs.next()) rs.getString("raw_json") to rs.getString("fetched_at") else null to null
                }
            }
        }
        if (rawJson == null) return null to emptyMap()
        return fetchedAt to parsePrices(rawJson)
    }

    private suspend fun refreshIfStale() {
        if (PRICE_API_KEY.isEmpty()) return // کلید ست نشده - فعلاً هیچ کاری نکن، endpoint خالی برمی‌گرده
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
                Log.info("price_fetch_failed", "دریافتِ قیمت از gheymat.online ناموفق بود", "status" to response.status.value)
                return
            }
            val body = response.bodyAsText()
            // مطمئن شو واقعاً JSONِ معتبره قبل از ذخیره (وگرنه یه خطای HTML رو کش می‌کنیم).
            json.parseToJsonElement(body).jsonObject
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
            Log.info("price_fetch_ok", "قیمت‌های gheymat.online تازه‌سازی شد")
        }.onFailure { e ->
            Log.info("price_fetch_error", "خطا در دریافتِ قیمت از gheymat.online", "error" to (e.message ?: "?"))
        }
    }

    /** استخراجِ symbol → sell_price از آرایه‌ی `data` - رجوع کن به هشدارِ بالای فایل. */
    private fun parsePrices(rawJson: String): Map<String, Double> {
        val root = runCatching { json.parseToJsonElement(rawJson).jsonObject }.getOrNull() ?: return emptyMap()
        val items: JsonArray = runCatching { root["data"]?.jsonArray }.getOrNull() ?: return emptyMap()
        val result = mutableMapOf<String, Double>()
        for (item in items) {
            val obj = runCatching { item.jsonObject }.getOrNull() ?: continue
            val symbol = obj["symbol"]?.jsonPrimitive?.content ?: continue
            val price = obj["sell_price"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: continue
            result[symbol] = price
        }
        return result
    }
}
