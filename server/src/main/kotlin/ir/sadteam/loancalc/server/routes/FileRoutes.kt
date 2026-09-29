package ir.sadteam.loancalc.server.routes

import ir.sadteam.loancalc.server.toUserRow
import ir.sadteam.loancalc.server.isSubscribed
import ir.sadteam.loancalc.server.queryOne
import ir.sadteam.loancalc.server.Db
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import ir.sadteam.loancalc.server.env
import ir.sadteam.loancalc.server.requireAuth
import kotlinx.serialization.Serializable
import java.io.File

/**
 * عکس‌های کاربر (رسید، چک، وام) - ۷ مهر، «همه‌چیز روی سرور». هر کاربر پوشه‌ی خودش را دارد و
 * فقط با توکنِ خودش به آن می‌رسد. اپ پیش از فرستادن، عکس را کوچک و فشرده می‌کند.
 *   GET /api/files                → فهرستِ نام‌ها
 *   PUT /api/files/{dir}/{name}   → بدنه = خودِ فایل
 *   GET /api/files/{dir}/{name}   → خودِ فایل
 */
@Serializable
private data class FileList(val files: List<String>)

private val DIRS = setOf("attachments", "receipts")
private val NAME = Regex("^[A-Za-z0-9._-]{1,100}$")
private const val MAX_FILE_BYTES = 1_900_000L
private const val MAX_USER_BYTES = 300L * 1024 * 1024

private fun userRoot(uid: Long): File =
    File(env("FILES_DIR", System.getProperty("user.home") + "/VameMan/files"), uid.toString())

private fun safeFile(uid: Long, dir: String?, name: String?): File? {
    if (dir !in DIRS || name == null || !NAME.matches(name) || name.startsWith(".")) return null
    return File(File(userRoot(uid), dir!!), name)
}

fun Route.fileRoutes() {
    route("/api/files") {
        get {
            val authed = call.requireAuth() ?: return@get
            val root = userRoot(authed.uid)
            val names = DIRS.flatMap { d -> File(root, d).listFiles()?.filter { it.isFile }?.map { "$d/${it.name}" } ?: emptyList() }
            call.respond(FileList(names))
        }
        put("/{dir}/{name}") {
            val authed = call.requireAuth() ?: return@put
            val file = safeFile(authed.uid, call.parameters["dir"], call.parameters["name"])
            if (file == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "bad_name"))
                return@put
            }
            val user = Db.withConnection { conn ->
                conn.queryOne(
                    "SELECT id, phone, subscribed, subscribed_until, subscription_tier, created_at, trial_blocked FROM users WHERE id = ?", authed.uid,
                ) { it.toUserRow() }
            }
            if (!isSubscribed(user)) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "subscription_required"))
                return@put
            }
            val bytes = call.receive<ByteArray>()
            if (bytes.isEmpty() || bytes.size > MAX_FILE_BYTES) {
                call.respond(HttpStatusCode.PayloadTooLarge, mapOf("error" to "file_too_large"))
                return@put
            }
            val used = userRoot(authed.uid).walkTopDown().filter { it.isFile }.sumOf { it.length() }
            if (used + bytes.size > MAX_USER_BYTES) {
                call.respond(HttpStatusCode.InsufficientStorage, mapOf("error" to "quota"))
                return@put
            }
            file.parentFile.mkdirs()
            file.writeBytes(bytes)
            call.respond(mapOf("ok" to true))
        }
        get("/{dir}/{name}") {
            val authed = call.requireAuth() ?: return@get
            val file = safeFile(authed.uid, call.parameters["dir"], call.parameters["name"])
            if (file == null || !file.isFile) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "not_found"))
                return@get
            }
            call.respondBytes(file.readBytes(), ContentType.Image.JPEG)
        }
    }
}
