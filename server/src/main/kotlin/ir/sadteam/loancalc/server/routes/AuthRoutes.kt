package ir.sadteam.loancalc.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import ir.sadteam.loancalc.server.Db
import ir.sadteam.loancalc.server.SmsSendException
import ir.sadteam.loancalc.server.env
import ir.sadteam.loancalc.server.execute
import ir.sadteam.loancalc.server.insertReturningId
import ir.sadteam.loancalc.server.isSubscribed
import ir.sadteam.loancalc.server.parseUtc
import ir.sadteam.loancalc.server.queryOne
import ir.sadteam.loancalc.server.requireAuth
import ir.sadteam.loancalc.server.sendOtpSms
import ir.sadteam.loancalc.server.signToken
import ir.sadteam.loancalc.server.toUserRow
import ir.sadteam.loancalc.server.trialDaysLeftIfApplicable
import kotlinx.serialization.Serializable
import java.security.MessageDigest
import java.security.SecureRandom

private val PHONE_RE = Regex("^09\\d{9}$")
private const val OTP_TTL_MS = 2 * 60 * 1000L // ۲ دقیقه اعتبار کد
private const val OTP_RESEND_COOLDOWN_MS = 60 * 1000L // حداقل فاصله بین دو درخواست کد برای یه شماره
private const val MAX_VERIFY_ATTEMPTS = 5

private val secureRandom = SecureRandom()

/* حسابِ تستِ دائمی (برای فرمِ «حساب تستی»ِ بررسی‌کننده‌های کافه‌بازار/مایکت + تستِ خودِ توسعه‌دهنده):
   اگه TEST_ACCOUNT_PHONE و TEST_ACCOUNT_CODE تو .env ست شده باشن، برای اون یه شماره هیچ پیامکی
   ارسال نمی‌شه و کدِ تایید همیشه همون مقدارِ ثابته - یعنی حتی بعد از فعال‌شدنِ پیامکِ واقعیِ
   ملی‌پیامک هم این حساب کار می‌کنه (شماره‌ش غیرواقعیه و پیامک بهش نمی‌رسید). برای بقیه‌ی شماره‌ها
   هیچ رفتاری عوض نمی‌شه؛ تا وقتی این دو env ست نشن، این قابلیت کلاً خاموشه. */
private val TEST_ACCOUNT_PHONE = env("TEST_ACCOUNT_PHONE")
private val TEST_ACCOUNT_CODE = env("TEST_ACCOUNT_CODE")

private fun isTestAccount(phone: String): Boolean =
    TEST_ACCOUNT_PHONE.isNotEmpty() && TEST_ACCOUNT_CODE.isNotEmpty() && phone == TEST_ACCOUNT_PHONE

private fun hashCode(code: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(code.toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
}

@Serializable
private data class RequestOtpBody(val phone: String? = null)

@Serializable
private data class VerifyOtpBody(val phone: String? = null, val code: String? = null)

@Serializable
private data class VerifyOtpResponse(
    val token: String,
    val phone: String,
    val subscribed: Boolean,
    val subscribedUntil: String?,
    val subscriptionTier: String?,
    val trialDaysLeft: Int?,
)

@Serializable
private data class MeResponse(
    val phone: String,
    val subscribed: Boolean,
    val subscribedUntil: String?,
    val subscriptionTier: String?,
    val trialDaysLeft: Int?,
    /** true یعنی این کاربر ۱۵ روزِ هدیه‌ی «قدیمی‌بودن» گرفته - اپ جمله‌ی اضافه رو نشون می‌ده. */
    val legacyGift: Boolean = false,
)

private data class OtpRow(val id: Long, val codeHash: String, val expiresAt: Long, val attempts: Int)

fun Route.authRoutes() {
    route("/api/auth") {
        post("/request-otp") {
            val body = runCatching { call.receive<RequestOtpBody>() }.getOrNull()
            val phone = (body?.phone ?: "").trim()
            if (!PHONE_RE.matches(phone)) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_phone"))
                return@post
            }

            val recentCreatedAt = Db.withConnection { conn ->
                conn.queryOne("SELECT created_at FROM otps WHERE phone = ? ORDER BY id DESC LIMIT 1", phone) { rs ->
                    rs.getString("created_at")
                }
            }
            if (recentCreatedAt != null && System.currentTimeMillis() - parseUtc(recentCreatedAt) < OTP_RESEND_COOLDOWN_MS) {
                call.respond(HttpStatusCode.TooManyRequests, mapOf("error" to "too_soon"))
                return@post
            }

            // حسابِ تست: کدِ ثابت از env، بدونِ ارسالِ پیامک (رجوع کن به کامنتِ TEST_ACCOUNT_PHONE بالا).
            val testAccount = isTestAccount(phone)
            val code = if (testAccount) TEST_ACCOUNT_CODE else (10000 + secureRandom.nextInt(90000)).toString() // ۵ رقمی
            val expiresAt = System.currentTimeMillis() + OTP_TTL_MS
            Db.withConnection { conn ->
                conn.execute(
                    "INSERT INTO otps (phone, code_hash, expires_at) VALUES (?, ?, ?)",
                    phone, hashCode(code), expiresAt
                )
            }

            if (!testAccount) {
                try {
                    sendOtpSms(phone, code)
                } catch (e: SmsSendException) {
                    println("[SMS-FAIL] ارسالِ OTP برای $phone شکست خورد: ${e.message}")
                    call.respond(HttpStatusCode.BadGateway, mapOf("error" to "sms_send_failed"))
                    return@post
                }
            }

            call.respond(mapOf("ok" to true))
        }

        post("/verify-otp") {
            val body = runCatching { call.receive<VerifyOtpBody>() }.getOrNull()
            val phone = (body?.phone ?: "").trim()
            val code = (body?.code ?: "").trim()
            if (!PHONE_RE.matches(phone) || code.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_input"))
                return@post
            }

            val otp = Db.withConnection { conn ->
                conn.queryOne("SELECT * FROM otps WHERE phone = ? ORDER BY id DESC LIMIT 1", phone) { rs ->
                    OtpRow(rs.getLong("id"), rs.getString("code_hash"), rs.getLong("expires_at"), rs.getInt("attempts"))
                }
            }
            if (otp == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "no_otp_requested"))
                return@post
            }
            if (otp.attempts >= MAX_VERIFY_ATTEMPTS) {
                call.respond(HttpStatusCode.TooManyRequests, mapOf("error" to "too_many_attempts"))
                return@post
            }
            if (System.currentTimeMillis() > otp.expiresAt) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "code_expired"))
                return@post
            }

            Db.withConnection { conn -> conn.execute("UPDATE otps SET attempts = attempts + 1 WHERE id = ?", otp.id) }
            if (hashCode(code) != otp.codeHash) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "wrong_code"))
                return@post
            }

            Db.withConnection { conn -> conn.execute("DELETE FROM otps WHERE id = ?", otp.id) } // یک‌بارمصرف

            var user = Db.withConnection { conn ->
                conn.queryOne(
                    "SELECT id, phone, subscribed, subscribed_until, subscription_tier, created_at FROM users WHERE phone = ?", phone
                ) { it.toUserRow() }
            }
            if (user == null) {
                val newId = Db.withConnection { conn -> conn.insertReturningId("INSERT INTO users (phone) VALUES (?)", phone) }
                Db.withConnection { conn -> conn.execute("INSERT INTO loans (user_id, data) VALUES (?, '[]')", newId) }
                // بعد از insert دوباره از دیتابیس می‌خونیم (نه یه آبجکت دستیِ ناقص) تا created_at
                // واقعی (لازم برای محاسبه‌ی دوره‌ی آزمایشی ۷ روزه‌ی isSubscribed) رو داشته باشیم.
                user = Db.withConnection { conn ->
                    conn.queryOne(
                        "SELECT id, phone, subscribed, subscribed_until, subscription_tier, created_at FROM users WHERE id = ?", newId
                    ) { it.toUserRow() }
                }!!
            }

            val token = signToken(user.id, user.phone)
            call.respond(
                VerifyOtpResponse(
                    token = token,
                    phone = user.phone,
                    subscribed = isSubscribed(user),
                    subscribedUntil = user.subscribedUntil,
                    subscriptionTier = user.subscriptionTier,
                    trialDaysLeft = trialDaysLeftIfApplicable(user)
                )
            )
        }

        /* وضعیت فعلی حساب (از جمله اشتراک) — کلاینت بعد از باز شدن اپ این رو صدا می‌زنه تا اگه
           اشتراک از جای دیگه (مثلاً گوشی دیگه، یا بعداً از طریق خرید درون‌برنامه‌ای کافه‌بازار)
           فعال شده باشه، بدون نیاز به لاگین مجدد باخبر بشه. */
        get("/me") {
            val authed = call.requireAuth() ?: return@get
            val user = Db.withConnection { conn ->
                conn.queryOne(
                    "SELECT id, phone, subscribed, subscribed_until, subscription_tier, created_at FROM users WHERE id = ?", authed.uid
                ) { it.toUserRow() }
            }
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "user_not_found"))
                return@get
            }
            call.respond(
                MeResponse(
                    phone = user.phone,
                    subscribed = isSubscribed(user),
                    legacyGift = user.legacyGift,
                    subscribedUntil = user.subscribedUntil,
                    subscriptionTier = user.subscriptionTier,
                    trialDaysLeft = trialDaysLeftIfApplicable(user)
                )
            )
        }

        /* حذف کامل حساب - الزامِ استانداردِ فروشگاه‌های اپ برای هر اپی که با شماره‌موبایل لاگین
           می‌گیره و داده‌ی کاربر رو سمت سرور نگه می‌داره (نه فقط یه لاگ‌اوت محلی). چون هیچ FK ای تو
           Db.kt با ON DELETE CASCADE تعریف نشده، هر جدولِ وابسته به user_id/phone رو دستی و قبل از
           خودِ ردیفِ users پاک می‌کنیم؛ اگه جدولِ جدیدی به user_id/phone وابسته اضافه شد، همینجا هم
           باید اضافه بشه. */
        delete("/account") {
            val authed = call.requireAuth() ?: return@delete
            Db.withConnection { conn ->
                conn.execute("DELETE FROM loans WHERE user_id = ?", authed.uid)
                conn.execute("DELETE FROM cheques_backup WHERE user_id = ?", authed.uid)
                conn.execute("DELETE FROM accounts_backup WHERE user_id = ?", authed.uid)
                conn.execute("DELETE FROM otps WHERE phone = ?", authed.phone)
                conn.execute("DELETE FROM users WHERE id = ?", authed.uid)
            }
            call.respond(mapOf("ok" to true))
        }
    }
}
