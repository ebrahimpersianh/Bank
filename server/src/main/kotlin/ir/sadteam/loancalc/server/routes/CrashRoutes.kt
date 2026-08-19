package ir.sadteam.loancalc.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import ir.sadteam.loancalc.server.Db
import ir.sadteam.loancalc.server.execute
import ir.sadteam.loancalc.server.rateLimitOk
import kotlinx.serialization.Serializable

@Serializable
private data class CrashBody(
    val message: String? = null,
    val stack: String? = null,
    val context: String? = null,
    val appVersion: String? = null
)

fun Route.crashRoutes() {
    route("/api/crash") {
        post {
            /* 🚨 این مسیر عمداً بدونِ ورود بازه (کرشِ اپ ممکنه قبل از لاگین رخ بده)، پس تنها
               محافظش همین سقفه - وگرنه هر کسی می‌تونست دیسکِ سرور رو با ردیفِ بی‌نهایت پر کنه. */
            if (!call.rateLimitOk("crash", 20, 60 * 60 * 1000L)) return@post
            val body = runCatching { call.receive<CrashBody>() }.getOrNull()
            val message = (body?.message ?: "").take(2000)
            if (message.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "no_message"))
                return@post
            }
            val stack = (body?.stack ?: "").take(8000)
            val context = (body?.context ?: "").take(500)
            val appVersion = (body?.appVersion ?: "").take(50)

            Db.withConnection { conn ->
                conn.execute(
                    "INSERT INTO crash_reports (message, stack, context, app_version) VALUES (?, ?, ?, ?)",
                    message, stack, context, appVersion
                )
            }

            call.respond(mapOf("ok" to true))
        }
    }
}
