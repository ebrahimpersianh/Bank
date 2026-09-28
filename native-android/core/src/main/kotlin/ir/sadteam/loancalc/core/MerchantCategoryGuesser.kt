package ir.sadteam.loancalc.core

/**
 * حدسِ دسته‌بندیِ تراکنشِ خودکار (پیامک/اعلانِ بانکی) از رو کلیدواژه‌ی متنِ پیامک.
 *
 * تصمیمِ صریحِ کاربر (بندِ ۲.۲): تراکنشِ خودکار قبلاً همیشه می‌رفت تو دسته‌ی کورِ «سایر
 * هزینه»/«سایر درآمد» - هیچ حدسی نمی‌زد. حالا اگه اسمِ فروشگاه/سرویس تو متنِ پیامک با یکی از
 * کلیدواژه‌های زیر بخونه، همون دسته برگردونده می‌شه؛ وگرنه `null` (یعنی نامطمئن) و تماسِ‌کننده
 * (`BankSmsReceiver`/`BankNotificationListener`) همون دسته‌ی کورِ قبلی رو می‌ذاره و تو پیامِ
 * اعلان صریح می‌گه که کاربر خودش باید دسته‌بندی کنه.
 *
 * ⚠️ **این کلاس اسمِ دسته‌ها رو عیناً از `Category.kt`ِ ماژولِ `:app` کپی کرده** (نه ایمپورت -
 * `:core` نباید به Compose وابسته بشه). اگه اسمِ یه دسته اونجا عوض شد، اینجا هم باید عوض بشه.
 *
 * ترتیب مهمه: **اولین کلیدواژه‌ای که تو متن پیدا بشه** دسته رو تعیین می‌کنه، پس کلیدواژه‌های
 * خاص‌تر (مثلاً «اسنپ‌فود») باید قبل از کلیدواژه‌ی عمومی‌ترشون («اسنپ») بیان.
 */
object MerchantCategoryGuesser {

    private data class Keyword(val keyword: String, val category: String)

    private val withdrawalKeywords: List<Keyword> = listOf(
        // خوراک - قبل از «اسنپ»ِ عمومی (که رفت‌وآمده) چک می‌شه
        Keyword("اسنپ فود", "خوراک"),
        Keyword("اسنپ‌فود", "خوراک"),
        Keyword("فودو", "خوراک"),
        Keyword("رستوران", "خوراک"),
        Keyword("کافه", "خوراک"),
        Keyword("فست فود", "خوراک"),
        Keyword("فست‌فود", "خوراک"),
        Keyword("پیتزا", "خوراک"),
        Keyword("کبابی", "خوراک"),
        Keyword("سوپرمارکت", "خوراک"),
        Keyword("سوپر مارکت", "خوراک"),
        // رفت‌وآمد
        Keyword("تپسی", "رفت‌وآمد"),
        Keyword("اسنپ", "رفت‌وآمد"),
        Keyword("پمپ بنزین", "رفت‌وآمد"),
        Keyword("بنزین", "رفت‌وآمد"),
        Keyword("پارکینگ", "رفت‌وآمد"),
        Keyword("مترو", "رفت‌وآمد"),
        Keyword("تاکسی", "رفت‌وآمد"),
        Keyword("عوارض آزادراه", "رفت‌وآمد"),
        // خرید
        Keyword("دیجی‌کالا", "خرید"),
        Keyword("دیجی کالا", "خرید"),
        Keyword("ترب", "خرید"),
        Keyword("باسلام", "خرید"),
        Keyword("فروشگاه", "خرید"),
        // قبض
        Keyword("قبض", "قبض"),
        Keyword("همراه اول", "قبض"),
        Keyword("ایرانسل", "قبض"),
        Keyword("رایتل", "قبض"),
        Keyword("شارژ", "قبض"),
        Keyword("آب و فاضلاب", "قبض"),
        Keyword("برق منطقه", "قبض"),
        Keyword("شرکت گاز", "قبض"),
        // سلامت
        Keyword("داروخانه", "سلامت"),
        Keyword("بیمارستان", "سلامت"),
        Keyword("کلینیک", "سلامت"),
        Keyword("آزمایشگاه", "سلامت"),
        // تفریح
        Keyword("سینما", "تفریح"),
        Keyword("کنسرت", "تفریح"),
        Keyword("تیوال", "تفریح"),
    )

    private val depositKeywords: List<Keyword> = listOf(
        Keyword("حقوق", "حقوق"),
        Keyword("تامین اجتماعی", "حقوق"),
        Keyword("دیوار", "فروش"),
        Keyword("شیپور", "فروش"),
        Keyword("سود سپرده", "سودِ سرمایه‌گذاری"),
        Keyword("سودِ سپرده", "سودِ سرمایه‌گذاری"),
    )

    /** دسته‌ی حدس‌زده‌شده، یا `null` اگه هیچ کلیدواژه‌ای تو متن نبود (نامطمئن). */
    fun guess(text: String, isWithdrawal: Boolean): String? {
        val keywords = if (isWithdrawal) withdrawalKeywords else depositKeywords
        return keywords.firstOrNull { text.contains(it.keyword, ignoreCase = true) }?.category
    }
}
