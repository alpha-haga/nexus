package nexus.infrastructure.group.query

import nexus.core.pagination.PaginatedResult
import nexus.group.query.GroupContractSearchCondition
import nexus.group.query.GroupContractSearchDto
import nexus.group.query.GroupContractQueryService
import nexus.infrastructure.jdbc.SqlLoader
import nexus.infrastructure.jdbc.query.*
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service

/**
 * 法人横断契約一覧 QueryService（JDBC 実装）
 *
 * P2-5: SqlQueryBuilder による動的WHERE化
 * - Profile "jdbc" で有効化
 * - SqlQueryBuilder を使用して SQL を動的生成
 * - NULL吸収目的の OR を廃止し、動的WHERE化
 * - count/search の FROM/JOIN/WHERE を構造で一致させる
 * - ソートは whitelist で安全に処理
 *
 * Bean競合回避:
 * - jdbc プロファイルでのみ有効化（JPA MIN は @Profile("!jdbc")）
 */
@Profile("jdbc")
@Service
class JdbcGroupContractQueryService(
    private val jdbc: NamedParameterJdbcTemplate,
    private val sqlLoader: SqlLoader,
) : GroupContractQueryService {

    private val rowMapper = GroupContractRowMapper()

    // SqlQueryBuilder を構築（シングルトンとして保持）
    private val queryBuilder = SqlQueryBuilder(
        sqlLoader = sqlLoader,
        baseSqlPath = "group/group_contract_base.sql",
        selectColumnsSqlPath = "group/group_contract_select_columns.sql",
        conditionApplier = GroupContractConditionApplier(),
        orderByBuilder = OrderByBuilder(
            allowed = mapOf(
                // 許可されたソートキー（APIパラメータ名、camelCase）
                // 既存SQLの列名: contract_receipt_ymd, contract_no
                // P1-B0では「業務合意待ち」だが、既存SQLを正として採用
                "contractReceiptYmd" to "contract_search.contract_receipt_ymd",
                "contractNo" to "contract_search.contract_no"
            ),
            defaultOrderBy = "ORDER BY contract_search.contract_receipt_ymd DESC, contract_search.contract_no",
            stableSecondaryOrderBy = "contract_search.contract_no ASC"
        )
    )

    override fun search(
        condition: GroupContractSearchCondition,
        page: Int,
        size: Int
    ): PaginatedResult<GroupContractSearchDto> {
        // ページネーション仕様を構築
        val pageSpec = PageSpec(
            offset = page * size,
            limit = size
        )

        // COUNT クエリを生成・実行
        val countQuery = queryBuilder.build(
            mode = QueryMode.COUNT,
            condition = condition,
            sortKey = null,
            sortDir = null,
            page = null
        )
        val total = jdbc.queryForObject(countQuery.sql, countQuery.params, Long::class.java) ?: 0L

        // SELECT クエリを生成・実行
        // 現状はソート固定（将来の動的ソート拡張に備えて sortKey/sortDir は null で固定）
        val searchQuery = queryBuilder.build(
            mode = QueryMode.SELECT_PAGED,
            condition = condition,
            sortKey = null, // 現状はデフォルトソートを使用
            sortDir = null, // 現状はデフォルトソートを使用
            page = pageSpec
        )
        val content = jdbc.query(searchQuery.sql, searchQuery.params, rowMapper)

        // ページ数計算
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