package ir.sadteam.loancalc.server

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

object Db {
    private val dbPath: String = env("DB_PATH", "data.sqlite")

    init {
        Class.forName("org.sqlite.JDBC")
        withConnection { conn ->
            conn.createStatement().use { st ->
                st.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        phone TEXT UNIQUE NOT NULL,
                        subscribed INTEGER NOT NULL DEFAULT 0,
                        subscribed_until TEXT,
                        created_at TEXT NOT NULL DEFAULT (datetime('now'))
                    )
                    """.trimIndent()
                )
                st.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS otps (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        phone TEXT NOT NULL,
                        code_hash TEXT NOT NULL,
                        expires_at INTEGER NOT NULL,
                        attempts INTEGER NOT NULL DEFAULT 0,
                        created_at TEXT NOT NULL DEFAULT (datetime('now'))
                    )
                    """.trimIndent()
                )
                st.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS loans (
                        user_id INTEGER PRIMARY KEY REFERENCES users(id),
                        data TEXT NOT NULL DEFAULT '[]',
                        updated_at TEXT NOT NULL DEFAULT (datetime('now'))
                    )
                    """.trimIndent()
                )
                st.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS crash_reports (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        message TEXT NOT NULL,
                        stack TEXT,
                        context TEXT,
                        app_version TEXT,
                        created_at TEXT NOT NULL DEFAULT (datetime('now'))
                    )
                    """.trimIndent()
                )
            }
            /* migration برای دیتابیس‌های قدیمی که از قبل جدول users رو بدون این ستون‌ها دارن */
            runCatching { conn.createStatement().use { it.executeUpdate("ALTER TABLE users ADD COLUMN subscribed INTEGER NOT NULL DEFAULT 0") } }
            runCatching { conn.createStatement().use { it.executeUpdate("ALTER TABLE users ADD COLUMN subscribed_until TEXT") } }
        }
    }

    /* هر فراخوانی یه کانکشن جدا باز/بسته می‌کنه (به‌جای یه کانکشن مشترک سراسری مثل better-sqlite3)
       چون Ktor/Netty چندریسمانیه و JDBC Connection thread-safe نیست؛ با WAL + busy_timeout
       چندین کانکشن هم‌زمان رو یه فایل SQLite بدون قفل‌شدن کار می‌کنن. */
    fun <T> withConnection(block: (Connection) -> T): T {
        DriverManager.getConnection("jdbc:sqlite:$dbPath").use { conn ->
            conn.createStatement().use { it.executeUpdate("PRAGMA journal_mode=WAL") }
            conn.createStatement().use { it.executeUpdate("PRAGMA busy_timeout=5000") }
            return block(conn)
        }
    }
}

data class UserRow(
    val id: Long,
    val phone: String,
    val subscribed: Boolean,
    val subscribedUntil: String?,
    val createdAt: String
)

fun ResultSet.toUserRow(): UserRow = UserRow(
    id = getLong("id"),
    phone = getString("phone"),
    subscribed = getInt("subscribed") != 0,
    subscribedUntil = getString("subscribed_until"),
    createdAt = getString("created_at")
)
