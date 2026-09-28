package ir.sadteam.loancalc.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.history.CalculationHistoryScreen
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppSurface
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
 * حالتِ آخر عمداً **ذخیره نمی‌شه** (خواسته‌ی صریحِ طرح): هر بار ورود، فهرستِ انتخابِ فریمِ `76b`
 * دیده می‌شه، نه آخرین حالت.
 */
@Composable
fun CalculatorHostScreen(
    onCalculated: (BankLoanOutcome) -> Unit,
    /** رجوع کن به `BankLoanScreen.onAddManualLoan`. */
    onAddManualLoan: () -> Unit = {},
) {
    // 🚨 **فریمِ ۷۶b - سگمنتِ دوحالته به دو کارتِ توضیح‌دار تبدیل شد.**
    //
    // آن دو حالت **هم‌وزن و هم‌جنس نبودند**: هرکدام یک سوالِ متفاوت می‌پرسد («قسطم چقدر
    // می‌شود» در برابرِ «چقدر وام از عهده‌ام برمی‌آید») و تاگلِ دوکلمه‌ای این را پنهان
    // می‌کرد. کارتِ توضیح‌دار خودش می‌گوید کدام را لازم داری. ضمناً ردیفِ دومِ تاگل از
    // بالای صفحه می‌رود، که مسئله‌ی اصلیِ کاربر بود (هدرِ ۲۴۸dp).
    //
    // `null` یعنی هنوز انتخاب نشده و فهرستِ انتخاب دیده می‌شود.
    var mode by remember { mutableStateOf<CalcMode?>(null) }
    var showHistory by remember { mutableStateOf(false) }

    // 🚨 فریمِ `70a`: این قبلاً از `onCalculated` پر می‌شد، یعنی **فقط با تپِ دکمه**.
    //
    // تا بخشِ ۶۸ درست بود: دکمه تنها راهِ دیدنِ قسط بود، پس هر کسی که نتیجه می‌خواست
    // می‌زدش. ولی بخشِ ۶۹ هیروِ زنده آورد و متنِ دکمه «جدولِ اقساط را ببین» شد - کاربری
    // که فقط قسط را می‌خواست دیگر دکمه را نمی‌زند، و کارتِ پل **هیچ‌وقت ظاهر نمی‌شد**.
    var liveInstallment by remember { mutableStateOf<Double?>(null) }

    // برگشت از یک حالت به فهرستِ انتخاب، قبل از اینکه دکمه‌ی برگشت تب را عوض کند.
    BackHandler(enabled = mode != null || showHistory) {
        showHistory = false
        mode = null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            showHistory -> CalculationHistoryScreen(onBack = { showHistory = false })
            mode == CalcMode.INSTALLMENT -> BankLoanScreen(
                onCalculated = onCalculated,
                onAddManualLoan = onAddManualLoan,
                // هیروِ زنده هر بار که عددش عوض می‌شود این را صدا می‌زند؛ `null` یعنی
                // ورودی ناقص یا ترکیبِ نامعتبر است و هیرو ساخته نشده.
                onLiveInstallment = { liveInstallment = it },
                // کارتِ سبزِ «از عهده‌اش برمی‌آیم؟» زیرِ نتیجه - تنها چیزی که این ادغام رو از
                // دو تبِ جدا **بهتر** می‌کنه، نه فقط جمع‌وجورتر (تاکیدِ صریحِ طرح: حذفش نکن).
                footer = {
                    val installment = liveInstallment
                    if (installment != null && installment > 0) {
                        AffordabilityBridgeCard(
                            installment = installment,
                            onClick = { mode = CalcMode.AFFORDABILITY },
                        )
                    }
                },
            )
            mode == CalcMode.AFFORDABILITY -> AffordScreen(initialInstallment = liveInstallment)
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CalcChoiceCard(
                    icon = Icons.Filled.Calculate,
                    title = CalcMode.INSTALLMENT.label,
                    hint = "مبلغ و نرخ را می‌دانی، قسط را می‌خواهی",
                    tint = AppPrimaryInk,
                    tintBg = AppPrimaryPill,
                    onClick = { mode = CalcMode.INSTALLMENT },
                )
                CalcChoiceCard(
                    icon = Icons.Filled.TrendingUp,
                    title = CalcMode.AFFORDABILITY.label,
                    hint = "قسطی که می‌توانی بدهی را می‌دانی",
                    tint = AppPrimaryInk,
                    tintBg = AppPrimaryPill,
                    onClick = { mode = CalcMode.AFFORDABILITY },
                )
                // بندِ ۶ فریمِ ۷۶c: تاریخچه **همین‌جا** می‌مانَد نه در «ابزارها» - جایش
                // همان‌جاست که محاسبه ساخته می‌شود.
                CalcChoiceCard(
                    icon = Icons.Filled.History,
                    title = "تاریخچه‌ی محاسبات",
                    hint = "محاسبه‌های قبلیِ وام، سقفِ وام و سودِ سپرده",
                    tint = AppMuted,
                    tintBg = AppChipBg,
                    onClick = { showHistory = true },
                )
            }
        }
    }
}

/**
 * کارتِ انتخابِ فریمِ `76b` - قابِ آیکونِ ۳۴ + عنوان + یک خطِ توضیح + فلش.
 *
 * ⚠️ توضیح **اختیاری نیست**: کلِ استدلالِ این فریم همین است که دو حالت هم‌جنس نیستند و
 * عنوانِ دوکلمه‌ای این را نمی‌گوید.
 */
@Composable
private fun CalcChoiceCard(
    icon: ImageVector,
    title: String,
    hint: String,
    tint: Color,
    tintBg: Color,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppSurface)
            .border(1.5.dp, AppLineRow, shape)
            .pressScaleClickable(onClick = onClick)
            .heightIn(min = 44.dp)
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tintBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(17.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = AppText, fontSize = 12.sp, fontWeight = FontWeight.Black)
            Text(
                hint,
                color = AppMuted,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = AppMuted,
            modifier = Modifier.size(13.dp),
        )
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
                "با قسطِ ${fmt(rialToToman(installment.toLong()).toDouble()).faDigits()} تومان به حالتِ توانِ بازپرداخت برو",
                color = AppPrimaryInk,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        // `ArrowForwardIos`ِ خودچرخان: تو RTL چپ رو نشون می‌ده، یعنی «برو جلو». نسخه‌ی
        // `ArrowBackIos` تو RTL راست‌گرد می‌شد و «برگرد» معنی می‌داد.
        Icon(
            Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = AppPrimaryInk,
            modifier = Modifier.size(15.dp),
        )
    }
}
