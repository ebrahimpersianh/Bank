package ir.sadteam.loancalc.ui.cheque

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.Delete
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.jibak.rialToToman
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.core.ChequeRiskScore
import ir.sadteam.loancalc.core.ChequeStatus
import ir.sadteam.loancalc.core.JalaliCalendar
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.data.db.ChequeEntity
import ir.sadteam.loancalc.ui.account.AccountViewModel
import ir.sadteam.loancalc.ui.components.AccountPickerDialog
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.Avatar
import ir.sadteam.loancalc.ui.components.AvatarColor
import ir.sadteam.loancalc.ui.components.AvatarShape
import ir.sadteam.loancalc.ui.components.AvatarView
import ir.sadteam.loancalc.ui.components.ConfirmDeleteDialog
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.PhotoAttachmentCard
import ir.sadteam.loancalc.ui.components.ReminderOverrideCard
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.debt.DebtViewModel
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
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
    accountViewModel: AccountViewModel = hiltViewModel(),
    debtViewModel: DebtViewModel = hiltViewModel(),
) {
    val typeLabel = if (cheque.type == "RECEIVED") "دریافتی" else "پرداختی"
    val banner = rememberInAppBanner()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // طرفِ‌حسابِ لینک‌شده + امتیازِ ریسکِ برگشت - جوابِ سوالِ ۲ و ۶، فریمِ `29l`.
    val counterparties by debtViewModel.counterparties.collectAsState()
    val counterparty = counterparties.firstOrNull { it.id == cheque.counterpartyId }
    var riskScore by remember(cheque.id) { mutableStateOf<ChequeRiskScore?>(null) }
    LaunchedEffect(cheque.counterpartyId) {
        val cid = cheque.counterpartyId
        if (cid != null) viewModel.riskScoreFor(cid) { riskScore = it } else riskScore = null
    }

    // سینکِ خودکارِ پاس‌شدنِ چک ↔ حسابداری (تصمیمِ صریحِ کاربر، رجوع کن به CLAUDE.md، هم‌الگو با
    // LoanDetailScreen) - فقط موقعِ گذر *به* «پاس‌شده» (نه سایرِ وضعیت‌ها، نه وقتی از قبل پاس‌شده)
    // پرسیده می‌شه. چکِ دریافتی یعنی پول میاد تو (DEPOSIT)، چکِ پرداختی یعنی پول می‌ره (WITHDRAWAL).
    val accounts by accountViewModel.accounts.collectAsState()
    var pendingPassStatus by remember { mutableStateOf(false) }
    fun commitPass(account: AccountEntity?) {
        viewModel.setStatus(cheque, ChequeStatus.PASSED)
        if (account != null) {
            val today = JalaliCalendar.today()
            accountViewModel.addTransaction(
                accountId = account.id,
                type = if (cheque.type == "RECEIVED") TransactionType.DEPOSIT else TransactionType.WITHDRAWAL,
                amount = cheque.amount,
                description = "چک ${typeLabel} - ${cheque.ownerName}",
                year = today.y,
                month = today.m,
                day = today.d,
                category = "قسط/چک",
                sourceType = "cheque",
                sourceId = cheque.id.toString(),
            )
        } else {
            // خواسته‌ی صریحِ کاربر («باید جایی اعلام کنی») - قبلاً وقتی هیچ حسابی نبود، پرداخت
            // بی‌سروصدا تو حسابداری ثبت نمی‌شد و هیچ توضیحی هم داده نمی‌شد.
            banner.show("این چک تو حسابداری ثبت نشد - برای اینکه خودکار ثبت بشه، از تبِ «دارایی» یه حساب بساز.")
        }
    }
    if (pendingPassStatus && accounts.isNotEmpty()) {
        AccountPickerDialog(
            accounts = accounts,
            title = if (cheque.type == "RECEIVED") "به کدوم حساب واریز شد؟" else "از کدوم حساب پرداخت کردی؟",
            onSelect = { account -> commitPass(account); pendingPassStatus = false },
            onDismiss = { pendingPassStatus = false },
        )
    }

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
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = ir.sadteam.loancalc.ui.theme.AppText)
                }
                Text("جزئیات چک", color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 4.dp))
            }
        }

        item {
            AppCard {
                Column {
                    // طرحِ Claude Design (۸ مهر): نوع به‌صورتِ قرص، مبلغ درشت، ردیف‌ها با خطِ جداکننده.
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("نوع", color = AppMuted, fontSize = 13.sp)
                        val typeColor = if (cheque.type == "RECEIVED") AppPrimary else AppDanger
                        Text(
                            typeLabel, color = typeColor, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(typeColor.copy(alpha = 0.12f)).padding(horizontal = 12.dp, vertical = 3.dp),
                        )
                    }
                    DetailRow("مبلغ", ir.sadteam.loancalc.ui.privacy.maskIfPrivate(ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode.current, "${fmt(rialToToman((cheque.amount).toLong()).toDouble())} ${ir.sadteam.loancalc.ui.jibak.unitFa()}"), big = true)
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

        if (counterparty != null) {
            item {
                AppCard(label = "طرف حساب") {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvatarView(
                                avatar = Avatar(
                                    shape = runCatching { AvatarShape.valueOf(counterparty.avatarShape) }.getOrDefault(AvatarShape.BOY),
                                    color = runCatching { AvatarColor.valueOf(counterparty.avatarColor) }.getOrDefault(AvatarColor.NEUTRAL),
                                ),
                                size = 40.dp,
                            )
                            Column(modifier = Modifier.padding(start = 10.dp)) {
                                Text(counterparty.name, color = AppText, fontSize = 14.sp)
                                val phone = counterparty.phone
                                if (!phone.isNullOrBlank()) {
                                    Text(toFa(phone), color = AppMuted, fontSize = 11.sp)
                                }
                            }
                        }
                        val risk = riskScore
                        if (risk != null && risk.hasHistory) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("امتیازِ ریسکِ برگشت", color = AppMuted, fontSize = 12.sp)
                                Text(
                                    "${toFa(risk.score)} از ۱۰۰ (${toFa(risk.passedCount)} پاس، ${toFa(risk.bouncedCount)} برگشتی)",
                                    color = if (risk.score >= 70) AppPrimary else AppDanger,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }
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
                // شبکه‌ی ۲×۲ از قرص‌های هم‌عرض (طرحِ Claude Design).
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChequeStatus.entries.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pair.forEach { status ->
                                val selected = cheque.status == status.name
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(if (selected) AppPrimary else AppSurface2)
                                        .border(1.5.dp, if (selected) AppPrimary else AppLine, RoundedCornerShape(999.dp))
                                        .pressScaleClickable {
                                            if (status == ChequeStatus.PASSED && cheque.status != ChequeStatus.PASSED.name) {
                                                if (accounts.isEmpty()) commitPass(null) else pendingPassStatus = true
                                            } else {
                                                viewModel.setStatus(cheque, status)
                                            }
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        status.label,
                                        color = if (selected) Color.White else AppText,
                                        fontSize = 13.sp,
                                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                                    )
                                }
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
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
            TextButton(
                onClick = { showDeleteConfirm = true },
                colors = ButtonDefaults.textButtonColors(contentColor = AppDanger),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("حذف چک", fontWeight = FontWeight.ExtraBold)
            }
        }
    }

        InAppBannerHost(banner, modifier = Modifier.align(Alignment.BottomCenter))
    }
    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = "حذف چک",
            text = "چکِ شماره‌ی «${toFa(cheque.chequeNumber)}» حذف بشه؟ اگه پاس شده بود، تراکنشش هم از حساب برمی‌گرده. این کار قابلِ‌برگشت نیست.",
            onConfirm = onDelete,
            onDismiss = { showDeleteConfirm = false },
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String, big: Boolean = false) {
    androidx.compose.material3.HorizontalDivider(thickness = 1.5.dp, color = AppLine)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = AppMuted, fontSize = 13.sp)
        Text(value, color = AppText, fontSize = if (big) 17.sp else 14.sp, fontWeight = if (big) FontWeight.Black else FontWeight.Bold)
    }
}
