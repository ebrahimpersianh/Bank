package ir.sadteam.loancalc.ui.cheque

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.ChequeBookEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * لیست دسته‌چک‌ها (برای پیشنهاد خودکار شماره‌ی سریال بعدی تو فرم افزودن چک) + یه فرم ساده‌ی افزودن
 * دسته‌چک جدید (مالک، بانک، بازه‌ی سریال). حذف مستقیمه، بدون مودال تایید - هم‌الگو با حذف وام/چک تو
 * بقیه‌ی صفحات این پروژه.
 */
@Composable
fun ChequeBooksScreen(
    books: List<ChequeBookEntity>,
    onBack: () -> Unit,
    onAdd: (owner: String, bank: String, start: Long, end: Long) -> Unit,
    onDelete: (ChequeBookEntity) -> Unit,
) {
    var showAddForm by remember { mutableStateOf(false) }
    var owner by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }
    var startText by remember { mutableStateOf("") }
    var endText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 40.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                }
                Text("دسته‌چک‌ها", color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
            }
        }

        if (books.isEmpty() && !showAddForm) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                    Text("هنوز دسته‌چکی ثبت نشده", color = AppText, fontSize = 15.sp)
                }
            }
        } else {
            items(books, key = { it.id }) { book ->
                AppCard(modifier = Modifier.animateItem()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("${book.ownerName} - ${book.bankName}", color = AppText, fontSize = 14.sp)
                            Text(
                                "سریال ${toFa(book.startSerial)} تا ${toFa(book.endSerial)}",
                                color = AppMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                            Text(
                                "سریال بعدی: ${toFa(book.nextSerial)}",
                                color = AppMuted,
                                fontSize = 11.sp,
                            )
                        }
                        OutlinedButton(
                            onClick = { onDelete(book) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppDanger),
                        ) {
                            Text("حذف", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            if (showAddForm) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppCard(label = "مالک دسته‌چک") {
                        OutlinedTextField(
                            value = owner,
                            onValueChange = { owner = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                    }
                    AppCard(label = "بانک") {
                        OutlinedTextField(
                            value = bank,
                            onValueChange = { bank = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppCard(label = "شماره شروع", modifier = Modifier.weight(1f)) {
                            // Ltr: رجوع کن به کامنتِ Ltr.kt.
                            Ltr {
                                OutlinedTextField(
                                    value = startText,
                                    onValueChange = { startText = cleanNum(it) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )
                            }
                        }
                        AppCard(label = "شماره پایان", modifier = Modifier.weight(1f)) {
                            Ltr {
                                OutlinedTextField(
                                    value = endText,
                                    onValueChange = { endText = cleanNum(it) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )
                            }
                        }
                    }
                    if (error != null) {
                        Text(text = error ?: "", color = AppDanger, fontSize = 12.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GradientButton(
                            onClick = {
                                val start = startText.toLongOrNull()
                                val end = endText.toLongOrNull()
                                error = when {
                                    owner.trim().isEmpty() -> "اسم مالک رو وارد کن"
                                    bank.trim().isEmpty() -> "اسم بانک رو وارد کن"
                                    start == null || end == null -> "بازه‌ی سریال رو کامل وارد کن"
                                    end < start -> "شماره پایان باید بزرگ‌تر از شروع باشه"
                                    else -> null
                                }
                                if (error == null) {
                                    onAdd(owner.trim(), bank.trim(), startText.toLong(), endText.toLong())
                                    owner = ""
                                    bank = ""
                                    startText = ""
                                    endText = ""
                                    showAddForm = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("ذخیره")
                        }
                        OutlinedButton(onClick = { showAddForm = false }, modifier = Modifier.weight(1f)) {
                            Text("انصراف")
                        }
                    }
                }
            } else {
                GradientButton(onClick = { showAddForm = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("+ افزودن دسته‌چک")
                }
            }
        }
    }
}
