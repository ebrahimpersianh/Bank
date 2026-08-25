package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.ui.haptics.rememberBuzz
import ir.sadteam.loancalc.ui.theme.AppElevation
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.hardShadow

/**
 * دکمه‌ی شناورِ افزودن - **بازطراحیِ سبکِ «جیبک»**.
 *
 * جایگزینِ `FloatingActionButton`ِ Material3 که سایه‌ی **تارِ** خودش رو می‌سازه و با زبانِ
 * «سایه‌ی سختِ بدونِ تاری»ِ این سبک جور در نمیاد. همون رفتارِ فشرده‌شدنِ [GradientButton] رو داره
 * (سایه جمع می‌شه، دکمه به همون اندازه پایین می‌ره).
 *
 * ⚠️ این با «دکمه‌ی شناورِ **میانیِ** نوارِ ناوبری» فرق داره - اون یه‌بار اضافه و به‌خواستِ صریحِ
 * کاربر برداشته شد و سیستمِ طراحی هم صریحاً می‌گه نباشه. این دکمه‌ی گوشه‌ی صفحه‌ست و می‌مونه.
 */
@Composable
fun AppFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Add,
    contentDescription: String? = "افزودن",
) {
    val shape = RoundedCornerShape(20.dp)
    val buzz = rememberBuzz()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val shadow by animateDpAsState(
        if (pressed) AppElevation.pressed else AppElevation.raised,
        tween(70),
        label = "fabShadow",
    )
    val sink by animateDpAsState(
        AppElevation.raised - shadow,
        tween(70),
        label = "fabSink",
    )

    Box(
        modifier = modifier
            .offset(y = sink)
            .size(56.dp)
            .hardShadow(AppPrimaryDim, shadow, 20.dp)
            .clip(shape)
            .background(AppPrimary)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = { buzz(); onClick() },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(24.dp),
        )
    }
}
