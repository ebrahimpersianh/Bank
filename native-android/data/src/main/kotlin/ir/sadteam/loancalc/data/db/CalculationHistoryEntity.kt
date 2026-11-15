package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * تاریخچه‌ی سبک از هر محاسبه‌ای که تو محاسبه‌گرهای اپ (وام بانکی، سقف وام، سود سپرده) انجام می‌شه -
 * برخلاف «وام‌های من» که فقط وامِ رسماً ذخیره‌شده رو نگه می‌داره، این هر محاسبه‌ی «چی‌میشه‌اگه» رو هم
 * (بدون اینکه کاربر صریحاً «ذخیره» بزنه) ثبت می‌کنه تا بعداً بشه جستجو/مرور کرد.
 */
@Entity(tableName = "calculation_history")
data class CalculationHistoryEntity(
    @PrimaryKey val id: Long,
    /** LOAN | AFFORD | DEPOSIT */
    val kind: String,
    val title: String,
    val summary: String,
    val amount: Double,
    val createdAt: String,
)
