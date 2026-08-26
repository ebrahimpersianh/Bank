package ir.sadteam.loancalc.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * **امضاهای بصریِ برند** - بخشِ ۳۴ فایلِ طراحی (کارت‌های `34a`..`34c`).
 *
 * قاعده‌ی صریحِ `34c` («سه امضا، سه کار») که این فایل نگهبانشه:
 * - **سکه** واحدِ پاداشه - هرجا امتیاز و جایزه هست.
 * - **زنجیر** پیوستگیِ عادته - هرجا روزهای پشتِ‌سرهم شمرده می‌شه.
 * - **ریبون** اصالتِ عدده - هرجا رقمی از خرجِ واقعی اومده.
 *
 * ⚠️ «هیچ‌کدام برای زیبایی تکرار نمی‌شوند. اگر معنایی که پشتشان است در آن صفحه نیست، نشانه هم
 * نباید باشد.» - قبل از گذاشتنِ هرکدوم تو یه صفحه‌ی تازه، این جمله رو دوباره بخون.
 */

/** پالتِ طلاییِ سکه، عیناً از فایلِ «آیکونِ سکه» (گونه‌ی «آ»). */
private val CoinEdge = Color(0xFFB07E0C)
private val CoinFace = Color(0xFFD9A32C)
private val CoinFaceLight = Color(0xFFF0C356)

/**
 * **آیکونِ سکه** - گونه‌ی **«آ · تخت با حاشیه»**، انتخابِ صریحِ کاربر از بینِ چهار پیشنهادِ
 * `design/آیکونِ سکه.dc.html` («طرح یک، برای کلِ برنامه»).
 *
 * مقادیر عیناً از همون فایل، رو ویوباکسِ ۳۲×۳۲:
 * - حاشیه‌ی ضخیمِ تیره: دایره‌ی `r=15` با `#B07E0C`
 * - صورتِ صاف: دایره‌ی `r=12.6` با `#F0C356`
 * - حلقه‌ی نازکِ داخلی: `r=9.4`، خطِ `#D9A32C` به ضخامتِ ۱٫۵
 *
 * ⚠️ **زیرِ ۲۰ پیکسل حلقه‌ی داخلی حذف می‌شه** و صورت به `r=11.4` بزرگ می‌شه - قاعده‌ی صریحِ
 * همون فایل: «در ۱۳px حلقه و حرف حذف می‌شوند و فقط دو دایره می‌ماند».
 *
 * ⚠️ **صورتِ سکه خالیه** - حرف و عدد روش نمی‌ره، چون عددش همیشه کنارشه.
 */
@Composable
fun CoinIcon(size: Dp, modifier: Modifier = Modifier) {
    val small = size.value < 20f
    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = this.size.minDimension / 2f
            val c = Offset(this.size.width / 2f, this.size.height / 2f)
            // نسبت‌ها بر پایه‌ی شعاعِ ۱۶ واحدیِ ویوباکس
            fun k(v: Float) = r * v / 16f

            drawCircle(color = CoinEdge, radius = k(15f), center = c)
            drawCircle(color = CoinFaceLight, radius = k(if (small) 11.4f else 12.6f), center = c)
            if (!small) {
                drawCircle(
                    color = CoinFace,
                    radius = k(9.4f),
                    center = c,
                    style = Stroke(width = k(1.5f)),
                )
            }
        }
    }
}

/**
 * **زنجیرِ «فعال»** - کارتِ `34a`.
 *
 * ⚠️ واژه‌ی «استریک» عمداً به کار نرفته؛ خواسته‌ی صریحِ کاربر «فعال»ه.
 *
 * قاعده‌های صریحِ `34a`/`34c` که اینجا پیاده شدن:
 * - حلقه‌ها با **بندِ ۹×۵** به هم وصل‌ان «پس چشم آن را یک رشته می‌بیند نه هفت نقطه».
 * - حلقه‌ی **امروز بزرگ‌تره** (۲۸ در برابرِ ۲۴) و **هاله** داره.
 * - روزِ **جاافتاده** حلقه‌اش خط‌چینِ قرمز می‌شه و بندش پاره؛ حلقه‌های بعدش خاکستریِ توخالی.
 *   «عدد لازم نیست، شکل خودش می‌گوید.»
 *
 * @param filled چند روزِ اولِ [total] کامل شده.
 * @param brokenAt اگه non-null باشه، همون اندیس (۰-پایه) روزِ جاافتاده‌ست.
 */
@Composable
fun ActiveChainMark(
    filled: Int,
    modifier: Modifier = Modifier,
    total: Int = 7,
    ringSize: Dp = 24.dp,
    brokenAt: Int? = null,
    ringColor: Color = Color(0xFF0EA968),
    mutedColor: Color = Color(0xFFC9D6CF),
    brokenColor: Color = Color(0xFFFF4B4B),
) {
    val todaySize = ringSize * (28f / 24f)
    val bandW = ringSize * (9f / 24f)
    val bandH = ringSize * (5f / 24f)
    Row(
        modifier = modifier.height(todaySize * 1.5f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { i ->
            if (i > 0) {
                val bandBroken = brokenAt != null && (i == brokenAt || i == brokenAt + 1)
                val bandColor = when {
                    bandBroken -> brokenColor
                    i < filled -> ringColor
                    else -> mutedColor
                }
                Canvas(modifier = Modifier.width(bandW).height(bandH)) {
                    if (!bandBroken) {
                        drawRoundRect(
                            color = bandColor,
                            cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
                        )
                    } else {
                        // بندِ پاره: دو تکه‌ی کوتاه و کج (قاعده‌ی صریحِ `34c`).
                        val piece = size.width * 0.38f
                        drawRoundRect(
                            color = bandColor,
                            topLeft = Offset(0f, size.height * 0.15f),
                            size = Size(piece, size.height * 0.7f),
                            cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
                        )
                        drawRoundRect(
                            color = bandColor,
                            topLeft = Offset(size.width - piece, size.height * 0.15f),
                            size = Size(piece, size.height * 0.7f),
                            cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
                        )
                    }
                }
            }
            val isToday = i == filled - 1
            val isBroken = i == brokenAt
            val done = i < filled && !isBroken
            Box(
                modifier = Modifier.size(if (isToday) todaySize * 1.5f else ringSize * 1.2f),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(if (isToday) todaySize else ringSize)) {
                    val r = size.minDimension / 2f
                    val c = Offset(size.width / 2f, size.height / 2f)
                    when {
                        isBroken -> drawCircle(
                            color = brokenColor,
                            radius = r - 1.5f,
                            center = c,
                            style = Stroke(
                                width = 3f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f),
                            ),
                        )
                        done -> {
                            if (isToday) {
                                // هاله‌ی حلقه‌ی امروز: `0 0 0 5.5px rgba(14,169,104,.34)`.
                                drawCircle(
                                    color = ringColor.copy(alpha = 0.34f),
                                    radius = r + 5.5f,
                                    center = c,
                                )
                            }
                            drawCircle(color = ringColor, radius = r, center = c)
                        }
                        else -> drawCircle(
                            color = mutedColor,
                            radius = r - 1.5f,
                            center = c,
                            style = Stroke(width = 3f),
                        )
                    }
                }
            }
        }
    }
}

/**
 * **ریبونِ رسید** - کارتِ `34b`.
 *
 * قاعده‌های صریح:
 * - دندانه **فقط پایینِ کارت** میاد، «هرگز بالا و کنار — تا حسِ کاغذِ بریده‌شده بماند».
 * - دندانه‌ها **۱۴px پهن و ۱۰px بلند**ن و «در هر عرضی همین اندازه می‌مانند» (پس تعدادشون با
 *   عرض عوض می‌شه، نه اندازه‌شون).
 * - گوشه‌های **بالای** کارت ۱۸، پایین صفر.
 * - «در کامپوز یک `Shape` سفارشی که `Path` دندانه را می‌سازد، تا سایه و `clip` هم درست کار
 *   کند — نه تصویر و نه `nine-patch`.»
 *
 * ⚠️ «حداکثر یک ریبون در هر صفحه‌ی دیده‌شده» و «دندانه روی دکمه، نوارِ پایین، ورودی و کارتِ
 * حساب نمی‌آید».
 */
object ReceiptRibbonShape : Shape {
    private const val NOTCH_W_DP = 14f
    private const val NOTCH_H_DP = 10f
    private const val TOP_RADIUS_DP = 18f

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val notchW = with(density) { NOTCH_W_DP.dp.toPx() }
        val notchH = with(density) { NOTCH_H_DP.dp.toPx() }
        val topR = with(density) { TOP_RADIUS_DP.dp.toPx() }.coerceAtMost(size.width / 2f)
        val bodyBottom = size.height - notchH

        val path = Path().apply {
            moveTo(0f, topR)
            arcTo(Rect(0f, 0f, topR * 2f, topR * 2f), 180f, 90f, false)
            lineTo(size.width - topR, 0f)
            arcTo(Rect(size.width - topR * 2f, 0f, size.width, topR * 2f), 270f, 90f, false)
            lineTo(size.width, bodyBottom)
            // دندانه‌های مثلثی - از راست به چپ، با اندازه‌ی ثابت؛ تکه‌ی باقی‌مانده صاف می‌مونه.
            val count = (size.width / notchW).toInt()
            var x = size.width
            repeat(count) {
                lineTo(x - notchW / 2f, size.height)
                lineTo(x - notchW, bodyBottom)
                x -= notchW
            }
            lineTo(0f, bodyBottom)
            lineTo(0f, topR)
            close()
        }
        return Outline.Generic(path)
    }
}
