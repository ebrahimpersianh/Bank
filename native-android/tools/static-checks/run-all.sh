#!/bin/sh
# همه‌ی بررسی‌های ایستا رو پشتِ‌سرهم اجرا می‌کنه.
#
# ⚠️ **چرا این‌ها وجود دارن**: سندباکسِ توسعه نمی‌تونه `native-android` رو بیلد کنه
# (`dl.google.com` مسدوده)، و هر رانِ CI حدودِ ۱۵ دقیقه از سهمیه‌ی ماهانه‌ی کاربر می‌خوره.
# پس قبل از هر `workflow_dispatch` این‌ها باید سبز باشن.
#
# این‌ها **جایگزینِ کامپایلر نیستن** - فقط شش کلاس خطا رو می‌گیرن که تجربه نشون داده تو
# ویرایش‌های برنامه‌نویسی‌شده‌ی Compose بیشتر از همه پیش میان:
#   ۱. ایمپورتِ گمشده        (نمادِ استفاده‌شده ولی ایمپورت‌نشده)
#   ۲. ایمپورتِ شکسته        (نمادی که دیگه وجود نداره - مثلاً بعدِ حذفِ یه فیلد)
#   ۳. ناتعادلِ آکولاد/پرانتز (ویرایشِ متنی راحت یه `}` جا می‌ندازه)
#   ۴. نوعِ اشتباهِ پول       (پروژه همه‌ی مبلغ‌ها رو Double نگه می‌داره، نه Long/Int)
#   ۵. فیلدِ ناموجودِ پالت    (`palette.X` که تو AppColorPalette نیست)
#   ۶. Modifierِ خراب        (اورلودِ ناسازگارِ padding، اکستنشنِ Modifier بدونِ ایمپورت)
#
# بندهای ۴ و ۵ بعدِ شکستِ بیلدِ ۴۶۵ اضافه شدن و بندِ ۶ بعدِ شکستِ بیلدِ ۴۶۸ - هر سه کلاس
# خطایی که بررسیِ ایمپورت‌محور نمی‌گرفت.
#
# استفاده:  sh native-android/tools/static-checks/run-all.sh
# برای محدودکردن به چند فایل:  sh run-all.sh ui/home/HomeScreen.kt ui/theme/Color.kt

set -e
DIR=$(dirname "$0")
FAIL=0

run() {
  printf '\n── %s ───────────────────────────────\n' "$1"
  shift
  python3 "$@" || FAIL=1
}

if [ $# -gt 0 ]; then
  run "ایمپورتِ گمشده" "$DIR/verify.py" "$@"
  run "تعادلِ آکولاد" "$DIR/braces.py" "$@"
else
  run "ایمپورتِ گمشده" "$DIR/verify.py"
  # بدونِ آرگومان، braces.py فایلی نمی‌گیره - همه‌ی فایل‌های تغییرکرده‌ی گیت رو بهش بده.
  CHANGED=$(git -C "$DIR/../../.." diff --name-only HEAD -- '*.kt' 2>/dev/null |
            sed 's|native-android/app/src/main/kotlin/ir/sadteam/loancalc/||' || true)
  if [ -n "$CHANGED" ]; then
    # shellcheck disable=SC2086
    run "تعادلِ آکولاد (فایل‌های تغییرکرده)" "$DIR/braces.py" $CHANGED
  fi
fi

run "ایمپورتِ شکسته" "$DIR/verify2.py"
run "نوعِ پول" "$DIR/moneytypes.py"
run "فیلدهای پالت" "$DIR/palette.py"
run "اکستنشن و آرگومانِ Modifier" "$DIR/modifiers.py"
run "کامنت و انوتیشن" "$DIR/comments.py"
run "نمادِ پروژه بدونِ ایمپورت" "$DIR/ownsymbols.py"
run "مقدارِ منفیِ غیرمجاز" "$DIR/negativedp.py"
run "پراپرتیِ BoxWithConstraints" "$DIR/scopeprops.py"
run "آیکونِ بدونِ ایمپورت" "$DIR/icons.py"
run "نشتِ رنگِ برند" "$DIR/themeleak.py"
run "@Composable تو لیستِ تنبل" "$DIR/lazyscope.py"

printf '\n'
if [ "$FAIL" -eq 0 ]; then
  echo "✅ همه‌ی بررسی‌ها سبزن - می‌تونی بیلد بگیری."
else
  echo "❌ حداقل یه بررسی شکست خورد - قبل از مصرفِ سهمیه‌ی CI درستش کن."
  exit 1
fi
