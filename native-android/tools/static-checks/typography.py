"""
بررسیِ تایپوگرافی در برابرِ مقیاسِ سیستمِ طراحی.

بخشِ «۲ · تایپوگرافی»ِ سیستمِ طراحی فقط این پله‌ها رو تعریف می‌کنه:

    ۲۸/۹۰۰   عددِ قهرمان        (۲۶ و ۲۷ و ۲۹ هم تو کارت‌های قهرمانِ واقعی دیده می‌شن)
    ۱۸/۹۰۰   تیترِ صفحه
    ۱۲٫۵/۹۰۰ عنوانِ ردیف
    ۱۰–۱۱/۸۰۰ دکمه و چیپ
    ۹٫۵/۷۰۰  فرادادهٔ ردیف

هر `fontSize` بیرونِ این مجموعه یعنی یه پله‌ی خودسرانه که مقیاس رو به‌هم می‌ریزه. این اسکریپت
اون‌ها رو با تعداد گزارش می‌ده تا بشه یکی‌یکی به نزدیک‌ترین پله رسوند.

⚠️ هشدارِ کاذبِ عمدی نداره: پله‌های مجاز سخاوتمندانه گرفته شدن (هر چیزی که تو خودِ فایلِ
طراحی حداقل یک‌بار به کار رفته).
"""
import re, glob, os, sys
from collections import Counter

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                    '..', '..', 'app/src/main/kotlin/ir/sadteam/loancalc')

# پله‌هایی که واقعاً تو فایلِ طراحی به کار رفتن
ALLOWED = {
    '7', '7.5', '8.5', '9', '9.5', '10', '10.5', '11', '11.5',
    '12', '12.5', '13', '13.5', '14', '15', '16', '17', '18', '19',
    '20', '21', '22', '23', '24', '26', '27', '28', '29', '36',
}

counts = Counter()
where = {}
for path in sorted(glob.glob(os.path.join(ROOT, '**/*.kt'), recursive=True)):
    rel = os.path.relpath(path, ROOT)
    src = open(path, encoding='utf-8').read()
    body = re.sub(r'//.*', '', src)
    body = re.sub(r'/\*.*?\*/', '', body, flags=re.S)
    for m in re.finditer(r'fontSize\s*=\s*(\d+(?:\.\d+)?)\.sp', body):
        raw = m.group(1)
        # نرمال‌سازی: «۱۴.۰» → «۱۴»، ولی «۱۲.۵» دست‌نخورده. (rstrip('0') روی «۱۰» غلط بود
        # و «۱» می‌داد - برای همین فقط وقتی نقطه هست اعمال می‌شه.)
        v = (raw.rstrip('0').rstrip('.') if '.' in raw else raw)
        if v not in ALLOWED:
            counts[v] += 1
            where.setdefault(v, []).append(f'{rel}:{body[:m.start()].count(chr(10)) + 1}')

if not counts:
    print('✅ همه‌ی fontSizeها رو پله‌های سیستمِ طراحی‌ان')
    sys.exit(0)

print(f'⚠️ {sum(counts.values())} fontSizeِ بیرون از مقیاس:\n')
for v, c in counts.most_common():
    print(f'  {v}.sp  ×{c}')
    for loc in where[v][:4]:
        print(f'      {loc}')
    if len(where[v]) > 4:
        print(f'      … و {len(where[v]) - 4} جای دیگه')
