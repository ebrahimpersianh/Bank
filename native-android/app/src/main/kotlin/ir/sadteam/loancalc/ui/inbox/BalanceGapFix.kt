package ir.sadteam.loancalc.ui.inbox

import ir.sadteam.loancalc.data.db.InboxMessageEntity
import ir.sadteam.loancalc.ui.jibak.faDigits
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
internal data class BalanceGap(val accountId: Long?, val accountName: String?, val gapRial: Double, val bankBalanceRial: Double? = null)

internal const val GAP_REF_PREFIX = "gap:"

/** شناسه‌ی ثابتِ پیامِ اختلافِ هر حساب (بالاتر از هر `currentTimeMillis`) - پیامِ تازه جای قبلی را می‌گیرد. */
internal fun gapMessageId(accountId: Long) = 7_000_000_000_000L + accountId

/** ۱۰ آبان: مانده‌ی خودِ بانک هم در refId می‌نشیند تا «ثبت» با **آخرین** مانده و موجودیِ همان لحظه حساب شود. */
internal fun gapRef(accountId: Long, gapRial: Double, bankBalanceRial: Double) =
    "$GAP_REF_PREFIX$accountId:${gapRial.toLong()}:${bankBalanceRial.toLong()}"

private val GAP_TITLE = Regex("موجودیِ «(.+)» با بانک فرق دارد")
private val GAP_BODY = Regex("مانده را ([0-9۰-۹٬,]+) (تومان|ریال) (بیشتر|کمتر)")

internal fun parseBalanceGap(m: InboxMessageEntity): BalanceGap? {
    if (m.kind != InboxMessageEntity.Kind.SYSTEM) return null
    m.refId?.takeIf { it.startsWith(GAP_REF_PREFIX) }?.let { ref ->
        val parts = ref.removePrefix(GAP_REF_PREFIX).split(':')
        val id = parts.getOrNull(0)?.toLongOrNull() ?: return null
        val gap = parts.getOrNull(1)?.toDoubleOrNull() ?: return null
        val bank = parts.getOrNull(2)?.toDoubleOrNull()
        return BalanceGap(id, GAP_TITLE.find(m.title)?.groupValues?.get(1), gap, bank)
    }
    val name = GAP_TITLE.find(m.title)?.groupValues?.get(1) ?: return null
    val b = GAP_BODY.find(m.body) ?: return null
    val amount = b.groupValues[1].parseFaAmount()?.toDouble() ?: return null
    val rial = if (b.groupValues[2] == "تومان") amount * 10 else amount
    return BalanceGap(null, name, if (b.groupValues[3] == "بیشتر") rial else -rial)
}

/**
 * مانده‌ی تازه‌ی بانک در برابرِ جیبک (پیامک و اعلان هر دو از این می‌گذرند).
 *
 * ۱۰ آبان (گزارشِ کاربر: «ثبت را زدم، موجودیِ بلو منفی شد»): مبلغِ اختلاف لحظه‌ی رسیدنِ پیام حساب و
 * در پیام قفل می‌شد؛ کاربر صبحِ بعد ردیف‌های اشتباه را پاک کرد و بعد «ثبت» زد → همان عددِ کهنه دوباره
 * کم شد. حالا **آخرین مانده‌ی بانک مبناست**: هر پیامِ مانده‌دار پیامِ اختلاف را به‌روز می‌کند (حتی وقتی
 * دوباره خبر نمی‌دهد)، اگر دیگر اختلافی نیست پیامِ باز پاک می‌شود، و «ثبت» اختلاف را همان لحظه دوباره
 * حساب می‌کند ([InboxViewModel.fixBalanceGap]).
 */
internal suspend fun reportBalanceGap(
    accounts: ir.sadteam.loancalc.data.AccountRepository,
    inbox: ir.sadteam.loancalc.data.InboxRepository,
    prefs: ir.sadteam.loancalc.data.prefs.UiPrefs,
    account: ir.sadteam.loancalc.data.db.AccountEntity,
    bankBalanceRial: Double,
    sourceLabel: String,
    sourceText: String,
) {
    val id = gapMessageId(account.id)
    val existing = inbox.byId(id)
    val gap = accounts.bankBalanceGap(account, false, 0.0, bankBalanceRial)
    if (gap == null) {
        // جیبک و بانک حالا یکی‌اند → پیامِ بازِ قبلی دیگر معنا ندارد (پیامِ «ثبت شد» می‌ماند).
        if (existing != null && existing.actionState != InboxMessageEntity.ActionState.DONE) inbox.delete(id)
        return
    }
    val today = ir.sadteam.loancalc.core.JalaliCalendar.today()
    val gapToman = ir.sadteam.loancalc.core.fmt(
        ir.sadteam.loancalc.ui.jibak.rialToToman(kotlin.math.abs(gap).toLong()).toDouble(),
    ).faDigits()
    val title = "موجودیِ «${account.name}» با بانک فرق دارد"
    val body = "بانک مانده را $gapToman ${ir.sadteam.loancalc.core.CoreMoneyUnit.label} " + (if (gap > 0) "بیشتر" else "کمتر") +
        " از جیبک نوشته. احتمالاً تراکنشی ثبت نشده یا «موجودیِ روزِ شروع» درست نیست."
    val ref = gapRef(account.id, gap, bankBalanceRial)
    // روزی یک بار برای هر مبلغ خبر (همان قاعده‌ی قبلی)؛ بقیه‌ی وقت‌ها فقط بی‌صدا به‌روز می‌شود.
    if (prefs.claimAutoImportKey("gap|${account.id}|${today.y}-${today.m}-${today.d}|${(gap / 10_000).toLong()}") || existing == null) {
        inbox.post(id = id, kind = InboxMessageEntity.Kind.SYSTEM, title = title, refId = ref, body = body, sourceLabel = sourceLabel, sourceText = sourceText)
    } else {
        inbox.replaceQuietly(existing.copy(title = title, body = body, refId = ref, sourceLabel = sourceLabel, sourceText = sourceText))
    }
}
