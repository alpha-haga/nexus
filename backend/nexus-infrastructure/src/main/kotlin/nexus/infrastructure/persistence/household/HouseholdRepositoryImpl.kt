package nexus.infrastructure.persistence.household

import nexus.core.id.CorporationId
import nexus.core.id.HouseholdId
import nexus.core.id.PersonId
import nexus.household.member.entity.Household
import nexus.household.member.repository.HouseholdRepository
import nexus.infrastructure.persistence.jpa.JpaHouseholdRepository
import nexus.infrastructure.persistence.jpa.JpaHouseholdMemberRepository
import org.springframework.stereotype.Repository

@Repository
class HouseholdRepositoryImpl(
    private val jpa: JpaHouseholdRepository,
    private val memberJpa: JpaHouseholdMemberRepository
) : HouseholdRepository {

    override fun save(household: Household): Household =
        jpa.save(household)

    override fun findById(householdId: HouseholdId): Household? =
        jpa.findById(householdId.value).orElse(null)

    override fun findByCorporationId(corporationId: CorporationId): List<Household> =
        jpa.findByCorporationId(corporationId.value)

    override fun findByHeadPersonId(personId: PersonId): Household? =
        jpa.findByHeadPersonId(personId.value)

    override fun findByMemberPersonId(personId: PersonId): List<Household> {
        val members = memberJpa.findByPersonIdAndLeftAtIsNull(personId.value)
    
        val householdIds = members
            .map { it.household.id }
            .distinct()
    
        if (householdIds.isEmpty()) return emptyList()
    
        return jpa.findAllById(householdIds)
    }
}
