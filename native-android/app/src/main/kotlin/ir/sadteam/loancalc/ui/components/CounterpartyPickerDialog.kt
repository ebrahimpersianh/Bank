package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.data.db.CounterpartyEntity

/**
 * دیالوگِ انتخاب/ساختِ طرفِ‌حساب - جوابِ سوالِ ۶ی `design/ANSWERS-chequecounterpartydang.md`:
 * موقعِ ثبتِ چک/وامِ جدید انتخاب یا ساختِ طرفِ‌حساب **اجباری**ه. عمداً (مثلِ AccountPickerDialog)
 * دکمه‌ی «بدونِ طرفِ‌حساب» نداره - بستنِ دیالوگ (onDismiss) یعنی کاربر از کلِ فرم منصرف شده،
 * منطقِ فراخوان تصمیم می‌گیره.
 *
 * فیلدِ بالای دیالوگ هم جستجوی لیستِ موجوده هم اسمِ طرفِ‌حسابِ تازه - اگه هیچ طرفِ‌حسابی با این
 * اسم نبود، دکمه‌ی «افزودنِ طرفِ‌حسابِ جدید» زیرِ لیست ظاهر می‌شه.
 */
@Composable
fun CounterpartyPickerDialog(
    counterparties: List<CounterpartyEntity>,
    onSelect: (CounterpartyEntity) -> Unit,
    onCreateNew: (name: String) -> Unit,
    onDismiss: () -> Unit,
    title: String = "طرف حساب رو انتخاب کن",
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, counterparties) {
        if (query.isBlank()) counterparties else counterparties.filter { it.name.contains(query.trim()) }
    }
    val exactMatch = counterparties.any { it.name.trim() == query.trim() }

    JibakAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("جستجو یا اسمِ طرفِ‌حسابِ جدید") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                if (filtered.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp).padding(top = 6.dp)) {
                        items(filtered, key = { it.id }) { counterparty ->
                            TextButton(
                                onClick = { onSelect(counterparty) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    AvatarView(
                                        avatar = Avatar(
                                            shape = runCatching { AvatarShape.valueOf(counterparty.avatarShape) }.getOrDefault(AvatarShape.BOY),
                                            color = runCatching { AvatarColor.valueOf(counterparty.avatarColor) }.getOrDefault(AvatarColor.NEUTRAL),
                                        ),
                                        size = 26.dp,
                                    )
                                    Text(
                                        counterparty.name,
                                        modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                                    )
                                }
                            }
                        }
                    }
                }
                if (query.isNotBlank() && !exactMatch) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    TextButton(
                        onClick = { onCreateNew(query.trim()) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("+ افزودنِ «${query.trim()}» به‌عنوانِ طرفِ‌حسابِ جدید")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        },
    )
}
