package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** یه دسته‌چک (چک‌بوک) - محدوده‌ی شماره سریال [startSerial]..[endSerial]، و [nextSerial] که برای
 * پیشنهاد خودکار شماره‌ی چک بعدی موقع افزودن چک جدید از همین دسته استفاده می‌شه. */
@Entity(tableName = "cheque_books")
data class ChequeBookEntity(
    @PrimaryKey val id: Long,
    val ownerName: String,
    val bankName: String,
    val startSerial: Long,
    val endSerial: Long,
    val nextSerial: Long,
    val createdAt: String,
)
