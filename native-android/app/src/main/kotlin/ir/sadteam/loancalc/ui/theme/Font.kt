package ir.sadteam.loancalc.ui.theme

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
