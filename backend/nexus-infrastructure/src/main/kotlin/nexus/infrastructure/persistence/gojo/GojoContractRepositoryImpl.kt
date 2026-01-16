package nexus.infrastructure.persistence.gojo

import nexus.gojo.contract.entity.ContractStatus
import nexus.core.db.DbContext
import nexus.core.pagination.PaginatedResult
import nexus.core.id.GojoContractId
import nexus.core.id.PersonId
import nexus.gojo.contract.entity.Contract
import nexus.gojo.contract.repository.GojoContractRepository
import nexus.infrastructure.db.DbConnectionProvider
import java.time.LocalDate
import org.springframework.stereotype.Repository
/**
 * 互助会契約リポジトリ（JDBC実装）
 *
 * 地区DB（REGION）を使用
 * - DbContext.forRegion(regionId) で接続を取得
 * - regionId は操作対象の地区を指定
 *
 * 重要:
 * - 統合DBへの接続は禁止（それは nexus-group の責務）
 * - 同一地区内の複数法人は同じDBを共有
 */
@Repository
class GojoContractRepositoryImpl(
    private val dbConnectionProvider: DbConnectionProvider
) : GojoContractRepository {

    override fun findById(contractId: GojoContractId, regionId: String): Contract? {
        val context = DbContext.forRegion(regionId)

        return dbConnectionProvider.getConnection(context).use { conn ->
            // TODO: 実際のSQL検索を実装
            // ContractRow から Contract Entity へのマッピング
            null
        }
    }

    override fun findByContractorPersonId(personId: PersonId, regionId: String): List<Contract> {
        val context = DbContext.forRegion(regionId)

        return dbConnectionProvider.getConnection(context).use { conn ->
            // TODO: 実際のSQL検索を実装
            emptyList()
        }
    }

    override fun save(contract: Contract, regionId: String): Contract {
        val context = DbContext.forRegion(regionId)

        return dbConnectionProvider.getConnection(context).use { conn ->
            // TODO: 実際のINSERT/UPDATE処理を実装
            contract
        }
    }

    override fun updateStatus(contractId: GojoContractId, newStatus: String, regionId: String): Int {
        val context = DbContext.forRegion(regionId)

        return dbConnectionProvider.getConnection(context).use { conn ->
            // TODO: 実際のUPDATE処理を実装
            0
        }
    }
    override fun findByRegion(regionId: String, page: Int, size: Int): PaginatedResult<Contract> {
        val context = DbContext.forRegion(regionId)

        return dbConnectionProvider.getConnection(context).use { conn ->
            // 総件数を取得
            val countSql = "SELECT COUNT(*) FROM gojo_contracts"
            val totalElements = conn.prepareStatement(countSql).use { ps ->
                ps.executeQuery().use { rs ->
                    if (rs.next()) rs.getLong(1) else 0L
                }
            }

            // ページネーションでデータを取得
            val offset = page * size
            val dataSql = """
                SELECT id, corporation_id, contractor_person_id, beneficiary_person_id,
                       plan_code, plan_name, monthly_fee, maturity_amount,
                       contract_date, maturity_date, status, created_at, updated_at, version
                FROM gojo_contracts
                ORDER BY created_at DESC
                LIMIT ? OFFSET ?
            """.trimIndent()

            val contracts = conn.prepareStatement(dataSql).use { ps ->
                ps.setInt(1, size)
                ps.setInt(2, offset)
                ps.executeQuery().use { rs ->
                    // TODO: ResultSet から Contract へのマッピングを実装
                    // ContractRow を使用してマッピング
                    emptyList<Contract>()
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

    override fun findAll(regionId: String, corporationId: String?, page: Int, size: Int): PaginatedResult<Contract> {
        val context = DbContext.forRegion(regionId)

        return dbConnectionProvider.getConnection(context).use { conn ->
            // 総件数を取得
            val countSql = if (corporationId != null) {
                "SELECT COUNT(*) FROM region_gojo_contracts_v WHERE corporation_id = ?"
            } else {
                "SELECT COUNT(*) FROM region_gojo_contracts_v"
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
                           contract_date, maturity_date, status, created_at, updated_at, version
                    FROM region_gojo_contracts_v
                    WHERE corporation_id = ?
                    ORDER BY contract_date DESC, id
                    OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """.trimIndent()
            } else {
                """
                    SELECT id, corporation_id, contractor_person_id, beneficiary_person_id,
                           plan_code, plan_name, monthly_fee, maturity_amount,
                           contract_date, maturity_date, status, created_at, updated_at, version
                    FROM region_gojo_contracts_v
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
                                Contract(
                                    id = rs.getString("id"),
                                    corporationId = rs.getString("corporation_id"),
                                    contractorPersonId = rs.getString("contractor_person_id"),
                                    beneficiaryPersonId = rs.getString("beneficiary_person_id"),
                                    planCode = rs.getString("plan_code"),
                                    planName = rs.getString("plan_name"),
                                    monthlyFee = rs.getLong("monthly_fee"),
                                    maturityAmount = rs.getLong("maturity_amount"),
                                    contractDate = rs.getObject("contract_date", LocalDate::class.java),
                                    maturityDate = rs.getObject("maturity_date", LocalDate::class.java),
                                    status = ContractStatus.valueOf(rs.getString("status")),
                                    createdAt = rs.getObject("created_at", java.time.LocalDateTime::class.java),
                                    updatedAt = rs.getObject("updated_at", java.time.LocalDateTime::class.java),
                                    version = rs.getLong("version")
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

/**
 * 契約データ行（JDBC用）
 *
 * JDBC ResultSet からのマッピング用
 * Entity とは別に定義（JPA Entity への依存を避ける）
 */
internal data class ContractRow(
    val contractId: String,
    val corporationId: String,
    val contractorPersonId: String,
    val beneficiaryPersonId: String?,
    val status: String,
    val totalPaidAmount: Long,
    val maturityAmount: Long
)