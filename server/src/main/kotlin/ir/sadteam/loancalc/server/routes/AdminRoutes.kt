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
import ir.sadteam.loancalc.server.queryOne
import ir.sadteam.loancalc.server.requireAuth
import kotlinx.serialization.Serializable
import java.sql.Connection

/**
 * صفحه‌ی «آمارِ جیبک» داخلِ اپ - فقط برای حسابی که `is_admin` دارد (صاحبِ برنامه).
 *   GET  /api/admin/check  - {admin: bool}؛ اپ بر اساسش ردیفِ تنظیمات را نشان می‌دهد
 *   GET  /api/admin/stats  - گزارشِ کامل (فقط ادمین)
 *   POST /api/admin/grant  - {userId|phone, revoke?} فقط با X-Admin-Token (ورک‌فلوی make-admin)
 *
 * همه‌ی عددها از `installs`/`usage_daily` می‌آیند که به‌ازای **نصبِ بی‌نام** است؛ هیچ مبلغ،
 * متن یا شماره‌ای در آن‌ها نیست.
 */
@Serializable
data class AdminCheck(val admin: Boolean)

@Serializable
data class DayPoint(val day: String, val active: Int, val new: Int)

@Serializable
data class NamedCount(val name: String, val count: Int)

@Serializable
data class FeatureUsage(val name: String, val users: Int, val total: Int)

@Serializable
data class RetentionPoint(val afterDays: Int, val base: Int, val returned: Int)

@Serializable
data class StatsResponse(
    val today: String,
    val totalInstalls: Int,
    val activeToday: Int,
    val active7: Int,
    val active30: Int,
    val new7: Int,
    val new30: Int,
    val loggedInActive30: Int,
    val avgActiveDays30: Double,
    val sessions7: Int,
    val daily: List<DayPoint>,
    val retention: List<RetentionPoint>,
    val funnel: List<NamedCount>,
    val screens: List<FeatureUsage>,
    val actions: List<FeatureUsage>,
    val versions: List<NamedCount>,
    val stores: List<NamedCount>,
    val sdks: List<NamedCount>,
)

@Serializable
private data class GrantBody(val userId: Long? = null, val phone: String? = null, val revoke: Boolean = false)

private suspend fun ApplicationCall.isAdminUser(): Boolean? {
    val authed = requireAuth() ?: return null
    return Db.withConnection { conn ->
        conn.queryOne("SELECT is_admin FROM users WHERE id = ?", authed.uid) { it.getInt(1) == 1 }
    } ?: false
}

private fun Connection.int(sql: String, vararg params: Any?): Int = queryOne(sql, *params) { it.getInt(1) } ?: 0

private fun Connection.list(sql: String, vararg params: Any?, map: (java.sql.ResultSet) -> Unit) {
    prepareStatement(sql).use { ps ->
        params.forEachIndexed { i, p -> ps.setObject(i + 1, p) }
        ps.executeQuery().use { rs -> while (rs.next()) map(rs) }
    }
}

internal fun buildStats(conn: Connection): StatsResponse {
    val today = iranDay()
    val d7 = iranDay(-6)
    val d30 = iranDay(-29)
    fun activeSince(day: String) = conn.int("SELECT COUNT(DISTINCT install_id) FROM usage_daily WHERE day >= ?", day)

    val activeByDay = HashMap<String, Int>()
    conn.list("SELECT day, COUNT(DISTINCT install_id) FROM usage_daily WHERE day >= ? GROUP BY day", d30) {
        activeByDay[it.getString(1)] = it.getInt(2)
    }
    val newByDay = HashMap<String, Int>()
    conn.list("SELECT first_day, COUNT(*) FROM installs WHERE first_day >= ? GROUP BY first_day", d30) {
        newByDay[it.getString(1)] = it.getInt(2)
    }
    val daily = (29 downTo 0).map { back ->
        val day = iranDay(-back.toLong())
        DayPoint(day, activeByDay[day] ?: 0, newByDay[day] ?: 0)
    }

    // «برگشت بعد از N روز» = بعد از روزِ Nِ پس از نصب، دست‌کم یک بار دیگر باز کرده. پایه فقط
    // نصب‌هایی است که N روز از عمرشان گذشته (وگرنه نصبِ دیروز «برنگشته» حساب می‌شد).
    val retention = listOf(1, 7, 30).map { n ->
        val cutoff = iranDay(-n.toLong())
        val base = conn.int("SELECT COUNT(*) FROM installs WHERE first_day <= ? AND first_day >= ?", cutoff, iranDay(-120))
        val returned = conn.int(
            "SELECT COUNT(*) FROM installs i WHERE i.first_day <= ? AND i.first_day >= ? AND EXISTS " +
                "(SELECT 1 FROM usage_daily u WHERE u.install_id = i.install_id AND u.day >= date(i.first_day, '+$n days'))",
            cutoff, iranDay(-120),
        )
        RetentionPoint(n, base, returned)
    }

    val totalInstalls = conn.int("SELECT COUNT(*) FROM installs")
    val funnel = listOf(NamedCount("installed", totalInstalls)) +
        listOf("onboarding_completed", "account_created", "transaction_created", "report_viewed").map { name ->
            NamedCount(name, conn.int("SELECT COUNT(DISTINCT install_id) FROM usage_daily WHERE name = ?", name))
        }

    fun features(prefix: String): List<FeatureUsage> = buildList {
        conn.list(
            "SELECT name, COUNT(DISTINCT install_id), SUM(count) FROM usage_daily WHERE day >= ? AND name LIKE ? " +
                "GROUP BY name ORDER BY 2 DESC, 3 DESC LIMIT 80",
            d30, "$prefix%",
        ) { add(FeatureUsage(it.getString(1).removePrefix(prefix), it.getInt(2), it.getInt(3))) }
    }

    fun split(column: String): List<NamedCount> = buildList {
        conn.list(
            "SELECT coalesce(CAST($column AS TEXT), '?'), COUNT(*) FROM installs WHERE last_day >= ? GROUP BY 1 ORDER BY 2 DESC LIMIT 12",
            d30,
        ) { add(NamedCount(it.getString(1), it.getInt(2))) }
    }

    val avgDays = conn.queryOne(
        "SELECT AVG(c) FROM (SELECT COUNT(DISTINCT day) AS c FROM usage_daily WHERE day >= ? GROUP BY install_id)", d30,
    ) { it.getDouble(1) } ?: 0.0

    return StatsResponse(
        today = today,
        totalInstalls = totalInstalls,
        activeToday = activeSince(today),
        active7 = activeSince(d7),
        active30 = activeSince(d30),
        new7 = conn.int("SELECT COUNT(*) FROM installs WHERE first_day >= ?", d7),
        new30 = conn.int("SELECT COUNT(*) FROM installs WHERE first_day >= ?", d30),
        loggedInActive30 = conn.int("SELECT COUNT(*) FROM installs WHERE last_day >= ? AND logged_in = 1", d30),
        avgActiveDays30 = Math.round(avgDays * 10) / 10.0,
        sessions7 = conn.int("SELECT coalesce(SUM(count), 0) FROM usage_daily WHERE day >= ? AND name = 'session_start'", d7),
        daily = daily,
        retention = retention,
        funnel = funnel,
        screens = features("screen:"),
        actions = features("action:"),
        versions = split("app_version"),
        stores = split("store"),
        sdks = split("sdk"),
    )
}

fun Route.adminRoutes() {
    route("/api/admin") {
        get("/check") {
            val admin = call.isAdminUser() ?: return@get
            call.respond(AdminCheck(admin))
        }
        get("/stats") {
            val admin = call.isAdminUser() ?: return@get
            if (!admin) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "admin_only"))
                return@get
            }
            call.respond(Db.withConnection { buildStats(it) })
        }
        post("/grant") {
            val token = env("ADMIN_TOKEN", "")
            if (token.isEmpty() || call.request.headers["X-Admin-Token"] != token) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "admin_only"))
                return@post
            }
            val body = runCatching { call.receive<GrantBody>() }.getOrNull()
            val flag = if (body?.revoke == true) 0 else 1
            val changed = Db.withConnection { conn ->
                when {
                    body?.userId != null -> conn.executeCounting("UPDATE users SET is_admin = ? WHERE id = ?", flag, body.userId)
                    !body?.phone.isNullOrBlank() -> conn.executeCounting("UPDATE users SET is_admin = ? WHERE phone = ?", flag, body!!.phone)
                    else -> 0
                }
            }
            if (changed == 0) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "user_not_found"))
            } else {
                call.respond(mapOf("ok" to true, "admin" to (flag == 1)))
            }
        }
    }
}
