#!/usr/bin/env python3
"""ساختِ آیکونِ لانچر از رو **فریمِ `24a`**ی فایلِ طراحی (لوگوی v4).

نسخه‌ی قبلی یه کراپ از یه عکسِ AI-generated بود؛ این یکی از رو مقادیرِ خودِ فریم رسم می‌شه،
پس هر وقت طرح عوض شد فقط همین فایل عوض می‌شه، نه یه عکسِ دست‌ساز.

    python3 tools/branding/make_launcher_icon.py

خروجی: `app/src/main/res/mipmap-*/ic_launcher.png` و `ic_launcher_round.png`.
همه‌چیز ۸ برابر بزرگ کشیده و بعد کوچیک می‌شه تا لبه‌ها نرم دربیان.
"""
import pathlib
from PIL import Image, ImageDraw

ROOT = pathlib.Path(__file__).resolve().parents[2]
RES = ROOT / "app/src/main/res"
DENSITIES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
SS = 8  # supersample

TILE = ((0x1F, 0xD6, 0x8C), (0x0E, 0xA9, 0x68), (0x08, 0x7A, 0x4C))
BODY = ((0xF4, 0xFB, 0xF7), (0xDC, 0xED, 0xE3), (0xC6, 0xDC, 0xCF))
FLAP = ((0x8F, 0xCF, 0xAB), (0x5F, 0xB5, 0x88), (0x3F, 0x91, 0x69))
LOCK = (0x2E, 0x78, 0x54)
COIN_RING, COIN_EDGE = (0xF0, 0xC3, 0x56), (0xB0, 0x7E, 0x0C)
COIN_FACE = ((0xFF, 0xFD, 0xF5), (0xF9, 0xC0, 0x42), (0x8F, 0x54, 0x00))


def lerp(a, b, t):
    return tuple(round(x + (y - x) * t) for x, y in zip(a, b))


def stops(colors, t):
    """گرادیانِ سه‌ایستگاهی؛ ایستگاهِ میانی رو ۴۸٪ (مثلِ فریم)."""
    mid = 0.48
    if t <= mid:
        return lerp(colors[0], colors[1], t / mid)
    return lerp(colors[1], colors[2], (t - mid) / (1 - mid))


def diagonal_gradient(size, colors, angle_bias=0.5):
    """گرادیانِ مورب - x و y با هم، تقریبِ همون ۱۵۸/۱۶۸ درجه‌ی فریم."""
    w, h = size
    img = Image.new("RGB", size)
    px = img.load()
    for y in range(h):
        for x in range(w):
            t = (x / max(w - 1, 1) * (1 - angle_bias) + y / max(h - 1, 1) * angle_bias)
            px[x, y] = stops(colors, min(max(t, 0.0), 1.0))
    return img


def rounded_mask(size, radius):
    m = Image.new("L", size, 0)
    ImageDraw.Draw(m).rounded_rectangle([0, 0, size[0] - 1, size[1] - 1], radius=radius, fill=255)
    return m


def wallet(draw, ox, oy, w):
    """کیف رو جعبه‌ی مرجعِ ۹۶×۷۶ - همون هندسه‌ی JibakLogo.kt."""
    k = w / 96

    def u(v):
        return v * k

    def box(x0, y0, x1, y1, r):
        return [ox + u(x0), oy + u(y0), ox + u(x1) - 1, oy + u(y1) - 1], u(r)

    # بدنه
    rect, r = box(0, 21, 96, 76, 19)
    body = diagonal_gradient((round(u(96)), round(u(55))), BODY)
    draw._image.paste(body, (round(ox), round(oy + u(21))), rounded_mask(body.size, round(r)))
    # درزِ خط‌چین
    y = oy + u(66)
    x = ox + u(12)
    while x < ox + u(84):
        draw.line([x, y, min(x + u(5), ox + u(84)), y], fill=(0x08, 0x7A, 0x4C, 60), width=max(1, round(u(2))))
        x += u(9)
    # سکه
    cx, cy = ox + u(48), oy + u(35.5)
    draw.ellipse([cx - u(19.5), cy - u(19.5), cx + u(19.5), cy + u(19.5)], fill=COIN_EDGE)
    draw.ellipse([cx - u(17.5), cy - u(17.5), cx + u(17.5), cy + u(17.5)], fill=COIN_RING)
    steps = 26
    for i in range(steps, 0, -1):
        t = i / steps
        rr = u(15.5) * t
        draw.ellipse(
            [cx - u(4) * (1 - t) - rr, cy - u(5) * (1 - t) - rr, cx - u(4) * (1 - t) + rr, cy - u(5) * (1 - t) + rr],
            # نزدیکِ مرکز روشن‌تر: بزرگ‌ترین دایره تیره‌ترین رنگه، نه برعکس.
            fill=stops(COIN_FACE, t),
        )
    # درِ کیف
    flap = diagonal_gradient((round(u(96)), round(u(36))), FLAP)
    draw._image.paste(flap, (round(ox), round(oy + u(7))), rounded_mask(flap.size, round(u(20))))
    # زبانه‌ی قفل
    draw.rounded_rectangle(
        [cx - u(11.5), oy + u(34), cx + u(11.5), oy + u(49)],
        radius=u(6),
        fill=LOCK,
    )


def build(px):
    s = px * SS
    tile = diagonal_gradient((s, s), TILE, angle_bias=0.45)
    img = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    img.paste(tile, (0, 0), rounded_mask((s, s), round(s * 0.28)))
    d = ImageDraw.Draw(img, "RGBA")
    d._image = img
    # لبه‌ی روشنِ دورِ کاشی (بندِ v4)
    d.rounded_rectangle([0, 0, s - 1, s - 1], radius=round(s * 0.28), outline=(255, 255, 255, 72), width=max(1, round(s * 0.011)))
    # خواسته‌ی صریحِ کاربر: لوگو کلِ آیکون رو پر کنه، نه یه نشانِ کوچیکِ وسطِ کاشی.
    w = s * 0.86
    h = w * 76 / 96
    wallet(d, (s - w) / 2, (s - h) / 2, w)
    return img.resize((px, px), Image.LANCZOS)


for name, px in DENSITIES.items():
    icon = build(px)
    out = RES / f"mipmap-{name}"
    out.mkdir(parents=True, exist_ok=True)
    icon.save(out / "ic_launcher.png")
    # نسخه‌ی گرد: همون تصویر با ماسکِ دایره
    circle = Image.new("L", (px, px), 0)
    ImageDraw.Draw(circle).ellipse([0, 0, px - 1, px - 1], fill=255)
    round_icon = build(px)
    round_icon.putalpha(circle)
    round_icon.save(out / "ic_launcher_round.png")
    print(f"  {name}: {px}×{px}")
print("✅ آیکونِ لانچر از رو فریمِ 24a ساخته شد")
