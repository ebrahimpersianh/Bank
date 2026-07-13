/* این فایل با esbuild بسته‌بندی می‌شه به www/vendor/biometric-auth.js تا بدون
   نیاز به build step واقعی تو خودِ index.html، پلاگین بایومتریک به‌صورت یه
   اسکریپت ساده (بدون import/module) قابل استفاده باشه. اگه نسخه‌ی پکیج رو عوض
   کردید، دوباره از پوشه‌ی اصلی پروژه این دستور رو بزنید:
     npx esbuild scripts/biometric-entry.js --bundle --format=iife --outfile=www/vendor/biometric-auth.js */
import { BiometricAuth, BiometryErrorType } from '@aparajita/capacitor-biometric-auth';

window.BiometricAuth = BiometricAuth;
window.BiometryErrorType = BiometryErrorType;
