package ir.sadteam.loancalc.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * پالتِ ثابتِ آیکون برای دسته‌بندیِ دلخواهِ کاربر - چون Room نمی‌تونه [ImageVector] رو مستقیم ذخیره
 * کنه، فقط یه کلیدِ ثابت (رجوع کن به [CustomCategoryEntity.iconKey]) ذخیره می‌شه و اینجا map می‌شه.
 * ترتیبِ [categoryIconChoices] همون ترتیبی‌یه که تو دیالوگِ ساختِ دسته‌ی جدید نشون داده می‌شه.
 */
val categoryIconChoices: List<Pair<String, ImageVector>> = listOf(
    "restaurant" to Icons.Filled.Restaurant,
    "home" to Icons.Filled.Home,
    "car" to Icons.Filled.DirectionsCar,
    "hospital" to Icons.Filled.LocalHospital,
    "shopping" to Icons.Filled.ShoppingBag,
    "receipt" to Icons.Filled.ReceiptLong,
    "celebration" to Icons.Filled.Celebration,
    "payments" to Icons.Filled.Payments,
    "work" to Icons.Filled.Work,
    "sell" to Icons.Filled.Sell,
    "gift" to Icons.Filled.CardGiftcard,
    "trending" to Icons.Filled.TrendingUp,
    "bolt" to Icons.Filled.Bolt,
    "school" to Icons.Filled.School,
    "pets" to Icons.Filled.Pets,
    "sports" to Icons.Filled.SportsSoccer,
    "star" to Icons.Filled.Star,
    "other" to Icons.Filled.MoreHoriz,
)

fun iconForKey(key: String): ImageVector = categoryIconChoices.firstOrNull { it.first == key }?.second ?: Icons.Filled.MoreHoriz
