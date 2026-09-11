#!/usr/bin/env python3
"""
**نشتِ رنگِ برند** - هر جا یکی از پنج رنگِ خانواده‌ی `AppPrimary` به‌صورتِ عددِ خام نوشته
شده باشه، اون تیکه با **تمِ رنگیِ خریدنی** عوض نمی‌شه و سبز می‌مونه.

قاعده‌ی قفل‌شده‌ی طراحی: تمِ رنگی فقط `AppPrimary` و چهار مشتقش (`Dim`/`Ink`/`Pill`/`Border`)
رو عوض می‌کنه. پس **دقیقاً همین پنج مقدار** نباید هیچ‌جا هاردکد باشن.

آزمایشِ دستیِ مکمل (پیشنهادِ طراح): یه تمِ آزمایشیِ زشت بساز و کلِ اپ رو باهاش بگرد؛
هر جا سبز موند، هاردکده. این اسکریپت همون کار رو ایستا انجام می‌ده.
"""
import os
import re
import sys

# پنج مقدارِ روشن + معادلِ تیره‌شون (از LightAppColors/DarkAppColors)
BRAND = {
    "0XFF0EA968": "AppPrimary",
    "0XFF0B8C57": "AppPrimaryDim / AppPrimaryInk",
    "0XFFE9F7EF": "AppPrimaryPill",
    "0XFF9FE0BC": "AppPrimaryBorder",
    "0XFF3DDC96": "AppPrimaryInk (تیره) / AppPrimaryInkLight",
}
# برند و لوگو عمداً سبزِ ثابت‌ان و با تم عوض نمی‌شن.
SKIP = ("/theme/", "JibakLogo.kt", "SplashIntroScreen.kt", "BrandMarks.kt", "JibakMascot.kt", "AppHeroCard.kt", "Avatar.kt",
        # ویجت Glance است نه Compose: `LocalAppColors` را نمی‌بیند، پس رنگش ناچار خام است
        # (قاعده‌ی ۴ی فریمِ `57c` - تمِ خریدنی روی ویجت نمی‌آید).
        "/ui/widget/")

found = []
for root in ("app/src", "data/src", "core/src"):
    for dp, _, fs in os.walk(root):
        for f in fs:
            p = os.path.join(dp, f)
            if not f.endswith(".kt") or any(s in p for s in SKIP):
                continue
            for i, line in enumerate(open(p, encoding="utf-8").read().split("\n")):
                for m in re.findall(r"Color\((0x[0-9A-Fa-f]{8})\)", line):
                    token = BRAND.get(m.upper())
                    if token:
                        found.append(f"  {p}:{i + 1}  {m} → {token}")

if found:
    print(f"❌ {len(found)} نشتِ رنگِ برند (با تمِ رنگی عوض نمی‌شن):")
    print("\n".join(found))
    sys.exit(1)
print("✅ هیچ رنگِ خانواده‌ی primary هاردکد نشده - تمِ رنگی همه‌جا اثر می‌ذاره")
