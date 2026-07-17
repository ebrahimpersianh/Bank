package ir.sadteam.loancalc.server

import java.io.File

/* بارگذاری ساده‌ی .env (خطوط KEY=VALUE)، معادل رفتار پکیج dotenv سمت Node که قبلاً استفاده می‌شد؛
   فقط اگه متغیر از قبل تو System env ست نشده باشه، مقدار .env رو به‌عنوان system property می‌ذاره. */
fun loadDotEnv(path: String = ".env") {
    val file = File(path)
    if (!file.exists()) return
    file.readLines().forEach { rawLine ->
        val line = rawLine.trim()
        if (line.isEmpty() || line.startsWith("#")) return@forEach
        val idx = line.indexOf('=')
        if (idx <= 0) return@forEach
        val key = line.substring(0, idx).trim()
        val value = line.substring(idx + 1).trim().trim('"', '\'')
        if (System.getProperty(key) == null) {
            System.setProperty(key, value)
        }
    }
}

/* رشته‌ی خالی (مثلاً DB_PATH= تو .env بدونِ مقدار) باید مثلِ نبودنِ متغیر رفتار کنه، نه یه مقدارِ
   خالیِ واقعی - وگرنه پیش‌فرض هیچ‌وقت اعمال نمی‌شه (باگی که واقعاً رخ داد: dbPath خالی موند،
   jdbc:sqlite: با مسیرِ خالی هر بار یه دیتابیسِ موقتِ تو-حافظه‌ی جدا می‌سازه). */
fun env(key: String, default: String = ""): String =
    System.getenv(key)?.takeIf { it.isNotBlank() }
        ?: System.getProperty(key)?.takeIf { it.isNotBlank() }
        ?: default
