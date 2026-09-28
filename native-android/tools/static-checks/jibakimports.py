#!/usr/bin/env python3
"""اکستنشن/تابعِ `ui.jibak` که بی ایمپورت صدا زده شده - از شکستِ بیلدهای ۵۴۷ و ۵۴۸.

`verify.py` فهرستِ ثابتی از نمادهای Compose دارد و `localcalls.py` فقط نامِ **بزرگ‌حرف**
را می‌گیرد، پس `x.toFaMoney()` و `rialToToman(x)`ِ بی‌ایمپورت از هر دو رد می‌شدند - دو
بیلدِ پیاپی سرِ همین رفت.
"""
import re
import sys
import pathlib

ROOT = pathlib.Path(__file__).resolve().parents[2]
APP = ROOT / "app/src/main/kotlin/ir/sadteam/loancalc"
JIBAK = APP / "ui/jibak"

# نامِ همه‌ی توابعِ سطحِ‌بالای پکیجِ jibak (چه اکستنشن چه معمولی).
DECL = re.compile(r"^fun\s+(?:[\w.<>]+\.)?(\w+)\s*\(")
names: dict[str, str] = {}
for f in JIBAK.rglob("*.kt"):
    for line in f.read_text(encoding="utf-8").splitlines():
        m = DECL.match(line)
        if m:
            names[m.group(1)] = f.name

bad = []
for f in APP.rglob("*.kt"):
    if JIBAK in f.parents:
        continue
    raw = f.read_text(encoding="utf-8")
    # خطِ کامنت به حساب نمی‌آید - توضیحِ «toFa برداشته شد» فراخوانی نیست.
    src = "\n".join(l for l in raw.splitlines() if not l.lstrip().startswith(("*", "//", "/*")))
    src_with_imports = raw
    for name in names:
        # هم `name(` و هم `.name(` - گیرنده می‌تواند `)` هم باشد
        # (`rialToToman(x).toFaMoney()`)، پس الگو نباید دنبالِ نامِ متغیر بگردد.
        if not re.search(r"(?<!\w)\.?%s\s*\(" % name, src):
            continue
        # هر ایمپورتی به همان نام کافی است: `toFa` هم در `:core` هست هم در `jibak`
        # (اولی سراسری، دومی اکستنشن) و فایل ممکن است عمداً آنِ دیگری را بخواهد.
        if re.search(r"^import [\w.]+\.%s$" % name, src_with_imports, re.M):
            continue
        if re.search(r"^(?:private )?fun\s+(?:[\w.<>]+\.)?%s\s*\(" % name, src, re.M):
            continue
        bad.append(f"  {f.relative_to(ROOT)}\n     {name}(…) → import ir.sadteam.loancalc.ui.jibak.{name}")

if bad:
    print(f"❌ {len(bad)} فراخوانیِ `ui.jibak` بدونِ ایمپورت:")
    print("\n".join(sorted(set(bad))))
    sys.exit(1)
print(f"✅ همه‌ی فراخوانی‌های `ui.jibak` ایمپورت دارن ({len(names)} تابع)")
