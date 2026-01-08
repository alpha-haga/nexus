package nexus.gojo.contract.dto

import nexus.core.id.CorporationId
import nexus.core.id.PersonId
import java.time.LocalDate

/**
 * 契約作成コマンド（内部用DTO）
 */
data class CreateContractCommand(
    val corporationId: CorporationId,
    val contractorPersonId: PersonId,
    val beneficiaryPersonId: PersonId? = null,
    val planCode: String,
    val planName: String,
    val monthlyFee: Long,
    val maturityAmount: Long,
    val contractDate: LocalDate = LocalDate.now()
)
