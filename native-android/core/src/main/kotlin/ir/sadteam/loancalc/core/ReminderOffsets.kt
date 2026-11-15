package ir.sadteam.loancalc.core

/** گزینه‌های ثابتِ زمان‌بندیِ یادآوری که تو تنظیماتِ سراسری/اختصاصیِ هر وام و چک قابل‌انتخابن -
 * تعداد روزهای قبل از سررسید. */
val REMINDER_OFFSET_OPTIONS = listOf(1, 3, 7)

/** پورت مشترکِ parse/format برای فیلدِ CSVِ `reminderDayOffsets` (هم رو LoanEntity/ChequeEntity
 * هم رو UiPrefs) - یه‌جا نگه داشته می‌شه تا Worker و UI دقیقاً یه برداشتِ یکسان از رشته داشته باشن. */
fun parseReminderOffsets(csv: String): Set<Int> =
    csv.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()

fun formatReminderOffsets(offsets: Set<Int>): String = offsets.sorted().joinToString(",")

/** برچسبِ فارسیِ هر گزینه‌ی [REMINDER_OFFSET_OPTIONS] برای نمایش رو چیپ‌ها. */
fun reminderOffsetLabel(days: Int): String = when (days) {
    7 -> "۱ هفته قبل"
    1 -> "۱ روز قبل"
    else -> "${toFa(days.toString())} روز قبل"
}
