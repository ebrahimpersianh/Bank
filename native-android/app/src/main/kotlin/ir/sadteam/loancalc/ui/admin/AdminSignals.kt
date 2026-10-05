package ir.sadteam.loancalc.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow

/** پلِ سراسریِ ادمین: تعدادِ پیامِ بی‌جواب (نشانِ روی آیکون‌ها) و «اعلان را زدند، گزارش را باز کن». */
object AdminSignals {
    val unreadSupport = MutableStateFlow(0)
    val openAdmin = MutableStateFlow(false)

    fun parseUnread(notes: List<String>): Int =
        notes.firstOrNull { it.startsWith("open_support:") }?.removePrefix("open_support:")?.toIntOrNull() ?: 0
}

/** نشانِ قرمزِ عدددار روی گوشه‌ی آیکون. */
@androidx.compose.runtime.Composable
fun UnreadDot(count: Int, modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .defaultMinSize(minWidth = 17.dp, minHeight = 17.dp)
            .background(androidx.compose.ui.graphics.Color(0xFFDC2626), androidx.compose.foundation.shape.CircleShape)
            .padding(horizontal = 4.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        androidx.compose.material3.Text(
            ir.sadteam.loancalc.core.toFa(count.coerceAtMost(99)),
            color = androidx.compose.ui.graphics.Color.White,
            fontSize = androidx.compose.ui.unit.TextUnit(10f, androidx.compose.ui.unit.TextUnitType.Sp),
            fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
        )
    }
}
