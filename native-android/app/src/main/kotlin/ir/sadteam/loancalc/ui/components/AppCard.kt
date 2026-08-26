package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppElevation
import ir.sadteam.loancalc.ui.theme.AppGoldBorder
import ir.sadteam.loancalc.ui.theme.AppGoldFrom
import ir.sadteam.loancalc.ui.theme.AppGoldInk
import ir.sadteam.loancalc.ui.theme.AppGoldInk2
import ir.sadteam.loancalc.ui.theme.AppGoldTo
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppShadowNeutral
import ir.sadteam.loancalc.ui.theme.AppSpacing
import ir.sadteam.loancalc.ui.theme.AppStroke
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.LocalAppColors
import ir.sadteam.loancalc.ui.theme.hardShadow

/**
 * چهار گونه‌ی کارت - از بخشِ «۵ · کارت‌ها»ی سیستمِ طراحی. هر کدوم زمینه/حاشیه/سایه‌ی خودش رو داره.
 */
enum class AppCardVariant {
    /** سطحِ سفید، حاشیه‌ی ۲ پیکسل. برای هر محتوای بی‌طرف. */
    DEFAULT,

    /** کاغذِ طلایی - **فقط** کارتِ پول و دستاورد. */
    GOLD,

    /** سررسیدِ امروز و فردا. **حداکثر یکی در هر صفحه، بالای فهرست.** */
    URGENT,

    /** تسویه‌شده و خوانده‌شده - شفافیتِ ۰٫۷۲ و بدونِ سایه. */
    DONE,
}

/**
 * کارتِ مشترکِ کلِ اپ - **بازطراحیِ سبکِ «جیبک»** (مرداد ۱۴۰۵).
 *
 * قاعده‌ی ماندگار: **هیچ کارتی رو دستی با `clip/background/border` نساز - همیشه این.**
 *
 * ⚠️ **تغییرِ بنیادی نسبت به دورِ قبل**: سبکِ «Liquid Glass» (پایه‌ی نیمه‌شفاف + گرادیانِ نوری +
 * هایلایتِ گوشه + سایه‌ی تارِ ۱۴dp) **کاملاً حذف شد**. حالا:
 * - سطحِ **کاملاً مات** (`#FFFFFF` روشن / `#1B2530` تیره)
 * - حاشیه‌ی **۲ پیکسلیِ توپر** (`#E3ECE7` / `#2A3640`)
 * - سایه‌ی **سختِ عمودیِ بدونِ تاری** (`0 3px 0 #E8EFEB`) - رجوع کن به [hardShadow]
 * - گوشه‌ی ۲۰dp (قبلاً ۲۲) و حاشیه‌ی درونیِ ۱۶dp (قبلاً ۱۴)
 *
 * **باگِ تاریخیِ رفع‌شده که هنوز برقراره**: این کامپوننت `Surface` نیست، پس `LocalContentColor` رو
 * خودکار حساب نمی‌کنه. برای همین محتوا تو `CompositionLocalProvider(LocalContentColor provides ...)`
 * پیچیده شده - اگه جایی `Text`ِ بدونِ `color`ِ صریح نامرئی شد، **اول چک کن بیرونِ `AppCard` نباشه**.
 *
 * @param variant گونه‌ی کارت - رجوع کن به [AppCardVariant].
 * @param label عنوانِ اختیاریِ بالای کارت.
 * @param borderColor override برای کارتی که واقعاً حاشیه‌ی رنگیِ خودش رو لازم داره.
 * @param backgroundColor override برای کارتی که واقعاً زمینه‌ی رنگیِ خودش رو لازم داره - وقتی
 *   ست بشه [variant] نادیده گرفته می‌شه.
 * @param accentGradient لایه‌ی گرادیانِ اختیاری برای کارتِ «قهرمانِ» صفحه (مانده‌ی کلِ خانه/دارایی).
 *   ⚠️ تو سبکِ جدید **کم استفاده کن** - قاعده‌ی «حداکثر یک رنگِ لهجه در هر صفحه».
 * @param contentPadding حاشیه‌ی درونی؛ برای کارتی که خودش لیستِ لبه‌به‌لبه داره `0.dp` بده.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    variant: AppCardVariant = AppCardVariant.DEFAULT,
    label: String? = null,
    borderColor: Color? = null,
    backgroundColor: Color? = null,
    accentGradient: Brush? = null,
    // طبقِ بندِ ۴ فایلِ توکنِ طراحی پدینگِ خودِ کارت `cardPaddingTight` (۱۴)ه؛
    // ۱۶ مالِ کارتِ قهرمانه. قبلاً هر دو ۱۶ بودن.
    contentPadding: androidx.compose.ui.unit.Dp = AppSpacing.cardPaddingTight,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(AppRadius.card)
    val colors = LocalAppColors.current

    // زمینه: یا صریح از بیرون، یا از رو گونه‌ی کارت.
    val fillBrush: Brush? = when {
        backgroundColor != null -> null
        variant == AppCardVariant.GOLD -> Brush.linearGradient(listOf(AppGoldFrom, AppGoldTo))
        else -> null
    }
    val fillColor: Color = backgroundColor ?: when (variant) {
        AppCardVariant.DEFAULT -> AppSurface
        AppCardVariant.GOLD -> AppGoldFrom // زیرِ گرادیان؛ برای وقتی گرادیان رسم نشه
        AppCardVariant.URGENT -> colors.urgentBg
        AppCardVariant.DONE -> AppSurface2
    }
    val strokeColor: Color = borderColor ?: when (variant) {
        AppCardVariant.DEFAULT -> AppLine
        AppCardVariant.GOLD -> AppGoldBorder
        AppCardVariant.URGENT -> colors.urgentBorder
        AppCardVariant.DONE -> AppLineRow
    }
    val strokeWidth = if (variant == AppCardVariant.GOLD) AppStroke.row else AppStroke.card
    // کارتِ «تمام‌شده» عمداً بی‌سایه‌ست - قاعده‌ی صریحِ سیستمِ طراحی.
    val shadowColor: Color = when (variant) {
        AppCardVariant.DONE -> Color.Transparent
        AppCardVariant.URGENT -> colors.urgentShadow
        else -> AppShadowNeutral
    }
    val shadowOffset = if (variant == AppCardVariant.URGENT) AppElevation.raised else AppElevation.neutral
    // رنگِ متنِ پیش‌فرضِ داخلِ کارت - رو کاغذِ طلایی باید جوهرِ طلایی باشه نه متنِ معمولی.
    val ink = if (variant == AppCardVariant.GOLD) AppGoldInk else AppText
    val labelInk = if (variant == AppCardVariant.GOLD) AppGoldInk2 else AppMuted

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (variant == AppCardVariant.DONE) Modifier.alpha(0.72f) else Modifier)
            .hardShadow(shadowColor, shadowOffset, AppRadius.card)
            .clip(shape)
            .background(fillColor)
            .then(if (fillBrush != null) Modifier.background(fillBrush) else Modifier)
            .then(if (accentGradient != null) Modifier.background(accentGradient) else Modifier)
            .border(strokeWidth, strokeColor, shape)
            .padding(contentPadding),
    ) {
        CompositionLocalProvider(LocalContentColor provides ink) {
            if (label != null) {
                Text(
                    text = label,
                    color = labelInk,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            content()
        }
    }
}
