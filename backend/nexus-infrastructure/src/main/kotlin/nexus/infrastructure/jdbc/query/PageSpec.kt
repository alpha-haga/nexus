package nexus.infrastructure.jdbc.query

/**
 * ページネーション仕様
 *
 * @param offset オフセット（0以上）
 * @param limit 取得件数（1以上）
 */
data class PageSpec(
    val offset: Int,
    val limit: Int
) {
    init {
        require(offset >= 0) { "offset must be >= 0, but was $offset" }
        require(limit > 0) { "limit must be > 0, but was $limit" }
    }
}
