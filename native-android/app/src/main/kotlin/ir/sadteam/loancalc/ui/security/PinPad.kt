package ir.sadteam.loancalc.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

/** بیشینه‌ی طولِ PIN - همان سقفِ قبلیِ فیلدِ متنی. */
const val PIN_MAX = 8

/**
 * خانه‌های PIN (فریمِ `26a`/`26c`ِ ChatGPT). طولِ PIN بینِ ۴ تا ۸ است، پس تعدادِ خانه‌ها
 * `max(4, طول)` است نه ثابتِ ۶ - طرح همیشه ۶ خانه کشیده بود.
 */
@Composable
fun PinBoxes(length: Int, error: Boolean = false, boxWidth: Int = 36) {
    val count = maxOf(4, length).coerceAtMost(PIN_MAX)
    Ltr {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(count) { i ->
                val filled = i < length
                Box(
                    modifier = Modifier
                        .width(boxWidth.dp)
                        .height(46.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(AppSurface)
                        .border(1.dp, if (error) AppDanger else if (filled) AppPrimary else AppLine, RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (filled) Box(Modifier.size(12.dp).clip(CircleShape).background(if (error) AppDanger else AppPrimary))
                }
            }
        }
    }
}

/** صفحه‌کلیدِ عددیِ خودِ اپ (فریمِ `26c`): ۱..۹، پاک‌کردن، ۰، تأیید. */
@Composable
fun NumberPad(onDigit: (Char) -> Unit, onBackspace: () -> Unit, onDone: () -> Unit, enabled: Boolean = true, keyWidth: Int = 84) {
    val rows = listOf("123", "456", "789")
    Ltr {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            rows.forEach { r ->
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    r.forEach { c -> PadKey(keyWidth = keyWidth, enabled = enabled, onClick = { onDigit(c) }) { Text(toFa(c.digitToInt()), color = AppText, fontSize = 22.sp, fontWeight = FontWeight.Bold) } }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                PadKey(keyWidth = keyWidth, enabled = enabled, onClick = onBackspace) { PadIcon(Icons.AutoMirrored.Filled.Backspace, "پاک‌کردن") }
                PadKey(keyWidth = keyWidth, enabled = enabled, onClick = { onDigit('0') }) { Text(toFa(0), color = AppText, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
                PadKey(keyWidth = keyWidth, enabled = enabled, onClick = onDone) { PadIcon(Icons.Filled.Check, "تأیید") }
            }
        }
    }
}

@Composable
private fun PadIcon(icon: ImageVector, label: String) = Icon(icon, contentDescription = label, tint = AppMuted, modifier = Modifier.size(22.dp))

@Composable
private fun PadKey(keyWidth: Int, enabled: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .width(keyWidth.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AppChipBg)
            .border(1.dp, AppLine, RoundedCornerShape(14.dp))
            .pressScaleClickable { if (enabled) onClick() },
        contentAlignment = Alignment.Center,
    ) { content() }
}
