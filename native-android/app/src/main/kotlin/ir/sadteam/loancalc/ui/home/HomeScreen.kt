package ir.sadteam.loancalc.ui.home

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

private data class HomeShortcut(
    val route: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
)

/**
 * تبِ «خانه» - داشبوردِ ورودیِ اصلیِ اپ (بازسازیِ فازِ اولِ تب‌بندی، رجوع کن به CLAUDE.md). این
 * نسخه‌ی *اولیه*ست - فقط میان‌برهای سریع به بقیه‌ی تب‌ها، بدونِ جمع‌بندیِ عددی/آماریِ کاملِ اپِ
 * مرجع (که نیازمندِ جمع‌کردنِ داده از چند ViewModelِ جدا با ریسکِ بیشتره - عمداً به فازِ بعدی
 * موکول شده).
 */
@Composable
fun HomeScreen(onNavigateToRoute: (String) -> Unit) {
    val shortcuts = remember {
        listOf(
            HomeShortcut("loan", "وام", "محاسبه و پیگیریِ اقساط", Icons.Filled.Payments, AppPrimary),
            HomeShortcut("cheque", "چک", "دریافتی/پرداختی و دسته‌چک", Icons.Filled.ReceiptLong, Color(0xFFB8860B)),
            HomeShortcut("accounting", "حسابداری", "دخل‌وخرج، بودجه، گزارش", Icons.Filled.AccountBalanceWallet, Color(0xFF2E7D32)),
            HomeShortcut("due", "سررسید", "یادداشت و پرداخت‌های نزدیک", Icons.Filled.EventNote, Color(0xFF1565C0)),
        )
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AppCard {
                Text("خوش اومدی!", color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(
                    "از اینجا سریع به وام، چک، حسابداری و سررسیدهات دسترسی داری.",
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
                    .size(320.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(shortcuts) { shortcut -> HomeShortcutTile(shortcut, onNavigateToRoute) }
            }
        }
    }
}

@Composable
private fun HomeShortcutTile(shortcut: HomeShortcut, onClick: (String) -> Unit) {
    AppCard(
        modifier = Modifier
            .aspectRatio(1.35f)
            .pressScaleClickable { onClick(shortcut.route) },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(shortcut.color.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(shortcut.icon, contentDescription = shortcut.title, tint = shortcut.color)
            }
            Text(
                shortcut.title,
                color = AppText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                shortcut.subtitle,
                color = AppMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
