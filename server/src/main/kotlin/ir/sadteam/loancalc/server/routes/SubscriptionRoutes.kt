package ir.sadteam.loancalc.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import ir.sadteam.loancalc.server.CafebazaarException
import ir.sadteam.loancalc.server.Db
import ir.sadteam.loancalc.server.MyketException
import ir.sadteam.loancalc.server.cafebazaarConfigured
import ir.sadteam.loancalc.server.execute
import ir.sadteam.loancalc.server.myketConfigured
import ir.sadteam.loancalc.server.queryOne
import ir.sadteam.loancalc.server.requireAuth
import ir.sadteam.loancalc.server.validateInAppPurchase
import ir.sadteam.loancalc.server.validateMyketPurchase
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

/* کدِ کوتاهِ پلن که تو دیتابیس (users.subscription_tier) ذخیره و به کلاینت برگردونده می‌شه - برای
   نمایشِ دقیقِ نوعِ اشتراک تو تنظیمات («اشتراک یک‌ماهه»/«سه‌ماهه»/...)، به‌جای شناسه‌ی خامِ محصول. */
private val PRODUCT_TIER_CODE = mapOf(
    "unlimited_loans_1m" to "1m",
    "unlimited_loans_3m" to "3m",
    "unlimited_loans_6m" to "6m",
    "unlimited_loans_1y" to "1y"
)

/* store رو نسخه‌های قدیمی‌ترِ اپ (قبل از اضافه‌شدنِ فلیورِ مایکت) اصلاً نمی‌فرستن - پیش‌فرضش
   cafebazaar می‌مونه که سازگار با رفتارِ قبلی بمونه. */
@Serializable
private data class VerifyBody(
    val productId: String? = null,
    val purchaseToken: String? = null,
    val store: String? = null,
)

@Serializable
private data class PurchaseDto(
    val productId: String,
    val tier: String? = null,
    val store: String,
    val durationDays: Int,
    val subscribedUntil: String,
    val createdAt: String,
)

@Serializable
private data class HistoryResponse(val ok: Boolean = true, val items: List<PurchaseDto>)

@Serializable
private data class VerifyResponse(val ok: Boolean = true, val subscribed: Boolean = true, val subscribedUntil: String)

fun Route.subscriptionRoutes() {
    route("/api/subscription") {
        /* کلاینت بعد از یه خرید موفق (purchaseProduct از پلاگین Poolakey) این رو صدا می‌زنه؛
           ما هم مستقیماً حرف کلاینت رو باور نمی‌کنیم، خودمون با API کافه‌بازار تایید می‌کنیم. */
        post("/verify") {
            val authed = call.requireAuth() ?: return@post

            val body = runCatching { call.receive<VerifyBody>() }.getOrNull()
            val productId = body?.productId
            val purchaseToken = body?.purchaseToken
            val store = body?.store ?: "cafebazaar"
            if (productId.isNullOrEmpty() || purchaseToken.isNullOrEmpty()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_input"))
                return@post
            }
            val durationDays = TIER_DURATION_DAYS[productId]
            if (durationDays == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "unknown_product"))
                return@post
            }
            if (store != "myket" && !cafebazaarConfigured()) {
                call.respond(HttpStatusCode.ServiceUnavailable, mapOf("error" to "cafebazaar_not_configured"))
                return@post
            }
            if (store == "myket" && !myketConfigured()) {
                call.respond(HttpStatusCode.ServiceUnavailable, mapOf("error" to "myket_not_configured"))
                return@post
            }

            val valid = try {
                if (store == "myket") validateMyketPurchase(productId, purchaseToken)
                else validateInAppPurchase(productId, purchaseToken)
            } catch (e: CafebazaarException) {
                call.respond(HttpStatusCode.BadGateway, mapOf("error" to "cafebazaar_validation_failed"))
                return@post
            } catch (e: MyketException) {
                call.respond(HttpStatusCode.BadGateway, mapOf("error" to "myket_validation_failed"))
                return@post
            }
            if (!valid) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "purchase_not_valid"))
                return@post
            }

            /* 🚨 جلوگیری از **تکرارِ یک رسید** (باگِ امنیتیِ رفع‌شده - مرداد ۱۴۰۵): قبلاً
               subscribed_until بی‌قید و شرط جلو می‌رفت و فقط ردیفِ تاریخچه با
               `ON CONFLICT(purchase_token) DO NOTHING` تکراری نمی‌شد. نتیجه: هر کسی می‌تونست
               **یه رسیدِ واقعی رو n بار بفرسته و n برابر اشتراک بگیره** (API استورها رسیدِ
               مصرف‌شده رو هم معتبر برمی‌گردونن، پس اعتبارسنجی جلوش رو نمی‌گرفت).
               حالا اگه این توکن قبلاً پردازش شده، بدونِ هیچ تمدیدی همون وضعیتِ فعلی برگردونده
               می‌شه - عمداً خطا نمی‌ده، چون `restorePurchases()` سمتِ کلاینت به‌طورِ عادی و مکرر
               همین رسیدها رو دوباره می‌فرسته و نباید خطا ببینه. */
            val alreadyProcessed = Db.withConnection { conn ->
                conn.queryOne(
                    "SELECT subscribed_until FROM subscription_purchases WHERE purchase_token = ?",
                    purchaseToken,
                ) { rs -> rs.getString("subscribed_until") }
            }
            if (alreadyProcessed != null) {
                val until = Db.withConnection { conn ->
                    conn.queryOne("SELECT subscribed_until FROM users WHERE id = ?", authed.uid) { rs ->
                        rs.getString("subscribed_until")
                    }
                } ?: alreadyProcessed
                call.respond(VerifyResponse(subscribedUntil = until))
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
            val tierCode = PRODUCT_TIER_CODE[productId]

            Db.withConnection { conn ->
                conn.execute(
                    "UPDATE users SET subscribed_until = ?, subscription_tier = ? WHERE id = ?",
                    newExpiry, tierCode, authed.uid,
                )
                /* ثبتِ خرید تو تاریخچه - purchase_token یکتاست، پس اگه همین خرید دوباره فرستاده بشه
                   (مثلاً restorePurchases بعدِ گم‌شدنِ callback) ردیفِ تکراری ساخته نمی‌شه. */
                conn.execute(
                    """
                    INSERT INTO subscription_purchases
                        (user_id, product_id, tier, store, purchase_token, duration_days, subscribed_until)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT(purchase_token) DO NOTHING
                    """.trimIndent(),
                    authed.uid, productId, tierCode, store, purchaseToken, durationDays, newExpiry,
                )
            }
            call.respond(VerifyResponse(subscribedUntil = newExpiry))
        }

        /* تاریخچه‌ی خریدهای همین کاربر - صفحه‌ی «اشتراک» تو اپ ازش برای لیستِ «اشتراک‌های خریداری‌شده»
           استفاده می‌کنه. خالی‌بودنش طبیعیه (خریدهای قبل از اضافه‌شدنِ این جدول ثبت نشدن). */
        get("/history") {
            val authed = call.requireAuth() ?: return@get
            val items = Db.withConnection { conn ->
                conn.prepareStatement(
                    """
                    SELECT product_id, tier, store, duration_days, subscribed_until, created_at
                    FROM subscription_purchases WHERE user_id = ? ORDER BY id DESC
                    """.trimIndent(),
                ).use { ps ->
                    ps.setLong(1, authed.uid)
                    ps.executeQuery().use { rs ->
                        val list = mutableListOf<PurchaseDto>()
                        while (rs.next()) {
                            list.add(
                                PurchaseDto(
                                    productId = rs.getString("product_id"),
                                    tier = rs.getString("tier"),
                                    store = rs.getString("store"),
                                    durationDays = rs.getInt("duration_days"),
                                    subscribedUntil = rs.getString("subscribed_until"),
                                    createdAt = rs.getString("created_at"),
                                ),
                            )
                        }
                        list
                    }
                }
            }
            call.respond(HistoryResponse(items = items))
        }
    }
}
