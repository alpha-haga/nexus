package nexus.infrastructure.group.query

import nexus.group.query.GroupContractContractContentsDto
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

/**
 * GroupContractContractContentsDto の RowMapper
 *
 * P2-7: SQL の列を attributes (Map) に変換
 * - SQL alias (snake_case) を直接読む
 * - cmp_cd / contract_no を除く全列を attributes に格納
 * - 取得できない項目は null として扱う
 */
class GroupContractContractContentsRowMapper : RowMapper<GroupContractContractContentsDto> {
    override fun mapRow(rs: ResultSet, rowNum: Int): GroupContractContractContentsDto {
        val cmpCd = requireNotNull(rs.getString("cmp_cd")) {
            "cmp_cd is null (row=$rowNum)"
        }
        val contractNo = requireNotNull(rs.getString("contract_no")) {
            "contract_no is null (row=$rowNum)"
        }

        // SQL のすべての列を attributes に変換（cmp_cd / contract_no を除く）
        val attributes = mutableMapOf<String, String?>()
        val metaData = rs.metaData
        val columnCount = metaData.columnCount
        for (i in 1..columnCount) {
            val columnName = metaData.getColumnLabel(i)
            // cmp_cd と contract_no は除外（DTO の直接フィールドとして扱う）
            if (columnName != "cmp_cd" && columnName != "contract_no") {
                val value = rs.getString(i)
                attributes[columnName] = value
            }
        }

        return GroupContractContractContentsDto(
            cmpCd = cmpCd,
            contractNo = contractNo,
            attributes = attributes
        )
    }
}
