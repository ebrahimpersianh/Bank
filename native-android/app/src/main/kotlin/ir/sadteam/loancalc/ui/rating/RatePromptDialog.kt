package ir.sadteam.loancalc.ui.rating

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary

/** رجوع کن به [RatePromptViewModel] برای منطقِ زمان‌بندیِ نمایش. */
@Composable
fun RatePromptDialog(
    onRateNow: () -> Unit,
    onLater: () -> Unit,
    onDismissForever: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onLater,
        icon = { Icon(Icons.Filled.Star, contentDescription = null, tint = AppPrimary) },
        title = { Text("لذت بردی از «حسابدار من»؟") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("اگه اپ رو مفید دیدی، یه امتیازِ ۵ ستاره تو استور خیلی به ما کمک می‌کنه.")
                TextButton(onClick = onDismissForever) {
                    Text("دیگه نپرس", color = AppMuted, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = onRateNow) { Text("بله! امتیاز می‌دم") }
        },
        dismissButton = {
            TextButton(onClick = onLater) { Text("بعداً") }
        },
    )
}
