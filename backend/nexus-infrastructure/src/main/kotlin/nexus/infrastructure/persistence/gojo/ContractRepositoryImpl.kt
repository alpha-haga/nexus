package nexus.infrastructure.persistence.gojo

import nexus.core.id.CorporationId
import nexus.core.id.GojoContractId
import nexus.core.id.PersonId
import nexus.gojo.contract.entity.Contract
import nexus.gojo.contract.entity.ContractStatus
import nexus.gojo.contract.repository.ContractRepository
import nexus.infrastructure.persistence.jpa.JpaContractRepository
import org.springframework.stereotype.Repository

/**
 * ContractRepository の実装（JPA）
 *
 * domain 層の interface を実装し、JPA への橋渡しを行う
 * ValueObject ID を String に変換する責務は infrastructure 層に閉じ込める
 */
@Repository
class ContractRepositoryImpl(
    private val jpa: JpaContractRepository
) : ContractRepository {

    override fun save(contract: Contract): Contract =
        jpa.save(contract)

    override fun findById(id: GojoContractId): Contract? =
        jpa.findById(id.value).orElse(null)

    override fun findByCorporationId(corporationId: CorporationId): List<Contract> =
        jpa.findByCorporationId(corporationId.value)

    override fun findByContractorPersonId(personId: PersonId): List<Contract> =
        jpa.findByContractorPersonId(personId.value)

    override fun findByBeneficiaryPersonId(personId: PersonId): List<Contract> =
        jpa.findByBeneficiaryPersonId(personId.value)

    override fun findByContractorPersonIdAndStatus(
        personId: PersonId,
        status: ContractStatus
    ): List<Contract> =
        jpa.findByContractorPersonIdAndStatus(personId.value, status)

    override fun findByCorporationIdAndStatus(
        corporationId: CorporationId,
        status: ContractStatus
    ): List<Contract> =
        jpa.findByCorporationIdAndStatus(corporationId.value, status)
}