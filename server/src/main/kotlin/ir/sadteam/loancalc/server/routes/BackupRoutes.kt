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

/*
 * پشتیبان‌گیری ابری چک‌ها و حساب‌ها (پورت مفهومیِ «پشتیبان‌گیری ابری از تمامی وام‌ها» - همون
 * فیچری که تو BenefitsScreen اپ بومی به‌عنوان مزیت اشتراک تبلیغ می‌شه ولی بک‌اندش وجود نداشت).
 * برخلاف /api/loans که یه آرایه‌ی JSON واقعی و تایپ‌شده نگه می‌داره (چون کلاینت وب هم مستقیم
 * می‌خوندش)، این‌جا سرور فقط یه blob مات ذخیره/برمی‌گردونه - همون رشته‌ی خامِ خروجیِ
 * ChequeRepository/AccountRepository.exportBackupJson تو اپ بومی؛ شکل داخلیِ JSON فقط دستِ
 * کلاینته، سرور بهش کاری نداره (اگه فرمتِ بکاپ تو اپ عوض بشه، این‌جا هیچ تغییری لازم نیست).
 *
 * GET همیشه مجازه (دیتای خودِ کاربره، حتی اگه اشتراکش الان منقضی شده باشه بازیابیِ بکاپِ قدیمی
 * نباید قفل بشه)؛ PUT فقط برای مشترک/دوره‌ی آزمایشی مجازه - سازگار با تبلیغِ «فیچر اشتراکی».
 */
@Serializable
private data class BackupBlobResponse(val data: String, val updatedAt: String?)

@Serializable
private data class BackupBlobRequest(val data: String)

private fun Route.backupBlobRoutes(path: String, table: String) {
    route(path) {
        get {
            val authed = call.requireAuth() ?: return@get
            val row = Db.withConnection { conn ->
                conn.queryOne("SELECT data, updated_at FROM $table WHERE user_id = ?", authed.uid) { rs ->
                    (rs.getString("data") ?: "{}") to rs.getString("updated_at")
                }
            }
            call.respond(BackupBlobResponse(data = row?.first ?: "{}", updatedAt = row?.second))
        }

        put {
            val authed = call.requireAuth() ?: return@put
            val body = runCatching { call.receive<BackupBlobRequest>() }.getOrNull()
            if (body == null || body.data.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_data"))
                return@put
            }

            val user = Db.withConnection { conn ->
                conn.queryOne(
                    "SELECT id, phone, subscribed, subscribed_until, subscription_tier, created_at FROM users WHERE id = ?", authed.uid
                ) { it.toUserRow() }
            }
            if (!isSubscribed(user)) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "subscription_required"))
                return@put
            }

            Db.withConnection { conn ->
                conn.execute(
                    """
                    INSERT INTO $table (user_id, data, updated_at) VALUES (?, ?, datetime('now'))
                    ON CONFLICT(user_id) DO UPDATE SET data = excluded.data, updated_at = excluded.updated_at
                    """.trimIndent(),
                    authed.uid, body.data
                )
            }

            call.respond(mapOf("ok" to true))
        }
    }
}

fun Route.chequesBackupRoutes() = backupBlobRoutes("/api/cheques", "cheques_backup")

fun Route.accountsBackupRoutes() = backupBlobRoutes("/api/accounts", "accounts_backup")
