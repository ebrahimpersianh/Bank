package ir.sadteam.loancalc.ui.jibak

/**
 * کامپوننت‌های جیبک — بخشِ ۳ تا ۹ فایلِ سیستمِ طراحی.
 * صفحه‌ها فقط از اینها ساخته می‌شوند. اگر چیزی اینجا نیست، اول اینجا اضافه شود.
 */

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ------------------------------------------------- سایه‌ی سخت */

/**
 * معادلِ CSS `box-shadow: 0 Ypx 0 color`.
 * سایه‌ی سخت است: بدون blur، فقط افستِ عمودی، هم‌رنگِ تیره‌ترِ خودِ عنصر.
 * از Modifier.shadow() استفاده نکن — آن blur دارد و طرح را عوض می‌کند.
 */
fun Modifier.hardShadow(color: Color, offsetY: Dp, radius: Dp): Modifier = drawBehind {
    val y = offsetY.toPx()
    val r = radius.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(0f, y),
        size = Size(size.width, size.height),
        cornerRadius = CornerRadius(r, r),
    )
}

/* ------------------------------------------------------- کارت‌ها */

enum class JibakCardStyle { Default, Gold, Urgent, Done, Raised }

/**
 * کارتِ استاندارد. حاشیه‌ی ۲dp، گردیِ ۲۰، درونش ۱۴–۱۶.
 * Urgent: حداکثر یکی در هر صفحه، بالای فهرست.
 * Done: شفافیتِ ۰٫۷۲، بدون سایه.
 */
@Composable
fun JibakCard(
    modifier: Modifier = Modifier,
    style: JibakCardStyle = JibakCardStyle.Default,
    padding: Dp = JibakSpace.cardInner,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = JibakTheme.colors
    val shape = RoundedCornerShape(JibakRadius.card)

    val bgModifier = when (style) {
        JibakCardStyle.Gold -> Modifier.background(
            Brush.linearGradient(listOf(c.goldTop, c.goldBottom)), shape
        )
        JibakCardStyle.Urgent -> Modifier.background(c.redBg, shape)
        JibakCardStyle.Done -> Modifier.background(c.cardBg2, shape)
        else -> Modifier.background(c.cardBg, shape)
    }
    val borderColor = when (style) {
        JibakCardStyle.Gold -> c.goldBorder
        JibakCardStyle.Urgent -> c.redBorder
        JibakCardStyle.Done -> c.rowBorder
        else -> c.cardBorder
    }
    val borderWidth = if (style == JibakCardStyle.Gold) 1.5.dp else 2.dp
    val shadow = when (style) {
        JibakCardStyle.Urgent -> c.redSoft to JibakElevation.raised
        JibakCardStyle.Raised -> c.neutralShadow to JibakElevation.neutral
        else -> null
    }

    Column(
        modifier = modifier
            .then(
                if (shadow != null) Modifier.hardShadow(shadow.first, shadow.second, JibakRadius.card)
                else Modifier
            )
            .then(bgModifier)
            .border(borderWidth, borderColor, shape)
            .padding(padding),
        content = content,
    )
}

/** کارتِ قهرمانِ سبز — گرادیانِ ۱۶۰deg + سایه‌ی سختِ ۵. */
@Composable
fun JibakHeroCard(
    modifier: Modifier = Modifier,
    padding: Dp = JibakSpace.cardInner,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = JibakTheme.colors
    val shape = RoundedCornerShape(JibakRadius.card)
    Column(
        modifier = modifier
            .hardShadow(c.greenGradShadow, JibakElevation.heroCard, JibakRadius.card)
            .background(
                Brush.linearGradient(listOf(c.greenGradTop, c.greenGradBottom)), shape
            )
            .clip(shape)
            .padding(padding),
        content = content,
    )
}

/* ------------------------------------------------------ دکمه‌ها */

enum class JibakButtonStyle { Primary, Secondary, Neutral, Destructive, Disabled }

/**
 * ارتفاعِ لمسیِ حداقل ۴۴. فشرده‌شدن: سایه از ۴ به ۱ می‌رود و محتوا ۳ پایین می‌آید،
 * پس ارتفاعِ کلِ دکمه ثابت می‌ماند.
 * اصلی: یکی در هر صفحه. مخرب: همیشه با تأییدِ دوم.
 */
@Composable
fun JibakButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: JibakButtonStyle = JibakButtonStyle.Primary,
    compact: Boolean = false,
) {
    val c = JibakTheme.colors
    val shape = RoundedCornerShape(JibakRadius.pill)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val hasShadow = style == JibakButtonStyle.Primary || style == JibakButtonStyle.Destructive
    val baseShadow = if (compact) JibakElevation.rowButton else JibakElevation.raised
    val shadowY by animateDpAsState(
        if (pressed && hasShadow) JibakElevation.pressed else baseShadow, label = "shadowY"
    )
    val sink = if (hasShadow) baseShadow - shadowY else 0.dp

    val bg = when (style) {
        JibakButtonStyle.Primary -> c.green
        JibakButtonStyle.Secondary -> c.cardBg
        JibakButtonStyle.Neutral -> c.chipBg
        JibakButtonStyle.Destructive -> c.red
        JibakButtonStyle.Disabled -> if (c.isDark) c.chipBg else Color(0xFFDDE7E2)
    }
    val fg = when (style) {
        JibakButtonStyle.Primary, JibakButtonStyle.Destructive -> Color.White
        JibakButtonStyle.Secondary -> c.greenShadow
        JibakButtonStyle.Neutral -> c.textSecondary
        JibakButtonStyle.Disabled -> c.textDisabled
    }
    val shadowColor = if (style == JibakButtonStyle.Destructive) c.redShadow else c.greenShadow

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = JibakSpace.minTouch)
            .then(if (hasShadow) Modifier.hardShadow(shadowColor, shadowY, JibakRadius.pill) else Modifier)
            .offset(y = sink)
            .background(bg, shape)
            .then(
                if (style == JibakButtonStyle.Secondary) Modifier.border(2.dp, c.green, shape)
                else Modifier
            )
            .clip(shape)
            .clickable(
                interactionSource = interaction,
                indication = rememberRipple(color = fg),
                enabled = style != JibakButtonStyle.Disabled,
                onClick = onClick,
            )
            .padding(
                horizontal = if (compact) 14.dp else 20.dp,
                vertical = if (compact) 9.dp else 15.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = if (compact) JibakTheme.type.chip else JibakTheme.type.button,
            color = fg,
            textAlign = TextAlign.Center,
        )
    }
}

/* -------------------------------------------------- چیپ و تب */

/** چیپ با سایه = قابلِ لمس. چیپِ بی‌سایه = فقط برچسب. */
@Composable
fun JibakChip(
    text: String,
    selected: Boolean,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val c = JibakTheme.colors
    val shape = RoundedCornerShape(JibakRadius.pill)
    Box(
        modifier = modifier
            .then(
                if (selected && onClick != null)
                    Modifier.hardShadow(c.greenShadow, JibakElevation.rowButton, JibakRadius.pill)
                else Modifier
            )
            .background(if (selected) c.green else c.chipBg, shape)
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 11.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            style = JibakTheme.type.chip,
            color = if (selected) Color.White else c.textSecondary,
        )
    }
}

/** تب‌ها بدون خطِ زیرین‌اند؛ تبِ فعال قرصِ پرشده است. */
@Composable
fun JibakTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        tabs.forEachIndexed { i, label ->
            if (i == selectedIndex) {
                JibakChip(label, selected = true, onClick = { onSelect(i) })
            } else {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(JibakRadius.pill))
                        .clickable { onSelect(i) }
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                ) {
                    Text(label, style = JibakTheme.type.chip, color = JibakTheme.colors.textSecondary)
                }
            }
        }
    }
}

/* --------------------------------------------- ردیفِ فهرست */

/**
 * آیکونِ ۳۲ در قابِ گردیِ ۱۰ با ته‌رنگِ دسته · عنوان ۱۲/۸۰۰ · فرادادهٔ ۹٫۵
 * · مبلغ چپ‌چین و ۹۰۰. حاشیه‌ی ردیف روشن‌تر از کارت است.
 */
@Composable
fun JibakListRow(
    title: String,
    meta: String,
    amount: String,
    amountColor: Color,
    iconTint: Color,
    iconBg: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    icon: @Composable () -> Unit,
) {
    val c = JibakTheme.colors
    val shape = RoundedCornerShape(JibakRadius.row)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = JibakSpace.minTouch)
            .background(c.cardBg, shape)
            .border(2.dp, c.rowBorder, shape)
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(
            Modifier
                .size(32.dp)
                .background(iconBg, RoundedCornerShape(JibakRadius.iconSmall)),
            contentAlignment = Alignment.Center,
        ) { icon() }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = JibakTheme.type.rowTitle, color = c.text)
            Text(meta, style = JibakTheme.type.meta, color = c.textSecondary)
        }
        Text(amount, style = JibakTheme.type.rowAmount, color = amountColor)
    }
}

/* -------------------------------------------- نشانگرها */

/** حلقه: ضخامتِ ۵ در ویوباکسِ ۴۰ (نسبتِ ۰٫۱۲۵ از قطر)، شروع از بالا. */
@Composable
fun JibakProgressRing(
    progress: Float,
    color: Color,
    trackColor: Color,
    size: Dp = 52.dp,
    strokeRatio: Float = 5f / 40f,
    content: @Composable () -> Unit = {},
) {
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = this.size.minDimension * strokeRatio
            val inset = stroke / 2f + (this.size.minDimension * (4f / 40f) - stroke / 2f)
            val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
            drawArc(
                color = trackColor, startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize,
                style = Stroke(width = stroke),
            )
            drawArc(
                color = color, startAngle = -90f, sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false, topLeft = Offset(inset, inset), size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        content()
    }
}

/** حلقه‌ی چند‌بخشی — سهمِ دسته‌ها. sweep‌ها پشتِ‌هم از بالا. */
@Composable
fun JibakSegmentRing(
    segments: List<Pair<Float, Color>>,
    trackColor: Color,
    size: Dp = 74.dp,
    strokeRatio: Float = 7f / 40f,
    content: @Composable () -> Unit = {},
) {
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = this.size.minDimension * strokeRatio
            val inset = this.size.minDimension * (4.5f / 40f)
            val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
            drawArc(
                color = trackColor, startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize, style = Stroke(width = stroke),
            )
            var start = -90f
            segments.forEach { (fraction, color) ->
                val sweep = 360f * fraction
                drawArc(
                    color = color, startAngle = start, sweepAngle = sweep, useCenter = false,
                    topLeft = Offset(inset, inset), size = arcSize, style = Stroke(width = stroke),
                )
                start += sweep
            }
        }
        content()
    }
}

/** نوارِ ۸ برای بودجه. رنگ از وضعیت می‌آید نه از دسته: ۷۰٪+ نارنجی، ۱۰۰٪+ قرمز. */
@Composable
fun JibakBudgetBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
) {
    val c = JibakTheme.colors
    val p = progress.coerceIn(0f, 1f)
    val fill = when {
        progress >= 1f -> c.red
        progress >= 0.7f -> c.orange
        else -> c.green
    }
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .background(c.rowBorder, RoundedCornerShape(JibakRadius.pill))
    ) {
        Box(
            Modifier
                .fillMaxWidth(p)
                .fillMaxHeight()
                .background(fill, RoundedCornerShape(JibakRadius.pill))
        )
    }
}

/** نوارِ ۱۲ با سایه‌ی درونی برای پیشرفتِ گام‌ها. */
@Composable
fun JibakStepBar(progress: Float, modifier: Modifier = Modifier) {
    val c = JibakTheme.colors
    Box(
        modifier
            .fillMaxWidth()
            .height(12.dp)
            .background(c.cardBorder, RoundedCornerShape(JibakRadius.pill))
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(c.green, RoundedCornerShape(JibakRadius.pill))
                .drawBehind {
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.12f),
                        topLeft = Offset(0f, size.height - 3.dp.toPx()),
                        size = Size(size.width, 3.dp.toPx()),
                    )
                }
        )
    }
}

/* -------------------------------------- نویگیشنِ پایین */

data class JibakTab(val label: String, val badge: Int = 0, val icon: @Composable (Color) -> Unit)

/**
 * پنج تب و فقط همین پنج: خانه، دارایی، گزارش، بودجه، سررسید.
 * تبِ فعال: قرصِ ۴۲×۲۸ گردیِ ۱۱ پشتِ آیکون + آیکونِ ضخیم‌تر + برچسبِ ۹۰۰ سبز.
 * تبِ غیرفعال همان جعبه‌ی ۴۲×۲۸ را بدونِ پس‌زمینه دارد، پس آیکون‌ها جابه‌جا نمی‌شوند.
 */
@Composable
fun JibakBottomNav(
    tabs: List<JibakTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = JibakTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(c.cardBg)
            .drawBehind {
                drawRect(
                    color = c.rowBorder,
                    size = Size(size.width, 2.dp.toPx()),
                )
            }
            .padding(top = 2.dp)
            .padding(horizontal = 6.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEachIndexed { i, tab ->
            val active = i == selectedIndex
            Column(
                modifier = Modifier
                    .widthIn(min = 52.dp)
                    .heightIn(min = JibakSpace.minTouch)
                    .clip(RoundedCornerShape(JibakRadius.tabDisc))
                    .clickable { onSelect(i) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    Modifier
                        .size(width = 42.dp, height = 28.dp)
                        .then(
                            if (active) Modifier.background(c.greenLight, RoundedCornerShape(JibakRadius.tabDisc))
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    tab.icon(if (active) c.green else c.textDisabled)
                    if (tab.badge > 0) {
                        Box(
                            Modifier
                                .align(Alignment.TopStart)
                                .offset(x = 8.dp, y = (-2).dp)
                                .size(15.dp)
                                .background(c.red, RoundedCornerShape(JibakRadius.pill)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                tab.badge.toFa(),
                                style = JibakTheme.type.micro.copy(fontSize = 8.sp),
                                color = Color.White,
                            )
                        }
                    }
                }
                Text(
                    tab.label,
                    style = if (active) JibakTheme.type.tabLabel
                    else JibakTheme.type.tabLabel.copy(fontWeight = FontWeight.W700),
                    color = if (active) c.green else c.textDisabled,
                )
            }
        }
    }
}
