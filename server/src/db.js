const Database = require('better-sqlite3');
const path = require('path');

const dbPath = process.env.DB_PATH || path.join(__dirname, '..', 'data.sqlite');
const db = new Database(dbPath);
db.pragma('journal_mode = WAL');

db.exec(`
  CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    phone TEXT UNIQUE NOT NULL,
    subscribed INTEGER NOT NULL DEFAULT 0,
    subscribed_until TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
  );

  CREATE TABLE IF NOT EXISTS otps (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    phone TEXT NOT NULL,
    code_hash TEXT NOT NULL,
    expires_at INTEGER NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
  );

  CREATE TABLE IF NOT EXISTS loans (
    user_id INTEGER PRIMARY KEY REFERENCES users(id),
    data TEXT NOT NULL DEFAULT '[]',
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
  );

  CREATE TABLE IF NOT EXISTS crash_reports (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    message TEXT NOT NULL,
    stack TEXT,
    context TEXT,
    app_version TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
  );
`);

/* migration برای دیتابیس‌های قدیمی که از قبل جدول users رو بدون این ستون‌ها دارن */
try { db.exec(`ALTER TABLE users ADD COLUMN subscribed INTEGER NOT NULL DEFAULT 0`); } catch (e) { /* از قبل وجود داره */ }
try { db.exec(`ALTER TABLE users ADD COLUMN subscribed_until TEXT`); } catch (e) { /* از قبل وجود داره */ }

module.exports = db;
