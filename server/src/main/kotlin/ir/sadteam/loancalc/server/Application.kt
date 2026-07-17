package ir.sadteam.loancalc.server

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import ir.sadteam.loancalc.server.routes.accountsBackupRoutes
import ir.sadteam.loancalc.server.routes.authRoutes
import ir.sadteam.loancalc.server.routes.chequesBackupRoutes
import ir.sadteam.loancalc.server.routes.crashRoutes
import ir.sadteam.loancalc.server.routes.loansRoutes
import ir.sadteam.loancalc.server.routes.subscriptionRoutes
import kotlinx.serialization.json.Json

fun main() {
    loadDotEnv()
    Db // فراخوانی برای اجرای init (ساخت جدول‌ها) قبل از بالا اومدن سرور
    val port = env("PORT", "3000").toIntOrNull() ?: 3000
    println("سرور روی پورت $port بالا اومد")
    embeddedServer(Netty, port = port, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json { encodeDefaults = true; ignoreUnknownKeys = true })
    }
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
    }

    routing {
        get("/health") { call.respond(mapOf("ok" to true)) }
        authRoutes()
        loansRoutes()
        chequesBackupRoutes()
        accountsBackupRoutes()
        crashRoutes()
        subscriptionRoutes()
    }
}
