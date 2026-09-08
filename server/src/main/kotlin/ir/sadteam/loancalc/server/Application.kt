package ir.sadteam.loancalc.server

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import ir.sadteam.loancalc.server.routes.accountsBackupRoutes
import ir.sadteam.loancalc.server.routes.appVersionRoutes
import ir.sadteam.loancalc.server.routes.authRoutes
import ir.sadteam.loancalc.server.routes.chequesBackupRoutes
import ir.sadteam.loancalc.server.routes.crashRoutes
import ir.sadteam.loancalc.server.routes.creditRatesRoutes
import ir.sadteam.loancalc.server.routes.loansRoutes
import ir.sadteam.loancalc.server.routes.pricesRoutes
import ir.sadteam.loancalc.server.routes.subscriptionRoutes
import kotlinx.serialization.json.Json

fun main() {
    loadDotEnv()
    Db // فراخوانی برای اجرای init (ساخت جدول‌ها) قبل از بالا اومدن سرور
    val port = env("PORT", "3000").toIntOrNull() ?: 3000
    Log.info("server_start", "سرور بالا اومد", "port" to port)
    embeddedServer(Netty, port = port, module = Application::module).start(wait = true)
}

fun Application.module() {
    // 🚨 سقفِ حجمِ بدنه. بدونِ این، یک حسابِ واردشده می‌توانست با چند `PUT /api/loans`ِ
    // چندصدمگابایتی دیسکِ VPS را پر کند - مسیرهای بکاپ هیچ کرانی نداشتند. دو مگابایت برای
    // بزرگ‌ترین پشتیبانِ واقعی هم فراوان است.
    intercept(ApplicationCallPipeline.Plugins) {
        val declared = call.request.headers[HttpHeaders.ContentLength]?.toLongOrNull()
        if (declared != null && declared > MAX_BODY_BYTES) {
            call.respond(HttpStatusCode.PayloadTooLarge, mapOf("error" to "body_too_large"))
            finish()
        }
    }

    install(ContentNegotiation) {
        json(Json { encodeDefaults = true; ignoreUnknownKeys = true })
    }
    // 🚨 CORS برداشته شد. تنها کلاینتِ این API یک اپِ اندروید است و اپِ اندروید اصلاً CORS
    // نمی‌بیند - این بلوک فقط به هر سایتی در هر دامنه‌ای اجازه می‌داد از مرورگرِ کاربر و با
    // هدرِ Authorization به API بزند. وقتی وب‌اپ ساخته شد، به‌جای `anyHost()` همان یک دامنه
    // اجازه بگیرد.

    routing {
        get("/health") { call.respond(mapOf("ok" to true)) }
        authRoutes()
        loansRoutes()
        chequesBackupRoutes()
        accountsBackupRoutes()
        creditRatesRoutes()
        crashRoutes()
        subscriptionRoutes()
        appVersionRoutes()
        pricesRoutes()
    }
}

/** سقفِ بدنه‌ی درخواست - دو مگابایت. */
private const val MAX_BODY_BYTES = 2L * 1024 * 1024
