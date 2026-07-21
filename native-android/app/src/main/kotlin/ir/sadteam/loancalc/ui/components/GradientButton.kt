package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.ui.haptics.rememberBuzz
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppGlassBase
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * پورت افکت گرادینت رو دکمه‌های اصلی CTA (به‌جای رنگ صاف تخت `Button` معمولی) برای حس «امروزی‌تر» -
 * جایگزین `Button(colors = ButtonDefaults.buttonColors(containerColor = AppPrimary))` تو همه‌ی
 * دکمه‌های اصلی اپ. ظاهر/رفتار (ripple، enabled/disabled، شکل گرد) مثل `Button` معمولیه، فقط
 * محتوای هر call site (معمولاً یه `Text`) بدون تغییر کار می‌کنه چون [content] هم مثل `Button` یه
 * `RowScope.() -> Unit` هست.
 *
 * **سبکِ شیشه‌ای** (خواسته‌ی صریح کاربر، هم‌زمان با شیشه‌ای‌شدنِ [AppCard]، با پیش‌نمایشِ تاییدشده):
 * پس‌زمینه دیگه یه گرادینتِ تخت‌رنگِ [AppPrimary]→primaryDim نیست - یه گرادینتِ فیروزه‌ایِ نیمه‌شفاف
 * (برای حفظِ هویتِ CTA) رو یه پایه‌ی شیشه‌ای‌ِ تم‌آگاه (همون [AppGlassBase]ی AppCard) نشسته، تا هم
 * زبونِ باکس‌ها رو داشته باشه هم رنگِ اصلیِ دکمه گم نشه. رنگِ متن از سیاهِ ثابتِ قبلی
 * (`Color(0xFF04211C)`، مخصوصِ زمینه‌ی تخت‌رنگِ روشن) به [AppText] عوض شد چون [AppText] خودش
 * تم‌آگاهه و رو زمینه‌ی شیشه‌ایِ تیره‌ترِ جدید (چه تمِ روشن چه تیره) خوانا می‌مونه.
 *
 * **افکتِ شیمر (باگِ رفع‌شده)**: قبلاً بازه‌ی حرکتِ نوارِ نور یه عددِ ثابت/حدسی بود (`shimmerX * 300f`)،
 * بدونِ توجه به عرضِ واقعیِ دکمه - رو دکمه‌های پهن‌تر، نوار وسطِ راه از این‌ور به اون‌ور «می‌پرید» چون
 * چرخه‌ی بعدی (`RepeatMode.Restart`) وقتی نوار هنوز داخلِ دکمه (نه کاملاً بیرون از لبه‌ش) بود ریست
 * می‌شد. الان با `Modifier.onSizeChanged` عرضِ واقعیِ دکمه اندازه‌گیری می‌شه و بازه‌ی حرکت طوری حساب
 * می‌شه که نوار همیشه **کاملاً بیرون از لبه‌ی راست شروع** و **کاملاً بیرون از لبه‌ی چپ تموم** بشه (و
 * برعکس) - یعنی لحظه‌ی ریست‌شدنِ چرخه دقیقاً وقتیه که نوار نامرئیه، پس هیچ پرشی حس نمی‌شه.
 * آلفای نوار هم به‌خواستِ کاربر («خیلی ظریف باشه، انگار یک سایه‌ست») از ۰.۶ به ۰.۱۴ کم شد - دیگه یه
 * نوارِ طلاییِ پررنگ نیست، فقط یه هایلایتِ ظریفِ عبوریه.
 */
@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    val glassGradient = Brush.linearGradient(
        listOf(AppPrimary.copy(alpha = 0.38f), AppPrimary.copy(alpha = 0.10f)),
    )
    val buzz = rememberBuzz()
    val shimmerTransition = rememberInfiniteTransition(label = "buttonShimmer")
    // ۰→۱ (نه یه بازه‌ی حدسیِ منفی/مثبت) - محاسبه‌ی واقعیِ مختصات از رو عرضِ اندازه‌گیری‌شده انجام می‌شه.
    val shimmerT by shimmerTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerT",
    )
    var widthPx by remember { mutableFloatStateOf(0f) }
    Surface(
        onClick = { buzz(); onClick() },
        enabled = enabled,
        modifier = modifier
            .height(48.dp)
            .onSizeChanged { widthPx = it.width.toFloat() },
        shape = shape,
        color = Color.Transparent,
        contentColor = AppText,
        border = BorderStroke(1.dp, AppAccent.copy(alpha = 0.45f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(AppGlassBase)
                .background(glassGradient)
                .alpha(if (enabled) 1f else 0.5f),
            contentAlignment = Alignment.Center,
        ) {
            if (enabled && widthPx > 0f) {
                // نوار (streak) ۳۴٪ از عرضِ دکمه‌ست؛ مرکزش از -(نصفِ عرض + نصفِ عرضِ نوار) تا
                // +(نصفِ عرض + نصفِ عرضِ نوار) حرکت می‌کنه - یعنی هر دو سرِ حرکت کاملاً بیرون از دکمه‌ست.
                val halfStreak = widthPx * 0.17f
                val centerX = (shimmerT * 2f - 1f) * (widthPx / 2f + halfStreak)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color.Transparent, AppAccent.copy(alpha = 0.14f), Color.Transparent),
                                start = Offset(centerX - halfStreak, 0f),
                                end = Offset(centerX + halfStreak, widthPx * 0.25f),
                            ),
                        ),
                )
            }
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}
