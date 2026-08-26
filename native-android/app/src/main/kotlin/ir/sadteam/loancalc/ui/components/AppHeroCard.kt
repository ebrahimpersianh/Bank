package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSpacing
import androidx.compose.ui.geometry.Offset
import ir.sadteam.loancalc.ui.theme.hardShadow

/**
 * لهجه‌ی رنگیِ کارتِ قهرمان. **این‌ها حدسی نیستن** - هر کدوم از کارتِ همون تب تو فایلِ طراحی
 * برداشته شده، و عمداً همه سبز نیستن (تبِ گزارش/آمار بنفشه، طبقِ توکنِ «بنفش - بودجه و آمار»).
 */
enum class HeroTone(
    internal val from: Long,
    internal val to: Long,
    internal val shadow: Long,
) {
    /** خانه (`15a`)، دارایی (`26b`)، بودجه (`27c`). */
    GREEN(0xFF0EA968, 0xFF0B8C57, 0xFF096F45),

    /** گزارش و آمار (`26a`). */
    PURPLE(0xFFA56EFF, 0xFF7440C9, 0xFF5C2FA8),

    /** اطلاع/انتقال - فعلاً جایی استفاده نشده، برای صفحاتِ بعدیِ همین بازطراحی. */
    BLUE(0xFF1CB0F6, 0xFF0E86C0, 0xFF0A6795),
}

/**
 * کارتِ **قهرمانِ** بالای صفحه - **بازطراحیِ سبکِ «جیبک»**.
 *
 * تو دورِ قبل هر صفحه این کارت رو با `AppCard(accentGradient = Brush.linearGradient(...))` و یه
 * گرادیانِ سبزِ **نیمه‌شفاف** می‌ساخت (پنج جای اپ، پنج‌بار همون دو خط تکرار شده بود). سبکِ جدید
 * به‌جاش یه **کارتِ کاملاً ماتِ رنگی با متنِ سفید** می‌خواد، پس اینجا یه‌بار تعریف شده:
 * - پرشدنِ گرادیانِ **مات** طبقِ [tone]
 * - سایه‌ی سختِ `0 5px 0` هم‌رنگِ تیره‌ترِ خودش (نه سایه‌ی تارِ Material)
 * - `LocalContentColor` سفید، پس هر `Text`ِ بدونِ `color`ِ صریح خودبه‌خود سفید می‌شه
 *
 * ⚠️ قاعده‌ی «حداکثر **یک** رنگِ لهجه در هر صفحه» - تو هر صفحه فقط **یکی** از این‌ها بذار.
 *
 * ⚠️ متن‌های داخلش نباید رنگِ تم‌آگاه (`AppText`/`AppMuted`) بگیرن - رو زمینه‌ی رنگیِ تیره نامرئی
 * می‌شن. به‌جاش سفید (پیش‌فرض) یا [HeroMuted].
 */
@Composable
fun AppHeroCard(
    modifier: Modifier = Modifier,
    tone: HeroTone = HeroTone.GREEN,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(AppRadius.card)
    // ⚠️ گرادیان **۱۶۰ درجه**ست نه عمودی (اصلاحیه‌ی طراح؛ دورِ قبل عمودی گفته بود و اشتباه
    // بود). نسخه‌ی بنفشِ فریمِ `26a` هم همینه، پس هر دو گونه یه جهت دارن. جهتش مهمه نه
    // عددِ دقیقش: از بالا-راست به پایین-چپ.
    val gradient = Brush.linearGradient(
        colors = listOf(Color(tone.from), Color(tone.to)),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .hardShadow(Color(tone.shadow), 5.dp, AppRadius.card)
            .clip(shape)
            .background(gradient)
            .padding(AppSpacing.cardPadding),
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            content()
        }
    }
}

/** متنِ کم‌رنگ‌ترِ رو کارتِ قهرمان - `rgba(255,255,255,.78)` طبقِ طرح. */
val HeroMuted: Color = Color.White.copy(alpha = 0.78f)

/** ته‌رنگِ قرصِ نیمه‌شفافِ رو کارتِ قهرمان - `rgba(255,255,255,.2)` طبقِ طرح. */
val HeroPillBg: Color = Color.White.copy(alpha = 0.20f)

/** سایه‌ی سختِ کارتِ قهرمان - `#096F45`، تیره‌ترِ همون سبز (نه خاکستریِ عمومی). */
private val HeroShadowColor = Color(0xFF096F45)

/**
 * قرصِ کوچکِ نیمه‌شفافِ رو کارتِ قهرمان - `rgba(255,255,255,.2)`، متنِ ۹/۹۰۰ سفید، پدینگِ ۵×۱۰،
 * کپسولِ کامل. طبقِ قرص‌های تفکیکِ کارتِ `26b` («نقد ۱۶٫۷M»، «طلا ۹۳٫۹M»...).
 */
@Composable
fun HeroSmallPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(AppRadius.button))
            .background(HeroPillBg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(text, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}
