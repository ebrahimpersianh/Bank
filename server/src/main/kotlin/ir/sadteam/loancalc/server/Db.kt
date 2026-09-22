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
                        subscription_tier TEXT,
                        session_version INTEGER NOT NULL DEFAULT 0,
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
                        revision INTEGER NOT NULL DEFAULT 0,
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
                        revision INTEGER NOT NULL DEFAULT 0,
                        updated_at TEXT NOT NULL DEFAULT (datetime('now'))
                    )
                    """.trimIndent()
                )
                st.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS accounts_backup (
                        user_id INTEGER PRIMARY KEY REFERENCES users(id),
                        data TEXT NOT NULL DEFAULT '{}',
                        revision INTEGER NOT NULL DEFAULT 0,
                        updated_at TEXT NOT NULL DEFAULT (datetime('now'))
                    )
                    """.trimIndent()
                )
                // 🐞 **گزارشِ مشکل** - رجوع کن به `routes/SupportRoutes.kt`.
                // `user_id` مهم‌ترین ستون است: کدِ هدیه به همان حساب داده می‌شود.
                st.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS bug_reports (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        ticket TEXT UNIQUE NOT NULL,
                        user_id INTEGER REFERENCES users(id),
                        phone TEXT,
                        message TEXT NOT NULL,
                        app_version TEXT,
                        device TEXT,
                        status TEXT NOT NULL DEFAULT 'open',
                        created_at TEXT NOT NULL DEFAULT (datetime('now'))
                    )
                    """.trimIndent()
                )

                // 🎁 **کدهای هدیه‌ی اشتراک** - خواسته‌ی کاربر (۳۱ شهریور): جایزه‌ی
                // ۱ تا ۱۵ روزه، و هدیه به کسی که باگ گزارش می‌کند.
                //
                // 🚨 **چرا کد و نه یک دکمه‌ی «جایزه بگیر» در اپ**: موجودیِ سکه و رخدادهای
                // اپ همگی **روی خودِ گوشی**اند و سرور نمی‌تواند راستی‌آزمایی‌شان کند؛ یک
                // اندپوینتِ «به من N روز اشتراک بده» یعنی هرکسی با یک درخواستِ ساده
                // اشتراکِ نامحدود می‌گیرد. کد را **سرور** می‌سازد، پس مرجعِ حقیقت سرور
                // می‌مانَد و هر کد دقیقاً یک بار مصرف می‌شود.
                st.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS gift_codes (
                        code TEXT PRIMARY KEY,
                        days INTEGER NOT NULL,
                        note TEXT,
                        used_by INTEGER REFERENCES users(id),
                        used_at TEXT,
                        expires_at TEXT,
                        created_at TEXT NOT NULL DEFAULT (datetime('now'))
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

                // تاریخچه‌ی خریدِ اشتراک - تا قبل از این فقط وضعیتِ *فعلی* رو users نگه داشته می‌شد
                // (subscribed_until + subscription_tier) و کاربر هیچ‌جا نمی‌تونست ببینه کِی چی خریده.
                // هر ردیف یه خریدِ تاییدشده‌ست؛ purchase_token یکتاست تا اگه کلاینت یه خرید رو دوباره
                // بفرسته (مثلاً restorePurchases) ردیفِ تکراری ساخته نشه.
                st.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS subscription_purchases (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        product_id TEXT NOT NULL,
                        tier TEXT,
                        store TEXT NOT NULL,
                        purchase_token TEXT NOT NULL UNIQUE,
                        duration_days INTEGER NOT NULL,
                        subscribed_until TEXT NOT NULL,
                        created_at TEXT NOT NULL DEFAULT (datetime('now'))
                    )
                    """.trimIndent()
                )
                st.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_subscription_purchases_user ON subscription_purchases(user_id)"
                )

                // کشِ قیمتِ طلا/ارز/سکه (منبع: نوسان) - ردیفِ تکی (id=1) مثلِ app_version.
                // چرا اینجا و نه هاردکد تو اپ: کلیدِ API رو گیت‌هاب Secret می‌مونه، لو نمی‌ره؛
                // و سهمیه‌ی وب‌سرویس (۱۲۰ درخواست در ماه) با تازه‌سازیِ کم‌فاصله رعایت می‌شه -
                // رجوع کن به PriceService.kt.
                st.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS price_snapshot (
                        id INTEGER PRIMARY KEY CHECK (id = 1),
                        raw_json TEXT NOT NULL,
                        fetched_at TEXT NOT NULL
                    )
                    """.trimIndent()
                )

                // تاریخچه‌ی روزانه‌ی قیمت - یه ردیف در روز برای هر نماد، از رو همون fetchهای
                // ساعتی ساخته می‌شه (سهمیه‌ی اضافه نمی‌خواد). مبنای «نسبت به ماهِ قبل» تو تبِ
                // دارایی. کلیدِ مرکب یعنی fetchهای بعدیِ همون روز به‌روزرسانی می‌کنن نه تکرار.
                st.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS price_history (
                        symbol TEXT NOT NULL,
                        date TEXT NOT NULL,
                        price REAL NOT NULL,
                        PRIMARY KEY (symbol, date)
                    )
                    """.trimIndent()
                )
            }
            /* migration برای دیتابیس‌های قدیمی که از قبل جدول users رو بدون این ستون‌ها دارن.
               ⚠️ **فقط خطای «ستون از قبل هست» بخشیده می‌شود**؛ هر خطای دیگری بالا می‌رود تا
               دیتابیسِ خراب/ناسازگار بی‌صدا رد نشود (یافته‌ی بازبینی، ۳۱ شهریور). */
            addColumnIfMissing(conn, "ALTER TABLE users ADD COLUMN subscribed INTEGER NOT NULL DEFAULT 0")
            addColumnIfMissing(conn, "ALTER TABLE users ADD COLUMN subscribed_until TEXT")
            // پلنِ خریداری‌شده ("1m"/"3m"/"6m"/"1y") - قبلاً اصلاً ذخیره نمی‌شد، فقط تاریخِ انقضا؛
            // برای نمایشِ دقیقِ نوعِ اشتراک تو تنظیمات لازم شد - رجوع کن به SubscriptionRoutes.kt.
            addColumnIfMissing(conn, "ALTER TABLE users ADD COLUMN subscription_tier TEXT")
            // نامِ اختیاریِ کاربر - فقط برای سربرگِ خروجیِ PDF/اکسل. هیچ‌وقت اجباری نیست.
            addColumnIfMissing(conn, "ALTER TABLE users ADD COLUMN name TEXT")
            // هدیه‌ی «کاربرِ قدیمی» - رجوع کن به grantLegacyGift پایین‌تر.
            addColumnIfMissing(conn, "ALTER TABLE users ADD COLUMN legacy_gift_granted INTEGER NOT NULL DEFAULT 0")
            // نسخه‌ی نشست - رجوع کن به Auth.kt. بالا رفتنش یعنی «همه‌ی توکن‌های قبلی باطل».
            addColumnIfMissing(conn, "ALTER TABLE users ADD COLUMN session_version INTEGER NOT NULL DEFAULT 0")
            // شماره‌ی نسخه‌ی هر اسنپ‌شاتِ ابری - پایه‌ی کنترلِ هم‌زمانی (رجوع کن به BackupRoutes).
            // کلاینتِ کهنه که `expectedRevision` نمی‌فرستد، رفتارِ قبلی را می‌گیرد.
            addColumnIfMissing(conn, "ALTER TABLE loans ADD COLUMN revision INTEGER NOT NULL DEFAULT 0")
            addColumnIfMissing(conn, "ALTER TABLE cheques_backup ADD COLUMN revision INTEGER NOT NULL DEFAULT 0")
            addColumnIfMissing(conn, "ALTER TABLE accounts_backup ADD COLUMN revision INTEGER NOT NULL DEFAULT 0")
            grantLegacyGift(conn)
        }
    }

    /* هر فراخوانی یه کانکشن جدا باز/بسته می‌کنه (به‌جای یه کانکشن مشترک سراسری مثل better-sqlite3)
       چون Ktor/Netty چندریسمانیه و JDBC Connection thread-safe نیست؛ با WAL + busy_timeout
       چندین کانکشن هم‌زمان رو یه فایل SQLite بدون قفل‌شدن کار می‌کنن. */
    /**
     * هدیه‌ی یک‌بارمصرفِ کاربرانِ قدیمی: **۱۵ روزِ اضافه** (روی ۳۰ روزِ پایه، جمعاً ۴۵) برای هر
     * شماره‌ای که *در لحظه‌ی اجرای این مهاجرت* از قبل تو دیتابیس بوده.
     *
     * خواسته‌ی صریحِ کاربر (۲۰ مرداد). عمداً یه مهاجرتِ خودکاره نه یه دستورِ دستیِ SSH - چون
     * `sqlite3` رو VPS نصب نیست و اجرای دستیِ SQL رو سرورِ زنده هنوز تاییدنشده‌ست (CLAUDE.md).
     *
     * ستونِ `legacy_gift_granted` ضامنِ ضدِتکراره: هر ردیف حداکثر یک بار هدیه می‌گیره، پس
     * ری‌استارت‌های بعدیِ سرور دوباره تمدیدش نمی‌کنن. کاربرانی که بعد از این نقطه ثبت‌نام کنن،
     * چون ستونشون از همون اول ۱ ست می‌شه، فقط ۳۰ روزِ پایه رو می‌گیرن.
     */
    /**
     * `ALTER TABLE ... ADD COLUMN` را اجرا می‌کند و **تنها** خطای «این ستون از قبل هست» را
     * می‌بخشد. تفاوتِ «مهاجرت لازم نبود» با «مهاجرت شکست خورد» باید دیده شود.
     */
    private fun addColumnIfMissing(conn: Connection, sql: String) {
        try {
            conn.createStatement().use { it.executeUpdate(sql) }
        } catch (e: java.sql.SQLException) {
            val message = e.message.orEmpty().lowercase()
            if (!message.contains("duplicate column")) throw e
        }
    }

    private fun grantLegacyGift(conn: Connection) {
        runCatching {
            // ۴۵ روز از الان: ۳۰ روزِ پایه + ۱۵ روزِ هدیه‌ی قدیمی‌بودن.
            val until = java.time.Instant.now().plusSeconds(45L * 24 * 60 * 60).toString()
            conn.prepareStatement(
                """
                UPDATE users
                SET subscribed_until = ?, legacy_gift_granted = 1
                WHERE legacy_gift_granted = 0
                  AND (subscribed_until IS NULL OR subscribed_until < ?)
                """.trimIndent(),
            ).use { ps ->
                ps.setString(1, until)
                ps.setString(2, until)
                ps.executeUpdate()
            }
            // هر کسی که از قبل اشتراکِ طولانی‌تری داشته، فقط علامت می‌خوره تا دوباره بررسی نشه -
            // اشتراکش عمداً کوتاه نمی‌شه.
            conn.createStatement().use { it.executeUpdate("UPDATE users SET legacy_gift_granted = 1 WHERE legacy_gift_granted = 0") }
        }
    }

    fun <T> withConnection(block: (Connection) -> T): T {
        DriverManager.getConnection("jdbc:sqlite:$dbPath").use { conn ->
            conn.createStatement().use { it.executeUpdate("PRAGMA journal_mode=WAL") }
            conn.createStatement().use { it.executeUpdate("PRAGMA busy_timeout=5000") }
            // 🚨 **کلیدهای خارجی در SQLite پیش‌فرض خاموش‌اند و هر کانکشن جداگانه باید
            // روشنشان کند.** بی این خط، `REFERENCES users(id)`ِ جدول‌ها فقط یک جمله‌ی
            // تزئینی بود: حذفِ کاربر می‌توانست ردیف‌های پشتیبانِ یتیم به‌جا بگذارد.
            conn.createStatement().use { it.executeUpdate("PRAGMA foreign_keys=ON") }
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
    val subscriptionTier: String?,
    /** نامِ اختیاریِ کاربر (می‌تونه null باشه) - رجوع کن به مسیرِ PUT /api/auth/name. */
    val name: String?,
    /** آیا این کاربر جزو «کاربرانِ قدیمی» بود که ۱۵ روزِ هدیه‌ی اضافه گرفت؟ اپ ازش برای نشون‌دادنِ
     * جمله‌ی «چون از قبل وارد برنامه شده بودی...» استفاده می‌کنه - رجوع کن به grantLegacyGift. */
    val legacyGift: Boolean,
    val createdAt: String
)

fun ResultSet.toUserRow(): UserRow = UserRow(
    id = getLong("id"),
    phone = getString("phone"),
    subscribed = getInt("subscribed") != 0,
    subscribedUntil = getString("subscribed_until"),
    subscriptionTier = getString("subscription_tier"),
    name = runCatching { getString("name") }.getOrNull(),
    // runCatching چون دیتابیس‌های خیلی قدیمی ممکنه هنوز این ستون رو نداشته باشن (قبل از migration).
    legacyGift = runCatching { getInt("legacy_gift_granted") != 0 }.getOrDefault(false),
    createdAt = getString("created_at")
)
