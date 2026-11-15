package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey val id: Long,
    val label: String,
    val amount: Double,
    val type: String,
    val createdAt: String,
)
