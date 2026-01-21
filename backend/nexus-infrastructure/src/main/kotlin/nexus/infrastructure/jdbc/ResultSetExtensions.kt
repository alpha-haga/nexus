package nexus.infrastructure.jdbc

import java.sql.ResultSet
import java.time.LocalDate

/**
 * JDBC ResultSet extension（P0-3d 最小）
 *
 * - RowMapper ヘルパは "ResultSet extension" のみに止める
 * - object/DSL は作らない（抽象化を増やさない）
 */

fun ResultSet.getStringOrNull(columnLabel: String): String? =
    this.getString(columnLabel)

fun ResultSet.getIntOrNull(columnLabel: String): Int? {
    val value = this.getInt(columnLabel)
    return if (this.wasNull()) null else value
}

fun ResultSet.getLongOrNull(columnLabel: String): Long? {
    val value = this.getLong(columnLabel)
    return if (this.wasNull()) null else value
}

fun ResultSet.getLocalDateOrNull(columnLabel: String): LocalDate? {
    // JDBC driver によって getObject(LocalDate) がサポートされない場合があるため、
    // まず getObject を試し、だめなら Date 経由にフォールバックする。
    return try {
        this.getObject(columnLabel, LocalDate::class.java)
    } catch (_: Throwable) {
        this.getDate(columnLabel)?.toLocalDate()
    }
}
