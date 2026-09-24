"""چک‌لیستِ کارت‌به‌کارتِ هر فریمِ فایلِ طراحی.

هر فریم یه `<div id="15a">` داره و داخلش قابِ گوشی (`border-radius:32px`). بچه‌های مستقیمِ
اون قاب دقیقاً همون کارت‌های صفحه‌ان، به همون ترتیب. این اسکریپت با شمارشِ **متوازنِ** تگ
مرزِ هر بلوک رو درست پیدا می‌کنه - نه با بریدنِ کورِ چند هزار کاراکتر (که یه‌بار محتوای
فریمِ بغلی رو قاطی کرد).
"""
import re, sys

VOID = ('br', 'img', 'input', 'path', 'circle', 'rect', 'line', 'polyline', 'polygon', 'stop')
TAG = re.compile(r'<(/?)(\w+)([^>]*?)(/?)>')


def body_of(html, open_start):
    """بدنه‌ی تگی که از [open_start] شروع می‌شه (بین `>` بازکننده و `</...>` متناظرش)."""
    m = TAG.match(html, open_start)
    depth, j = 1, m.end()
    while depth and j < len(html):
        t = TAG.search(html, j)
        if not t:
            break
        if t.group(2) not in VOID and t.group(4) != '/':
            depth += -1 if t.group(1) else 1
        j = t.end()
        if depth == 0:
            return html[m.end():t.start()]
    return html[m.end():]


def children(html):
    out, i = [], 0
    while i < len(html):
        m = TAG.search(html, i)
        if not m:
            break
        if m.group(1) or m.group(2) in VOID or m.group(4) == '/':
            i = m.end()
            continue
        body = body_of(html, m.start())
        out.append(body)
        i = m.start() + len(body) + len(m.group(0))
        nxt = html.find('>', i)
        i = nxt + 1 if nxt != -1 else len(html)
    return out


def text(h, limit=95):
    return re.sub(r'\s+', ' ', re.sub(r'<[^>]+>', ' ', h)).strip()[:limit]


def frame(src, fid):
    m = re.search(r'<div id="%s"[ >]' % re.escape(fid), src)
    if not m:
        return print(f'!! فریمِ {fid} پیدا نشد')
    outer = body_of(src, m.start())
    title = re.search(r'999px">%s</div><div[^>]*>([^<]*)</div>' % re.escape(fid), outer)
    print(f'\n===== {fid} — {title.group(1) if title else "?"} =====')
    p = re.search(r'<div style="border-radius:32px;background:', outer)
    if not p:
        return print('  !! قابِ گوشی پیدا نشد')
    kids = [b for b in children(body_of(outer, p.start())) if text(b)]
    # بعضی فریم‌ها یه لفافِ اضافه دارن (فقط یه بچه) - تا رسیدن به لیستِ واقعیِ کارت‌ها باز می‌شه.
    while len(kids) == 1:
        deeper = [b for b in children(kids[0]) if text(b)]
        if len(deeper) <= 1:
            break
        kids = deeper
    # ساختارِ واقعی: [نوارِ وضعیت] [بدنه‌ی اسکرول] [نوارِ پایین]. کارت‌ها یه لایه داخلِ
    # بدنه‌ان، پس بزرگ‌ترین بچه رو یه پله باز می‌کنیم.
    if len(kids) > 1:
        big = max(kids, key=len)
        kids = [k if k is not big else None for k in kids]
        out = []
        for k in kids:
            out.extend([b for b in children(big) if text(b)] if k is None else [k])
        kids = out
    for k, body in enumerate(kids, 1):
        print(f'  {k}. {text(body)}')


src = open(sys.argv[1], encoding='utf-8').read()
for fid in sys.argv[2:]:
    frame(src, fid)
