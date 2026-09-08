"""
بررسیِ **دو `companion object` در یک کلاس**.

بیلدِ ۵۲۰ با همین شکست: به `UiPrefs` که از قبل یک companion وسطِ فایل داشت، یکی دیگر تهِ
فایل اضافه شد - «Only one companion object is allowed per class». کاتلین فقط یکی اجازه
می‌دهد.

`redeclare.py` این را نمی‌گیرد چون آن دنبالِ یک **نام** در دو فایلِ هم‌پکیج است؛ اینجا هر دو
بی‌نام‌اند و در یک فایل. الگوی خطرش هم مشخص است و تکرارشدنی: کلاسِ بلندی که companionش وسطِ
فایل گم شده و کسی تهِ فایل یکی تازه می‌سازد.
"""
import glob
import os
import re
import sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..')
DECL = re.compile(r'^(?:@\w+\s+)*(?:\w+\s+)*(?:class|object|interface)\s+(\w+)')

problems = []
for path in glob.glob(os.path.join(ROOT, '**/*.kt'), recursive=True):
    if '/build/' in path.replace(os.sep, '/'):
        continue
    src = open(path, encoding='utf-8').read()
    src = re.sub(r'/\*.*?\*/', '', src, flags=re.S)
    src = re.sub(r'//[^\n]*', '', src)
    rel = os.path.relpath(path, ROOT)
    # عمقِ آکولاد نگه داشته می‌شود تا فقط اعلانِ **سطحِ بالا** مالکِ companion شمرده شود -
    # وگرنه کلاسِ تودرتو با companionِ خودش هشدارِ کاذب می‌داد.
    depth = 0
    owner = None
    seen = {}
    for line in src.split('\n'):
        stripped = line.strip()
        if depth == 0:
            m = DECL.match(stripped)
            if m:
                owner = m.group(1)
                seen.setdefault(owner, 0)
        if stripped.startswith('companion object') and owner and depth == 1:
            seen[owner] = seen.get(owner, 0) + 1
        depth = max(0, depth + line.count('{') - line.count('}'))
    for name, count in seen.items():
        if count > 1:
            problems.append(f'{rel}  {name}  ← {count} تا companion object')

if problems:
    print(f'❌ {len(problems)} کلاس با بیش از یک companion object:')
    for p in problems:
        print('   ' + p)
    print('   کاتلین فقط یکی اجازه می‌دهد - محتوای دومی را در همان اولی ادغام کن.')
    sys.exit(1)
print('✅ هیچ کلاسی دو companion object ندارد')
