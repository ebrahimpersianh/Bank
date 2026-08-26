#!/usr/bin/env python3
"""نمادِ **خودِ پروژه** که استفاده شده ولی ایمپورت نشده.

`verify.py` فقط یه لیستِ ثابت از نمادهای Compose رو می‌شناسه، پس وقتی یه Composableِ خودمون
(مثلِ `AccountsScreen`) از پکیجِ دیگه‌ای صدا زده می‌شه و ایمپورتش جا می‌مونه، هیچ‌کدوم از
بررسی‌های قبلی نمی‌گیرنش - بیلدِ ۴۷۵ دقیقاً همین‌جا شکست.

روش: از رو کلِ ماژول‌ها یه فهرستِ «نامِ سطحِ‌بالا → پکیج» ساخته می‌شه؛ بعد تو هر فایل هر
`Name(` که تو همون فایل/پکیج تعریف نشده و ایمپورتم نشده گزارش می‌شه. نامی که تو چند پکیج
تعریف شده عمداً رد می‌شه (نمی‌شه مطمئن بود کدومه).
"""
import collections
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
SRC = [ROOT / "app/src/main/kotlin", ROOT / "data/src/main/kotlin", ROOT / "core/src/main/kotlin"]
MODS = r'(?:public |private |internal |abstract |sealed |open |data |enum |value )*'
# فقط تعریفِ **بی‌گیرنده** وارد فهرست می‌شه: `fun List<T>.foo()` یا `fun RowScope.Tile()` اسمِ
# تابع نیستن، گیرنده‌ان - وگرنه `List` و `RowScope` به‌عنوانِ نمادِ پروژه ایندکس می‌شدن.
DECL = re.compile(r'^\s*(?:@\w+\s+)*' + MODS + r'(?:fun\s+([A-Z]\w*)\s*\(|(?:class|object|interface)\s+([A-Z]\w*))', re.M)
# ولی برای «تو همین فایل تعریف شده؟» هر دو شکل حساب می‌شن.
LOCAL = re.compile(r'^\s*(?:@\w+\s+)*' + MODS + r'(?:fun\s+(?:[\w.<>, ?]+\.)?([A-Z]\w*)\s*\(|(?:class|object|interface)\s+([A-Z]\w*))', re.M)
# فلیورها/ویجت: پکیج‌بندیِ متفاوت یا نسخه‌ی جدا به‌ازای هر فلیور
SKIP = ("/widget/", "/src/myket/", "/src/cafebazaar/")

files = [p for d in SRC for p in d.rglob("*.kt") if not any(s in str(p) for s in SKIP)]
index = collections.defaultdict(set)
own = {}
for p in files:
    src = p.read_text(encoding="utf-8")
    pkg = re.search(r'^package (\S+)', src, re.M)
    pkg = pkg.group(1) if pkg else ""
    def pick(rx):
        return {a or b for a, b in rx.findall(src)} - {""}
    own[p] = (pkg, pick(LOCAL), src)
    for n in pick(DECL):
        index[n].add(pkg)

bad = []
for p, (pkg, names, src) in own.items():
    imports = set(re.findall(r'^import (\S+)', src, re.M))
    imported = {i.rsplit(".", 1)[-1] for i in imports}
    star = {i[:-2] for i in imports if i.endswith(".*")}
    body = re.sub(r'^import .*$', '', src, flags=re.M)
    body = re.sub(r'//.*', '', body)
    body = re.sub(r'/\*.*?\*/', '', body, flags=re.S)
    for name in sorted(set(re.findall(r'(?<![\w.])([A-Z]\w*)\s*\(', body))):
        pkgs = index.get(name)
        if not pkgs or len(pkgs) != 1:
            continue                       # نامِ ناشناخته یا تکراری - قابلِ اتکا نیست
        home = next(iter(pkgs))
        if home == pkg or name in names or name in imported or home in star:
            continue
        bad.append(f"  {p.relative_to(ROOT)}\n     {name} → import {home}.{name}")

if bad:
    print(f"❌ {len(bad)} نمادِ پروژه بدونِ ایمپورت:\n" + "\n".join(bad))
    sys.exit(1)
print(f"✅ همه‌ی نمادهای داخلیِ استفاده‌شده ایمپورت شده‌ان ({len(files)} فایل)")
