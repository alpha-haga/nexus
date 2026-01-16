package nexus.group.query

import nexus.core.id.CorporationId
import nexus.core.id.GojoContractId
import nexus.core.id.PersonId
import java.time.LocalDate

/**
 * 法人横断契約一覧DTO（Read Only）
 *
 * integration DB から取得する契約情報
 * Entity ではなく DTO として定義（一覧専用）
 */
data class GroupContractDto(
    val id: GojoContractId,
    val corporationId: CorporationId,
    val contractorPersonId: PersonId,
    val beneficiaryPersonId: PersonId?,
    val planCode: String,
    val planName: String,
    val monthlyFee: Long,
    val maturityAmount: Long,
    val contractDate: LocalDate,
    val maturityDate: LocalDate?,
    val status: String
)