package nexus.infrastructure.jdbc

/**
 * ページネーション用の offset / limit 計算専用クラス
 *
 * P0-3d-4: Pagination 最小共通
 * - SQL 側では :offset / :limit を使う前提
 * - 計算専用（Repository / DAO 基底ではない）
 */
data class PaginationOffsetLimit(
    val offset: Int,
    val limit: Int
) {
    init {
        require(offset >= 0) { "offset must be >= 0, but was $offset" }
        require(limit > 0) { "limit must be > 0, but was $limit" }
    }

    companion object {
        /**
         * page（0始まり）と size から offset / limit を計算する。
         *
         * 計算式:
         * - offset = page * size
         * - limit  = size
         */
        fun fromPage(page: Int, size: Int): PaginationOffsetLimit {
            require(page >= 0) { "page must be >= 0, but was $page" }
            require(size > 0) { "size must be > 0, but was $size" }
            return PaginationOffsetLimit(
                offset = page * size,
                limit = size
            )
        }
    }
}
