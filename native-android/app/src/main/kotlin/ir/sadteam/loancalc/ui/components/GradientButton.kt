package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.haptics.rememberBuzz
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDisabledFill
import ir.sadteam.loancalc.ui.theme.AppDisabledText
import ir.sadteam.loancalc.ui.theme.AppElevation
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSpacing
import ir.sadteam.loancalc.ui.theme.AppStroke
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.hardShadow

/**
 * پنج گونه‌ی دکمه - از بخشِ «۴ · دکمه‌ها»ی سیستمِ طراحی.
 */
enum class AppButtonVariant {
    /** اقدامِ اصلیِ صفحه. **فقط یکی در هر صفحه.** سبزِ پرشده با سایه‌ی سخت. */
    PRIMARY,

    /** جایگزینِ هم‌ارز - حاشیه‌ی سبز، **بدونِ سایه**. */
    SECONDARY,

    /** انصراف، بعداً، رد کردن - خنثی و بی‌سایه. */
    NEUTRAL,

    /** حذف. **همیشه با تاییدِ دوم.** */
    DESTRUCTIVE,

    /** دکمه‌ی کوچیکِ درونِ ردیفِ فهرست - سایه‌ی ۳ پیکسل. */
    IN_ROW,
}

/**
 * دکمه‌ی اصلیِ اپ - **بازطراحیِ سبکِ «جیبک»** (مرداد ۱۴۰۵).
 *
 * ⚠️ اسمِ `GradientButton` **تاریخیه و دیگه گرادیانی در کار نیست** - عمداً عوض نشد چون ده‌ها فایل
 * صداش می‌زنن و تغییرِ اسم فقط سروصدای بی‌خودیِ دیف می‌سازه. تو سبکِ جدید:
 * - پرشدنِ **تخت و مات** (`#0EA968`)، نه گرادیانِ نیمه‌شفاف
 * - کپسولِ کامل (گوشه‌ی ۹۹۹)، نه گوشه‌ی ۱۴dp
 * - سایه‌ی **سختِ `0 4px 0 #0B8C57`**، نه سایه‌ی تارِ Material
 * - **فشرده‌شدن**: سایه به ۱ پیکسل جمع می‌شه و خودِ دکمه به همون اندازه پایین می‌ره - همون حسِ
 *   «کلیدِ فیزیکی» که امضای این سبکه
 * - نوارِ شیمرِ طلاییِ دورِ قبل **حذف شد** (قاعده: طلایی فقط نشانه‌ی پرمیوم/اشتراکه)
 *
 * @param variant گونه‌ی دکمه - رجوع کن به [AppButtonVariant].
 * @param fullWidth دکمه‌ی اصلی معمولاً تمامِ عرض رو می‌گیره؛ برای دکمه‌ی درونِ ردیف `false`.
 */
@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: AppButtonVariant = AppButtonVariant.PRIMARY,
    content: @Composable RowScope.() -> Unit,
) {
    val shape = RoundedCornerShape(AppRadius.button)
    val buzz = rememberBuzz()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val fill: Color = when {
        !enabled -> AppDisabledFill
        variant == AppButtonVariant.PRIMARY || variant == AppButtonVariant.IN_ROW -> AppPrimary
        variant == AppButtonVariant.DESTRUCTIVE -> AppDanger
        else -> AppSurface
    }
    val ink: Color = when {
        !enabled -> AppDisabledText
        variant == AppButtonVariant.PRIMARY ||
            variant == AppButtonVariant.IN_ROW ||
            variant == AppButtonVariant.DESTRUCTIVE -> Color.White
        variant == AppButtonVariant.SECONDARY -> AppPrimaryInk
        else -> AppMuted
    }
    val stroke: BorderStroke? = when {
        !enabled -> null
        variant == AppButtonVariant.SECONDARY -> BorderStroke(AppStroke.card, AppPrimary)
        variant == AppButtonVariant.NEUTRAL -> BorderStroke(AppStroke.card, AppLine)
        else -> null
    }
    // دکمه‌ی خاموش و دکمه‌ی «دومین/خنثی» عمداً بی‌سایه‌ان - قاعده‌ی صریحِ سیستمِ طراحی.
    val restingShadow = when {
        !enabled -> 0.dp
        variant == AppButtonVariant.SECONDARY || variant == AppButtonVariant.NEUTRAL -> 0.dp
        variant == AppButtonVariant.IN_ROW -> AppElevation.inRow
        else -> AppElevation.raised
    }
    val shadowColor: Color = when {
        restingShadow == 0.dp -> Color.Transparent
        variant == AppButtonVariant.DESTRUCTIVE -> Color(0xFFB32B2B)
        else -> AppPrimaryDim
    }

    // فشرده‌شدن: سایه جمع می‌شه و دکمه دقیقاً به همون اندازه پایین می‌ره، پس لبه‌ی بالاییِ دکمه
    // ثابت می‌مونه و فقط ضخامتِ «کلید» کم می‌شه - همون حسِ فشردنِ کلیدِ فیزیکی.
    val targetShadow = if (pressed && restingShadow > AppElevation.pressed) AppElevation.pressed else restingShadow
    val shadow by animateDpAsState(targetShadow, tween(70), label = "btnShadow")
    val sink by animateDpAsState(restingShadow - shadow, tween(70), label = "btnSink")

    val minHeight = if (variant == AppButtonVariant.IN_ROW) 34.dp else AppSpacing.minTouchTarget
    val hPad = if (variant == AppButtonVariant.IN_ROW) 14.dp else 22.dp

    Box(
        modifier = modifier
            .offset(y = sink)
            .hardShadow(shadowColor, shadow, AppRadius.button)
            .clip(shape)
            .background(fill)
            .then(if (stroke != null) Modifier.border(stroke, shape) else Modifier)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = { buzz(); onClick() },
            )
            .defaultMinSize(minHeight = minHeight)
            .padding(horizontal = hPad, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides ink,
            LocalTextStyle provides LocalTextStyle.current.copy(
                color = ink,
                fontSize = if (variant == AppButtonVariant.IN_ROW) 10.5.sp else 13.sp,
                fontWeight = FontWeight.Black,
            ),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}
