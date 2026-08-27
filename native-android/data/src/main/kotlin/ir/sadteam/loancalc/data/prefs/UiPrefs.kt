package ir.sadteam.loancalc.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
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
        val DAILY_EXPENSE_REMINDER_ENABLED = booleanPreferencesKey("daily_expense_reminder_enabled")
        val AVATAR_SHAPE = stringPreferencesKey("avatar_shape")
        val AVATAR_COLOR = stringPreferencesKey("avatar_color")
        val AVATAR_PHOTO = stringPreferencesKey("avatar_photo")
        val SHORTCUT_ORDER = stringPreferencesKey("shortcut_order")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
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

    /** پورت .app.fs-small/fs-medium/fs-large (zoom:0.9/1/1.15) - پیش‌فرض «متوسط» (۱). */
    val fontScale: Flow<Float> = context.uiPrefsDataStore.data.map { it[Keys.FONT_SCALE] ?: 1f }

    /**
     * «انیمیشنِ کم» - برای گوشی‌های کم‌قدرت. قاعده‌ی طراح: **هرچه فقط تزئینه می‌ره، هرچه
     * بازخوردِ لمسه می‌مونه** (جابه‌جاییِ دستگیره‌ی کلید، تغییرِ رنگ و نوارِ پیشرفت می‌مونن).
     */
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

    /** یادآوریِ روزانه‌ی «دخل‌وخرج امروز یادت نره» (رجوع کن به DueDateReminderWorker) - پیش‌فرض خاموش
     * مثلِ بقیه‌ی یادآوری‌های اپ، فقط با سوییچِ صریحِ کاربر تو تنظیمات روشن می‌شه. */
    val dailyExpenseReminderEnabled: Flow<Boolean> =
        context.uiPrefsDataStore.data.map { it[Keys.DAILY_EXPENSE_REMINDER_ENABLED] ?: false }

    suspend fun setDailyExpenseReminderEnabled(value: Boolean) {
        context.uiPrefsDataStore.edit { it[Keys.DAILY_EXPENSE_REMINDER_ENABLED] = value }
    }
}
