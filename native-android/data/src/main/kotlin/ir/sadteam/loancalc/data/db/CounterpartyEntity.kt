package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** یه «طرفِ‌حساب» برای ماژولِ طلب‌وبدهی - فقط یه شخص/نهاده، مانده‌ی طلب/بدهیش از رو جمعِ
 * [DebtEntity] های مربوطه محاسبه می‌شه (ذخیره نمی‌شه)، هم‌الگو با AccountEntity.currentBalance. */
@Entity(tableName = "counterparties")
data class CounterpartyEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val createdAt: String,
)
