#!/usr/bin/env python3
"""smart-castِ غیرمجاز رو پراپرتیِ یه ماژولِ دیگه (`:data`).

الگوی `if (entity.prop != null) { ... entity.prop ... }` برای پراپرتیِ nullableی که تو
ماژولِ دیگه‌ای (`:data`) تعریف شده **کامپایل نمی‌شه**:

    Smart cast to 'kotlin.Any' is impossible, because 'x' is a public API property
    declared in different module

چون کاتلین تضمین نمی‌کنه اون پراپرتی بینِ دو خوانش عوض نشه. راهِ درست (الگوی خودِ پروژه،
رجوع کن به کامنتِ `ChequeDetailScreen.kt`): اول تو یه `val` محلی بریزش، بعد چکِ null.

شکستِ بیلدِ ۴۹۸ (ChequeBooksScreen: `book.last4`/`book.sayadId`) - هیچ‌کدوم از ۱۲ بررسیِ
دیگه نمی‌گرفتش چون نماد **ایمپورت‌شده و املاش درسته**، فقط کاتلین اجازه‌ی هوشمندسازیش رو نمی‌ده.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
APP = ROOT / "app/src/main/kotlin"
DATA = ROOT / "data/src/main/kotlin"

# نامِ پراپرتی‌های nullableی که تو `:data` تعریف شدن - فقط همون‌ها ریسکِ smart-cast دارن.
nullable_props: set[str] = set()
PROP = re.compile(r"^\s*(?:val|var)\s+(\w+)\s*:\s*[\w<>., ]+\?\s*(?:=|,|$)")
for p in DATA.rglob("*.kt"):
    for line in p.read_text(encoding="utf-8").splitlines():
        m = PROP.match(line)
        if m:
            nullable_props.add(m.group(1))

# `if (x.prop != null) {` … بدنه‌ای که دوباره `x.prop` رو صدا می‌زنه.
IF_NOT_NULL = re.compile(r"\bif\s*\(\s*(\w+)\.(\w+)\s*!=\s*null\s*\)\s*\{")

bad = []
for p in APP.rglob("*.kt"):
    lines = p.read_text(encoding="utf-8").splitlines()
    for n, line in enumerate(lines, 1):
        m = IF_NOT_NULL.search(line)
        if not m:
            continue
        receiver, prop = m.group(1), m.group(2)
        if prop not in nullable_props:
            continue
        # فقط مصرفی که **واقعاً** غیرnull می‌خواد: آرگومانِ تابع (`toFa(x.p)`) یا صداکردنِ
        # عضوی روش (`x.p.length`). مقایسه‌ی `x.p == …` نیازی به smart-cast نداره و
        # کامپایل می‌شه، پس هشدارِ کاذب نده.
        needs_nonnull = re.compile(
            r"(?:\w\s*\(\s*[^)]*\b%s\.%s\b(?!\s*[=!]=)|\b%s\.%s\s*\.)" % (receiver, prop, receiver, prop)
        )
        # بدنه‌ی بلوک: تا وقتی آکولادها متعادل بشن (سقفِ ۴۰ خط، بیشترش عملاً پیش نمیاد).
        depth = line.count("{") - line.count("}")
        for body in lines[n : n + 40]:
            if needs_nonnull.search(body):
                bad.append(
                    f"  {p.relative_to(ROOT)}:{n}\n"
                    f"     if ({receiver}.{prop} != null) … بعد دوباره {receiver}.{prop} صدا زده شده\n"
                    f"     → اول `val {prop} = {receiver}.{prop}` بذار و رو همون چک کن"
                )
                break
            depth += body.count("{") - body.count("}")
            if depth <= 0:
                break

# الگوی دوم (از شکستِ بیلدِ ۵۴۷): `when { x.p == null -> … ; x.p.startsWith(…) }`.
# حالتِ کلی‌ترش این است: هر جا `x.p.something` صدا زده می‌شود **و** همان فایل جای دیگری
# `x.p` را با null مقایسه کرده (یعنی می‌داند nullable است)، کامپایلر smart-cast نمی‌کند.
MEMBER_CALL = re.compile(r"(?<![?\w.])(\w+)\.(\w+)\.\w")
for p in APP.rglob("*.kt"):
    src = p.read_text(encoding="utf-8")
    for n, line in enumerate(src.splitlines(), 1):
        if line.lstrip().startswith(("*", "//", "/*")):
            continue
        for m in MEMBER_CALL.finditer(line):
            receiver, prop = m.group(1), m.group(2)
            if prop not in nullable_props:
                continue
            if f"{receiver}.{prop} == null" not in src and f"{receiver}.{prop} != null" not in src:
                continue
            entry = (
                f"  {p.relative_to(ROOT)}:{n}\n"
                f"     {receiver}.{prop}.… بی `?.` روی پراپرتیِ nullableی `:data`\n"
                f"     → اول `val {prop} = {receiver}.{prop}` بذار و رو همون کار کن"
            )
            if entry not in bad:
                bad.append(entry)

if bad:
    print(f"❌ {len(bad)} smart-castِ غیرمجاز رو پراپرتیِ `:data`:\n" + "\n".join(bad))
    sys.exit(1)

print(f"✅ هیچ smart-castِ غیرمجازی رو پراپرتیِ nullableی `:data` پیدا نشد ({len(nullable_props)} پراپرتی بررسی شد)")
