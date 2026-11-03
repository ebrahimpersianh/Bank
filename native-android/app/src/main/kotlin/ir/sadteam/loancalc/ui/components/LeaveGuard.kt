package ir.sadteam.loancalc.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue

/**
 * 🚨 یافته‌ی ۴۲: برگشت وسطِ فرم (دنگ، قبض) همه‌چیز را بی‌پرسش دور می‌ریخت.
 *
 * تابعی برمی‌گرداند که به‌جای [onLeave] صدا زده شود (فلشِ بازگشت، «انصراف»، لمسِ بیرونِ
 * پنجره). اگر [dirty] باشد اول می‌پرسد؛ وگرنه بی‌پرسش می‌رود - پرسیدن برای فرمِ دست‌نخورده
 * همان «دیالوگِ بی‌ارزش» است که [ConfirmDialog] از آن پرهیز می‌کند. دکمه‌ی برگشتِ گوشی هم
 * با [BackHandler] گرفته می‌شود.
 */
@Composable
fun rememberLeaveGuard(dirty: Boolean, onLeave: () -> Unit): () -> Unit {
    var asking by remember { mutableStateOf(false) }
    val leave by rememberUpdatedState(onLeave)
    BackHandler(enabled = dirty) { asking = true }
    if (asking) {
        ConfirmDialog(
            title = "بیرون می‌روی؟",
            consequence = "چیزهایی که نوشتی ذخیره نمی‌شوند.",
            actionLabel = "بیرون برو",
            tone = ConfirmTone.DESTRUCTIVE,
            onConfirm = { asking = false; leave() },
            onDismiss = { asking = false },
            dismissLabel = "ادامه بده",
        )
    }
    return { if (dirty) asking = true else leave() }
}
