package ir.sadteam.loancalc.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import ir.sadteam.loancalc.server.Db
import ir.sadteam.loancalc.server.execute
import ir.sadteam.loancalc.server.isSubscribed
import ir.sadteam.loancalc.server.queryOne
import ir.sadteam.loancalc.server.requireAuth
import ir.sadteam.loancalc.server.toUserRow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement

/* همگام‌سازی به سبک «کل آرایه رو بگیر/بده» — چون سمت کلاینت هم currentLoans همیشه یه آرایه‌ی
   کامل JSON تو localStorage بوده، همین‌جوری هم رو سرور نگه می‌داریم؛ منطق merge/تعارض (کدوم
   گوشی برنده باشه) سمت کلاینته، نه اینجا. loans یه JsonElement خامه (نه یه data class مشخص)
   چون شکل دقیق هر وام کاملاً دست کلاینته، سرور فقط یه blob رو ذخیره/برمی‌گردونه. */

@Serializable
private data class LoansGetResponse(val loans: JsonElement, val updatedAt: String?)

@Serializable
private data class LoansPutBody(val loans: JsonElement? = null)

fun Route.loansRoutes() {
    route("/api/loans") {
        get {
            val authed = call.requireAuth() ?: return@get
            val row = Db.withConnection { conn ->
                conn.queryOne("SELECT data, updated_at FROM loans WHERE user_id = ?", authed.uid) { rs ->
                    (rs.getString("data") ?: "[]") to rs.getString("updated_at")
                }
            }
            val loansJson = Json.parseToJsonElement(row?.first ?: "[]")
            call.respond(LoansGetResponse(loans = loansJson, updatedAt = row?.second))
        }

        put {
            val authed = call.requireAuth() ?: return@put
            val body = runCatching { call.receive<LoansPutBody>() }.getOrNull()
            val loans = body?.loans
            if (loans !is JsonArray) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_loans"))
                return@put
            }

            /* بدون اشتراک فقط یه وام مجازه؛ این جلوی دور زدن محدودیت از طریق فراخوانی مستقیم API
               رو می‌گیره (منطق اصلی/پیام به کاربر سمت کلاینته، این فقط یه لایه‌ی دفاعی سمت سرورـه) */
            val user = Db.withConnection { conn ->
                conn.queryOne(
                    "SELECT id, phone, subscribed, subscribed_until, created_at FROM users WHERE id = ?", authed.uid
                ) { it.toUserRow() }
            }
            if (!isSubscribed(user) && loans.size > 1) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "subscription_required"))
                return@put
            }

            Db.withConnection { conn ->
                conn.execute(
                    """
                    INSERT INTO loans (user_id, data, updated_at) VALUES (?, ?, datetime('now'))
                    ON CONFLICT(user_id) DO UPDATE SET data = excluded.data, updated_at = excluded.updated_at
                    """.trimIndent(),
                    authed.uid, loans.toString()
                )
            }

            call.respond(mapOf("ok" to true))
        }
    }
}
