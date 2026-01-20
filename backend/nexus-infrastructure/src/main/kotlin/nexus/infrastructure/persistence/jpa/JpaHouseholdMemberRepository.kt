package nexus.infrastructure.persistence.jpa

import nexus.household.member.entity.HouseholdMember
import org.springframework.data.jpa.repository.JpaRepository

interface JpaHouseholdMemberRepository : JpaRepository<HouseholdMember, Long> {
    fun findByPersonId(personId: String): List<HouseholdMember>

    // “active” の定義(left_at is null)をJPAで表現
    fun findByPersonIdAndLeftAtIsNull(personId: String): List<HouseholdMember>
}