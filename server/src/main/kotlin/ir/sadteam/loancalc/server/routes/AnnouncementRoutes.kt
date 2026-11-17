package ir.sadteam.loancalc.server.routes

import ir.sadteam.loancalc.server.execute
import ir.sadteam.loancalc.server.queryOne
import ir.sadteam.loancalc.server.optionalUid
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.header
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import ir.sadteam.loancalc.server.Db
import ir.sadteam.loancalc.server.adminTokenMatches
import ir.sadteam.loancalc.server.env
import ir.sadteam.loancalc.server.executeCounting
import ir.sadteam.loancalc.server.giftBaseMs
import ir.sadteam.loancalc.server.insertReturningId
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ir.sadteam.loancalc.server.Log

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
data class AnnouncementDto(
    val id: Long, val title: String, val body: String, val kind: String, val createdAt: String, val coins: Int = 0, val action: String? = null, val imageId: String? = null,
    /** کی این کاربر پیام را باز کرد (برای پیامِ همگانی از جدولِ announcement_reads، برای اختصاصی از read_atِ ردیف)؛ null یعنی هنوز باز نشده. */
    val readAt: String? = null,
    /** شناسه‌ی پیامِ گروهی (فقط پیام‌های broadcast)؛ null یعنی پیامِ تکی/همگانیِ معمولی. */
    val batchId: Long? = null,
)

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

/** هدیه‌ی روزِ انتشارِ جیبک برای همه‌ی کاربرانِ قدیمی (یک بار برای هر نفر). */
@Serializable
private data class LaunchGiftBody(
    val days: Int = 30,
    /** فقط کسانی که قبل از این لحظه ثبت‌نام کرده‌اند (ISO)؛ خالی = همه‌ی کاربرانِ فعلی. */
    val createdBefore: String? = null,
    val paidTitle: String,
    val paidMessage: String,
    val oldTitle: String,
    val oldMessage: String,
    /** true = فقط شمارش، بی هیچ تغییری. */
    val dryRun: Boolean = true,
)

@Serializable
private data class LaunchGiftResponse(val ok: Boolean = true, val dryRun: Boolean, val paid: Int, val old: Int, val skipped: Int)

@Serializable
private data class DeactivateBody(val id: Long)

@Serializable
private data class CreatedResponse(val ok: Boolean = true, val id: Long)

@Serializable
private data class OkResponse(val ok: Boolean)

private suspend fun ApplicationCall.isAdmin(): Boolean {
    // ⚠️ بی `ADMIN_TOKEN` نوشتن **اصلاً باز نمی‌شود** - همان قاعده‌ی کدِ هدیه.
    val adminToken = env("ADMIN_TOKEN", "")
    val ok = adminToken.isNotEmpty() && adminTokenMatches(request.headers["X-Admin-Token"])
    if (!ok) respond(HttpStatusCode.Unauthorized, mapOf("error" to "admin_only"))
    return ok
}

fun Route.announcementRoutes() {
    route("/api/announcements") {
        // «دیده شد» برای آمارِ پیامِ گروهی؛ برای پیامِ اختصاصیِ خودِ کاربر read_atِ ردیف پر
        // می‌شود و برای **همه‌ی** پیام‌ها (از جمله همگانی‌ها که target_user_id ندارند) بازشدنِ
        // این کاربر در جدولِ announcement_reads ثبت می‌شود — کلیدِ مرکب یعنی باز کردنِ
        // دوباره شمارش را دو برابر نمی‌کند.
        post("/{id}/read") {
            val uid = call.optionalUid() ?: return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "auth"))
            val id = call.parameters["id"]?.toLongOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "id"))
            Db.withConnection { conn ->
                val visible = conn.queryOne(
                    "SELECT 1 FROM announcements WHERE id = ? AND active = 1 AND (target_user_id IS NULL OR target_user_id = ?)",
                    id, uid,
                ) { true } == true
                if (!visible) return@withConnection
                conn.executeCounting(
                    "UPDATE announcements SET read_at = datetime('now') WHERE id = ? AND target_user_id = ? AND read_at IS NULL", id, uid,
                )
                conn.execute(
                    "INSERT OR IGNORE INTO announcement_reads (announcement_id, user_id) VALUES (?, ?)", id, uid,
                )
            }
            call.respond(mapOf("ok" to true))
        }
        // عکسِ همراهِ پیام (۴ آبان) - فقط برای کسی که آن پیام مالِ اوست (یا پیامِ همگانی).
        get("/image/{id}") {
            val uid = call.optionalUid() ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "auth"))
            val id = call.parameters["id"].orEmpty()
            val allowed = Db.withConnection { conn ->
                conn.queryOne(
                    "SELECT 1 FROM announcements WHERE image_id = ? AND (target_user_id IS NULL OR target_user_id = ?)", id, uid,
                ) { true } == true
            }
            val file = ir.sadteam.loancalc.server.SupportFiles.fileFor(id)
            if (!allowed || file == null || !file.isFile) return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "not_found"))
            call.response.header("X-Content-Type-Options", "nosniff")
            call.respondBytes(file.readBytes(), io.ktor.http.ContentType.Image.JPEG)
        }
        get {
            val since = call.request.queryParameters["since"]?.toLongOrNull() ?: 0L
            // همگانی‌ها برای همه؛ اختصاصی‌ها فقط برای صاحبش (با توکن). بی ورود، فقط همگانی.
            val uid = call.optionalUid() ?: -1L
            val items = Db.withConnection { conn ->
                conn.prepareStatement(
                    "SELECT a.id, a.title, a.body, a.kind, a.created_at, a.coins, a.action, a.image_id, " +
                        "a.read_at, a.batch_id, r.read_at FROM announcements a " +
                        "LEFT JOIN announcement_reads r ON r.announcement_id = a.id AND r.user_id = ? " +
                        "WHERE a.active = 1 AND a.id > ? AND (a.target_user_id IS NULL OR a.target_user_id = ?) " +
                        "ORDER BY a.id DESC LIMIT 30",
                ).use { ps ->
                    ps.setLong(1, uid)
                    ps.setLong(2, since)
                    ps.setLong(3, uid)
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
                                        coins = rs.getInt("coins"),
                                        action = rs.getString("action"),
                                        imageId = rs.getString("image_id"),
                                        // پیامِ همگانی read_atِ ردیف ندارد؛ بازشدنِ این کاربر از جدولِ جدا می‌آید.
                                        // ستونِ ۹ = a.read_at، ستونِ ۱۱ = r.read_at (هر دو هم‌نام‌اند، پس با شماره خوانده می‌شوند).
                                        readAt = rs.getString(11) ?: rs.getString(9),
                                        batchId = rs.getLong("batch_id").takeIf { !rs.wasNull() },
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
                // تمدید + درجِ پیام در **یک تراکنش**: خرابیِ وسطِ کار نباید تمدیدِ بی‌پیام (یا برعکس) بگذارد.
                // پایه از max(الان، انقضا، پایانِ دوره‌ی آزمایشی) است تا روزهای هدیه با ماهِ مجانی هم‌پوشانی نکنند.
                conn.autoCommit = false
                try {
                    val expiry = java.time.Instant.ofEpochMilli(
                        giftBaseMs(conn, uid) + body.days * 24L * 60 * 60 * 1000,
                    ).toString()
                    conn.execute("UPDATE users SET subscribed_until = ? WHERE id = ?", expiry, uid)
                    val text = body.message?.trim()?.takeIf { it.isNotEmpty() }?.take(1000)
                        ?: "${body.days} روز اشتراکِ جیبک هدیه گرفتی. ممنون که همراهِ ما هستی!"
                    conn.insertReturningId(
                        "INSERT INTO announcements (title, body, kind, target_user_id) VALUES (?, ?, ?, ?)",
                        "🎁 هدیه‌ی اشتراک", text, "info", uid,
                    )
                    conn.commit()
                    GiftResponse(userId = uid, subscribedUntil = expiry)
                } catch (e: Exception) {
                    runCatching { conn.rollback() }
                    throw e
                } finally {
                    runCatching { conn.autoCommit = true }
                }
            }
            if (result == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "user_not_found"))
                return@post
            }
            call.respond(result)
        }

        // 🎉 هدیه‌ی انتشار: به هر کاربرِ قدیمی [days] روز (اشتراکِ فعال از انقضایش جلو می‌رود) و یک
        // پیامِ تشکرِ جدا - خریداران یک متن، بقیه متنِ دیگر. ستونِ launch_gift_granted ضدِتکرار است.
        post("/launch-gift") {
            if (!call.isAdmin()) return@post
            val body = runCatching { call.receive<LaunchGiftBody>() }.getOrNull()
            if (body == null || body.days !in 1..366 ||
                listOf(body.paidTitle, body.paidMessage, body.oldTitle, body.oldMessage).any { it.isBlank() } ||
                body.paidMessage.length > 1000 || body.oldMessage.length > 1000
            ) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_request"))
                return@post
            }
            val result = Db.withConnection { conn ->
                val rows = conn.prepareStatement(
                    "SELECT u.id, u.subscribed_until, u.launch_gift_granted, " +
                        "(u.subscription_tier IS NOT NULL OR EXISTS (SELECT 1 FROM subscription_purchases p WHERE p.user_id = u.id)) AS paid " +
                        "FROM users u WHERE (? IS NULL OR u.created_at < ?)",
                ).use { ps ->
                    ps.setString(1, body.createdBefore)
                    ps.setString(2, body.createdBefore)
                    ps.executeQuery().use { rs ->
                        buildList {
                            while (rs.next()) add(listOf(rs.getLong(1), rs.getString(2), rs.getInt(3), rs.getInt(4)))
                        }
                    }
                }
                var paid = 0; var old = 0; var skipped = 0
                for (r in rows) {
                    val uid = r[0] as Long
                    if ((r[2] as Int) != 0) { skipped++; continue }
                    if ((r[3] as Int) != 0) paid++ else old++
                    if (body.dryRun) continue
                    // فقط «در انتظار» علامت می‌خورد؛ خودِ هدیه اولین باری داده می‌شود که این شخص
                    // برنامه را باز کند (claimLaunchGift) - تا کسی که ماه‌ها بعد برمی‌گردد هم ۳۰ روزِ کامل بگیرد.
                    conn.execute("UPDATE users SET launch_gift_granted = 2 WHERE id = ? AND launch_gift_granted = 0", uid)
                }
                if (!body.dryRun) {
                    conn.execute(
                        "INSERT OR REPLACE INTO app_config (key, json) VALUES ('launch_gift', ?)",
                        Json.encodeToString(LaunchGiftBody.serializer(), body),
                    )
                }
                LaunchGiftResponse(dryRun = body.dryRun, paid = paid, old = old, skipped = skipped)
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

/**
 * هدیه‌ی انتشار برای کسی که «در انتظار» است (launch_gift_granted = 2): N روز از
 * max(الان، انقضای فعلی، پایانِ دوره‌ی آزمایشی) + پیامِ تشکرِ گروهِ خودش.
 * ضدِتکرار: شرطِ `= 2` در همان UPDATE، پس دو درخواستِ هم‌زمان دو بار نمی‌دهند.
 */
internal fun claimLaunchGift(uid: Long) {
    runCatching {
        Db.withConnection { conn ->
            val cfgJson = conn.queryOne("SELECT json FROM app_config WHERE key = 'launch_gift'") { it.getString(1) } ?: return@withConnection
            val cfg = Json { ignoreUnknownKeys = true }.decodeFromString(LaunchGiftBody.serializer(), cfgJson)
            val isPaid = conn.queryOne(
                "SELECT (u.subscription_tier IS NOT NULL OR EXISTS " +
                    "(SELECT 1 FROM subscription_purchases p WHERE p.user_id = u.id)) FROM users u " +
                    "WHERE u.id = ? AND u.launch_gift_granted = 2",
                uid,
            ) { it.getInt(1) != 0 } ?: return@withConnection
            // پایه مثلِ بقیه‌ی هدیه‌ها: max(الان، انقضا، پایانِ دوره‌ی آزمایشی).
            val expiry = java.time.Instant.ofEpochMilli(giftBaseMs(conn, uid) + cfg.days * 24L * 60 * 60 * 1000).toString()
            val n = conn.executeCounting(
                "UPDATE users SET subscribed_until = ?, launch_gift_granted = 1 WHERE id = ? AND launch_gift_granted = 2", expiry, uid,
            )
            if (n == 0) return@withConnection
            conn.insertReturningId(
                "INSERT INTO announcements (title, body, kind, target_user_id) VALUES (?, ?, ?, ?)",
                (if (isPaid) cfg.paidTitle else cfg.oldTitle).trim(),
                (if (isPaid) cfg.paidMessage else cfg.oldMessage).trim(),
                "info", uid,
            )
            Log.info("launch_gift_claimed", "هدیه‌ی انتشار داده شد", "uid" to uid, "paid" to isPaid)
        }
    }
}
