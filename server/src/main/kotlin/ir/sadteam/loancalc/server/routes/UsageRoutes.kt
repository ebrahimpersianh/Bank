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
