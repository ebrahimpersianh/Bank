package ir.sadteam.loancalc.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.CalculationHistoryEntity
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.appFieldColors
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText

private fun kindLabel(kind: String): String = when (kind) {
    "LOAN" -> "وام بانکی"
    "AFFORD" -> "سقف وام"
    "DEPOSIT" -> "سود سپرده"
    else -> kind
}

private fun dateLabel(iso: String): String = runCatching {
    val date = JalaliCalendar.fromGregorian(
        iso.substring(0, 4).toInt(),
        iso.substring(5, 7).toInt(),
        iso.substring(8, 10).toInt(),
    )
    "${toFa(date.d)}/${toFa(date.m)}/${toFa(date.y)}"
}.getOrDefault("")

/**
 * تاریخچه/جستجوی سناریوهای محاسبه - هر محاسبه‌ای که تو تب‌های وام بانکی/محاسبه‌گر سقف/سود سپرده
 * انجام بشه (حتی بدون «ذخیره‌ی» رسمی) این‌جا ثبت و قابل جستجوئه.
 */
@Composable
fun CalculationHistoryScreen(onBack: () -> Unit, viewModel: CalculationHistoryViewModel = hiltViewModel()) {
    val history by viewModel.history.collectAsState()
    var query by remember { mutableStateOf("") }
    val filtered = remember(history, query) {
        if (query.isBlank()) {
            history
        } else {
            history.filter { it.title.contains(query, ignoreCase = true) || it.summary.contains(query, ignoreCase = true) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
            }
            Text("تاریخچه محاسبات", color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
            Box(modifier = Modifier.weight(1f))
            if (history.isNotEmpty()) {
                TextButton(onClick = { viewModel.clearAll() }) {
                    Text("پاک‌کردن همه", color = AppDanger)
                }
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("جستجو (نام بانک، مبلغ، ...)") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            colors = appFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
        )

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // دو حالتِ کاملاً متفاوت: «هیچ‌وقت چیزی نبوده» در برابر «هست ولی جستجو چیزی
                // پیدا نکرد» - متن و آیکونِ یکسان برای این دوتا گیج‌کننده بود.
                if (history.isEmpty()) {
                    EmptyState(
                        icon = Icons.Outlined.History,
                        title = "هنوز محاسبه‌ای ثبت نشده",
                        description = "هر محاسبه‌ای که انجام بدی خودکار اینجا ذخیره می‌شه " +
                            "تا بعداً بتونی دوباره ببینیش.",
                    )
                } else {
                    EmptyState(
                        icon = Icons.Outlined.SearchOff,
                        title = "چیزی پیدا نشد",
                        description = "با این عبارت محاسبه‌ای پیدا نکردم؛ یه کلمه‌ی دیگه رو امتحان کن.",
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(14.dp, 8.dp, 14.dp, 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filtered, key = { it.id }) { entry ->
                    // بدونِ این، فیلترشدنِ لیست با هر حرفی که تو جستجو تایپ می‌شه یه پرشِ
                    // ناگهانیه؛ با این، ردیف‌ها نرم جابه‌جا/محو می‌شن.
                    HistoryRow(
                        entry,
                        onDelete = { viewModel.delete(entry) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(
    entry: CalculationHistoryEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(kindLabel(entry.kind), color = AppPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        " · ${dateLabel(entry.createdAt)}",
                        color = AppMuted,
                        fontSize = 11.sp,
                    )
                }
                Text(entry.title, color = AppText, fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
                Text(entry.summary, color = AppMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                if (entry.amount > 0) {
                    Text("${fmt(entry.amount)} ریال", color = AppText, fontSize = 12.5.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = AppDanger)
            }
        }
    }
}
