package ir.sadteam.loancalc.server

/* ================= تایید خرید درون‌برنامه‌ای کافه‌بازار (Developer API v2) =================
   برای گرفتن CAFEBAZAAR_CLIENT_ID/SECRET/REFRESH_TOKEN باید تو پنل توسعه‌دهندگان کافه‌بازار
   (pardakht.cafebazaar.ir/panel/developer-api) یه کلاینت API بسازید و یک‌بار فلوی OAuth رو
   دستی طی کنید تا refresh_token بگیرید؛ جزئیات تو server/README.md هست.
   مستندات رسمی: https://developers.cafebazaar.ir/en/guidelines/in-app-billing/api/validation */

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.Parameters
import io.ktor.http.encodeURLParameter
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

private val CAFEBAZAAR_CLIENT_ID = env("CAFEBAZAAR_CLIENT_ID")
private val CAFEBAZAAR_CLIENT_SECRET = env("CAFEBAZAAR_CLIENT_SECRET")
private val CAFEBAZAAR_REFRESH_TOKEN = env("CAFEBAZAAR_REFRESH_TOKEN")
private val CAFEBAZAAR_PACKAGE_NAME = env("CAFEBAZAAR_PACKAGE_NAME", "ir.sadteam.loancalc")

private const val TOKEN_URL = "https://pardakht.cafebazaar.ir/devapi/v2/auth/token/"
private const val PURCHASE_URL_TMPL =
    "https://pardakht.cafebazaar.ir/devapi/v2/api/validate/:package_name/inapp/:purchase_id/purchases/:purchase_token/"

private var cachedAccessToken: String? = null
private var cachedAccessTokenExpiresAt: Long = 0

private val cafebazaarHttpClient = HttpClient(CIO) {
    install(ContentNegotiation) { json() }
}

class CafebazaarException(message: String) : Exception(message)

fun cafebazaarConfigured(): Boolean =
    CAFEBAZAAR_CLIENT_ID.isNotEmpty() && CAFEBAZAAR_CLIENT_SECRET.isNotEmpty() && CAFEBAZAAR_REFRESH_TOKEN.isNotEmpty()

private suspend fun getAccessToken(): String {
    cachedAccessToken?.let { if (System.currentTimeMillis() < cachedAccessTokenExpiresAt) return it }

    val response = cafebazaarHttpClient.submitForm(
        url = TOKEN_URL,
        formParameters = Parameters.build {
            append("grant_type", "refresh_token")
            append("refresh_token", CAFEBAZAAR_REFRESH_TOKEN)
            append("client_id", CAFEBAZAAR_CLIENT_ID)
            append("client_secret", CAFEBAZAAR_CLIENT_SECRET)
        }
    )
    val json = runCatching { response.body<JsonObject>() }.getOrNull()
    val accessToken = json?.get("access_token")?.jsonPrimitive?.contentOrNull
    if (!response.status.isSuccess() || accessToken == null) {
        throw CafebazaarException("cafebazaar_token_refresh_failed")
    }
    val expiresIn = json["expires_in"]?.jsonPrimitive?.longOrNull ?: 0
    cachedAccessToken = accessToken
    /* کمی زودتر از انقضای واقعی منقضی‌ش می‌کنیم که وسط یه درخواست expire نشه */
    cachedAccessTokenExpiresAt = System.currentTimeMillis() + (expiresIn - 60) * 1000
    return accessToken
}

/* محصول رو به‌عنوان «غیرقابل‌مصرف» (نه اشتراک تمدیدشونده) تایید می‌کنه — چون کلاینت هم از
   purchaseProduct (نه subscribeProduct) استفاده می‌کنه. purchaseState=0 یعنی خریداری‌شده
   و برگشت‌نخورده (0 = purchased, 1 = refunded, طبق مستندات کافه‌بازار). */
suspend fun validateInAppPurchase(productId: String, purchaseToken: String): Boolean {
    val accessToken = getAccessToken()
    val url = PURCHASE_URL_TMPL
        .replace(":package_name", CAFEBAZAAR_PACKAGE_NAME.encodeURLParameter())
        .replace(":purchase_id", productId.encodeURLParameter())
        .replace(":purchase_token", purchaseToken.encodeURLParameter())

    val response = cafebazaarHttpClient.get(url) {
        parameter("access_token", accessToken)
    }
    if (!response.status.isSuccess()) return false
    val json = runCatching { response.body<JsonObject>() }.getOrNull() ?: return false
    return json["purchaseState"]?.jsonPrimitive?.intOrNull == 0
}
