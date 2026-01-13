package nexus.infrastructure.persistence.gojo

import nexus.core.db.DbContext
import nexus.core.id.GojoContractId
import nexus.core.id.PersonId
import nexus.gojo.contract.entity.Contract
import nexus.gojo.contract.repository.GojoContractRepository
import nexus.infrastructure.db.DbConnectionProvider
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