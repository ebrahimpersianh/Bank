package ir.sadteam.loancalc.ui.due

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.debt.DebtScreen
import ir.sadteam.loancalc.ui.note.NoteScreen
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.Motion

private enum class DueSubView { NONE, DEBT, NOTES }

private data class DueShortcut(
    val title: String,
    val icon: ImageVector,
    // برای «تراکنش»/«قسط و وام»/«چک»/«حساب‌کتاب» که به تبِ دیگه‌ای می‌رن؛ null یعنی این کاشی
    // زیرصفحه‌ی داخلیِ خودِ همین تبه (رجوع کن به [subView]).
    val route: String?,
    val subView: DueSubView?,
)

/**
 * تبِ «سررسید» - گریدِ میان‌برِ کاشی‌ای، هم‌الگو با صفحه‌ی مرجع. تراکنش/قسط‌ووام/چک/حساب‌کتاب به
 * بخش‌های موجودِ اپ (وامِ ادغام‌شده/چک/حسابداری) وصل می‌شن؛ طلب‌وبدهی و یادداشتِ مستقل زیرصفحه‌ی
 * داخلیِ خودِ همین تبن (رجوع کن به [DueSubView]) - هم‌الگو با ChequeScreen.screenKey.
 */
@Composable
fun DueScreen(onNavigateToRoute: (String) -> Unit) {
    var subView by rememberSaveable { mutableStateOf(DueSubView.NONE) }
    // برگشتن از طلب‌وبدهی/یادداشت به گریدِ اصلی - قبل از رسیدن به BackHandlerِ بیرونیِ LoanCalcApp
    // (که دیگه معنیش خروج/برگشتنِ بینِ تب‌هاست)، هم‌الگو با LoanTab.
    BackHandler(enabled = subView != DueSubView.NONE) { subView = DueSubView.NONE }
    val shortcuts = remember {
        listOf(
            DueShortcut("طلب و بدهی", Icons.Filled.Handshake, null, DueSubView.DEBT),
            DueShortcut("یادداشت", Icons.Filled.EditNote, null, DueSubView.NOTES),
            DueShortcut("تراکنش", Icons.Filled.SwapHoriz, "accounting", null),
            DueShortcut("قسط و وام", Icons.Filled.Payments, "loan", null),
            DueShortcut("چک", Icons.Filled.ReceiptLong, "cheque", null),
            DueShortcut("حساب‌کتاب", Icons.Filled.AccountBalanceWallet, "accounting", null),
        )
    }

    AnimatedContent(
        targetState = subView,
        transitionSpec = { Motion.contentEnter togetherWith Motion.contentExit },
        label = "dueScreen",
    ) { current ->
        when (current) {
            DueSubView.DEBT -> DebtScreen(onBack = { subView = DueSubView.NONE })
            DueSubView.NOTES -> NoteScreen(onBack = { subView = DueSubView.NONE })
            DueSubView.NONE -> DueGrid(
                shortcuts = shortcuts,
                onNavigateToRoute = onNavigateToRoute,
                onOpenSubView = { subView = it },
            )
        }
    }
}

@Composable
private fun DueGrid(
    shortcuts: List<DueShortcut>,
    onNavigateToRoute: (String) -> Unit,
    onOpenSubView: (DueSubView) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AppCard {
                Text("سررسید", color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(
                    "میان‌برِ سریع به طلب‌وبدهی، یادداشت، تراکنش، قسط و چک.",
                    color = AppMuted,
                    fontSize = 12.5.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .size(480.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(shortcuts) { shortcut ->
                    DueShortcutTile(
                        shortcut = shortcut,
                        onClick = {
                            shortcut.subView?.let(onOpenSubView)
                            shortcut.route?.let(onNavigateToRoute)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DueShortcutTile(shortcut: DueShortcut, onClick: () -> Unit) {
    AppCard(
        modifier = Modifier
            .aspectRatio(1.35f)
            .pressScaleClickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(AppPrimary.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(shortcut.icon, contentDescription = shortcut.title, tint = AppPrimary)
            }
            Text(
                shortcut.title,
                color = AppText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}
