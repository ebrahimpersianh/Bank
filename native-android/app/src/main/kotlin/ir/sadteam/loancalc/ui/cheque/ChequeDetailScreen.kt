package ir.sadteam.loancalc.ui.cheque

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.ChequeStatus
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.ChequeEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.PhotoAttachmentCard
import ir.sadteam.loancalc.ui.components.ReminderOverrideCard
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * جزئیات یه چک - نمایش کامل فیلدها + مدیریت وضعیت (وضع‌نشده/پاس‌شده/برگشت‌خورده/مسترد) با AppChip،
 * آرشیو/بازگردانی، ویرایش/حذف، و یه دکمه برای استعلام صیادی (رجوع کن به `SayadInquiryScreen.kt`).
 */
@Composable
fun ChequeDetailScreen(
    cheque: ChequeEntity,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSayadInquiry: () -> Unit,
    viewModel: ChequeViewModel,
) {
    val typeLabel = if (cheque.type == "RECEIVED") "دریافتی" else "پرداختی"
    val banner = rememberInAppBanner()

    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 40.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                }
                Text("جزئیات چک", color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
            }
        }

        item {
            AppCard {
                Column {
                    DetailRow("نوع", typeLabel)
                    DetailRow("مبلغ", "${fmt(cheque.amount)} ریال")
                    DetailRow("شماره چک", toFa(cheque.chequeNumber))
                    // smart-cast مستقیم رو یه property از یه ماژول دیگه (:data) مجاز نیست، برای
                    // همین اول تو یه val محلی می‌ریزیمش.
                    val sayadId = cheque.sayadId
                    if (!sayadId.isNullOrBlank()) DetailRow("شناسه صیادی", toFa(sayadId))
                    DetailRow("بانک", cheque.bankName)
                    if (cheque.branchName.isNotBlank()) DetailRow("شعبه", cheque.branchName)
                    DetailRow(if (cheque.type == "RECEIVED") "پرداخت‌کننده" else "دریافت‌کننده", cheque.ownerName)
                    DetailRow(
                        "تاریخ سررسید",
                        "${toFa(cheque.dueDay)}/${toFa(cheque.dueMonth)}/${toFa(cheque.dueYear)}",
                    )
                    if (cheque.notes.isNotBlank()) DetailRow("بابت", cheque.notes)
                }
            }
        }

        item {
            PhotoAttachmentCard(
                photoPath = cheque.photoPath,
                onPick = { uri -> viewModel.setChequePhoto(cheque, uri) },
                onRemove = { viewModel.removeChequePhoto(cheque) },
            )
        }

        item {
            AppCard(label = "وضعیت چک") {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    ChequeStatus.entries.forEach { status ->
                        AppChip(
                            label = status.label,
                            selected = cheque.status == status.name,
                            onClick = { viewModel.setStatus(cheque, status) },
                        )
                    }
                }
            }
        }

        item {
            ReminderOverrideCard(
                currentOffsets = cheque.reminderDayOffsets,
                onChange = { viewModel.setChequeReminderOffsets(cheque, it) },
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Text("ویرایش")
                }
                OutlinedButton(
                    onClick = { viewModel.setArchived(cheque, !cheque.archived) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (cheque.archived) "بازگردانی از بایگانی" else "بایگانی")
                }
            }
        }

        item {
            GradientButton(
                onClick = onSayadInquiry,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("استعلام چک صیادی")
            }
        }

        item {
            OutlinedButton(
                onClick = onDelete,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppDanger),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("حذف چک")
            }
        }
    }

        InAppBannerHost(banner, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = AppMuted, fontSize = 12.sp)
        Text(value, color = AppText, fontSize = 13.sp)
    }
}
