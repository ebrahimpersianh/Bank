package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * یه دارایی غیرنقدیِ کاربر (طلا، سکه، ارز، رمزارز، یا هر عنوانِ دلخواه).
 *
 * **این جدول فقط «چه چیزی» رو نگه می‌داره، نه «چقدر»**: مقدارِ فعلیِ هر دارایی از جمعِ
 * خرید/فروش‌های [AssetTradeEntity] حساب می‌شه - دقیقاً همون الگوی موجودیِ حساب‌کتاب که از رو
 * تراکنش‌ها محاسبه می‌شه، نه یه ستونِ جدا که می‌تونه از واقعیت جا بمونه.
 *
 * به همین دلیل هر دارایی **یه ردیفِ واحد** تو تبِ دارایی داره، حتی اگه ده بار خرید و فروش شده
 * باشه (خواسته‌ی صریحِ کاربر: «دو تا بیت‌کوین خرید و فروش گذاشتم، دوتا تو یکی میاد»).
 */
@Entity(tableName = "assets", indices = [Index(value = ["symbol"], unique = true)])
data class AssetEntity(
    @PrimaryKey val id: Long,
    /** کلیدِ یکتای دارایی - برای دارایی‌های آماده همون نمادِ کاتالوگ (`BTC`، `SEKKE_EMAMI`…) و
     * برای «عنوانِ دلخواه» یه کلیدِ ساخته‌شده از روی همون اسم. یکتاست تا یه دارایی دو بار ساخته نشه. */
    val symbol: String,
    val name: String,
    /** یکی از [ASSET_CATEGORY_CRYPTO] / [ASSET_CATEGORY_FIAT] / [ASSET_CATEGORY_GOLD] / [ASSET_CATEGORY_CUSTOM]. */
    val category: String,
    /**
     * قیمتِ هر واحد به ریال.
     *
     * ⚠️ برای دارایی‌های آماده این عدد باید از سرویسِ قیمتِ لحظه‌ای بیاد؛ چون کلیدِ اون سرویس هنوز
     * نرسیده (تصمیمِ صریحِ کاربر: «همه‌چیز رو بساز و قیمت‌ها جای خالی بمونن»)، فعلاً `null` می‌مونه
     * و UI به‌جای عدد «—» نشون می‌ده. برای «عنوانِ دلخواه» خودِ کاربر واردش می‌کنه.
     */
    val unitPriceRial: Double? = null,
    /** آخرین باری که [unitPriceRial] به‌روز شده (ISO-8601). null یعنی هیچ‌وقت. */
    val priceUpdatedAt: String? = null,
    val createdAt: String,
)

/** یه خرید یا فروشِ یه دارایی. مقدارِ فعلی = جمعِ خریدها منهای جمعِ فروش‌ها. */
@Entity(tableName = "asset_trades", indices = [Index(value = ["assetId"])])
data class AssetTradeEntity(
    @PrimaryKey val id: Long,
    val assetId: Long,
    /** `true` = خرید (به مقدار اضافه می‌شه)، `false` = فروش. */
    val isBuy: Boolean,
    /** تعدادِ واحد (مثلاً ۱.۲ بیت‌کوین یا ۳ سکه) - اعشاری، چون رمزارز/طلا کسری خرید می‌شن. */
    val quantity: Double,
    /** مبلغِ کلِ همین معامله به ریال، همون‌طور که کاربر وارد کرده. */
    val totalRial: Double,
    val year: Int,
    val month: Int,
    val day: Int,
    val description: String = "",
    val createdAt: String,
)

const val ASSET_CATEGORY_CRYPTO = "crypto"
const val ASSET_CATEGORY_FIAT = "fiat"
const val ASSET_CATEGORY_GOLD = "gold"
const val ASSET_CATEGORY_CUSTOM = "custom"
