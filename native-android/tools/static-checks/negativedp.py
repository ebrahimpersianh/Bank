#!/usr/bin/env python3
"""مقدارِ **منفی** جایی که Compose قبول نمی‌کنه.

`Modifier.padding` عددِ منفی رو با `IllegalArgumentException: Padding must be non-negative`
رد می‌کنه - و چون تو مرحله‌ی **رسم** پرت می‌شه نه کامپایل، هیچ‌کدوم از بررسی‌های دیگه
نمی‌گیرنش و اپ فقط رو گوشیِ کاربر می‌ترکه (کرشِ نسخه‌ی ۱.۰.۴۷۷: هر تبی که باز می‌شد).

اگه می‌خوای یه المان رو بکشی بالا/کنار، `Modifier.offset` منفی قبول می‌کنه.
`size`/`height`/`width`/`spacedBy` هم منفی نمی‌گیرن.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
SRC = [ROOT / "app/src/main/kotlin", ROOT / "data/src/main/kotlin", ROOT / "core/src/main/kotlin"]
# تابع → آیا منفی مجازه؟
BANNED = ("padding", "size", "height", "width", "spacedBy", "requiredSize")
NEG = re.compile(r'\b(%s)\s*\([^)]*?\(\s*-\s*[\d.]+\s*\)\s*\.dp' % "|".join(BANNED))
NEG2 = re.compile(r'\b(%s)\s*\([^)]*?=\s*-[\d.]+\s*\.dp' % "|".join(BANNED))

bad = []
for d in SRC:
    for p in d.rglob("*.kt"):
        for n, line in enumerate(p.read_text(encoding="utf-8").splitlines(), 1):
            if NEG.search(line) or NEG2.search(line):
                bad.append(f"  {p.relative_to(ROOT)}:{n}\n     {line.strip()}")

if bad:
    print(f"❌ {len(bad)} مقدارِ منفیِ غیرمجاز (به‌جاش offset استفاده کن):\n" + "\n".join(bad))
    sys.exit(1)
print("✅ هیچ padding/size منفی‌ای پیدا نشد")
