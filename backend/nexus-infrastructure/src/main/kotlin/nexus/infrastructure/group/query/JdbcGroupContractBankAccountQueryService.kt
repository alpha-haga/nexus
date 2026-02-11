package nexus.infrastructure.group.query

import nexus.group.query.GroupContractBankAccountDto
import nexus.group.query.GroupContractBankAccountQueryService
import nexus.infrastructure.jdbc.SqlLoader
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service

/**
 * 口座情報 QueryService（JDBC 実装）
 *
 * P2-8: 口座情報サブリソースの実装
 * - Profile "jdbc" で有効化
 * - SQL から取得した口座情報を DTO に変換
 * - 1行想定（0件→null、1件→OK、複数件→例外）
 */
@Profile("jdbc")
@Service
class JdbcGroupContractBankAccountQueryService(
    private val jdbc: NamedParameterJdbcTemplate,
    private val sqlLoader: SqlLoader,
) : GroupContractBankAccountQueryService {

    private val rowMapper = GroupContractBankAccountRowMapper()

    override fun getBankAccount(cmpCd: String, contractNo: String): GroupContractBankAccountDto? {
        // SQL を読み込み
        val sql = sqlLoader.load("group/group_contract_bank_account.sql")

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
            else -> throw IllegalStateException("GroupContractBankAccount returned ${results.size} rows for $cmpCd/$contractNo")
        }
    }
}
