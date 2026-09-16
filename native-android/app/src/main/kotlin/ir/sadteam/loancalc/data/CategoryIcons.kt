package ir.sadteam.loancalc.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Work
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * ═══════════ نمادِ دسته‌بندی ═══════════
 *
 * پالتِ ثابتِ آیکون برای دسته‌بندیِ دلخواهِ کاربر - چون Room نمی‌تونه [ImageVector] رو مستقیم
 * ذخیره کنه، فقط یه کلیدِ ثابت (رجوع کن به `CustomCategoryEntity.iconKey`) ذخیره می‌شه و
 * اینجا map می‌شه. ترتیبِ [categoryIconChoices] همون ترتیبی‌یه که تو دیالوگِ ساختِ دسته‌ی
 * جدید نشون داده می‌شه.
 *
 * **ستِ خریدنی (بندِ ۱ی `72a`).** هر کلید حالا سه شکل دارد: توپر (پیش‌فرض)، گرد، خطی.
 * نمادِ دسته تنها قلمِ فروشگاه است که **هر روز** دیده می‌شود - در فرمِ ثبت، در دونات، و
 * روی هر ردیفِ تراکنش - و همین آن را از تم متمایز می‌کند: تم رنگِ دکمه‌ها را عوض می‌کند،
 * نماد **چیزی را که کاربر به آن نگاه می‌کند**.
 */
enum class SymbolStyle(val id: String, val label: String) {
    FILLED("filled", "توپر"),
    ROUNDED("rounded", "گرد"),
    OUTLINED("outlined", "خطی"),
    ;

    companion object {
        /** شناسه‌ی فروشگاهی (`symbolset:rounded`) به سبک. `null`/ناشناس یعنی پیش‌فرض. */
        fun fromItemId(itemId: String?): SymbolStyle =
            entries.firstOrNull { itemId == "symbolset:${it.id}" } ?: FILLED
    }
}

/**
 * 🚨 **سبکِ فعال یک `mutableStateOf` سراسری است، نه `CompositionLocal`.**
 *
 * دلیلش این است که نمادها از فهرست‌های **ثابتِ** بالای فایل می‌آیند
 * ([expenseCategories]، `iconForKey`) و آن‌ها composable نیستند؛ سی‌وهشت جای مصرف هم
 * نباید امضا عوض کنند. با state سراسری، عوض‌شدنِ سبک خودش کامپوزِ دوباره را راه می‌اندازد
 * چون خواندنش داخلِ composable اتفاق می‌افتد.
 *
 * مقدارش را `MainActivity` از `UiPrefs.activeSymbolSet` پر می‌کند - یعنی **یک نقطه‌ی
 * نوشتن** دارد، همان قاعده‌ی «دو منبعِ حقیقت = فاجعه».
 */
object SymbolTheme {
    var style: SymbolStyle by mutableStateOf(SymbolStyle.FILLED)
}

/** سه شکلِ یک کلید. ترتیب: توپر، گرد، خطی. */
private val ICON_TABLE: List<Triple<String, ImageVector, Pair<ImageVector, ImageVector>>> = listOf(
    Triple("restaurant", Icons.Filled.Restaurant, Icons.Rounded.Restaurant to Icons.Outlined.Restaurant),
    Triple("home", Icons.Filled.Home, Icons.Rounded.Home to Icons.Outlined.Home),
    Triple("car", Icons.Filled.DirectionsCar, Icons.Rounded.DirectionsCar to Icons.Outlined.DirectionsCar),
    Triple("hospital", Icons.Filled.LocalHospital, Icons.Rounded.LocalHospital to Icons.Outlined.LocalHospital),
    Triple("shopping", Icons.Filled.ShoppingBag, Icons.Rounded.ShoppingBag to Icons.Outlined.ShoppingBag),
    Triple("receipt", Icons.Filled.ReceiptLong, Icons.Rounded.ReceiptLong to Icons.Outlined.ReceiptLong),
    Triple("celebration", Icons.Filled.Celebration, Icons.Rounded.Celebration to Icons.Outlined.Celebration),
    Triple("payments", Icons.Filled.Payments, Icons.Rounded.Payments to Icons.Outlined.Payments),
    Triple("work", Icons.Filled.Work, Icons.Rounded.Work to Icons.Outlined.Work),
    Triple("sell", Icons.Filled.Sell, Icons.Rounded.Sell to Icons.Outlined.Sell),
    Triple("gift", Icons.Filled.CardGiftcard, Icons.Rounded.CardGiftcard to Icons.Outlined.CardGiftcard),
    Triple("trending", Icons.Filled.TrendingUp, Icons.Rounded.TrendingUp to Icons.Outlined.TrendingUp),
    Triple("bolt", Icons.Filled.Bolt, Icons.Rounded.Bolt to Icons.Outlined.Bolt),
    Triple("school", Icons.Filled.School, Icons.Rounded.School to Icons.Outlined.School),
    Triple("pets", Icons.Filled.Pets, Icons.Rounded.Pets to Icons.Outlined.Pets),
    Triple("sports", Icons.Filled.SportsSoccer, Icons.Rounded.SportsSoccer to Icons.Outlined.SportsSoccer),
    Triple("star", Icons.Filled.Star, Icons.Rounded.Star to Icons.Outlined.Star),
    Triple("other", Icons.Filled.MoreHoriz, Icons.Rounded.MoreHoriz to Icons.Outlined.MoreHoriz),
)

/**
 * فهرستِ انتخابِ آیکونِ دسته‌ی دلخواه - با سبکِ **فعال**، نه همیشه توپر: دیالوگِ ساختِ دسته
 * باید همان چیزی را نشان بدهد که بعداً روی ردیف می‌نشیند.
 */
val categoryIconChoices: List<Pair<String, ImageVector>>
    get() = ICON_TABLE.map { it.first to iconForKey(it.first) }

/** نمادِ یک کلید در سبکِ [style] (پیش‌فرض: سبکِ فعالِ کاربر). */
fun iconForKey(key: String, style: SymbolStyle = SymbolTheme.style): ImageVector {
    val row = ICON_TABLE.firstOrNull { it.first == key } ?: ICON_TABLE.last()
    return when (style) {
        SymbolStyle.FILLED -> row.second
        SymbolStyle.ROUNDED -> row.third.first
        SymbolStyle.OUTLINED -> row.third.second
    }
}
