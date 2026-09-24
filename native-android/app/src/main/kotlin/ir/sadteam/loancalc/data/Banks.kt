package ir.sadteam.loancalc.data

import androidx.compose.ui.graphics.Color

/**
 * پورت مستقیم از آرایه‌های banks/creditServices تو www/index.html.
 * logoAsset مسیر فایل تو assets/ هست (نه drawable) چون همون فایل‌های PNG/JPG
 * موجود تو icons/banks و icons/services مستقیم کپی شدن، بدون تبدیل فرمت.
 *
 * هشدار (از CLAUDE.md): صرفاً یکی‌بودن بایت با یه فایل قدیمی رو ملاک درستی اسم بانک نگیر؛
 * همیشه لوگو رو با شکل واقعی و شناخته‌شده‌ی همون بانک تطبیق بده.
 */
data class BankEntry(
    val name: String,
    val color: Color,
    val logoAsset: String,
    // فقط برای خدمات اعتباری (رو انتخاب، فرم نرخ/ماه/مبلغ رو پیش‌فرض می‌کنه)
    val ratePct: Double = 0.0,
    val months: Int = 0,
    val minAmount: Long = 0,
    val maxAmount: Long = 0,
)

val banks: List<BankEntry> = listOf(
    BankEntry("بانک ملی ایران", Color(0xFF1565C0), "banks/melli.png"),
    BankEntry("بانک ملت", Color(0xFFD32F2F), "banks/mellat.png"),
    BankEntry("بانک صادرات", Color(0xFFF9A825), "banks/saderat.png"),
    BankEntry("بانک تجارت", Color(0xFF0277BD), "banks/tejarat.png"),
    BankEntry("بانک سپه", Color(0xFF2E7D32), "banks/sepah.png"),
    BankEntry("بانک مسکن", Color(0xFF5D4037), "banks/maskan.png"),
    BankEntry("بانک کشاورزی", Color(0xFF388E3C), "banks/keshavarzi.png"),
    BankEntry("بانک رفاه کارگران", Color(0xFF00838F), "banks/refah.png"),
    BankEntry("پست بانک ایران", Color(0xFFF57C00), "banks/postbank.png"),
    BankEntry("بانک اقتصاد نوین", Color(0xFF6A1B9A), "banks/eghtesad-novin.png"),
    BankEntry("بانک پارسیان", Color(0xFFAD1457), "banks/parsian.png"),
    BankEntry("بانک پاسارگاد", Color(0xFF283593), "banks/pasargad.png"),
    BankEntry("بانک سامان", Color(0xFF00695C), "banks/saman.png"),
    BankEntry("بانک شهر", Color(0xFF37474F), "banks/shahr.png"),
    BankEntry("بانک دی", Color(0xFF4527A0), "banks/dey.png"),
    BankEntry("بانک گردشگری", Color(0xFFC62828), "banks/gardeshgari.png"),
    BankEntry("بانک کارآفرین", Color(0xFFEF6C00), "banks/karafarin.png"),
    BankEntry("بلوبانک", Color(0xFF0091EA), "banks/blubank.png"),
    BankEntry("بانک مرکزی", Color(0xFF283593), "banks/markazi.png"),
    BankEntry("بانک انصار", Color(0xFFB71C1C), "banks/ansar.png"),
    BankEntry("بانک حکمت ایرانیان", Color(0xFF1976D2), "banks/hekmat.png"),
    BankEntry("موسسه اعتباری آرمان", Color(0xFF8D6E63), "banks/arman.png"),
    BankEntry("موسسه اعتباری ملل", Color(0xFF1565C0), "banks/melal.png"),
    BankEntry("بانک صنعت و معدن", Color(0xFF8D6E63), "banks/sanat-madan.png"),
    BankEntry("بانک سرمایه", Color(0xFF37474F), "banks/sarmayeh.png"),
    BankEntry("بانک سینا", Color(0xFF0D47A1), "banks/sina.png"),
    BankEntry("بانک توسعه تعاون", Color(0xFF00838F), "banks/tosee-taavon.png"),
    BankEntry("بانک توسعه صادرات ایران", Color(0xFF2E7D32), "banks/tosee-saderat.png"),
    BankEntry("موسسه اعتباری توسعه", Color(0xFF8D2F2F), "banks/tosee-credit.png"),
    BankEntry("بانک خاورمیانه", Color(0xFFF57F17), "banks/khavarmianeh.png"),
    BankEntry("بانک رسالت", Color(0xFF00838F), "banks/resalat.png"),
    BankEntry("بانک ایران زمین", Color(0xFF6A1B9A), "banks/iran-zamin.png"),
    BankEntry("بانک مهر ایران", Color(0xFF2E7D32), "banks/mehr-iran.png"),
)

val creditServices: List<BankEntry> = listOf(
    BankEntry("دیجی‌پی (خرید اقساطی)", Color(0xFFE53935), "services/digipay.png", ratePct = 23.0, months = 12, minAmount = 100_000_000, maxAmount = 500_000_000),
    BankEntry("اسنپ‌پی (اعتبار بانکی)", Color(0xFF43A047), "services/snapp-pay.jpg", ratePct = 22.0, months = 24, minAmount = 50_000_000, maxAmount = 1_000_000_000),
    BankEntry("اسنپ‌پی (۴ قسط بدون سود)", Color(0xFF66BB6A), "services/snapp-pay-4.jpg", ratePct = 0.0, months = 4, minAmount = 10_000_000, maxAmount = 100_000_000),
    BankEntry("آپ (Up)", Color(0xFF8E24AA), "services/up.png", ratePct = 24.0, months = 12, minAmount = 50_000_000, maxAmount = 500_000_000),
    BankEntry("ازکی وام", Color(0xFFFB8C00), "services/azki.jpg", ratePct = 23.0, months = 18, minAmount = 50_000_000, maxAmount = 700_000_000),
    BankEntry("ویپاد", Color(0xFF00ACC1), "services/vipad.jpg", ratePct = 24.0, months = 12, minAmount = 50_000_000, maxAmount = 500_000_000),
)
