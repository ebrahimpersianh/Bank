package ir.sadteam.loancalc.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import ir.sadteam.loancalc.server.Db
import ir.sadteam.loancalc.server.env
import ir.sadteam.loancalc.server.execute
import ir.sadteam.loancalc.server.rateLimitOk
import kotlinx.serialization.Serializable

/**
 * آمارِ استفاده‌ی بی‌نام - فقط پنج رویدادِ ثابت، فقط شمارشِ روزانه.
 *   POST /api/events          {"name":"transaction_created"}  - عمومی، با سقفِ نرخ
 *   POST /api/events/batch    - آمارِ کامل به‌ازای نصبِ بی‌نام (رجوع کن به AdminRoutes)
 *   GET  /api/events/summary  - فقط با X-Admin-Token (ورک‌فلوی check-server-health)
 * نامِ ناشناخته رد می‌شود تا کسی نتواند جدول را با اسم‌های دلخواه پر کند.
 */
private val EVENTS = setOf(
    "first_open", "onboarding_completed", "transaction_created", "account_created", "report_viewed",
)

@Serializable
private data class EventBody(val name: String = "")

@Serializable
private data class EventRow(val day: String, val name: String, val count: Int)

@Serializable
private data class BatchEvent(val name: String = "", val count: Int = 1, val day: String? = null)

/**
 * بسته‌ی رویدادهای یک نصب (۷ مهر). [installId] شناسه‌ی تصادفیِ خودِ اپ است - به شماره یا حساب
 * وصل نیست. [loggedIn] فقط «واردشده یا نه» است، نه اینکه چه کسی.
 */
@Serializable
private data class EventBatch(
    val installId: String = "",
    val appVersion: Int? = null,
    val store: String? = null,
    val sdk: Int? = null,
    val loggedIn: Boolean = false,
    val events: List<BatchEvent> = emptyList(),
    /** روزی یک بار - مشخصاتِ بی‌نام (مدل، تنظیمات، تعدادها). ذخیره در `installs.profile`. */
    val profile: Map<String, String>? = null,
)

private val PROFILE_KEY = Regex("^[a-z0-9_]{1,32}$")

/** فقط کلید/مقدارِ کوتاه و امن - تا کسی نتواند چیزِ بزرگ یا دلخواه در جدول بریزد. */
private fun cleanProfile(raw: Map<String, String>?): String? {
    if (raw.isNullOrEmpty()) return null
    val clean = raw.entries.asSequence()
        .filter { PROFILE_KEY.matches(it.key) }
        .take(80)
        .associate { it.key to it.value.take(48) }
    if (clean.isEmpty()) return null
    return kotlinx.serialization.json.Json.encodeToString(
        kotlinx.serialization.json.JsonObject.serializer(),
        kotlinx.serialization.json.JsonObject(clean.mapValues { kotlinx.serialization.json.JsonPrimitive(it.value) }),
    )
}

private val INSTALL_ID = Regex("^[A-Za-z0-9-]{16,64}$")

/** نامِ رویداد: `screen:loan_detail`، `action:loan_added`… - آزاد ولی محدود به حروفِ امن. */
private val EVENT_NAME = Regex("^[a-z0-9_:.\\-]{1,64}$")
private val STORE_NAME = Regex("^[a-z]{1,20}$")

/** روزِ ایران به‌صورتِ `yyyy-MM-dd` - همان `date('now', '+210 minutes')`ِ SQL. */
internal fun iranDay(offsetDays: Long = 0): String =
    java.time.OffsetDateTime.now(java.time.ZoneOffset.ofHoursMinutes(3, 30)).toLocalDate().plusDays(offsetDays).toString()

fun Route.usageRoutes() {
    route("/api/events") {
        post {
            if (!call.rateLimitOk("events", 200, 60 * 60 * 1000L)) return@post
            val name = runCatching { call.receive<EventBody>() }.getOrNull()?.name.orEmpty()
            if (name !in EVENTS) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "unknown_event"))
                return@post
            }
            Db.withConnection { conn ->
                conn.execute(
                    "INSERT INTO usage_events (day, name, count) VALUES (date('now', '+210 minutes'), ?, 1) " +
                        "ON CONFLICT(day, name) DO UPDATE SET count = count + 1",
                    name,
                )
            }
            call.respond(mapOf("ok" to true))
        }
        post("/batch") {
            if (!call.rateLimitOk("events-batch", 120, 60 * 60 * 1000L)) return@post
            val body = runCatching { call.receive<EventBatch>() }.getOrNull()
            if (body == null || !INSTALL_ID.matches(body.installId)) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "bad_batch"))
                return@post
            }
            val today = iranDay()
            val oldest = iranDay(-14)
            // روزِ کلاینت فقط در بازه‌ی دو هفته‌ی اخیر پذیرفته می‌شود (رویدادِ آفلاین)؛ بقیه = امروز.
            val events = body.events.take(300)
                .filter { EVENT_NAME.matches(it.name) }
                .map { e ->
                    val day = e.day?.takeIf { d -> Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(d) && d in oldest..today } ?: today
                    // «time:…» ثانیه است نه تعداد - سقفش یک شبانه‌روز.
                    val cap = if (e.name.startsWith("time:")) 86_400 else 1000
                    Triple(day, e.name, e.count.coerceIn(1, cap))
                }
            if (events.isEmpty()) {
                call.respond(mapOf("ok" to true))
                return@post
            }
            val firstDay = events.minOf { it.first }
            val lastDay = events.maxOf { it.first }
            Db.withConnection { conn ->
                conn.autoCommit = false
                try {
                    // ترتیبِ SET در SQLite روی مقدارِ **قبلیِ** ردیف حساب می‌شود، پس CASE و max هر دو
                    // last_dayِ قدیمی را می‌بینند.
                    conn.execute(
                        """
                        INSERT INTO installs (install_id, first_day, last_day, active_days, app_version, store, sdk, logged_in, profile, last_seen_at)
                        VALUES (?, ?, ?, 1, ?, ?, ?, ?, ?, datetime('now'))
                        ON CONFLICT(install_id) DO UPDATE SET
                            active_days = active_days + (CASE WHEN excluded.last_day > installs.last_day THEN 1 ELSE 0 END),
                            first_day = min(installs.first_day, excluded.first_day),
                            last_day = max(installs.last_day, excluded.last_day),
                            app_version = coalesce(excluded.app_version, installs.app_version),
                            store = coalesce(excluded.store, installs.store),
                            sdk = coalesce(excluded.sdk, installs.sdk),
                            logged_in = excluded.logged_in,
                            profile = coalesce(excluded.profile, installs.profile),
                            last_seen_at = datetime('now')
                        """.trimIndent(),
                        body.installId, firstDay, lastDay,
                        body.appVersion?.takeIf { it in 1..1_000_000 },
                        body.store?.takeIf { STORE_NAME.matches(it) },
                        body.sdk?.takeIf { it in 1..100 },
                        if (body.loggedIn) 1 else 0,
                        cleanProfile(body.profile),
                    )
                    events.forEach { (day, name, count) ->
                        conn.execute(
                            "INSERT INTO usage_daily (day, install_id, name, count) VALUES (?, ?, ?, ?) " +
                                "ON CONFLICT(day, install_id, name) DO UPDATE SET count = count + excluded.count",
                            day, body.installId, name, count,
                        )
                    }
                    conn.commit()
                } catch (e: Exception) {
                    conn.rollback()
                    throw e
                } finally {
                    conn.autoCommit = true
                }
            }
            call.respond(mapOf("ok" to true))
        }
        get("/summary") {
            if (!call.adminOk()) return@get
            val rows = Db.withConnection { conn ->
                conn.prepareStatement(
                    "SELECT day, name, count FROM usage_events WHERE day >= date('now', '-30 days') ORDER BY day DESC, name",
                ).use { ps ->
                    ps.executeQuery().use { rs ->
                        buildList { while (rs.next()) add(EventRow(rs.getString(1), rs.getString(2), rs.getInt(3))) }
                    }
                }
            }
            call.respond(rows)
        }
    }
}

private suspend fun ApplicationCall.adminOk(): Boolean {
    val token = env("ADMIN_TOKEN", "")
    val ok = token.isNotEmpty() && request.headers["X-Admin-Token"] == token
    if (!ok) respond(HttpStatusCode.Unauthorized, mapOf("error" to "admin_only"))
    return ok
}
