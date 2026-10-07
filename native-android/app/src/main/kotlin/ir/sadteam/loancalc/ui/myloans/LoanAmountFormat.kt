package ir.sadteam.loancalc.ui.myloans

import ir.sadteam.loancalc.core.fmt
import ir.sadteam.loancalc.ui.jibak.faDigits
import ir.sadteam.loancalc.ui.jibak.rialToToman

/** مبلغِ ریالیِ دیتابیس → متنِ تومانیِ فارسی. قبلاً سه نسخه‌ی یکسان در سه فایلِ همین پوشه بود. */
internal fun amountToman(rial: Double): String = fmt(rialToToman(rial.toLong()).toDouble()).faDigits()
