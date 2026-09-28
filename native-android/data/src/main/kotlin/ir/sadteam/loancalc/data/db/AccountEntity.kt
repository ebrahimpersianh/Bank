package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val bankName: String,
    val initialBalance: Double,
    val createdAt: String,
    // شماره‌کارت اختیاریه - فقط برای تشخیصِ خودکارِ بانک (رجوع کن به data.detectBankByCardNumber
    // تو :app) نگه داشته می‌شه، جای دیگه‌ای نمایش/استفاده نمی‌شه.
    val cardNumber: String? = null,
    // شماره/سرشماره‌ی پیامکِ همین بانک (مثلاً «BANKMELLAT» یا «۱۰۰۰۱۱۱۱») - اختیاری، فقط برای
    // اینکه خوندنِ خودکارِ پیامکِ بانکی (BankSmsReceiver) بدونه پیامکِ رسیده مالِ کدوم حسابه.
    // ذخیره‌ی نرمال‌شده نیست؛ تطبیق موقعِ دریافتِ پیامک با normalizeSmsSender انجام می‌شه.
    val smsSender: String? = null,
    /**
     * نوعِ حساب‌کتاب: `"bank"` (کارتِ بانکی) یا `"other"` (منبعِ دیگر - نقدی، کیفِ پول، کارتِ
     * اعتباری و…). خواسته‌ی صریحِ کاربر طبقِ اپِ مرجع: تا قبل از این هر حساب حتماً باید یه بانک
     * می‌داشت و اصلاً مفهومِ «نقدی/غیربانکی» وجود نداشت.
     *
     * پیش‌فرضِ `"bank"` عمدیه تا حساب‌های موجود دقیقاً مثلِ قبل رفتار کنن (مهاجرت هیچ داده‌ای رو
     * عوض نمی‌کنه). برای نوعِ `other`، به‌جای لوگوی بانک آیکونِ [iconKey] نشون داده می‌شه.
     */
    val type: String = ACCOUNT_TYPE_BANK,
    /** کلیدِ آیکونِ حساب‌کتابِ غیربانکی - هم‌الگو با `CustomCategoryEntity.iconKey` (Room نمی‌تونه
     * ImageVector ذخیره کنه). برای نوعِ `bank` بی‌استفاده‌ست و null می‌مونه. */
    val iconKey: String? = null,
    /**
     * خواندنِ خودکارِ پیامکِ **همین** حساب‌کتاب. کلیدِ سراسریِ `smsAutoImportEnabled` بالادستِ
     * اینه: اگه اون خاموش باشه هیچ پیامکی خونده نمی‌شه، فرقی نمی‌کنه این چی باشه.
     *
     * پیش‌فرض `true` عمدیه تا حساب‌های موجود دقیقاً مثلِ قبل رفتار کنن.
     */
    val smsEnabled: Boolean = true,
    /**
     * زمانِ آخرین پیامکی که واقعاً رو این حساب نشست - میلی‌ثانیه‌ی یونیکس، `null` یعنی هیچ‌وقت.
     *
     * جایگزینِ **ارزانِ** «تعدادِ پیامکِ ۳۰ روزِ اخیر»ه (تصمیمِ طراح): همون حرف رو می‌زنه بدونِ
     * جدول یا شمارنده. صفحه‌ی تنظیماتِ پیامک زیرنویسِ هر بانک رو از همین می‌سازه.
     */
    val lastSmsAt: Long? = null,
)

const val ACCOUNT_TYPE_BANK = "bank"
const val ACCOUNT_TYPE_OTHER = "other"
