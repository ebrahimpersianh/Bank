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
import kotlinx.serialization.Serializable
import io.ktor.server.request.receive

/** کلیدهای مجاز - چیزی خارج از این فهرست نوشته/خوانده نمی‌شود. */
private val CONFIG_KEYS = setOf("shop", "app")

/**
 * ⚙️ **تنظیمِ از-راه-دور** (۸ مهر، خواسته‌ی کاربر: «فروشگاه کلاً از روی سرور»). هر کلید یک JSON
 * که اپ موقعِ باز شدن می‌گیرد و ذخیره می‌کند؛ ادمین از داخلِ اپ عوضش می‌کند، بی‌آپدیتِ برنامه.
 */
@Serializable
data class SurveyAnswer(val id: String = "", val answer: String = "", val install: String = "")

@Serializable
data class AppVersionEdit(val code: Int = 0, val changelog: String = "")

@Serializable
data class AppVersionView(val code: Int, val changelog: String)

fun Route.remoteConfigRoutes() {
    // 🗳 نظرسنجیِ یک‌سؤاله (۸ مهر): هر نصب یک جواب (دوباره = جایگزین).
    post("/api/survey") {
        val a = runCatching { call.receive<SurveyAnswer>() }.getOrNull()
        if (a == null || a.id.isBlank() || a.answer.isBlank() || a.install.isBlank()) return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "bad"))
        Db.withConnection { c ->
            c.execute(
                "INSERT INTO survey_answers (survey_id, install_id, answer) VALUES (?, ?, ?) " +
                    "ON CONFLICT(survey_id, install_id) DO UPDATE SET answer = excluded.answer",
                a.id.take(40), a.install.take(64), a.answer.take(120),
            )
        }
        call.respond(mapOf("ok" to true))
    }
    get("/api/admin/survey") {
        val admin = call.isAdminUser() ?: return@get
        if (!admin) return@get call.respond(HttpStatusCode.Forbidden, mapOf("error" to "admin_only"))
        val id = call.request.queryParameters["id"].orEmpty()
        val rows = Db.withConnection { c ->
            buildList { c.list("SELECT answer, COUNT(*) FROM survey_answers WHERE survey_id = ? GROUP BY 1 ORDER BY 2 DESC", id) { add(NamedCount(it.getString(1), it.getInt(2))) } }
        }
        call.respond(rows)
    }
    // 🆕 «چه چیز تازه است» و آخرین نسخه - قبلاً فقط با sqlite3 روی سرور.
    get("/api/admin/app-version") {
        val admin = call.isAdminUser() ?: return@get
        if (!admin) return@get call.respond(HttpStatusCode.Forbidden, mapOf("error" to "admin_only"))
        val v = Db.withConnection { c -> c.queryOne("SELECT latest_version_code, coalesce(changelog, '') FROM app_version WHERE id = 1") { AppVersionView(it.getInt(1), it.getString(2)) } }
        call.respond(v ?: AppVersionView(0, ""))
    }
    post("/api/admin/app-version") {
        val admin = call.isAdminUser() ?: return@post
        if (!admin) return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to "admin_only"))
        val e = runCatching { call.receive<AppVersionEdit>() }.getOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "bad"))
        Db.withConnection { c -> c.execute("UPDATE app_version SET latest_version_code = ?, changelog = ? WHERE id = 1", e.code, e.changelog.take(2000)) }
        call.respond(mapOf("ok" to true))
    }

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
