#!/usr/bin/env python3
"""بررسیِ چهاردهم — سبکِ صداکردنِ اکستنشن در برابرِ تابعِ سراسری.

سه بیلد (۴۹۱، ۵۰۳، ۵۰۷) دقیقاً با همین خطا شکستن. علتش این است که پروژه **دو**
`toFa` دارد:

    ir.sadteam.loancalc.core.toFa(value: Any): String   ← تابعِ سراسری  →  toFa(x)
    ir.sadteam.loancalc.ui.jibak.Long.toFa(): String    ← اکستنشن       →  x.toFa()

فایلی که ایمپورتِ jibak را دارد ولی هنوز `toFa(x)` صدا می‌زند کامپایل نمی‌شود، و
هیچ‌کدام از سیزده بررسیِ قبلی نمی‌گیردش چون نماد **ایمپورت شده و املایش درست است**.

قاعده: هر نامی که در `ui/jibak/JibakFormat.kt` **اکستنشن** تعریف شده، در فایلی که
همان نام را از پکیجِ jibak ایمپورت کرده باید با نقطه صدا زده شود.
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / "app/src/main/kotlin/ir/sadteam/loancalc"
JIBAK = SRC / "ui/jibak/JibakFormat.kt"
PKG = "ir.sadteam.loancalc.ui.jibak"


def extension_names(path: Path) -> set:
    """نام‌هایی که با گیرنده تعریف شده‌ان: `fun String.faDigits()` → faDigits."""
    text = path.read_text(encoding="utf-8")
    return set(re.findall(r"^\s*(?:private\s+|internal\s+)?fun\s+[\w.<>?]+\.(\w+)\s*\(", text, re.M))


def main() -> int:
    if not JIBAK.exists():
        print(f"⚠️ {JIBAK} پیدا نشد - بررسی رد شد.")
        return 0
    exts = extension_names(JIBAK)
    problems = []
    files = list(SRC.rglob("*.kt"))
    for f in files:
        text = f.read_text(encoding="utf-8")
        imported = set(re.findall(rf"^import {re.escape(PKG)}\.(\w+)$", text, re.M))
        targets = imported & exts
        if not targets:
            continue
        for i, line in enumerate(text.splitlines(), 1):
            if line.lstrip().startswith(("import ", "*", "//")):
                continue
            for name in targets:
                if re.search(rf"(?<![.\w]){re.escape(name)}\s*\(", line):
                    problems.append((f.relative_to(ROOT), i, name, line.strip()))

    if problems:
        print(f"❌ {len(problems)} بار اکستنشن مثلِ تابعِ سراسری صدا زده شده:")
        for path, line_no, name, src in problems:
            print(f"  {path}:{line_no}  →  «{name}(...)» باید «(...).{name}()» باشه")
            print(f"      {src}")
        return 1
    print(f"✅ همه‌ی اکستنشن‌های jibak با نقطه صدا زده شده‌ان ({len(exts)} اکستنشن، {len(files)} فایل)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
