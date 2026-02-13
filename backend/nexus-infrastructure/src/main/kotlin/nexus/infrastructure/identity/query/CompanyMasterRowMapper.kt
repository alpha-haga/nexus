package nexus.infrastructure.identity.query

import nexus.core.auth.Company
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

/**
 * Company の RowMapper
 */
class CompanyMasterRowMapper : RowMapper<Company> {
    override fun mapRow(rs: ResultSet, rowNum: Int): Company =
        Company(
            cmpCd = requireNotNull(rs.getString("cmp_cd")) { "cmp_cd is null" },
            companyName = requireNotNull(rs.getString("company_name")) { "company_name is null" },
            companyNameShort = rs.getString("company_name_short"),
            regionCd = requireNotNull(rs.getString("region_cd")) { "region_cd is null" },
            companyCd = requireNotNull(rs.getString("company_cd")) { "company_cd is null" },
            availableDomains = rs.getString("available_domains")
                .split(",")
                .map { it.trim().uppercase() }
                .filter { it.isNotEmpty() }
                .toSet(),
            displayOrder = rs.getInt("display_order"),
            isActive = requireNotNull(rs.getString("is_active")) { "is_active is null" }
        )
}
