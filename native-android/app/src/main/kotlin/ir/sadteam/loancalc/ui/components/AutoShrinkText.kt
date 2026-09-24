package ir.sadteam.loancalc.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * متنِ همیشه تک‌خطی: اگه تو عرض جا نشه، به‌جای شکستن به خط دوم، فونت رو قدم‌قدم کوچیک می‌کنه تا جا
 * بشه (تا حداقل [minFontSize]). برای متنِ حروفیِ مبلغ (مثلاً «هفتصد و هشتاد و ... تومان») که کاربر
 * خواست هیچ‌وقت دو سطر نشه.
 */
@Composable
fun AutoShrinkText(
    text: String,
    color: Color,
    maxFontSize: TextUnit,
    modifier: Modifier = Modifier,
    minFontSize: TextUnit = 8.sp,
    /** عددِ هیرو (بخشِ ۶۹) درشت و ضخیم است؛ بقیه‌ی مصرف‌ها وزنِ پیش‌فرض می‌گیرند. */
    fontWeight: FontWeight? = null,
) {
    var fontSize by remember(text) { mutableStateOf(maxFontSize) }
    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        onTextLayout = { result ->
            if (result.hasVisualOverflow && fontSize.value > minFontSize.value) {
                fontSize = (fontSize.value - 0.5f).coerceAtLeast(minFontSize.value).sp
            }
        },
        modifier = modifier,
    )
}
