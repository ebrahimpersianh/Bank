package ir.sadteam.loancalc.ui.admin

import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CardGiftcard
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
import ir.sadteam.loancalc.ui.theme.AppRadius
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
            // طرحِ ChatGPT (۱۰ مهر): تصویرِ بزرگ بالا، عنوانِ وسط‌چین.
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                if (coinsMode) {
                    androidx.compose.foundation.Image(
                        androidx.compose.ui.res.painterResource(ir.sadteam.loancalc.R.drawable.empty_illu_coins),
                        null, modifier = Modifier.size(110.dp),
                    )
                } else {
                    Box(
                        Modifier.size(96.dp).clip(RoundedCornerShape(28.dp))
                            .background(androidx.compose.ui.graphics.Brush.radialGradient(listOf(Color(0x66F5C84B), Color.Transparent))),
                        contentAlignment = Alignment.Center,
                    ) { androidx.compose.material3.Icon(Icons.Filled.CardGiftcard, null, tint = Color(0xFFF5C84B), modifier = Modifier.size(64.dp)) }
                }
                Text(if (coinsMode) "هدیه‌ی سکه" else "هدیه‌ی اشتراک", fontWeight = FontWeight.Black, fontSize = 22.sp, modifier = Modifier.padding(top = 8.dp))
                Text(if (coinsMode) "سکه به کیفِ یک کاربر" else "روزِ اشتراک به یک کاربر", color = AppMuted, fontSize = 13.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Ltr {
                    OutlinedTextField(
 textStyle = ir.sadteam.loancalc.ui.components.appFieldTextStyle(),
                        value = user,
                        onValueChange = { user = it.take(20) },
                        placeholder = { Text("شماره‌ی کاربری (Uid)") },
                        leadingIcon = { androidx.compose.material3.Icon(Icons.Filled.Person, null, tint = AppMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = AdminFieldShape,
                        colors = adminFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    presets.forEach { p ->
                        val sel = amount == p.toString()
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (sel) Color(0xFF16A34A) else AppSurface2)
                                .clickable { amount = p.toString() },
                        ) {
                            if (sel) androidx.compose.material3.Icon(Icons.Filled.CheckCircle, null, tint = Color.White, modifier = Modifier.padding(end = 3.dp).size(14.dp))
                            Text(
                                if (coinsMode) toFa(p) else if (p == 365) "۱ سال" else "${toFa(p)} روز",
                                color = if (sel) Color.White else AppText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                val n = amount.toIntOrNull() ?: 0
                val step = if (coinsMode) 100 else 1
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppSurface2).padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    androidx.compose.material3.Icon(if (coinsMode) Icons.Filled.MonetizationOn else Icons.Filled.CalendarMonth, null, tint = AppMuted, modifier = Modifier.size(20.dp))
                    Text(if (coinsMode) "تعدادِ سکه" else "تعدادِ روز", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(start = 8.dp))
                    Stepper(n, onMinus = { amount = (n - step).coerceAtLeast(0).toString() }, onPlus = { amount = (n + step).toString() })
                }
                OutlinedTextField(
 textStyle = ir.sadteam.loancalc.ui.components.appFieldTextStyle(),
                    value = text,
                    onValueChange = { text = it.take(200) },
                    placeholder = { Text("پیام برای کاربر (اختیاری)") },
                    leadingIcon = { androidx.compose.material3.Icon(Icons.Filled.Chat, null, tint = AppMuted) },
                    supportingText = { Text("${toFa(text.length)}/${toFa(200)}", fontSize = 11.sp) },
                    minLines = 2,
                    shape = AdminFieldShape,
                    colors = adminFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                result?.let {
                    Text(
                        when (it) {
                            "ok" -> "هدیه فرستاده شد ✓"
                            "user_not_found" -> "کاربری با این شماره پیدا نشد"
                            else -> "فرستاده نشد؛ دوباره امتحان کن"
                        },
                        color = if (it == "ok") AppPrimary else AppDanger,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0x332B2205))
                        .border(1.dp, Color(0x66F5C84B), RoundedCornerShape(14.dp)).padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    androidx.compose.material3.Icon(Icons.Filled.CardGiftcard, null, tint = Color(0xFFF5C84B), modifier = Modifier.size(22.dp))
                    Text("این هدیه در «پیام‌های جیبک»ِ همان کاربر هم نشان داده می‌شود.", color = AppText, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
                }
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
            ) { Text(if (sending) "در حالِ فرستادن…" else if (coinsMode) "اهدای سکه 🎁" else "اهدای اشتراک 🎁") }
        },
        dismissButton = {
            androidx.compose.material3.OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(14.dp)) { Text("بستن", color = AppText) }
        },
    )
}
