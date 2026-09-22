package ir.sadteam.loancalc.ui.accounting

import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.RecurringDetector
import ir.sadteam.loancalc.core.RecurringExpense
import ir.sadteam.loancalc.core.RecurringInput
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.data.db.RecurringPaymentEntity
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.jibak.toFa

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
    /**
     * **اشتراک‌یاب** - خرج‌های تکرارشونده‌ای که اپ خودش از رو تاریخچه کشف کرده و کاربر
     * **اعلامشون نکرده**. با [recurringCount] فرق داره: اون پرداخت‌های تکراریِ دستیِ خودِ
     * کاربره، این چیزیه که فراموش شده. رجوع کن به [RecurringDetector].
     */
    val detectedSubscriptions: List<RecurringExpense> = emptyList(),
    /**
     * سه عددِ ردیفِ آمارِ دوره (بازخوردِ ۳۱ شهریور با طرحِ مرجع).
     *
     * ⚠️ درآمد از قبل **داخلِ همین تابع** حساب می‌شد (برای `fixedShare`) ولی هیچ‌جا نشان
     * داده نمی‌شد - یعنی کلِ تبِ گزارش فقط خرج را می‌گفت. حالا بیرون می‌آید، نه دوباره
     * حساب می‌شود: دو منبعِ حقیقت برای یک عدد همان خطای ثبت‌شده‌ی پروژه است.
     */
    val periodIncome: Double = 0.0,
    val incomeDeltaPercent: Int? = null,
    val transactionCount: Int = 0,
    val transactionCountDelta: Int = 0,
) {
    /** جمعِ ماهانه‌ی اشتراک‌های کشف‌شده. */
    val detectedMonthly: Double get() = detectedSubscriptions.sumOf { it.typicalAmountRial }
}

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
    // 🚨 جابه‌جایی بینِ دو حسابِ خودِ کاربر **خرج نیست**: هر انتقال دو ردیف می‌سازد (برداشت از
    // مبدأ، واریز به مقصد) با `sourceType = "transfer"` و یک `sourceId`ِ مشترک. تا امروز این
    // علامت این‌جا خوانده نمی‌شد، پس جابه‌جاییِ دو میلیونی، هم‌زمان دو میلیون «خرج» و دو میلیون
    // «درآمد» شمرده می‌شد و کلِ گزارش و میانگینِ ماهانه را باد می‌کرد. در محاسبه‌ی **موجودیِ
    // حساب‌ها** این ردیف‌ها لازم‌اند و دست‌نخورده می‌مانند - فقط از تحلیلِ خرج بیرون می‌روند.
    val realTransactions = transactions.filter { it.sourceType != "transfer" }
    val expenses = realTransactions.filter { it.type == EXPENSE }

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

    // 🚨 **میله‌ها با خودِ دوره عوض می‌شوند** (بازخوردِ ۳۱ شهریور با طرحِ مرجع).
    //
    // تا امروز نمودار همیشه «۷ ماهِ گذشته» بود، حتی وقتی کاربر روی «ماه» ایستاده بود -
    // یعنی عددِ بالای کارت مالِ یک ماه بود و نمودارِ زیرش مالِ هفت ماه. در نمای ماه،
    // **روزهای همان ماه** را نشان می‌دهد؛ در فصل و سال همان روندِ ماهانه می‌مانَد چون
    // ۹۰ یا ۳۶۵ میله در عرضِ یک کارت خط می‌شود نه نمودار.
    val bars = if (period == ReportPeriod.MONTH) {
        (1..JalaliCalendar.daysInMonth(today.y, today.m)).map { day ->
            expenses.filter { it.year == today.y && it.month == today.m && it.day == day }.sumOf { it.amount }
        }
    } else {
        (BAR_MONTHS - 1 downTo 0).map { back ->
            val (y, m) = monthBack(today, back)
            expenses.filter { it.year == y && it.month == m }.sumOf { it.amount }
        }
    }

    val byCategory = inWindow
        .groupBy { it.category?.takeIf { c -> c.isNotBlank() } ?: "سایر" }
        .mapValues { (_, list) -> list.sumOf { it.amount } }

    // ثابت در برابرِ آزاد: «ثابت» جمعِ پرداخت‌های تکراریِ ماهانه‌ست (اجاره، قسط، قبض)،
    // «آزاد» یعنی درآمدِ همین پنجره منهای همون.
    val recurringExpenses = recurring.filter { it.type == EXPENSE }
    val fixedAmount = recurringExpenses.sumOf { it.amount } * period.months
    val income = realTransactions
        .filter { it.type == DEPOSIT && windowMonths.any { w -> w.first == it.year && w.second == it.month } }
        .sumOf { it.amount }
    val prevIncome = realTransactions
        .filter { it.type == DEPOSIT && prevMonths.any { p -> p.first == it.year && p.second == it.month } }
        .sumOf { it.amount }
    val incomeDelta = if (prevIncome > 0.0) (((income - prevIncome) / prevIncome) * 100).toInt() else null
    // شمارش روی **همه‌ی** تراکنش‌های واقعی است (واریز و برداشت)، نه فقط خرج: عنوانش
    // «تعدادِ تراکنش» است و کاربر همان را می‌شمارد.
    val countNow = realTransactions.count { tx -> windowMonths.any { it.first == tx.year && it.second == tx.month } }
    val countPrev = realTransactions.count { tx -> prevMonths.any { it.first == tx.year && it.second == tx.month } }
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
        // برچسبِ دو سرِ نمودار با همان چیزی که کشیده شده می‌خوانَد.
        firstBarLabel = if (period == ReportPeriod.MONTH) {
            "${1.toFa()} ${persianMonthName(today.m)}"
        } else {
            persianMonthName(monthBack(today, BAR_MONTHS - 1).second)
        },
        lastBarLabel = if (period == ReportPeriod.MONTH) {
            "${JalaliCalendar.daysInMonth(today.y, today.m).toFa()} ${persianMonthName(today.m)}"
        } else {
            persianMonthName(today.m)
        },
        byCategory = byCategory,
        fixedShare = fixedShare,
        fixedAmount = fixedAmount,
        freeAmount = (income - fixedAmount).coerceAtLeast(0.0),
        recurringCount = recurringExpenses.size,
        recurringMonthly = recurringExpenses.sumOf { it.amount },
        overspentCategory = findOverspentCategory(expenses, today),
        detectedSubscriptions = detectSubscriptions(expenses, recurringExpenses),
        periodIncome = income,
        incomeDeltaPercent = incomeDelta,
        transactionCount = countNow,
        transactionCountDelta = countNow - countPrev,
    )
}

/**
 * اشتراک‌های کشف‌شده، منهای چیزهایی که کاربر **از قبل خودش** به‌عنوانِ پرداختِ تکراری ثبت
 * کرده (وگرنه یه قلم دو بار به کاربر نشون داده می‌شه: یه‌بار «پرداختِ تکراری»، یه‌بار «کشف»).
 * تطبیق با همون نرمال‌سازیِ [RecurringDetector.normalizeLabel] انجام می‌شه.
 */
private fun detectSubscriptions(
    expenses: List<AccountTransactionEntity>,
    declared: List<RecurringPaymentEntity>,
): List<RecurringExpense> {
    val found = RecurringDetector.detect(
        expenses.map {
            RecurringInput(
                description = it.description,
                amountRial = it.amount,
                year = it.year,
                month = it.month,
                day = it.day,
                category = it.category,
            )
        },
    )
    val declaredLabels = declared.map { RecurringDetector.normalizeLabel(it.name) }.toSet()
    return found.filter { it.label !in declaredLabels }
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
