package ir.sadteam.loancalc.ui.components

import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Size
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.ui.haptics.rememberBuzz
import ir.sadteam.loancalc.ui.theme.AppElevation
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.hardShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.CircleShape

/**
 * دکمه‌ی شناورِ افزودن - **بازطراحیِ سبکِ «جیبک»**.
 *
 * جایگزینِ `FloatingActionButton`ِ Material3 که سایه‌ی **تارِ** خودش رو می‌سازه و با زبانِ
 * «سایه‌ی سختِ بدونِ تاری»ِ این سبک جور در نمیاد. همون رفتارِ فشرده‌شدنِ [GradientButton] رو داره
 * (سایه جمع می‌شه، دکمه به همون اندازه پایین می‌ره).
 *
 * ⚠️ این با «دکمه‌ی شناورِ **میانیِ** نوارِ ناوبری» فرق داره - اون یه‌بار اضافه و به‌خواستِ صریحِ
 * کاربر برداشته شد و سیستمِ طراحی هم صریحاً می‌گه نباشه. این دکمه‌ی گوشه‌ی صفحه‌ست و می‌مونه.
 */
@Composable
fun AppFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Add,
    contentDescription: String? = "افزودن",
    /**
     * نشانکِ ریزِ «+» روی گوشه‌ی پایین-چپ (جوابِ طراح، دورِ ۸).
     *
     * فقط برای FABای که مقصدش **افزودن نیست ولی کارِ نهایی‌اش افزودن است** - مثلِ FABِ تبِ
     * وام که ماشین‌حساب را باز می‌کند. «+»ِ خالی وعده‌ی فرم می‌دهد و ماشین‌حساب نشان
     * می‌دهد؛ ماشین‌حسابِ تنها هم یک‌دستیِ FAB را در پنج تبِ دیگر می‌شکند.
     */
    plusBadge: Boolean = false,
) {
    // ⚠️ **دایره، نه گردگوشه.** فریمِ `15a` صریحاً `border-radius:50%` داره. اندازه ۵۶،
    // سایه‌ی سختِ ۵ پیکسلی، و یه گرادیانِ شعاعیِ ملایم (`#17C57D` → `#0EA968` تا ۶۵٪).
    //
    // فریم یه هاله‌ی تارِ طلایی هم داره (`0 7px 16px rgba(185,134,11,.28)`) که **عمداً
    // پیاده نشد** - بندِ ۳ سیستمِ طراحی صریحاً می‌گه «هیچ سایه‌ی تارِ رنگی؛ عمق فقط با
    // سایه‌ی سختِ عمودی». تنها جایی که فریم و سیستمِ طراحی با هم مخالف‌ان و قاعده رو ترجیح دادم.
    // 🎨 طرحِ ChatGPT (۳ مهر): دکمه‌ی شیشه‌ایِ براق - گرادیانِ سه‌رنگ از **خودِ رنگِ تم**
    // (پس با تم عوض می‌شود؛ قبلاً یک سبزِ ثابت `#17C57D` داشت)، هاله‌ی نرمِ هم‌رنگ، برقِ
    // سفیدِ بالا و «+»ِ سفیدِ گرد.
    val shape = CircleShape
    val buzz = rememberBuzz()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, tween(90), label = "fabScale")
    val primary = AppPrimary
    val light = lerp(primary, Color.White, 0.30f)
    val deep = lerp(primary, Color.Black, 0.28f)

    Box(
        modifier = modifier
            .size(58.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(14.dp, shape, ambientColor = primary.copy(alpha = 0.55f), spotColor = primary.copy(alpha = 0.55f))
            .clip(shape)
            .background(Brush.linearGradient(listOf(light, primary, deep)))
            .drawWithContent {
                drawContent()
                // برقِ شیشه‌ایِ نیمه‌ی بالا.
                drawOval(
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.42f), Color.White.copy(alpha = 0f)),
                        startY = size.height * 0.04f,
                        endY = size.height * 0.52f,
                    ),
                    topLeft = Offset(size.width * 0.14f, size.height * 0.05f),
                    size = Size(size.width * 0.72f, size.height * 0.46f),
                )
                // لبه‌ی روشنِ ظریف.
                drawCircle(
                    color = Color.White.copy(alpha = 0.28f),
                    radius = size.minDimension / 2f - 1.dp.toPx(),
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            }
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = { buzz(); onClick() },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (icon == Icons.Filled.Add) {
            // «+»ِ ضخیم با سرِ گرد و سایه‌ی نرمِ زیرش، مثلِ طرح.
            Canvas(modifier = Modifier.size(24.dp).semantics { contentDescription?.let { this.contentDescription = it } }) {
                val w = 5.5.dp.toPx()
                val c = center
                val half = size.minDimension / 2f - w / 2f
                val shadowOffset = Offset(0f, 1.5.dp.toPx())
                listOf(Color.Black.copy(alpha = 0.18f) to shadowOffset, Color.White to Offset.Zero).forEach { (col, o) ->
                    drawLine(col, Offset(c.x - half, c.y) + o, Offset(c.x + half, c.y) + o, w, StrokeCap.Round)
                    drawLine(col, Offset(c.x, c.y - half) + o, Offset(c.x, c.y + half) + o, w, StrokeCap.Round)
                }
            }
        } else {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
        if (plusBadge) {
            // دایره‌ی ریز با حاشیه‌ی سفید، تا روی گرادیان بنشیند و جزئی از نماد دیده شود.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 11.dp, bottom = 11.dp)
                    .size(15.dp)
                    .clip(shape)
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    tint = AppPrimary,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
    }
}

