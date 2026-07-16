package ir.sadteam.loancalc.ui.cheque

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.ChequeType
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.ChequeBookEntity
import ir.sadteam.loancalc.data.db.ChequeEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.AutoShrinkText
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted

private val faMonthNamesCheque = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

/**
 * فرم افزودن/ویرایش چک - هم‌الگو با AddManualLoanScreen (تاریخ با سه دراپ‌داون، اعتبارسنجی ساده،
 * GradientButton برای ذخیره). انتخاب دسته‌چک اختیاریه و اگه شماره‌ی چک هنوز خالی باشه، شماره‌ی
 * سریال بعدی همون دسته‌چک به‌عنوان پیشنهاد اولیه ست می‌شه.
 */
@Composable
fun AddEditChequeScreen(
    existing: ChequeEntity?,
    chequeBooks: List<ChequeBookEntity>,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: ChequeViewModel,
) {
    var type by remember { mutableStateOf(existing?.let { ChequeType.valueOf(it.type) } ?: ChequeType.RECEIVED) }
    var amountText by remember { mutableStateOf(existing?.amount?.toLong()?.toString() ?: "") }
    var chequeNumber by remember { mutableStateOf(existing?.chequeNumber ?: "") }
    var bankName by remember { mutableStateOf(existing?.bankName ?: "") }
    var branchName by remember { mutableStateOf(existing?.branchName ?: "") }
    var ownerName by remember { mutableStateOf(existing?.ownerName ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var dueYear by remember { mutableStateOf(existing?.dueYear ?: 1404) }
    var dueMonth by remember { mutableStateOf(existing?.dueMonth ?: 1) }
    var dueDay by remember { mutableStateOf(existing?.dueDay ?: 1) }
    var chequeBookId by remember { mutableStateOf(existing?.chequeBookId) }
    var error by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(if (existing == null) "افزودن چک" else "ویرایش چک", fontSize = 16.sp)
        }
        item {
            AppCard(label = "نوع چک") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppChip(label = "دریافتی", selected = type == ChequeType.RECEIVED, onClick = { type = ChequeType.RECEIVED })
                    AppChip(label = "پرداختی", selected = type == ChequeType.PAID, onClick = { type = ChequeType.PAID })
                }
            }
        }
        item {
            AppCard(label = "مبلغ (ریال)") {
                // هم‌الگو با بقیه‌ی فیلدهای مبلغِ اپ (وام/درآمد): جداکننده‌ی هزارگان تو خودِ فیلد +
                // معادلِ حروفی تومانی زیرش - قبلاً این یکی فرمتِ ساده‌ی بدونِ کاما داشت.
                OutlinedTextField(
                    value = if (amountText.isEmpty()) "" else fmt((amountText.toLongOrNull() ?: 0L).toDouble()),
                    onValueChange = { amountText = cleanNum(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                val amountVal = amountText.toLongOrNull() ?: 0L
                if (amountVal > 0) {
                    AutoShrinkText(
                        text = "${numberToWordsFa((amountVal / 10).toDouble())} تومان",
                        color = AppAccent,
                        maxFontSize = 11.5.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        if (chequeBooks.isNotEmpty()) {
            item {
                AppCard(label = "دسته‌چک (اختیاری)") {
                    ChequeBookDropdown(
                        chequeBooks = chequeBooks,
                        selected = chequeBookId,
                        onSelect = { book ->
                            chequeBookId = book?.id
                            if (chequeNumber.isBlank() && book != null) {
                                chequeNumber = book.nextSerial.toString()
                            }
                        },
                    )
                }
            }
        }
        item {
            AppCard(label = "شماره چک") {
                OutlinedTextField(
                    value = chequeNumber,
                    onValueChange = { chequeNumber = cleanNum(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        item {
            AppCard(label = "بانک") {
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        item {
            AppCard(label = "شعبه (اختیاری)") {
                OutlinedTextField(
                    value = branchName,
                    onValueChange = { branchName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        item {
            AppCard(label = if (type == ChequeType.RECEIVED) "نام پرداخت‌کننده" else "نام دریافت‌کننده") {
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        item {
            AppCard(label = "تاریخ سررسید") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ChequeDateDropdown(
                        options = (1350..1410).map { it to toFa(it) },
                        selected = dueYear,
                        onSelect = { dueYear = it },
                        modifier = Modifier.weight(1f),
                    )
                    ChequeDateDropdown(
                        options = faMonthNamesCheque.mapIndexed { idx, name -> (idx + 1) to name },
                        selected = dueMonth,
                        onSelect = { dueMonth = it },
                        modifier = Modifier.weight(1f),
                    )
                    ChequeDateDropdown(
                        options = (1..31).map { it to toFa(it) },
                        selected = dueDay,
                        onSelect = { dueDay = it },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        item {
            AppCard(label = "یادداشت (اختیاری)") {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        if (error != null) {
            item {
                Text(text = error ?: "", color = AppDanger, fontSize = 12.sp)
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GradientButton(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        error = when {
                            amount <= 0 -> "مبلغ چک رو وارد کن"
                            chequeNumber.trim().isEmpty() -> "شماره چک رو وارد کن"
                            bankName.trim().isEmpty() -> "اسم بانک رو وارد کن"
                            ownerName.trim().isEmpty() -> "این فیلد رو وارد کن"
                            else -> null
                        }
                        if (error == null) {
                            if (existing == null) {
                                viewModel.addCheque(
                                    type = type,
                                    amount = amount,
                                    chequeNumber = chequeNumber.trim(),
                                    bankName = bankName.trim(),
                                    branchName = branchName.trim(),
                                    ownerName = ownerName.trim(),
                                    dueYear = dueYear,
                                    dueMonth = dueMonth,
                                    dueDay = dueDay,
                                    notes = notes.trim(),
                                    chequeBookId = chequeBookId,
                                    onSaved = onSaved,
                                )
                            } else {
                                viewModel.updateCheque(
                                    existing.copy(
                                        type = type.name,
                                        amount = amount,
                                        chequeNumber = chequeNumber.trim(),
                                        bankName = bankName.trim(),
                                        branchName = branchName.trim(),
                                        ownerName = ownerName.trim(),
                                        dueYear = dueYear,
                                        dueMonth = dueMonth,
                                        dueDay = dueDay,
                                        notes = notes.trim(),
                                        chequeBookId = chequeBookId,
                                    ),
                                    onSaved = onSaved,
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("ذخیره چک")
                }
                OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                    Text("انصراف")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChequeDateDropdown(
    options: List<Pair<Int, String>>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selected }?.second ?: ""
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            modifier = Modifier.menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, label) ->
                DropdownMenuItem(text = { Text(label) }, onClick = { onSelect(value); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChequeBookDropdown(
    chequeBooks: List<ChequeBookEntity>,
    selected: Long?,
    onSelect: (ChequeBookEntity?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedBook = chequeBooks.firstOrNull { it.id == selected }
    val selectedLabel = selectedBook?.let { "${it.ownerName} - ${it.bankName}" } ?: "بدون دسته‌چک"
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("بدون دسته‌چک") }, onClick = { onSelect(null); expanded = false })
            chequeBooks.forEach { book ->
                DropdownMenuItem(
                    text = { Text("${book.ownerName} - ${book.bankName}") },
                    onClick = { onSelect(book); expanded = false },
                )
            }
        }
    }
}
