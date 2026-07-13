/* ================= ارسال پیامک OTP =================
   سرویس: پیام‌رسان (payam-resan.com) — وب‌سرویس REST تحت آدرس sms-webservice.com.
   برای استفاده از یه سرویس پیامکی دیگه، فقط تابع sendOtpSms رو با فراخوانی API همون
   سرویس جایگزین کن؛ بقیه‌ی کد (routes/auth) کاری به این نداره که پیامک از کجا میره.
   اگه PAYAMRESAN_API_KEY ست نشده باشه (مثلاً موقع توسعه‌ی محلی)، کد رو فقط تو لاگ سرور
   چاپ می‌کنه تا بدون داشتن حساب واقعی پیامکی هم بشه کل فلوی ورود رو تست کرد. */

const PAYAMRESAN_API_KEY = process.env.PAYAMRESAN_API_KEY || '';
const PAYAMRESAN_SENDER = process.env.PAYAMRESAN_SENDER || '';

async function sendOtpSms(phone, code) {
  if (!PAYAMRESAN_API_KEY) {
    console.log(`[SMS-DEV] کد تایید برای ${phone}: ${code} (PAYAMRESAN_API_KEY ست نشده، پیامک واقعی ارسال نشد)`);
    return { ok: true, dev: true };
  }

  const message = `کد تایید وام من: ${code}`;
  const params = new URLSearchParams();
  params.append('ApiKey', PAYAMRESAN_API_KEY);
  params.append('Text', message);
  params.append('Sender', PAYAMRESAN_SENDER);
  params.append('Recipients', phone);
  const url = `https://api.sms-webservice.com/api/V3/Send?${params.toString()}`;

  const res = await fetch(url);
  const json = await res.json().catch(() => null);
  if (!res.ok) {
    console.error('خطای ارسال پیامک پیام‌رسان:', json || res.status);
    throw new Error('sms_send_failed');
  }
  return { ok: true };
}

module.exports = { sendOtpSms };
