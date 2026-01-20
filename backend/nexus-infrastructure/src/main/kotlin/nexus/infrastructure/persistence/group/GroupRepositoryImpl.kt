package nexus.infrastructure.persistence.group

import nexus.core.id.PersonId
import nexus.core.pagination.PaginatedResult
import nexus.group.query.GroupContractDto
import nexus.group.query.GroupSearchCriteria
import nexus.group.query.GroupSearchResult
import nexus.group.repository.GroupRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

/**
 * GroupRepository の実装（JDBC）
 *
 * - Read Only（統合DB）
 * - ReadModel/DTO を返す
 *
 * NOTE:
 * local(H2) での起動をブロックしないため、現時点はスタブ実装。
 * P0-3 で統合DBのスキーマ/SQL確定後にクエリ実装へ置換する。
 */
@Repository
class GroupRepositoryImpl(
    private val jdbc: NamedParameterJdbcTemplate
) : GroupRepository {

    override fun searchPersons(criteria: GroupSearchCriteria): List<GroupSearchResult> {
        // TODO(P0-3): integration DB の実SQL実装
        return emptyList()
    }

    override fun findMergeCandidates(personId: PersonId): List<GroupSearchResult> {
        // TODO(P0-3): integration DB の実SQL実装
        return emptyList()
    }

    override fun findContracts(corporationId: String?, page: Int, size: Int): PaginatedResult<GroupContractDto> {
        // TODO(P0-3): integration DB の実SQL実装（ページング）
        return PaginatedResult(
            content = emptyList<GroupContractDto>(),
            totalElements = 0L,
            totalPages = 0,
            page = page,
            size = size
        )
    }
}
