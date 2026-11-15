package ir.sadteam.loancalc.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

/** پورت مستقیم آرایه‌ی presets تو www/index.html (وام‌های پرتکرار) */
data class LoanPreset(
    val key: String,
    val icon: ImageVector,
    val title: String,
    val sub: String,
    val amount: Long,
    val ratePct: Double,
    val months: Int,
    val graceMonths: Int,
    val note: String? = null,
)

val loanPresets: List<LoanPreset> = listOf(
    LoanPreset(
        key = "marriage", icon = Icons.Filled.Favorite, title = "وام ازدواج", sub = "۴ درصد",
        amount = 3_000_000_000, ratePct = 4.0, months = 120, graceMonths = 0,
        note = "طبق مصوبه بانک مرکزی: کارمزد ۴٪ سالانه (قرض‌الحسنه، بدون سود)، مبلغ ۳۰۰ میلیون تومان برای هر نفر (تا ۳۵۰ میلیون برای زوجین زیر ۲۳/۲۵ سال) و بازپرداخت ۱۲۰ ماهه.",
    ),
    LoanPreset(
        key = "child", icon = Icons.Filled.ChildCare, title = "وام فرزندآوری", sub = "۴ درصد",
        amount = 440_000_000, ratePct = 4.0, months = 36, graceMonths = 6,
        note = "کارمزد ۴٪ سالانه. مبلغ فرزند اول ۴۴ میلیون تومان (۳۶ ماهه)، فرزند دوم ۸۸ میلیون (۴۸ ماهه) تا فرزند پنجم به بعد ۲۲۰ میلیون (۸۴ ماهه)؛ ۶ ماه تنفس قبل از اولین قسط.",
    ),
    LoanPreset(
        key = "housing", icon = Icons.Filled.Home, title = "وام مسکن", sub = "۱۸ درصد",
        amount = 20_000_000_000, ratePct = 18.0, months = 144, graceMonths = 0,
    ),
    LoanPreset(
        key = "rentDeposit", icon = Icons.Filled.Home, title = "وام ودیعه مسکن", sub = "۲۳ درصد",
        amount = 3_000_000_000, ratePct = 23.0, months = 60, graceMonths = 0,
        note = "نرخ سود مصوب شورای پول و اعتبار: ۲۳٪، بازپرداخت ۶۰ ماهه. سقف تسهیلات در تهران ۳۰۰ میلیون تومان، مراکز استان‌ها ۱۵۰ میلیون و سایر شهرها کمتر (بسته به منطقه).",
    ),
    LoanPreset(
        key = "car", icon = Icons.Filled.DirectionsCar, title = "وام خودرو", sub = "۲۳ درصد",
        amount = 3_000_000_000, ratePct = 23.0, months = 36, graceMonths = 0,
    ),
    LoanPreset(
        key = "qarz", icon = Icons.Filled.Handshake, title = "قرض‌الحسنه", sub = "۴ درصد",
        amount = 1_000_000_000, ratePct = 4.0, months = 24, graceMonths = 0,
    ),
    LoanPreset(
        key = "goods", icon = Icons.Filled.ShoppingBag, title = "کالا / لوازم خانگی", sub = "۱۸ درصد",
        amount = 800_000_000, ratePct = 18.0, months = 18, graceMonths = 0,
    ),
)
