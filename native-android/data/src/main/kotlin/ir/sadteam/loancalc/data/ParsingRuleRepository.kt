package ir.sadteam.loancalc.data

import ir.sadteam.loancalc.data.db.ParsingRuleDao
import ir.sadteam.loancalc.data.db.ParsingRuleEntity
import kotlinx.coroutines.flow.Flow

/**
 * موتورِ قاعده‌های تشخیص. **مرتب، اولین تطبیق برنده** - این تصمیمِ بنیادیِ طرحه و
 * [firstMatch] تنها جاییه که اعمال می‌شه.
 */
class ParsingRuleRepository(private val dao: ParsingRuleDao) {
    fun observeRules(): Flow<List<ParsingRuleEntity>> = dao.observeAll()

    suspend fun getRules(): List<ParsingRuleEntity> = dao.getAll()

    suspend fun save(rule: ParsingRuleEntity) = dao.upsert(rule)

    suspend fun delete(rule: ParsingRuleEntity) = dao.delete(rule)

    /** ترتیبِ تازه رو ذخیره می‌کنه - `sortOrder` از جای هر قاعده تو لیست ساخته می‌شه. */
    suspend fun reorder(rules: List<ParsingRuleEntity>) {
        dao.upsertAll(rules.mapIndexed { index, rule -> rule.copy(sortOrder = index) })
    }

    /**
     * اولین قاعده‌ای که با [text] می‌خونه، یا `null`. شمارنده‌ی تطبیقِ همون قاعده هم
     * یکی بالا می‌ره تا کاربر تو صفحه‌ی قاعده‌ها ببینه کدوم واقعاً کار می‌کنه.
     */
    suspend fun firstMatch(text: String, isWithdrawal: Boolean): ParsingRuleEntity? {
        val match = dao.getAll().firstOrNull { rule ->
            text.contains(rule.pattern, ignoreCase = true) &&
                (
                    rule.txType == null ||
                        (rule.txType == "WITHDRAWAL") == isWithdrawal
                    )
        } ?: return null
        dao.bumpMatchCount(match.id)
        return match
    }
}
