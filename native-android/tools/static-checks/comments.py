#!/usr/bin/env python3
"""دو خطای متنیِ ساده که کامپایلر می‌گیره ولی بررسیِ ایمپورت‌محور نه:

۱. دنباله‌ی `**/` داخلِ کامنت - کامنت رو **زودتر از موعد می‌بنده**.
۲. `@Composable` که به‌جای تابع رو یه `val` نشسته (خطای «annotation is not applicable»).

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

# ۲ · @Composable رو یه پراپرتی (نه تابع)
for f in root.rglob("*.kt"):
    lines = f.read_text(encoding="utf-8").splitlines()
    for n, line in enumerate(lines):
        if line.strip() != "@Composable":
            continue
        # اولین خطِ بعدی که کامنت/انوتیشن نیست
        for nxt in lines[n + 1:]:
            t = nxt.strip()
            if not t or t.startswith(("//", "*", "/*", "@")):
                continue
            if " val " in f" {t} " or " var " in f" {t} ":
                bad.append(f"  {f.relative_to(root)}:{n + 1}\n     @Composable رو پراپرتی: {t}")
            break

if bad:
    print(f"❌ {len(bad)} مشکلِ متنیِ کامنت/انوتیشن:\n" + "\n".join(bad))
    sys.exit(1)
print("✅ هیچ کامنتِ زودبسته یا @Composableِ بدجا پیدا نشد")
