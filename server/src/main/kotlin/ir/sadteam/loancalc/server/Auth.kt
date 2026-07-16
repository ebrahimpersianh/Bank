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

fun signToken(uid: Long, phone: String): String =
    JWT.create()
        .withClaim("uid", uid)
        .withClaim("phone", phone)
        .withExpiresAt(Date(System.currentTimeMillis() + TOKEN_TTL_MS))
        .sign(jwtAlgorithm)

data class AuthedUser(val uid: Long, val phone: String)

private fun verifyToken(token: String): AuthedUser {
    val decoded = JWT.require(jwtAlgorithm).build().verify(token)
    val uid = decoded.getClaim("uid").asLong() ?: throw JWTVerificationException("no uid claim")
    val phone = decoded.getClaim("phone").asString() ?: throw JWTVerificationException("no phone claim")
    return AuthedUser(uid, phone)
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
    return try {
        verifyToken(token)
    } catch (e: JWTVerificationException) {
        respond(HttpStatusCode.Unauthorized, mapOf("error" to "invalid_token"))
        null
    }
}
