package ir.sadteam.loancalc.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant

/**
 * نگه‌داریِ داده‌ی ابری پس از پایانِ اشتراک (تصمیمِ کاربر، ۷ مهر): **۹۰ روز**، بعد پاک.
 * دو یادآوری در «پیام‌های جیبک»: ۳۰ و ۷ روز مانده به پاک‌شدن. داده‌ی روی گوشیِ کاربر دست نمی‌خورد.
 *
 * `cloud_warn_for` = لحظه‌ی انقضایی که یادآوری‌ها برایش فرستاده شده؛ با تمدید و انقضای دوباره
 * عوض می‌شود و یادآوری‌ها از نو فرستاده می‌شوند. `cloud_warn_stage`: ۰ هیچ، ۱ سی‌روزه، ۲ هفت‌روزه.
 */
object CloudRetention {
    const val KEEP_DAYS = 90L
    private const val DAY_MS = 24L * 60 * 60 * 1000
    private const val TRIAL_MS = 30L * DAY_MS

    /** ساعتِ ۹۰ روزه برای کسانی که **پیش از راه‌اندازیِ این قانون** منقضی شده‌اند از همین لحظه
     * شمرده می‌شود - وگرنه اولین اجرا داده‌ی همه‌ی کاربرانِ قدیمیِ «وام من» را فوراً پاک می‌کرد. */
    private val POLICY_START: Long = parse(env("CLOUD_RETENTION_START", "2026-10-01T00:00:00Z")) ?: Long.MAX_VALUE

    fun start(scope: CoroutineScope) {
        scope.launch {
            delay(5 * 60_000)
            while (true) {
                runCatching { runOnce(System.currentTimeMillis()) }
                runCatching { SupportFiles.sweep() }
                delay(6 * 60 * 60_000L)
            }
        }
    }

    private fun parse(s: String?): Long? = s?.let {
        runCatching { Instant.parse(it).toEpochMilli() }.getOrNull()
            ?: runCatching { Instant.parse(it.replace(' ', 'T') + "Z").toEpochMilli() }.getOrNull()
    }

    private fun filesRoot(uid: Long): File =
        File(env("FILES_DIR", System.getProperty("user.home") + "/VameMan/files"), uid.toString())

    fun runOnce(now: Long) {
        Db.withConnection { conn ->
            val rows = conn.prepareStatement(
                "SELECT id, subscribed, subscribed_until, created_at, cloud_warn_for, cloud_warn_stage FROM users WHERE subscribed = 0",
            ).use { ps ->
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) add(
                            listOf(rs.getLong(1), rs.getString(3), rs.getString(4), rs.getString(5), rs.getInt(6)),
                        )
                    }
                }
            }
            for (r in rows) {
                val uid = r[0] as Long
                val expiry = maxOf(parse(r[1] as String?) ?: 0L, (parse(r[2] as String?) ?: 0L) + TRIAL_MS)
                if (expiry <= 0L || expiry > now) continue
                val clockStart = maxOf(expiry, POLICY_START)
                if (clockStart > now) continue
                val hasCloud = listOf("accounts_backup", "cheques_backup", "loans").any { t ->
                    conn.queryOne("SELECT 1 AS x FROM $t WHERE user_id = ? LIMIT 1", uid) { true } == true
                } || filesRoot(uid).exists()
                if (!hasCloud) continue
                val deleteAt = clockStart + KEEP_DAYS * DAY_MS
                val daysLeft = (deleteAt - now) / DAY_MS
                val warnFor = Instant.ofEpochMilli(clockStart).toString()
                val stage = if (r[3] as String? == warnFor) r[4] as Int else 0
                when {
                    now >= deleteAt -> {
                        listOf("accounts_backup", "cheques_backup", "loans").forEach { t ->
                            conn.execute("DELETE FROM $t WHERE user_id = ?", uid)
                        }
                        filesRoot(uid).deleteRecursively()
                        conn.execute("UPDATE users SET cloud_warn_for = NULL, cloud_warn_stage = 0 WHERE id = ?", uid)
                    }
                    daysLeft <= 7 && stage < 2 -> warn(conn, uid, warnFor, 2, daysLeft)
                    daysLeft <= 30 && stage < 1 -> warn(conn, uid, warnFor, 1, daysLeft)
                }
            }
        }
    }

    private fun warn(conn: java.sql.Connection, uid: Long, warnFor: String, stage: Int, daysLeft: Long) {
        conn.insertReturningId(
            "INSERT INTO announcements (title, body, kind, target_user_id) VALUES (?, ?, ?, ?)",
            "☁️ اطلاعاتِ ابریت به‌زودی پاک می‌شه",
            "اشتراکت تموم شده و نسخه‌ی پشتیبانِ ابریت فقط ${faDigits(daysLeft.coerceAtLeast(1))} روزِ دیگه نگه داشته می‌شه. " +
                "اطلاعاتِ روی گوشیت دست نمی‌خوره، ولی اگه گوشی عوض کنی یا برنامه پاک بشه دیگه برنمی‌گرده. با تمدیدِ اشتراک حفظش کن.",
            "info", uid,
        )
        conn.execute("UPDATE users SET cloud_warn_for = ?, cloud_warn_stage = ? WHERE id = ?", warnFor, stage, uid)
    }

    private fun faDigits(n: Long): String = n.toString().map { "۰۱۲۳۴۵۶۷۸۹"[it - '0'] }.joinToString("")
}
