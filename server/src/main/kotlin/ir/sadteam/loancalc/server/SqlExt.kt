package ir.sadteam.loancalc.server

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Types

private fun bindParams(ps: PreparedStatement, params: Array<out Any?>) {
    params.forEachIndexed { index, param ->
        val i = index + 1
        when (param) {
            null -> ps.setNull(i, Types.NULL)
            is Int -> ps.setInt(i, param)
            is Long -> ps.setLong(i, param)
            is Boolean -> ps.setBoolean(i, param)
            is String -> ps.setString(i, param)
            else -> ps.setObject(i, param)
        }
    }
}

fun Connection.execute(sql: String, vararg params: Any?) {
    prepareStatement(sql).use { ps ->
        bindParams(ps, params)
        ps.executeUpdate()
    }
}

/** مثلِ [execute] ولی تعدادِ ردیفِ تغییرکرده را برمی‌گرداند - برای `INSERT ... ON CONFLICT DO
 * NOTHING` که با صفر یعنی «کسِ دیگری زودتر همین کلید را ثبت کرده». */
fun Connection.executeCounting(sql: String, vararg params: Any?): Int {
    prepareStatement(sql).use { ps ->
        bindParams(ps, params)
        return ps.executeUpdate()
    }
}

fun Connection.insertReturningId(sql: String, vararg params: Any?): Long {
    prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { ps ->
        bindParams(ps, params)
        ps.executeUpdate()
        ps.generatedKeys.use { keys ->
            if (keys.next()) return keys.getLong(1)
        }
    }
    error("no generated key returned")
}

fun <T> Connection.queryOne(sql: String, vararg params: Any?, map: (ResultSet) -> T): T? {
    prepareStatement(sql).use { ps ->
        bindParams(ps, params)
        ps.executeQuery().use { rs ->
            return if (rs.next()) map(rs) else null
        }
    }
}
