package ir.sadteam.loancalc.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import java.util.Date

val JWT_SECRET: String = env("JWT_SECRET").ifEmpty {
    throw IllegalStateException("JWT_SECRET تو env تنظیم نشده — بدون این، توکن‌های ورود قابل جعل می‌شن.")
}

private val jwtAlgorithm = Algorithm.HMAC256(JWT_SECRET)
private const val TOKEN_TTL_MS = 90L * 24 * 60 * 60 * 1000 // ۹۰ روز

fun signToken(uid: Long, phone: String, sessionVersion: Long = 0L): String =
    JWT.create()
        .withClaim("uid", uid)
        .withClaim("phone", phone)
        // نسخه‌ی نشست: با هر «خروج از همه‌ی دستگاه‌ها»/حذفِ حساب یکی بالا می‌رود و همه‌ی
        // توکن‌های قبلی همان لحظه بی‌اعتبار می‌شوند - بی این، توکنِ صادرشده تا ۹۰ روز
        // معتبر می‌مانْد، حتی بعد از حذفِ حساب.
        .withClaim("sv", sessionVersion)
        .withExpiresAt(Date(System.currentTimeMillis() + TOKEN_TTL_MS))
        .sign(jwtAlgorithm)

data class AuthedUser(val uid: Long, val phone: String, val sessionVersion: Long = 0L)

private fun verifyToken(token: String): AuthedUser {
    val decoded = JWT.require(jwtAlgorithm).build().verify(token)
    val uid = decoded.getClaim("uid").asLong() ?: throw JWTVerificationException("no uid claim")
    val phone = decoded.getClaim("phone").asString() ?: throw JWTVerificationException("no phone claim")
    val sv = decoded.getClaim("sv").asLong() ?: 0L
    return AuthedUser(uid, phone, sv)
}

/* معادل middleware/auth.js: هدر Authorization رو چک می‌کنه، اگه نامعتبر بود خودش پاسخ ۴۰۱ می‌ده
   و null برمی‌گردونه — فراخوان باید بلافاصله با `?: return@post` (یا get/put) رد بشه. */
suspend fun ApplicationCall.requireAuth(): AuthedUser? {
    val header = request.headers[HttpHeaders.Authorization] ?: ""
    val token = if (header.startsWith("Bearer ")) header.removePrefix("Bearer ") else null
    if (token == null) {
        respond(HttpStatusCode.Unauthorized, mapOf("error" to "no_token"))
        return null
    }
    val authed = try {
        verifyToken(token)
    } catch (e: JWTVerificationException) {
        respond(HttpStatusCode.Unauthorized, mapOf("error" to "invalid_token"))
        return null
    }
    // 🚨 **امضای معتبر کافی نیست.** حسابِ حذف‌شده ردیفی در `users` ندارد و توکنش باید
    // همان لحظه بمیرد، نه بعد از ۹۰ روز. `session_version` هم چک می‌شود تا ابطالِ دستی
    // ممکن باشد (یافته‌ی بازبینی، ۳۱ شهریور).
    val current = Db.withConnection { conn ->
        conn.prepareStatement("SELECT session_version FROM users WHERE id = ?").use { ps ->
            ps.setLong(1, authed.uid)
            ps.executeQuery().use { rs -> if (rs.next()) rs.getLong("session_version") else null }
        }
    }
    if (current == null || current != authed.sessionVersion) {
        respond(HttpStatusCode.Unauthorized, mapOf("error" to "session_revoked"))
        return null
    }
    return authed
}
