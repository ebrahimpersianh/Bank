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
enum class AppFontChoice(
    val id: String,
    val label: String,
    val family: FontFamily,
    /** کجای برنامه می‌نشیند - رجوع کن به [FontTarget]. */
    val target: FontTarget,
) {
    VAZIRMATN("font:vazirmatn", "وزیرمتن", VazirmatnFontFamily, FontTarget.EVERYWHERE),
    NASKH("font:naskh", "نسخِ عربی", FontFamily(Font(R.font.noto_naskh_arabic, FontWeight.Normal)), FontTarget.BODY),
    MARKAZI("font:markazi", "مرکزی", FontFamily(Font(R.font.markazi_text, FontWeight.Normal)), FontTarget.BODY),
    LALEZAR("font:lalezar", "لاله‌زار", FontFamily(Font(R.font.lalezar_regular, FontWeight.Normal)), FontTarget.TITLE),
    ;

    companion object {
        /** شناسه‌ی ناشناس یا `null` → وزیرمتن. قلمِ خوانده‌نشده نباید برنامه را بی‌متن کند. */
        fun fromId(id: String?): AppFontChoice = entries.firstOrNull { it.id == id } ?: VAZIRMATN
    }
}

/**
 * کجای برنامه قلمِ خریدنی می‌نشیند - جوابِ طراح به سوالِ وزن (دورِ ۱۰، راهِ سوم).
 *
 * قلمِ تک‌وزن روی تیترِ درشت بدترین حالتش را نشان می‌دهد: اندروید وزنِ ۹۰۰ را مصنوعی
 * می‌سازد و پخش‌شدنِ حروفِ فارسی دقیقاً در بزرگ‌ترین اندازه دیده می‌شود. پس:
 *
 * - [BODY] قلم روی **متنِ بدنه** می‌نشیند و تیترها وزیرمتنِ ۹۰۰ می‌مانند. بدنه جایی است
 *   که بیشترِ متنِ برنامه است، پس کاربر تفاوت را می‌بیند بی این‌که افتادگی ببیند.
 * - [TITLE] برعکس، فقط تیتر - مالِ «لاله‌زار» که نمایشی است و روی بدنه صفحه را شبیهِ
 *   پوستر می‌کند. ⚠️ توضیحِ ردیفش در فروشگاه **باید** بگوید «برای تیترها»، وگرنه کاربر
 *   می‌خرد و فکر می‌کند اعمال نشده.
 * - [EVERYWHERE] فقط وزیرمتن، چون تنها قلمی است که شش وزنِ واقعی دارد.
 */
enum class FontTarget { EVERYWHERE, BODY, TITLE }

/** قلمِ فعال. فقط `ThemeViewModel` می‌نویسد؛ `Theme.kt` می‌خوانَد. */
object AppFontState {
    var choice: AppFontChoice by mutableStateOf(AppFontChoice.VAZIRMATN)
}
