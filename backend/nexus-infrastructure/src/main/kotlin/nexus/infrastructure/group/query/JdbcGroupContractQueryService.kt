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
 * P04 Week1: JDBC Read 実装
 * - Profile "jdbc" で有効化
 * - SqlLoader を使用して SQL を読み込み
 * - PaginationOffsetLimit を使用してページネーション
 *
 * Bean競合回避:
 * - jdbc プロファイルでのみ有効化する（JPA MIN は残す）
 *
 */
@Profile("jdbc")
@Service
class JdbcGroupContractQueryService(
    private val jdbc: NamedParameterJdbcTemplate,
    private val sqlLoader: SqlLoader,
) : GroupContractQueryService {

    private val rowMapper = GroupContractRowMapper()

    override fun search(
        corporationId: CorporationId,
        personId: PersonId?,
        page: Int,
        size: Int
    ): PaginatedResult<GroupContractDto> {
        val pagination = PaginationOffsetLimit.of(page = page, size = size)

        val searchSql = sqlLoader.load("group/group_contract_search.sql")
        val countSql = sqlLoader.load("group/group_contract_count.sql")

        val params = mapOf(
            "corporationId" to corporationId.value,
            "personId" to personId?.value,
            "offset" to pagination.offset,
            "limit" to pagination.limit,
        )

        val content = jdbc.query(searchSql, params, rowMapper)
        val total = jdbc.queryForObject(countSql, params, Long::class.java) ?: 0L
        val totalPages = if (total > 0) ((total + size - 1) / size).toInt() else 0

        return PaginatedResult(
            content = content,
            totalElements = total,
            totalPages = totalPages,
            page = page,
            size = size,
        )
    }
}