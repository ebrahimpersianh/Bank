package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.ui.haptics.rememberBuzz
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppPrimary

/**
 * پورت ظاهر `.slider`/`.slider::-webkit-slider-thumb` تو www/index.html، با پالایشِ خواسته‌ی کاربر
 * («اون گرده که برای اسکرول هست شیک‌تر بشه، گردش کوچیک‌تر بشه، یکم بازتاب داشته باشه، حرکتش نرم
 * باشه، رنگش مرکزش پررنگ‌تر باشه و بغل کمی کم‌رنگ‌تر»):
 *  - دستگیره یه گرادینت شعاعی داره (مرکز پررنگ‌تر AppPrimary، لبه‌ها به‌سمتِ AppPrimaryDim محو‌تر)
 *    به‌جای رنگ صاف یک‌دست - حس «گوی شیشه‌ای/جلا‌خورده».
 *  - یه هایلایتِ کوچیکِ نیمه‌شفاف بالا-چپِ دستگیره (پورتِ بازتابِ نور رو یه گویِ واقعی).
 *  - اندازه‌ی پایه کمی کوچیک‌تر شد (قبلاً ۱۹dp)، ولی موقعِ لمس/کشیدن با spring (نه tween سفت) بزرگ
 *    می‌شه - حسِ «افکت قبل از حرکت».
 *  - یه لرزشِ ظریف (ویبره‌ی اشتراکی) دقیقاً لحظه‌ی شروعِ کشیدن.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlimSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val dragged by interactionSource.collectIsDraggedAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val touched = dragged || pressed
    val thumbSize by animateDpAsState(
        if (touched) 22.dp else 17.dp,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
        label = "thumbSize",
    )
    val thumbElevation by animateDpAsState(if (touched) 8.dp else 1.dp, label = "thumbElevation")
    val buzz = rememberBuzz()
    LaunchedEffect(dragged) {
        if (dragged) buzz()
    }

    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        interactionSource = interactionSource,
        modifier = modifier.fillMaxWidth(),
        thumb = {
            val edgeColor = lerp(AppPrimary, Color.White, 0.32f)
            Box(
                modifier = Modifier
                    .size(thumbSize)
                    .shadow(thumbElevation, CircleShape, clip = false)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(colors = listOf(AppPrimary, edgeColor)))
                    .border(2.5.dp, AppBg, CircleShape),
                contentAlignment = Alignment.TopStart,
            ) {
                // بازتابِ نور: یه هایلایتِ محوِ نیمه‌شفاف رو ربع بالا-چپ دستگیره (مثل گویِ جلاخورده).
                Box(
                    modifier = Modifier
                        .padding(top = thumbSize * 0.12f, start = thumbSize * 0.16f)
                        .size(thumbSize * 0.38f)
                        .background(
                            Brush.radialGradient(colors = listOf(Color.White.copy(alpha = 0.6f), Color.Transparent)),
                            CircleShape,
                        ),
                )
            }
        },
        // تِرکِ سفارشی به‌جای SliderDefaults.Track پیش‌فرض - پیش‌فرضِ متریال۳ وقتی steps>۰ باشه یه
        // ردیف نقطه‌ی ریزِ تیک رو مسیرِ اسلایدر می‌کشه (خواسته‌ی کاربر: این نقطه‌ها زشتن، همه‌ی
        // اسلایدرها - چه پله‌دار چه پیوسته - باید دقیقاً مثلِ هم، یه خطِ صافِ یک‌دست باشن).
        track = { sliderState ->
            val fraction = remember(sliderState.value, sliderState.valueRange) {
                val range = sliderState.valueRange.endInclusive - sliderState.valueRange.start
                if (range <= 0f) 0f else ((sliderState.value - sliderState.valueRange.start) / range).coerceIn(0f, 1f)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(50)),
            ) {
                Box(modifier = Modifier.fillMaxWidth().fillMaxHeight().background(AppBg))
                Box(modifier = Modifier.fillMaxWidth(fraction).fillMaxHeight().background(AppPrimary))
            }
        },
    )
}
