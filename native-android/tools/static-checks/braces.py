"""تعادلِ آکولاد/پرانتز - ویرایش‌های متنیِ برنامه‌نویسی‌شده راحت یه `}` جا می‌ندازن و
سندباکس نمی‌تونه کامپایل کنه، پس این حداقلِ چکِ ساختاریه.
رشته‌ها، کاراکترها و کامنت‌ها حذف می‌شن تا آکولادِ داخلِ متن شمرده نشه."""
import re, sys, os

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                    '..', '..', 'app/src/main/kotlin/ir/sadteam/loancalc')

def strip(src: str) -> str:
    out, i, n = [], 0, len(src)
    while i < n:
        c = src[i]
        if src.startswith('"""', i):
            j = src.find('"""', i + 3)
            i = n if j < 0 else j + 3
            continue
        if c == '"':
            i += 1
            while i < n and src[i] != '"':
                i += 2 if src[i] == '\\' else 1
            i += 1
            continue
        if c == "'":
            i += 1
            while i < n and src[i] != "'":
                i += 2 if src[i] == '\\' else 1
            i += 1
            continue
        if src.startswith('//', i):
            j = src.find('\n', i)
            i = n if j < 0 else j
            continue
        if src.startswith('/*', i):
            j = src.find('*/', i + 2)
            i = n if j < 0 else j + 2
            continue
        out.append(c)
        i += 1
    return ''.join(out)

bad = []
for rel in sys.argv[1:]:
    path = rel if rel.startswith('/') else os.path.join(ROOT, rel)
    s = strip(open(path, encoding='utf-8').read())
    for open_c, close_c, name in (('{', '}', 'آکولاد'), ('(', ')', 'پرانتز'), ('[', ']', 'براکت')):
        d = s.count(open_c) - s.count(close_c)
        if d != 0:
            bad.append((rel, name, d))

if bad:
    print('❌ ناتعادل:')
    for rel, name, d in bad:
        print(f'   {rel}: {name} {"+" if d > 0 else ""}{d}')
    sys.exit(1)
print(f'✅ همه‌ی {len(sys.argv) - 1} فایل متعادل‌ان')
