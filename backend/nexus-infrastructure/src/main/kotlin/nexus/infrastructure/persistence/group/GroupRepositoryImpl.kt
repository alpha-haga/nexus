package nexus.infrastructure.persistence.group

import nexus.core.db.DbContext
import nexus.core.pagination.PaginatedResult
import nexus.core.id.GojoContractId
import nexus.core.id.CorporationId
import nexus.core.id.PersonId
import nexus.group.query.GroupContractDto
import nexus.group.query.GroupSearchCriteria
import nexus.group.query.GroupSearchResult
import nexus.group.repository.GroupRepository
import nexus.infrastructure.db.DbConnectionProvider
import org.springframework.stereotype.Repository
import java.time.LocalDate

/**
 * 法人横断検索リポジトリ（JDBC実装）
 *
 * 統合DB（integration）のみを使用
 * - DbContext.forIntegration() で接続を取得
 * - 地区DBへの接続は禁止
 *
 * 重要:
 * - Read Only - 更新操作は提供しない
 * - 法人横断の参照専用DB
 */
@Repository
class GroupRepositoryImpl(
    private val dbConnectionProvider: DbConnectionProvider
) : GroupRepository {

    override fun searchPersons(criteria: GroupSearchCriteria): List<GroupSearchResult> {
        val context = DbContext.forIntegration()

        return dbConnectionProvider.getConnection(context).use { conn ->
            // TODO: 実際のSQL検索を実装
            emptyList()
        }
    }

    override fun findMergeCandidates(personId: PersonId): List<GroupSearchResult> {
        val context = DbContext.forIntegration()

        return dbConnectionProvider.getConnection(context).use { conn ->
            // TODO: 名寄せアルゴリズムを実装
            emptyList()
        }
    }

    override fun findContracts(corporationId: String?, page: Int, size: Int): PaginatedResult<GroupContractDto> {
        val context = DbContext.forIntegration()

        return dbConnectionProvider.getConnection(context).use { conn ->
            // 総件数を取得
            val countSql = if (corporationId != null) {
                "SELECT COUNT(*) FROM integration_gojo_contracts_v WHERE corporation_id = ?"
            } else {
                "SELECT COUNT(*) FROM integration_gojo_contracts_v"
            }

            val totalElements = conn.prepareStatement(countSql).use { ps ->
                if (corporationId != null) {
                    ps.setString(1, corporationId)
                }
                ps.executeQuery().use { rs ->
                    if (rs.next()) rs.getLong(1) else 0L
                }
            }

            // ページネーションでデータを取得（Oracle OFFSET/FETCH）
            val offset = page * size
            val dataSql = if (corporationId != null) {
                """
                    SELECT id, corporation_id, contractor_person_id, beneficiary_person_id,
                           plan_code, plan_name, monthly_fee, maturity_amount,
                           contract_date, maturity_date, status
                    FROM integration_gojo_contracts_v
                    WHERE corporation_id = ?
                    ORDER BY contract_date DESC, id
                    OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """.trimIndent()
            } else {
                """
                    SELECT id, corporation_id, contractor_person_id, beneficiary_person_id,
                           plan_code, plan_name, monthly_fee, maturity_amount,
                           contract_date, maturity_date, status
                    FROM integration_gojo_contracts_v
                    ORDER BY contract_date DESC, id
                    OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """.trimIndent()
            }

            val contracts = conn.prepareStatement(dataSql).use { ps ->
                if (corporationId != null) {
                    ps.setString(1, corporationId)
                    ps.setInt(2, offset)
                    ps.setInt(3, size)
                } else {
                    ps.setInt(1, offset)
                    ps.setInt(2, size)
                }
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(
                                GroupContractDto(
                                    id = GojoContractId(rs.getString("id")),
                                    corporationId = CorporationId(rs.getString("corporation_id")),
                                    contractorPersonId = PersonId(rs.getString("contractor_person_id")),
                                    beneficiaryPersonId = rs.getString("beneficiary_person_id")?.let { PersonId(it) },
                                    planCode = rs.getString("plan_code"),
                                    planName = rs.getString("plan_name"),
                                    monthlyFee = rs.getLong("monthly_fee"),
                                    maturityAmount = rs.getLong("maturity_amount"),
                                    contractDate = rs.getObject("contract_date", LocalDate::class.java),
                                    maturityDate = rs.getObject("maturity_date", LocalDate::class.java),
                                    status = rs.getString("status")
                                )
                            )
                        }
                    }
                }
            }

            val totalPages = if (totalElements > 0) {
                ((totalElements - 1) / size + 1).toInt()
            } else {
                0
            }

            PaginatedResult(
                content = contracts,
                totalElements = totalElements,
                totalPages = totalPages,
                page = page,
                size = size
            )
        }
    }
}