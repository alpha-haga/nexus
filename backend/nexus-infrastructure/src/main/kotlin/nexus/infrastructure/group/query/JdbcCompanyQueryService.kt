package nexus.infrastructure.group.query

import nexus.group.query.CompanyDto
import nexus.group.query.CompanyQueryService
import nexus.infrastructure.jdbc.SqlLoader
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service

/**
 * 法人マスタ一覧 QueryService（JDBC 実装）
 *
 * @Profile("jdbc") で有効化
 */
@Profile("jdbc")
@Service
class JdbcCompanyQueryService(
    private val jdbc: NamedParameterJdbcTemplate,
    private val sqlLoader: SqlLoader,
) : CompanyQueryService {

    private val rowMapper = CompanyRowMapper()

    override fun findAll(): List<CompanyDto> {
        val sql = sqlLoader.load("group/company_master_list.sql")
        return jdbc.query(sql, emptyMap<String, Any>(), rowMapper)
    }
}
