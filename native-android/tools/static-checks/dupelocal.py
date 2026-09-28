#!/usr/bin/env python3
"""بررسیِ «اعلانِ محلیِ تکراری» - از شکستِ بیلدِ ۵۵۳.

`MyLoansScreen` دو `overdueCount` داشت: یکی تعدادِ قسطِ معوق (تازه، بالای تابع) و
یکی تعدادِ وامِ عقب‌افتاده (قدیمی، چند ده خط پایین‌تر). کاتلین این را
«Conflicting declarations» می‌دهد و بیلد می‌شکند.

الگو عمداً **محافظه‌کارانه** است: فقط `val`/`var`ِ سطحِ اولِ بدنه‌ی یک تابع
(تورفتگیِ چهار) شمرده می‌شوند. اعلان‌های داخلِ لامبدا و بلوکِ تودرتو اسکوپِ خودشان
را دارند و سایه‌انداختن آن‌جا قانونی است، پس دست نمی‌خورند.
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
FUN = re.compile(r"^(?:@\w+\s*)*(?:private |internal |public )?(?:suspend )?fun\s")
DECL = re.compile(r"^    (?:val|var)\s+(\w+)\b")

problems = []
for path in ROOT.rglob("*.kt"):
    if "/build/" in str(path):
        continue
    seen: dict[str, int] = {}
    in_fun = False
    for n, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        # هر خطِ ستونِ صفر یک اعلانِ سطحِ بالا را باز یا بسته می‌کند، پس اسکوپ عوض
        # می‌شود. فقط داخلِ **تابع** می‌شماریم: بدنه‌ی `data class` هم تورفتگیِ چهار
        # دارد ولی ردیف‌هایش پراپرتی‌اند نه متغیرِ محلی (۱۶ مثبتِ کاذبِ نسخه‌ی اول).
        if line and not line[0].isspace():
            seen = {}
            in_fun = bool(FUN.match(line))
            continue
        if not in_fun:
            continue
        m = DECL.match(line)
        if m:
            name = m.group(1)
            if name in seen:
                problems.append(
                    f"  {path.relative_to(ROOT)}:{n} «{name}» قبلاً در خط {seen[name]} اعلام شده"
                )
            else:
                seen[name] = n

if problems:
    print(f"❌ {len(problems)} اعلانِ محلیِ تکراری:")
    print("\n".join(problems))
    sys.exit(1)
print("✅ هیچ تابعی دو متغیرِ هم‌نام در یک اسکوپ ندارد")
