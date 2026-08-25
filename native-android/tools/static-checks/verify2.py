"""
بررسیِ ارجاعاتِ داخلیِ پروژه: هر `import ir.sadteam.loancalc.X.Y` باید واقعاً تو پکیجِ X یه
declarationِ به‌نامِ Y داشته باشه. مکملِ verify.py (که ایمپورتِ *گمشده* رو می‌گیره) - این یکی
ایمپورتِ *شکسته* رو می‌گیره: نمادی که ایمپورت شده ولی دیگه وجود نداره.
"""
import re, os, glob, sys
from collections import defaultdict

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..')
SRC_DIRS = [
    os.path.join(ROOT, 'app/src/main/kotlin'),
    os.path.join(ROOT, 'data/src/main/kotlin'),
    os.path.join(ROOT, 'core/src/main/kotlin'),
]

DECL = re.compile(
    r'^\s*(?:@\w+(?:\([^)]*\))?\s*)*'
    r'(?:public\s+|internal\s+|private\s+|abstract\s+|open\s+|sealed\s+|data\s+|enum\s+|annotation\s+|value\s+|const\s+|lateinit\s+|suspend\s+|inline\s+|external\s+)*'
    r'(?:class|interface|object|fun|val|var|typealias)\s+'
    r'(?:<[^>]+>\s*)?'
    r'(?:[\w.]+\.)?'          # receiver برای extension
    r'(\w+)', re.M)

# پکیج → مجموعه‌ی نمادهای اعلام‌شده
pkg_syms = defaultdict(set)
all_files = []
for d in SRC_DIRS:
    all_files += glob.glob(os.path.join(d, '**/*.kt'), recursive=True)

for path in all_files:
    src = open(path, encoding='utf-8').read()
    m = re.search(r'^package (\S+)', src, re.M)
    if not m:
        continue
    pkg = m.group(1)
    body = re.sub(r'/\*.*?\*/', '', src, flags=re.S)
    body = re.sub(r'//.*', '', body)
    for name in DECL.findall(body):
        pkg_syms[pkg].add(name)
    # enum entryها هم ایمپورت‌شدنی‌ان
    for em in re.finditer(r'enum class (\w+)\s*(?:\([^)]*\))?\s*\{([^}]*)', body, re.S):
        pkg_syms[pkg].add(em.group(1))

broken = []
for path in all_files:
    src = open(path, encoding='utf-8').read()
    rel = os.path.relpath(path, ROOT)
    for imp in re.findall(r'^import (ir\.sadteam\.loancalc\.[\w.]+)', src, re.M):
        pkg, _, sym = imp.rpartition('.')
        if sym in ('R', 'BuildConfig'):
            continue                      # کلاس‌های تولیدشده‌ی Gradle - رو دیسک نیستن
        if pkg not in pkg_syms:
            continue                      # پکیجِ ناشناخته (مثلاً ماژولِ دیگه) - رد
        if sym not in pkg_syms[pkg]:
            # ممکنه عضوِ enum/companion باشه: `pkg.Enum.ENTRY`
            outer_pkg, _, outer = pkg.rpartition('.')
            if outer_pkg in pkg_syms and outer in pkg_syms[outer_pkg]:
                continue
            broken.append((rel, imp))

if broken:
    print(f'❌ {len(broken)} ایمپورتِ شکسته:\n')
    for rel, imp in broken:
        print(f'  {rel}\n     {imp}')
    sys.exit(1)
print(f'✅ همه‌ی {sum(len(v) for v in pkg_syms.values())} نمادِ داخلی سالم‌ان ({len(all_files)} فایل)')
