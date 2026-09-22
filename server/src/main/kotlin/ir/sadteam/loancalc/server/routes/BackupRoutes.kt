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
/** پاسخِ موفقِ نوشتن - نسخه‌ی تازه برمی‌گردد تا کلاینت برای نوشتنِ بعدی داشته باشدش. */
@Serializable
data class WriteOkResponse(val ok: Boolean = true, val revision: Long)

@Serializable
private data class BackupBlobResponse(val data: String, val updatedAt: String?, val revision: Long = 0)

@Serializable
/**
 * [expectedRevision] = نسخه‌ای که کلاینت آخرین بار از سرور گرفته.
 *
 * 🚨 اگر با نسخه‌ی فعلیِ سرور نخواند یعنی **گوشیِ دیگری زودتر نوشته** و این نوشتن
 * داده‌ی تازه‌تر را پاک می‌کرد؛ سرور ۴۰۹ می‌دهد و کلاینت باید اول بگیرد.
 * `null` = کلاینتِ نسخه‌ی قدیمی، همان رفتارِ قبلیِ «آخرین نوشته برنده».
 */
private data class BackupBlobRequest(val data: String, val expectedRevision: Long? = null)

private fun Route.backupBlobRoutes(path: String, table: String) {
    route(path) {
        get {
            val authed = call.requireAuth() ?: return@get
            val row = Db.withConnection { conn ->
                conn.queryOne("SELECT data, updated_at, revision FROM $table WHERE user_id = ?", authed.uid) { rs ->
                    Triple(rs.getString("data") ?: "{}", rs.getString("updated_at"), rs.getLong("revision"))
                }
            }
            call.respond(
                BackupBlobResponse(
                    data = row?.first ?: "{}",
                    updatedAt = row?.second,
                    revision = row?.third ?: 0,
                ),
            )
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

            val newRevision = Db.withConnection { conn ->
                conn.autoCommit = false
                try {
                    val current = conn.queryOne(
                        "SELECT revision FROM $table WHERE user_id = ?", authed.uid,
                    ) { it.getLong("revision") } ?: 0L
                    if (body.expectedRevision != null && body.expectedRevision != current) {
                        conn.rollback()
                        return@withConnection null
                    }
                    val next = current + 1
                    conn.execute(
                        """
                        INSERT INTO $table (user_id, data, revision, updated_at) VALUES (?, ?, ?, datetime('now'))
                        ON CONFLICT(user_id) DO UPDATE SET
                            data = excluded.data,
                            revision = excluded.revision,
                            updated_at = excluded.updated_at
                        """.trimIndent(),
                        authed.uid, body.data, next,
                    )
                    conn.commit()
                    next
                } catch (e: Exception) {
                    conn.rollback()
                    throw e
                } finally {
                    conn.autoCommit = true
                }
            }

            if (newRevision == null) {
                // کلاینت باید اول بگیرد، بعد دوباره بنویسد - سکوت یعنی پاک‌شدنِ داده‌ی گوشیِ دیگر.
                call.respond(HttpStatusCode.Conflict, mapOf("error" to "revision_conflict"))
                return@put
            }
            // ⚠️ `mapOf("ok" to true, "revision" to 1L)` سریالایز نمی‌شود (دو نوعِ متفاوت
            // در یک Map)؛ پاسخ باید یک تایپِ مشخص باشد.
            call.respond(WriteOkResponse(revision = newRevision))
        }
    }
}

fun Route.chequesBackupRoutes() = backupBlobRoutes("/api/cheques", "cheques_backup")

fun Route.accountsBackupRoutes() = backupBlobRoutes("/api/accounts", "accounts_backup")
