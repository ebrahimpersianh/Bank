package ir.sadteam.loancalc.ui.history

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppDangerPill
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
import ir.sadteam.loancalc.ui.components.SwipeToDeleteRow
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

    // فیلترِ نوع - فقط نوع‌هایی که واقعاً در تاریخچه هستند چیپ می‌گیرند.
    var kindFilter by remember { mutableStateOf<String?>(null) }
    val kinds = remember(history) { history.map { it.kind }.distinct() }
    val shown = remember(filtered, kindFilter) { if (kindFilter == null) filtered else filtered.filter { it.kind == kindFilter } }
    var confirmClearAll by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<CalculationHistoryEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppSurface)
                    .border(1.dp, AppLine, RoundedCornerShape(14.dp))
                    .pressScaleClickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText)
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text("تاریخچه‌ی محاسبات", color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Black)
                if (history.isNotEmpty()) {
                    Text("${toFa(history.size)} محاسبه", color = AppMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (history.isNotEmpty()) {
                TextButton(onClick = { confirmClearAll = true }) {
                    Text("پاک‌کردنِ همه", color = AppDanger, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 4.dp)
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(AppSurface)
                .border(1.dp, AppLine, RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Search, contentDescription = null, tint = AppMuted, modifier = Modifier.size(20.dp))
            Box(modifier = Modifier.weight(1f).padding(start = 10.dp), contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    Text("نامِ بانک، مبلغ، نوعِ محاسبه…", color = AppLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(color = AppText, fontSize = 12.5.sp, fontWeight = FontWeight.Bold),
                    cursorBrush = SolidColor(AppPrimary),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (query.isNotEmpty()) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "پاک‌کردنِ جستجو",
                    tint = AppMuted,
                    modifier = Modifier.size(20.dp).clip(CircleShape).pressScaleClickable { query = "" },
                )
            }
        }

        if (kinds.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                AppChip(label = "همه", selected = kindFilter == null, onClick = { kindFilter = null })
                kinds.forEach { k -> AppChip(label = kindLabel(k), selected = kindFilter == k, onClick = { kindFilter = k }) }
            }
        }

        if (shown.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // دو حالتِ کاملاً متفاوت: «هیچ‌وقت چیزی نبوده» در برابر «هست ولی جستجو چیزی پیدا نکرد».
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
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(shown, key = { it.id }) { entry ->
                    HistoryRow(
                        entry,
                        onDelete = { pendingDelete = entry },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }

    if (confirmClearAll) {
        AlertDialog(
            onDismissRequest = { confirmClearAll = false },
            title = { Text("همه‌ی تاریخچه پاک شود؟", fontWeight = FontWeight.Black) },
            text = { Text("${toFa(history.size)} محاسبه برای همیشه پاک می‌شود و برگشت ندارد.") },
            confirmButton = {
                TextButton(onClick = { confirmClearAll = false; viewModel.clearAll() }) { Text("پاک کن", color = AppDanger) }
            },
            dismissButton = { TextButton(onClick = { confirmClearAll = false }) { Text("بی‌خیال") } },
        )
    }
    pendingDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("این محاسبه حذف شود؟", fontWeight = FontWeight.Black) },
            text = { Text(entry.title) },
            confirmButton = {
                TextButton(onClick = { pendingDelete = null; viewModel.delete(entry) }) { Text("حذف", color = AppDanger) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("بی‌خیال") } },
        )
    }
}

@Composable
private fun HistoryRow(
    entry: CalculationHistoryEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                kindLabel(entry.kind),
                color = AppPrimaryInk,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(AppPrimaryPill)
                    .padding(horizontal = 9.dp, vertical = 3.dp),
            )
            Box(modifier = Modifier.weight(1f))
            Text(dateLabel(entry.createdAt), color = AppMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
        }
        Text(entry.title, color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 8.dp))
        Text(entry.summary, color = AppMuted, fontSize = 11.5.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 3.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (entry.amount > 0) {
                Text("${fmt(entry.amount)} ریال", color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
            Box(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AppDangerPill)
                    .pressScaleClickable(onClick = onDelete),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = AppDanger, modifier = Modifier.size(18.dp))
            }
        }
    }
}
