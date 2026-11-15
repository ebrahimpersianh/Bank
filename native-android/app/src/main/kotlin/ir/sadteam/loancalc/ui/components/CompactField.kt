package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextFieldDefaults
import ir.sadteam.loancalc.ui.text.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary

/**
 * کادرِ ورودیِ **کوتاه** (۴۶dp به‌جای ۵۶+ِ OutlinedTextField) - ۲۰ مهر، خواسته‌ی کاربر: «باکس‌های مبلغ
 * خیلی بزرگ‌اند». همان رنگ‌ها ([appFieldColors]) و متن ([appFieldTextStyle])، فقط فاصله‌ی داخلیِ کمتر و
 * گوشه‌ی ۱۶. برای فیلدهای تک‌خطیِ فرم‌های بلند (صفحه‌ی «افزودنِ وام»).
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CompactField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    suffix: String? = null,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val interaction = remember { MutableInteractionSource() }
    val colors = appFieldColors()
    val shape = RoundedCornerShape(16.dp)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.heightIn(min = 46.dp),
        readOnly = readOnly,
        textStyle = appFieldTextStyle().copy(fontSize = 14.sp),
        keyboardOptions = keyboardOptions,
        singleLine = true,
        visualTransformation = visualTransformation,
        interactionSource = interaction,
        cursorBrush = SolidColor(AppPrimary),
        decorationBox = { inner ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = inner,
                enabled = true,
                singleLine = true,
                visualTransformation = visualTransformation,
                interactionSource = interaction,
                placeholder = if (placeholder != null) {
                    { Text(placeholder, color = AppMuted, fontSize = 13.sp, maxLines = 1) }
                } else null,
                suffix = if (suffix != null) {
                    { Text(suffix, color = AppMuted, fontSize = 12.sp) }
                } else null,
                colors = colors,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 11.dp),
                container = {
                    OutlinedTextFieldDefaults.ContainerBox(
                        enabled = true,
                        isError = false,
                        interactionSource = interaction,
                        colors = colors,
                        shape = shape,
                    )
                },
            )
        },
    )
}
