package ir.sadteam.loancalc.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.sql.Connection

/**
 * بازبینیِ دوره‌ایِ خریدهای اشتراک با API استور (۲۷ آبان — یافته‌ی بازبینی، server.md §۶ مورد ۱).
 *
 * چرا این کار لازم است: نه کافه‌بازار نه مایکت هیچ وب‌هوک/اعلانِ ریفاند (RTDN) ندارند و
 * پاسخِ اعتبارسنجیِ هیچ‌کدام هم نشانه‌ی «خریدِ تستی» ندارد — خریدِ تستی در لحظه‌ی خرید
 * `purchaseState=0` برمی‌گرداند و مثلِ خریدِ واقعی اشتراک می‌گیرد؛ ریفاندِ همان روز
 * (در مایکت ساعتِ ۲۳:۵۵) هم هیچ‌چیز را پس نمی‌گیرد مگر اینکه دوباره از استور پرسیده شود.
 * پس هر چند ساعت یک بار، خریدهای تازه‌ی هنوز-معتبر را دوباره با همان API چک می‌کنیم:
 * اگر `purchaseState` به ۱ برگشته بود، خرید «برگشت‌خورده» علامت می‌خورد، از آمارِ فروش
 * کنار می‌رود و روزهای اشتراکی که داده بود پس گرفته می‌شود.
 *
 * خریدِ تستی را API لو نمی‌دهد؛ راهِ دستی‌اش `POST /api/admin/purchases/flag` است
 * (رجوع کن به AdminRoutes) که همان [markTest] را صدا می‌زند.
 */
object PurchaseRevalidation {
    private const val REVALIDATE_EVERY_MS = 6L * 60 * 60 * 1000
    private const val FIRST_RUN_DELAY_MS = 10L * 60 * 1000
    /** فقط خریدهای ۶۰ روزِ اخیر بازبینی می‌شوند — ریفاندِ تستی همان روز اتفاق می‌افتد. */
    private const val LOOKBACK_DAYS = 60L
    private const val BATCH_LIMIT = 100

    data class RunStats(val checked: Int, val stillValid: Int, val refunded: Int, val skipped: Int)

    private data class Candidate(
        val userId: Long,
        val productId: String,
        val store: String,
        val token: String,
        val durationDays: Int,
        val subscribedUntil: String,
    )

    fun start(scope: CoroutineScope) {
        scope.launch {
            delay(FIRST_RUN_DELAY_MS)
            while (true) {
                runCatching { runOnce() }.onFailure { e ->
                    Log.info("purchase_revalidate_error", "خطا در بازبینیِ خریدها", "error" to (e.message ?: "?"))
                }
                delay(REVALIDATE_EVERY_MS)
            }
        }
    }

    /** یک دور بازبینی؛ برای فراخوانیِ دستی/تست هم قابلِ استفاده است. */
    suspend fun runOnce(): RunStats {
        val candidates = Db.withConnection { conn ->
            conn.prepareStatement(
                """
                SELECT user_id, product_id, store, purchase_token, duration_days, subscribed_until
                FROM subscription_purchases
                WHERE refunded = 0 AND is_test = 0 AND created_at >= datetime('now', '-$LOOKBACK_DAYS days')
                  AND (last_validated_at IS NULL OR last_validated_at < datetime('now', '-24 hours'))
                ORDER BY created_at DESC LIMIT $BATCH_LIMIT
                """.trimIndent(),
            ).use { ps ->
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                Candidate(
                                    userId = rs.getLong(1), productId = rs.getString(2), store = rs.getString(3),
                                    token = rs.getString(4), durationDays = rs.getInt(5), subscribedUntil = rs.getString(6),
                                ),
                            )
                        }
                    }
                }
            }
        }
        var valid = 0; var refunded = 0; var skipped = 0
        for (c in candidates) {
            val configured = if (c.store == "myket") myketConfigured() else cafebazaarConfigured()
            if (!configured) {
                skipped++
                continue
            }
            val result = runCatching {
                if (c.store == "myket") validateMyketPurchase(c.productId, c.token)
                else validateInAppPurchase(c.productId, c.token)
            }.getOrNull()
            if (result == null) {
                // خطای API/شبکه: معلوم نیست برگشت خورده یا نه — هیچ‌چیز را علامت نمی‌زنیم؛
                // دورِ بعد دوباره چک می‌شود.
                skipped++
                Log.info("purchase_revalidate_skip", "بازبینیِ خرید به‌خاطرِ خطای API رد شد", "token" to maskToken(c.token))
                continue
            }
            if (result.refunded) {
                Db.withConnection { conn -> markRefunded(conn, c.token) }
                refunded++
                Log.info(
                    "purchase_refunded", "خریدِ برگشت‌خورده پیدا و پس گرفته شد",
                    "store" to c.store, "product" to c.productId, "uid" to c.userId,
                )
            } else {
                if (result.valid) {
                    Db.withConnection { conn ->
                        conn.execute(
                            "UPDATE subscription_purchases SET last_validated_at = datetime('now') WHERE purchase_token = ?",
                            c.token,
                        )
                    }
                    valid++
                } else {
                    // نامعتبر ولی نه صریحاً برگشت‌خورده (مثلاً فیلدِ purchaseState نیامد):
                    // اشتراک پس گرفته نمی‌شود و `last_validated_at` هم به‌روز نمی‌شود تا دورِ
                    // بعد دوباره چک شود.
                    skipped++
                }
            }
        }
        val stats = RunStats(candidates.size, valid, refunded, skipped)
        if (candidates.isNotEmpty()) {
            Log.info(
                "purchase_revalidate_done", "بازبینیِ خریدها تمام شد",
                "checked" to stats.checked, "stillValid" to stats.stillValid,
                "refunded" to stats.refunded, "skipped" to stats.skipped,
            )
        }
        return stats
    }

    /**
     * خرید را «برگشت‌خورده» علامت می‌زند و روزهای اشتراکی که داده بود را پس می‌گیرد.
     * پس‌گرفتن فقط وقتی انجام می‌شود که انقضای فعلیِ کاربر دقیقاً همان انقضایی باشد که این
     * خرید ساخته بود (یعنی بعد از آن خرید، هدیه/خریدِ دیگری انقضا را جلو نبرده باشد)؛
     * وگرنه فقط علامت می‌خورد و پس‌گرفتن به بررسیِ دستی می‌ماند تا روزهای هدیه/خریدِ
     * دیگر اشتباه کم نشود. فراخوانیِ دوباره بی‌اثر است (idempotent).
     */
    fun markRefunded(conn: Connection, purchaseToken: String): Boolean =
        revokePurchase(conn, purchaseToken, test = false)

    /** مثلِ [markRefunded] ولی برای خریدِ تستی که دستی علامت می‌خورد (API نشانه‌ای ندارد). */
    fun markTest(conn: Connection, purchaseToken: String): Boolean =
        revokePurchase(conn, purchaseToken, test = true)

    private fun revokePurchase(conn: Connection, purchaseToken: String, test: Boolean): Boolean {
        val row = conn.queryOne(
            "SELECT user_id, duration_days, subscribed_until FROM subscription_purchases WHERE purchase_token = ?",
            purchaseToken,
        ) { rs -> Triple(rs.getLong(1), rs.getInt(2), rs.getString(3)) } ?: return false
        val (uid, durationDays, purchaseUntil) = row
        conn.autoCommit = false
        try {
            if (test) {
                conn.execute(
                    "UPDATE subscription_purchases SET is_test = 1, last_validated_at = datetime('now') WHERE purchase_token = ?",
                    purchaseToken,
                )
            } else {
                conn.execute(
                    "UPDATE subscription_purchases SET refunded = 1, refunded_at = datetime('now'), " +
                        "last_validated_at = datetime('now') WHERE purchase_token = ?",
                    purchaseToken,
                )
            }
            // عقب‌کشیدنِ انقضا فقط وقتی امن است که همین خرید آخرین چیزی باشد که انقضا را ساخته.
            // چون تمدید همیشه از max(انقضای فعلی، الان) + مدت انجام می‌شود، کم‌کردنِ همان مدت
            // دقیقاً انقضایِ پیش از این خرید را برمی‌گرداند.
            val currentUntil = conn.queryOne("SELECT subscribed_until FROM users WHERE id = ?", uid) { it.getString(1) }
            if (currentUntil != null && currentUntil == purchaseUntil) {
                val untilMs = runCatching { java.time.Instant.parse(purchaseUntil).toEpochMilli() }.getOrNull()
                if (untilMs != null) {
                    val rolledBack = java.time.Instant.ofEpochMilli(untilMs - durationDays * 24L * 60 * 60 * 1000).toString()
                    conn.execute("UPDATE users SET subscribed_until = ? WHERE id = ?", rolledBack, uid)
                }
            }
            conn.commit()
            return true
        } catch (e: Exception) {
            runCatching { conn.rollback() }
            throw e
        } finally {
            runCatching { conn.autoCommit = true }
        }
    }
}
