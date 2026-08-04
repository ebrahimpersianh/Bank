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

// دورِ پنجمِ تمِ تیره - تعدیلِ سبزِ نئونیِ دورِ قبل (خواسته‌ی صریحِ کاربر: «رنگ سبز ملایم‌تر بشه، کل اپ
// مینیمال‌تر باشه»). primary از سبزِ نئونیِ خالص (#00FF9C) به یه زمردیِ ملایم‌تر (#34D399، هم‌خانواده
// ولی کمتر چشم‌آزار/تبلیغاتی) عوض شد. آلفای گرادیان/حاشیه/هایلایتِ شیشه‌ای هم کم شد تا افکتِ «گلو»
// کمتر توی‌ذوق‌بزن باشه - حسِ مینیمال‌تر عمدتاً از همین‌جا میاد (نه حذفِ خودِ افکت‌ها). accent (طلایی)
// عمداً دست‌نخورده موند - نشانه‌ی جداگانه‌ی «پرمیوم/اشتراک» تو کل اپ (بج‌های اشتراک، GoldSheenBox).
val DarkAppColors = AppColorPalette(
    bg = Color(0xFF000000), // کاملاً مشکی (خواسته‌ی صریحِ کاربر: «پشت کامل مشکی»)
    surface = Color(0xFF0F1712),
    surface2 = Color(0xFF16211C),
    primary = Color(0xFF34D399),
    primaryDim = Color(0xFF0F9D6D),
    accent = Color(0xFFFFB020),
    text = Color(0xFFEEF1F8),
    muted = Color(0xFF869489),
    danger = Color(0xFFE56B6F),
    line = Color(0x1034D399), // زمردیِ ملایم با آلفای ~۶٪ - جداکننده‌های ظریف
    // پایه‌ی کارت کمی روشن‌تر از bg (که تقریباً مشکیه) تا کارت‌ها رو زمینه دیده بشن؛ گرادیان/حاشیه/
    // هایلایت زمردیِ ملایم‌ان (آلفای کمتر از دورِ قبل، برای حسِ مینیمال‌تر/کمتر پرزرق‌وبرق).
    glassBase = Color(0x5C14241E), // مشکیِ سبزتاب با آلفای ~۳۶٪
    glassGradientStart = Color(0x2034D399), // زمردیِ ملایم با آلفای ~۱۲٪
    glassGradientEnd = Color(0x0034D399), // کاملاً شفاف
    glassBorder = Color(0x4034D399), // زمردیِ ملایم با آلفای ~۲۵٪ - لبه‌ی ظریف‌تر از قبل
    glassHighlight = Color(0x3A34D399), // زمردیِ ملایم با آلفای ~۲۳٪
)

// دورِ پنجمِ تمِ روشن - هم‌قدم با تعدیلِ تمِ تیره‌ی بالا (کاربر: «تو حالتِ روشن قشنگ نیست خیلی»).
// primary/primaryDim به یه زمردیِ متعادل‌تر (نه سبزِ خیلی پررنگِ دورِ قبل) عوض شد که رو پس‌زمینه‌ی
// سفید هم کنتراستِ خوبی داره هم چشم‌آزار نیست. آلفای گرادیان/حاشیه/هایلایت هم کم شد (حسِ مینیمال‌تر).
val LightAppColors = AppColorPalette(
    bg = Color(0xFFFFFFFF), // کاملاً سفید (خواسته‌ی صریحِ کاربر: «پشت کامل سفید»)
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFF1F7F3),
    primary = Color(0xFF10B981),
    primaryDim = Color(0xFF047857),
    accent = Color(0xFFB8860B),
    text = Color(0xFF141A2A),
    muted = Color(0xFF62705F),
    danger = Color(0xFFC6474B),
    line = Color(0x1010B981), // زمردیِ متعادل با آلفای ~۶٪
    // باگِ رفع‌شده (تاریخی): مقادیرِ اولیه (پایه‌ی ۷۰٪ + گرادیانِ ۸٪ + حاشیه‌ی ۱۲٪) رو زمینه‌ی از قبل
    // تقریباً سفیدِ تمِ روشن عملاً نامرئی بود - کاربر گزارش داد «تو حالت عادی باکس‌ها اصلاً شیشه‌ای
    // نیست سفیده». پایه‌ی نیمه‌شفاف‌تر شد و گرادیان/حاشیه پررنگ‌تر - همون درس اینجا هم رعایت شد.
    // دورِ پنجم: هم رنگ (زمردیِ متعادل‌تر به‌جای سبزِ خیلی پررنگ) هم آلفا (کمتر) تعدیل شد.
    glassBase = Color(0x5CFFFFFF), // سفید با آلفای ~۳۶٪
    glassGradientStart = Color(0x1810B981), // زمردیِ متعادل با آلفای ~۹٪
    glassGradientEnd = Color(0x0010B981), // کاملاً شفاف
    glassBorder = Color(0x3810B981), // زمردیِ متعادل با آلفای ~۲۲٪
    glassHighlight = Color(0x4510B981), // زمردیِ متعادل با آلفای ~۲۷٪
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
