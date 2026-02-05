package nexus.infrastructure.group.query

import nexus.group.query.CompanyDto
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

/**
 * CompanyDto の RowMapper
 *
 * SQL alias (snake_case) を直接読む。
 * - 命名ルール: SQL alias は lower_snake_case
 * - 変換は RowMapper で明示的に行う（自動マッピング禁止）
 */
class CompanyRowMapper : RowMapper<CompanyDto> {
    override fun mapRow(rs: ResultSet, rowNum: Int): CompanyDto =
        CompanyDto(
            cmpCd = requireNotNull(rs.getString("cmp_cd")) {
                "cmp_cd is null (row=$rowNum)"
            },
            cmpShortNm = requireNotNull(rs.getString("cmp_short_nm")) {
                "cmp_short_nm is null (row=$rowNum)"
            },
            cmpNm = rs.getString("cmp_nm"),
            regionCd = rs.getString("region_cd")
        )
}
