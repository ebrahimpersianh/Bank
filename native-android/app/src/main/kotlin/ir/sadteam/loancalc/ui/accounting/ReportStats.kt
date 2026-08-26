package ir.sadteam.loancalc.ui.accounting

import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.data.db.RecurringPaymentEntity
import ir.sadteam.loancalc.ui.components.persianMonthName

/** خروجیِ محاسبه‌شده‌ی تبِ گزارش - همه‌چیزِ فریمِ `26a` تو یه شیِ واحد. */
data class ReportStats(
    val periodLabel: String,
    val periodSpend: Double,
    val deltaPercent: Int?,
    val monthlyBars: List<Double>,
    val firstBarLabel: String,
    val lastBarLabel: String,
    val byCategory: Map<String, Double>,
    val fixedShare: Int?,
    val fixedAmount: Double,
    val freeAmount: Double,
    val recurringCount: Int,
    val recurringMonthly: Double,
    val overspentCategory: OverspentCategory?,
)

/** دسته‌ای که این ماه محسوس بیشتر از معمول خرج شده - کارتِ کشفِ دومِ `26a`. */
data class OverspentCategory(val name: String, val percent: Int)

private const val EXPENSE = "WITHDRAWAL"
private const val DEPOSIT = "DEPOSIT"

/** «۷ ماهِ گذشته تا همین ماه» - همون تعدادی که نمودارِ هیرو نشون می‌ده. */
private const val BAR_MONTHS = 7

/**
 * قاعده‌ی کارتِ کشفِ دوم، عیناً از `design/ANSWERS-section-37.md` بندِ ۵:
 *
 * > دسته‌ای که جمعِ ماهِ جاری‌اش دستِ‌کم **۱۵٪** از میانگینِ **سه ماهِ کاملِ گذشته** بیشتر باشد،
 * > با شرطِ **حداقل پنج تراکنش در هر پنجره**. بزرگ‌ترین انحراف نشان داده می‌شود، یکی در ماه.
 */
private const val OVERSPEND_THRESHOLD = 0.15
private const val OVERSPEND_MIN_TX = 5

fun buildReportStats(
    transactions: List<AccountTransactionEntity>,
    recurring: List<RecurringPaymentEntity>,
    today: PersianDate,
    period: ReportPeriod,
): ReportStats {
    val expenses = transactions.filter { it.type == EXPENSE }

    // ماه‌های پنجره‌ی فعلی (۱ / ۳ / ۱۲ ماه، شاملِ همین ماه)
    val windowMonths = (0 until period.months).map { back -> monthBack(today, back) }
    val inWindow = expenses.filter { tx -> windowMonths.any { it.first == tx.year && it.second == tx.month } }
    val periodSpend = inWindow.sumOf { it.amount }

    // پنجره‌ی قبلی، برای درصدِ تغییر
    val prevMonths = (period.months until period.months * 2).map { back -> monthBack(today, back) }
    val prevSpend = expenses
        .filter { tx -> prevMonths.any { it.first == tx.year && it.second == tx.month } }
        .sumOf { it.amount }
    val delta = if (prevSpend > 0.0) (((periodSpend - prevSpend) / prevSpend) * 100).toInt() else null

    // نمودارِ ۷ ماهه - همیشه ماهانه‌ست، مستقل از دوره‌ی انتخاب‌شده
    val bars = (BAR_MONTHS - 1 downTo 0).map { back ->
        val (y, m) = monthBack(today, back)
        expenses.filter { it.year == y && it.month == m }.sumOf { it.amount }
    }

    val byCategory = inWindow
        .groupBy { it.category?.takeIf { c -> c.isNotBlank() } ?: "سایر" }
        .mapValues { (_, list) -> list.sumOf { it.amount } }

    // ثابت در برابرِ آزاد: «ثابت» جمعِ پرداخت‌های تکراریِ ماهانه‌ست (اجاره، قسط، قبض)،
    // «آزاد» یعنی درآمدِ همین پنجره منهای همون.
    val recurringExpenses = recurring.filter { it.type == EXPENSE }
    val fixedAmount = recurringExpenses.sumOf { it.amount } * period.months
    val income = transactions
        .filter { it.type == DEPOSIT && windowMonths.any { w -> w.first == it.year && w.second == it.month } }
        .sumOf { it.amount }
    val fixedShare = if (income > 0.0 && fixedAmount > 0.0) {
        ((fixedAmount / income) * 100).toInt().coerceIn(0, 100)
    } else {
        null
    }

    return ReportStats(
        periodLabel = when (period) {
            ReportPeriod.MONTH -> persianMonthName(today.m)
            ReportPeriod.SEASON -> "سه ماه"
            ReportPeriod.YEAR -> "امسال"
        },
        periodSpend = periodSpend,
        deltaPercent = delta,
        monthlyBars = bars,
        firstBarLabel = persianMonthName(monthBack(today, BAR_MONTHS - 1).second),
        lastBarLabel = persianMonthName(today.m),
        byCategory = byCategory,
        fixedShare = fixedShare,
        fixedAmount = fixedAmount,
        freeAmount = (income - fixedAmount).coerceAtLeast(0.0),
        recurringCount = recurringExpenses.size,
        recurringMonthly = recurringExpenses.sumOf { it.amount },
        overspentCategory = findOverspentCategory(expenses, today),
    )
}

/** ماهِ `back` تا قبل از [from] - به‌صورتِ (سال، ماه). */
private fun monthBack(from: PersianDate, back: Int): Pair<Int, Int> {
    var y = from.y
    var m = from.m - back
    while (m <= 0) {
        m += 12
        y -= 1
    }
    return y to m
}

/**
 * پیاده‌سازیِ دقیقِ قاعده‌ی بندِ ۵. جدا نگه داشته شد تا تست‌پذیر باشه.
 *
 * ⚠️ «سه ماهِ **کاملِ** گذشته» یعنی ماهِ جاری داخلش نیست.
 */
internal fun findOverspentCategory(
    expenses: List<AccountTransactionEntity>,
    today: PersianDate,
): OverspentCategory? {
    val current = expenses.filter { it.year == today.y && it.month == today.m }
    if (current.size < OVERSPEND_MIN_TX) return null
    val pastMonths = (1..3).map { monthBack(today, it) }
    val past = expenses.filter { tx -> pastMonths.any { it.first == tx.year && it.second == tx.month } }
    if (past.size < OVERSPEND_MIN_TX) return null

    val label = { tx: AccountTransactionEntity -> tx.category?.takeIf { it.isNotBlank() } ?: "سایر" }
    val currentByCat = current.groupBy(label).mapValues { (_, l) -> l.sumOf { it.amount } }
    val pastByCat = past.groupBy(label)

    return currentByCat.mapNotNull { (name, now) ->
        val rows = pastByCat[name] ?: return@mapNotNull null
        if (rows.size < OVERSPEND_MIN_TX) return@mapNotNull null
        val average = rows.sumOf { it.amount } / 3.0
        if (average <= 0.0) return@mapNotNull null
        val ratio = (now - average) / average
        if (ratio < OVERSPEND_THRESHOLD) null else OverspentCategory(name, (ratio * 100).toInt())
    }.maxByOrNull { it.percent }
}
