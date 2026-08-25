"""
بررسیِ نوعِ آرگومان برای توابعِ پول‌محورِ پروژه.

بیلدِ ۴۶۵ به‌خاطرِ همین شکست: تو `HomeBalanceHero` پارامتر رو `Long` تعریف کرده بودم ولی کلِ
پروژه مبلغ‌ها رو `Double` نگه می‌داره (`AccountTransactionEntity.amount`، `balanceOf()`،
`fmt()`، `countUpAmount()` همه Double). بررسیِ ایمپورت این کلاس خطا رو نمی‌گیره.

این اسکریپت هر تابعِ محلی رو که پارامترِ پول‌مانند داره پیدا می‌کنه و هشدار می‌ده اگه به‌جای
Double از Long/Int استفاده شده باشه.
"""
import re, glob, os, sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..')
MONEY = re.compile(
    r'(?:amount|balance|total|spend|spent|sum|price|cap|value|paid|remaining|installment|qest)',
    re.I,
)

problems = []
for path in glob.glob(os.path.join(ROOT, '**/*.kt'), recursive=True):
    if '/build/' in path:
        continue
    src = open(path, encoding='utf-8').read()
    rel = os.path.relpath(path, ROOT)
    # ⚠️ استثنای عمدی: **سکه** پول نیست، یه شمارنده‌ی صحیحه (طرح صریحاً می‌گه سکه به پول
    # تبدیل نمی‌شه). پس `amount: Int` تو دفترِ سکه درسته، نه باگ.
    if 'Gamification' in rel or 'CoinEvent' in rel or 'CoinDao' in rel:
        continue
    # هر امضای تابع
    for m in re.finditer(r'fun\s+\w+\s*\(([^)]*)\)', src, re.S):
        params = m.group(1)
        line = src[:m.start()].count('\n') + 1
        for pm in re.finditer(r'(\w+)\s*:\s*(Long|Int)\b', params):
            name = pm.group(1)
            if MONEY.search(name):
                # استثنا: شمارنده‌ها و شناسه‌ها
                if re.search(r'(count|index|id|days?|months?|years?|steps?|digits?|seconds?)$', name, re.I):
                    continue
                # `value` و `total`ِ خالی خیلی عمومی‌ان - تو setterهای تنظیمات و شمارنده‌ی
                # مرحله هم میان. فقط اسمِ صریحاً پولی گزارش می‌شه.
                if name.lower() in ('value', 'total'):
                    continue
                problems.append((rel, line, name, pm.group(2)))

if problems:
    print(f'⚠️ {len(problems)} پارامترِ پول‌مانند با نوعِ صحیح به‌جای Double:\n')
    for rel, line, name, typ in problems:
        print(f'  {rel}:{line}  {name}: {typ}')
    print('\n(پروژه همه‌ی مبلغ‌ها رو Double نگه می‌داره - fmt/countUpAmount/amount/balanceOf.)')
    sys.exit(1)
print('✅ هیچ پارامترِ پول‌مانندی با نوعِ اشتباه پیدا نشد')
