package ir.sadteam.loancalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.BankLoanOutcome
import ir.sadteam.loancalc.ui.BankLoanScreen
import ir.sadteam.loancalc.ui.ResultScreen
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.LoanCalcTheme

private enum class BottomTab(val label: String, val icon: ImageVector) {
    BANK_LOAN("وام بانکی", Icons.Filled.AccountBalance),
    AFFORD("محاسبه‌گر", Icons.Filled.Calculate),
    DEPOSIT("سود سپرده", Icons.Filled.Savings),
    MY_LOANS("وام‌های من", Icons.Filled.Folder),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoanCalcTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    LoanCalcApp()
                }
            }
        }
    }
}

@Composable
private fun LoanCalcApp() {
    var activeTab by remember { mutableStateOf(BottomTab.BANK_LOAN) }
    var loanOutcome by remember { mutableStateOf<BankLoanOutcome?>(null) }

    Scaffold(
        bottomBar = {
            Surface(color = AppSurface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    BottomTab.entries.forEach { tab ->
                        BottomNavItem(
                            tab = tab,
                            selected = activeTab == tab,
                            onClick = {
                                activeTab = tab
                                if (tab == BottomTab.BANK_LOAN) loanOutcome = null
                            },
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                activeTab == BottomTab.BANK_LOAN && loanOutcome == null -> {
                    BankLoanScreen(onCalculated = { loanOutcome = it })
                }
                activeTab == BottomTab.BANK_LOAN && loanOutcome != null -> {
                    ResultScreen(outcome = loanOutcome!!)
                }
                else -> {
                    // بقیه‌ی تب‌ها (محاسبه‌گر، سود سپرده، وام‌های من) هنوز پورت نشدن -
                    // فعلاً طبق تصمیم اول، فقط تب «وام بانکی» کامل ساخته شده.
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            "این تب هنوز به Kotlin پورت نشده",
                            color = AppMuted,
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.BottomNavItem(
    tab: BottomTab,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = if (selected) AppPrimary else AppMuted
    Column(
        modifier = Modifier
            .weight(1f)
            .then(if (selected) Modifier.background(AppPrimary.copy(alpha = 0.1f)) else Modifier)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(tab.icon, contentDescription = tab.label, tint = color, modifier = Modifier.height(22.dp))
        Text(
            tab.label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
