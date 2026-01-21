package nexus.infrastructure.group.query

import jakarta.persistence.EntityManager
import nexus.core.id.CorporationId
import nexus.core.id.PersonId
import nexus.core.pagination.PaginatedResult
import nexus.group.query.GroupContractDto
import nexus.group.query.GroupContractQueryService
import org.springframework.stereotype.Service

/**
 * 法人横断契約一覧のクエリサービス実装（JPA MIN）
 *
 * P0-3c: Read 導線の実装
 * - domain の GroupContractQueryService を実装
 * - JPA MIN のため EntityManager を使用
 * - スキーマ確定後、実SQL実装へ置換予定
 *
 * 重要:
 * - RegionContext を直接参照しない（DataSource ルーティングは共通基盤に委譲）
 * - ReadModel（GroupContractDto）を返す（Entity を返さない）
 */
@Service
class JpaGroupContractQueryService(
    private val entityManager: EntityManager
) : GroupContractQueryService {

    override fun search(
        corporationId: CorporationId,
        personId: PersonId?,
        page: Int,
        size: Int
    ): PaginatedResult<GroupContractDto> {
        // TODO(P0-3d): 統合DBのスキーマ確定後、実SQL実装へ置換
        // 現時点は compile を通すための empty 実装
        return PaginatedResult(
            content = emptyList<GroupContractDto>(),
            totalElements = 0L,
            totalPages = 0,
            page = page,
            size = size
        )
    }
}