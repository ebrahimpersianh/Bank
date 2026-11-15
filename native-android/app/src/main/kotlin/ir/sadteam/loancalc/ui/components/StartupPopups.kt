package ir.sadteam.loancalc.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 🚦 **صفِ پنجره‌های شروع** (۸ مهر، گزارشِ کاربر: «اینترو با پنجره‌ی نسخه‌ی جدید و یک چیزِ دیگر
 * همزمان آمد و قاطی بود»). هر بار فقط یکی، به این ترتیب:
 * ۱) راهنمای اولین ورود ۲) برگه‌ی آپدیت ۳) هشدارِ پایانِ اشتراک ۴) سکه‌ی ورودِ روزانه ۵) امتیاز به برنامه.
 * هر پنجره فقط وقتی نشان داده می‌شود که قبلی‌هایش بسته باشند؛ پنجره‌ی عقب‌افتاده منتظر می‌ماند، گم نمی‌شود.
 */
object StartupPopups {
    /** ۱ و ۲: از MainActivity پر می‌شود. */
    var tourOrUpdate by mutableStateOf(false)
    var expiryOpen by mutableStateOf(false)
    var checkInOpen by mutableStateOf(false)

    val canShowExpiry get() = !tourOrUpdate
    val canShowCheckIn get() = !tourOrUpdate && !expiryOpen
    val canShowRate get() = !tourOrUpdate && !expiryOpen && !checkInOpen
}
