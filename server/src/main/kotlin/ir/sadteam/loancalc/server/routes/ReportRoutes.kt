package ir.sadteam.loancalc.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import ir.sadteam.loancalc.server.Db
import ir.sadteam.loancalc.server.env
import ir.sadteam.loancalc.server.execute
import ir.sadteam.loancalc.server.executeCounting
import ir.sadteam.loancalc.server.insertReturningId
import ir.sadteam.loancalc.server.queryOne
import kotlinx.serialization.Serializable

/**
 * ۲۲ آبان (خواسته‌ی صاحبِ برنامه): آدرسی تا «Claude / Muse» روی گوشی‌اش گزارشِ پنلِ مدیریت را
 * بخواند و کارها را انجام دهد - بی ورود به برنامه.
 *
 * دو راهِ ورود، با همان کلید:
 *   ۱) کلید در مسیر:  GET /api/report/{key}             (برای بازکردنِ ساده‌ی لینک)
 *   ۲) کلید در هدر:   GET /api/owner   + هدرِ ثابت       (برای Connector؛ هدر جایگزین نمی‌شود)
 *       هدر یکی از این دو:  Authorization: Bearer <key>   یا   X-Report-Key: <key>
 *
 * کلید = `REPORT_KEY` در `.env` (حداقل ۲۴ حرف؛ نبودش = مسیر بسته). مقایسه در زمانِ ثابت.
 * هرچه پنلِ مدیریت دارد؛ کارهای نوشتنی بی `confirm=yes` فقط پیش‌نمایش‌اند (۲۴ آبان: هدر هم اضافه شد).
 */
@Serializable
data class OwnerReport(
    val guide: String,
    val today: DigestResponse,
    val week: DigestResponse,
    val month: DigestResponse,
    val stats: StatsResponse,
    val money: MoneyResponse,
    /** ۲۳ آبان (صاحبِ برنامه: «دسترسیِ کامل»): هرچه پنلِ مدیریت دارد. */
    val users: List<AdminUserRow> = emptyList(),
    val health: List<HealthDetail> = emptyList(),
    val support: List<ReportSupportItem> = emptyList(),
    val supportOpen: Int = 0,
    val broadcasts: List<BroadcastHistoryItem> = emptyList(),
    val segments: Map<String, Int> = emptyMap(),
    val usage: List<ReportUsageRow> = emptyList(),
)

@Serializable
data class ReportSupportItem(
    val ticket: String,
    val category: String,
    val status: String,
    val message: String,
    val appVersion: String? = null,
    val device: String? = null,
    val phone: String? = null,
    val createdAt: String,
    val attachments: Int = 0,
    /** شماره‌ی کاربری (Uid) برای پیام/هدیه با `do/send?segment=one&user=`. */
    val userCode: String? = null,
)

@Serializable
data class ReportUsageRow(val day: String, val name: String, val count: Int)

private fun <T> safe(default: T, block: () -> T): T = runCatching(block).getOrDefault(default)

private fun reportSupport(conn: java.sql.Connection): List<ReportSupportItem> = buildList {
    conn.list(
        "SELECT b.ticket, COALESCE(b.category,'bug'), b.status, b.message, b.app_version, b.device, b.phone, b.created_at, " +
            "(SELECT COUNT(*) FROM support_files f WHERE f.report_id = b.id), b.user_id FROM bug_reports b ORDER BY b.id DESC LIMIT 100",
    ) {
        add(
            ReportSupportItem(
                ticket = it.getString(1).orEmpty(), category = it.getString(2), status = it.getString(3).orEmpty(),
                message = it.getString(4).orEmpty(), appVersion = it.getString(5), device = it.getString(6),
                phone = it.getString(7), createdAt = it.getString(8).orEmpty(), attachments = it.getInt(9),
                userCode = it.getLong(10).takeIf { _ -> !it.wasNull() }?.let { uid -> userCodeOf(conn, uid)?.removePrefix("Uid:") },
            ),
        )
    }
}

private fun reportUsage(conn: java.sql.Connection): List<ReportUsageRow> = buildList {
    conn.list("SELECT day, name, count FROM usage_events WHERE day >= date('now', '-30 days') ORDER BY day DESC, count DESC") {
        add(ReportUsageRow(it.getString(1), it.getString(2), it.getInt(3)))
    }
}

private fun keyIsValid(sent: String?): Boolean {
    val key = env("REPORT_KEY", "")
    if (key.length < 24 || sent.isNullOrEmpty()) return false
    return java.security.MessageDigest.isEqual(key.toByteArray(), sent.toByteArray())
}

/** کلید از مسیر (`{key}`) یا هدر (`Authorization: Bearer …` یا `X-Report-Key`). */
private fun ApplicationCall.sentKey(): String? {
    parameters["key"]?.let { return it }
    request.headers["X-Report-Key"]?.takeIf { it.isNotBlank() }?.let { return it.trim() }
    request.headers["Authorization"]?.takeIf { it.isNotBlank() }?.let {
        return it.trim().removePrefix("Bearer ").removePrefix("bearer ").trim()
    }
    return null
}

private suspend fun ApplicationCall.reportGate(): Boolean {
    val ok = keyIsValid(sentKey())
    if (!ok) respond(HttpStatusCode.NotFound, mapOf("error" to "not_found"))
    else {
        response.headers.append("Cache-Control", "no-store")
        response.headers.append("X-Robots-Tag", "noindex")
    }
    return ok
}

fun Route.reportRoutes() {
    // کلید در مسیر - برای بازکردنِ ساده‌ی لینک.
    route("/api/report/{key}") { reportEndpoints() }
    // کلید در هدر - برای Connector (هدرِ ثابت، بی جایگزینیِ کدِ موقت).
    route("/api/owner") { reportEndpoints() }
}

/** همان مجموعه‌ی مسیرها، مستقل از اینکه کلید از مسیر آمده یا از هدر. */
private fun Route.reportEndpoints() {
    get { if (call.reportGate()) call.respond(call.fullReport()) }
    get("/help") { if (call.reportGate()) call.respond(mapOf("help" to REPORT_HELP)) }
    get("/user") {
        if (!call.reportGate()) return@get
        val code = call.request.queryParameters["code"].orEmpty()
        call.respond(Db.withConnection { buildTimeline(it, code) })
    }
    get("/do/reply") { if (call.reportGate()) call.doReply() }
    get("/do/status") { if (call.reportGate()) call.doStatus() }
    get("/do/send") { if (call.reportGate()) call.doSend() }
}

private suspend fun ApplicationCall.fullReport(): OwnerReport = Db.withConnection { conn ->
    OwnerReport(
        guide = "گزارشِ پنلِ مدیریتِ اپِ «جیبک» (اندروید، بازار و مایکت). " +
            "today/week/month: مقایسه‌ی این بازه با بازه‌ی قبل (now در برابرِ prev؛ روزها به وقتِ ایران). " +
            "stats: آمارِ کلی (نصب، کاربرِ فعال، مشترک‌ها، پیامک، کرش‌ها). " +
            "money: فروش و خریدهای اخیر (مبلغ به تومان، شماره‌ها نیمه‌پنهان). " +
            "users: ۳۰۰ کاربرِ اخیر (شماره کامل، اشتراک، استور، آخرین بازدید به UTC). " +
            "health: کرش/خطای همگام‌سازی/گوشیِ چندحسابه/ماهِ رایگانِ ردشده. support: پیام‌های پشتیبانی. " +
            "broadcasts/segments: پیام‌های گروهی و اندازه‌ی گروه‌ها. usage: شمارِ استفاده از هر بخش در ۳۰ روز. " +
            "برای کارها (جواب، وضعیت، پیام/هدیه) مسیرِ `/help` را از همین پایه (base) بگیر. " +
            "برای صاحبِ برنامه به فارسیِ ساده خلاصه کن: مهم‌ترین تغییرها، خریدهای تازه، و هر مشکل یا کرش.",
        today = buildDigest(conn, "day"),
        week = buildDigest(conn, "week"),
        month = buildDigest(conn, "month"),
        stats = buildStats(conn),
        money = buildMoney(conn),
        users = safe(emptyList()) { listUsers(conn, "") },
        health = listOf("crash", "sync", "multi", "trial").mapNotNull { k -> safe(null) { healthDetail(conn, k) } },
        support = safe(emptyList()) { reportSupport(conn) },
        supportOpen = safe(0) { conn.queryOne("SELECT COUNT(*) FROM bug_reports WHERE status = 'open'") { it.getInt(1) } ?: 0 },
        broadcasts = safe(emptyList()) { broadcastHistory(conn) },
        segments = safe(emptyMap()) { broadcastCounts(conn) },
        usage = safe(emptyList()) { reportUsage(conn) },
    )
}

// جواب به یک پیامِ پشتیبانی → «پیام‌های جیبک»ِ همان کاربر. close=0 → بسته نشود.
private suspend fun ApplicationCall.doReply() {
    val q = request.queryParameters
    val ticket = q["ticket"].orEmpty().trim()
    val text = q["text"].orEmpty().trim()
    if (ticket.isEmpty() || text.isEmpty() || text.length > 1000) {
        respond(HttpStatusCode.BadRequest, mapOf("error" to "need ticket and text (<=1000)")); return
    }
    val confirm = q["confirm"] == "yes"
    val close = q["close"] != "0"
    val result = Db.withConnection { conn ->
        val row = conn.queryOne("SELECT id, user_id, message FROM bug_reports WHERE ticket = ?", ticket) {
            Triple(it.getLong(1), it.getLong(2).takeIf { _ -> !it.wasNull() }, it.getString(3))
        } ?: return@withConnection mapOf("error" to "ticket_not_found")
        val uid = row.second ?: return@withConnection mapOf("error" to "no_user_for_ticket")
        if (!confirm) return@withConnection mapOf("preview" to "will reply to $ticket", "theirMessage" to row.third.orEmpty().take(300), "reply" to text, "next" to "add &confirm=yes to send")
        conn.insertReturningId(
            "INSERT INTO announcements (title, body, kind, target_user_id) VALUES (?, ?, ?, ?)",
            "💬 جوابِ پشتیبانی ($ticket)", text, "info", uid,
        )
        if (close) conn.execute("UPDATE bug_reports SET status = 'answered' WHERE id = ?", row.first)
        mapOf("ok" to "sent")
    }
    respond(result)
}

// وضعیتِ یک پیام: open / answered / closed.
private suspend fun ApplicationCall.doStatus() {
    val q = request.queryParameters
    val ticket = q["ticket"].orEmpty().trim()
    val status = q["status"].orEmpty()
    if (ticket.isEmpty() || status !in setOf("open", "answered", "closed")) {
        respond(HttpStatusCode.BadRequest, mapOf("error" to "need ticket and status=open|answered|closed")); return
    }
    if (q["confirm"] != "yes") { respond(mapOf("preview" to "set $ticket to $status", "next" to "add &confirm=yes")); return }
    val n = Db.withConnection { it.executeCounting("UPDATE bug_reports SET status = ? WHERE ticket = ?", status, ticket) }
    respond(if (n > 0) mapOf("ok" to "updated") else mapOf("error" to "ticket_not_found"))
}

// پیام (و هدیه‌ی روز/سکه) به یک نفر (segment=one&user=Uid) یا یک گروه - همان پیامِ گروهیِ پنل.
private suspend fun ApplicationCall.doSend() {
    val q = request.queryParameters
    val body = BroadcastBody(
        segment = q["segment"].orEmpty(),
        title = q["title"].orEmpty().trim(),
        body = q["body"].orEmpty().trim(),
        dryRun = q["confirm"] != "yes",
        days = q["days"]?.toIntOrNull() ?: 0,
        coins = q["coins"]?.toIntOrNull() ?: 0,
        action = q["action"],
        user = q["user"],
    )
    if (!body.dryRun && (body.title.isEmpty() || body.body.isEmpty())) {
        respond(HttpStatusCode.BadRequest, mapOf("error" to "need title and body")); return
    }
    val result = Db.withConnection { broadcast(it, body) }
    if (result == null) respond(HttpStatusCode.BadRequest, mapOf("error" to "bad segment/days/coins"))
    else respond(result)
}

private const val REPORT_HELP =
    "Base = /api/report/<key>  OR  /api/owner with header 'Authorization: Bearer <key>' (or 'X-Report-Key: <key>'). " +
        "Read: (base) full report · base/user?code=<Uid or phone> one user's history. " +
        "Write (without &confirm=yes it is only a preview; nothing changes): " +
        "base/do/reply?ticket=<ticket>&text=<reply>[&close=0] → reply to a support message (shows in user's 'Jibak messages'); " +
        "base/do/status?ticket=<ticket>&status=open|answered|closed; " +
        "base/do/send?segment=<one|all|inactive7|inactive30|trial_ending|expired|free|new7|cafebazaar|myket|old_version|paid>" +
        "[&user=<Uid> for one]&title=<title>&body=<text>[&days=<sub days 0-365>][&coins=<0-100000>][&action=shop|subscription|update] " +
        "→ message (and gift) to one user or a group; preview returns the count. " +
        "Always show the owner the exact text and count and get their OK before adding confirm=yes. Write in friendly formal Persian (تو، می‌شود، را)."
