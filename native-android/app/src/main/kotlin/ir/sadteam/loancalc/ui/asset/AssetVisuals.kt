package ir.sadteam.loancalc.ui.asset

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_CRYPTO
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_CUSTOM
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_FIAT
import ir.sadteam.loancalc.data.db.ASSET_CATEGORY_GOLD
import ir.sadteam.loancalc.ui.components.CoinIcon
import ir.sadteam.loancalc.ui.theme.AppAssetBorder
import ir.sadteam.loancalc.ui.theme.AppAssetInk
import ir.sadteam.loancalc.ui.theme.AppGoldBorder
import ir.sadteam.loancalc.ui.theme.AppGoldFrom
import ir.sadteam.loancalc.ui.theme.AppGoldInk
import ir.sadteam.loancalc.ui.theme.AppGoldTo
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.toFa

/**
 * نشانِ یک دارایی.
 *
 * - `logo` وکتور دراوبلِ واقعی. اولویت دارد؛ فعلاً همه‌جا خالی است.
 * - `glyph` جانشینِ حرفی/علامتی.
 * - `brand` رنگِ برند. **تنها جایی که هگزِ ثابت مجاز است** و در تمِ تیره هم
 *   نمی‌چرخد — نشانِ برند تِم ندارد؛ بیت‌کوین در شبانه هم نارنجی است. خوانایی
 *   از کنتراستِ گلیفِ سفید روی رنگِ پُر می‌آید، نه از توکن.
 * - `gold = true` یعنی نشان را به `CoinIcon`ِ موجود بسپار.
 */
data class AssetVisual(
    val glyph: String,
    val brand: Color,
    @DrawableRes val logo: Int? = null,
    val gold: Boolean = false,
)

private val GoldVisual = AssetVisual("", Color(0xFFD9A825), gold = true)

/**
 * جدولِ نماد → نشان. کلید **نمادِ** `AssetEntity.symbol` است، نه اسمِ فارسی.
 * ⚠️ با نمادهای واقعیِ `AssetCatalog.kt` جورش کن؛ اینها حدس است. نمادِ بیرونِ
 * جدول نمی‌شکند — حرفِ اولش را در دایره‌ی خنثی می‌گیرد.
 */
private val assetVisuals: Map<String, AssetVisual> = mapOf(
    // ── ارز: علامتِ خودِ ارز ────────────────────────────────────────────
    "USD" to AssetVisual("$", Color(0xFF2E7D4F)),
    "EUR" to AssetVisual("€", Color(0xFF20439B)),
    "GBP" to AssetVisual("£", Color(0xFF8C1D3F)),
    "AED" to AssetVisual("د.إ", Color(0xFF00713C)),
    "TRY" to AssetVisual("₺", Color(0xFFC8102E)),
    "CAD" to AssetVisual("$", Color(0xFFB3121B)),
    // ── رمز ارز: حرفِ اولِ نماد. رنگِ برند تفکیک را می‌سازد، نه حرف. ─────
    "BTC" to AssetVisual("B", Color(0xFFF7931A)),
    "ETH" to AssetVisual("E", Color(0xFF5A6FC0)),
    "USDT" to AssetVisual("U", Color(0xFF1F8F6B)),
    "XAUT" to AssetVisual("X", Color(0xFFD9A825)),
    "BNB" to AssetVisual("Bn", Color(0xFFC79A16)),
    "TRX" to AssetVisual("T", Color(0xFFC23631)),
    "LTC" to AssetVisual("L", Color(0xFF345D9D)),
    "SOL" to AssetVisual("S", Color(0xFF9945FF)),
    // #23292F تقریباً مشکیه و رو AppSurfaceِ تیره دیسکِ نامرئی می‌شه؛ روشن‌ترِ همون برند.
    // استثناست نه قاعده - بقیه‌ی هگزها کنتراستِ کافی دارن.
    "XRP" to AssetVisual("Xr", Color(0xFF3A4550)),
    "DOGE" to AssetVisual("D", Color(0xFFB89A3E)),
    "TON" to AssetVisual("Tn", Color(0xFF0098EA)),
)

fun assetVisualOf(symbol: String, category: String): AssetVisual =
    assetVisuals[symbol.uppercase()] ?: when (category) {
        ASSET_CATEGORY_GOLD -> GoldVisual
        ASSET_CATEGORY_CRYPTO -> AssetVisual(symbol.firstLetterOrDot(), Color(0xFF4B5A8C))
        else -> AssetVisual(symbol.firstLetterOrDot(), Color(0xFF5A6B63))
    }

/** «CUSTOM_انگشتر» → «ا». بهتر از سکه‌ی غلط. */
private fun String.firstLetterOrDot(): String =
    removePrefix("CUSTOM_").trim().firstOrNull()?.uppercase() ?: "•"

/**
 * دایره‌ی نشان. سه حالت به ترتیبِ اولویت: لوگوی واقعی، سکه‌ی طلا، گلیفِ برند.
 * اندازه‌ی گلیف نسبتِ ۰٫۴۲ِ قطر است تا در ۱۹ و ۳۲ و ۳۸ هر سه درست بنشیند.
 */
@Composable
fun AssetBadge(symbol: String, category: String, size: Dp, modifier: Modifier = Modifier) {
    val v = assetVisualOf(symbol, category)
    when {
        v.logo != null -> Image(
            painter = painterResource(v.logo),
            contentDescription = null,
            modifier = modifier.size(size).clip(CircleShape),
        )
        v.gold -> CoinIcon(size, modifier)
        else -> Box(
            modifier = modifier.size(size).clip(CircleShape).background(v.brand),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                v.glyph,
                color = Color.White,
                fontSize = (size.value * 0.42f).sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
            )
        }
    }
}

/**
 * پالتِ گروه.
 *
 * **فقط طلا کاغذِ طلایی می‌گیرد.** ارز و رمز ارز سطحِ معمولی‌اند و هویتشان از
 * دایره‌ی نشان می‌آید. این همان چیزی است که در تمِ تیره غلط بود: دلار و
 * بیت‌کوین روی کاغذِ طلاییِ تیره با جوهرِ `AppGoldInk` رندر می‌شدند.
 *
 * `paper = null` یعنی گروه کارتِ دربرگیرنده ندارد و ردیف‌هایش مثلِ ردیفِ حسابِ
 * بانکی مستقیم روی صفحه می‌نشینند.
 */
data class GroupPalette(
    val paper: Brush?,
    val rowSurface: Color,
    val rowBorder: Color,
    val ink: Color,
    val subInk: Color,
)

@Composable
fun groupPalette(category: String): GroupPalette = when (category) {
    ASSET_CATEGORY_GOLD -> GroupPalette(
        paper = Brush.verticalGradient(listOf(AppGoldFrom, AppGoldTo)),
        rowSurface = AppSurface,
        rowBorder = AppGoldBorder,
        ink = AppGoldInk,
        subInk = AppAssetInk,
    )
    ASSET_CATEGORY_FIAT -> GroupPalette(null, AppSurface, AppAssetBorder, AppText, AppAssetInk)
    ASSET_CATEGORY_CRYPTO -> GroupPalette(null, AppSurface, AppLineRow, AppText, AppMuted)
    else -> GroupPalette(null, AppSurface, AppLine, AppText, AppMuted)
}

/** ترتیبِ گروه‌ها در صفحه و عنوانشان. */
val assetGroupOrder: List<Pair<String, String>> = listOf(
    ASSET_CATEGORY_GOLD to "طلا",
    ASSET_CATEGORY_FIAT to "ارز",
    ASSET_CATEGORY_CRYPTO to "رمز ارز",
    ASSET_CATEGORY_CUSTOM to "سایر",
)

/**
 * مقدارِ دارایی معمولاً کسری است (۱٫۲ بیت‌کوین) ولی سکه و ارز اغلب صحیح - عددِ
 * صحیح بی «٫۰».
 *
 * ⚠️ `Locale.US` اجباری است: بی آن، روی گوشیِ فارسی خودِ `format` رقمِ فارسی و
 * ممیزِ «٫» برمی‌گرداند و `trimEnd('0')`/`trimEnd('.')` هیچ‌چیزی پیدا نمی‌کنند،
 * پس «۱٫۲ بیت‌کوین» می‌شد «۱٫۲۰۰۰ بیت‌کوین».
 */
internal fun formatQuantity(q: Double): String =
    if (q == q.toLong().toDouble()) {
        q.toLong().toFa()
    } else {
        String.format(java.util.Locale.US, "%.4f", q)
            .trimEnd('0').trimEnd('.')
            .faDigits()
    }
