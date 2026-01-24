package nexus.group.query

import nexus.core.pagination.PaginatedResult

/**
 * 法人横断契約一覧のクエリサービス（インターフェース）
 *
 * Read 導線の入口として定義。
 * 実装は infrastructure 層に配置（JdbcGroupContractQueryService / JpaGroupContractQueryService）
 *
 * P0-3c: Read/Write 導線の分離
 * - Repository とは別導線として定義
 * - 将来の実装置き換え（JDBC ↔ JPA）に対応
 */
interface GroupContractQueryService {

    /**
     * 法人横断契約一覧を検索
     *
     * @param condition 検索条件（全て nullable、指定されたもののみ WHERE 句に含める）
     * @param page ページ番号（0始まり）
     * @param size ページサイズ
     * @return ページネーション結果（GroupContractSearchDto のリスト）
     */
    fun search(
        condition: GroupContractSearchCondition,
        page: Int,
        size: Int
    ): PaginatedResult<GroupContractSearchDto>
}