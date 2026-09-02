package ir.sadteam.loancalc.ui.jibak

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.util.Locale

/**
 * قاعده‌ی عدد — از بخشِ ۲ فایلِ سیستمِ طراحی:
 * ارقام همیشه فارسی و با جداکنندهٔ «٬». مبلغ‌ها بدون واحد در ردیف‌ها؛
 * «تومان» فقط در کارتِ خلاصه. عددِ منفی با «−» می‌آید نه پرانتز.
 *
 * لایه‌ی سراسری: هیچ صفحه‌ای نباید عددِ لاتین چاپ کند. سه راه، به همین ترتیبِ اولویت:
 *   ۱. تابع‌های همین فایل روی عددِ خام (toFaMoney، toFaDate، …) — راهِ درست.
 *   ۲. String.faDigits() روی رشته‌ای که از سرور یا از strings.xml می‌آید.
 *   ۳. JibakNumerals.Transformation در فیلدهای ورودی — مقدارِ زیرین لاتین می‌ماند.
 * فهرستِ استثناها (جایی که رقم لاتین می‌ماند) در Numerals-global-handoff.md.
 */

private val faDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

/** U+066C — جداکننده‌ی هزارگانِ فارسی. نه کاما، نه نقطه. */
const val FA_GROUP_SEPARATOR = '٬'

/** U+066B — ممیزِ فارسی. */
const val FA_DECIMAL_SEPARATOR = '٫'

/** U+2212 — علامتِ منها. نه hyphen، نه پرانتز. */
const val FA_MINUS = '−'

// ─── لایه‌ی رشته‌ای ────────────────────────────────────────────────────────────

/**
 * هر رقمِ لاتین یا عربیِ رشته را فارسی می‌کند و کاما/نقطه‌ی جداکننده را هم
 * برمی‌گرداند. برای متنی که خودمان نساخته‌ایم: پاسخِ سرور، strings.xml با %d،
 * نامِ بانک با شماره، پیامکِ خام در صفحه‌ی قاعده‌های تشخیص.
 * روی URL و ایمیل و شبا صدا نزنید — فهرستِ استثناها را ببینید.
 */
fun String.faDigits(): String = buildString(length) {
    this@faDigits.forEach { c ->
        append(
            when {
                c in '0'..'9' -> faDigits[c - '0']
                c in '\u0660'..'\u0669' -> faDigits[c - '\u0660'] // ارقامِ عربی
                c == ',' -> FA_GROUP_SEPARATOR
                c == '-' -> FA_MINUS
                else -> c
            }
        )
    }
}

/** برگشت به لاتین — پیشِ ارسال به سرور، پارسِ عدد، و مقایسه‌ی رشته‌ها. */
fun String.latinDigits(): String = buildString(length) {
    this@latinDigits.forEach { c ->
        append(
            when {
                c in '۰'..'۹' -> '0' + (c - '۰')
                c in '\u0660'..'\u0669' -> '0' + (c - '\u0660')
                c == FA_GROUP_SEPARATOR || c == ',' -> return@forEach
                c == FA_DECIMAL_SEPARATOR -> '.'
                c == FA_MINUS -> '-'
                else -> c
            }
        )
    }
}

/** عددِ فیلدِ ورودی → Long. رقمِ فارسی، جداکننده، فاصله‌ی مجازی: همه پاک می‌شوند. */
fun String.parseFaAmount(): Long? =
    latinDigits().filter { it.isDigit() || it == '-' }.toLongOrNull()

// ─── مبلغ و درصد ──────────────────────────────────────────────────────────────

fun Int.toFa(): String = toLong().toFa()
fun Long.toFa(): String = buildString {
    val negative = this@toFa < 0
    val digits = kotlin.math.abs(this@toFa).toString()
    if (negative) append(FA_MINUS)
    digits.forEach { append(faDigits[it - '0']) }
}

/** ۸۴۰۰۰۰ → «۸۴۰٬۰۰۰» · ‎-۸۴۰۰۰۰ → «−۸۴۰٬۰۰۰» */
fun Long.toFaMoney(): String {
    val negative = this < 0
    val digits = kotlin.math.abs(this).toString()
    val grouped = digits.reversed().chunked(3).joinToString(FA_GROUP_SEPARATOR.toString()).reversed()
    val fa = buildString { grouped.forEach { append(if (it.isDigit()) faDigits[it - '0'] else it) } }
    return if (negative) "$FA_MINUS$fa" else fa
}

fun Int.toFaMoney(): String = toLong().toFaMoney()

/** مبلغِ ردیفِ فهرست — با علامتِ + برای درآمد، − برای خرج. */
fun Long.toFaSignedMoney(): String =
    if (this >= 0) "+${kotlin.math.abs(this).toFaMoney()}" else toFaMoney()

/**
 * فرمِ فشرده با علامت — «+۹٫۲M» / «−۸۴۰K». سودِ هر ردیف و سودِ هر گروه در تبِ
 * دارایی. جدا از toFaSignedMoney چون آنجا جا برای عددِ کامل هست و اینجا نیست.
 */
fun Long.toFaSignedCompact(): String =
    if (this >= 0) "+${kotlin.math.abs(this).toFaCompact()}" else toFaCompact()

/** ۶۵ → «۶۵٪» */
fun Int.toFaPercent(): String = "${toFa()}٪"

/** ۱۸٫۵ → «۱۸٫۵٪» — نرخِ سود، تنها جایی که درصدِ اعشاری داریم. یک رقمِ اعشار. */
fun Double.toFaPercent(): String {
    // Locale.US اجباری است: بی آن، روی گوشیِ فارسی خودِ format رقمِ فارسی و ممیزِ
    // «٫» برمی‌گرداند و replace('.') هیچ‌چیزی پیدا نمی‌کند.
    val s = String.format(Locale.US, "%.1f", this).removeSuffix(".0")
    return "${s.faDigits().replace('.', FA_DECIMAL_SEPARATOR)}٪"
}

/**
 * فرمِ فشرده‌ی **فارسی** — «۲۲۰ هزار» / «۶۷۶٫۶ میلیون» / «۹٫۱ میلیارد» / «۹۲۰».
 * جایی که ستون تنگ است: ردیفِ دارایی، سرگروه، قرصِ هیرو، خطِ قیمتِ روز.
 *
 * ⚠️ ورودی **تومان** است، نه ریال. برای مقدارِ ریالیِ دیتابیس `rialToFaCompact`
 * را صدا بزنید — نسخه‌ی قبلی همین اشتباه را می‌کرد و «۲۲۰ هزار تومانِ» یک دلار را
 * «۲٫۲M» نشان می‌داد.
 *
 * ⚠️ حرفِ «M»/«K» لاتین حذف شد: در صفحه‌ای که همه‌ی ارقامش فارسی است یک M لاتین
 * وسطِ عدد می‌نشیند و خوانده نمی‌شود؛ واحدِ فارسی هم گویاتر است.
 *
 * چهار بازه، و زیرِ هزار عددِ کامل چون سه رقم همیشه جا می‌شود.
 */
fun Long.toFaCompact(): String {
    val negative = this < 0
    val v = kotlin.math.abs(this)
    val body = when {
        v >= 1_000_000_000L -> faScaled(v, 1_000_000_000.0, "میلیارد")
        v >= 1_000_000L -> faScaled(v, 1_000_000.0, "میلیون")
        v >= 1_000L -> faScaled(v, 1_000.0, "هزار")
        else -> v.toFa()
    }
    return if (negative) "$FA_MINUS$body" else body
}

/**
 * یک رقمِ اعشار، بی صفرِ آخر، با واحدِ فارسی. `Locale.US` اجباری است (رجوع کن به
 * `toFaPercent`).
 */
private fun faScaled(v: Long, unit: Double, word: String): String {
    val s = String.format(Locale.US, "%.1f", v / unit).trimEnd('0').trimEnd('.')
    val digits = buildString {
        s.forEach { append(if (it.isDigit()) faDigits[it - '0'] else FA_DECIMAL_SEPARATOR) }
    }
    return "$digits $word"
}

/**
 * مقدارِ **ریالیِ** دیتابیس → فرمِ فشرده‌ی تومانی. تنها راهِ درستِ نمایشِ ستونِ مبلغ.
 *
 * هر جا در UI `.toLong().toFaCompact()` روی عددی از دیتابیس دیدید، باگ است:
 * عدد ریال است و ده برابر بزرگ نشان داده می‌شود.
 */
fun Double.rialToFaCompact(): String = rialToToman(toLong()).toFaCompact()

/** همان، با علامت — برای سود و زیان. */
fun Double.rialToFaSignedCompact(): String {
    val t = rialToToman(toLong())
    return if (t >= 0) "+" + kotlin.math.abs(t).toFaCompact() else t.toFaCompact()
}

/**
 * مقدارِ ریالی → جفتِ (عدد، واحد) — «۱۰۲٫۶» و «میلیون تومان».
 *
 * برای وسطِ نمودارِ دایره‌ای، جایی که یک خطِ کامل جا نمی‌شود: قطرِ داخلیِ دوناتِ ۷۴dp با
 * رینگِ ۱۳dp فقط **۴۸dp** است و «۱۰۲٫۶ میلیون تومان» در ۱۱sp حدودِ ۵۲dp عرض می‌گیرد، پس
 * به لبه‌ی رینگ می‌چسبد. دو خطِ کوتاه جا می‌شود، و عددِ درشت‌تر از واحد سلسله‌مراتبِ
 * درستی هم هست.
 *
 * زیرِ یک میلیون واحد «هزار تومان» می‌شود و زیرِ هزار «تومان» — پس خطِ دوم هیچ‌وقت خالی
 * نمی‌ماند و ارتفاعِ کارت با تغییرِ مبلغ نمی‌پرد.
 *
 * ⚠️ اینجا و نه در دو صفحه‌ی جدا: صفحه‌ی اول و تبِ گزارش هر دو همین دونات را دارند، و
 * نسخه‌ی محلی یعنی کپیِ سوم — همان الگویی که سه بار در `compact` تکرار شد.
 */
fun Double.rialToFaCompactParts(): Pair<String, String> {
    val toman = rialToToman(toLong())
    val v = kotlin.math.abs(toman)
    val (number, unit) = when {
        v >= 1_000_000_000L -> faOneDecimal(v / 1_000_000_000.0) to "میلیارد تومان"
        v >= 1_000_000L -> faOneDecimal(v / 1_000_000.0) to "میلیون تومان"
        v >= 1_000L -> faOneDecimal(v / 1_000.0) to "هزار تومان"
        else -> v.toFa() to "تومان"
    }
    return (if (toman < 0L) "$FA_MINUS$number" else number) to unit
}

/** یک رقمِ اعشار، بی صفرِ آخر، با ممیزِ فارسی. `Locale.US` اجباری است. */
private fun faOneDecimal(v: Double): String {
    val s = String.format(Locale.US, "%.1f", v).trimEnd('0').trimEnd('.')
    return buildString {
        s.forEach { append(if (it.isDigit()) faDigits[it - '0'] else FA_DECIMAL_SEPARATOR) }
    }
}

/** حالتِ حریمِ خصوصی: همیشه پنج نقطه، مستقل از تعدادِ رقم. */
const val MASKED_AMOUNT = "•••••"

fun Long.toFaMoneyMasked(hidden: Boolean): String =
    if (hidden) MASKED_AMOUNT else toFaMoney()

// ─── تاریخ و زمان ─────────────────────────────────────────────────────────────

private val faMonths = arrayOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
)

/** ماهِ ۱..۱۲ → نامِ ماه. ورودیِ بیرونِ بازه استثنا می‌دهد، چون یعنی باگِ تقویم. */
fun faMonthName(month: Int): String = faMonths[month - 1]

/** ۱۴۰۵/۶/۱۰ → «۱۰ شهریور ۱۴۰۵» — تاریخِ کاملِ سرصفحه‌ها. */
fun toFaDate(year: Int, month: Int, day: Int): String =
    "${day.toFa()} ${faMonthName(month)} ${year.toFa()}"

/** ۱۴۰۵/۶/۱۰ → «۱۰ شهریور» — ردیفِ تراکنش، جایی که سال معلوم است. */
fun toFaDateShort(month: Int, day: Int): String = "${day.toFa()} ${faMonthName(month)}"

/** ۱۴۰۵/۶/۱۰ → «۱۴۰۵/۰۶/۱۰» — فقط جدول و فیلترِ بازه، جایی که ستون باید بچینَد. */
fun toFaDateNumeric(year: Int, month: Int, day: Int): String =
    "${year.toFa()}/${month.toString().padStart(2, '0').faDigits()}/${day.toString().padStart(2, '0').faDigits()}"

/** ۹:۵ → «۰۹:۰۵» — همیشه دو رقم، همیشه ۲۴ ساعته، هیچ‌وقت ق.ظ/ب.ظ. */
fun toFaTime(hour: Int, minute: Int): String =
    "${hour.toString().padStart(2, '0').faDigits()}:${minute.toString().padStart(2, '0').faDigits()}"

/**
 * فاصله‌ی زمانی برای ردیفِ صندوقِ پیام و «آخرین همگام‌سازی».
 * زیرِ یک دقیقه «همین حالا»؛ تا ۲۴ ساعت ساعت‌شمار؛ تا ۷ روز روزشمار؛
 * بعد از آن تاریخِ کوتاه — چون «۴۳ روز پیش» را کسی نمی‌شمارد.
 */
fun faRelativeTime(minutesAgo: Long): String = when {
    minutesAgo < 1 -> "همین حالا"
    minutesAgo < 60 -> "${minutesAgo.toFa()} دقیقه پیش"
    minutesAgo < 60 * 24 -> "${(minutesAgo / 60).toFa()} ساعت پیش"
    minutesAgo < 60 * 24 * 7 -> "${(minutesAgo / (60 * 24)).toFa()} روز پیش"
    else -> ""
}

/** «۳ ماه» / «۱ سال و ۶ ماه» — مدتِ وام و قسط. صفرِ ماه چاپ نمی‌شود. */
fun faDuration(months: Int): String {
    val y = months / 12
    val m = months % 12
    return when {
        y == 0 -> "${m.toFa()} ماه"
        m == 0 -> "${y.toFa()} سال"
        else -> "${y.toFa()} سال و ${m.toFa()} ماه"
    }
}

/** «قسطِ ۷ از ۲۴» — شمارنده‌ی قسط. */
fun faOfTotal(index: Int, total: Int): String = "${index.toFa()} از ${total.toFa()}"

// ─── شماره‌های بلند ───────────────────────────────────────────────────────────

/** ۰۹۱۲۳۴۵۶۷۸۹ → «۰۹۱۲ ۳۴۵ ۶۷۸۹» — گروهِ ۴/۳/۴، فاصله نه خط‌تیره. */
fun String.toFaPhone(): String {
    val d = latinDigits().filter { it.isDigit() }
    if (d.length != 11) return faDigits()
    return "${d.substring(0, 4)} ${d.substring(4, 7)} ${d.substring(7)}".faDigits()
}

/** چهار رقمِ آخرِ کارت → «•••• ۶۲۷۴». شماره‌ی کاملِ کارت را هیچ‌جا نشان نمی‌دهیم. */
fun faCardTail(last4: String): String = "•••• ${last4.faDigits()}"

// ─── واحدِ پول ─────────────────────────────────────────────────────────

/**
 * تومانِ ورودیِ کاربر → ریالِ دیتابیس.
 *
 * ستونِ مبلغ در دیتابیس **ریال** است ولی همه‌ی برنامه تومان نشان می‌دهد، پس
 * تبدیل فقط در دو لبه اتفاق می‌افتد: اینجا (پیشِ ذخیره) و `rialToToman`
 * (پرکردنِ فیلد). هیچ محاسبه‌ی وسطی تبدیل نمی‌کند — نه جمعِ دسته، نه سود، نه
 * قیمتِ واحد. اگر جایی وسطِ زنجیره ضربِ ده دیدید، باگ است.
 *
 * سه فیلدِ ورودی: خرید/فروشِ دارایی، موجودیِ اولیه‌ی حساب، مبلغِ تراکنش.
 */
fun tomanToRial(toman: Long): Long = toman * 10

/** ریالِ دیتابیس → تومانِ فیلد. تقسیمِ صحیح؛ ریالِ باقی‌مانده دور می‌رود. */
fun rialToToman(rial: Long): Long = rial / 10

// ─── فیلدهای ورودی ────────────────────────────────────────────────────────────

object JibakNumerals {

    /**
     * فیلدِ ورودی: مقدارِ زیرین لاتینِ خام می‌ماند (پارس و اعتبارسنجی سالم بماند)،
     * ولی کاربر رقمِ فارسی می‌بیند. نقشه‌ی جای‌نما یک‌به‌یک است چون هر رقم یک رقم
     * می‌شود — بی جداکننده. برای فیلدِ مبلغ AmountVisualTransformation را بگیرید.
     */
    val Transformation = VisualTransformation { text ->
        TransformedText(AnnotatedString(text.text.faDigits()), OffsetMapping.Identity)
    }

    /**
     * فیلدِ مبلغ: هم فارسی می‌کند هم سه‌رقم‌سه‌رقم جدا می‌کند. جداکننده طولِ رشته را
     * عوض می‌کند، پس نقشه‌ی جای‌نما باید بشمارد — وگرنه مکان‌نما می‌پرد.
     */
    val AmountTransformation = VisualTransformation { text ->
        val raw = text.text.filter { it.isDigit() }
        val out = raw.toLongOrNull()?.toFaMoney() ?: ""
        val sepCount = { n: Int -> if (n <= 0) 0 else (n - 1) / 3 }
        TransformedText(
            AnnotatedString(out),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    val fromEnd = raw.length - offset.coerceIn(0, raw.length)
                    return out.length - (fromEnd + sepCount(fromEnd))
                }

                override fun transformedToOriginal(offset: Int): Int {
                    val slice = out.take(offset.coerceIn(0, out.length))
                    return slice.count { it != FA_GROUP_SEPARATOR }
                }
            }
        )
    }
}

/**
 * قلابِ نجات برای درختی که خودمان نساخته‌ایم (کتابخانه‌ی نمودار، متنِ webview).
 * پیش‌فرض روشن است؛ فقط در صفحه‌ی استثنا (شبا، نسخه، لینک) خاموشش کنید:
 *   CompositionLocalProvider(LocalFaDigits provides false) { … }
 * این آخرین راهِ چاره است نه راهِ اول — تابعِ عددی همیشه بهتر است.
 */
val LocalFaDigits: ProvidableCompositionLocal<Boolean> = compositionLocalOf { true }

@Composable
fun localizedNumerals(text: String): String =
    if (LocalFaDigits.current) text.faDigits() else text
