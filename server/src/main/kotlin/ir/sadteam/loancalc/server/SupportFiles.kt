package ir.sadteam.loancalc.server

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.SecureRandom
import javax.imageio.ImageIO

/**
 * 🔒 پیوستِ پیامِ پشتیبانی - **فقط عکس و فیلم** (خواسته‌ی صریحِ کاربر: «فایل نفرستن هک کنن»).
 *
 * لایه‌های دفاع:
 *  1. نوع از **بایت‌های اولِ فایل** تشخیص داده می‌شود، نه از نام یا Content-Type که کاربر می‌فرستد.
 *  2. عکس **دوباره ساخته می‌شود** (decode → encode به JPEG): هر چیزِ پنهان در فایل (اسکریپت،
 *     متادیتا، مکانِ GPS، polyglot) دور ریخته می‌شود و فقط پیکسل‌ها می‌مانند.
 *  3. فیلم فقط MP4/3GP (جعبه‌ی `ftyp`) و سقفِ حجم؛ هرگز اجرا یا پردازش نمی‌شود.
 *  4. نامِ فایل روی دیسک را سرور می‌سازد (۳۲ رقمِ تصادفی، بی پسوند) - مسیرِ ورودی از کاربر نیست.
 *  5. پوشه خارج از هر مسیرِ وب است و فقط از راهِ endpointِ ادمین با `nosniff` خوانده می‌شود.
 */
object SupportFiles {
    const val MAX_IMAGE_BYTES = 8L * 1024 * 1024
    const val MAX_VIDEO_BYTES = 20L * 1024 * 1024
    private const val MAX_PIXELS = 40_000_000L
    private const val MAX_SIDE = 2000

    fun dir(): File = File(env("SUPPORT_FILES_DIR", System.getProperty("user.home") + "/VameMan/support-files")).apply { mkdirs() }

    private val ID = Regex("^[0-9a-f]{32}$")
    fun fileFor(id: String): File? = if (ID.matches(id)) File(dir(), id) else null

    fun newId(): String {
        val b = ByteArray(16).also { SecureRandom().nextBytes(it) }
        return b.joinToString("") { "%02x".format(it) }
    }

    sealed interface Result {
        data class Ok(val kind: String, val mime: String, val bytes: ByteArray) : Result
        data class Rejected(val reason: String) : Result
    }

    private fun startsWith(b: ByteArray, vararg sig: Int, offset: Int = 0) =
        b.size >= offset + sig.size && sig.indices.all { (b[offset + it].toInt() and 0xFF) == sig[it] }

    fun sanitize(raw: ByteArray): Result {
        if (raw.isEmpty()) return Result.Rejected("empty")
        val isJpeg = startsWith(raw, 0xFF, 0xD8, 0xFF)
        val isPng = startsWith(raw, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
        val isMp4 = startsWith(raw, 0x66, 0x74, 0x79, 0x70, offset = 4) // "ftyp"
        return when {
            isJpeg || isPng -> {
                if (raw.size > MAX_IMAGE_BYTES) return Result.Rejected("too_large")
                reencode(raw)?.let { Result.Ok("image", "image/jpeg", it) } ?: Result.Rejected("bad_image")
            }
            isMp4 -> {
                if (raw.size > MAX_VIDEO_BYTES) return Result.Rejected("too_large")
                val brand = String(raw, 8, 4, Charsets.US_ASCII)
                if (brand.any { it.code !in 0x20..0x7E }) return Result.Rejected("bad_video")
                Result.Ok("video", "video/mp4", raw)
            }
            else -> Result.Rejected("unsupported_type")
        }
    }

    private fun reencode(raw: ByteArray): ByteArray? = runCatching {
        // اندازه را قبل از decodeِ کامل می‌خوانیم تا «بمبِ پیکسلی» حافظه را پر نکند.
        ImageIO.createImageInputStream(ByteArrayInputStream(raw)).use { iis ->
            val reader = ImageIO.getImageReaders(iis).asSequence().firstOrNull() ?: return null
            try {
                reader.input = iis
                val w = reader.getWidth(0).toLong()
                val h = reader.getHeight(0).toLong()
                if (w <= 0 || h <= 0 || w * h > MAX_PIXELS) return null
                val src = reader.read(0)
                val scale = minOf(1.0, MAX_SIDE.toDouble() / maxOf(src.width, src.height))
                val tw = (src.width * scale).toInt().coerceAtLeast(1)
                val th = (src.height * scale).toInt().coerceAtLeast(1)
                val out = BufferedImage(tw, th, BufferedImage.TYPE_INT_RGB)
                val g = out.createGraphics()
                g.color = java.awt.Color.WHITE
                g.fillRect(0, 0, tw, th)
                g.drawImage(src, 0, 0, tw, th, null)
                g.dispose()
                val bos = ByteArrayOutputStream()
                if (!ImageIO.write(out, "jpg", bos)) return null
                bos.toByteArray()
            } finally {
                reader.dispose()
            }
        }
    }.getOrNull()
}
