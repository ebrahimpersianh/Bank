package ir.sadteam.loancalc.subscription

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.staticCompositionLocalOf
import ir.myket.billingclient.IabHelper
import ir.myket.billingclient.util.IabResult
import ir.myket.billingclient.util.Inventory
import ir.myket.billingclient.util.Purchase

/** کلیدِ عمومیِ واقعیِ اپ (نه رازِ دولوپر)، از پنلِ توسعه‌دهندگانِ مایکت (بخشِ محصولات درون‌برنامه‌ای
 * → کلید عمومی) گرفته شده - برای تاییدِ محلیِ امضای خرید توسطِ IabHelper استفاده می‌شه. */
const val MYKET_IAB_PUBLIC_KEY =
    "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDbE0HmWXnUYn9v8Y2UIhjwdoL3pxWwe4Nde12haN0+R7wQ/lxlpAiiwp8GG8Au4h9UPUhlsU5XEjIVZ4K9WycM4emoyhhpf61fTseCkpeaZV330/1M/6UW6DlW1lqxJC7ZMcMgPTd4oIzFi6hQ22l5ihwGTjOScLZOaOUi8d0GnQIDAQAB"

/** پورت CAFEBAZAAR_SUBSCRIPTION_TIERS برای مایکت - همون شناسه‌های محصول، چون هر استور کاتالوگِ
 * محصولِ جدا و مستقل داره (تداخلی با کافه‌بازار نداره)؛ باید دقیقاً همین ۴ شناسه تو پنلِ مایکت هم
 * (بعد از ثبتِ اپ) به‌عنوانِ محصولاتِ درون‌برنامه‌ای تعریف بشن. */
val subscriptionTiers = listOf(
    "unlimited_loans_1m" to "اشتراک ۱ ماهه",
    "unlimited_loans_3m" to "اشتراک ۳ ماهه",
    "unlimited_loans_6m" to "اشتراک ۶ ماهه",
    "unlimited_loans_1y" to "اشتراک ۱ ساله",
)

/**
 * پوششی رو SDK بومیِ رسمیِ مایکت (`myket-billing-client`، پورتِ خودِ مایکت از Android In-app
 * Billing v3 - همون الگوی کلاسیکِ Google، نه AIDL جدید). معادلِ همون امضای عمومیِ SubscriptionManager
 * تو فلیورِ cafebazaar (connect/disconnect/getPrices/purchase) تا بقیه‌ی اپ (SubscriptionScreen،
 * MainActivity) هیچ فرقی بینِ دو فلیور نبینه.
 *
 * برخلافِ Poolakey که با ActivityResultRegistry مدرن کار می‌کنه، IabHelper رو نتیجه‌ی خریدِ خودش
 * رو با onActivityResultِ خامِ اکتیویتی برمی‌گردونه - برای همین [handleActivityResult] باید از
 * MainActivity.onActivityResult صدا زده بشه.
 */
class SubscriptionManager(private val activity: ComponentActivity) {
    private val helper = IabHelper(activity, MYKET_IAB_PUBLIC_KEY)
    private var connected = false
    private var latestInventory: Inventory? = null

    fun connect(onStateChange: (connected: Boolean) -> Unit) {
        helper.startSetup { result: IabResult ->
            if (!result.isSuccess) {
                connected = false
                onStateChange(false)
                return@startSetup
            }
            connected = true
            helper.queryInventoryAsync(true, subscriptionTiers.map { it.first }) { queryResult, inventory ->
                if (queryResult.isSuccess) latestInventory = inventory
                onStateChange(true)
            }
        }
    }

    fun disconnect() {
        helper.dispose()
        connected = false
    }

    fun getPrices(
        productIds: List<String>,
        onResult: (Map<String, String>) -> Unit,
        onError: () -> Unit,
    ) {
        val inventory = latestInventory
        if (!connected || inventory == null) {
            onError()
            return
        }
        val prices = productIds.mapNotNull { id ->
            inventory.getSkuDetails(id)?.price?.let { id to it }
        }.toMap()
        if (prices.isEmpty()) onError() else onResult(prices)
    }

    fun purchase(
        productId: String,
        onSucceed: (purchaseToken: String) -> Unit,
        onFailed: () -> Unit,
        onCanceled: () -> Unit,
    ) {
        if (!connected) {
            onFailed()
            return
        }
        /* پنلِ توسعه‌دهندگانِ مایکت گزینه‌ی جداگانه‌ای برای «اشتراکِ واقعی» نداره - هر محصولی که اونجا
           ساخته بشه یه محصولِ درون‌برنامه‌ایِ معمولیه (دقیقاً هم‌الگو با تصمیمِ قبلی برای کافه‌بازار،
           رجوع کن به Poolakey/purchaseProduct تو فلیورِ cafebazaar). قبلاً اینجا ITEM_TYPE_SUBS
           فرستاده می‌شد که با نوعِ واقعیِ محصول یکی نبود و باعثِ «خرید ناموفق» فوری می‌شد (گزارشِ
           بازبینِ مایکت). */
        helper.launchPurchaseFlow(
            activity,
            productId,
            IabHelper.ITEM_TYPE_INAPP,
            { result: IabResult, purchase: Purchase? ->
                when {
                    result.isFailure -> onFailed()
                    purchase == null -> onFailed()
                    else -> onSucceed(purchase.token)
                }
            },
            "sub_$productId",
        )
    }

    /** باگِ جدیِ رفع‌شده (گزارشِ بازبینِ مایکت رو نسخه‌ی ۳۴۸: «پرداخت موفق، ولی بعدِ برگشت به اپ
     * محصول فعال نمی‌شه»): این متد قبلاً کاملاً خالی بود، با این توجیهِ اشتباه که IabHelperِ مایکت
     * (پورتِ خودشون از الگوی کلاسیکِ Google IAB v3) نتیجه‌ی خرید رو خودش داخلی مدیریت می‌کنه. ولی
     * دقیقاً برعکس - این الگوی کلاسیک (مثلِ IabHelperِ اصلیِ گوگل که TrivialDrive/بقیه‌ی
     * سمپل‌های قدیمی ازش استفاده می‌کردن) *نیاز داره* اکتیویتیِ میزبان نتیجه‌ی onActivityResultِ
     * خام رو صریحاً به helper.handleActivityResult پاس بده تا اون بتونه Intentِ برگشتی رو پارس کنه
     * و listenerِ launchPurchaseFlow رو صدا بزنه. بدونِ این پاس‌دادن، بعدِ یه خریدِ *واقعاً موفق*
     * (پول از کاربر کم می‌شه)، callbackِ purchase() هیچ‌وقت اجرا نمی‌شه - نه onSucceed نه onFailed -
     * پس درخواستِ تاییدِ سمتِ سرور (verifySubscriptionPurchase) هم هیچ‌وقت فرستاده نمی‌شه و اشتراک
     * فعال نمی‌مونه؛ دقیقاً هم‌راستا با علامتِ گزارش‌شده. */
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        helper.handleActivityResult(requestCode, resultCode, data)
    }
}

/** null یعنی هنوز وصل نشده/در دسترس نیست (مثلاً مایکت رو گوشی نصب نیست) - صفحه‌ی اشتراک باید این
 * حالت رو مثل وب («این قابلیت فقط رو نسخه‌ی نصبی اپ کار می‌کنه») مدیریت کنه. */
val LocalSubscriptionManager = staticCompositionLocalOf<SubscriptionManager?> { null }
