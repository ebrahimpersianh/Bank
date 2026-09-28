package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.ui.theme.AppPrimary
import kotlinx.coroutines.delay

/** چقدر تیکِ سبز رو صفحه می‌مونه قبل از اینکه واقعاً صفحه بسته بشه (میلی‌ثانیه). */
private const val CHECKMARK_HOLD_MS = 650L

/**
 * تیکِ سبزِ متحرکِ تاییدِ ذخیره - قبلاً صفحه‌ی افزودن/ویرایشِ وام و چک بلافاصله بعدِ ذخیره‌شدن
 * می‌بست، بدونِ هیچ تاییدِ بصری؛ فقط اسپینرِ خودِ دکمه ناپدید می‌شد و صفحه عوض می‌شد - حسِ «قطعاً
 * ذخیره شد» رو نمی‌داد.
 *
 * الگوی استفاده: به‌جای پاس‌دادنِ مستقیمِ `onSaved` واقعی به ویومدل، یه state محلی
 * (`var savedOk by remember { mutableStateOf(false) }`) ست کن و بجاش `onSaved = { savedOk =
 * true }` بده؛ این کامپوننت رو هم به‌عنوانِ آخرین فرزندِ یه `Box` بیرونی اضافه کن با
 * `onFinished = onSaved` (همون callbackِ واقعی، که حالا با تاخیر صدا زده می‌شه).
 */
@Composable
fun SuccessCheckmarkOverlay(visible: Boolean, onFinished: () -> Unit) {
    if (!visible) return

    LaunchedEffect(Unit) {
        delay(CHECKMARK_HOLD_MS)
        onFinished()
    }

    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
                .clip(CircleShape)
                .background(AppPrimary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
        }
    }
}
