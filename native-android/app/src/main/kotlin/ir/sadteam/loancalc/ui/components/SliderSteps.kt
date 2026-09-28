package ir.sadteam.loancalc.ui.components

/**
 * تعدادِ `steps`ِ یه اسلایدرِ مبلغ (برای [SlimSlider]) طوری که کشیدنش رو گام‌های گردِ [chunk] ریالی
 * بایسته (مثلاً ۱۰۰,۰۰۰,۰۰۰ ریال = ۱۰ میلیون تومان) - خواسته‌ی صریحِ کاربر: کشیدنِ اسلایدرِ مبلغ باید
 * عددِ گرد بده (۲۰۰ بعد ۲۱۰ میلیون تومان...)، نه مقادیرِ پیوسته/دلبخواه. فرمولِ Composeِ Slider:
 * تعدادِ کلِ توقف‌ها = steps+۲ (شاملِ دو سرِ رنج)، پس فاصله‌ی هر توقف = span/(steps+۱).
 */
fun amountSliderSteps(range: ClosedFloatingPointRange<Float>, chunk: Float = 100_000_000f): Int {
    val span = range.endInclusive - range.start
    if (span <= 0f || chunk <= 0f) return 0
    return ((span / chunk).toInt() - 1).coerceAtLeast(0)
}
