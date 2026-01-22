package nexus.infrastructure.group.query

import nexus.core.id.CorporationId
import nexus.core.id.PersonId
import nexus.core.pagination.PaginatedResult
import nexus.group.query.GroupContractDto
import nexus.group.query.GroupContractQueryService
import nexus.infrastructure.jdbc.PaginationOffsetLimit
import nexus.infrastructure.jdbc.SqlLoader
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service

/**
 * 法人横断契約一覧 QueryService（JDBC 実装）
 *
 * P0-3d-5:
 * - JDBC 実装を追加し、差し替え導線を固定する
 * - P0 のため SQL は空結果（スキーマ確定後に実装）
 *
 * Bean競合回避:
 * - local プロファイルでのみ有効化する（JPA MIN は残す）
 */
@Profile("local")
@Service
class JdbcGroupContractQueryService(
    private val jdbc: NamedParameterJdbcTemplate,
    private val sqlLoader: SqlLoader,
) : GroupContractQueryService {

    override fun search(
        corporationId: CorporationId,
        personId: PersonId?,
        page: Int,
        size: Int
    ): PaginatedResult<GroupContractDto> {
        val pagination = PaginationOffsetLimit.of(page = page, size = size)

        // SqlLoader が "sql/" を前置する前提（P0-3d-2の固定）
        val sql = sqlLoader.load("group/group_contract_search.sql")

        val params = mapOf(
            "offset" to pagination.offset,
            "limit" to pagination.limit,
            // 将来用（SQL確定後に使う）
            "corporationId" to corporationId.value,
            "personId" to personId?.value,
        )

        val content: List<GroupContractDto> = jdbc.query(sql, params) { _, _ ->
            // P0: SQL が空結果なので到達しない
            throw IllegalStateException("RowMapper should not be invoked because SQL returns no rows in P0.")
        }

        return PaginatedResult(
            content = content,
            totalElements = 0L,
            totalPages = 0,
            page = page,
            size = size,
        )
    }
}