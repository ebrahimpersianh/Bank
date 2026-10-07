package ir.sadteam.loancalc.ui.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.res.painterResource
import ir.sadteam.loancalc.R
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.data.SymbolStyle
import ir.sadteam.loancalc.data.iconForKey
import ir.sadteam.loancalc.data.coin.ShopItem
import ir.sadteam.loancalc.data.coin.themeById
import ir.sadteam.loancalc.ui.components.AvatarFramePreview
import ir.sadteam.loancalc.ui.components.AvatarFrameStyle
import ir.sadteam.loancalc.ui.components.CoinIcon
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppFontChoice
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppPrimaryInkLight
import ir.sadteam.loancalc.ui.background.LiveBackgroundLayer
import ir.sadteam.loancalc.ui.background.LiveBackground
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * پیش‌نمایشِ هر قلم، بر اساسِ نوعش - همان چیزی که ردیفِ فشرده هم می‌گذارد.
 *
 * یک نقطه‌ی انتخاب برای هر دو نما، تا کارت و ردیف هیچ‌وقت دو چیزِ متفاوت نشان ندهند.
 */
@Composable
internal fun previewFor(item: ShopItem) {
    when {
        item.id.startsWith("icon:") -> AppIconPreview(item.id)
        item.id.startsWith("symbolset:") -> SymbolSetPreview(item.id)
        item.id.startsWith("coinskin:") -> CoinSkinPreview(item.id)
        item.id.startsWith("chart:") -> ChartStylePreview(item.id)
        item.id.startsWith("frame:") -> AvatarFramePreview(AvatarFrameStyle.fromItemId(item.id))
        item.id.startsWith("font:") -> FontPreview(AppFontChoice.fromId(item.id))
        item.id.startsWith("bg_") -> BackdropPreview(LiveBackground.byId(item.id.removePrefix("bg_")))
        item.id.startsWith("theme:") -> {
            val tone = themeById(item.id.removePrefix("theme:"))
            val (dark, primary, light) = tone?.let {
                Triple(Color(it.dark), Color(it.primary), Color(it.light))
            } ?: when (item.id.removePrefix("theme:")) {
                "green" -> Triple(Color(0xFF08734B), Color(0xFF0EA968), Color(0xFF80D6AE))
                "blue" -> Triple(Color(0xFF174A8B), Color(0xFF2878D4), Color(0xFF9BC7F5))
                "purple" -> Triple(Color(0xFF5D3585), Color(0xFF8B55C7), Color(0xFFCBA9EC))
                "gold" -> Triple(Color(0xFF806018), Color(0xFFC99625), Color(0xFFF3D57B))
                else -> Triple(AppMuted, AppIconFrame, AppSurface2)
            }
            ThemeMiniPreview(dark, primary, light)
        }
        else -> GenericItemPreview(item)
    }
}
/**
 * پیش‌نمایشِ آیکونِ برنامه - **خودِ فایلِ آیکون**، نه یک نمادِ جایگزین.
 *
 * دو لایه‌ی `adaptive-icon` دستی روی هم می‌نشینند (پس‌زمینه و پیش‌زمینه)، چون
 * `mipmap-anydpi-v26` را `painterResource` مستقیم نمی‌کشد.
 *
 * ⚠️ همیشه **پله‌ی صفر** است، حتی اگر آیکونِ فعالِ کاربر پژمرده باشد (`49d`).
 */
@Composable
internal fun AppIconPreview(itemId: String) {
    val (bg, fg) = when (itemId) {
        "icon:coin" -> R.drawable.ic_launcher_coin_background to R.drawable.ic_launcher_coin_foreground
        "icon:letter" -> R.drawable.ic_launcher_letter_background to R.drawable.ic_launcher_letter_foreground
        "icon:piggy" -> R.drawable.ic_launcher_piggy_background to R.drawable.ic_launcher_piggy_foreground
        "icon:shop" -> R.drawable.ic_launcher_shop_background to R.drawable.jibak_shop_0
        // نه طرحِ تصویریِ بخشِ ۸۰ - هر کدام پس‌زمینه‌ی **پُرِ** خودش را دارد (رنگ از
        // تیره‌ترین ناحیه‌ی خودِ فایلِ هنری)، چون لایه‌ی شفاف روی بعضی لانچرها
        // صفحه‌ی مشکی می‌گیرد.
        "icon:aqua" -> R.drawable.jibak_ic_aqua_bg to R.drawable.jibak_ic_aqua
        "icon:calligraphy" -> R.drawable.jibak_ic_calligraphy_bg to R.drawable.jibak_ic_calligraphy
        "icon:fox" -> R.drawable.jibak_ic_fox_bg to R.drawable.jibak_ic_fox
        "icon:emerald" -> R.drawable.jibak_ic_emerald_bg to R.drawable.jibak_ic_emerald
        "icon:leaf" -> R.drawable.jibak_ic_leaf_bg to R.drawable.jibak_ic_leaf
        "icon:orbit" -> R.drawable.jibak_ic_orbit_bg to R.drawable.jibak_ic_orbit
        "icon:growth" -> R.drawable.jibak_ic_growth_bg to R.drawable.jibak_ic_growth
        "icon:sprout" -> R.drawable.jibak_ic_sprout_bg to R.drawable.jibak_ic_sprout
        "icon:neon" -> R.drawable.jibak_ic_neon_bg to R.drawable.jibak_ic_neon
        "icon:star" -> R.drawable.jibak_ic_star_bg to R.drawable.jibak_ic_star
        // پیش‌فرض «کیفِ پول» است؛ پیش‌زمینه‌اش PNGِ mipmap است نه وکتورِ drawable.
        else -> R.drawable.ic_launcher_background to R.mipmap.ic_launcher_foreground
    }
    Box(
        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(AppRadius.icon)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
        Image(
            painter = painterResource(fg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
/** پیش‌نمایشِ بقیه‌ی قلم‌ها - نمادی که کارِ قلم را می‌گوید، در همان قابِ ۳۸ِ آیکون. */
@Composable
private fun GenericItemPreview(item: ShopItem) {
    val icon = when {
        item.id.startsWith("coinskin:") -> Icons.Filled.Savings
        item.id.startsWith("symbolset:") -> Icons.Filled.Category
        else -> Icons.Filled.WorkspacePremium
    }
    Box(
        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(AppRadius.icon)).background(AppIconFrame),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = AppMuted, modifier = Modifier.size(19.dp))
    }
}
/**
 * پیش‌نمایشِ ستِ نماد - **چهار نماد در شبکه**، نه یکی: ست است و یک نماد جنسش را نمی‌گوید
 * (`72b`). همان قابِ ۳۸ِ بقیه‌ی قلم‌ها.
 */
/** پیش‌نمایشِ قلم: حرفِ «آ» با خودِ همان قلم، هم‌اندازه‌ی بقیه‌ی پیش‌نمایش‌ها. */
@Composable
private fun FontPreview(choice: AppFontChoice) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(AppRadius.icon))
            .background(AppSurface2),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "آ",
            color = AppText,
            fontSize = 19.sp,
            fontFamily = choice.family,
        )
    }
}
@Composable
private fun SymbolSetPreview(itemId: String) {
    val style = SymbolStyle.fromItemId(itemId)
    val (background, tint) = when (style) {
        SymbolStyle.FILLED -> AppIconFrame to AppMuted
        SymbolStyle.ROUNDED -> AppPrimaryPill to AppPrimaryInk
        SymbolStyle.OUTLINED -> AppSurface2 to AppText
        SymbolStyle.SHARP -> Color(0xFFFFEEE2) to Color(0xFFB64C19)
        SymbolStyle.TWO_TONE -> Color(0xFFEAE5FF) to Color(0xFF6842B8)
        SymbolStyle.PICTORIAL -> Color(0xFFE0F4EE) to Color(0xFF087D5B)
        SymbolStyle.SOLID -> Color(0xFFFFF4D6) to Color(0xFF7A5A00)
        SymbolStyle.CUTE -> Color(0xFFFFE6F0) to Color(0xFFB0306A)
        SymbolStyle.FINE_LINE -> Color(0xFFE3F0FF) to Color(0xFF1F5FB0)
    }
    val keys = listOf("restaurant", "home", "car", "celebration")
    Box(
        modifier = Modifier.size(42.dp).clip(RoundedCornerShape(AppRadius.icon)).background(background),
        contentAlignment = Alignment.Center,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            keys.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                    pair.forEach { key ->
                        Icon(
                            iconForKey(key, style),
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(16.dp).padding(1.dp),
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun CoinSkinPreview(itemId: String) {
    val accent = if (itemId == "coinskin:ancient") Color(0xFF8A6744) else Color(0xFFC99625)
    Box(
        modifier = Modifier.size(42.dp).clip(RoundedCornerShape(AppRadius.icon)).background(accent.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        CoinIcon(size = 25.dp, modifier = Modifier.alpha(if (itemId == "coinskin:ancient") 0.76f else 1f))
    }
}
/**
 * پیش‌نمایشِ پس‌زمینه‌ی زنده - همان `LiveBackdrop`ِ واقعی در یک مربعِ ۳۸، روی زمینه‌ی
 * سطحِ برنامه تا نسبتش با صفحه‌ی واقعی دیده شود.
 *
 * ⚠️ آلفای لایه در اندازه‌ی ۳۸ تقریباً نامرئی است، پس این پیش‌نمایش عمداً **دو برابرِ**
 * اندازه را در خودش می‌کشد و برش می‌دهد؛ وگرنه ردیفِ ویترین یک مربعِ خالی می‌شد.
 */
@Composable
private fun BackdropPreview(backdrop: LiveBackground?) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            // زمینه‌ی تیره‌ی ثابت: پیش‌نمایش همیشه حالتِ تیره را می‌کشد و روی زمینه‌ی روشن محو بود.
            .background(Color(0xFF14202E)),
        contentAlignment = Alignment.Center,
    ) {
        if (backdrop != null) {
            LiveBackgroundLayer(
                background = backdrop,
                primary = AppPrimary,
                primaryLight = AppPrimaryInkLight,
                // پیش‌نمایش همیشه حالتِ تیره را می‌کشد، وگرنه «چرخشِ شب» در تمِ روشن
                // یک مربعِ خالی می‌شد و کاربر فکر می‌کرد ردیف خراب است.
                isDark = true,
                modifier = Modifier.size(76.dp),
            )
        }
    }
}
