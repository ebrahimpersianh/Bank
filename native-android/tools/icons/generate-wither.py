#!/usr/bin/env python3
"""ساختِ هشت پله‌ی پژمردگیِ آیکونِ لانچر از روی پیش‌زمینه‌ی پله‌ی صفر.

⚠️ **چرا اسکریپت و نه کد**: فرمولِ `saturate = 1 − 0.10·n` در زمانِ اجرا **اجراشدنی
نیست** - آیکونِ لانچر یک ریسورسِ ایستاست و `VectorDrawable` فیلترِ اشباع ندارد. پس
اشباع همین‌جا، پیش از بیلد، روی خودِ رنگ‌ها حساب می‌شود و هشت فایلِ واقعی بیرون می‌آید.

قاعده‌ی `58c` (بندِ ۳):
    داخلِ گروهِ `immune` (سکه):  markSaturate = max(0.65, 1 − 0.05·n)
    بیرونش (بدنه):              bodySaturate = max(0.00, 1 − 0.10·n)

اجرا (فقط وقتی طرحِ آیکون عوض شد):
    python3 native-android/tools/icons/generate-wither.py
"""
import colorsys
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DRAWABLE = ROOT / "app/src/main/res/drawable"
MIPMAP = ROOT / "app/src/main/res/mipmap-anydpi-v26"
SOURCE = DRAWABLE / "ic_launcher_wallet_foreground.xml"
BACKGROUND = DRAWABLE / "ic_launcher_wallet_background.xml"
STEPS = 8


def desaturate(hex_color: str, amount: float) -> str:
    r, g, b = (int(hex_color[i:i + 2], 16) / 255 for i in (1, 3, 5))
    h, l, s = colorsys.rgb_to_hls(r, g, b)
    r2, g2, b2 = colorsys.hls_to_rgb(h, l, s * amount)
    return "#%02X%02X%02X" % (round(r2 * 255), round(g2 * 255), round(b2 * 255))


def group_spans(src: str):
    """بازه‌ی متنیِ هر گروه، تا بدانیم هر `fillColor` داخلِ کدام است."""
    spans = []
    for m in re.finditer(r'<group android:name="(\w+)">', src):
        end = src.index("</group>", m.end())
        spans.append((m.group(1), m.end(), end))
    return spans


def main() -> None:
    src = SOURCE.read_text(encoding="utf-8")
    # رنگِ پس‌زمینه دست نمی‌خورد: بندِ افقی عمداً هم‌رنگِ پس‌زمینه است (تصمیمِ ۵۸)، و اگر
    # پژمرده شود از پس‌زمینه جدا می‌افتد و به یک خطِ شناور تبدیل می‌شود - دقیقاً همان چیزی
    # که طراح با هم‌رنگ‌کردنش حلش کرده بود.
    bg_match = re.search(r'android:fillColor="(#[0-9A-Fa-f]{6})"', BACKGROUND.read_text(encoding="utf-8"))
    bg_color = bg_match.group(1).upper() if bg_match else ""
    spans = group_spans(src)
    assert any(name == "immune" for name, _, _ in spans), "گروهِ immune پیدا نشد"

    for n in range(1, STEPS + 1):
        mark = max(0.65, 1 - 0.05 * n)
        body = max(0.0, 1 - 0.10 * n)

        def replace(m: re.Match) -> str:
            pos = m.start()
            if m.group(1).upper() == bg_color:
                return m.group(0)
            inside_immune = any(name == "immune" and s <= pos <= e for name, s, e in spans)
            return 'android:fillColor="%s"' % desaturate(m.group(1), mark if inside_immune else body)

        out = re.sub(r'android:fillColor="(#[0-9A-Fa-f]{6})"', replace, src)
        out = out.replace(
            "<vector",
            f"<!-- ساختهٔ generate-wither.py - پلهٔ {n}. دستی ویرایشش نکن. -->\n<vector",
            1,
        )
        (DRAWABLE / f"ic_launcher_wither{n}_foreground.xml").write_text(out, encoding="utf-8")
        (MIPMAP / f"ic_launcher_wither{n}.xml").write_text(
            '<?xml version="1.0" encoding="utf-8"?>\n'
            '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
            '    <background android:drawable="@drawable/ic_launcher_wallet_background" />\n'
            f'    <foreground android:drawable="@drawable/ic_launcher_wither{n}_foreground" />\n'
            f'    <monochrome android:drawable="@drawable/ic_launcher_wither{n}_foreground" />\n'
            "</adaptive-icon>\n",
            encoding="utf-8",
        )
    print(f"✅ {STEPS} پله ساخته شد")


if __name__ == "__main__":
    main()
