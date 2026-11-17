package ir.sadteam.loancalc.data

import android.content.Context
import android.database.sqlite.SQLiteFullException
import android.os.StatFs
import android.util.Log
import ir.sadteam.loancalc.data.db.AppDatabase
import java.io.File

/**
 * خطای «حافظه‌ی گوشی پر است» — بازبینیِ لایه‌ی داده (data.md §6) نشان داد هیچ‌جای
 * `native-android/` هیچ ارجاعی به `SQLiteFullException` / `SQLITE_FULL` نبود و همه‌ی
 * نوشتن‌های معمولیِ Room بدونِ هیچ محافظی مستقیم به صدازننده (ViewModel) پرتاب می‌شدند
 * و کرش می‌کردند.
 *
 * قراردادِ تازه:
 * - همه‌ی نوشتن‌های دیتابیس از [runDbWrite] (یا [inTransaction]‌های ریپازیتوری‌ها که
 *   همین‌جا نگاشت می‌شوند) می‌گذرند؛ `SQLiteFullException` به [StorageFullException]
 *   (یک استثنای دامنه‌ایِ تایپ‌شده، با پیامِ فارسیِ آماده) تبدیل می‌شود.
 * - لایه‌ی UI فقط [StorageFullException] را می‌گیرد و پیامِ [STORAGE_FULL_MESSAGE] را
 *   نشان می‌دهد؛ بقیه‌ی خطاها مثلِ قبل رفتار می‌کنند.
 * - [dbWriteResult] هم همان نگاشت را به‌صورتِ نتیجه‌ی تایپ‌شده ([DbWriteResult]) برمی‌گرداند،
 *   برای صدازننده‌هایی که نمی‌خواهند استثنا بگیرند (مثلاً Workerها).
 */
const val STORAGE_FULL_MESSAGE = "حافظه گوشی شما پر است"

/** استثنای دامنه‌ایِ «دیسک/دیتابیس پر است» — به‌جایِ `SQLiteFullException`ِ خام به UI می‌رسد. */
class StorageFullException(cause: Throwable? = null) :
    Exception(STORAGE_FULL_MESSAGE, cause)

/** `true` اگر این خطا (یا علتش) یعنی «دیسک/دیتابیس پر است». */
fun Throwable.isStorageFull(): Boolean {
    var t: Throwable? = this
    while (t != null) {
        if (t is StorageFullException || t is SQLiteFullException) return true
        // SQLCipher/Room گاهی همان خطا را به‌صورتِ SQLiteException با همین متن می‌دهد.
        val msg = t.message.orEmpty()
        if (msg.contains("database or disk is full", ignoreCase = true) ||
            msg.contains("database is full", ignoreCase = true) ||
            msg.contains("disk is full", ignoreCase = true)
        ) return true
        t = t.cause
    }
    return false
}

/**
 * پیامِ فارسیِ قابلِ نمایش برای خطای دیتابیس؛ فقط برای پر بودنِ حافظه غیر-null است،
 * برای بقیه‌ی خطاها `null` تا صدازننده پیامِ عمومیِ خودش را نشان بدهد.
 */
fun userMessageForDbError(t: Throwable): String? =
    if (t.isStorageFull()) STORAGE_FULL_MESSAGE else null

/**
 * یک بلوکِ نوشتنِ دیتابیس را اجرا می‌کند و پر بودنِ دیسک را به [StorageFullException] تبدیل می‌کند.
 *
 * عمداً `inline` است (بدونِ `crossinline`) تا بتوان کلِ بدنه‌ی یک تابعِ ریپازیتوری را با آن
 * پوشاند و `return`های داخلِ بدنه همچنان «برگشت از همان تابع» بمانند.
 */
suspend inline fun <T> runDbWrite(block: suspend () -> T): T =
    try {
        block()
    } catch (e: Throwable) {
        if (e.isStorageFull()) throw StorageFullException(e) else throw e
    }

/** همان [runDbWrite] برای بلوک‌های غیر-suspend (مثلاً نوشتنِ مستقیم با `SupportSQLiteDatabase`). */
fun <T> runDbWriteSync(block: () -> T): T =
    try {
        block()
    } catch (e: Throwable) {
        if (e.isStorageFull()) throw StorageFullException(e) else throw e
    }

/** نتیجه‌ی تایپ‌شده‌ی یک نوشتنِ دیتابیس — جایگزینِ پرتابِ استثنا برای صدازننده‌های پس‌زمینه. */
sealed interface DbWriteResult<out T> {
    data class Ok<T>(val value: T) : DbWriteResult<T>
    data object StorageFull : DbWriteResult<Nothing>
    data class Failed(val error: Throwable) : DbWriteResult<Nothing>
}

suspend fun <T> dbWriteResult(block: suspend () -> T): DbWriteResult<T> =
    try {
        DbWriteResult.Ok(block())
    } catch (e: Throwable) {
        if (e.isStorageFull()) DbWriteResult.StorageFull else DbWriteResult.Failed(e)
    }

/**
 * بررسیِ فضای خالیِ دیسک با [StatFs] — هشدارِ زودهنگامِ «حافظه رو به اتمام است»، پیش از
 * آن‌که نوشتنِ بعدی با `SQLITE_FULL` شکست بخورد.
 */
object StorageSpace {
    /** زیرِ این مقدار، حافظه «کم» حساب می‌شود و باید به کاربر هشدار داد (۱۰۰ مگابایت). */
    const val LOW_STORAGE_WARN_BYTES: Long = 100L * 1024 * 1024

    /** فضای خالیِ قابلِ نوشتن روی پارتیشنی که [dir] رویش است (بایت)؛ `-1` اگر خوانده نشد. */
    fun freeBytes(dir: File): Long =
        runCatching { StatFs(dir.path).availableBytes }.getOrDefault(-1L)

    fun freeBytes(context: Context): Long = freeBytes(context.filesDir)

    /** `true` اگر فضای خالی کمتر از [LOW_STORAGE_WARN_BYTES] است (یا اصلاً خوانده نشد). */
    fun isLow(dir: File, thresholdBytes: Long = LOW_STORAGE_WARN_BYTES): Boolean {
        val free = freeBytes(dir)
        return free < 0 || free < thresholdBytes
    }

    fun isLow(context: Context, thresholdBytes: Long = LOW_STORAGE_WARN_BYTES): Boolean =
        isLow(context.filesDir, thresholdBytes)
}

/**
 * پاک‌سازیِ فضای اشغال‌شده‌ی خودِ اپ — بدونِ دست‌زدن به داده‌ی کاربر (جدول‌های دیتابیس
 * هیچ‌وقت این‌جا پاک نمی‌شوند):
 * 1. همه‌ی `cacheDir` (کشِ Coil/تصویر/پشتیبانی — بازسازی‌شدنی است)؛
 * 2. فایل‌های `*.part`ِ نیمه‌دانلودشده‌ی [PhotoSync] در `attachments`/`receipts`؛
 * 3. عکس‌های یتیمِ `attachments`/`receipts` که هیچ رکوردِ دیتابیسی به آن‌ها اشاره نمی‌کند
 *    (مسیرها از ستون‌های `photoPath`/`receiptPath` خوانده می‌شوند؛ مسیرهای چندعکسیِ ردیفِ
 *    قسط با `|` جدا شده‌اند)؛
 * 4. فایل‌های لاگِ قدیمی‌تر از ۳۰ روز در `filesDir/logs` (اگر وجود داشته باشد).
 *
 * ⚠️ فقط فایل‌هایی که **داخلِ** پوشه‌های خودِ اپ‌اند حذف می‌شوند؛ مسیرِ خارج از آن‌ها
 * (مثلاً مسیرِ گالری که کاربر انتخاب کرده) هرگز پاک نمی‌شود.
 */
object StorageCleanup {
    data class Report(
        val cacheFilesDeleted: Int = 0,
        val partFilesDeleted: Int = 0,
        val orphanFilesDeleted: Int = 0,
        val oldLogFilesDeleted: Int = 0,
    ) {
        val totalDeleted: Int get() = cacheFilesDeleted + partFilesDeleted + orphanFilesDeleted + oldLogFilesDeleted
    }

    private val LOG_MAX_AGE_MS = 30L * 24 * 60 * 60 * 1000

    /**
     * پاک‌سازیِ کامل؛ [knownPaths] را صدازننده می‌تواند بدهد، وگرنه از خودِ دیتابیس
     * خوانده می‌شود. روی `Dispatchers.IO` صدا زده شود (I/Oی فایل + کوئری دارد).
     */
    suspend fun cleanup(context: Context, database: AppDatabase? = null, knownPaths: Set<String>? = null): Report {
        var cache = 0
        var part = 0
        var orphan = 0
        var logs = 0
        runCatching { cache = deleteRecursively(context.cacheDir) }
        for (dirName in listOf("attachments", "receipts")) {
            val dir = File(context.filesDir, dirName)
            if (!dir.isDirectory) continue
            // اگر مسیرهای شناخته‌شده را نتوان خواند (دیتابیس در دسترس نیست)، `known`
            // خالی نیست بلکه نامعلوم است — در آن حالت هیچ فایلی یتیم حساب نمی‌شود و فقط
            // `*.part`ها پاک می‌شوند (fail-closed).
            val known: Set<String>? = knownPaths ?: database?.let { collectKnownPaths(it) }
            dir.listFiles()?.forEach { file ->
                if (!file.isFile) return@forEach
                when {
                    file.name.endsWith(".part") -> if (file.delete()) part++
                    known != null && file.absolutePath !in known && file.name !in known -> if (file.delete()) orphan++
                }
            }
        }
        val logsDir = File(context.filesDir, "logs")
        if (logsDir.isDirectory) {
            val cutoff = System.currentTimeMillis() - LOG_MAX_AGE_MS
            logsDir.listFiles()?.forEach { file ->
                if (file.isFile && file.lastModified() < cutoff && file.delete()) logs++
            }
        }
        Log.i("StorageCleanup", "cleanup: cache=$cache part=$part orphan=$orphan oldLogs=$logs")
        return Report(cache, part, orphan, logs)
    }

    /** فقط فایل‌های `*.part` و یتیم‌ها را پاک می‌کند (بدونِ دست‌زدن به کش) — برای صدا زدنِ مکرر. */
    suspend fun purgeStaleAttachments(context: Context, database: AppDatabase? = null): Report {
        var part = 0
        var orphan = 0
        for (dirName in listOf("attachments", "receipts")) {
            val dir = File(context.filesDir, dirName)
            if (!dir.isDirectory) continue
            val known: Set<String>? = database?.let { collectKnownPaths(it) }
            dir.listFiles()?.forEach { file ->
                if (!file.isFile) return@forEach
                when {
                    file.name.endsWith(".part") -> if (file.delete()) part++
                    known != null && file.absolutePath !in known && file.name !in known -> if (file.delete()) orphan++
                }
            }
        }
        return Report(partFilesDeleted = part, orphanFilesDeleted = orphan)
    }

    private fun deleteRecursively(dir: File?): Int {
        if (dir == null || !dir.exists()) return 0
        var count = 0
        dir.listFiles()?.forEach { child ->
            count += if (child.isDirectory) deleteRecursively(child) else if (child.delete()) 1 else 0
        }
        return count
    }

    /**
     * همه‌ی مسیرهای فایلِ ضمیمه/رسید که هنوز رکوردِ دیتابیسی دارند. اگر دیتابیس در دسترس
     * نباشد، مجموعه‌ی خالی برمی‌گردد — یعنی در آن حالت هیچ فایلی «یتیم» حساب نمی‌شود و
     * چیزی پاک نمی‌شود (fail-closed: پاک‌نکردن امن‌تر از پاک‌کردنِ اشتباه است).
     */
    fun collectKnownPaths(database: AppDatabase?): Set<String> {
        val db = database?.openHelper?.readableDatabase ?: return emptySet()
        val known = HashSet<String>()
        // (جدول، ستون)هایی که مسیرِ فایل نگه می‌دارند.
        val columns = listOf(
            "loans" to "photoPath",
            "loan_rows" to "photoPath",
            "cheques" to "photoPath",
            "notes" to "photoPath",
            "account_transactions" to "receiptPath",
        )
        for ((table, column) in columns) {
            runCatching {
                db.query("SELECT `$column` FROM `$table` WHERE `$column` IS NOT NULL").use { c ->
                    while (c.moveToNext()) {
                        c.getString(0)?.split('|')?.forEach { p ->
                            val path = p.trim()
                            if (path.isNotEmpty()) {
                                known += path
                                known += File(path).name
                            }
                        }
                    }
                }
            }
        }
        return known
    }
}
