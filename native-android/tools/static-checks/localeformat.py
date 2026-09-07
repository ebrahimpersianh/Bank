"""
بررسیِ `String.format`/`.format()`ِ **بدونِ Locale** روی رشته‌ی عددی.

باگِ خاموشی که کلاد دیزاین پیدا کرد: `"%.2f".format(v)` از `Locale.getDefault()` استفاده
می‌کند. روی گوشیِ فارسی - یعنی دقیقاً کاربرِ هدفِ این برنامه - خروجی رقمِ فارسی و جداکننده‌ی
اعشارِ عربی می‌دهد. آن رشته مستقیم در `rateText` می‌نشیند و بعد `toDoubleOrNull()` می‌شود:
`null` برمی‌گردد، نرخ صفر می‌شود، و چون نرخِ صفر یعنی `LoanMethod.QARZ`، کلِ محاسبه‌ی وام
عوض می‌شود - **بدونِ هیچ خطا یا نشانه‌ای**. در چهار صفحه‌ی محاسبه تکرار شده بود.

قاعده: هر جا خروجیِ فرمت قرار است دوباره پارس شود، باید `Locale.US` صریح باشد. متنی که فقط
نمایش داده می‌شود این مشکل را ندارد، ولی تفکیکشان از روی متن ممکن نیست، پس همه‌ی موارد
گزارش می‌شوند و استثناها با `Locale` صریح رفع می‌شوند.
"""
import re, glob, os, sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..')
# فرمتِ عددی؛ %s تنها بی‌خطر است چون رشته را دست‌کاری نمی‌کند.
NUMERIC_FORMAT = re.compile(r'"[^"\n]*%[-+ 0,#]*[\d.]*[dfeEgGx][^"\n]*"\s*\.format\s*\(')

problems = []
for path in glob.glob(os.path.join(ROOT, '**/*.kt'), recursive=True):
    if '/build/' in path.replace(os.sep, '/'):
        continue
    src = open(path, encoding='utf-8').read()
    src = re.sub(r'//[^\n]*', '', src)
    rel = os.path.relpath(path, ROOT)
    for m in NUMERIC_FORMAT.finditer(src):
        line = src[:m.start()].count('\n') + 1
        problems.append(f'{rel}:{line}  {m.group(0).strip()}…')

if problems:
    print(f'❌ {len(problems)} فرمتِ عددیِ بدونِ Locale:')
    for p in problems:
        print('   ' + p)
    print('   رفع: String.format(Locale.US, "…", v) - وگرنه رو گوشیِ فارسی رقمِ فارسی می‌دهد')
    print('   و اگر دوباره پارس شود، بی‌صدا null/صفر می‌شود.')
    sys.exit(1)
print('✅ همه‌ی فرمت‌های عددی Locale صریح دارند')
