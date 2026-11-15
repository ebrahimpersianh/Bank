package ir.sadteam.loancalc.core

/** روشِ تقسیمِ یه رویدادِ «دنگ» - فریمِ `22c`، جوابِ سوالِ ۷ی
 * design/MESSAGE-round4-to-design.md: هر ۴ روش از نسخه‌ی اول. */
enum class DangMethod(val label: String) {
    EQUAL("مساوی"),
    PERCENTAGE("درصدی"),
    CUSTOM("دلخواه"),
    ITEMIZED("قلم‌به‌قلم"),
}

/**
 * سهمِ مساویِ [count] نفر از [total] - تقسیمِ سادهٔ اعشاری باقیمانده جمع می‌ذاره (مثلاً ۱۰۰۰۰۰ بینِ
 * ۳ نفر می‌شه ۳۳۳۳۳٫۳۳ که سه‌تاش جمعاً به ۹۹۹۹۹٫۹۹ می‌رسه، نه ۱۰۰۰۰۰) - باقیمانده به ریال‌ترین شکل
 * (گردکردنِ سهمِ اول به‌جای نصف‌کردنِ خطا بینِ همه) به **نفرِ اول** اضافه می‌شه تا جمعِ دقیقِ سهم‌ها
 * همیشه با [total] برابر باشه.
 */
fun equalDangShares(total: Double, count: Int): List<Double> {
    if (count <= 0) return emptyList()
    val base = total / count
    val shares = MutableList(count) { base }
    val roundedSum = shares.sum()
    shares[0] += total - roundedSum
    return shares
}

/**
 * سهمِ هرکس از [total] طبقِ [percentages]ِ داده‌شده (به همون ترتیب) - جمعِ درصدها لازم نیست دقیقاً
 * ۱۰۰ بشه (اعتبارسنجیِ اون کارِ UIه)، این تابع فقط `total × pct/100` رو برمی‌گردونه.
 */
fun percentageDangShares(total: Double, percentages: List<Double>): List<Double> =
    percentages.map { total * it / 100.0 }
