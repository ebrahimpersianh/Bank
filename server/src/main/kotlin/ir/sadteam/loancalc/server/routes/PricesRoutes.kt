package ir.sadteam.loancalc.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import ir.sadteam.loancalc.server.PriceService
import kotlinx.serialization.Serializable

/*
 * قیمتِ روزِ طلا/سکه/ارز/رمزارز - عمومی و بدونِ auth، مثلِ credit-rates. جزئیاتِ کش/سهمیه/نگاشتِ
 * نمادها تو PriceService.kt. اگه GitHub Secretِ PRICE_API_KEY ست نشده باشه، prices خالی
 * برمی‌گرده (خطا نه) تا اپ بتونه بی‌سروصدا «—» نشون بده.
 */
@Serializable
private data class PricesResponse(
    val updatedAt: String?,
    /** کلید = نمادِ کاتالوگِ اپ (`BTC`, `GOLD_18`, ...)، مقدار = **ریال**. */
    val prices: Map<String, Double>,
    /** نمادهای خامِ سرویس - فقط برای دیباگ/تنظیمِ نگاشت، اپ ازش استفاده نمی‌کنه. */
    val raw: Map<String, Double>,
)

@Serializable
private data class PricePoint(val date: String, val price: Double)

@Serializable
private data class PriceHistoryResponse(val symbol: String, val points: List<PricePoint>)

fun Route.pricesRoutes() {
    get("/api/prices") {
        val snapshot = PriceService.currentPrices()
        call.respond(PricesResponse(snapshot.updatedAt, snapshot.prices, snapshot.raw))
    }

    // تاریخچه‌ی یه نماد - `?symbol=BTC&days=30`. مبنای «نسبت به ماهِ قبل» و نمودارِ صفحه‌ی
    // جزئیاتِ دارایی. داده‌ش از اسنپ‌شاتِ روزانه‌ی خودمونه، پس از روزِ اولِ راه‌اندازی پر می‌شه.
    get("/api/prices/history") {
        val symbol = call.request.queryParameters["symbol"]
        if (symbol.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "symbol_required"))
            return@get
        }
        val days = call.request.queryParameters["days"]?.toIntOrNull() ?: 30
        val points = PriceService.history(symbol, days).map { PricePoint(it.first, it.second) }
        call.respond(PriceHistoryResponse(symbol, points))
    }
}
