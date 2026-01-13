package nexus.gojo.contract.repository

import nexus.core.db.DbContext
import nexus.core.id.GojoContractId
import nexus.core.id.PersonId
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
class ContractJdbcRepository(
    private val dbConnectionProvider: DbConnectionProvider
) {

    /**
     * 契約IDで契約を取得
     *
     * @param contractId 契約ID
     * @param regionId 地区ID（必須）
     * @return 契約情報（存在しない場合は null）
     */
    fun findById(contractId: GojoContractId, regionId: String): ContractRow? {
        // 地区DB への接続を取得
        val context = DbContext.forRegion(regionId)

        return dbConnectionProvider.getConnection(context).use { conn ->
            // TODO: 実際のSQL検索を実装
            //
            // val sql = """
            //     SELECT
            //         c.contract_id,
            //         c.corporation_id,
            //         c.contractor_person_id,
            //         c.beneficiary_person_id,
            //         c.status,
            //         c.total_paid_amount,
            //         c.maturity_amount,
            //         c.created_at,
            //         c.updated_at
            //     FROM gojo_contracts c
            //     WHERE c.contract_id = ?
            // """
            //
            // val ps = conn.prepareStatement(sql)
            // ps.setString(1, contractId.value)
            // val rs = ps.executeQuery()
            // if (rs.next()) { ... }
            null
        }
    }

    /**
     * 契約者（人物ID）で契約一覧を取得
     *
     * @param personId 契約者の人物ID
     * @param regionId 地区ID（必須）
     * @return 契約一覧
     */
    fun findByContractorPersonId(personId: PersonId, regionId: String): List<ContractRow> {
        val context = DbContext.forRegion(regionId)

        return dbConnectionProvider.getConnection(context).use { conn ->
            // TODO: 実際のSQL検索を実装
            emptyList()
        }
    }

    /**
     * 契約を登録
     *
     * @param contract 契約データ
     * @param regionId 地区ID（必須）
     * @return 登録された契約ID
     */
    fun insert(contract: ContractRow, regionId: String): GojoContractId {
        val context = DbContext.forRegion(regionId)

        return dbConnectionProvider.getConnection(context).use { conn ->
            // TODO: 実際のINSERT処理を実装
            //
            // val sql = """
            //     INSERT INTO gojo_contracts (
            //         contract_id,
            //         corporation_id,
            //         contractor_person_id,
            //         beneficiary_person_id,
            //         status,
            //         total_paid_amount,
            //         maturity_amount,
            //         created_at,
            //         updated_at
            //     ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            // """
            //
            // conn.prepareStatement(sql).use { ps ->
            //     ps.setString(1, contract.contractId)
            //     ...
            //     ps.executeUpdate()
            // }
            GojoContractId.generate()
        }
    }

    /**
     * 契約ステータスを更新
     *
     * @param contractId 契約ID
     * @param newStatus 新しいステータス
     * @param regionId 地区ID（必須）
     * @return 更新件数
     */
    fun updateStatus(contractId: GojoContractId, newStatus: String, regionId: String): Int {
        val context = DbContext.forRegion(regionId)

        return dbConnectionProvider.getConnection(context).use { conn ->
            // TODO: 実際のUPDATE処理を実装
            0
        }
    }

    /**
     * 地区DB への接続テスト
     *
     * @param regionId 地区ID
     * @return 接続成功なら true
     */
    fun testConnection(regionId: String): Boolean {
        val context = DbContext.forRegion(regionId)

        return try {
            dbConnectionProvider.getConnection(context).use { conn ->
                // H2/PostgreSQL: "SELECT 1", Oracle: "SELECT 1 FROM DUAL"
                conn.prepareStatement("SELECT 1").use { ps ->
                    ps.executeQuery().use { rs ->
                        rs.next()
                    }
                }
            }
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * 契約データ行
 *
 * JDBC ResultSet からのマッピング用
 * Entity とは別に定義（JPA Entity への依存を避ける）
 */
data class ContractRow(
    val contractId: String,
    val corporationId: String,
    val contractorPersonId: String,
    val beneficiaryPersonId: String?,
    val status: String,
    val totalPaidAmount: Long,
    val maturityAmount: Long
)
