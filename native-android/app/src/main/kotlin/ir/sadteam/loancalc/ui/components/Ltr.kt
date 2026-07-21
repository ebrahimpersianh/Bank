package ir.sadteam.loancalc.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/**
 * محتوای [content] رو صرف‌نظر از ambientِ RTLِ کلِ اپ همیشه LTR رندر می‌کنه - برای فیلدها/چیپ‌ها/
 * ردیف‌هایی که محتواشون ذاتاً یه عددِ چپ‌به‌راسته (شماره‌موبایل، PIN، کدِ تایید، شناسه‌ی ملی، شماره‌چک،
 * شناسه‌ی صیادی...): بدونِ این override یا کرسر/جهتِ تایپ معکوس می‌شه، یا رشته‌های کوتاهِ مختلط
 * (مثلِ «+۹۸») موقعِ اندازه‌گیری با bidi ناخواسته می‌شکنن.
 *
 * قبلاً این الگو (`CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr)`)
 * جدا جدا تو چندجا کپی شده بود - این کامپوننت جایگزینِ همه‌ی اون کپی‌هاست.
 */
@Composable
fun Ltr(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr, content = content)
}
