package ir.sadteam.loancalc.server.routes

import ir.sadteam.loancalc.server.RateLimit
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import ir.sadteam.loancalc.server.Db
import ir.sadteam.loancalc.server.Log
import ir.sadteam.loancalc.server.SmsSendException
import ir.sadteam.loancalc.server.env
import ir.sadteam.loancalc.server.execute
import ir.sadteam.loancalc.server.maskPhone
import ir.sadteam.loancalc.server.rateLimitOk
import ir.sadteam.loancalc.server.insertReturningId
import ir.sadteam.loancalc.server.isSubscribed
import ir.sadteam.loancalc.server.parseUtc
import ir.sadteam.loancalc.server.queryOne
import ir.sadteam.loancalc.server.requireAuth
import ir.sadteam.loancalc.server.sendOtpSms
import ir.sadteam.loancalc.server.signToken
import ir.sadteam.loancalc.server.toUserRow
import ir.sadteam.loancalc.server.trialDaysLeftIfApplicable
import kotlinx.serialization.Serializable
import java.security.MessageDigest
import java.security.SecureRandom

private val PHONE_RE = Regex("^09\\d{9}$")
private const val OTP_TTL_MS = 2 * 60 * 1000L // ۲ دقیقه اعتبار کد
private const val OTP_RESEND_COOLDOWN_MS = 60 * 1000L // حداقل فاصله بین دو درخواست کد برای یه شماره
private const val MAX_VERIFY_ATTEMPTS = 5

/* سقفِ درخواست به‌ازای هر IP (رجوع کن به RateLimit.kt برای دلیلِ امنیتی). عددها عمداً خیلی
   بالاتر از نیازِ یه کاربرِ واقعی‌ان (یه آدم عادی روزی چند بار لاگین نمی‌کنه)، ولی جلوی اسکریپتی
   که می‌خواد اعتبارِ پیامک رو بسوزونه رو کاملاً می‌گیرن. */
private const val HOUR_MS = 60 * 60 * 1000L
// ۷ مهر: اپراتورهای موبایلِ ایران کاربران را پشتِ IPِ مشترک (CGNAT/VPN) می‌برند؛ ۸ در ساعت برای
// «هر IP» عملاً سقفِ جمعیِ صدها کاربر بود و ورودِ واقعی رد می‌شد. سقفِ اصلی کول‌داون + سقفِ روزانه‌ی هر شماره است.
// ۱۶ مهر (تأییدِ مالک): ۴۰ کم بود - اینترنتِ همراه صدها کاربر را پشتِ یک IP می‌برد (۴۳ بار
// «درخواستِ زیاد» برای ۱۰ نفر). سقفِ اصلی حالا به‌ازای **گوشی** است (پایین‌تر)؛ این فقط سدِ آخر است.
private const val OTP_REQUESTS_PER_IP_PER_HOUR = 120
private const val OTP_REQUESTS_PER_IP_PER_DAY = 200
private const val VERIFY_ATTEMPTS_PER_IP_PER_HOUR = 30

private val secureRandom = SecureRandom()

/* حسابِ تستِ دائمی (برای فرمِ «حساب تستی»ِ بررسی‌کننده‌های کافه‌بازار/مایکت + تستِ خودِ توسعه‌دهنده):
   اگه TEST_ACCOUNT_PHONE و TEST_ACCOUNT_CODE تو .env ست شده باشن، برای اون یه شماره هیچ پیامکی
   ارسال نمی‌شه و کدِ تایید همیشه همون مقدارِ ثابته - یعنی حتی بعد از فعال‌شدنِ پیامکِ واقعیِ
   ملی‌پیامک هم این حساب کار می‌کنه (شماره‌ش غیرواقعیه و پیامک بهش نمی‌رسید). برای بقیه‌ی شماره‌ها
   هیچ رفتاری عوض نمی‌شه؛ تا وقتی این دو env ست نشن، این قابلیت کلاً خاموشه. */
private val TEST_ACCOUNT_PHONE = env("TEST_ACCOUNT_PHONE")
private val TEST_ACCOUNT_CODE = env("TEST_ACCOUNT_CODE")

private fun isTestAccount(phone: String): Boolean =
    TEST_ACCOUNT_PHONE.isNotEmpty() && TEST_ACCOUNT_CODE.isNotEmpty() && phone == TEST_ACCOUNT_PHONE

private fun hashCode(code: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(code.toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
}

@Serializable
private data class RequestOtpBody(val phone: String? = null, val store: String? = null, val device: String? = null)

@Serializable
private data class VerifyOtpBody(
    val phone: String? = null,
    val code: String? = null,
    /** کدِ یک‌طرفه‌ی شناسه‌ی گوشی (SHA-256؛ خودِ شناسه هرگز نمی‌آید) - برای «ماهِ مجانی یک بار برای هر گوشی». */
    val deviceHash: String? = null,
)

private val DEVICE_HASH_RE = Regex("^[0-9a-f]{64}$")

@Serializable
private data class VerifyOtpResponse(
    val token: String,
    val phone: String,
    val subscribed: Boolean,
    val subscribedUntil: String?,
    val subscriptionTier: String?,
    val trialDaysLeft: Int?,
)

@Serializable
private data class MeResponse(
    val phone: String,
    /** شماره‌ی کاربریِ یکتا - در صفحه‌ی «حساب کاربری» برای پشتیبانی نشان داده می‌شود. */
    val userId: Long = 0,
    /** شماره‌ی کاربریِ نمایشی، مثلِ `Uid:7405024` - رجوع کن به [UserCode]. */
    val userCode: String? = null,
    val subscribed: Boolean,
    val subscribedUntil: String?,
    val subscriptionTier: String?,
    val trialDaysLeft: Int?,
    /** true یعنی این کاربر ۱۵ روزِ هدیه‌ی «قدیمی‌بودن» گرفته - اپ جمله‌ی اضافه رو نشون می‌ده. */
    val legacyGift: Boolean = false,
    /** نامِ اختیاریِ کاربر؛ null یعنی هنوز وارد نکرده (کاملاً عادیه). */
    val name: String? = null,
)

@Serializable
private data class SetNameBody(val name: String? = null)

private data class OtpRow(val id: Long, val codeHash: String, val expiresAt: Long, val attempts: Int)

fun Route.authRoutes() {
    route("/api/auth") {
        post("/request-otp") {
            /* 🚨 دو سقفِ هم‌زمان: ساعتی و روزانه. بدونِ این، کول‌داونِ ۶۰ثانیه‌ایِ پایین که
               به‌ازای **شماره**ست، با چرخوندنِ شماره‌ها به‌راحتی دور زده می‌شد. */
            if (!call.rateLimitOk("otp_req_h", OTP_REQUESTS_PER_IP_PER_HOUR, HOUR_MS)) return@post
            if (!call.rateLimitOk("otp_req_d", OTP_REQUESTS_PER_IP_PER_DAY, 24 * HOUR_MS)) return@post
            val body = runCatching { call.receive<RequestOtpBody>() }.getOrNull()
            val phone = (body?.phone ?: "").trim()
            if (!PHONE_RE.matches(phone)) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_phone"))
                return@post
            }

            // سقفِ هر **گوشی** (۱۶ مهر): ۸ در ساعت، ۲۰ در روز - کاربرهای پشتِ یک IP دیگر جریمه‌ی هم را نمی‌دهند.
            val device = body?.device?.filter { it.isLetterOrDigit() }?.take(80)?.takeIf { it.length >= 8 }
            if (device != null && (!RateLimit.allow("otp_dev_h:$device", 8, HOUR_MS) || !RateLimit.allow("otp_dev_d:$device", 20, 24 * HOUR_MS))) {
                call.respond(HttpStatusCode.TooManyRequests, mapOf("error" to "rate_limited"))
                return@post
            }
            // سقفِ هر **شماره** (جایگزینِ سقفِ سخت‌گیرانه‌ی IP): ۶ پیامک در ساعت، ۱۵ در روز.
            if (!RateLimit.allow("otp_phone_h:$phone", 6, HOUR_MS) || !RateLimit.allow("otp_phone_d:$phone", 15, 24 * HOUR_MS)) {
                call.respond(HttpStatusCode.TooManyRequests, mapOf("error" to "rate_limited"))
                return@post
            }
            val recentCreatedAt = Db.withConnection { conn ->
                conn.queryOne("SELECT created_at FROM otps WHERE phone = ? ORDER BY id DESC LIMIT 1", phone) { rs ->
                    rs.getString("created_at")
                }
            }
            if (recentCreatedAt != null && System.currentTimeMillis() - parseUtc(recentCreatedAt) < OTP_RESEND_COOLDOWN_MS) {
                call.respond(HttpStatusCode.TooManyRequests, mapOf("error" to "too_soon"))
                return@post
            }

            // حسابِ تست: کدِ ثابت از env، بدونِ ارسالِ پیامک (رجوع کن به کامنتِ TEST_ACCOUNT_PHONE بالا).
            val testAccount = isTestAccount(phone)
            val code = if (testAccount) TEST_ACCOUNT_CODE else (10000 + secureRandom.nextInt(90000)).toString() // ۵ رقمی
            val expiresAt = System.currentTimeMillis() + OTP_TTL_MS
            Db.withConnection { conn ->
                // کدهای منقضی هیچ‌وقت پاک نمی‌شدند، پس جدول به یک آرشیوِ دائمی از هر شماره‌ای
                // که تا حالا کد گرفته تبدیل می‌شد - داده‌ی شخصی که هیچ کاربردی ندارد.
                conn.execute("DELETE FROM otps WHERE expires_at < ?", System.currentTimeMillis())
                conn.execute(
                    "INSERT INTO otps (phone, code_hash, expires_at) VALUES (?, ?, ?)",
                    phone, hashCode(code), expiresAt
                )
            }

            if (!testAccount) {
                // استورِ پیامک: اپِ تازه خودش می‌فرستد؛ نسخه‌ی قدیمی از آخرین نصبِ همین شماره.
                val smsStore = body?.store?.lowercase()?.filter { it in 'a'..'z' }?.take(20)?.takeIf { it.isNotEmpty() }
                    ?: runCatching {
                        Db.withConnection { conn ->
                            conn.queryOne(
                                "SELECT i.store FROM installs i JOIN users u ON u.id = i.user_id WHERE u.phone = ? AND i.store IS NOT NULL ORDER BY i.last_day DESC LIMIT 1",
                                phone,
                            ) { rs -> rs.getString(1) }
                        }
                    }.getOrNull()
                    ?: "unknown"
                fun logSms(ok: Boolean) = runCatching {
                    Db.withConnection { conn -> conn.execute("INSERT INTO sms_log (day, store, ok) VALUES (?, ?, ?)", iranDay(), smsStore, if (ok) 1 else 0) }
                }
                try {
                    sendOtpSms(phone, code)
                    logSms(true)
                } catch (e: SmsSendException) {
                    logSms(false)
                    // ⚠️ شماره **ماسک‌شده** لاگ می‌شه (قبلاً کاملِ شماره چاپ می‌شد) و کدِ OTP
                    // هیچ‌وقت لاگ نمی‌شه - رجوع کن به قاعده‌ی بالای Log.kt.
                    Log.error("otp_sms_failed", e.message ?: "خطای ناشناخته", "phone" to maskPhone(phone))
                    call.respond(HttpStatusCode.BadGateway, mapOf("error" to "sms_send_failed"))
                    return@post
                }
            }

            Log.info("otp_sent", "کدِ تایید ارسال شد", "phone" to maskPhone(phone), "test" to testAccount)
            call.respond(mapOf("ok" to true))
        }

        post("/verify-otp") {
            if (!call.rateLimitOk("otp_verify", VERIFY_ATTEMPTS_PER_IP_PER_HOUR, HOUR_MS)) return@post
            val body = runCatching { call.receive<VerifyOtpBody>() }.getOrNull()
            val phone = (body?.phone ?: "").trim()
            val code = (body?.code ?: "").trim()
            if (!PHONE_RE.matches(phone) || code.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_input"))
                return@post
            }

            val otp = Db.withConnection { conn ->
                conn.queryOne("SELECT * FROM otps WHERE phone = ? ORDER BY id DESC LIMIT 1", phone) { rs ->
                    OtpRow(rs.getLong("id"), rs.getString("code_hash"), rs.getLong("expires_at"), rs.getInt("attempts"))
                }
            }
            if (otp == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "no_otp_requested"))
                return@post
            }
            if (otp.attempts >= MAX_VERIFY_ATTEMPTS) {
                call.respond(HttpStatusCode.TooManyRequests, mapOf("error" to "too_many_attempts"))
                return@post
            }
            if (System.currentTimeMillis() > otp.expiresAt) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "code_expired"))
                return@post
            }

            Db.withConnection { conn -> conn.execute("UPDATE otps SET attempts = attempts + 1 WHERE id = ?", otp.id) }
            // مقایسه‌ی ثابت‌زمان: `!=`ِ رشته‌ای به‌محضِ اولین کاراکترِ متفاوت برمی‌گردد و از
            // اختلافِ زمانش می‌شود کد را حرف‌به‌حرف حدس زد.
            if (!MessageDigest.isEqual(hashCode(code).toByteArray(), otp.codeHash.toByteArray())) {
                Log.warn("login_wrong_code", "کدِ اشتباه", "phone" to maskPhone(phone), "attempt" to (otp.attempts + 1))
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "wrong_code"))
                return@post
            }

            Db.withConnection { conn -> conn.execute("DELETE FROM otps WHERE id = ?", otp.id) } // یک‌بارمصرف
            Db.withConnection { conn ->
                conn.queryOne("SELECT id FROM users WHERE phone = ?", phone) { it.getLong(1) }
            }?.let { claimLaunchGift(it) }

            var user = Db.withConnection { conn ->
                conn.queryOne(
                    "SELECT id, phone, subscribed, subscribed_until, subscription_tier, created_at, trial_blocked, name, legacy_gift_granted FROM users WHERE phone = ?", phone
                ) { it.toUserRow() }
            }
            val isNewUser = user == null
            if (user == null) {
                val newId = Db.withConnection { conn -> conn.insertReturningId("INSERT INTO users (phone, trial_days, legacy_gift_granted) VALUES (?, ?, 1)", phone, configuredTrialDays(conn)) }
                Db.withConnection { conn -> conn.execute("INSERT INTO loans (user_id, data) VALUES (?, '[]')", newId) }
                // 🎁 ۱۰۰۰ سکه‌ی خوش‌آمد برای هر حسابِ تازه (خواسته‌ی کاربر ۱۳ مهر) - مثلِ هدیه‌ی گروهی، از «پیام‌ها» دریافت می‌شود.
                runCatching {
                    Db.withConnection { conn ->
                        conn.insertReturningId(
                            "INSERT INTO announcements (title, body, kind, target_user_id, coins) VALUES (?, ?, 'gift', ?, ?)",
                            WELCOME_COINS_TITLE, WELCOME_COINS_BODY, newId, WELCOME_COINS,
                        )
                    }
                }
                // بعد از insert دوباره از دیتابیس می‌خونیم (نه یه آبجکت دستیِ ناقص) تا created_at
                // واقعی (لازم برای محاسبه‌ی دوره‌ی آزمایشی ۷ روزه‌ی isSubscribed) رو داشته باشیم.
                user = Db.withConnection { conn ->
                    conn.queryOne(
                        "SELECT id, phone, subscribed, subscribed_until, subscription_tier, created_at, trial_blocked, name, legacy_gift_granted FROM users WHERE id = ?", newId
                    ) { it.toUserRow() }
                }!!
            }

            // 🔒 ماهِ مجانی یک بار برای هر گوشی (۷ مهر): حسابِ **تازه** روی گوشی‌ای که حسابِ
            // دیگری از قبل رویش بوده، ماهِ مجانی نمی‌گیرد. حسابِ قدیمی و خریدِ واقعی دست نمی‌خورند.
            val deviceHash = body?.deviceHash?.lowercase()?.takeIf { DEVICE_HASH_RE.matches(it) }
            if (deviceHash != null) {
                val uid = user!!.id
                val blocked = Db.withConnection { conn ->
                    val others = conn.queryOne(
                        "SELECT COUNT(*) FROM device_users WHERE device_hash = ? AND user_id != ?", deviceHash, uid,
                    ) { it.getInt(1) } ?: 0
                    conn.execute("INSERT OR IGNORE INTO device_users (device_hash, user_id) VALUES (?, ?)", deviceHash, uid)
                    if (isNewUser && others > 0) {
                        conn.execute("UPDATE users SET trial_blocked = 1 WHERE id = ?", uid)
                        true
                    } else {
                        false
                    }
                }
                if (blocked) {
                    user = user!!.copy(trialBlocked = true)
                    Log.info("trial_blocked", "حسابِ تازه روی گوشیِ تکراری - بی ماهِ مجانی", "uid" to uid)
                }
            }

            Log.info(
                "login_ok", "ورودِ موفق",
                "uid" to user.id, "phone" to maskPhone(user.phone), "new" to (user.subscribedUntil == null),
            )
            // نسخه‌ی نشستِ همین لحظه داخلِ توکن می‌نشیند؛ `requireAuth` هر بار با ستونِ
            // دیتابیس مقایسه‌اش می‌کند، پس ابطال فوری اثر می‌کند.
            val sessionVersion = Db.withConnection { conn ->
                conn.queryOne("SELECT session_version FROM users WHERE id = ?", user.id) { it.getLong("session_version") }
            } ?: 0L
            val token = signToken(user.id, user.phone, sessionVersion)
            call.respond(
                VerifyOtpResponse(
                    token = token,
                    phone = user.phone,
                    subscribed = isSubscribed(user),
                    subscribedUntil = user.subscribedUntil,
                    subscriptionTier = user.subscriptionTier,
                    trialDaysLeft = trialDaysLeftIfApplicable(user)
                )
            )
        }

        /* وضعیت فعلی حساب (از جمله اشتراک) — کلاینت بعد از باز شدن اپ این رو صدا می‌زنه تا اگه
           اشتراک از جای دیگه (مثلاً گوشی دیگه، یا بعداً از طریق خرید درون‌برنامه‌ای کافه‌بازار)
           فعال شده باشه، بدون نیاز به لاگین مجدد باخبر بشه. */
        get("/me") {
            val authed = call.requireAuth() ?: return@get
            claimLaunchGift(authed.uid)
            val user = Db.withConnection { conn ->
                conn.queryOne(
                    "SELECT id, phone, subscribed, subscribed_until, subscription_tier, created_at, trial_blocked, name, legacy_gift_granted FROM users WHERE id = ?", authed.uid
                ) { it.toUserRow() }
            }
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "user_not_found"))
                return@get
            }
            call.respond(
                MeResponse(
                    phone = user.phone,
                    userId = authed.uid,
                    userCode = runCatching { ir.sadteam.loancalc.server.UserCode.of(authed.uid, user.createdAt) }.getOrNull(),
                    subscribed = isSubscribed(user),
                    legacyGift = user.legacyGift,
                    name = user.name,
                    subscribedUntil = user.subscribedUntil,
                    subscriptionTier = user.subscriptionTier,
                    trialDaysLeft = trialDaysLeftIfApplicable(user)
                )
            )
        }

        /* حذف کامل حساب - الزامِ استانداردِ فروشگاه‌های اپ برای هر اپی که با شماره‌موبایل لاگین
           می‌گیره و داده‌ی کاربر رو سمت سرور نگه می‌داره (نه فقط یه لاگ‌اوت محلی). چون هیچ FK ای تو
           Db.kt با ON DELETE CASCADE تعریف نشده، هر جدولِ وابسته به user_id/phone رو دستی و قبل از
           خودِ ردیفِ users پاک می‌کنیم؛ اگه جدولِ جدیدی به user_id/phone وابسته اضافه شد، همینجا هم
           باید اضافه بشه. */
        /* نامِ اختیاریِ کاربر - همیشه قابلِ خالی‌کردنه (null/رشته‌ی خالی = پاک‌کردنِ اسم).
           عمداً هیچ اعتبارسنجیِ سخت‌گیرانه‌ای نداره چون این فیلد هیچ‌جا اجباری نیست. */
        put("/name") {
            val authed = call.requireAuth() ?: return@put
            val body = runCatching { call.receive<SetNameBody>() }.getOrNull()
            val name = body?.name?.trim()?.take(60)?.ifBlank { null }
            Db.withConnection { conn ->
                conn.execute("UPDATE users SET name = ? WHERE id = ?", name, authed.uid)
            }
            call.respond(mapOf("ok" to true))
        }

        delete("/account") {
            val authed = call.requireAuth() ?: return@delete
            Log.info("account_deleted", "حذفِ کاملِ حساب", "uid" to authed.uid, "phone" to maskPhone(authed.phone))
            // ⚠️ **یک تراکنش**: تا امروز پنج حذفِ مستقل بود و شکستِ وسطِ کار، ردیف‌های
            // پشتیبانِ بی‌صاحب به‌جا می‌گذاشت. حالا یا همه پاک می‌شوند یا هیچ‌کدام.
            Db.withConnection { conn ->
                conn.autoCommit = false
                try {
                    conn.execute("DELETE FROM loans WHERE user_id = ?", authed.uid)
                    conn.execute("DELETE FROM cheques_backup WHERE user_id = ?", authed.uid)
                    conn.execute("DELETE FROM accounts_backup WHERE user_id = ?", authed.uid)
                    conn.execute("DELETE FROM otps WHERE phone = ?", authed.phone)
                    // بازبینیِ پیش از انتشار (۹ مهر): پیام‌ها، پیوست‌ها و پیوندِ آمار هم پاک/جدا شوند.
                    conn.prepareStatement("SELECT id FROM support_files WHERE user_id = ?").use { ps ->
                        ps.setLong(1, authed.uid)
                        ps.executeQuery().use { rs -> while (rs.next()) ir.sadteam.loancalc.server.SupportFiles.fileFor(rs.getString(1))?.delete() }
                    }
                    conn.execute("DELETE FROM support_files WHERE user_id = ?", authed.uid)
                    conn.execute("DELETE FROM bug_reports WHERE user_id = ?", authed.uid)
                    conn.execute("DELETE FROM announcements WHERE target_user_id = ?", authed.uid)
                    conn.execute("UPDATE installs SET user_id = NULL WHERE user_id = ?", authed.uid)
                    conn.execute("UPDATE crash_reports SET user_id = NULL WHERE user_id = ?", authed.uid)
                    conn.execute("DELETE FROM users WHERE id = ?", authed.uid)
                    conn.commit()
                } catch (e: Exception) {
                    conn.rollback()
                    throw e
                } finally {
                    conn.autoCommit = true
                }
            }
            // عکس‌های ابری (رسید/چک) - بیرون از تراکنش، چون فایل است نه ردیف.
            runCatching {
                java.io.File(env("FILES_DIR", System.getProperty("user.home") + "/VameMan/files"), authed.uid.toString()).deleteRecursively()
            }
            call.respond(mapOf("ok" to true))
        }
    }
}


internal const val WELCOME_COINS = 1000
internal const val WELCOME_COINS_TITLE = "🎁 ۱۰۰۰ سکه هدیه"
internal const val WELCOME_COINS_BODY = "به جیبک خوش آمدی! ۱۰۰۰ سکه هدیه گرفتی - از فروشگاه تم، نماد و قاب بخر و برنامه را مالِ خودت کن."
