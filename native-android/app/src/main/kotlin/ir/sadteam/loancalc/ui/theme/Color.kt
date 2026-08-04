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
// بشه. **بروزرسانی**: کاربر بعداً خواستِ تمِ روشن هم همین حس رو بگیره (رجوع کن به کامنتِ
// [LightAppColors] پایین) - پس الان هر دو تم سبزِ پررنگ/خوش‌رنگ دارن، نه فقط تیره.
val DarkAppColors = AppColorPalette(
    bg = Color(0xFF000000), // کاملاً مشکی (خواسته‌ی صریحِ کاربر: «پشت کامل مشکی»)
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

// دورِ چهارمِ تمِ روشن - هم‌راستا با تمِ تیره‌ی جدید (خواسته‌ی صریحِ کاربر: «حالتِ روشن هم باشه، پشت
// کامل سفید، تو پنجره‌ها/تب‌ها همون سبزِ خوش‌رنگ»). primary/primaryDim از فیروزه‌ای به یه سبزِ پررنگ/
// اشباع‌شده عوض شد (نه دقیقاً همون سبزِ نئونیِ تمِ تیره، چون اون رو زمینه‌ی سفید کنتراستِ کافی نداره -
// رجوع کن به تاریخچه‌ی باگِ کنتراستِ تمِ روشن پایین‌تر؛ همون درس اینجا هم رعایت شد) - ولی بازم واضح و
// اشباع‌شده‌ست، نه یه سبزِ کم‌رنگ. bg کاملاً سفید شد (قبلاً یه سفیدِ کمی‌آبی‌گرا بود).
val LightAppColors = AppColorPalette(
    bg = Color(0xFFFFFFFF), // کاملاً سفید (خواسته‌ی صریحِ کاربر: «پشت کامل سفید»)
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFF1F7F3),
    primary = Color(0xFF00A651),
    primaryDim = Color(0xFF00803D),
    accent = Color(0xFFB8860B),
    text = Color(0xFF141A2A),
    muted = Color(0xFF62705F),
    danger = Color(0xFFC6474B),
    line = Color(0x1400A651), // سبزِ پررنگ با آلفای ~۸٪
    // باگِ رفع‌شده (تاریخی): مقادیرِ اولیه (پایه‌ی ۷۰٪ + گرادیانِ ۸٪ + حاشیه‌ی ۱۲٪) رو زمینه‌ی از قبل
    // تقریباً سفیدِ تمِ روشن عملاً نامرئی بود - کاربر گزارش داد «تو حالت عادی باکس‌ها اصلاً شیشه‌ای
    // نیست سفیده». پایه‌ی نیمه‌شفاف‌تر شد و گرادیان/حاشیه پررنگ‌تر - همون درس اینجا هم رعایت شد.
    // دورِ چهارم: حاشیه/هایلایتِ کارت از سفیدِ خنثی به سبزِ پررنگ عوض شد («پنجره‌ها سبزِ خوش‌رنگ باشن»)،
    // پایه همچنان عمدتاً سفید موند (پس‌زمینه‌ی «کامل سفید» از پشتِ کارت هم دیده بشه).
    glassBase = Color(0x66FFFFFF), // سفید با آلفای ~۴۰٪
    glassGradientStart = Color(0x2200A651), // سبزِ پررنگ با آلفای ~۱۳٪ (به‌جای سایه‌ی تیره‌ی قبلی)
    glassGradientEnd = Color(0x0000A651), // کاملاً شفاف
    glassBorder = Color(0x5500A651), // سبزِ پررنگ با آلفای ~۳۳٪ - لبه‌ی سبزِ واضح
    glassHighlight = Color(0x6000A651), // سبزِ پررنگ با آلفای ~۳۸٪
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
