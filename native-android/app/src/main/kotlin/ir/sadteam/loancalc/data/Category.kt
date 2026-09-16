package ir.sadteam.loancalc.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ir.sadteam.loancalc.core.TransactionType

/**
 * دسته‌بندیِ ثابت (نه یه جدولِ دیتابیس - عیناً هم‌الگو با [Banks.kt]/`BankEntry`) برای ماژولِ
 * حسابداریِ شخصی. اسمِ همین [CategoryEntry.name] رو خودِ `AccountTransactionEntity.category`
 * ذخیره می‌شه (نه idِ عددی)، پس اگه بعداً اسمِ یه دسته عوض بشه، تراکنش‌های قدیمی دیگه با هیچ
 * دسته‌ای match نمی‌شن - تغییرِ اسمِ دسته‌های موجود عمداً باید با احتیاط انجام بشه.
 *
 * ⚠️ **آیکون دیگر مقدارِ ثابت نیست، از [iconKey] مشتق می‌شود.** ستِ نمادِ خریدنی
 * (`symbolset:rounded`، بندِ ۱ی `72a`) باید روی دسته‌های ثابت هم بنشیند، وگرنه کاربر
 * ۵۰۰ سکه می‌دهد و فقط دسته‌های دلخواهش عوض می‌شوند. `icon` یک `get()` است، پس خواندنش
 * داخلِ composable خودش کامپوزِ دوباره را بعدِ خرید راه می‌اندازد.
 */
data class CategoryEntry(
    val name: String,
    val color: Color,
    val iconKey: String,
    val type: TransactionType,
) {
    val icon: ImageVector get() = iconForKey(iconKey)
}

val expenseCategories: List<CategoryEntry> = listOf(
    CategoryEntry("خوراک", Color(0xFFE53935), "restaurant", TransactionType.WITHDRAWAL),
    CategoryEntry("خانه", Color(0xFF6D4C41), "home", TransactionType.WITHDRAWAL),
    CategoryEntry("رفت‌وآمد", Color(0xFF1E88E5), "car", TransactionType.WITHDRAWAL),
    CategoryEntry("سلامت", Color(0xFF43A047), "hospital", TransactionType.WITHDRAWAL),
    CategoryEntry("خرید", Color(0xFF8E24AA), "shopping", TransactionType.WITHDRAWAL),
    CategoryEntry("قبض", Color(0xFFF4511E), "receipt", TransactionType.WITHDRAWAL),
    CategoryEntry("تفریح", Color(0xFF00ACC1), "celebration", TransactionType.WITHDRAWAL),
    CategoryEntry("قسط/چک", Color(0xFF3949AB), "payments", TransactionType.WITHDRAWAL),
    CategoryEntry("سایر هزینه", Color(0xFF757575), "other", TransactionType.WITHDRAWAL),
)

val incomeCategories: List<CategoryEntry> = listOf(
    CategoryEntry("حقوق", Color(0xFF2E7D32), "work", TransactionType.DEPOSIT),
    CategoryEntry("فروش", Color(0xFF00838F), "sell", TransactionType.DEPOSIT),
    CategoryEntry("هدیه", Color(0xFFAD1457), "gift", TransactionType.DEPOSIT),
    CategoryEntry("سودِ سرمایه‌گذاری", Color(0xFFF9A825), "trending", TransactionType.DEPOSIT),
    CategoryEntry("سایر درآمد", Color(0xFF757575), "other", TransactionType.DEPOSIT),
)

val allCategories: List<CategoryEntry> = expenseCategories + incomeCategories

fun categoriesFor(type: TransactionType): List<CategoryEntry> =
    if (type == TransactionType.DEPOSIT) incomeCategories else expenseCategories

fun findCategory(name: String?): CategoryEntry? = if (name == null) null else allCategories.find { it.name == name }
