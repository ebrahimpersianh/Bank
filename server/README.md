# بک‌اند «وام من»

یه سرور خیلی سبک (Node.js + Express + SQLite) برای سه تا کار:
1. ورود با شماره موبایل و کد تایید پیامکی (OTP)
2. همگام‌سازی ابری «وام‌های من» بین چند گوشی
3. جمع‌آوری گزارش خطا/کرش از کلاینت (بدون سرویس ثالث)

## راه‌اندازی رو سرور خودتون

نیاز دارید:
- یه VPS با Node.js نسخه ۱۸ به بالا (هر هاست ایرانی یا خارجی که SSH داره کافیه)
- یه دامنه یا ساب‌دامنه که بشه روش HTTPS ست کرد (مثلاً `api.example.com`) — چون اپ اندروید به آدرس http ساده وصل نمی‌شه مگر صریح اجازه بدید، و ارسال شماره موبایل/کد روی http ساده هم امن نیست

### مراحل

```bash
git clone <این‌ریپو>
cd server
npm install
cp .env.example .env
# .env رو باز کنید و JWT_SECRET رو با این دستور بسازید:
node -e "console.log(require('crypto').randomBytes(48).toString('hex'))"
# و اگه حساب پیام‌رسان دارید PAYAMRESAN_API_KEY رو هم بذارید (وگرنه فقط تو لاگ سرور کد رو می‌بینید، پیامک واقعی نمی‌ره)

npm install -g pm2
pm2 start src/index.js --name loan-calc-api
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

### تنظیم سمت اپ

بعد از این‌که سرور بالا اومد و آدرسش (`https://api.example.com`) رو دارید، تو `www/index.html`
مقدار `API_BASE_URL` رو با همون آدرس عوض کنید (دنبال کامنت `TODO: آدرس بک‌اند` بگردید) و یه بیلد جدید بگیرید.

## دیتابیس

یه فایل SQLite ساده (`data.sqlite`) کنار کد ساخته می‌شه — نیازی به نصب دیتابیس جدا نیست.
برای بک‌آپ‌گیری کافیه همین فایل رو دوره‌ای کپی کنید جای امن.

## سرویس پیامکی

پیش‌فرض پیام‌رسان (payam-resan.com، وب‌سرویس تحت sms-webservice.com) هست. برای عوض کردن به یه سرویس دیگه
(کاوه‌نگار، ملی‌پیامک، ippanel و ...) فقط کافیه `src/sms.js` رو ویرایش کنید — بقیه‌ی کد کاری نداره پیامک از کجا می‌ره.

## گزارش خطا (کرش ریپورت)

کلاینت (`window.onerror`/`unhandledrejection` تو `www/index.html`) خطاهای رخ‌داده رو به‌صورت خودکار
و بی‌صدا به `POST /api/crash` می‌فرسته (بدون نیاز به لاگین، بدون سرویس ثالث مثل Sentry).
چون فعلاً پنل مدیریتی جداگانه‌ای نساختیم، برای دیدن گزارش‌ها کافیه رو خودِ سرور مستقیم دیتابیس رو کوئری کنید:

```bash
sqlite3 server/data.sqlite "SELECT id, message, context, app_version, created_at FROM crash_reports ORDER BY id DESC LIMIT 20;"
```

## اشتراک (محدودیت یک وام رایگان)

هر کاربر (مهمون یا لاگین‌کرده) بدون اشتراک فقط می‌تونه یک وام تو «وام‌های من» ذخیره کنه؛ برای وام
بیشتر اول باید وارد بشه، بعد اشتراک بخره. خرید از طریق پرداخت درون‌برنامه‌ای کافه‌بازار (پلاگین
Poolakey) انجام می‌شه — چون پلاگین موجود فقط خرید یک‌باره/غیرقابل‌مصرف پشتیبانی می‌کنه (نه اشتراک
تمدیدشونده‌ی واقعی)، تو پنل کافه‌بازار هم محصول رو از نوع **«محصول درون‌برنامه‌ای غیرقابل‌مصرف»**
تعریف کنید، نه «اشتراک».

برای فعال شدن واقعیِ تایید خرید (`POST /api/subscription/verify`)، باید این مراحل رو یه‌بار تو
[پنل توسعه‌دهندگان کافه‌بازار](https://pardakht.cafebazaar.ir/panel/developer-api) انجام بدید:

1. اپ (`ir.sadteam.loancalc`) رو تو پنل کافه‌بازار (Pishkhan) ثبت کنید و یه محصول درون‌برنامه‌ای
   غیرقابل‌مصرف براش تعریف کنید — شناسه‌ی محصول (Product ID) رو یادداشت کنید.
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
   `.env` سرور بذارید و `pm2 restart` بزنید.

تا وقتی این‌ها ست نشن، `POST /api/subscription/verify` همیشه `503 {error: 'cafebazaar_not_configured'}`
برمی‌گردونه (کلاینت پیام خطا نشون می‌ده، کرش نمی‌کنه). برای فعال کردن دستی اشتراک یه کاربر
(مثلاً برای پشتیبانی یا تست، بدون نیاز به کل این فلو) کافیه رو خودِ سرور این رو بزنید:

```bash
sqlite3 server/data.sqlite "UPDATE users SET subscribed = 1 WHERE phone = '09xxxxxxxxx';"
```
