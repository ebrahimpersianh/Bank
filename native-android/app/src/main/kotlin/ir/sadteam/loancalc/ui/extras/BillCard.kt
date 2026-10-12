package ir.sadteam.loancalc.ui.extras

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.BillEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.Ltr
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppTxIn

/** «۰۹۱۲•••۴۵۶۷» / «••••۱۲۳۴» - شماره و شناسه کامل روی صفحه نمی‌آیند. */
internal fun maskBillNumber(raw: String): String {
    val d = raw.filter { it.isDigit() }
    if (d.length <= 4) return toFa(d)
    return if (d.length == 11 && d.startsWith("09")) toFa(d.take(4)) + "•••" + toFa(d.takeLast(4))
    else "••••" + toFa(d.takeLast(4))
}

/**
 * کارتِ قبض - بازطراحیِ ۱۶ مهر (طرحِ تصویریِ کاربر، بی تصویرِ سرِ صفحه): نام و شماره/شناسه‌ی پوشیده،
 * برچسبِ وضعیت، منوی ⋮، و سه خانه‌ی «مبلغِ قبض / موعد / پرداخت».
 */
@Composable
internal fun BillCard(
    bill: BillEntity,
    icon: ImageVector,
    tint: Color,
    todayY: Int,
    todayM: Int,
    todayD: Int,
    onEdit: () -> Unit,
    onPay: () -> Unit,
    onUnpay: () -> Unit,
    onDelete: () -> Unit,
) {
    val privacy = LocalPrivacyMode.current
    val paid = bill.lastPaidKey == "$todayY-$todayM"
    val due = bill.isDueSoon(todayY, todayM, todayD)
    val overdue = due && todayD > bill.dueDay
    var menu by remember { mutableStateOf(false) }
    AppCard(modifier = Modifier.pressScaleClickable(scale = 0.99f) { onEdit() }) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(42.dp).clip(RoundedCornerShape(AppRadius.icon)).background(tint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) { Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp)) }
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("قبضِ ${billKindLabel(bill.kind)} · ${bill.name}", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    val number = bill.phone?.takeIf { it.isNotBlank() } ?: bill.billId?.takeIf { it.isNotBlank() }
                    if (number != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (bill.phone.isNullOrBlank()) "شناسه: " else "موبایل: ", color = AppMuted, fontSize = 11.sp)
                            Ltr { Text(maskBillNumber(number), color = AppMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        }
                    } else {
                        Text(if (bill.periodMonths > 1) "هر ${toFa(bill.periodMonths)} ماه" else "هر ماه", color = AppMuted, fontSize = 11.sp)
                    }
                }
                val (label, ink, bg) = when {
                    paid -> Triple("پرداخت شد", AppTxIn, AppTxIn.copy(alpha = 0.13f))
                    overdue -> Triple("گذشته", AppDanger, AppDangerPill)
                    due -> Triple("موعدِ نزدیک", AppDanger, AppDangerPill)
                    else -> Triple("در انتظار", AppMuted, AppSurface2)
                }
                Text(
                    label, color = ink, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clip(RoundedCornerShape(99.dp)).background(bg).padding(horizontal = 9.dp, vertical = 4.dp),
                )
                Box {
                    IconButton(onClick = { menu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "گزینه‌ها", tint = AppMuted)
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("ویرایش") }, onClick = { menu = false; onEdit() })
                        if (paid) {
                            DropdownMenuItem(text = { Text("برگرداندنِ پرداخت") }, onClick = { menu = false; onUnpay() })
                        } else {
                            DropdownMenuItem(text = { Text("پرداخت شد") }, onClick = { menu = false; onPay() })
                        }
                        DropdownMenuItem(text = { Text("حذف", color = AppDanger) }, onClick = { menu = false; onDelete() })
                    }
                }
            }
            Box(Modifier.fillMaxWidth().padding(vertical = 10.dp).height(1.dp).background(AppLine))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                BillInfoCell(
                    "مبلغِ قبض",
                    if (bill.lastAmount > 0) maskIfPrivate(privacy, "${fmt(bill.lastAmount / ir.sadteam.loancalc.ui.jibak.unitDiv)} ${ir.sadteam.loancalc.ui.jibak.unitFa()}") else "—",
                    Modifier.weight(1.3f),
                )
                BillInfoCell("موعد", "${toFa(bill.dueDay)} ${ir.sadteam.loancalc.ui.jibak.faMonthName(todayM)}", Modifier.weight(1f))
                BillInfoCell(
                    "پرداخت",
                    bill.lastPaidKey?.split('-')?.getOrNull(1)?.toIntOrNull()?.let { ir.sadteam.loancalc.ui.jibak.faMonthName(it) } ?: "—",
                    Modifier.weight(1f),
                )
            }
            if (due && !paid) {
                GradientButton(onClick = onPay, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) { Text("پرداخت شد") }
            }
        }
    }
}

@Composable
private fun BillInfoCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(AppSurface2).padding(horizontal = 9.dp, vertical = 7.dp)) {
        Text(label, color = AppMuted, fontSize = 10.sp)
        Text(value, color = AppText, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}
