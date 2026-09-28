package ir.sadteam.loancalc.ui.sound

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

/**
 * صدای کوتاهِ حذف (خواسته‌ی صریحِ کاربر، رجوع کن به CLAUDE.md) - عمداً بدونِ فایلِ صوتیِ جدا؛
 * [ToneGenerator] یه تُنِ کوتاهِ سیستمی می‌سازه، سبک و بدونِ نیاز به asset. رو استریمِ SYSTEM پخش
 * می‌شه، پس خودکار از تنظیمِ صدای سیستم/حالتِ بی‌صدای گوشی پیروی می‌کنه - نیازی به سوییچِ جداگانه
 * تو تنظیماتِ اپ نیست.
 */
@Composable
fun rememberDeleteSound(): () -> Unit {
    val toneGenerator = remember {
        runCatching { ToneGenerator(AudioManager.STREAM_SYSTEM, 70) }.getOrNull()
    }
    DisposableEffect(Unit) {
        onDispose { toneGenerator?.release() }
    }
    return {
        runCatching { toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 120) }
    }
}
