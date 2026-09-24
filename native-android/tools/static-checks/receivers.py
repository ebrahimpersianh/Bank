#!/usr/bin/env python3
"""بررسیِ پانزدهم — گیرنده‌ی اعلان‌نشده (`repository.foo()` که `repository` وجود ندارد).

بیلدِ ۵۱۰ با همین شکست: در `AccountViewModel` نوشته شد `repository.updateTransaction(...)`
در حالی که فیلدِ واقعی `accountRepository` نام دارد. نماد **ایمپورت لازم ندارد** (عضوِ
همان کلاس است) و املایش هم بی‌ایراد است، پس هیچ‌کدام از چهارده بررسیِ قبلی نمی‌گیردش.

قاعده: در فایل‌هایی که یک کلاسِ ساده‌اند (ViewModel/Repository)، هر گیرنده‌ای که با
حروفِ کوچک شروع شود باید جایی در همان فایل **اعلان** شده باشد — پارامترِ سازنده، پراپرتی،
پارامترِ تابع، یا متغیرِ محلی. اگر نه، یا غلطِ املایی است یا فیلدی که وجود ندارد.

عمداً محافظه‌کار: فقط `ViewModel.kt`/`Repository.kt` را می‌بیند (جایی که تزریقِ سازنده
هست و همین خطا رخ می‌دهد)، و هر چیزی که ایمپورت شده یا نامِ شناخته‌شده‌ی Kotlin/Compose
است را نادیده می‌گیرد.
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]

KNOWN = {
    "it", "this", "super", "value", "result", "context", "require", "check",
    "println", "listOf", "mapOf", "setOf", "emptyList", "emptyMap", "buildString",
    "runCatching", "withContext", "launch", "async", "flow", "combine", "arrayOf",
    "maxOf", "minOf", "error", "lazy", "run", "let", "also", "apply", "with",
}


def declared_names(text: str) -> set:
    names = set()
    # پارامترِ سازنده و پراپرتی: `private val foo:` / `val foo =` / `var foo`
    names |= set(re.findall(r"\b(?:private |internal |public )?va[lr]\s+(\w+)", text))
    # پارامترِ تابع و لامبدا: `foo: Type`
    names |= set(re.findall(r"[(,]\s*(\w+)\s*:", text))
    # هر چیزی که ایمپورت شده (آخرین بخشِ مسیر)
    names |= set(re.findall(r"^import\s+[\w.]*?\.(\w+)$", text, re.M))
    # نامِ توابعِ خودِ فایل
    names |= set(re.findall(r"\bfun\s+(?:<[^>]+>\s*)?(?:[\w.<>?]+\.)?(\w+)\s*\(", text))
    # `for (x in ...)` و `catch (e: ...)`
    names |= set(re.findall(r"\bfor\s*\(\s*(\w+)", text))
    # پارامترِ لامبدا: `{ file ->` و شکلِ ساختارشکنش `{ (date, price) ->`
    for params in re.findall(r"\{\s*\(?([\w,\s]+?)\)?\s*->", text):
        names |= {p.strip() for p in params.split(",") if p.strip()}
    return names


def main() -> int:
    files = [
        p for p in (ROOT / "app/src/main/kotlin").rglob("*.kt")
        if p.name.endswith("ViewModel.kt") or p.name.endswith("Repository.kt")
    ]
    problems = []
    for f in files:
        text = f.read_text(encoding="utf-8")
        known = declared_names(text) | KNOWN
        for i, line in enumerate(text.splitlines(), 1):
            stripped = line.strip()
            if stripped.startswith(("*", "//", "import ", "package ")):
                continue
            for m in re.finditer(r"(?<![\w.\"'])([a-z]\w{2,})\.\w+\s*\(", line):
                name = m.group(1)
                if name not in known:
                    problems.append((f.relative_to(ROOT), i, name, stripped))

    if problems:
        print(f"❌ {len(problems)} گیرنده‌ی اعلان‌نشده:")
        for path, line_no, name, src in problems:
            print(f"  {path}:{line_no}  →  «{name}» در این فایل اعلان نشده")
            print(f"      {src}")
        return 1
    print(f"✅ همه‌ی گیرنده‌ها در فایلِ خودشان اعلان شده‌اند ({len(files)} فایل)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
