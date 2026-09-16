package ir.sadteam.loancalc.ui.components

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
    val shape = CircleShape
    val buzz = rememberBuzz()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val shadow by animateDpAsState(
        if (pressed) AppElevation.pressed else FabShadow,
        tween(70),
        label = "fabShadow",
    )
    val sink by animateDpAsState(
        FabShadow - shadow,
        tween(70),
        label = "fabSink",
    )

    Box(
        modifier = modifier
            .offset(y = sink)
            .size(56.dp)
            .hardShadow(AppPrimaryDim, shadow, 28.dp)
            .clip(shape)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF17C57D), AppPrimary),
                    center = Offset(0.32f * 56f, 0.26f * 56f),
                    // مرکزِ ۳۲٪/۲۶٪ و پایانِ گرادیان رو ۶۵٪ - مقدارِ صریحِ فریمِ `15a`.
                    radius = 56f * 0.65f,
                ),
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = { buzz(); onClick() },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(24.dp),
        )
        if (plusBadge) {
            // دایره‌ی ریز **هم‌رنگِ خودِ FAB** با حاشیه‌ی سفید، تا روی گرادیان بنشیند و
            // جزئی از نماد دیده شود نه یک بجِ چسبانده‌شده.
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

/** سایه‌ی سختِ دکمه‌ی شناور - ۵ پیکسل طبقِ فریم، نه ۴ پیکسلِ دکمه‌های معمولی. */
private val FabShadow = 5.dp
