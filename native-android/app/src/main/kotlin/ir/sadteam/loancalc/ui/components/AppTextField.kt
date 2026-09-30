package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppPrimary

/** شکل گوشه‌گرد مینیمال مشترک برای فیلدهای ورودی (هماهنگ با shapes.extraSmall تو تم). */
val AppFieldShape: Shape = RoundedCornerShape(14.dp)

/**
 * رنگ‌های مشترک فیلدهای ورودی: حاشیه‌ی سبز کم‌رنگ در حالت عادی، و طلایی (AppAccent) موقع فوکوس/تپ -
 * پورت خواسته‌ی کاربر «رو هر باکسی که می‌زنم دورش طلایی باشه» به فیلدهای متنی.
 */
@Composable
fun appFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    // همان حسِ کادرِ جستجو (۸ مهر): زمینه‌ی کم‌رنگ، حاشیه‌ی رنگِ تم؛ طلایی فقط مالِ اشتراک است.
    focusedBorderColor = AppPrimary,
    unfocusedBorderColor = AppPrimary.copy(alpha = 0.35f),
    focusedContainerColor = ir.sadteam.loancalc.ui.theme.AppSurface2,
    unfocusedContainerColor = ir.sadteam.loancalc.ui.theme.AppSurface2,
    cursorColor = AppPrimary,
    focusedLabelColor = AppPrimary,
)

/** گوشه‌ی گردِ همه‌ی کادرها - مثلِ کادرِ جستجو. */
val AppFieldShape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
