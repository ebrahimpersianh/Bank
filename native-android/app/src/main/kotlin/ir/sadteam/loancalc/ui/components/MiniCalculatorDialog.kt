package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import ir.sadteam.loancalc.ui.text.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * ماشین‌حسابِ کوچکِ کنارِ فیلدِ مبلغ (دنگِ جدید، ۱۶ مهر): «۲۴۰ + ۱۸۵ + ۹۰» → جمع، و «تایید»
 * عدد را در فیلد می‌گذارد. ضرب/تقسیم پیش از جمع/تفریق حساب می‌شود.
 */
@Composable
fun MiniCalculatorDialog(
    initial: String,
    onResult: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var expr by remember { mutableStateOf(initial.filter { it.isDigit() }) }
    val value = remember(expr) { evalCalc(expr) }
    JibakAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ماشین‌حساب") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Ltr {
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppSurface2).padding(12.dp),
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(expr.ifEmpty { "0" }, color = AppMuted, fontSize = 15.sp, maxLines = 2)
                        Text(value?.let { fmt(it.toDouble()) } ?: "—", color = AppText, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                }
                val rows = listOf(
                    listOf("7", "8", "9", "÷"),
                    listOf("4", "5", "6", "×"),
                    listOf("1", "2", "3", "−"),
                    listOf("C", "0", "⌫", "+"),
                )
                Ltr {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        rows.forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                row.forEach { k ->
                                    val op = k in "÷×−+"
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (op) AppPrimaryPill else AppSurface2)
                                            .pressScaleClickable {
                                                expr = when (k) {
                                                    "C" -> ""
                                                    "⌫" -> expr.dropLast(1)
                                                    else -> if (op && (expr.isEmpty() || expr.last() in "÷×−+")) expr.dropLast(if (expr.isEmpty()) 0 else 1) + (if (expr.isEmpty()) "" else k)
                                                    else (expr + k).take(40)
                                                }
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(k, color = if (op) AppPrimary else AppText, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { value?.takeIf { it > 0 }?.let(onResult); onDismiss() }) { Text("تایید") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
    )
}

/** «۱۲+۳×۴» → ۲۴. ورودیِ ناقص (عملگرِ آخر) نادیده گرفته می‌شود؛ تقسیم بر صفر = null. */
internal fun evalCalc(expr: String): Long? {
    val e = expr.trimEnd('÷', '×', '−', '+')
    if (e.isEmpty()) return 0L
    val nums = mutableListOf<Double>()
    val ops = mutableListOf<Char>()
    var cur = StringBuilder()
    for (c in e) {
        if (c.isDigit()) cur.append(c) else {
            nums += cur.toString().toDoubleOrNull() ?: return null
            ops += c
            cur = StringBuilder()
        }
    }
    nums += cur.toString().toDoubleOrNull() ?: return null
    // اول ضرب و تقسیم.
    val n2 = mutableListOf(nums[0])
    val o2 = mutableListOf<Char>()
    ops.forEachIndexed { i, op ->
        val b = nums[i + 1]
        when (op) {
            '×' -> n2[n2.lastIndex] = n2.last() * b
            '÷' -> { if (b == 0.0) return null; n2[n2.lastIndex] = n2.last() / b }
            else -> { o2 += op; n2 += b }
        }
    }
    var r = n2[0]
    o2.forEachIndexed { i, op -> r = if (op == '+') r + n2[i + 1] else r - n2[i + 1] }
    return Math.round(r)
}
