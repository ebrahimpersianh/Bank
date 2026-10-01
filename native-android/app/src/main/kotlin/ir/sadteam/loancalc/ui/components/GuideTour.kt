package ir.sadteam.loancalc.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.ui.theme.AppElevation
import ir.sadteam.loancalc.ui.theme.AppLine
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppShadowNeutral
import ir.sadteam.loancalc.ui.theme.AppStroke
import ir.sadteam.loancalc.ui.theme.AppSurface
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.hardShadow

/**
 * 🧭 **راهنمای تعاملی روی خودِ برنامه** (۹ مهر، خواسته‌ی کاربر: «رو خودِ برنامه بگه رو فلان
 * دکمه بزن، بعد رو افزودنِ حساب بزن»). هر دکمه‌ی هدف با [guideTarget] جایش را ثبت می‌کند؛
 * راهنما رویش نور می‌اندازد و **منتظرِ کارِ واقعیِ کاربر** می‌ماند (لمس از سوراخِ نور رد می‌شود).
 */
object Guide {
    val bounds = mutableStateMapOf<String, Rect>()
}

/** این عنصر را برای راهنما قابلِ نشانه‌گیری می‌کند؛ وقتی از صفحه رفت، جایش هم پاک می‌شود. */
fun Modifier.guideTarget(key: String): Modifier = composed {
    DisposableEffect(key) { onDispose { Guide.bounds.remove(key) } }
    onGloballyPositioned { Guide.bounds[key] = it.boundsInRoot() }
}

/**
 * یک قدم. [target] = کلیدِ دکمه (یا `null` برای کارتِ توضیحیِ بی‌نشانه). قدمِ بی‌هدف یا قدمی که
 * هدفش فعلاً روی صفحه نیست فقط کارت نشان می‌دهد و جلوی کار را نمی‌گیرد.
 */
data class GuideStep(
    val target: String?,
    val title: String,
    val text: String,
    /** «بعداً» دارد (کارِ اختیاری). */
    val optional: Boolean = false,
    /** کارتِ توضیحی: دکمه‌ی «بعدی» دارد؛ قدمِ هدف‌دار با کارِ کاربر خودش جلو می‌رود. */
    val manual: Boolean = target == null,
)

@Composable
fun GuideOverlay(
    step: GuideStep,
    index: Int,
    total: Int,
    onNext: () -> Unit,
    onClose: () -> Unit,
) {
    val rect = step.target?.let { Guide.bounds[it] }
    val density = LocalDensity.current
    val primary = AppPrimary
    Box(Modifier.fillMaxSize()) {
        if (rect != null) {
            val hole = rect.inflate(with(density) { 8.dp.toPx() })
            // پرده‌ی تیره به‌جز سوراخ؛ چهار تکه‌ی لمس‌گیر دورِ سوراخ تا لمس فقط از خودِ دکمه رد شود.
            Canvas(Modifier.fillMaxSize()) {
                val scrim = Path().apply { addRect(Rect(Offset.Zero, size)) }
                val holePath = Path().apply { addRoundRect(RoundRect(hole, CornerRadius(24f, 24f))) }
                drawPath(Path().apply { op(scrim, holePath, PathOperation.Difference) }, Color.Black.copy(alpha = 0.66f))
                drawRoundRect(primary, hole.topLeft, hole.size, CornerRadius(24f, 24f), style = Stroke(width = 3.dp.toPx()))
            }
            BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.AbsoluteAlignment.TopLeft) {
                val w = constraints.maxWidth.toFloat()
                val h = constraints.maxHeight.toFloat()
                @Composable
                fun blocker(x: Float, y: Float, bw: Float, bh: Float) {
                    if (bw <= 0f || bh <= 0f) return
                    Box(
                        Modifier
                            .absoluteOffset { IntOffset(x.toInt(), y.toInt()) }
                            .size(with(density) { bw.toDp() }, with(density) { bh.toDp() })
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
                    )
                }
                blocker(0f, 0f, w, hole.top)
                blocker(0f, hole.bottom, w, h - hole.bottom)
                blocker(0f, hole.top, hole.left, hole.height)
                blocker(hole.right, hole.top, w - hole.right, hole.height)
            }
        }
        // کارت: اگر هدف پایینِ صفحه است بالا می‌نشیند، وگرنه پایین.
        val atTop = rect == null || rect.center.y > with(density) { 360.dp.toPx() }
        AnimatedContent(targetState = index, label = "guide", modifier = Modifier
            .align(if (atTop) Alignment.TopCenter else Alignment.BottomCenter)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = if (atTop) 12.dp else 110.dp)) { _ ->
            Surface(color = AppSurface, shape = RoundedCornerShape(AppRadius.card), modifier = Modifier.fillMaxWidth().hardShadow(AppShadowNeutral, AppElevation.neutral, AppRadius.card).border(AppStroke.card, AppLine, RoundedCornerShape(AppRadius.card))) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(26.dp).clip(CircleShape).background(primary), contentAlignment = Alignment.Center) {
                            Text(toFa(index + 1), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                        Text(step.title, color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 8.dp).weight(1f))
                        Text("${toFa(index + 1)} از ${toFa(total)}", color = AppMuted, fontSize = 11.sp)
                    }
                    Text(step.text, color = AppMuted, fontSize = 13.5.sp, lineHeight = 22.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onClose) { Text("بستنِ راهنما", color = AppMuted, fontSize = 12.5.sp) }
                        Spacer(Modifier.weight(1f))
                        if (step.optional) TextButton(onClick = onNext) { Text("بعداً", color = AppMuted, fontSize = 13.5.sp) }
                        if (step.manual) TextButton(onClick = onNext) {
                            Text(if (index == total - 1) "شروع کن" else "بعدی", color = primary, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        } else if (!step.optional) TextButton(onClick = onNext) { Text("رد شو", color = AppMuted, fontSize = 13.sp) }
                    }
                }
            }
        }
    }
}
