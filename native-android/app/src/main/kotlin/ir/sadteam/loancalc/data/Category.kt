package ir.sadteam.loancalc.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ir.sadteam.loancalc.core.TransactionType

/**
 * دسته‌بندیِ ثابت (نه یه جدولِ دیتابیس - عیناً هم‌الگو با [Banks.kt]/`BankEntry`) برای ماژولِ
 * حسابداریِ شخصی. اسمِ همین [CategoryEntry.name] رو خودِ `AccountTransactionEntity.category`
 * ذخیره می‌شه (نه idِ عددی)، پس اگه بعداً اسمِ یه دسته عوض بشه، تراکنش‌های قدیمی دیگه با هیچ
 * دسته‌ای match نمی‌شن - تغییرِ اسمِ دسته‌های موجود عمداً باید با احتیاط انجام بشه.
 */
data class CategoryEntry(
    val name: String,
    val color: Color,
    val icon: ImageVector,
    val type: TransactionType,
)

val expenseCategories: List<CategoryEntry> = listOf(
    CategoryEntry("خوراک", Color(0xFFE53935), Icons.Filled.Restaurant, TransactionType.WITHDRAWAL),
    CategoryEntry("خانه", Color(0xFF6D4C41), Icons.Filled.Home, TransactionType.WITHDRAWAL),
    CategoryEntry("رفت‌وآمد", Color(0xFF1E88E5), Icons.Filled.DirectionsCar, TransactionType.WITHDRAWAL),
    CategoryEntry("سلامت", Color(0xFF43A047), Icons.Filled.LocalHospital, TransactionType.WITHDRAWAL),
    CategoryEntry("خرید", Color(0xFF8E24AA), Icons.Filled.ShoppingBag, TransactionType.WITHDRAWAL),
    CategoryEntry("قبض", Color(0xFFF4511E), Icons.Filled.ReceiptLong, TransactionType.WITHDRAWAL),
    CategoryEntry("تفریح", Color(0xFF00ACC1), Icons.Filled.Celebration, TransactionType.WITHDRAWAL),
    CategoryEntry("قسط/چک", Color(0xFF3949AB), Icons.Filled.Payments, TransactionType.WITHDRAWAL),
    CategoryEntry("سایر هزینه", Color(0xFF757575), Icons.Filled.MoreHoriz, TransactionType.WITHDRAWAL),
)

val incomeCategories: List<CategoryEntry> = listOf(
    CategoryEntry("حقوق", Color(0xFF2E7D32), Icons.Filled.Work, TransactionType.DEPOSIT),
    CategoryEntry("فروش", Color(0xFF00838F), Icons.Filled.Sell, TransactionType.DEPOSIT),
    CategoryEntry("هدیه", Color(0xFFAD1457), Icons.Filled.CardGiftcard, TransactionType.DEPOSIT),
    CategoryEntry("سودِ سرمایه‌گذاری", Color(0xFFF9A825), Icons.Filled.TrendingUp, TransactionType.DEPOSIT),
    CategoryEntry("سایر درآمد", Color(0xFF757575), Icons.Filled.MoreHoriz, TransactionType.DEPOSIT),
)

val allCategories: List<CategoryEntry> = expenseCategories + incomeCategories

fun categoriesFor(type: TransactionType): List<CategoryEntry> =
    if (type == TransactionType.DEPOSIT) incomeCategories else expenseCategories

fun findCategory(name: String?): CategoryEntry? = if (name == null) null else allCategories.find { it.name == name }
