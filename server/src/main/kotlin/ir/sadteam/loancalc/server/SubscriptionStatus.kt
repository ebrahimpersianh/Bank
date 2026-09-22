package ir.sadteam.loancalc.server

import java.time.Instant

/* دوره‌ی آزمایشی رایگان: ۷ روز از لحظه‌ی اولین ورودِ موفق با شماره موبایل (created_at رو جدول
   users، که فقط همون یه‌بار موقع ساختِ ردیف ست می‌شه و هیچ‌جای دیگه‌ای دست‌کاری نمی‌شه) - یعنی
   کاملاً سمت سرور و کلید‌خورده به شماره تلفنه، نه به گوشی/نصب. خروج از حساب، پاک‌کردن داده‌ی اپ،
   حذف و نصب دوباره، یا حتی نصب رو گوشی دیگه هیچ‌کدوم دوره‌ی آزمایشی رو ریست نمی‌کنن، چون فقط با
   همون شماره موبایل دوباره وارد شدن، همون ردیف قدیمیِ users (و همون created_at قدیمی) پیدا می‌شه. */
/** دوره‌ی هدیه‌ی پایه برای هر کاربرِ جدید. ۲۰ مرداد ۱۴۰۵ به خواستِ صریحِ کاربر از ۷ به ۳۰ روز
 * تغییر کرد. کاربرانی که *قبل از این تغییر* تو سرور بودن، ۱۵ روزِ اضافه (جمعاً ۴۵) گرفتن -
 * رجوع کن به grantLegacyGift تو Db.kt. */
private const val TRIAL_DAYS = 30
private const val TRIAL_MS = TRIAL_DAYS * 24L * 60 * 60 * 1000

/* ستون‌های TEXT تاریخ تو sqlite با datetime('now') به‌صورت UTC ولی با فاصله (نه 'T') و بدون
   پسوند Z ذخیره می‌شن؛ برای پارس درست با java.time.Instant باید فاصله رو به 'T' تبدیل و Z رو
   اضافه کنیم، وگرنه Instant.parse با DateTimeParseException می‌ترکه. */
fun parseUtc(sqliteDatetime: String): Long =
    Instant.parse(sqliteDatetime.trim().replace(' ', 'T') + "Z").toEpochMilli()

fun trialEndsAtMs(createdAt: String?): Long? {
    if (createdAt == null) return null
    return parseUtc(createdAt) + TRIAL_MS
}

/** پورت «چند روز از دوره‌ی آزمایشی مونده» - قبلاً کلاینت این رو خودش از رو trialEndsAtMs (یه
 * timestamp خام) با ساعتِ خودِ گوشی حساب می‌کرد؛ اگه کاربر تاریخِ گوشیش رو دستکاری می‌کرد (این
 * تاثیری رو خودِ isSubscribed نداشت چون اون همیشه سمت سرور با System.currentTimeMillis حساب
 * می‌شه، ولی) این عددِ نمایشی می‌تونست اشتباه نشون داده بشه. حالا خودِ عددِ نهایی (نه timestamp خام)
 * اینجا با ساعتِ سرور حساب و مستقیم به کلاینت داده می‌شه، پس دیگه به ساعتِ گوشی هیچ وابستگی‌ای نداره.
 * null یعنی یا هنوز مشترک نشده یا دوره‌ی آزمایشی تموم شده. */
fun trialDaysLeft(createdAt: String?): Int? {
    val end = trialEndsAtMs(createdAt) ?: return null
    val remainingMs = end - System.currentTimeMillis()
    if (remainingMs <= 0) return null
    return (remainingMs / (24 * 60 * 60 * 1000)).toInt() + 1
}

/* یه کاربر مشترکه (یا هنوز تو دوره‌ی آزمایشی رایگانه) اگه:
   - دستی (subscribed=1، برای پشتیبانی/تست) فعال شده باشه، یا
   - یکی از پلن‌های زمان‌دار (subscribed_until) هنوز منقضی نشده باشه، یا
   - هنوز تو ۷ روز اولِ بعد از اولین ثبت‌نامش باشه. */
fun isSubscribed(user: UserRow?): Boolean {
    if (user == null) return false
    if (user.subscribed) return true
    val until = user.subscribedUntil
    if (until != null) {
        val untilMs = runCatching { Instant.parse(until).toEpochMilli() }.getOrNull()
        if (untilMs != null && untilMs > System.currentTimeMillis()) return true
    }
    val trialEnds = trialEndsAtMs(user.createdAt)
    if (trialEnds != null && trialEnds > System.currentTimeMillis()) return true
    return false
}

/** باگِ رفع‌شده: [trialDaysLeft] قبلاً بی‌قید و شرط فقط از رو created_at حساب می‌شد و به کلاینت
 * می‌رفت - یعنی حتی یه حسابِ **دائمیِ** دستی (subscribed=1، مثلِ حسابِ تست/شخصیِ توسعه‌دهنده) که
 * تصادفاً تو ۷ روزِ اولِ ثبت‌نامش بود، همچنان بجِ گمراه‌کننده‌ی «دوره‌ی آزمایشی: X روز مانده» می‌گرفت
 * (گزارشِ کاربر: «حساب دائمی درست نیست، بازم اشتباه می‌زنه»). این تابع فقط وقتی trialDaysLeft
 * واقعاً *دلیلِ* مشترک‌بودنه (نه یه اشتراکِ دستی یا خریدِ واقعیِ زمان‌دار) عددی برمی‌گردونه؛ کلاینت
 * (SettingsScreen) از رو null-بودنش بجِ آزمایشی رو نشون نمی‌ده. */
fun trialDaysLeftIfApplicable(user: UserRow): Int? {
    if (user.subscribed) return null
    val until = user.subscribedUntil
    if (until != null) {
        val untilMs = runCatching { Instant.parse(until).toEpochMilli() }.getOrNull()
        if (untilMs != null && untilMs > System.currentTimeMillis()) return null
    }
    return trialDaysLeft(user.createdAt)
}
