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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import ir.sadteam.loancalc.ui.text.Text
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
    val overdue = due && todayD > bill.dueDay && !bill.createdAfterDueIn(todayY, todayM)
    var menu by remember { mutableStateOf(false) }
    AppCard(modifier = Modifier.pressScaleClickable(scale = 0.99f) { onEdit() }) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(42.dp).clip(RoundedCornerShape(AppRadius.icon)).background(tint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) { Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp)) }
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    val kindLabel = billKindLabel(bill.kind)
                    // نامِ پیش‌فرض همان نوع است؛ «قبضِ برق · برق» تکراری می‌شد.
                    val title = if (bill.name.isBlank() || bill.name.trim() == kindLabel) "قبضِ $kindLabel" else "قبضِ $kindLabel · ${bill.name}"
                    Text(title, color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
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
                    overdue -> Triple("موعد گذشته", AppDanger, AppDangerPill)
                    // ۱۳ آبان: «نزدیک» هشدار است نه خطا - قرمز فقط برای موعدِ گذشته.
                    due -> Triple("موعدِ نزدیک", ir.sadteam.loancalc.ui.theme.AppWarningInk, ir.sadteam.loancalc.ui.theme.AppWarningInk.copy(alpha = 0.12f))
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
            // ۱۱ آبان (شکایتِ کاربر: کادرهای خالیِ «—»؛ تستِ شبیه‌ساز: موعد بعد از پرداخت روی همان ماه می‌ماند):
            // خانه‌ی بی‌مقدار نشان داده نمی‌شود، و «موعد» همیشه **موعدِ بعدی** است.
            val nextDueMonth = if (paid || bill.createdAfterDueIn(todayY, todayM)) {
                ((todayM - 1 + bill.periodMonths.coerceAtLeast(1)) % 12) + 1
            } else {
                todayM
            }
            val paidMonth = bill.lastPaidKey?.split('-')?.getOrNull(1)?.toIntOrNull()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                if (bill.lastAmount > 0) BillInfoCell(
                    "مبلغِ قبض",
                    maskIfPrivate(privacy, "${fmt(bill.lastAmount / ir.sadteam.loancalc.ui.jibak.unitDiv)} ${ir.sadteam.loancalc.ui.jibak.unitFa()}"),
                    Modifier.weight(1.3f),
                )
                BillInfoCell("موعدِ بعدی", "${toFa(bill.dueDay)} ${ir.sadteam.loancalc.ui.jibak.faMonthName(nextDueMonth)}", Modifier.weight(1f))
                if (paidMonth != null) BillInfoCell("آخرین پرداخت", ir.sadteam.loancalc.ui.jibak.faMonthName(paidMonth), Modifier.weight(1f))
            }
            if (due && !paid) {
                // ۲۰ مهر: دکمه‌ی کم‌رنگِ تیک‌دار، نه دکمه‌ی پررنگِ هم‌شکلِ «افزودن».
                // ۱۱ آبان: دکمه‌ی «پرداخت شد» همیشه سبز (قرمز = خطر و گیج‌کننده بود)؛ برچسبِ «گذشته» قرمز می‌ماند.
                val payInk = AppTxIn
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(payInk.copy(alpha = 0.12f))
                        .pressScaleClickable { onPay() },
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = payInk, modifier = Modifier.size(18.dp))
                    Text("پرداخت شد", color = payInk, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
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
