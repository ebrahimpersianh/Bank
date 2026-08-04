package ir.sadteam.loancalc.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** دقیقاً هم‌راستا با :root {}/body.light {} تو www/index.html. */
data class AppColorPalette(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val primary: Color,
    val primaryDim: Color,
    val accent: Color,
    val text: Color,
    val muted: Color,
    val danger: Color,
    val line: Color,
    // سبکِ «شیشه‌ای» باکس‌های بزرگ (AppCard) - الهام‌گرفته از اپِ «تقویم من» (خواسته‌ی صریحِ کاربر،
    // با پیش‌نمایش تاییدشده). دو تمِ روشن/تیره عمداً یه معکوسِ ساده‌ی هم نیستن: تو تیره یه گرادیانِ
    // نورِ سفید رو یه پایه‌ی سرمه‌ای نیمه‌شفاف نشسته (چون پس‌زمینه تیره‌ست، «نور» باید روشن‌تر از
    // زمینه باشه)؛ تو روشن یه گرادیانِ سایه‌ی تیره رو یه پایه‌ی سفیدِ نیمه‌شفاف (چون زمینه‌ش از قبل
    // روشنه، سفیدِ اضافه اثری نداره - سایه‌ی ظریف حسِ لبه‌ی شیشه رو می‌ده).
    val glassBase: Color,
    val glassGradientStart: Color,
    val glassGradientEnd: Color,
    val glassBorder: Color,
    // نقطه‌ی نورِ نوکِ‌تیزِ گوشه‌ی بالا-چپِ AppCard (خواسته‌ی کاربر: طرحِ «Liquid Glass»ِ iOS از بینِ
    // ۴ طرحِ پیش‌نمایش‌شده‌ی HTML انتخاب شد) - همون هایلایتِ نوریِ مشخصه‌ی شیشه‌ی اپل که با یه بلورِ
    // واقعی شبیه‌سازی می‌شه، اینجا چون AuroraBackground عمداً بدونِ RenderEffect (بلورِ واقعی، سنگین
    // رو گوشیِ ضعیف) پیاده شده، با یه Brush.radialGradient ثابت تقلید می‌شه.
    val glassHighlight: Color,
)

// دورِ چهارمِ تمِ تیره - «Glassmorphism مشکی + سبزِ نئونی» (خواسته‌ی صریحِ کاربر، الهام از یه رفرنسِ
// UIِ فینتکِ تیره - پس‌زمینه‌ی تقریباً مشکی + کارت‌های شیشه‌ایِ نیمه‌شفاف با گلوِ سبزِ نئونی). primary
// از سبزآبیِ قبلی به یه سبزِ نئونیِ واقعی عوض شد؛ چون همه‌ی پس‌زمینه‌های کم‌رنگِ باکس‌ها/Aurora/دکمه‌ها
// تو کل اپ با AppPrimary.copy(alpha=...) ساخته می‌شن، همین یه تغییرِ رنگِ پایه کافیه که همه‌جا (دکمه،
// آیکون، Aurora، حاشیه‌ی شیشه‌ای) یکجا آپدیت بشه. accent (طلایی) عمداً دست‌نخورده موند - نشانه‌ی
// جداگانه‌ی «پرمیوم/اشتراک» تو کل اپ (بج‌های اشتراک، GoldSheenBox) که نباید با رنگِ اصلیِ جدید قاطی
// بشه. تمِ روشن ([LightAppColors] پایین) عمداً تغییر نکرد - رفرنسِ کاربر فقط تیره بود.
val DarkAppColors = AppColorPalette(
    bg = Color(0xFF070B09),
    surface = Color(0xFF0F1712),
    surface2 = Color(0xFF16211C),
    primary = Color(0xFF00FF9C),
    primaryDim = Color(0xFF00B873),
    accent = Color(0xFFFFB020),
    text = Color(0xFFEEF1F8),
    muted = Color(0xFF869489),
    danger = Color(0xFFE56B6F),
    line = Color(0x1400FF9C), // سبزِ نئونی با آلفای ~۸٪ - جداکننده‌های ظریف
    // پایه‌ی کارت کمی روشن‌تر از bg (که تقریباً مشکیه) تا کارت‌ها رو زمینه دیده بشن؛ گرادیان/حاشیه/
    // هایلایت همه سبزِ نئونی شدن (به‌جای سفیدِ خنثیِ قبلی) تا حسِ «گلوِ نئونی»ِ رفرنس رو بدن.
    glassBase = Color(0x6614241E), // مشکیِ سبزتاب با آلفای ~۴۰٪
    glassGradientStart = Color(0x2A00FF9C), // سبزِ نئونی با آلفای ~۱۶٪
    glassGradientEnd = Color(0x0000FF9C), // کاملاً شفاف
    glassBorder = Color(0x5500FF9C), // سبزِ نئونی با آلفای ~۳۳٪ - لبه‌ی نورانی
    glassHighlight = Color(0x5000FF9C), // سبزِ نئونی با آلفای ~۳۱٪
)

val LightAppColors = AppColorPalette(
    bg = Color(0xFFF6F7FB),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFEEF1F8),
    primary = Color(0xFF008C99),
    primaryDim = Color(0xFF00707A),
    accent = Color(0xFFB8860B),
    text = Color(0xFF141A2A),
    muted = Color(0xFF6B7488),
    danger = Color(0xFFC6474B),
    line = Color(0x14141A2A), // rgba(20,26,42,0.08)
    // باگِ رفع‌شده: مقادیرِ اولیه (پایه‌ی ۷۰٪ + گرادیانِ ۸٪ + حاشیه‌ی ۱۲٪) رو زمینه‌ی از قبل تقریباً
    // سفیدِ تمِ روشن عملاً نامرئی بود - کاربر گزارش داد «تو حالت عادی باکس‌ها اصلاً شیشه‌ای نیست
    // سفیده». پایه‌ی نیمه‌شفاف‌تر شد (پس‌زمینه/Aurora بیشتر ازش رد می‌شه) و گرادیان/حاشیه پررنگ‌تر.
    // دورِ سومِ شیشه‌ای‌شدن («Liquid Glass»): پایه شفاف‌تر شد (۵۰٪→۴۰٪)، و برخلافِ حاشیه‌ی تیره‌ی
    // قبلی، حاشیه هم سفید/روشن شد (لبه‌ی شیشه‌ی واقعی نور رو منعکس می‌کنه، نه سایه می‌ندازه).
    glassBase = Color(0x66FFFFFF), // سفید با آلفای ~۴۰٪
    glassGradientStart = Color(0x20141A2A), // متن تیره با آلفای ~۱۳٪ (سایه‌ی ظریفِ لبه‌ی شیشه)
    glassGradientEnd = Color(0x00141A2A), // کاملاً شفاف
    glassBorder = Color(0x4DFFFFFF), // سفید با آلفای ~۳۰٪
    glassHighlight = Color(0x99FFFFFF), // سفید با آلفای ~۶۰٪
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

/**
 * این‌ها عمداً property هستن نه val ثابت (با `@Composable get()`) - همه‌ی صفحه‌ها همین اسم‌ها رو
 * مستقیم import می‌کنن (`AppText`، `AppPrimary`، ...)، این‌جوری فقط با اضافه‌کردن تم روشن به
 * [LocalAppColors]، بدون تغییر تک‌تک صفحه‌ها همه‌جا رنگ درست به‌روز می‌شه.
 */
val AppBg: Color @Composable get() = LocalAppColors.current.bg
val AppSurface: Color @Composable get() = LocalAppColors.current.surface
val AppSurface2: Color @Composable get() = LocalAppColors.current.surface2
val AppPrimary: Color @Composable get() = LocalAppColors.current.primary
val AppPrimaryDim: Color @Composable get() = LocalAppColors.current.primaryDim
val AppAccent: Color @Composable get() = LocalAppColors.current.accent
val AppText: Color @Composable get() = LocalAppColors.current.text
val AppMuted: Color @Composable get() = LocalAppColors.current.muted
val AppDanger: Color @Composable get() = LocalAppColors.current.danger
val AppLine: Color @Composable get() = LocalAppColors.current.line
val AppGlassBase: Color @Composable get() = LocalAppColors.current.glassBase
val AppGlassGradientStart: Color @Composable get() = LocalAppColors.current.glassGradientStart
val AppGlassGradientEnd: Color @Composable get() = LocalAppColors.current.glassGradientEnd
val AppGlassBorder: Color @Composable get() = LocalAppColors.current.glassBorder
val AppGlassHighlight: Color @Composable get() = LocalAppColors.current.glassHighlight
