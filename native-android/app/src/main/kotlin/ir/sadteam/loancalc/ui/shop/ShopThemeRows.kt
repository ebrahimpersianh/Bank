package ir.sadteam.loancalc.ui.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.data.coin.ShopItem
import ir.sadteam.loancalc.data.coin.THEME_CATALOG
import ir.sadteam.loancalc.data.coin.themeById
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppGoldPillSoft
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppSurface2
import androidx.compose.foundation.border
import ir.sadteam.loancalc.ui.theme.AppText
import androidx.compose.ui.draw.scale

/** توضیحِ کوتاهِ حسِ هر تم - هم ردیف و هم کارتِ دوتایی. */
internal fun themeDisplay(item: ShopItem): ShopItem {
    val paletteId = item.id.removePrefix("theme:")
    return item.copy(
        blurb = when (paletteId) {
            "aubergine", "plum" -> "بنفشِ عمیق و آرام"
            "crimson", "garnet" -> "قرمزِ عمیق و رسمی"
            "saffron", "copper" -> "گرم و پرانرژی"
            "cobalt", "lapis", "indigo" -> "آبیِ عمیق و خنک"
            "olive", "teal", "turquoise" -> "سبزآبیِ نرم و تازه"
            "graphite" -> "خنثی و مینیمال"
            "forest" -> "سبزِ جنگلیِ عمیق و آرام"
            // هفت تمِ نام‌دارِ بخشِ ۸۲ - توضیحشان حس را می‌گوید نه رنگ را.
            "nature" -> "سبزِ جنگل، آرام و زنده"
            "night" -> "سرمه‌ایِ عمیقِ شبانه"
            "sunset" -> "نارنجیِ گرمِ غروب"
            "minimal" -> "سبزآبیِ ملایم و ساده"
            "luxe" -> "طلاییِ مات و باوقار"
            "calm" -> "بنفشِ نرم و آرام"
            "ice" -> "فیروزه‌ایِ خنک و روشن"
            "gold" -> "طلاییِ ویژه"
            else -> item.blurb
        },
    )
}
/** ردیفِ تم. تفاوتش با ردیفِ عادی فقط سه‌رنگیِ سمتِ راست است. */
@Composable
internal fun ThemeRow(
    item: ShopItem,
    state: RowState,
    balance: Int,
    onActivate: (ShopItem) -> Unit,
    onConfirm: (ShopItem) -> Unit,
) {
    val displayItem = themeDisplay(item)
    val paletteId = item.id.removePrefix("theme:")
    // اسمِ رنگ به‌تنهایی کافی نیست؛ توضیحِ کوتاه حسِ واقعیِ هر تم را می‌دهد و مثل
    // پیش‌نمایشِ فروشگاه، انتخاب را قبل از خرید قابل‌فهم می‌کند.
    ShopRow(
        item = displayItem,
        state = state,
        balance = balance,
        onActivate = onActivate,
        onConfirm = onConfirm,
        leading = {
            // خودِ رنگِ واقعیِ تم، در یک کاشیِ درشت؛ نمونه‌ی سه‌خطیِ قبلی در گوشی
            // تقریباً دیده نمی‌شد و کاربر نمی‌فهمید «بادمجانی» واقعاً چه رنگی است.
            val swatch = themeById(paletteId)
            val (dark, primary, light) = swatch?.let {
                Triple(Color(it.dark), Color(it.primary), Color(it.light))
            } ?: when (paletteId) {
                "green" -> Triple(Color(0xFF08734B), Color(0xFF0EA968), Color(0xFF80D6AE))
                "blue" -> Triple(Color(0xFF174A8B), Color(0xFF2878D4), Color(0xFF9BC7F5))
                "purple" -> Triple(Color(0xFF5D3585), Color(0xFF8B55C7), Color(0xFFCBA9EC))
                "gold" -> Triple(Color(0xFF806018), Color(0xFFC99625), Color(0xFFF3D57B))
                else -> Triple(AppMuted, AppIconFrame, AppSurface2)
            }
            // 🚨 **نمای کوچکِ خودِ برنامه، نه یک کاشیِ رنگ** (خواسته‌ی کاربر با طرحِ
            // مرجع: «تم‌ها آن‌طوری بشود»).
            //
            // کاشیِ رنگ می‌گفت تم **چه رنگی** است، ولی نمی‌گفت **برنامه با آن چه شکلی**
            // می‌شود - و کسی که ۱۵۰ سکه می‌دهد دقیقاً همین را می‌خواهد بداند. این‌جا یک
            // صفحه‌ی مینیاتوریِ واقعی کشیده می‌شود: کارتِ قهرمان با گرادیانِ همان تم، دو
            // کارتِ سفید، سه میله‌ی نمودار و نوارِ پایین.
            //
            // ⚠️ هیچ فایلِ تصویری لازم ندارد و با هر تمِ تازه‌ای که به `THEME_CATALOG`
            // اضافه شود خودبه‌خود کار می‌کند - برخلافِ موکاپِ عکسی که باید دستی ساخته شود.
            ThemeMiniPreview(dark = dark, primary = primary, light = light)
        },
    )
}
@Composable
internal fun ShopRow(
    item: ShopItem,
    state: RowState,
    balance: Int,
    onActivate: (ShopItem) -> Unit,
    onConfirm: (ShopItem) -> Unit,
    leading: (@Composable () -> Unit)? = null,
) {
    val dimmed = state == RowState.POOR || state == RowState.BADGE_LOCKED || state == RowState.SOON
    val open = LocalOpenProduct.current
    val tap: (() -> Unit)? = if (open != null) ({ open(item) }) else when (state) {
        RowState.OWNED -> ({ onActivate(item) })
        RowState.BUY -> ({ onConfirm(item) })
        else -> null
    }
    AppCard(
        borderColor = if (state == RowState.ACTIVE) AppPrimary else null,
        modifier = Modifier
            .alpha(if (dimmed) 0.62f else 1f)
            .then(if (tap != null) Modifier.pressScaleClickable(scale = 0.99f, onClick = tap) else Modifier),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) {
                leading()
                Spacer(modifier = Modifier.width(11.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(item.label, color = AppText, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                Text(
                    item.blurb,
                    color = AppLabel,
                    fontSize = 9.5.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Spacer(modifier = Modifier.width(9.dp))
            when (state) {
                RowState.ACTIVE -> ActivePill()
                RowState.OWNED -> UsePill()
                // ردیفی که هنوز مقصد ندارد پنهان **نمی‌شود**: هدفی که دیده نشود،
                // جمع‌کردنِ سکه را بی‌معنی می‌کند. ولی خریدنی هم نیست.
                RowState.SOON -> Pill("به‌زودی", AppIconFrame, AppMuted)
                RowState.BADGE_LOCKED -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = AppMuted, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("نشان لازم است", color = AppMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                }
                // ردیفِ گران خاموش می‌شود، پنهان نه - با کمبودِ نوشته‌شده (`18c`).
                RowState.POOR -> Column(horizontalAlignment = Alignment.End) {
                    Pill("${toFa(item.price)} سکه", AppIconFrame, AppMuted)
                    Text(
                        "${toFa(item.price - balance)} سکه کم داری",
                        color = AppDangerInk,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                RowState.BUY -> Pill("${toFa(item.price)} سکه", AppGoldPillSoft, AppText)
            }
        }
    }
}
@Composable
internal fun ResetRow(label: String, onClick: () -> Unit) {
    AppCard(modifier = Modifier.pressScaleClickable(scale = 0.99f, onClick = onClick)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = AppMuted, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Text("بازگردان", color = AppPrimaryInk, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
        }
    }
}
/**
 * نوارِ انتخابِ رنگِ قابِ آواتار.
 *
 * رنگ‌ها از `THEME_CATALOG` می‌آیند - همان شانزده رنگی که تم‌های فروشگاه دارند، پس
 * افزودنِ تمِ تازه خودبه‌خود این‌جا هم می‌آید و دو فهرستِ موازی ساخته نمی‌شود.
 *
 * اولین گزینه «هم‌رنگِ تم» است (`null`)، یعنی همان رفتاری که تا امروز بود.
 */
@Composable
internal fun FrameColorRow(selected: String?, onSelect: (String?) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text("رنگِ قاب", color = AppMuted, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FrameColorDot(color = AppPrimary, selected = selected == null) { onSelect(null) }
            THEME_CATALOG.forEach { tone ->
                FrameColorDot(
                    color = Color(tone.primary),
                    selected = selected == tone.id,
                ) { onSelect(tone.id) }
            }
        }
    }
}
/** یک نقطه‌ی رنگ. انتخاب‌شده حلقه‌ی دورش را می‌گیرد، نه تیک - تیک روی رنگِ تیره گم می‌شود. */
@Composable
private fun FrameColorDot(color: Color, selected: Boolean, onClick: () -> Unit) {
    val ring = AppText
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .then(if (selected) Modifier.border(2.dp, ring, CircleShape) else Modifier)
            .padding(if (selected) 4.dp else 2.dp)
            .clip(CircleShape)
            .background(color)
            .pressScaleClickable(onClick = onClick),
    )
}
/**
 * مینیاتورِ صفحه‌ی خانه با رنگ‌های یک تم - پیش‌نمایشِ ردیفِ تمِ فروشگاه.
 *
 * عمداً **شبیهِ تبِ خانه** است نه یک شکلِ انتزاعی: کارتِ قهرمانِ رنگی بالا، دو کارتِ
 * روشن، میله‌های نمودار و نوارِ پایین با قرصِ تبِ فعال. همان چیزهایی که تم واقعاً
 * عوضشان می‌کند.
 */
@Composable
internal fun ThemeMiniPreview(dark: Color, primary: Color, light: Color) {
    val paper = AppSurface
    val shape = RoundedCornerShape(11.dp)
    Column(
        modifier = Modifier
            .size(width = 46.dp, height = 62.dp)
            .clip(shape)
            .background(paper)
            .border(1.dp, AppLineRow, shape)
            .padding(3.dp),
        verticalArrangement = Arrangement.spacedBy(2.5.dp),
    ) {
        // کارتِ قهرمان - همان گرادیانِ ۱۶۰درجه‌ی `AppHeroCard`، در مقیاسِ کوچک.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Brush.linearGradient(listOf(primary, dark))),
            contentAlignment = Alignment.BottomStart,
        ) {
            Row(
                modifier = Modifier.padding(start = 3.dp, bottom = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                listOf(4, 7, 5, 9).forEach { h ->
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height(h.dp)
                            .clip(RoundedCornerShape(topStart = 1.dp, topEnd = 1.dp))
                            .background(Color.White.copy(alpha = 0.75f)),
                    )
                }
            }
        }
        // دو کارتِ روشن - همان شبکه‌ی «دسترسیِ سریع»ِ تبِ خانه.
        Row(horizontalArrangement = Arrangement.spacedBy(2.5.dp), modifier = Modifier.fillMaxWidth()) {
            repeat(2) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(11.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(light.copy(alpha = 0.38f)),
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        // نوارِ پایین با قرصِ تبِ فعال.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(AppSurface2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == 0) 5.dp else 3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (index == 0) primary else AppMuted.copy(alpha = 0.45f)),
                )
            }
        }
    }
}
/**
 * فاصله‌ی دو کلیدِ جلالیِ `۱۴۰۵-۰۶-۳۱` به روز.
 *
 * ⚠️ کلیدِ خراب `Int.MAX_VALUE` می‌دهد نه صفر: صفر یعنی «امروز» و یک قلمِ بدتاریخ را
 * برای همیشه «تازه» نگه می‌داشت - دقیقاً همان چیزی که `addedOn = null` جلویش را می‌گیرد.
 */
internal fun daysBetweenKeys(from: String, to: String): Int {
    fun parse(key: String): PersianDate? {
        val parts = key.split('-')
        if (parts.size != 3) return null
        val y = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        val d = parts[2].toIntOrNull() ?: return null
        return PersianDate(y, m, d)
    }
    val a = parse(from) ?: return Int.MAX_VALUE
    val b = parse(to) ?: return Int.MAX_VALUE
    return runCatching { JalaliCalendar.daysBetween(a, b) }.getOrDefault(Int.MAX_VALUE)
}
