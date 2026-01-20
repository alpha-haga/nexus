package nexus.bff.query.gojo

import java.time.LocalDate
import java.time.LocalDateTime
import nexus.bff.dto.gojo.ContractStatusDto

data class GojoContractReadModel(
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
