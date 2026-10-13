package ir.sadteam.loancalc.ui.inbox

import ir.sadteam.loancalc.data.db.InboxMessageEntity
import ir.sadteam.loancalc.ui.jibak.parseFaAmount

/**
 * پیامِ «موجودیِ X با بانک فرق دارد» → اختلافِ قابلِ ثبت (۲۱ مهر، خواسته‌ی کاربر: «یک گزینه بیاد که
 * این رقم تراکنش ثبت بشه و مبلغ یکی بشه»).
 *
 * پیام‌های تازه `refId = "gap:<accountId>:<gapRial>"` دارند. پیام‌های قبل از این نسخه refId ندارند؛
 * از عنوان (نامِ حساب) و متن («... را ۱٬۰۳۷٬۷۵۰ تومان کمتر ...») خوانده می‌شوند.
 *
 * [gapRial] مثبت = بانک **بیشتر** دارد (یک واریز ثبت نشده)؛ منفی = بانک کمتر دارد (برداشتِ ثبت‌نشده).
 */
internal data class BalanceGap(val accountId: Long?, val accountName: String?, val gapRial: Double)

internal const val GAP_REF_PREFIX = "gap:"

/** شناسه‌ی ثابتِ پیامِ اختلافِ هر حساب (بالاتر از هر `currentTimeMillis`) - پیامِ تازه جای قبلی را می‌گیرد. */
internal fun gapMessageId(accountId: Long) = 7_000_000_000_000L + accountId

internal fun gapRef(accountId: Long, gapRial: Double) = "$GAP_REF_PREFIX$accountId:${gapRial.toLong()}"

private val GAP_TITLE = Regex("موجودیِ «(.+)» با بانک فرق دارد")
private val GAP_BODY = Regex("مانده را ([0-9۰-۹٬,]+) (تومان|ریال) (بیشتر|کمتر)")

internal fun parseBalanceGap(m: InboxMessageEntity): BalanceGap? {
    if (m.kind != InboxMessageEntity.Kind.SYSTEM) return null
    m.refId?.takeIf { it.startsWith(GAP_REF_PREFIX) }?.let { ref ->
        val parts = ref.removePrefix(GAP_REF_PREFIX).split(':')
        val id = parts.getOrNull(0)?.toLongOrNull() ?: return null
        val gap = parts.getOrNull(1)?.toDoubleOrNull() ?: return null
        return BalanceGap(id, GAP_TITLE.find(m.title)?.groupValues?.get(1), gap)
    }
    val name = GAP_TITLE.find(m.title)?.groupValues?.get(1) ?: return null
    val b = GAP_BODY.find(m.body) ?: return null
    val amount = b.groupValues[1].parseFaAmount()?.toDouble() ?: return null
    val rial = if (b.groupValues[2] == "تومان") amount * 10 else amount
    return BalanceGap(null, name, if (b.groupValues[3] == "بیشتر") rial else -rial)
}
