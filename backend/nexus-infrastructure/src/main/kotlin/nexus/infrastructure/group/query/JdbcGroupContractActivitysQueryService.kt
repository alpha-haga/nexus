package nexus.infrastructure.group.query

import nexus.group.query.GroupContractActivitysDto
import nexus.group.query.GroupContractActivitysQueryService
import nexus.infrastructure.jdbc.SqlLoader
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service

/**
 * 対応履歴 QueryService（JDBC 実装）
 *
 * P2-10: 対応履歴サブリソースの実装
 * - Profile "jdbc" で有効化
 * - SQL から取得した対応履歴を DTO に変換
 * - 複数行想定（0件→emptyList()）
 */
@Profile("jdbc")
@Service
class JdbcGroupContractActivitysQueryService(
    private val jdbc: NamedParameterJdbcTemplate,
    private val sqlLoader: SqlLoader,
) : GroupContractActivitysQueryService {

    private val rowMapper = GroupContractActivitysRowMapper()

    override fun getActivitys(cmpCd: String, contractNo: String): GroupContractActivitysDto {
        // SQL を読み込み
        val sql = sqlLoader.load("group/group_contract_activitys.sql")

        // パラメータを構築
        val params = mapOf(
            "cmpCd" to cmpCd,
            "contractNo" to contractNo
        )

        // 複数行取得（0件→emptyList()）
        val activities = jdbc.query(sql, params, rowMapper)

        return GroupContractActivitysDto(
            cmpCd = cmpCd,
            contractNo = contractNo,
            activities = activities
        )
    }
}
