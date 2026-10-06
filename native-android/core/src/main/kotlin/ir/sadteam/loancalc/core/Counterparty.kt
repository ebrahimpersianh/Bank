package ir.sadteam.loancalc.core

/**
 * 🧠 طرفِ حسابِ یک پیامک/اعلانِ بانکی (۱۴ مهر): «به مبینا فتحی» → «مبینا فتحی»، «خرید از
 * فروشگاه رفاه» → «فروشگاه رفاه». دو کار دارد: توضیحِ تراکنش به‌جای «خودکار از اعلان» اسمِ
 * واقعی را نشان می‌دهد، و همین اسم کلیدِ **یادگیریِ دسته** است - کاربر یک بار برای «مبینا فتحی»
 * دسته انتخاب کند، دفعه‌ی بعد خودکار همان می‌خورد.
 *
 * عمداً محافظه‌کار: فقط کلمه‌های فارسیِ بی‌رقم، حداکثر چهار کلمه، و با فهرستِ کلمه‌های ممنوع
 * («حساب»، «شما»، فعل‌ها…) قطع می‌شود. اسمی پیدا نشد → `null` (هیچ یادگیری‌ای بهتر از یادگیریِ غلط).
 */
object Counterparty {
    private val stopWords = setOf(
        "حساب", "حسابت", "حسابتان", "شما", "تو", "کارت", "مبلغ", "ریال", "تومان", "تومن", "بانک", "سپرده",
        "منتقل", "انتقال", "واریز", "برداشت", "شد", "شده", "کرد", "کردی", "کردید", "گردید", "انجام",
        "فرستادی", "فرستاده", "ارسال", "پرداخت", "خرید", "مانده", "موجودی", "بابت", "در", "از", "به",
        "با", "و", "را", "برای", "توسط", "موفق", "عزیز", "تاریخ", "ساعت", "شماره", "پیگیری", "کم", "اضافه",
    )

    // نشانه‌ها به ترتیبِ اطمینان؛ اولی که اسمِ درست بدهد برنده است.
    private val withdrawalMarkers = listOf("پذیرنده", "فروشگاه", "خرید از", "پرداخت به", "به", "برای", "در")
    private val depositMarkers = listOf("واریز کننده", "واریزکننده", "توسط", "از")

    private val persianWord = Regex("^[\\u0621-\\u063A\\u0641-\\u064A\\u067E\\u0686\\u0698\\u06A9\\u06AF\\u06CC\\u0622]+$")

    fun extract(rawBody: String, isWithdrawal: Boolean): String? {
        val body = rawBody.replace('ي', 'ی').replace('ك', 'ک').replace('‌', ' ')
            .replace(Regex("[،,.:：؛;!()\\[\\]«»\"'\\-–]"), " ")
        val words = body.split(Regex("\\s+")).filter { it.isNotBlank() }
        for (marker in if (isWithdrawal) withdrawalMarkers else depositMarkers) {
            val mw = marker.split(' ')
            var i = 0
            while (i <= words.size - mw.size) {
                if ((mw.indices).all { words[i + it] == mw[it] }) {
                    val keepMarker = marker == "فروشگاه"
                    val name = mutableListOf<String>()
                    if (keepMarker) name += marker
                    var j = i + mw.size
                    while (j < words.size && name.size < 4 && persianWord.matches(words[j]) && words[j] !in stopWords) {
                        name += words[j]; j++
                    }
                    val real = if (keepMarker) name.drop(1) else name
                    val joined = name.joinToString(" ")
                    if (real.isNotEmpty() && joined.length >= 3) return joined
                }
                i++
            }
        }
        return null
    }
}
