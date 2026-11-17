package ir.sadteam.loancalc.server

/* ================= تایید خرید درون‌برنامه‌ای مایکت (Partners API) =================
   برای گرفتن MYKET_ACCESS_TOKEN باید تو پنل توسعه‌دهندگان مایکت (developer.myket.ir) بخشِ محصولاتِ
   درون‌برنامه‌ای، «توکن دسترسی جدید» بزنید. این توکن بینِ همه‌ی اپ‌های همون حساب مشترکه، هدرِ
   X-Access-Token هر درخواست باید همینو داشته باشه. */

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLParameter
import io.ktor.http.isSuccess
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val MYKET_ACCESS_TOKEN = env("MYKET_ACCESS_TOKEN")
private val MYKET_PACKAGE_NAME = env("MYKET_PACKAGE_NAME", "ir.sadteam.loancalc")

private const val VERIFY_URL_TMPL =
    "https://developer.myket.ir/api/partners/applications/:package_name/purchases/products/:product_id/verify"

private val myketHttpClient = HttpClient(CIO) {
    install(ContentNegotiation) { json() }
}

class MyketException(message: String) : Exception(message)

fun myketConfigured(): Boolean = MYKET_ACCESS_TOKEN.isNotEmpty()

@Serializable
private data class VerifyBody(val tokenId: String)

/* فرمتِ پاسخ حالا در مستنداتِ رسمیِ مایکت مشخص است (myket.ir/docs/en/technical/iab/web-application
   — بخشِ «Verify the purchase»): فیلدها `purchaseState` (۰ = موفق، ۱ = ناموفق)، `purchaseTime`،
   `kind`، `developerPayload`، `consumptionState` اند و مستندات صریحاً می‌گوید فقط وقتی
   `purchaseState` برابرِ ۰ باشد خرید معتبر است. پس برخلافِ قبل، نبودِ فیلد دیگر به‌معنای
   «معتبر» نیست (fail-closed)؛ و مثلِ کافه‌بازار، هیچ نشانه‌ای از «خریدِ تستی» و هیچ
   وب‌هوکِ ریفاندی هم نیست — رجوع کن به [PurchaseValidation] و [PurchaseRevalidation]. */
suspend fun validateMyketPurchase(productId: String, purchaseToken: String): PurchaseValidation {
    if (!myketConfigured()) throw MyketException("myket_not_configured")
    val url = VERIFY_URL_TMPL
        .replace(":package_name", MYKET_PACKAGE_NAME.encodeURLParameter())
        .replace(":product_id", productId.encodeURLParameter())

    val response = myketHttpClient.post(url) {
        header("X-Access-Token", MYKET_ACCESS_TOKEN)
        contentType(ContentType.Application.Json)
        setBody(Json.encodeToString(VerifyBody.serializer(), VerifyBody(tokenId = purchaseToken)))
    }
    if (!response.status.isSuccess()) return PurchaseValidation(valid = false)
    val text = runCatching { response.bodyAsText() }.getOrDefault("")
    Log.info("myket_verify", "پاسخِ تأییدِ مایکت", "product" to productId, "body" to text.take(300))
    val state = runCatching {
        Json.parseToJsonElement(text).jsonObject["purchaseState"]?.jsonPrimitive?.intOrNull
    }.getOrNull()
    return when (state) {
        0 -> PurchaseValidation(valid = true)
        1 -> PurchaseValidation(valid = false, refunded = true)
        // نبودِ فیلد یا مقدارِ ناشناخته = نامعتبر (قبلاً HTTP 2xx به‌تنهایی کافی بود).
        else -> PurchaseValidation(valid = false)
    }
}
