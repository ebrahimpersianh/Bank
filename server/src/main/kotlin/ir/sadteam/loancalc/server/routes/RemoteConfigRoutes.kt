package ir.sadteam.loancalc.server.routes

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import ir.sadteam.loancalc.server.Db
import ir.sadteam.loancalc.server.execute
import ir.sadteam.loancalc.server.queryOne
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/** کلیدهای مجاز - چیزی خارج از این فهرست نوشته/خوانده نمی‌شود. */
private val CONFIG_KEYS = setOf("shop")

/**
 * ⚙️ **تنظیمِ از-راه-دور** (۸ مهر، خواسته‌ی کاربر: «فروشگاه کلاً از روی سرور»). هر کلید یک JSON
 * که اپ موقعِ باز شدن می‌گیرد و ذخیره می‌کند؛ ادمین از داخلِ اپ عوضش می‌کند، بی‌آپدیتِ برنامه.
 */
fun Route.remoteConfigRoutes() {
    get("/api/config/{key}") {
        val key = call.parameters["key"].orEmpty()
        if (key !in CONFIG_KEYS) return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "unknown_key"))
        val json = Db.withConnection { c -> c.queryOne("SELECT json FROM app_config WHERE key = ?", key) { it.getString(1) } } ?: "{}"
        call.respondText(json, ContentType.Application.Json)
    }
    post("/api/admin/config/{key}") {
        val admin = call.isAdminUser() ?: return@post
        if (!admin) return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to "admin_only"))
        val key = call.parameters["key"].orEmpty()
        if (key !in CONFIG_KEYS) return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "unknown_key"))
        val body = call.receiveText()
        // فقط JSONِ شیءِ معتبر و کوچک.
        val ok = body.length < 200_000 && runCatching { Json.parseToJsonElement(body) is JsonObject }.getOrDefault(false)
        if (!ok) return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "bad_json"))
        Db.withConnection { c ->
            c.execute(
                "INSERT INTO app_config (key, json, updated_at) VALUES (?, ?, datetime('now')) " +
                    "ON CONFLICT(key) DO UPDATE SET json = excluded.json, updated_at = excluded.updated_at",
                key, body,
            )
        }
        call.respond(mapOf("ok" to true))
    }
}
