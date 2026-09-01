package ir.sadteam.loancalc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.theme.AppSegmentPill
import ir.sadteam.loancalc.ui.theme.AppSegmentRail
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryBorder
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.hardShadow

/** دو حالتِ داخلیِ تبِ «محاسبه‌گر» - فریمِ `27f`. */
private enum class CalcMode(val label: String) {
    /** همون محتوای تبِ «بانکی»ِ قدیم: قسط و سود و کلِ بازپرداخت. پیش‌فرض. */
    INSTALLMENT("قسط و سود"),

    /** محاسبه‌گرِ توانِ بازپرداخت. */
    AFFORDABILITY("توانِ بازپرداخت"),
}

/**
 * **تبِ «محاسبه‌گر» - ادغامِ دو تبِ قبلی (فریمِ `27f`).**
 *
 * تصمیمِ کلاد دیزاین: تبِ چهارمِ «بانکی» تو «محاسبه‌گر» ادغام شد، چون هر دو یه شکل ورودی/خروجی
 * دارن و کاربر معمولاً پشتِ سرِ هم از هر دو استفاده می‌کنه («با این نرخ چند قسطم می‌شه» و «از
 * عهده‌اش برمی‌آم؟»)؛ دو تبِ جدا مجبورش می‌کرد بینِ دو صفحه رفت‌وبرگشت کنه و عددها رو از حفظ ببره.
 *
 * ⚠️ **شماره‌ی فریم `27f`ه نه `27e`** - ارجاعِ اولیه غلط بود و خودِ طراح اصلاحش کرد؛ `27e` تو همه‌ی
 * نسخه‌ها «مودال‌ها و شیت‌ها» بوده.
 *
 * ⚠️ **هیچ منطقی حذف نشد** - هر دو صفحه و ViewModelهاشون دست‌نخورده‌ان؛ این فقط یه میزبانِ
 * دوحالته‌ست که بالاشون می‌شینه.
 *
 * حالتِ آخر عمداً **ذخیره نمی‌شه** (خواسته‌ی صریحِ طرح): هر بار ورود با `INSTALLMENT` شروع می‌شه.
 */
@Composable
fun CalculatorHostScreen(onCalculated: (BankLoanOutcome) -> Unit) {
    var mode by remember { mutableStateOf(CalcMode.INSTALLMENT) }
    // قسطِ محاسبه‌شده‌ی حالتِ اول، تا کارتِ سبز بتونه با همون عدد به حالتِ دوم بپره.
    var lastInstallment by remember { mutableStateOf<Double?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // سگمنتِ فریم: ریل و قرصِ فعال توکنِ اختصاصی دارن، چون نقششون بینِ دو تم جابه‌جا می‌شه
        // (رجوع کن به segmentRail/segmentPill تو Color.kt).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(AppSegmentRail)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            CalcMode.entries.forEach { entry ->
                val selected = entry == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(999.dp))
                        .then(if (selected) Modifier.background(AppSegmentPill) else Modifier)
                        .pressScaleClickable { mode = entry }
                        // ارتفاعِ لمسیِ هر نیمه ≥۴۴dp - تاکیدِ صریحِ هندآف.
                        .heightIn(min = 44.dp)
                        .padding(vertical = 9.dp, horizontal = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        entry.label,
                        color = if (selected) AppText else AppMuted,
                        fontSize = 10.5.sp,
                        fontWeight = if (selected) FontWeight.Black else FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (mode) {
                CalcMode.INSTALLMENT -> BankLoanScreen(
                    onCalculated = { outcome ->
                        lastInstallment = outcome.result.installment
                        onCalculated(outcome)
                    },
                    // کارتِ سبزِ «از عهده‌اش برمی‌آیم؟» زیرِ نتیجه - تنها چیزی که این ادغام رو از
                    // دو تبِ جدا **بهتر** می‌کنه، نه فقط جمع‌وجورتر (تاکیدِ صریحِ طرح: حذفش نکن).
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

/**
 * پلِ حالتِ اول به دوم - قسطِ همین‌الان‌محاسبه‌شده رو با خودش می‌بره.
 *
 * مقادیر از خودِ فریم: قرصِ سبزِ روشن با حاشیه‌ی ۲ و سایه‌ی سختِ ۴، گوشه‌ی ۱۸.
 */
@Composable
private fun AffordabilityBridgeCard(installment: Double, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .hardShadow(AppPrimaryBorder, offsetY = 4.dp, cornerRadius = 18.dp)
            .clip(shape)
            .background(AppPrimaryPill)
            .border(2.dp, AppPrimaryBorder, shape)
            .pressScaleClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "از عهده‌اش برمی‌آیم؟",
                color = AppPrimaryDim,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                "با قسطِ ${fmt(installment)} ریال به حالتِ توانِ بازپرداخت برو",
                color = AppPrimaryInk,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowBackIos,
            contentDescription = null,
            tint = AppPrimaryInk,
            modifier = Modifier.size(15.dp),
        )
    }
}
