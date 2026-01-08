package nexus.household.member.repository

import nexus.household.member.entity.Household
import nexus.household.member.entity.HouseholdMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface HouseholdRepository : JpaRepository<Household, String> {

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

@Repository
interface HouseholdMemberRepository : JpaRepository<HouseholdMember, Long> {

    fun findByPersonIdAndLeftAtIsNull(personId: String): List<HouseholdMember>
}
