package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * یه «طرفِ‌حساب» برای ماژولِ طلب‌وبدهی - فقط یه شخص/نهاده، مانده‌ی طلب/بدهیش از رو جمعِ
 * [DebtEntity] های مربوطه محاسبه می‌شه (ذخیره نمی‌شه)، هم‌الگو با AccountEntity.currentBalance.
 *
 * [phone]/[avatarColor]/[avatarShape] طبقِ فریمِ `22b` (MIGRATION_28_29) اضافه شدن. مقادیرِ
 * [avatarColor]/[avatarShape] رشته‌ی `name` اینام‌های `AvatarColor`/`AvatarShape` (تو
 * `ui/components/Avatar.kt`) هستن - این ماژول (`:data`) به `:app` وابسته نیست، برای همینه که
 * Room مستقیم اینام نمی‌گیره، همون الگوی [ChequeEntity.type]/[ChequeEntity.status].
 * [avatarColor] موقعِ ساختن **قطعی از رو نامِ طرفِ‌حساب** تعیین می‌شه (نه تصادفی، نه دستی -
 * جوابِ سوالِ ۱۱ی design/MESSAGE-round4-to-design.md) - رجوع کن به [DebtRepository][ir.sadteam.loancalc.data.DebtRepository].
 */
@Entity(tableName = "counterparties")
data class CounterpartyEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val createdAt: String,
    val phone: String? = null,
    val avatarColor: String = "GREEN",
    val avatarShape: String = "BOY",
)
