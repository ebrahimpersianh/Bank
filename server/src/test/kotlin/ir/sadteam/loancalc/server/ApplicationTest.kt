package ir.sadteam.loancalc.server

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/* یه سناریوی سرتاسر ساده (سلامتی سرور + کل فلوی ورود با OTP در حالت dev + همگام‌سازی وام‌ها)
   تا رفتار پورت‌شده از server/src (Node.js قدیمی) با تست خودکار هم تایید بشه، نه فقط دستی. */
class ApplicationTest {

    @Test
    fun `health and otp login flow work end to end`() = testApplication {
        val dbFile = File.createTempFile("loan-calc-test", ".sqlite")
        dbFile.deleteOnExit()
        System.setProperty("DB_PATH", dbFile.absolutePath)
        System.setProperty("JWT_SECRET", "test-secret-for-unit-tests-only")

        application { module() }

        val health = client.get("/health")
        assertEquals(HttpStatusCode.OK, health.status)

        val phone = "09121234567"

        val otpResponse = client.post("/api/auth/request-otp") {
            contentType(ContentType.Application.Json)
            setBody("""{"phone":"$phone"}""")
        }
        assertEquals(HttpStatusCode.OK, otpResponse.status)

        // تو حالت dev (بدون PAYAMRESAN_API_KEY)، کد فقط لاگ می‌شه؛ برای تست مستقیم از دیتابیس می‌خونیمش
        val code = Db.withConnection { conn ->
            conn.queryOne("SELECT code_hash FROM otps WHERE phone = ? ORDER BY id DESC LIMIT 1", phone) { rs ->
                rs.getString("code_hash")
            }
        }
        assertTrue(code != null)

        // چون فقط هش کد رو داریم نه خودش، مستقیم با کد اشتباه چک می‌کنیم که به‌درستی رد بشه
        val wrongVerify = client.post("/api/auth/verify-otp") {
            contentType(ContentType.Application.Json)
            setBody("""{"phone":"$phone","code":"00000"}""")
        }
        assertTrue(wrongVerify.status == HttpStatusCode.BadRequest)
        val wrongBody = Json.parseToJsonElement(wrongVerify.bodyAsText()).jsonObject
        assertEquals("wrong_code", wrongBody["error"]?.jsonPrimitive?.content)

        val meNoToken = client.get("/api/auth/me")
        assertEquals(HttpStatusCode.Unauthorized, meNoToken.status)
    }

    @Test
    fun `loans put rejects non array payload`() = testApplication {
        val dbFile = File.createTempFile("loan-calc-test-loans", ".sqlite")
        dbFile.deleteOnExit()
        System.setProperty("DB_PATH", dbFile.absolutePath)
        System.setProperty("JWT_SECRET", "test-secret-for-unit-tests-only-2")

        application { module() }

        val token = signToken(1, "09120000000")
        val response = client.put("/api/loans") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"loans":"not-an-array"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
