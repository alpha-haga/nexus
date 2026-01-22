package nexus.infrastructure.jdbc

/**
 * ページネーション用 offset / limit 計算
 *
 * P0-3d-4: Pagination 最小共通
 * - Kotlin の overload 衝突回避のため、factory で生成する
 */
data class PaginationOffsetLimit(
    val offset: Int,
    val limit: Int,
) {
    companion object {
        fun of(page: Int, size: Int): PaginationOffsetLimit {
            require(page >= 0) { "page must be >= 0, but was $page" }
            require(size > 0) { "size must be > 0, but was $size" }
            return PaginationOffsetLimit(
                offset = page * size,
                limit = size,
            )
        }
    }
}