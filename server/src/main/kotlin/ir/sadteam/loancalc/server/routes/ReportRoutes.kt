package ir.sadteam.loancalc.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import ir.sadteam.loancalc.server.Db
import ir.sadteam.loancalc.server.env
import ir.sadteam.loancalc.server.queryOne
import kotlinx.serialization.Serializable

/**
 * ۲۲ آبان (خواسته‌ی صاحبِ برنامه): یک آدرسِ فقط‌خواندنی تا «Claude Duet» روی گوشی‌اش گزارشِ
 * پنلِ مدیریت را بخواند و برایش تعریف کند - بی ورود به برنامه.
 *
 *   GET /api/report/{key}
 *
 * کلید = `REPORT_KEY` در `.env` (حداقل ۲۴ حرف؛ نبودش = مسیر بسته). مقایسه در زمانِ ثابت.
 * همه‌ی چیزهایی که پنلِ مدیریت نشان می‌دهد (۲۳ آبان: کامل، با فهرستِ کاربران و پشتیبانی).
 */
@Serializable
data class OwnerReport(
    val guide: String,
    val today: DigestResponse,
    val week: DigestResponse,
    val month: DigestResponse,
    val stats: StatsResponse,
    val money: MoneyResponse,
    /** ۲۳ آبان (صاحبِ برنامه: «دسترسیِ کامل»): هرچه پنلِ مدیریت دارد، فقط‌خواندنی. */
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
)

@Serializable
data class ReportUsageRow(val day: String, val name: String, val count: Int)

private fun <T> safe(default: T, block: () -> T): T = runCatching(block).getOrDefault(default)

private fun reportSupport(conn: java.sql.Connection): List<ReportSupportItem> = buildList {
    conn.list(
        "SELECT b.ticket, COALESCE(b.category,'bug'), b.status, b.message, b.app_version, b.device, b.phone, b.created_at, " +
            "(SELECT COUNT(*) FROM support_files f WHERE f.report_id = b.id) FROM bug_reports b ORDER BY b.id DESC LIMIT 100",
    ) {
        add(
            ReportSupportItem(
                ticket = it.getString(1).orEmpty(), category = it.getString(2), status = it.getString(3).orEmpty(),
                message = it.getString(4).orEmpty(), appVersion = it.getString(5), device = it.getString(6),
                phone = it.getString(7), createdAt = it.getString(8).orEmpty(), attachments = it.getInt(9),
            ),
        )
    }
}

private fun reportUsage(conn: java.sql.Connection): List<ReportUsageRow> = buildList {
    conn.list("SELECT day, name, count FROM usage_events WHERE day >= date('now', '-30 days') ORDER BY day DESC, count DESC") {
        add(ReportUsageRow(it.getString(1), it.getString(2), it.getInt(3)))
    }
}

private fun reportKeyMatches(sent: String?): Boolean {
    val key = env("REPORT_KEY", "")
    if (key.length < 24 || sent == null) return false
    return java.security.MessageDigest.isEqual(key.toByteArray(), sent.toByteArray())
}

fun Route.reportRoutes() {
    get("/api/report/{key}") {
        if (!reportKeyMatches(call.parameters["key"])) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "not_found"))
            return@get
        }
        val report = Db.withConnection { conn ->
            OwnerReport(
                guide = "گزارشِ پنلِ مدیریتِ اپِ «جیبک» (اندروید، بازار و مایکت). " +
                    "today/week/month: مقایسه‌ی این بازه با بازه‌ی قبل (now در برابرِ prev؛ روزها به وقتِ ایران). " +
                    "stats: آمارِ کلی (نصب، کاربرِ فعال، مشترک‌ها، پیامک، کرش‌ها). " +
                    "money: فروش و خریدهای اخیر (مبلغ به تومان، شماره‌ها نیمه‌پنهان). " +
                    "users: ۳۰۰ کاربرِ اخیر (شماره کامل، اشتراک، استور، آخرین بازدید به UTC). " +
                    "health: کرش/خطای همگام‌سازی/گوشیِ چندحسابه/ماهِ رایگانِ ردشده. support: پیام‌های پشتیبانی. " +
                    "broadcasts/segments: پیام‌های گروهی و اندازه‌ی گروه‌ها. usage: شمارِ استفاده از هر بخش در ۳۰ روز. " +
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
        call.response.headers.append("Cache-Control", "no-store")
        call.response.headers.append("X-Robots-Tag", "noindex")
        call.respond(report)
    }
}
