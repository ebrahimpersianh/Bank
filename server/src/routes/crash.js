const express = require('express');
const db = require('../db');

const router = express.Router();

router.post('/', (req, res) => {
  const message = String(req.body?.message || '').slice(0, 2000);
  if (!message) return res.status(400).json({ error: 'no_message' });
  const stack = String(req.body?.stack || '').slice(0, 8000);
  const context = String(req.body?.context || '').slice(0, 500);
  const appVersion = String(req.body?.appVersion || '').slice(0, 50);

  db.prepare(
    `INSERT INTO crash_reports (message, stack, context, app_version) VALUES (?, ?, ?, ?)`
  ).run(message, stack, context, appVersion);

  res.json({ ok: true });
});

module.exports = router;
