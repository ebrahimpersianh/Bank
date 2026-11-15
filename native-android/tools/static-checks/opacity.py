"""
شکارِ **سطحِ نیمه‌شفاف** - قاعده‌ی صریحِ سیستمِ طراحی: «سطحِ کاملاً مات».

هر `background(<رنگ>.copy(alpha = ...))` یعنی یه سطحِ نیمه‌شفاف که رنگِ پشتش رو نشون می‌ده.
تو سبکِ جدید این‌ها باید توکنِ **ماتِ** خودشون رو داشته باشن (`AppPrimaryPill`، `AppInfoPill`،
`AppDangerPill`، …).

⚠️ چند استثنای عمدی:
  * رو کارتِ **قهرمانِ رنگی**، سفیدِ کم‌آلفا درسته (`Color.White.copy(alpha=…)`) - خودِ طرح
    `rgba(255,255,255,.2)` داره.
  * `border(...)` و `tint = ...` سطح نیستن - آلفا اونجا مشکلی نداره.
  * نمودارها (`drawArc`/`drawPath`) سطحِ کارت نیستن.
"""
import re, glob, os, sys
from collections import defaultdict

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                    '..', '..', 'app/src/main/kotlin/ir/sadteam/loancalc')

SKIP_FILES = ('AuroraBackground.kt', 'GoldSheen.kt', 'ShimmerEffect.kt',
              'PulseGlow.kt', 'CoinCelebration.kt', 'ThemeReveal.kt',
              'SuccessCheckmark.kt', 'ProgressRing.kt', 'CategoryDonut.kt',
              'Scrollbar.kt', 'AppTourOverlay', 'SplashIntroScreen.kt')

hits = defaultdict(list)
for path in sorted(glob.glob(os.path.join(ROOT, '**/*.kt'), recursive=True)):
    rel = os.path.relpath(path, ROOT)
    if any(sk in rel for sk in SKIP_FILES):
        continue
    src = open(path, encoding='utf-8').read()
    body = re.sub(r'//.*', '', src)
    body = re.sub(r'/\*.*?\*/', '', body, flags=re.S)
    for m in re.finditer(r'\.background\(\s*(\w+)\.copy\(alpha\s*=\s*([\d.]+)f\)', body):
        token, alpha = m.group(1), m.group(2)
        # سفیدِ کم‌آلفا رو کارتِ قهرمان عمدیه (خودِ طرح داره)
        if token in ('Color', 'White') or 'Hero' in body[max(0, m.start() - 400):m.start()]:
            continue
        # استثنای عمدی: نانِ/توستِ شناور رو **محتوا** می‌شینه نه رو کارت؛ کمی شفافیت اونجا
        # همون چیزیه که طرح می‌خواد («روی محتوا»، نه یه سطحِ مستقل).
        if token == 'AppText' and float(alpha) >= 0.9:
            continue
        hits[rel].append((body[:m.start()].count('\n') + 1, f'{token}.copy(alpha = {alpha})'))

total = sum(len(v) for v in hits.values())
if not hits:
    print('✅ هیچ سطحِ نیمه‌شفافی نیست - همه مات‌ان')
    sys.exit(0)

print(f'⚠️ {total} سطحِ نیمه‌شفاف تو {len(hits)} فایل:\n')
for rel in sorted(hits, key=lambda r: -len(hits[r])):
    print(f'  {rel}  ({len(hits[rel])})')
    for line, what in hits[rel][:6]:
        print(f'     خط {line}: {what}')
    if len(hits[rel]) > 6:
        print(f'     … و {len(hits[rel]) - 6} مورد')
