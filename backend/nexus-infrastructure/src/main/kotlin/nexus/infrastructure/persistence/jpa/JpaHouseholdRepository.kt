package nexus.infrastructure.persistence.jpa

import nexus.household.member.entity.Household
import org.springframework.data.jpa.repository.JpaRepository

interface JpaHouseholdRepository : JpaRepository<Household, String> {

    fun findByCorporationId(corporationId: String): List<Household>

    fun findByHeadPersonId(headPersonId: String): Household?
}
