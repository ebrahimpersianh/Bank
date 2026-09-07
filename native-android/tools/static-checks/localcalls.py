"""
بررسیِ فراخوانیِ کامپوزیبلِ **محلی**ی که تعریفش تو فایل نیست و ایمپورت هم نشده.

بیلدِ ۵۱۵ به‌خاطرِ همین شکست: بسته‌ی طراح برای `LoanDetailScreen` تعریفِ
`private fun AttachmentToggleButton(...)` رو (که تهِ همون فایل بود) جا انداخته بود، ولی دو
فراخوانش رو نگه داشته بود → `Unresolved reference`.

`ownsymbols.py` این رو نمی‌گیره چون اون دنبالِ نمادِ **پروژه‌ای بدونِ ایمپورت**ه؛ اینجا نماد
اصلاً هیچ‌جای پروژه وجود نداره. الگوی خطرناکش هم مشخصه: بسته‌ی بیرونی که یه فایلِ کامل رو
جایگزین می‌کنه، راحت یه تابعِ کمکیِ خصوصیِ تهِ فایل رو می‌ندازه.

قاعده: هر شناسه‌ای که با حرفِ بزرگ شروع می‌شه و مثلِ تابع صدا زده شده، باید یا تو همین فایل
تعریف شده باشه، یا ایمپورت شده باشه، یا جای دیگه‌ای از پروژه public/internal باشه.
"""
import re, glob, os, sys

# نامِ نوع/تابعِ استانداردِ کاتلین که ایمپورت نمی‌خوان
BUILTIN = {
    'String', 'StringBuilder', 'Regex', 'Pair', 'Triple', 'Array', 'IntArray', 'DoubleArray',
    'LongArray', 'BooleanArray', 'FloatArray', 'ByteArray', 'CharArray', 'Exception',
    'RuntimeException', 'IllegalStateException', 'IllegalArgumentException', 'Throwable',
    'Error', 'Int', 'Long', 'Double', 'Float', 'Boolean', 'Char', 'Byte', 'Short', 'Any',
    'Unit', 'Nothing', 'List', 'Map', 'Set', 'MutableList', 'MutableMap', 'MutableSet',
    'ReplaceWith', 'Deprecated', 'Suppress', 'JvmStatic', 'JvmOverloads', 'Volatile',
    'Comparator', 'Thread', 'Runnable', 'Math', 'Charsets', 'System', 'Locale',
    # عضوِ اسکوپِ کامپوزیبل - بدونِ ایمپورتِ جدا در دسترسه
    'ExposedDropdownMenu',
}

def strip_noise(src):
    """کامنت و رشته رو خالی می‌کنه تا نامِ داخلِ توضیح/متن هشدارِ کاذب نده."""
    src = re.sub(r'/\*.*?\*/', lambda m: '\n' * m.group(0).count('\n'), src, flags=re.S)
    src = re.sub(r'//[^\n]*', '', src)
    src = re.sub(r'""".*?"""', lambda m: '\n' * m.group(0).count('\n'), src, flags=re.S)
    src = re.sub(r'"(?:[^"\\\n]|\\.)*"', '""', src)
    return src

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..')
KT = [p for p in glob.glob(os.path.join(ROOT, '**/*.kt'), recursive=True)
      if '/build/' not in p.replace(os.sep, '/')]

# هر نامی که یه‌جای پروژه به‌عنوانِ تابع/کلاس/آبجکت/enum تعریف شده
declared = set()
for path in KT:
    src = strip_noise(open(path, encoding='utf-8').read())
    # شاملِ تابعِ گیرنده‌دار (`fun RowScope.BottomNavItem(`) و جنریک (`fun <T> Foo(`)
    for m in re.finditer(r'\b(?:fun|class|object|interface)\s+(?:<[^>]*>\s*)?(?:[\w.]+\.)?([A-Z]\w*)', src):
        declared.add(m.group(1))
    for m in re.finditer(r'\bval\s+([A-Z]\w*)\s*[:=]', src):
        declared.add(m.group(1))
    # هر چیزی که **جایی از پروژه** ایمپورت شده، نامِ شناخته‌شده‌ست (کامپوزیبل‌های فریمورک،
    # سازنده‌های کتابخانه‌ها و…). بدونِ این، هر `Column(`/`Text(` هشدارِ کاذب می‌شد.
    for name in re.findall(r'^import\s+[\w.]*?\.([A-Z]\w*)', src, re.M):
        declared.add(name)
    # عضوِ enum/ثابت که با نامِ بزرگ صدا زده می‌شه
    for name in re.findall(r'^\s{4}([A-Z][A-Z0-9_]+)\s*\(', src, re.M):
        declared.add(name)

problems = []
for path in KT:
    src = strip_noise(open(path, encoding='utf-8').read())
    rel = os.path.relpath(path, ROOT)
    imported = set(re.findall(r'^import\s+[\w.]*?\.(\w+)', src, re.M))
    # سازنده‌ی superclass تو اعلانِ کلاس (`: Application()`) نه فراخوانیِ معمولی

    local = set(re.findall(r'\b(?:fun|class|object|interface|val|var)\s+([A-Z]\w*)', src))
    # دو الگو: فراخوانیِ تابع/سازنده (`Name(`) و دسترسیِ عضوِ نوع (`Type.MEMBER`).
    # الگوی دوم را نداشتیم و بیلدِ ۵۱۸ با `AppButtonVariant.SECONDARY`ِ بی‌ایمپورت شکست -
    # enum و objectِ پروژه معمولاً همین‌طور مصرف می‌شوند، نه با پرانتز.
    hits = list(re.finditer(r'(?<![\w.@])([A-Z]\w{2,})\s*\(', src))
    hits += list(re.finditer(r'(?<![\w.@"])([A-Z]\w{2,})\.[A-Z_]\w*', src))
    for m in hits:
        name = m.group(1)
        # نامِ تمام‌بزرگ = عضوِ enum یا ثابت، نه کامپوزیبل. الگوی خطایی که این بررسی
        # دنبالشه CamelCase ـه.
        if name.isupper():
            continue
        if name in BUILTIN or name in local or name in imported or name in declared:
            continue
        # سازنده‌ی نوعِ کاتلین/اندروید که ایمپورتِ خودکار دارن یا با نامِ کامل صدا زده می‌شن
        line = src[:m.start()].count('\n') + 1
        problems.append(f'{rel}:{line}  {name}(  ← نه تو فایل تعریف شده، نه ایمپورت')

if problems:
    print(f'❌ {len(problems)} فراخوانیِ نمادِ ناموجود:')
    for p in problems:
        print('   ' + p)
    sys.exit(1)
print(f'✅ همه‌ی فراخوانی‌های نامِ‌بزرگ تعریف یا ایمپورت شده‌اند ({len(KT)} فایل)')
