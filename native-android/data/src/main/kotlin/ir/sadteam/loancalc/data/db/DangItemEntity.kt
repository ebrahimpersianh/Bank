package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * یه قلمِ فاکتور - فقط برای روشِ `ITEMIZED` (قلم‌به‌قلم) استفاده می‌شه؛ روش‌های دیگه هیچ ردیفی
 * تو این جدول نمی‌سازن. ⚠️ جوابِ سوالِ ۷ی MESSAGE-round4 صریحاً گفته این روش پیچیده‌تره چون هر
 * قلم به چند نفر نسبت داده می‌شه - رجوع کن به [DangItemShareEntity].
 */
@Entity(tableName = "dang_items", indices = [Index("eventId")])
data class DangItemEntity(
    @PrimaryKey val id: Long,
    val eventId: Long,
    val description: String,
    val amount: Double,
)

/** سهمِ یه شرکت‌کننده از یه قلمِ خاص - جدولِ اتصالِ many-to-many بینِ [DangItemEntity] و
 * [DangParticipantEntity]، چون هر قلم می‌تونه به چند نفر و هر نفر می‌تونه از چند قلم سهم داشته باشه. */
@Entity(
    tableName = "dang_item_shares",
    indices = [Index("itemId"), Index("participantId")],
)
data class DangItemShareEntity(
    @PrimaryKey val id: Long,
    val itemId: Long,
    val participantId: Long,
    val shareAmount: Double,
)
