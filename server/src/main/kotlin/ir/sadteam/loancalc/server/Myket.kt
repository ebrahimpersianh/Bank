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

/* فرمتِ دقیقِ ریسپانسِ موفق تو مستنداتِ پنلِ مایکت مشخص نشده (فقط فرمتِ درخواست مستند شده)، برای
   همین به‌جای حدس‌زدنِ اسمِ یه فیلد، فقط بر اساسِ کدِ HTTP قضاوت می‌کنیم: 2xx یعنی توکنِ خرید معتبر
   تایید شده، هر چیزِ دیگه (404/400/401/...) یعنی نامعتبر یا خطا. */
suspend fun validateMyketPurchase(productId: String, purchaseToken: String): Boolean {
    if (!myketConfigured()) throw MyketException("myket_not_configured")
    val url = VERIFY_URL_TMPL
        .replace(":package_name", MYKET_PACKAGE_NAME.encodeURLParameter())
        .replace(":product_id", productId.encodeURLParameter())

    val response = myketHttpClient.post(url) {
        header("X-Access-Token", MYKET_ACCESS_TOKEN)
        contentType(ContentType.Application.Json)
        setBody(Json.encodeToString(VerifyBody.serializer(), VerifyBody(tokenId = purchaseToken)))
    }
    return response.status.isSuccess()
}
