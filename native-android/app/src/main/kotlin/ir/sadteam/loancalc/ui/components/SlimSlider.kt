package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.ui.theme.AppBg
import ir.sadteam.loancalc.ui.theme.AppPrimary

/**
 * پورت ظاهر `.slider`/`.slider::-webkit-slider-thumb` تو www/index.html (تراک باریک ۵px + دستگیره‌ی
 * گرد کوچیک با یه حلقه‌ی هم‌رنگ پس‌زمینه دورش). به‌درخواست کاربر «اسکرول مینیمال‌تر با افکت و حرکت
 * روون» موقع کشیدن، دستگیره کمی بزرگ‌تر می‌شه و یه سایه‌ی نرم می‌گیره (حس زنده‌تر، نه حسِ آمارگیر).
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
    val thumbSize by animateDpAsState(if (dragged) 23.dp else 19.dp, label = "thumbSize")
    val thumbElevation by animateDpAsState(if (dragged) 8.dp else 1.dp, label = "thumbElevation")

    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        interactionSource = interactionSource,
        modifier = modifier.fillMaxWidth(),
        thumb = {
            Box(
                modifier = Modifier
                    .size(thumbSize)
                    .shadow(thumbElevation, CircleShape, clip = false)
                    .background(AppPrimary, CircleShape)
                    .border(3.dp, AppBg, CircleShape),
            )
        },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                modifier = Modifier.height(5.dp),
                colors = SliderDefaults.colors(activeTrackColor = AppPrimary, inactiveTrackColor = AppBg),
            )
        },
    )
}
