package ir.sadteam.loancalc.ui.myloans

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterList
import ir.sadteam.loancalc.ui.components.JibakAlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * پورت داشبورد اصلی اپ رقیب (VAMMAN): سه‌تا کارت بزرگ و خوانا - وضعیت کلی بدهی‌ها (مجموع مانده‌ی
 * همه‌ی وام‌ها، از رو installment×(n−paidCount) هر وام)، مجموع اقساط ماهانه (جمع installment همه‌ی
 * وام‌ها - تقریبی، فرض دوره‌ی ماهانه)، و تحلیل درآمد (نسبت اقساط به جمع چند منبع درآمد مستقل -
 * ثابت/متغیر - با آستانه‌ی «منطقه‌ی امن» ۶۵٪ که از رشته‌های واقعی رقیب استخراج شد؛ نسخه‌ی قبلی این
 * پروژه اشتباهاً دو آستانه‌ی ۳۰٪/۵۰٪ حدسی داشت که تو هیچ‌جای رقیب پیدا نشد). برخلاف رقیب که این یه
 * صفحه‌ی جدا (home) بود، چون معماری تب‌های این اپ (رجوع کن به CLAUDE.md) ثابته، بالای همین «وام‌های
 * من» اضافه شده - جایی که داده‌ی وام‌ها از قبل در دسترسه.
 */
/**
 * حلقه‌ی ۵۲dpیِ ردیفِ وام - فریمِ `27a`. ضخامتِ ۶٫۵، سرِ گرد، از ساعتِ ۱۲ پادساعت‌گرد.
 * وسطش درصد می‌نویسه، مگر وامِ تسویه‌شده که تیک می‌گیره.
 *
 * @param showCoin سکه‌ی ۱۱dpیِ «نزدیک‌ترین قسط» رو گوشه‌ی بالا-راستِ حلقه (فقط حالتِ فوری).
 */
/**
 * **ردیفِ پشتیبان‌گیری و بازیابی - فریمِ `27a`.**
 *
 * یه ردیفِ فهرستِ ساده با قابِ آیکونِ ۳۰ی. تپ روش یه شیتِ دوگزینه‌ای باز می‌کنه، چون فریم
 * **یک** ردیف داره ولی ما دو تا کار داریم (گرفتن و برگردوندن) - جاسازیِ دو دکمه تو یه ردیف
 * هدفِ لمسی رو زیرِ ۴۴dp می‌برد که خلافِ بندِ ۸ سیستمِ طراحیه.
 */
/**
 * ردیفِ فهرستِ سبکِ `27a` - قابِ آیکونِ ۳۰ + عنوان + فلش. دو مصرف دارد (پشتیبان‌گیری و
 * آمار)، پس یک‌بار نوشته شد نه دوبار.
 */
@Composable
private fun LoanListActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppSurface)
            .border(2.dp, AppLineRow, RoundedCornerShape(16.dp))
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(AppRadius.icon))
                .background(AppSurface2),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = AppMuted, modifier = Modifier.size(16.dp))
        }
        Text(
            label,
            color = AppText,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = null,
            tint = AppMuted,
            modifier = Modifier.size(18.dp),
        )
    }
}
@Composable
internal fun BackupRestoreRow(onBackup: () -> Unit, onRestore: () -> Unit) {
    var showSheet by remember { mutableStateOf(false) }

    if (showSheet) {
        JibakAlertDialog(
            onDismissRequest = { showSheet = false },
            title = { Text("پشتیبان‌گیری و بازیابیِ وام‌ها") },
            text = { Text("یه فایلِ پشتیبان از همه‌ی وام‌هات بساز، یا یه فایلِ قبلی رو برگردون.") },
            confirmButton = {
                TextButton(onClick = {
                    showSheet = false
                    onBackup()
                }) { Text("گرفتنِ پشتیبان", color = AppPrimary) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSheet = false
                    onRestore()
                }) { Text("بازیابی از فایل", color = AppMuted) }
            },
        )
    }

    LoanListActionRow(
        icon = Icons.Filled.CloudUpload,
        label = "پشتیبان‌گیری و بازیابیِ وام‌ها",
        onClick = { showSheet = true },
    )
}
/**
 * خطِ «N وام عقب‌افتاده» بالای فهرست.
 *
 * عمداً **کارتِ کامل نیست و دکمه ندارد**: کاری برای انجام‌دادن پیشنهاد نمی‌کند، فقط عدد را
 * می‌گوید. خودِ کارتِ هر وام دکمه‌ی پرداختش را دارد.
 */
@Composable
private fun OverdueSummaryRow(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.row))
            .background(AppDangerPill)
            .padding(horizontal = 13.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = AppDangerInk,
            modifier = Modifier.size(16.dp),
        )
        Text(
            "${toFa(count)} وام عقب‌افتاده",
            color = AppDangerInk,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
/** دکمه‌ی «پرداخت»ِ ردیفِ سررسیدِ نزدیک - فریمِ `27a`. */
@Composable
internal fun LoanPayButton(onClick: () -> Unit) {
    // 🚨 قابِ بیرونی **هیچ‌وقت `size` ثابت نگیرد.** قبلاً `Modifier.size(44.dp)` بود و
    // چون خودِ کپسول پهن‌تر از ۴۴ است، متن بریده می‌شد و روی گوشیِ کاربر «پردا» دیده
    // می‌شد. ارتفاعِ هدفِ لمسی با `defaultMinSize` تامین می‌شود، نه با بریدنِ عرض.
    Box(
        modifier = Modifier.defaultMinSize(minHeight = 44.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                // کشیده و باریک (طرحِ ChatGPT): عرضِ ثابتِ کمینه، ارتفاعِ کم، بی سایه‌ی سخت.
                .defaultMinSize(minWidth = 84.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(AppPrimary)
                .pressScaleClickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 6.dp),
        ) {
            Text(
                "پرداخت",
                color = Color.White,
                fontSize = 11.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center),
                fontWeight = FontWeight.Black,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}
/**
 * **فیلترِ دوتاییِ «فعال / تسویه‌شده» - فریمِ `27a`.**
 *
 * جایگزینِ دکمه‌ی متنیِ قبلی («وام‌های تسویه‌شده (۳)» / «بازگشت به وام‌های فعال»). طبقِ تصمیمِ
 * کلاد دیزاین (۹ شهریور) این فیلتر **جای خالیِ هدر** رو پر می‌کنه - همون جایی که قبلاً قرار بود
 * دکمه‌ی + بشینه ولی حذف شد تا الگوی «افزودن همیشه با FAB» نشکنه.
 *
 * پیش‌فرض «فعال»ه. تعدادِ تسویه‌شده‌ها رو خودِ چیپ نشون می‌ده تا اگه صفر بود کاربر بیخود
 * روش نزنه.
 */
@Composable
internal fun SettledLoansToggle(
    filter: LoanFilter,
    allCount: Int,
    settledCount: Int,
    activeCount: Int,
    overdueLoanCount: Int,
    onFilter: (LoanFilter) -> Unit,
) {
    val shape = RoundedCornerShape(999.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(AppSurface2)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        FilterChipHalf(
            label = "همه (${toFa(allCount)})",
            selected = filter == LoanFilter.ALL,
        ) { onFilter(LoanFilter.ALL) }
        FilterChipHalf(
            // بندِ ۳ جوابِ دورِ ۱۲: نوارِ «۹ وام عقب‌افتاده» **رفت** و عددش این‌جا نشست -
            // صفر پیکسلِ ارتفاعِ تازه، چون این ردیف از قبل بود.
            label = buildString {
                append("فعال (${toFa(activeCount)})")
                if (overdueLoanCount > 0) append(" · ${toFa(overdueLoanCount)} عقب")
            },
            selected = filter == LoanFilter.ACTIVE,
        ) { onFilter(LoanFilter.ACTIVE) }
        FilterChipHalf(
            label = "تسویه‌شده (${toFa(settledCount)})",
            selected = filter == LoanFilter.SETTLED,
        ) { onFilter(LoanFilter.SETTLED) }
    }
}
/** یه نیمه‌ی فیلترِ دوتایی - انتخاب‌شده قرصِ سفیدِ سایه‌دار می‌گیره، بقیه فقط متنِ خاکستری. */
@Composable
private fun FilterChipHalf(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .then(if (selected) Modifier.background(AppPrimary) else Modifier)
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (selected) Color.White else AppMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}
@Composable
internal fun LoanSortMenu(selected: LoanSortOption, onSelect: (LoanSortOption) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.FilterList, contentDescription = null, tint = AppPrimary, modifier = Modifier.padding(end = 4.dp))
            Text("فیلتر", color = AppPrimary, fontSize = 13.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            LoanSortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option.label,
                            color = if (option == selected) AppPrimary else AppText,
                        )
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
