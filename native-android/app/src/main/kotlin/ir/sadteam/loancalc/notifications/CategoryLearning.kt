package ir.sadteam.loancalc.notifications

import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.ParsingRuleRepository
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.data.db.ParsingRuleEntity
import kotlinx.coroutines.flow.first

/**
 * 🧠 یادگیریِ دسته (۱۴ مهر، خواسته‌ی کاربر: «برنامه باید هوشمندتر شود»).
 *
 * تراکنشِ خودکار، طرفِ حساب را در توضیحش دارد («به مبینا فتحی»). وقتی کاربر دسته‌اش را
 * انتخاب می‌کند، یک قاعده‌ی خودکار ساخته می‌شود: «هر متنی که "مبینا فتحی" دارد → همین دسته».
 * پیامک/اعلانِ بعدی از همین قاعده‌ها ([ParsingRuleRepository.firstMatch]) دسته می‌گیرد.
 */
object CategoryLearning {
    private const val GENERIC_PREFIX = "خودکار"

    /** نامِ طرفِ حساب از توضیحِ تراکنشِ خودکار؛ `null` اگر توضیح عمومی است. */
    fun counterpartyOf(tx: AccountTransactionEntity): String? {
        if (tx.originLabel == null) return null
        val name = tx.description.removePrefix("به ").removePrefix("از ").trim()
        return name.takeIf { it.length >= 3 && !it.startsWith(GENERIC_PREFIX) }
    }

    /** دسته را روی تراکنش می‌گذارد و یاد می‌گیرد. */
    suspend fun apply(
        accounts: AccountRepository,
        rules: ParsingRuleRepository,
        txId: Long,
        category: String,
    ) {
        val tx = accounts.transactionById(txId) ?: return
        accounts.updateTransaction(tx.copy(category = category))
        learn(rules, tx, category)
    }

    suspend fun learn(rules: ParsingRuleRepository, tx: AccountTransactionEntity, category: String) {
        val name = counterpartyOf(tx) ?: return
        if (category.startsWith("سایر")) return // «سایر» یاد گرفتنی نیست
        val all = rules.getRules()
        val same = all.firstOrNull { it.pattern == name && it.txType == tx.type }
        if (same != null) {
            if (same.category != category) rules.save(same.copy(category = category))
            return
        }
        ir.sadteam.loancalc.data.UsageStats.action("category_learned")
        rules.save(
            ParsingRuleEntity(
                id = System.currentTimeMillis(),
                pattern = name,
                category = category,
                txType = tx.type,
                sortOrder = all.size,
                auto = true,
            ),
        )
    }

    /** دو دسته‌ی پراستفاده‌ی همین نوع (برای دکمه‌های اعلان). «سایر…» حساب نمی‌شود. */
    suspend fun topCategories(accounts: AccountRepository, type: String, n: Int = 2): List<String> {
        val used = accounts.observeTransactions().first()
            .mapNotNull { tx -> tx.category?.takeIf { tx.type == type && it.isNotBlank() && !it.startsWith("سایر") } }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }.map { it.key }
        val defaults = if (type == "DEPOSIT") listOf("حقوق", "هدیه") else listOf("خوراک", "خرید")
        return (used + defaults).distinct().take(n)
    }
}
