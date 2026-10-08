package ir.sadteam.loancalc.ui

import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.rialToToman

/**
 * مبلغِ ریالیِ دیتابیس/محاسبه → متنِ تومانیِ فارسی (بندِ ۲ی README: ذخیره ریال، نمایش تومان).
 * قبلاً چهار نسخه‌ی یکسان در چهار صفحه‌ی ماشین‌حساب بود.
 */
internal fun amountToman(rial: Double): String = fmt(rialToToman(rial.toLong()).toDouble()).faDigits()
