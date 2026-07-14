const express = require('express');
const db = require('../db');
const { requireAuth } = require('../middleware/auth');
const { isConfigured, validateInAppPurchase } = require('../cafebazaar');

const router = express.Router();
router.use(requireAuth);

/* هر پلن یه محصولِ «خرید یک‌باره»‌ی جداگانه‌ست تو پنل کافه‌بازار (نه اشتراک واقعی
   تمدیدشونده، چون پلاگین Poolakey که وصله فقط purchaseProduct رو می‌ده) — این نگاشت
   شناسه‌ی محصول به تعداد روزی هست که با خریدش به subscribed_until کاربر اضافه می‌شه. */
const TIER_DURATION_DAYS = {
  unlimited_loans_1m: 30,
  unlimited_loans_3m: 90,
  unlimited_loans_6m: 180,
  unlimited_loans_1y: 365,
};

/* کلاینت بعد از یه خرید موفق (purchaseProduct از پلاگین Poolakey) این رو صدا می‌زنه؛
   ما هم مستقیماً حرف کلاینت رو باور نمی‌کنیم، خودمون با API کافه‌بازار تایید می‌کنیم. */
router.post('/verify', async (req, res) => {
  if (!isConfigured()) return res.status(503).json({ error: 'cafebazaar_not_configured' });

  const { productId, purchaseToken } = req.body || {};
  if (!productId || !purchaseToken) return res.status(400).json({ error: 'invalid_input' });
  const durationDays = TIER_DURATION_DAYS[productId];
  if (!durationDays) return res.status(400).json({ error: 'unknown_product' });

  try {
    const valid = await validateInAppPurchase(productId, purchaseToken);
    if (!valid) return res.status(400).json({ error: 'purchase_not_valid' });
  } catch (e) {
    return res.status(502).json({ error: 'cafebazaar_validation_failed' });
  }

  /* اگه اشتراک قبلی هنوز فعاله، از رو همون تاریخ انقضا جلو می‌ریم (نه از الان) تا خرید
     زودتر از موعد، مدت باقی‌مونده رو از دست ندی. */
  const user = db.prepare(`SELECT subscribed_until FROM users WHERE id = ?`).get(req.user.uid);
  const now = Date.now();
  const currentExpiry = user?.subscribed_until ? new Date(user.subscribed_until).getTime() : 0;
  const base = currentExpiry > now ? currentExpiry : now;
  const newExpiry = new Date(base + durationDays * 24 * 60 * 60 * 1000).toISOString();

  db.prepare(`UPDATE users SET subscribed_until = ? WHERE id = ?`).run(newExpiry, req.user.uid);
  res.json({ ok: true, subscribed: true, subscribedUntil: newExpiry });
});

module.exports = router;
