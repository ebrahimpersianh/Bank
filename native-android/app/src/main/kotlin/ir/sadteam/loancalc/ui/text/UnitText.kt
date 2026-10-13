package ir.sadteam.loancalc.ui.text

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.isSpecified
import androidx.compose.material3.Text as M3Text

/*
 * ۲۱ مهر، خواسته‌ی کاربر: «تومان با عدد فرقی داشته باشد که چشم سریع متوجه شود».
 * به‌جای دست‌بردن در ۲۰۰ جای مبلغ، همه‌ی فایل‌ها به‌جای `material3.Text` این `Text` را ایمپورت می‌کنند
 * (همان امضا، همان رفتار). اگر متن «عدد + (هزار|میلیون|میلیارد)؟ + تومان|ریال» داشته باشد، تکه‌ی واحد
 * کمی کوچک‌تر، کم‌رنگ‌تر و نازک‌تر کشیده می‌شود؛ وگرنه دقیقاً همان Textِ متریال است.
 * متنِ ریز (زیرِ ۱۳sp) دست نمی‌خورد تا جمله‌های کوچک ناخوانا نشوند. حروفِ زیرِ کادر («... هزار تومان»)
 * عدد ندارد، پس عوض نمی‌شود.
 */

private val UNIT_REGEX = Regex(
    "[0-9۰-۹•][\\u2066-\\u2069\\u200E\\u200F\\u061C]*[\\s\\u00A0\\u202F]+" +
        "((?:(?:هزار|میلیون|میلیارد)[\\s\\u00A0\\u202F]+)?(?:تومان|ریال))(?![\\u0600-\\u06FF])",
)

private fun hasUnitWord(s: String) = s.contains("تومان") || s.contains("ریال")

private fun unitSpan(sizePx: Float, base: Color): SpanStyle? = when {
    sizePx >= 16f -> SpanStyle(fontSize = 0.72.em, fontWeight = FontWeight.Normal, color = base.copy(alpha = base.alpha * 0.7f))
    sizePx >= 13f -> SpanStyle(fontSize = 0.82.em, fontWeight = FontWeight.Normal, color = base.copy(alpha = base.alpha * 0.72f))
    else -> null
}

private fun styleUnits(text: AnnotatedString, span: SpanStyle): AnnotatedString {
    val matches = UNIT_REGEX.findAll(text.text).toList()
    if (matches.isEmpty()) return text
    return AnnotatedString.Builder(text).apply {
        matches.forEach { m -> m.groups[1]?.range?.let { addStyle(span, it.first, it.last + 1) } }
    }.toAnnotatedString()
}

@Composable
private fun unitSpanFor(color: Color, fontSize: TextUnit, style: TextStyle): SpanStyle? {
    val size = when {
        fontSize.isSpecified && fontSize.isSp -> fontSize.value
        style.fontSize.isSpecified && style.fontSize.isSp -> style.fontSize.value
        else -> 14f
    }
    val base = color.takeOrElse { style.color.takeOrElse { LocalContentColor.current } }
    return unitSpan(size, base)
}

@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current,
) {
    val span = if (hasUnitWord(text)) unitSpanFor(color, fontSize, style) else null
    val styled = if (span != null) remember(text, span) { styleUnits(AnnotatedString(text), span) } else null
    if (styled == null || styled.spanStyles.isEmpty()) {
        M3Text(
            text = text, modifier = modifier, color = color, fontSize = fontSize, fontStyle = fontStyle,
            fontWeight = fontWeight, fontFamily = fontFamily, letterSpacing = letterSpacing,
            textDecoration = textDecoration, textAlign = textAlign, lineHeight = lineHeight,
            overflow = overflow, softWrap = softWrap, maxLines = maxLines, minLines = minLines,
            onTextLayout = { onTextLayout?.invoke(it) }, style = style,
        )
    } else {
        M3Text(
            text = styled, modifier = modifier, color = color, fontSize = fontSize, fontStyle = fontStyle,
            fontWeight = fontWeight, fontFamily = fontFamily, letterSpacing = letterSpacing,
            textDecoration = textDecoration, textAlign = textAlign, lineHeight = lineHeight,
            overflow = overflow, softWrap = softWrap, maxLines = maxLines, minLines = minLines,
            onTextLayout = { onTextLayout?.invoke(it) }, style = style,
        )
    }
}

@Composable
fun Text(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
) {
    val span = if (hasUnitWord(text.text)) unitSpanFor(color, fontSize, style) else null
    val styled = if (span != null) remember(text, span) { styleUnits(text, span) } else text
    M3Text(
        text = styled, modifier = modifier, color = color, fontSize = fontSize, fontStyle = fontStyle,
        fontWeight = fontWeight, fontFamily = fontFamily, letterSpacing = letterSpacing,
        textDecoration = textDecoration, textAlign = textAlign, lineHeight = lineHeight,
        overflow = overflow, softWrap = softWrap, maxLines = maxLines, minLines = minLines,
        inlineContent = inlineContent, onTextLayout = onTextLayout, style = style,
    )
}
