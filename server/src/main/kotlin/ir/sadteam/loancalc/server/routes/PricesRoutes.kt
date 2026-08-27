package ir.sadteam.loancalc.server.routes

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import ir.sadteam.loancalc.server.PriceService
import kotlinx.serialization.Serializable

/*
 * قیمتِ روزِ طلا/سکه/ارز/رمزارز - عمومی و بدونِ auth، مثلِ credit-rates. جزئیاتِ کش/سهمیه تو
 * PriceService.kt. اگه GitHub Secretِ PRICE_API_KEY هنوز ست نشده، prices خالی برمی‌گرده.
 */
@Serializable
private data class PricesResponse(val updatedAt: String?, val prices: Map<String, Double>)

fun Route.pricesRoutes() {
    get("/api/prices") {
        val (updatedAt, prices) = PriceService.currentPrices()
        call.respond(PricesResponse(updatedAt, prices))
    }
}
