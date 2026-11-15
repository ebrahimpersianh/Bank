package ir.sadteam.loancalc.ui.inbox

import android.content.Context
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import org.json.JSONArray
import org.json.JSONObject

/**
 * سطل‌زباله‌ی تراکنش‌های ردشده‌ی پیامک/اعلان (برگرفته از پارمیس، ۶ مهر): ردِ اشتباهی قابلِ برگشت
 * است. فقط روی همین گوشی، حداکثر ۵۰ مورد و ۳۰ روز. تراکنشِ ردشده هیچ اثری روی موجودی ندارد.
 */
object SmsRecycleBin {
    private const val PREFS = "sms_recycle_bin"
    private const val KEY = "items"
    private const val MAX = 50
    private const val TTL_MS = 30L * 24 * 60 * 60 * 1000

    data class Item(val tx: AccountTransactionEntity, val deletedAt: Long)

    fun add(context: Context, tx: AccountTransactionEntity) {
        val list = (listOf(Item(tx, System.currentTimeMillis())) + all(context)).take(MAX)
        save(context, list)
    }

    fun all(context: Context): List<Item> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null) ?: return emptyList()
        val now = System.currentTimeMillis()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Item(
                    AccountTransactionEntity(
                        id = o.getLong("id"),
                        accountId = o.getLong("accountId"),
                        type = o.getString("type"),
                        amount = o.getDouble("amount"),
                        description = o.optString("description"),
                        year = o.getInt("y"),
                        month = o.getInt("m"),
                        day = o.getInt("d"),
                        createdAt = o.optString("createdAt"),
                        category = o.optString("category").ifBlank { null },
                        originLabel = o.optString("origin").ifBlank { null },
                    ),
                    o.getLong("deletedAt"),
                )
            }.filter { now - it.deletedAt < TTL_MS }
        }.getOrDefault(emptyList())
    }

    fun remove(context: Context, id: Long) = save(context, all(context).filter { it.tx.id != id })

    private fun save(context: Context, list: List<Item>) {
        val arr = JSONArray()
        list.forEach { (tx, at) ->
            arr.put(
                JSONObject()
                    .put("id", tx.id).put("accountId", tx.accountId).put("type", tx.type).put("amount", tx.amount)
                    .put("description", tx.description).put("y", tx.year).put("m", tx.month).put("d", tx.day)
                    .put("createdAt", tx.createdAt).put("category", tx.category ?: "").put("origin", tx.originLabel ?: "")
                    .put("deletedAt", at),
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, arr.toString()).apply()
    }
}
