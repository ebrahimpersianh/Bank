package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppSurface

/** پورت .card تو www/index.html - از `Surface` (نه یه `Column` دستی با background/border خام)
 * استفاده می‌کنه تا هم سایه‌ی ظریف (`shadowElevation`) هم روشن‌شدن تونالِ سطح (`tonalElevation`،
 * سیستم عمق متریال۳ برای تم تیره) رو مجانی داشته باشیم - قبلاً کاملاً flat بود (فقط رنگ+حاشیه)،
 * که رو تم تیره خیلی «خشک» و بی‌عمق دیده می‌شد. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    label: String? = null,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = AppSurface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, AppLine),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (label != null) {
                Text(
                    text = label,
                    color = AppMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 7.dp),
                )
            }
            content()
        }
    }
}
