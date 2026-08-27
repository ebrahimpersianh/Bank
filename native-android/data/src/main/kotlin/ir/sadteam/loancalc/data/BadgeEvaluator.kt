package ir.sadteam.loancalc.data

import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianCalendar
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.data.db.BudgetEntity
import ir.sadteam.loancalc.data.db.LoanEntity
import kotlinx.coroutines.flow.first

/**
 * **سنجشِ نُه نشانِ `18a`** - یک‌جا، رو داده‌ی محلی.
 *
 * 🚨 **قاعده‌ی گذشته - مهم‌ترین بندِ این فایل.**
 * کاربرانِ فعلی ماه‌ها داده دارن. لحظه‌ای که نشان‌ها روشن بشن، پنج‌شش نشان **با هم** باز
 * می‌شن و صدها سکه یک‌جا می‌ریزه. اگه این مدیریت نشه دو مشکل داریم:
 * شش جشنِ پشتِ‌سرهم (آزاردهنده)، و جشنِ ارزان (کاربر کاری نکرده و همه‌چیز باز شد، پس
 * نشان‌ها از همون اول بی‌ارزشن).
 *
 * پس بازشدنِ **گذشته صامته** و نوعش تو دفتر `badge_retro`ه نه `badge`؛ اپ یه‌بار جمعش رو
 * تو یه شیت نشون می‌ده. جشنِ کامل فقط برای بازشدنِ **زنده**ست.
 */
class BadgeEvaluator(
    private val gamification: GamificationRepository,
    private val accountRepository: AccountRepository,
    private val loanRepository: LoanRepository,
) {
    /**
     * همه‌ی شرط‌ها رو می‌سنجه و نشان‌های تازه رو باز می‌کنه.
     *
     * @param silent بازشدنِ گذشته - بی‌جشن، با نوعِ `badge_retro`.
     * @return نشان‌هایی که **همین حالا** باز شدن (برای جشن یا شیتِ جمع‌بندی).
     */
    suspend fun evaluate(silent: Boolean): List<Badge> {
        val already = gamification.achievements.first().map { it.code }.toSet()
        val state = snapshot()
        val opened = mutableListOf<Badge>()
        Badge.entries.forEach { badge ->
            if (badge.code in already || badge.comingSoon) return@forEach
            if (progressOf(badge, state) >= 1f) {
                gamification.unlock(badge.code, badge.coins, silent = silent)
                opened += badge
            }
        }
        return opened
    }

    /** وضعیتِ همه‌ی نشان‌ها برای صفحه‌ی `18a` - باز، در جریان (با درصد)، یا قفل. */
    suspend fun progressList(): List<BadgeProgress> {
        val already = gamification.achievements.first().map { it.code }.toSet()
        val state = snapshot()
        return Badge.entries.map { badge ->
            val unlocked = badge.code in already
            val p = if (badge.comingSoon) null else progressOf(badge, state).coerceIn(0f, 1f)
            BadgeProgress(badge, unlocked, if (unlocked) 1f else p)
        }
    }

    private data class Snapshot(
        val transactions: List<AccountTransactionEntity>,
        val budgets: List<BudgetEntity>,
        val loans: List<LoanEntity>,
        val activeDays: Int,
    )

    private suspend fun snapshot() = Snapshot(
        transactions = accountRepository.observeTransactions().first(),
        budgets = accountRepository.observeBudgets().first(),
        loans = loanRepository.observeLoans().first(),
        activeDays = gamification.activeDays.first(),
    )

    private fun progressOf(badge: Badge, s: Snapshot): Float = when (badge) {
        Badge.FIRST_STEP -> if (s.transactions.isNotEmpty()) 1f else 0f
        Badge.BUDGETER -> if (s.budgets.any { it.monthlyCap > 0 }) 1f else 0f
        Badge.FULL_WEEK -> s.activeDays / 7f
        Badge.STEADY_MONTH -> s.activeDays / 30f
        // تنها شرطی که میانگین می‌خواد: هفت روزِ پشتِ‌سرهم که خرجِ هر روز کمتر از
        // میانگینِ روزانه‌ی ۳۰ روزِ اخیر بوده.
        Badge.CAUTIOUS -> cautiousStreak(s.transactions) / 7f
        // یه ماهِ شمسیِ کامل بی ردشدن از **هیچ** بودجه‌ای. بودجه‌ی ما ماهانه‌ست
        // (`monthlyCap`)، پس مرزِ ماهِ شمسی درسته.
        Badge.UNDER_BUDGET -> underBudgetProgress(s)
        // همه‌ی تراکنش‌های ماهِ جاری دسته‌ی واقعی داشته باشن - نه «سایر هزینه»/«سایر درآمد».
        Badge.CLEAN_DESK -> cleanDeskProgress(s.transactions)
        Badge.LOAN_CLOSED -> s.loans.maxOfOrNull { loan ->
            if (loan.n <= 0) 0f else loan.paidCount.toFloat() / loan.n
        } ?: 0f
        // هدفِ پس‌انداز هنوز تو برنامه نیست - رجوع کن به `Badge.comingSoon`.
        Badge.GOAL_REACHED -> 0f
    }

    /** چند روزِ پشتِ‌سرهمِ اخیر خرجش زیرِ میانگینِ ۳۰ روزه بوده. */
    private fun cautiousStreak(all: List<AccountTransactionEntity>): Int {
        val today = JalaliCalendar.today()
        val spend = all.filter { it.type == TransactionType.WITHDRAWAL.name }
        if (spend.isEmpty()) return 0
        val perDay = spend.groupBy { "${it.year}-${it.month}-${it.day}" }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
        val average = perDay.values.average()
        if (average <= 0) return 0
        var streak = 0
        var cursor = today
        repeat(7) {
            val key = "${cursor.y}-${cursor.m}-${cursor.d}"
            val dayTotal = perDay[key] ?: 0.0
            // روزی که اصلاً خرج نداشته هم «زیرِ میانگین»ه.
            if (dayTotal < average) streak++ else return streak
            cursor = PersianCalendar.addDays(cursor, -1)
        }
        return streak
    }

    private fun underBudgetProgress(s: Snapshot): Float {
        val budgets = s.budgets.filter { it.monthlyCap > 0 }
        if (budgets.isEmpty()) return 0f
        val today = JalaliCalendar.today()
        val monthSpend = s.transactions
            .filter { it.type == TransactionType.WITHDRAWAL.name && it.year == today.y && it.month == today.m }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
        val exceeded = budgets.any { (monthSpend[it.categoryName] ?: 0.0) > it.monthlyCap }
        if (exceeded) return 0f
        // پیشرفت = چند روز از ماه گذشته. آخرِ ماه که رسید و هنوز رد نشده، باز می‌شه.
        return today.d / JalaliCalendar.daysInMonth(today.y, today.m).toFloat()
    }

    private fun cleanDeskProgress(all: List<AccountTransactionEntity>): Float {
        val today = JalaliCalendar.today()
        val month = all.filter { it.year == today.y && it.month == today.m }
        if (month.isEmpty()) return 0f
        val blind = setOf("سایر هزینه", "سایر درآمد", "")
        val categorized = month.count { it.category !in blind }
        return categorized.toFloat() / month.size
    }
}
