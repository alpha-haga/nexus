package nexus.infrastructure.persistence.gojo

import nexus.gojo.contract.entity.Contract
import nexus.gojo.contract.entity.ContractStatus
import nexus.gojo.contract.repository.ContractRepository
import nexus.infrastructure.persistence.jpa.JpaContractRepository
import org.springframework.stereotype.Repository

@Repository
class ContractRepositoryImpl(
    private val jpa: JpaContractRepository
) : ContractRepository {

    override fun save(contract: Contract): Contract =
        jpa.save(contract)

    override fun findById(id: String): Contract? =
        jpa.findById(id).orElse(null)

    override fun findByCorporationId(corporationId: String): List<Contract> =
        jpa.findByCorporationId(corporationId)

    override fun findByContractorPersonId(personId: String): List<Contract> =
        jpa.findByContractorPersonId(personId)

    override fun findByBeneficiaryPersonId(personId: String): List<Contract> =
        jpa.findByBeneficiaryPersonId(personId)

    override fun findByContractorPersonIdAndStatus(
        personId: String,
        status: ContractStatus
    ): List<Contract> =
        jpa.findByContractorPersonIdAndStatus(personId, status)

    override fun findByCorporationIdAndStatus(
        corporationId: String,
        status: ContractStatus
    ): List<Contract> =
        jpa.findByCorporationIdAndStatus(corporationId, status)
}
