package ir.sadteam.loancalc.ui.accounting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.data.CategoryEntry
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.ui.components.AccountBadge
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * انتخابگرِ حساب‌کتاب برای شیتِ تراکنش - جایگزینِ [ir.sadteam.loancalc.ui.components.AccountPickerDialog]
 * تو این مسیر؛ اون یکی یه لیستِ متنیِ ساده‌ست، این یکی آیکون/لوگو و موجودی رو هم نشون می‌ده (طبقِ
 * اپِ مرجع). اون یکی عمداً دست‌نخورده مونده چون مسیرِ «پرداختِ قسط/چک» هنوز ازش استفاده می‌کنه.
 */
@Composable
fun AccountPickerSheet(
    accounts: List<AccountEntity>,
    onPick: (AccountEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("حساب‌کتاب‌ها", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
        text = {
            if (accounts.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Add,
                    title = "هنوز حساب‌کتابی نساختی",
                    description = "اول یه حساب‌کتاب (کارت بانکی یا نقدی) بساز تا بتونی تراکنش ثبت کنی.",
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(accounts, key = { it.id }) { account ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppSurface2, RoundedCornerShape(12.dp))
                                .pressScaleClickable(onClick = { onPick(account) })
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AccountBadge(account = account, size = 32.dp)
                            Column(modifier = Modifier.padding(start = 10.dp)) {
                                Text(account.name, color = AppText, fontSize = 13.sp)
                                Text(
                                    "${fmt(account.initialBalance)} ریال",
                                    color = AppMuted,
                                    fontSize = 11.sp,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
    )
}

/** انتخابگرِ دسته‌بندی - لیستِ کاملِ دسته‌های همون نوع (خرج یا دخل)، با آیکون و رنگِ خودشون. */
@Composable
fun CategoryPickerSheet(
    categories: List<CategoryEntry>,
    isIncome: Boolean,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isIncome) "دسته‌بندیِ دخل" else "دسته‌بندیِ خرج",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(categories, key = { it.name }) { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppSurface2, RoundedCornerShape(12.dp))
                            .pressScaleClickable(onClick = { onPick(entry.name) })
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            entry.icon,
                            contentDescription = null,
                            tint = entry.color,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            entry.name,
                            color = AppText,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(start = 10.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
    )
}
