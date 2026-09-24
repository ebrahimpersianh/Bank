"""
شکارِ بازمانده‌های سبکِ قدیمی («Liquid Glass») که تو بازطراحیِ «جیبک» باید حذف بشن.

هر کدوم از این‌ها قاعده‌ای از سیستمِ طراحی رو می‌شکنه:
  * `Modifier.shadow(`      → سایه‌ی **تار**؛ سبکِ جدید فقط سایه‌ی سختِ `hardShadow` داره
  * `AppGlass*`             → توکنِ منسوخِ شیشه‌ای
  * `.copy(alpha` رو سطح    → سطح باید **کاملاً مات** باشه
  * `RoundedCornerShape(<عددِ غیرِ توکن>)` → شعاع باید از `AppRadius` بیاد
  * `Brush.radialGradient`  → گرادیانِ نوری حذف شده (تنها استثنا هاله‌ی کارتِ قهرمان)
  * `FloatingActionButton`  → جاش `AppFab`ه

خروجی بر اساسِ فایل مرتب می‌شه تا بشه صفحه‌به‌صفحه پاک‌سازی کرد.
"""
import re, glob, os, sys
from collections import defaultdict

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                    '..', '..', 'app/src/main/kotlin/ir/sadteam/loancalc')

# شعاع‌های مجازِ توکن‌شده (AppRadius) + چندتای رایجِ بی‌ضرر
ALLOWED_RADII = {'12', '16', '20', '28', '999', '11', '14', '3', '4', '6', '8', '10', '18'}

PATTERNS = [
    (r'\.shadow\s*\(', 'سایه‌ی تارِ Material (جاش hardShadow)'),
    (r'\bAppGlass\w+', 'توکنِ منسوخِ شیشه‌ای'),
    (r'Brush\.radialGradient', 'گرادیانِ نوری/شعاعی'),
    (r'\bFloatingActionButton\s*\(', 'FAB متریال (جاش AppFab)'),
    (r'\bSurface\s*\(\s*$', 'Surface خام (جاش AppCard)'),
]

hits = defaultdict(list)
files = sorted(glob.glob(os.path.join(ROOT, '**/*.kt'), recursive=True))
for path in files:
    rel = os.path.relpath(path, ROOT)
    if rel.startswith('ui/theme/') or 'AppCard.kt' in rel or 'AppHeroCard.kt' in rel:
        continue                      # خودِ تعریف‌ها - استثنا
    src = open(path, encoding='utf-8').read()
    body = re.sub(r'//.*', '', src)
    body = re.sub(r'/\*.*?\*/', '', body, flags=re.S)
    for pat, label in PATTERNS:
        for m in re.finditer(pat, body, re.M):
            hits[rel].append((body[:m.start()].count('\n') + 1, label))
    # شعاعِ هاردکدِ غیرِ توکن
    for m in re.finditer(r'RoundedCornerShape\(\s*(\d+(?:\.\d+)?)\.dp', body):
        if m.group(1) not in ALLOWED_RADII:
            hits[rel].append((body[:m.start()].count('\n') + 1, f'شعاعِ هاردکدِ {m.group(1)}dp'))

total = sum(len(v) for v in hits.values())
if not hits:
    print(f'✅ هیچ بازمانده‌ی سبکِ قدیمی نیست ({len(files)} فایل)')
    sys.exit(0)

print(f'⚠️ {total} بازمانده‌ی سبکِ قدیمی تو {len(hits)} فایل:\n')
for rel in sorted(hits, key=lambda r: -len(hits[r])):
    print(f'  {rel}  ({len(hits[rel])})')
    seen = set()
    for line, label in sorted(hits[rel]):
        if label in seen and len(seen) > 0:
            continue
        seen.add(label)
        print(f'     خط {line}: {label}')
