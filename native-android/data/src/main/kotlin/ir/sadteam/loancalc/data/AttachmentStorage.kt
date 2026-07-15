package ir.sadteam.loancalc.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

/**
 * پورت «پیوست عکس رسید» اپ رقیب (VAMMAN) - عکس انتخابی رو (از رو Uri موقتی گالری/Photo Picker) به
 * فضای داخلی اپ (`context.filesDir/attachments`) کپی می‌کنه و مسیر مطلق فایل داخلی رو برمی‌گردونه؛
 * چون Uri های گالری همیشه پایدار نیستن (به‌خصوص بعد از ری‌استارت گوشی یا پاک‌شدن کش)، نگه‌داشتن
 * خودِ فایل قابل‌اعتمادتره تا صرفاً ذخیره‌ی همون Uri.
 */
class AttachmentStorage(private val context: Context) {
    fun copyToInternalStorage(uri: Uri): String? {
        val dir = File(context.filesDir, "attachments").apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun delete(path: String?) {
        if (path != null) File(path).delete()
    }
}
