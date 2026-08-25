package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppChipBg

/**
 * دکمه‌ی دوگزینه‌ای (یا بیشتر) به سبکِ کپسولِ لغزنده - جایگزینِ دو تا [AppChip] جداگانه‌ی کنارِ هم
 * (خواسته‌ی صریحِ کاربر: «کل برنامه مدرن‌تر بشه»، اعمال شده رو تاگلِ نوعِ تراکنش تو AddTransactionForm
 * و تاگلِ دخل/خرج تو ReportSection). یه پسِ‌زمینه‌ی رنگیِ متحرک (Modifier.offset، RTL-آگاه چون
 * offset جهت‌آگاهه نه absoluteOffset) زیرِ گزینه‌ی انتخاب‌شده با فنر سُر می‌خوره.
 */
@Composable
fun SegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = AppPrimary,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(21.dp))
            .background(AppChipBg)
            .border(1.dp, AppLine, RoundedCornerShape(21.dp))
            .padding(3.dp),
    ) {
        val segmentWidth = maxWidth / options.size
        val offsetX by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
            label = "segmentedToggleOffset",
        )
        Box(
            modifier = Modifier
                .offset(x = offsetX)
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(18.dp))
                .background(selectedColor),
        )
        Row(modifier = Modifier.fillMaxSize()) {
            options.forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pressScaleClickable(onClick = { onSelect(index) }),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        // وزنِ ۹۰۰ برای تبِ فعال و ۸۰۰ برای بقیه - بخشِ «۶ · فیلد، چیپ، تب»ِ
                        // سیستمِ طراحی («دکمه و چیپ: ۱۰–۱۱ / ۸۰۰»).
                        color = if (index == selectedIndex) Color.White else AppMuted,
                        fontSize = 11.5.sp,
                        fontWeight = if (index == selectedIndex) FontWeight.Black else FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}
