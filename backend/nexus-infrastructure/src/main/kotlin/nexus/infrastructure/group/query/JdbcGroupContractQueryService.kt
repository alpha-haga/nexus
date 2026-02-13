package nexus.infrastructure.group.query

import nexus.core.pagination.PaginatedResult
import nexus.group.query.GroupContractSearchCondition
import nexus.group.query.GroupContractSearchDto
import nexus.group.query.GroupContractDetailDto
import nexus.group.query.GroupContractQueryService
import nexus.infrastructure.jdbc.SqlLoader
import nexus.infrastructure.jdbc.query.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 法人横断契約一覧 QueryService（JDBC 実装）
 *
 * P2-5: パフォーマンス改善対応（target/search の2段階SQL構造）
 * - Profile "jdbc" で有効化
 * - COUNT: target SQL を wrap して生成
 * - SELECT_PAGED: search SQL を wrap + where + orderBy + offset/fetch
 * - count/search の FROM/JOIN/WHERE を構造で一致させる
 * - ソートは whitelist で安全に処理
 * - 業務日付パラメータ化（businessYmd）: SYSDATE 直書きを廃止
 *
 * Bean競合回避:
 * - jdbc プロファイルでのみ有効化（JPA MIN は @Profile("!jdbc")）
 */
@Profile("jdbc")
@Service
class JdbcGroupContractQueryService(
    @Qualifier("integrationJdbcTemplate")
    private val jdbc: NamedParameterJdbcTemplate,
    private val sqlLoader: SqlLoader,
) : GroupContractQueryService {

    private val rowMapper = GroupContractRowMapper()
    private val detailRowMapper = GroupContractDetailRowMapper()

    // 業務日付フォーマッター（YYYYMMDD形式）
    private val businessYmdFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    // SqlQueryBuilder を構築（シングルトンとして保持）
    private val queryBuilder = SqlQueryBuilder(
        sqlLoader = sqlLoader,
        targetSqlPath = "group/group_contract_target.sql",
        searchSqlPath = "group/group_contract_search.sql",
        conditionApplier = GroupContractConditionApplier(),
        orderByBuilder = OrderByBuilder(
            allowed = mapOf(
                // 許可されたソートキー（APIパラメータ名、camelCase）
                // 既存SQLの列名: contract_receipt_ymd, contract_no
                // P1-B0では「業務合意待ち」だが、既存SQLを正として採用
                "contractReceiptYmd" to "contract_receipt_ymd",
                "contractNo" to "contract_no"
            ),
            defaultOrderBy = "ORDER BY contract_receipt_ymd DESC, contract_no",
            stableSecondaryOrderBy = "contract_no ASC"
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

        // 業務日付を取得（現時点は SYSDATE 相当）
        val businessYmd = LocalDate.now(ZoneId.of("Asia/Tokyo")).format(businessYmdFormatter)

        // Step 1: COUNT クエリを生成・実行
        val countQuery = queryBuilder.build(
            mode = QueryMode.COUNT,
            condition = condition,
            sortKey = null,
            sortDir = null,
            page = null
        )
        // businessYmd パラメータを追加
        val countParams = countQuery.params.toMutableMap()
        countParams["businessYmd"] = businessYmd
        val total = jdbc.queryForObject(countQuery.sql, countParams, Long::class.java) ?: 0L

        // Step 2: SELECT_PAGED クエリを生成・実行
        // 現状はソート固定（将来の動的ソート拡張に備えて sortKey/sortDir は null で固定）
        val searchQuery = queryBuilder.build(
            mode = QueryMode.SELECT_PAGED,
            condition = condition,
            sortKey = null, // 現状はデフォルトソートを使用
            sortDir = null, // 現状はデフォルトソートを使用
            page = pageSpec
        )
        // businessYmd パラメータを追加
        val searchParams = searchQuery.params.toMutableMap()
        searchParams["businessYmd"] = businessYmd
        val content = jdbc.query(searchQuery.sql, searchParams, rowMapper)

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

    override fun findDetail(
        cmpCd: String,
        contractNo: String
    ): GroupContractDetailDto? {
        // SQL を読み込み
        val sql = sqlLoader.load("group/group_contract_detail.sql")

        // パラメータを構築
        val params = mapOf(
            "cmpCd" to cmpCd,
            "contractNo" to contractNo
        )

        // 1件取得（Fail Fast: 0件→null、1件→OK、複数件→例外）
        val results = jdbc.query(sql, params, detailRowMapper)
        return when (results.size) {
            0 -> null
            1 -> results[0]
            else -> throw IllegalStateException("GroupContractDetail returned ${results.size} rows for $cmpCd/$contractNo")
        }
    }
}