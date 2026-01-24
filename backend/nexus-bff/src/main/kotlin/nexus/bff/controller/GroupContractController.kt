package nexus.bff.controller

import nexus.core.exception.ValidationException
import nexus.core.id.CorporationId
import nexus.core.id.PersonId
import nexus.core.pagination.PaginatedResult
import nexus.group.query.GroupContractDto
import nexus.group.query.GroupContractQueryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

/**
 * 法人横断契約API
 *
 * P04: Read 導線（GroupContractQueryService）に統一する
 */
@RestController
@RequestMapping("/api/v1/group/contracts")
class GroupContractController(
    private val groupContractQueryService: GroupContractQueryService
) {

    @GetMapping
    fun search(
        @RequestParam(required = true) corporationId: String,
        @RequestParam(required = false) personId: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<GroupContractListResponse> {
        if (page < 0) throw ValidationException("page", "page must be >= 0")
        if (size <= 0) throw ValidationException("size", "size must be > 0")

        val result = groupContractQueryService.search(
            corporationId = CorporationId(corporationId),
            personId = personId?.let { PersonId(it) },
            page = page,
            size = size
        )

        return ResponseEntity.ok(result.toResponse())
    }
}

// Response DTOs

data class GroupContractListResponse(
    val content: List<GroupContractResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int
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

private fun PaginatedResult<GroupContractDto>.toResponse(): GroupContractListResponse =
    GroupContractListResponse(
        content = content.map { it.toResponse() },
        totalElements = totalElements,
        totalPages = totalPages,
        page = page,
        size = size
    )

private fun GroupContractDto.toResponse(): GroupContractResponse =
    GroupContractResponse(
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
