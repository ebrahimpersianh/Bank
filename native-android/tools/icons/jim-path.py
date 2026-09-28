#!/usr/bin/env python3
"""استخراجِ مسیرِ حرفِ «ج» از Vazirmatn 900 برای آیکونِ لانچر.

⚠️ چرا اسکریپت و نه `<text>`: فونتِ Vazirmatn روی آیکونِ لانچر وجود ندارد، پس `<text>`
یا با فونتِ سیستم رندر می‌شود یا اصلاً نمی‌آید (قاعده‌ی `58c` بندِ ۴). حرف باید **مسیر**
باشد.

سه شرطِ طراح رعایت شده:
  ۱ بومِ ۱۰۸، حرف داخلِ دایره‌ی امنِ ۶۶؛ بلندی ~۵۰ و مرکزِ بصری روی (۵۴، ۵۴).
  ۲ نقطه **مسیرِ جدا** می‌مانَد - ادغامش در یک pathData با evenOdd سوراخش می‌کند.
  ۳ وزن ۹۰۰ (`vazirmatn_black.ttf`)، نه ۷۰۰: در ۴۸dp کشیده‌ی «ج» باریک می‌شود.
"""
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.recordingPen import DecomposingRecordingPen, RecordingPen
from fontTools.ttLib import TTFont
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
FONT = ROOT / "app/src/main/res/font/vazirmatn_black.ttf"
JIM = 0x062C
TARGET_HEIGHT = 50.0
CENTER = (54.0, 54.0)


def contours(glyph_set, name):
    """هر کانتور را جدا برمی‌گرداند تا بتوانیم نقطه را از بدنه جدا کنیم.

    ⚠️ «ج» در این فونت یک گلیفِ **مرکب** است (`uni062D` به‌علاوه‌ی `arabic_dot`)، پس
    قلمِ معمولی فقط دو `addComponent` می‌دهد و هیچ کانتوری نمی‌آید. قلمِ تجزیه‌کننده
    اجزا را باز می‌کند.
    """
    rec = DecomposingRecordingPen(glyph_set)
    glyph_set[name].draw(rec)
    out, current = [], []
    for op, args in rec.value:
        if op == "moveTo" and current:
            out.append(current)
            current = []
        current.append((op, args))
    if current:
        out.append(current)
    return out


def bbox(contour):
    xs, ys = [], []
    for _, args in contour:
        for pt in args:
            if isinstance(pt, tuple):
                xs.append(pt[0])
                ys.append(pt[1])
    return min(xs), min(ys), max(xs), max(ys)


def main():
    font = TTFont(FONT)
    name = font.getBestCmap()[JIM]
    glyph_set = font.getGlyphSet()
    cs = contours(glyph_set, name)

    boxes = [bbox(c) for c in cs]
    # نقطه کوچک‌ترین کانتور است - کاسه‌ی «ج» چند برابرش است.
    dot_index = min(range(len(cs)), key=lambda i: (boxes[i][2] - boxes[i][0]) * (boxes[i][3] - boxes[i][1]))

    x0 = min(b[0] for b in boxes)
    y0 = min(b[1] for b in boxes)
    x1 = max(b[2] for b in boxes)
    y1 = max(b[3] for b in boxes)
    scale = TARGET_HEIGHT / (y1 - y0)
    # فونت y را بالا-مثبت می‌گیرد و وکتورِ اندروید پایین-مثبت: پس y قرینه می‌شود.
    dx = CENTER[0] - (x0 + x1) / 2 * scale
    dy = CENTER[1] + (y0 + y1) / 2 * scale

    def emit(idx):
        pen = SVGPathPen(glyph_set, ntos=lambda v: f"{v:.2f}")
        rec = RecordingPen()
        rec.value = cs[idx]
        rec.replay(_Transform(pen, scale, dx, dy))
        return pen.getCommands()

    print(f"# glyph={name}  scale={scale:.4f}  width={(x1 - x0) * scale:.1f}  height={TARGET_HEIGHT}")
    print("\n<!-- بدنه‌ی «ج» -->")
    for i, _ in enumerate(cs):
        if i != dot_index:
            print(f'<path android:fillColor="#0B3A2A" android:pathData="{emit(i)}" />')
    print("\n<!-- نقطه، مسیرِ جدا (شرطِ ۲) -->")
    print(f'<path android:fillColor="#0B3A2A" android:pathData="{emit(dot_index)}" />')


class _Transform:
    """قلمِ واسط: مقیاس + قرینه‌ی y + انتقال، پیش از رسیدن به قلمِ SVG."""

    def __init__(self, pen, scale, dx, dy):
        self.pen, self.s, self.dx, self.dy = pen, scale, dx, dy

    def _p(self, pt):
        return (pt[0] * self.s + self.dx, -pt[1] * self.s + self.dy)

    def moveTo(self, pt):
        self.pen.moveTo(self._p(pt))

    def lineTo(self, pt):
        self.pen.lineTo(self._p(pt))

    def curveTo(self, *pts):
        self.pen.curveTo(*[self._p(p) for p in pts])

    def qCurveTo(self, *pts):
        self.pen.qCurveTo(*[self._p(p) if p else None for p in pts])

    def closePath(self):
        self.pen.closePath()

    def endPath(self):
        self.pen.endPath()


if __name__ == "__main__":
    main()
