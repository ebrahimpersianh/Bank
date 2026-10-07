package ir.sadteam.loancalc.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import ir.sadteam.loancalc.core.toFa

internal fun percent(part: Int, whole: Int): Int = if (whole <= 0) 0 else Math.round(part * 100f / whole)
internal fun faDecimal(v: Double): String = toFa(v.toString().removeSuffix(".0")).replace('.', '٫')
/** `2026-09-29` → «۷ مهر». */
internal fun String.faDigitsAscii(): String = runCatching {
    val (y, m, d) = split('-').map { it.toInt() }
    val p = ir.sadteam.loancalc.core.JalaliCalendar.fromGregorian(y, m, d)
    "${toFa(p.d)} ${ir.sadteam.loancalc.ui.components.persianMonthName(p.m)}"
}.getOrDefault(toFa(this))
internal fun screenLabel(key: String): String = SCREEN_LABELS[key] ?: key
internal fun actionLabel(key: String): String = ACTION_LABELS[key]
    ?: when {
        key.startsWith("purchase_start_") -> "شروعِ خریدِ اشتراکِ ${key.removePrefix("purchase_start_")}"
        key.startsWith("purchase_done_") -> "خریدِ موفقِ اشتراکِ ${key.removePrefix("purchase_done_")}"
        key.startsWith("purchase_failed_") -> "خریدِ ناموفقِ اشتراکِ ${key.removePrefix("purchase_failed_")}"
        key.startsWith("purchase_cancel_") -> "انصراف از خریدِ اشتراکِ ${key.removePrefix("purchase_cancel_")}"
        key == "paywall_view" -> "دیدنِ صفحه‌ی اشتراک"
        key.startsWith("paywall_gate_") -> "قفلِ اشتراک: ${PAYWALL_GATE_LABELS[key.removePrefix("paywall_gate_")] ?: key.removePrefix("paywall_gate_")}"
        key.startsWith("guide_step_") -> "راهنمای شروع: قدمِ ${toFa((key.removePrefix("guide_step_").toIntOrNull() ?: 0) + 1)}"
        key.startsWith("notif_shown_") -> "اعلانِ فرستاده‌شده: ${key.removePrefix("notif_shown_")}"
        key.startsWith("notif_open_") -> "باز کردنِ اعلان: ${key.removePrefix("notif_open_")}"
        key.startsWith("notif_button_") -> "دکمه‌ی اعلان: ${NOTIF_BUTTON_LABELS[key.removePrefix("notif_button_")] ?: key}"
        key.startsWith("onboarding_step_") -> "معرفی: مرحله‌ی ${toFa((key.removePrefix("onboarding_step_").toIntOrNull() ?: 0) + 1)}"
        key.startsWith("shortcut_") -> "میان‌برِ آیکون: ${key.removePrefix("shortcut_")}"
        key == "widget_open" -> "باز کردن از ویجت"
        key == "purchase_verify_failed" -> "پول رفت ولی تأیید نشد (پیگیری کن!)"
        else -> key
    }
internal fun sdkLabel(sdk: String): String = when (sdk.toIntOrNull()) {
    null -> sdk
    in 35..99 -> "۱۵+"
    34 -> "۱۴"
    33 -> "۱۳"
    32, 31 -> "۱۲"
    30 -> "۱۱"
    29 -> "۱۰"
    28 -> "۹"
    27, 26 -> "۸"
    else -> "۷ و قدیمی‌تر"
}
internal val STORE_LABELS = mapOf("cafebazaar" to "کافه‌بازار", "myket" to "مایکت")
internal val FUNNEL_LABELS = mapOf(
    "installed" to "نصب و باز کرد",
    "onboarding_completed" to "مراحلِ شروع را تمام کرد",
    "account_created" to "حساب ساخت",
    "transaction_created" to "تراکنش ثبت کرد",
    "report_viewed" to "گزارش را دید",
)
/** کلیدها همان مسیرهای ناوبری‌اند، با «-» به «_» (رجوع کن به `UsageStats.clean`). */
internal val SCREEN_LABELS = linkedMapOf(
    "home" to "خانه",
    "assets" to "دارایی",
    "report" to "گزارش",
    "budget" to "بودجه",
    "due" to "سررسید",
    "loan" to "وام",
    "cheque" to "چک",
    "loan_stats" to "آمارِ وام‌ها",
    "cheque_report" to "گزارشِ چک",
    "debt" to "طلب و بدهی",
    "tools" to "ابزارها",
    "notes" to "یادداشت‌ها",
    "bug_report" to "گزارشِ مشکل",
    "savings_goal" to "هدفِ پس‌انداز",
    "categories" to "دسته‌بندی‌ها",
    "accounts" to "حساب‌ها",
    "shop" to "فروشگاه",
    "inbox" to "پیام‌ها",
    "calc_history" to "تاریخچه‌ی محاسبه",
    "sayad_inquiry" to "استعلامِ صیادی",
    "annual_archive" to "بایگانیِ سالانه",
    "financial_calendar" to "تقویمِ مالی",
    "settings_account" to "تنظیمات · حسابِ کاربری",
    "settings_appearance" to "تنظیمات · ظاهر",
    "settings_reminders" to "تنظیمات · یادآورها",
    "settings_data" to "تنظیمات · داده و پشتیبان",
    "settings_sms" to "تنظیمات · پیامکِ بانکی",
    "settings_background" to "تنظیمات · پس‌زمینه",
    "settings_tools" to "تنظیمات · ابزارها",
    "settings_security" to "تنظیمات · امنیت",
    "settings_color_theme" to "تنظیمات · تمِ رنگی",
    "settings_badges" to "تنظیمات · نشان‌ها",
    "settings_parsing_rules" to "تنظیمات · قاعده‌های پیامک",
    "settings_about" to "تنظیمات · درباره",
)
/** کلیدِ `PremiumPaywall.ask` → نامِ فارسیِ بخشِ قفل‌شده. */
private val PAYWALL_GATE_LABELS = mapOf(
    "budget" to "بودجه",
    "debts" to "طلب و بدهی",
    "receipt_photo" to "عکسِ رسید",
    "report_period" to "گزارشِ فصل و سال",
    "sms_auto" to "خواندنِ خودکارِ پیامک",
    "split" to "تقسیمِ خرید",
    "tags" to "برچسب",
    "tx_month" to "سقفِ تراکنشِ ماهانه",
)
internal val ACTION_LABELS = linkedMapOf(
    "transaction_added" to "ثبتِ تراکنش",
    "anr" to "هنگ‌کردنِ برنامه (ANR)",
    "ab_paywall_a" to "آزمایشِ اشتراک - گروهِ A",
    "ab_paywall_b" to "آزمایشِ اشتراک - گروهِ B",
    "login" to "ورود به حساب",
    "onboarding_first_account" to "اولین حساب در شروع",
    "loan_added_manual" to "ثبتِ وامِ دستی",
    "loan_saved_from_calc" to "ذخیره‌ی وام از محاسبه‌گر",
    "installment_paid" to "پرداختِ قسط",
    "installment_paid_late" to "پرداختِ قسط با تأخیر",
    "installments_paid_bulk" to "پرداختِ گروهیِ قسط",
    "installments_paid_bulk_late" to "پرداختِ گروهیِ قسط با تأخیر",
    "installment_receipt" to "عکسِ رسیدِ قسط",
    "installment_note" to "یادداشتِ قسط",
    "installment_amount_edited" to "ویرایشِ مبلغِ قسط",
    "home_mark_paid" to "پرداخت از کارتِ خانه",
    "loan_photo" to "عکسِ وام",
    "calendar_export" to "افزودن به تقویمِ گوشی",
    "income_added" to "ثبتِ درآمد (توانِ بازپرداخت)",
    "cheque_added" to "ثبتِ چک",
    "cheque_status_changed" to "تغییرِ وضعیتِ چک",
    "cheque_photo" to "عکسِ چک",
    "transfer_added" to "انتقال بینِ حساب‌ها",
    "budget_set" to "تعیینِ بودجه",
    "recurring_added" to "پرداختِ تکراری",
    "template_saved" to "ذخیره‌ی الگوی تراکنش",
    "bill_saved" to "ثبتِ قبض",
    "bill_paid" to "پرداختِ قبض",
    "category_added" to "دسته‌ی دلخواه",
    "categories_reordered" to "مرتب‌کردنِ دسته‌ها",
    "goal_added" to "هدفِ پس‌انداز",
    "asset_trade" to "خرید/فروشِ دارایی",
    "counterparty_added" to "طرفِ حسابِ تازه",
    "debt_added" to "ثبتِ طلب/بدهی",
    "debt_settled" to "تسویه‌ی طلب/بدهی",
    "dang_created" to "دنگ",
    "note_added" to "یادداشت",
    "sms_auto_tx" to "تراکنشِ خودکار از پیامک",
    "notif_auto_tx" to "تراکنشِ خودکار از اعلان",
    "sms_rule_saved" to "قاعده‌ی پیامک",
    "notif_import_toggled" to "روشن/خاموشِ خواندنِ اعلان",
    "auto_tx_toggled" to "روشن/خاموشِ اعلانِ تراکنش",
    "backup_exported" to "گرفتنِ پشتیبان",
    "restore_local" to "بازیابی از گوشی",
    "restore_cloud" to "بازیابی از سرور",
    "export_loans_pdf" to "خروجیِ PDFِ وام",
    "export_loans_excel" to "خروجیِ اکسلِ وام",
    "export_accounting_pdf" to "خروجیِ PDFِ حساب‌ها",
    "export_accounting_excel" to "خروجیِ اکسلِ حساب‌ها",
    "export_cheques_pdf" to "خروجیِ PDFِ چک",
    "export_cheques_excel" to "خروجیِ اکسلِ چک",
    "pin_set" to "تنظیمِ قفل",
    "biometric_toggled" to "اثرِ انگشت",
    "theme_mode_changed" to "تغییرِ تمِ روشن/تیره",
    "font_scale_changed" to "اندازه‌ی متن",
    "shop_buy" to "خرید از فروشگاهِ سکه",
    "coin_goal_set" to "هدفِ سکه",
)
internal fun formatDuration(seconds: Int): String = when {
    seconds < 60 -> "${toFa(seconds)} ثانیه"
    seconds < 3600 -> "${toFa(seconds / 60)} دقیقه"
    else -> "${toFa(seconds / 3600)} ساعت و ${toFa((seconds % 3600) / 60)} دقیقه"
}
internal fun profileValue(v: String): String = when (v) {
    "true" -> "بله"
    "false" -> "خیر"
    "com.farsitel.bazaar" -> "کافه‌بازار"
    "ir.mservices.market" -> "مایکت"
    "com.android.vending" -> "گوگل‌پلی"
    "unknown" -> "نامعلوم (فایلِ مستقیم)"
    "none" -> "ندارد"
    "dark" -> "تیره"
    "light" -> "روشن"
    "system" -> "مطابقِ گوشی"
    "default" -> "پیش‌فرض"
    "?" -> "نامشخص"
    else -> v.faDigitsAscii()
}
private val WEEKDAY_LABELS = mapOf(7 to "شنبه", 1 to "یکشنبه", 2 to "دوشنبه", 3 to "سه‌شنبه", 4 to "چهارشنبه", 5 to "پنجشنبه", 6 to "جمعه")
internal val PROFILE_LABELS = mapOf(
    "installer" to "منبعِ نصب",
    "subscription" to "اشتراک",
    "device_brand" to "برندِ گوشی",
    "device_model" to "مدلِ گوشی",
    "android" to "نسخه‌ی اندروید",
    "screen_dp" to "اندازه‌ی صفحه",
    "lang" to "زبانِ گوشی",
    "system_dark" to "گوشی در حالتِ تیره",
    "theme_mode" to "تمِ برنامه",
    "color_theme" to "رنگِ تم",
    "font_scale_app" to "اندازه‌ی متنِ برنامه",
    "font_scale_sys" to "اندازه‌ی متنِ گوشی",
    "lock" to "قفلِ برنامه",
    "biometric" to "اثرِ انگشت",
    "privacy_mode" to "پنهان‌کردنِ مبلغ‌ها",
    "perm_notifications" to "اجازه‌ی اعلان",
    "perm_sms" to "اجازه‌ی پیامک",
    "perm_calendar" to "اجازه‌ی تقویم",
    "notif_listener" to "خواندنِ اعلانِ بانک",
    "battery_unrestricted" to "باتریِ بدونِ محدودیت",
    "sms_import" to "ثبتِ خودکار از پیامک",
    "notif_import" to "ثبتِ خودکار از اعلان",
    "reminders" to "یادآوری‌ها",
    "reminder_hour" to "ساعتِ یادآوری",
    "daily_reminder" to "یادآورِ روزانه",
    "auto_backup" to "پشتیبانِ خودکار",
    "vibration" to "لرزش",
    "reduced_motion" to "انیمیشنِ کم",
    "owned_themes" to "تعدادِ تمِ خریده‌شده",
    "owned_items" to "تعدادِ آیتمِ فروشگاه",
)
internal val ADOPTION_LABELS = mapOf(
    "loans" to "وام",
    "cheques" to "چک",
    "cheque_books" to "دسته‌چک",
    "accounts" to "حساب",
    "account_transactions" to "تراکنش",
    "tx_auto" to "تراکنشِ خودکار (پیامک/اعلان)",
    "budgets" to "بودجه",
    "assets" to "دارایی",
    "asset_trades" to "خرید و فروشِ دارایی",
    "debts" to "طلب و بدهی",
    "counterparties" to "طرف‌حساب",
    "notes" to "یادداشت",
    "incomes" to "درآمد",
    "recurring_payments" to "پرداختِ تکراری",
    "savings_goals" to "هدفِ پس‌انداز",
    "tx_templates" to "الگوی تراکنش",
    "bills" to "قبض",
    "parsing_rules" to "قانونِ پیامک",
    "custom_categories" to "دسته‌ی دلخواه",
    "dang_events" to "دنگ",
    "inbox_messages" to "پیامِ مرکزِ پیام‌ها",
    "calculation_history" to "محاسبه‌ی ذخیره‌شده",
    "achievements" to "نشان",
)
// «?» = اشتراکِ بی پلن: ۳۰ روزِ رایگانِ کاربرِ تازه یا روزهای هدیه (۱۶ مهر).
internal val PLAN_LABELS = mapOf("1m" to "یک‌ماهه", "3m" to "سه‌ماهه", "6m" to "شش‌ماهه", "1y" to "یک‌ساله", "?" to "رایگان/هدیه")
internal val NOTIF_BUTTON_LABELS = mapOf("mark_paid" to "«پرداخت شد»", "snooze" to "«فردا یادم بنداز»", "confirm_tx" to "«تأیید»", "reject_tx" to "«رد»")
internal val ERROR_LABELS = mapOf(
    "accounts_import" to "برنگشتنِ حساب‌ها از سرور",
    "sync_conflict" to "تداخلِ دو گوشی",
    "sync_push_http" to "نرسیدنِ پشتیبان به سرور",
    "otp_rate_limited" to "کدِ ورود: درخواستِ زیاد",
    "otp_sms_send_failed" to "کدِ ورود: پیامک نرفت",
    "otp_invalid_phone" to "کدِ ورود: شماره‌ی اشتباه",
    "otp_unknown" to "کدِ ورود: خطای نامشخص",
    "otp_network" to "کدِ ورود: اینترنت وصل نبود",
    "otp_too_soon" to "کدِ ورود: زودتر از یک دقیقه دوباره خواست",
)
