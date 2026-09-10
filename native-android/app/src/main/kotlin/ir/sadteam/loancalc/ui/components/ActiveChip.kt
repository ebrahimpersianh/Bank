package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.toFa
import kotlin.math.cos
import kotlin.math.sin
import ir.sadteam.loancalc.ui.theme.AppWarningPill
import ir.sadteam.loancalc.ui.theme.AppWarningInk
import ir.sadteam.loancalc.ui.theme.AppWarning
import ir.sadteam.loancalc.ui.theme.AppSpacing
import ir.sadteam.loancalc.ui.theme.AppText
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.Icons

/**
 * قرصِ **«فعال»** و شمارنده‌ی **سکه** برای نوارِ بالای خانه (کارتِ `34c`: «چیپِ نوارِ بالای
 * خانه ۱۱px»).
 *
 * ⚠️ واژه‌ی «استریک» عمداً هیچ‌جا نیومده - خواسته‌ی صریحِ کاربر «فعال»ه.
 *
 * ⚠️ **خواسته‌ی صریحِ کاربر**: از **۷ روز به بالا** آیکونِ قرص باید **تارِ عنکبوت با عنکبوت**
 * باشه (نه شعله و نه هیچ نمادِ دیگه). زیرِ ۷ روز همون زنجیرِ کوچیکِ بخشِ ۳۴ می‌مونه.
 */
@Composable
fun ActiveChip(days: Int, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            // رنگِ قرص طبقِ فریم نارنجیِ کم‌رنگه (`#FFF1DC`)، نه سبز.
            .background(AppWarningPill)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        if (days >= 7) {
            SpiderWebIcon(size = 12.dp, color = AppWarning)
        } else {
            // زیرِ ۷ روز: خودِ زنجیرِ بخشِ ۳۴ تو کوچک‌ترین اندازه‌ش.
            // فریمِ `15a` زیرِ ۷ روز یه **شعله**ی ۱۲ پیکسلی داره، نه حلقه‌های زنجیر.
            Icon(
                Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = AppWarning,
                modifier = Modifier.size(12.dp),
            )
        }
        // ⚠️ فریمِ `15a` فقط **عدد** داره، نه «X روز فعال» - متنِ اضافه قرص رو پهن می‌کرد.
        Text(
            toFa(days),
            color = AppWarningInk,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

/**
 * شمارنده‌ی سکه - همون قرص، ولی طلایی، چون طلایی نشانه‌ی پاداش/پرمیومه.
 *
 * @param compact عددِ سکه را برمی‌دارد و فقط خودِ سکه می‌مانَد. **اولین چیزی است که در
 * تنگنا کوتاه می‌شود** (فریمِ `55b`: عددِ سکه ← تاریخ ← نام). نام آخر است چون تنها چیزِ
 * شخصیِ آن ردیف است.
 */
@Composable
fun CoinChip(
    coins: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    compact: Boolean = false,
) {
    // ⚠️ فریمِ `15a` برای سکه **قرص نداره** - فقط عدد و بعدش خودِ سکه.
    // `onClick` اختیاری است: هدفِ لمسی فقط وقتی ساخته می‌شود که مقصدی باشد، وگرنه یک
    // دکمه‌ی بی‌کار روی نوارِ بالا می‌نشیند و تپ‌های اطرافش را می‌خورد.
    //
    // ⚠️ `.padding()` **بعد از** `.clickable()` است و این باگ بود (قاعده‌ی ۴): پدینگِ
    // بعدِ کلیک‌پذیری هدفِ لمسی را **کوچک** می‌کند. حالا پدینگ اول می‌آید، و در حالتِ
    // `compact` که فقط یک سکه‌ی ۱۵ پیکسلی می‌مانَد، جعبه‌ی ۴۴ اجباری است وگرنه هدف به
    // نصفِ حداقل می‌رسد.
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .then(
                if (onClick != null && compact) {
                    Modifier.size(AppSpacing.minTouchTarget)
                } else {
                    Modifier
                },
            )
            .then(if (onClick != null) Modifier.clip(RoundedCornerShape(10.dp)) else Modifier)
            .then(
                if (onClick != null && !compact) {
                    Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                } else {
                    Modifier
                },
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        if (!compact) {
            Text(
                toFa(coins),
                color = AppText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
            )
        }
        CoinIcon(
            size = if (compact) 17.dp else 15.dp,
            modifier = if (compact) Modifier else Modifier.padding(start = 4.dp),
        )
    }
}

/**
 * **تارِ عنکبوت با عنکبوت** - نشانِ ۷ روز به بالای «فعال» (خواسته‌ی صریحِ کاربر).
 *
 * دستی کشیده شده چون تو ستِ آیکونِ Material چنین شکلی نیست. شش پرتوِ شعاعی + سه کمانِ
 * حلقوی + یه بدنِ کوچیکِ عنکبوت وسط.
 */
@Composable
fun SpiderWebIcon(size: Dp, color: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = this.size.minDimension / 2f
            val c = Offset(this.size.width / 2f, this.size.height / 2f)
            val stroke = r * 0.13f
            val spokes = 6
            // پرتوهای شعاعی
            repeat(spokes) { i ->
                val a = (Math.PI * 2 / spokes * i).toFloat()
                drawLine(
                    color = color,
                    start = c,
                    end = Offset(c.x + cos(a) * r, c.y + sin(a) * r),
                    strokeWidth = stroke,
                )
            }
            // کمان‌های حلقوی - بینِ هر دو پرتو یه خطِ صاف کشیده می‌شه، پس تار «چندضلعی» می‌شه،
            // دقیقاً مثلِ تارِ واقعی.
            listOf(0.42f, 0.70f, 0.96f).forEach { ring ->
                val path = Path()
                repeat(spokes + 1) { i ->
                    val a = (Math.PI * 2 / spokes * i).toFloat()
                    val p = Offset(c.x + cos(a) * r * ring, c.y + sin(a) * r * ring)
                    if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                }
                drawPath(path, color = color, style = Stroke(width = stroke * 0.8f))
            }
            // عنکبوت: بدنِ بیضی + هشت پا (چهار جفتِ کوتاه).
            drawCircle(color = color, radius = r * 0.17f, center = c)
            repeat(4) { i ->
                val a = (Math.PI / 4 * (i + 0.5)).toFloat()
                listOf(1f, -1f).forEach { dir ->
                    drawLine(
                        color = color,
                        start = c,
                        end = Offset(
                            c.x + cos(a) * r * 0.34f * dir,
                            c.y + sin(a) * r * 0.34f * dir,
                        ),
                        strokeWidth = stroke * 0.7f,
                    )
                }
            }
        }
    }
}
