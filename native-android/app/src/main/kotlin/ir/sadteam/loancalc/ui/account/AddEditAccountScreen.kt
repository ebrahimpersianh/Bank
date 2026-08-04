package ir.sadteam.loancalc.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.cleanNumDecimal
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.detectBankByCardNumber
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted

/** فرم افزودن/ویرایش حساب - هم‌الگو با AddEditChequeScreen (نام، بانک، موجودی اولیه). موجودی اولیه
 * تنها فیلد پولی این فرمه چون موجودی فعلی همیشه از رو تراکنش‌ها محاسبه می‌شه، نه دستی وارد بشه. */
@Composable
fun AddEditAccountScreen(
    existing: AccountEntity?,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AccountViewModel,
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var bankName by remember { mutableStateOf(existing?.bankName ?: "") }
    var cardNumberText by remember { mutableStateOf(existing?.cardNumber ?: "") }
    var initialBalanceText by remember { mutableStateOf(existing?.initialBalance?.let { fmtPlain(it) } ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    // تشخیصِ خودکارِ بانک از رو ۶ رقمِ اولِ شماره‌کارت (رجوع کن به data/BankBin.kt) - فقط یه
    // پیشنهاده: اگه فیلدِ بانک خالیه یا هنوز همون پیشنهادِ خودکارِ قبلیه، به‌روزش می‌کنه؛ اگه کاربر
    // خودش دستی یه چیزِ دیگه تایپ کرده، دیگه بازنویسی نمی‌شه.
    var lastAutoDetected by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(cardNumberText) {
        val detected = detectBankByCardNumber(cardNumberText)
        if (detected != null && (bankName.isBlank() || bankName == lastAutoDetected)) {
            bankName = detected
        }
        lastAutoDetected = detected
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(if (existing == null) "افزودن حساب" else "ویرایش حساب", fontSize = 16.sp)
        }
        item {
            AppCard(label = "اسم حساب") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        item {
            AppCard(label = "شماره کارت (اختیاری)") {
                Ltr {
                    OutlinedTextField(
                        value = cardNumberText,
                        onValueChange = { cardNumberText = cleanNum(it).take(16) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
                Text(
                    "با واردکردنِ ۶ رقمِ اول، بانک خودکار تشخیص داده می‌شه.",
                    color = AppMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp),
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
            AppCard(label = "موجودی اولیه (ریال)") {
                OutlinedTextField(
                    value = initialBalanceText,
                    onValueChange = { initialBalanceText = cleanNumDecimal(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
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
                        val initialBalance = initialBalanceText.toDoubleOrNull() ?: 0.0
                        error = when {
                            name.trim().isEmpty() -> "اسم حساب رو وارد کن"
                            bankName.trim().isEmpty() -> "اسم بانک رو وارد کن"
                            else -> null
                        }
                        if (error == null) {
                            val cardNumber = cardNumberText.trim().ifBlank { null }
                            if (existing == null) {
                                viewModel.addAccount(name.trim(), bankName.trim(), initialBalance, cardNumber)
                            } else {
                                viewModel.updateAccount(
                                    existing.copy(
                                        name = name.trim(),
                                        bankName = bankName.trim(),
                                        initialBalance = initialBalance,
                                        cardNumber = cardNumber,
                                    ),
                                )
                            }
                            onSaved()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("ذخیره حساب")
                }
                OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                    Text("انصراف")
                }
            }
        }
    }
}

private fun fmtPlain(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
