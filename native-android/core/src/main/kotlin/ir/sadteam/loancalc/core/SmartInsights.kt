package ir.sadteam.loancalc.core

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * 🧠 پیشنهادهای هوشمندِ جیبک (۷ مهر، خواسته‌ی کاربر: «برنامه هوشمندتر بشه»).
 * کاملاً روی گوشی؛ ورودی فقط تراکنش‌های خودِ کاربر. هر پیشنهاد یک کلیدِ ثابت دارد تا «بستن»
 * بتواند همان یکی را برای مدتی پنهان کند. مبلغ‌ها ریال‌اند (قاعده‌ی پروژه)، نمایش با برنامه.
 */
object SmartInsights {
    /** تراکنشِ ساده‌شده؛ جابه‌جایی بینِ حساب‌های خودِ کاربر نباید این‌جا بیاید. */
    data class Tx(
        val isExpense: Boolean,
        val amountRial: Double,
        val y: Int,
        val m: Int,
        val d: Int,
        val category: String?,
        val description: String,
    )

    enum class Kind { UNUSUAL_SPEND, MONTH_FORECAST, RECURRING, SALARY_MISSING, SAVE_SURPLUS, DUES_OVER_BALANCE }

    data class Insight(
        val key: String,
        val kind: Kind,
        val title: String,
        val body: String,
        /** برای دکمه‌ی کنشِ اختیاری (مثلاً نامِ دسته برای ساختِ پرداختِ تکراری). */
        val payload: String? = null,
    )

    private const val MIN_MEANINGFUL_RIAL = 5_000_000.0 // ۵۰۰ هزار تومان

    private fun ym(y: Int, m: Int) = y * 12 + (m - 1)
    private fun prevYm(y: Int, m: Int, back: Int): Pair<Int, Int> {
        val v = ym(y, m) - back
        return (v / 12) to (v % 12 + 1)
    }

    /** منفی با «−»ِ جلوی عدد (قاعده‌ی طرح) - قبلاً «۴٬۰۹۵٬۰۶۰-» چاپ می‌شد. */
    private fun toman(rial: Double): String {
        val v = (rial / 10).roundToLong()
        val s = toFa(fmt(abs(v).toDouble()))
        return if (v < 0) "−$s" else s
    }

    fun compute(
        txs: List<Tx>,
        today: PersianDate,
        daysInMonth: Int,
        /** جمعِ قسط و چکِ پرداختنیِ ۷ روزِ آینده (ریال) و جمعِ موجودیِ حساب‌ها. */
        upcomingDues7d: Double = 0.0,
        totalBalance: Double? = null,
        existingRecurringNames: Set<String> = emptySet(),
    ): List<Insight> {
        val out = mutableListOf<Insight>()
        val thisMonth = txs.filter { it.y == today.y && it.m == today.m }
        val prev = (1..3).map { prevYm(today.y, today.m, it) }
        val prevMonths = prev.map { (y, m) -> txs.filter { it.y == y && it.m == m } }
        val hasHistory = prevMonths.count { it.isNotEmpty() } >= 1

        // ۸) قسط و چکِ هفته‌ی آینده بیشتر از موجودی
        if (totalBalance != null && upcomingDues7d > 0 && upcomingDues7d > totalBalance) {
            out += Insight(
                "dues_${today.y}_${today.m}_${today.d / 7}", Kind.DUES_OVER_BALANCE,
                "هفته‌ی بعد پولت کم میاد",
                "تا ۷ روزِ آینده ${toman(upcomingDues7d)} تومان قسط و چک داری ولی موجودیِ حساب‌هات " +
                    // علامتِ منفی در متنِ راست‌به‌چپ سمتِ اشتباه می‌افتاد؛ با کلمه گفته می‌شود.
                    if (totalBalance < 0) "${toman(-totalBalance)} تومان منفیه." else "${toman(totalBalance)} تومانه.",
            )
        }

        if (hasHistory) {
            // ۲) خرجِ غیرعادی در یک دسته (نسبت به میانگینِ ماه‌های قبل، هم‌اندازه‌ی روزهای گذشته)
            val progress = today.d.toDouble() / daysInMonth
            val cats = thisMonth.filter { it.isExpense && it.category != null }.groupBy { it.category!! }
            cats.forEach { (cat, list) ->
                val now = list.sumOf { it.amountRial }
                val avg = prevMonths.filter { it.isNotEmpty() }.map { m -> m.filter { it.isExpense && it.category == cat }.sumOf { it.amountRial } }
                    .average().takeIf { !it.isNaN() } ?: return@forEach
                val expectedSoFar = avg * progress
                if (expectedSoFar > 0 && now > expectedSoFar * 1.4 && now - expectedSoFar > MIN_MEANINGFUL_RIAL) {
                    val pct = ((now / expectedSoFar - 1) * 100).roundToLong()
                    out += Insight(
                        "unusual_${cat}_${today.y}_${today.m}", Kind.UNUSUAL_SPEND,
                        "خرجِ «$cat» بالاست",
                        "این ماه تا امروز ${toFa(pct)}٪ بیشتر از معمولت برای «$cat» خرج کردی (${toman(now)} تومان).",
                        payload = cat,
                    )
                }
            }

            // ۳) پیش‌بینیِ آخرِ ماه
            val spentNow = thisMonth.filter { it.isExpense }.sumOf { it.amountRial }
            val lastMonthSpend = prevMonths[0].filter { it.isExpense }.sumOf { it.amountRial }
            if (today.d >= 7 && spentNow > 0 && lastMonthSpend > 0) {
                val forecast = spentNow / today.d * daysInMonth
                if (forecast > lastMonthSpend * 1.15 && forecast - lastMonthSpend > MIN_MEANINGFUL_RIAL) {
                    out += Insight(
                        "forecast_${today.y}_${today.m}", Kind.MONTH_FORECAST,
                        "پیش‌بینیِ آخرِ ماه",
                        "با این روند تا آخرِ ماه حدودِ ${toman(forecast)} تومان خرج می‌کنی؛ ماهِ قبل ${toman(lastMonthSpend)} بود.",
                    )
                }
            }

            // ۶) پس‌اندازِ ماهِ قبل
            val lastIncome = prevMonths[0].filter { !it.isExpense }.sumOf { it.amountRial }
            val surplus = lastIncome - lastMonthSpend
            if (today.d <= 10 && surplus > MIN_MEANINGFUL_RIAL * 2) {
                out += Insight(
                    "surplus_${today.y}_${today.m}", Kind.SAVE_SURPLUS,
                    "ماهِ قبل پول اضافه آوردی",
                    "ماهِ قبل ${toman(surplus)} تومان کمتر از درآمدت خرج کردی. بخشی‌ش رو بذار برای هدفِ پس‌اندازت.",
                )
            }
        }

        // ۵) حقوقِ دیرکرده: بزرگ‌ترین واریزِ هر کدام از ۳ ماهِ قبل، روزِ مشابه و مبلغِ مشابه
        val salaries = prevMonths.mapNotNull { m -> m.filter { !it.isExpense }.maxByOrNull { it.amountRial } }
        if (salaries.size == 3) {
            val days = salaries.map { it.d }
            val amounts = salaries.map { it.amountRial }
            val avgAmount = amounts.average()
            val steady = days.max() - days.min() <= 4 && amounts.all { abs(it - avgAmount) <= avgAmount * 0.25 }
            val usualDay = days.sorted()[1]
            val gotIt = thisMonth.any { !it.isExpense && it.amountRial >= avgAmount * 0.7 }
            if (steady && avgAmount > MIN_MEANINGFUL_RIAL && !gotIt && today.d in (usualDay + 2)..(usualDay + 10)) {
                out += Insight(
                    "salary_${today.y}_${today.m}", Kind.SALARY_MISSING,
                    "حقوقِ این ماه اومده؟",
                    "معمولاً حدودِ ${toFa(usualDay)}ِ هر ماه حدودِ ${toman(avgAmount)} تومان واریزی داری که این ماه هنوز ثبت نشده.",
                )
            }
        }

        // ۴) پرداختِ تکراری: همان شرح و مبلغِ نزدیک در هر ۳ ماهِ قبل، که هنوز «پرداختِ تکراری» نیست
        fun norm(s: String) = s.trim().lowercase().replace(Regex("\\s+"), " ")
        val candidates = prevMonths[0].filter { it.isExpense && it.description.isNotBlank() && !it.description.startsWith("خودکار") }
        candidates.groupBy { norm(it.description) }.forEach { (desc, list) ->
            val amount = list.first().amountRial
            val inAll = prevMonths.drop(1).all { m ->
                m.any { it.isExpense && norm(it.description) == desc && abs(it.amountRial - amount) <= amount * 0.1 }
            }
            if (inAll && amount > 0 && existingRecurringNames.none { norm(it) == desc }) {
                out += Insight(
                    "recurring_$desc", Kind.RECURRING,
                    "«${list.first().description}» هر ماه تکرار می‌شه",
                    "سه ماهِ اخیر هر ماه حدودِ ${toman(amount)} تومان برای این پرداختی. پرداختِ تکراری بسازم تا یادت بندازه؟",
                    payload = list.first().description,
                )
            }
        }

        return out.distinctBy { it.key }
    }

    /**
     * ۱) دسته‌ی پیشنهادی از روی تاریخچه: پرتکرارترین دسته‌ای که کاربر قبلاً برای همین شرح زده.
     */
    fun suggestCategory(history: List<Tx>, description: String, isExpense: Boolean): String? {
        val key = description.trim().lowercase()
        if (key.length < 2) return null
        return history.asSequence()
            .filter { it.isExpense == isExpense && it.category != null && it.description.trim().lowercase() == key }
            .groupingBy { it.category!! }.eachCount()
            .maxByOrNull { it.value }?.key
    }

    /** ۷) خلاصه‌ی هفته: جمعِ خرجِ ۷ روزِ اخیر، هفته‌ی قبلش، و پرخرج‌ترین دسته. */
    data class Week(val spend: Double, val prevSpend: Double, val topCategory: String?)

    fun week(txs: List<Tx>, today: PersianDate): Week {
        fun daysAgo(t: Tx) = runCatching { JalaliCalendar.daysBetween(PersianDate(t.y, t.m, t.d), today) }.getOrDefault(999)
        val w = txs.filter { it.isExpense && daysAgo(it) in 0..6 }
        val p = txs.filter { it.isExpense && daysAgo(it) in 7..13 }
        return Week(
            w.sumOf { it.amountRial },
            p.sumOf { it.amountRial },
            w.filter { it.category != null }.groupBy { it.category!! }.maxByOrNull { e -> e.value.sumOf { it.amountRial } }?.key,
        )
    }
}
