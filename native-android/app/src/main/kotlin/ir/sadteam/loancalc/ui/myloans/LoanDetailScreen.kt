package ir.sadteam.loancalc.ui.myloans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * پورت ساده‌شده‌ی openDetail/renderTable تو www/index.html، فقط برای وام‌های دستی (method=manual).
 * برخلاف وب که وضعیت پرداخت هر قسط مستقل و با تاریخ/تاخیر ثبت می‌شه (rows[].paid/paidLate/paidDate)،
 * اینجا فعلاً فقط یه آستانه‌ی ترتیبی (paidCount) داریم - «ثبت پرداخت قسط بعدی»/«لغو آخرین پرداخت».
 * پورت کامل مدل مستقل هر قسط (همراه تاریخ سررسید واقعی) فاز بعده.
 */
@Composable
fun LoanDetailScreen(
    loan: LoanEntity,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onMarkNextPaid: () -> Unit,
    onUndoLastPaid: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                }
                Text(loan.name, color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
            }
        }

        item {
            AppCard(label = loan.bank, modifier = Modifier.padding(horizontal = 14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("مبلغ هر قسط", fontSize = 11.sp, color = AppMuted)
                        Text("${fmt(loan.installment)} ریال", fontSize = 13.sp, color = AppText)
                    }
                    Column {
                        Text("پرداخت‌شده", fontSize = 11.sp, color = AppMuted)
                        Text("${toFa(loan.paidCount)} از ${toFa(loan.n)}", fontSize = 13.sp, color = AppPrimary)
                    }
                }
            }
        }

        items(loan.n) { idx ->
            val m = idx + 1
            val paid = m <= loan.paidCount
            val balance = (loan.n - m) * loan.installment
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("قسط ${toFa(m)}", color = AppText, fontSize = 12.5.sp)
                Text("مانده: ${fmt(balance)} ریال", color = AppMuted, fontSize = 11.sp)
                Text(
                    if (paid) "پرداخت‌شده ✓" else "در انتظار",
                    color = if (paid) AppPrimary else AppMuted,
                    fontSize = 11.5.sp,
                )
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onMarkNextPaid,
                    enabled = loan.paidCount < loan.n,
                    colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("ثبت پرداخت قسط بعدی")
                }
                OutlinedButton(
                    onClick = onUndoLastPaid,
                    enabled = loan.paidCount > 0,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("لغو آخرین پرداخت")
                }
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppDanger),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                ) {
                    Text("حذف وام")
                }
            }
        }
    }
}
