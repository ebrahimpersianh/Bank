package ir.sadteam.loancalc.ui.myloans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * فعلاً فقط لیست خالی/موجود تو Room رو نشون می‌ده (بدون فرم افزودن دستی، پرداخت قسط، سینک ابری —
 * اون‌ها فاز ۱ هستن). هدف این تب تو فاز ۰ صرفاً اثبات سیم‌کشی Compose → ViewModel → Repository →
 * Room با Hilt بود.
 */
@Composable
fun MyLoansScreen(viewModel: MyLoansViewModel = hiltViewModel()) {
    val loans by viewModel.loans.collectAsState()

    if (loans.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("هنوز وامی ذخیره نشده", color = AppText, fontSize = 15.sp)
                Text(
                    "فرم افزودن وام و سینک ابری در ادامه‌ی کار اضافه می‌شن",
                    color = AppMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(loans, key = { it.id }) { loan ->
            AppCard {
                Text(loan.name, color = AppText, fontSize = 15.sp)
                Text(loan.bank, color = AppMuted, fontSize = 12.sp)
            }
        }
    }
}
