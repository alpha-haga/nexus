package nexus.gojo.contract.repository

import nexus.gojo.contract.entity.Contract
import nexus.gojo.contract.entity.ContractStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ContractRepository : JpaRepository<Contract, String> {

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
