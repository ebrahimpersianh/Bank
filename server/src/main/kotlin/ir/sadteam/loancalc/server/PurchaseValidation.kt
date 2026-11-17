package ir.sadteam.loancalc.server

/**
 * نتیجه‌ی اعتبارسنجیِ یک رسید با API استور (کافه‌بازار/مایکت).
 *
 * هر دو استور فقط `purchaseState` را برمی‌گردانند (۰ = خریداری‌شده، ۱ = برگشت‌خورده)؛
 * **هیچ‌کدام نشانه‌ی «خریدِ تستی/سندباکس» در پاسخ ندارند** و وب‌هوکِ ریفاند (RTDN) هم
 * نمی‌دهند — پس «تستی‌بودن» از این‌جا قابلِ تشخیص نیست و راهِ دستی‌اش
 * `POST /api/admin/purchases/flag` است؛ «برگشت‌خورده‌بودن» را بازبینیِ دوره‌ای
 * ([PurchaseRevalidation]) از همین `refunded` می‌فهمد.
 */
data class PurchaseValidation(
    /** رسید واقعاً خریداری‌شده و برگشت‌نخورده است. */
    val valid: Boolean,
    /** استور صریحاً گفته خرید برگشت خورده (`purchaseState == 1`). */
    val refunded: Boolean = false,
)
