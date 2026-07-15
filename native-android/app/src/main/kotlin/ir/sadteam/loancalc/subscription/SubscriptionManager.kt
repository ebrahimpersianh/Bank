package ir.sadteam.loancalc.subscription

import androidx.activity.ComponentActivity
import androidx.compose.runtime.staticCompositionLocalOf
import ir.cafebazaar.poolakey.Connection
import ir.cafebazaar.poolakey.ConnectionState
import ir.cafebazaar.poolakey.Payment
import ir.cafebazaar.poolakey.config.PaymentConfiguration
import ir.cafebazaar.poolakey.config.SecurityCheck
import ir.cafebazaar.poolakey.request.PurchaseRequest

/** پورت CAFEBAZAAR_RSA_PUBLIC_KEY تو www/index.html - همون کلید واقعی که برای اپ کافه‌بازار
 * (applicationId مشترک ir.sadteam.loancalc) قبلاً ثبت و تایید شده. */
const val CAFEBAZAAR_RSA_PUBLIC_KEY =
    "MIHNMA0GCSqGSIb3DQEBAQUAA4G7ADCBtwKBrwCjfR4R/UMHznr0WXrUrnmzbiVQHT+jaupSCVLKJKixTsSvZxn5ZLo20xyh9s16BPIP3ppDsttotCM53O4jvJvmvPkVfmIRmkvwEIn9wADnl318Ozo7LmnmXQZ8v6diBbkpCpCk1zOWpeicVoiggRUPnmjPiMD5NjHISHWMVbWUrT8YdKmRab1OQzG5JUtnn8FjWf4lYPwMdUylQ3z5m2EO7GOP2EJvgZXRXSWyubUCAwEAAQ=="

/** پورت CAFEBAZAAR_SUBSCRIPTION_TIERS تو www/index.html - باید دقیقاً با پنل کافه‌بازار و
 * TIER_DURATION_DAYS تو server/src/routes/subscription.js یکی بمونه. */
val subscriptionTiers = listOf(
    "unlimited_loans_1m" to "اشتراک ۱ ماهه",
    "unlimited_loans_3m" to "اشتراک ۳ ماهه",
    "unlimited_loans_6m" to "اشتراک ۶ ماهه",
    "unlimited_loans_1y" to "اشتراک ۱ ساله",
)

/**
 * پوششی رو SDK بومی Poolakey (نه پلاگین Capacitor نسخه‌ی وب - همون کتابخونه‌ی زیرینش، ولی
 * مستقیم). عمداً Hilt-managed نیست چون `Payment`/`activityResultRegistry` به خودِ Activity
 * نیاز دارن؛ تو MainActivity ساخته می‌شه و از `onCreate` تا `onDestroy` زنده می‌مونه - دقیقاً
 * الگوی نمونه‌ی رسمی Poolakey (connect تو onCreate، disconnect تو onDestroy).
 *
 * برخلاف پلاگین Capacitor که فقط purchaseProduct/consumeProduct می‌ده، این کتابخونه واقعاً
 * subscribeProduct هم داره؛ ولی همچنان طبق تصمیم قبلی (رجوع کن به CLAUDE.md) هر پلن یه خرید
 * یک‌باره‌ی غیرقابل‌مصرفه، نه اشتراک واقعاً تمدیدشونده - چون تو پنل کافه‌بازار هر ۴ محصول از
 * نوع «غیرقابل‌مصرف» ثبت شدن، نه «اشتراک». عوض‌کردن این یعنی محصولات پنل هم باید از نو ساخته بشن.
 */
class SubscriptionManager(private val activity: ComponentActivity) {
    private val payment = Payment(
        context = activity,
        config = PaymentConfiguration(
            localSecurityCheck = SecurityCheck.Enable(rsaPublicKey = CAFEBAZAAR_RSA_PUBLIC_KEY),
        ),
    )

    private var connection: Connection? = null

    fun connect(onStateChange: (connected: Boolean) -> Unit) {
        connection = payment.connect {
            connectionSucceed { onStateChange(true) }
            connectionFailed { onStateChange(false) }
            disconnected { onStateChange(false) }
        }
    }

    fun disconnect() {
        connection?.disconnect()
    }

    private fun isConnected() = connection?.getState() == ConnectionState.Connected

    fun getPrices(
        productIds: List<String>,
        onResult: (Map<String, String>) -> Unit,
        onError: () -> Unit,
    ) {
        if (!isConnected()) {
            onError()
            return
        }
        payment.getInAppSkuDetails(skuIds = productIds) {
            getSkuDetailsSucceed { list -> onResult(list.associate { it.sku to it.price }) }
            getSkuDetailsFailed { onError() }
        }
    }

    fun purchase(
        productId: String,
        onSucceed: (purchaseToken: String) -> Unit,
        onFailed: () -> Unit,
        onCanceled: () -> Unit,
    ) {
        if (!isConnected()) {
            onFailed()
            return
        }
        payment.purchaseProduct(
            registry = activity.activityResultRegistry,
            request = PurchaseRequest(productId = productId, payload = "sub_$productId"),
        ) {
            failedToBeginFlow { onFailed() }
            purchaseSucceed { onSucceed(it.purchaseToken) }
            purchaseCanceled { onCanceled() }
            purchaseFailed { onFailed() }
        }
    }
}

/** null یعنی هنوز وصل نشده/در دسترس نیست (مثلاً کافه‌بازار رو گوشی نصب نیست) - صفحه‌ی اشتراک
 * باید این حالت رو مثل وب («این قابلیت فقط رو نسخه‌ی نصبی اپ کار می‌کنه») مدیریت کنه. */
val LocalSubscriptionManager = staticCompositionLocalOf<SubscriptionManager?> { null }

