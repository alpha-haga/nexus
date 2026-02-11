package nexus.infrastructure.group.query

import nexus.group.query.GroupContractActivitysDto
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

/**
 * GroupContractActivitysDto.Activity の RowMapper
 *
 * P2-10: SQL から対応履歴を取得
 * - SQL alias (snake_case) を直接読む
 * - 取得できない項目は null として扱う
 * - SQL の列順に準拠
 */
class GroupContractActivitysRowMapper : RowMapper<GroupContractActivitysDto.Activity> {
    override fun mapRow(rs: ResultSet, rowNum: Int): GroupContractActivitysDto.Activity {
        return GroupContractActivitysDto.Activity(
            recNo = rs.getString("rec_no"),
            serviceYmd = rs.getString("service_ymd"),
            serviceKbn = rs.getString("service_kbn"),
            serviceName = rs.getString("service_name"),
            serviceMethod = rs.getString("service_method"),
            serviceMethodName = rs.getString("service_method_name"),
            visitReasonKbn = rs.getString("visit_reason_kbn"),
            visitReasonName = rs.getString("visit_reason_name"),
            callStatusKbn = rs.getString("call_status_kbn"),
            callStatusName = rs.getString("call_status_name"),
            receptionPsnNm = rs.getString("reception_psn_nm"),
            freeComment = rs.getString("free_comment"),
            responsibleFamilyName = rs.getString("responsible_family_name"),
            responsibleFirstName = rs.getString("responsible_first_name"),
            responsibleSectName = rs.getString("responsible_sect_name"),
        )
    }
}
