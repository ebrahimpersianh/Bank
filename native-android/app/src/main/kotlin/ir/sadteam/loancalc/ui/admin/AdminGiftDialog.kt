package ir.sadteam.loancalc.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * 🎁 هدیه‌ی مستقیمِ ادمین (۸ مهر): شماره‌ی کاربری (`Uid:…`) + روزِ اشتراک یا سکه + پیام.
 * [coinsMode] = هدیه‌ی سکه؛ وگرنه اشتراک. هدیه در «پیام‌های جیبک»ِ همان کاربر می‌نشیند و
 * سکه با رسیدنِ آن پیام خودکار به دفترش اضافه می‌شود.
 */
@Composable
fun AdminGiftDialog(coinsMode: Boolean, onDismiss: () -> Unit, onSend: (user: String, amount: Int, text: String, done: (String?) -> Unit) -> Unit) {
    var user by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    val presets = if (coinsMode) listOf(100, 500, 1_000, 5_000) else listOf(7, 30, 90, 365)
    JibakAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            // بازطراحی (۸ مهر): کاشیِ رنگیِ آیکون + زیرعنوان، مثلِ ردیف‌های هاب.
            // اشتراک طلایی (مجاز: نشانِ اشتراک)؛ سکه بنفشِ هاب.
            val tint = if (coinsMode) ir.sadteam.loancalc.ui.theme.AppPurple else ir.sadteam.loancalc.ui.theme.AppGoldInkSoft
            val tintBg = if (coinsMode) ir.sadteam.loancalc.ui.theme.AppPurplePill else ir.sadteam.loancalc.ui.theme.AppGoldPillSoft
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(tintBg),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.material3.Icon(
                        if (coinsMode) Icons.Filled.MonetizationOn else Icons.Filled.WorkspacePremium,
                        null, tint = tint, modifier = Modifier.size(24.dp),
                    )
                }
                Column(Modifier.padding(start = 12.dp)) {
                    Text(if (coinsMode) "هدیه‌ی سکه" else "هدیه‌ی اشتراک", fontWeight = FontWeight.Black, fontSize = 17.sp)
                    Text(if (coinsMode) "سکه به کیفِ یک کاربر" else "روزِ اشتراک به یک کاربر", color = AppMuted, fontSize = 12.sp)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Ltr {
                    OutlinedTextField(
                        value = user,
                        onValueChange = { user = it.take(20) },
                        label = { Text("شماره‌ی کاربری (Uid)") },
                        placeholder = { Text("مثلاً 7405024") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = AdminFieldShape,
                        colors = appFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    presets.forEach { p ->
                        val sel = amount == p.toString()
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (sel) AppPrimary else AppSurface2)
                                .clickable { amount = p.toString() },
                        ) {
                            Text(
                                if (coinsMode) toFa(p) else if (p == 365) "۱ سال" else "${toFa(p)} روز",
                                color = if (sel) Color.White else AppText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = cleanNum(it).take(8) },
                    label = { Text(if (coinsMode) "تعدادِ سکه" else "تعدادِ روز") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = AdminFieldShape,
                    colors = appFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.take(300) },
                    label = { Text("پیام برای کاربر (اختیاری)") },
                    minLines = 2,
                    shape = AdminFieldShape,
                    colors = appFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                val n0 = amount.toIntOrNull() ?: 0
                if (user.isNotBlank() && n0 > 0) {
                    Text(
                        (if (coinsMode) "${toFa(n0)} سکه" else "${toFa(n0)} روز اشتراک") + " برای Uid:${user.trim().filter { it.isDigit() }}",
                        color = AppPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppPrimary.copy(alpha = 0.08f)).padding(10.dp),
                    )
                }
                result?.let {
                    Text(
                        when (it) {
                            "ok" -> "هدیه فرستاده شد"
                            "user_not_found" -> "کاربری با این شماره پیدا نشد"
                            else -> "فرستاده نشد؛ دوباره امتحان کن"
                        },
                        color = if (it == "ok") AppPrimary else AppDanger,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text("هدیه در «پیام‌های جیبک»ِ همان کاربر هم می‌آید.", color = AppMuted, fontSize = 12.5.sp, modifier = Modifier.padding(top = 2.dp))
            }
        },
        confirmButton = {
            val n = amount.toIntOrNull() ?: 0
            GradientButton(
                onClick = {
                    sending = true
                    onSend(user.trim(), n, text.trim()) { err ->
                        sending = false
                        result = err ?: "ok"
                        if (err == null) { user = ""; amount = ""; text = "" }
                    }
                },
                enabled = !sending && user.isNotBlank() && n > 0,
            ) { Text(if (sending) "در حالِ فرستادن…" else "بفرست") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("بستن") } },
    )
}
