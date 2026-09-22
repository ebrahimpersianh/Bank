"""
بررسیِ ایستای ایمپورت برای فایل‌های Kotlin/Compose.

سندباکس نمی‌تونه `native-android` رو بیلد کنه (dl.google.com مسدوده)، پس این حداقلِ تضمین قبل از
مصرف‌کردنِ سهمیه‌ی CIه: هر نمادِ شناخته‌شده‌ای که تو فایل استفاده شده ولی نه ایمپورت شده، نه
هم‌پکیجه و نه از ایمپورت‌های خودکارِ Kotlin میاد → گزارش می‌شه.

عمداً محافظه‌کاره: فقط نمادهایی که نگاشتِ قطعی دارن چک می‌شن، تا هشدارِ کاذب نده.
"""
import re, os, glob, sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                    '..', '..', 'app/src/main/kotlin/ir/sadteam/loancalc')

# نمادهایی که حتماً باید ایمپورتِ صریح داشته باشن (نگاشت: نماد → ایمپورتِ درست)
NEEDS = {
    'Color': 'androidx.compose.ui.graphics.Color',
    'Brush': 'androidx.compose.ui.graphics.Brush',
    'Modifier': 'androidx.compose.ui.Modifier',
    'Alignment': 'androidx.compose.ui.Alignment',
    'FontWeight': 'androidx.compose.ui.text.font.FontWeight',
    'CircleShape': 'androidx.compose.foundation.shape.CircleShape',
    'RoundedCornerShape': 'androidx.compose.foundation.shape.RoundedCornerShape',
    'Arrangement': 'androidx.compose.foundation.layout.Arrangement',
    'PaddingValues': 'androidx.compose.foundation.layout.PaddingValues',
    'MutableInteractionSource': 'androidx.compose.foundation.interaction.MutableInteractionSource',
    'LocalContentColor': 'androidx.compose.material3.LocalContentColor',
    'LocalTextStyle': 'androidx.compose.material3.LocalTextStyle',
    'CompositionLocalProvider': 'androidx.compose.runtime.CompositionLocalProvider',
    # چیدمان‌های پایه - یه‌بار `Spacer` بی‌ایمپورت از این بررسی رد شد و بیلد رو شکست.
    'BasicTextField': 'androidx.compose.foundation.text.BasicTextField',
    'SolidColor': 'androidx.compose.ui.graphics.SolidColor',
    'TextAlign': 'androidx.compose.ui.text.style.TextAlign',
    'TextStyle': 'androidx.compose.ui.text.TextStyle',
    'KeyboardOptions': 'androidx.compose.foundation.text.KeyboardOptions',
    'Spacer': 'androidx.compose.foundation.layout.Spacer',
    'Box': 'androidx.compose.foundation.layout.Box',
    'Row': 'androidx.compose.foundation.layout.Row',
    'Column': 'androidx.compose.foundation.layout.Column',
    # هر سه‌تای زیر تو بیلدِ ۴۹۳ بی‌ایمپورت مونده بودن.
    'rememberSaveable': 'androidx.compose.runtime.saveable.rememberSaveable',
    'AlertDialog': 'androidx.compose.material3.AlertDialog',
    'ModalBottomSheet': 'androidx.compose.material3.ModalBottomSheet',
    # موقعِ ساختِ انتخابگرِ اپ (رفعِ باگِ خواندنِ اعلان) هر چهارتا بی‌ایمپورت مونده بودن.
    'produceState': 'androidx.compose.runtime.produceState',
    # بیلدِ ۶۱۲: `LaunchedEffect` در این فهرست نبود، پس ایمپورتِ جاماند‌ه‌اش تا CI دیده نشد -
    # و چون تابعِ suspend داخلش بود، خطای بعدی «suspend function should be called from a
    # coroutine» شد که اصلاً علت نبود.
    'LaunchedEffect': 'androidx.compose.runtime.LaunchedEffect',
    'DisposableEffect': 'androidx.compose.runtime.DisposableEffect',
    'SideEffect': 'androidx.compose.runtime.SideEffect',
    'collectAsState': 'androidx.compose.runtime.collectAsState',
    'withContext': 'kotlinx.coroutines.withContext',
    'Dispatchers': 'kotlinx.coroutines.Dispatchers',
}
# Modifierهای زنجیره‌ای: `.name(` باید ایمپورتِ خودش رو داشته باشه
MODIFIERS = {
    'heightIn': 'androidx.compose.foundation.layout.heightIn',
    'widthIn': 'androidx.compose.foundation.layout.widthIn',
    'sizeIn': 'androidx.compose.foundation.layout.sizeIn',
    'defaultMinSize': 'androidx.compose.foundation.layout.defaultMinSize',
    'clip': 'androidx.compose.ui.draw.clip',
    'alpha': 'androidx.compose.ui.draw.alpha',
    'drawBehind': 'androidx.compose.ui.draw.drawBehind',
    'background': 'androidx.compose.foundation.background',
    'border': 'androidx.compose.foundation.border',
    'clickable': 'androidx.compose.foundation.clickable',
    'offset': 'androidx.compose.foundation.layout.offset',
    'padding': 'androidx.compose.foundation.layout.padding',
    'size': 'androidx.compose.foundation.layout.size',
    'fillMaxWidth': 'androidx.compose.foundation.layout.fillMaxWidth',
    'fillMaxSize': 'androidx.compose.foundation.layout.fillMaxSize',
    'fillMaxHeight': 'androidx.compose.foundation.layout.fillMaxHeight',
    'defaultMinSize': 'androidx.compose.foundation.layout.defaultMinSize',
    'navigationBarsPadding': 'androidx.compose.foundation.layout.navigationBarsPadding',
    'weight': None,          # RowScope/ColumnScope - ایمپورت لازم نداره
    'hardShadow': 'ir.sadteam.loancalc.ui.theme.hardShadow',
    'collectIsPressedAsState': 'androidx.compose.foundation.interaction.collectIsPressedAsState',
}

# فایل‌هایی که این بررسی روشون هشدارِ کاذب می‌ده و **عمداً** رد می‌شن:
#  - ویجت (`ui/widget/`): از Glance استفاده می‌کنه نه Compose، پس `background`/`padding`/... مالِ
#    پکیجِ `androidx.glance.*`ه نه `androidx.compose.*`.
#  - خروجی‌گیرِ PDF: `Alignment` اونجا `android.graphics.Paint.Alignment`ه.
#  - بقیه: نمادهایی که به‌شکلِ کاملاً‌واجد (fully-qualified) نوشته شدن.
SKIP = (
    'ui/widget/',
    'PdfExporter.kt',
)

targets = sys.argv[1:] or sorted(
    f for f in glob.glob(os.path.join(ROOT, '**/*.kt'), recursive=True)
    if not any(sk in f for sk in SKIP)
)
problems = []

for path in targets:
    if not path.startswith('/'):
        path = os.path.join(ROOT, path)
    src = open(path, encoding='utf-8').read()
    rel = os.path.relpath(path, ROOT)
    pkg = re.search(r'^package (\S+)', src, re.M)
    pkg = pkg.group(1) if pkg else ''
    imports = set(re.findall(r'^import (\S+)', src, re.M))
    body = re.sub(r'^import .*$', '', src, flags=re.M)
    body = re.sub(r'//.*', '', body)
    body = re.sub(r'/\*.*?\*/', '', body, flags=re.S)

    for sym, imp in NEEDS.items():
        if not re.search(r'\b%s\b' % sym, body) or imp in imports:
            continue
        if imp.rsplit('.', 1)[0] == pkg:
            continue                       # هم‌پکیج - ایمپورت لازم نداره
        # نامِ کاملاً‌واجد (`androidx.compose.ui.graphics.Color(...)`) هم ایمپورت لازم نداره.
        # اگه **هر** استفاده‌ای از این نماد پیشوندِ پکیجش رو داره، هشدار نده.
        bare = re.findall(r'(?<![.\w])%s\b' % sym, body)
        if not bare:
            continue
        problems.append((rel, sym, imp))

    for sym, imp in MODIFIERS.items():
        if imp is None:
            continue
        if imp in imports or imp.rsplit('.', 1)[0] == pkg:
            continue
        # فقط زنجیره‌ی Modifier مهمه - `Motion.offset()` یا `Foo.padding()` گیرنده‌ی دیگه‌ای
        # دارن و ایمپورتِ Compose لازم ندارن. الگو: یا اولِ خط بعدِ فاصله (زنجیره‌ی چندخطی)،
        # یا بلافاصله بعدِ `Modifier`/`)`/`}` (ادامه‌ی زنجیره).
        chained = re.search(r'(?:Modifier|\)|\}|^\s*)\s*\.%s\s*\(' % sym, body, re.M)
        if chained:
            problems.append((rel, f'.{sym}()', imp))

if problems:
    print(f'❌ {len(problems)} ایمپورتِ احتمالاً گمشده:\n')
    cur = None
    for rel, sym, imp in problems:
        if rel != cur:
            print(f'  {rel}')
            cur = rel
        print(f'     {sym:<28} → import {imp}')
    sys.exit(1)
else:
    print(f'✅ هیچ ایمپورتِ گمشده‌ای پیدا نشد ({len(targets)} فایل بررسی شد)')
