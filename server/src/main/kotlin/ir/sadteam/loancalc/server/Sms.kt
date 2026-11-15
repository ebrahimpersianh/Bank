package ir.sadteam.loancalc.server

/* ================= ارسال پیامک OTP =================
   سرویس: ملی‌پیامک (melipayamak.com) — وب‌سرویس SOAP قدیمی (payamak-panel.com)، متد
   SendByBaseNumber: ارسال با یه پترن متنی از پیش تاییدشده (bodyId) از طریق خط اشتراکی، بدون
   نیاز به خط اختصاصی. بدون یه bodyId تاییدشده تو پنل ملی‌پیامک (پیامک > پترن‌ها)، ارسال همیشه
   شکست می‌خوره — این یه پیش‌نیاز دستیه، کد نمی‌تونه جایگزینش کنه.
   برای استفاده از یه سرویس پیامکی دیگه، فقط تابع sendOtpSms رو با فراخوانی API همون
   سرویس جایگزین کن؛ بقیه‌ی کد (routes/AuthRoutes) کاری به این نداره که پیامک از کجا میره.
   اگه MELIPAYAMAK_USERNAME/MELIPAYAMAK_PASSWORD/MELIPAYAMAK_BODY_ID ست نشده باشن (مثلاً موقع
   توسعه‌ی محلی)، کد رو فقط تو لاگ سرور چاپ می‌کنه تا بدون داشتن حساب واقعی پیامکی هم بشه کل
   فلوی ورود رو تست کرد. */

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

private val MELIPAYAMAK_USERNAME = env("MELIPAYAMAK_USERNAME")
private val MELIPAYAMAK_PASSWORD = env("MELIPAYAMAK_PASSWORD")
private val MELIPAYAMAK_BODY_ID = env("MELIPAYAMAK_BODY_ID")

// طبق مستندات: http://api.payamak-panel.com/post/send.asmx (SOAP 1.1، غیر-WSDL)
private const val MELIPAYAMAK_SOAP_URL = "http://api.payamak-panel.com/post/send.asmx"
private const val MELIPAYAMAK_SOAP_ACTION = "http://tempuri.org/SendByBaseNumber"

private val smsHttpClient = HttpClient(CIO)

class SmsSendException(message: String = "sms_send_failed") : Exception(message)

private fun xmlEscape(value: String): String =
    value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

suspend fun sendOtpSms(phone: String, code: String) {
    if (MELIPAYAMAK_USERNAME.isEmpty() || MELIPAYAMAK_PASSWORD.isEmpty() || MELIPAYAMAK_BODY_ID.isEmpty()) {
        println("[SMS-DEV] کد تایید برای $phone: $code (تنظیمات ملی‌پیامک ست نشده، پیامک واقعی ارسال نشد)")
        return
    }

    val envelope = """
        <?xml version="1.0" encoding="utf-8"?>
        <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Body>
            <SendByBaseNumber xmlns="http://tempuri.org/">
              <username>${xmlEscape(MELIPAYAMAK_USERNAME)}</username>
              <password>${xmlEscape(MELIPAYAMAK_PASSWORD)}</password>
              <text>
                <string>${xmlEscape(code)}</string>
              </text>
              <to>${xmlEscape(phone)}</to>
              <bodyId>${MELIPAYAMAK_BODY_ID}</bodyId>
            </SendByBaseNumber>
          </soap:Body>
        </soap:Envelope>
    """.trimIndent()

    val response = smsHttpClient.post(MELIPAYAMAK_SOAP_URL) {
        headers {
            append(HttpHeaders.ContentType, "text/xml; charset=utf-8")
            append("SOAPAction", MELIPAYAMAK_SOAP_ACTION)
        }
        setBody(envelope)
    }

    val bodyText = response.bodyAsText()
    val result = Regex("<SendByBaseNumberResult>(.*?)</SendByBaseNumberResult>")
        .find(bodyText)?.groupValues?.get(1)

    // پاسخ موفق یه recId یکتای طولانیه (۱۵+ رقم)؛ کدهای خطا (۰-۱۹ یا -۱۰۸..-۱۱۰) کوتاهن.
    if (!response.status.isSuccess() || result.isNullOrEmpty() || result.length < 8) {
        throw SmsSendException("sms_send_failed:${result ?: bodyText.take(200)}")
    }
}
