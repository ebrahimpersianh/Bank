#!/usr/bin/env python3
"""بررسیِ هجدهم — امضای نادرستِ تابعِ پروژه و ثابتِ enumِ ناموجود.

بیلدِ ۵۳۲ با هر دو الگو شکست، و هیچ‌کدام از ۱۷ بررسیِ قبلی نمی‌گرفتشان — نماد ایمپورت
شده بود، املا درست بود، و گیرنده هم اعلان‌شده:

    GradientButton(text = "…", onClick = …)   → این تابع اصلاً `text` ندارد، `content` دارد
    CoinReason.WEEK_COMPLETE                  → نامِ واقعی `FULL_WEEK` است

هر دو از بسته‌ی طراح آمدند، که طبیعی است: طراح امضای واقعی را ندیده و حدس می‌زند.

قاعده:
  ۱. اگر تابعی **در همین پروژه** اعلان شده و فراخوانی‌اش آرگومانِ نام‌دار دارد، آن نام
     باید در امضای همان تابع باشد.
  ۲. اگر `X.Y` نوشته شده و `X` یک enumِ پروژه است، `Y` باید یکی از ثابت‌هایش (یا عضوِ
     اعلان‌شده‌اش) باشد.

عمداً محافظه‌کار: تابعِ هم‌نامِ چندگانه (overload) کلاً نادیده گرفته می‌شود، و فقط
نام‌هایی بررسی می‌شوند که دقیقاً یک اعلان در کلِ پروژه دارند.
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SRC = [p for p in ROOT.rglob("*.kt") if "/build/" not in p.as_posix()]

FUN_RE = re.compile(r"^\s*(?:@\w+\s+)*(?:(?:public|private|internal|inline|suspend|operator|expect|actual)\s+)*fun\s+(?:<[^>]+>\s*)?(?:[\w.]+\.)?(\w+)\s*\(", re.M)
ENUM_RE = re.compile(r"^\s*(?:(?:public|private|internal)\s+)*enum\s+class\s+(\w+)", re.M)


def strip_comments(src: str) -> str:
    """کامنت‌ها را با فاصله جایگزین می‌کند تا شماره‌ی خط دست‌نخورده بماند."""
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
    """آرگومان‌های نام‌دارِ **سطحِ اول** - نه آن‌هایی که داخلِ لامبدا/فراخوانیِ تودرتواند."""
    depth = 0
    for m in re.finditer(r"[()\[\]{}]|(?:^|,)\s*(\w+)\s*=(?!=)", args):
        tok = m.group(0)
        if m.group(1) and depth == 0:
            yield m.group(1), m.start()
        elif tok and tok[-1] in "([{":
            depth += 1
        elif tok and tok[-1] in ")]}":
            depth -= 1


def split_params(sig: str) -> set:
    """نامِ پارامترها از متنِ بینِ پرانتزها. تودرتویی (پیش‌فرضِ تابعی) را رد می‌کند."""
    names, depth, buf = set(), 0, ""
    for ch in sig:
        if ch in "([{":
            depth += 1
        elif ch in ")]}":
            depth -= 1
        if ch == "," and depth == 0:
            names.add(buf)
            buf = ""
        else:
            buf += ch
    names.add(buf)
    out = set()
    for chunk in names:
        m = re.match(r"\s*(?:@\w+\s+)*(?:vararg\s+|crossinline\s+|noinline\s+)*(?:val\s+|var\s+)?(\w+)\s*:", chunk)
        if m:
            out.add(m.group(1))
    return out


def balanced_signature(src: str, open_idx: int) -> str:
    depth, i = 0, open_idx
    while i < len(src):
        if src[i] in "([{":
            depth += 1
        elif src[i] in ")]}":
            depth -= 1
            if depth == 0:
                return src[open_idx + 1:i]
        i += 1
    return ""


funs, dup_funs = {}, set()
enums = {}
sources = {}

for path in SRC:
    src = strip_comments(path.read_text(encoding="utf-8"))
    sources[path] = src
    for m in FUN_RE.finditer(src):
        name = m.group(1)
        params = split_params(balanced_signature(src, m.end() - 1))
        if name in funs and funs[name] != params:
            dup_funs.add(name)
        funs[name] = params
    for m in ENUM_RE.finditer(src):
        body_start = src.find("{", m.end())
        if body_start < 0:
            continue
        depth, i = 0, body_start
        while i < len(src):
            if src[i] == "{":
                depth += 1
            elif src[i] == "}":
                depth -= 1
                if depth == 0:
                    break
            i += 1
        body = src[body_start:i + 1]
        # ثابت‌ها تا اولین `;` یا اولین اعلانِ عضو می‌آیند و می‌توانند یک‌خطی باشند.
        members = set(re.findall(r"(?:^|[{,;])\s*([A-Z][A-Z0-9_]*)\s*(?=[(,;}\n])", body, re.M))
        members |= set(re.findall(r"\b(?:val|var|fun)\s+(\w+)", body))
        # عضوِ companion و `entries`/`values`/`valueOf` همیشه هستند.
        members |= {"entries", "values", "valueOf", "name", "ordinal", "Companion"}
        enums[m.group(1)] = members

for name in dup_funs:
    funs.pop(name, None)

CALL_RE = re.compile(r"\b([A-Z]\w+)\s*\(")
NAMED_RE = re.compile(r"(?:^|[(,])\s*(\w+)\s*=(?!=)")
ENUM_USE_RE = re.compile(r"\b([A-Z]\w+)\.([A-Z][A-Z0-9_]{2,})\b")

problems = []
for path, src in sources.items():
    rel = path.relative_to(ROOT)
    for m in CALL_RE.finditer(src):
        fname = m.group(1)
        if fname not in funs:
            continue
        args = balanced_signature(src, m.end() - 1)
        if not args:
            continue
        for arg, _ in top_level_named(args):
            if arg not in funs[fname]:
                line = src[:m.start()].count("\n") + 1
                problems.append(f"  {rel}:{line}  {fname}(…) پارامترِ «{arg}» ندارد")
    for m in ENUM_USE_RE.finditer(src):
        holder, const = m.group(1), m.group(2)
        if holder in enums and const not in enums[holder]:
            line = src[:m.start()].count("\n") + 1
            problems.append(f"  {rel}:{line}  {holder} ثابتِ «{const}» ندارد")

if problems:
    print(f"❌ {len(problems)} فراخوانیِ ناسازگار با امضای واقعی:\n")
    for p in sorted(set(problems)):
        print(p)
    print("\n(امضای واقعی را از خودِ فایلِ اعلان بخوان - حدسِ بسته‌ی طراح ملاک نیست.)")
    sys.exit(1)

print(f"✅ همه‌ی فراخوانی‌ها با امضای واقعی جورند ({len(funs)} تابع، {len(enums)} enum)")
