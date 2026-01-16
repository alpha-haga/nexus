package nexus.core.pagination

/**
 * ページネーション結果
 *
 * Domain層のフレームワーク非依存な表現
 * 全モジュールで共通利用
 */
data class PaginatedResult<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int
) {
    init {
        require(page >= 0) { "page must be >= 0" }
        require(size > 0) { "size must be > 0" }
        require(totalElements >= 0) { "totalElements must be >= 0" }
        require(totalPages >= 0) { "totalPages must be >= 0" }
    }
}