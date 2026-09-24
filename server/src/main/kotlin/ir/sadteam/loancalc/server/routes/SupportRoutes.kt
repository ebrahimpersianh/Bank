package ir.sadteam.loancalc.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import ir.sadteam.loancalc.server.Db
import ir.sadteam.loancalc.server.Log
import ir.sadteam.loancalc.server.Mail
import ir.sadteam.loancalc.server.execute
import ir.sadteam.loancalc.server.maskPhone
import ir.sadteam.loancalc.server.queryOne
import ir.sadteam.loancalc.server.rateLimitOk
import ir.sadteam.loancalc.server.requireAuth
import kotlinx.serialization.Serializable
import java.security.SecureRandom

/*
 * 🐞 **گزارشِ مشکل**.
 *
 * چرا سمتِ سرور ثبت می‌شود و فقط ایمیل نمی‌رود: برای دادنِ هدیه باید بدانیم گزارش از
 * **کدام حساب** آمده. ایمیل این را نمی‌گوید (کاربر ممکن است از ایمیلِ شخصی‌اش بفرستد)،
 * ولی `user_id`ِ همین جدول دقیقاً همان چیزی است که کدِ هدیه به آن تعلق می‌گیرد.
 *
 * ✉️ **ایمیل حالا از خودِ سرور می‌رود** (`Mail`). گوشی هم همچنان صندوقِ ایمیل را با
 * موضوعِ آماده باز می‌کند، و این عمدی است: کاربر می‌خواهد چیزی را که فرستاده در
 * «ارسال‌شده‌ها»ی خودش ببیند، و اگر SMTP روزی از کار بیفتد راهِ دوم سرِ جایش است.
 *
 * 🚨 **ثبت در دیتابیس بر ارسالِ ایمیل مقدم است و ارسال هیچ‌وقت درخواست را نمی‌شکند.**
 * کدِ پیگیری چیزی است که هدیه به آن بسته می‌شود؛ اگر به‌خاطرِ یک SMTPِ خراب پاسخِ ۵۰۰
 * برگردد، کاربر گزارشش را از دست می‌دهد در حالی که همین حالا در جدول نشسته.
 */

@Serializable
private data class ReportBody(
    val message: String,
    /** نسخه‌ی اپ و مدلِ گوشی - برای بازتولیدِ مشکل. */
    val appVersion: String? = null,
    val device: String? = null,
)

@Serializable
private data class ReportResponse(val ok: Boolean = true, val ticket: String)

/** کدِ پیگیری: کوتاه و خوانا، چون کاربر آن را در ایمیل و پشتیبانی تکرار می‌کند. */
private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

private fun newTicket(): String {
    val rnd = SecureRandom()
    return "JB-" + (1..6).map { ALPHABET[rnd.nextInt(ALPHABET.length)] }.joinToString("")
}

fun Route.supportRoutes() {
    route("/api/support") {
        post("/report") {
            // ۱۰ گزارش در ساعت از هر IP: کاربرِ واقعی یکی می‌فرستد.
            if (!call.rateLimitOk("support_report", 10, 60 * 60 * 1000L)) return@post
            val authed = call.requireAuth() ?: return@post
            val body = runCatching { call.receive<ReportBody>() }.getOrNull()
            val message = body?.message?.trim()
            if (message.isNullOrBlank() || message.length < 5) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "message_too_short"))
                return@post
            }

            val ticket = newTicket()
            Db.withConnection { conn ->
                conn.execute(
                    """
                    INSERT INTO bug_reports (ticket, user_id, phone, message, app_version, device)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """.trimIndent(),
                    ticket,
                    authed.uid,
                    authed.phone,
                    // متن سقف می‌خورد: یک بدنه‌ی چندمگابایتی جدول را باد می‌کند.
                    message.take(4000),
                    body.appVersion?.take(40),
                    body.device?.take(80),
                )
            }
            // شماره ماسک می‌شود - قاعده‌ی ثبتِ لاگِ پروژه.
            Log.info("bug_report", "گزارشِ مشکلِ تازه", "ticket" to ticket, "uid" to authed.uid, "phone" to maskPhone(authed.phone))
            // ✉️ ارسال **بعد از** ثبت و بی‌اثر روی پاسخ - رجوع کن به کامنتِ بالای فایل.
            // `Mail.send` خودش استثنا نمی‌دهد و بی تنظیماتِ SMTP بی‌صدا `false` برمی‌گرداند.
            val mailed = Mail.send(
                to = Mail.supportTo,
                subject = "مشکل برنامه - $ticket",
                body = buildString {
                    appendLine("کدِ پیگیری: $ticket")
                    appendLine("شناسه‌ی کاربر: ${authed.uid}")
                    appendLine("شماره: ${maskPhone(authed.phone)}")
                    appendLine("نسخه: ${body.appVersion ?: "-"}")
                    appendLine("دستگاه: ${body.device ?: "-"}")
                    appendLine()
                    appendLine(message.take(4000))
                },
            )
            if (Mail.configured && !mailed) {
                Log.warn("bug_report_mail", "گزارش ثبت شد ولی ایمیلش نرفت", "ticket" to ticket)
            }
            call.respond(ReportResponse(ticket = ticket))
        }
    }
}
