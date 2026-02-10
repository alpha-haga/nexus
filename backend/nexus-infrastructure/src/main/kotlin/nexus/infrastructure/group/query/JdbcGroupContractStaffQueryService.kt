package nexus.infrastructure.group.query

import nexus.group.query.GroupContractStaffDto
import nexus.group.query.GroupContractStaffQueryService
import nexus.infrastructure.jdbc.SqlLoader
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service

/**
 * 担当者情報 QueryService（JDBC 実装）
 *
 * P2-7: 担当者情報サブリソースの実装
 * - Profile "jdbc" で有効化
 * - SQL から取得した担当者情報を List<Staff> に変換
 * - 4行必ず返す前提（UNION ALL）
 */
@Profile("jdbc")
@Service
class JdbcGroupContractStaffQueryService(
    private val jdbc: NamedParameterJdbcTemplate,
    private val sqlLoader: SqlLoader,
) : GroupContractStaffQueryService {

    private val rowMapper = GroupContractStaffRowMapper()

    override fun getStaffs(cmpCd: String, contractNo: String): GroupContractStaffDto? {
        // SQL を読み込み
        val sql = sqlLoader.load("group/group_contract_staff.sql")

        // パラメータを構築
        val params = mapOf(
            "cmpCd" to cmpCd,
            "contractNo" to contractNo
        )

        // 複数行取得（0件→null、4件想定、想定外件数は例外）
        val results = jdbc.query(sql, params, rowMapper)
        return when {
            results.isEmpty() -> null
            results.size == 4 -> GroupContractStaffDto(
                cmpCd = cmpCd,
                contractNo = contractNo,
                staffs = results
            )
            else -> throw IllegalStateException("GroupContractStaff returned ${results.size} rows (expected 4) for $cmpCd/$contractNo")
        }
    }
}
