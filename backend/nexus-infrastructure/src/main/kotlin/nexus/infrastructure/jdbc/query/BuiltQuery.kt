package nexus.infrastructure.jdbc.query

/**
 * 構築されたクエリ
 *
 * @param sql 生成された SQL 文字列
 * @param params バインドパラメータ（Map<パラメータ名, 値>）
 */
data class BuiltQuery(
    val sql: String,
    val params: Map<String, Any?>
)
