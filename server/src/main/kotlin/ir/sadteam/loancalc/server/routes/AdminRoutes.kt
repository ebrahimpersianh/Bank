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
data class ProfileSplit(val key: String, val values: List<NamedCount>)

/** «چند نصب این بخش را دارند» + میانگینِ تعداد در همان نصب‌ها (مثلاً ۳ وام). */
@Serializable
data class FeatureAdoption(val key: String, val installs: Int, val avg: Double)

/** یک ردیف برای هر نصبِ بی‌نام - شناسه فقط ۶ حرفِ اولِ شناسه‌ی تصادفیِ خودِ اپ است. */
@Serializable
data class InstallRow(
    val id: String,
    val firstDay: String,
    val lastDay: String,
    val activeDays: Int,
    val version: Int? = null,
    val store: String? = null,
    val device: String? = null,
    val android: String? = null,
    val sessions30: Int = 0,
    val minutes30: Int = 0,
    val topScreen: String? = null,
    val loggedIn: Boolean = false,
)

/** فروشِ واقعی از جدولِ `subscription_purchases` (خرید‌های تأییدشده‌ی سرور). مبلغ به تومان. */
@Serializable
data class SaleRow(val product: String, val count30: Int, val countAll: Int, val tomans30: Long, val tomansAll: Long)

/** قیمتِ هر پلن به تومان - همان قیمتِ پنلِ کافه‌بازار/مایکت (رجوع کن به CLAUDE.md). */
private val PLAN_PRICE_TOMAN = mapOf(
    "unlimited_loans_1m" to 30_000L,
    "unlimited_loans_3m" to 81_000L,
    "unlimited_loans_6m" to 144_000L,
    "unlimited_loans_1y" to 252_000L,
)

/** گروهِ هفتگیِ نصب: چند نفر در هفته‌ی k بعد از نصب هنوز برنامه را باز کرده‌اند. */
@Serializable
data class CohortRow(val weekStart: String, val size: Int, val weeks: List<Int>)

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
    // ۷ مهر - جزئیاتِ بیشتر (خواسته‌ی کاربر). همه پیش‌فرض دارند تا اپِ قدیمی نشکند.
    val hours: List<NamedCount> = emptyList(),
    val weekdays: List<NamedCount> = emptyList(),
    val avgSessionMinutes: Double = 0.0,
    val totalMinutes30: Int = 0,
    val avgScreensPerSession: Double = 0.0,
    val screenTime: List<FeatureUsage> = emptyList(),
    val profiledInstalls: Int = 0,
    val profileSplits: List<ProfileSplit> = emptyList(),
    val adoption: List<FeatureAdoption> = emptyList(),
    val installsList: List<InstallRow> = emptyList(),
    val sales: List<SaleRow> = emptyList(),
    val salesByStore: List<NamedCount> = emptyList(),
    val salesDaily: List<NamedCount> = emptyList(),
    val activeSubscribers: Int = 0,
    val activeByTier: List<NamedCount> = emptyList(),
    val giftsUsed30: Int = 0,
    val activeNow: Int = 0,
    val activeLastHour: Int = 0,
    val avgDau30: Double = 0.0,
    val cohorts: List<CohortRow> = emptyList(),
    val churned: Int = 0,
    val flows: List<FeatureUsage> = emptyList(),
    val errors: List<FeatureUsage> = emptyList(),
    val perf: List<FeatureUsage> = emptyList(),
    val crashes30: Int = 0,
    val nonFatal30: Int = 0,
    val crashesByVersion: List<NamedCount> = emptyList(),
    val topCrashes: List<NamedCount> = emptyList(),
    val activeByVersion: List<NamedCount> = emptyList(),
)

/** ترتیبِ نمایشِ مشخصات؛ کلیدِ ناشناخته آخرِ فهرست می‌آید. */
private val PROFILE_ORDER = listOf(
    "subscription", "device_brand", "device_model", "android", "screen_dp", "lang", "system_dark",
    "theme_mode", "color_theme", "font_scale_app", "font_scale_sys", "lock", "biometric", "privacy_mode",
    "perm_notifications", "perm_sms", "perm_calendar", "notif_listener", "battery_unrestricted",
    "sms_import", "notif_import", "reminders", "reminder_hour", "daily_reminder", "auto_backup",
    "vibration", "reduced_motion", "owned_themes", "owned_items",
)

private fun parseProfile(raw: String?): Map<String, String> = runCatching {
    kotlinx.serialization.json.Json.parseToJsonElement(raw ?: return emptyMap()).let { el ->
        (el as kotlinx.serialization.json.JsonObject).mapValues { (_, v) ->
            (v as? kotlinx.serialization.json.JsonPrimitive)?.content.orEmpty()
        }
    }
}.getOrDefault(emptyMap())

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

    fun sumOf(name: String, since: String) =
        conn.int("SELECT coalesce(SUM(count), 0) FROM usage_daily WHERE day >= ? AND name = ?", since, name)
    fun grouped(prefix: String): List<NamedCount> = buildList {
        conn.list(
            "SELECT name, SUM(count) FROM usage_daily WHERE day >= ? AND name LIKE ? GROUP BY name ORDER BY name",
            d30, "$prefix%",
        ) { add(NamedCount(it.getString(1).removePrefix(prefix), it.getInt(2))) }
    }
    val sessions30 = sumOf("session_start", d30)
    val appSeconds30 = sumOf("time:app", d30)
    val screens30 = conn.int("SELECT coalesce(SUM(count), 0) FROM usage_daily WHERE day >= ? AND name LIKE 'screen:%'", d30)

    // مشخصاتِ نصب‌های فعالِ ۳۰ روزِ اخیر.
    val profiles = buildList {
        conn.list("SELECT profile FROM installs WHERE last_day >= ? AND profile IS NOT NULL", d30) {
            add(parseProfile(it.getString(1)))
        }
    }.filter { it.isNotEmpty() }
    val splitKeys = profiles.flatMap { it.keys }.filter { !it.startsWith("n_") }.distinct()
        .sortedBy { k -> PROFILE_ORDER.indexOf(k).let { if (it < 0) 999 else it } }
    val profileSplits = splitKeys.map { key ->
        ProfileSplit(
            key,
            profiles.mapNotNull { it[key] }.groupingBy { it }.eachCount()
                .entries.sortedByDescending { it.value }.take(12).map { NamedCount(it.key, it.value) },
        )
    }
    val adoption = profiles.flatMap { it.keys }.filter { it.startsWith("n_") }.distinct().map { key ->
        val counts = profiles.mapNotNull { it[key]?.toLongOrNull() }.filter { it > 0 }
        FeatureAdoption(key.removePrefix("n_"), counts.size, if (counts.isEmpty()) 0.0 else Math.round(counts.average() * 10) / 10.0)
    }.sortedByDescending { it.installs }

    val installsList = buildList {
        conn.list(
            "SELECT install_id, first_day, last_day, active_days, app_version, store, profile, logged_in FROM installs " +
                "ORDER BY last_day DESC, first_day DESC LIMIT 60",
        ) { rs ->
            val id = rs.getString(1)
            val prof = parseProfile(rs.getString(7))
            add(
                id to InstallRow(
                    id = id.take(6),
                    firstDay = rs.getString(2),
                    lastDay = rs.getString(3),
                    activeDays = rs.getInt(4),
                    version = rs.getInt(5).takeIf { !rs.wasNull() },
                    store = rs.getString(6),
                    device = listOfNotNull(prof["device_brand"], prof["device_model"]).joinToString(" ").ifBlank { null },
                    android = prof["android"],
                    loggedIn = rs.getInt(8) == 1,
                ),
            )
        }
    }.map { (full, row) ->
        row.copy(
            sessions30 = conn.int("SELECT coalesce(SUM(count),0) FROM usage_daily WHERE install_id = ? AND day >= ? AND name = 'session_start'", full, d30),
            minutes30 = conn.int("SELECT coalesce(SUM(count),0) FROM usage_daily WHERE install_id = ? AND day >= ? AND name = 'time:app'", full, d30) / 60,
            topScreen = conn.queryOne(
                "SELECT name FROM usage_daily WHERE install_id = ? AND day >= ? AND name LIKE 'screen:%' GROUP BY name ORDER BY SUM(count) DESC LIMIT 1",
                full, d30,
            ) { it.getString(1).removePrefix("screen:") },
        )
    }

    // 💰 فروش - فقط از خرید‌هایی که سرور خودش تأیید کرده.
    val sales = buildList {
        conn.list(
            "SELECT product_id, SUM(CASE WHEN created_at >= ? THEN 1 ELSE 0 END), COUNT(*) FROM subscription_purchases GROUP BY product_id ORDER BY 3 DESC",
            d30,
        ) {
            val product = it.getString(1)
            val price = PLAN_PRICE_TOMAN[product] ?: 0L
            add(SaleRow(product.substringAfterLast('_'), it.getInt(2), it.getInt(3), price * it.getInt(2), price * it.getInt(3)))
        }
    }
    val salesByStore = buildList {
        conn.list("SELECT store, COUNT(*) FROM subscription_purchases WHERE created_at >= ? GROUP BY store ORDER BY 2 DESC", d30) {
            add(NamedCount(it.getString(1), it.getInt(2)))
        }
    }
    val salesByDay = HashMap<String, Int>()
    conn.list("SELECT substr(created_at, 1, 10), COUNT(*) FROM subscription_purchases WHERE created_at >= ? GROUP BY 1", d30) {
        salesByDay[it.getString(1)] = it.getInt(2)
    }
    val salesDaily = (29 downTo 0).map { back -> iranDay(-back.toLong()).let { d -> NamedCount(d, salesByDay[d] ?: 0) } }
    val now = java.time.Instant.now()
    val activeTiers = buildList {
        conn.list("SELECT coalesce(subscription_tier, '?'), subscribed_until FROM users WHERE subscribed_until IS NOT NULL") {
            val until = runCatching { java.time.Instant.parse(it.getString(2)) }.getOrNull()
            if (until != null && until.isAfter(now)) add(it.getString(1))
        }
    }
    val activeByTier = activeTiers.groupingBy { it }.eachCount().entries.sortedByDescending { it.value }.map { NamedCount(it.key, it.value) }
    val giftsUsed30 = conn.int("SELECT COUNT(*) FROM gift_codes WHERE used_at IS NOT NULL AND used_at >= ?", d30)

    // ⏱ «الان» - از آخرین بسته‌ی هر نصب (هر ۳ دقیقه یا رفتن به پس‌زمینه).
    val activeNow = conn.int("SELECT COUNT(*) FROM installs WHERE last_seen_at >= datetime('now', '-5 minutes')")
    val activeLastHour = conn.int("SELECT COUNT(*) FROM installs WHERE last_seen_at >= datetime('now', '-60 minutes')")
    val avgDau30 = Math.round(daily.map { it.active }.average().takeIf { !it.isNaN() }?.times(10) ?: 0.0) / 10.0

    // 📅 ماندگاریِ گروهی: ۸ هفته‌ی اخیر، هر ردیف = نصب‌های آن هفته.
    val cohorts = (7 downTo 0).map { back ->
        val start = iranDay(-7L * back - 6)
        val end = iranDay(-7L * back)
        val size = conn.int("SELECT COUNT(*) FROM installs WHERE first_day BETWEEN ? AND ?", start, end)
        val weeks = (0..(7 - back)).map { k ->
            val ws = java.time.LocalDate.parse(start).plusDays(7L * k).toString()
            val we = java.time.LocalDate.parse(start).plusDays(7L * k + 6).toString()
            conn.int(
                "SELECT COUNT(DISTINCT u.install_id) FROM usage_daily u JOIN installs i ON i.install_id = u.install_id " +
                    "WHERE i.first_day BETWEEN ? AND ? AND u.day BETWEEN ? AND ?",
                start, end, ws, we,
            )
        }
        CohortRow(start, size, weeks)
    }
    val churnCut = iranDay(-14)
    val churned = conn.int("SELECT COUNT(*) FROM installs WHERE last_day < ?", churnCut)

    // 💥 کرش‌ها - `context = 'sync'` خطای بی‌سروصدای همگام‌سازی است، نه کرش.
    val crashes30 = conn.int("SELECT COUNT(*) FROM crash_reports WHERE created_at >= ? AND coalesce(context, '') <> 'sync'", d30)
    val nonFatal30 = conn.int("SELECT COUNT(*) FROM crash_reports WHERE created_at >= ? AND context = 'sync'", d30)
    val crashesByVersion = buildList {
        conn.list(
            "SELECT coalesce(app_version, '?'), COUNT(*) FROM crash_reports WHERE created_at >= ? AND coalesce(context, '') <> 'sync' GROUP BY 1 ORDER BY 2 DESC LIMIT 12",
            d30,
        ) { add(NamedCount(it.getString(1), it.getInt(2))) }
    }
    val topCrashes = buildList {
        conn.list(
            "SELECT substr(message, 1, 90), COUNT(*) FROM crash_reports WHERE created_at >= ? AND coalesce(context, '') <> 'sync' GROUP BY 1 ORDER BY 2 DESC LIMIT 6",
            d30,
        ) { add(NamedCount(it.getString(1), it.getInt(2))) }
    }

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
        hours = grouped("hour:"),
        weekdays = grouped("dow:"),
        avgSessionMinutes = if (sessions30 == 0) 0.0 else Math.round(appSeconds30 / 60.0 / sessions30 * 10) / 10.0,
        totalMinutes30 = appSeconds30 / 60,
        avgScreensPerSession = if (sessions30 == 0) 0.0 else Math.round(screens30.toDouble() / sessions30 * 10) / 10.0,
        screenTime = features("time:screen:"),
        profiledInstalls = profiles.size,
        profileSplits = profileSplits,
        adoption = adoption,
        installsList = installsList,
        sales = sales,
        salesByStore = salesByStore,
        salesDaily = salesDaily,
        activeSubscribers = activeTiers.size,
        activeByTier = activeByTier,
        giftsUsed30 = giftsUsed30,
        activeNow = activeNow,
        activeLastHour = activeLastHour,
        avgDau30 = avgDau30,
        cohorts = cohorts,
        churned = churned,
        flows = features("flow:"),
        errors = features("error:"),
        perf = features("perf:"),
        crashes30 = crashes30,
        nonFatal30 = nonFatal30,
        crashesByVersion = crashesByVersion,
        topCrashes = topCrashes,
        activeByVersion = split("app_version"),
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
