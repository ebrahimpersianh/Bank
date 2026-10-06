package ir.sadteam.loancalc.ui.inbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.CategoryEntry
import ir.sadteam.loancalc.data.CategoryRepository
import ir.sadteam.loancalc.data.ParsingRuleRepository
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.notifications.CategoryLearning
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppText
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 🧠 انتخابِ سریعِ دسته‌ی یک تراکنشِ خودکار (۱۴ مهر). از دکمه‌ی «انتخابِ دسته/بقیه…»ِ اعلان و از
 * لمسِ کارتِ تراکنش در پیام‌ها باز می‌شود - قبلاً فقط صفحه‌ی پیام‌ها باز می‌شد و کاربر نمی‌دانست
 * کجا دسته بزند. انتخاب همان لحظه **یاد گرفته می‌شود** ([CategoryLearning]).
 */
@HiltViewModel
class QuickCategoryViewModel @Inject constructor(
    private val accounts: AccountRepository,
    private val categories: CategoryRepository,
    private val rules: ParsingRuleRepository,
) : ViewModel() {
    private val _tx = MutableStateFlow<AccountTransactionEntity?>(null)
    val tx: StateFlow<AccountTransactionEntity?> = _tx

    @OptIn(ExperimentalCoroutinesApi::class)
    val options: StateFlow<List<CategoryEntry>> = _tx.flatMapLatest { t ->
        if (t == null) flowOf(emptyList())
        else categories.orderedCategories(if (t.type == TransactionType.DEPOSIT.name) TransactionType.DEPOSIT else TransactionType.WITHDRAWAL)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun load(txId: Long) {
        viewModelScope.launch { _tx.value = accounts.transactionById(txId) }
    }

    fun pick(txId: Long, category: String, done: () -> Unit) {
        viewModelScope.launch {
            CategoryLearning.apply(accounts, rules, txId, category)
            done()
        }
    }
}

@Composable
fun QuickCategoryDialog(
    txId: Long,
    onDismiss: () -> Unit,
    onShowSource: (() -> Unit)? = null,
    viewModel: QuickCategoryViewModel = hiltViewModel(),
) {
    LaunchedEffect(txId) { viewModel.load(txId) }
    val tx by viewModel.tx.collectAsState()
    val options by viewModel.options.collectAsState()
    val t = tx
    JibakAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("دسته‌ی این تراکنش؟", fontSize = 15.sp, fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (t != null) {
                    val amount = ir.sadteam.loancalc.core.fmt(rialToToman(t.amount.toLong()).toDouble()).faDigits()
                    Text("$amount تومان · ${t.description}", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    if (CategoryLearning.counterpartyOf(t) != null) {
                        Text("دفعه‌ی بعد برای همین طرف، خودم همین دسته را می‌زنم.", color = AppMuted, fontSize = 11.sp)
                    }
                }
                options.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { c ->
                            AppChip(
                                c.name,
                                selected = t?.category == c.name,
                                onClick = { viewModel.pick(txId, c.name, onDismiss) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("بستن") } },
        dismissButton = onShowSource?.let { show -> { TextButton(onClick = show) { Text("متنِ پیام") } } },
    )
}
