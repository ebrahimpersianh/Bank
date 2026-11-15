package ir.sadteam.loancalc.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.sadteam.loancalc.data.Badge
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppGoldBorder
import ir.sadteam.loancalc.ui.theme.AppGoldFrom
import ir.sadteam.loancalc.ui.theme.AppGoldInk
import ir.sadteam.loancalc.ui.theme.AppGoldTo
import ir.sadteam.loancalc.ui.theme.AppLine

/**
 * ── تصمیمِ طراحی ──
 *
 * نشانِ باز‌شده **مدالِ مشترک** می‌گیرد: همان دیسکِ کاغذِ طلایی، همان حلقه.
 * تمایز از **نمادِ داخلِ مدال** می‌آید، نه از رنگِ مدال.
 *
 * چرا نه رنگ: نُه رنگِ نشان یعنی نُه هگزِ تازه که در تمِ تیره نمی‌چرخند — همان
 * دامی که در تبِ دارایی گرفتارش شدیم (`GoldPaperTo`, `HeroShadow`). نشانِ
 * دستاورد هم برخلافِ نشانِ برندِ بیت‌کوین، رنگِ هویتیِ خودش را ندارد؛ رنگش
 * رنگِ «دستاورد» است و آن یکی است. پس یک پالت، نُه نماد.
 *
 * تفاوتِ ۲۵ و ۱۵۰ سکه هم رنگ نمی‌گیرد — عددِ سکه کنارِ نشان نوشته می‌شود و
 * خودش گویاست؛ سه‌پله‌کردنِ طلا فقط مدال‌های کم‌ارزش را کدر می‌کرد.
 *
 * `LOAN_CLOSED` استثناست و `SettledMedal`ِ موجود را نگه می‌دارد: آن مدال از
 * قبل معنیِ «وامِ تسویه‌شده» را دارد و کاربر همان را در مرکزِ وام دیده.
 *
 * ── چه چیزی را دست نمی‌زند ──
 *
 * ردیفِ **در جریان** در `BadgesScreen` مدال ندارد، حلقه‌ی درصد دارد. آن حلقه
 * سر جایش می‌ماند و `BadgeMedal` جایش نمی‌نشیند: درصدِ پیشرفت خبرِ تازه است و
 * نمادِ نشان در همان ردیف کنارِ برچسبِ فارسی تکرارِ چیزی است که خوانده می‌شود.
 * پس `BadgeMedal` فقط دو حالتِ «باز شده» و «قفل» را می‌گیرد.
 */

/**
 * نمادِ داخلِ مدال برای هر نشان.
 *
 * ⚠️ هر نُه آیکون از `material-icons-extended` می‌آیند (مثلِ `TrendingUp` که
 * الان در `AssetSection` استفاده می‌شود)، پس وابستگیِ تازه‌ای اضافه نمی‌شود.
 * اگر روزی extended حذف شد، این فایل تنها جایی است که باید عوض شود.
 */
fun badgeIconOf(badge: Badge): ImageVector = when (badge) {
    Badge.FIRST_STEP   -> Icons.Filled.Flag          // پرچمِ شروع
    Badge.BUDGETER     -> Icons.Filled.PieChart      // سهمِ بودجه
    Badge.FULL_WEEK    -> Icons.Filled.DateRange     // بازه‌ی هفت‌روزه
    Badge.CAUTIOUS     -> Icons.Filled.Shield        // سپر = خرجِ کمتر از میانگین
    Badge.UNDER_BUDGET -> Icons.Filled.TrendingDown  // زیرِ خط ماندن
    Badge.CLEAN_DESK   -> Icons.Filled.Checklist     // همه دسته‌دار
    Badge.GOAL_REACHED -> Icons.Filled.EmojiEvents   // جامِ هدف
    Badge.LOAN_CLOSED  -> Icons.Filled.Shield        // دیده نمی‌شود؛ SettledMedal جایش است
    Badge.STEADY_MONTH -> Icons.Filled.CalendarMonth // ماهِ کامل
    Badge.COLLECTION_STARRY_NIGHT -> Icons.Filled.EmojiEvents // تکمیل مجموعه‌ی شب پرستاره
}

/**
 * دیسکِ نشان در سه حالت.
 *
 * - باز‌شده: کاغذِ طلایی + حلقه‌ی طلایی + نمادِ `AppGoldInk`.
 * - قفل: چیپِ خنثی + قفلِ `AppLabel`، قفل ۰٫۴۴ِ قطر. عیناً همان چیزی که امروز
 *   در `BadgesScreen` هست — رنگ و اندازه از همان‌جا آمده، نه از حدس.
 * - «به‌زودی» (فقط `GOAL_REACHED`): همان دیسکِ خنثی ولی با **نمادِ خودش**، نه
 *   قفل — قفل یعنی «هنوز نگرفتی»، و این یکی اصلاً قابلِ گرفتن نیست.
 *
 * `LOAN_CLOSED`ِ باز‌شده اینجا نمی‌آید؛ در صفحه `SettledMedal` صدا زده می‌شود.
 *
 * نمادِ نشان ۰٫۵ِ قطر است (۳۴dp می‌شود ۱۷dp)، قفل ۰٫۴۴ (۱۵dp) تا با ردیفِ فعلی
 * یکی باشد.
 */
@Composable
fun BadgeMedal(
    badge: Badge,
    unlocked: Boolean,
    size: Dp = 34.dp,
    modifier: Modifier = Modifier,
) {
    val comingSoon = badge.comingSoon && !unlocked
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (unlocked) {
                    Modifier.background(Brush.verticalGradient(listOf(AppGoldFrom, AppGoldTo)))
                } else {
                    Modifier.background(AppChipBg)
                }
            )
            .border(1.dp, if (unlocked) AppGoldBorder else AppLine, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        val showGlyph = unlocked || comingSoon
        Icon(
            imageVector = if (showGlyph) badgeIconOf(badge) else Icons.Filled.Lock,
            contentDescription = null, // برچسبِ فارسیِ نشان کنارش نوشته شده
            tint = if (unlocked) AppGoldInk else AppLabel,
            modifier = Modifier.size(size * if (showGlyph) 0.5f else 0.44f),
        )
    }
}
