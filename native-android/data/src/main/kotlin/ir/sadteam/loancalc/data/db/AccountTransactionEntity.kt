package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account_transactions")
data class AccountTransactionEntity(
    @PrimaryKey val id: Long,
    val accountId: Long,
    val type: String,
    val amount: Double,
    val description: String,
    val year: Int,
    val month: Int,
    val day: Int,
    val createdAt: String,
    val category: String? = null,
)
