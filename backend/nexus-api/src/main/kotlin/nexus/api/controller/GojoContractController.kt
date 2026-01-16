package nexus.api.controller

import nexus.core.pagination.PaginatedResult
import nexus.gojo.contract.entity.Contract
import nexus.gojo.contract.entity.ContractStatus
import nexus.gojo.contract.service.ContractQueryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 互助会契約API
 */
@RestController
@RequestMapping("/api/v1/gojo/contracts")
class GojoContractController(
    private val contractQueryService: ContractQueryService
) {

    @GetMapping("/local")
    fun listLocal(
        @RequestParam(required = true) regionId: String,
        @RequestParam(required = true) page: Int,
        @RequestParam(required = true) size: Int
    ): ResponseEntity<PaginatedContractResponse> {
        val result = contractQueryService.listLocalContracts(regionId, page, size)
        return ResponseEntity.ok(result.toResponse())
    }

    @GetMapping("/all")
    fun listAll(
        @RequestParam(required = true) regionId: String,
        @RequestParam(required = false) corporationId: String?,
        @RequestParam(required = true) page: Int,
        @RequestParam(required = true) size: Int
    ): ResponseEntity<PaginatedContractResponse> {
        val result = contractQueryService.listAllContracts(regionId, corporationId, page, size)
        return ResponseEntity.ok(result.toResponse())
    }
}

// Response DTOs

data class PaginatedContractResponse(
    val content: List<ContractResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int
)

data class ContractResponse(
    val id: String,
    val corporationId: String,
    val contractorPersonId: String,
    val beneficiaryPersonId: String?,
    val planCode: String,
    val planName: String,
    val monthlyFee: Long,
    val maturityAmount: Long,
    val contractDate: LocalDate,
    val maturityDate: LocalDate?,
    val status: ContractStatus,
    val totalPaidAmount: Long,
    val progressRate: Double,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

private fun PaginatedResult<Contract>.toResponse(): PaginatedContractResponse {
    return PaginatedContractResponse(
        content = content.map { it.toResponse() },
        totalElements = totalElements,
        totalPages = totalPages,
        page = page,
        size = size
    )
}

private fun Contract.toResponse(): ContractResponse {
    return ContractResponse(
        id = id,
        corporationId = corporationId,
        contractorPersonId = contractorPersonId,
        beneficiaryPersonId = beneficiaryPersonId,
        planCode = planCode,
        planName = planName,
        monthlyFee = monthlyFee,
        maturityAmount = maturityAmount,
        contractDate = contractDate,
        maturityDate = maturityDate,
        status = status,
        totalPaidAmount = totalPaidAmount,
        progressRate = progressRate,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}