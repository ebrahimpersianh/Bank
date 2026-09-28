#!/usr/bin/env python3
"""بررسیِ شانزدهم — اعلانِ تکراری در یک پکیج.

بیلدِ ۵۱۲ با همین شکست: بسته‌ی طراح دو فایل داشت (`CoinEconomy.kt` و `CoinRules.kt`)
که هر دو `CoinSpend`، `CoinEvent` و `BuyResult` را در **یک پکیج** تعریف می‌کردند.
هر دو کپی شدند و کامپایلر ۴۰ خطای `Redeclaration` داد.

هیچ‌کدام از پانزده بررسیِ قبلی این را نمی‌گیرد: هر دو فایل به‌تنهایی درست‌اند، ایمپورت‌ها
سالم‌اند، و املا بی‌ایراد است — تضاد فقط وقتی دیده می‌شود که کنارِ هم گذاشته شوند.
دقیقاً همان چیزی که با گرفتنِ بسته از بیرون زیاد پیش می‌آید.

فقط **نوع**ها بررسی می‌شوند (class/interface/object/enum). تابعِ سطحِ بالا عمداً بیرون
است چون سربارگذاری (overload) در کاتلین مجاز است و دو `fun` هم‌نام لزوماً خطا نیست.
"""
import re
import sys
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]

DECL = re.compile(
    r"^(?:@\w+(?:\([^)]*\))?\s+)*"
    r"(?:public\s+|internal\s+|private\s+|abstract\s+|open\s+|sealed\s+|data\s+|value\s+|annotation\s+)*"
    r"(class|interface|object|enum\s+class)\s+(\w+)",
    re.M,
)


def main() -> int:
    seen = defaultdict(list)
    files = [
        p for p in ROOT.rglob("*.kt")
        if "/build/" not in p.as_posix() and "/design/" not in p.as_posix()
    ]
    for f in files:
        text = f.read_text(encoding="utf-8")
        m = re.search(r"^package\s+([\w.]+)", text, re.M)
        pkg = m.group(1) if m else "<root>"
        # فقط اعلانِ سطحِ بالا: بدونِ تورفتگی. نوعِ تودرتو تضاد نمی‌سازد.
        # فلیورها هم‌زمان کامپایل نمی‌شوند: `src/cafebazaar` و `src/myket` هر کدام یک
        # `SubscriptionManager` دارند و این **درست** است. پس نامِ سورس‌ست بخشی از کلید است.
        parts = f.as_posix().split("/src/")
        source_set = parts[1].split("/")[0] if len(parts) > 1 else "main"
        for line in text.splitlines():
            if line[:1].isspace():
                continue
            d = DECL.match(line)
            if d:
                seen[(source_set, pkg, d.group(2))].append(f.relative_to(ROOT))

    dupes = {k: v for k, v in seen.items() if len({str(x) for x in v}) > 1}
    if dupes:
        print(f"❌ {len(dupes)} نوع در یک پکیج دو بار اعلان شده:")
        for (source_set, pkg, name), paths in dupes.items():
            print(f"  «{name}» در پکیجِ {pkg}:")
            for p in sorted({str(x) for x in paths}):
                print(f"      {p}")
        return 1
    print(f"✅ هیچ نوعی در یک پکیج دوباره اعلان نشده ({len(files)} فایل)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
