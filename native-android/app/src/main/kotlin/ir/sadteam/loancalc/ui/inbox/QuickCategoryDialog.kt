package ir.sadteam.loancalc.ui.inbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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
    onPicked: ((String) -> Unit)? = null,
    viewModel: QuickCategoryViewModel = hiltViewModel(),
) {
    LaunchedEffect(txId) { viewModel.load(txId) }
    val tx by viewModel.tx.collectAsState()
    val options by viewModel.options.collectAsState()
    val t = tx
    JibakAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("دسته‌ی این تراکنش؟", fontSize = 16.sp, fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (t != null) {
                    val amount = ir.sadteam.loancalc.core.fmt(rialToToman(t.amount.toLong()).toDouble()).faDigits()
                    Text("$amount ${ir.sadteam.loancalc.ui.jibak.unitFa()}", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text(t.description, color = AppMuted, fontSize = 12.sp, maxLines = 1)
                    if (CategoryLearning.counterpartyOf(t) != null) {
                        Text("دفعه‌ی بعد برای همین طرف، خودم همین دسته را می‌زنم.", color = AppMuted, fontSize = 11.sp)
                    }
                }
                // ۱۶ مهر: کاشی‌های آیکون‌دار (به‌جای تراشه‌های متنی‌ِ بی‌نظم) - چهار ستون.
                options.chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { c ->
                            val selected = t?.category == c.name
                            Column(
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                                    .background(if (selected) c.color.copy(alpha = 0.22f) else androidx.compose.ui.graphics.Color.Transparent)
                                    .clickable {
                                        viewModel.pick(txId, c.name) { onPicked?.invoke(c.name); onDismiss() }
                                    }
                                    .padding(vertical = 8.dp),
                            ) {
                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier.size(44.dp).clip(androidx.compose.foundation.shape.CircleShape).background(c.color.copy(alpha = 0.18f)),
                                    contentAlignment = androidx.compose.ui.Alignment.Center,
                                ) {
                                    androidx.compose.material3.Icon(c.icon, contentDescription = null, tint = c.color, modifier = Modifier.size(22.dp))
                                }
                                Text(
                                    c.name,
                                    color = AppText,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
                                    maxLines = 1,
                                    modifier = Modifier.padding(top = 5.dp),
                                )
                            }
                        }
                        repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("بعداً") } },
        dismissButton = onShowSource?.let { show -> { TextButton(onClick = show) { Text("متنِ پیام") } } },
    )
}
