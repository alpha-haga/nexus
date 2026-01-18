package nexus.gojo.contract.repository

import nexus.core.id.CorporationId
import nexus.core.id.GojoContractId
import nexus.core.id.PersonId
import nexus.gojo.contract.entity.Contract
import nexus.gojo.contract.entity.ContractStatus

interface ContractRepository {
    fun save(contract: Contract): Contract
    fun findById(id: GojoContractId): Contract?

    fun findByCorporationId(corporationId: CorporationId): List<Contract>
    fun findByContractorPersonId(personId: PersonId): List<Contract>
    fun findByBeneficiaryPersonId(personId: PersonId): List<Contract>

    fun findByContractorPersonIdAndStatus(
        personId: PersonId,
        status: ContractStatus
    ): List<Contract>

    fun findByCorporationIdAndStatus(
        corporationId: CorporationId,
        status: ContractStatus
    ): List<Contract>
}