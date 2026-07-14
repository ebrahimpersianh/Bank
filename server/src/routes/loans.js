const express = require('express');
const db = require('../db');
const { requireAuth } = require('../middleware/auth');

const router = express.Router();
router.use(requireAuth);

/* همگام‌سازی به سبک «کل آرایه رو بگیر/بده» — چون سمت کلاینت هم currentLoans
   همیشه یه آرایه‌ی کامل JSON تو localStorage بوده، همین‌جوری هم رو سرور نگه می‌داریم؛
   منطق merge/تعارض (کدوم گوشی برنده باشه) سمت کلاینته، نه اینجا. */

router.get('/', (req, res) => {
  const row = db.prepare(`SELECT data, updated_at FROM loans WHERE user_id = ?`).get(req.user.uid);
  res.json({ loans: JSON.parse(row?.data || '[]'), updatedAt: row?.updated_at || null });
});

router.put('/', (req, res) => {
  const loans = req.body?.loans;
  if (!Array.isArray(loans)) return res.status(400).json({ error: 'invalid_loans' });

  /* بدون اشتراک فقط یه وام مجازه؛ این جلوی دور زدن محدودیت از طریق فراخوانی مستقیم API
     رو می‌گیره (منطق اصلی/پیام به کاربر سمت کلاینته، این فقط یه لایه‌ی دفاعی سمت سرورـه) */
  const user = db.prepare(`SELECT subscribed FROM users WHERE id = ?`).get(req.user.uid);
  if (!user?.subscribed && loans.length > 1) {
    return res.status(403).json({ error: 'subscription_required' });
  }

  db.prepare(`
    INSERT INTO loans (user_id, data, updated_at) VALUES (?, ?, datetime('now'))
    ON CONFLICT(user_id) DO UPDATE SET data = excluded.data, updated_at = excluded.updated_at
  `).run(req.user.uid, JSON.stringify(loans));

  res.json({ ok: true });
});

module.exports = router;
