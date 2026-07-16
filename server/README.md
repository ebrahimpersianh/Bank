# بک‌اند «وام من»

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

### تنظیم سمت اپ

بعد از این‌که سرور بالا اومد و آدرسش (`https://api.example.com`) رو دارید، تو `www/index.html`
مقدار `API_BASE_URL` رو با همون آدرس عوض کنید (دنبال کامنت `TODO: آدرس بک‌اند` بگردید) و یه بیلد جدید بگیرید.

## دیتابیس

یه فایل SQLite ساده (`data.sqlite`) کنار jar ساخته می‌شه (مسیرش با `DB_PATH` تو `.env` قابل تغییره) —
نیازی به نصب دیتابیس جدا نیست. برای بک‌آپ‌گیری کافیه همین فایل رو دوره‌ای کپی کنید جای امن.
اسکیمای جدول‌ها (`Db.kt`) و مایگریشن‌های `ALTER TABLE` عیناً همون چیزیه که نسخه‌ی قبلی Node داشت.

## سرویس پیامکی

پیش‌فرض پیام‌رسان (payam-resan.com، وب‌سرویس تحت sms-webservice.com) هست. برای عوض کردن به یه سرویس دیگه
(کاوه‌نگار، ملی‌پیامک، ippanel و ...) فقط کافیه `src/main/kotlin/ir/sadteam/loancalc/server/Sms.kt` رو
ویرایش کنید — بقیه‌ی کد کاری نداره پیامک از کجا می‌ره.

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

تا وقتی این‌ها ست نشن، `POST /api/subscription/verify` همیشه `503 {error: 'cafebazaar_not_configured'}`
برمی‌گردونه (کلاینت پیام خطا نشون می‌ده، کرش نمی‌کنه). برای فعال کردن دستی اشتراک یه کاربر
(مثلاً برای پشتیبانی یا تست، بدون نیاز به کل این فلو) کافیه رو خودِ سرور این رو بزنید:

```bash
sqlite3 server/data.sqlite "UPDATE users SET subscribed = 1 WHERE phone = '09xxxxxxxxx';"
```

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
