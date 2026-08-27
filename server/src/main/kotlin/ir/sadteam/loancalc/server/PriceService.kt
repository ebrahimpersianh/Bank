package ir.sadteam.loancalc.server

/*
 * قیمتِ روزِ طلا/سکه/ارز/رمزارز - منبع: وب‌سرویسِ نوسان (navasan.tech).
 *
 * ⚠️ سهمیه‌ی کلیدِ رایگان فقط **۱۲۰ درخواست در ماه**ه (حدودِ ۴ بار در روز). به همین خاطر تازه‌سازی
 * *زمان‌محور*ه نه *درخواست‌محور*: هر بار /api/prices صدا زده می‌شه، فقط اگه از آخرین تازه‌سازیِ
 * واقعی بیشتر از [REFRESH_INTERVAL_HOURS] ساعت گذشته باشه دوباره از نوسان می‌گیریم - وگرنه از
 * کشِ جدولِ price_snapshot جواب می‌دیم. با فاصله‌ی ۶ ساعته دقیقاً ۴ بار در روز = ۱۲۰ بار در ماه،
 * سرِ حدِ مجاز نه بیشتر - صرفِ نظر از اینکه چندتا کاربر چندبار endpoint رو صدا بزنن.
 *
 * ⚠️ ساختارِ دقیقِ پاسخِ نوسان از رو مستنداتِ رسمی تایید نشده (سندباکس نمی‌تونه api.navasan.tech
 * یا لینکِ مستنداتشون رو fetch کنه - egress بسته‌ست). به همین خاطر پارسِ **عمومی**: هر کلید از
 * JSONِ ریشه رو - چه مقدارش عددِ خام باشه چه یه آبجکتِ `{"value": ...}` - به یه عددِ Double تبدیل
 * می‌کنیم و همون‌جوری تو جدول ذخیره می‌کنیم. یعنی هر اسمِ نمادی که نوسان برگردونه (usd_sell,
 * gol18, sekee, ...) خودکار تو خروجی میاد؛ کلاینت (اپِ اندروید) با اسمِ دقیقِ نمادها فیلتر می‌کنه.
 * بعدِ اولین فراخوانیِ واقعی، لاگِ سرور رو چک کن ببین اسمِ نمادها دقیقاً چیه.
 */

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Duration
import java.time.Instant

object PriceService {
    private val PRICE_API_KEY = env("PRICE_API_KEY")
    private const val NAVASAN_URL = "https://api.navasan.tech/latest/"
    private val REFRESH_INTERVAL = Duration.ofHours(6)

    private val httpClient = HttpClient(CIO)
    private val json = Json { ignoreUnknownKeys = true }

    /** جوابِ نهایی برای `/api/prices` - فقط اونی که تو کشه، هیچ‌وقت مستقیم بلاک نمی‌کنه رو نوسان. */
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
            val response = httpClient.get(NAVASAN_URL) {
                url { parameters.append("api_key", PRICE_API_KEY) }
            }
            if (!response.status.isSuccess()) {
                Log.info("price_fetch_failed", "دریافتِ قیمت از نوسان ناموفق بود", "status" to response.status.value)
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
            Log.info("price_fetch_ok", "قیمت‌های نوسان تازه‌سازی شد")
        }.onFailure { e ->
            Log.info("price_fetch_error", "خطا در دریافتِ قیمت از نوسان", "error" to (e.message ?: "?"))
        }
    }

    /** استخراجِ عمومیِ عدد از هر شکلِ مقدار - رجوع کن به هشدارِ بالای فایل. */
    private fun parsePrices(rawJson: String): Map<String, Double> {
        val root = runCatching { json.parseToJsonElement(rawJson).jsonObject }.getOrNull() ?: return emptyMap()
        val result = mutableMapOf<String, Double>()
        for ((key, element) in root) {
            val number = when {
                element is JsonPrimitive -> element.content.toDoubleOrNull()
                else -> runCatching { element.jsonObject["value"]?.jsonPrimitive?.content?.toDoubleOrNull() }.getOrNull()
            }
            if (number != null) result[key] = number
        }
        return result
    }
}
