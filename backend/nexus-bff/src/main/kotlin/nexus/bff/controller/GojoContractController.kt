package nexus.bff.controller

import nexus.bff.dto.gojo.ContractStatusDto
import nexus.bff.query.gojo.GojoContractQueryFacade
import nexus.bff.query.gojo.GojoContractReadModel
import nexus.core.pagination.PaginatedResult
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
    private val gojoContractQueryFacade: GojoContractQueryFacade
) {

    @GetMapping("/local")
    fun listLocal(
        @RequestParam(required = true) regionId: String,
        @RequestParam(required = true) page: Int,
        @RequestParam(required = true) size: Int
    ): ResponseEntity<PaginatedContractResponse> {
        val result = gojoContractQueryFacade.listLocal(regionId, page, size)
        return ResponseEntity.ok(result.toResponse())
    }

    @GetMapping("/all")
    fun listAll(
        @RequestParam(required = true) regionId: String,
        @RequestParam(required = false) corporationId: String?,
        @RequestParam(required = true) page: Int,
        @RequestParam(required = true) size: Int
    ): ResponseEntity<PaginatedContractResponse> {
        val result = gojoContractQueryFacade.listAll(regionId, corporationId, page, size)
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
    val status: ContractStatusDto,
    val totalPaidAmount: Long,
    val progressRate: Double,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

private fun PaginatedResult<GojoContractReadModel>.toResponse(): PaginatedContractResponse {
    return PaginatedContractResponse(
        content = content.map { it.toResponse() },
        totalElements = totalElements,
        totalPages = totalPages,
        page = page,
        size = size
    )
}

private fun GojoContractReadModel.toResponse(): ContractResponse {
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
