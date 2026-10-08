package ir.sadteam.loancalc.ui.myloans

import androidx.compose.foundation.layout.heightIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PriorityHigh
import ir.sadteam.loancalc.ui.components.AutoShrinkText
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.ui.components.persianMonthName
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppAccent
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppInfo
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppStroke
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.pillOverSurface
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Schedule
import ir.sadteam.loancalc.ui.theme.AppDueNextBorder
import ir.sadteam.loancalc.ui.theme.AppDueNextPill
import ir.sadteam.loancalc.ui.theme.AppDueOverdueBorder
import ir.sadteam.loancalc.ui.theme.AppDueOverduePill
import ir.sadteam.loancalc.ui.theme.AppInfoPill
import ir.sadteam.loancalc.ui.theme.AppPrimaryPillBorder
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.graphicsLayer

private val installmentRowHeight = 64.dp
@Composable
internal fun InstallmentRow(
    row: Map<String, Any?>,
    loan: LoanEntity,
    privacyMode: Boolean,
    onTogglePaid: (m: Int, paid: Boolean) -> Unit,
    onUnmark: (m: Int) -> Unit,
    onOpenPhoto: (m: Int) -> Unit,
    onEditAmount: (m: Int, installment: Double) -> Unit,
    modifier: Modifier = Modifier,
    dueInDays: Int? = null,
    isNext: Boolean = false,
    bulkPayMode: Boolean = false,
    selected: Boolean = false,
) {
    val m = (row["m"] as? Number)?.toInt() ?: 0
    val installment = (row["installment"] as? Number)?.toDouble() ?: loan.installment
    val paid = row["paid"] == true
    val paidLate = paid && row["paidLate"] == true
    val hasPhoto = (row["photoPath"] as? String) != null
    val due = row["dueDate"] as? Map<*, *>
    val dueLabel = due?.let {
        "${toFa(it["y"].toString())}/${toFa(it["m"].toString())}/${toFa(it["d"].toString())}"
    } ?: ""
    val overdue = !paid && dueInDays != null && dueInDays < 0
    // تاریخ با نامِ ماه («۲ مرداد ۱۴۰۵») - طرحِ ChatGPT؛ شکلِ عددیِ قبلی برای منو می‌ماند.
    val dueLong = due?.let {
        val y = (it["y"] as? Number)?.toInt()
        val mo = (it["m"] as? Number)?.toInt()
        val d = (it["d"] as? Number)?.toInt()
        if (y != null && mo != null && d != null) "${toFa(d)} ${persianMonthName(mo)} ${toFa(y)}" else dueLabel
    } ?: ""

    // چهار حالتِ ردیف (طرحِ ChatGPT): پرداخت‌شده · معوق · بعدی · آتی. همه از توکن‌های
    // `AppDue*` که تمِ شب را هم می‌چرخانند - هگزِ هاردکد این‌جا ممنوع.
    val upcoming = !paid && !overdue && isNext
    val stateLabel = when {
        paidLate -> "با تأخیر"
        paid -> "پرداخت شده"
        overdue -> "معوق"
        else -> "آتی"
    }
    val stateInk = when {
        paidLate -> AppDangerInk
        paid -> AppPrimaryInk
        overdue -> AppDangerInk
        upcoming -> AppInfo
        else -> AppMuted
    }
    val rowBg = when {
        selected -> AppPrimary.pillOverSurface(0.10f)
        paid -> AppPrimaryPill.copy(alpha = 0.45f)
        overdue -> AppDueOverduePill
        upcoming -> AppDueNextPill
        else -> AppSurface
    }
    val borderColor = when {
        selected -> AppPrimary
        paid -> AppPrimaryPillBorder
        overdue -> AppDueOverdueBorder
        upcoming -> AppDueNextBorder
        else -> AppLineRow
    }
    val stateIcon = when {
        paidLate -> Icons.Filled.Check
        paid -> Icons.Filled.Check
        overdue -> Icons.Filled.PriorityHigh
        else -> Icons.Filled.Schedule
    }
    // دایره‌ی وضعیت: پرداخت‌شده سبز، معوق قرمزِ پُر، بقیه قابِ روشن.
    val iconBg = when {
        paid -> AppPrimaryPill
        overdue -> AppDanger
        upcoming -> AppInfoPill
        else -> AppChipBg
    }
    val iconTint = when {
        overdue -> Color.White
        paid -> AppPrimaryInk
        upcoming -> AppInfo
        else -> AppMuted
    }
    // قرصِ زیرِ تاریخ فقط برای معوق و «بعدی» - ردیف‌های آتیِ دور چیزی برای گفتن ندارند.
    val chip = when {
        overdue -> "${toFa(-(dueInDays ?: 0))} روز گذشته"
        upcoming && dueInDays == 0 -> "امروز"
        upcoming && dueInDays != null -> "${toFa(dueInDays)} روز مانده"
        else -> null
    }
    val rowShape = RoundedCornerShape(16.dp)
    val bulkSelectable = bulkPayMode && !paid
    var menuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = installmentRowHeight)
            .alpha(if (bulkPayMode && paid) 0.5f else 1f)
            .clip(rowShape)
            .background(rowBg, rowShape)
            .border(if (selected) AppStroke.card else AppStroke.row, borderColor, rowShape)
            // تپِ ردیف = پرداخت (بی‌تغییر). منوی کارهای ردیف پشتِ شِورونِ سمتِ راست است.
            .pressScaleClickable(scale = 0.975f) { onTogglePaid(m, paid) }
            .padding(start = 2.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (bulkPayMode) {
            Checkbox(checked = selected, onCheckedChange = null, enabled = bulkSelectable)
        } else {
            Box {
                // فلش در یک دایره‌ی پُر تا «قابلِ لمس» خوانده شود؛ با باز شدنِ منو به پایین می‌چرخد
                // و پس‌زمینه‌اش پررنگ‌تر می‌شود (خواسته‌ی کاربر).
                val arrowTurn by animateFloatAsState(if (menuOpen) -90f else 0f, spring(stiffness = Spring.StiffnessMediumLow), label = "rowArrow")
                val arrowScale by animateFloatAsState(if (menuOpen) 1.12f else 1f, spring(dampingRatio = 0.5f), label = "rowArrowScale")
                val arrowTint = if (overdue) AppDangerInk else AppPrimaryInk
                IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(44.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .scale(arrowScale)
                            .clip(CircleShape)
                            .background(arrowTint.copy(alpha = if (menuOpen) 0.28f else 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.ChevronLeft,
                            contentDescription = "کارهای این قسط",
                            tint = arrowTint,
                            modifier = Modifier.size(20.dp).graphicsLayer { rotationZ = arrowTurn },
                        )
                    }
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    Text(
                        "قسط ${toFa(m)} · $dueLabel",
                        color = AppMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                    if (paid) {
                        DropdownMenuItem(
                            text = { Text("برداشتنِ پرداخت", color = AppDangerInk, fontSize = 13.sp) },
                            onClick = { menuOpen = false; onUnmark(m) },
                        )
                    }
                    // صفحه‌ی جزئیات (یادداشت/رسید) برای قسطِ پرداخت‌نشده هم باز می‌شود - یادداشت
                    // پیش از پرداخت هم معنا دارد و کاربر راهی برای رسیدن به آن پیدا نمی‌کرد.
                    DropdownMenuItem(
                        text = { Text(if (paid) "جزئیات، یادداشت و رسید" else "جزئیات و یادداشت", fontSize = 13.sp) },
                        onClick = { menuOpen = false; onOpenPhoto(m) },
                    )
                    DropdownMenuItem(
                        text = { Text("ویرایشِ مبلغِ این قسط", fontSize = 13.sp) },
                        onClick = { menuOpen = false; onEditAmount(m, installment) },
                    )
                }
            }
        }
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(999.dp)).background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(stateIcon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("قسط ${toFa(m)}", color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Black, maxLines = 1)
                if (hasPhoto) {
                    Icon(
                        Icons.Filled.AttachFile,
                        contentDescription = "رسید دارد",
                        tint = AppMuted,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
            Text(stateLabel, color = stateInk, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
        Column(
            modifier = Modifier.weight(1.2f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                dueLong,
                color = AppMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
            if (chip != null) {
                Text(
                    chip,
                    color = if (overdue) AppDangerInk else AppInfo,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (overdue) AppDangerPill else AppInfoPill)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 6.dp)) {
            PrivacyCrossfade(privacyMode) { masked ->
                AutoShrinkText(
                    maskIfPrivate(masked, amountToman(installment)),
                    color = if (overdue) AppDangerInk else AppText,
                    maxFontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                )
            }
            Text("${ir.sadteam.loancalc.ui.jibak.unitFa()}", color = AppMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// `LoanDonut` حذف شد - از وقتی `LoanSummaryCard` (حلقه‌ی ۸۸ی فریمِ `27b`) جاش رو
// گرفت، هیچ‌جا صدا زده نمی‌شد؛ توکنِ `AppAccent` هم فقط همین‌جا استفاده می‌شد.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailDateDropdown(
    options: List<Pair<Int, String>>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selected }?.second ?: ""
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            modifier = Modifier.menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, colors = ir.sadteam.loancalc.ui.components.appFieldColors(), shape = ir.sadteam.loancalc.ui.components.AppFieldShape,)
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, label) ->
                DropdownMenuItem(text = { Text(label) }, onClick = { onSelect(value); expanded = false })
            }
        }
    }
}
/** دکمه‌ی نوعِ وام با آیکون - فریمِ `36b`. */
@Composable
internal fun LoanTypeOption(category: LoanCategory, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .heightIn(min = 60.dp)
            .clip(shape)
            .background(if (selected) AppPrimaryPill else AppSurface2)
            .border(1.dp, if (selected) AppPrimary else AppLine, shape)
            .pressScaleClickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
    ) {
        Icon(category.glyph, contentDescription = null, tint = if (selected) AppPrimary else AppMuted, modifier = Modifier.size(20.dp))
        Text(
            category.label,
            color = if (selected) AppPrimaryInk else AppText,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
