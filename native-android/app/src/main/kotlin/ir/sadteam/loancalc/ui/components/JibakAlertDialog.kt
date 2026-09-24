package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * جایگزینِ هم‌امضای `AlertDialog`ِ متریال با ظاهرِ خودِ جیبک (همان سطح و گوشه‌ی
 * [ConfirmDialog]). ۳ مهر: همه‌ی دیالوگ‌های قدیمیِ سفید/خاکستریِ اندروید با همین یکدست شدند -
 * امضا عمداً یکی است تا جایگزینی فقط عوض‌کردنِ اسم باشد و محتوای هیچ دیالوگی دست نخورد.
 *
 * `containerColor` پذیرفته می‌شود ولی نادیده - سطح همیشه `AppSurface` است.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JibakAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    containerColor: Color = Color.Unspecified,
) {
    BasicAlertDialog(onDismissRequest = onDismissRequest, modifier = modifier) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(AppSurface)
                .border(1.5.dp, AppLine, RoundedCornerShape(24.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (title != null) {
                CompositionLocalProvider(
                    LocalContentColor provides AppText,
                    LocalTextStyle provides TextStyle(color = AppText, fontSize = 14.5.sp, fontWeight = FontWeight.Black),
                ) { title() }
            }
            if (text != null) {
                // ⚠️ اسکرولِ بیرونی عمداً نیست: بعضی دیالوگ‌ها خودشان LazyColumn دارند و داخلِ
                // verticalScroll کرش می‌کند. رفتار همان AlertDialogِ متریال است.
                Box(modifier = Modifier.heightIn(max = 520.dp)) {
                    CompositionLocalProvider(
                        LocalContentColor provides AppMuted,
                        LocalTextStyle provides TextStyle(color = AppMuted, fontSize = 12.sp, lineHeight = 21.sp),
                    ) { text() }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, androidx.compose.ui.Alignment.End),
            ) {
                dismissButton?.invoke()
                confirmButton()
            }
        }
    }
}
