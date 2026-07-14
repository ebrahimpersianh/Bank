package ir.sadteam.loancalc.ui.myloans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * فعلاً لیست وام‌های محلی Room + افزودن دستی/حذف رو نشون می‌ده. فرم ویرایش قسط، پرداخت قسط، سینک
 * ابری و بازکردن جزئیات/جدول یه وام (openSavedLoan تو www/index.html) فاز بعد هستن.
 */
@Composable
fun MyLoansScreen(viewModel: MyLoansViewModel = hiltViewModel()) {
    var showAddForm by remember { mutableStateOf(false) }

    if (showAddForm) {
        AddManualLoanScreen(
            onSaved = { showAddForm = false },
            onCancel = { showAddForm = false },
            viewModel = viewModel,
        )
        return
    }

    val loans by viewModel.loans.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddForm = true },
                containerColor = AppPrimary,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "افزودن وام")
            }
        },
    ) { padding ->
        if (loans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("هنوز وامی ذخیره نشده", color = AppText, fontSize = 15.sp)
                    Text(
                        "با دکمه‌ی + یه وام دستی اضافه کن",
                        color = AppMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(loans, key = { it.id }) { loan ->
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(loan.name, color = AppText, fontSize = 15.sp)
                            Text(loan.bank, color = AppMuted, fontSize = 12.sp)
                        }
                        IconButton(onClick = { viewModel.deleteLoan(loan.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "حذف وام", tint = AppDanger)
                        }
                    }
                }
            }
        }
    }
}
