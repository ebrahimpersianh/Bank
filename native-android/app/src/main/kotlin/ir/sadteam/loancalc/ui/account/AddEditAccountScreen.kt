package ir.sadteam.loancalc.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.detectBankByCardNumber
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.BankBadge
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
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
    var smsSenderText by remember { mutableStateOf(existing?.smsSender ?: "") }
    var initialBalanceText by remember { mutableStateOf(existing?.initialBalance?.toLong()?.toString() ?: "") }
    var error by remember { mutableStateOf<String?>(null) }
    var showSmsSenderPicker by remember { mutableStateOf(false) }

    if (showSmsSenderPicker) {
        SmsSenderPickerDialog(
            onDismiss = { showSmsSenderPicker = false },
            onPick = { picked ->
                smsSenderText = picked
                showSmsSenderPicker = false
            },
        )
    }

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
            AppCard(label = "شماره کارت") {
                Ltr {
                    OutlinedTextField(
                        value = cardNumberText,
                        onValueChange = { cardNumberText = cleanNum(it).take(16) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            }
        }
        item {
            // خواسته‌ی صریحِ کاربر: بعدِ تشخیصِ خودکار (یا انتخابِ دستی)، لوگوی واقعیِ بانک هم کنارِ
            // فیلد دیده بشه، نه فقط اسمِ متنی - رجوع کن به BankBadge (همون کامپوننتِ مشترکی که
            // BankLoanScreen/LoanDetailScreen هم استفاده می‌کنن).
            AppCard(label = "بانک") {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    BankBadge(bankName = bankName, size = 42.dp)
                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        modifier = Modifier.weight(1f).padding(start = 10.dp),
                        singleLine = true,
                    )
                }
            }
        }
        item {
            // خواسته‌ی صریحِ کاربر: شماره‌ی پیامکِ بانک به حساب وصل بشه تا هر واریز/برداشتی که از
            // همون شماره پیامک می‌شه، خودکار رو همین حساب ثبت بشه (رجوع کن به BankSmsReceiver).
            // Ltr چون سرشماره ذاتاً چپ‌به‌راسته؛ KeyboardType.Text چون بعضی بانک‌ها به‌جای عدد یه
            // نامِ حرفی می‌فرستن (مثلاً BANKMELLAT).
            AppCard(label = "شماره‌ی پیامکِ بانک") {
                Ltr {
                    OutlinedTextField(
                        value = smsSenderText,
                        onValueChange = { smsSenderText = it.trim() },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("مثلاً 100011111 یا BANKMELLAT", fontSize = 12.sp) },
                    )
                }
                // خواسته‌ی صریحِ کاربر: به‌جای تایپِ دستی، بره تو پیامک‌های گوشی و همون‌جا انتخاب کنه.
                OutlinedButton(
                    onClick = { showSmsSenderPicker = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Text("انتخاب از پیامک‌های گوشی")
                }
                Text(
                    "اختیاری. اگه پرش کنی، هر پیامکِ برداشت/واریز از این شماره خودکار رو همین حساب " +
                        "ثبت می‌شه (باید «خوندنِ خودکارِ پیامکِ بانکی» رو تو تنظیمات روشن کرده باشی). " +
                        "شماره رو عیناً از روی پیامکِ بانک تو گوشیت بردار.",
                    color = AppMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        item {
            AppCard(label = "موجودی اولیه") {
                OutlinedTextField(
                    value = initialBalanceText,
                    onValueChange = { initialBalanceText = cleanNum(it) },
                    visualTransformation = ThousandsSeparatorTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    suffix = { Text("ریال", color = AppMuted, fontSize = 13.sp) },
                )
                val balanceRial = initialBalanceText.toLongOrNull() ?: 0L
                if (balanceRial > 0) {
                    Text(
                        "${numberToWordsFa((balanceRial / 10).toDouble())} تومان",
                        color = AppMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
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
                            val smsSender = smsSenderText.trim().ifBlank { null }
                            if (existing == null) {
                                viewModel.addAccount(name.trim(), bankName.trim(), initialBalance, cardNumber, smsSender)
                            } else {
                                viewModel.updateAccount(
                                    existing.copy(
                                        name = name.trim(),
                                        bankName = bankName.trim(),
                                        initialBalance = initialBalance,
                                        cardNumber = cardNumber,
                                        smsSender = smsSender,
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
