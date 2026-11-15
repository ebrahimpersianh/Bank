package ir.sadteam.loancalc.ui.subscription

import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate

/** نامِ تجاریِ اشتراکِ اپ - یه‌جا تعریف شده تا اگه عوض شد همه‌جا با هم عوض بشه. */
const val PLUS_NAME = "جیبک پلاس"

/**
 * تاریخِ انقضای اشتراک به‌صورتِ شمسی + تعدادِ روزِ باقی‌مونده.
 *
 * `subscribedUntil` که از سرور میاد یه رشته‌ی ISO-8601ه (مثلِ `2026-09-04T12:00:00Z`) - رجوع کن به
 * `SubscriptionStatus.isSubscribed` سمتِ سرور که با `Instant.parse` می‌خونتش. اینجا عمداً بدونِ
 * `java.time` پارس می‌شه (فقط بخشِ تاریخِ ابتدای رشته) تا نیازی به desugaring و بالابردنِ minSdk
 * نباشه؛ ساعت برای نمایشِ «تا فلان روز» اهمیتی نداره.
 *
 * اگه رشته خالی/خراب باشه `null` برمی‌گرده و صفحه به‌جای عددِ اشتباه، هیچی نشون نمی‌ده.
 */
data class SubscriptionExpiry(val date: PersianDate, val daysLeft: Int)

/** تاریخِ میلادیِ سرور (چه ISO-8601 چه `yyyy-MM-dd HH:mm:ss`ِ SQLite) → شمسی. فقط ۱۰ کاراکترِ اولِ
 * رشته خونده می‌شه، پس هر دو فرمت کار می‌کنن. */
fun parseServerDate(raw: String?): PersianDate? {
    if (raw.isNullOrBlank()) return null
    val parts = raw.take(10).split("-")
    if (parts.size != 3) return null
    val gy = parts[0].toIntOrNull() ?: return null
    val gm = parts[1].toIntOrNull() ?: return null
    val gd = parts[2].toIntOrNull() ?: return null
    if (gm !in 1..12 || gd !in 1..31) return null
    return runCatching { JalaliCalendar.fromGregorian(gy, gm, gd) }.getOrNull()
}

/** نامِ فارسیِ پلن از روی کدِ کوتاهِ سرور ("1m"/"3m"/"6m"/"1y") - رجوع کن به PRODUCT_TIER_CODE
 * تو server/.../SubscriptionRoutes.kt. */
fun tierDisplayName(tier: String?): String = when (tier) {
    "1m" -> "اشتراک یک‌ماهه"
    "3m" -> "اشتراک سه‌ماهه"
    "6m" -> "اشتراک شش‌ماهه"
    "1y" -> "اشتراک یک‌ساله"
    else -> "اشتراک"
}

fun parseSubscribedUntil(raw: String?): SubscriptionExpiry? {
    if (raw.isNullOrBlank()) return null
    val datePart = raw.take(10) // yyyy-MM-dd
    val parts = datePart.split("-")
    if (parts.size != 3) return null
    val gy = parts[0].toIntOrNull() ?: return null
    val gm = parts[1].toIntOrNull() ?: return null
    val gd = parts[2].toIntOrNull() ?: return null
    if (gm !in 1..12 || gd !in 1..31) return null

    val persian = runCatching { JalaliCalendar.fromGregorian(gy, gm, gd) }.getOrNull() ?: return null
    val daysLeft = runCatching { JalaliCalendar.daysBetween(JalaliCalendar.today(), persian) }.getOrNull() ?: return null
    return SubscriptionExpiry(date = persian, daysLeft = daysLeft.coerceAtLeast(0))
}
