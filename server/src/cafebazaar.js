/* ================= تایید خرید درون‌برنامه‌ای کافه‌بازار (Developer API v2) =================
   برای گرفتن CAFEBAZAAR_CLIENT_ID/SECRET/REFRESH_TOKEN باید تو پنل توسعه‌دهندگان کافه‌بازار
   (pardakht.cafebazaar.ir/panel/developer-api) یه کلاینت API بسازید و یک‌بار فلوی OAuth رو
   دستی طی کنید تا refresh_token بگیرید؛ جزئیات تو server/README.md هست.
   مستندات رسمی: https://developers.cafebazaar.ir/en/guidelines/in-app-billing/api/validation */

const CAFEBAZAAR_CLIENT_ID = process.env.CAFEBAZAAR_CLIENT_ID || '';
const CAFEBAZAAR_CLIENT_SECRET = process.env.CAFEBAZAAR_CLIENT_SECRET || '';
const CAFEBAZAAR_REFRESH_TOKEN = process.env.CAFEBAZAAR_REFRESH_TOKEN || '';
const CAFEBAZAAR_PACKAGE_NAME = process.env.CAFEBAZAAR_PACKAGE_NAME || 'ir.sadteam.loancalc';

const TOKEN_URL = 'https://pardakht.cafebazaar.ir/devapi/v2/auth/token/';
const PURCHASE_URL_TMPL = 'https://pardakht.cafebazaar.ir/devapi/v2/api/validate/:package_name/inapp/:purchase_id/purchases/:purchase_token/';

let cachedAccessToken = null;
let cachedAccessTokenExpiresAt = 0;

function isConfigured() {
  return !!(CAFEBAZAAR_CLIENT_ID && CAFEBAZAAR_CLIENT_SECRET && CAFEBAZAAR_REFRESH_TOKEN);
}

async function getAccessToken() {
  if (cachedAccessToken && Date.now() < cachedAccessTokenExpiresAt) return cachedAccessToken;

  const res = await fetch(TOKEN_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      grant_type: 'refresh_token',
      refresh_token: CAFEBAZAAR_REFRESH_TOKEN,
      client_id: CAFEBAZAAR_CLIENT_ID,
      client_secret: CAFEBAZAAR_CLIENT_SECRET,
    }),
  });
  const json = await res.json().catch(() => null);
  if (!res.ok || !json?.access_token) {
    throw new Error('cafebazaar_token_refresh_failed');
  }
  cachedAccessToken = json.access_token;
  /* کمی زودتر از انقضای واقعی منقضی‌ش می‌کنیم که وسط یه درخواست expire نشه */
  cachedAccessTokenExpiresAt = Date.now() + (json.expires_in - 60) * 1000;
  return cachedAccessToken;
}

/* محصول رو به‌عنوان «غیرقابل‌مصرف» (نه اشتراک تمدیدشونده) تایید می‌کنه — چون کلاینت هم از
   purchaseProduct (نه subscribeProduct) استفاده می‌کنه. purchaseState=0 یعنی خریداری‌شده
   و برگشت‌نخورده (0 = purchased, 1 = refunded, طبق مستندات کافه‌بازار). */
async function validateInAppPurchase(productId, purchaseToken) {
  const accessToken = await getAccessToken();
  const url = PURCHASE_URL_TMPL
    .replace(':package_name', encodeURIComponent(CAFEBAZAAR_PACKAGE_NAME))
    .replace(':purchase_id', encodeURIComponent(productId))
    .replace(':purchase_token', encodeURIComponent(purchaseToken))
    + '?access_token=' + encodeURIComponent(accessToken);

  const res = await fetch(url);
  const json = await res.json().catch(() => null);
  if (!res.ok || !json) return false;
  return json.purchaseState === 0;
}

module.exports = { isConfigured, validateInAppPurchase };
