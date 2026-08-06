package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account_transactions")
data class AccountTransactionEntity(
    @PrimaryKey val id: Long,
    val accountId: Long,
    val type: String,
    val amount: Double,
    val description: String,
    val year: Int,
    val month: Int,
    val day: Int,
    val createdAt: String,
    val category: String? = null,
    // ردیابیِ تراکنش‌هایی که خودکار از پرداختِ قسطِ وام/چک ساخته شدن (رجوع کن به
    // LoanDetailScreen.commitPayment/ChequeDetailScreen.commitPass). sourceType یکی از
    // "loan"/"cheque"ه؛ sourceId برای وام "loanId:m1,m2,..."ه (چون پرداختِ گروهیِ چند قسط با هم
    // می‌تونه یه تراکنشِ ترکیبی بسازه)، برای چک همون chequeId.
    val sourceType: String? = null,
    val sourceId: String? = null,
)
