/* دوره‌ی آزمایشی رایگان: ۷ روز از لحظه‌ی اولین ورودِ موفق با شماره موبایل (created_at رو جدول
   users، که فقط همون یه‌بار موقع ساختِ ردیف ست می‌شه و هیچ‌جای دیگه‌ای دست‌کاری نمی‌شه) - یعنی
   کاملاً سمت سرور و کلید‌خورده به شماره تلفنه، نه به گوشی/نصب. خروج از حساب، پاک‌کردن داده‌ی اپ،
   حذف و نصب دوباره، یا حتی نصب رو گوشی دیگه هیچ‌کدوم دوره‌ی آزمایشی رو ریست نمی‌کنن، چون فقط با
   همون شماره موبایل دوباره وارد شدن، همون ردیف قدیمیِ users (و همون created_at قدیمی) پیدا می‌شه. */
const TRIAL_DAYS = 7;
const TRIAL_MS = TRIAL_DAYS * 24 * 60 * 60 * 1000;

/* ستون‌های TEXT تاریخ تو sqlite با datetime('now') به‌صورت UTC ولی بدون پسوند Z ذخیره می‌شن؛
   برای پارس درست باید صریح Z اضافه بشه، وگرنه Date سازنده‌ی جاوااسکریپت اونو محلی حساب می‌کنه. */
function parseUtc(sqliteDatetime) {
  return new Date(`${sqliteDatetime}Z`).getTime();
}

function trialEndsAtMs(user) {
  if (!user?.created_at) return null;
  return parseUtc(user.created_at) + TRIAL_MS;
}

/* یه کاربر مشترکه (یا هنوز تو دوره‌ی آزمایشی رایگانه) اگه:
   - دستی (`subscribed=1`، برای پشتیبانی/تست) فعال شده باشه، یا
   - یکی از پلن‌های زمان‌دار (`subscribed_until`) هنوز منقضی نشده باشه، یا
   - هنوز تو ۷ روز اولِ بعد از اولین ثبت‌نامش باشه. */
function isSubscribed(user) {
  if (!user) return false;
  if (user.subscribed) return true;
  if (user.subscribed_until && new Date(user.subscribed_until).getTime() > Date.now()) return true;
  const trialEnds = trialEndsAtMs(user);
  if (trialEnds !== null && trialEnds > Date.now()) return true;
  return false;
}

module.exports = { isSubscribed, trialEndsAtMs, TRIAL_DAYS };
