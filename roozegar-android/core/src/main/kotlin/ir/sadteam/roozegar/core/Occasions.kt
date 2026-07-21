package ir.sadteam.roozegar.core

/** نوع افکت تمام‌صفحه‌ی روزهای خاص (موتور افکت data-driven - رجوع کن به ui/effects تو ماژول app). */
enum class EffectType { NONE, BLOSSOM, SNOW, STARS, LEAVES }

/**
 * یه مناسبت با تاریخ شمسی ثابت.
 *
 * ⚠️ محدوده‌ی فاز ۱: فقط مناسبت‌ها/تعطیلاتِ **شمسیِ ثابت** (که هر سال همون روزن) + جشن‌های ایران
 * باستان. تعطیلات قمری (عید فطر، تاسوعا/عاشورا و...) که هر سال جابه‌جا می‌شن و لایه‌ی میلادی/جهانی
 * و آپدیت سالانه‌ی از راه دور، همه مال فاز ۲ (لایه‌بندی سه‌گانه‌ی قابل روشن/خاموش طبق پرامپت) هستن -
 * این لیست رو «کامل» فرض نکن.
 */
data class Occasion(
    val month: Int,
    val day: Int,
    val title: String,
    val holiday: Boolean = false,
    val effect: EffectType = EffectType.NONE,
)

object Occasions {
    /** مناسبت‌های شمسیِ ثابت (تعطیلات رسمی ثابت + جشن‌های باستانی + شروع فصل‌ها). */
    val fixedJalali: List<Occasion> = listOf(
        Occasion(1, 1, "نوروز - جشن آغاز سال نو", holiday = true, effect = EffectType.BLOSSOM),
        Occasion(1, 2, "عید نوروز", holiday = true, effect = EffectType.BLOSSOM),
        Occasion(1, 3, "عید نوروز", holiday = true, effect = EffectType.BLOSSOM),
        Occasion(1, 4, "عید نوروز", holiday = true, effect = EffectType.BLOSSOM),
        Occasion(1, 12, "روز جمهوری اسلامی", holiday = true),
        Occasion(1, 13, "روز طبیعت (سیزده‌به‌در)", holiday = true, effect = EffectType.BLOSSOM),
        Occasion(3, 14, "رحلت امام خمینی", holiday = true),
        Occasion(3, 15, "قیام ۱۵ خرداد", holiday = true),
        Occasion(4, 1, "آغاز تابستان", effect = EffectType.STARS),
        Occasion(7, 1, "آغاز پاییز", effect = EffectType.LEAVES),
        Occasion(7, 16, "جشن مهرگان", effect = EffectType.LEAVES),
        Occasion(8, 15, "جشن میانه‌ی پاییز", effect = EffectType.LEAVES),
        Occasion(9, 30, "شب یلدا (شب چله)", effect = EffectType.STARS),
        Occasion(10, 1, "آغاز زمستان", effect = EffectType.SNOW),
        Occasion(10, 15, "جشن میانه‌ی زمستان (چله‌ی کوچک در پیش است)", effect = EffectType.SNOW),
        Occasion(11, 10, "جشن سده", effect = EffectType.STARS),
        Occasion(11, 22, "پیروزی انقلاب اسلامی", holiday = true),
        Occasion(12, 5, "سپندارمذگان - روز عشق ایرانی", effect = EffectType.STARS),
        Occasion(12, 29, "روز ملی شدن صنعت نفت", holiday = true),
    )

    /** همه‌ی مناسبت‌های یه روز مشخص (ممکنه خالی باشه). */
    fun of(date: PersianDate): List<Occasion> =
        fixedJalali.filter { it.month == date.m && it.day == date.d }

    /** آیا این روز تعطیل رسمیه؟ (جمعه‌ها جدا تو UI قرمز می‌شن، این فقط تعطیلات مناسبتیه.) */
    fun isHoliday(date: PersianDate): Boolean = of(date).any { it.holiday }

    /** افکت تمام‌صفحه‌ی این روز (اولین مناسبت افکت‌دار)، یا NONE. */
    fun effectOf(date: PersianDate): EffectType =
        of(date).firstOrNull { it.effect != EffectType.NONE }?.effect ?: EffectType.NONE

    /** عنوان‌های مناسبت‌های روز، جداشده با «،» - یا null اگه مناسبتی نیست (برای نوتیفیکیشن/ویجت). */
    fun titlesOf(date: PersianDate): String? =
        of(date).takeIf { it.isNotEmpty() }?.joinToString("، ") { it.title }
}
