# راه‌اندازیِ Firebase Test Lab برای جیبک — کارِ ابراهیم (یک‌بار، ~۱۰ دقیقه)

پروژه‌ی **jibak-test** ساخته شده. فقط سه کار مانده. همه با حسابِ گوگلِ خودت.

## ۱) روشن کردنِ دو سرویس
1. برو به: https://console.cloud.google.com/apis/library?project=jibak-test
2. در جست‌وجو بنویس **Cloud Testing API** ← بازش کن ← **Enable**.
3. دوباره جست‌وجو: **Cloud Tool Results API** ← **Enable**.

> ⚠️ اگر هر جا **کارتِ بانکی / Billing** خواست، **ادامه نده** و به من بگو (قرار است هیچ هزینه‌ای نباشد).

## ۲) ساختنِ «حسابِ ربات» و کلیدش
1. برو به: https://console.cloud.google.com/iam-admin/serviceaccounts?project=jibak-test
2. **+ Create service account** ← اسم: `github-testlab` ← **Create and continue**.
3. نقش (Role): **Editor** را انتخاب کن ← **Continue** ← **Done**.
   (این ربات فقط به همین پروژه‌ی تستی دسترسی دارد، نه به چیزِ دیگری.)
4. روی همان حسابِ تازه بزن ← تبِ **Keys** ← **Add key** ← **Create new key** ← **JSON** ← **Create**.
   یک فایلِ `.json` دانلود می‌شود.

## ۳) گذاشتنِ کلید در گیت‌هاب
1. برو به: https://github.com/ebrahimpersianh/Bank/settings/secrets/actions
2. **New repository secret**
   - Name: `FIREBASE_SERVICE_ACCOUNT`
   - Secret: فایلِ `.json` را با Notepad باز کن، **همه‌اش** را کپی کن و این‌جا بچسبان.
3. **Add secret**.
4. حالا فایلِ `.json` را از کامپیوتر **پاک کن** (و از سطلِ زباله هم).

> 🔒 این فایل/متن را **هیچ‌وقت** در چت نفرست — نه برای من، نه برای میوز. فقط در گیت‌هاب.

## بعد چه می‌شود
- به Claude بگو «تمام شد» تا اولین گشت را اجرا کند.
- از آن به بعد، **بعد از هر بیلدِ اندروید** خودکار اجرا می‌شود (ورک‌فلوی `firebase-robo.yml`).
- دستی هم می‌شود: Actions ← «گشتِ خودکارِ Firebase Test Lab» ← Run workflow.
- نتیجه: در صفحه‌ی همان اجرا یک **لینک** می‌آید (فیلم، عکس، فهرستِ کرش‌ها در Firebase) + فایلِ نتایج.
- هر اجرا ۲ دستگاه = ۲ تا از سهمیه‌ی رایگانِ روزانه (۱۵ تا).
