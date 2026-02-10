package nexus.infrastructure.group.query

import nexus.group.query.GroupContractStaffDto
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

/**
 * GroupContractStaffDto.Staff の RowMapper
 *
 * P2-7: SQL から担当者情報を取得
 * - SQL alias (snake_case) を直接読む
 * - role, role_label, bosyu_cd, staff_name を固定で読み DTO に詰める
 * - 推測ロジックは禁止（SQL で確定した列を読むだけ）
 */
class GroupContractStaffRowMapper : RowMapper<GroupContractStaffDto.Staff> {
    override fun mapRow(rs: ResultSet, rowNum: Int): GroupContractStaffDto.Staff {
        val role = requireNotNull(rs.getString("role")) {
            "role is null (row=$rowNum)"
        }
        val roleLabel = requireNotNull(rs.getString("role_label")) {
            "role_label is null (row=$rowNum)"
        }

        return GroupContractStaffDto.Staff(
            role = role,
            roleLabel = roleLabel,
            bosyuCd = rs.getString("bosyu_cd"),
            staffName = rs.getString("staff_name")
        )
    }
}
