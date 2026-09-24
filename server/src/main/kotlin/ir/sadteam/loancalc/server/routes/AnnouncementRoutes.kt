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
import ir.sadteam.loancalc.server.executeCounting
import ir.sadteam.loancalc.server.insertReturningId
import kotlinx.serialization.Serializable

/*
 * «پیام‌های جیبک» - اطلاعیه‌های عمومی از طرفِ صاحبِ برنامه برای همه‌ی کاربرها.
 *
 *   GET  /api/announcements?since=<id>   - عمومی، بی‌ورود؛ اطلاعیه‌های فعالِ بعد از آن id
 *   POST /api/announcements/create       - فقط با X-Admin-Token (ورک‌فلوی send-announcement)
 *   POST /api/announcements/deactivate   - فقط با X-Admin-Token؛ اطلاعیه را از فهرست برمی‌دارد
 *
 * داده‌ی هیچ کاربری این‌جا نیست، پس GET عمومی است؛ اپ آن‌ها را در صندوقِ پیام‌های خودش نگه می‌دارد.
 */
private val KINDS = setOf("update", "outage", "feature", "info")

@Serializable
data class AnnouncementDto(val id: Long, val title: String, val body: String, val kind: String, val createdAt: String)

@Serializable
private data class AnnouncementsResponse(val items: List<AnnouncementDto>)

@Serializable
private data class CreateAnnouncementBody(val title: String, val body: String, val kind: String = "info")

@Serializable
private data class DeactivateBody(val id: Long)

@Serializable
private data class CreatedResponse(val ok: Boolean = true, val id: Long)

@Serializable
private data class OkResponse(val ok: Boolean)

private suspend fun ApplicationCall.isAdmin(): Boolean {
    // ⚠️ بی `ADMIN_TOKEN` نوشتن **اصلاً باز نمی‌شود** - همان قاعده‌ی کدِ هدیه.
    val adminToken = env("ADMIN_TOKEN", "")
    val ok = adminToken.isNotEmpty() && request.headers["X-Admin-Token"] == adminToken
    if (!ok) respond(HttpStatusCode.Unauthorized, mapOf("error" to "admin_only"))
    return ok
}

fun Route.announcementRoutes() {
    route("/api/announcements") {
        get {
            val since = call.request.queryParameters["since"]?.toLongOrNull() ?: 0L
            val items = Db.withConnection { conn ->
                conn.prepareStatement(
                    "SELECT id, title, body, kind, created_at FROM announcements " +
                        "WHERE active = 1 AND id > ? ORDER BY id DESC LIMIT 30",
                ).use { ps ->
                    ps.setLong(1, since)
                    ps.executeQuery().use { rs ->
                        buildList {
                            while (rs.next()) {
                                add(
                                    AnnouncementDto(
                                        id = rs.getLong("id"),
                                        title = rs.getString("title"),
                                        body = rs.getString("body"),
                                        kind = rs.getString("kind"),
                                        createdAt = rs.getString("created_at"),
                                    ),
                                )
                            }
                        }
                    }
                }
            }
            call.respond(AnnouncementsResponse(items))
        }

        post("/create") {
            if (!call.isAdmin()) return@post
            val body = runCatching { call.receive<CreateAnnouncementBody>() }.getOrNull()
            if (body == null || body.title.isBlank() || body.body.isBlank() ||
                body.title.length > 120 || body.body.length > 1000 || body.kind !in KINDS
            ) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_request"))
                return@post
            }
            val id = Db.withConnection { conn ->
                conn.insertReturningId(
                    "INSERT INTO announcements (title, body, kind) VALUES (?, ?, ?)",
                    body.title.trim(), body.body.trim(), body.kind,
                )
            }
            call.respond(CreatedResponse(id = id))
        }

        post("/deactivate") {
            if (!call.isAdmin()) return@post
            val body = runCatching { call.receive<DeactivateBody>() }.getOrNull()
            if (body == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_request"))
                return@post
            }
            val n = Db.withConnection { conn ->
                conn.executeCounting("UPDATE announcements SET active = 0 WHERE id = ?", body.id)
            }
            call.respond(OkResponse(n > 0))
        }
    }
}
