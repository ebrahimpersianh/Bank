#!/usr/bin/env python3
"""بررسیِ APIهای آزمایشیِ متریال بدونِ @OptIn - از شکستِ بیلدِ ۵۴۵.

`ModalBottomSheet` و چند API دیگر `@ExperimentalMaterial3Api`ند: بی `@OptIn` کامپایل
نمی‌شوند (پروژه `-Werror`گونه رفتار می‌کند و خطا می‌دهد نه هشدار). بسته‌ی طراح این
انوتیشن را همراه خودش نمی‌آورد، پس هر بار که یک شیتِ تازه می‌آید همین دام هست.
"""
import re, sys, pathlib

MARKERS = ("ModalBottomSheet", "rememberModalBottomSheetState", "SheetState(")
ROOT = pathlib.Path(__file__).resolve().parents[2]
bad = []
for f in ROOT.rglob("*.kt"):
    if "/build/" in str(f):
        continue
    src = f.read_text(encoding="utf-8", errors="ignore")
    # خطوطِ کامنت به حساب نمی‌آیند - `ShortcutDrawer` فقط در توضیح اسمش را برده.
    code = "\n".join(l for l in src.splitlines() if not l.lstrip().startswith(("*", "//", "/*")))
    if any(m in code for m in MARKERS) and "ExperimentalMaterial3Api" not in src:
        bad.append(f.relative_to(ROOT))

if bad:
    print("❌ APIِ آزمایشیِ متریال بدونِ @OptIn(ExperimentalMaterial3Api::class):")
    for f in bad:
        print(f"   {f}")
    sys.exit(1)
print("✅ هر جا APIِ آزمایشیِ متریال هست @OptIn هم دارد")
