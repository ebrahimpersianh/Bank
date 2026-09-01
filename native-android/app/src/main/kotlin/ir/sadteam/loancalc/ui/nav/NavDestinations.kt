package ir.sadteam.loancalc.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * **مخزنِ مقصدهای نوارِ پایین** - بخشِ ۴۱ فایلِ طراحی.
 *
 * قاعده‌ی صریحِ `41c` برای عضویت تو مخزن: **هیچ مقصدی نباید تنها راهش نوار باشد.** هر هفت
 * موردِ زیر راهِ دومی هم دارن (کشوی میان‌بُر، کاشیِ «سررسید»، یا هدرِ خانه)، پس برداشتنشون
 * از نوار هیچ‌جا رو غیرقابلِ‌دسترس نمی‌کنه.
 *
 * ⚠️ **فاصله‌ی آگاهانه با طرح**: فریمِ `41b` از «۱۴ مقصد» حرف می‌زنه و «هدف»/«تقویم»/«پیام‌ها»/
 * «سکه‌ها» رو هم می‌شمره. اون‌ها تو `NavHost`ِ فعلی **مقصدِ ناوبری نیستن** (پیام‌ها و تنظیمات
 * پوششِ روی صفحه‌ان، هدفِ پس‌انداز اصلاً هنوز ساخته نشده). مخزن عمداً فقط مقصدهای واقعیه؛
 * با اضافه‌شدنِ هر route جدید، فقط یه ردیف به همین enum اضافه می‌شه و بقیه‌ی سیستم دست‌نخورده
 * کار می‌کنه.
 */
enum class NavDestination(
    val id: String,
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    HOME("home", "home", "خانه", Icons.Outlined.Home, Icons.Filled.Home),
    ASSETS("assets", "assets", "دارایی", Icons.Outlined.AccountBalanceWallet, Icons.Filled.AccountBalanceWallet),
    REPORT("report", "report", "گزارش", Icons.Outlined.BarChart, Icons.Filled.BarChart),
    BUDGET("budget", "budget", "بودجه", Icons.Outlined.Savings, Icons.Filled.Savings),
    DUE("due", "due", "سررسید", Icons.Outlined.EventNote, Icons.Filled.EventNote),
    LOAN("loan", "loan", "وام", Icons.Outlined.Payments, Icons.Filled.Payments),
    CHEQUE("cheque", "cheque", "چک", Icons.Outlined.Description, Icons.Filled.Description);

    companion object {
        /** نوارِ پیش‌فرض - همون پنج تبِ همیشگی. اسلاتِ ۰ (راست‌ترین تو RTL) خانه‌ست. */
        val DEFAULT_SLOTS = listOf(HOME.id, ASSETS.id, REPORT.id, BUDGET.id, DUE.id)

        const val SLOT_COUNT = 5

        fun byId(id: String): NavDestination? = entries.firstOrNull { it.id == id }

        fun byRoute(route: String?): NavDestination? = entries.firstOrNull { it.route == route }

        /**
         * شناسه‌های ذخیره‌شده رو به یه نوارِ همیشه‌معتبر تبدیل می‌کنه.
         *
         * شناسه‌ی ناشناخته (مقصدی که تو نسخه‌ی بعدی حذف شده) دور ریخته می‌شه و جای خالی از
         * نوارِ پیش‌فرض پر می‌شه، پس نوار هیچ‌وقت کوتاه‌تر از پنج خانه نمی‌شه.
         *
         * **اسلاتِ ۰ همیشه خانه‌ست** - قفلِ `41c` اینجا اعمال می‌شه، نه فقط تو UI، تا هیچ
         * مسیرِ دیگه‌ای (بکاپِ خراب، مهاجرت) نتونه نوارِ بی‌خانه بسازه.
         */
        fun sanitize(ids: List<String>): List<NavDestination> {
            val valid = ids.mapNotNull { byId(it) }.distinct().toMutableList()
            valid.remove(HOME)
            valid.add(0, HOME)
            DEFAULT_SLOTS.mapNotNull { byId(it) }.forEach { fallback ->
                if (valid.size >= SLOT_COUNT) return@forEach
                if (fallback !in valid) valid.add(fallback)
            }
            return valid.take(SLOT_COUNT)
        }
    }
}
