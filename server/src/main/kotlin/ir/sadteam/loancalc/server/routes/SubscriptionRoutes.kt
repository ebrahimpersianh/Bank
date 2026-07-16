package ir.sadteam.loancalc.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import ir.sadteam.loancalc.server.CafebazaarException
import ir.sadteam.loancalc.server.Db
import ir.sadteam.loancalc.server.cafebazaarConfigured
import ir.sadteam.loancalc.server.execute
import ir.sadteam.loancalc.server.queryOne
import ir.sadteam.loancalc.server.requireAuth
import ir.sadteam.loancalc.server.validateInAppPurchase
import kotlinx.serialization.Serializable
import java.time.Instant

/* هر پلن یه محصولِ «خرید یک‌باره»‌ی جداگانه‌ست تو پنل کافه‌بازار (نه اشتراک واقعی تمدیدشونده،
   چون پلاگین Poolakey که وصله فقط purchaseProduct رو می‌ده) — این نگاشت شناسه‌ی محصول به تعداد
   روزی هست که با خریدش به subscribed_until کاربر اضافه می‌شه. */
private val TIER_DURATION_DAYS = mapOf(
    "unlimited_loans_1m" to 30,
    "unlimited_loans_3m" to 90,
    "unlimited_loans_6m" to 180,
    "unlimited_loans_1y" to 365
)

@Serializable
private data class VerifyBody(val productId: String? = null, val purchaseToken: String? = null)

@Serializable
private data class VerifyResponse(val ok: Boolean = true, val subscribed: Boolean = true, val subscribedUntil: String)

fun Route.subscriptionRoutes() {
    route("/api/subscription") {
        /* کلاینت بعد از یه خرید موفق (purchaseProduct از پلاگین Poolakey) این رو صدا می‌زنه؛
           ما هم مستقیماً حرف کلاینت رو باور نمی‌کنیم، خودمون با API کافه‌بازار تایید می‌کنیم. */
        post("/verify") {
            val authed = call.requireAuth() ?: return@post
            if (!cafebazaarConfigured()) {
                call.respond(HttpStatusCode.ServiceUnavailable, mapOf("error" to "cafebazaar_not_configured"))
                return@post
            }

            val body = runCatching { call.receive<VerifyBody>() }.getOrNull()
            val productId = body?.productId
            val purchaseToken = body?.purchaseToken
            if (productId.isNullOrEmpty() || purchaseToken.isNullOrEmpty()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_input"))
                return@post
            }
            val durationDays = TIER_DURATION_DAYS[productId]
            if (durationDays == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "unknown_product"))
                return@post
            }

            val valid = try {
                validateInAppPurchase(productId, purchaseToken)
            } catch (e: CafebazaarException) {
                call.respond(HttpStatusCode.BadGateway, mapOf("error" to "cafebazaar_validation_failed"))
                return@post
            }
            if (!valid) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "purchase_not_valid"))
                return@post
            }

            /* اگه اشتراک قبلی هنوز فعاله، از رو همون تاریخ انقضا جلو می‌ریم (نه از الان) تا خرید
               زودتر از موعد، مدت باقی‌مونده رو از دست ندی. */
            val currentSubscribedUntil = Db.withConnection { conn ->
                conn.queryOne("SELECT subscribed_until FROM users WHERE id = ?", authed.uid) { rs ->
                    rs.getString("subscribed_until")
                }
            }
            val now = System.currentTimeMillis()
            val currentExpiry = currentSubscribedUntil
                ?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() } ?: 0
            val base = if (currentExpiry > now) currentExpiry else now
            val newExpiry = Instant.ofEpochMilli(base + durationDays * 24L * 60 * 60 * 1000).toString()

            Db.withConnection { conn -> conn.execute("UPDATE users SET subscribed_until = ? WHERE id = ?", newExpiry, authed.uid) }
            call.respond(VerifyResponse(subscribedUntil = newExpiry))
        }
    }
}
