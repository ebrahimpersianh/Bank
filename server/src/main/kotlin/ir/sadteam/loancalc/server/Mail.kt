package ir.sadteam.loancalc.server

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Writer
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * ═══════════ فرستنده‌ی ایمیل ═══════════
 *
 * **چرا کتابخانه‌ای در کار نیست.** اولین تلاش با `jakarta.mail` بود و در سندباکس اصلاً
 * دانلود نشد؛ SMTP هم پروتکلِ کوچکی است که برای «یک ایمیلِ متنی به یک گیرنده» چند ده خط
 * بیشتر نمی‌خواهد. پس هیچ وابستگیِ تازه‌ای به `build.gradle.kts` اضافه نشد.
 *
 * دو حالتِ رمزنگاری پشتیبانی می‌شود، چون سرویس‌های ایرانی و خارجی هر دو را می‌دهند:
 * - **SMTPS** روی درگاهِ ۴۶۵: از همان اول TLS.
 * - **STARTTLS** روی ۵۸۷ (پیش‌فرض): اول متنِ ساده، بعد ارتقا به TLS.
 *
 * 🚨 **بی تنظیمات، بی‌صدا خاموش است.** اگر `SMTP_HOST` خالی باشد [send] بدونِ خطا `false`
 * برمی‌گرداند. این عمدی است: گزارشِ مشکل باید ثبت شود حتی وقتی ایمیل راه نیفتاده، وگرنه
 * تنها راهِ خبردادن از یک باگ خودش به باگ گره می‌خورد.
 *
 * 🚨 **رمز هیچ‌وقت لاگ نمی‌شود** و در کد هم نیست - از `.env` می‌آید (قاعده‌ی ثبت‌شده‌ی پروژه).
 */
object Mail {

    val host: String get() = env("SMTP_HOST")
    private val port: Int get() = env("SMTP_PORT", "587").toIntOrNull() ?: 587
    private val user: String get() = env("SMTP_USER")
    private val password: String get() = env("SMTP_PASSWORD")

    /** نشانیِ فرستنده. پیش‌فرض همان حسابِ ورود است، چون اکثر سرویس‌ها نشانیِ غریبه را رد می‌کنند. */
    private val from: String get() = env("SMTP_FROM").ifBlank { user }

    /** گیرنده‌ی گزارش‌ها. خالی بماند، ایمیل فرستاده نمی‌شود. */
    val supportTo: String get() = env("SUPPORT_EMAIL_TO").ifBlank { from }

    /** سرویس‌های امروزی روی ۴۶۵ مستقیم TLS می‌خواهند، نه STARTTLS. */
    private val implicitTls: Boolean get() = env("SMTP_SSL", if (port == 465) "true" else "false") == "true"

    val configured: Boolean get() = host.isNotBlank() && supportTo.isNotBlank()

    /**
     * یک ایمیلِ متنیِ ساده می‌فرستد. هیچ استثنایی بیرون نمی‌دهد - نتیجه فقط `true`/`false`
     * است، چون هیچ مسیرِ فراخوانی‌ای نباید به‌خاطرِ نرسیدنِ ایمیل شکست بخورد.
     */
    fun send(to: String, subject: String, body: String): Boolean {
        if (!configured) return false
        return runCatching { deliver(to, subject, body) }
            .onFailure { Log.warn("mail_failed", "ارسالِ ایمیل ناموفق بود", "to" to maskEmail(to), "error" to it.message) }
            .getOrDefault(false)
    }

    private fun deliver(to: String, subject: String, body: String): Boolean {
        val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
        var socket: Socket = if (implicitTls) factory.createSocket(host, port) else Socket(host, port)
        socket.soTimeout = 20_000
        try {
            var reader = BufferedReader(InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
            var writer: Writer = OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)
            expect(reader, 220)

            fun hello() {
                send(writer, "EHLO ${hostnameForEhlo()}")
                expect(reader, 250)
            }
            hello()

            if (!implicitTls) {
                send(writer, "STARTTLS")
                expect(reader, 220)
                // ⚠️ سوکتِ امن **روی همان سوکتِ باز** ساخته می‌شود (`autoClose = true`)؛
                // اتصالِ تازه یعنی سرور جلسه‌ی قبلی را نمی‌شناسد.
                val secure = factory.createSocket(socket, host, port, true) as SSLSocket
                secure.startHandshake()
                socket = secure
                reader = BufferedReader(InputStreamReader(secure.getInputStream(), StandardCharsets.UTF_8))
                writer = OutputStreamWriter(secure.getOutputStream(), StandardCharsets.UTF_8)
                hello()
            }

            if (user.isNotBlank()) {
                send(writer, "AUTH LOGIN")
                expect(reader, 334)
                send(writer, base64(user))
                expect(reader, 334)
                send(writer, base64(password))
                expect(reader, 235)
            }

            send(writer, "MAIL FROM:<$from>")
            expect(reader, 250)
            send(writer, "RCPT TO:<$to>")
            expect(reader, 250, 251)
            send(writer, "DATA")
            expect(reader, 354)
            writer.write(message(to, subject, body))
            writer.write("\r\n.\r\n")
            writer.flush()
            expect(reader, 250)
            runCatching { send(writer, "QUIT") }
            return true
        } finally {
            runCatching { socket.close() }
        }
    }

    /**
     * بدنه‌ی ایمیل.
     *
     * 🚨 **موضوع و متن هر دو فارسی‌اند**، پس هیچ‌کدام نمی‌توانند خامِ ASCII بروند: موضوع با
     * کدگذاریِ RFC 2047 و متن با `base64` می‌رود، وگرنه گیرنده مشتی `?????` می‌بیند.
     */
    private fun message(to: String, subject: String, body: String): String {
        val date = ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME)
        val encodedBody = Base64.getMimeEncoder().encodeToString(body.toByteArray(StandardCharsets.UTF_8))
        return buildString {
            append("From: $from\r\n")
            append("To: $to\r\n")
            append("Subject: =?UTF-8?B?${base64(subject)}?=\r\n")
            append("Date: $date\r\n")
            append("MIME-Version: 1.0\r\n")
            append("Content-Type: text/plain; charset=UTF-8\r\n")
            append("Content-Transfer-Encoding: base64\r\n")
            append("\r\n")
            append(encodedBody.replace("\r\n", "\n").replace("\n", "\r\n"))
        }
    }

    private fun hostnameForEhlo(): String = env("SMTP_EHLO", "jibak.local")

    private fun base64(value: String): String =
        Base64.getEncoder().encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    private fun send(writer: Writer, line: String) {
        writer.write(line + "\r\n")
        writer.flush()
    }

    /**
     * یک پاسخ را می‌خواند و کدش را با انتظار می‌سنجد.
     *
     * پاسخِ چندخطی (`250-…` بعد `250 …`) در SMTP معمول است - خطِ آخر خط‌تیره ندارد.
     */
    private fun expect(reader: BufferedReader, vararg codes: Int) {
        var line = reader.readLine() ?: throw IllegalStateException("اتصال بسته شد")
        while (line.length > 3 && line[3] == '-') {
            line = reader.readLine() ?: throw IllegalStateException("اتصال بسته شد")
        }
        val code = line.take(3).toIntOrNull() ?: throw IllegalStateException("پاسخِ نامفهوم: $line")
        if (code !in codes) throw IllegalStateException("کدِ $code (انتظار: ${codes.joinToString("/")})")
    }
}

/** `ali@example.com` → `al***@example.com` - برای لاگ، بی لو دادنِ نشانی. */
fun maskEmail(value: String): String {
    val at = value.indexOf('@')
    if (at <= 0) return "***"
    val name = value.take(at)
    return "${name.take(2)}***${value.substring(at)}"
}
