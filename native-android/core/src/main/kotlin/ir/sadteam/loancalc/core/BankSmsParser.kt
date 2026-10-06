package ir.sadteam.loancalc.core

/** نتیجه‌ی پارسِ یه پیامکِ بانکی - [amountRial] همیشه به ریال (نه تومان) نرمال شده، [cardSuffix]
 * (اگه پیدا بشه) ۴ رقمِ آخرِ کارت برای تطبیق با [ir.sadteam.loancalc.data.db.AccountEntity.cardNumber]. */
data class ParsedBankSms(
    val amountRial: Double,
    val type: TransactionType,
    val cardSuffix: String?,
    /** مانده‌ی بعد از تراکنش، اگر بانک نوشته باشد (ریال) - برای مقایسه با موجودیِ ثبت‌شده. */
    val balanceRial: Double? = null,
)

/**
 * پارسِ متنِ پیامکِ بانکی به یه تراکنشِ ساختاریافته - رجوع کن به CLAUDE.md، «خوندنِ خودکارِ پیامکِ
 * بانکی». چون فرمتِ پیامکِ هر بانکِ ایرانی فرق داره، عمداً بر اساسِ کلیدواژه‌های رایج (نه فرمتِ دقیقِ
 * یه بانکِ خاص) کار می‌کنه - قابلِ‌اعتمادتر از یه‌سری regexِ خیلی سخت‌گیرانه، ولی همیشه هم درست
 * تشخیص نمی‌ده؛ به همین خاطر تراکنشِ ساخته‌شده باید همیشه قابلِ‌حذف/ویرایشِ دستی بمونه (رجوع کن به
 * BankSmsReceiver - هیچ‌وقت داده رو جایی نمی‌فرسته، فقط محلی یه تراکنش می‌سازه).
 */
object BankSmsParser {
    // مبلغ: یه رشته‌ی رقمی (با یا بدون جداکننده‌ی هزارگان) بلافاصله قبل از «ریال»/«تومان». حداقل
    // ۴ رقم چون مبلغ‌های بانکی واقعی همیشه حداقل هزارتومانی‌ان - جلوگیری از قاپیدنِ اعدادِ کوچیکِ
    // بی‌ربط (مثلاً شماره‌ی پیگیری).
    private val amountRegex = Regex("([\\d۰-۹]{1,3}(?:[,٬٫.][\\d۰-۹]{3})+|[\\d۰-۹]{4,})\\s*(ریال|ريال|تومان|تومن)")
    private val signedRegex = Regex("(?:^|[\\s:])([+-])\\s?(\\d{1,3}(?:[,٬]\\d{3})+|\\d{5,})|(?:^|[\\s:])()(\\d{1,3}(?:[,٬]\\d{3})+|\\d{5,})([+-])(?![\\d])")
    private val labeledRegex = Regex("(?:مبلغ|برداشت|واریز|خرید|انتقال|پرداخت)\\s*[:：]?\\s*(\\d{1,3}(?:[,٬]\\d{3})+|\\d{4,})")
    private val balanceRegex = Regex("(?:مانده|موجودی)\\s*[:：]?\\s*(\\d{1,3}(?:[,٬]\\d{3})+|\\d{4,})")

    private fun cleanAmount(raw: String): Double? =
        toEnDigits(raw).replace(",", "").replace("٬", "").replace("٫", "").replace(".", "").toDoubleOrNull()?.takeIf { it > 0 }

    /** عدد درست بعد از «مانده/موجودی» آمده؟ (۱۲ کاراکترِ قبلش) */
    private fun isBalanceNumber(text: String, at: Int): Boolean {
        val before = text.substring(maxOf(0, at - 12), at)
        return before.contains("مانده") || before.contains("موجودی")
    }

    private val cardSuffixRegex = Regex("(?:\\*+|منتهی به|کارت)\\D{0,6}(\\d{4})(?!\\d)")
    // ⚠️ بانک‌های دیجیتال (بلوبانک و مانندش) متنِ اعلانشان با پیامکِ بانکِ سنتی فرق دارد و
    // فعل‌های دیگری به کار می‌برند. با فهرستِ قبلی، اعلان پارس نمی‌شد و بی‌صدا رد می‌شد -
    // گزارشِ واقعیِ کاربر: «اعلانِ بلوبانک آمد ولی هیچ کاری نشد».
    private val depositKeywords = listOf("واریز", "بستانکار", "افزایش موجودی", "دریافت", "واریزی", "به شما منتقل", "به حساب شما منتقل")
    private val withdrawalKeywords = listOf(
        "برداشت", "خرید", "بدهکار", "کاهش موجودی", "انتقال وجه", "پرداخت", "کسر", "انتقال به",
        // ۱۴ مهر: عبارت‌های انتقالِ بانک‌های دیجیتال (بلو و…) - «… منتقل شد»، «انتقال موفق».
        "منتقل شد", "انتقال", "ارسال شد", "کارت به کارت", "پایا", "ساتنا",
    )

    /**
     * 🚨 **پیامکی که «دعوت به پرداخت» است، نه گزارشِ پرداخت.**
     *
     * قبضِ برق/تلفن، یادآوریِ بدهی و پیامکِ تبلیغاتی همگی هم مبلغ دارند هم فعلِ «پرداخت»،
     * پس با فهرستِ کلیدواژه‌ها برداشتِ واقعی خوانده می‌شدند. کاربر یک «برداشتِ ۵۱۱٬۵۰۰ تومان -
     * دسته: قبض» دید که اصلاً رخ نداده بود.
     *
     * قاعده: اگر یکی از این نشانه‌ها در متن باشد، پیامک **تراکنش نیست** - مگر این‌که
     * [balanceKeywords] هم بیاید، چون گزارشِ واقعیِ بانک تقریباً همیشه مانده را هم می‌گوید.
     */
    private val notATransactionKeywords = listOf(
        "قابل پرداخت", "قابل‌پرداخت", "مهلت", "شناسه قبض", "شناسه پرداخت", "قبض شما",
        "بدهی شما", "سررسید", "جهت پرداخت", "برای پرداخت", "پرداخت کنید", "تخفیف",
        "جشنواره", "برنده", "تمدید", "اقساط معوق",
        // 🚨 **خبرِ آینده، نه گزارشِ انجام‌شده.** پیامکِ بیمه‌ی تکمیلی نوشته بود «مبلغ …
        // قابل پرداخت بوده که به حساب شما واریز خواهد شد» و برنامه یک واریزِ ۲۲۴هزارتومانی
        // ساخت که هنوز رخ نداده بود. فعلِ آینده یعنی هنوز پولی جابه‌جا نشده.
        "خواهد شد", "واریز می‌شود", "پرداخت می‌شود", "به‌زودی", "به زودی",
    )

    /** نشانه‌ی گزارشِ واقعیِ بانک - مانده/موجودی بعد از تراکنش. */
    private val balanceKeywords = listOf("مانده", "موجودی", "موجودي")

    /**
     * 🚨 **پیامکِ تبلیغاتیِ اپراتور/سرویس - هرگز تراکنش نیست.**
     *
     * دو گزارشِ واقعیِ کاربر: تبلیغِ همراهِ اول («هزینه اشتراک ماهانه: ۷٫۵۰۰ تومان … خرید و
     * فعال‌سازی») و تبلیغِ ایرانسل («۱۰۰ گیگ اینترنت هدیه … پس از خرید بسته») هر دو هم مبلغ
     * داشتند هم فعلِ «خرید»، پس برداشتِ واقعی خوانده شدند.
     *
     * پیامکِ **رمزِ پویا** هم این‌جاست: مبلغ دارد ولی هنوز خریدی انجام نشده - فقط رمز صادر شده.
     */
    private val adKeywords = listOf(
        "اشتراک ماهانه", "هزینه اشتراک", "فعال‌سازی", "فعال سازی", "کد دستوری",
        "گیگ", "گیگابایت", "بسته اینترنت", "خرید بسته", "هدیه", "ظرفیت محدود",
        "ثبت‌نام", "ثبت نام", "http", "لغو11", "لغو 11",
        "رمز پویا", "رمز یکبار", "رمز یک‌بار", "رمز دوم",
        // اعتبار/کیف‌پولِ خریدِ اقساطی (گزارشِ کاربر: «بازگشت به اعتبار»ِ دیجی‌پی واریز خوانده شد).
        // این پول به حسابِ بانکی نمی‌آید - اعتبارِ خرید است.
        "دیجی‌پی", "دیجی پی", "دیجیپی", "اسنپ‌پی", "اسنپ پی", "اسنپ پِی",
        "اعتبار قابل مصرف", "بازگشت به اعتبار", "شارژ اعتبار", "کیف دیجی",
    )

    /**
     * 🚨 **نشانه‌ی این‌که پیامک واقعاً گزارشِ یک تراکنشِ انجام‌شده است.**
     *
     * تا این‌جا منطق فقط «رد کردن» بود: هر متنی که مبلغ + فعل داشت و در فهرستِ ممنوعه نبود
     * تراکنش حساب می‌شد. تبلیغِ تازه همیشه از آن فهرست رد می‌شد. حالا برعکس است - پیامک
     * باید **دلیلی برای پذیرفته‌شدن** داشته باشد: یا مانده/موجودی بگوید، یا از کارت/حساب/شبا
     * نام ببرد، یا فعلش **گذشته** باشد («واریز شد»، نه «قابل خرید»).
     *
     * بانکِ واقعی تقریباً همیشه دستِ‌کم یکی از این‌ها را دارد؛ تبلیغ هیچ‌کدام را ندارد.
     */
    private val reportEvidence = listOf(
        "مانده", "موجودی", "موجودي", "کارت", "كارت", "حساب", "شبا", "سپرده",
        "پیگیری", "پيگيري", "تراکنش", "ترمینال", "ترمينال", "انتقال",
        "واریز شد", "برداشت شد", "کسر شد", "پرداخت شد", "خرید شد", "انجام شد", "منتقل شد", "انتقال موفق",
    )

    /**
     * 🌐 **کلیدواژه‌های اضافه از سرور** (۸ مهر): بانکی که متنش را عوض کرد، بی‌آپدیتِ برنامه درست می‌شود.
     * فقط **اضافه** می‌کنند؛ فهرستِ داخلی سرِ جایش می‌ماند.
     */
    @Volatile var remoteDeposit: List<String> = emptyList()
    @Volatile var remoteWithdrawal: List<String> = emptyList()
    @Volatile var remoteIgnore: List<String> = emptyList()

    /**
     * [trustedSource] = اعلانِ اپی که خودِ کاربر به‌عنوانِ اپِ بانکش انتخاب کرده (مثلِ بلو):
     * متنِ این اعلان‌ها کوتاه است و «مانده/کارت/حساب» ندارد («پرداخت قبض ۲۰۰٬۰۰۰ ریال بابت…»)،
     * پس شرطِ «نشانه‌ی گزارشِ بانکی» برایشان لازم نیست.
     */
    fun parse(rawBody: String, trustedSource: Boolean = false): ParsedBankSms? {
        // 🧠 (۱۴ مهر) یکدست‌سازی: ي/ك عربی، نیم‌فاصله و ارقامِ فارسی - بانک‌ها هر کدام یک‌جور می‌نویسند.
        val body = rawBody.replace('ي', 'ی').replace('ك', 'ک').replace('\u200c', ' ').replace('−', '-')
        val numbered = toEnDigits(body)
        var signType: TransactionType? = null
        val amountRial: Double = run {
            amountRegex.find(body)?.let { m ->
                val a = cleanAmount(m.groupValues[1]) ?: return null
                return@run if (m.groupValues[2] == "تومان" || m.groupValues[2] == "تومن") a * 10 else a
            }
            // بی «ریال/تومان»: خیلی از بانک‌ها فقط عدد با علامت می‌نویسند («-1,200,000» / «1,200,000+»)
            // یا بعد از «مبلغ/برداشت/واریز/خرید». پیش‌فرضِ پیامکِ بانکی ریال است. عددِ بعد از «مانده» نه.
            signedRegex.findAll(numbered).firstOrNull { !isBalanceNumber(numbered, it.range.first) }?.let { m ->
                val num = m.groupValues[2].ifEmpty { m.groupValues[4] }
                val sign = m.groupValues[1].ifEmpty { m.groupValues[5] }
                signType = if (sign == "+") TransactionType.DEPOSIT else TransactionType.WITHDRAWAL
                return@run cleanAmount(num) ?: return null
            }
            labeledRegex.find(numbered)?.let { m -> return@run cleanAmount(m.groupValues[1]) ?: return null }
            return null
        }
        if (amountRial <= 0) return null

        // 🚨 (۱۴ مهر) نوع = کلیدواژه‌ای که **زودتر** در متن آمده، نه «اول واریز را بگرد». پیامکِ
        // «پرداخت صورتحساب … از حساب شما انجام شد … طرحِ ویژه دریافت کنید» به‌خاطرِ «دریافت»ِ
        // تبلیغِ ته پیام، واریز ثبت می‌شد.
        fun firstIndex(words: List<String>) = words.mapNotNull { w -> body.indexOf(w).takeIf { it >= 0 } }.minOrNull()
        val dIdx = firstIndex(depositKeywords + remoteDeposit)
        val wIdx = firstIndex(withdrawalKeywords + remoteWithdrawal)
        val type = when {
            signType != null -> signType!!
            dIdx == null && wIdx == null -> return null
            dIdx == null -> TransactionType.WITHDRAWAL
            wIdx == null -> TransactionType.DEPOSIT
            // «از حساب شما» نشانه‌ی قطعیِ برداشت است، هر جای متن باشد.
            body.contains("از حساب شما") || body.contains("از حسابت") -> TransactionType.WITHDRAWAL
            // «انتقال وجه … به حساب شما واریز شد» = واریز، با اینکه «انتقال» زودتر آمده.
            body.contains("به حساب شما") || body.contains("به حسابت") || body.contains("به شما") -> TransactionType.DEPOSIT
            wIdx < dIdx -> TransactionType.WITHDRAWAL
            else -> TransactionType.DEPOSIT
        }

        // تبلیغ/رمزِ پویا بی‌قیدوشرط رد می‌شود - حتی اگر کلمه‌ی «حساب» هم تویش باشد.
        if ((adKeywords + remoteIgnore).any { body.contains(it) }) return null

        // دعوت‌نامه‌ی پرداخت را تراکنش حساب نکن (توضیحِ کاملش بالای notATransactionKeywords).
        if (notATransactionKeywords.any { body.contains(it) } &&
            balanceKeywords.none { body.contains(it) }
        ) {
            return null
        }

        // و در آخر: بدونِ نشانه‌ی گزارشِ واقعی، پیامک پذیرفته نمی‌شود.
        if (!trustedSource && reportEvidence.none { body.contains(it) }) return null

        val cardSuffix = cardSuffixRegex.find(numbered)?.groupValues?.get(1)
        val balance = balanceRegex.find(numbered)?.groupValues?.get(1)?.let { cleanAmount(it) }
        return ParsedBankSms(amountRial, type, cardSuffix, balance)
    }
}

/**
 * نرمال‌سازیِ فرستنده‌ی پیامک برای تطبیق با [ir.sadteam.loancalc.data.db.AccountEntity.smsSender]:
 * ارقامِ فارسی به لاتین، حذفِ فاصله/خط‌تیره/پرانتز، حروف بزرگ. پیش‌شماره‌ی ایران هم یکدست می‌شه
 * (`+98…`/`0098…`/`98…` → `0…`) چون یه سرشماره‌ی یکسان بعضی گوشی‌ها با پیش‌شماره و بعضی بدونش
 * نشون داده می‌شه و کاربر هر کدوم رو ببینه همون رو وارد می‌کنه.
 */
fun normalizeSmsSender(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    var s = toEnDigits(raw).uppercase().filter { it.isLetterOrDigit() || it == '+' }
    s = s.removePrefix("+")
    if (s.startsWith("0098")) s = s.removePrefix("0098")
    if (s.length > 10 && s.startsWith("98")) s = s.removePrefix("98")
    if (s.isNotEmpty() && s.first().isDigit() && !s.startsWith("0")) s = "0$s"
    return s
}

/** آیا فرستنده‌ی این پیامک همونیه که کاربر برای این حساب ثبت کرده؟ برای اینکه یه سرشماره‌ی
 * ۱۰-۱۴رقمی که اپراتورها گاهی با چند رقمِ اضافه تحویل می‌دن هم بگیره، تطبیقِ «یکی پسوندِ اون یکی
 * باشه» هم قبوله (با حداقلِ ۴ کاراکتر، تا دو سرشماره‌ی بی‌ربط تصادفی جور در نیان). */
fun smsSenderMatches(accountSender: String?, incoming: String?): Boolean =
    // یک حساب می‌تواند سرشماره‌ی پیامک و بسته‌نامِ اپِ اعلان را با «,» کنارِ هم داشته باشد.
    accountSender.orEmpty().split(',').any { singleSenderMatches(it, incoming) }

private fun singleSenderMatches(accountSender: String?, incoming: String?): Boolean {
    val a = normalizeSmsSender(accountSender)
    val b = normalizeSmsSender(incoming)
    if (a.isEmpty() || b.isEmpty()) return false
    if (a == b) return true
    val min = minOf(a.length, b.length)
    return min >= 4 && (a.endsWith(b) || b.endsWith(a))
}

/**
 * اثرانگشتِ یک پیامک/اعلانِ بانکی برای تشخیصِ **تحویلِ تکراری**.
 *
 * 🚨 چرا خودِ متن: کلیدِ قبلی فقط «فرستنده + نوع + مبلغ + ۴ رقمِ کارت» بود و پنجره‌اش
 * ۶ ساعت. یعنی دو **خریدِ واقعیِ** هم‌مبلغ با یک کارت در یک بعدازظهر، دومی بی‌صدا
 * دور ریخته می‌شد. متنِ بانک همیشه چیزی دارد که آن دو را از هم جدا می‌کند (مانده‌ی
 * جدید، ساعت، شماره‌ی پیگیری)، پس خودِ متن دقیق‌ترین شناسه‌ی در دسترس است.
 *
 * نرمال‌سازی لازم است چون همان اعلان گاهی با فاصله‌ی متفاوت یا ارقامِ فارسی/لاتین
 * دوباره منتشر می‌شود؛ آن حالت واقعاً تکراری است و باید یک اثرانگشت بدهد.
 */
fun smsDedupeFingerprint(body: String): String =
    toEnDigits(body).filter { !it.isWhitespace() }.hashCode().toString()
