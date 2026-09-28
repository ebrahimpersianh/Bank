package ir.sadteam.loancalc.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ParsingRuleDao {
    @Query("SELECT * FROM parsing_rules ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<ParsingRuleEntity>>

    @Query("SELECT * FROM parsing_rules ORDER BY sortOrder ASC")
    suspend fun getAll(): List<ParsingRuleEntity>

    @Upsert
    suspend fun upsert(rule: ParsingRuleEntity)

    @Upsert
    suspend fun upsertAll(rules: List<ParsingRuleEntity>)

    @Delete
    suspend fun delete(rule: ParsingRuleEntity)

    @Query("UPDATE parsing_rules SET matchCount = matchCount + 1 WHERE id = :id")
    suspend fun bumpMatchCount(id: Long)
}
