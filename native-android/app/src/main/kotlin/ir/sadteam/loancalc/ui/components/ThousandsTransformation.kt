package ir.sadteam.loancalc.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import ir.sadteam.loancalc.core.FA_THOUSANDS_SEPARATOR
import ir.sadteam.loancalc.core.toFa

/**
 * جداکننده‌ی هزارگان برای فیلدهای مبلغ، به‌صورت VisualTransformation - یعنی متنِ واقعیِ داخلِ
 * فیلد همیشه فقط رقمه و کاماها صرفاً «نمایشی»ان، با OffsetMapping درست برای مکان‌نما.
 *
 * چرا: الگوی قبلی (فرمت‌کردن تو onValueChange و برگردوندنِ رشته‌ی کامادار به‌عنوان value) یه باگِ
 * جدیِ گزارش‌شده داشت - وقتی رشته‌ی value با چیزی که کاربر تایپ کرده فرق می‌کنه، Compose جای
 * مکان‌نما رو حدس می‌زنه و بعد از جااُفتادنِ کاما، رقم‌های بعدی وسطِ عدد درج می‌شدن (کاربر
 * ۱۲۷۴۹۰۰۰ می‌زد، ۱۲۷۴۰۰۰۹ ثبت می‌شد!). با VisualTransformation بافرِ ویرایش هیچ‌وقت زیرِ پای
 * مکان‌نما عوض نمی‌شه و این کلاسِ باگ کلاً از بین می‌ره.
 *
 * قرارداد: state فیلد باید «فقط رقمِ لاتین» نگه داره (onValueChange با cleanNum تمیز کنه) و
 * خوندن‌های بعدی مستقیم toLongOrNull/toDoubleOrNull بزنن.
 */
class ThousandsSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = if (digits.isEmpty()) {
            ""
        } else {
            // ارقام و جداکننده هر دو **فارسی** نمایش داده می‌شن؛ متنِ واقعیِ state همچنان
            // رقمِ لاتینِ خامه (قراردادِ بالای همین کلاس). چون هر رقمِ فارسی و خودِ `٬` هم
            // یه کاراکترن، OffsetMapping دست‌نخورده درست می‌مونه.
            toFa(digits).reversed().chunked(3).joinToString(FA_THOUSANDS_SEPARATOR.toString()).reversed()
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, digits.length)
                if (o == 0) return 0
                var separators = 0
                // یه کاما قبل از رقمِ p ام (از چپ) هست وقتی تعدادِ رقم‌های سمتِ راستش مضربِ ۳ باشه.
                for (p in 1 until digits.length) {
                    if (p <= o && (digits.length - p) % 3 == 0) separators++
                }
                return o + separators
            }

            override fun transformedToOriginal(offset: Int): Int {
                val t = offset.coerceIn(0, formatted.length)
                // 🚨 جداکننده `٬`ِ فارسی است نه `,`ِ لاتین (خروجی از `toFa` می‌آید). شمردنِ
                // کاماي لاتین همیشه صفر می‌داد، پس مکان‌نما به‌اندازه‌ی تعدادِ جداکننده‌ها جلو
                // می‌افتاد و ویرایشِ وسطِ عدد رقم را جای غلط درج می‌کرد.
                val commas = formatted.take(t).count { it == FA_THOUSANDS_SEPARATOR }
                return (t - commas).coerceIn(0, digits.length)
            }
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
