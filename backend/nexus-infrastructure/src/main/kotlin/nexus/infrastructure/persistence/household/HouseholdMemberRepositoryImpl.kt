package nexus.infrastructure.persistence.household

import nexus.core.id.PersonId
import nexus.household.member.entity.HouseholdMember
import nexus.household.member.repository.HouseholdMemberRepository
import nexus.infrastructure.persistence.jpa.JpaHouseholdMemberRepository
import org.springframework.stereotype.Repository

@Repository
class HouseholdMemberRepositoryImpl(
    private val jpa: JpaHouseholdMemberRepository
) : HouseholdMemberRepository {

    override fun save(member: HouseholdMember): HouseholdMember =
        jpa.save(member)

    override fun findActiveByPersonId(personId: PersonId): List<HouseholdMember> =
        jpa.findByPersonIdAndLeftAtIsNull(personId.value)
}
