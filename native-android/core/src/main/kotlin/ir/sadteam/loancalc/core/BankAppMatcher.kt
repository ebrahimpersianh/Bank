package ir.sadteam.loancalc.core

/**
 * **تشخیصِ اپ‌های بانکی/پرداختی** از بینِ همه‌ی اپ‌های نصب‌شده‌ی گوشی.
 *
 * چرا لازم شد (گزارشِ صریحِ کاربر): صفحه‌ی «اعلانِ کدوم اپ‌ها خونده بشه؟» **همه‌ی** اپ‌های
 * گوشی رو الفبایی نشون می‌داد - دوربین، قطب‌نما، رادیو، سیم‌کارت، ضبطِ صفحه… در حالی که
 * «اپ‌هایی که واقعاً پیامِ تراکنش می‌دن خیلی کم‌ان». نتیجه: اپِ بانکیِ واقعی (بلوبانک) لای
 * ده‌ها اپِ بی‌ربط گم می‌شد و کاربر باید کلی اسکرول می‌کرد.
 *
 * ⚠️ **عمداً بر پایه‌ی کلیدواژه‌ست، نه لیستِ ثابتِ بسته‌نام.** بسته‌نامِ دقیقِ اپ‌های بانکیِ
 * ایرانی رو نمی‌شه از اینجا تایید کرد (نه دسترسی به کافه‌بازار هست نه گوشیِ واقعی)، و
 * حدس‌زدنشون همون اشتباهیه که تو حادثه‌ی لوگوی بانک‌ها درسش گرفته شد. کلیدواژه‌ی اسمِ اپ
 * چیزیه که خودِ کاربر هم رو صفحه می‌بینه، پس قابلِ‌راستی‌آزماییه.
 *
 * ⚠️ این فقط **ترتیب** رو عوض می‌کنه، نه دسترسی رو: اپِ تشخیص‌داده‌نشده هم هنوز تو لیستِ
 * کاملِ پایینِ صفحه هست و کاربر می‌تونه انتخابش کنه. هیچ اپی مخفی نمی‌شه - برای همینه که
 * چند موردِ مثبتِ کاذب اینجا بی‌ضرره، ولی جاافتادنِ یه بانکِ واقعی نه.
 */
object BankAppMatcher {
    /**
     * کلیدواژه‌های **بلند** که هرجای اسمِ اپ باشن قابلِ‌اتکان.
     * («پرداخت» تو «آسان پرداخت»، «bank» تو «Blu Bank»…)
     */
    private val LABEL_CONTAINS = listOf(
        "بانک", "bank", "بلوبانک", "نئوبانک", "neobank",
        "پرداخت", "payment", "کیف پول", "wallet", "همراه",
        "زرین", "zarin", "شاپرک", "shaparak", "سداد", "sadad", "fintech",
        // نامِ خودِ بانک‌ها - اپِ خیلی‌هاشون کلمه‌ی «بانک» رو تو اسمِ نمایشی نداره
        // («همراه من»، «مهر ایرانیان»، «آینده‌نگر»…) و بدونِ این‌ها اصلاً تشخیص داده نمی‌شدن.
        "ملی", "ملت", "صادرات", "تجارت", "سپه", "مسکن", "کشاورزی", "رفاه", "پست",
        "پارسیان", "پاسارگاد", "سامان", "شهر", "سینا", "آینده", "اقتصاد", "نوین",
        "رسالت", "گردشگری", "کارآفرین", "ایران زمین", "خاورمیانه", "قوامین",
        "مهر ایران", "توسعه", "صنعت و معدن", "قرض الحسنه",
        // کیفِ پول/پرداختِ رایجِ ایرانی
        // ⚠️ «آپ»/«تاپ»/«بلو»/«دی» عمداً این‌جا نیستن و تو LABEL_EXACT موندن - با contains
        // «آپارات»، «لپ‌تاپ» و «ویدیو» هم بانکی اعلام می‌شدن (رجوع کن به توضیحِ LABEL_EXACT).
        "دیجی پی", "digipay", "اسنپ پی", "snapp pay", "ایوا",
        "جیبیت", "jibit", "زیبال", "zibal", "ترب پی", "torob pay",
    )

    /**
     * اسم‌های **کوتاه** که فقط با تطبیقِ کاملِ اسمِ اپ پذیرفته می‌شن، نه `contains`.
     *
     * ⚠️ دلیلش یه موردِ واقعیه: «آپ» (آسان‌پرداخت) اگه با `contains` چک بشه، **آپارات** رو
     * هم می‌گیره. «تاپ» هم «لپ‌تاپ» رو. اسمِ کوتاه باید دقیق باشه.
     */
    private val LABEL_EXACT = listOf(
        "آپ", "تاپ", "بام", "بلو", "blu", "بله", "ایوا", "eva", "sep", "همراه بام",
    )

    /**
     * کلیدواژه‌ی **بسته‌نام** - فقط اسم‌های مشخصِ بانکی/پرداختی.
     * ⚠️ عمداً هیچ تکه‌ی دوسه‌حرفی («ap», "top", "bam") اینجا نیست: `contains` روی
     * بسته‌نام با تکه‌ی کوتاه، نصفِ اپ‌های گوشی رو «بانکی» اعلام می‌کنه.
     */
    private val PACKAGE_KEYWORDS = listOf(
        "bank", "wallet", "sadad", "shaparak", "neobank", "fintech", "payment",
        "melli", "mellat", "saderat", "tejarat", "sepah", "maskan", "keshavarzi",
        "refah", "postbank", "parsian", "pasargad", "saman", "shahr", "sina",
        "ayandeh", "eghtesad", "novin", "resalat", "gardeshgari", "karafarin",
        "blubank", "asanpardakht", "digipay", "snapppay", "zarinpal",
        "hamrahbank", "mobilebank", "internetbank", "ebank", "baman", "bpm",
        "iranzamin", "khavarmiane", "ghavamin", "mehreqtesad", "day", "ansarbank",
        "jibit", "zibal", "torobpay", "azkivam",
    )

    /**
     * اپ‌های سیستمی هیچ‌وقت پیشنهاد نمی‌شن، حتی اگه اسمشون تصادفی کلیدواژه داشته باشه
     * (مثلِ «Google Play Store» که تو بعضی تطبیق‌ها گیر می‌کنه).
     */
    private val SYSTEM_PREFIXES = listOf(
        "com.android.", "com.google.android.", "com.miui.", "com.xiaomi.",
        "com.mi.", "android.", "com.qualcomm.", "com.mediatek.",
    )

    /** آیا این اپ به‌نظر بانکی/پرداختیه؟ */
    fun looksLikeBankApp(label: String, packageName: String): Boolean {
        val pkg = packageName.lowercase()
        if (SYSTEM_PREFIXES.any { pkg.startsWith(it) }) return false

        val name = normalizePersianName(label)
        if (LABEL_CONTAINS.any { name.contains(normalizePersianName(it)) }) return true
        if (LABEL_EXACT.any { name == normalizePersianName(it) }) return true
        return PACKAGE_KEYWORDS.any { pkg.contains(it) }
    }

    /**
     * لیستِ اپ‌ها رو به (پیشنهادی، بقیه) می‌شکنه - ترتیبِ داخلیِ هر دسته دست‌نخورده می‌مونه.
     *
     * @param apps جفتِ (بسته‌نام، اسمِ نمایشی)
     */
    fun split(
        apps: List<Pair<String, String>>,
    ): Pair<List<Pair<String, String>>, List<Pair<String, String>>> =
        apps.partition { (pkg, label) -> looksLikeBankApp(label, pkg) }
}
