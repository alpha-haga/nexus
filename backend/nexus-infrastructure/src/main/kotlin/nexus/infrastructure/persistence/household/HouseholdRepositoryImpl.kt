package nexus.infrastructure.persistence.household

import nexus.core.id.CorporationId
import nexus.core.id.HouseholdId
import nexus.core.id.PersonId
import nexus.household.member.entity.Household
import nexus.household.member.entity.HouseholdMember
import nexus.household.member.repository.HouseholdMemberRepository
import nexus.household.member.repository.HouseholdRepository
import nexus.infrastructure.persistence.jpa.JpaHouseholdMemberRepository
import nexus.infrastructure.persistence.jpa.JpaHouseholdRepository
import org.springframework.stereotype.Repository

/**
 * HouseholdRepository の実装
 *
 * domain 層の interface を実装し、JPA への橋渡しを行う
 * domain 層はこの実装クラスを知らない（DI で注入）
 */
@Repository
class HouseholdRepositoryImpl(
    private val jpaRepository: JpaHouseholdRepository
) : HouseholdRepository {

    override fun save(household: Household): Household {
        return jpaRepository.save(household)
    }

    override fun findById(householdId: HouseholdId): Household? {
        return jpaRepository.findById(householdId.value).orElse(null)
    }

    override fun findByCorporationId(corporationId: CorporationId): List<Household> {
        return jpaRepository.findByCorporationId(corporationId.value)
    }

    override fun findByHeadPersonId(personId: PersonId): Household? {
        return jpaRepository.findByHeadPersonId(personId.value)
    }

    override fun findByMemberPersonId(personId: PersonId): List<Household> {
        return jpaRepository.findByMemberPersonId(personId.value)
    }
}

/**
 * HouseholdMemberRepository の実装
 */
@Repository
class HouseholdMemberRepositoryImpl(
    private val jpaRepository: JpaHouseholdMemberRepository
) : HouseholdMemberRepository {

    override fun save(member: HouseholdMember): HouseholdMember {
        return jpaRepository.save(member)
    }

    override fun findActiveByPersonId(personId: PersonId): List<HouseholdMember> {
        return jpaRepository.findByPersonIdAndLeftAtIsNull(personId.value)
    }
}
