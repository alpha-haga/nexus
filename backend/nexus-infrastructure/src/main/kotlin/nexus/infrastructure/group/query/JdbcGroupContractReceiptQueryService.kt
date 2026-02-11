package nexus.infrastructure.group.query

import nexus.group.query.GroupContractReceiptDto
import nexus.group.query.GroupContractReceiptQueryService
import nexus.infrastructure.jdbc.SqlLoader
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service

/**
 * 入金情報 QueryService（JDBC 実装）
 *
 * P2-9: 入金情報サブリソースの実装
 * - Profile "jdbc" で有効化
 * - SQL から取得した入金情報を DTO に変換
 * - 複数行想定（0件→emptyList()）
 */
@Profile("jdbc")
@Service
class JdbcGroupContractReceiptQueryService(
    private val jdbc: NamedParameterJdbcTemplate,
    private val sqlLoader: SqlLoader,
) : GroupContractReceiptQueryService {

    private val rowMapper = GroupContractReceiptRowMapper()

    override fun getReceipts(cmpCd: String, contractNo: String): GroupContractReceiptDto {
        // SQL を読み込み
        val sql = sqlLoader.load("group/group_contract_receipts.sql")

        // パラメータを構築
        val params = mapOf(
            "cmpCd" to cmpCd,
            "contractNo" to contractNo
        )

        // 複数行取得（0件→emptyList()）
        val receipts = jdbc.query(sql, params, rowMapper)

        return GroupContractReceiptDto(
            cmpCd = cmpCd,
            contractNo = contractNo,
            receipts = receipts
        )
    }
}
