package nexus.bff.query.gojo

import nexus.bff.dto.gojo.ContractStatusDto
import nexus.core.pagination.PaginatedResult
import nexus.gojo.contract.entity.Contract
import nexus.gojo.contract.service.ContractQueryService
import org.springframework.stereotype.Service

@Service
class GojoContractQueryFacade(
    private val contractQueryService: ContractQueryService
) {
    fun listLocal(regionId: String, page: Int, size: Int): PaginatedResult<GojoContractReadModel> {
        val result: PaginatedResult<Contract> = contractQueryService.listLocalContracts(regionId, page, size)
        return result.toReadModelResult()
    }

    fun listAll(regionId: String, corporationId: String?, page: Int, size: Int): PaginatedResult<GojoContractReadModel> {
        val result: PaginatedResult<Contract> = contractQueryService.listAllContracts(regionId, corporationId, page, size)
        return result.toReadModelResult()
    }

    private fun PaginatedResult<Contract>.toReadModelResult(): PaginatedResult<GojoContractReadModel> {
        // PaginatedResult に map が無い可能性があるため、ここでは明示変換に寄せる
        return PaginatedResult(
            content = this.content.map { it.toReadModel() },
            totalElements = this.totalElements,
            totalPages = this.totalPages,
            page = this.page,
            size = this.size
        )
    }

    private fun Contract.toReadModel(): GojoContractReadModel =
        GojoContractReadModel(
            id = this.id,
            corporationId = this.corporationId,
            contractorPersonId = this.contractorPersonId,
            beneficiaryPersonId = this.beneficiaryPersonId,
            planCode = this.planCode,
            planName = this.planName,
            monthlyFee = this.monthlyFee,
            maturityAmount = this.maturityAmount,
            contractDate = this.contractDate,
            maturityDate = this.maturityDate,
            status = this.status.toDto(),
            totalPaidAmount = this.totalPaidAmount,
            progressRate = this.progressRate,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )

    private fun nexus.gojo.contract.entity.ContractStatus.toDto(): ContractStatusDto =
        when (this) {
            nexus.gojo.contract.entity.ContractStatus.ACTIVE -> ContractStatusDto.ACTIVE
            nexus.gojo.contract.entity.ContractStatus.MATURED -> ContractStatusDto.MATURED
            nexus.gojo.contract.entity.ContractStatus.USED -> ContractStatusDto.USED
            nexus.gojo.contract.entity.ContractStatus.CANCELLED -> ContractStatusDto.CANCELLED
            nexus.gojo.contract.entity.ContractStatus.SUSPENDED -> ContractStatusDto.SUSPENDED
        }
}
