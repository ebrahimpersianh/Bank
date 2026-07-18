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
                // پشتیبان‌گیری ابری چک‌ها و حساب‌ها (اپ بومی): برخلاف loans که یه آرایه‌ی JSON واقعی
                // نگه می‌داره (چون کلاینت وب هم مستقیم مصرفش می‌کنه)، این دوتا فقط یه blob مات از
                // همون JSON ای هستن که ChequeRepository/AccountRepository.exportBackupJson تو اپ
                // بومی تولید می‌کنه - سرور به شکل داخلیش کاری نداره، فقط ذخیره/برمی‌گردونه.
                st.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS cheques_backup (
                        user_id INTEGER PRIMARY KEY REFERENCES users(id),
                        data TEXT NOT NULL DEFAULT '{}',
                        updated_at TEXT NOT NULL DEFAULT (datetime('now'))
                    )
                    """.trimIndent()
                )
                st.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS accounts_backup (
                        user_id INTEGER PRIMARY KEY REFERENCES users(id),
                        data TEXT NOT NULL DEFAULT '{}',
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
                // نرخِ خدمات اعتباری (دیجی‌پی، اسنپ‌پی و ...) قبلاً فقط تو کدِ اپ بومی hardcode بود؛
                // هر بار یکی از این سرویس‌ها نرخش رو عوض می‌کرد، باید یه نسخه‌ی جدیدِ اپ منتشر می‌شد.
                // حالا این جدول منبع حقیقته - رجوع کن به routes/CreditRatesRoutes.kt. seed اولیه‌ش
                // دقیقاً همون مقادیریه که قبلاً تو data/Banks.kt هاردکد بود.
                st.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS credit_rates (
                        key TEXT PRIMARY KEY,
                        name TEXT NOT NULL,
                        color_hex TEXT NOT NULL,
                        logo_asset TEXT NOT NULL,
                        rate_pct REAL NOT NULL,
                        months INTEGER NOT NULL,
                        min_amount INTEGER NOT NULL,
                        max_amount INTEGER NOT NULL,
                        sort_order INTEGER NOT NULL,
                        updated_at TEXT NOT NULL DEFAULT (datetime('now'))
                    )
                    """.trimIndent()
                )
                val creditRatesCount = st.executeQuery("SELECT COUNT(*) AS c FROM credit_rates").use { rs ->
                    rs.next(); rs.getInt("c")
                }
                if (creditRatesCount == 0) {
                    seedDefaultCreditRates(conn)
                }

                // آپدیتِ خودکار: چون نه کافه‌بازار نه مایکت API خودکارِ «نسخه‌ی جدید منتشر شد یا نه»
                // ندارن، خودِ سرور منبعِ حقیقتِ «آخرین نسخه»ست - رجوع کن به routes/AppVersionRoutes.kt.
                // یه ردیفِ تکی (id=1)؛ بعدِ هر انتشارِ واقعیِ نسخه‌ی جدید رو کافه‌بازار/مایکت، باید
                // دستی آپدیت بشه (رجوع کن به کامنتِ AppVersionRoutes.kt برای دستورِ SQL دقیق).
                st.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS app_version (
                        id INTEGER PRIMARY KEY CHECK (id = 1),
                        latest_version_code INTEGER NOT NULL,
                        cafebazaar_url TEXT,
                        myket_url TEXT,
                        updated_at TEXT NOT NULL DEFAULT (datetime('now'))
                    )
                    """.trimIndent()
                )
                st.executeUpdate(
                    "INSERT INTO app_version (id, latest_version_code) VALUES (1, 1) ON CONFLICT(id) DO NOTHING"
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

private data class DefaultCreditRate(
    val key: String,
    val name: String,
    val colorHex: String,
    val logoAsset: String,
    val ratePct: Double,
    val months: Int,
    val minAmount: Long,
    val maxAmount: Long,
)

// عیناً همون شش‌تا مقداری که قبلاً تو native-android/app/.../data/Banks.kt (creditServices)
// هاردکد بود - از این به بعد فقط seed اولیه‌ست، منبع حقیقت همین جدوله.
private val defaultCreditRates = listOf(
    DefaultCreditRate("digipay", "دیجی‌پی (خرید اقساطی)", "#E53935", "services/digipay.png", 23.0, 12, 100_000_000, 500_000_000),
    DefaultCreditRate("snapp_pay", "اسنپ‌پی (اعتبار بانکی)", "#43A047", "services/snapp-pay.jpg", 22.0, 24, 50_000_000, 1_000_000_000),
    DefaultCreditRate("snapp_pay_4", "اسنپ‌پی (۴ قسط بدون سود)", "#66BB6A", "services/snapp-pay-4.jpg", 0.0, 4, 10_000_000, 100_000_000),
    DefaultCreditRate("up", "آپ (Up)", "#8E24AA", "services/up.png", 24.0, 12, 50_000_000, 500_000_000),
    DefaultCreditRate("azki", "ازکی وام", "#FB8C00", "services/azki.jpg", 23.0, 18, 50_000_000, 700_000_000),
    DefaultCreditRate("vipad", "ویپاد", "#00ACC1", "services/vipad.jpg", 24.0, 12, 50_000_000, 500_000_000),
)

private fun seedDefaultCreditRates(conn: Connection) {
    defaultCreditRates.forEachIndexed { index, rate ->
        conn.execute(
            """
            INSERT INTO credit_rates (key, name, color_hex, logo_asset, rate_pct, months, min_amount, max_amount, sort_order)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(key) DO NOTHING
            """.trimIndent(),
            rate.key, rate.name, rate.colorHex, rate.logoAsset, rate.ratePct, rate.months, rate.minAmount, rate.maxAmount, index,
        )
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
