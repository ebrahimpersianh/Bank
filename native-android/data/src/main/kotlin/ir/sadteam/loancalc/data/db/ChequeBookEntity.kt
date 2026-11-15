package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** یه دسته‌چک (چک‌بوک) - محدوده‌ی شماره سریال [startSerial]..[endSerial]، و [nextSerial] که برای
 * پیشنهاد خودکار شماره‌ی چک بعدی موقع افزودن چک جدید از همین دسته استفاده می‌شه.
 *
 * [sayadId]/[last4]/[closedAt] طبقِ فریمِ `29k` (MIGRATION_28_29) اضافه شدن - هر سه nullable و
 * بدونِ defaultِ معنادار (نه رشته‌ی خالی) چون دسته‌چک‌های قدیمی هیچ‌کدوم رو ندارن؛ `closedAt`
 * غیرِنال یعنی دسته‌چک بسته شده (نمایشِ «بسته‌شده در فلان‌ماه» تو ۲۹k).
 */
@Entity(tableName = "cheque_books")
data class ChequeBookEntity(
    @PrimaryKey val id: Long,
    val ownerName: String,
    val bankName: String,
    val startSerial: Long,
    val endSerial: Long,
    val nextSerial: Long,
    val createdAt: String,
    val sayadId: String? = null,
    val last4: String? = null,
    val closedAt: String? = null,
)
