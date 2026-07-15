package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val bankName: String,
    val initialBalance: Double,
    val createdAt: String,
)
