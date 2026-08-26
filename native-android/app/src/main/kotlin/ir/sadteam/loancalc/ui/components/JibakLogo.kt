package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * **لوگوی جیبک، نسخه‌ی ۴** - فریمِ `24a`/`24b`ی فایلِ طراحی.
 *
 * سه حجم رو هم می‌شینن تا سیلوئت حتی تو ۲۸ پیکسل هم خونا بمونه (بندِ ۷۸ فایلِ طراحی):
 * ۱) بدنه‌ی روشنِ کیف · ۲) درِ سبزِ میانی · ۳) سکه‌ی طلایی که **نصفش پشتِ در** می‌ره
 * (بندِ ۷۷ - «حسِ داخل رفتن، نه چسبیده به گوشه»). زبانه‌ی قفلِ زیرِ در جزئیاتِ سطحِ اوله و
 * تو اندازه‌های ریز حذف می‌شه (بندِ ۷۹).
 *
 * همه‌ی مختصات نسبت به یه جعبه‌ی مرجعِ **۹۶×۷۶** حساب می‌شن، پس با هر [width] درست درمیاد.
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
    val tiny = w < 60f // اندازه‌ی ریز: زبانه‌ی قفل و درزِ خط‌چین حذف می‌شن

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

    // ۳ · سکه‌ی طلایی - مرکز، نصفش زیرِ درِ کیف می‌مونه
    val coinCenter = p(u(48f), u(35.5f))
    drawCircle(color = CoinEdgeDeep, radius = u(19.5f), center = coinCenter)
    drawCircle(color = CoinRing, radius = u(17.5f), center = coinCenter)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(CoinShine, CoinFace, CoinFaceDeep),
            center = Offset(coinCenter.x - u(4f), coinCenter.y - u(5f)),
            radius = u(18f),
        ),
        radius = u(15.5f),
        center = coinCenter,
    )

    // ۴ · درِ سبزِ کیف - رو سکه می‌شینه
    drawPath(
        roundedPath(
            left = origin.x,
            top = origin.y + u(7f),
            right = origin.x + u(96f),
            bottom = origin.y + u(43f),
            topRadius = u(14f),
            bottomRadius = u(30f),
        ),
        brush = Brush.linearGradient(
            colors = listOf(FlapLight, FlapMid, FlapDeep),
            start = p(u(96f) * 0.2f, u(7f)),
            end = p(u(96f) * 0.8f, u(43f)),
        ),
    )

    // ۵ · زبانه‌ی قفل
    if (!tiny) {
        drawPath(
            roundedPath(
                left = coinCenter.x - u(11.5f),
                top = origin.y + u(36f),
                right = coinCenter.x + u(11.5f),
                bottom = origin.y + u(49f),
                topRadius = 0f,
                bottomRadius = u(9f),
            ),
            brush = Brush.verticalGradient(
                colors = listOf(FlapDeep, LockDeep),
                startY = origin.y + u(36f),
                endY = origin.y + u(49f),
            ),
        )
    }
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
private val LockDeep = Color(0xFF2E7854)
private val CoinRing = Color(0xFFF0C356)
private val CoinEdgeDeep = Color(0xFFB07E0C)
private val CoinShine = Color(0xFFFFFDF5)
private val CoinFace = Color(0xFFF9C042)
private val CoinFaceDeep = Color(0xFF8F5400)
