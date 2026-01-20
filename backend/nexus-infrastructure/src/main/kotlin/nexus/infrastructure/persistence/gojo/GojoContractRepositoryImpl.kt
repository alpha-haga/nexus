package nexus.infrastructure.persistence.gojo

import nexus.core.id.GojoContractId
import nexus.core.id.PersonId
import nexus.core.pagination.PaginatedResult
import nexus.gojo.contract.entity.Contract
import nexus.gojo.contract.entity.ContractStatus
import nexus.gojo.contract.repository.GojoContractRepository
import nexus.infrastructure.persistence.jpa.JpaContractRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * GojoContractRepository の実装（JPA）
 *
 * - domain 層 interface を実装し、JPA への橋渡しを行う
 * - regionId による DB ルーティングは P0-3 で導入（現時点では未使用）
 */
@Repository
class GojoContractRepositoryImpl(
    private val jpa: JpaContractRepository
) : GojoContractRepository {

    @Transactional(readOnly = true)
    override fun findById(contractId: GojoContractId, regionId: String): Contract? {
        return jpa.findById(contractId.value).orElse(null)
    }

    @Transactional(readOnly = true)
    override fun findByContractorPersonId(personId: PersonId, regionId: String): List<Contract> {
        return jpa.findByContractorPersonId(personId.value)
    }

    @Transactional
    override fun save(contract: Contract, regionId: String): Contract {
        return jpa.save(contract)
    }

    @Transactional
    override fun updateStatus(contractId: GojoContractId, newStatus: String, regionId: String): Int {
        // Spring Data の update クエリが定義されていないため、取得→更新→保存で対応
        val contract = jpa.findById(contractId.value).orElse(null) ?: return 0
        contract.status = ContractStatus.valueOf(newStatus)
        jpa.save(contract)
        return 1
    }

    @Transactional(readOnly = true)
    override fun findByRegion(regionId: String, page: Int, size: Int): PaginatedResult<Contract> {
        val pageable = PageRequest.of(page, size)
        val pageResult = jpa.findAll(pageable)
        return pageResult.toPaginatedResult(page, size)
    }

    @Transactional(readOnly = true)
    override fun findAll(regionId: String, corporationId: String?, page: Int, size: Int): PaginatedResult<Contract> {
        // NOTE:
        // corporationId が指定されている場合、JpaContractRepository は Pageable 対応の findByCorporationId を持たないため、
        // ここでは一旦全件ページング（local 起動・疎通目的）。P0-3 でクエリ/ReadModel最適化を入れる。
        val pageable = PageRequest.of(page, size)
        val pageResult = jpa.findAll(pageable)
        return pageResult.toPaginatedResult(page, size)
    }

    private fun org.springframework.data.domain.Page<Contract>.toPaginatedResult(page: Int, size: Int): PaginatedResult<Contract> {
        return PaginatedResult(
            content = this.content,
            totalElements = this.totalElements,
            totalPages = this.totalPages,
            page = page,
            size = size
        )
    }
}
