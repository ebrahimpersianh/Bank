# جیبک - راهنمای کار (خلاصه)
جزئیات/تاریخچه فقط وقتِ لازم: `CLAUDE-ARCHIVE.md` · `CLAUDE-HISTORY.md` · `HANDOVER.md` (چک‌لیستِ تست).

## کاربر
- **فنی نیست.** بعدِ هر تغییر، بی‌آنکه بپرسد، خلاصه‌ی ساده‌ی فارسی بده: چه شد، خطر دارد؟ کاری لازم است؟ بی اصطلاحِ فنی.
- ساعت‌ها به **وقتِ ایران** (UTC+3:30). هم‌زمان با دو چت روی ریپو کار نشود (لپ‌تاپ: پوشه‌ی `Jibak`).
- «ذخیره کن» = فقط ثبت کن، کد نزن. «همه رو انجام بده» = اجرا.
- فایلی که کاربر می‌فرستد را فقط بررسی/گزارش کن؛ بی تأیید اعمال نکن. پیام/هدیه به کاربران: اول متن را تأیید بگیر.
- **طراحی با ChatGPT است** («جی»). روش: یک اسکرین‌شاتِ واقعی + ۲-۳ خط («همین را بهتر کن؛ هدر/نوار/تب/سبز عوض نشود، چیزی اضافه/حذف نشود، مسکات نه، خروجی ZIP با PNGِ شفاف + SPEC.md»). کارِ من: جاگذاری.

## پروژه
اپِ اندرویدِ فارسیِ حسابداریِ شخصی «جیبک» (قبلاً «وام من») برای کافه‌بازار/مایکت.
- 🚫 `applicationId = ir.sadteam.loancalc` و کلیدِ امضای release **هرگز** عوض نشوند.
- `native-android/` (Kotlin+Compose؛ `:app` UI، `:data` Room+SQLCipher/DataStore/Retrofit، `:core` محاسباتِ خالص) · `server/` (Ktor+SQLite).
- فلیورها: `cafebazaar` (Poolakey) و `myket` (IAB؛ `ITEM_TYPE_INAPP`). محصولات `unlimited_loans_1m/3m/6m/1y`.
- مرزِ رایگان/پولی: `ui/subscription/Premium.kt`. کاربرِ تازه ۳۰ روز مجانی + ۱۰۰۰ سکه‌ی خوش‌آمد (سرور، `AuthRoutes`).

## بیلد و گیت
- اندروید فقط با `build-native-android.yml` (دستی، ~۱۵ دقیقه). `server/`: `./gradlew compileKotlin --offline`.
- ⛔ **سهمیه‌ی Actions محدود:** کامیت‌های میانی `[skip ci]`؛ آخرِ کار **یک** بیلدِ دستی.
- همه‌چیز روی `main` (+ هم‌گام با `claude/bank-9fiumm`). push روی `server/**` بدونِ skip = **دیپلویِ سرور**؛ دستی jar آپلود نکن.
- APK در Releases: `Jibak-2.0.<run>-<store>.apk`.
- قبل از بیلد: `native-android/tools/static-checks/*.py` + چکِ دستیِ ایمپورتِ جاافتاده.
- فایلِ UI بالای ~۷۰۰ خط نشود. جداکردن فقط با `tools/refactor/ktsplit.py`+`ktverify.py` (تابعِ غول: `ktextract.py`+`ktxverify.py`)؛ بعدش تداخلِ نام/نوعِ خصوصی. **تقسیمِ بیشتر نه** (تصمیمِ کاربر).
- برنچ‌های `ccr-07146a93-ufo9vg`، `main-cherry`، `server-price-*`: دست نزن.

## سرور (VPS آروان، زنده)
- `~/VameMan/` (pm2: `loan-calc-api`، `.env`، `data.sqlite`). دسترسی فقط از ورک‌فلوها (`appleboy/ssh-action` + سکرت‌های VPS).
- کارِ یک‌باره روی دیتابیس = ورک‌فلوی کوچکِ جدا؛ قبل از حذف/تغییرِ گروهی **پشتیبان** + تأییدِ کاربر.
- فقط‌خواندنی: `inspect-server.yml` · سلامت: `check-server-health.yml`.
- هر `SELECT ... FROM users` که `toUserRow()` می‌خورد همه‌ی ستون‌ها را داشته باشد.
- ادمین فقط داخلِ اپ (`users.is_admin=1`، `make-admin.yml`). آمار: `UsageStats.action(...)` + برچسب در `ACTION_LABELS`؛ هیچ مبلغ/متن/شماره‌ای نفرست.
- نسخه/آپدیتِ اجباری: `set-app-version.yml` (فعلاً ۷۲۱) و `set-min-version.yml` (فعلاً ۸۴۸؛ ۰ = خاموش) - فقط با شماره‌ی منتشرشده در **هر دو** استور. داخلِ اپ هم: ادمین ← «تنظیماتِ از راهِ دور».

## 🚨 دیتابیس
- **۷۲۱ اولین نسخه‌ی منتشرشده (DB ۳۵).** الان DB **۳۶**. به migrationهای ≤ ۳۶ دست نزن؛ بعدی = `MIGRATION_36_37` + تست در `MigrationTest.kt`. ⚠️ `fallbackToDestructiveMigration` روشن: migrationِ جاافتاده = پاک‌شدنِ داده.
- هر `CREATE INDEX` در migration باید در `indices` همان `@Entity` هم باشد.
- عددی که هم ستون دارد هم در JSON کپی شده → یک نقطه‌ی نوشتن (`withPaidCount`).
- `LoanRepository.getRows()` suspend است؛ در `remember{}` نه (state + `LaunchedEffect`). تراکنش در حلقه: `id` صریح.
- **دیتابیس ریال، نمایش تومان یا ریال** (`MoneyUnit`/`CoreMoneyUnit`). مبلغ با `unitDiv`/`unitFa()`؛ حروفِ زیرِ کادر همیشه تومان (`numberToWordsTomanFa`). `tomanToRial`/`rialToToman` فقط لبه‌ی UI. علامت فقط با `isoSigned`.
- `smsSender` حساب = «سرشماره,بسته‌نامِ اپ» (`smsSenderMatches`).

## Compose - قواعدی که نقضشان باگ داده
- توکن‌های رنگ `@Composable`اند؛ داخلِ `Canvas` قبلش در `val` بخوان.
- **`Text` همیشه از `ir.sadteam.loancalc.ui.text.Text`** (نه `material3.Text`) - واحدِ بعد از عدد را کوچک/کم‌رنگ می‌کشد و تک‌خطیِ جانشده را کوچک می‌کند.
- کارت همیشه `AppCard`. `Icon` بیرونِ AppCard همیشه `tint` صریح.
- فیلدِ مبلغ: state فقط رقمِ خام + `ThousandsSeparatorTransformation`؛ در onValueChange فرمت نکن. مبلغِ تازه → `maskIfPrivate`.
- `core.toFa(x)` در برابرِ `x.toFa()`ِ `ui.jibak` - جابه‌جا نکن.
- فیلدهای LTR (موبایل، کارت، PIN، شبا…) داخلِ `Ltr {}`.
- `LazyVerticalGrid` داخلِ لیستِ تنبل نه → `chunked(n)` + `Row`.
- زیرصفحه‌ی تنظیمات → `FullScreenDialog`.
- تاریخِ فارسی: `addDays/addMonths` با عددِ منفی مسیرِ جدا دارند؛ فاصله‌ی ۳۰روزه → `addMonths`.
- فرمولِ قرض‌الحسنه (`LoanMethod.QARZ`) را بی تستِ رگرسیون عوض نکن.
- طلایی فقط اشتراک/سکه. بلورِ واقعی نه. فونت Vazirmatn. لمس ≥ ۴۴dp. فلشِ «بعدی» سمتِ چپ.

## تصمیم‌های قفل (دوباره پیشنهاد نکن)
مسکات/شخصیت هیچ‌جا · «+» وسطِ نوار نه · لوگوی بانک‌ها نه (فقط حرفِ اول) · دراپ‌باکس نه · خرید از سایت نه · «خریدِ همیشگی» نه · ایمیلِ پشتیبانی نه (درون‌برنامه‌ای؛ یادآوری هم نکن) · نامِ Servix نیاید · تقویمِ مالی و اسپلش تأییدشده · کارتِ خرید به مشترک نشان داده نمی‌شود · حجمِ APK (~۳۷ مگ) دست نخورد · OTP همان ۵ رقمی.
- برگشت = یک صفحه عقب (`MainActivity.navigateTo/goBack`؛ تب‌ها ریشه‌اند). صفحه‌های تودرتو `BackHandler`.
- دنگ: «خودم» تیک ندارد؛ تیکِ همه = تسویه‌ی خودکار؛ سهمِ پرداخت‌نشده = طلب در «طلب و بدهی» (از `dang_participants`، بی ردیفِ کپی).
- `QuietHours`: یادآورها قبل از ساعتِ یادآور/بعد از ۲۳ نه؛ اعلانِ ادمین فقط ۱۰ تا ۲۲.
- پیامکِ شماره‌ی موبایلِ شخصی هرگز خوانده/نشان داده نمی‌شود (مایکت)؛ انتخابگر پیش‌فرض «فقط بانکی».
- **واحد همیشه کنارِ عدد، هم‌خط** (جا نشد متن کوچک شود). **«میلیون/میلیارد» نه** - عددِ کامل؛ فقط برچسبِ محورِ نمودار `toFaShort`/`rialToFaShort`. علامت‌دار: `isoSigned(...) + " " + unitFa()`.
- فرم‌های بلند: `CompactField` (۴۶dp).
- کارتِ سبزِ خانه = **چیدمانِ بیلدِ ۸۵۹** (قرص‌ها بالا؛ عدد + نمودار راست، درآمد/خرجِ امروز ستونِ چپ). ۲۱ مهر کاربر چیدمانِ ۸۶۰ (`home-hero.png`) و پیشنهادِ مرتب‌سازی را رد کرد - دوباره پیشنهاد نکن. پیامِ اختلافِ موجودی: آستانه ۱۰٬۰۰۰ ریال، شناسه‌ی ثابت (`gapMessageId`)، دکمه فقط روی جدیدترین. کانالِ تراکنشِ خودکار `jibak_auto_transactions_v2`. زنگوله = اقدام‌دار + خبرِ خوانده‌نشده. «پیام‌های جیبک» آبی، فقط تبِ «همه». سطلِ زباله فقط آیکون.
- گزارشِ روزِ ادمین: نیمه‌شب تا نیمه‌شبِ ایران. نمودارهای ادمین: امروز سمتِ راست + لمسی.
- کارتِ وام: جمعِ اقساطِ معوق + «قسطِ ۳، ۴ معوق».

## باز / یادآوری
- 📦 **۲۱ مهر:** آخرین بیلد **۸۶۰**، هنوز به استور نرفته. کاربر روی گوشی تست کند: واحدِ ریال (عددِ ریز؟)، صدای اعلانِ تراکنش (شیائومی)، تکانِ زنگوله، دکمه‌ی «ثبت به‌عنوانِ …»، کارتِ سبزِ خانه. بعد به هر دو استور؛ بعد از تأییدِ هر دو: `set-min-version.yml` و `set-app-version.yml` با همان شماره + یک خریدِ واقعی در هر استور.
  - متنِ «تغییراتِ این نسخه» (تأییدشده):
    ✨ تازه‌های جیبک · دنگِ کاملاً تازه (ماشین‌حساب، تیکِ پرداخت، تسویه‌ی خودکار، سهمِ دوستان در «طلب و بدهی») · انتخابِ تومان/ریال؛ واحد کنارِ عدد · مبلغ‌ها کامل و خواناتر (بدونِ «میلیون»، عددِ پررنگ، واحدِ کم‌رنگ) · یکی‌کردنِ موجودی با بانک با یک لمس · صفحه‌ی پیام‌ها مرتب‌تر (واریز/برداشت رنگی، پیام‌های جیبک جدا) · قبض‌ها با طرحِ تازه و شماره‌ی موبایل · افزودنِ وامِ جمع‌وجورتر · کارتِ وام با جمعِ اقساطِ معوق و شماره‌شان · «انتقال بین حساب‌های خودم» · تشخیصِ دقیق‌ترِ پیامکِ بانک (تبلیغاتی ثبت نمی‌شود) · بهبودِ جابه‌جایی و یادآورها · رفعِ ایرادها. (برای ۸۶۰ اضافه شود: «صدای اعلانِ تراکنش و زنگوله‌ی پیام‌ها» و «کارتِ خانه‌ی تازه».)
- ⏳ نسخه‌ی بعد: کم‌استفاده‌ها پشتِ «بیشتر…» (بعد از چند هفته آمار)؛ اینتروی تازه؛ ثبتِ چند تراکنش با هم؛ پیش‌نویسِ تراکنش؛ «مانده» از پیامک؛ «تازه‌های این نسخه» بعد از آپدیت.
- ⏳ طرح‌های ذخیره‌شده: `design/2026-10-16/NOTES.md` (سبک‌کردنِ کارتِ بالای خانه منتظرِ طرحِ جی). `ExactReminderAlarm` هنوز روی گوشیِ واقعی تست نشده.
- جزئیاتِ فنیِ قدیمی‌تر (حالتِ ساده، `SlowMainWatcher`، `PersistentTabs`، کلیدِ SSH و…): `CLAUDE-ARCHIVE.md` ← «جداشده از CLAUDE.md».

## graphify

This project has a code knowledge graph at graphify-out/ (Kotlin app + Kotlin/Ktor server, code only; docs/design are excluded via `.graphifyignore`).

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
