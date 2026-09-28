package ir.sadteam.loancalc.ui.accounting

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppMuted

/**
 * ═══════════ سه اصلاحِ ردیفِ تراکنش - فریمِ `48a` ═══════════
 *
 * هیچ‌کدام فیچرِ تازه نیستند؛ هر سه چیزی‌اند که در طرح فرض شده بود و در دستِ کاربر کار
 * نمی‌کرد.
 */

/**
 * **۱ · واحدِ مبلغ، خطِ دوم.**
 *
 * «۳۵۰ هزار» خطِ اول، «تومان» خطِ دومِ کم‌رنگ. چسباندنِ واحد به عدد ستونِ ارقام را از
 * تراز درمی‌آورد، چون طولِ واحد ثابت نیست («تومان» در برابرِ «میلیون تومان»).
 *
 * ⚠️ خودِ عدد از لایه‌ی ماسکِ خصوصیِ موجود رد می‌شود (`maskDigits`)؛ **واحد ماسک
 * نمی‌گیرد** - نقطه‌چین‌کردنِ «تومان» معنایی ندارد و فقط ستون را می‌شکند.
 */
@Composable
fun AmountWithUnit(
    formattedAmount: String,
    isExpense: Boolean,
    unit: String = "تومان",
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.End) {
        Text(
            formattedAmount,
            color = if (isExpense) AppDangerInk else AppMuted,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Black,
        )
        Text(unit, color = AppMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp))
    }
}

/**
 * **۲ · اشاره‌ی کشیدن.**
 *
 * اولین بارِ باز شدنِ صفحه در هر نشست، ردیفِ اول ۳۰px می‌سُرد و برمی‌گردد، پس قرمزیِ حذف
 * یک لحظه پیدا می‌شود.
 *
 * ⚠️ **یک‌بار در نشست، نه هر بار** - `rememberSaveable` بیرونِ اسکرین نگه داشته می‌شود
 * (در ViewModel یا یک `object`ِ نشست). تکرارش در هر ورود آزاردهنده می‌شود و کاربری که
 * اشاره را فهمیده، هر بار یک ردیفِ لرزان می‌بیند.
 *
 * تاخیرِ ۶۰۰ms لازم است: بی آن، لغزش هم‌زمان با انیمیشنِ ورودِ صفحه اجرا می‌شود و
 * کاربر آن را حرکتِ خودِ صفحه می‌فهمد، نه اشاره‌ی ردیف.
 */
@Composable
fun rememberSwipeHintOffset(enabled: Boolean): Modifier {
    val density = LocalDensity.current
    val offset = remember { Animatable(0f) }
    var shown by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(enabled) {
        if (!enabled || shown) return@LaunchedEffect
        shown = true
        kotlinx.coroutines.delay(600)
        val px = with(density) { 30.dp.toPx() }
        // در RTL ردیف به **راست** می‌سُرد تا حذفِ سمتِ چپ پیدا شود.
        offset.animateTo(px, tween(400))
        offset.animateTo(0f, tween(250))
    }
    return Modifier.graphicsLayer { translationX = offset.value }
}

/**
 * **۳ · ویرایش = همان شیتِ ثبت، در حالتِ ویرایش.**
 *
 * فرمِ جدا نمی‌سازیم؛ فیلدها پرشده باز می‌شوند.
 *
 * ⚠️ **ویرایش سکه نمی‌دهد.** وگرنه ویرایشِ پیاپیِ یک تراکنش معدنِ سکه می‌شود -
 * سقفِ `TRANSACTION_ADDED` هم جلویش را نمی‌گیرد چون کلیدِ دیگری است. متنِ دکمه همین را
 * می‌گوید: «ذخیره‌ی تغییر»، نه «ثبت ۱۰ سکه».
 *
 * و بالای شیت «حذف» هم هست، پس **کشیدن تنها راهِ حذف نمی‌ماند** - کاربری که اشاره‌ی
 * موردِ ۲ را ندیده هم راهی دارد.
 */
enum class TxSheetMode(val actionLabel: String, val awardsCoins: Boolean) {
    ADD("ثبت", awardsCoins = true),
    EDIT("ذخیره‌ی تغییر", awardsCoins = false),
}
