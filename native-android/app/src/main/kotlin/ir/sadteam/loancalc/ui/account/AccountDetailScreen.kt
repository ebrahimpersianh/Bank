package ir.sadteam.loancalc.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.cleanNum
import ir.sadteam.loancalc.core.numberToWordsFa
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.CalendarPickerScreen
import ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.SwipeToDeleteRow
import ir.sadteam.loancalc.ui.components.ThousandsSeparatorTransformation
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.toFaDate
import ir.sadteam.loancalc.ui.jibak.toFaDateNumeric
import ir.sadteam.loancalc.ui.jibak.toFaMoney
import ir.sadteam.loancalc.ui.jibak.toFaSignedMoney
import ir.sadteam.loancalc.ui.jibak.tomanToRial
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * جزئیات یه حساب - موجودی فعلی بزرگ بالای صفحه، فرم افزودن تراکنش (واریز/برداشت + توضیح + تاریخ
 * شمسی)، و دفترچه‌ی تراکنش‌ها (تاریخ نزولی، هر ردیف قابل‌حذف).
 *
 * ⚠️ `onEdit`/`onDelete` **اختیاری**اند - از تبِ دارایی ویرایش هست، حذف نیست: کاربر همون‌جا
 * می‌بینه اسم/فرستنده‌ی پیامک غلطه و باید بتونه درستش کنه، ولی حذف از مسیرِ تماشا جای درستی نیست.
 */
@Composable
fun AccountDetailScreen(
    account: AccountEntity,
    onBack: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val allTransactions by viewModel.transactions.collectAsState()
    val transactions = remember(allTransactions, account.id) {
        allTransactions.filter { it.accountId == account.id }
    }
    val balance = remember(account, allTransactions) { viewModel.balanceOf(account, allTransactions) }

    var showAddTransaction by remember { mutableStateOf(false) }
    var txType by remember { mutableStateOf(TransactionType.DEPOSIT) }
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var txDate by remember { mutableStateOf(JalaliCalendar.today()) }
    // فرم هر بار که باز می‌شه امروز رو دوباره حساب می‌کنه - `remember` تنها یک‌بار حساب می‌شد
    // و اگه گوشی از نیمه‌شب رد می‌شد، تاریخِ پیش‌فرض دیروز می‌موند.
    LaunchedEffect(showAddTransaction) {
        if (showAddTransaction) txDate = JalaliCalendar.today()
    }
    var showCalendar by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deletingTx by remember { mutableStateOf<AccountTransactionEntity?>(null) }

    // تقویم **روی** صفحه می‌نشیند، نه به‌جایش. با `return` کلِ LazyColumn از کامپوزیشن بیرون
    // می‌رفت و اسکرولِ دفترچه‌ی تراکنش‌ها با هر انتخابِ تاریخ صفر می‌شد.
    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 100.dp),
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
                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Text(account.name, color = AppText, fontSize = 16.sp)
                    Text(account.bankName, color = AppMuted, fontSize = 12.sp)
                }
            }
        }

        item {
            AppCard(label = "موجودی فعلی") {
                Text(
                    "${balance.toLong().toFaMoney()} تومان",
                    color = if (balance < 0) AppDangerInk else AppPrimaryInk,
                    fontSize = 22.sp,
                )
            }
        }

        if (onEdit != null || onDelete != null) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    onEdit?.let {
                        OutlinedButton(onClick = it, modifier = Modifier.weight(1f)) {
                            Text("ویرایش حساب")
                        }
                    }
                    onDelete?.let {
                        OutlinedButton(
                            onClick = { showDeleteConfirm = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppDanger),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("حذف حساب")
                        }
                    }
                }
            }
        }

        item {
            if (showAddTransaction) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppCard(label = "نوع تراکنش") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppChip(
                                label = "واریز",
                                selected = txType == TransactionType.DEPOSIT,
                                onClick = { txType = TransactionType.DEPOSIT },
                            )
                            AppChip(
                                label = "برداشت",
                                selected = txType == TransactionType.WITHDRAWAL,
                                onClick = { txType = TransactionType.WITHDRAWAL },
                            )
                        }
                    }
                    AppCard(label = "مبلغ") {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = cleanNum(it) },
                            visualTransformation = ThousandsSeparatorTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            suffix = { Text("تومان", color = AppMuted, fontSize = 13.sp) },
                        )
                        val amountToman = amountText.toLongOrNull() ?: 0L
                        if (amountToman > 0) {
                            Text(
                                "${numberToWordsFa(amountToman.toDouble())} تومان",
                                color = AppMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                    AppCard(label = "توضیح (اختیاری)") {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                    }
                    AppCard(label = "تاریخ") {
                        Row(
                            modifier = Modifier.fillMaxWidth().pressScaleClickable { showCalendar = true },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = AppMuted, modifier = Modifier.size(18.dp))
                            Text(
                                toFaDate(txDate.y, txDate.m, txDate.d),
                                color = AppText,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f).padding(start = 8.dp),
                            )
                        }
                    }
                    if (error != null) {
                        Text(text = error ?: "", color = AppDanger, fontSize = 12.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GradientButton(
                            onClick = {
                                val toman = amountText.toLongOrNull() ?: 0L
                                error = if (toman <= 0L) "مبلغ رو وارد کن" else null
                                if (error == null) {
                                    viewModel.addTransaction(
                                        accountId = account.id,
                                        type = txType,
                                        amount = tomanToRial(toman).toDouble(),
                                        description = description.trim(),
                                        year = txDate.y,
                                        month = txDate.m,
                                        day = txDate.d,
                                    )
                                    amountText = ""
                                    description = ""
                                    showAddTransaction = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("ثبت تراکنش")
                        }
                        OutlinedButton(onClick = { showAddTransaction = false }, modifier = Modifier.weight(1f)) {
                            Text("انصراف")
                        }
                    }
                }
            } else {
                GradientButton(onClick = { showAddTransaction = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("+ ثبت تراکنش")
                }
            }
        }

        if (transactions.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.SwapVert,
                    title = "هنوز تراکنشی ثبت نشده",
                    description = "واریز و برداشت‌های این حساب که ثبت بشن، همین‌جا " +
                        "به‌ترتیبِ تاریخ می‌بینیشون.",
                )
            }
        } else {
            items(transactions, key = { it.id }) { tx ->
                TransactionRow(
                    tx = tx,
                    onDelete = { deletingTx = tx },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
        if (showCalendar) {
            // زمینه اجباری است - CalendarPickerScreen خودش زمینه ندارد و بی این، صفحه‌ی زیرش
            // از لابه‌لایش دیده می‌شود.
            Box(modifier = Modifier.fillMaxSize().background(AppSurface)) {
                CalendarPickerScreen(
                    initialDate = txDate,
                    onDateSelected = { txDate = it; showCalendar = false },
                    onBack = { showCalendar = false },
                )
            }
        }

        if (showDeleteConfirm) {
            ConfirmDeleteDialog(
                title = "حذف حساب",
                text = "حسابِ «${account.name} - ${account.bankName}» حذف بشه؟ این کار قابلِ‌برگشت نیست.",
                // پرچم را خودش پایین می‌آورد - وابسته‌بودن به این‌که onDelete صفحه را ببندد
                // یک وابستگیِ نامرئی بود.
                onConfirm = { showDeleteConfirm = false; onDelete?.invoke() },
                onDismiss = { showDeleteConfirm = false },
            )
        }
        deletingTx?.let { tx ->
            ConfirmDeleteDialog(
                title = "حذفِ تراکنش",
                text = "این تراکنش حذف بشه؟ این کار قابلِ‌برگشت نیست.",
                onConfirm = { viewModel.deleteTransaction(tx); deletingTx = null },
                onDismiss = { deletingTx = null },
            )
        }
    }
}

/** دو ستون: نوع+توضیح+تاریخ و مبلغِ باعلامت. دکمه‌ی «حذف»ِ دائمی برداشته شد - `SwipeToDeleteRow`
 * از قبل همون کار رو می‌کرد، دو راهِ حذف روی یه ردیف زیادی بود. */
@Composable
private fun TransactionRow(tx: AccountTransactionEntity, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    SwipeToDeleteRow(onDelete = onDelete, confirmDismiss = false, modifier = modifier) {
        AppCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        if (tx.type == TransactionType.DEPOSIT.name) "واریز" else "برداشت",
                        color = if (tx.type == TransactionType.DEPOSIT.name) AppPrimaryInk else AppDangerInk,
                        fontSize = 13.sp,
                    )
                    if (tx.description.isNotBlank()) {
                        Text(tx.description, color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                    Text(
                        // «۱۴۰۵/۰۶/۱۰» - بی صفرِ پیشوند ستون نمی‌چیند.
                        toFaDateNumeric(tx.year, tx.month, tx.day),
                        color = AppMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Text(
                    tx.amount.toLong().let { if (tx.type == TransactionType.DEPOSIT.name) it else -it }
                        .toFaSignedMoney(),
                    color = if (tx.type == TransactionType.DEPOSIT.name) AppPrimaryInk else AppDangerInk,
                    fontSize = 13.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                )
            }
        }
    }
}
