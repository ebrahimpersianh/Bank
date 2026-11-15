"""
هر دسترسیِ `palette.X` / `LocalAppColors.current.X` / `colors.X` باید یه فیلدِ واقعیِ
`AppColorPalette` باشه.

بیلدِ ۴۶۵ دو تا از خطاهاش دقیقاً همین بود: فیلدِ `accent` رو از پالت حذف کردم ولی `Theme.kt`
هنوز `palette.accent` صداش می‌زد. بررسیِ ایمپورت این رو نمی‌گیره چون ایمپورتی در کار نیست.
"""
import re, glob, os, sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                    '..', '..', 'app/src/main/kotlin/ir/sadteam/loancalc')
color_src = open(os.path.join(ROOT, 'ui/theme/Color.kt'), encoding='utf-8').read()

# فیلدهای data class AppColorPalette
m = re.search(r'data class AppColorPalette\((.*?)\n\)', color_src, re.S)
fields = set(re.findall(r'^\s*val (\w+):', m.group(1), re.M))
print(f'فیلدهای پالت: {len(fields)}')

bad = []
for path in glob.glob(os.path.join(ROOT, '**/*.kt'), recursive=True):
    src = open(path, encoding='utf-8').read()
    rel = os.path.relpath(path, ROOT)
    body = re.sub(r'//.*', '', src)
    body = re.sub(r'/\*.*?\*/', '', body, flags=re.S)
    for pat in (r'\bpalette\.(\w+)', r'LocalAppColors\.current\.(\w+)', r'\bcolors\.(\w+)'):
        for mm in re.finditer(pat, body):
            name = mm.group(1)
            if name in ('copy', 'current'):
                continue
            if name not in fields:
                # `colors.` ممکنه مالِ MaterialTheme.colorScheme باشه - فقط وقتی گزارش کن که
                # فایل از LocalAppColors استفاده کرده باشه
                if pat.startswith(r'\bcolors\.') and 'LocalAppColors' not in src:
                    continue
                line = body[:mm.start()].count('\n') + 1
                bad.append((rel, line, name))

if bad:
    print(f'\n❌ {len(bad)} دسترسی به فیلدی که تو پالت نیست:\n')
    for rel, line, name in bad:
        print(f'  {rel}:{line}  → .{name}')
    sys.exit(1)
print('✅ همه‌ی دسترسی‌های پالت معتبرن')
