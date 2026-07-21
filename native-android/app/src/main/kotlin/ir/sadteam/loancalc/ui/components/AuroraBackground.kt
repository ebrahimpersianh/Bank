package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppPrimary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * پس‌زمینه‌ی زنده‌ی «شفق» (Aurora): سه هاله‌ی گرادیانِ شعاعیِ خیلی محو (دوتا سبزآبیِ [AppPrimary]،
 * یکی طلاییِ [AppAccent] - امتدادِ همون هویتِ دوتاییِ اسپلش) که با یه چرخه‌ی چنددقیقه‌ای خیلی آروم
 * تو صفحه شناور می‌شن. پشتِ کل محتوای تب‌های اصلی می‌شینه (رجوع کن به MainActivity که containerColor
 * رو Transparent کرده) تا بینِ کارت‌ها/فاصله‌ها دیده بشه، نه روشون.
 *
 * ملاحظاتِ کارایی (مهم - این هر فریم redraw می‌شه):
 * - فقط سه drawCircle با گرادیانِ شعاعی - بدونِ blur واقعی (RenderEffect نیازِ API 31+ داره و رو
 *   گوشی‌های ضعیف سنگینه)؛ محو بودن از خودِ falloff گرادیان میاد که رو همه‌ی APIها ارزونه.
 * - حرکت با sin/cos رو یه فازِ 0..2π ئه که تهش به سرش می‌چسبه (بدون پرش موقعِ restart).
 *
 * آلفای هاله‌ها به‌نسبتِ روشناییِ تم تنظیم می‌شه: رو تمِ روشن هاله‌ی پررنگ حسِ «لکه» می‌ده، پس
 * کم‌رنگ‌تره؛ رو تمِ تاریک می‌شه پررنگ‌تر بود تا واقعاً دیده بشه.
 */
@Composable
fun AuroraBackground(modifier: Modifier = Modifier) {
    val bg = AppBg
    val primary = AppPrimary
    val accent = AppAccent
    val isDark = bg.luminance() < 0.5f
    // نسخه‌ی اول (۰.۰۹/۰.۰۶ رو تمِ روشن) اون‌قدر محو بود که کاربر اصلاً متوجهش نشد؛ دورِ دوم «بیشتر
    // بشه بد نیست» گفت؛ دورِ سوم از بینِ ۴ سطحِ پیش‌نمایش‌شده تو یه HTML، پررنگ‌ترین (سطحِ ۴) انتخاب
    // شد؛ دورِ چهارم کاربر خواستِ «دو شماره کم» - یعنی سطحِ ۲.
    val primaryAlpha = if (isDark) 0.52f else 0.39f
    val accentAlpha = if (isDark) 0.40f else 0.29f

    val transition = rememberInfiniteTransition(label = "aurora")
    // یه دورِ کامل ~۱۲ ثانیه (دورِ دومِ بازخورد کاربر: ۲۲ثانیه هم هنوز «انگار حرکت نداره» بود) -
    // همراه با دامنه‌ی جابه‌جاییِ بزرگ‌تر پایین، الان تو ۲-۳ ثانیه نگاه‌کردن حرکت واضح دیده می‌شه.
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Restart),
        label = "auroraPhase",
    )
    // یه «نفس‌کشیدنِ» آرومِ جدا برای شدتِ نور، که حرکت یکنواخت/مکانیکی حس نشه.
    val breath by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Reverse),
        label = "auroraBreath",
    )

    Canvas(modifier = modifier) {
        drawRect(bg)

        val w = size.width
        val h = size.height

        // هاله‌ی سبزآبیِ بزرگ - بالای صفحه، آروم چپ‌وراست می‌ره.
        val c1 = Offset(
            x = w * (0.65f + 0.30f * sin(phase)),
            y = h * (0.14f + 0.09f * cos(phase * 2f)),
        )
        val r1 = w * 0.85f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = primaryAlpha * breath), Color.Transparent),
                center = c1,
                radius = r1,
            ),
            radius = r1,
            center = c1,
        )

        // هاله‌ی طلایی - پایینِ صفحه، خلافِ جهتِ اولی (لهجه‌ی طلایی طبق الگوی مصوب: پس‌زمینه، نه فونت).
        val c2 = Offset(
            x = w * (0.28f + 0.26f * sin(phase + (PI / 2).toFloat())),
            y = h * (0.85f + 0.09f * cos(phase)),
        )
        val r2 = w * 0.75f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = accentAlpha * breath), Color.Transparent),
                center = c2,
                radius = r2,
            ),
            radius = r2,
            center = c2,
        )

        // هاله‌ی سبزآبیِ کوچیک‌ترِ میانی - عمقِ سوم، خیلی محوتر.
        val c3 = Offset(
            x = w * (0.40f + 0.34f * cos(phase * 0.5f)),
            y = h * (0.50f + 0.14f * sin(phase + PI.toFloat())),
        )
        val r3 = w * 0.55f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = primaryAlpha * 0.55f), Color.Transparent),
                center = c3,
                radius = r3,
            ),
            radius = r3,
            center = c3,
        )
    }
}
