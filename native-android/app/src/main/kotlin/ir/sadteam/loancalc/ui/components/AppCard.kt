package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppGlassBase
import ir.sadteam.loancalc.ui.theme.AppGlassBorder
import ir.sadteam.loancalc.ui.theme.AppGlassGradientEnd
import ir.sadteam.loancalc.ui.theme.AppGlassGradientStart
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppText

/** پورت .card تو www/index.html - قبلاً یه `Surface` تخت‌رنگ بود. الان (خواسته‌ی صریح کاربر، با
 * پیش‌نمایشِ تاییدشده - الهام‌گرفته از باکس‌های اپِ «تقویم من») سبکِ «شیشه‌ای» داره: یه پایه‌ی
 * نیمه‌شفافِ تم‌آگاه (`AppGlassBase`) + یه گرادیانِ نورِ ظریف روش (`AppGlassGradientStart/End`) + یه
 * حاشیه‌ی نازکِ نورانی (`AppGlassBorder`) - چون Compose Material3 `Surface` فقط `Color` تخت قبول
 * می‌کنه نه `Brush`، اینجا به‌جای `Surface` مستقیم از `Modifier.shadow/clip/background/border` رو
 * یه `Column` استفاده شده. [borderColor] برای مواردی که یه کارتِ خاص واقعاً به خط دور رنگیِ خودش
 * نیاز داره (مثل کارت وام‌های پرتکرار) همچنان override می‌شه. لیبلِ بالای کارت طلایی نیست (تصمیمِ
 * قدیمی‌تر کاربر: طلایی فقط رو پس‌زمینه/حاشیه، نه رو فونت).
 *
 * [backgroundColor] (رنگِ زمینه‌ی صریح، نه شیشه‌ای) برای مواردی که کارت واقعاً باید رنگ زمینه‌ی خاص
 * خودش داشته باشه override می‌شه - عمداً از مسیرِ شیشه‌ای رد نمی‌شه و همون `Surface` تخت‌رنگِ قدیمی
 * می‌مونه: یه رنگِ نیمه‌شفافِ اضافه رو یه پس‌زمینه‌ی از قبل رنگی راحت دوتُنی/کثیف به‌نظر می‌رسه؛ کارتِ
 * تحلیلِ درآمد تو MyLoansScreen قبلاً دقیقاً همین مشکل رو داشت.
 *
 * **باگِ رفع‌شده (ریشه‌ای)**: `Surface` خودش خودکار `LocalContentColor` رو از رو رنگِ زمینه‌ش حساب
 * می‌کرد (`contentColorFor`)، پس `Text`های داخلِ کارت که `color` صریح نداشتن (مثلاً اسمِ بانک تو
 * `BankTile`، عنوانِ `PresetCard`) خودکار رنگِ درستِ تم‌آگاه می‌گرفتن. وقتی مسیرِ پیش‌فرض از
 * `Surface` به یه `Column` خام عوض شد (برای پشتیبانیِ گرادیانِ شیشه‌ای)، این محاسبه‌ی خودکار از دست
 * رفت و همچین `Text`هایی رو دارک‌مود مشکی/نامرئی می‌شدن. الان با `CompositionLocalProvider
 * (LocalContentColor provides AppText)` همون رفتار دستی برگردونده شده - اگه بازم جایی تو اپ متنِ
 * بدونِ `color` صریح داخلِ یه `AppCard` نامرئی بود، این همون علتشه. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    label: String? = null,
    borderColor: Color? = null,
    backgroundColor: Color? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    if (backgroundColor != null) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = backgroundColor,
            tonalElevation = 3.dp,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, borderColor ?: Color.Transparent),
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                AppCardLabelAndContent(label, content)
            }
        }
    } else {
        val glassGradient = Brush.linearGradient(listOf(AppGlassGradientStart, AppGlassGradientEnd))
        Column(
            modifier = modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = shape,
                    ambientColor = Color.Black.copy(alpha = 0.22f),
                    spotColor = Color.Black.copy(alpha = 0.22f),
                )
                .clip(shape)
                .background(AppGlassBase)
                .background(glassGradient)
                .border(1.dp, borderColor ?: AppGlassBorder, shape)
                .padding(14.dp),
        ) {
            // رجوع کن به کامنتِ «باگِ رفع‌شده (ریشه‌ای)» بالا - جایگزینِ contentColorFor خودکارِ
            // Surface که این مسیرِ Columnِ خام دیگه نداره.
            CompositionLocalProvider(LocalContentColor provides AppText) {
                AppCardLabelAndContent(label, content)
            }
        }
    }
}

@Composable
private fun AppCardLabelAndContent(label: String?, content: @Composable () -> Unit) {
    if (label != null) {
        Text(
            text = label,
            color = AppMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 7.dp),
        )
    }
    content()
}
