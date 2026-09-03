package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * **لوگوی جیبک، نسخه‌ی ۴** - فریمِ `24a`/`24b`ی فایلِ طراحی.
 *
 * سه حجم رو هم می‌شینن تا سیلوئت حتی تو ۲۸dp هم خونا بمونه (بندِ ۷۸ فایلِ طراحی):
 * ۱) بدنه‌ی روشنِ کیف · ۲) درِ سبزِ میانی · ۳) سکه‌ی طلایی که **نصفش پشتِ در** می‌ره
 * (بندِ ۷۷ - «حسِ داخل رفتن، نه چسبیده به گوشه»).
 *
 * همه‌ی مختصات نسبت به یه جعبه‌ی مرجعِ **۹۶×۷۶** حساب می‌شن، پس با هر [width] درست درمیاد.
 *
 * ══════ سه اصلاحِ هندسی (بازبینیِ لوگو) ══════
 *
 * **الف · سکه واقعاً نصف نبود.** درِ کیف تا `y=43` می‌آمد و سکه مرکزش `y=35.5` بود با شعاعِ
 * ۱۹٫۵ - یعنی از ۳۹ واحد قطرِ سکه، ۲۷ واحدش (۷۰٪) زیرِ در می‌رفت و فقط یه هلالِ نازکِ طلایی
 * پیدا بود. بندِ ۷۷ «نصف» می‌گوید. الان: درِ کیف تا `y=38`، سکه مرکز `y=39` شعاع ۱۹ - سکه
 * از ۲۰ تا ۵۸ کشیده می‌شود و در، ۱۸ واحدِ بالایش را می‌پوشاند. **دقیقاً نصف.**
 *
 * **ب · زبانه‌ی قفل حذف شد.** در هر چهار اندازه‌ی آزمایش (۹۶/۵۶/۴۰/۲۸) زبانه دقیقاً روی نیمه‌ی
 * پیدای سکه می‌نشست و - چون هم‌رنگِ خودِ در بود - طلا را به یک هلالِ باریکِ زیرِ یک لکه‌ی سبز
 * تبدیل می‌کرد. سکه عنصرِ برند است و زبانه تزئین بود؛ در تضاد، تزئین می‌رود. (بندِ ۷۹ زبانه را
 * «جزئیاتِ سطحِ اول» خوانده بود که در اندازه‌ی ریز حذف می‌شود - ولی مشکل در اندازه‌ی **بزرگ**
 * هم بود.) رنگِ `LockDeep` هم با آن رفت.
 *
 * **پ · آستانه‌ی «ریز» پیکسل بود، نه dp.** `w < 60f` روی `size.width`ی بوم حساب می‌شد که
 * **پیکسلِ فیزیکی** است: روی گوشیِ ۳x یک لوگوی ۲۸dp می‌شود ۸۴px، پس شرط هیچ‌وقت برقرار
 * نمی‌شد و درزِ خط‌چین در آیکونِ ریز هم کشیده می‌شد (یک لکه‌ی خاکستریِ بی‌معنا). الان
 * `34.dp.toPx()`.
 */
@Composable
fun JibakLogo(width: Dp, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(width, width * 76f / 96f)) {
        Canvas(modifier = Modifier.fillMaxSize()) { drawJibakWallet() }
    }
}

/**
 * کیفِ لوگو رو با عرضِ [w] و از نقطه‌ی [origin] می‌کشه - جعبه‌ی مرجع **۹۶×۷۶**.
 * عمداً پارامتری، نه وابسته به `size`ی بوم، تا هم تنها و هم داخلِ کاشی یکسان دربیاد.
 */
fun DrawScope.drawJibakWallet(w: Float = size.width, origin: Offset = Offset.Zero) {
    val k = w / 96f
    fun u(v: Float) = v * k
    fun p(x: Float, y: Float) = Offset(origin.x + x, origin.y + y)
    // اندازه‌ی ریز: درزِ خط‌چین حذف می‌شه. آستانه در **dp** است نه پیکسل (رجوع کن به بندِ «پ»).
    val tiny = w < 34.dp.toPx()

    // ۱ · بدنه‌ی روشن - گوشه‌ی بالا ۱۳، پایین ۲۵
    drawPath(
        roundedPath(
            left = origin.x,
            top = origin.y + u(21f),
            right = origin.x + u(96f),
            bottom = origin.y + u(76f),
            topRadius = u(13f),
            bottomRadius = u(25f),
        ),
        brush = Brush.linearGradient(
            colors = listOf(BodyLight, BodyMid, BodyDeep),
            start = p(u(96f) * 0.2f, u(21f)),
            end = p(u(96f) * 0.8f, u(76f)),
        ),
    )

    // ۲ · درزِ خط‌چینِ بدنه
    if (!tiny) {
        val y = u(66f)
        drawLine(
            color = SeamInk,
            start = p(u(12f), y),
            end = p(u(84f), y),
            strokeWidth = u(2f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(u(5f), u(4f)), 0f),
        )
    }

    // ۳ · سکه‌ی طلایی - مرکزش **روی لبه‌ی پایینِ در** می‌نشیند، پس دقیقاً نصفش پیداست.
    // `coinY == flapBottom + 1` رابطه‌ی کلیدی است؛ اگر یکی را عوض کردی، آن یکی را هم عوض کن.
    val coinCenter = p(u(48f), u(39f))
    drawCircle(color = CoinEdgeDeep, radius = u(19f), center = coinCenter)
    drawCircle(color = CoinRing, radius = u(17f), center = coinCenter)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(CoinShine, CoinFace, CoinFaceDeep),
            center = Offset(coinCenter.x - u(4f), coinCenter.y - u(5f)),
            radius = u(18f),
        ),
        radius = u(15f),
        center = coinCenter,
    )

    // ۴ · درِ سبزِ کیف - رو سکه می‌شینه و نیمه‌ی بالاییش رو می‌پوشونه.
    // ارتفاع از ۴۳ به **۳۸** کم شد و شعاعِ پایین از ۳۰ به ۲۶ - در با شعاعِ ۳۰ روی ارتفاعِ ۳۱
    // واحد تقریباً نیم‌دایره می‌شد و لبه‌ش با کمانِ سکه هم‌مرکز می‌افتاد، پس دو قوسِ هم‌شکلِ
    // چسبیده دیده می‌شد نه «در روی سکه».
    drawPath(
        roundedPath(
            left = origin.x,
            top = origin.y + u(7f),
            right = origin.x + u(96f),
            bottom = origin.y + u(38f),
            topRadius = u(14f),
            bottomRadius = u(26f),
        ),
        brush = Brush.linearGradient(
            colors = listOf(FlapLight, FlapMid, FlapDeep),
            start = p(u(96f) * 0.2f, u(7f)),
            end = p(u(96f) * 0.8f, u(38f)),
        ),
    )
}

/** مستطیلِ گردگوشه با شعاعِ متفاوت برای بالا و پایین - شکلِ بدنه و درِ کیف. */
private fun roundedPath(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    topRadius: Float,
    bottomRadius: Float,
): Path = Path().apply {
    addRoundRect(
        RoundRect(
            rect = androidx.compose.ui.geometry.Rect(left, top, right, bottom),
            topLeft = CornerRadius(topRadius, topRadius),
            topRight = CornerRadius(topRadius, topRadius),
            bottomRight = CornerRadius(bottomRadius, bottomRadius),
            bottomLeft = CornerRadius(bottomRadius, bottomRadius),
        ),
    )
}

/**
 * همون لوگو ولی **داخلِ کاشیِ سبز** - نسخه‌ی آیکونِ لانچر (فریمِ `24a`).
 * گوشه‌ی کاشی ۲۸٪ ضلع، و لوگو ۶۴٪ عرضِ کاشی.
 */
@Composable
fun JibakLogoTile(size: Dp, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = this.size.width * 0.28f
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(TileLight, TileMid, TileDeep),
                    start = Offset(this.size.width * 0.18f, 0f),
                    end = Offset(this.size.width * 0.82f, this.size.height),
                ),
                cornerRadius = CornerRadius(r, r),
            )
            // درخششِ ملایمِ بالای کاشی
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.20f), Color.Transparent),
                    endY = this.size.height * 0.5f,
                ),
                cornerRadius = CornerRadius(r, r),
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.28f),
                cornerRadius = CornerRadius(r, r),
                style = Stroke(width = this.size.width * 0.011f),
            )
            // لوگو وسطِ کاشی
            val w = this.size.width * 0.636f
            val h = w * 76f / 96f
            drawJibakWallet(
                w = w,
                origin = Offset((this.size.width - w) / 2f, (this.size.height - h) / 2f),
            )
        }
    }
}

// مقادیرِ صریحِ فریم - این‌ها هویتِ برندن، پس عمداً توکنِ تم نیستن و تو تمِ تیره هم عوض نمی‌شن.
private val TileLight = Color(0xFF1FD68C)
private val TileMid = Color(0xFF0EA968)
private val TileDeep = Color(0xFF087A4C)
private val BodyLight = Color(0xFFFFFFFF)
private val BodyMid = Color(0xFFE4F1E9)
private val BodyDeep = Color(0xFFCBDFD3)
private val SeamInk = Color(0x3D087A4C)
private val FlapLight = Color(0xFFA5DBBE)
private val FlapMid = Color(0xFF6BBE92)
private val FlapDeep = Color(0xFF42956C)
private val CoinRing = Color(0xFFF0C356)
private val CoinEdgeDeep = Color(0xFFB07E0C)
private val CoinShine = Color(0xFFFFFDF5)
private val CoinFace = Color(0xFFF9C042)
private val CoinFaceDeep = Color(0xFF8F5400)

/**
 * **نشانِ برند** - لوگو + واژه‌نشانِ «جیبک» زیرش، هم‌چیده.
 *
 * چرا این تابع اضافه شد: خواسته‌ی صریحِ کاربر «تمامیِ صفحه‌های ورود لوگو داشته باشند». تا الان
 * فقط [SplashIntroScreen] لوگو داشت و بعدش کاربر چهار صفحه‌ی پشتِ‌سرِ هم می‌دید (قفل، مجوز،
 * آنبوردینگ، ورود) که هیچ‌کدام نشانِ برند نداشتند - یعنی درست همان‌جا که کاربر باید بفهمد
 * «دارم به جیبک شماره‌ام را می‌دهم»، هیچ نشانه‌ای از جیبک نبود.
 *
 * تک‌جا بودنش هم عمدی است: چهار صفحه در سه پکیجِ مختلف (`onboarding`, `auth`, `security`) از
 * همین می‌خوانند، پس اندازه/فاصله/وزنِ واژه‌نشان یک‌جا عوض می‌شود.
 *
 * [tint] فقط برای زمینه‌ی سبزِ اسپلش لازم است (سفید)؛ باقیِ صفحه‌ها `null` می‌دهند تا
 * `AppText`ِ تم را بگیرد و در تمِ تیره هم درست بچرخد.
 *
 * [tagline] هم فقط اسپلش می‌دهد. باقیِ صفحه‌ها سربرگِ خودشان را دارند و شعار زیرِ نشان
 * دو عنوانِ پشتِ‌هم می‌شد.
 */
@Composable
fun JibakBrandMark(
    width: Dp = 56.dp,
    tint: Color? = null,
    showWordmark: Boolean = true,
    tagline: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        JibakLogo(width = width)
        if (showWordmark) {
            Text(
                "جیبک",
                color = tint ?: AppText,
                // نسبت به عرضِ لوگو، نه یه عددِ ثابت - تا در ۴۰dp و ۷۲dp هر دو متناسب بماند.
                fontSize = (width.value * 0.30f).sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        if (tagline != null) {
            Text(
                tagline,
                // شعار همیشه کم‌رنگ‌تر از واژه‌نشان است - روی سبز ۸۶٪ سفید، روی تم `AppMuted`.
                color = tint?.copy(alpha = 0.86f) ?: AppMuted,
                fontSize = (width.value * 0.135f).sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 7.dp),
            )
        }
    }
}
