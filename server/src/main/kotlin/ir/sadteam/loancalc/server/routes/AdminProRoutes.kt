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
import ir.sadteam.loancalc.server.execute
import ir.sadteam.loancalc.server.queryOne
import kotlinx.serialization.Serializable
import java.sql.Connection

/**
 * 📊 **ابزارهای حرفه‌ایِ آمار** (۸ مهر، کاربر: «همه رو بساز»):
 * پول (تبدیلِ آزمایشی به خرید، تمدید، درآمد به‌ازای کاربر، A/B)، تاریخچه‌ی یک کاربر،
 * پیامِ هدفمند به یک گروه. همه فقط برای حسابِ ادمین.
 */
@Serializable
data class MoneyResponse(
    val trialEnded: Int,
    val converted: Int,
    val payers: Int,
    val expiredPayers: Int,
    val renewed: Int,
    val users: Int,
    val grossTotal: Long,
    val netTotal: Long,
    val abA: AbGroup,
    val abB: AbGroup,
    val daily: List<MoneyDay> = emptyList(),
)

/** فروشِ هر روزِ ۳۰ روزِ اخیر (روزِ ایران) برای نمودار. */
@Serializable
data class MoneyDay(val day: String, val purchases: Int, val gross: Long, val net: Long)

@Serializable
data class AbGroup(val installs: Int, val paywallViews: Int, val purchases: Int)

@Serializable
data class TimelinePurchase(val product: String, val store: String, val at: String, val until: String)

@Serializable
data class TimelineInstall(val id: String, val firstDay: String, val lastDay: String, val activeDays: Int, val version: Int?, val store: String?, val model: String?)

@Serializable
data class TimelineDay(val day: String, val events: Int, val screens: Int)

@Serializable
data class UserTimeline(
    val found: Boolean,
    val code: String = "",
    val createdAt: String = "",
    val subscribedUntil: String? = null,
    val purchases: List<TimelinePurchase> = emptyList(),
    val supportCount: Int = 0,
    val lastSupport: String? = null,
    val installs: List<TimelineInstall> = emptyList(),
    val topScreens: List<NamedCount> = emptyList(),
    val days: List<TimelineDay> = emptyList(),
)

@Serializable
data class BroadcastBody(
    val segment: String = "",
    val title: String = "",
    val body: String = "",
    val dryRun: Boolean = true,
    /** هدیه‌ی همراهِ پیام: روزِ اشتراک (از max(الان، انقضا)) و سکه. */
    val days: Int = 0,
    val coins: Int = 0,
    /** دکمه‌ی داخلِ پیام: shop / subscription / update. */
    val action: String? = null,
)

@Serializable
data class BroadcastResult(val segment: String, val count: Int, val sent: Boolean)

private suspend fun ApplicationCall.adminOrNull(): Boolean {
    val admin = isAdminUser() ?: return false
    if (!admin) respond(HttpStatusCode.Forbidden, mapOf("error" to "admin_only"))
    return admin
}

private fun Connection.revenueTotal(net: Boolean): Long {
    var sum = 0L
    list("SELECT product_id, store, coalesce(SUM(price_toman),0) FROM subscription_purchases GROUP BY 1, 2") {
        val gross = it.getLong(3)
        val r = STORE_PAYOUT[it.getString(2)]
        sum += if (net && r != null) gross * r.first / r.second else gross
    }
    return sum
}

private fun Connection.abGroup(variant: String): AbGroup {
    val tag = "action:ab_paywall_$variant"
    val installs = int("SELECT COUNT(DISTINCT install_id) FROM usage_daily WHERE name = ?", tag)
    val views = int(
        "SELECT coalesce(SUM(count),0) FROM usage_daily WHERE name = 'action:paywall_view' AND install_id IN " +
            "(SELECT install_id FROM usage_daily WHERE name = ?)",
        tag,
    )
    val buys = int(
        "SELECT coalesce(SUM(count),0) FROM usage_daily WHERE name LIKE 'action:purchase_done_%' AND install_id IN " +
            "(SELECT install_id FROM usage_daily WHERE name = ?)",
        tag,
    )
    return AbGroup(installs, views, buys)
}

private fun moneyDaily(conn: Connection): List<MoneyDay> {
    val rows = mutableMapOf<String, MoneyDay>()
    conn.list(
        "SELECT date(created_at, '+210 minutes'), product_id, store, COUNT(*), coalesce(SUM(price_toman),0) FROM subscription_purchases " +
            "WHERE created_at >= datetime('now', '-31 days') GROUP BY 1, 2, 3",
    ) {
        val gross = it.getLong(5)
        val r = STORE_PAYOUT[it.getString(3)]
        val net = if (r != null) gross * r.first / r.second else gross
        val d = it.getString(1)
        val old = rows[d] ?: MoneyDay(d, 0, 0, 0)
        rows[d] = MoneyDay(d, old.purchases + it.getInt(4), old.gross + gross, old.net + net)
    }
    val today = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).plusMinutes(210).toLocalDate()
    return (29 downTo 0).map { today.minusDays(it.toLong()).toString() }.map { rows[it] ?: MoneyDay(it, 0, 0, 0) }
}

internal fun buildMoney(conn: Connection): MoneyResponse {
    // آزمایشیِ ۳۰روزه تمام‌شده = ثبت‌نامِ بیش از ۳۰ روز پیش (و قفل‌نشده).
    val ended = "created_at <= datetime('now', '-30 days') AND trial_blocked = 0"
    val trialEnded = conn.int("SELECT COUNT(*) FROM users WHERE $ended")
    val converted = conn.int("SELECT COUNT(*) FROM users WHERE $ended AND id IN (SELECT user_id FROM subscription_purchases)")
    val payers = conn.int("SELECT COUNT(DISTINCT user_id) FROM subscription_purchases")
    // تمدید: از کسانی که اولین اشتراکِ خریده‌شده‌شان تمام شده، چند نفر دوباره خریدند.
    val expired = "user_id IN (SELECT user_id FROM subscription_purchases GROUP BY user_id HAVING min(subscribed_until) < datetime('now'))"
    val expiredPayers = conn.int("SELECT COUNT(DISTINCT user_id) FROM subscription_purchases WHERE $expired")
    val renewed = conn.int("SELECT COUNT(*) FROM (SELECT user_id FROM subscription_purchases WHERE $expired GROUP BY user_id HAVING COUNT(*) >= 2)")
    return MoneyResponse(
        trialEnded = trialEnded,
        converted = converted,
        payers = payers,
        expiredPayers = expiredPayers,
        renewed = renewed,
        users = conn.int("SELECT COUNT(*) FROM users"),
        grossTotal = conn.revenueTotal(false),
        netTotal = conn.revenueTotal(true),
        abA = conn.abGroup("a"),
        abB = conn.abGroup("b"),
        daily = moneyDaily(conn),
    )
}

internal fun buildTimeline(conn: Connection, input: String): UserTimeline {
    val uid = resolveUserCode(conn, input) ?: return UserTimeline(found = false)
    val created = conn.queryOne("SELECT created_at FROM users WHERE id = ?", uid) { it.getString(1) } ?: return UserTimeline(false)
    val until = conn.queryOne("SELECT subscribed_until FROM users WHERE id = ?", uid) { it.getString(1) }
    val purchases = buildList {
        conn.list("SELECT product_id, store, created_at, subscribed_until FROM subscription_purchases WHERE user_id = ? ORDER BY created_at DESC LIMIT 20", uid) {
            add(TimelinePurchase(it.getString(1).substringAfterLast('_'), it.getString(2), it.getString(3), it.getString(4)))
        }
    }
    val installs = buildList {
        conn.list("SELECT install_id, first_day, last_day, active_days, app_version, store, profile FROM installs WHERE user_id = ? ORDER BY last_day DESC LIMIT 5", uid) {
            val model = it.getString(7)?.let { p -> Regex("\"device_model\"\\s*:\\s*\"([^\"]*)\"").find(p)?.groupValues?.get(1) }
            add(TimelineInstall(it.getString(1).take(8), it.getString(2), it.getString(3), it.getInt(4), it.getObject(5) as? Int, it.getString(6), model))
        }
    }
    val installFilter = "install_id IN (SELECT install_id FROM installs WHERE user_id = $uid)"
    val top = buildList {
        conn.list("SELECT name, SUM(count) FROM usage_daily WHERE $installFilter AND name LIKE 'screen:%' GROUP BY 1 ORDER BY 2 DESC LIMIT 6") {
            add(NamedCount(it.getString(1).removePrefix("screen:"), it.getInt(2)))
        }
    }
    val days = buildList {
        conn.list(
            "SELECT day, SUM(count), SUM(CASE WHEN name LIKE 'screen:%' THEN count ELSE 0 END) FROM usage_daily " +
                "WHERE $installFilter GROUP BY day ORDER BY day DESC LIMIT 14",
        ) { add(TimelineDay(it.getString(1), it.getInt(2), it.getInt(3))) }
    }
    return UserTimeline(
        found = true,
        code = userCodeOf(conn, uid) ?: uid.toString(),
        createdAt = created,
        subscribedUntil = until,
        purchases = purchases,
        supportCount = conn.int("SELECT COUNT(*) FROM bug_reports WHERE user_id = ?", uid),
        lastSupport = conn.queryOne("SELECT message FROM bug_reports WHERE user_id = ? ORDER BY created_at DESC LIMIT 1", uid) { it.getString(1) }?.take(140),
        installs = installs,
        topScreens = top,
        days = days,
    )
}

/** گروه‌های پیامِ هدفمند. هر کدام یک `WHERE` روی `users`. */
private val SEGMENTS = mapOf(
    "all" to "1 = 1",
    // نیامده‌ها: هیچ نصبِ وصل‌شده‌ای در ۷ روزِ اخیر فعال نبوده.
    "inactive7" to "id NOT IN (SELECT user_id FROM installs WHERE user_id IS NOT NULL AND last_day >= date('now', '+210 minutes', '-7 days'))",
    // مجانیِ رو به اتمام: ۲۷ تا ۳۰ روز از ثبت‌نام گذشته و هنوز نخریده.
    "trial_ending" to "created_at <= datetime('now', '-27 days') AND created_at > datetime('now', '-30 days') AND id NOT IN (SELECT user_id FROM subscription_purchases)",
    // اشتراکِ خریده‌شده‌ی تمام‌شده، بدونِ تمدید.
    "expired" to "id IN (SELECT user_id FROM subscription_purchases) AND (subscribed_until IS NULL OR subscribed_until < datetime('now'))",
    "free" to "id NOT IN (SELECT user_id FROM subscription_purchases)",
    // ۱۰ مهر: گروه‌های تازه.
    "inactive30" to "id NOT IN (SELECT user_id FROM installs WHERE user_id IS NOT NULL AND last_day >= date('now', '+210 minutes', '-30 days'))",
    "new7" to "created_at >= datetime('now', '-7 days')",
    "cafebazaar" to "id IN (SELECT user_id FROM installs WHERE user_id IS NOT NULL AND store = 'cafebazaar')",
    "myket" to "id IN (SELECT user_id FROM installs WHERE user_id IS NOT NULL AND store = 'myket')",
    // نسخه‌ی قدیمی: آخرین نصبِ فعالِ این حساب از آخرین نسخه‌ی ثبت‌شده عقب‌تر است.
    "old_version" to "id IN (SELECT user_id FROM installs WHERE user_id IS NOT NULL AND app_version IS NOT NULL " +
        "AND app_version < (SELECT latest_version_code FROM app_version WHERE id = 1))",
    "paid" to "subscribed_until >= datetime('now') AND id IN (SELECT user_id FROM subscription_purchases)",
)

private val BROADCAST_ACTIONS = setOf("shop", "subscription", "update")

internal fun broadcast(conn: Connection, b: BroadcastBody): BroadcastResult? {
    val where = SEGMENTS[b.segment] ?: return null
    if (b.days !in 0..365 || b.coins !in 0..100_000) return null
    val action = b.action?.takeIf { it in BROADCAST_ACTIONS }
    val ids = buildList { conn.list("SELECT id FROM users WHERE $where") { add(it.getLong(1)) } }
    if (b.dryRun || b.title.isBlank() || b.body.isBlank()) return BroadcastResult(b.segment, ids.size, false)
    val gift = b.days > 0 || b.coins > 0
    val now = System.currentTimeMillis()
    ids.forEach { uid ->
        if (b.days > 0) {
            val cur = conn.queryOne("SELECT subscribed_until FROM users WHERE id = ?", uid) { it.getString(1) }
                ?.let { runCatching { java.time.Instant.parse(it).toEpochMilli() }.getOrNull() } ?: 0L
            val expiry = java.time.Instant.ofEpochMilli(maxOf(cur, now) + b.days * 24L * 60 * 60 * 1000).toString()
            conn.execute("UPDATE users SET subscribed_until = ? WHERE id = ?", expiry, uid)
        }
        conn.execute(
            "INSERT INTO announcements (title, body, kind, target_user_id, coins, action) VALUES (?, ?, ?, ?, ?, ?)",
            b.title.take(80), b.body.take(600), if (gift) "gift" else "info", uid, b.coins, action,
        )
    }
    return BroadcastResult(b.segment, ids.size, true)
}

/** تعدادِ هر گروه، برای نشان‌دادنِ عدد کنارِ همه‌ی کارت‌ها. */
internal fun broadcastCounts(conn: Connection): Map<String, Int> =
    SEGMENTS.mapValues { (_, where) -> runCatching { conn.int("SELECT COUNT(*) FROM users WHERE $where") }.getOrDefault(0) }

fun Route.adminProRoutes() {
    route("/api/admin") {
        get("/money") {
            if (!call.adminOrNull()) return@get
            call.respond(Db.withConnection { buildMoney(it) })
        }
        get("/user") {
            if (!call.adminOrNull()) return@get
            val code = call.request.queryParameters["code"].orEmpty()
            call.respond(Db.withConnection { buildTimeline(it, code) })
        }
        get("/broadcast/counts") {
            if (!call.adminOrNull()) return@get
            call.respond(Db.withConnection { broadcastCounts(it) })
        }
        post("/broadcast") {
            if (!call.adminOrNull()) return@post
            val body = call.receive<BroadcastBody>()
            val result = Db.withConnection { broadcast(it, body) }
            if (result == null) call.respond(HttpStatusCode.BadRequest, mapOf("error" to "bad_segment")) else call.respond(result)
        }
    }
}
