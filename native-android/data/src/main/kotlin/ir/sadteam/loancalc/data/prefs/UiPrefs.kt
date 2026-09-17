package ir.sadteam.loancalc.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.uiPrefsDataStore by preferencesDataStore(name = "ui_prefs")

/** پورت toggleTheme/اندازه فونت تو www/index.html (که تو localStorage ذخیره می‌شن). */
class UiPrefs(private val context: Context) {
    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        val LAST_AUTO_BACKUP_AT = stringPreferencesKey("last_auto_backup_at")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val PRIVACY_MODE_ENABLED = booleanPreferencesKey("privacy_mode_enabled")
        val REMINDER_DAY_OFFSETS = stringPreferencesKey("reminder_day_offsets")
        val REMINDER_SOUND_URI = stringPreferencesKey("reminder_sound_uri")
        val REMINDER_VIBRATE = booleanPreferencesKey("reminder_vibrate")
        val RATE_PROMPT_OPENS = intPreferencesKey("rate_prompt_opens")
        val RATE_PROMPT_DISMISSED = booleanPreferencesKey("rate_prompt_dismissed")
        val RATE_PROMPT_LAST_SHOWN_AT_OPENS = intPreferencesKey("rate_prompt_last_shown_at_opens")
        val SMS_AUTO_IMPORT_ENABLED = booleanPreferencesKey("sms_auto_import_enabled")
        val LAST_SMS_IMPORT_AT = stringPreferencesKey("last_sms_import_at")
        val NOTIF_AUTO_IMPORT_ENABLED = booleanPreferencesKey("notif_auto_import_enabled")
        val COME_BACK_REMINDER_ENABLED = booleanPreferencesKey("come_back_reminder_enabled")
        val LAST_COME_BACK_NOTIFIED_AT = stringPreferencesKey("last_come_back_notified_at")
        val NOTIF_AUTO_IMPORT_PACKAGES = stringPreferencesKey("notif_auto_import_packages")
        val IGNORED_SUBSCRIPTIONS = stringPreferencesKey("ignored_subscriptions")
        val SNOOZED_REMINDERS = stringPreferencesKey("snoozed_reminders")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val AUTO_TX_NOTIFY_ENABLED = booleanPreferencesKey("auto_tx_notify_enabled")
        val RECENT_AUTO_IMPORT_KEYS = stringPreferencesKey("recent_auto_import_keys")
        val DAILY_EXPENSE_REMINDER_ENABLED = booleanPreferencesKey("daily_expense_reminder_enabled")
        val AVATAR_SHAPE = stringPreferencesKey("avatar_shape")
        val AVATAR_COLOR = stringPreferencesKey("avatar_color")
        val AVATAR_PHOTO = stringPreferencesKey("avatar_photo")
        val SHORTCUT_ORDER = stringPreferencesKey("shortcut_order")
        val SHORTCUT_SELECTION = stringPreferencesKey("shortcut_selection")
        val DISMISSED_DISCOVERIES = stringPreferencesKey("dismissed_discoveries")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val BADGES_RETRO_DONE = booleanPreferencesKey("badges_retro_done")
        val COLOR_THEME = stringPreferencesKey("color_theme")
        val OWNED_THEMES = stringPreferencesKey("owned_themes")
        val OWNED_ITEMS = stringPreferencesKey("owned_items")
        val ACTIVE_ICON = stringPreferencesKey("active_icon")
        val ACTIVE_FRAME = stringPreferencesKey("active_frame")
        val ACTIVE_SYMBOL_SET = stringPreferencesKey("active_symbol_set")
        val ACTIVE_FONT = stringPreferencesKey("active_font")
        val ACTIVE_BACKDROP = stringPreferencesKey("active_backdrop")
        val NAV_SLOTS = stringPreferencesKey("nav_slots")
        val NAV_USAGE = stringPreferencesKey("nav_usage")
        val NAV_USAGE_STARTED_AT = longPreferencesKey("nav_usage_started_at")
        val NAV_SUGGEST_LAST_AT = longPreferencesKey("nav_suggest_last_at")
        val NAV_SUGGEST_DISMISSED = stringPreferencesKey("nav_suggest_dismissed")
    }

    // پیش‌فرض روشن/سفید (به‌درخواست کاربر «تم اصلی برنامه سفید باشه») - کاربری که قبلاً دستی
    // تم رو عوض کرده باشه، انتخابش تو DataStore می‌مونه؛ فقط نصب‌های تازه پیش‌فرض روشن می‌گیرن.
    // اگه کاربر قبل از اضافه‌شدنِ کلیدِ جدید فقط تاریک/روشنِ قدیمی (DARK_THEME بولین) رو ست کرده
    // بود، همون مقدار مهاجرت می‌شه؛ کلید جدید THEME_MODE (light/dark) اولویت داره.
    val themeMode: Flow<String> = context.uiPrefsDataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE] ?: if (prefs[Keys.DARK_THEME] == true) "dark" else "light"
    }

    suspend fun setThemeMode(value: String) {
        context.uiPrefsDataStore.edit { it[Keys.THEME_MODE] = value }
    }

    /**
     * **آدمکِ پروفایل** (بخشِ ۳۲ فایلِ طراحی). سه مقدارِ ساده‌ی متنی نگه داشته می‌شن نه یه شیِ
     * سریال‌شده، تا اضافه‌شدنِ رنگ/شکلِ تازه تو آینده مقدارِ ذخیره‌شده رو خراب نکنه.
     *
     * ⚠️ `null` بودنِ شکل/رنگ یعنی **کاربر هنوز انتخاب نکرده** - طرح صریحاً می‌گه پیش‌فرض
     * تصادفی نباشه، پس UI باید حالتِ خنثی رو نشون بده.
     */
    val avatarShape: Flow<String?> = context.uiPrefsDataStore.data.map { it[Keys.AVATAR_SHAPE] }
    val avatarColor: Flow<String?> = context.uiPrefsDataStore.data.map { it[Keys.AVATAR_COLOR] }
    val avatarPhoto: Flow<String?> = context.uiPrefsDataStore.data.map { it[Keys.AVATAR_PHOTO] }

    suspend fun setAvatar(shape: String, color: String) {
        context.uiPrefsDataStore.edit {
            it[Keys.AVATAR_SHAPE] = shape
            it[Keys.AVATAR_COLOR] = color
        }
    }

    suspend fun setAvatarPhoto(path: String?) {
        context.uiPrefsDataStore.edit {
            if (path == null) it.remove(Keys.AVATAR_PHOTO) else it[Keys.AVATAR_PHOTO] = path
        }
    }

    /**
     * ترتیبِ میان‌برهای **کشوی میان‌بُر** (بخشِ ۳۱ فایلِ طراحی) - شناسه‌ها با `,` جدا می‌شن.
     *
     * ⚠️ طرح این ترتیب رو رو **سرور** (`PATCH /me/shortcuts`) می‌خواد؛ سرورِ فعلی همچین مسیری
     * نداره، پس فعلاً محلیه. اضافه‌شدنِ مسیرِ سرور فقط یه لایه‌ی سینک روی همین می‌خواد.
     */
    val shortcutOrder: Flow<String?> = context.uiPrefsDataStore.data.map { it[Keys.SHORTCUT_ORDER] }

    suspend fun setShortcutOrder(ids: List<String>) {
        context.uiPrefsDataStore.edit { it[Keys.SHORTCUT_ORDER] = ids.joinToString(",") }
    }

    /**
     * **انتخابِ** میان‌برهای کشو (هشت از چهاردهِ مخزن) - عمداً از [shortcutOrder] جداست:
     * یکی «کدام‌ها» را می‌گوید و دیگری «به چه ترتیب». `null`/خالی یعنی کاربر هنوز انتخاب
     * نکرده، پس جای فراخوان باید هشت‌تای پیش‌فرض را بگذارد، نه کشوی خالی.
     */
    val shortcutSelection: Flow<String?> = context.uiPrefsDataStore.data.map { it[Keys.SHORTCUT_SELECTION] }

    suspend fun setShortcutSelection(ids: List<String>) {
        context.uiPrefsDataStore.edit { it[Keys.SHORTCUT_SELECTION] = ids.joinToString(",") }
    }

    /**
     * کارت‌های کشفِ نادیده‌گرفته‌شده‌ی تبِ گزارش، با `,` جدا. کلیدِ هر مورد `(نوع، ماهِ شمسی)`ست،
     * پس نادیده‌گرفتن **ماهانه** است نه دائمی - کشفی که برای همیشه خاموش می‌شود یعنی خبری که
     * هیچ‌وقت به کاربر نمی‌رسد.
     */
    val dismissedDiscoveries: Flow<String?> = context.uiPrefsDataStore.data.map { it[Keys.DISMISSED_DISCOVERIES] }

    suspend fun setDismissedDiscoveries(keys: List<String>) {
        context.uiPrefsDataStore.edit { it[Keys.DISMISSED_DISCOVERIES] = keys.joinToString(",") }
    }

    /* --------------------------------------------------------------------------------------
     * شخصی‌سازیِ **نوارِ پایین** - بخشِ ۴۱ فایلِ طراحی.
     *
     * ⚠️ این‌ها عمداً از [shortcutOrder] **جدان**: طرح صریح می‌گه نوار و کشو «یک فهرست، دو
     * نمایش»ن و تغییرِ یکی نباید اون یکی رو عوض کنه. مخزنِ مقصدها مشترکه، ترتیبِ ذخیره‌شده نه.
     *
     * همه‌چیز **محلیه و به سرور نمی‌ره** (قاعده‌ی صریحِ `41c` درباره‌ی شمارشِ استفاده).
     * -------------------------------------------------------------------------------------- */

    /** پنج جایگاهِ نوار، با `,` جدا. `null` یعنی هنوز دست نخورده → نوارِ پیش‌فرض. */
    val navSlots: Flow<String?> = context.uiPrefsDataStore.data.map { it[Keys.NAV_SLOTS] }

    suspend fun setNavSlots(ids: List<String>) {
        context.uiPrefsDataStore.edit { it[Keys.NAV_SLOTS] = ids.joinToString(",") }
    }

    /** برگشت به نوارِ پیش‌فرض - کلید کاملاً پاک می‌شه، نه اینکه مقدارِ پیش‌فرض نوشته بشه. */
    suspend fun clearNavSlots() {
        context.uiPrefsDataStore.edit { it.remove(Keys.NAV_SLOTS) }
    }

    /**
     * شمارشِ بازشدنِ صفحه‌ها: `destId:weekKey=count` که با `;` جدا شدن.
     *
     * ⚠️ رشته‌ی تخت (نه جدولِ Room) عمدیه: حجمش ناچیزه (۷ مقصد × ۳ هفته)، هیچ‌وقت کوئریِ
     * پیچیده نمی‌خواد، و **یه مهاجرتِ دیتابیسِ دیگه** برای یه شمارنده‌ی موقت ارزشش رو نداره.
     */
    val navUsage: Flow<String?> = context.uiPrefsDataStore.data.map { it[Keys.NAV_USAGE] }

    /** لحظه‌ی شروعِ شمارش - شرطِ «≥۲۱ روز داده»ی `41c` از رو همین حساب می‌شه. */
    val navUsageStartedAt: Flow<Long> =
        context.uiPrefsDataStore.data.map { it[Keys.NAV_USAGE_STARTED_AT] ?: 0L }

    /**
     * یه بازشدنِ صفحه رو ثبت می‌کنه و هم‌زمان هفته‌های خارج از پنجره رو دور می‌ندازه.
     *
     * پاک‌سازی همین‌جا انجام می‌شه (نه یه کارِ زمان‌بندی‌شده‌ی جدا) تا رشته هیچ‌وقت رشد نکنه.
     */
    suspend fun recordNavOpen(destId: String, weekKey: Int, keepWeeks: Set<Int>, nowMillis: Long) {
        context.uiPrefsDataStore.edit { prefs ->
            if ((prefs[Keys.NAV_USAGE_STARTED_AT] ?: 0L) == 0L) {
                prefs[Keys.NAV_USAGE_STARTED_AT] = nowMillis
            }
            val counts = parseNavUsage(prefs[Keys.NAV_USAGE])
                .filterKeys { it.second in keepWeeks }
                .toMutableMap()
            val key = destId to weekKey
            counts[key] = (counts[key] ?: 0) + 1
            prefs[Keys.NAV_USAGE] = counts.entries
                .joinToString(";") { (k, v) -> "${k.first}:${k.second}=$v" }
        }
    }

    /** زمانِ آخرین پیشنهادِ نشون‌داده‌شده - برای فاصله‌ی ۶۰ روزه. */
    val navSuggestLastAt: Flow<Long> =
        context.uiPrefsDataStore.data.map { it[Keys.NAV_SUGGEST_LAST_AT] ?: 0L }

    suspend fun setNavSuggestLastAt(millis: Long) {
        context.uiPrefsDataStore.edit { it[Keys.NAV_SUGGEST_LAST_AT] = millis }
    }

    /** جفت‌هایی که کاربر «نه» گفته (`promote>demote`)، با `,` جدا. */
    val navSuggestDismissed: Flow<String?> =
        context.uiPrefsDataStore.data.map { it[Keys.NAV_SUGGEST_DISMISSED] }

    suspend fun addNavSuggestDismissed(pairKey: String) {
        context.uiPrefsDataStore.edit { prefs ->
            val existing = prefs[Keys.NAV_SUGGEST_DISMISSED]
                ?.split(',')?.filter { it.isNotBlank() }.orEmpty()
            prefs[Keys.NAV_SUGGEST_DISMISSED] = (existing + pairKey).distinct().joinToString(",")
        }
    }

    companion object {
        /** ساعتِ پیش‌فرضِ یادآور - صبح، وقتی کاربر هنوز فرصتِ کاری کردن دارد. */
        const val DEFAULT_REMINDER_HOUR = 9

        /** پنجره‌ی تشخیصِ ثبتِ تکراریِ خودکار - شش ساعت. */
        const val IMPORT_DEDUPE_WINDOW_MS = 6L * 60 * 60 * 1000

        /** `destId:weekKey=count;…` → نگاشت. ردیفِ خراب بی‌صدا نادیده گرفته می‌شه. */
        fun parseNavUsage(raw: String?): Map<Pair<String, Int>, Int> =
            raw?.split(';').orEmpty().mapNotNull { entry ->
                val (left, count) = entry.split('=').takeIf { it.size == 2 } ?: return@mapNotNull null
                val (dest, week) = left.split(':').takeIf { it.size == 2 } ?: return@mapNotNull null
                val w = week.toIntOrNull() ?: return@mapNotNull null
                val c = count.toIntOrNull() ?: return@mapNotNull null
                (dest to w) to c
            }.toMap()
    }

    /** پورت .app.fs-small/fs-medium/fs-large (zoom:0.9/1/1.15) - پیش‌فرض «متوسط» (۱). */
    val fontScale: Flow<Float> = context.uiPrefsDataStore.data.map { it[Keys.FONT_SCALE] ?: 1f }

    /**
     * «انیمیشنِ کم» - برای گوشی‌های کم‌قدرت. قاعده‌ی طراح: **هرچه فقط تزئینه می‌ره، هرچه
     * بازخوردِ لمسه می‌مونه** (جابه‌جاییِ دستگیره‌ی کلید، تغییرِ رنگ و نوارِ پیشرفت می‌مونن).
     */
    /** شناسه‌ی تمِ رنگیِ فعال - رجوع کن به `ColorTheme`. */
    val colorTheme: Flow<String?> = context.uiPrefsDataStore.data.map { it[Keys.COLOR_THEME] }

    suspend fun setColorTheme(id: String) {
        context.uiPrefsDataStore.edit { it[Keys.COLOR_THEME] = id }
    }

    /**
     * تم‌هایی که کاربر **خریده**. خرید یک‌باره‌ست و انتخاب بی‌نهایت - یعنی بعد از خرید
     * هر وقت بخواد بینشون سوئیچ می‌کنه.
     */
    val ownedThemes: Flow<Set<String>> = context.uiPrefsDataStore.data.map { prefs ->
        prefs[Keys.OWNED_THEMES]?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
    }

    suspend fun addOwnedTheme(id: String) {
        context.uiPrefsDataStore.edit { prefs ->
            val current = prefs[Keys.OWNED_THEMES]?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
            prefs[Keys.OWNED_THEMES] = (current + id).joinToString(",")
        }
    }

    /**
     * **مالکیتِ قلم‌های فروشگاه** - کلید *شناسه‌ی گونه* است نه نامِ `CoinSpend`:
     * `theme:lapis`، `icon:piggy`، `coinskin:ancient`.
     *
     * 🚨 چرا: `CoinSpend.THEME_PALETTE` **یک ردیفِ قیمت** است، پس اگر مالکیت با نامِ enum
     * کلید بخورد، خریدِ «لاجورد» پنج تمِ دیگر را هم باز می‌کند (تشخیصِ طراح، بخشِ ۵۹).
     *
     * تم‌های خریده‌شده‌ی قدیمی که با شناسه‌ی خام (`blue`) ذخیره شده بودند همین‌جا خوانده و
     * به `theme:blue` ترجمه می‌شوند - کسی مالکیتش را از دست نمی‌دهد.
     */
    val ownedItems: Flow<Set<String>> = context.uiPrefsDataStore.data.map { prefs ->
        val items = prefs[Keys.OWNED_ITEMS]?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
        val legacy = prefs[Keys.OWNED_THEMES]?.split(",")?.filter { it.isNotBlank() }
            ?.map { "theme:$it" }?.toSet() ?: emptySet()
        items + legacy
    }

    /**
     * آیکونِ لانچرِ فعال (`icon:piggy`…). `null` یعنی «کیفِ پول»ِ پیش‌فرض - همان که در
     * کاتالوگ نیست چون فروشی نیست.
     *
     * ⚠️ فقط آیکونِ پیش‌فرض پله‌های پژمردگی دارد؛ آیکونِ خریداری‌شده همیشه پله‌ی صفر است.
     */
    val activeIcon: Flow<String?> = context.uiPrefsDataStore.data.map { it[Keys.ACTIVE_ICON] }

    suspend fun setActiveIcon(key: String?) {
        context.uiPrefsDataStore.edit { prefs ->
            if (key == null) prefs.remove(Keys.ACTIVE_ICON) else prefs[Keys.ACTIVE_ICON] = key
        }
    }

    /**
     * قابِ آواتارِ فعال (`frame:gold`…). `null` یعنی بی‌قاب - همان حالتی که تا امروز بود.
     *
     * ⚠️ قاب **جای آواتار را نمی‌گیرد**، دورش می‌نشیند: آواتار تنها جای شخصیِ برنامه است
     * (بندِ ۲ی `72a`) و قاب باید همان‌جا دیده شود که کاربر خریدش را انجام داده - هدرِ خانه.
     */
    val activeFrame: Flow<String?> = context.uiPrefsDataStore.data.map { it[Keys.ACTIVE_FRAME] }

    suspend fun setActiveFrame(key: String?) {
        context.uiPrefsDataStore.edit { prefs ->
            if (key == null) prefs.remove(Keys.ACTIVE_FRAME) else prefs[Keys.ACTIVE_FRAME] = key
        }
    }

    /**
     * ستِ نمادِ دسته‌بندیِ فعال (`symbolset:outlined`…). `null` یعنی ستِ توپرِ پیش‌فرض.
     *
     * تنها قلمی که **هر روز** دیده می‌شود (بندِ ۱ی `72a`): در فرمِ ثبت، در دونات، و روی هر
     * ردیفِ تراکنش.
     */
    val activeSymbolSet: Flow<String?> = context.uiPrefsDataStore.data.map { it[Keys.ACTIVE_SYMBOL_SET] }

    suspend fun setActiveSymbolSet(key: String?) {
        context.uiPrefsDataStore.edit { prefs ->
            if (key == null) prefs.remove(Keys.ACTIVE_SYMBOL_SET) else prefs[Keys.ACTIVE_SYMBOL_SET] = key
        }
    }

    /** قلمِ متنِ فعال (دسته‌ی «قلم»ِ فروشگاه). `null` = وزیرمتنِ پیش‌فرض. */
    val activeFont: Flow<String?> = context.uiPrefsDataStore.data.map { it[Keys.ACTIVE_FONT] }

    /** پس‌زمینه‌ی زنده‌ی فعال - `null` یعنی پس‌زمینه‌ی سادهٔ برنامه. */
    val activeBackdrop: Flow<String?> = context.uiPrefsDataStore.data.map { it[Keys.ACTIVE_BACKDROP] }

    suspend fun setActiveBackdrop(key: String?) {
        context.uiPrefsDataStore.edit { prefs ->
            if (key == null) prefs.remove(Keys.ACTIVE_BACKDROP) else prefs[Keys.ACTIVE_BACKDROP] = key
        }
    }

    suspend fun setActiveFont(key: String?) {
        context.uiPrefsDataStore.edit { prefs ->
            if (key == null) prefs.remove(Keys.ACTIVE_FONT) else prefs[Keys.ACTIVE_FONT] = key
        }
    }

    suspend fun addOwnedItem(key: String) {
        context.uiPrefsDataStore.edit { prefs ->
            val current = prefs[Keys.OWNED_ITEMS]?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
            prefs[Keys.OWNED_ITEMS] = (current + key).joinToString(",")
        }
    }

    /**
     * اولین سنجشِ نشان‌ها (بازشدنِ **گذشته**) انجام شده؟ - فقط یک‌بار در عمرِ نصب.
     * تا وقتی `false`ه، نشان‌های گذشته صامت باز می‌شن و یه شیتِ جمع‌بندی نشون داده می‌شه.
     */
    val badgesRetroDone: Flow<Boolean> = context.uiPrefsDataStore.data.map { it[Keys.BADGES_RETRO_DONE] ?: false }

    suspend fun setBadgesRetroDone() {
        context.uiPrefsDataStore.edit { it[Keys.BADGES_RETRO_DONE] = true }
    }

    val reducedMotion: Flow<Boolean> = context.uiPrefsDataStore.data.map { it[Keys.REDUCED_MOTION] ?: false }

    suspend fun setReducedMotion(value: Boolean) {
        context.uiPrefsDataStore.edit { it[Keys.REDUCED_MOTION] = value }
    }

    suspend fun setFontScale(value: Float) {
        context.uiPrefsDataStore.edit { it[Keys.FONT_SCALE] = value }
    }

    /** پورت یادآوری سررسید (وب هنوز نداره) - پیش‌فرض خاموش چون روشن‌کردنش رو Android 13+ نیاز به
     * مجوز POST_NOTIFICATIONS داره که فقط با تعامل کاربر (سوییچ تو تنظیمات) درخواست می‌شه. */
    val notificationsEnabled: Flow<Boolean> = context.uiPrefsDataStore.data.map { it[Keys.NOTIFICATIONS_ENABLED] ?: false }

    suspend fun setNotificationsEnabled(value: Boolean) {
        context.uiPrefsDataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = value }
    }

    /** پورت «پشتیبان‌گیری خودکار روزانه» اپ رقیب - رجوع کن به AutoBackupWorker/AutoBackupScheduler. */
    val autoBackupEnabled: Flow<Boolean> = context.uiPrefsDataStore.data.map { it[Keys.AUTO_BACKUP_ENABLED] ?: false }

    suspend fun setAutoBackupEnabled(value: Boolean) {
        context.uiPrefsDataStore.edit { it[Keys.AUTO_BACKUP_ENABLED] = value }
    }

    val lastAutoBackupAt: Flow<String?> = context.uiPrefsDataStore.data.map { it[Keys.LAST_AUTO_BACKUP_AT] }

    suspend fun setLastAutoBackupAt(value: String) {
        context.uiPrefsDataStore.edit { it[Keys.LAST_AUTO_BACKUP_AT] = value }
    }

    /** ویبره‌ی واقعی (نه فقط هپتیک ظریف Compose) رو تپ‌های اصلی - ویژگی اشتراکی؛ پیش‌فرض روشنه
     * (چون خودِ روشن/خاموش‌کردنش تو تنظیمات پشت گیت اشتراک قرار داره، رجوع کن به HapticsViewModel). */
    val vibrationEnabled: Flow<Boolean> = context.uiPrefsDataStore.data.map { it[Keys.VIBRATION_ENABLED] ?: true }

    suspend fun setVibrationEnabled(value: Boolean) {
        context.uiPrefsDataStore.edit { it[Keys.VIBRATION_ENABLED] = value }
    }

    /** حالت خصوصی: مخفی‌کردن همه‌ی مبلغ‌های روی صفحه پشت «•••» - برای وقتی گوشی دستِ کسیه. */
    val privacyModeEnabled: Flow<Boolean> = context.uiPrefsDataStore.data.map { it[Keys.PRIVACY_MODE_ENABLED] ?: false }

    suspend fun setPrivacyModeEnabled(value: Boolean) {
        context.uiPrefsDataStore.edit { it[Keys.PRIVACY_MODE_ENABLED] = value }
    }

    /** پیش‌فرضِ سراسریِ زمان‌بندیِ یادآوری (CSV از تعداد روزهای قبل از سررسید، مثلاً "1,3,7") - هر
     * وام/چک که تنظیمِ اختصاصی نداره (`reminderDayOffsets == null`) از همین استفاده می‌کنه.
     * پیش‌فرضِ نصبِ تازه «۱» (یه روز قبل)، جایگزینِ رفتارِ قدیمیِ ثابتِ «امروز/فردا». */
    val reminderDayOffsets: Flow<String> = context.uiPrefsDataStore.data.map { it[Keys.REMINDER_DAY_OFFSETS] ?: "1" }

    suspend fun setReminderDayOffsets(value: String) {
        context.uiPrefsDataStore.edit { it[Keys.REMINDER_DAY_OFFSETS] = value }
    }

    /** URI صدای اعلانِ یادآوری (از RingtoneManager انتخاب می‌شه) - null یعنی صدای پیش‌فرضِ سیستم. */
    val reminderSoundUri: Flow<String?> = context.uiPrefsDataStore.data.map { it[Keys.REMINDER_SOUND_URI] }

    suspend fun setReminderSoundUri(value: String?) {
        context.uiPrefsDataStore.edit {
            if (value == null) it.remove(Keys.REMINDER_SOUND_URI) else it[Keys.REMINDER_SOUND_URI] = value
        }
    }

    /** ویبره‌ی مخصوصِ نوتیفِ یادآوری (مستقل از [vibrationEnabled] که برای هپتیکِ لمسیِ خودِ اپه). */
    val reminderVibrate: Flow<Boolean> = context.uiPrefsDataStore.data.map { it[Keys.REMINDER_VIBRATE] ?: true }

    suspend fun setReminderVibrate(value: Boolean) {
        context.uiPrefsDataStore.edit { it[Keys.REMINDER_VIBRATE] = value }
    }

    /** یادآوریِ دوره‌ایِ امتیازدهی تو استور (مورد ۲۵) - [rateDialogOpens] یه‌بار به‌ازای هر بازشدنِ
     * موفقِ اپ زیاد می‌شه؛ [rateDialogDismissedForever] با «نه ممنون» یا «بله امتیاز می‌دم» true
     * می‌شه (دیگه هیچ‌وقت دوباره نشون داده نمی‌شه)؛ [rateDialogLastShownAtOpens] با «بعداً» یا اولین
     * نمایش آپدیت می‌شه تا فاصله‌ی نمایشِ بعدی از روش حساب بشه - رجوع کن به RatePrompt.kt. */
    val rateDialogOpens: Flow<Int> = context.uiPrefsDataStore.data.map { it[Keys.RATE_PROMPT_OPENS] ?: 0 }

    suspend fun incrementRateDialogOpens(): Int {
        var result = 0
        context.uiPrefsDataStore.edit {
            result = (it[Keys.RATE_PROMPT_OPENS] ?: 0) + 1
            it[Keys.RATE_PROMPT_OPENS] = result
        }
        return result
    }

    val rateDialogDismissedForever: Flow<Boolean> =
        context.uiPrefsDataStore.data.map { it[Keys.RATE_PROMPT_DISMISSED] ?: false }

    suspend fun setRateDialogDismissedForever(value: Boolean) {
        context.uiPrefsDataStore.edit { it[Keys.RATE_PROMPT_DISMISSED] = value }
    }

    val rateDialogLastShownAtOpens: Flow<Int?> =
        context.uiPrefsDataStore.data.map { it[Keys.RATE_PROMPT_LAST_SHOWN_AT_OPENS] }

    suspend fun setRateDialogLastShownAtOpens(value: Int) {
        context.uiPrefsDataStore.edit { it[Keys.RATE_PROMPT_LAST_SHOWN_AT_OPENS] = value }
    }

    /** خوندنِ خودکارِ پیامکِ بانکی (رجوع کن به BankSmsReceiver/BankSmsParser تو core) - پیش‌فرض
     * خاموش (مجوزِ حساسیه، فقط با سوییچِ صریحِ کاربر تو تنظیمات روشن و مجوزِ Runtime درخواست می‌شه). */
    val smsAutoImportEnabled: Flow<Boolean> = context.uiPrefsDataStore.data.map { it[Keys.SMS_AUTO_IMPORT_ENABLED] ?: false }

    suspend fun setSmsAutoImportEnabled(value: Boolean) {
        context.uiPrefsDataStore.edit { it[Keys.SMS_AUTO_IMPORT_ENABLED] = value }
    }

    val lastSmsImportAt: Flow<String?> = context.uiPrefsDataStore.data.map { it[Keys.LAST_SMS_IMPORT_AT] }

    suspend fun setLastSmsImportAt(value: String) {
        context.uiPrefsDataStore.edit { it[Keys.LAST_SMS_IMPORT_AT] = value }
    }

    /** خوندنِ خودکارِ **نوتیفیکیشنِ** بانکی - برای بانک‌های دیجیتال (بلوبانک و…) که اصلاً پیامک
     * نمی‌فرستن و فقط اعلانِ درون‌اپی می‌دن، پس مسیرِ پیامکیِ بالا براشون بی‌فایده‌ست.
     * پیش‌فرض خاموش؛ علاوه بر این سوییچ، کاربر باید دسترسیِ «خواندن اعلان‌ها» رو هم دستی از
     * تنظیماتِ خودِ گوشی بده (مجوزِ Runtime نداره) - رجوع کن به BankNotificationListener. */
    val notifAutoImportEnabled: Flow<Boolean> =
        context.uiPrefsDataStore.data.map { it[Keys.NOTIF_AUTO_IMPORT_ENABLED] ?: false }

    suspend fun setNotifAutoImportEnabled(value: Boolean) {
        context.uiPrefsDataStore.edit { it[Keys.NOTIF_AUTO_IMPORT_ENABLED] = value }
    }

    /** اعلانِ «X روزه تراکنش ثبت نکردی» - رجوع کن به ComeBackWorker. پیش‌فرض روشنه چون کاربر
     * صریحاً خواستش؛ از تنظیمات قابلِ خاموش‌کردنه. */
    val comeBackReminderEnabled: Flow<Boolean> =
        context.uiPrefsDataStore.data.map { it[Keys.COME_BACK_REMINDER_ENABLED] ?: true }

    suspend fun setComeBackReminderEnabled(value: Boolean) {
        context.uiPrefsDataStore.edit { it[Keys.COME_BACK_REMINDER_ENABLED] = value }
    }

    /** کلیدِ روزِ آخرین اعلانِ برگشت (`y-m-d`) - هم برای «روزی یک‌بار» و هم برای اینکه یادآورِ
     * روزانه همون روز ساکت بمونه (وگرنه کاربرِ غایب دو تا اعلان می‌گرفت). */
    val lastComeBackNotifiedAt: Flow<String?> =
        context.uiPrefsDataStore.data.map { it[Keys.LAST_COME_BACK_NOTIFIED_AT] }

    suspend fun setLastComeBackNotifiedAt(value: String) {
        context.uiPrefsDataStore.edit { it[Keys.LAST_COME_BACK_NOTIFIED_AT] = value }
    }

    /** بسته‌نامِ اپ‌هایی که کاربر **خودش** انتخاب کرده اعلانشون خونده بشه (با `,` جدا شده).
     * عمداً لیستِ سفیده نه «همه‌ی اپ‌ها»: هم حریمِ خصوصی، هم جوابِ روشن برای بازبینِ استور. */
    val notifAutoImportPackages: Flow<Set<String>> = context.uiPrefsDataStore.data.map { prefs ->
        prefs[Keys.NOTIF_AUTO_IMPORT_PACKAGES]
            ?.split(',')
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.toSet()
            ?: emptySet()
    }

    suspend fun setNotifAutoImportPackages(value: Set<String>) {
        context.uiPrefsDataStore.edit { it[Keys.NOTIF_AUTO_IMPORT_PACKAGES] = value.joinToString(",") }
    }

    /** برچسبِ اشتراک‌هایی که کاربر تو «اشتراک‌یاب» گفته نادیده گرفته بشن (خرجِ تکراری‌ای که
     * خودش می‌دونه چیه و نمی‌خواد هر ماه یادآوری بشه). با `,` جدا می‌شن - رجوع کن به
     * [ir.sadteam.loancalc.core.RecurringDetector]. */
    val ignoredSubscriptions: Flow<Set<String>> = context.uiPrefsDataStore.data.map { prefs ->
        prefs[Keys.IGNORED_SUBSCRIPTIONS]
            ?.split(',')
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.toSet()
            ?: emptySet()
    }

    suspend fun setIgnoredSubscriptions(value: Set<String>) {
        context.uiPrefsDataStore.edit { it[Keys.IGNORED_SUBSCRIPTIONS] = value.joinToString(",") }
    }

    /**
     * موردهایی که کاربر از داخلِ اعلان «فردا یادم بیاور» زده - هر عضو `کلید@تاریخِ تعویق`.
     *
     * تاریخ همراهِ کلید ذخیره می‌شود تا اجرای فردا بفهمد تعویق **مالِ دیروز** بوده و دوباره
     * بفرستد؛ بدونِ تاریخ، تعویقِ یک‌روزه بی‌صدا دائمی می‌شد و کاربر دیگر هیچ‌وقت یادآورِ آن قسط
     * را نمی‌گرفت.
     */
    val snoozedReminders: Flow<Set<String>> = context.uiPrefsDataStore.data.map { prefs ->
        prefs[Keys.SNOOZED_REMINDERS]
            ?.split(',')
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.toSet()
            ?: emptySet()
    }

    /** کلیدِ مورد را با تاریخِ **امروز** ثبت می‌کند؛ ورودی به شکلِ `y-m-d` است. */
    suspend fun setSnoozedUntilTomorrow(key: String, todayKey: String) {
        context.uiPrefsDataStore.edit { prefs ->
            val current = prefs[Keys.SNOOZED_REMINDERS].orEmpty()
                .split(',').map { it.trim() }.filter { it.isNotEmpty() }
                // تعویقِ قبلیِ همین مورد جایگزین می‌شود، نه اینکه روی هم انباشته شود.
                .filterNot { it.substringBefore('@') == key }
                // تعویقِ روزهای گذشته هیچ‌وقت خوانده نمی‌شود و فقط فهرست را باد می‌کند.
                .filter { it.substringAfter('@', "") == todayKey }
            prefs[Keys.SNOOZED_REMINDERS] = (current + "$key@$todayKey").joinToString(",")
        }
    }

    /** ساعتِ روزانه‌ی یادآور (۰ تا ۲۳). پیش‌فرض ۹ صبح. */
    val reminderHour: Flow<Int> = context.uiPrefsDataStore.data.map { prefs ->
        (prefs[Keys.REMINDER_HOUR] ?: DEFAULT_REMINDER_HOUR).coerceIn(0, 23)
    }

    suspend fun setReminderHour(hour: Int) {
        context.uiPrefsDataStore.edit { it[Keys.REMINDER_HOUR] = hour.coerceIn(0, 23) }
    }

    /**
     * کلیدِ تراکنش‌هایی که همین اواخر خودکار وارد شده‌اند، برای جلوگیری از **ثبتِ تکراری**.
     *
     * 🚨 چرا لازم شد: اپ‌های بانکی یک اعلان را دوباره منتشر یا به‌روزرسانی می‌کنند و
     * `onNotificationPosted` هر بار اجرا می‌شود. هیچ کنترلی نبود، پس یک واریز می‌توانست دو
     * تراکنشِ منتظرِ تایید بسازد و با تاییدِ هر دو، موجودی دو برابر جابه‌جا می‌شد.
     *
     * هر مقدار `<کلید>@<زمانِ میلی‌ثانیه‌ای>` است و پنجره‌ی [IMPORT_DEDUPE_WINDOW_MS] ساعتی
     * نگه داشته می‌شود - نه بیشتر، چون خریدِ واقعاً تکراری با همان مبلغ در روزِ بعد باید ثبت شود.
     */
    val recentAutoImportKeys: Flow<List<String>> = context.uiPrefsDataStore.data.map { prefs ->
        prefs[Keys.RECENT_AUTO_IMPORT_KEYS].orEmpty().split(',').filter { it.isNotBlank() }
    }

    /** `true` یعنی این تراکنش تازه است و ثبت شد؛ `false` یعنی تکراری بود و باید نادیده گرفته شود. */
    suspend fun claimAutoImportKey(key: String): Boolean {
        val now = System.currentTimeMillis()
        var fresh = true
        context.uiPrefsDataStore.edit { prefs ->
            val kept = prefs[Keys.RECENT_AUTO_IMPORT_KEYS].orEmpty()
                .split(',')
                .filter { it.isNotBlank() }
                .filter { now - (it.substringAfterLast('@').toLongOrNull() ?: 0L) < IMPORT_DEDUPE_WINDOW_MS }
            fresh = kept.none { it.substringBeforeLast('@') == key }
            if (fresh) prefs[Keys.RECENT_AUTO_IMPORT_KEYS] = (kept + "$key@$now").takeLast(60).joinToString(",")
            else prefs[Keys.RECENT_AUTO_IMPORT_KEYS] = kept.joinToString(",")
        }
        return fresh
    }

    /** اعلانِ «تراکنشِ خودکار ثبت شد» (کانالِ [ReminderChannels.CHANNEL_AUTO_TX], فریمِ `50b`).
     * پیش‌فرض **روشن**: کاربری که خواندنِ پیامک/اعلانِ بانک را خودش روشن کرده، منتظرِ خبر است -
     * و تراکنش تا تاییدش روی موجودی اثر ندارد، پس بی‌خبری یعنی کارِ نیمه‌تمامِ نادیده. */
    val autoTxNotifyEnabled: Flow<Boolean> =
        context.uiPrefsDataStore.data.map { it[Keys.AUTO_TX_NOTIFY_ENABLED] ?: true }

    suspend fun setAutoTxNotifyEnabled(value: Boolean) {
        context.uiPrefsDataStore.edit { it[Keys.AUTO_TX_NOTIFY_ENABLED] = value }
    }

    /** یادآوریِ روزانه‌ی «دخل‌وخرج امروز یادت نره» (رجوع کن به DueDateReminderWorker) - پیش‌فرض خاموش
     * مثلِ بقیه‌ی یادآوری‌های اپ، فقط با سوییچِ صریحِ کاربر تو تنظیمات روشن می‌شه. */
    val dailyExpenseReminderEnabled: Flow<Boolean> =
        context.uiPrefsDataStore.data.map { it[Keys.DAILY_EXPENSE_REMINDER_ENABLED] ?: false }

    suspend fun setDailyExpenseReminderEnabled(value: Boolean) {
        context.uiPrefsDataStore.edit { it[Keys.DAILY_EXPENSE_REMINDER_ENABLED] = value }
    }
}
