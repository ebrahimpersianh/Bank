package ir.sadteam.loancalc.ui.security

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary

/**
 * قفلِ الگویی (برگرفته از پولکس، ۶ مهر). الگو به‌صورتِ رشته‌ی شماره‌ی نقطه‌ها («۱۴۷۸۹» → "14789")
 * در **همان جای PIN** و با همان هش ذخیره می‌شود؛ فقط [LockType] می‌گوید صفحه‌ی قفل کدام را نشان دهد.
 */
object LockType {
    private const val PREFS = "lock_type"
    fun isPattern(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("pattern", false)
    fun setPattern(context: Context, value: Boolean) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("pattern", value).apply()
}

/** صفحه‌ی ۳×۳؛ با برداشتنِ انگشت، اگر دست‌کم ۴ نقطه وصل شده باشد [onComplete] صدا می‌شود. */
@Composable
fun PatternPad(onComplete: (String) -> Unit, modifier: Modifier = Modifier) {
    val selected = remember { mutableStateListOf<Int>() }
    var finger by remember { mutableStateOf<Offset?>(null) }
    val dot = AppLine
    val ink = AppPrimary
    val ring = AppMuted
    var cell by remember { mutableStateOf(0f) }
    fun centerOf(i: Int) = Offset((i % 3) * cell + cell / 2, (i / 3) * cell + cell / 2)
    fun hit(p: Offset): Int? = (0 until 9).firstOrNull { (centerOf(it) - p).getDistance() < cell * 0.32f }
    Canvas(
        modifier = modifier
            .widthIn(max = 280.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { p ->
                        selected.clear()
                        hit(p)?.let { selected.add(it) }
                        finger = p
                    },
                    onDrag = { change, _ ->
                        finger = change.position
                        hit(change.position)?.let { if (it !in selected) selected.add(it) }
                    },
                    onDragEnd = {
                        finger = null
                        if (selected.size >= 4) onComplete(selected.joinToString("") { (it + 1).toString() })
                        selected.clear()
                    },
                    onDragCancel = { finger = null; selected.clear() },
                )
            },
    ) {
        cell = size.width / 3f
        for (i in 1 until selected.size) {
            drawLine(ink, centerOf(selected[i - 1]), centerOf(selected[i]), strokeWidth = 8.dp.toPx(), cap = StrokeCap.Round)
        }
        val last = selected.lastOrNull()
        val f = finger
        if (last != null && f != null) drawLine(ink.copy(alpha = 0.5f), centerOf(last), f, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
        for (i in 0 until 9) {
            val on = i in selected
            // فریمِ `26b`/`48`: نقطه‌ی حلقه‌دار و پرکنتراست؛ نقطه‌ی وصل‌شده هاله می‌گیرد.
            if (on) {
                drawCircle(ink.copy(alpha = 0.22f), radius = 22.dp.toPx(), center = centerOf(i))
                drawCircle(ink, radius = 11.dp.toPx(), center = centerOf(i))
            } else {
                drawCircle(dot, radius = 10.dp.toPx(), center = centerOf(i))
                drawCircle(ring, radius = 10.dp.toPx(), center = centerOf(i), style = Stroke(width = 2.dp.toPx()))
            }
        }
    }
}
