"""
بررسیِ فراخوانیِ کمک‌تابع‌های **Long-only** با مقدارِ Double.

بیلدِ ۵۱۴ به‌خاطرِ همین شکست: بسته‌ی طراح `rialToToman(loan.installment)` نوشته بود، ولی
`rialToToman`/`tomanToRial` (تو `ui/jibak/JibakFormat.kt`) امضاشون `Long`ه در حالی که کلِ
مبلغ‌های پروژه `Double`ن (رجوع کن به `moneytypes.py`). دو قاعده‌ی درست، برخوردشون خطا:
«مبلغ Double است» + «تبدیلِ واحد Long است».

`moneytypes.py` این رو نمی‌گیره چون اسمِ پارامترِ خودِ این توابع (`rial`/`toman`) تو الگوی
پول‌مانندِ اون نیست - و عوض‌کردنِ امضای خودِ توابع هم ریسکِ بی‌مورده (فراخوان‌های Longِ درستِ
دیگه‌ای دارن).

قاعده: آرگومانِ این توابع باید صریحاً Long باشه - یعنی یا `.toLong()` تهش باشه، یا عددِ ثابت،
یا اسمی که خودش Long بودنش پیداست. هر چیزِ دیگه هشدار می‌گیره تا نویسنده صریح تصمیم بگیره.
"""
import re, glob, os, sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..')
LONG_ONLY = ('rialToToman', 'tomanToRial')
# آرگومانی که Long بودنش از رو خودِ متن قطعیه
SAFE = re.compile(r'(\.toLong\(\)|\.toLongOrNull\(\)|^\d+L?$|Long\b)')
IDENT = re.compile(r'^\w+$')

problems = []
for path in glob.glob(os.path.join(ROOT, '**/*.kt'), recursive=True):
    if '/build/' in path.replace(os.sep, '/'):
        continue
    src = open(path, encoding='utf-8').read()
    rel = os.path.relpath(path, ROOT)
    for fn in LONG_ONLY:
        # امضای خودِ تابع رو نگیر
        for m in re.finditer(r'(?<![\w.])' + fn + r'\(', src):
            before = src[max(0, m.start() - 4):m.start()]
            if before.endswith('fun '):
                continue
            # آرگومان تا پرانتزِ بسته‌ی متناظر
            depth, i = 1, m.end()
            while i < len(src) and depth:
                if src[i] == '(':
                    depth += 1
                elif src[i] == ')':
                    depth -= 1
                i += 1
            arg = src[m.end():i - 1].strip()
            if SAFE.search(arg):
                continue
            # `it` داخلِ یه زنجیره‌ی `...toLong()?.let { ... }` خودش Longه.
            if arg == 'it' and 'toLong' in src[max(0, m.start() - 80):m.start()]:
                continue
            # آرگومانِ تک‌اسمی: اگه تو همین فایل از یه عبارتِ Long ساخته شده، سالمه.
            if IDENT.match(arg) and re.search(
                r'\b(?:val|var)\s+' + re.escape(arg) + r'\b[^\n]*(?:toLong|toLongOrNull|\d+L)',
                src,
            ):
                continue
            if True:
                line = src[:m.start()].count('\n') + 1
                problems.append(f'{rel}:{line}  {fn}({arg})  ← آرگومان Long نیست')

if problems:
    print(f'❌ {len(problems)} فراخوانیِ کمک‌تابعِ Long-only با آرگومانِ غیرLong:')
    for p in problems:
        print('   ' + p)
    print('   رفع: `.toLong()` بذار و اگه خروجی جای Double لازمه `.toDouble()` بگیر.')
    sys.exit(1)
print('✅ همه‌ی فراخوانی‌های rialToToman/tomanToRial آرگومانِ Long دارن')
