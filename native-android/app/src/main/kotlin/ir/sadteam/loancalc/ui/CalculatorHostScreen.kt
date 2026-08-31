package ir.sadteam.loancalc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppSurface2
import ir.sadteam.loancalc.ui.theme.AppText

/** دو حالتِ داخلیِ تبِ «محاسبه‌گر» - فریمِ `27e`. */
private enum class CalcMode(val label: String) {
    /** همون محتوای تبِ «بانکی»ِ قدیم: انتخابِ بانک/سرویس و نرخِ خودکار. پیش‌فرض، چون پرکاربردتره. */
    RATE_FROM_BANK("قسط از نرخِ بانک"),

    /** محاسبه‌گرِ توانِ بازپرداخت. */
    AFFORDABILITY("توانِ بازپرداخت"),
}

/**
 * **تبِ «محاسبه‌گر» - ادغامِ دو تبِ قبلی (`27e`).**
 *
 * تصمیمِ کلاد دیزاین (۹ شهریور): تبِ چهارمِ «بانکی» تو «محاسبه‌گر» ادغام شد، چون هر دو یه شکل
 * ورودی/خروجی دارن و کاربر معمولاً پشتِ سرِ هم از هر دو استفاده می‌کنه («با نرخِ این بانک چند
 * قسطم می‌شه» و «از عهده‌اش برمی‌آم؟»)؛ دو تبِ جدا مجبورش می‌کرد بینِ دو صفحه رفت‌وبرگشت کنه و
 * عددها رو از حفظ ببره. ضمناً چهار تبِ فارسی تو عرضِ گوشی به هم می‌چسبید.
 *
 * ⚠️ **هیچ منطقی حذف نشد** - هر دو صفحه و ViewModelهاشون دست‌نخورده‌ان؛ این فقط یه میزبانِ
 * دوحالته‌ست که بالاشون می‌شینه.
 *
 * حالتِ آخر عمداً **ذخیره نمی‌شه** (خواسته‌ی صریحِ طرح): هر بار ورود با حالتِ اول شروع می‌شه.
 */
@Composable
fun CalculatorHostScreen(onCalculated: (BankLoanOutcome) -> Unit) {
    var mode by remember { mutableStateOf(CalcMode.RATE_FROM_BANK) }
    // قسطِ محاسبه‌شده‌ی حالتِ اول، تا کارتِ سبز بتونه با همون عدد به حالتِ دوم بپره.
    var lastInstallment by remember { mutableStateOf<Double?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CalcMode.entries.forEach { entry ->
                val selected = entry == mode
                val shape = RoundedCornerShape(999.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(shape)
                        .background(if (selected) AppPrimaryPill else AppSurface2)
                        .then(if (selected) Modifier.border(1.5.dp, AppPrimary, shape) else Modifier)
                        .pressScaleClickable { mode = entry }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        entry.label,
                        color = if (selected) AppPrimaryInk else AppMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (mode) {
                CalcMode.RATE_FROM_BANK -> BankLoanScreen(
                    onCalculated = { outcome ->
                        lastInstallment = outcome.result.installment
                        onCalculated(outcome)
                    },
                    // کارتِ سبزِ «از عهده‌اش برمی‌آیم؟» زیرِ نتیجه - تنها چیزی که این ادغام رو از
                    // دو تبِ جدا **بهتر** می‌کنه، نه فقط جمع‌وجورتر (تاکیدِ صریحِ طرح).
                    footer = {
                        val installment = lastInstallment
                        if (installment != null && installment > 0) {
                            AffordabilityBridgeCard(
                                installment = installment,
                                onClick = { mode = CalcMode.AFFORDABILITY },
                            )
                        }
                    },
                )
                CalcMode.AFFORDABILITY -> AffordScreen(initialInstallment = lastInstallment)
            }
        }
    }
}

/** پلِ حالتِ اول به دوم - قسطِ همین‌الان‌محاسبه‌شده رو با خودش می‌بره. */
@Composable
private fun AffordabilityBridgeCard(installment: Double, onClick: () -> Unit) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .pressScaleClickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(AppPrimaryPill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Calculate,
                    contentDescription = null,
                    tint = AppPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "از عهده‌اش برمی‌آیم؟",
                    color = AppText,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    "با قسطِ ${fmt(installment)} ریال بررسی کن",
                    color = AppMuted,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
