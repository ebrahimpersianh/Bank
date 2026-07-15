package ir.sadteam.loancalc.ui.myloans

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.auth.GateState
import ir.sadteam.loancalc.ui.auth.LoginScreen
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * فعلاً لیست وام‌های محلی Room + افزودن دستی/حذف/بازکردن جزئیات (پرداخت قسط) رو نشون می‌ده.
 * سینک ابری واقعی و ویرایش دستی مبلغ هر قسط فاز بعد هستن.
 *
 * پورت canSaveAnotherLoan/handleLoanLimitReached تو www/index.html: بعد از اولین وام، مهمون‌ها
 * باید وارد بشن (LoginScreen غیراجباری، با دکمه‌ی بازگشت)، کاربرهای واردشده‌ی بدون اشتراک باید
 * اشتراک بخرن (فعلاً فقط یه پیام - خرید واقعی کافه‌بازار فاز بعده).
 */
@Composable
fun MyLoansScreen(viewModel: MyLoansViewModel = hiltViewModel(), authViewModel: AuthViewModel = hiltViewModel()) {
    var showAddForm by remember { mutableStateOf(false) }
    var openedLoanId by remember { mutableStateOf<Long?>(null) }
    var showLoginPrompt by remember { mutableStateOf(false) }
    var showSubscriptionDialog by remember { mutableStateOf(false) }

    val loans by viewModel.loans.collectAsState()
    val gateState by authViewModel.gateState.collectAsState()
    val subscribed by authViewModel.subscribed.collectAsState()
    val canSaveAnotherLoan = loans.isEmpty() || (gateState == GateState.LOGGED_IN && subscribed)

    if (showLoginPrompt) {
        LoginScreen(
            onDismiss = { showLoginPrompt = false },
            onLoginSuccess = { showLoginPrompt = false },
        )
        return
    }

    if (showSubscriptionDialog) {
        AlertDialog(
            onDismissRequest = { showSubscriptionDialog = false },
            title = { Text("نیاز به اشتراک") },
            text = { Text("برای ذخیره‌ی بیش از یه وام باید اشتراک بخری. خرید اشتراک هنوز تو این نسخه پیاده نشده.") },
            confirmButton = {
                TextButton(onClick = { showSubscriptionDialog = false }) { Text("متوجه شدم") }
            },
        )
    }

    if (showAddForm) {
        AddManualLoanScreen(
            onSaved = { showAddForm = false },
            onCancel = { showAddForm = false },
            viewModel = viewModel,
        )
        return
    }

    val openedLoan = openedLoanId?.let { id -> loans.firstOrNull { it.id == id } }
    if (openedLoan != null) {
        LoanDetailScreen(
            loan = openedLoan,
            onBack = { openedLoanId = null },
            onDelete = { viewModel.deleteLoan(openedLoan.id); openedLoanId = null },
            onMarkNextPaid = { viewModel.setPaidCount(openedLoan, openedLoan.paidCount + 1) },
            onUndoLastPaid = { viewModel.setPaidCount(openedLoan, openedLoan.paidCount - 1) },
        )
        return
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when {
                        canSaveAnotherLoan -> showAddForm = true
                        gateState == null -> Unit // هنوز از DataStore خونده نشده، صبر کن
                        gateState != GateState.LOGGED_IN -> showLoginPrompt = true
                        else -> showSubscriptionDialog = true
                    }
                },
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
                AppCard(modifier = Modifier.clickable { openedLoanId = loan.id }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(loan.name, color = AppText, fontSize = 15.sp)
                            Text(loan.bank, color = AppMuted, fontSize = 12.sp)
                            Text(
                                "${loan.paidCount} از ${loan.n} قسط پرداخت‌شده",
                                color = AppPrimary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
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
