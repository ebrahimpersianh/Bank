package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppElevation
import ir.sadteam.loancalc.ui.theme.AppIsDark
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryDim
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppSpacing
import ir.sadteam.loancalc.ui.theme.LocalAppColors
import ir.sadteam.loancalc.ui.theme.hardShadow

/**
 * لهجه‌ی رنگیِ کارتِ قهرمان. **این‌ها حدسی نیستن** - هر کدوم از کارتِ همون تب تو فایلِ طراحی
 * برداشته شده، و عمداً همه سبز نیستن (تبِ گزارش/آمار بنفشه، طبقِ توکنِ «بنفش - بودجه و آمار»).
 */
enum class HeroTone(
    internal val from: Long,
    internal val to: Long,
    internal val shadow: Long,
) {
    /** خانه (`15a`)، دارایی (`26b`)، بودجه (`27c`). */
    GREEN(0xFF0EA968, 0xFF0B8C57, 0xFF096F45),

    /** گزارش و آمار (`26a`). */
    PURPLE(0xFFA56EFF, 0xFF7440C9, 0xFF5C2FA8),

    /** اطلاع/انتقال - فعلاً جایی استفاده نشده، برای صفحاتِ بعدیِ همین بازطراحی. */
    BLUE(0xFF1CB0F6, 0xFF0E86C0, 0xFF0A6795),

    /**
     * سررسید وقتی موردِ **عقب‌افتاده** دارد (`51a`). ⚠️ تنها مصرفِ مجازش همین است - هیرویِ
     * قرمزِ همیشگی به کاربری که همه‌چیز را پرداخت کرده هم حسِ بدهی می‌دهد، پس در نبودِ
     * عقب‌افتاده همان کارت [GREEN] می‌شود.
     */
    RED(0xFFFF4B4B, 0xFFD93838, 0xFFB02A2A),
}

/**
 * کارتِ **قهرمانِ** بالای صفحه - **بازطراحیِ سبکِ «جیبک»**.
 *
 * تو دورِ قبل هر صفحه این کارت رو با `AppCard(accentGradient = Brush.linearGradient(...))` و یه
 * گرادیانِ سبزِ **نیمه‌شفاف** می‌ساخت (پنج جای اپ، پنج‌بار همون دو خط تکرار شده بود). سبکِ جدید
 * به‌جاش یه **کارتِ کاملاً ماتِ رنگی با متنِ سفید** می‌خواد، پس اینجا یه‌بار تعریف شده:
 * - پرشدنِ گرادیانِ **مات** طبقِ [tone]
 * - سایه‌ی سختِ `0 5px 0` هم‌رنگِ تیره‌ترِ خودش (نه سایه‌ی تارِ Material)
 * - `LocalContentColor` سفید، پس هر `Text`ِ بدونِ `color`ِ صریح خودبه‌خود سفید می‌شه
 *
 * ⚠️ قاعده‌ی «حداکثر **یک** رنگِ لهجه در هر صفحه» - تو هر صفحه فقط **یکی** از این‌ها بذار.
 *
 * ⚠️ متن‌های داخلش نباید رنگِ تم‌آگاه (`AppText`/`AppMuted`) بگیرن - رو زمینه‌ی رنگیِ تیره نامرئی
 * می‌شن. به‌جاش سفید (پیش‌فرض) یا [HeroMuted].
 */
@Composable
fun AppHeroCard(
    modifier: Modifier = Modifier,
    tone: HeroTone = HeroTone.GREEN,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(AppRadius.card)
    // 🎨 **تصمیمِ تاییدشده**: فقط لحنِ **سبز** به تمِ رنگیِ خریدنی گره می‌خوره؛ بنفش و آبی
    // سرِ جاشون می‌مونن. دلیلش: سبز در واقع رنگِ **برند**ه، ولی بنفش معنی حمل می‌کنه
    // («بنفش = گزارش و آمار» تو سیستمِ طراحی). اگه تم همه‌ی لحن‌ها رو عوض کنه اون معنی می‌پره.
    //
    // ⚠️ عارضه‌ی پذیرفته‌شده: با تمِ **بنفش**، کارتِ خانه و گزارش هم‌رنگ می‌شن. چون عنوان و
    // محتوا و تبِ فعالشون فرق داره گیج‌کننده نیست - و جایگزینش (سبز موندنِ خانه وقتی کلِ اپ
    // آبی شده) خیلی بدتره.
    val colorTheme = LocalAppColors.current
    val from = if (tone == HeroTone.GREEN) colorTheme.primary else Color(tone.from)
    val to = if (tone == HeroTone.GREEN) colorTheme.primaryDim else Color(tone.to)
    val shadow = if (tone == HeroTone.GREEN) colorTheme.heroShadow else Color(tone.shadow)
    // ⚠️ گرادیان **۱۶۰ درجه**ست نه عمودی (اصلاحیه‌ی طراح؛ دورِ قبل عمودی گفته بود و اشتباه
    // بود). نسخه‌ی بنفشِ فریمِ `26a` هم همینه، پس هر دو گونه یه جهت دارن. جهتش مهمه نه
    // عددِ دقیقش: از بالا-راست به پایین-چپ.
    val gradient = Brush.linearGradient(
        colors = listOf(from, to),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .hardShadow(shadow, 5.dp, AppRadius.card)
            .clip(shape)
            .background(gradient)
            // 🌿 **نقشِ برگِ گوشه** (خواسته‌ی کاربر با طرحِ مرجع، ۳۱ شهریور).
            //
            // با `drawBehind` **زیرِ محتوا** کشیده می‌شود و هیچ فضایی نمی‌گیرد، پس چیدمانِ
            // هیچ کارتی عوض نمی‌شود. گوشه‌ی بالا-چپ انتخاب شد نه راست: برنامه راست‌به‌چپ
            // است و راستِ کارت جای برچسب و عدد است.
            //
            // ⚠️ **سفیدِ کم‌رنگ، نه یک رنگِ ثابت** - همین یک تصمیم کاری می‌کند که نقش با
            // **هر تمی** جور دربیاید (خواسته‌ی دومِ همان پیام): روی سبز، بنفش، لاجورد یا
            // هر تمِ خریدنیِ بعدی، رنگش از خودِ زمینه می‌آید. یک هگزِ ثابت روی نیمی از
            // تم‌ها لکه می‌شد.
            .drawBehind { drawHeroLeaves(bothSides = true) }
            .padding(AppSpacing.cardPadding),
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            content()
        }
    }
}

/**
 * دو برگِ هم‌پوشان در گوشه‌ی بالا-چپِ کارتِ قهرمان.
 *
 * تصویر نیست، مسیرِ برداری است: هیچ فایلی به بسته اضافه نمی‌کند، در هر اندازه‌ی صفحه
 * تمیز می‌مانَد و رنگش از زمینه‌ی همان کارت می‌آید.
 *
 * ⚠️ شفافیتِ ۰٫۰۹ و ۰٫۰۶ عمدی است: نقش باید **حس** شود نه دیده؛ پررنگ‌تر از این با
 * عددِ قهرمان رقابت می‌کند - همان دلیلی که کارتِ تزئینیِ شلوغ در این بازطراحی حذف شد.
 */
fun DrawScope.drawHeroLeaves(bothSides: Boolean = false) {
    val unit = size.minDimension
    // برگِ بزرگ‌تر، کمی بیرون از کادر می‌نشیند تا «بریده از لبه» دیده شود نه «چسبانده».
    drawLeaf(
        center = Offset(unit * 0.10f, unit * 0.06f),
        length = unit * 0.62f,
        width = unit * 0.26f,
        rotationDeg = 32f,
        color = Color.White.copy(alpha = 0.09f),
    )
    drawLeaf(
        center = Offset(unit * 0.02f, unit * 0.30f),
        length = unit * 0.46f,
        width = unit * 0.19f,
        rotationDeg = -14f,
        color = Color.White.copy(alpha = 0.06f),
    )
    if (!bothSides) return
    // 🚨 **قرینه‌ی سمتِ راست** (خواسته‌ی کاربر، ۳۱ شهریور: «قرار بود سمت راست هم باشد»).
    // کم‌رنگ‌تر از چپ است چون سمتِ راست جای برچسب و عدد است و نقش نباید پشتِ متن
    // پررنگ شود؛ در تمِ راست‌به‌چپ چشم از همان‌جا شروع می‌کند.
    drawLeaf(
        center = Offset(size.width - unit * 0.08f, unit * 0.10f),
        length = unit * 0.50f,
        width = unit * 0.21f,
        rotationDeg = -28f,
        color = Color.White.copy(alpha = 0.06f),
    )
    drawLeaf(
        center = Offset(size.width - unit * 0.01f, unit * 0.34f),
        length = unit * 0.36f,
        width = unit * 0.15f,
        rotationDeg = 12f,
        color = Color.White.copy(alpha = 0.04f),
    )
}

/** یک برگ: دو کمانِ قرینه از نوک تا نوک. */
private fun DrawScope.drawLeaf(
    center: Offset,
    length: Float,
    width: Float,
    rotationDeg: Float,
    color: Color,
) {
    val path = Path().apply {
        moveTo(0f, 0f)
        quadraticBezierTo(width, length * 0.30f, 0f, length)
        quadraticBezierTo(-width, length * 0.30f, 0f, 0f)
        close()
    }
    withTransform({
        translate(center.x, center.y)
        rotate(rotationDeg, Offset.Zero)
    }) {
        drawPath(path, color)
    }
}

/** متنِ کم‌رنگ‌ترِ رو کارتِ قهرمان - `rgba(255,255,255,.78)` طبقِ طرح. */
val HeroMuted: Color = Color.White.copy(alpha = 0.78f)

/**
 * سبزِ درآمد و قرمزِ خرج **روی زمینه‌ی تیره‌ی کارتِ قهرمان**.
 *
 * ⚠️ عمداً `AppPrimary`/`AppDanger` نیستند: آن‌ها برای سطحِ روشن ساخته شده‌اند و روی این
 * زمینه کم‌کنتراست می‌شوند. این دو روشن‌ترِ همان دو معنی‌اند و **با تمِ خریدنی نمی‌چرخند**،
 * چون معنایی‌اند نه برندی - همان قاعده‌ی `ThemePalette` که سبزِ درآمد و قرمزِ خرج را از
 * پالت بیرون گذاشت.
 */
val HeroIncome: Color = Color(0xFF6EE7A8)
val HeroExpense: Color = Color(0xFFFF9A9A)

/** ته‌رنگِ قرصِ نیمه‌شفافِ رو کارتِ قهرمان - `rgba(255,255,255,.2)` طبقِ طرح. */
val HeroPillBg: Color = Color.White.copy(alpha = 0.20f)

/** سایه‌ی سختِ کارتِ قهرمان - `#096F45`، تیره‌ترِ همون سبز (نه خاکستریِ عمومی). */
private val HeroShadowColor = Color(0xFF096F45)

/**
 * قرصِ کوچکِ نیمه‌شفافِ رو کارتِ قهرمان - `rgba(255,255,255,.2)`، متنِ ۹/۹۰۰ سفید، پدینگِ ۵×۱۰،
 * کپسولِ کامل. طبقِ قرص‌های تفکیکِ کارتِ `26b` («نقد ۱۶٫۷M»، «طلا ۹۳٫۹M»...).
 */
@Composable
fun HeroSmallPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(AppRadius.button))
            .background(HeroPillBg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(text, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

/**
 * **ردیفِ برجسته‌ی گرادیانی** - کارتِ اشتراکِ فریمِ `27d`.
 *
 * عمداً [AppHeroCard] نیست: سایه‌اش ۴ه نه ۵ و پدینگش ۱۴ نه ۱۶ - یعنی «ردیفِ برجسته»ست،
 * نه بلوکِ آماری. طراح صریحاً خواست دو تا جدا بمونن تا با هم قاطی نشن.
 */
@Composable
fun AppHeroRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(AppRadius.card)
    val gradient = Brush.linearGradient(
        colors = listOf(AppPrimary, HeroRowEnd),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .hardShadow(if (AppIsDark) HeroRowShadowDark else HeroRowShadowLight, AppElevation.raised, AppRadius.card)
            .clip(shape)
            .background(gradient)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                Text(
                    subtitle,
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Box(modifier = Modifier.height(44.dp), contentAlignment = Alignment.CenterStart) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White)
                    .pressScaleClickable(onClick = onAction)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            ) {
                Text(actionLabel, color = AppPrimaryInk, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

/** پایانِ گرادیانِ ردیفِ برجسته و سایه‌ی سختش - مقادیرِ صریحِ فریم. */
private val HeroRowEnd = Color(0xFF0B8C57)
private val HeroRowShadowLight = Color(0xFF096F45)
private val HeroRowShadowDark = Color(0xFF07724A)
