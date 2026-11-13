package ir.sadteam.loancalc.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import ir.sadteam.loancalc.server.Db
import ir.sadteam.loancalc.server.env
import kotlinx.serialization.Serializable

/**
 * ۲۲ آبان (خواسته‌ی صاحبِ برنامه): یک آدرسِ فقط‌خواندنی تا «Claude Duet» روی گوشی‌اش گزارشِ
 * پنلِ مدیریت را بخواند و برایش تعریف کند - بی ورود به برنامه.
 *
 *   GET /api/report/{key}
 *
 * کلید = `REPORT_KEY` در `.env` (حداقل ۲۴ حرف؛ نبودش = مسیر بسته). مقایسه در زمانِ ثابت.
 * فقط همان چیزهایی که پنلِ مدیریت نشان می‌دهد؛ شماره‌ی خریداران نیمه‌پنهان (`maskPhone`).
 */
@Serializable
data class OwnerReport(
    val guide: String,
    val today: DigestResponse,
    val week: DigestResponse,
    val month: DigestResponse,
    val stats: StatsResponse,
    val money: MoneyResponse,
)

private fun reportKeyMatches(sent: String?): Boolean {
    val key = env("REPORT_KEY", "")
    if (key.length < 24 || sent == null) return false
    return java.security.MessageDigest.isEqual(key.toByteArray(), sent.toByteArray())
}

fun Route.reportRoutes() {
    get("/api/report/{key}") {
        if (!reportKeyMatches(call.parameters["key"])) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "not_found"))
            return@get
        }
        val report = Db.withConnection { conn ->
            OwnerReport(
                guide = "گزارشِ پنلِ مدیریتِ اپِ «جیبک» (اندروید، بازار و مایکت). " +
                    "today/week/month: مقایسه‌ی این بازه با بازه‌ی قبل (now در برابرِ prev؛ روزها به وقتِ ایران). " +
                    "stats: آمارِ کلی (نصب، کاربرِ فعال، مشترک‌ها، پیامک، کرش‌ها). " +
                    "money: فروش و خریدهای اخیر (مبلغ به تومان، شماره‌ها نیمه‌پنهان). " +
                    "برای صاحبِ برنامه به فارسیِ ساده خلاصه کن: مهم‌ترین تغییرها، خریدهای تازه، و هر مشکل یا کرش.",
                today = buildDigest(conn, "day"),
                week = buildDigest(conn, "week"),
                month = buildDigest(conn, "month"),
                stats = buildStats(conn),
                money = buildMoney(conn),
            )
        }
        call.response.headers.append("Cache-Control", "no-store")
        call.response.headers.append("X-Robots-Tag", "noindex")
        call.respond(report)
    }
}
