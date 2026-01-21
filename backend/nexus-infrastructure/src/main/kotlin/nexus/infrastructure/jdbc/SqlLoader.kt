package nexus.infrastructure.jdbc

import java.nio.charset.StandardCharsets

/**
 * SQL ローダ
 *
 * - 呼び出し側は相対パスのみ渡す（例: "group/group_contract_search.sql"）
 * - このクラスが "sql/" を必ず前置して classpath から読み込む
 *
 * ルール:
 * - path は "/" で開始しない（揺れ防止）
 * - 実体は classpath: sql/<path>
 */
object SqlLoader {

    private const val SQL_PREFIX = "sql/"

    fun load(path: String): String {
        require(path.isNotBlank()) { "SQL path must not be blank." }
        require(!path.startsWith("/")) { "SQL path must be relative (must not start with '/'): $path" }
        require(!path.startsWith(SQL_PREFIX)) {
            "SQL path must be relative without 'sql/' prefix: $path"
        }

        val fullPath = SQL_PREFIX + path
        val stream = Thread.currentThread().contextClassLoader.getResourceAsStream(fullPath)
            ?: throw IllegalArgumentException("SQL file not found on classpath: $fullPath")

        return stream.use { it.readBytes().toString(StandardCharsets.UTF_8) }
    }
}
