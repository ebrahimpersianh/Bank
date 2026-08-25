package ir.sadteam.loancalc.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * پالتِ رنگیِ اپ - **بازطراحیِ دومِ ظاهری، سبکِ Duolingo/«جیبک»** (مرداد ۱۴۰۵).
 *
 * مقادیر مو‌به‌مو از `design_handoff_bank_redesign/سیستمِ طراحی جیبک.dc.html` (بخشِ «۱ · رنگ» و
 * جدولِ «نقشه‌ی توکن»ِ حالتِ تیره) برداشته شدن - **حدس زده نشدن**.
 *
 * ⚠️ **تفاوتِ بنیادی با دورِ قبل («Liquid Glass»)**: سطحِ نیمه‌شفاف، گرادیانِ نوری، حاشیه‌ی درخشان و
 * بلورِ پس‌زمینه **همه حذف شدن**. زبانِ جدید: سطحِ کاملاً مات، حاشیه‌ی ۲ پیکسلیِ توپر، سایه‌ی
 * **سختِ عمودی بدونِ تاری** (`0 4px 0` هم‌رنگِ تیره‌ترِ خودش)، دکمه‌ی کپسولی، و **حداکثر یک رنگِ
 * لهجه در هر صفحه**.
 *
 * فیلدهای `glass*` عمداً حذف نشدن (چند فایل هنوز ازشون استفاده می‌کنن) ولی به معادلِ **تختِ** خودشون
 * اشاره می‌کنن تا هیچ جای اپ سطحِ شیشه‌ای باقی نمونه - رجوع کن به کامنتِ خودشون پایین‌تر.
 */
data class AppColorPalette(
    // ── سطح و متن ────────────────────────────────────────────────────────────────
    /** زمینه‌ی صفحه. **هیچ‌وقت مشکیِ خالص نیست** (قاعده‌ی صریحِ سیستمِ طراحی). */
    val bg: Color,
    /** سطحِ کارت. */
    val surface: Color,
    /** سطحِ دوم - ردیفِ جدول، بخشِ فرعیِ داخلِ کارت. */
    val surface2: Color,
    /** متنِ اصلی (تیتر و عدد). **هیچ‌وقت مشکیِ خالص نیست.** */
    val text: Color,
    /** متنِ دوم - توضیح. */
    val muted: Color,
    /** متنِ سوم - برچسبِ ریز، فراداده‌ی ردیف. */
    val label: Color,
    /** حاشیه‌ی کارت (۲ پیکسل). */
    val line: Color,
    /** حاشیه‌ی ردیف‌های فهرست - عمداً روشن‌تر از [line]. */
    val lineRow: Color,
    /** تهرنگِ چیپ و برچسبِ بی‌سایه. */
    val chip: Color,

    // ── سبز (رنگِ اصلی) ──────────────────────────────────────────────────────────
    /** سبزِ اصلی - دکمه‌ی اصلی، درآمد، حالتِ فعال. */
    val primary: Color,
    /** سایه‌ی سختِ زیرِ دکمه‌ی سبز + رنگِ متنِ لینک. */
    val primaryDim: Color,
    /**
     * سبزِ **متن و آیکون**. تو تمِ روشن با [primary] یکیه؛ تو تیره روشن‌تره (`#3DDC96`) چون
     * سبزِ دکمه رو زمینه‌ی تیره برای متن کنتراستِ کافی نداره. قاعده‌ی صریحِ سیستمِ طراحی:
     * «سبزِ دکمه عوض نمی‌شود؛ فقط سایه» ولی سبزِ متن/آیکون عوض می‌شود.
     */
    val primaryInk: Color,
    /** تهرنگِ قرصِ سبز (چیپِ وضعیتِ مثبت). */
    val primaryPill: Color,
    /** حاشیه‌ی قرصِ سبز - تو روشن شفافه، تو تیره دیده می‌شه. */
    val primaryPillBorder: Color,

    // ── قرمزِ هزینه ──────────────────────────────────────────────────────────────
    /** قرمزِ پرکننده - دکمه‌ی حذف، نوارِ هشدار. */
    val danger: Color,
    /** قرمزِ **متن** - عددِ منفی، سررسیدِ گذشته. تو روشن تیره‌تر از [danger] برای خوانایی. */
    val dangerInk: Color,
    /** تهرنگِ قرصِ قرمز. */
    val dangerPill: Color,
    /** زمینه‌ی کارتِ **فوری** (سررسیدِ امروز/فردا). حداکثر یکی در هر صفحه، بالای فهرست. */
    val urgentBg: Color,
    /** حاشیه‌ی کارتِ فوری. */
    val urgentBorder: Color,
    /** سایه‌ی سختِ کارتِ فوری (`0 4px 0`). */
    val urgentShadow: Color,

    // ── بقیه‌ی رنگ‌های معنایی ────────────────────────────────────────────────────
    /** آبی - اطلاع، انتقال بینِ حساب‌ها. */
    val info: Color,
    /** بنفش - بودجه و آمار. */
    val purple: Color,
    /** نارنجی - هشدارِ نرم، بودجه‌ی بالای ۷۰٪. */
    val warning: Color,

    // ── کاغذِ طلایی (کارتِ پول و دستاورد) ────────────────────────────────────────
    /** شروعِ گرادیانِ کارتِ طلایی. */
    val goldFrom: Color,
    /** پایانِ گرادیانِ کارتِ طلایی. */
    val goldTo: Color,
    /** حاشیه‌ی کارتِ طلایی. */
    val goldBorder: Color,
    /** متنِ اصلیِ رو کاغذِ طلایی. */
    val goldInk: Color,
    /** متنِ دومِ رو کاغذِ طلایی. */
    val goldInk2: Color,

    // ── حالتِ غیرفعال و سایه ─────────────────────────────────────────────────────
    /** پرکننده‌ی دکمه‌ی خاموش. */
    val disabledFill: Color,
    /** متنِ دکمه‌ی خاموش. */
    val disabledText: Color,
    /** سایه‌ی سختِ خنثی (`0 3px 0`) - زیرِ کارت و چیپِ بی‌رنگ. */
    val shadowNeutral: Color,

    // ── سازگاریِ عقب‌رو (فیلدهای دورِ «Liquid Glass») ─────────────────────────────
    // این‌ها حذف نشدن چون چند فایل (GradientButton، HomeScreen، MainActivity) هنوز مستقیم
    // صداشون می‌زنن؛ ولی همه به معادلِ **تختِ** خودشون اشاره می‌کنن تا حتی قبل از بازنویسیِ اون
    // فایل‌ها هم هیچ سطحِ شیشه‌ای/گرادیانِ نوری‌ای تو اپ باقی نمونه.
    // 🚫 **تو کدِ جدید ازشون استفاده نکن** - به‌جاش surface/line/Transparent.
    val glassBase: Color,
    val glassGradientStart: Color,
    val glassGradientEnd: Color,
    val glassBorder: Color,
    val glassHighlight: Color,

    /** برای تشخیصِ روشن/تیره داخلِ کامپوننت‌ها بدونِ نیاز به پاس‌دادنِ [ThemeMode]. */
    val isDark: Boolean,
)

/**
 * تمِ **روشن** - حالتِ پایه‌ی طرح. زمینه‌ی صفحه یه سفیدِ کمی آبی‌تاب (`#F5FBFF`)ه نه سفیدِ خالص، تا
 * کارت‌های سفیدِ روش دیده بشن.
 */
val LightAppColors = AppColorPalette(
    bg = Color(0xFFF5FBFF),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFF7FAF8),
    text = Color(0xFF16221C),
    muted = Color(0xFF5B6A63),
    label = Color(0xFF8B9A93),
    line = Color(0xFFE3ECE7),
    lineRow = Color(0xFFEEF3F0),
    chip = Color(0xFFF1F5F2),

    primary = Color(0xFF0EA968),
    primaryDim = Color(0xFF0B8C57),
    primaryInk = Color(0xFF0EA968),
    primaryPill = Color(0xFFE9F7EF),
    primaryPillBorder = Color(0x00000000), // تو روشن قرصِ سبز حاشیه نداره

    danger = Color(0xFFFF4B4B),
    dangerInk = Color(0xFFD93838),
    dangerPill = Color(0xFFFFECEC),
    urgentBg = Color(0xFFFFF5F5),
    urgentBorder = Color(0xFFFFC9C9),
    urgentShadow = Color(0xFFFFECEC),

    info = Color(0xFF1CB0F6),
    purple = Color(0xFFA56EFF),
    warning = Color(0xFFFF9600),

    goldFrom = Color(0xFFFFFCF4),
    goldTo = Color(0xFFF3E7CE),
    goldBorder = Color(0xFFEBD9B4),
    goldInk = Color(0xFF8B6F3D),
    goldInk2 = Color(0xFFA98D5B),

    disabledFill = Color(0xFFDDE7E2),
    disabledText = Color(0xFF94A5A0),
    shadowNeutral = Color(0xFFE8EFEB),

    glassBase = Color(0xFFFFFFFF),
    glassGradientStart = Color(0x00000000),
    glassGradientEnd = Color(0x00000000),
    glassBorder = Color(0xFFE3ECE7),
    glassHighlight = Color(0x00000000),

    isDark = false,
)

/**
 * تمِ **تیره** - طبقِ قاعده‌ی صریحِ سیستمِ طراحی «یک نقشه‌ی توکن، نه یک طراحیِ دوم»: اندازه، فاصله،
 * گوشه و ضخامتِ حاشیه **عیناً** مثلِ روشنه، فقط رنگ عوض می‌شه.
 *
 * ⚠️ زمینه `#10181F`ه نه مشکیِ خالص - این عمدیه و با دورِ قبل (که `#000000` بود، خواسته‌ی وقتِ خودِ
 * کاربر) فرق داره. تو سبکِ جدید مشکیِ خالص لبه‌ی کارت‌ها رو می‌خوره.
 */
val DarkAppColors = AppColorPalette(
    bg = Color(0xFF10181F),
    surface = Color(0xFF1B2530),
    surface2 = Color(0xFF202B36),
    text = Color(0xFFF3F7F5),
    muted = Color(0xFF8B9A94),
    label = Color(0xFF7A8A84),
    line = Color(0xFF2A3640),
    lineRow = Color(0xFF232E38),
    chip = Color(0xFF232E38),

    // سبزِ دکمه عمداً همون سبزِ روشنه (قاعده‌ی صریح)؛ فقط سایه‌ش تیره‌تر می‌شه و سبزِ متن روشن‌تر.
    primary = Color(0xFF0EA968),
    primaryDim = Color(0xFF07724A),
    primaryInk = Color(0xFF3DDC96),
    primaryPill = Color(0x243DDC96), // rgba(61,220,150,.14)
    primaryPillBorder = Color(0x593DDC96), // rgba(61,220,150,.35)

    danger = Color(0xFFFF6B6B),
    dangerInk = Color(0xFFFF6B6B),
    dangerPill = Color(0x1FFF6B6B), // rgba(255,107,107,.12)
    // معادلِ تیره‌ی کارتِ فوری - همون قرمزِ #FF6B6B با آلفای کم، ولی **مات** روی سطحِ #1B2530
    // حساب‌شده (کارت نباید نیمه‌شفاف باشه؛ قاعده‌ی «سطحِ کاملاً مات»).
    urgentBg = Color(0xFF2A1E22),
    urgentBorder = Color(0xFF4A2E2E),
    urgentShadow = Color(0xFF1E1519),

    info = Color(0xFF55C8FF),
    purple = Color(0xFFBE97FF),
    warning = Color(0xFFFFB44D),

    goldFrom = Color(0xFF242018),
    goldTo = Color(0xFF1B1712),
    goldBorder = Color(0xFF3A3226),
    goldInk = Color(0xFFF0D9A8),
    goldInk2 = Color(0xFFC9AE74),

    disabledFill = Color(0xFF232E38),
    disabledText = Color(0xFF6C7B75),
    shadowNeutral = Color(0xFF0B131A),

    glassBase = Color(0xFF1B2530),
    glassGradientStart = Color(0x00000000),
    glassGradientEnd = Color(0x00000000),
    glassBorder = Color(0xFF2A3640),
    glassHighlight = Color(0x00000000),

    isDark = true,
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

/**
 * این‌ها عمداً property هستن نه val ثابت (با `@Composable get()`) - همه‌ی صفحه‌ها همین اسم‌ها رو
 * مستقیم import می‌کنن (`AppText`، `AppPrimary`، ...)، این‌جوری با عوض‌شدنِ [LocalAppColors]
 * بدونِ تغییرِ تک‌تکِ صفحه‌ها همه‌جا رنگِ درست به‌روز می‌شه.
 *
 * ⚠️ **این‌ها `@Composable`ان** - صداکردنشون داخلِ بلوکِ `Canvas` (که `DrawScope`ه) خطای کامپایل
 * می‌ده. همیشه قبل از `Canvas` تو یه `val` محلی بخونشون.
 */
val AppBg: Color @Composable get() = LocalAppColors.current.bg
val AppSurface: Color @Composable get() = LocalAppColors.current.surface
val AppSurface2: Color @Composable get() = LocalAppColors.current.surface2
val AppText: Color @Composable get() = LocalAppColors.current.text
val AppMuted: Color @Composable get() = LocalAppColors.current.muted
val AppLabel: Color @Composable get() = LocalAppColors.current.label
val AppLine: Color @Composable get() = LocalAppColors.current.line
val AppLineRow: Color @Composable get() = LocalAppColors.current.lineRow
val AppChipBg: Color @Composable get() = LocalAppColors.current.chip

val AppPrimary: Color @Composable get() = LocalAppColors.current.primary
val AppPrimaryDim: Color @Composable get() = LocalAppColors.current.primaryDim
val AppPrimaryInk: Color @Composable get() = LocalAppColors.current.primaryInk
val AppPrimaryPill: Color @Composable get() = LocalAppColors.current.primaryPill
val AppPrimaryPillBorder: Color @Composable get() = LocalAppColors.current.primaryPillBorder

val AppDanger: Color @Composable get() = LocalAppColors.current.danger
val AppDangerInk: Color @Composable get() = LocalAppColors.current.dangerInk
val AppDangerPill: Color @Composable get() = LocalAppColors.current.dangerPill

val AppInfo: Color @Composable get() = LocalAppColors.current.info
val AppPurple: Color @Composable get() = LocalAppColors.current.purple
val AppWarning: Color @Composable get() = LocalAppColors.current.warning

val AppGoldFrom: Color @Composable get() = LocalAppColors.current.goldFrom
val AppGoldTo: Color @Composable get() = LocalAppColors.current.goldTo
val AppGoldBorder: Color @Composable get() = LocalAppColors.current.goldBorder
val AppGoldInk: Color @Composable get() = LocalAppColors.current.goldInk
val AppGoldInk2: Color @Composable get() = LocalAppColors.current.goldInk2

val AppDisabledFill: Color @Composable get() = LocalAppColors.current.disabledFill
val AppDisabledText: Color @Composable get() = LocalAppColors.current.disabledText
val AppShadowNeutral: Color @Composable get() = LocalAppColors.current.shadowNeutral

val AppIsDark: Boolean @Composable get() = LocalAppColors.current.isDark

/**
 * طلاییِ «پرمیوم/اشتراک». تو کلِ اپ (بج‌های اشتراک، `GoldSheenBox`) به این اسم صدا زده می‌شه.
 * تو سبکِ جدید همون جوهرِ کاغذِ طلاییه.
 *
 * ⚠️ قاعده‌ی ماندگار: **طلایی فقط نشانه‌ی پرمیوم/اشتراکه** - رو حاشیه‌ی دکمه‌های معمولی نه.
 */
val AppAccent: Color @Composable get() = LocalAppColors.current.goldInk

// ── سازگاریِ عقب‌رو - رجوع کن به کامنتِ [AppColorPalette.glassBase] ─────────────────
@Deprecated("سبکِ شیشه‌ای حذف شد. به‌جاش AppSurface.", ReplaceWith("AppSurface"))
val AppGlassBase: Color @Composable get() = LocalAppColors.current.glassBase

@Deprecated("سبکِ شیشه‌ای حذف شد - همیشه شفافه.", ReplaceWith("Color.Transparent"))
val AppGlassGradientStart: Color @Composable get() = LocalAppColors.current.glassGradientStart

@Deprecated("سبکِ شیشه‌ای حذف شد - همیشه شفافه.", ReplaceWith("Color.Transparent"))
val AppGlassGradientEnd: Color @Composable get() = LocalAppColors.current.glassGradientEnd

@Deprecated("سبکِ شیشه‌ای حذف شد. به‌جاش AppLine.", ReplaceWith("AppLine"))
val AppGlassBorder: Color @Composable get() = LocalAppColors.current.glassBorder

@Deprecated("سبکِ شیشه‌ای حذف شد - همیشه شفافه.", ReplaceWith("Color.Transparent"))
val AppGlassHighlight: Color @Composable get() = LocalAppColors.current.glassHighlight
