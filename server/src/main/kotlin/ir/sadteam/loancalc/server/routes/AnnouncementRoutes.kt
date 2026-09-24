package ir.sadteam.loancalc.server.routes

import ir.sadteam.loancalc.server.execute
import ir.sadteam.loancalc.server.queryOne
import ir.sadteam.loancalc.server.optionalUid
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
private data class CreateAnnouncementBody(
    val title: String,
    val body: String,
    val kind: String = "info",
    /** پیامِ اختصاصی: فقط همین کاربر می‌بیند (شناسه یا موبایل؛ هر دو خالی = همگانی). */
    val targetUserId: Long? = null,
    val targetPhone: String? = null,
)

@Serializable
private data class GiftBody(
    val userId: Long? = null,
    val phone: String? = null,
    val days: Int,
    val message: String? = null,
)

@Serializable
private data class GiftResponse(val ok: Boolean = true, val userId: Long, val subscribedUntil: String)

/** شناسه‌ی کاربر از شناسه یا موبایل؛ `null` یعنی چنین کاربری نیست. */
private fun resolveUser(conn: java.sql.Connection, userId: Long?, phone: String?): Long? = when {
    userId != null -> conn.queryOne("SELECT id FROM users WHERE id = ?", userId) { it.getLong("id") }
    !phone.isNullOrBlank() -> conn.queryOne("SELECT id FROM users WHERE phone = ?", phone.trim()) { it.getLong("id") }
    else -> null
}

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
            // همگانی‌ها برای همه؛ اختصاصی‌ها فقط برای صاحبش (با توکن). بی ورود، فقط همگانی.
            val uid = call.optionalUid() ?: -1L
            val items = Db.withConnection { conn ->
                conn.prepareStatement(
                    "SELECT id, title, body, kind, created_at FROM announcements " +
                        "WHERE active = 1 AND id > ? AND (target_user_id IS NULL OR target_user_id = ?) " +
                        "ORDER BY id DESC LIMIT 30",
                ).use { ps ->
                    ps.setLong(1, since)
                    ps.setLong(2, uid)
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
            val wantsTarget = body.targetUserId != null || !body.targetPhone.isNullOrBlank()
            val id = Db.withConnection { conn ->
                val target = if (wantsTarget) resolveUser(conn, body.targetUserId, body.targetPhone) ?: return@withConnection null else null
                conn.insertReturningId(
                    "INSERT INTO announcements (title, body, kind, target_user_id) VALUES (?, ?, ?, ?)",
                    body.title.trim(), body.body.trim(), body.kind, target,
                )
            }
            if (id == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "user_not_found"))
                return@post
            }
            call.respond(CreatedResponse(id = id))
        }

        // 🎁 هدیه‌ی مستقیمِ اشتراک به یک نفر (بی کد) + یک پیامِ اختصاصی که در برنامه‌اش می‌بیند.
        post("/gift") {
            if (!call.isAdmin()) return@post
            val body = runCatching { call.receive<GiftBody>() }.getOrNull()
            if (body == null || body.days !in 1..366) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_request"))
                return@post
            }
            val result = Db.withConnection { conn ->
                val uid = resolveUser(conn, body.userId, body.phone) ?: return@withConnection null
                val currentUntil = conn.queryOne("SELECT subscribed_until FROM users WHERE id = ?", uid) {
                    it.getString("subscribed_until")
                }
                // اشتراکِ فعال از دست نمی‌رود: از انقضای فعلی جلو می‌رویم (همان قاعده‌ی کدِ هدیه).
                val now = System.currentTimeMillis()
                val current = currentUntil?.let { runCatching { java.time.Instant.parse(it).toEpochMilli() }.getOrNull() } ?: 0L
                val base = if (current > now) current else now
                val expiry = java.time.Instant.ofEpochMilli(base + body.days * 24L * 60 * 60 * 1000).toString()
                conn.execute("UPDATE users SET subscribed_until = ? WHERE id = ?", expiry, uid)
                val text = body.message?.trim()?.takeIf { it.isNotEmpty() }?.take(1000)
                    ?: "${body.days} روز اشتراکِ جیبک هدیه گرفتی. ممنون که همراهِ ما هستی!"
                conn.insertReturningId(
                    "INSERT INTO announcements (title, body, kind, target_user_id) VALUES (?, ?, ?, ?)",
                    "🎁 هدیه‌ی اشتراک", text, "info", uid,
                )
                GiftResponse(userId = uid, subscribedUntil = expiry)
            }
            if (result == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "user_not_found"))
                return@post
            }
            call.respond(result)
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
