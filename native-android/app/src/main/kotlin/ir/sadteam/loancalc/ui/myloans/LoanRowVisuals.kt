package ir.sadteam.loancalc.ui.myloans

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ═══════════ نشانِ هر وام ═══════════
 *
 * طرحِ مرجعِ کاربر (۳۱ شهریور) هر ردیفِ وام را با یک **کاشیِ رنگیِ نشان‌دار** شروع می‌کند
 * (خانه، خودرو، سفر…) - همان چیزی که فهرست را از «چند سطرِ متنِ هم‌شکل» بیرون می‌آورد.
 *
 * 🚨 **وام ستونِ دسته ندارد**، پس نشان از **نامِ خودِ وام** حدس زده می‌شود. این عمدی است:
 * مهاجرتِ دیتابیس برای یک تزئین قیمتِ درستی نیست، و کاربری که وامش را «وام مسکن» نامیده
 * دقیقاً همان کلمه را نوشته که نشان را پیدا می‌کند. چیزی هم از دست نمی‌رود - نامِ بی‌کلیدواژه
 * نشانِ پیش‌فرضِ «سند» می‌گیرد.
 */
/**
 * نوع‌های انتخابیِ وام. `id` در `dataJson` ذخیره می‌شود و **هیچ‌وقت عوض نمی‌شود** - همان
 * رشته در فایلِ پشتیبان و روی سرور هم نشسته.
 */
enum class LoanCategory(val id: String, val label: String, val glyph: ImageVector) {
    HOME("home", "مسکن", Icons.Filled.Home),
    CAR("car", "خودرو", Icons.Filled.DirectionsCar),
    TRIP("trip", "سفر", Icons.Filled.AirplanemodeActive),
    MARRIAGE("marriage", "ازدواج", Icons.Filled.Favorite),
    HEALTH("health", "درمان", Icons.Filled.LocalHospital),
    STUDY("study", "تحصیل", Icons.Filled.School),
    BUSINESS("business", "کسب‌وکار", Icons.Filled.Storefront),
    GOODS("goods", "کالا", Icons.Filled.ShoppingBag),
    OTHER("other", "سایر", Icons.Filled.Description),
}

/**
 * نشانِ ردیف: اگر کاربر نوع را **انتخاب کرده** همان، وگرنه حدس از روی نام.
 *
 * ترتیب مهم است - انتخابِ صریحِ کاربر همیشه بر حدس مقدم است، حتی اگر نامِ وام کلیدواژه‌ای
 * داشته باشد که حدس را به جای دیگری ببرد.
 */
fun loanGlyphFor(name: String, categoryId: String? = null): ImageVector {
    val chosen = categoryId?.let { id -> LoanCategory.entries.firstOrNull { it.id == id } }
    if (chosen != null) return chosen.glyph
    return loanGlyphGuessedFrom(name)
}

private fun loanGlyphGuessedFrom(name: String): ImageVector {
    val n = name.trim()
    fun has(vararg keys: String) = keys.any { n.contains(it) }
    return when {
        has("مسکن", "خانه", "منزل", "ملک", "ودیعه", "رهن", "اجاره", "ساخت") -> Icons.Filled.Home
        has("خودرو", "ماشین", "موتور", "پراید", "سواری", "تاکسی") -> Icons.Filled.DirectionsCar
        has("سفر", "زیارت", "مسافرت", "کربلا", "مشهد", "حج") -> Icons.Filled.AirplanemodeActive
        has("ازدواج", "جهیزیه", "عروسی") -> Icons.Filled.Favorite
        has("درمان", "بیمار", "پزشک", "دارو", "جراحی") -> Icons.Filled.LocalHospital
        has("دانش", "تحصیل", "دانشگاه", "شهریه", "آموزش") -> Icons.Filled.School
        has("کسب", "کار", "تجار", "سرمایه", "بنگاه", "مغازه") -> Icons.Filled.Storefront
        has("کالا", "خرید", "لوازم", "ضرور") -> Icons.Filled.ShoppingBag
        else -> Icons.Filled.Description
    }
}

/** کاشیِ نشانِ ردیفِ وام - رنگش از حالتِ ردیف می‌آید، نه از نوعِ وام. */
@Composable
fun LoanGlyphTile(
    glyph: ImageVector,
    tint: Color,
    background: Color,
    size: Dp = 42.dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(glyph, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

/**
 * ═══════════ موجِ زیرِ عددِ قهرمان ═══════════
 *
 * خواسته‌ی کاربر با طرحِ مرجع: «آن خطِ پایینِ رقم». خطِ نرمی که **ماندهٔ بدهی در شش ماهِ
 * پیشِ رو** را می‌کشد، با یک نقطه‌ی برجسته روی ماهِ جاری.
 *
 * 🚨 **داده‌ی واقعی است، نه تزئین**: هر نقطه ماندهٔ بدهی بعد از پرداختِ اقساطِ آن ماه است.
 * یک موجِ تصادفیِ خوش‌شکل همان‌قدر جا می‌گرفت و هیچ چیزی نمی‌گفت - و بدتر، کاربر آن را
 * داده فرض می‌کرد.
 *
 * ⚠️ رنگ‌ها **پارامترند** نه توکنِ خوانده‌شده در `DrawScope`: توکن‌ها `@Composable`اند و
 * داخلِ `Canvas` صدا زده نمی‌شوند (قاعده‌ی ثبت‌شده‌ی پروژه).
 */
@Composable
fun DebtTrendWave(
    points: List<Float>,
    lineColor: Color,
    dotColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 34.dp,
) {
    if (points.size < 2) return
    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        drawDebtWave(points, lineColor, dotColor)
    }
}

private fun DrawScope.drawDebtWave(points: List<Float>, lineColor: Color, dotColor: Color) {
    val max = points.max()
    val min = points.min()
    val span = (max - min).takeIf { it > 0f } ?: 1f
    val padding = size.height * 0.18f
    val usable = size.height - padding * 2
    // 🚨 راست‌به‌چپ: نقطه‌ی **اول** سمتِ راست می‌نشیند، مثلِ هر چیزِ دیگری در این برنامه.
    val xs = points.indices.map { size.width - it * (size.width / (points.size - 1)) }
    val ys = points.map { padding + usable * (1f - (it - min) / span) }

    val path = Path().apply {
        moveTo(xs[0], ys[0])
        for (i in 0 until points.size - 1) {
            // منحنیِ مکعبی با دسته‌های افقی: همان نرمیِ طرحِ مرجع، بی هیچ کتابخانه‌ای.
            val cx = (xs[i] + xs[i + 1]) / 2
            cubicTo(cx, ys[i], cx, ys[i + 1], xs[i + 1], ys[i + 1])
        }
    }
    drawPath(
        path = path,
        color = lineColor,
        style = Stroke(width = size.height * 0.09f, cap = StrokeCap.Round),
    )
    // نقطه‌ی ماهِ جاری - هاله‌ی روشن زیرش تا روی خطِ هم‌رنگ گم نشود.
    drawCircle(color = dotColor.copy(alpha = 0.25f), radius = size.height * 0.20f, center = Offset(xs[0], ys[0]))
    drawCircle(color = dotColor, radius = size.height * 0.10f, center = Offset(xs[0], ys[0]))
}
