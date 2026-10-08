package ir.sadteam.loancalc

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.ui.AffordScreen
import ir.sadteam.loancalc.ui.BankLoanOutcome
import ir.sadteam.loancalc.ui.BankLoanScreen
import ir.sadteam.loancalc.ui.CalculatorHostScreen
import ir.sadteam.loancalc.ui.DepositScreen
import ir.sadteam.loancalc.ui.ResultScreen
import ir.sadteam.loancalc.ui.myloans.MyLoansScreen
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyModeViewModel
import ir.sadteam.loancalc.ui.theme.AppLineRow
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryBorder
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.hardShadow

/**
 * میزبانِ تبِ «محاسبه‌گر» - همون [BankLoanTab]ِ قبلی، ولی حالا [CalculatorHostScreen] رو
 * به‌جای [BankLoanScreen] تو حالتِ فرم می‌ذاره تا سگمنتِ دوحالته‌ی `27f` بالاش بشینه.
 * منطقِ فرم↔نتیجه و حفظِ حالتِ فرم عیناً همون قبلیه.
 */
@Composable
private fun CalculatorHostTab(
    onAddManualLoan: () -> Unit = {},
    startInInstallment: Boolean = false,
    onLeaveInstallment: () -> Unit = {},
) {
    BankLoanTab(
        useCalculatorHost = true,
        onAddManualLoan = onAddManualLoan,
        startInInstallment = startInInstallment,
        onLeaveInstallment = onLeaveInstallment,
    )
}
@Composable
private fun BankLoanTab(
    useCalculatorHost: Boolean = false,
    onAddManualLoan: () -> Unit = {},
    startInInstallment: Boolean = false,
    onLeaveInstallment: () -> Unit = {},
) {
    var loanOutcome by remember { mutableStateOf<BankLoanOutcome?>(null) }
    // نگه‌دارنده‌ی حالتِ ذخیره‌پذیر (SaveableStateHolder): وقتی loanOutcome پر می‌شه، BankLoanScreen
    // کاملاً از کامپوزیشن بیرون می‌ره (جایگزینِ ResultScreen می‌شه) - remember/rememberSaveableِ
    // معمولیِ توش با این کار پاک می‌شد (خواسته‌ی کاربر: «اگه اشتباه زده باشم باید از نو بزنم»).
    // با پیچوندنِ BankLoanScreen تو SaveableStateProvider با یه کلیدِ ثابت، حالتِ rememberSaveableِ
    // فیلدهاش (مبلغ/نرخ/ماه/تاریخ/بانکِ‌انتخابی) حتی بعدِ بیرون‌رفتن از کامپوزیشن حفظ می‌شه و با
    // برگشتن (دکمه‌ی «ویرایش» تو ResultScreen) دوباره برمی‌گرده - نه از صفر.
    val formStateHolder = rememberSaveableStateHolder()
    // اسلاید جهت‌دار فرم→نتیجه (هم‌خانواده‌ی اسلاید تب‌های پایین): نتیجه از چپ میاد تو و فرم به
    // راست می‌ره؛ برگشت به فرم برعکس - به‌جای fade+scale قبلی.
    AnimatedContent(
        targetState = loanOutcome,
        transitionSpec = {
            val dir = if (targetState != null) -1 else 1
            (
                slideInHorizontally(
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
                ) { dir * it / 3 } + fadeIn(tween(220))
                ).togetherWith(
                    slideOutHorizontally(animationSpec = tween(180)) { -dir * it / 4 } + fadeOut(tween(150)),
                )
        },
        label = "bankLoanTab",
    ) { outcome ->
        if (outcome == null) {
            formStateHolder.SaveableStateProvider("bankLoanForm") {
                if (useCalculatorHost) {
                    CalculatorHostScreen(
                        onCalculated = { loanOutcome = it },
                        onAddManualLoan = onAddManualLoan,
                        startInInstallment = startInInstallment,
                        onLeaveInstallment = onLeaveInstallment,
                    )
                } else {
                    BankLoanScreen(onCalculated = { loanOutcome = it }, onAddManualLoan = onAddManualLoan)
                }
            }
        } else {
            ResultScreen(
                outcome = outcome,
                // قرصِ منبعِ `68`/`69b`: حالا خودِ `BankLoanScreen` می‌گوید نرخ از سرور آمده
                // («سرویسِ اعتباری») یا کاربر دستی زده («وامِ بانکی»).
                sourceLabel = outcome.rateSourceLabel,
                // ویرایش دیگه اینجا (BankLoanTab) مدیریت نمی‌شه - مورد ۴، ResultScreen خودش با
                // یه پنلِ اینلاین ویرایش می‌کنه، دیگه نیازی به onEdit/برگشتن به فرم نیست.
                // بعدِ ذخیره‌ی موفقِ وام، حالتِ ذخیره‌شده‌ی فرم (مبلغ/بانک/...) صریحاً پاک می‌شه -
                // وگرنه فرم برای وامِ *بعدی* هنوز اعدادِ وامِ قبلاً ذخیره‌شده رو نشون می‌داد (باگِ
                // گزارش‌شده‌ی کاربر: «ذخیره که می‌کنم بازم اعداد و بانک هستن»).
                onNewCalculation = {
                    formStateHolder.removeState("bankLoanForm")
                    loanOutcome = null
                },
            )
        }
    }
}
/**
 * تبِ ادغام‌شده‌ی «وام» - جایگزینِ ۴ تبِ جداگانه‌ی قبلی (وام بانکی/محاسبه‌گر/سود سپرده/وام‌های من).
 * یه انتخابگرِ افقیِ ساده بالای صفحه بینِ چهار زیرصفحه‌ی موجود سوییچ می‌کنه - خودِ صفحه‌ها
 * ([BankLoanTab]/[AffordScreen]/[DepositScreen]/[MyLoansScreen]) دست‌نخورده می‌مونن.
 * [requestedSubTab] برای ناوبریِ خارجی (تور/نوتیفیکیشنِ دیپ‌لینک) استفاده می‌شه - وقتی مقدارش عوض
 * می‌شه، زیرصفحه‌ی متناظر باز می‌شه.
 */
@Composable
internal fun LoanTab(
    onBack: () -> Unit,
    requestedSubTab: LoanSubTab?,
    onManualAddFabPositioned: (Rect) -> Unit,
    onBottomBarVisibilityChanged: (Boolean) -> Unit,
    deepLinkLoanId: Long?,
    onDeepLinkConsumed: () -> Unit,
    onOpenSettings: () -> Unit = {},
) {
    var subTab by remember { mutableStateOf(LoanSubTab.MY_LOANS) }
    // خواسته‌ی کاربر (۲۶ شهریور): دکمه‌ی «+»ِ «وام‌های من» دیگر مستقیم فرمِ دستی را باز نمی‌کند،
    // **محاسبه‌گر** را باز می‌کند؛ و «افزودنِ وامِ دستی» به تهِ همان محاسبه‌گر رفت. این پرچم
    // همان مسیرِ برگشت است: محاسبه‌گر می‌گوید «فرمِ دستی را باز کن» و تبِ «وام‌های من» بازش می‌کند.
    var openManualAdd by remember { mutableStateOf(false) }
    // «افزودنِ وام» در «وام‌های من» → فرمِ «قسط و سود»ِ تبِ محاسبه‌گر (۱۰ مهر).
    var calcAddLoan by remember { mutableStateOf(false) }
    // **فریمِ ۷۶**: جست‌وجو و «تحلیل درآمد» از داخلِ فهرست به دو آیکونِ هم‌ردیفِ عنوان آمدند،
    // پس حالتشان این‌جاست و به [MyLoansScreen] پاس داده می‌شود. فقط در زیرتبِ «وام‌های من»
    // معنی دارند.
    var searchOpen by remember { mutableStateOf(false) }
    LaunchedEffect(requestedSubTab) {
        requestedSubTab?.let { subTab = it }
    }
    // نوارِ پایینِ تب‌ها (که فقط MyLoansScreen موقعِ اسکرول جمعش می‌کنه) باید موقعِ سوییچ به هر
    // زیرصفحه‌ی دیگه‌ای دوباره نمایان بشه - چون از دیدِ ناوبریِ بیرونی، این سوییچ اصلاً route عوض
    // نمی‌کنه که خودش این ریست رو انجام بده.
    LaunchedEffect(subTab) {
        if (subTab != LoanSubTab.MY_LOANS) onBottomBarVisibilityChanged(true)
    }
    // برگشتن از یه زیرصفحه‌ی غیرِ«بانکی» به «بانکی» (زیرصفحه‌ی پیش‌فرض) - قبل از رسیدن به
    // BackHandlerِ بیرونیِ LoanCalcApp (که دیگه معنیش برگشتن به «خانه»ست). زیرصفحه‌های داخلیِ
    // خودِ هر اسکرین (مثلاً جزئیاتِ وام تو MyLoansScreen) اولویتِ بالاتری دارن چون دیرتر رجیستر می‌شن.
    BackHandler(enabled = subTab != LoanSubTab.MY_LOANS) { subTab = LoanSubTab.MY_LOANS }

    // سربرگِ واحد (۸ مهر): جزئیاتِ وام سربرگِ خودش را دارد؛ دو فلشِ برگشت پشتِ‌هم گیج‌کننده بود.
    var loanDetailOpen by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize()) {
      if (!(subTab == LoanSubTab.MY_LOANS && loanDetailOpen)) {
        // «وام» دیگه تبِ نوارِ پایین نیست (رجوع کن به کامنتِ بالای BottomTab تو این فایل) - چون از
        // «سررسید»/«خانه» به‌عنوانِ صفحه‌ی پوش‌شده باز می‌شه، یه دکمه‌ی برگشتِ واقعی لازم داره.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ir.sadteam.loancalc.ui.components.PageHeaderHeight)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = ir.sadteam.loancalc.ui.theme.AppText)
            }
            // فریمِ `27a`: عنوانِ ۱۸ با وزنِ ۹۰۰، و دکمه‌ی افزودن سمتِ مقابل تو قابِ ۳۲ی سبز.
            Text(
                "وام",
                color = AppText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f),
            )
            // فریمِ ۷۶a: دو آیکونِ ۳۲ی هم‌ردیفِ عنوان - **صفر پیکسل ارتفاعِ تازه**.
            // ⚠️ انحراف از بندِ ۵ فریمِ `76c`: دکمه‌ی بازگشت **می‌مانَد**. طراح فرض کرده
            // «وام» تبِ سطحِ اول است و نوارِ پایین جای برگشتن، ولی در این برنامه وام
            // **تبِ نوارِ پایین نیست** - صفحه‌ای پوش‌شده از «خانه»/«سررسید» است، پس
            // برداشتنِ دکمه تنها راهِ برگشت را به دکمه‌ی سخت‌افزاری محدود می‌کرد.
            // چون در همان ردیف است، ارتفاعی هم اضافه نمی‌کند.
            // ۸ مهر (خواسته‌ی کاربر): تنظیمات و تیره/روشن کنارِ جستجوی تبِ وام. ۹ مهر: جای جستجو و تنظیمات عوض شد.
            // سربرگِ یکدست (۱۵ مهر): جستجو و چشمِ مبلغ (چپ‌ترین). تیره/روشن و تنظیمات از این‌جا رفتند:
            // تنظیمات از آدمکِ صفحه‌ی خانه باز می‌شود و تیره/روشن در «تنظیمات ← ظاهرِ برنامه» است.
            if (subTab == LoanSubTab.MY_LOANS) {
                ir.sadteam.loancalc.ui.components.HeaderIconButton(
                    icon = Icons.Filled.Search,
                    description = "جست‌وجو در وام‌ها",
                    active = searchOpen,
                    onClick = { searchOpen = !searchOpen },
                )
            }
            val loanPrivacyVm: ir.sadteam.loancalc.ui.privacy.PrivacyModeViewModel = hiltViewModel()
            ir.sadteam.loancalc.ui.components.PrivacyEyeHeaderButton(
                privacyMode = ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode.current,
                onToggle = { loanPrivacyVm.toggle() },
            )
        }
        // حالتِ ساده: فقط «وام‌های من»، بی تب‌های سپرده/محاسبه‌گر.
        if (!ir.sadteam.loancalc.ui.privacy.LocalSimpleMode.current) Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // فریمِ `27a`: تبِ فعال یه **قرصِ سبزِ توپر با سایه‌ی سخت** و متنِ سفیده؛ بقیه فقط
            // متنِ خاکستریِ بی‌زمینه‌ان. (نسخه‌ی قبلی هر چهارتا رو یه Surfaceِ کم‌آلفا می‌کرد.)
            LoanSubTab.entries.forEach { entry ->
                val selected = entry == subTab
                val shape = RoundedCornerShape(999.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (selected) {
                                Modifier
                                    .hardShadow(AppPrimaryDim, offsetY = 3.dp, cornerRadius = 999.dp)
                                    .clip(shape)
                                    .background(AppPrimary)
                            } else {
                                Modifier.clip(shape)
                            },
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { calcAddLoan = false; subTab = entry }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        entry.label,
                        color = if (selected) Color.White else AppMuted,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
      }
        Box(modifier = Modifier.weight(1f)) {
            when (subTab) {
                LoanSubTab.CALCULATOR -> CalculatorHostTab(
                    onAddManualLoan = {
                        openManualAdd = true
                        calcAddLoan = false
                        subTab = LoanSubTab.MY_LOANS
                    },
                    startInInstallment = calcAddLoan,
                    onLeaveInstallment = {
                        calcAddLoan = false
                        subTab = LoanSubTab.MY_LOANS
                    },
                )
                LoanSubTab.DEPOSIT -> DepositScreen()
                LoanSubTab.MY_LOANS -> MyLoansScreen(
                    searchOpen = searchOpen,
                    onOpenCalculator = {
                        calcAddLoan = true
                        subTab = LoanSubTab.CALCULATOR
                    },
                    openManualAddSignal = openManualAdd,
                    onManualAddSignalConsumed = { openManualAdd = false },
                    onManualAddFabPositioned = onManualAddFabPositioned,
                    onBottomBarVisibilityChanged = onBottomBarVisibilityChanged,
                    deepLinkLoanId = deepLinkLoanId,
                    onDeepLinkConsumed = onDeepLinkConsumed,
                    onDetailOpenChanged = { loanDetailOpen = it },
                )
            }
        }
    }
}
/**
 * آیکونِ ۳۲یِ هم‌ردیفِ عنوانِ تبِ وام - فریمِ `76a`.
 *
 * هدفِ لمسی ۴۴dp است ولی **قاب** ۳۲ - همان الگوی بندِ ۸ سیستمِ طراحی: فضای لمسی بزرگ‌تر
 * از فضای دیده‌شده. حالتِ فعال قرصِ سبز می‌گیرد تا کاربر بداند فیلد/کارتِ پایین مالِ
 * کدام دکمه است.
 */
@Composable
private fun LoanHeaderIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(11.dp)
    Box(
        modifier = Modifier
            .size(44.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(shape)
                .background(if (active) AppPrimaryPill else AppSurface)
                .border(1.5.dp, if (active) AppPrimaryBorder else AppLineRow, shape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (active) AppPrimaryInk else AppMuted,
                modifier = Modifier.size(15.dp),
            )
        }
    }
}
