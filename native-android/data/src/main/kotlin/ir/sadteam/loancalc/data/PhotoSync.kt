package ir.sadteam.loancalc.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import ir.sadteam.loancalc.data.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * همگام‌سازیِ عکس‌ها (رسید، چک، وام) با سرور - ۷ مهر، «همه‌چیز روی سرور».
 *
 * عکس‌ها در دو پوشه‌ی داخلیِ اپ‌اند (`attachments`، `receipts`) و رکوردها **نامِ فایل** را نگه
 * می‌دارند؛ پس همگام‌سازی بر پایه‌ی نام است: هرچه این‌جا هست و سرور ندارد بالا می‌رود (کوچک و
 * فشرده)، هرچه سرور دارد و این‌جا نیست پایین می‌آید. حذف همگام نمی‌شود (عکسِ اضافه ضرری ندارد).
 */
object PhotoSync {
    private val DIRS = listOf("attachments", "receipts")
    private val NAME = Regex("^[A-Za-z0-9._-]{1,100}$")
    private val api by lazy { ApiClient.create() }
    private val lock = Mutex()

    suspend fun sync(context: Context, token: String) = withContext(Dispatchers.IO) {
        if (!lock.tryLock()) return@withContext
        try {
            val auth = "Bearer $token"
            val remote = runCatching { api.listFiles(auth).files.orEmpty().toSet() }.getOrNull() ?: return@withContext
            for (dir in DIRS) {
                val folder = File(context.filesDir, dir).apply { mkdirs() }
                folder.listFiles()?.filter { it.isFile && NAME.matches(it.name) && "$dir/${it.name}" !in remote }?.forEach { f ->
                    val bytes = compress(f) ?: return@forEach
                    runCatching { api.putFile(auth, dir, f.name, bytes.toRequestBody("image/jpeg".toMediaType())) }
                }
                remote.filter { it.startsWith("$dir/") }.map { it.removePrefix("$dir/") }
                    .filter { NAME.matches(it) && !File(folder, it).exists() }
                    .forEach { name ->
                        runCatching {
                            val body = api.getFile(auth, dir, name).bytes()
                            val tmp = File(folder, "$name.part")
                            tmp.writeBytes(body)
                            tmp.renameTo(File(folder, name))
                        }
                    }
            }
        } finally {
            lock.unlock()
        }
    }

    /** بلندترین ضلع ≤ ۱۶۰۰ پیکسل و JPEG تا زیرِ ~۹۰۰ کیلوبایت - عکسِ رسید خوانا می‌ماند. */
    private fun compress(file: File): ByteArray? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 1600) sample *= 2
        val bmp = BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: return@runCatching file.readBytes().takeIf { it.size < 1_800_000 }
        var quality = 82
        var out: ByteArray
        do {
            val bos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, bos)
            out = bos.toByteArray()
            quality -= 12
        } while (out.size > 900_000 && quality > 30)
        bmp.recycle()
        out
    }.getOrNull()
}
