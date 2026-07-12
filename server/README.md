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
# و اگه حساب کاوه‌نگار دارید KAVENEGAR_API_KEY رو هم بذارید (وگرنه فقط تو لاگ سرور کد رو می‌بینید، پیامک واقعی نمی‌ره)

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

پیش‌فرض کاوه‌نگار (kavenegar.com) هست چون REST ساده‌ای داره. برای عوض کردن به یه سرویس دیگه
(ملی‌پیامک، ippanel و ...) فقط کافیه `src/sms.js` رو ویرایش کنید — بقیه‌ی کد کاری نداره پیامک از کجا می‌ره.

## گزارش خطا (کرش ریپورت)

کلاینت (`window.onerror`/`unhandledrejection` تو `www/index.html`) خطاهای رخ‌داده رو به‌صورت خودکار
و بی‌صدا به `POST /api/crash` می‌فرسته (بدون نیاز به لاگین، بدون سرویس ثالث مثل Sentry).
چون فعلاً پنل مدیریتی جداگانه‌ای نساختیم، برای دیدن گزارش‌ها کافیه رو خودِ سرور مستقیم دیتابیس رو کوئری کنید:

```bash
sqlite3 server/data.sqlite "SELECT id, message, context, app_version, created_at FROM crash_reports ORDER BY id DESC LIMIT 20;"
```
