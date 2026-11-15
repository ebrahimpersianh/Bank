package ir.sadteam.loancalc.server

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.server.response.respond
import java.util.concurrent.ConcurrentHashMap

/**
 * محدودکننده‌ی تعدادِ درخواست - عمداً **درون‌حافظه‌ای و بدونِ هیچ وابستگیِ جدید** (نه Redis، نه
 * پلاگینِ RateLimitِ Ktor 3): سرور تک‌نمونه‌ست و با pm2 اجرا می‌شه، پس یه شمارنده‌ی ساده‌ی
 * پنجره‌ی لغزان کافیه. با ری‌استارتِ سرور شمارنده‌ها صفر می‌شن که برای این کاربرد اشکالی نداره.
 *
 * 🚨 **دلیلِ وجودش (حفره‌ی امنیتیِ رفع‌شده - مرداد ۱۴۰۵)**: `/api/auth/request-otp` فقط یه
 * کول‌داونِ ۶۰ثانیه‌ای **به‌ازای هر شماره** داشت، ولی هیچ سقفی به‌ازای هر IP نبود. یعنی یه اسکریپت
 * می‌تونست با هزاران شماره‌ی متفاوت، در چند دقیقه **کلِ اعتبارِ پیامکِ ملی‌پیامک رو بسوزونه** -
 * ضررِ مالیِ مستقیم. `/api/crash` هم بدونِ auth و بدونِ سقف باز بود (پرکردنِ دیسکِ سرور).
 */
object RateLimit {
    private data class Window(var windowStartMs: Long, var count: Int)

    private val buckets = ConcurrentHashMap<String, Window>()

    /** آخرین پاک‌سازیِ کلیدهای منقضی - جلوگیری از رشدِ بی‌نهایتِ مپ. */
    @Volatile
    private var lastSweepMs = System.currentTimeMillis()

    /**
     * اگه [key] تو [windowMs] میلی‌ثانیه‌ی اخیر بیشتر از [limit] بار دیده شده باشه false برمی‌گردونه.
     * فراخوانِ موفق خودش شمارنده رو یکی زیاد می‌کنه.
     */
    fun allow(key: String, limit: Int, windowMs: Long): Boolean {
        val now = System.currentTimeMillis()
        sweepIfNeeded(now, windowMs)
        val window = buckets.compute(key) { _, existing ->
            if (existing == null || now - existing.windowStartMs >= windowMs) {
                Window(now, 1)
            } else {
                existing.count++
                existing
            }
        }!!
        return window.count <= limit
    }

    private fun sweepIfNeeded(now: Long, windowMs: Long) {
        if (now - lastSweepMs < 10 * 60 * 1000) return
        lastSweepMs = now
        buckets.entries.removeIf { now - it.value.windowStartMs > maxOf(windowMs, 60 * 60 * 1000L) }
    }

    fun reset() = buckets.clear()
}

/**
 * IP واقعیِ کلاینت. چون nginx جلوی سرور نشسته، بی این تابع همه‌ی درخواست‌ها یک IPِ واحد
 * (خودِ localhost) دیده می‌شدند و محدودیت بی‌معنی می‌شد.
 *
 * 🚨 **فقط `X-Real-IP` خوانده می‌شود، نه `X-Forwarded-For`.** نسخه‌ی قبلی اولین عضوِ
 * `X-Forwarded-For` را برمی‌داشت، ولی کانفیگِ nginxِ ما آن هدر را اصلاً ست نمی‌کند - یعنی
 * دستِ خودِ فرستنده بود. با یک `X-Forwarded-For: <عددِ تصادفی>` روی هر درخواست، هر سه سقفِ
 * OTP و سقفِ گزارشِ کرش بی‌اثر می‌شدند؛ دقیقاً همان چیزی که این فایل برای بستنش نوشته شد
 * (سوزاندنِ اعتبارِ پیامک و پرکردنِ دیسک). `X-Real-IP` را خودِ nginx می‌نویسد و هرچه کلاینت
 * بفرستد بازنویسی می‌شود، پس جعل‌شدنی نیست.
 */
fun ApplicationCall.clientIp(): String =
    request.headers["X-Real-IP"]?.trim()?.takeIf { it.isNotEmpty() }
        ?: request.origin.remoteHost

/**
 * سقفِ درخواست به‌ازای IP. اگه رد شد خودش پاسخِ ۴۲۹ می‌ده و `false` برمی‌گردونه - فراخوان باید
 * بلافاصله `return@post` کنه (همون الگوی [requireAuth]).
 */
suspend fun ApplicationCall.rateLimitOk(bucket: String, limit: Int, windowMs: Long): Boolean {
    val ip = clientIp()
    if (RateLimit.allow("$bucket:$ip", limit, windowMs)) return true
    Log.warn("rate_limit", "سقفِ درخواست رد شد", "bucket" to bucket, "ip" to maskIp(ip))
    respond(HttpStatusCode.TooManyRequests, mapOf("error" to "rate_limited"))
    return false
}
