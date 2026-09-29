package ir.sadteam.loancalc.server.routes

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.header
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.get
import ir.sadteam.loancalc.server.SupportFiles
import ir.sadteam.loancalc.server.insertReturningId
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import ir.sadteam.loancalc.server.Db
import ir.sadteam.loancalc.server.Log
import ir.sadteam.loancalc.server.Mail
import ir.sadteam.loancalc.server.execute
import ir.sadteam.loancalc.server.maskPhone
import ir.sadteam.loancalc.server.queryOne
import ir.sadteam.loancalc.server.rateLimitOk
import ir.sadteam.loancalc.server.requireAuth
import kotlinx.serialization.Serializable
import java.security.SecureRandom

/*
 * 🐞 **گزارشِ مشکل**.
 *
 * چرا سمتِ سرور ثبت می‌شود و فقط ایمیل نمی‌رود: برای دادنِ هدیه باید بدانیم گزارش از
 * **کدام حساب** آمده. ایمیل این را نمی‌گوید (کاربر ممکن است از ایمیلِ شخصی‌اش بفرستد)،
 * ولی `user_id`ِ همین جدول دقیقاً همان چیزی است که کدِ هدیه به آن تعلق می‌گیرد.
 *
 * ✉️ **ایمیل حالا از خودِ سرور می‌رود** (`Mail`). گوشی هم همچنان صندوقِ ایمیل را با
 * موضوعِ آماده باز می‌کند، و این عمدی است: کاربر می‌خواهد چیزی را که فرستاده در
 * «ارسال‌شده‌ها»ی خودش ببیند، و اگر SMTP روزی از کار بیفتد راهِ دوم سرِ جایش است.
 *
 * 🚨 **ثبت در دیتابیس بر ارسالِ ایمیل مقدم است و ارسال هیچ‌وقت درخواست را نمی‌شکند.**
 * کدِ پیگیری چیزی است که هدیه به آن بسته می‌شود؛ اگر به‌خاطرِ یک SMTPِ خراب پاسخِ ۵۰۰
 * برگردد، کاربر گزارشش را از دست می‌دهد در حالی که همین حالا در جدول نشسته.
 */

@Serializable
private data class ReportBody(
    val message: String,
    /** نسخه‌ی اپ و مدلِ گوشی - برای بازتولیدِ مشکل. */
    val appVersion: String? = null,
    val device: String? = null,
    /** شناسه‌ی پیوست‌هایی که پیش‌تر با `/upload` فرستاده شده‌اند (حداکثر ۴). */
    val attachments: List<String> = emptyList(),
    /** bug = مشکل، design = طراحی، idea = پیشنهاد، question = سؤال. */
    val category: String = "bug",
)

private val CATEGORIES = setOf("bug", "design", "idea", "question")
private val GIFT_DAYS = setOf(1, 3, 7, 10)

@Serializable
private data class SupportGiftBody(val id: Long, val days: Int, val text: String)

@Serializable
private data class UploadResponse(val ok: Boolean = true, val id: String, val kind: String)

@Serializable
data class SupportAttachmentDto(val id: String, val kind: String)

@Serializable
data class SupportMessageDto(
    val id: Long,
    val ticket: String,
    val userId: Long?,
    val message: String,
    val appVersion: String?,
    val device: String?,
    val status: String,
    val createdAt: String,
    val category: String = "bug",
    val rewardedDays: Int = 0,
    val attachments: List<SupportAttachmentDto>,
)

@Serializable
private data class SupportListResponse(val items: List<SupportMessageDto>, val openCount: Int)

@Serializable
private data class ReplyBody(val id: Long, val text: String, val close: Boolean = true)

@Serializable
private data class StatusBody(val id: Long, val status: String)

private const val MAX_ATTACHMENTS = 4

/** فقط حسابِ `is_admin`؛ هر پاسخِ دیگری ۴۰۳. */
private suspend fun ApplicationCall.requireSupportAdmin(): Boolean {
    val authed = requireAuth() ?: return false
    val ok = Db.withConnection { conn ->
        conn.queryOne("SELECT is_admin FROM users WHERE id = ?", authed.uid) { it.getInt(1) == 1 }
    } == true
    if (!ok) respond(HttpStatusCode.Forbidden, mapOf("error" to "admin_only"))
    return ok
}

@Serializable
private data class ReportResponse(val ok: Boolean = true, val ticket: String)

/** کدِ پیگیری: کوتاه و خوانا، چون کاربر آن را در ایمیل و پشتیبانی تکرار می‌کند. */
private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

private fun newTicket(): String {
    val rnd = SecureRandom()
    return "JB-" + (1..6).map { ALPHABET[rnd.nextInt(ALPHABET.length)] }.joinToString("")
}

fun Route.supportRoutes() {
    route("/api/support") {
        post("/report") {
            // ۱۰ گزارش در ساعت از هر IP: کاربرِ واقعی یکی می‌فرستد.
            if (!call.rateLimitOk("support_report", 10, 60 * 60 * 1000L)) return@post
            val authed = call.requireAuth() ?: return@post
            val body = runCatching { call.receive<ReportBody>() }.getOrNull()
            val message = body?.message?.trim()
            if (message.isNullOrBlank() || message.length < 5) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "message_too_short"))
                return@post
            }

            val ticket = newTicket()
            val attachIds = body.attachments.distinct().take(MAX_ATTACHMENTS)
            Db.withConnection { conn ->
                val reportId = conn.insertReturningId(
                    """
                    INSERT INTO bug_reports (ticket, user_id, phone, message, app_version, device, category)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent(),
                    ticket,
                    authed.uid,
                    authed.phone,
                    // متن سقف می‌خورد: یک بدنه‌ی چندمگابایتی جدول را باد می‌کند.
                    message.take(4000),
                    body.appVersion?.take(40),
                    body.device?.take(80),
                    body.category.takeIf { it in CATEGORIES } ?: "bug",
                )
                // فقط پیوست‌های **خودِ همین کاربر** که هنوز به گزارشی وصل نشده‌اند.
                attachIds.forEach { fid ->
                    conn.execute(
                        "UPDATE support_files SET report_id = ? WHERE id = ? AND user_id = ? AND report_id IS NULL",
                        reportId, fid, authed.uid,
                    )
                }
            }
            // شماره ماسک می‌شود - قاعده‌ی ثبتِ لاگِ پروژه.
            Log.info("bug_report", "گزارشِ مشکلِ تازه", "ticket" to ticket, "uid" to authed.uid, "phone" to maskPhone(authed.phone))
            // ✉️ ارسال **بعد از** ثبت و بی‌اثر روی پاسخ - رجوع کن به کامنتِ بالای فایل.
            // `Mail.send` خودش استثنا نمی‌دهد و بی تنظیماتِ SMTP بی‌صدا `false` برمی‌گرداند.
            val mailed = Mail.send(
                to = Mail.supportTo,
                subject = "مشکل برنامه - $ticket",
                body = buildString {
                    appendLine("کدِ پیگیری: $ticket")
                    appendLine("شناسه‌ی کاربر: ${authed.uid}")
                    appendLine("شماره: ${maskPhone(authed.phone)}")
                    appendLine("نسخه: ${body.appVersion ?: "-"}")
                    appendLine("دستگاه: ${body.device ?: "-"}")
                    appendLine()
                    appendLine(message.take(4000))
                },
            )
            if (Mail.configured && !mailed) {
                Log.warn("bug_report_mail", "گزارش ثبت شد ولی ایمیلش نرفت", "ticket" to ticket)
            }
            call.respond(ReportResponse(ticket = ticket))
        }

        // 🔒 پیوست: فقط عکس (JPEG/PNG، بازسازی‌شده) یا فیلمِ MP4 - رجوع کن به SupportFiles.
        post("/upload") {
            if (!call.rateLimitOk("support_upload", 20, 60 * 60 * 1000L)) return@post
            val authed = call.requireAuth() ?: return@post
            val declared = call.request.headers[HttpHeaders.ContentLength]?.toLongOrNull()
            if (declared == null || declared <= 0 || declared > SupportFiles.MAX_VIDEO_BYTES) {
                call.respond(HttpStatusCode.PayloadTooLarge, mapOf("error" to "too_large"))
                return@post
            }
            // سقفِ روزانه‌ی هر کاربر: ۲۰ پیوست.
            val today = Db.withConnection { conn ->
                conn.queryOne(
                    "SELECT COUNT(*) FROM support_files WHERE user_id = ? AND created_at >= datetime('now','-1 day')", authed.uid,
                ) { it.getInt(1) } ?: 0
            }
            if (today >= 20) {
                call.respond(HttpStatusCode.TooManyRequests, mapOf("error" to "daily_limit"))
                return@post
            }
            val raw = call.receive<ByteArray>()
            when (val r = SupportFiles.sanitize(raw)) {
                is SupportFiles.Result.Rejected -> call.respond(HttpStatusCode.UnsupportedMediaType, mapOf("error" to r.reason))
                is SupportFiles.Result.Ok -> {
                    val id = SupportFiles.newId()
                    SupportFiles.fileFor(id)!!.writeBytes(r.bytes)
                    Db.withConnection { conn ->
                        conn.execute(
                            "INSERT INTO support_files (id, user_id, kind, mime, size) VALUES (?, ?, ?, ?, ?)",
                            id, authed.uid, r.kind, r.mime, r.bytes.size,
                        )
                    }
                    call.respond(UploadResponse(id = id, kind = r.kind))
                }
            }
        }
    }

    // صندوقِ پشتیبانی برای صاحبِ برنامه (داخلِ اپ، بخشِ «پیام‌های کاربران»).
    route("/api/admin/support") {
        get {
            if (!call.requireSupportAdmin()) return@get
            val result = Db.withConnection { conn ->
                val files = mutableMapOf<Long, MutableList<SupportAttachmentDto>>()
                conn.prepareStatement("SELECT id, report_id, kind FROM support_files WHERE report_id IS NOT NULL").use { ps ->
                    ps.executeQuery().use { rs ->
                        while (rs.next()) files.getOrPut(rs.getLong(2)) { mutableListOf() }.add(SupportAttachmentDto(rs.getString(1), rs.getString(3)))
                    }
                }
                val items = conn.prepareStatement(
                    "SELECT id, ticket, user_id, message, app_version, device, status, created_at, category, rewarded_days FROM bug_reports ORDER BY id DESC LIMIT 200",
                ).use { ps ->
                    ps.executeQuery().use { rs ->
                        buildList {
                            while (rs.next()) {
                                val id = rs.getLong(1)
                                add(
                                    SupportMessageDto(
                                        id = id, ticket = rs.getString(2), userId = rs.getLong(3).takeIf { !rs.wasNull() },
                                        message = rs.getString(4), appVersion = rs.getString(5), device = rs.getString(6),
                                        status = rs.getString(7), createdAt = rs.getString(8), attachments = files[id].orEmpty(),
                                        category = rs.getString(9) ?: "bug", rewardedDays = rs.getInt(10),
                                    ),
                                )
                            }
                        }
                    }
                }
                val open = conn.queryOne("SELECT COUNT(*) FROM bug_reports WHERE status = 'open'") { it.getInt(1) } ?: 0
                SupportListResponse(items, open)
            }
            call.respond(result)
        }
        get("/file/{id}") {
            if (!call.requireSupportAdmin()) return@get
            val id = call.parameters["id"].orEmpty()
            val file = SupportFiles.fileFor(id)
            val mime = Db.withConnection { conn -> conn.queryOne("SELECT mime FROM support_files WHERE id = ?", id) { it.getString(1) } }
            if (file == null || mime == null || !file.isFile) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "not_found"))
                return@get
            }
            call.response.header("X-Content-Type-Options", "nosniff")
            call.response.header(HttpHeaders.ContentDisposition, "attachment; filename=\"$id\"")
            call.respondBytes(file.readBytes(), if (mime == "video/mp4") ContentType.Video.MP4 else ContentType.Image.JPEG)
        }
        // جوابِ صاحبِ برنامه → پیامِ اختصاصی در «پیام‌های جیبک»ِ همان کاربر.
        post("/reply") {
            if (!call.requireSupportAdmin()) return@post
            val body = runCatching { call.receive<ReplyBody>() }.getOrNull()
            val text = body?.text?.trim()
            if (body == null || text.isNullOrBlank() || text.length > 1000) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_request"))
                return@post
            }
            val ok = Db.withConnection { conn ->
                val row = conn.queryOne("SELECT user_id, ticket FROM bug_reports WHERE id = ?", body.id) {
                    it.getLong(1) to it.getString(2)
                } ?: return@withConnection false
                conn.insertReturningId(
                    "INSERT INTO announcements (title, body, kind, target_user_id) VALUES (?, ?, ?, ?)",
                    "💬 جوابِ پشتیبانی (${row.second})", text, "info", row.first,
                )
                if (body.close) {
                    conn.execute("UPDATE bug_reports SET status = 'answered' WHERE id = ?", body.id)
                    SupportFiles.deleteForReport(conn, body.id)
                }
                true
            }
            if (ok) call.respond(mapOf("ok" to true)) else call.respond(HttpStatusCode.NotFound, mapOf("error" to "not_found"))
        }
        // 🎁 هدیه‌ی اشتراک برای یک پیام (۱/۳/۷/۱۰ روز) + متنِ دلخواه در «پیام‌های جیبک»ِ همان کاربر.
        // هر پیام فقط یک بار هدیه می‌گیرد؛ اشتراکِ فعال از تاریخِ انقضایش جلو می‌رود.
        post("/gift") {
            if (!call.requireSupportAdmin()) return@post
            val body = runCatching { call.receive<SupportGiftBody>() }.getOrNull()
            val text = body?.text?.trim()
            if (body == null || body.days !in GIFT_DAYS || text.isNullOrBlank() || text.length > 1000) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_request"))
                return@post
            }
            val result = Db.withConnection { conn ->
                val row = conn.queryOne("SELECT user_id, ticket, rewarded_days FROM bug_reports WHERE id = ?", body.id) {
                    Triple(it.getLong(1), it.getString(2), it.getInt(3))
                } ?: return@withConnection "not_found"
                if (row.third > 0) return@withConnection "already_rewarded"
                val currentUntil = conn.queryOne("SELECT subscribed_until FROM users WHERE id = ?", row.first) { it.getString(1) }
                val now = System.currentTimeMillis()
                val current = currentUntil?.let { runCatching { java.time.Instant.parse(it).toEpochMilli() }.getOrNull() } ?: 0L
                val expiry = java.time.Instant.ofEpochMilli(maxOf(current, now) + body.days * 24L * 60 * 60 * 1000).toString()
                conn.execute("UPDATE users SET subscribed_until = ? WHERE id = ?", expiry, row.first)
                conn.execute("UPDATE bug_reports SET rewarded_days = ?, status = 'answered' WHERE id = ?", body.days, body.id)
                SupportFiles.deleteForReport(conn, body.id)
                conn.insertReturningId(
                    "INSERT INTO announcements (title, body, kind, target_user_id) VALUES (?, ?, ?, ?)",
                    "🎁 هدیه‌ی اشتراک (${row.second})", text, "info", row.first,
                )
                "ok"
            }
            when (result) {
                "ok" -> call.respond(mapOf("ok" to true))
                "already_rewarded" -> call.respond(HttpStatusCode.Conflict, mapOf("error" to "already_rewarded"))
                else -> call.respond(HttpStatusCode.NotFound, mapOf("error" to "not_found"))
            }
        }

        post("/status") {
            if (!call.requireSupportAdmin()) return@post
            val body = runCatching { call.receive<StatusBody>() }.getOrNull()
            if (body == null || body.status !in setOf("open", "answered", "closed")) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_request"))
                return@post
            }
            Db.withConnection { conn ->
                conn.execute("UPDATE bug_reports SET status = ? WHERE id = ?", body.status, body.id)
                if (body.status != "open") SupportFiles.deleteForReport(conn, body.id)
            }
            call.respond(mapOf("ok" to true))
        }
    }
}
