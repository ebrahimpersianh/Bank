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
import ir.sadteam.loancalc.server.trialEndsAtMs
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
    /** ۱۶ مهر: «چه کسی خرید» - ۵۰ خریدِ آخر، تازه‌ترین اول. */
    val recent: List<RecentPurchase> = emptyList(),
    /** فروشِ امروز / ۷ روز / ۳۰ روز به‌تفکیکِ استور. */
    val periods: List<SalesPeriod> = emptyList(),
)

@Serializable
data class RecentPurchase(val code: String, val phone: String, val product: String, val store: String, val price: Long, val at: String)

@Serializable
data class SalesPeriod(val label: String, val count: Int, val net: Long, val cafebazaar: Int, val myket: Int)

/** جزئیاتِ کاشی‌های سلامت (کرش، خطای همگام‌سازی، گوشیِ چندحسابه، ماهِ رایگانِ ردشده). */
@Serializable
data class HealthRow(val title: String, val sub: String = "", val at: String = "")

@Serializable
data class HealthDetail(val kind: String, val rows: List<HealthRow>)

/** «09121234567» → «0912•••4567». */
private fun maskPhone(p: String?): String {
    val d = p.orEmpty()
    return if (d.length >= 8) d.take(4) + "•••" + d.takeLast(4) else d
}

private fun recentPurchases(conn: Connection): List<RecentPurchase> = buildList {
    conn.list(
        "SELECT p.user_id, u.phone, p.product_id, p.store, coalesce(p.price_toman, 0), p.created_at FROM subscription_purchases p " +
            "LEFT JOIN users u ON u.id = p.user_id ORDER BY p.created_at DESC LIMIT 50",
    ) {
        val uid = it.getLong(1)
        add(
            RecentPurchase(
                code = userCodeOf(conn, uid)?.removePrefix("Uid:") ?: uid.toString(),
                phone = maskPhone(it.getString(2)),
                product = it.getString(3).substringAfterLast('_'),
                store = it.getString(4),
                price = it.getLong(5),
                at = it.getString(6),
            ),
        )
    }
}

private fun salesPeriods(conn: Connection): List<SalesPeriod> {
    val fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val nowUtc = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC)
    // «امروز» = از نیمه‌شبِ ایران.
    val todayStart = nowUtc.plusMinutes(210).toLocalDate().atStartOfDay().minusMinutes(210)
    return listOf(
        "امروز" to todayStart,
        "۷ روز" to nowUtc.minusDays(7),
        "۳۰ روز" to nowUtc.minusDays(30),
    ).map { (label, from) ->
        var count = 0; var net = 0L; var bazaar = 0; var myket = 0
        conn.list(
            "SELECT store, COUNT(*), coalesce(SUM(price_toman),0) FROM subscription_purchases WHERE created_at >= ? GROUP BY store",
            from.format(fmt),
        ) {
            val store = it.getString(1); val c = it.getInt(2); val gross = it.getLong(3)
            val r = STORE_PAYOUT[store]
            count += c
            net += if (r != null) gross * r.first / r.second else gross
            if (store == "cafebazaar") bazaar += c else if (store == "myket") myket += c
        }
        SalesPeriod(label, count, net, bazaar, myket)
    }
}

internal fun healthDetail(conn: Connection, kind: String): HealthDetail {
    val d30 = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).minusDays(30)
        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    val rows = buildList {
        when (kind) {
            "crash", "sync" -> conn.list(
                "SELECT message, coalesce(app_version, ''), created_at FROM crash_reports WHERE created_at >= ? AND " +
                    (if (kind == "sync") "context = 'sync'" else "coalesce(context, '') NOT IN ('sync', 'slow')") +
                    " ORDER BY created_at DESC LIMIT 60",
                d30,
            ) { add(HealthRow(it.getString(1).take(160), if (it.getString(2).isBlank()) "" else "نسخه‌ی ${it.getString(2)}", it.getString(3))) }
            "multi" -> conn.list(
                "SELECT device_hash, COUNT(*), GROUP_CONCAT(user_id), MAX(first_seen) FROM device_users " +
                    "GROUP BY device_hash HAVING COUNT(*) >= 3 ORDER BY 2 DESC LIMIT 60",
            ) {
                val codes = it.getString(3).orEmpty().split(',').mapNotNull { s -> s.trim().toLongOrNull() }
                    .joinToString("، ") { uid -> userCodeOf(conn, uid)?.removePrefix("Uid:") ?: uid.toString() }
                add(HealthRow("گوشیِ ${it.getString(1).take(6)} · ${it.getInt(2)} حساب", codes, it.getString(4)))
            }
            "trial" -> conn.list(
                "SELECT id, phone, created_at FROM users WHERE trial_blocked = 1 ORDER BY created_at DESC LIMIT 100",
            ) { add(HealthRow(userCodeOf(conn, it.getLong(1))?.removePrefix("Uid:") ?: it.getLong(1).toString(), maskPhone(it.getString(2)), it.getString(3))) }
        }
    }
    return HealthDetail(kind, rows)
}

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
    /** فقط برای گروهِ «one»: شماره‌ی کاربری (Uid). */
    val user: String? = null,
)

@Serializable
data class BroadcastHistoryItem(val batchId: Long, val title: String, val segment: String, val sentAt: String, val sent: Int, val opened: Int)

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
        recent = runCatching { recentPurchases(conn) }.getOrDefault(emptyList()),
        periods = runCatching { salesPeriods(conn) }.getOrDefault(emptyList()),
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
    // 🔒 شناسه با «?» می‌رود، نه چسبیده به متنِ SQL (۱۶ مهر).
    val installFilter = "install_id IN (SELECT install_id FROM installs WHERE user_id = ?)"
    val top = buildList {
        conn.list("SELECT name, SUM(count) FROM usage_daily WHERE $installFilter AND name LIKE 'screen:%' GROUP BY 1 ORDER BY 2 DESC LIMIT 6", uid) {
            add(NamedCount(it.getString(1).removePrefix("screen:"), it.getInt(2)))
        }
    }
    val days = buildList {
        conn.list(
            "SELECT day, SUM(count), SUM(CASE WHEN name LIKE 'screen:%' THEN count ELSE 0 END) FROM usage_daily " +
                "WHERE $installFilter GROUP BY day ORDER BY day DESC LIMIT 14",
            uid,
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
    if (b.days !in 0..365 || b.coins !in 0..100_000) return null
    val action = b.action?.takeIf { it in BROADCAST_ACTIONS }
    val ids: List<Long> = if (b.segment == "one") {
        listOfNotNull(b.user?.let { resolveUserCode(conn, it) })
    } else {
        val where = SEGMENTS[b.segment] ?: return null
        buildList { conn.list("SELECT id FROM users WHERE $where") { add(it.getLong(1)) } }
    }
    if (b.dryRun || b.title.isBlank() || b.body.isBlank()) return BroadcastResult(b.segment, ids.size, false)
    val gift = b.days > 0 || b.coins > 0
    val now = System.currentTimeMillis()
    val batch = now
    conn.execute(
        "INSERT OR REPLACE INTO app_config (key, json) VALUES (?, ?)",
        "broadcast_$batch", kotlinx.serialization.json.Json.encodeToString(kotlinx.serialization.json.JsonPrimitive.serializer(), kotlinx.serialization.json.JsonPrimitive(b.segment)),
    )
    ids.forEach { uid ->
        if (b.days > 0) {
            val cur = conn.queryOne("SELECT subscribed_until FROM users WHERE id = ?", uid) { it.getString(1) }
                ?.let { runCatching { java.time.Instant.parse(it).toEpochMilli() }.getOrNull() } ?: 0L
            // ۲۰ مهر (خواسته‌ی کاربر): کسی که هنوز در ماهِ رایگان است، هدیه را **بعد از پایانِ** آن می‌گیرد،
            // نه از امروز (وگرنه روزهای هدیه با روزهای رایگانِ باقی‌مانده روی هم می‌افتادند).
            val trialEnd = conn.queryOne("SELECT created_at, trial_days, trial_blocked FROM users WHERE id = ?", uid) { r ->
                if (r.getInt(3) != 0) 0L
                else trialEndsAtMs(r.getString(1), r.getInt(2).takeIf { !r.wasNull() }) ?: 0L
            } ?: 0L
            val expiry = java.time.Instant.ofEpochMilli(maxOf(cur, now, trialEnd) + b.days * 24L * 60 * 60 * 1000).toString()
            conn.execute("UPDATE users SET subscribed_until = ? WHERE id = ?", expiry, uid)
        }
        conn.execute(
            "INSERT INTO announcements (title, body, kind, target_user_id, coins, action, batch_id) VALUES (?, ?, ?, ?, ?, ?, ?)",
            b.title.take(80), b.body.take(600), if (gift) "gift" else "info", uid, b.coins, action, batch,
        )
    }
    return BroadcastResult(b.segment, ids.size, true)
}

/** ۱۰ پیامِ گروهیِ آخر با تعدادِ فرستاده/دیده‌شده. */
internal fun broadcastHistory(conn: Connection): List<BroadcastHistoryItem> = buildList {
    conn.list(
        "SELECT batch_id, MIN(title), MIN(created_at), COUNT(*), SUM(CASE WHEN read_at IS NOT NULL THEN 1 ELSE 0 END) " +
            "FROM announcements WHERE batch_id IS NOT NULL GROUP BY batch_id ORDER BY batch_id DESC LIMIT 10",
    ) {
        val id = it.getLong(1)
        val seg = runCatching {
            conn.queryOne("SELECT json FROM app_config WHERE key = ?", "broadcast_$id") { r -> r.getString(1) }?.trim('"')
        }.getOrNull().orEmpty()
        add(BroadcastHistoryItem(id, it.getString(2), seg, it.getString(3), it.getInt(4), it.getInt(5)))
    }
}

@Serializable
data class AdminUserRow(
    val code: String,
    val phone: String,
    val createdAt: String,
    val subscribedUntil: String? = null,
    val paid: Boolean = false,
    val store: String? = null,
    val lastDay: String? = null,
    /** آخرین بازدید با ساعت (UTC، «YYYY-MM-DD HH:MM:SS») - ۱۴ مهر. */
    val lastSeenAt: String? = null,
)

@Serializable
data class AdminUserList(val users: List<AdminUserRow>)

internal fun listUsers(conn: Connection, q: String): List<AdminUserRow> {
    val rows = buildList {
        conn.list(
            "SELECT u.id, u.phone, u.created_at, u.subscribed_until, " +
                "EXISTS(SELECT 1 FROM subscription_purchases p WHERE p.user_id = u.id), " +
                "(SELECT store FROM installs i WHERE i.user_id = u.id ORDER BY last_day DESC LIMIT 1), " +
                "(SELECT MAX(last_day) FROM installs i WHERE i.user_id = u.id), " +
                "(SELECT MAX(last_seen_at) FROM installs i WHERE i.user_id = u.id) AS seen " +
                "FROM users u " + (if (q.isNotEmpty()) "WHERE u.phone LIKE ? " else "") +
                // ۱۴ مهر: بر اساسِ آخرین بازدید (خواسته‌ی کاربر)؛ بی‌بازدیدها با تاریخِ ثبت‌نام.
                "ORDER BY COALESCE(seen, u.created_at) DESC LIMIT 300",
            *(if (q.isNotEmpty()) arrayOf<Any?>("%$q%") else emptyArray()),
        ) {
            add(
                AdminUserRow(
                    code = it.getLong(1).toString(), phone = it.getString(2), createdAt = it.getString(3),
                    subscribedUntil = it.getString(4), paid = it.getInt(5) != 0, store = it.getString(6), lastDay = it.getString(7),
                    lastSeenAt = it.getString(8),
                ),
            )
        }
    }
    // id → Uid (شماره‌ی کاربری‌ای که کاربر در برنامه می‌بیند).
    val mapped = rows.map { r -> r.copy(code = userCodeOf(conn, r.code.toLong())?.removePrefix("Uid:") ?: r.code) }
    return if (q.isEmpty() || mapped.isNotEmpty()) mapped
    else resolveUserCode(conn, q)
        ?.let { uid -> conn.queryOne("SELECT phone FROM users WHERE id = ?", uid) { it.getString(1) } }
        ?.takeIf { it.isNotBlank() }
        ?.let { phone -> listUsers(conn, phone) }
        .orEmpty()
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
        get("/health") {
            if (!call.adminOrNull()) return@get
            val kind = call.request.queryParameters["kind"].orEmpty().takeIf { it in setOf("crash", "sync", "multi", "trial") }
            if (kind == null) { call.respond(HttpStatusCode.BadRequest, mapOf("error" to "bad_kind")); return@get }
            call.respond(Db.withConnection { healthDetail(it, kind) })
        }
        get("/user") {
            if (!call.adminOrNull()) return@get
            val code = call.request.queryParameters["code"].orEmpty()
            call.respond(Db.withConnection { buildTimeline(it, code) })
        }
        // 👥 فهرستِ کاربران (تازه‌ترین اول) + جستجو با بخشی از شماره یا Uid.
        get("/users") {
            if (!call.adminOrNull()) return@get
            val q = call.request.queryParameters["q"].orEmpty().filter { it.isDigit() }
            call.respond(Db.withConnection { conn -> AdminUserList(listUsers(conn, q)) })
        }
        get("/broadcast/history") {
            if (!call.adminOrNull()) return@get
            call.respond(Db.withConnection { broadcastHistory(it) })
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
