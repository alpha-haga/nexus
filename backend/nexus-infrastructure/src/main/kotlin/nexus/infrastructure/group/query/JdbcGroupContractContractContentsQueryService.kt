package nexus.infrastructure.group.query

import nexus.group.query.GroupContractContractContentsDto
import nexus.group.query.GroupContractContractContentsQueryService
import nexus.infrastructure.jdbc.SqlLoader
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service

/**
 * 契約内容 QueryService（JDBC 実装）
 *
 * P2-7: 契約内容サブリソースの実装
 * - Profile "jdbc" で有効化
 * - SQL から取得した列を attributes (Map) に変換
 */
@Profile("jdbc")
@Service
class JdbcGroupContractContractContentsQueryService(
    @Qualifier("integrationJdbcTemplate")
    private val jdbc: NamedParameterJdbcTemplate,
    private val sqlLoader: SqlLoader,
) : GroupContractContractContentsQueryService {

    private val rowMapper = GroupContractContractContentsRowMapper()

    override fun getContractContents(cmpCd: String, contractNo: String): GroupContractContractContentsDto? {
        // SQL を読み込み
        val sql = sqlLoader.load("group/group_contract_contract_contents.sql")

        // パラメータを構築
        val params = mapOf(
            "cmpCd" to cmpCd,
            "contractNo" to contractNo
        )

        // 1件取得（Fail Fast: 0件→null、1件→OK、複数件→例外）
        val results = jdbc.query(sql, params, rowMapper)
        return when (results.size) {
            0 -> null
            1 -> results[0]
            else -> throw IllegalStateException("GroupContractContractContents returned ${results.size} rows for $cmpCd/$contractNo")
        }
    }
}
