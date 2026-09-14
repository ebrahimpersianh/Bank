#!/usr/bin/env python3
"""بررسیِ نوزدهم — لامبدای انتهایی که به پارامترِ اشتباه می‌چسبد.

بیلدِ **۵۴۱** با همین شکست و هیچ‌کدام از ۱۸ بررسیِ قبلی نگرفتش: نماد ایمپورت بود،
امضا وجود داشت، و هیچ آرگومانِ نام‌داری غلط نبود.

    ShopRow(item, state, balance, onActivate) { confirming = it }

`ShopRow` شش پارامتر دارد و آخرینش `leading`ِ اختیاری است، نه `onConfirm`. لامبدای
انتهایی در کاتلین همیشه به **آخرین** پارامتر می‌چسبد، پس `onConfirm` بی‌مقدار ماند.

قاعده: لامبدای انتهایی جای پارامترِ آخر را پر می‌کند؛ پس هر پارامترِ **بدونِ پیش‌فرض**
که بینِ آخرین آرگومانِ ترتیبی و پارامترِ آخر بیفتد، خطاست.

عمداً محافظه‌کار: فقط تابع‌هایی که دقیقاً یک اعلان در پروژه دارند، و فقط فراخوانی‌هایی
که هیچ آرگومانِ نام‌داری ندارند (وگرنه شمارشِ ترتیبی معنی نمی‌دهد).
"""
import re
import sys
from pathlib import Path



def strip_comments(src: str) -> str:
    """کامنت‌ها را با فاصله جایگزین می‌کند تا شماره‌ی خط دست‌نخورده بماند.

    ⚠️ عمداً کپیِ همان تابعِ `apisurface.py` است نه import: آن فایل در سطحِ ماژول
    اجرا می‌شود و import کردنش کلِ بررسیِ دیگر را هم راه می‌اندازد.
    """
    out, i, n = [], 0, len(src)
    while i < n:
        two = src[i:i + 2]
        if two == "//":
            j = src.find("\n", i)
            j = n if j < 0 else j
            out.append(" " * (j - i))
            i = j
        elif two == "/*":
            j = src.find("*/", i + 2)
            j = n if j < 0 else j + 2
            out.append("".join(c if c == "\n" else " " for c in src[i:j]))
            i = j
        elif src[i] == '"':
            j = i + 1
            while j < n and src[j] != '"':
                j += 2 if src[j] == "\\" else 1
            j = min(j + 1, n)
            out.append("".join(c if c == "\n" else " " for c in src[i:j]))
            i = j
        else:
            out.append(src[i])
            i += 1
    return "".join(out)


def top_level_named(args: str):
    """آرگومان‌های نام‌دارِ سطحِ اول."""
    depth = 0
    for m in re.finditer(r"[()\[\]{}]|(?:^|,)\s*(\w+)\s*=(?!=)", args):
        tok = m.group(0)
        if m.group(1) and depth == 0:
            yield m.group(1), m.start()
        elif tok and tok[-1] in "([{":
            depth += 1
        elif tok and tok[-1] in ")]}":
            depth -= 1

ROOT = Path(__file__).resolve().parents[2]
SRC = [p for p in ROOT.rglob("*.kt") if "/build/" not in p.as_posix()]

FUN_RE = re.compile(
    r"^\s*(?:@\w+\s+)*(?:(?:public|private|internal|inline|suspend|operator|expect|actual)\s+)*"
    r"fun\s+(?:<[^>]+>\s*)?(?:[\w.]+\.)?(\w+)\s*\(",
    re.M,
)


def balanced(src: str, open_idx: int) -> str:
    """متنِ داخلِ پرانتزی که در [open_idx] باز شده."""
    depth, i, n = 0, open_idx, len(src)
    while i < n:
        c = src[i]
        if c in "([{":
            depth += 1
        elif c in ")]}":
            depth -= 1
            if depth == 0:
                return src[open_idx + 1:i]
        i += 1
    return ""


def params_of(sig: str):
    """فهرستِ (نام، پیش‌فرض‌دارد؟) به ترتیب.

    ⚠️ فلشِ `->` اول خنثی می‌شود: آن `>` وگرنه عمقِ جنریک را منفی می‌کرد و هر امضایی
    که پارامترِ تابعی داشت (یعنی نیمی از composableها) بی‌صدا رد می‌شد.
    """
    sig = sig.replace("->", "__")
    out, depth, buf = [], 0, ""
    for ch in sig:
        if ch in "([{<":
            depth += 1
        elif ch in ")]}>":
            depth -= 1
        if ch == "," and depth == 0:
            out.append(buf)
            buf = ""
        else:
            buf += ch
    if buf.strip():
        out.append(buf)
    result = []
    for raw in out:
        piece = raw.strip()
        if not piece:
            continue
        name = re.match(r"(?:@\w+\s+)*(?:vararg\s+)?(\w+)\s*:", piece)
        if not name:
            return None
        has_default = "=" in piece.split(":", 1)[1] if ":" in piece else False
        result.append((name.group(1), has_default))
    return result


declared = {}
duplicate = set()
for path in SRC:
    src = strip_comments(path.read_text(encoding="utf-8"))
    for m in FUN_RE.finditer(src):
        name = m.group(1)
        params = params_of(balanced(src, m.end() - 1))
        if params is None:
            duplicate.add(name)
            continue
        if name in declared and declared[name] != params:
            duplicate.add(name)
        declared[name] = params

CALL_RE = re.compile(r"\b([A-Z]\w*)\s*\(")

problems = []
for path in SRC:
    src = strip_comments(path.read_text(encoding="utf-8"))
    for m in CALL_RE.finditer(src):
        name = m.group(1)
        if name in duplicate or name not in declared:
            continue
        # خودِ **اعلان** هم با همین الگو می‌خورد و `{`ِ بعدش بدنه‌ی تابع است، نه لامبدا.
        before = src[max(0, m.start() - 6):m.start()]
        if before.rstrip().endswith("fun"):
            continue
        params = declared[name]
        if len(params) < 2:
            continue
        args = balanced(src, m.end() - 1)
        close = m.end() - 1 + len(args) + 2  # درست بعدِ پرانتزِ بسته
        # لامبدای انتهایی؟ (فقط وقتی بلافاصله بعدِ پرانتزِ بسته می‌آید)
        after = src[close:close + 40]
        if not re.match(r"\s*\{", after):
            continue
        if any(True for _ in top_level_named(args)):
            continue
        count, depth, seen = 0, 0, False
        for ch in args:
            if ch in "([{<":
                depth += 1
            elif ch in ")]}>":
                depth -= 1
            elif ch == "," and depth == 0:
                count += 1
            if not ch.isspace():
                seen = True
        positional = count + 1 if seen else 0
        gap = params[positional:len(params) - 1]
        missing = [p for p, has_default in gap if not has_default]
        if missing:
            line = src[:m.start()].count("\n") + 1
            rel = path.relative_to(ROOT)
            problems.append(
                f"  {rel}:{line}  {name}(…) {{ }} → لامبدا به «{params[-1][0]}» می‌چسبد، "
                f"پس {'، '.join(missing)} بی‌مقدار می‌ماند"
            )

if problems:
    print(f"\n❌ {len(problems)} لامبدای انتهایی که به پارامترِ اشتباه می‌چسبد:\n")
    print("\n".join(problems))
    sys.exit(1)
print(f"✅ همه‌ی لامبداهای انتهایی به پارامترِ درست می‌چسبند ({len(SRC)} فایل)")
