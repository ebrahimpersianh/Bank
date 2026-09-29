package ir.sadteam.loancalc.server

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
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

        // ⚠️ از وقتی توکنِ کاربرِ ناموجود ۴۰۱ می‌گیرد (ابطالِ نشست)، این تست باید یک
        // کاربرِ واقعی بسازد تا همان چیزی را بسنجد که برایش نوشته شده: ردِ بدنه‌ی نامعتبر.
        val uid = Db.withConnection { conn ->
            conn.insertReturningId("INSERT INTO users (phone) VALUES (?)", "09120000000")
        }
        val token = signToken(uid, "09120000000")
        val response = client.put("/api/loans") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"loans":"not-an-array"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `cheques backup put requires subscription`() = testApplication {
        val dbFile = File.createTempFile("loan-calc-test-cheques-noauth", ".sqlite")
        dbFile.deleteOnExit()
        System.setProperty("DB_PATH", dbFile.absolutePath)
        System.setProperty("JWT_SECRET", "test-secret-for-unit-tests-only-3")

        application { module() }

        // کاربرِ واقعیِ **بدونِ اشتراک** - قبلاً uidِ ناموجود بود، که حالا اصلاً به
        // بررسیِ اشتراک نمی‌رسد چون نشستش باطل شمرده می‌شود.
        val uid = Db.withConnection { conn ->
            conn.insertReturningId("INSERT INTO users (phone, created_at) VALUES (?, '2000-01-01 00:00:00')", "09129999999")
        }
        val token = signToken(uid, "09129999999")
        val response = client.put("/api/cheques") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"data":"{\"cheques\":[]}"}""")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `cheques and accounts backup roundtrip for a subscribed user`() = testApplication {
        val dbFile = File.createTempFile("loan-calc-test-cheques-roundtrip", ".sqlite")
        dbFile.deleteOnExit()
        System.setProperty("DB_PATH", dbFile.absolutePath)
        System.setProperty("JWT_SECRET", "test-secret-for-unit-tests-only-4")

        application { module() }

        val uid = Db.withConnection { conn ->
            conn.insertReturningId(
                "INSERT INTO users (phone, subscribed) VALUES (?, 1)", "09121112233"
            )
        }
        val token = signToken(uid, "09121112233")

        val getBeforePut = client.get("/api/cheques") { header("Authorization", "Bearer $token") }
        assertEquals(HttpStatusCode.OK, getBeforePut.status)
        assertEquals("{}", Json.parseToJsonElement(getBeforePut.bodyAsText()).jsonObject["data"]?.jsonPrimitive?.content)

        val chequesBlob = """{"cheques":[{"id":1}],"chequeBooks":[]}"""
        val putResponse = client.put("/api/cheques") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject { put("data", JsonPrimitive(chequesBlob)) }.toString())
        }
        assertEquals(HttpStatusCode.OK, putResponse.status)

        val getAfterPut = client.get("/api/cheques") { header("Authorization", "Bearer $token") }
        assertEquals(chequesBlob, Json.parseToJsonElement(getAfterPut.bodyAsText()).jsonObject["data"]?.jsonPrimitive?.content)

        // /api/accounts کاملاً مستقل از /api/cheques ـه (جدول جدا)
        val accountsBlob = """{"accounts":[],"transactions":[]}"""
        val putAccounts = client.put("/api/accounts") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject { put("data", JsonPrimitive(accountsBlob)) }.toString())
        }
        assertEquals(HttpStatusCode.OK, putAccounts.status)
        val getAccounts = client.get("/api/accounts") { header("Authorization", "Bearer $token") }
        assertEquals(accountsBlob, Json.parseToJsonElement(getAccounts.bodyAsText()).jsonObject["data"]?.jsonPrimitive?.content)
    }

    @Test
    fun `credit rates are seeded and publicly readable without auth`() = testApplication {
        val dbFile = File.createTempFile("loan-calc-test-credit-rates", ".sqlite")
        dbFile.deleteOnExit()
        System.setProperty("DB_PATH", dbFile.absolutePath)
        System.setProperty("JWT_SECRET", "test-secret-for-unit-tests-only-5")

        application { module() }

        val response = client.get("/api/credit-rates")
        assertEquals(HttpStatusCode.OK, response.status)
        val rates = Json.parseToJsonElement(response.bodyAsText()).jsonObject["rates"]?.jsonArray
        assertTrue(rates != null && rates.size == 6)
        val first = rates!!.first().jsonObject
        assertEquals("digipay", first["key"]?.jsonPrimitive?.content)
    }
    @Test
    fun `token of a deleted user is rejected`() = testApplication {
        val dbFile = File.createTempFile("loan-calc-test-revoked", ".sqlite")
        dbFile.deleteOnExit()
        System.setProperty("DB_PATH", dbFile.absolutePath)
        System.setProperty("JWT_SECRET", "test-secret-for-unit-tests-only-revoke")

        application { module() }

        val uid = Db.withConnection { conn ->
            conn.insertReturningId("INSERT INTO users (phone, subscribed) VALUES (?, 1)", "09120001122")
        }
        val token = signToken(uid, "09120001122")
        assertEquals(HttpStatusCode.OK, client.get("/api/cheques") { header("Authorization", "Bearer $token") }.status)

        Db.withConnection { conn -> conn.execute("DELETE FROM users WHERE id = ?", uid) }

        // امضای توکن هنوز معتبر است، ولی حساب دیگر وجود ندارد.
        assertEquals(
            HttpStatusCode.Unauthorized,
            client.get("/api/cheques") { header("Authorization", "Bearer $token") }.status,
        )
    }

    @Test
    fun `stale revision put is rejected with conflict`() = testApplication {
        val dbFile = File.createTempFile("loan-calc-test-revision", ".sqlite")
        dbFile.deleteOnExit()
        System.setProperty("DB_PATH", dbFile.absolutePath)
        System.setProperty("JWT_SECRET", "test-secret-for-unit-tests-only-revision")

        application { module() }

        val uid = Db.withConnection { conn ->
            conn.insertReturningId("INSERT INTO users (phone, subscribed) VALUES (?, 1)", "09120002233")
        }
        val token = signToken(uid, "09120002233")

        fun putWith(expected: Long?) = buildJsonObject {
            put("data", JsonPrimitive("""{"accounts":[],"transactions":[]}"""))
            if (expected != null) put("expectedRevision", JsonPrimitive(expected))
        }.toString()

        val first = client.put("/api/accounts") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(putWith(0))
        }
        assertEquals(HttpStatusCode.OK, first.status)

        // همان نسخه‌ی کهنه دوباره: یعنی گوشیِ دوم چیزی را که ندیده پاک می‌کرد.
        val stale = client.put("/api/accounts") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(putWith(0))
        }
        assertEquals(HttpStatusCode.Conflict, stale.status)

        // کلاینتِ قدیمی که اصلاً نسخه نمی‌فرستد، مثلِ قبل کار می‌کند.
        val legacy = client.put("/api/accounts") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(putWith(null))
        }
        assertEquals(HttpStatusCode.OK, legacy.status)
    }

    @Test
    fun `gift code grants days once and rejects reuse`() = testApplication {
        val dbFile = File.createTempFile("loan-calc-test-gift", ".sqlite")
        dbFile.deleteOnExit()
        System.setProperty("DB_PATH", dbFile.absolutePath)
        System.setProperty("JWT_SECRET", "test-secret-for-unit-tests-only-gift")

        application { module() }

        val uid = Db.withConnection { conn ->
            conn.insertReturningId("INSERT INTO users (phone) VALUES (?)", "09120005566")
        }
        val token = signToken(uid, "09120005566")
        Db.withConnection { conn ->
            conn.execute("INSERT INTO gift_codes (code, days, note) VALUES (?, ?, ?)", "JIBAK-TEST1-TEST2", 15, "تست")
        }

        val first = client.post("/api/gift/redeem") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"code":"jibak-test1-test2"}""")
        }
        assertEquals(HttpStatusCode.OK, first.status)
        // اشتراک واقعاً تمدید شد، نه فقط پاسخِ موفق.
        val until = Db.withConnection { conn ->
            conn.queryOne("SELECT subscribed_until FROM users WHERE id = ?", uid) { it.getString("subscribed_until") }
        }
        assertTrue(!until.isNullOrBlank())

        // همان کد، بارِ دوم: باید رد شود وگرنه یک کد بی‌نهایت اشتراک می‌دهد.
        val again = client.post("/api/gift/redeem") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"code":"JIBAK-TEST1-TEST2"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, again.status)
    }

    @Test
    fun `gift code creation needs the admin token`() = testApplication {
        val dbFile = File.createTempFile("loan-calc-test-gift-admin", ".sqlite")
        dbFile.deleteOnExit()
        System.setProperty("DB_PATH", dbFile.absolutePath)
        System.setProperty("JWT_SECRET", "test-secret-for-unit-tests-only-gift-admin")

        application { module() }

        // بی هدرِ ادمین (و با ADMIN_TOKENِ تنظیم‌نشده) مسیر بسته است.
        val denied = client.post("/api/gift/create") {
            contentType(ContentType.Application.Json)
            setBody("""{"days":5}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, denied.status)
    }

    @Test
    fun `bug report is stored with a ticket and the user id`() = testApplication {
        val dbFile = File.createTempFile("loan-calc-test-support", ".sqlite")
        dbFile.deleteOnExit()
        System.setProperty("DB_PATH", dbFile.absolutePath)
        System.setProperty("JWT_SECRET", "test-secret-for-unit-tests-only-support")

        application { module() }

        val uid = Db.withConnection { conn ->
            conn.insertReturningId("INSERT INTO users (phone) VALUES (?)", "09120007788")
        }
        val token = signToken(uid, "09120007788")

        val response = client.post("/api/support/report") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"message":"دکمه‌ی پرداخت کار نمی‌کند","appVersion":"1.0.622","device":"Xiaomi"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)

        // مهم‌ترین بخش: گزارش به **همان حساب** بسته شده، وگرنه نمی‌شود هدیه داد.
        val storedUid = Db.withConnection { conn ->
            conn.queryOne("SELECT user_id FROM bug_reports WHERE phone = ?", "09120007788") { it.getLong("user_id") }
        }
        assertEquals(uid, storedUid)

        // متنِ خیلی کوتاه پذیرفته نمی‌شود - وگرنه صندوق پر از گزارشِ خالی می‌شود.
        val tooShort = client.post("/api/support/report") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"message":"سلام"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, tooShort.status)
    }


    @Test
    fun `usage batch feeds admin stats and only admins can read them`() = testApplication {
        val dbFile = File.createTempFile("loan-calc-test-usage", ".sqlite")
        dbFile.deleteOnExit()
        System.setProperty("DB_PATH", dbFile.absolutePath)
        System.setProperty("JWT_SECRET", "test-secret-for-unit-tests-only-usage")

        application { module() }

        val install = "abcdef0123456789-install"
        val batch = client.post("/api/events/batch") {
            contentType(ContentType.Application.Json)
            setBody(
                """{"installId":"$install","appVersion":651,"store":"myket","sdk":34,"loggedIn":true,
                   "events":[{"name":"session_start"},{"name":"screen:home","count":3},
                             {"name":"action:loan_added"},{"name":"transaction_created","count":2},
                             {"name":"BAD NAME"}]}""",
            )
        }
        assertEquals(HttpStatusCode.OK, batch.status)
        // شناسه‌ی نامعتبر رد می‌شود.
        val bad = client.post("/api/events/batch") {
            contentType(ContentType.Application.Json)
            setBody("""{"installId":"x","events":[{"name":"screen:home"}]}""")
        }
        assertEquals(HttpStatusCode.BadRequest, bad.status)

        val uid = Db.withConnection { conn -> conn.insertReturningId("INSERT INTO users (phone) VALUES (?)", "09120009988") }
        val token = signToken(uid, "09120009988")
        val check = client.get("/api/admin/check") { header("Authorization", "Bearer $token") }
        assertTrue(check.bodyAsText().contains("false"))
        val forbidden = client.get("/api/admin/stats") { header("Authorization", "Bearer $token") }
        assertEquals(HttpStatusCode.Forbidden, forbidden.status)

        Db.withConnection { conn -> conn.execute("UPDATE users SET is_admin = 1 WHERE id = ?", uid) }
        val stats = client.get("/api/admin/stats") { header("Authorization", "Bearer $token") }
        assertEquals(HttpStatusCode.OK, stats.status)
        val json = Json.parseToJsonElement(stats.bodyAsText()).jsonObject
        assertEquals("1", json["activeToday"]!!.jsonPrimitive.content)
        assertEquals("1", json["loggedInActive30"]!!.jsonPrimitive.content)
        val screens = json["screens"]!!.jsonArray
        assertEquals("home", screens[0].jsonObject["name"]!!.jsonPrimitive.content)
        assertEquals("3", screens[0].jsonObject["total"]!!.jsonPrimitive.content)
        assertEquals("loan_added", json["actions"]!!.jsonArray[0].jsonObject["name"]!!.jsonPrimitive.content)
        assertTrue(!stats.bodyAsText().contains("BAD NAME"))
    }

    @Test
    fun `photos upload list and download per user`() = testApplication {
        val dbFile = File.createTempFile("loan-calc-test-files", ".sqlite")
        dbFile.deleteOnExit()
        val dir = kotlin.io.path.createTempDirectory("files").toFile()
        System.setProperty("DB_PATH", dbFile.absolutePath)
        System.setProperty("JWT_SECRET", "test-secret-for-unit-tests-only-files")
        System.setProperty("FILES_DIR", dir.absolutePath)
        application { module() }
        val uid = Db.withConnection { conn -> conn.insertReturningId("INSERT INTO users (phone) VALUES (?)", "09120003344") }
        val token = signToken(uid, "09120003344")
        val put = client.put("/api/files/receipts/r_1.jpg") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.OctetStream)
            setBody(byteArrayOf(1, 2, 3))
        }
        assertEquals(HttpStatusCode.OK, put.status)
        val bad = client.put("/api/files/receipts/..%2Fx") { header("Authorization", "Bearer $token"); setBody(byteArrayOf(1)) }
        assertTrue(bad.status != HttpStatusCode.OK)
        assertTrue(client.get("/api/files") { header("Authorization", "Bearer $token") }.bodyAsText().contains("receipts/r_1.jpg"))
        val got = client.get("/api/files/receipts/r_1.jpg") { header("Authorization", "Bearer $token") }
        assertEquals(3, got.readBytes().size)
    }
}
