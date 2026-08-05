# بک‌اند «حسابدار من»

یه سرور خیلی سبک (Kotlin + Ktor + SQLite) برای سه تا کار:
1. ورود با شماره موبایل و کد تایید پیامکی (OTP)
2. همگام‌سازی ابری «وام‌های من» بین چند گوشی
3. جمع‌آوری گزارش خطا/کرش از کلاینت (بدون سرویس ثالث)

> این سرور قبلاً Node.js+Express بود؛ کل رفتار (endpoint ها، کدهای خطا، فرمول‌ها، فایل `data.sqlite`)
> عیناً به Kotlin/Ktor پورت شده — یه فایل `data.sqlite` قدیمی از نسخه‌ی Node مستقیماً با این نسخه هم کار می‌کنه
> (اسکیمای جدول‌ها یکیه، فقط دسترسی از better-sqlite3 به JDBC/sqlite-jdbc عوض شده).

## راه‌اندازی رو سرور خودتون

نیاز دارید:
- یه VPS با **JDK نسخه ۲۱ به بالا** (هر هاست ایرانی یا خارجی که SSH داره کافیه)
- یه دامنه یا ساب‌دامنه که بشه روش HTTPS ست کرد (مثلاً `api.example.com`) — چون اپ اندروید به آدرس http ساده وصل نمی‌شه مگر صریح اجازه بدید، و ارسال شماره موبایل/کد روی http ساده هم امن نیست

### مراحل

```bash
git clone <این‌ریپو>
cd server
cp .env.example .env
# .env رو باز کنید و JWT_SECRET رو با این دستور بسازید:
openssl rand -hex 48
# و اگه حساب پیام‌رسان دارید PAYAMRESAN_API_KEY رو هم بذارید (وگرنه فقط تو لاگ سرور کد رو می‌بینید، پیامک واقعی نمی‌ره)

./gradlew fatJar
# یه jar واحد و runnable می‌سازه: build/libs/loan-calculator-server-1.0.0-all.jar

npm install -g pm2   # اگه از قبل ندارید (pm2 خودش رو Node اجرا می‌شه، ولی هر برنامه‌ای از جمله jar رو مدیریت می‌کنه)
pm2 start java --name loan-calc-api --interpreter none -- -jar build/libs/loan-calculator-server-1.0.0-all.jar
pm2 save
```

بعد یه Nginx reverse proxy جلوش بذارید (برای HTTPS با Let's Encrypt/certbot):

```nginx
server {
    listen 443 ssl;
    server_name api.example.com;
    ssl_certificate     /etc/letsencrypt/live/api.example.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.example.com/privkey.pem;

    location / {
        proxy_pass http://127.0.0.1:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

**آپدیت بعدی**: `git pull`، `./gradlew fatJar`، بعد `pm2 restart loan-calc-api`. چون `data.sqlite` کنار jar (تو پوشه‌ی کاری pm2، معمولاً همون `server/`) نگه داشته می‌شه، بین آپدیت‌ها دست‌نخورده می‌مونه.

### دیپلویِ خودکار (CI/CD) - جایگزینِ آپلودِ دستی

`.github/workflows/build-server.yml` یه jobِ `deploy` داره که بعدِ هر پوش به `main` (اگه تست‌ها رد
بشن و jar ساخته بشه) خودکار jarِ جدید رو مستقیم رو VPS کپی، سالم‌بودنش رو چک، و pm2 رو ری‌استارت
می‌کنه - این جایگزینِ روشِ قبلیِ «آپلودِ دستیِ تیکه‌تیکه‌ی jar از رو چت» ئه که چندبار (jarِ خالی از
یه کپی‌ِ ناقص، jar تو مسیرِ اشتباه) سرورِ زنده رو کرش کرده بود.

**راه‌اندازیِ یه‌بارِ اولیه** (بدونِ این مراحل، jobِ deploy خودش رو گیت‌هاب رد/skip می‌شه، به build
لطمه نمی‌زنه):

1. رو همون VPS، یه کلیدِ SSHِ مخصوصِ دیپلوی بساز (نه کلیدِ شخصیِ خودت):
   ```bash
   ssh-keygen -t ed25519 -f ~/.ssh/github_deploy -N ""
   cat ~/.ssh/github_deploy.pub >> ~/.ssh/authorized_keys
   cat ~/.ssh/github_deploy   # این خروجی (کلیدِ خصوصی) رو کپی کن، مرحله‌ی بعد لازمش داری
   ```
2. رو VPS، پوشه‌ی مقصدِ آپلودِ موقت رو بساز (اسکریپت ازش استفاده می‌کنه):
   ```bash
   mkdir -p ~/VameMan/incoming
   ```
   (پوشه‌ی اصلیِ سرور قبلاً `~/loan-server` بود - رو خودِ VPS به `~/VameMan` تغییرِ نام داده شد تا
   اسمِ برندِ اپ رو داشته باشه و برای آینده که چند برنامه‌ی دیگه هم قراره رو همین سرور بیان، هرکدوم
   پوشه/اسمِ خودشون رو داشته باشن.)
3. تو گیت‌هاب: `Settings → Secrets and variables → Actions → New repository secret`، سه‌تا سکرت بساز:
   - `VPS_HOST` → `37.152.187.44`
   - `VPS_USER` → یوزرِ SSHِ VPS (همون یوزری که `~/VameMan` زیرِ همین هومِ همون یوزره)
   - `VPS_SSH_KEY` → کلیدِ خصوصیِ کاملِ مرحله‌ی ۱ (از `-----BEGIN` تا `-----END`)

بعدِ این سه‌تا، هر پوشِ بعدی به `main` که `server/**` رو عوض کنه، خودکار دیپلوی می‌شه - نتیجه‌ش تو
تبِ Actions همون کامیت قابلِ دیدنه (شاملِ لاگِ `pm2 logs` بعدِ ری‌استارت، برای تاییدِ واقعیِ بالا
اومدن، نه فقط «دستور اجرا شد»).

### تنظیم سمت اپ

بعد از این‌که سرور بالا اومد و آدرسش (`https://api.example.com`) رو دارید، تو `www/index.html`
مقدار `API_BASE_URL` رو با همون آدرس عوض کنید (دنبال کامنت `TODO: آدرس بک‌اند` بگردید) و یه بیلد جدید بگیرید.

## دیتابیس

یه فایل SQLite ساده (`data.sqlite`) کنار jar ساخته می‌شه (مسیرش با `DB_PATH` تو `.env` قابل تغییره) —
نیازی به نصب دیتابیس جدا نیست. برای بک‌آپ‌گیری کافیه همین فایل رو دوره‌ای کپی کنید جای امن.
اسکیمای جدول‌ها (`Db.kt`) و مایگریشن‌های `ALTER TABLE` عیناً همون چیزیه که نسخه‌ی قبلی Node داشت.

## سرویس پیامکی

پیش‌فرض ملی‌پیامک (melipayamak.com) هست — وب‌سرویس SOAP قدیمی (`api.payamak-panel.com`)، متد
`SendByBaseNumber` (ارسال با خط اشتراکی/پایه، نه خط اختصاصی).

**پیش‌نیاز دستی قبل از کار کردن OTP واقعی**: تو پنل ملی‌پیامک باید یه پترن متنی برای کد تایید
بسازید و منتظر تاییدش بمونید (پیامک > پترن‌ها؛ چیزی مثل «کد تایید شما: %code%»)، بعد شماره‌ی اون
پترن (`bodyId`) رو تو `.env` (`MELIPAYAMAK_BODY_ID`) بذارید — بدون پترن تاییدشده، ارسال همیشه
شکست می‌خوره. `MELIPAYAMAK_USERNAME`/`MELIPAYAMAK_PASSWORD` هم از پنل ملی‌پیامک گرفته می‌شن (اگه
حساب جدید Api Key بهتون داده به‌جای رمز عبور، همون Api Key رو تو `MELIPAYAMAK_PASSWORD` بذارید).

**گذاشتنِ این سه مقدار رو VPS دیگه نیازی به SSH دستی نداره**: ورک‌فلوی
`.github/workflows/set-melipayamak-env.yml` (اجرای دستی از تبِ Actions) این سه مقدار رو از سکرت‌های
گیت‌هاب (`MELIPAYAMAK_USERNAME`/`MELIPAYAMAK_PASSWORD`/`MELIPAYAMAK_BODY_ID`، دقیقاً همون روشِ ساده‌ای
که برای `RELEASE_KEYSTORE_PASSWORD` و بقیه استفاده شده) می‌خونه و خودش تو `.env` رو VPS می‌نویسه/آپدیت
می‌کنه و سرویس رو ری‌استارت می‌کنه - فقط کافیه این سه سکرت تو تنظیماتِ ریپو اضافه بشن.

برای عوض کردن به یه سرویس دیگه (کاوه‌نگار، ippanel و ...) فقط کافیه
`src/main/kotlin/ir/sadteam/loancalc/server/Sms.kt` رو ویرایش کنید — بقیه‌ی کد کاری نداره
پیامک از کجا می‌ره.

## گزارش خطا (کرش ریپورت)

کلاینت (`window.onerror`/`unhandledrejection` تو `www/index.html`) خطاهای رخ‌داده رو به‌صورت خودکار
و بی‌صدا به `POST /api/crash` می‌فرسته (بدون نیاز به لاگین، بدون سرویس ثالث مثل Sentry).
چون فعلاً پنل مدیریتی جداگانه‌ای نساختیم، برای دیدن گزارش‌ها کافیه رو خودِ سرور مستقیم دیتابیس رو کوئری کنید:

```bash
sqlite3 server/data.sqlite "SELECT id, message, context, app_version, created_at FROM crash_reports ORDER BY id DESC LIMIT 20;"
```

## اشتراک (محدودیت یک وام رایگان)

هر کاربر (مهمون یا لاگین‌کرده) بدون اشتراک فقط می‌تونه یک وام تو «وام‌های من» ذخیره کنه؛ برای وام
بیشتر اول باید وارد بشه، بعد اشتراک بخره.

**دوره‌ی آزمایشیِ رایگانِ ۷روزه**: هر کاربری که با شماره موبایل وارد بشه (`POST /api/auth/verify-otp`)
تا ۷ روز بعد از اولین ورودش دسترسیِ کاملِ اشتراکی داره، بدونِ نیاز به خرید. این تو `isSubscribed()`
(`SubscriptionStatus.kt`) پیاده شده: علاوه بر `subscribed`/`subscribed_until`، اگه هنوز تو ۷ روزِ
اولِ بعد از `created_at` ردیفِ `users` باشیم هم true برمی‌گرده. `created_at` فقط یه‌بار (موقعِ ساختِ
ردیفِ کاربر، همون اولین `verify-otp` موفقِ اون شماره) ست می‌شه و هیچ روتی دیگه‌ای بهش دست نمی‌زنه —
یعنی خروج از حساب، پاک‌کردنِ داده‌ی اپ، حذف/نصبِ دوباره، یا حتی نصب رو گوشیِ دیگه، هیچ‌کدوم دوره‌ی
آزمایشی رو ریست نمی‌کنن (کلید همیشه شماره‌تلفنه، نه گوشی/نصب). بعد از ۷ روز، `isSubscribed()` خودکار
false برمی‌گرده و کاربر دقیقاً مثلِ یه کاربرِ عادیِ غیرمشترک محدود می‌شه (۱ وام).

استثنای این قانون `DELETE /api/auth/account` (حذفِ کاملِ حساب - رجوع کن به `routes/AuthRoutes.kt`)
هست: چون ردیفِ `users` واقعاً پاک می‌شه، اگه همون شماره دوباره وارد بشه یه ردیفِ کاملاً جدید با
`created_at` تازه ساخته می‌شه، یعنی دورهٔ آزمایشی ازنو شروع می‌شه. این عمداً همینه (حذفِ حساب یعنی
واقعاً از صفر شروع کردن)، برخلافِ خروج/نصبِ دوباره که چیزی رو سرور پاک نمی‌کنن.

هر مسیری که `isSubscribed(user)` رو صدا می‌زنه (`routes/AuthRoutes.kt` `/me`، `routes/LoansRoutes.kt` PUT) باید
`created_at` رو تو `SELECT`ش داشته باشه، وگرنه چک آزمایشی همیشه false می‌مونه — اگه یه روتِ جدید به
این تابع نیاز داشت، حواستون به این ستون باشه. اجرای واقعیِ محدودیت («۱ وام رایگان») همیشه سمتِ سرور
(`routes/LoansRoutes.kt` PUT، ۴۰۳ اگه `isSubscribed=false` و آرایه بیش از ۱ تا) چک می‌شه، نه کلاینت — یعنی
حتی اگه یه کلاینتِ دستکاری‌شده ادعا کنه مشترکه، این چک مستقل جلوش رو می‌گیره.

خرید واقعی از طریق پرداخت درون‌برنامه‌ای کافه‌بازار (پلاگین
Poolakey) انجام می‌شه — چون پلاگین موجود فقط خرید یک‌باره/غیرقابل‌مصرف پشتیبانی می‌کنه (نه اشتراک
تمدیدشونده‌ی واقعی)، ۴ تا پلن جداگانه (۱/۳/۶/۱۲ ماهه، هرکدوم یه محصول جدا تو پنل کافه‌بازار از
نوع **«غیرقابل‌مصرف»**، نه «اشتراک») تعریف می‌شن؛ سرور با شناسه‌ی محصولی که خریده شده تشخیص می‌ده
چند روز به `subscribed_until` کاربر اضافه کنه (`routes/SubscriptionRoutes.kt`، ثابت `TIER_DURATION_DAYS`).
یعنی تمدید خودکار نیست — کاربر باید خودش دوباره بعد از انقضا بخره.

برای فعال شدن واقعیِ تایید خرید (`POST /api/subscription/verify`)، باید این مراحل رو یه‌بار تو
[پنل توسعه‌دهندگان کافه‌بازار](https://pardakht.cafebazaar.ir/panel/developer-api) انجام بدید:

1. اپ (`ir.sadteam.loancalc`) رو تو پنل کافه‌بازار (Pishkhan) ثبت کنید و ۴ تا محصول درون‌برنامه‌ای
   غیرقابل‌مصرف براش تعریف کنید، با همین شناسه‌ها (باید دقیقاً با `CAFEBAZAAR_SUBSCRIPTION_TIERS` تو
   `www/index.html` و `TIER_DURATION_DAYS` تو `routes/SubscriptionRoutes.kt` یکی باشن):
   `unlimited_loans_1m`، `unlimited_loans_3m`، `unlimited_loans_6m`، `unlimited_loans_1y`
2. از بخش «پرداخت درون‌برنامه‌ای» اپ، **کلید RSA** رو کپی کنید — این تو کلاینت (`www/index.html`،
   ثابت `CAFEBAZAAR_RSA_PUBLIC_KEY`) لازمه.
3. از بخش «Developer API» یه کلاینت OAuth بسازید (client_id/client_secret می‌گیرید).
4. این آدرس رو تو مرورگر باز کنید (به‌جای مقادیر داخل `<>` مقادیر واقعی رو بذارید) و اجازه بدید:
   ```
   https://pardakht.cafebazaar.ir/devapi/v2/auth/authorize/?response_type=code&access_type=offline&redirect_uri=<REDIRECT_URI>&client_id=<CLIENT_ID>
   ```
   بعد از تایید، به `redirect_uri` با یه پارامتر `code` ریدایرکت می‌شید.
5. با اون `code`، یه درخواست POST به `https://pardakht.cafebazaar.ir/devapi/v2/auth/token/` بزنید
   (پارامترها: `grant_type=authorization_code`, `code`, `client_id`, `client_secret`, `redirect_uri`)
   تا `refresh_token` بگیرید — این یه‌بار مصرفه، فقط برای گرفتن refresh_token لازمه.
6. مقادیر `CAFEBAZAAR_CLIENT_ID`, `CAFEBAZAAR_CLIENT_SECRET`, `CAFEBAZAAR_REFRESH_TOKEN` رو تو
   `.env` سرور بذارید و `pm2 restart loan-calc-api` بزنید.

تا وقتی این‌ها ست نشن، `POST /api/subscription/verify` برای خریدهای کافه‌بازار همیشه
`503 {error: 'cafebazaar_not_configured'}` برمی‌گردونه (کلاینت پیام خطا نشون می‌ده، کرش نمی‌کنه).

### تایید خرید مایکت (`Myket.kt`)
همون منطق برای فلیورِ مایکت، ولی ساده‌تر - مایکت به‌جای فلوی OAuth یه **توکنِ دسترسیِ ثابت** می‌ده که
بینِ همه‌ی اپ‌های حساب مشترکه:

1. تو [پنل توسعه‌دهندگان مایکت](https://developer.myket.ir)، برای اپ (`ir.sadteam.loancalc`) همون ۴
   محصولِ درون‌برنامه‌ای رو با همین شناسه‌ها تعریف کنید (`unlimited_loans_1m/3m/6m/1y`).
2. از بخشِ محصولاتِ درون‌برنامه‌ای → «توکن دسترسی جدید» رو بزنید و مقدارش رو کپی کنید.
3. مقدارِ `MYKET_ACCESS_TOKEN` رو تو `.env` سرور بذارید و `pm2 restart loan-calc-api` بزنید.

کلاینت هر بار `store` (از `BuildConfig.FLAVOR`: `"cafebazaar"` یا `"myket"`) رو هم تو بدنه‌ی
`POST /api/subscription/verify` می‌فرسته تا سرور بدونه با کدوم API خرید رو تایید کنه؛ نسخه‌های
قدیمی‌ترِ اپ که این فیلد رو نمی‌فرستن، پیش‌فرض `cafebazaar` حساب می‌شن (سازگاریِ عقب‌رو). تا
`MYKET_ACCESS_TOKEN` ست نشه، خریدِ مایکت `503 {error: 'myket_not_configured'}` می‌گیره.

⚠️ فرمتِ دقیقِ بدنه‌ی ریسپانسِ موفقِ API صحت‌سنجیِ مایکت تو پنلشون مستند نشده (فقط فرمتِ درخواست
مستند شده)؛ برای همین `validateMyketPurchase` صرفاً بر اساسِ کدِ وضعیتِ HTTP (۲xx = معتبر) تصمیم
می‌گیره، نه یه فیلدِ خاص تو بدنه. بعد از اولین خریدِ واقعیِ تستی رو مایکت، لاگ‌های سرور رو چک کنید
ببینید این فرض درست بوده یا نه.

برای فعال کردن دستی اشتراک یه کاربر (مثلاً برای پشتیبانی یا تست، بدون نیاز به کل این فلو) کافیه
رو خودِ سرور این رو بزنید (فلگِ `subscribed=1` دائمیه و هیچ‌وقت منقضی نمی‌شه - برای حسابِ شخصیِ
خودِ توسعه‌دهنده هم همینه):

```bash
sqlite3 server/data.sqlite "UPDATE users SET subscribed = 1 WHERE phone = '09xxxxxxxxx';"
```

### حسابِ تستِ دائمی (برای بررسی‌کننده‌های استور)
اگه این دو متغیر تو `.env` ست بشن، برای اون یه شماره پیامک ارسال نمی‌شه و کدِ تایید همیشه ثابته -
حتی بعد از فعال‌شدنِ پیامکِ واقعیِ ملی‌پیامک (`routes/AuthRoutes.kt`، `isTestAccount`):

```
TEST_ACCOUNT_PHONE=09121111111
TEST_ACCOUNT_CODE=11111
```

بعدِ ست‌کردن و `pm2 restart loan-calc-api`، هر کسی (مثلاً تیمِ بررسیِ کافه‌بازار/مایکت) می‌تونه با
اون شماره + همون کدِ ثابت وارد بشه. برای اینکه حسابِ تست همه‌ی امکاناتِ اشتراکی رو هم نشون بده،
بعدِ اولین ورودش یه‌بار همون SQL بالا رو براش بزنید. برای بقیه‌ی شماره‌ها هیچ رفتاری عوض نمی‌شه.

## ساختار کد

```
server/
  build.gradle.kts          # وابستگی‌ها (Ktor، java-jwt، sqlite-jdbc) + تسک fatJar
  src/main/kotlin/ir/sadteam/loancalc/server/
    Application.kt           # main() + ماژول Ktor (CORS، ContentNegotiation، routing)
    Env.kt                    # بارگذاری .env
    Db.kt                     # اسکیمای SQLite + مایگریشن + helper تبدیل ردیف کاربر
    SqlExt.kt                 # هلپرهای کوچیک JDBC (execute/queryOne/insertReturningId)
    Auth.kt                   # امضا/چک JWT + requireAuth
    Sms.kt                    # ارسال OTP
    SubscriptionStatus.kt     # منطق isSubscribed/دوره‌ی آزمایشی
    Cafebazaar.kt              # تایید خرید درون‌برنامه‌ای
    routes/
      AuthRoutes.kt            # /api/auth/*
      LoansRoutes.kt           # /api/loans
      SubscriptionRoutes.kt    # /api/subscription/verify
      CrashRoutes.kt           # /api/crash
  src/test/kotlin/...         # تست end-to-end با ktor-server-test-host
```
