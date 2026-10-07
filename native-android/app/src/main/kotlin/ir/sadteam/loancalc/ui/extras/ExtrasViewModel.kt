package ir.sadteam.loancalc.ui.extras

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.sadteam.loancalc.data.db.BillDao
import ir.sadteam.loancalc.data.db.BillEntity
import ir.sadteam.loancalc.data.db.TxTemplateDao
import ir.sadteam.loancalc.data.db.TxTemplateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * الگوهای تراکنش، قبض‌ها و عکسِ رسید - قابلیت‌های برگرفته از مقایسه با پارمیس/پولکس (۶ مهر).
 */
@HiltViewModel
class ExtrasViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val templateDao: TxTemplateDao,
    private val billDao: BillDao,
    private val accountRepository: ir.sadteam.loancalc.data.AccountRepository,
) : ViewModel() {
    val accounts = accountRepository.observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val templates: StateFlow<List<TxTemplateEntity>> = templateDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bills: StateFlow<List<BillEntity>> = billDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveTemplate(name: String, type: String, amountRial: Double, category: String?, accountId: Long?) {
        ir.sadteam.loancalc.data.UsageStats.action("template_saved")
        viewModelScope.launch {
            templateDao.upsert(
                TxTemplateEntity(
                    id = System.currentTimeMillis(),
                    name = name.trim(),
                    type = type,
                    amount = amountRial,
                    category = category,
                    accountId = accountId,
                ),
            )
        }
    }

    fun deleteTemplate(t: TxTemplateEntity) {
        viewModelScope.launch { templateDao.delete(t) }
    }

    fun saveBill(bill: BillEntity) {
        ir.sadteam.loancalc.data.UsageStats.action("bill_saved")
        viewModelScope.launch { billDao.upsert(bill) }
    }

    fun deleteBill(bill: BillEntity) {
        viewModelScope.launch { billDao.delete(bill) }
    }

    /** «پرداخت شد» برای دوره‌ی جاری - یادآورِ همین دوره دیگر نمی‌آید. */
    fun markBillPaid(bill: BillEntity, year: Int, month: Int, amountRial: Double, accountId: Long?) {
        ir.sadteam.loancalc.data.UsageStats.action("bill_paid")
        viewModelScope.launch {
            billDao.upsert(bill.copy(lastPaidKey = "$year-$month", lastAmount = amountRial))
            // پرداختِ قبض هم مثلِ قسط از حساب کم می‌شود (بخشِ «اتصالِ پرداخت‌ها»).
            accountRepository.recordLinkedPayment(
                accountId = accountId,
                sourceType = "bill",
                sourceId = "${bill.id}:$year-$month",
                amount = amountRial,
                description = "قبض ${billKindLabel(bill.kind)} - ${bill.name}",
                category = "قبض",
            )
        }
    }

    /** «برگرداندنِ پرداخت»: دوره دوباره پرداخت‌نشده می‌شود و مبلغش به حساب برمی‌گردد. */
    fun unmarkBillPaid(bill: BillEntity, year: Int, month: Int) {
        viewModelScope.launch {
            billDao.upsert(bill.copy(lastPaidKey = null))
            accountRepository.removeLinkedPayment("bill", "${bill.id}:$year-$month")
        }
    }

    /**
     * عکسِ رسید را به حافظه‌ی **داخلیِ خودِ برنامه** کپی می‌کند (نه گالری) و مسیرش را برمی‌گرداند.
     * پشتیبانِ ابری عکس را نمی‌برد - فقط روی همین گوشی می‌ماند.
     */
    suspend fun importReceipt(uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(context.filesDir, "receipts").apply { mkdirs() }
            val out = File(dir, "r_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input -> out.outputStream().use { input.copyTo(it) } }
            out.absolutePath
        }.getOrNull()
    }
}
