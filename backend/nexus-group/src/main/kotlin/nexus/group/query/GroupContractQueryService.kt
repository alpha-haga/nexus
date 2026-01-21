package nexus.group.query

import nexus.core.id.CorporationId
import nexus.core.id.PersonId
import nexus.core.pagination.PaginatedResult

/**
 * 法人横断契約一覧のクエリサービス（インターフェース）
 *
 * Read 導線の入口として定義。
 * 実装は infrastructure 層に配置（JpaGroupContractQueryService）
 *
 * P0-3c: Read/Write 導線の分離
 * - Repository とは別導線として定義
 * - 将来 JDBC に差し替え可能な設計
 */
interface GroupContractQueryService {

    /**
     * 法人横断契約一覧を検索
     *
     * @param corporationId 法人ID（必須）
     * @param personId 人物ID（任意、null の場合は全人物）
     * @param page ページ番号（0始まり）
     * @param size ページサイズ
     * @return ページネーション結果
     */
    fun search(
        corporationId: CorporationId,
        personId: PersonId?,
        page: Int,
        size: Int
    ): PaginatedResult<GroupContractDto>
}