package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * کادرِ جستجوی کپسولی - طرحِ ChatGPT (۸ مهر) برای جستجوی «وام‌های من»، همین برای جستجوی کلی.
 * ۵۶dp، گوشه‌ی کاملاً گرد، سطحِ دوم، حاشیه‌ی ۱dpِ سبزِ کم‌رنگ؛ دایره‌ی سبزِ ذره‌بین سمتِ راست،
 * خطِ جداکننده و دکمه‌ی پاک‌کردن سمتِ چپ. همه‌ی رنگ‌ها توکنِ تم‌اند، پس با تم عوض می‌شوند.
 */
@Composable
fun PillSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    /** برای `focusRequester` - روی خودِ فیلدِ متن می‌نشیند نه کپسول. */
    textFieldModifier: Modifier = Modifier,
) {
    val primary = AppPrimary
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(AppSurface2)
            .border(1.dp, primary.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(44.dp).clip(CircleShape).background(primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Filled.Search, contentDescription = null, tint = primary, modifier = Modifier.size(22.dp)) }
        Box(Modifier.padding(horizontal = 12.dp).width(2.dp).height(24.dp).background(primary))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) Text(placeholder, color = AppMuted, fontSize = 14.sp, maxLines = 1)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                cursorBrush = SolidColor(primary),
                modifier = textFieldModifier.fillMaxWidth(),
            )
        }
        if (value.isNotEmpty()) {
            Box(Modifier.padding(horizontal = 10.dp).width(1.dp).height(24.dp).background(AppLine))
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(AppChipBg).clickable { onValueChange("") },
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.Close, contentDescription = "پاک کردن", tint = AppMuted, modifier = Modifier.size(20.dp)) }
        } else {
            Spacer(Modifier.width(8.dp))
        }
    }
}
