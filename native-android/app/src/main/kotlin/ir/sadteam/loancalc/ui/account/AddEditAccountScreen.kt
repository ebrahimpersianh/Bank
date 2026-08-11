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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.DEFAULT_ACCOUNT_ICON_KEY
import ir.sadteam.loancalc.data.accountIconChoices
import ir.sadteam.loancalc.data.db.ACCOUNT_TYPE_BANK
import ir.sadteam.loancalc.data.db.ACCOUNT_TYPE_OTHER
import ir.sadteam.loancalc.data.detectBankByCardNumber
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.BankBadge
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.components.SegmentedToggle
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppPrimary

/** فرم افزودن/ویرایش حساب - هم‌الگو با AddEditChequeScreen (نام، بانک، موجودی اولیه). موجودی اولیه
 * تنها فیلد پولی این فرمه چون موجودی فعلی همیشه از رو تراکنش‌ها محاسبه می‌شه، نه دستی وارد بشه. */
@Composable
fun AddEditAccountScreen(
    existing: AccountEntity?,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AccountViewModel,
) {
    // نوعِ حساب‌کتاب (خواسته‌ی صریحِ کاربر طبقِ اپِ مرجع): «کارت بانکی» یا «منبع دیگر» (نقدی،
    // کیفِ پول، کارتِ اعتباری…). حساب‌های قدیمی همه bank ـن، پس فرمشون دقیقاً مثلِ قبل باز می‌شه.
    var accountType by remember { mutableStateOf(existing?.type ?: ACCOUNT_TYPE_BANK) }
    var iconKey by remember { mutableStateOf(existing?.iconKey ?: DEFAULT_ACCOUNT_ICON_KEY) }
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
            Text(if (existing == null) "افزودن حساب‌کتاب" else "ویرایش حساب‌کتاب", fontSize = 16.sp)
        }
        item {
            AppCard(label = "نوع حساب‌کتاب") {
                SegmentedToggle(
                    options = listOf("کارت بانکی", "منبع دیگر"),
                    selectedIndex = if (accountType == ACCOUNT_TYPE_BANK) 0 else 1,
                    onSelect = { accountType = if (it == 0) ACCOUNT_TYPE_BANK else ACCOUNT_TYPE_OTHER },
                )
            }
        }
        if (accountType == ACCOUNT_TYPE_OTHER) {
            item {
                // آیکونِ حساب‌کتابِ غیربانکی - جایگزینِ لوگوی بانک تو کلِ اپ (رجوع کن به AccountBadge).
                AppCard(label = "آیکون") {
                    // گریدِ دستی با chunked+Row (نه LazyVerticalGrid و نه FlowRowِ آزمایشی) - همون
                    // الگوی مصوبِ پروژه برای گریدِ wrap-contentِ داخلِ یه لیستِ تنبل، رجوع کن به CLAUDE.md.
                    val perRow = 5
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        accountIconChoices.chunked(perRow).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                rowItems.forEach { (key, icon) ->
                                    val selected = key == iconKey
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (selected) AppPrimary.copy(alpha = 0.22f) else AppSurface2,
                                            )
                                            .border(
                                                width = if (selected) 1.5.dp else 0.dp,
                                                color = if (selected) AppPrimary else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp),
                                            )
                                            .pressScaleClickable(onClick = { iconKey = key }),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (selected) AppPrimary else AppMuted,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                }
                                // پرکردنِ جای خالیِ ردیفِ ناقص تا کاشی‌ها کش نیان.
                                repeat(perRow - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }
        }
        item {
            AppCard(label = if (accountType == ACCOUNT_TYPE_BANK) "اسم حساب‌کتاب" else "اسم منبع") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        if (accountType == ACCOUNT_TYPE_BANK) {
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
                // متنِ توضیحیِ زیرِ این دکمه به‌خواستِ صریحِ کاربر حذف شد - فقط خودِ فیلد و دکمه بمونه.
            }
        }
        } // پایانِ بخشِ مخصوصِ «کارت بانکی» - منبعِ دیگر شماره‌کارت/بانک/سرشماره نداره.
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
                        val isBank = accountType == ACCOUNT_TYPE_BANK
                        error = when {
                            name.trim().isEmpty() -> if (isBank) "اسم حساب‌کتاب رو وارد کن" else "اسم منبع رو وارد کن"
                            isBank && bankName.trim().isEmpty() -> "اسم بانک رو وارد کن"
                            else -> null
                        }
                        if (error == null) {
                            // برای «منبعِ دیگر» فیلدهای مخصوصِ بانک عمداً پاک می‌شن - اگه کاربر اول
                            // «کارت بانکی» رو پر کرده و بعد نوع رو عوض کرده، نباید یه شماره‌کارتِ
                            // یتیم رو حسابِ نقدی بمونه (و بدتر: BankSmsReceiver باهاش مچ کنه).
                            val cardNumber = if (isBank) cardNumberText.trim().ifBlank { null } else null
                            val smsSender = if (isBank) smsSenderText.trim().ifBlank { null } else null
                            val finalBank = if (isBank) bankName.trim() else ""
                            val finalIcon = if (isBank) null else iconKey
                            if (existing == null) {
                                viewModel.addAccount(
                                    name.trim(), finalBank, initialBalance, cardNumber, smsSender,
                                    accountType, finalIcon,
                                )
                            } else {
                                viewModel.updateAccount(
                                    existing.copy(
                                        name = name.trim(),
                                        bankName = finalBank,
                                        initialBalance = initialBalance,
                                        cardNumber = cardNumber,
                                        smsSender = smsSender,
                                        type = accountType,
                                        iconKey = finalIcon,
                                    ),
                                )
                            }
                            onSaved()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("ذخیره حساب‌کتاب")
                }
                OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                    Text("انصراف")
                }
            }
        }
    }
}
