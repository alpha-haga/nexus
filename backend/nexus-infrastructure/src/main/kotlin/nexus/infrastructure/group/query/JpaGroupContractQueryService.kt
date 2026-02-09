package nexus.infrastructure.group.query

import nexus.core.pagination.PaginatedResult
import nexus.group.query.GroupContractSearchCondition
import nexus.group.query.GroupContractSearchDto
import nexus.group.query.GroupContractDetailDto
import nexus.group.query.GroupContractQueryService
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

/**
 * 法人横断契約一覧のクエリサービス実装（JPA MIN）
 *
 * P04-2: jdbc profile では無効化
 * - @Profile("!jdbc") を付与して Bean 競合を回避
 * - インターフェース変更に追随（GroupContractSearchCondition を受け取る形に統一）
 * - 中身は empty + TODO のままでOK（実装は Jdbc に統一予定）
 *
 * 重要:
 * - 非jdbc プロファイルでの compile を通すためのみ存在
 * - 実装は行わない（将来的に削除予定）
 */
@Profile("!jdbc")
@Service
class JpaGroupContractQueryService(
) : GroupContractQueryService {

    override fun search(
        condition: GroupContractSearchCondition,
        page: Int,
        size: Int
    ): PaginatedResult<GroupContractSearchDto> {
        // TODO(P04-2): JDBC 実装に統一後、本クラスは削除予定
        // 現時点は compile を通すための empty 実装
        return PaginatedResult(
            content = emptyList<GroupContractSearchDto>(),
            totalElements = 0L,
            totalPages = 0,
            page = page,
            size = size
        )
    }

    override fun findDetail(
        cmpCd: String,
        contractNo: String
    ): GroupContractDetailDto? {
        // TODO(P04-2): JDBC 実装に統一後、本クラスは削除予定
        return null
    }
}