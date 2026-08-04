package ir.sadteam.loancalc.ui.due

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

private data class DueShortcut(
    val title: String,
    val icon: ImageVector,
    // route == null یعنی این فیچر هنوز پیاده نشده (طلب‌وبدهی/یادداشتِ مستقل - رجوع کن به CLAUDE.md،
    // بخشِ فازهای بعدیِ بازسازیِ جامع) - تپ‌کردنش فعلاً کاری نمی‌کنه، فقط برچسبِ «به‌زودی» می‌گیره.
    val route: String?,
)

/**
 * تبِ «سررسید» - نسخه‌ی *اولیه*. گریدِ میان‌برِ کاشی‌ای، هم‌الگو با صفحه‌ی مرجع - تراکنش/قسط‌ووام/
 * چک/حساب‌کتاب به بخش‌های موجودِ اپ وصل می‌شن؛ طلب‌وبدهی و یادداشتِ مستقل هنوز وجود ندارن (فازِ
 * بعدی).
 */
@Composable
fun DueScreen(onNavigateToRoute: (String) -> Unit) {
    val shortcuts = remember {
        listOf(
            DueShortcut("طلب و بدهی", Icons.Filled.Handshake, null),
            DueShortcut("یادداشت", Icons.Filled.EditNote, null),
            DueShortcut("تراکنش", Icons.Filled.SwapHoriz, "accounting"),
            DueShortcut("قسط و وام", Icons.Filled.Payments, "loan"),
            DueShortcut("چک", Icons.Filled.ReceiptLong, "cheque"),
            DueShortcut("حساب‌کتاب", Icons.Filled.AccountBalanceWallet, "accounting"),
        )
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AppCard {
                Text("سررسید", color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(
                    "میان‌برِ سریع به یادداشت، تراکنش، قسط و چک - نسخه‌ی کاملِ این تب (طلب‌وبدهی و " +
                        "یادداشتِ مستقل) تو فازِ بعدی اضافه می‌شه.",
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
                items(shortcuts) { shortcut -> DueShortcutTile(shortcut, onNavigateToRoute) }
            }
        }
    }
}

@Composable
private fun DueShortcutTile(shortcut: DueShortcut, onClick: (String) -> Unit) {
    val comingSoon = shortcut.route == null
    AppCard(
        modifier = Modifier
            .aspectRatio(1.35f)
            .pressScaleClickable { shortcut.route?.let(onClick) },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(AppPrimary.copy(alpha = if (comingSoon) 0.08f else 0.16f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    shortcut.icon,
                    contentDescription = shortcut.title,
                    tint = if (comingSoon) AppMuted else AppPrimary,
                )
            }
            Text(
                shortcut.title,
                color = if (comingSoon) AppMuted else AppText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp),
            )
            if (comingSoon) {
                Text("به‌زودی", color = AppMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}
