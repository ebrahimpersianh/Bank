package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppElevation
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppShadowNeutral
import ir.sadteam.loancalc.ui.theme.hardShadow

/**
 * چیپ/تب - **بازطراحیِ سبکِ «جیبک»**، بخشِ «۶ · فیلد، چیپ، تب»ِ سیستمِ طراحی.
 *
 * قاعده‌های صریحِ طرح:
 * - **تب‌ها خطِ زیرین ندارن**؛ تبِ فعال یه **قرصِ پرشده**‌ست (نه ته‌رنگِ کم‌آلفا با حاشیه).
 * - **چیپ با سایه = قابلِ لمس، چیپِ بی‌سایه = فقط برچسب.**
 *
 * تغییر نسبت به دورِ قبل: گوشه از ۱۰dp به کپسولِ کامل، حالتِ انتخاب‌شده از «سبزِ ۱۴٪ + حاشیه»
 * به «سبزِ توپر + متنِ سفید»، و حاشیه‌ی طلاییِ لحظه‌ی فشردن حذف شد (قاعده: طلایی فقط پرمیوم).
 *
 * @param labelOnly برچسبِ صرف - بی‌سایه و غیرقابلِ لمس، طبقِ قاعده‌ی بالا.
 */
@Composable
fun AppChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    labelOnly: Boolean = false,
) {
    val shape = RoundedCornerShape(AppRadius.button)
    val bg = if (selected) AppPrimary else AppChipBg
    val textColor = if (selected) Color.White else AppMuted
    val shadowColor = when {
        labelOnly -> Color.Transparent
        selected -> AppPrimaryDim
        else -> AppShadowNeutral
    }
    val shadowOffset = if (labelOnly) 0.dp else AppElevation.neutral

    Text(
        text = label,
        color = textColor,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = modifier
            .hardShadow(shadowColor, shadowOffset, AppRadius.button)
            .then(if (labelOnly) Modifier else Modifier.pressScaleClickable(onClick = onClick))
            .background(bg, shape)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    )
}
