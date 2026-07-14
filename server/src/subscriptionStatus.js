/* یه کاربر مشترکه اگه یا دستی (`subscribed=1`، برای پشتیبانی/تست) فعال شده باشه،
   یا یکی از پلن‌های زمان‌دار (`subscribed_until`) هنوز منقضی نشده باشه. */
function isSubscribed(user) {
  if (!user) return false;
  if (user.subscribed) return true;
  if (user.subscribed_until && new Date(user.subscribed_until).getTime() > Date.now()) return true;
  return false;
}

module.exports = { isSubscribed };
