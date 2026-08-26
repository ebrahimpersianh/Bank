#!/usr/bin/env python3
"""دنباله‌ی `**/` داخلِ کامنت - کامنت رو **زودتر از موعد می‌بنده**.

تو متنِ فارسیِ توضیحاتِ فریم زیاد پیش میاد چیزی مثلِ «عدد **۲۷**/۹۰۰» نوشته بشه؛ همون `**` +
`/` از دیدِ کاتلین یعنی `*/` و بقیه‌ی کامنت کدِ آزاد حساب می‌شه (بیلدِ ۴۷۲ دقیقاً همین‌جا شکست).
"""
import pathlib
import sys

root = pathlib.Path(__file__).resolve().parents[2]
bad = []
for f in root.rglob("*.kt"):
    for n, line in enumerate(f.read_text(encoding="utf-8").splitlines(), 1):
        stripped = line.lstrip()
        if not (stripped.startswith("*") or stripped.startswith("//") or "/**" in line):
            continue
        if "**/" in line and not stripped.endswith("**/"):
            bad.append(f"  {f.relative_to(root)}:{n}\n     {line.strip()}")

if bad:
    print(f"❌ {len(bad)} کامنتِ زودبسته‌شده (`**/`):\n" + "\n".join(bad))
    sys.exit(1)
print("✅ هیچ کامنتی زودتر بسته نشده")
