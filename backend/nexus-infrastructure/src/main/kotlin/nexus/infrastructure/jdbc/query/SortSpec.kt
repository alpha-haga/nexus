package nexus.infrastructure.jdbc.query

/**
 * ソート方向
 */
enum class SortDirection(val sql: String) {
    ASC("ASC"),
    DESC("DESC");

    companion object {
        /**
         * 文字列から SortDirection を取得
         *
         * @param raw "asc" または "desc"（大文字小文字不問）
         * @return SortDirection
         * @throws IllegalArgumentException "asc"/"desc" 以外の場合
         */
        fun fromString(raw: String?): SortDirection? {
            if (raw == null) return null
            return when (raw.lowercase()) {
                "asc" -> ASC
                "desc" -> DESC
                else -> throw IllegalArgumentException("Invalid sort direction: $raw. Must be 'asc' or 'desc'")
            }
        }
    }
}

/**
 * ソート仕様
 *
 * @param key ソートキー（null の場合はデフォルトソートを使用）
 * @param direction ソート方向（null の場合は ASC として扱う）
 */
data class SortSpec(
    val key: String?,
    val direction: SortDirection?
)
