package nexus.infrastructure.persistence.jpa

import nexus.household.member.entity.Household
import nexus.household.member.entity.HouseholdMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * JPA用 Household Repository
 *
 * infrastructure 層に閉じ込め、domain からは見えない
 * domain 層の HouseholdRepository interface の実装で使用
 */
@Repository
interface JpaHouseholdRepository : JpaRepository<Household, String> {

    fun findByCorporationId(corporationId: String): List<Household>

    fun findByHeadPersonId(headPersonId: String): Household?

    @Query("""
        SELECT h FROM Household h
        JOIN h.members m
        WHERE m.personId = :personId
        AND m.leftAt IS NULL
    """)
    fun findByMemberPersonId(personId: String): List<Household>
}

/**
 * JPA用 HouseholdMember Repository
 */
@Repository
interface JpaHouseholdMemberRepository : JpaRepository<HouseholdMember, Long> {

    fun findByPersonIdAndLeftAtIsNull(personId: String): List<HouseholdMember>
}
