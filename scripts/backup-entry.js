/* این فایل با esbuild بسته‌بندی می‌شه به www/vendor/backup.js تا بدون نیاز به
   build step واقعی تو خودِ index.html، پلاگین‌های Filesystem/Share به‌صورت یه
   اسکریپت ساده (بدون import/module) قابل استفاده باشن. اگه نسخه‌ی پکیج رو عوض
   کردید، دوباره از پوشه‌ی اصلی پروژه این دستور رو بزنید:
     npx esbuild scripts/backup-entry.js --bundle --format=iife --outfile=www/vendor/backup.js */
import { Filesystem, Directory, Encoding } from '@capacitor/filesystem';
import { Share } from '@capacitor/share';

window.CapFilesystem = Filesystem;
window.CapDirectory = Directory;
window.CapEncoding = Encoding;
window.CapShare = Share;
