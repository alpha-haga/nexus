package nexus.api.controller

import nexus.core.pagination.PaginatedResult
import nexus.group.query.GroupContractDto
import nexus.group.service.GroupQueryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

/**
 * 法人横断契約API
 */
@RestController
@RequestMapping("/api/v1/group/contracts")
class GroupContractController(
    private val groupQueryService: GroupQueryService
) {

    @GetMapping
    fun listContracts(
        @RequestParam(required = false) corporationId: String?,
        @RequestParam(required = true) page: Int,
        @RequestParam(required = true) size: Int
    ): ResponseEntity<GroupContractListResponse> {
        val result = groupQueryService.listContracts(corporationId, page, size)
        return ResponseEntity.ok(result.toResponse())
    }
}

// Response DTOs

data class GroupContractListResponse(
    val items: List<GroupContractResponse>,
    val page: Int,
    val size: Int,
    val total: Long
)

data class GroupContractResponse(
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
    val status: String
)

private fun PaginatedResult<GroupContractDto>.toResponse(): GroupContractListResponse {
    return GroupContractListResponse(
        items = content.map { it.toResponse() },
        page = page,
        size = size,
        total = totalElements
    )
}

private fun GroupContractDto.toResponse(): GroupContractResponse {
    return GroupContractResponse(
        id = id.value,
        corporationId = corporationId.value,
        contractorPersonId = contractorPersonId.value,
        beneficiaryPersonId = beneficiaryPersonId?.value,
        planCode = planCode,
        planName = planName,
        monthlyFee = monthlyFee,
        maturityAmount = maturityAmount,
        contractDate = contractDate,
        maturityDate = maturityDate,
        status = status
    )
}