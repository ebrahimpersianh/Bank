package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * یه رویدادِ «دنگ» (تقسیمِ یه هزینه بینِ چند نفر) - فریمِ `22c`.
 *
 * [method] رشته‌ی `name` اینامِ `DangMethod` (تو `:core`) - مساوی/درصدی/دلخواه/قلم‌به‌قلم
 * (جوابِ سوالِ ۷ی MESSAGE-round4: هر ۴ روش از نسخه‌ی اول).
 * [isEventMode] یعنی «حالتِ مهمانی/مناسبت» (جوابِ سوالِ ۸): پاکتِ موقت تا [settled]، و از میانگینِ
 * ماهانه‌ی گزارش حذف می‌شه - این حذف تو محاسبه‌ی گزارش با همین یه پرچم انجام می‌شه، ستونِ جدا لازم نبود.
 */
@Entity(tableName = "dang_events")
data class DangEventEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val method: String,
    val totalAmount: Double,
    val year: Int,
    val month: Int,
    val day: Int,
    val isEventMode: Boolean = false,
    val settled: Boolean = false,
    val createdAt: String,
)

/**
 * سهمِ یه نفر از یه [DangEventEntity]. [counterpartyId] نال یعنی «خودم» (صاحبِ اپ) - همیشه یه
 * ردیف برای «خودم» هم ساخته می‌شه تا جمعِ سهم‌ها با [DangEventEntity.totalAmount] برابر بمونه.
 * [percentage] فقط برای روشِ `PERCENTAGE` پر می‌شه؛ بقیه‌ی روش‌ها مستقیم [shareAmount] رو حساب
 * می‌کنن و این فیلد null می‌مونه.
 */
@Entity(
    tableName = "dang_participants",
    indices = [Index("eventId"), Index("counterpartyId")],
)
data class DangParticipantEntity(
    @PrimaryKey val id: Long,
    val eventId: Long,
    val counterpartyId: Long? = null,
    val shareAmount: Double,
    val percentage: Double? = null,
    val settled: Boolean = false,
)
