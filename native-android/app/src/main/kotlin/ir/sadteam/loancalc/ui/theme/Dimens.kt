package ir.sadteam.loancalc.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.runtime.Composable

/**
 * ابعادِ ثابتِ سبکِ «جیبک» - از بخشِ «۳ · شعاع، سایه، فاصله»ی سیستمِ طراحی.
 *
 * قاعده‌ی طلایی: **هیچ سایه‌ی تارِ رنگی وجود نداره.** عمق فقط با یه سایه‌ی **سختِ عمودی** ساخته
 * می‌شه (`0 4px 0` هم‌رنگِ تیره‌ترِ خودش). این مهم‌ترین تفاوتِ بصری با دورِ قبله - اگه جایی
 * `Modifier.shadow(...)`ِ استانداردِ Compose (که تار می‌سازه) دیدی، جاش [hardShadow]ه.
 */
object AppRadius {
    /** قابِ آیکون. */
    val icon = 12.dp

    /** ردیفِ فهرست. */
    val row = 16.dp

    /** کارت. */
    val card = 20.dp

    /** شیتِ پایین‌کشو. */
    val sheet = 28.dp

    /** دکمه - کپسولِ کامل. */
    val button = 999.dp
}

/**
 * فاصله‌ها همه مضربِ ۴ هستن.
 */
object AppSpacing {
    /** حاشیه‌ی درونیِ کارت. */
    val cardPadding = 16.dp

    /** حاشیه‌ی درونیِ فشرده‌ترِ کارت (کارتِ کوچیک/ردیف). */
    val cardPaddingTight = 14.dp

    /** فاصله‌ی بینِ دو کارتِ پشتِ‌سرهم. */
    val betweenCards = 12.dp

    /** حاشیه‌ی چپ/راستِ صفحه. */
    val page = 16.dp

    /** کمینه‌ی ارتفاعِ لمسی - قاعده‌ی صریحِ سیستمِ طراحی. */
    val minTouchTarget = 44.dp
}

/**
 * ارتفاعِ سایه‌ی سخت (offsetِ عمودی).
 */
object AppElevation {
    /** سایه‌ی «برجسته» - زیرِ دکمه‌ی اصلی و کارتِ رنگی. */
    val raised = 4.dp

    /** سایه‌ی خنثی - زیرِ کارتِ سفید و چیپِ قابلِ لمس. */
    val neutral = 3.dp

    /** سایه‌ی درونِ ردیف (دکمه‌ی کوچیکِ داخلِ فهرست). */
    val inRow = 3.dp

    /** حالتِ فشرده‌شده - سایه تا این حد جمع می‌شه و خودِ المان به همون اندازه پایین می‌ره. */
    val pressed = 1.dp
}

/**
 * ضخامتِ حاشیه‌ها.
 */
object AppStroke {
    /** حاشیه‌ی کارت. */
    val card = 2.dp

    /** حاشیه‌ی ردیفِ فهرست و فیلد. */
    val row = 1.5.dp

    /** ضخامتِ خطِ آیکون‌ها (طبقِ بخشِ «۱۰ · آیکون»: ۲٫۳ تا ۲٫۸). */
    val icon = 2.3.dp

    /** ضخامتِ خطِ آیکونِ تبِ **فعال** - عمداً ضخیم‌تر. */
    val iconActive = 2.6.dp
}

/**
 * **سایه‌ی سختِ عمودی** - امضای بصریِ کلِ این سبک.
 *
 * برخلافِ `Modifier.shadow()`ِ استانداردِ Compose که یه سایه‌ی **تار** می‌سازه، این یه کپیِ دقیقِ
 * همون شکل رو با [offsetY] پیکسل پایین‌تر و بدونِ هیچ محوشدگی می‌کشه - معادلِ `box-shadow: 0 Npx 0 c`
 * تو CSS که تو کلِ فایلِ طراحی استفاده شده.
 *
 * ⚠️ این Modifier **فقط سایه** رو می‌کشه؛ خودِ پس‌زمینه/حاشیه رو جدا بذار و **بعد** از این بیارش:
 * ```
 * Modifier
 *     .hardShadow(AppShadowNeutral, AppElevation.neutral, AppRadius.card)
 *     .clip(RoundedCornerShape(AppRadius.card))
 *     .background(AppSurface)
 * ```
 *
 * @param color رنگِ سایه - همیشه تیره‌ترِ **همون رنگِ** المان (نه خاکستریِ عمومی).
 * @param offsetY فاصله‌ی عمودی. با فشرده‌شدن به [AppElevation.pressed] می‌رسه.
 * @param cornerRadius گوشه‌گردی - باید با گوشه‌گردیِ خودِ المان یکی باشه وگرنه لبه بیرون می‌زنه.
 */
fun Modifier.hardShadow(
    color: Color,
    offsetY: Dp,
    cornerRadius: Dp,
): Modifier = this.drawBehind {
    if (color.alpha == 0f || offsetY <= 0.dp) return@drawBehind
    val dy = offsetY.toPx()
    val r = cornerRadius.toPx().coerceAtMost(size.minDimension / 2f)
    drawRoundRect(
        color = color,
        topLeft = Offset(0f, dy),
        size = Size(size.width, size.height),
        cornerRadius = CornerRadius(r, r),
    )
}

/**
 * ته‌رنگِ **ماتِ** یه رنگِ پویا رو سطحِ کارت.
 *
 * سیستمِ طراحی صریحاً «سطحِ کاملاً مات» می‌خواد و برای ته‌رنگ‌های ثابت هم توکنِ مخصوصِ خودشون رو
 * داره (`AppPrimaryPill`، `AppInfoPill`، …). ولی چند جا رنگ **پویاست** (رنگِ وضعیتِ چک، رنگِ
 * بانک) و توکنِ آماده نداره؛ اونجا به‌جای `alpha` - که رنگِ پشتش رو نشون می‌ده - همون رنگ با
 * `compositeOver` رو سطح **ترکیب** می‌شه و نتیجه یه رنگِ کاملاً کدره.
 */
@Composable
fun Color.pillOverSurface(alpha: Float = 0.14f, surface: Color = AppSurface): Color =
    copy(alpha = alpha).compositeOver(surface)
