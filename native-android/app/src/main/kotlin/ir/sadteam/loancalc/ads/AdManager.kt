package ir.sadteam.loancalc.ads

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * قرارداد مشترکِ تبلیغات - دقیقاً هم‌الگو با `SubscriptionManager`: هر فلیور پیاده‌سازیِ خودش رو
 * داره (کافه‌بازار ادز رو فلیورِ cafebazaar، مایکت ادز رو فلیورِ myket) و بقیه‌ی اپ فقط همین
 * اینترفیس رو می‌بینه.
 *
 * **دو قاعده‌ی غیرقابلِ‌مذاکره** (خواسته‌ی صریحِ کاربر):
 * 1. **فقط برای کاربرِ بدونِ اشتراک.** مشترک نباید هیچ تبلیغی ببینه و SDK اصلاً براش بارگذاری نشه.
 * 2. **اگه شناسه خالی باشه، هیچی نشون داده نمی‌شه.** کاربر گفته «زیرساخت رو الان بساز، شناسه‌ها
 *    رو به‌موقعش می‌دم» - پس تا اون موقع اپ باید دقیقاً مثلِ الان بی‌تبلیغ کار کنه: نه بنرِ خالی،
 *    نه جای خالی، نه لودینگِ بی‌پایان.
 */
interface AdManager {
    /** شناسه‌ی زونِ بنر؛ خالی یعنی هنوز تنظیم نشده و هیچ بنری نباید ساخته بشه. */
    val bannerZoneId: String

    /** آیا اصلاً می‌شه تبلیغ نشون داد؟ (شناسه تنظیم شده + SDK در دسترس) */
    fun isReady(): Boolean = bannerZoneId.isNotBlank()
}

/** پیاده‌سازیِ خنثی - وقتی هیچ شناسه‌ای تنظیم نشده. */
object NoOpAdManager : AdManager {
    override val bannerZoneId: String = ""
    override fun isReady(): Boolean = false
}

/**
 * `null` یعنی این بیلد اصلاً تبلیغ نداره. عمداً nullable گذاشته شده تا مصرف‌کننده مجبور بشه حالتِ
 * «تبلیغ نداریم» رو صریح مدیریت کنه - همون الگوی [ir.sadteam.loancalc.subscription.LocalSubscriptionManager].
 */
val LocalAdManager = staticCompositionLocalOf<AdManager?> { null }
