package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import kotlinx.coroutines.delay

/**
 * حالت‌های «بارگذاری / خالی / خطا» - **بخشِ ۳۳ فایلِ طراحی** (کارت‌های `33a`..`33d`).
 *
 * چهار قاعده‌ی صریحِ طرح که این فایل پیاده‌شون می‌کنه:
 *
 * 1. **بارگذاری فقط بالای ۴۰۰ms** - زیرِ اون هیچ اسکلتی نشون داده نمی‌شه تا صفحه چشمک نزنه
 *    ([LoadStateHost]).
 * 2. **خالی ≠ خطا** - تا وقتی درخواست جواب نداده، حالتِ خالی حق نداره ظاهر بشه. یه منبعِ حالتِ
 *    واحد: [LoadPhase].
 * 3. **داده‌ی کهنه بهتر از صفحه‌ی سفید** - خطا صفحه رو سفید نمی‌کنه؛ آخرین جوابِ موفق با
 *    نصف شفافیت می‌مونه و [StaleDataBar] بالاش میاد.
 * 4. **تلاشِ دوباره تو دو جا** - نوارِ بالا وقتی داده‌ی کهنه هست، دکمه‌ی پایین وقتی نیست. هر دو
 *    یه `retry()`.
 *
 * ⚠️ اسکلت **شکلِ محتوای واقعی** رو داره نه اسپینر (قاعده‌ی `33a`) - برای همین
 * [SkeletonRowList] ردیفِ لیستِ واقعی رو تقلید می‌کنه، و سه ردیفِ آخر پله‌ای محو می‌شن تا
 * مرزِ لیست پیدا باشه.
 */
enum class LoadPhase { LOADING, CONTENT, EMPTY, ERROR }

/**
 * درخششِ اسکلت. جدا از [shimmerEffect]ِ قدیمی نگه داشته شد چون طرح برای این بخش عددهای
 * مشخصِ خودش رو داره: **۱٬۴ ثانیه** دوره و **۱۰۰ms تاخیر بین خانه‌های هم‌ردیف**.
 */
@Composable
private fun Modifier.skeletonShimmer(delayMillis: Int = 0): Modifier {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val translate by transition.animateFloat(
        initialValue = -420f,
        targetValue = 420f,
        animationSpec = infiniteRepeatable(
            tween(1400, delayMillis = delayMillis, easing = LinearEasing),
            RepeatMode.Restart,
        ),
        label = "skeletonTranslate",
    )
    val base = AppMuted
    return this.background(
        Brush.linearGradient(
            colors = listOf(
                base.copy(alpha = 0.10f),
                base.copy(alpha = 0.26f),
                base.copy(alpha = 0.10f),
            ),
            start = Offset(translate - 220f, 0f),
            end = Offset(translate + 220f, 220f),
        ),
    )
}

/** یه خانه‌ی خالیِ اسکلت. */
@Composable
fun SkeletonBox(
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    radius: androidx.compose.ui.unit.Dp = 6.dp,
    delayMillis: Int = 0,
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(radius))
            .skeletonShimmer(delayMillis),
    )
}

/**
 * اسکلتِ لیست - شکلِ **ردیفِ واقعیِ** اپ (قابِ آیکون + دو خطِ متن + مبلغِ سمتِ چپ).
 *
 * سه ردیفِ آخر پله‌ای محو می‌شن (`1f`، `.62f`، `.34f`) دقیقاً طبقِ `33a`.
 */
@Composable
fun SkeletonRowList(
    modifier: Modifier = Modifier,
    rows: Int = 6,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.card))
            .background(AppSurface)
            .border(2.dp, AppLineRow, RoundedCornerShape(AppRadius.card))
            .padding(vertical = 6.dp),
    ) {
        repeat(rows) { i ->
            // محوشدنِ پله‌ایِ انتهای لیست: فقط سه ردیفِ آخر.
            val fade = when (rows - i) {
                1 -> 0.34f
                2 -> 0.62f
                3 -> 0.82f
                else -> 1f
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(fade)
                    .padding(horizontal = 12.dp, vertical = 9.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(AppRadius.icon))
                        .skeletonShimmer(delayMillis = i * 100),
                )
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    SkeletonBox(112.dp, 11.dp, delayMillis = i * 100 + 100)
                    Spacer(Modifier.height(6.dp))
                    SkeletonBox(64.dp, 9.dp, delayMillis = i * 100 + 200)
                }
                Spacer(Modifier.weight(1f))
                SkeletonBox(72.dp, 12.dp, delayMillis = i * 100 + 100)
            }
        }
    }
}

/**
 * نوارِ «داده‌ی کهنه» - وقتی درخواست شکست خورده ولی آخرین جوابِ موفق هنوز هست.
 * طبقِ `33c` بالای محتوای نیمه‌شفاف می‌شینه و دکمه‌ی «تلاشِ دوباره»ی خودش رو داره.
 */
@Composable
fun StaleDataBar(
    lastUpdatedLabel: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.row))
            .background(AppDangerPill)
            .border(2.dp, AppDanger.copy(alpha = 0.34f), RoundedCornerShape(AppRadius.row))
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.CloudOff,
            contentDescription = null,
            tint = AppDangerInk,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = lastUpdatedLabel,
            color = AppDangerInk,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f),
        )
        GradientButton(
            onClick = onRetry,
            variant = AppButtonVariant.IN_ROW,
        ) {
            Text("تلاشِ دوباره")
        }
    }
}

/**
 * کارتِ خطای کامل - وقتی **هیچ داده‌ی ذخیره‌شده‌ای** نیست (`33c`: «اگر هیچ داده‌ی ذخیره‌شده‌ای
 * نبود، فقط بخشِ پایین می‌ماند»).
 */
@Composable
fun ErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "به سرور وصل نشدیم",
    description: String = "اینترنت که برگردد، خودش به‌روز می‌شود.",
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.card))
            .background(AppSurface)
            .border(2.dp, AppLineRow, RoundedCornerShape(AppRadius.card))
            .padding(horizontal = 16.dp, vertical = 22.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(AppRadius.card))
                .background(AppDangerPill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.CloudOff,
                contentDescription = null,
                tint = AppDangerInk,
                modifier = Modifier.size(30.dp),
            )
        }
        Text(
            text = title,
            color = AppText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = description,
            color = AppMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 19.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
        GradientButton(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Text("تلاشِ دوباره", modifier = Modifier.padding(start = 6.dp))
            }
        }
    }
}

/**
 * میزبانِ حالت - همون «یک منبعِ حالت» که طرح می‌خواد.
 *
 * @param phase حالتِ فعلی.
 * @param hasCachedContent وقتی [phase] برابرِ [LoadPhase.ERROR]ه: اگه `true` باشه محتوای کهنه با
 *   نصف شفافیت زیرِ [StaleDataBar] می‌مونه، وگرنه [ErrorState] نشون داده می‌شه.
 * @param lastUpdatedLabel متنِ «آخرین به‌روزرسانی: …» برای نوارِ بالا.
 * @param skeleton اسکلتی که شکلِ محتوای همین صفحه رو داره (پیش‌فرض: لیست).
 * @param empty حالتِ خالیِ اختصاصیِ همین صفحه (متنش تو `33d` برای هر صفحه جدا اومده).
 */
@Composable
fun LoadStateHost(
    phase: LoadPhase,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    hasCachedContent: Boolean = false,
    lastUpdatedLabel: String = "",
    skeleton: @Composable () -> Unit = { SkeletonRowList() },
    empty: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        when (phase) {
            LoadPhase.LOADING -> DelayedSkeleton(skeleton)
            LoadPhase.EMPTY -> empty()
            LoadPhase.CONTENT -> content()
            LoadPhase.ERROR -> if (hasCachedContent) {
                StaleDataBar(
                    lastUpdatedLabel = lastUpdatedLabel.ifBlank { "به سرور وصل نشدیم" },
                    onRetry = onRetry,
                )
                Spacer(Modifier.height(10.dp))
                // داده‌ی کهنه با نصف شفافیت - قاعده‌ی صریحِ `33c`.
                Box(Modifier.alpha(0.5f)) { content() }
            } else {
                ErrorState(onRetry = onRetry)
            }
        }
    }
}

/**
 * قاعده‌ی «بارگذاری فقط بالای ۴۰۰ms». تا ۴۰۰ میلی‌ثانیه هیچی کشیده نمی‌شه؛ اگه جواب زودتر
 * برسه کاربر اصلاً اسکلتی نمی‌بینه و صفحه چشمک نمی‌زنه.
 */
@Composable
private fun DelayedSkeleton(skeleton: @Composable () -> Unit) {
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(400)
        show = true
    }
    if (show) skeleton() else Spacer(Modifier.height(0.dp))
}

/** رنگِ خنثی برای جاهایی که خانه‌ی اسکلت رو پس‌زمینه‌ی رنگی می‌شینه. */
internal val SkeletonOnHero = Color.White.copy(alpha = 0.22f)
