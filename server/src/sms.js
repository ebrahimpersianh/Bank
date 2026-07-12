/* ================= ارسال پیامک OTP =================
   پیش‌فرض: کاوه‌نگار (kavenegar.com) — چون REST ساده و بدون نیاز به گوگل/سرویس خارجی داره.
   برای استفاده از یه سرویس پیامکی دیگه (ملی‌پیامک، ippanel و ...)، فقط تابع sendOtpSms
   رو با فراخوانی API همون سرویس جایگزین کن؛ بقیه‌ی کد (routes/auth) کاری به این نداره
   که پیامک از کجا میره.
   اگه KAVENEGAR_API_KEY ست نشده باشه (مثلاً موقع توسعه‌ی محلی)، کد رو فقط تو لاگ سرور
   چاپ می‌کنه تا بدون داشتن حساب واقعی پیامکی هم بشه کل فلوی ورود رو تست کرد. */

const KAVENEGAR_API_KEY = process.env.KAVENEGAR_API_KEY || '';
const KAVENEGAR_SENDER = process.env.KAVENEGAR_SENDER || '';

async function sendOtpSms(phone, code) {
  if (!KAVENEGAR_API_KEY) {
    console.log(`[SMS-DEV] کد تایید برای ${phone}: ${code} (KAVENEGAR_API_KEY ست نشده، پیامک واقعی ارسال نشد)`);
    return { ok: true, dev: true };
  }

  const message = `کد تایید وام من: ${code}`;
  const url = `https://api.kavenegar.com/v1/${KAVENEGAR_API_KEY}/sms/send.json` +
    `?receptor=${encodeURIComponent(phone)}&message=${encodeURIComponent(message)}` +
    (KAVENEGAR_SENDER ? `&sender=${encodeURIComponent(KAVENEGAR_SENDER)}` : '');

  const res = await fetch(url);
  const json = await res.json().catch(() => null);
  if (!res.ok || !json || json.return?.status !== 200) {
    console.error('خطای ارسال پیامک کاوه‌نگار:', json || res.status);
    throw new Error('sms_send_failed');
  }
  return { ok: true };
}

module.exports = { sendOtpSms };
