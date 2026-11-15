package ir.sadteam.loancalc.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * شمارش صعودی نرمِ یه عدد (پورت animateNumber تو www/index.html: ease-out cubic) - رو `Double` تا
 * برای مبالغ میلیاردی خطای گردکردن Float پیش نیاد. هر بار [target] عوض بشه از مقدار فعلی (نه صفر)
 * به مقصد جدید می‌ره - عین `el.dataset.animVal` وب.
 */
@Composable
fun countUpDouble(target: Double, durationMs: Long = 650): Double {
    var value by remember { mutableStateOf(0.0) }
    LaunchedEffect(target) {
        val from = value
        val start = System.currentTimeMillis()
        while (true) {
            val p = ((System.currentTimeMillis() - start).toDouble() / durationMs).coerceIn(0.0, 1.0)
            val eased = 1.0 - (1.0 - p) * (1.0 - p) * (1.0 - p)
            value = from + (target - from) * eased
            if (p >= 1.0) break
            delay(16)
        }
    }
    return value
}
