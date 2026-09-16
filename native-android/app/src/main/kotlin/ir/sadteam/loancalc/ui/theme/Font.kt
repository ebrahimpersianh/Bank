package ir.sadteam.loancalc.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import ir.sadteam.loancalc.R

val VazirmatnFontFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
    // ⚠️ **۸۰۰ و ۹۰۰ نبودن و همین باعثِ «فونت فرق می‌کنه» شد.** سیستمِ طراحی صریحاً چهار وزنِ
    // ۵۰۰/۷۰۰/۸۰۰/۹۰۰ می‌خواد و تقریباً هر تیتر و عددِ قهرمانی وزنِ ۹۰۰ داره. بدونِ فایلِ
    // واقعی، اندروید از رو Bold یه «چاقِ مصنوعی» می‌سازه که حروفِ فارسی رو پخش و بدترکیب
    // نشون می‌ده. این دو فایل از مخزنِ رسمیِ Vazirmatn اومدن.
    Font(R.font.vazirmatn_extrabold, FontWeight.ExtraBold),
    Font(R.font.vazirmatn_black, FontWeight.Black),
)

/**
 * قلم‌های خریدنیِ فروشگاه (دستهٔ «قلم»).
 *
 * 🚨 چرا `mutableStateOf`ِ سراسری و نه `CompositionLocal`: دقیقاً همان دلیلِ
 * `SymbolTheme` در بخشِ ۷۲ - `AppTypography` یک `val`ِ سطحِ فایل است و ساخته‌شدنش
 * composable نیست. تنها نقطه‌ی **نوشتن** `ThemeViewModel.init` است.
 *
 * ⚠️ وزن‌ها: فقط وزیرمتن شش وزنِ واقعی دارد. بقیه تک‌وزن‌اند و اندروید وزنِ سنگین را
 * **مصنوعی** می‌سازد؛ روی حروفِ فارسی این پخش و بدترکیب می‌شود (همان درسی که یک‌بار
 * برای خودِ وزیرمتن گرفتیم). پس قلمِ تک‌وزن فقط برای کسی است که خودش انتخابش کرده،
 * و پیش‌فرض هیچ‌وقت عوض نمی‌شود.
 */
enum class AppFontChoice(val id: String, val label: String, val family: FontFamily) {
    VAZIRMATN("font:vazirmatn", "وزیرمتن", VazirmatnFontFamily),
    NASKH("font:naskh", "نسخِ عربی", FontFamily(Font(R.font.noto_naskh_arabic, FontWeight.Normal))),
    MARKAZI("font:markazi", "مرکزی", FontFamily(Font(R.font.markazi_text, FontWeight.Normal))),
    LALEZAR("font:lalezar", "لاله‌زار", FontFamily(Font(R.font.lalezar_regular, FontWeight.Normal))),
    ;

    companion object {
        /** شناسه‌ی ناشناس یا `null` → وزیرمتن. قلمِ خوانده‌نشده نباید برنامه را بی‌متن کند. */
        fun fromId(id: String?): AppFontChoice = entries.firstOrNull { it.id == id } ?: VAZIRMATN
    }
}

/** قلمِ فعال. فقط `ThemeViewModel` می‌نویسد؛ `Theme.kt` می‌خوانَد. */
object AppFontState {
    var choice: AppFontChoice by mutableStateOf(AppFontChoice.VAZIRMATN)
}
