const express = require('express');
const crypto = require('crypto');
const jwt = require('jsonwebtoken');
const db = require('../db');
const { sendOtpSms } = require('../sms');
const { JWT_SECRET, requireAuth } = require('../middleware/auth');
const { isSubscribed, trialEndsAtMs } = require('../subscriptionStatus');

const router = express.Router();

const PHONE_RE = /^09\d{9}$/;
const OTP_TTL_MS = 2 * 60 * 1000; // ۲ دقیقه اعتبار کد
const OTP_RESEND_COOLDOWN_MS = 60 * 1000; // حداقل فاصله بین دو درخواست کد برای یه شماره
const MAX_VERIFY_ATTEMPTS = 5;

function hashCode(code) {
  return crypto.createHash('sha256').update(code).digest('hex');
}

router.post('/request-otp', async (req, res) => {
  const phone = String(req.body?.phone || '').trim();
  if (!PHONE_RE.test(phone)) {
    return res.status(400).json({ error: 'invalid_phone' });
  }

  const recent = db.prepare(
    `SELECT created_at FROM otps WHERE phone = ? ORDER BY id DESC LIMIT 1`
  ).get(phone);
  if (recent && Date.now() - new Date(recent.created_at + 'Z').getTime() < OTP_RESEND_COOLDOWN_MS) {
    return res.status(429).json({ error: 'too_soon' });
  }

  const code = String(crypto.randomInt(10000, 100000)); // ۵ رقمی
  const expiresAt = Date.now() + OTP_TTL_MS;
  db.prepare(`INSERT INTO otps (phone, code_hash, expires_at) VALUES (?, ?, ?)`)
    .run(phone, hashCode(code), expiresAt);

  try {
    await sendOtpSms(phone, code);
  } catch (e) {
    return res.status(502).json({ error: 'sms_send_failed' });
  }

  res.json({ ok: true });
});

router.post('/verify-otp', (req, res) => {
  const phone = String(req.body?.phone || '').trim();
  const code = String(req.body?.code || '').trim();
  if (!PHONE_RE.test(phone) || !code) {
    return res.status(400).json({ error: 'invalid_input' });
  }

  const otp = db.prepare(
    `SELECT * FROM otps WHERE phone = ? ORDER BY id DESC LIMIT 1`
  ).get(phone);
  if (!otp) return res.status(400).json({ error: 'no_otp_requested' });
  if (otp.attempts >= MAX_VERIFY_ATTEMPTS) return res.status(429).json({ error: 'too_many_attempts' });
  if (Date.now() > otp.expires_at) return res.status(400).json({ error: 'code_expired' });

  db.prepare(`UPDATE otps SET attempts = attempts + 1 WHERE id = ?`).run(otp.id);
  if (hashCode(code) !== otp.code_hash) {
    return res.status(400).json({ error: 'wrong_code' });
  }

  db.prepare(`DELETE FROM otps WHERE id = ?`).run(otp.id); // یک‌بارمصرف؛ نذار همین کد دوباره جواب بده

  let user = db.prepare(`SELECT * FROM users WHERE phone = ?`).get(phone);
  if (!user) {
    const info = db.prepare(`INSERT INTO users (phone) VALUES (?)`).run(phone);
    db.prepare(`INSERT INTO loans (user_id, data) VALUES (?, '[]')`).run(info.lastInsertRowid);
    // بعد از insert دوباره از دیتابیس می‌خونیم (نه یه آبجکت دستیِ ناقص) تا created_at واقعی
    // (لازم برای محاسبه‌ی دوره‌ی آزمایشی ۷ روزه‌ی isSubscribed) رو داشته باشیم.
    user = db.prepare(`SELECT * FROM users WHERE id = ?`).get(info.lastInsertRowid);
  }

  const token = jwt.sign({ uid: user.id, phone: user.phone }, JWT_SECRET, { expiresIn: '90d' });
  res.json({ token, phone: user.phone, subscribed: isSubscribed(user), trialEndsAt: trialEndsAtMs(user) });
});

/* وضعیت فعلی حساب (از جمله اشتراک) — کلاینت بعد از باز شدن اپ این رو صدا می‌زنه تا اگه
   اشتراک از جای دیگه (مثلاً گوشی دیگه، یا بعداً از طریق خرید درون‌برنامه‌ای کافه‌بازار)
   فعال شده باشه، بدون نیاز به لاگین مجدد باخبر بشه. */
router.get('/me', requireAuth, (req, res) => {
  const user = db.prepare(`SELECT phone, subscribed, subscribed_until, created_at FROM users WHERE id = ?`).get(req.user.uid);
  if (!user) return res.status(404).json({ error: 'user_not_found' });
  res.json({
    phone: user.phone,
    subscribed: isSubscribed(user),
    subscribedUntil: user.subscribed_until || null,
    trialEndsAt: trialEndsAtMs(user),
  });
});

module.exports = router;
