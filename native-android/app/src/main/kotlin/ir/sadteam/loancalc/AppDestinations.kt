package ir.sadteam.loancalc

import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ManageSearch
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AddCard
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ir.sadteam.loancalc.ui.CalculatorHostScreen
import ir.sadteam.loancalc.ui.accounting.AssetsScreen
import ir.sadteam.loancalc.ui.accounting.BudgetScreen
import ir.sadteam.loancalc.ui.accounting.ReportScreen
import ir.sadteam.loancalc.ui.components.Shortcut
import ir.sadteam.loancalc.ui.debt.DebtScreen
import ir.sadteam.loancalc.ui.nav.NavDestination
import ir.sadteam.loancalc.ui.profile.ShortcutViewModel
import ir.sadteam.loancalc.ui.theme.Motion

// آیکون‌های نوار پایین: حالت عادی outline (مینیمال، مثل نسخه‌ی وب)، تب فعال پُر (filled).
//
// بازطراحیِ تب‌بندی (فازِ اولِ بازسازیِ جامع، رجوع کن به CLAUDE.md): قبلاً ۴ تبِ جدا (وام بانکی/
// محاسبه‌گر/سود سپرده/وام‌های من) + حسابداری بودن. الان زیرِ یه تبِ واحدِ «وام» ادغام شدن (رجوع کن
// به [LoanTab]/[LoanSubTab])، «چک» که قبلاً فقط زیرمجموعه‌ی تبِ وام بانکی/تنظیمات بود ترفیع گرفته
// به تبِ مستقل، و دو تبِ کاملاً جدید («خانه»، «سررسید») اضافه شدن.
//
// دورِ دومِ بازطراحی (خواسته‌ی صریحِ کاربر: «تب‌های پایین دقیقاً مثل اون برنامه [رفرنس] باشه») - نوارِ
// پایین دیگه «وام»/«چک»/«حسابداری» نداره؛ به‌جاش «دارایی»/«گزارش»/«بودجه» (دقیقاً هم‌الگو با
// رفرنس). «وام» و «چک» دیگه تبِ بالانوارِ پایین نیستن - از تبِ «سررسید» (میان‌برهای «قسط و وام»/
// «چک») به‌عنوانِ یه صفحه‌ی پوش‌شده (با دکمه‌ی برگشتِ خودشون - رجوع کن به [LOAN_ROUTE]/
// [CHEQUE_ROUTE]) باز می‌شن. «حسابداری»ِ قبلی سه‌جا شد: لیستِ حساب‌ها/تراکنش‌ها → «دارایی»
// (AssetsScreen)، گزارش‌گیری → تبِ مستقلِ «گزارش» (ReportScreen)، بودجه‌بندی → تبِ مستقلِ «بودجه»
// (BudgetScreen). «پرداختِ تکراری» و «دسته‌بندی‌ها» هر دو رفتن زیرِ «بودجه» (رجوع کن به CLAUDE.md،
// «تصمیمِ کاشیِ پرداختِ تکراری» - اول رفته بود زیرِ «سررسید»، بعداً از اونجا به اینجا منتقل شد).
internal enum class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    HOME("home", "خانه", Icons.Outlined.Home, Icons.Filled.Home),
    ASSETS("assets", "دارایی", Icons.Outlined.AccountBalanceWallet, Icons.Filled.AccountBalanceWallet),
    REPORT("report", "گزارش", Icons.Outlined.BarChart, Icons.Filled.BarChart),
    BUDGET("budget", "بودجه", Icons.Outlined.Savings, Icons.Filled.Savings),
    DUE("due", "سررسید", Icons.Outlined.EventNote, Icons.Filled.EventNote),
}
/** «وام» و «چک» دیگه تبِ نوارِ پایین نیستن (رجوع کن به کامنتِ بالای [BottomTab]) - این دو route
 * مستقیم به‌عنوانِ رشته تعریف شدن (نه عضوِ enumِ BottomTab) چون فقط از تبِ «سررسید»/«خانه» به‌عنوانِ
 * صفحه‌ی پوش‌شده باز می‌شن، تو نوارِ پایین رندر نمی‌شن. */
/**
 * هشت میان‌برِ پیش‌فرضِ **کشوی میان‌بُر** - عیناً همون‌هایی که کارتِ `31b` نشون می‌ده.
 * ترتیبِ اینجا فقط پیش‌فرضه؛ ترتیبِ واقعی از [ShortcutViewModel] میاد.
 */
internal val defaultShortcuts = listOf(
    Shortcut("expense", "ثبتِ خرج", Icons.Outlined.Payments, "home", locked = true),
    Shortcut("transfer", "انتقال", Icons.Outlined.SwapHoriz, "assets"),
    Shortcut("report", "گزارشِ ماه", Icons.Outlined.BarChart, "report"),
    Shortcut("due", "سررسید", Icons.Outlined.EventNote, "due"),
    Shortcut("cheque", "چک‌ها", Icons.Outlined.Description, "cheque"),
)
/**
 * **مخزنِ مقصدهای کشو** - ورودیِ حالتِ ویرایشِ فریمِ `53a`. هشتِ بالا انتخابِ پیش‌فرض‌اند،
 * این فهرست همه‌ی چیزهایی است که کاربر می‌تواند بینشان عوض کند.
 *
 * ⚠️ **فاصله‌ی آگاهانه با طرح**، عیناً همان دلیلِ [NavDestination]: فریم از «۱۴ مقصد» حرف
 * می‌زند و «پیام‌ها»/«هدفِ پس‌انداز»/«تقویم»/«سکه‌ها» را هم می‌شمرد؛ آن‌ها در `NavHost`ِ
 * فعلی مقصدِ ناوبری **نیستند**. با اضافه‌شدنِ هر route، فقط یک ردیف این‌جا اضافه می‌شود.
 */
// 🚨 **هیچ دو میان‌بری نباید یک آیکون داشته باشند.** شش جفتِ تکراری بود (پرداخت،
// کیفِ پول، نمودار، تقویم، سند، گروه) و کاربر دو خانه‌ی هم‌شکل می‌دید که از هم
// تشخیص‌پذیر نبودند - فقط برچسبِ ریزِ زیرشان فرق داشت. ردیفِ تازه هم باید آیکونی
// بردارد که در این فهرست نیست.
/** ۱۴ مهر (خواسته‌ی کاربر): تکراریِ تب‌ها و موارد بی‌فایده در کشو نشان داده نمی‌شوند. */
internal val hiddenShortcutIds = setOf("home", "loan", "assets", "shop", "inbox", "expense", "report", "budget", "gold", "transfer")
internal val allShortcutPool = defaultShortcuts + listOf(
    Shortcut("gold", "طلا", Icons.Outlined.MonetizationOn, "assets"),
    Shortcut("budget", "بودجه", Icons.Outlined.Savings, "budget"),
    // مقصدش «سررسید» بود و اشتباه: «دنگ» زیرصفحه‌ی `DebtScreen` است، پس تپ روی این
    // میان‌بر کاربر را به تبِ سررسید می‌برد و هیچ‌وقت به دنگ نمی‌رساند.
    Shortcut("debt", "دنگ", Icons.Outlined.Groups, DANG_ROUTE),
    // خواسته‌ی کاربر (۱۳ مهر): «طلب و بدهی» پیدا نمی‌شد.
    Shortcut("debts", "طلب و بدهی", Icons.Outlined.Handshake, DEBT_ROUTE),
    Shortcut("loan", "وام", Icons.Outlined.CreditCard, LOAN_ROUTE),
    Shortcut("home", "خانه", Icons.Outlined.Home, "home"),
    Shortcut("assets", "دارایی", Icons.Outlined.AccountBalanceWallet, "assets"),
    Shortcut("tools", "ابزارها", Icons.Outlined.Build, TOOLS_ROUTE),
    Shortcut("calendar", "تقویم مالی", Icons.Outlined.DateRange, CALENDAR_ROUTE),
    // خواسته‌ی کاربر (۳۱ شهریور): «تعدادِ میان‌برها را بیشتر کن». هر ردیفِ تازه باید
    // یک routeِ **واقعیِ** NavHost داشته باشد، وگرنه میان‌بر به هیچ‌جا نمی‌رود.
    Shortcut("loan-stats", "آمارِ وام", Icons.Outlined.PieChart, LOAN_STATS_ROUTE),
    Shortcut("cheque-report", "گزارشِ چک", Icons.Outlined.Assessment, CHEQUE_REPORT_ROUTE),
    Shortcut("archive", "آرشیوِ سالانه", Icons.Outlined.Archive, ANNUAL_ARCHIVE_ROUTE),
    Shortcut("sayad", "استعلامِ صیادی", Icons.Outlined.Search, SAYAD_INQUIRY_ROUTE),
    Shortcut("notes", "یادداشت‌ها", Icons.Outlined.EditNote, NOTES_ROUTE),
    // 🐞 گزارشِ مشکل - هم این‌جا هم در تنظیمات (خواسته‌ی کاربر): باگ همیشه سرِ
    // ناراحتی پیدا می‌شود، و آن لحظه کسی حوصله‌ی گشتن در تنظیمات را ندارد.
    Shortcut("bug", "گزارشِ مشکل", Icons.Outlined.BugReport, BUG_REPORT_ROUTE),
    // خواسته‌ی کاربر (۳۱ شهریور، دورِ دوم): «این‌جا را اگر می‌توانی بیشتر اضافه کن».
    // هر شش مقصدِ زیر صفحه‌ی **واقعیِ** موجود بودند که تا حالا فقط از دلِ تنظیمات یا
    // یک تب باز می‌شدند؛ این‌جا فقط `composable` گرفتند، صفحه‌ی تازه‌ای ساخته نشد.
    Shortcut("savings-goal", "هدفِ پس‌انداز", Icons.Outlined.Flag, SAVINGS_GOAL_ROUTE),
    Shortcut("categories", "دسته‌بندی‌ها", Icons.Outlined.Category, CATEGORIES_ROUTE),
    Shortcut("accounts", "حساب‌های بانکی", Icons.Outlined.AccountBalance, ACCOUNTS_ROUTE),
    Shortcut("shop", "فروشگاهِ سکه", Icons.Outlined.Storefront, SHOP_ROUTE),
    Shortcut("inbox", "پیام‌ها", Icons.Outlined.MarkEmailUnread, INBOX_ROUTE),
    Shortcut("calc-history", "تاریخچه‌ی محاسبات", Icons.Outlined.History, CALC_HISTORY_ROUTE),
    // ۸ مهر (خواسته‌ی کاربر: «هر قابلیتی یک میان‌بر داشته باشد»).
    Shortcut("add-account", "افزودنِ حساب", Icons.Outlined.AddCard, ADD_ACCOUNT_ROUTE),
    Shortcut("bills", "قبض‌ها", Icons.Outlined.ReceiptLong, BILLS_ROUTE),
    Shortcut("payoff", "تسویه‌ی بدهی‌ها", Icons.Outlined.TrendingDown, PAYOFF_ROUTE),
    Shortcut("statement", "صورت‌حسابِ بانکی", Icons.Outlined.UploadFile, STATEMENT_ROUTE),
    Shortcut("search", "جستجوی کلی", Icons.Outlined.ManageSearch, "search"),
    Shortcut("settings", "تنظیمات", Icons.Outlined.Settings, "settings"),
)
internal const val LOAN_ROUTE = "loan"
/** نگاشتِ مسیر به حسِ حرکتِ بخشش - هر بخش حسِ خودش، درونِ بخش یکدست. */
internal fun feelOf(route: String?): Motion.Feel = when (route) {
    "home", "shop" -> Motion.Feel.PLAYFUL
    "assets", "accounts" -> Motion.Feel.FLOW
    "report", "budget", "loan-stats", "cheque-report", "categories", "savings-goal", "annual-archive" -> Motion.Feel.INSIGHT
    "loan", "cheque", "due", "debt", "financial-calendar", "sayad-inquiry" -> Motion.Feel.SOLID
    else -> Motion.Feel.CALM
}
internal const val CHEQUE_ROUTE = "cheque"
internal const val LOAN_STATS_ROUTE = "loan-stats"
internal const val CHEQUE_REPORT_ROUTE = "cheque-report"
internal const val DEBT_ROUTE = "debt"
internal const val DANG_ROUTE = "dang"
internal const val TOOLS_ROUTE = "tools"
internal const val SAYAD_INQUIRY_ROUTE = "sayad-inquiry"
internal const val ANNUAL_ARCHIVE_ROUTE = "annual-archive"
internal const val CALENDAR_ROUTE = "financial-calendar"
internal const val NOTES_ROUTE = "notes"
internal const val BUG_REPORT_ROUTE = "bug-report"
internal const val SAVINGS_GOAL_ROUTE = "savings-goal"
internal const val CATEGORIES_ROUTE = "categories"
internal const val ACCOUNTS_ROUTE = "accounts"
internal const val SHOP_ROUTE = "shop"
internal const val INBOX_ROUTE = "inbox"
internal const val CALC_HISTORY_ROUTE = "calc-history"
internal const val ADD_ACCOUNT_ROUTE = "accounts-add"
internal const val BILLS_ROUTE = "bills"
internal const val PAYOFF_ROUTE = "debt-payoff"
internal const val STATEMENT_ROUTE = "statement-import"
/** زیرصفحه‌های داخلِ تبِ «وام» - جایگزینِ ۴ تبِ جداگانه‌ی قبلی. رجوع کن به [LoanTab]. */
internal enum class LoanSubTab(val label: String) {
    // سه تب، طبقِ فریمِ `27a`. تبِ چهارمِ «بانکی» **حذف نشد، ادغام شد**: تصمیمِ کلاد دیزاین
    // (۹ شهریور) این بود که با «محاسبه‌گر» یکی بشه و به‌جاش داخلِ همون تب یه سگمنتِ دوحالته
    // بیاد - رجوع کن به [CalculatorHostScreen] و فریمِ `27f`.
    MY_LOANS("وام‌های من"),
    DEPOSIT("سپرده"),
    CALCULATOR("محاسبه‌گر"),
}
// نگاشتِ TourTarget های تبی → BottomTab واقعی (برای این‌که AppTourOverlay بدونه با کدوم تب باید
// هماهنگ بشه - هم گرفتنِ مختصات از BottomNavItem هم ناوبریِ خودکار). «وام»/«چک» دیگه تبِ نوارِ
// پایین نیستن (رجوع کن به کامنتِ بالای BottomTab)، پس دیگه قدمِ تورِ مستقل ندارن.
internal fun TourTarget.asBottomTab(): BottomTab? = when (this) {
    TourTarget.ASSETS -> BottomTab.ASSETS
    TourTarget.REPORT -> BottomTab.REPORT
    TourTarget.BUDGET -> BottomTab.BUDGET
    // قدمِ «چیدمانِ نوار» روی خودِ خانه اسپاتلایت می‌شود: نوار همان‌جا هم هست و تبِ تازه‌ای
    // باز نمی‌کند.
    TourTarget.REORDER -> BottomTab.HOME
    else -> null
}
