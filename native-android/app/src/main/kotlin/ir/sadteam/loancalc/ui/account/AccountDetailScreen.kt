package ir.sadteam.loancalc.ui.account

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import ir.sadteam.loancalc.core.cleanNumDecimal
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

private val faMonthNamesAccount = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

/**
 * جزئیات یه حساب - موجودی فعلی بزرگ بالای صفحه، فرم افزودن تراکنش (واریز/برداشت + توضیح + تاریخ
 * شمسی)، و دفترچه‌ی تراکنش‌ها (تاریخ نزولی، هر ردیف قابل‌حذف). ویرایش/حذف خودِ حساب هم از همین‌جا.
 */
@Composable
fun AccountDetailScreen(
    account: AccountEntity,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val allTransactions by viewModel.transactions.collectAsState()
    val transactions = remember(allTransactions, account.id) {
        allTransactions.filter { it.accountId == account.id }
    }
    val balance = viewModel.balanceOf(account, allTransactions)

    var showAddTransaction by remember { mutableStateOf(false) }
    var txType by remember { mutableStateOf(TransactionType.DEPOSIT) }
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val today = remember { JalaliCalendar.today() }
    var txYear by remember { mutableStateOf(today.y) }
    var txMonth by remember { mutableStateOf(today.m) }
    var txDay by remember { mutableStateOf(today.d) }
    var error by remember { mutableStateOf<String?>(null) }

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
                    "${fmt(balance)} ریال",
                    color = if (balance < 0) AppDanger else AppPrimary,
                    fontSize = 22.sp,
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Text("ویرایش حساب")
                }
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppDanger),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("حذف حساب")
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
                    AppCard(label = "مبلغ (ریال)") {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = cleanNumDecimal(it) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
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
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            AccountDateDropdown(
                                options = (1380..1410).map { it to toFa(it) },
                                selected = txYear,
                                onSelect = { txYear = it },
                                modifier = Modifier.weight(1f),
                            )
                            AccountDateDropdown(
                                options = faMonthNamesAccount.mapIndexed { idx, name -> (idx + 1) to name },
                                selected = txMonth,
                                onSelect = { txMonth = it },
                                modifier = Modifier.weight(1f),
                            )
                            AccountDateDropdown(
                                options = (1..31).map { it to toFa(it) },
                                selected = txDay,
                                onSelect = { txDay = it },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    if (error != null) {
                        Text(text = error ?: "", color = AppDanger, fontSize = 12.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GradientButton(
                            onClick = {
                                val amount = amountText.toDoubleOrNull() ?: 0.0
                                error = if (amount <= 0) "مبلغ رو وارد کن" else null
                                if (error == null) {
                                    viewModel.addTransaction(
                                        accountId = account.id,
                                        type = txType,
                                        amount = amount,
                                        description = description.trim(),
                                        year = txYear,
                                        month = txMonth,
                                        day = txDay,
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
                Box(modifier = Modifier.fillMaxWidth().padding(top = 30.dp), contentAlignment = Alignment.Center) {
                    Text("هنوز تراکنشی ثبت نشده", color = AppText, fontSize = 14.sp)
                }
            }
        } else {
            items(transactions, key = { it.id }) { tx ->
                TransactionRow(tx = tx, onDelete = { viewModel.deleteTransaction(tx) })
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: AccountTransactionEntity, onDelete: () -> Unit) {
    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    if (tx.type == TransactionType.DEPOSIT.name) "واریز" else "برداشت",
                    color = if (tx.type == TransactionType.DEPOSIT.name) AppPrimary else AppDanger,
                    fontSize = 13.sp,
                )
                if (tx.description.isNotBlank()) {
                    Text(tx.description, color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                }
                Text(
                    "${toFa(tx.day)}/${toFa(tx.month)}/${toFa(tx.year)}",
                    color = AppMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${if (tx.type == TransactionType.DEPOSIT.name) "+" else "-"}${fmt(tx.amount)}",
                    color = if (tx.type == TransactionType.DEPOSIT.name) AppPrimary else AppDanger,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(end = 8.dp),
                )
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppDanger),
                ) {
                    Text("حذف", fontSize = 11.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountDateDropdown(
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
