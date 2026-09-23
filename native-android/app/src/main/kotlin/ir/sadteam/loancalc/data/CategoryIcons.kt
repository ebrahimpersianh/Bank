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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.Cottage
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.material.icons.sharp.*
import androidx.compose.material.icons.twotone.*
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
    FILLED("filled", "توپر"), ROUNDED("rounded", "گرد"), OUTLINED("outlined", "خطی"),
    SHARP("sharp", "زاویه‌دار"), TWO_TONE("two_tone", "دو‌لایه"), PICTORIAL("pictorial", "روزمره"), SOLID("solid", "برجسته");
    companion object { fun fromItemId(itemId: String?): SymbolStyle = entries.firstOrNull { itemId == "symbolset:${it.id}" } ?: FILLED }
}
object SymbolTheme { var style: SymbolStyle by mutableStateOf(SymbolStyle.FILLED) }
private data class IconPack(val key:String,val filled:ImageVector,val rounded:ImageVector,val outlined:ImageVector,val sharp:ImageVector,val twoTone:ImageVector,val pictorial:ImageVector)
private val ICON_TABLE = listOf(
    IconPack("restaurant", Icons.Filled.Restaurant, Icons.Rounded.Restaurant, Icons.Outlined.Restaurant, Icons.Sharp.Restaurant, Icons.TwoTone.Restaurant, Icons.Filled.Fastfood),
    IconPack("home", Icons.Filled.Home, Icons.Rounded.Home, Icons.Outlined.Home, Icons.Sharp.Home, Icons.TwoTone.Home, Icons.Filled.Cottage),
    IconPack("car", Icons.Filled.DirectionsCar, Icons.Rounded.DirectionsCar, Icons.Outlined.DirectionsCar, Icons.Sharp.DirectionsCar, Icons.TwoTone.DirectionsCar, Icons.Filled.Commute),
    IconPack("hospital", Icons.Filled.LocalHospital, Icons.Rounded.LocalHospital, Icons.Outlined.LocalHospital, Icons.Sharp.LocalHospital, Icons.TwoTone.LocalHospital, Icons.Filled.Medication),
    IconPack("shopping", Icons.Filled.ShoppingBag, Icons.Rounded.ShoppingBag, Icons.Outlined.ShoppingBag, Icons.Sharp.ShoppingBag, Icons.TwoTone.ShoppingBag, Icons.Filled.Storefront),
    IconPack("receipt", Icons.Filled.ReceiptLong, Icons.Rounded.ReceiptLong, Icons.Outlined.ReceiptLong, Icons.Sharp.ReceiptLong, Icons.TwoTone.ReceiptLong, Icons.Filled.ListAlt),
    IconPack("celebration", Icons.Filled.Celebration, Icons.Rounded.Celebration, Icons.Outlined.Celebration, Icons.Sharp.Celebration, Icons.TwoTone.Celebration, Icons.Filled.Cake),
    IconPack("payments", Icons.Filled.Payments, Icons.Rounded.Payments, Icons.Outlined.Payments, Icons.Sharp.Payments, Icons.TwoTone.Payments, Icons.Filled.AccountBalanceWallet),
    IconPack("work", Icons.Filled.Work, Icons.Rounded.Work, Icons.Outlined.Work, Icons.Sharp.Work, Icons.TwoTone.Work, Icons.Filled.BusinessCenter),
    IconPack("sell", Icons.Filled.Sell, Icons.Rounded.Sell, Icons.Outlined.Sell, Icons.Sharp.Sell, Icons.TwoTone.Sell, Icons.Filled.LocalOffer),
    IconPack("gift", Icons.Filled.CardGiftcard, Icons.Rounded.CardGiftcard, Icons.Outlined.CardGiftcard, Icons.Sharp.CardGiftcard, Icons.TwoTone.CardGiftcard, Icons.Filled.Redeem),
    IconPack("trending", Icons.Filled.TrendingUp, Icons.Rounded.TrendingUp, Icons.Outlined.TrendingUp, Icons.Sharp.TrendingUp, Icons.TwoTone.TrendingUp, Icons.Filled.ShowChart),
    IconPack("bolt", Icons.Filled.Bolt, Icons.Rounded.Bolt, Icons.Outlined.Bolt, Icons.Sharp.Bolt, Icons.TwoTone.Bolt, Icons.Filled.ElectricBolt),
    IconPack("school", Icons.Filled.School, Icons.Rounded.School, Icons.Outlined.School, Icons.Sharp.School, Icons.TwoTone.School, Icons.Filled.MenuBook),
    IconPack("pets", Icons.Filled.Pets, Icons.Rounded.Pets, Icons.Outlined.Pets, Icons.Sharp.Pets, Icons.TwoTone.Pets, Icons.Filled.Park),
    IconPack("sports", Icons.Filled.SportsSoccer, Icons.Rounded.SportsSoccer, Icons.Outlined.SportsSoccer, Icons.Sharp.SportsSoccer, Icons.TwoTone.SportsSoccer, Icons.Filled.FitnessCenter),
    IconPack("star", Icons.Filled.Star, Icons.Rounded.Star, Icons.Outlined.Star, Icons.Sharp.Star, Icons.TwoTone.Star, Icons.Filled.Stars),
    IconPack("other", Icons.Filled.MoreHoriz, Icons.Rounded.MoreHoriz, Icons.Outlined.MoreHoriz, Icons.Sharp.MoreHoriz, Icons.TwoTone.MoreHoriz, Icons.Filled.Apps),
)
val categoryIconChoices: List<Pair<String, ImageVector>> get() = ICON_TABLE.map { it.key to iconForKey(it.key) }
fun iconForKey(key: String, style: SymbolStyle = SymbolTheme.style): ImageVector {
    val icon = ICON_TABLE.firstOrNull { it.key == key } ?: ICON_TABLE.last()
    return when(style) {
        SymbolStyle.FILLED -> icon.filled
        SymbolStyle.PICTORIAL -> icon.pictorial
        SymbolStyle.SOLID -> SolidSymbols.forKey(icon.key) ?: icon.filled
        SymbolStyle.ROUNDED -> icon.rounded
        SymbolStyle.OUTLINED -> icon.outlined
        SymbolStyle.SHARP -> icon.sharp
        SymbolStyle.TWO_TONE -> icon.twoTone
    }
}
