package nexus.household.member.dto

import nexus.core.id.CorporationId

/**
 * 世帯作成コマンド
 */
data class CreateHouseholdCommand(
    val corporationId: CorporationId,
    val name: String? = null,
    val postalCode: String? = null,
    val prefecture: String? = null,
    val city: String? = null,
    val street: String? = null,
    val building: String? = null
)
