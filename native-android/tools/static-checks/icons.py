#!/usr/bin/env python3
"""آیکونِ متریال و اکستنشنِ kotlin.math که استفاده شده ولی ایمپورت نشده.

`Icons.Filled.Check` بدونِ `import androidx.compose.material.icons.filled.Check` کامپایل
نمی‌شه، ولی هیچ‌کدوم از بررسی‌های قبلی این الگو رو نمی‌دیدن: `verify.py` فقط یه لیستِ ثابت از
نمادها داره و `ownsymbols.py` فقط نمادهای **خودِ پروژه** رو می‌شناسه. این خلأ موقعِ پیاده‌سازیِ
فریمِ `27b` پیدا شد (دو آیکونِ `Check` و `PriorityHigh` بی‌ایمپورت مونده بودن).

نگاشت: `Icons.Filled.X` → `androidx.compose.material.icons.filled.X`
        `Icons.Outlined.X` → `...icons.outlined.X`
        `Icons.AutoMirrored.Filled.X` → `...icons.automirrored.filled.X`
"""
import pathlib
import re

def strip_noise(src):
    """کامنت و رشته خالی می‌شود - ارجاعِ داخلِ توضیح (مثلِ «Icons.Filled.ArrowBackِ قبلی»)
    کدِ اجراشدنی نیست و نباید هشدار بدهد."""
    src = re.sub(r'/\*.*?\*/', lambda m: '\n' * m.group(0).count('\n'), src, flags=re.S)
    src = re.sub(r'//[^\n]*', '', src)
    return src
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
SRC = [ROOT / "app/src/main/kotlin", ROOT / "data/src/main/kotlin"]

USE = re.compile(r'\bIcons\.(AutoMirrored\.Filled|AutoMirrored\.Outlined|Filled|Outlined|Rounded|Sharp|TwoTone)\.([A-Z]\w*)')

# اکستنشن‌های kotlin.math که ایمپورتِ صریح می‌خوان - `roundToInt` بی‌ایمپورت همون کلاس
# خطاییه که موقعِ فریمِ `27b` پیدا شد (کامپایلر نمی‌شناسدش ولی هیچ بررسیِ دیگه‌ای نمی‌گرفتش).
# فقط اونایی که تو kotlin.math‌ان و ایمپورتِ صریح می‌خوان (coerceIn/coerceAtLeast تو
# kotlin.ranges‌ان و خودکار ایمپورت می‌شن، پس اینجا نیستن).
MATH_NEEDS_IMPORT = {"roundToInt": "kotlin.math.roundToInt", "roundToLong": "kotlin.math.roundToLong"}


def package_for(group: str) -> str:
    return "androidx.compose.material.icons." + group.replace(".", "").lower() \
        if group.startswith("AutoMirrored") else "androidx.compose.material.icons." + group.lower()


def expected_import(group: str, name: str) -> str:
    if group == "AutoMirrored.Filled":
        pkg = "androidx.compose.material.icons.automirrored.filled"
    elif group == "AutoMirrored.Outlined":
        pkg = "androidx.compose.material.icons.automirrored.outlined"
    else:
        pkg = "androidx.compose.material.icons." + group.lower()
    return f"import {pkg}.{name}"


def main() -> int:
    targets = [p for root in SRC for p in root.rglob("*.kt")]
    problems = []
    for path in targets:
        text = strip_noise(path.read_text(encoding="utf-8"))
        imports = set(re.findall(r'^import (androidx\.compose\.material\.icons\.[\w.]+)$', text, re.M))
        missing = set()
        for group, name in USE.findall(text):
            imp = expected_import(group, name)[len("import "):]
            if imp not in imports:
                missing.add((group, name))
        for group, name in sorted(missing):
            problems.append((path.relative_to(ROOT), f"Icons.{group}.{name}", expected_import(group, name)))

        for fn, imp in MATH_NEEDS_IMPORT.items():
            if re.search(r'\.' + fn + r'\(', text) and f"import {imp}" not in text:
                problems.append((path.relative_to(ROOT), f".{fn}()", f"import {imp}"))

    if not problems:
        print(f"✅ آیکون‌های متریال و اکستنشن‌های kotlin.math همه ایمپورت شده‌ان ({len(targets)} فایل)")
        return 0

    print(f"❌ {len(problems)} نمادِ بدونِ ایمپورت:")
    for rel, used, imp in problems:
        print(f"  {rel}\n     {used} → {imp}")
    return 1


if __name__ == "__main__":
    sys.exit(main())
