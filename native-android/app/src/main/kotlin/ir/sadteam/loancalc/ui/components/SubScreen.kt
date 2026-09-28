package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.Motion

/**
 * زیرصفحه‌ای که **روی** تب می‌نشیند، با ورود/خروجِ نرم به‌جای پریدنِ ناگهانی.
 *
 * [item] وقتی null شود زیرصفحه با انیمیشن بسته می‌شود؛ آخرین مقدار تا پایانِ خروج نگه داشته
 * می‌شود تا محتوا وسطِ محو شدن خالی نشود.
 */
@Composable
fun <T : Any> SubScreen(item: T?, content: @Composable (T) -> Unit) {
    var last by remember { mutableStateOf(item) }
    if (item != null) last = item
    AnimatedVisibility(
        visible = item != null,
        enter = Motion.subScreenEnter,
        exit = Motion.subScreenExit,
    ) {
        Box(modifier = Modifier.fillMaxSize().background(AppSurface)) {
            last?.let { content(it) }
        }
    }
}
