#!/usr/bin/env python3
"""
پراپرتیِ گیرنده‌ی `BoxWithConstraints` (maxWidth/maxHeight/minWidth/minHeight/constraints)
فقط تو **بدنه‌ی مستقیمِ** خودش در دسترسه. به‌محضِ اینکه یه لامبدای دیگه (Column/Row/Canvas…)
باز بشه، گیرنده‌ی ضمنی عوض می‌شه و کامپایلر می‌گه:

    'val maxWidth: Dp' cannot be called in this context with an implicit receiver.

بیلدِ ۴۸۲ رو همین شکست. راهِ درست: همون بالای بدنه تو یه `val` محلی بخونش.
"""
import os
import re
import sys

PROPS = ("maxWidth", "maxHeight", "minWidth", "minHeight")
ROOTS = ("app/src", "data/src", "core/src")
found = []

for root in ROOTS:
    for dp, _, fs in os.walk(root):
        for f in fs:
            if not f.endswith(".kt"):
                continue
            path = os.path.join(dp, f)
            lines = open(path, encoding="utf-8").read().split("\n")
            depth = 0
            body_depth = None
            for i, line in enumerate(lines):
                if body_depth is None and "BoxWithConstraints(" in line:
                    body_depth = depth  # بدنه یه سطح عمیق‌تر از همین‌جاست
                opens = line.count("{") - line.count("}")
                depth += opens
                if body_depth is None:
                    continue
                if depth <= body_depth:
                    body_depth = None
                    continue
                if depth > body_depth + 1:
                    for p in PROPS:
                        if re.search(r"(?<![.\w])" + p + r"\b", line) and "val " + p not in line:
                            found.append(f"  {path}:{i + 1}  {p} داخلِ لامبدای تودرتو → تو val محلی بخونش")

if found:
    print(f"❌ {len(found)} استفاده‌ی نامعتبر از پراپرتیِ BoxWithConstraints:")
    print("\n".join(found))
    sys.exit(1)
print("✅ هیچ پراپرتیِ BoxWithConstraints تو لامبدای تودرتو استفاده نشده")
