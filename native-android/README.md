# وام من — نسخه‌ی Kotlin بومی (در حال ساخت)

این پوشه شروع بازنویسی کامل اپ به Kotlin/Jetpack Compose هست (به‌جای Capacitor/WebView فعلی تو `www/`).
پروژه‌ی وب فعلی دست‌نخورده و در کنارش باقی می‌مونه.

## وضعیت فعلی

- ✅ **موتور محاسبه‌ی وام** (`core/`) — پورت کامل و تست‌شده‌ی `computeLoan` (وام عادی + قرض‌الحسنه)
  و توابع تاریخ شمسی/فرمت اعداد فارسی از `www/index.html`. تست‌ها (`core/src/test/`) مقادیرشون
  مستقیم از اجرای نسخه‌ی JS واقعی گرفته شدن تا تطابق دقیق تضمین بشه.
- ✅ **تب «وام بانکی»** (`app/src/main/kotlin/.../ui/BankLoanScreen.kt` + `ResultScreen.kt`) —
  فرم ورودی کامل (پرتکرارها، بانک‌ها/خدمات اعتباری، تاریخ، مبلغ، نرخ، تعداد اقساط، فاصله، تنفس)
  و صفحه‌ی نتیجه (نمودار حلقه‌ای، اعداد، جدول کامل اقساط).
- ✅ **معماری چندماژوله** — ماژول جدید `data/` (Room برای «وام‌های من»، DataStore برای
  auth/subscribed/guest_mode، Retrofit مطابق دقیق `server/`'s API contract)، Hilt برای DI
  (`di/AppModule.kt`)، Navigation-Compose به‌جای سوییچ دستی تب‌ها تو `MainActivity`.
- ✅ **تب «وام‌های من» (`ui/myloans/`)** — زنجیره‌ی کامل Compose → Hilt ViewModel → Repository →
  Room سیم‌کشی شده. فرم افزودن وام دستی (`AddManualLoanScreen`، پورت دقیق اعتبارسنجی
  `saveManualLoan`، با فرم انتخاب تاریخ شروع)، حذف وام، و بازکردن جزئیات یه وام (`LoanDetailScreen`)
  با وضعیت پرداخت **مستقل هر قسط** (`rows[].paid`، تپ‌کردن رو هر ردیف toggle می‌کنه - نه یه آستانه‌ی
  ترتیبی)، تاریخ سررسید واقعی (از startDate/intervalDays محاسبه می‌شه، پورت دقیق حلقه‌ی
  `renderTable`)، ویرایش دستی مبلغ هر قسط (پورت `confirmEditInstallment`، با سوال «همین مبلغ رو
  بقیه هم بگیرن؟» بعد از ذخیره - `setAllRowsInstallment`)، و تاخیر پرداخت (`paidLate`/`paidDate`،
  پورت `openPayModal`/`payOnTime`/`confirmLatePayment`: تپ رو قسطِ پرداخت‌نشده یه انتخاب
  «به‌موقع»/«با تاخیر» نشون می‌ده، حالت دوم یه انتخابگر تاریخ واقعی پرداخت هم داره).
- ✅ **تب‌های محاسبه‌گر و سود سپرده** (`AffordScreen`/`DepositScreen`) — پورت مستقیم
  `calculateAffordability`/`calculateDeposit`، با فرمول‌هاشون تو `core/` (`AffordabilityCalculator`،
  `DepositCalculator`) تست‌شده.
- ✅ **رفع باگ ارقام فارسی/عربی** — `cleanNum`/`cleanNumDecimal` (پورت از وب) به `core/` اضافه
  شد و تو همه‌ی ورودی‌های عددی وصل شده (قبلش با کیبورد فارسی مقدار صفر parse می‌شد).
- ✅ **گیت ورود اجباری + حالت مهمان** (`ui/auth/`) — `AuthViewModel`/`LoginScreen` پورت
  checkLoginGateOnStart/sendPhoneOtp/confirmPhoneOtp/continueAsGuest هستن؛ اولین بار که اپ باز
  می‌شه (و هنوز نه لاگین نه مهمونه) `LoginScreen` اجباری نشون داده می‌شه، بعد از ورود موفق یا
  انتخاب مهمان دیگه هیچ‌وقت دوباره نشون داده نمی‌شه (دقیقاً مثل وب). session تو DataStore
  (`AuthPrefs`) ذخیره می‌شه.
- ✅ **محدودیت «۱ وام رایگان» + خرید واقعی اشتراک کافه‌بازار** (`canSaveAnotherLoan` تو
  `MyLoansScreen`، `subscription/`) — بعد از اولین وام، دکمه‌ی افزودن برای مهمون‌ها `LoginScreen`
  غیراجباری باز می‌کنه، برای کاربرهای واردشده‌ی بدون اشتراک `SubscriptionScreen` (۴ پلن پلکانی).
  خرید با **SDK بومی رسمی Poolakey** انجام می‌شه (`com.github.cafebazaar.Poolakey:poolakey` از
  JitPack - همون کتابخونه‌ای که پلاگین Capacitor نسخه‌ی وب هم زیرش داره، ولی اینجا مستقیم، نه از
  پشت یه پل جاوااسکریپت). `SubscriptionManager` (تو `subscription/`) عمداً Hilt-managed نیست چون
  به خودِ Activity نیاز داره (`activityResultRegistry`) - تو `MainActivity.onCreate` ساخته و
  connect می‌شه، `onDestroy` disconnect؛ بقیه‌ی صفحه‌ها از یه `CompositionLocal`
  (`LocalSubscriptionManager`) بهش دسترسی دارن. کلید RSA و شناسه‌ی محصولات دقیقاً همونایی هستن که
  برای اپ وب (همون applicationId مشترک `ir.sadteam.loancalc`) قبلاً تو پنل کافه‌بازار ثبت شدن.
  بعد از خرید موفق، سرور (همون `POST /api/subscription/verify` که برای وب هم استفاده می‌شه)
  مستقل تایید می‌کنه - به کلاینت اعتماد نمی‌شه.
- ✅ **صفحه‌ی تنظیمات + خروج از حساب** (`ui/settings/`) — دسترسی از یه آیکون چرخ‌دنده تو نوار
  بالای اپ (`LoanCalcApp`'s `TopAppBar`، معادل `.app-header` تو وب)؛ کارت حساب (وضعیت ورود/مهمان،
  خروج) و اندازه فونت (کوچک/متوسط/بزرگ) پیاده شدن - یادآوری سررسید و بقیه‌ی بخش‌های تنظیمات وب
  هنوز جای دیگه‌ای تو اپ بومی وجود ندارن که معنی داشته باشه پیاده بشن.
- ✅ **سینک ابری واقعی** — `LoanRepository.pushToServer` (پورت `syncLoansToServer`) بعد از هر
  تغییر محلی، اگه لاگین باشیم، بی‌صدا PUT می‌کنه؛ `syncAfterLogin` هم فقط یه‌بار بلافاصله بعد از
  ورود موفق: اگه فقط سرور داده داشت جایگزین می‌شه، اگه هر دو داده‌ی متفاوت داشتن `LoginScreen` از
  کاربر می‌پرسه (پورت `openConfirmModal`)، وگرنه محلی به سرور پوش می‌شه. تشخیص «فرق دارن» بر
  اساس (id, paidCount) هر وامه، نه مقایسه‌ی بایت‌به‌بایت JSON مثل وب.
- ✅ **تم روشن** (`ui/theme/`) — پورت `toggleTheme`/`body.light`: `AppColorPalette` (تیره/روشن،
  دقیقاً هم‌رنگ `:root`/`body.light` تو وب) از یه `CompositionLocal` (`LocalAppColors`) پخش می‌شه؛
  `AppBg`/`AppSurface`/... که همه‌ی صفحه‌ها همین‌جوری import می‌کردن، حالا `@Composable get()` هستن
  نه val ثابت - یعنی هیچ صفحه‌ای برای پشتیبانی از تم روشن نیازی به تغییر نداشت. انتخاب کاربر تو
  DataStore (`UiPrefs`) ذخیره می‌شه؛ دکمه‌ی تغییر تم گوشه‌ی نوار بالای اپه (معادل `.theme-btn`).
- ✅ **اندازه فونت** (پورت `.app.fs-small/fs-medium/fs-large` تو وب) — یه `LocalDensity` override
  رو ریشه‌ی Compose (`MainActivity`) هم dp هم sp رو با هم مقیاس می‌کنه، دقیقاً معادل زوم CSS رو کل
  کانتینر `.app`؛ مقدار (۰.۹/۱/۱.۱۵) تو `UiPrefs.fontScale` ذخیره می‌شه.
- ✅ **یادآوری سررسید** (`notifications/`) — کاملاً native-only (وب اصلاً این قابلیت رو نداره تا
  ازش پورت بشه). `ReminderScheduler` یه `PeriodicWorkRequest` روزانه (هر ۲۴ ساعت، `WorkManager`
  - نه `AlarmManager` خام - چون WorkManager خودش زمان‌بندی رو حتی بعد از ری‌استارت گوشی حفظ می‌کنه،
  بدون نیاز به `BroadcastReceiver` دستی برای `BOOT_COMPLETED`) با `ExistingPeriodicWorkPolicy.KEEP`
  ثبت می‌کنه. `DueDateReminderWorker` (`@HiltWorker`، تزریق `LoanRepository`) هر بار همه‌ی وام‌ها رو
  چک می‌کنه و برای هر قسط پرداخت‌نشده‌ای که سررسیدش امروز یا فرداست یه نوتیف مجزا می‌ده. سوییچ فعال/
  غیرفعال تو تنظیمات (`SettingsScreen`) رو Android 13+ مجوز `POST_NOTIFICATIONS` رو runtime درخواست
  می‌کنه (`ActivityResultContracts.RequestPermission`)، پایین‌تر از اون نیازی به مجوز نیست.
  «امروز» واقعی از یه تبدیل جلالی↔میلادی دقیق نجومی (`core/JalaliCalendar.kt`، الگوریتم
  jalaali-js/Borkowski با جدول واقعی سال‌های کبیسه) به‌دست میاد - نه تقویم ساده‌شده‌ی
  `PersianCalendar` بالا که فقط برای فاصله‌ی روزهای اقساط از رو یه startDate دلخواهه، نه گرفتن
  «امروز». الگوریتم با ده‌ها هزار تاریخ در برابر کتابخونه‌ی پایتون jdatetime تایید شده
  (`JalaliCalendarTest.kt`).
- ✅ **گزارش خطا (کرش ریپورت)** (`crash/CrashReporter.kt`) — `ApiService.reportCrash` از قبل تعریف
  شده بود ولی هیچ‌جا صدا زده نمی‌شد؛ حالا واقعاً وصله. پورت `reportCrash`/`window.onerror`/
  `unhandledrejection` تو www/index.html: یه `Thread.setDefaultUncaughtExceptionHandler` سراسری
  (معادل native هر دو قلاب وب، چون exception بدون catchِ کوروتین‌های ساختاریافته هم نهایتاً به
  همین handler می‌رسه) حداکثر ۵ گزارش در هر بار باز شدن اپ به `POST /api/crash` می‌فرسته (با
  `runBlocking`+تایم‌اوت کوتاه، نه fire-and-forget، چون فرصت پردازش قبل از کشته‌شدن اپ محدوده)، بدون
  سرویس ثالث (نه Sentry نه Crashlytics) - دقیقاً مثل وب.
- ✅ **درباره‌برنامه + حریم خصوصی** تو `SettingsScreen` (پورت `toggleAbout`/`togglePrivacy`، متن
  عینِ وب) - یه `AccordionCard` مشترک، تپ رو عنوان باز/بسته می‌کنه. «تنظیمات پیشرفته یادآوری» (صدای
  اعلان، سفارشی‌سازی به‌ازای هر وام) و فرم «نظرات و مشکلات» عمداً پورت نشدن - رو خودِ وب هم صرفاً
  UI نمایشیه (اولی طبق کامنت خودِ وب هیچ‌وقت واقعاً وصل نشده، دومی فقط تو localStorage نوشته می‌شه و
  هیچ‌جا خونده نمی‌شه) - یادآوری سررسید بالا genuinely کار می‌کنه و جایگزین بهتریه.
- 🚫 **قفل اثر انگشت عمداً پورت نمی‌شه** - طبق CLAUDE.md، این قابلیت قبلاً از خودِ وب هم به تصمیم
  صریح کاربر کامل حذف شد چون ورود/مهمان‌بودن الان به‌جاش مدیریت می‌شه؛ همون منطق اینجا هم صدق
  می‌کنه. اگه دوباره خواسته شد باید صریحاً درخواست بشه، نه فرض پیش‌فرض این بازنویسی.

## محدودیت مهم محیط توسعه

این پروژه تو سندباکس فعلی (که `dl.google.com` توش مسدوده) قابل build نیست چون Android Gradle
Plugin و همه‌ی کتابخونه‌های androidx/Compose فقط رو Google's Maven میزبانی می‌شن. برای همین:
- کد به‌صورت دستی و دقیق review شده ولی توسط کامپایلر واقعی تأیید نشده
- یه workflow جدا (`.github/workflows/build-native-android.yml`) اضافه شده که رو گیت‌هاب اکشن
  (که اینترنت کامل داره) واقعاً build و تست می‌کنه — نتیجه‌ی واقعی رو از اونجا ببین

## اجرای تست‌های موتور محاسبه (بدون نیاز به اندروید)

```bash
cd native-android
./gradlew :core:test
```

## ساخت APK دیباگ (نیاز به دسترسی به dl.google.com)

```bash
cd native-android
./gradlew :app:assembleDebug
```

(از این به بعد پروژه `gradlew` داره — دیگه نیازی به نصب جدا Gradle نیست. اگه محیطت هم مثل سندباکس فعلی
`dl.google.com`/`services.gradle.org` رو بلاک کرده، دانلود اولیه‌ی distribution هم fail می‌شه؛ رو یه
ماشین معمولی با اینترنت کامل (یا رو گیت‌هاب اکشن) مشکلی نداره.)
