package ir.sadteam.loancalc.server

/* ================= ارسال پیامک OTP =================
   سرویس: پیام‌رسان (payam-resan.com) — وب‌سرویس REST تحت آدرس sms-webservice.com.
   برای استفاده از یه سرویس پیامکی دیگه، فقط تابع sendOtpSms رو با فراخوانی API همون
   سرویس جایگزین کن؛ بقیه‌ی کد (routes/AuthRoutes) کاری به این نداره که پیامک از کجا میره.
   اگه PAYAMRESAN_API_KEY ست نشده باشه (مثلاً موقع توسعه‌ی محلی)، کد رو فقط تو لاگ سرور
   چاپ می‌کنه تا بدون داشتن حساب واقعی پیامکی هم بشه کل فلوی ورود رو تست کرد. */

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess

private val PAYAMRESAN_API_KEY = env("PAYAMRESAN_API_KEY")
private val PAYAMRESAN_SENDER = env("PAYAMRESAN_SENDER")

private val smsHttpClient = HttpClient(CIO)

class SmsSendException : Exception("sms_send_failed")

suspend fun sendOtpSms(phone: String, code: String) {
    if (PAYAMRESAN_API_KEY.isEmpty()) {
        println("[SMS-DEV] کد تایید برای $phone: $code (PAYAMRESAN_API_KEY ست نشده، پیامک واقعی ارسال نشد)")
        return
    }

    val message = "کد تایید وام من: $code"
    val response = smsHttpClient.get("https://api.sms-webservice.com/api/V3/Send") {
        parameter("ApiKey", PAYAMRESAN_API_KEY)
        parameter("Text", message)
        parameter("Sender", PAYAMRESAN_SENDER)
        parameter("Recipients", phone)
    }
    if (!response.status.isSuccess()) {
        throw SmsSendException()
    }
}
