#!/usr/bin/env python3
"""ساختِ هشت پله‌ی پژمردگی برای **هر چهار طرحِ آیکون**.

⚠️ **چرا اسکریپت و نه کد**: فیلترِ اشباع/درخشندگی در زمانِ اجرا روی `VectorDrawable`
وجود ندارد و آیکونِ لانچر یک ریسورسِ ایستاست. پس همه‌چیز همین‌جا، پیش از بیلد، روی خودِ
رنگ‌ها حساب می‌شود و فایل‌های واقعی بیرون می‌آید.

## دو فرمول، نه یکی (یافته‌ی طراح، بخشِ ۶۱)

    s = 1 − 0.10·n      اشباع      → طرحِ الف (کیف) و ب (سکه)
    b = 1 − 0.055·n     درخشندگی   → طرحِ پ (حرفی) و ت (قلک)

دلیلش ریاضی است نه سلیقه: پس‌زمینه‌ی `#F4EFE2` و `#12261E` از اول **تقریباً بی‌فام**اند و
اشباع‌زدایی روی رنگی که اشباع ندارد هیچ کاری نمی‌کند - یعنی کاربری که ۴۰۰ سکه برای آن دو
طرح داده، پژمردگی را اصلاً نمی‌دید.

ناحیه‌ی `immune` سقف دارد (اشباع ۰٫۶۵ · درخشندگی ۰٫۷۲): اگر کاملاً ثابت بماند، کاربر فکر
می‌کند بخشی از آیکون **خراب** شده نه پژمرده.

⚠️ اشباع‌زدایی با **درخشندگیِ Rec.709** حساب می‌شود، نه HLS - تا هگزِ خروجی دقیقاً با
جدولِ پله‌ی ۸ِ طراح یکی باشد (`design/IconStage8-palettes.md`).

## لایه‌ی رویه

`ic_wither_overlay_{dark,light}.xml` چهار گروهِ `dust`/`web`/`spider`/`crack` دارند و
`fillAlpha`شان مقدارِ **پله‌ی ۸** است؛ این‌جا خطی تا آستانه‌ی هر گروه پایین می‌آید و پیش از
آن اصلاً رسم نمی‌شود. انتخابِ dark/light روی محورِ **روشناییِ زمینه** است نه طرح.

اجرا (فقط وقتی طرحِ آیکون یا جدولِ پله عوض شد):
    python3 native-android/tools/icons/generate-wither.py
"""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DRAWABLE = ROOT / "app/src/main/res/drawable"
MIPMAP = ROOT / "app/src/main/res/mipmap-anydpi-v26"
STEPS = 8

# (نامِ طرح، حالت، آیا پس‌زمینه هم پژمرده می‌شود، نسخه‌ی رویه)
#
# ⚠️ پس‌زمینه‌ی طرحِ **الف** عمداً دست‌نخورده می‌ماند: بندِ افقیِ نشان هم‌رنگِ پس‌زمینه است
# (تصمیمِ ۵۸) و اگر جدا بپژمرد به یک خطِ شناور تبدیل می‌شود.
DESIGNS = [
    ("wallet", "saturate", False, "dark"),
    ("coin", "saturate", True, "light"),
    ("letter", "brightness", True, "light"),
    ("piggy", "brightness", True, "dark"),
]

# آستانه‌ی ظهورِ هر لایه‌ی رویه - فاصله‌ی نامساوی، طبقِ `45c` (روزهای ۲-۷ و ۱۴ و ۳۰).
OVERLAY_START = {"dust": 1, "web": 3, "spider": 5, "crack": 7}


def _rgb(hex_color: str):
    return [int(hex_color[i:i + 2], 16) for i in (1, 3, 5)]


def _hex(rgb) -> str:
    return "#%02X%02X%02X" % tuple(max(0, min(255, round(c))) for c in rgb)


def desaturate(hex_color: str, amount: float) -> str:
    """اشباع‌زدایی با حفظِ درخشندگیِ Rec.709 - همان کاری که `saturate()` می‌کند."""
    r, g, b = _rgb(hex_color)
    lum = 0.2126 * r + 0.7152 * g + 0.0722 * b
    return _hex([lum + (c - lum) * amount for c in (r, g, b)])


def darken(hex_color: str, amount: float) -> str:
    """کم‌کردنِ درخشندگی - روی رنگِ بی‌فام هم کار می‌کند، برخلافِ اشباع."""
    return _hex([c * amount for c in _rgb(hex_color)])


def group_spans(src: str):
    """بازه‌ی متنیِ هر گروه، تا بدانیم هر `fillColor` داخلِ کدام است."""
    spans = []
    for m in re.finditer(r'<group android:name="(\w+)">', src):
        end = src.index("</group>", m.end())
        spans.append((m.group(1), m.end(), end))
    return spans


def overlay_groups(variant: str, n: int) -> str:
    """گروه‌های رویه با شفافیتِ پله‌ی [n]؛ گروهی که هنوز نوبتش نشده حذف می‌شود."""
    src = (DRAWABLE / f"ic_wither_overlay_{variant}.xml").read_text(encoding="utf-8")
    out = []
    for name, start, end in group_spans(src):
        first = OVERLAY_START.get(name, 1)
        if n < first:
            continue
        factor = (n - first + 1) / (STEPS - first + 1)
        body = src[start:end]
        body = re.sub(
            r'android:fillAlpha="([0-9.]+)"',
            lambda m: 'android:fillAlpha="%.3f"' % (float(m.group(1)) * factor),
            body,
        )
        out.append(f'    <group android:name="{name}">{body}</group>')
    return "\n".join(out)


def stage_colors(src: str, mode: str, n: int, skip: str | None) -> str:
    spans = group_spans(src)
    if mode == "saturate":
        mark, body = max(0.65, 1 - 0.05 * n), max(0.0, 1 - 0.10 * n)
        shift = desaturate
    else:
        mark, body = max(0.72, 1 - 0.035 * n), max(0.0, 1 - 0.055 * n)
        shift = darken

    def replace(m: re.Match) -> str:
        if skip and m.group(1).upper() == skip:
            return m.group(0)
        inside = any(name == "immune" and s <= m.start() <= e for name, s, e in spans)
        return 'android:fillColor="%s"' % shift(m.group(1), mark if inside else body)

    return re.sub(r'android:fillColor="(#[0-9A-Fa-f]{6})"', replace, src)


def stamp(src: str, n: int) -> str:
    return src.replace(
        "<vector",
        f"<!-- ساختهٔ generate-wither.py - پلهٔ {n}. دستی ویرایشش نکن. -->\n<vector",
        1,
    )


def main() -> None:
    made = 0
    for design, mode, wither_bg, variant in DESIGNS:
        fg = (DRAWABLE / f"ic_launcher_{design}_foreground.xml").read_text(encoding="utf-8")
        bg_path = DRAWABLE / f"ic_launcher_{design}_background.xml"
        bg = bg_path.read_text(encoding="utf-8")
        bg_color = re.search(r'android:fillColor="(#[0-9A-Fa-f]{6})"', bg).group(1).upper()
        assert any(name == "immune" for name, _, _ in group_spans(fg)), f"{design}: گروهِ immune نیست"

        for n in range(1, STEPS + 1):
            out = stage_colors(fg, mode, n, skip=None if wither_bg else bg_color)
            # رویه **بعدِ** رنگ می‌نشیند و `immune` ندارد: عنکبوت و ترک روی کل می‌مانند.
            out = out.replace("</vector>", overlay_groups(variant, n) + "\n</vector>")
            out = stamp(out, n)

            if design == "wallet":
                fg_name = f"ic_launcher_wither{n}_foreground"
                bg_name = "ic_launcher_wallet_background"
                mip_name = f"ic_launcher_wither{n}"
            else:
                fg_name = f"ic_launcher_{design}_w{n}_foreground"
                bg_name = f"ic_launcher_{design}_w{n}_background"
                mip_name = f"ic_launcher_{design}_w{n}"
                (DRAWABLE / f"{bg_name}.xml").write_text(
                    stamp(stage_colors(bg, mode, n, skip=None), n), encoding="utf-8"
                )

            (DRAWABLE / f"{fg_name}.xml").write_text(out, encoding="utf-8")
            (MIPMAP / f"{mip_name}.xml").write_text(
                '<?xml version="1.0" encoding="utf-8"?>\n'
                '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
                f'    <background android:drawable="@drawable/{bg_name}" />\n'
                f'    <foreground android:drawable="@drawable/{fg_name}" />\n'
                f'    <monochrome android:drawable="@drawable/{fg_name}" />\n'
                "</adaptive-icon>\n",
                encoding="utf-8",
            )
            made += 1
    print(f"✅ {made} پله ساخته شد ({len(DESIGNS)} طرح × {STEPS})")


if __name__ == "__main__":
    main()
