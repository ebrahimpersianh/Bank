const express = require('express');
const db = require('../db');
const { requireAuth } = require('../middleware/auth');
const { isConfigured, validateInAppPurchase } = require('../cafebazaar');

const router = express.Router();
router.use(requireAuth);

/* کلاینت بعد از یه خرید موفق (purchaseProduct از پلاگین Poolakey) این رو صدا می‌زنه؛
   ما هم مستقیماً حرف کلاینت رو باور نمی‌کنیم، خودمون با API کافه‌بازار تایید می‌کنیم. */
router.post('/verify', async (req, res) => {
  if (!isConfigured()) return res.status(503).json({ error: 'cafebazaar_not_configured' });

  const { productId, purchaseToken } = req.body || {};
  if (!productId || !purchaseToken) return res.status(400).json({ error: 'invalid_input' });

  try {
    const valid = await validateInAppPurchase(productId, purchaseToken);
    if (!valid) return res.status(400).json({ error: 'purchase_not_valid' });
  } catch (e) {
    return res.status(502).json({ error: 'cafebazaar_validation_failed' });
  }

  db.prepare(`UPDATE users SET subscribed = 1 WHERE id = ?`).run(req.user.uid);
  res.json({ ok: true, subscribed: true });
});

module.exports = router;
