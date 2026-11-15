package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** یه ردیفِ طلب/بدهی مربوط به یه [CounterpartyEntity]. [type] یا "OWED_TO_ME" (طرف به من بدهکاره،
 * یعنی طلبِ من) یا "I_OWE" (من به طرف بدهکارم) - رجوع کن به core.DebtType. */
@Entity(tableName = "debts", indices = [Index("counterpartyId")])
data class DebtEntity(
    @PrimaryKey val id: Long,
    val counterpartyId: Long,
    val amount: Double,
    val type: String,
    val description: String,
    val year: Int,
    val month: Int,
    val day: Int,
    val settled: Boolean,
    val createdAt: String,
)
