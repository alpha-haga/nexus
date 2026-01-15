package nexus.infrastructure.persistence.jpa

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
