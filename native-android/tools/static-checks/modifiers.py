"""
دو کلاس خطای کامپایل که بررسیِ ایمپورت‌محور نمی‌گیره - هر دو بیلدِ ۴۶۸ رو شکستن.

۱. **آرگومان‌های ناسازگارِ `padding`** - کامپوز چهار اورلود داره و هیچ‌کدوم
   `horizontal`/`vertical` رو با `start`/`end`/`top`/`bottom` قاطی نمی‌کنه.
   `padding(horizontal = 6.dp, bottom = 11.dp)` کامپایل نمی‌شه.

۲. **اکستنشنِ `Modifier` بدونِ ایمپورت** - مثلاً `.height(1.dp)` وقتی
   `androidx.compose.foundation.layout.height` ایمپورت نشده. `verify.py` این رو نمی‌گرفت
   چون `height` به‌عنوانِ پراپرتیِ `size.height` هم تو همون فایل دیده می‌شد.
"""
import re, glob, os, sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..')

# اکستنشن‌های پرمصرفِ Modifier و ایمپورتِ لازمشون
MODIFIER_IMPORTS = {
    'height': 'androidx.compose.foundation.layout.height',
    'width': 'androidx.compose.foundation.layout.width',
    'size': 'androidx.compose.foundation.layout.size',
    'padding': 'androidx.compose.foundation.layout.padding',
    'fillMaxWidth': 'androidx.compose.foundation.layout.fillMaxWidth',
    'fillMaxHeight': 'androidx.compose.foundation.layout.fillMaxHeight',
    'fillMaxSize': 'androidx.compose.foundation.layout.fillMaxSize',
    'offset': 'androidx.compose.foundation.layout.offset',
    'background': 'androidx.compose.foundation.background',
    'border': 'androidx.compose.foundation.border',
    'clickable': 'androidx.compose.foundation.clickable',
    'heightIn': 'androidx.compose.foundation.layout.heightIn',
    'widthIn': 'androidx.compose.foundation.layout.widthIn',
    'sizeIn': 'androidx.compose.foundation.layout.sizeIn',
    'defaultMinSize': 'androidx.compose.foundation.layout.defaultMinSize',
    'clip': 'androidx.compose.ui.draw.clip',
    'alpha': 'androidx.compose.ui.draw.alpha',
    'rotate': 'androidx.compose.ui.draw.rotate',
    'drawBehind': 'androidx.compose.ui.draw.drawBehind',
    'graphicsLayer': 'androidx.compose.ui.graphics.graphicsLayer',
    'weight': None,   # فقط داخلِ RowScope/ColumnScope - ایمپورت لازم نداره
}

problems = []
for path in glob.glob(os.path.join(ROOT, '**/*.kt'), recursive=True):
    if '/build/' in path:
        continue
    # ویجت رو Glance ساخته شده - `GlanceModifier` اکستنشن‌های خودش رو داره، نه کامپوزِ معمولی.
    if 'ui/widget/' in path.replace(os.sep, '/'):
        continue
    rel = os.path.relpath(path, ROOT)
    src = open(path, encoding='utf-8').read()
    body = re.sub(r'//.*', '', src)
    body = re.sub(r'/\*.*?\*/', '', body, flags=re.S)

    # ۱. padding با آرگومانِ قاطی
    for m in re.finditer(r'padding\(([^()]*(?:\([^()]*\)[^()]*)*)\)', body):
        args = m.group(1)
        axis = re.search(r'\b(horizontal|vertical)\s*=', args)
        side = re.search(r'\b(start|end|top|bottom)\s*=', args)
        if axis and side:
            line = body[:m.start()].count('\n') + 1
            problems.append((rel, line, f'padding({args.strip()[:60]}) - {axis.group(1)} با {side.group(1)} قاطی شده'))

    # ۲. اکستنشنِ Modifier بدونِ ایمپورت - فقط جایی که واقعاً زنجیره‌ی Modifierه
    #    (`Modifier.x(` یا خطی که با `.x(` شروع می‌شه)
    for name, imp in MODIFIER_IMPORTS.items():
        if imp is None or imp in src:
            continue
        pattern = re.compile(r'(?:\bModifier\s*\.\s*%s\(|^\s*\.\s*%s\()' % (name, name), re.M)
        m = pattern.search(body)
        if m:
            line = body[:m.start()].count('\n') + 1
            problems.append((rel, line, f'Modifier.{name}() بدونِ ایمپورتِ {imp}'))

if problems:
    print(f'⚠️ {len(problems)} مشکلِ Modifier:\n')
    for rel, line, what in problems:
        print(f'  {rel}:{line}  {what}')
    sys.exit(1)
print('✅ هیچ مشکلِ Modifier ای پیدا نشد')
