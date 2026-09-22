package ir.sadteam.loancalc.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import ir.sadteam.loancalc.server.Db
import ir.sadteam.loancalc.server.env
import ir.sadteam.loancalc.server.execute
import ir.sadteam.loancalc.server.executeCounting
import ir.sadteam.loancalc.server.queryOne
import ir.sadteam.loancalc.server.requireAuth
import kotlinx.serialization.Serializable
import java.security.SecureRandom
import java.time.Instant

/*
 * 🎁 کدهای هدیه‌ی اشتراک.
 *
 * دو مسیر:
 *   POST /api/gift/redeem   - کاربرِ واردشده کد را خرج می‌کند (اشتراکش تمدید می‌شود)
 *   POST /api/gift/create   - ساختِ کد؛ فقط با ADMIN_TOKEN (برای خودِ صاحبِ برنامه)
 *
 * 🚨 چرا مدت **۱ تا ۱۵ روز** محدود است: این جدول جایزه است نه کانالِ فروش. کدی که
 * بتواند یک‌ساله بدهد، لو رفتنش یعنی از دست رفتنِ درآمد؛ سقفِ ۱۵ روز ضررِ بدترین حالت
 * را محدود می‌کند.
 */

/** بازه‌ی مجازِ جایزه - خواسته‌ی صریحِ کاربر: یک تا پانزده روز. */
private val REWARD_DAYS = 1..15

@Serializable
private data class RedeemBody(val code: String)

@Serializable
private data class RedeemResponse(val ok: Boolean = true, val days: Int, val subscribedUntil: String)

@Serializable
private data class CreateBody(
    val days: Int,
    val note: String? = null,
    /** چند کد با همین مشخصات؛ پیش‌فرض یکی. */
    val count: Int = 1,
    /** `null` یعنی بی‌انقضا. */
    val expiresAt: String? = null,
)

@Serializable
private data class CreateResponse(val ok: Boolean = true, val codes: List<String>)

/** کدِ خوانا: بی حروفِ هم‌شکل (O/0، I/1) تا موقعِ تایپِ دستی اشتباه نشود. */
private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

private fun newCode(): String {
    val rnd = SecureRandom()
    val body = (1..10).map { ALPHABET[rnd.nextInt(ALPHABET.length)] }.joinToString("")
    return "JIBAK-" + body.substring(0, 5) + "-" + body.substring(5)
}

fun Route.giftCodeRoutes() {
    route("/api/gift") {
        post("/redeem") {
            val authed = call.requireAuth() ?: return@post
            val body = runCatching { call.receive<RedeemBody>() }.getOrNull()
            val code = body?.code?.trim()?.uppercase()
            if (code.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_code"))
                return@post
            }

            // 🚨 همه‌ی کار در **یک تراکنش**: خواندنِ کد، علامت‌زدنش و تمدیدِ اشتراک.
            // دو درخواستِ هم‌زمان با یک کد وگرنه هر دو «مصرف‌نشده» می‌دیدند و دو بار
            // تمدید می‌شد - همان باگی که یک‌بار با رسیدِ خرید رخ داد.
            val outcome = Db.withConnection { conn ->
                conn.autoCommit = false
                try {
                    val row = conn.queryOne(
                        "SELECT days, used_by, expires_at FROM gift_codes WHERE code = ?", code,
                    ) { rs ->
                        Triple(rs.getInt("days"), rs.getObject("used_by"), rs.getString("expires_at"))
                    }
                    if (row == null) {
                        conn.rollback()
                        return@withConnection "not_found" to null
                    }
                    val (days, usedBy, expiresAt) = row
                    if (usedBy != null) {
                        conn.rollback()
                        return@withConnection "already_used" to null
                    }
                    val expired = expiresAt?.let {
                        runCatching { Instant.parse(it).toEpochMilli() < System.currentTimeMillis() }
                            .getOrDefault(false)
                    } ?: false
                    if (expired) {
                        conn.rollback()
                        return@withConnection "expired" to null
                    }
                    // برنده‌ی همین آپدیت (تعدادِ ردیفِ ۱) تنها کسی است که تمدید می‌گیرد.
                    val claimed = conn.executeCounting(
                        "UPDATE gift_codes SET used_by = ?, used_at = datetime('now') WHERE code = ? AND used_by IS NULL",
                        authed.uid, code,
                    )
                    if (claimed == 0) {
                        conn.rollback()
                        return@withConnection "already_used" to null
                    }
                    val currentUntil = conn.queryOne(
                        "SELECT subscribed_until FROM users WHERE id = ?", authed.uid,
                    ) { rs -> rs.getString("subscribed_until") }
                    // اشتراکِ فعال از دست نمی‌رود: از تاریخِ انقضای فعلی جلو می‌رویم، نه از الان.
                    val now = System.currentTimeMillis()
                    val currentExpiry = currentUntil
                        ?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() } ?: 0
                    val base = if (currentExpiry > now) currentExpiry else now
                    val expiry = Instant.ofEpochMilli(base + days * 24L * 60 * 60 * 1000).toString()
                    conn.execute("UPDATE users SET subscribed_until = ? WHERE id = ?", expiry, authed.uid)
                    conn.commit()
                    "ok" to RedeemResponse(days = days, subscribedUntil = expiry)
                } catch (e: Exception) {
                    runCatching { conn.rollback() }
                    throw e
                } finally {
                    runCatching { conn.autoCommit = true }
                }
            }

            val (status, payload) = outcome
            if (payload == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to status))
                return@post
            }
            call.respond(payload)
        }

        post("/create") {
            // ⚠️ بی `ADMIN_TOKEN` این مسیر **اصلاً باز نمی‌شود** - نه اینکه باز و بی‌محافظ
            // بماند. سکرتِ تنظیم‌نشده یعنی قابلیت خاموش است، نه بی‌قفل.
            val adminToken = env("ADMIN_TOKEN", "")
            val sent = call.request.headers["X-Admin-Token"]
            if (adminToken.isEmpty() || sent != adminToken) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "admin_only"))
                return@post
            }
            val body = runCatching { call.receive<CreateBody>() }.getOrNull()
            if (body == null || body.days !in REWARD_DAYS || body.count !in 1..100) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_request"))
                return@post
            }
            val codes = Db.withConnection { conn ->
                (1..body.count).map {
                    var code = newCode()
                    // برخوردِ کد عملاً ناممکن است، ولی چون کلیدِ اصلی است یک بار دوباره
                    // امتحان می‌کنیم تا درخواست به‌خاطرِ یک تصادف شکست نخورد.
                    repeat(3) {
                        val exists = conn.queryOne("SELECT 1 AS x FROM gift_codes WHERE code = ?", code) { true } != null
                        if (exists) code = newCode()
                    }
                    conn.execute(
                        "INSERT INTO gift_codes (code, days, note, expires_at) VALUES (?, ?, ?, ?)",
                        code, body.days, body.note, body.expiresAt,
                    )
                    code
                }
            }
            call.respond(CreateResponse(codes = codes))
        }
    }
}
