/*
 * TEMP DISABLED:
 * nexus-infrastructure は DB接続基盤に限定する方針のため、業務/横断の永続化実装を退避。
 * 後で各ドメイン側（例: nexus-jpa-infrastructure 等）へ移設する。
 */
package nexus.infrastructure._disabled.persistence.jpa

import nexus.gojo.contract.entity.Contract
import nexus.gojo.contract.entity.ContractStatus
import org.springframework.data.jpa.repository.JpaRepository

interface JpaContractRepository : JpaRepository<Contract, String> {
    fun findByCorporationId(corporationId: String): List<Contract>
    fun findByContractorPersonId(personId: String): List<Contract>
    fun findByBeneficiaryPersonId(personId: String): List<Contract>
    fun findByContractorPersonIdAndStatus(
        personId: String,
        status: ContractStatus
    ): List<Contract>
    fun findByCorporationIdAndStatus(
        corporationId: String,
        status: ContractStatus
    ): List<Contract>
}
