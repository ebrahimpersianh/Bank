/* این فایل با esbuild بسته‌بندی می‌شه به www/vendor/poolakey.js تا بدون نیاز به
   build step واقعی تو خودِ index.html، پلاگین پرداخت درون‌برنامه‌ای کافه‌بازار
   (Poolakey) به‌صورت یه اسکریپت ساده (بدون import/module) قابل استفاده باشه. اگه
   نسخه‌ی پکیج رو عوض کردید، دوباره از پوشه‌ی اصلی پروژه این دستور رو بزنید:
     npx esbuild scripts/poolakey-entry.js --bundle --format=iife --outfile=www/vendor/poolakey.js */
import { CafebazaarPoolakey } from '@salarizadi/capacitor-cafebazaar-poolakey';

window.CafebazaarPoolakey = CafebazaarPoolakey;
