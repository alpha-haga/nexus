package nexus.infrastructure.identity.query

import nexus.core.auth.Company
import nexus.identity.query.CompanyMasterQueryService
import nexus.infrastructure.jdbc.SqlLoader
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service

/**
 * 法人マスタ QueryService（JDBC 実装）
 * 
 * Integration DB の NXCM_COMPANY テーブルから取得
 */
@Profile("jdbc")
@Service
class JdbcCompanyMasterQueryService(
    @Qualifier("integrationJdbcTemplate")
    private val jdbc: NamedParameterJdbcTemplate,
    private val sqlLoader: SqlLoader,
) : CompanyMasterQueryService {

    private val rowMapper = CompanyMasterRowMapper()

    override fun findByRegionAndCompany(regionCd: String, companyCd: String): Company? {
        val sql = sqlLoader.load("group/company_master_by_region_company.sql")
        val params = mapOf(
            "regionCd" to regionCd.lowercase(),
            "companyCd" to companyCd.lowercase()
        )
        val results = jdbc.query(sql, params, rowMapper)
        return results.firstOrNull()
    }

    override fun findByCmpCd(cmpCd: String): Company? {
        val sql = sqlLoader.load("group/company_master_by_cmp_cd.sql")
        val params = mapOf("cmpCd" to cmpCd)
        val results = jdbc.query(sql, params, rowMapper)
        return results.firstOrNull()
    }

    override fun findAllActive(): List<Company> {
        val sql = sqlLoader.load("group/company_master_active_list.sql")
        return jdbc.query(sql, emptyMap<String, Any>(), rowMapper)
    }
}
