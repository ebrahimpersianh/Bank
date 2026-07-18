package ir.sadteam.loancalc.subscription

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.staticCompositionLocalOf
import ir.myket.billingclient.IabHelper
import ir.myket.billingclient.util.IabResult
import ir.myket.billingclient.util.Inventory
import ir.myket.billingclient.util.Purchase

/** کلیدِ عمومیِ اپ (نه رازِ دولوپر) که مایکت موقعِ ثبتِ اپ تو پنلش می‌ده - رجوع کن به
 * SUPPORT_EMAIL تو SettingsScreen.kt برای الگوی مشابه: placeholder ئه، تا وقتی کاربر خودش اپ رو
 * تو مایکت ثبت نکرده و این کلید رو از پنلش نگرفته، خریدِ واقعی رو مایکت کار نمی‌کنه (فقط پیامِ
 * خطا می‌ده، کرش نمی‌کنه - رجوع کن به isConnected/onStateChange پایین). */
const val MYKET_IAB_PUBLIC_KEY = "MYKET_IAB_PUBLIC_KEY_PLACEHOLDER"

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
        helper.launchPurchaseFlow(
            activity,
            productId,
            IabHelper.ITEM_TYPE_SUBS,
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

    /** پورتِ اجباریِ الگوی کلاسیکِ IAB v3 - بدونِ این، نتیجه‌ی launchPurchaseFlow هیچ‌وقت به
     * onIabPurchaseFinished نمی‌رسه، چون خریدِ مایکت (برخلافِ Poolakey) یه اکتیویتیِ خارجی با
     * startIntentSenderForResult باز می‌کنه، نه یه ActivityResultLauncher مدرن. */
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        helper.handleActivityResult(requestCode, resultCode, data)
    }
}

/** null یعنی هنوز وصل نشده/در دسترس نیست (مثلاً مایکت رو گوشی نصب نیست) - صفحه‌ی اشتراک باید این
 * حالت رو مثل وب («این قابلیت فقط رو نسخه‌ی نصبی اپ کار می‌کنه») مدیریت کنه. */
val LocalSubscriptionManager = staticCompositionLocalOf<SubscriptionManager?> { null }
